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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;

public interface IGraphPresentation extends IPresentationElement
{
	/**
	 * Get/Set the model element attached to this presentation element
	*/
	public IElement getModelElement();

	/**
	 * Get/Set the model element attached to this presentation element
	*/
	public void setModelElement( IElement value );

	/**
	 * Is this presentation element on this diagram?
	*/
	public boolean getIsOnDiagram( IDiagram pDiagram );

	/**
	 * Is this presentation element on this diagram (by etl filename)?
	*/
	public boolean getIsOnDiagramFilename( String sFullFilename );

	/**
	 * Determine the presentation element's selected state
	*/
	public boolean getSelected();

	/**
	 * Determine the presentation element's selected state
	*/
	public void setSelected( boolean value );

//   /**
//    * Sets the presentation UI component.
//    */
//   public void setUI(IETGraphObjectUI ui);
   
   /**
    * Retreives the presentation UI component.
    */
   public IETGraphObjectUI getUI();
   
   /**
    * Retrieves the draw engine used by the UI component to render the 
    * graph object.
    */
   public IDrawEngine getDrawEngine();
   
   /**
    * Retrieves the graph object.
    */
   public IETGraphObject getETGraphObject();
   
  
   /**
    * Redraw this presentation element
    */
   public void invalidate();
   
   /** 
    * Called when the element connected to this PE has possibly been reparented 
    * to another document as a result of SCC operations.
    */
   public void externalElementLoaded();
   
}
