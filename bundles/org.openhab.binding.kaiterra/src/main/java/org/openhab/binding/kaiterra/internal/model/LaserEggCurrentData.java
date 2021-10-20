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
package org.openhab.binding.kaiterra.internal.model;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link LaserEggCurrentData} is a configuration exception for the connections to LaserEgg
 * API.
 *
 * @author Michał Boroński - Initial contribution
 */
public class LaserEggCurrentData {

    private String id;

    @SerializedName("info.aqi")
    @Expose
    private InfoAqi infoAqi;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InfoAqi getInfoAqi() {
        return infoAqi;
    }

    public void setInfoAqi(InfoAqi infoAqi) {
        this.infoAqi = infoAqi;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
