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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link KaiterraBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michał Boroński - Initial contribution
 */
@NonNullByDefault
public class KaiterraBindingConstants {

    private static final String BINDING_ID = "kaiterra";

    // Bridge
    public static final ThingTypeUID THING_TYPE_KAITERRA_API = new ThingTypeUID(BINDING_ID, "kaiterra-api");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LASEREGG2_PLUS = new ThingTypeUID(BINDING_ID, "laseregg2plus");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_LASEREGG2_PLUS).collect(Collectors.toSet()));

    // List of all Channel id's
    public static final String PM25 = "pm25";
    public static final String PM10 = "pm10";
    public static final String CO = "co";
    public static final String ID = "id";
    public static final String TIMESTAMP = "ts";
    public static final String TEMPERATURE = "temp";
    public static final String HUMIDITY = "humidity";
    public static final String RTVOC = "rtvoc";

    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Dimensionless> HUMIDITY_UNIT = Units.PERCENT;
}
