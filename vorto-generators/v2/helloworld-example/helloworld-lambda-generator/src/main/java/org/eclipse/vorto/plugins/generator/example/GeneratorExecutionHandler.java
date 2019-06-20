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
package org.eclipse.vorto.plugins.generator.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.eclipse.vorto.core.api.model.informationmodel.InformationModel;
import org.eclipse.vorto.core.api.model.model.Model;
import org.eclipse.vorto.model.ModelContent;
import org.eclipse.vorto.model.conversion.ModelContentToEcoreConverter;
import org.eclipse.vorto.plugin.generator.GeneratorException;
import org.eclipse.vorto.plugin.generator.ICodeGenerator;
import org.eclipse.vorto.plugin.generator.IGenerationResult;
import org.eclipse.vorto.plugin.generator.InvocationContext;
import org.eclipse.vorto.plugin.generator.adapter.ObjectMapperFactory;
import org.eclipse.vorto.plugin.utils.ApiGatewayRequest;
import org.eclipse.vorto.plugin.utils.ApiGatewayResponse;
import org.eclipse.vorto.plugin.utils.Utils;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lambda Function that acts as a generator gateway and delegates generation request to our generator implementations.
 *
 */
public class GeneratorExecutionHandler implements RequestStreamHandler {
  
    private static final String PLUGINKEY = "pluginkey";
    private static final Set<ICodeGenerator> generators = new HashSet<>();
    
    static {
      //TODO: add your code generators here
      generators.add(new HelloWorldGenerator());
    }
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context)
        throws IOException {
      
      
      ObjectMapper mapper = ObjectMapperFactory.getInstance();
      
      ApiGatewayRequest request = ApiGatewayRequest.createFromJson(input);

      Optional<ICodeGenerator> generator = generators.stream().filter(gen -> gen.getMeta().getKey().equals(request.getPathParam(PLUGINKEY))).findAny();
      
      if (!generator.isPresent()) {
        objectMapper.writeValue(output,createHttpReponse(404));
      } 
            
      ModelContent modelContent = mapper.readValue(request.getInput(), ModelContent.class);
            
      ModelContentToEcoreConverter converter = new ModelContentToEcoreConverter();
      
      Model converted = converter.convert(modelContent, Optional.empty());
      
      InvocationContext invocationContext = InvocationContext.simpleInvocationContext(request.getQueryParams());
      
      InformationModel infomodel = Utils.toInformationModel(converted);
      
      try {
        IGenerationResult generatorResult = generator.get().generate(infomodel, invocationContext);
        ApiGatewayResponse validResponse = createResponse(generatorResult);
  
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(objectMapper.writeValueAsString(validResponse));
        writer.close();
      } catch (GeneratorException e) {
        ApiGatewayResponse response = createHttpReponse(500); 
        objectMapper.writeValue(output, response); 
      }
    }

    private ApiGatewayResponse createHttpReponse(int errorCode) {
      ApiGatewayResponse response = ApiGatewayResponse.builder()
          .setStatusCode(errorCode)
          .build();
      return response;
    }

    private ApiGatewayResponse createResponse(IGenerationResult generatorResult) {
      return ApiGatewayResponse.builder()
              .addHeader("Content-Type", "application/octet-stream")
              .addHeader("Content-Disposition", "attachment; filename=\""+generatorResult.getFileName()+"\"")
              .setBinaryBody(generatorResult.getContent())
              .build();
    }
}