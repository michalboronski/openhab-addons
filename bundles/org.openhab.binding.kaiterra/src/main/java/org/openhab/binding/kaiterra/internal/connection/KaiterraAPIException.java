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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KaiterraAPIException} is a communication exception for the connections to Kaiterra API
 * API.
 *
 * @author Michał Boroński - Initial contribution
 */
@NonNullByDefault
public class KaiterraAPIException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with null as its detail message.
     */
    public KaiterraAPIException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Detail message
     */
    public KaiterraAPIException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause The cause
     */
    public KaiterraAPIException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message Detail message
     * @param cause The cause
     */
    public KaiterraAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
