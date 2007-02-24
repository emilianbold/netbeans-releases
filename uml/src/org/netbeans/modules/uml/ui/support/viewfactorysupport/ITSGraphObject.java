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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.graph.TSGraph;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;

/**
 * @author KevinM
 *
 * All graph objects must implement this interface, nodes, edges, labels, connectors, path nodes, node labels...
 */
public interface ITSGraphObject {
	public IETGraphObjectUI getETUI();
	public boolean isNode();
	public boolean isEdge();
	public boolean isLabel();
	public boolean isConnector();
	public boolean isPathNode();
	public TSEObject getObject();	// Operator.
	public boolean isSelected();
	public void setSelected(boolean selected);
	public boolean isVisible();
	public void setVisible(boolean visible); 
	public void setUserObject(Object userObject);
	public Object getUserObject();
	public String getText();
	public void setText(Object text);
	public TSGraph getOwnerGraph();
	public boolean isOwned();
	
	public void copy(final ITSGraphObject objToClone);
	public void delete();
	public TSConstRect getBounds();		// Returns the current object bounds.
	
}
