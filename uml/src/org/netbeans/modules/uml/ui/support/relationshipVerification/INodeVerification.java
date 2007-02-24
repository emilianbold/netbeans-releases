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



package org.netbeans.modules.uml.ui.support.relationshipVerification;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;

/**
 * 
 * @author Trey Spiva
 */
public interface INodeVerification
{
   /**
    * Creates the appropriate metatype for this node.  
    * 
    * @param pDiagram The current diagram
    * @param pCreatedNode The node that just got created
    * @param pNamespace The namespace the new node should be in
    * @param metaTypeString The metatype string of the new element
    * @param sInitializationString The initialization string of the node that was just created.
    * @return A pair that contains that conitains the IElement and a 
    *         presentation reference relationship is created between the 
    *         referencing presentation element and the PresentationElement to
    *         be created.  If no referencing presentation element exist then
    *         <code>null</code> will be returned.
    */ 
   public ETPairT <IElement, IPresentationElement> createAndVerify(IDiagram   pDiagram,
                                                            IETNode     pCreatedNode,
                                                            INamespace pNamespace,
                                                            String     metaTypeString,
                                                            String     sInitializationString);
                            
	/**
	 * Creates the appropriate metatype for this node.  
	 * 
	 * @param pDiagram The current diagram
	 * @param pCreatedNode The node that just got created
	 * @param pNamespace The namespace the new node should be in, can be null.
	 * @return A pair that contains that conitains the IElement and a 
	 *         presentation reference relationship is created between the 
	 *         referencing presentation element and the PresentationElement to
	 *         be created.  If no referencing presentation element exist then
	 *         <code>null</code> will be returned.
	 */ 
	public ETPairT < IElement, IPresentationElement > createAndVerify(IDiagram pDiagram,
			IETNode pCreatedNode, INamespace pNamespace);
 
   /**
    * Verifies that this node is valid at this point.  Fired by the diagram 
    * add node tool.  If the node requires a node to node relationship the
    * actual location will be different then the location passed into the method. 
    * 
    * @param pDiagram The diagram to verify the location.
    * @param location The location to verify.
    * @return <code>true</code> if the location if valid, <code>false</code>
    *         otherwise.
    */ 
  public boolean verifyCreationLocation(IDiagram pDiagram,
                                  IETPoint  location) ;
                            
   /**
    * During the creation process this is fired when the node is dragged around.
    * Fired by the diagram add node tool.
    */
   public boolean verifyDragDuringCreation(IDiagram pDiagram,
                                 IETNode   pCreatedNode,
                                 IETPoint location);
}
