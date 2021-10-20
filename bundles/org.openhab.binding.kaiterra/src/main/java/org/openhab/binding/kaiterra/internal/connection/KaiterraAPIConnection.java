/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.kaiterra.internal.connection;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.kaiterra.internal.KaiterraAPIHandler;
import org.openhab.binding.kaiterra.internal.config.KaiterraAPIBindingConfiguration;
import org.openhab.binding.kaiterra.internal.model.LaserEggCurrentData;
import org.openhab.core.cache.ExpiringCacheMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link KaiterraAPIConnection} is responsible for handling the REST connections to Kaiterra cloud API.
 *
 * @author Michał Boroński - Initial contribution
 */
@NonNullByDefault
public class KaiterraAPIConnection {

    private final Logger logger = LoggerFactory.getLogger(KaiterraAPIConnection.class);

    private static final String KAITERRA_API_URL = "https://api.origins-china.cn/v1/lasereggs/%DEVICE_UUID%?key=%API_KEY%";

    private final HttpClient httpClient;

    private final ExpiringCacheMap<String, String> cache;

    private final Gson gson = new Gson();
    private long cooldownTimer = 0;

    public KaiterraAPIConnection(KaiterraAPIHandler handler, HttpClient httpClient) {
        this.httpClient = httpClient;

        KaiterraAPIBindingConfiguration config = handler.getLaserEggBindingConfig();
        cache = new ExpiringCacheMap<>(TimeUnit.SECONDS.toMillis(config.getRefreshInterval()));
    }

    public synchronized @Nullable LaserEggCurrentData getLaserEggData(String apiKey, String deviceUUID)
            throws JsonSyntaxException, KaiterraAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(KAITERRA_API_URL, apiKey, deviceUUID)),
                LaserEggCurrentData.class);
    }

    private String buildURL(String url, String apiKey, String deviceUUID) {
        return url.replaceFirst("%DEVICE_UUID%", deviceUUID).replaceFirst("%API_KEY%", apiKey);
    }

    private @Nullable String getResponseFromCache(String url) {
        return cache.putIfAbsentAndGet(url, () -> getResponse(url));
    }

    private String getResponse(String url) {
        try {
            if (cooldownTimer > System.currentTimeMillis()) {
                logger.debug("Cooldown period is active, waiting Kaiterra cloud server responded with status code");
                throw new KaiterraAPIException(
                        "Cooldown period is active, waiting Kaiterra cloud server responded with status code");
            }

            ContentResponse contentResponse = httpClient.newRequest(url).method(GET).timeout(15, TimeUnit.SECONDS)
                    .send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            logger.trace("Kaiterra server response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case OK_200:
                    return content;
                case BAD_REQUEST_400:
                case UNAUTHORIZED_401:
                case NOT_FOUND_404:
                    logger.debug("Kaiterra server responded with status code {}", httpStatus);
                    throw new KaiterraAPIException(String.format("Error with status %d", httpStatus));
                case TOO_MANY_REQUESTS_429:
                    cooldownTimer = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
                default:
                    logger.debug("Kaiterra cloud server responded with status code {}", httpStatus);
                    throw new KaiterraAPIException(String.format("Error with status %d", httpStatus));
            }
        } catch (ExecutionException e) {
            @NonNull
            String errorMessage = StringUtils.defaultString(e.getLocalizedMessage(), "Unknown");
            logger.trace("Exception occurred during execution: {}", errorMessage, e);
            throw new KaiterraAPIException(errorMessage, e);
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new KaiterraAPIException(StringUtils.defaultString(e.getLocalizedMessage(), ""), e);
        } catch (Exception e) {
            logger.error("Unexpected exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new KaiterraAPIException(StringUtils.defaultString(e.getLocalizedMessage(), ""), e);
        }
    }
}
