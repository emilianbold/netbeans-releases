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


package org.netbeans.modules.uml.ui.support.diagramsupport;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;

public interface IPresentationTarget
{
	/**
	 * The path to the diagram filename.
	*/
	public String getDiagramFilename();

	/**
	 * The path to the diagram filename.
	*/
	public void setDiagramFilename( String value );

	/**
	 * The XMIID of the presentation element.
	*/
	public String getPresentationID();

	/**
	 * The XMIID of the presentation element.
	*/
	public void setPresentationID( String value );

	/**
	 * If the diagram is open, here's the open diagram.
	*/
	public IDiagram getOpenDiagram();

	/**
	 * If the diagram is open, here's the open diagram.
	*/
	public void setOpenDiagram( IDiagram value );

	/**
	 * Returns the proxy diagram.
	*/
	public IProxyDiagram getProxyDiagram();

	/**
	 * gets The XMIID of the model element to find on the diagram and navigate to.  
	 * This is not as exact as the pe xmiid because there could be multiple PEs on a 
	 * diagram for a given ME.
	*/
	public String getModelElementID();
	
	/**
	 * sets The XMIID of the model element to find on the diagram and navigate to.  
	 * This is not as exact as the pe xmiid because there could be multiple PEs on a 
	 * diagram for a given ME.
	*/
  	public void setModelElementID(String newVal);

	/**
	 * sets The TopLevelXMIID of the model element to find on the diagram and navigate 
	 * to.  This is not as exact as the pe xmiid because there could be multiple PEs on 
	 * a diagram for a given ME.  
	*/
  	public void setTopLevelID(String newVal);

	/**
	 * gets The TopLevelXMIID of the model element to find on the diagram and navigate 
	 * to.  This is not as exact as the pe xmiid because there could be multiple PEs on 
	 * a diagram for a given ME.  
	*/
  	public String getTopLevelID();

	//needed for showing the element properly in navigation dialog.
	public String toString();
}
