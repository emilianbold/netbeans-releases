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

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngineFactory;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISimpleListCompartment;

public class DrawingFactory implements IDrawingFactory
{
   private static final String PACKAGE_NAME = "org.netbeans.modules.uml.ui.support.applicationmanager.";
   protected static ETList< ETPairT<ICompartment, IProductArchiveElement> > m_CompartmentResourcePairs = new ETArrayList < ETPairT<ICompartment, IProductArchiveElement> > ();

   public DrawingFactory()
   {
      
   }

   public IGraphPresentation createPresentation(IDrawEngine drawEngine)
   {
      return createPresentationObj(drawEngine);
   }

   public static IGraphPresentation createPresentationObj(IDrawEngine drawEngine)
   {

      IGraphPresentation pe = null;
      if (drawEngine != null && drawEngine.getUI() != null)
      {
         String typeName = getPresentationClassName(drawEngine);

         if (typeName != null)
         {
            //        try{
            //          pe = (IGraphPresentation) Class.forName(PACKAGE_NAME + className).newInstance();
            //
            //          if (pe != null) {
            //            pe.setUI(drawEngine.getUI());
            //            drawEngine.getUI().getModelElement().addPresentationElement(pe);
            //          }
            //        }
            //        catch(Exception e){
            //          e.printStackTrace();
            //        }

            ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
            if(factory != null)
            {
               Object presentationObj = factory.retrieveMetaType(typeName, "");
               if (presentationObj instanceof IGraphPresentation)
               {
                  pe = (IGraphPresentation)presentationObj;    
                  
                  IETGraphObjectUI engine = drawEngine.getUI();
                  if(engine != null)
                  {
                     engine.getModelElement().addPresentationElement(pe);
                  }              
               }
            }
         }
      }
      return pe;
   }

   /**
	* Retrieve the type that is our node presentation element
	*
	* @param pCreatedPE [out,retval] The created node presentation element
	*/
	public static INodePresentation retrieveNodePresentationMetaType()
	{
		INodePresentation retObj = null;
		ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
		if(factory != null)
		{
		   Object presentationObj = factory.retrieveMetaType("NodePresentation", null);
		   if (presentationObj instanceof INodePresentation)
		   {
			  retObj = (INodePresentation)presentationObj;    
		   }
		}
		return retObj;
	}

	/**
	 * Retrieve the type that is our label presentation element
	 *
	 * @param pCreatedPE [out,retval] The created label presentation element
	 */
	public static ILabelPresentation retrieveLabelPresentationMetaType()
	{
		ILabelPresentation retObj = null;
		ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
		if(factory != null)
		{
		   Object presentationObj = factory.retrieveMetaType("LabelPresentation", null);
		   if (presentationObj instanceof ILabelPresentation)
		   {
			  retObj = (ILabelPresentation)presentationObj;    
		   }
		}
		return retObj;
	}

	/**
	 * Retrieve the type that is our edge presentation element
	 *
	 * @param typeName [in] The type of edge presentation element we should create
	 * @param pCreatedPE [out,retval] The created node presentation element
	 */
	public static IEdgePresentation retrieveEdgePresentationMetaType(String typeName)
	{
		IEdgePresentation retObj = null;
		ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
		if(factory != null)
		{
		   Object presentationObj = factory.retrieveMetaType(typeName, null);
		   if (presentationObj instanceof IEdgePresentation)
		   {
			  retObj = (IEdgePresentation)presentationObj;    
		   }
		}
		return retObj;
	}
	
   protected static String getPresentationClassName(IDrawEngine drawEngine)
   {
      return drawEngine != null ? drawEngine.getPresentationType() : null;
   }
   
   public static IDrawEngine createDrawEngine(IETGraphObjectUI ui, 
   											  IProductArchive prodArch, 
   											  IProductArchiveElement archEle)
   {
   		IDrawEngine retObj = null;
		try
		{
			retObj = ETDrawEngineFactory.createDrawEngine(ui);
			if (retObj != null)
			{
				//Need to uncomment this once I figure out why nodes are not loaded 
				//in proper order when I do the C++ way.
				retObj.readFromArchive(prodArch, archEle);
			}
		}
		catch (ETException e)
		{
			e.printStackTrace();
		}
   		return retObj;
   }
   
   /**
	* Creates a compartment based on reading in from the archive.  The compartment name is used
	* as a lookup into EssentialConfig file that will resolve to a progid and cocreate the correct
	* compartment.
	*
	* @param pParentDrawEngine [in] The DrawEngine these new compartments are a part of.
	* @param pProductArchive [in] The archive file (etlp) that contains the drawing information.
	* @param pElement [in] The toplevel element for a compartment.
	*/
   public static void createCompartments(IDrawEngine pParentDrawEngine,
										 IProductArchive pProductArchive, 
										 IProductArchiveElement pElement)
   {
   		if (pParentDrawEngine != null)
   		{
   			if (pElement != null)
   			{
   				IProductArchiveElement[] subElems = pElement.getElements();
   				if (subElems != null)
   				{
   					for (int i=0; i<subElems.length; i++)
   					{
   						IProductArchiveElement elem = subElems[i];
   						ETPairT<IProductArchiveElement,String> result = pProductArchive.getTableEntry(elem, 
   												IProductArchiveDefinitions.COMPARTMENTNAMETABLEINDEXATTRIBUTE_STRING, 
   												IProductArchiveDefinitions.COMPARTMENTNAMETABLE_STRING);
						String foundElemId = null;
						if (result != null)
						{
							foundElemId = result.getParamTwo();
						}
   						if (foundElemId != null && foundElemId.length() > 0)
   						{
							// Create the compartment based on the name in the archive
   							ICompartment newComp = CreationFactoryHelper.createCompartment(foundElemId);
   							if (newComp != null)
   							{
								// Add this compartment to the engine.
								//newComp.setEngine(pParentDrawEngine);
   								//pParentDrawEngine.addCompartment(newComp, 0);
                           pParentDrawEngine.addCompartment(newComp, -1);
   								newComp.readFromArchive(pProductArchive, elem);
   							}
   						}
   					}
   				}
   			}
   		}
   }

