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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;

/**
 * @author sumitabhk
 *
 */
public interface IComponentDrawEngine extends IADContainerDrawEngine
{
	// Returns the ports that are attached to this draw engine 
	public ETList<IPresentationElement> getPorts();

	// Returns the displayed ports that are attached to this draw engine 
	public ETList<IElement> getPorts2();

	// Selects all ports 
	public void selectAllPorts(boolean bSelect);

	// Hide all ports 
	public void hideAllPorts(boolean bHide);

	// Reposition all ports 
	public void repositionAllPorts();

	// Remembers all the port positions 
	public void rememberAllPortPositions();

	// Restores all the port positions 
	public void restoreAllPortPositions();

	// Distributes interfaces on all the ports 
	public void distributeInterfacesOnAllPorts(boolean bRedraw);

	// Returns the bounding rectangle of the component with all ports and their attached lollypops 
	public ETPairT<IETRect, IETRect> getBoundingRectWithLollypops();
                                           
	// Moves the ports to avoid any intersections 
	public void movePortsToAvoidIntersections( int nSide, ETList<IPresentationElement> pPorts);
                                           
	// Moves all ports the the side nSide 
	public void movePortsToSide( int nSide );
        
        // Moves the ports listed in portPEs to the side nSide.
	public void movePortsToSide(int nSide, ETList<IPresentationElement> portPEs);
   
	// Should the drawengine autoroute edges during graph events? 
	public boolean getAllowAutoRouteEdges();
	
	// Should the drawengine autoroute edges during graph events? 
	public void  setAllowAutoRouteEdges(boolean bAutoRouteEdges);
}
