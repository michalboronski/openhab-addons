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
package org.openhab.binding.kaiterra.internal.config;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KaiterraAPIBindingConfiguration} class defines common properties used by the binding
 *
 * @author Michał Boroński - Initial contribution
 */
@NonNullByDefault
public class KaiterraAPIBindingConfiguration {
    public static final String CONFIG_DEF_API_KEY = "apiKey";

    public static final String CONFIG_DEF_DEVICE_UUID = "deviceUUID";

    public static final String CONFIG_DEF_REFRESH_INTERVAL = "refreshInterval";

    public static final String CONFIG_DEF_TEMP_OFFSET = "tempOffset";

    public static final String CONFIG_DEF_HUMIDITY_OFFSET = "humidityOffset";

    /**
     * Kaiterra cloud API token
     */
    private String apiKey = "";

    /**
     * A laseregg device uuid
     */
    private String deviceUUID = "";

    /**
     * Data retrieval rate from the Kaiterra cloud
     */
    private int refreshInterval = 15;

    /**
     * Adjust temperature readings in case the LaserEgg device needs some corrections
     */
    private double tempOffset = 0;

    /**
     * Adjust humidity readings in case the LaserEgg device needs some corrections
     */
    private double humidityOffset = 0;

    public void updateFromProperties(Map<String, Object> properties) {
        Validate.notNull(properties);

        for (Map.Entry<String, Object> e : properties.entrySet()) {
            switch (e.getKey()) {
                case CONFIG_DEF_API_KEY:
                    setApiKey((String) e.getValue());
                    break;
                case CONFIG_DEF_DEVICE_UUID:
                    setDeviceUUID((String) e.getValue());
                    break;
                case CONFIG_DEF_REFRESH_INTERVAL:
                    setRefreshInterval((Integer) e.getValue());
                    break;
                case CONFIG_DEF_TEMP_OFFSET:
                    setTempOffset((Double) e.getValue());
                    break;
                case CONFIG_DEF_HUMIDITY_OFFSET:
                    setHumidityOffset((Double) e.getValue());
                    break;
            }

        }
    }

    public void updateFromProperties(Dictionary<String, Object> properties) {
        Validate.notNull(properties);
        List<String> keys = Collections.list(properties.keys());
        Map<String, Object> dictCopy = keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
        updateFromProperties(dictCopy);
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public double getTempOffset() {
        return tempOffset;
    }

    public void setTempOffset(double tempOffset) {
        this.tempOffset = tempOffset;
    }

    public double getHumidityOffset() {
        return humidityOffset;
    }

    public void setHumidityOffset(double humidityOffset) {
        this.humidityOffset = humidityOffset;
    }
}
