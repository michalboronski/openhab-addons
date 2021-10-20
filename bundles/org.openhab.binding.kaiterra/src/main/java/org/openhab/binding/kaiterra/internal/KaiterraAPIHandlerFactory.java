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
package org.openhab.binding.kaiterra.internal;

import static org.openhab.binding.kaiterra.internal.KaiterraBindingConstants.THING_TYPE_LASEREGG2_PLUS;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link KaiterraAPIHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michał Boroński - Initial contribution
 */
@Component(configurationPid = "binding.kaiterra", service = ThingHandlerFactory.class)
@NonNullByDefault
public class KaiterraAPIHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(KaiterraAPIHandler.SUPPORTED_THING_TYPES.stream().collect(Collectors.toSet()));

    private @NonNullByDefault({}) HttpClientFactory httpClientFactory;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_LASEREGG2_PLUS.equals(thingTypeUID)) {
            KaiterraAPIHandler handler = new KaiterraAPIHandler(thing, httpClientFactory.getCommonHttpClient());

            return handler;
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof KaiterraAPIHandler) {
            super.removeHandler(thingHandler);
        }
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = null;
    }
}
