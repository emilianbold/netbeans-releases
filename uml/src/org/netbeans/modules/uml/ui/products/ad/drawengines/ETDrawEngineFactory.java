/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.util.HashMap;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETStrings;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
/**
 * @author Embarcadero Technologies Inc.
 *
 *
 */
public class ETDrawEngineFactory {

	public ETDrawEngineFactory() {
	}

	public static IDrawEngine createDrawEngine(IETGraphObjectUI objectUI) throws ETException {
		IDrawEngine drawEngine = ETDrawEngineFactory.createDrawEngine(objectUI.getDrawEngineClass());
		if (drawEngine != null) {
			drawEngine.setParent(objectUI);
			objectUI.setDrawEngine(drawEngine);
			
			// We can not init the engine here, because it causes infinite loops.  The Engine
			// Datamember must be set first.
			//drawEngine.init();
			
		}
		return drawEngine;
	}

	public static IDrawEngine createDrawEngine(String name) throws ETException {
		ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
		Object value = factory.retrieveEmptyMetaType("DrawEngines", name, null);

		return value instanceof IDrawEngine ? (IDrawEngine) value : null;
	}

	public static ICompartment createCompartment(String name) throws ETException {
		try {
			ICompartment newCompartment = (ICompartment) Class.forName(COMPARTMENT_PACKAGE + getCompartmentClass(name)).newInstance();
			return newCompartment;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ETException(ETStrings.E_CMN_UNEXPECTED_EXC, "ETDrawEngineFactory.createCompartment()", e.getMessage());
		}
	}

	private static final String COMPARTMENT_PACKAGE = "org.netbeans.modules.uml.ui.products.ad.compartments.";

	// compartments
	public static final String ATTRIBUTE_LIST_COMPARTMENT = "ETAttributeListCompartment";
	public static final String CLASS_ATTRIBUTE_COMPARTMENT = "ETClassAttributeCompartment";
	public static final String CLASS_OPERATION_COMPARTMENT = "ETClassOperationCompartment";
	public static final String CLASS_NAME_COMPARTMENT = "ETClassNameCompartment";
	public static final String CLASS_NAME_LIST_COMPARTMENT = "ETClassNameListCompartment";
	public static final String OPERATION_LIST_COMPARTMENT = "ETOperationListCompartment";
	public static final String REDEFINED_OPERATION_LIST_COMPARTMENT = "ETRedefinedOperationListCompartment";
	public static final String STEREOTYPE_COMPARTMENT = "ETStereoTypeCompartment";
	public static final String TAGGED_VALUES_COMPARTMENT = "ETTaggedValuesCompartment";

	private static HashMap compartmentMap;

	private static String getCompartmentClass(String pCompartmentName) {
		if (compartmentMap == null) {
			initComparmtentMap();
			return (String) compartmentMap.get(pCompartmentName);
		} else {
			return (String) compartmentMap.get(pCompartmentName);
		}
	}

	private static void initComparmtentMap() {

		compartmentMap = new HashMap();

		compartmentMap.put(ATTRIBUTE_LIST_COMPARTMENT, ATTRIBUTE_LIST_COMPARTMENT);
		compartmentMap.put(CLASS_ATTRIBUTE_COMPARTMENT, CLASS_ATTRIBUTE_COMPARTMENT);
		compartmentMap.put(CLASS_OPERATION_COMPARTMENT, CLASS_OPERATION_COMPARTMENT);
		compartmentMap.put(CLASS_NAME_COMPARTMENT, CLASS_NAME_COMPARTMENT);
		compartmentMap.put(CLASS_NAME_LIST_COMPARTMENT, CLASS_NAME_LIST_COMPARTMENT);
		compartmentMap.put(OPERATION_LIST_COMPARTMENT, OPERATION_LIST_COMPARTMENT);
		compartmentMap.put(REDEFINED_OPERATION_LIST_COMPARTMENT, REDEFINED_OPERATION_LIST_COMPARTMENT);
		compartmentMap.put(STEREOTYPE_COMPARTMENT, STEREOTYPE_COMPARTMENT);
		compartmentMap.put(TAGGED_VALUES_COMPARTMENT, TAGGED_VALUES_COMPARTMENT);

		compartmentMap.put("ADAttributeListCompartment", ATTRIBUTE_LIST_COMPARTMENT);
		compartmentMap.put("ADClassAttributeCompartment", CLASS_ATTRIBUTE_COMPARTMENT);
		compartmentMap.put("ADClassOperationCompartment", CLASS_OPERATION_COMPARTMENT);
		compartmentMap.put("ADClassNameCompartment", CLASS_NAME_COMPARTMENT);
		compartmentMap.put("ADClassNameListCompartment", CLASS_NAME_LIST_COMPARTMENT);
		compartmentMap.put("ADOperationListCompartment", OPERATION_LIST_COMPARTMENT);
		compartmentMap.put("ADRedefinedOperationListCompartment", REDEFINED_OPERATION_LIST_COMPARTMENT);
		compartmentMap.put("StereotypeCompartment", STEREOTYPE_COMPARTMENT);
	}
}
