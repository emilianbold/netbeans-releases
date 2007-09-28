/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