   /**
	* Creates a group of compartments
	*/
   public static void createCompartments(ISimpleListCompartment pParentCompartment,
										 IProductArchive pProductArchive, 
										 IProductArchiveElement[] pElements)
   {
   		if (pParentCompartment != null && pProductArchive != null && pElements != null)
   		{
   			int count = pElements.length;
   			for (int i=0; i<count; i++)
   			{
   				IProductArchiveElement pEle = pElements[i];
   				ETPairT<IProductArchiveElement, String> result = pProductArchive.getTableEntry(pEle, IProductArchiveDefinitions.COMPARTMENTNAMETABLEINDEXATTRIBUTE_STRING, IProductArchiveDefinitions.COMPARTMENTNAMETABLE_STRING);
   				if (result != null)
   				{
   					String foundId = result.getParamTwo();
   					
   					if (foundId != null && foundId.length() > 0)
   					{
						// Create the compartment based on the name in the archive
						ICompartment newComp = CreationFactoryHelper.createCompartment(foundId);
						if (newComp != null)
						{
							// Add this compartment to the engine.  Do before the ReadFromArchive
							// because the ReadFromArchive requires the draw engine which is set in
							// AddCompartment
							pParentCompartment.addCompartment(newComp, -1, false);
							newComp.readFromArchive(pProductArchive, pEle);
						}
   					}
   				}
   			}
   		}
   }

   /**
	* Adds a compartment resource pair
	*/
   public static void addCompartmentResourcePair(ICompartment pCompartment, 
   												 IProductArchiveElement pElement)
   {
   		if (pCompartment != null && pElement != null)
   		{
			m_CompartmentResourcePairs.add(new ETPairT<ICompartment, IProductArchiveElement>(pCompartment, pElement) );
   		}
   }
   
   /**
	* Reads the resources for all comparments
	*/
   public static void readCompartmentResourcesFromArchive(IProductArchive pProductArchive)
   {
   		if (pProductArchive != null)
   		{
			int count = m_CompartmentResourcePairs.size();

			for (int i = 0; i < count; i++)
			{
				ETPairT<ICompartment, IProductArchiveElement> val = m_CompartmentResourcePairs.get(i);
				ICompartment pCompartment = val.getParamOne();
				IProductArchiveElement pProductArchiveElement = val.getParamTwo();
				if (pCompartment != null && pProductArchiveElement != null)
				{
					pCompartment.getCompartmentResourceUser().readResourcesFromArchive(pProductArchive, pProductArchiveElement);
				}
			}
   		}
		m_CompartmentResourcePairs.clear();
   }
   
   /**
	* Retrieve the type that is our element
	*
	* @param typeName [in] The type of element we should create
	* @param pCreatedElement [out,retval] The created element
	*/
   public static IElement retrieveModelElement(String typeName)
   {
		IElement retObj = null;
		ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
		if(factory != null)
		{
		   Object obj = factory.retrieveMetaType(typeName, null);
		   if (obj instanceof IElement)
		   {
			  retObj = (IElement)obj;    
		   }
		}
		return retObj;
   }

   /**
	* Retrieve the type specified by the type name, e.g. Class. The type has been fully prepared and initialize
	*/
   public static Object retrieveMetaType(String typeName)
   {
		Object retObj = null;
		ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
		if(factory != null)
		{
		   retObj = factory.retrieveMetaType(typeName, null);
		}
		return retObj;
   }

   /**
    * @param eventManagerType
    * @return
    */
   public static IEventManager retrieveEventManager(String eventManagerType)
   {
      IEventManager retObj = null;
      ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
      if(factory != null)
      {
      	 Object obj = factory.retrieveEmptyMetaType("Managers", eventManagerType, null);
         if (obj instanceof IEventManager)
         {
           retObj = (IEventManager)obj;    
         }
      }
      return retObj;
   }
 
   public static ILabelManager retrieveLabelManager(String labelManagerType)
   {
	  ILabelManager retObj = null;
	  ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
	  if(factory != null)
	  {
		 Object obj = factory.retrieveEmptyMetaType("Managers", labelManagerType, null);
		 if (obj instanceof ILabelManager)
		 {
		   retObj = (ILabelManager)obj;    
		 }
	  }
	  return retObj;
   }
  
   /**
	 * Retrieve the type that is our compartment
	 *
	 * @param typeName [in] The type of element we should create
	 * @param pCreatedManager [out,retval] The created manager
	 */
	public static ICompartment retrieveCompartment(String typeName)
   {
		ICompartment retObj = null;
	   Object obj = CreationFactoryHelper.retrieveMetaType("Compartments", typeName);
	   if (obj instanceof ICompartment)
	   {
		  retObj = (ICompartment)obj;
	   }
		return retObj;
   }
}
