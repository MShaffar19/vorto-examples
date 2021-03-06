/**
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.vorto.middleware.plugins;

import org.eclipse.vorto.middleware.deserializer.MimeType;
import org.eclipse.vorto.middleware.monitoring.IPayloadMonitor;

/**
 * Execution context container that contains properties that are passed from southbound IoT protocol adapters
 *
 */
public class ExecutionContext {

	private String deviceId;
	private String correlationId;
	private MimeType mimeType;
	private Object rawPayload;
	
	private IPayloadMonitor logger = null;
	
	public ExecutionContext(String deviceId, String correlationId, MimeType mimeType, Object rawPayload, IPayloadMonitor logger) {
		super();
		this.deviceId = deviceId;
		this.mimeType = mimeType;
		this.rawPayload = rawPayload;
		this.logger = logger;
		this.correlationId = correlationId;
	}
	

	public String getDeviceId() {
		return deviceId;
	}
	
	public MimeType getMimeType() {
	  return mimeType;
	}

	public Object getRawPayload() {
	  return this.rawPayload;
	}
	
	public IPayloadMonitor getLogger() {
		return this.logger;
	}


	public String getCorrelationId() {
		return correlationId;
	}
	
}
