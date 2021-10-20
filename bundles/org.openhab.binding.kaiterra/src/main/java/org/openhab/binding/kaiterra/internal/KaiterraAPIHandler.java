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

import static org.openhab.binding.kaiterra.internal.KaiterraBindingConstants.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.kaiterra.internal.config.KaiterraAPIBindingConfiguration;
import org.openhab.binding.kaiterra.internal.connection.KaiterraAPIConnection;
import org.openhab.binding.kaiterra.internal.connection.KaiterraAPIException;
import org.openhab.binding.kaiterra.internal.model.LaserEggCurrentData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractLaserEggHandler} is responsible for handling commands through Kaiterra cloud API, which are sent
 * to one of the
 * channels.
 *
 * @author Michał Boroński - Initial contribution
 */
@NonNullByDefault
public class KaiterraAPIHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KaiterraAPIHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = KaiterraBindingConstants.SUPPORTED_THING_TYPES_UIDS;

    private static final long INITIAL_DELAY_IN_SECONDS = 5;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) KaiterraAPIBindingConfiguration config;

    private @NonNullByDefault({}) KaiterraAPIConnection connection;
    private @Nullable LaserEggCurrentData currentData;
    private final HttpClient httpClient;

    public KaiterraAPIHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(KaiterraAPIBindingConfiguration.class);

        boolean configValid = true;
        if (StringUtils.isEmpty(config.getApiKey())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Api key is missing");
            configValid = false;
        }

        int refreshInterval = config.getRefreshInterval();
        if (refreshInterval < 5) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "refreshInterval too low");
            configValid = false;
        }

        if (configValid) {
            connection = new KaiterraAPIConnection(this, httpClient);

            updateStatus(ThingStatus.UNKNOWN);

            if (refreshJob == null || refreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} sec.", refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateThing, INITIAL_DELAY_IN_SECONDS,
                        refreshInterval, TimeUnit.SECONDS);
            }
        }
    }

    private void updateThing() {
        ThingStatus status = ThingStatus.OFFLINE;

        if (connection != null) {
            this.updateData(connection);
            status = thing.getStatus();
        } else {
            logger.debug("Cannot update LaserEgg device {} from Kaiterra API cloud", thing.getUID());
            status = ThingStatus.OFFLINE;
        }

        updateStatus(status);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID, currentData);
        } else {
            logger.debug("Kaiterra binding is read-only and can only retrieve LaserEgg readings '{}'.", command);
        }
    }

    /**
     * Updates LaserEgg with data from Kaiterra cloud API
     *
     * @param connection {@link KaiterraAPIConnection} instance
     */
    public void updateData(KaiterraAPIConnection connection) {
        try {
            if (requestData(connection)) {
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (KaiterraAPIException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * Requests the data from Kaiterra API.
     *
     * @param connection {@link KaiterraAPIConnection} instance
     * @return true, if the request for the Kaiterra Api data was successful
     * @throws KaiterraAPIException
     * @throws KaiterraAPIConfigurationException
     */
    protected boolean requestData(KaiterraAPIConnection connection) throws KaiterraAPIException {
        LaserEggCurrentData data = connection.getLaserEggData(config.getApiKey(), config.getDeviceUUID());
        if (data != null && StringUtils.isNotEmpty(data.getId())) {

            currentData = data;

            return true;
        }

        return false;
    }

    /**
     * Updates all channels of this handler from the latest LaserEgg data retrieved.
     */
    private void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (ChannelKind.STATE.equals(channel.getKind()) && isLinked(channelUID)) {
                updateChannel(channelUID, currentData);
            }
        }
    }

    /**
     * Updates the channel with the given UID from the latest LaserEgg data retrieved.
     *
     * @param channelUID UID of the channel
     */
    protected void updateChannel(ChannelUID channelUID, @Nullable LaserEggCurrentData data) {
        if (isLinked(channelUID)) {

            Object value;
            try {
                value = getValue(channelUID.getAsString(), data);
            } catch (Exception e) {
                logger.debug("LaserEgg doesn't provide {} measurement", channelUID.getAsString().toUpperCase());
                return;
            }

            State state = org.openhab.core.types.UnDefType.NULL;
            if (value instanceof QuantityType<?>) {
                state = (QuantityType<?>) value;
            } else if (value instanceof BigDecimal) {
                state = new DecimalType((BigDecimal) value);
            } else if (value instanceof Integer) {
                state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
            } else if (value instanceof String) {
                state = new StringType(value.toString());
            } else if (value != null) {
                logger.warn("Update channel {}: Unsupported value type {}", channelUID,
                        value.getClass().getSimpleName());
            }

            logger.debug("Update channel {} with state {} ({})", channelUID, state.toString(),
                    (data == null) ? "null" : data.getId());

            updateState(channelUID, state);
        }
    }

    public @Nullable Object getValue(String channelId, @Nullable LaserEggCurrentData data) throws Exception {
        String[] fields = StringUtils.split(channelId, ":");

        if (data != null) {
            switch (fields[fields.length - 1]) {
                // TODO : calcualte AQI values
                // case AQI:
                // return data.getData().getAqi();
                // case AQIDESCRIPTION:
                // return data.getData().getAqiDescription();
                case RTVOC:
                    return data.getInfoAqi().getData().getRtvoc();
                case PM25:
                    return data.getInfoAqi().getData().getPm25();
                case PM10:
                    return data.getInfoAqi().getData().getPm10();
                case ID:
                    return data.getId();
                case TIMESTAMP:
                    return data.getInfoAqi().getTs();
                case TEMPERATURE:
                    return new QuantityType<Temperature>(data.getInfoAqi().getData().getTemp() + config.getTempOffset(),
                            TEMPERATURE_UNIT);
                case HUMIDITY:
                    return new QuantityType<>(data.getInfoAqi().getData().getHumidity() + config.getHumidityOffset(),
                            HUMIDITY_UNIT);
            }
        }

        return null;
    }

    public KaiterraAPIBindingConfiguration getLaserEggBindingConfig() {
        return config;
    }
}
