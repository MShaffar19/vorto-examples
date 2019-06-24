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
package org.eclipse.vorto.example.mapping.internal;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.eclipse.vorto.example.mapping.handler.Context;
import org.eclipse.vorto.example.mapping.handler.IPayloadHandler;
import org.eclipse.vorto.mapping.engine.model.binary.BinaryData;
import org.eclipse.vorto.model.ModelId;
import org.eclipse.vorto.model.runtime.InfomodelValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * AMQP Queue listener for any telemetry data coming in for the given tenant
 *
 */
public class HonoAMQPListener implements MessageListener {

  private static final Logger logger = LoggerFactory.getLogger(HonoAMQPListener.class);

  @Value(value = "${hono.tenantId}")
  private String tenantId;
  
  @Value(value = "${vorto.modelId}")
  private String modelId;

  @Autowired
  private MappingService mappingService;
  
  @Autowired
  private IPayloadHandler payloadHandler;
  
  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
  
  private static final String HEADER_DEVICE_ID = "device_id";

  

  @Override
  public void onMessage(Message message) {
    logger.debug("Received AMQP message from Hono ...");
    try {

      Object payload = null;
      if (message instanceof BytesMessage) {
        BytesMessage byteMessage = (BytesMessage) message;
        payload = convertBytesMessageToString(byteMessage);
      } else if (message instanceof TextMessage) {
        payload = gson.fromJson(((TextMessage) message).getText(), Object.class);
      } else {
        logger.warn("Unsupported message format for incoming hono message :" + message.getClass());
      }

      InfomodelValue normalizedData = mappingService
          .map(ModelId.fromPrettyFormat(modelId), payload);
      
      final String json = gson.toJson(normalizedData.serialize());
      
      final String deviceId = message.getStringProperty(HEADER_DEVICE_ID);

      JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
      
      payloadHandler.handlePayload(jsonObject, new Context(deviceId,normalizedData));
      
    } catch (JMSException e) {
      logger.error("Problem with consuming AMQP message", e);
    }
  }

  private BinaryData convertBytesMessageToString(BytesMessage message) throws JMSException {
    byte[] byteData = null;
    byteData = new byte[(int) message.getBodyLength()];
    message.readBytes(byteData);
    message.reset();
    return new BinaryData(byteData);
  }
}
