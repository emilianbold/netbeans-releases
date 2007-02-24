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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidPointerException;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;

import java.util.Iterator;

/**
 * @author KevinM
 *
 */
public class ETRegionsCompartment extends ETZonesCompartment implements IRegionsCompartment
{

   /**
    * 
    */
   public ETRegionsCompartment()
   {
      super();
      m_zonedividers.setLineStyle(DrawEngineLineKindEnum.DELK_DOT);
   }

   public ETRegionsCompartment(IDrawEngine pDrawEngine)
   {
      super();
      this.setEngine(pDrawEngine);
      m_zonedividers.setLineStyle(DrawEngineLineKindEnum.DELK_DOT);
      initResources();
   }
   
   /**
    * This is the name of the drawengine used when storing and reading from the product archive.
    *
    * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
    * product archive (etlp file).
    */
   public String getCompartmentID()
   {
      return "ADRegionsCompartment";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.ETZonesCompartment#createZonesButtons(org.netbeans.modules.uml.ui.products.ad.application.IMenuManager)
    */
   protected void createZonesButtons(IMenuManager manager)
   {
      // TODO Auto-generated constructor stub
      //??m_ButtonHandler.addRegionsButtons( manager.get, m_zonedividers.getOrientation() ));
	   IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_POPUP_REGIONS"), "");
	   if (subMenu != null) 
	   {

		boolean bDefaultSensitivity = this.getObject().getDiagram().getReadOnly();
		int orientation = m_zonedividers.getOrientation();
		if ( orientation != IETZoneDividers.DMO_HORIZONTAL )
		{
			subMenu.add(createMenuAction(loadString("IDS_REGION_ADD_COLUMN"), "MBK_Z_ADD_COLUMN"));
			if( orientation != IETZoneDividers.DMO_UNKNOWN )
			{
				subMenu.add(createMenuAction(loadString("IDS_REGION_DELETE_COLUMN"), "MBK_Z_DELETE_COLUMN"));
			}
		}
		//_VH( AddSeparatorMenuItem( pContextMenu, pSubMenuItems ));
		if( orientation != IETZoneDividers.DMO_VERTICAL )
		{
			subMenu.add(createMenuAction(loadString("IDS_REGION_ADD_ROW"), "MBK_Z_ADD_ROW"));
			if( orientation != IETZoneDividers.DMO_UNKNOWN )
			{
				subMenu.add(createMenuAction(loadString("IDS_REGION_DELETE_ROW"), "MBK_Z_DELETE_ROW"));
			}
		}
	   }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.ETZonesCompartment#validateZoneCompartments()
    */
   protected void validateZoneCompartments(boolean attachElements)
   {
      IElement element = getModelElement();
      if( element instanceof IState )
      {
         IState parentElement = (IState)element;
               
         ETList< IRegion > regions = parentElement.getContents();
         if( regions != null )
         {
            // Copy the partitions to the elements
            ETList< IElement > elements = null;
            elements = (new CollectionTranslator<IRegion, IElement>()).copyCollection(regions);
      
            super.validateZoneCompartments( elements, IETZoneDividers.DMO_HORIZONTAL, attachElements );
         }
      }
   }

   /*
    * 	Returns a new Region Element, and adds it to the contents of this list compartment.
    */
   protected IRegion createNewRegion() throws RuntimeException
   {
      IElement cpElement = this.getModelElement();
      if (cpElement == null)
      {
         throw new InvalidPointerException();
      }

      IState cpParentState = cpElement instanceof IState ? (IState) cpElement : null;
      if (cpParentState == null)
      {
         throw new InvalidPointerException();
      }

      TypedFactoryRetriever < IRegion > factory = new TypedFactoryRetriever < IRegion > ();
      IRegion cpRegion = factory.createType("Region");
      if (cpRegion != null)
      {
         // cpRegion.CopyTo( ppRegion );

         cpParentState.addContent(cpRegion);
      }
      return cpRegion;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.ETZonesCompartment#createNewElement()
    */
   protected IElement createNewElement() throws RuntimeException
   {
      IRegion cpElement = createNewRegion();
      return cpElement;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#addModelElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, int)
    */
   public void addModelElement(IElement element, int nIndex) throws RuntimeException
   {
      // TODO Auto-generated method stub
      super.addModelElement(element, nIndex);
   }

}
