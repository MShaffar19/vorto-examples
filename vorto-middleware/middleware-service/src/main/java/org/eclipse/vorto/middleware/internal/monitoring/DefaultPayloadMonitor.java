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
package org.eclipse.vorto.middleware.internal.monitoring;

import org.eclipse.vorto.middleware.monitoring.IPayloadMonitor;
import org.eclipse.vorto.middleware.monitoring.IPayloadMonitorCallback;
import org.eclipse.vorto.middleware.monitoring.MonitorMessage;
import org.eclipse.vorto.middleware.monitoring.MonitorMessage.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultPayloadMonitor implements IPayloadMonitor {
	
	private IPayloadMonitorCallback callback = null;
	
	private static int activeConnections = 0;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultPayloadMonitor.class);

	@Override
	public void monitor(MonitorMessage message) {
		if (message.getSeverity() == Severity.WARNING)  {
			logger.warn(message.toString());
		} else if (message.getSeverity() == Severity.ERROR)  {
			logger.error(message.toString());
		}
		this.doLog(message);
	}
	
	private void doLog(MonitorMessage message) {
		if (callback != null) {
			callback.onMessage(message);
		}
		
	}

	@Override
	public void registerCallback(IPayloadMonitorCallback callback) {
		this.callback = callback;
		activeConnections++;
		
	}

	@Override
	public void unregisterCallback() {
		activeConnections--;
		 if (activeConnections <= 0) {
			 callback = null;
		 }
		
	}

}
