/*******************************************************************************
 * Copyright (c) 2017 Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *   
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *   
 * Contributors:
 * Bosch Software Innovations GmbH - Please refer to git log
 *******************************************************************************/
package org.eclipse.vorto.codegen.jsonschema.templates;

import org.eclipse.vorto.core.api.model.datatype.Property
import org.eclipse.vorto.core.api.model.functionblock.Event
import org.eclipse.vorto.core.api.model.functionblock.FunctionblockModel
import org.eclipse.vorto.plugin.generator.InvocationContext
import org.eclipse.vorto.plugin.generator.utils.ITemplate

class EventTemplate implements ITemplate<Event>{
	
	new() {
	}
	
	override getContent(Event event, InvocationContext invocationContext) {
		var fbm = event.eContainer.eContainer as FunctionblockModel;
		var definition = fbm.namespace + ":" + fbm.name + ":" + fbm.version;
		'''
			{
				"$schema": "http://json-schema.org/draft-07/schema#",
				"title": "Event payload validation of definition <«definition»> for message subject (event name) <«event.name»>"«IF !event.properties.empty»,«ENDIF»
				«IF event.properties.size === 1»
					«calcSinglePropertyContent(event.properties.get(0), invocationContext)»
				«ELSEIF !event.properties.empty»
					"type": "object",
					"properties": {
						«EntityTemplate.handleProperties(event.properties, invocationContext).toString.trim»
					},
					«EntityTemplate.calculateRequired(event.properties)»
				«ENDIF»
			}
		'''
	}
	
	static def CharSequence calcSinglePropertyContent(Property property, InvocationContext invocationContext) {
		val propertyStuff = EntityTemplate.handleProperty(property, invocationContext).toString.trim
						.replace("\"" + property.name + "\": {", "").trim;
		val theStringContent = propertyStuff.substring(0, propertyStuff.length-1);
		'''
			«theStringContent»
		'''
	}
}
		