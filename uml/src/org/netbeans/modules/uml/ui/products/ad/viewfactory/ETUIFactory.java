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


/*
 * Created on May 20, 2003
 *
 */
package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETStrings;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author Embarcadero Technologies Inc.
 *
 *
 */
public class ETUIFactory {

	public ETUIFactory() {
	}

	public static ETGenericGraphUI createGraphUI(String command) {

		return null;

	}

	public static IETGraphObjectUI createGraphObjectUI(String UIClass, String initString, String drawEngineName, IDrawingAreaControl drawingArea) throws ETException {
		IETGraphObjectUI ui = null;

		try {
			//ui = (IETGraphObjectUI) Class.forName(UIFACTORY_PACKAGE + UIClass).newInstance();
			ui = (IETGraphObjectUI) Class.forName(UIClass).newInstance();
			ui.setInitStringValue(initString);
			ui.setDrawEngineClass(drawEngineName);
			ui.setDrawingArea(drawingArea);
			return ui;
		} catch (InstantiationException e) {
			throw new ETException(ETStrings.E_CMN_CREATE_INSTANCE, UIClass, e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ETException(ETStrings.E_CMN_ILLEGAL_ACCESS, UIClass, e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new ETException(ETStrings.E_CMN_CLASS_NOT_FOUND, UIClass, e.getMessage());
		} catch (Exception e) {
			throw new ETException(ETStrings.E_CMN_UNEXPECTED_EXC, "ETUIFactory.createGraphObjUI()", e.getMessage());
		}
	}

	public static ETGenericNodeUI createNodeUI(String nodeUIClass, String initString, String drawEngineName, IDrawingAreaControl drawingArea) throws ETException {

		IETGraphObjectUI retVal = createGraphObjectUI(nodeUIClass, initString, drawEngineName, drawingArea);

		return (ETGenericNodeUI) retVal;
	}

	public static ETGenericNodeLabelUI createNodeLabelUI(String nodeLabelUIClass, String initString, String drawEngineName, IDrawingAreaControl drawingArea) throws ETException {

		ETSystem.out.println("Inside ETUIFactory.createNodeLabelUI() ");
		IETGraphObjectUI retVal = createGraphObjectUI(nodeLabelUIClass, initString, drawEngineName, drawingArea);

		return (ETGenericNodeLabelUI) retVal;
	}

	public static ETGenericEdgeUI createEdgeUI(String edgeUIClass, String initString, String drawEngineClass, IDrawingAreaControl drawingArea) throws ETException {
		return (ETGenericEdgeUI) createGraphObjectUI(edgeUIClass, initString, drawEngineClass, drawingArea);
	}

	public static ETGenericEdgeLabelUI createEdgeLabelUI(String edgeLabelUIClass, String initString, String drawEngineName, IDrawingAreaControl drawingArea) throws ETException {

		ETSystem.out.println("Inside ETUIFactory.createEdgeLabelUI() ");
		IETGraphObjectUI retVal = createGraphObjectUI(edgeLabelUIClass, initString, drawEngineName, drawingArea);

		return (ETGenericEdgeLabelUI) retVal;
	}

	public static ETGenericConnectorUI createConnectorUI(String command) {
		return null;
	}

	// Package where the classes for the supported UI' can be found
	private static final String UIFACTORY_PACKAGE = "org.netbeans.modules.uml.ui.products.ad.viewfactory.";

	// The class names of the supported UI's
	public static final String GENERIC_GRAPH_UI = "ETGenericGraphUI";
	public static final String GENERIC_EDGE_UI = "ETGenericEdgeUI";
	public static final String GENERIC_EDGE_LABEL_UI = "ETGenericEdgeLabelUI";
	public static final String GENERIC_NODE_UI = "ETGenericNodeUI";
	public static final String GENERIC_NODE_LABEL_UI = "ETGenericNodeLabelUI";
	public static final String GENERIC_CONNECTOR_UI = "ETGenericConnectorUI";

}
