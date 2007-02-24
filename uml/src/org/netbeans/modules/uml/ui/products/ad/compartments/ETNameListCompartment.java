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

import java.awt.Point;
import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

public class ETNameListCompartment extends ETListCompartment implements INameListCompartment
{
   protected static final int OC_TEMPLATE_PARAMETERS = 1;
   protected static final int OC_STATICTEXT = 2;
   protected static final int OC_STEREOTYPE = 4;
   protected static final int OC_TAGGEDVALUES = 8;
   protected static final int OC_PACKAGEIMPORT = 16;

   public static String DEFAULTCOMPARTMENTKIND_STRING = "DefaultCompartment";
   public static String STATICTEXT_STRING = "staticText";
   protected IADEditableCompartment m_DefaultCompartment = null;
   protected String m_NameCompartmentID = "ADClassNameCompartment";
   protected String m_StaticText = "";
   protected boolean m_SetEnginesDefaultCompartment = true;
   protected boolean m_EnablePackageImportCompartment = true;
   protected boolean m_EnableTemplateParameter = false;

   public ETNameListCompartment()
   {
      super();
      this.init();
   }

   public ETNameListCompartment(IDrawEngine pDrawEngine)
   {
      super(pDrawEngine);
      this.init();
   }

   private void init()
   {
      m_SetEnginesDefaultCompartment = true;
      setShowName(false);
      m_name = "";
      //m_enableScroll = false;
      m_collapsible = false;
      
      m_resizeable = false;  
      m_NameCompartmentID = "ADClassNameCompartment";
      this.initResources();
   }  

   public IStereotypeCompartment getStereotypeCompartment()
   {
      ICompartment pCompartment = (ICompartment) getCompartmentByKind(IPackageImportCompartment.class);
      return pCompartment instanceof IStereotypeCompartment ? (IStereotypeCompartment) pCompartment : null;
   }

   public ITaggedValuesCompartment getTaggedValuesCompartment()
   {
      ICompartment pCompartment = (ICompartment) getCompartmentByKind(IPackageImportCompartment.class);
      return pCompartment instanceof ITaggedValuesCompartment ? (ITaggedValuesCompartment)pCompartment : null;    
   }

   public IPackageImportCompartment getPackageImportCompartment()
   {
      ICompartment pCompartment = (ICompartment) getCompartmentByKind(IPackageImportCompartment.class);
      return pCompartment instanceof IPackageImportCompartment ? (IPackageImportCompartment)pCompartment : null;    
   }

   public IADNameCompartment getNameCompartment()
   {
      ICompartment compartment = getDefaultCompartment();
      return compartment instanceof IADNameCompartment ? (IADNameCompartment) compartment : null;
   }

   public void addStaticText(String sStaticText)
   {
      m_StaticText = sStaticText;
      updateStaticTextCompartment(false);
   }

   /**
    * Should we show the static text compartment.
    *
    * @param pElement [in] The element that acts as the parent to the optional compartments
    * @param bAddedOrRemovedCompartment [out] true if a compartment was added or removed.
    */
   public void updateStaticTextCompartment(boolean bAddedOrRemovedCompartment)
   {
      ICompartment pStaticTextCompartment = (ICompartment) getCompartmentByKind(IADStaticTextCompartment.class);

      if (m_StaticText.length() > 0)
      {
         if (pStaticTextCompartment == null)
         {
            pStaticTextCompartment = new ETStaticTextCompartment();
            pStaticTextCompartment.setEngine(this.getEngine());
            this.addCompartment(pStaticTextCompartment, 0, false);
         }

         pStaticTextCompartment.setName(m_StaticText);
         pStaticTextCompartment.setReadOnly(true);
      }
      else if (pStaticTextCompartment != null)
      {
         this.removeCompartment(pStaticTextCompartment, false);
      }
   }

   /**
    * Adds a model element to this compartment.
    *
    * @param pElements[in] The list of elements this list compartment should attach to.  It creates sub
    * compartments as necessary
    */
   public void attach(IElement pElement)
   {
      if (m_DefaultCompartment == null)
      {
         addModelElement(pElement, -1);
         return;
      }

      if (m_engine != null && m_DefaultCompartment != null)
      {
      	Iterator<ICompartment> iter = this.getCompartments().iterator();
         while (iter.hasNext())
         {
            ICompartment pComp = iter.next();
            if (pComp instanceof IADEditableCompartment)
            {
               IADEditableCompartment nameComp = (IADEditableCompartment) pComp;
               nameComp.addModelElement(pElement, -1);
               nameComp.setShowName(true);
            }
         }

         ///////////////////////////////////////////////////////////////////
         // Create the optional compartments (stereotype, tagged values and package import)
         if (updateAllOptionalCompartments(pElement))
         {
            setIsDirty();
         }
      }
   }

   public boolean getPackageImportCompartmentEnabled()
   {
      return m_EnablePackageImportCompartment;
   }

   public boolean getResizeToFitCompartments()
   {
      return this.isResizeable();
   }

   public boolean getTemplateParameterCompartmentEnabled()
   {
      return false;
   }

   public void setNameCompartment(String sNameCompartmentName)
   {
      if (sNameCompartmentName != null && sNameCompartmentName.length() > 0)
      {
         m_NameCompartmentID = sNameCompartmentName;
      }
   }

   public void setPackageImportCompartmentEnabled(boolean bEnabled)
   {
      m_EnablePackageImportCompartment = bEnabled;
   }

   //	public void setResizeToFitCompartments(boolean bResizeToFit) {
   //
   //	}

   public void setTemplateParameterCompartmentEnabled(boolean pEnabled)
   {

   }

   protected int getCompartmentListIndex(int nCompartmentKind)
   {
      int nIndex = 0;

      switch (nCompartmentKind)
      {
         case OC_TEMPLATE_PARAMETERS :
            {
               // Always on top
               nIndex = 0;
            }
            break;
         case OC_STATICTEXT :
            {
               // Always on after the template parameters or on top
               ITemplateParametersCompartment pTemplateParametersCompartment = getCompartmentByKind(ITemplateParametersCompartment.class);
               if (pTemplateParametersCompartment != null)
               {
                  int nThisIndex = getCompartmentIndex((ICompartment) pTemplateParametersCompartment);
                  nIndex = nThisIndex + 1; // Should be 1
               }
               else
               {
                  nIndex = 0;
               }
            }
            break;
         case OC_STEREOTYPE :
            {
               // Always on after the static text or after template parameters or on top
               ITemplateParametersCompartment pTemplateParametersCompartment = getCompartmentByKind(ITemplateParametersCompartment.class);
               IADStaticTextCompartment pStaticText = getCompartmentByKind(IADStaticTextCompartment.class);
               if (pTemplateParametersCompartment != null)
               {
                  int nThisIndex = getCompartmentIndex((ICompartment) pTemplateParametersCompartment);
                  nIndex = nThisIndex + 1; // Should be 1
               }
               else if (pStaticText != null)
               {
                  int nThisIndex = getCompartmentIndex(pStaticText);
                  nIndex = nThisIndex + 1;
               }
               else
               {
                  nIndex = 0;
               }
            }
            break;
         case OC_TAGGEDVALUES :
            {
               // After the name
               if (m_DefaultCompartment instanceof IADNameCompartment)
               {
                  IADNameCompartment pNameCompartment = (IADNameCompartment) m_DefaultCompartment;
                  int nThisIndex = getCompartmentIndex(pNameCompartment);
                  nIndex = nThisIndex + 1;
               }
               else
               {
                  // Error
                  nIndex = -1;
               }
            }
            break;
         case OC_PACKAGEIMPORT :
            {
               // After the tagged values, if no tagged values then after the name
               IADNameCompartment pNameCompartment = m_DefaultCompartment instanceof IADNameCompartment ? (IADNameCompartment) m_DefaultCompartment : null;
               
               ITaggedValuesCompartment pTaggedValuesCompartment = getCompartmentByKind(ITaggedValuesCompartment.class);
               if (pTaggedValuesCompartment != null)
               {
                  int nThisIndex = getCompartmentIndex(pTaggedValuesCompartment);
                  nIndex = nThisIndex + 1;
               }
               else
               {
                  // No tagged values compartment, put after the name
                  if (pNameCompartment != null)
                  {
                     int nThisIndex = getCompartmentIndex(pNameCompartment);
                     nIndex = nThisIndex + 1;
                  }
                  else
                  {
                     // Error
                     nIndex = -1;
                  }
               }
            }
            break;
      }

      return nIndex;
   }

   protected boolean updateStereotypeCompartment(IElement pElement)
   {
      boolean bAddedOrRemovedCompartment = false;

      IADStereotypeCompartment pStereoCompartment = getCompartmentByKind(IADStereotypeCompartment.class);
      if (pElement != null)
      {
         // If we've got stereotypes then get the string to display
         String finalName = getStereotypeText(pElement);
         if (finalName.length() > 0)
         {
            boolean bDoAdd = false;
            if (pStereoCompartment == null)
            {
               // Create the compartment
               pStereoCompartment = (IADStereotypeCompartment) (new ETStereoTypeCompartment());
               pStereoCompartment.setEngine(m_engine);
               pStereoCompartment.addModelElement(pElement, -1);

               bDoAdd = true;
            }

            if (pStereoCompartment != null)
            {
               pStereoCompartment.setName(finalName);
               if (bDoAdd)
               {
                  super.addCompartment((ICompartment) pStereoCompartment, getCompartmentListIndex(OC_STEREOTYPE), false);
                  bAddedOrRemovedCompartment = true;
                  pStereoCompartment.onGraphEvent(IGraphEventKind.GEK_POST_CREATE);
               }
               pStereoCompartment.setReadOnly(true);
            }
         }
         else if (pStereoCompartment != null)
         {
            // No stereotypes, we need to remove this compartment
            removeCompartment((ICompartment) pStereoCompartment, false);
            bAddedOrRemovedCompartment = true;
         }
      }
      else if (pStereoCompartment != null)
      {
         // No modelelement, we need to remove this compartment
         removeCompartment((ICompartment) pStereoCompartment, false);
         bAddedOrRemovedCompartment = true;
      }

      return bAddedOrRemovedCompartment;
   }

   protected boolean updateTaggedValuesCompartment(IElement pElement)
   {
      boolean bAddedOrRemovedCompartment = false;

      ITaggedValuesCompartment pTaggedValuesCompartment = getCompartmentByKind(ITaggedValuesCompartment.class);
      if (pElement != null)
      {
         ETList < ITaggedValue > pValues = null;
         boolean bFoundTaggedValue = false;

         pValues = pElement.getTaggedValues();
			int nCount = pValues != null ? pValues.size() : 0;
         if (nCount > 0)
         {
            boolean bDoAdd = false;
            if (pTaggedValuesCompartment == null)
            {
               // Create the compartment
               pTaggedValuesCompartment = new ETTaggedValuesCompartment();
               pTaggedValuesCompartment.setEngine(m_engine);
               pTaggedValuesCompartment.addModelElement(pElement, -1);
               bDoAdd = true;
            }

            if (pTaggedValuesCompartment != null)
            {
               String finalName = "{";
               for (int i = 0; i < nCount; i++)
               {
                  ITaggedValue pValue = pValues.get(i);
                  if (pValue != null)
                  {
							String name = pValue.getNameWithAlias();
							String value = pValue.getDataValue();

                     // throw out the documentation property
                     String sName = name.toLowerCase();
                     if (sName == "documentation")
                     {
                        continue;
                     }

                     if (i > 0)
                     {
                        finalName += ",";
                     }
                     
                     finalName += name;
                     finalName += "=";
                     finalName += value;
                     bFoundTaggedValue = true;
                  }
               }
               
               finalName += "}";
               pTaggedValuesCompartment.setName(finalName);
               if (bDoAdd)
               {
                  super.addCompartment((ICompartment) pTaggedValuesCompartment, getCompartmentListIndex(OC_TAGGEDVALUES), false);
                  bAddedOrRemovedCompartment = true;
                  pTaggedValuesCompartment.onGraphEvent(IGraphEventKind.GEK_POST_CREATE);

                  // If we're connected go ahead and initialize the resources, otherwise
                  // they'll get initialized when the draw engine's get initialized.
                  if (m_ResourceUser.getResourceMgr() != null)
                  {
                     pTaggedValuesCompartment.initResources();
                  }
               }
               pTaggedValuesCompartment.setReadOnly(true);
            }
         }

         if (bFoundTaggedValue == false && pTaggedValuesCompartment != null)
         {
            // No tagged values, we need to remove this compartment
            removeCompartment((ICompartment) pTaggedValuesCompartment, false);
            bAddedOrRemovedCompartment = true;
         }
      }
      else if (pTaggedValuesCompartment != null)
      {
         // No modelelement, we need to remove this compartment
         removeCompartment((ICompartment) pTaggedValuesCompartment, false);
         bAddedOrRemovedCompartment = true;
      }
      return bAddedOrRemovedCompartment;
   }

   protected boolean updatePackageImportCompartment(IElement pElement)
   {
      boolean bAddedOrRemovedCompartment = false;

      if (m_EnablePackageImportCompartment)
      {
         IPackageImportCompartment pPackageImportCompartment = getCompartmentByKind(IPackageImportCompartment.class);
         if (pElement != null)
         {
            String sPackageImportText = getPackageImportText(pElement);
            if (sPackageImportText != null && sPackageImportText.length() > 0)
            {
               boolean bDoAdd = false;
               if (pPackageImportCompartment == null)
               {
                  // Create the compartment
                  pPackageImportCompartment = new ETPackageImportCompartment();
                  pPackageImportCompartment.setEngine(m_engine);
                  pPackageImportCompartment.addModelElement(pElement, -1);
                  bDoAdd = true;
               }

               if (pPackageImportCompartment != null)
               {
                  pPackageImportCompartment.setName(sPackageImportText);
                  if (bDoAdd)
                  {
                     super.addCompartment(pPackageImportCompartment, getCompartmentListIndex(OC_PACKAGEIMPORT), false);
                     bAddedOrRemovedCompartment = true;
                     pPackageImportCompartment.onGraphEvent(IGraphEventKind.GEK_POST_CREATE);

                     // If we're connected go ahead and initialize the resources, otherwise
                     // they'll get initialized when the draw engine's get initialized.
                     if (m_ResourceUser.getResourceMgr() != null)
                     {
                        pPackageImportCompartment.initResources();
                     }
                  }
                  pPackageImportCompartment.setReadOnly(true);
               }
            }
            else if (pPackageImportCompartment != null)
            {
               // No namespace text - we need to remove this compartment
               removeCompartment(pPackageImportCompartment, false);
               bAddedOrRemovedCompartment = true;
            }
         }
         else if (pPackageImportCompartment != null)
         {
            // No modelelement, we need to remove this compartment
            removeCompartment(pPackageImportCompartment, false);
            bAddedOrRemovedCompartment = true;
         }
      }
      return bAddedOrRemovedCompartment;
   }

   protected boolean updateTemplateParametersCompartment(IElement pElement)
   {
      boolean bAddedOrRemovedCompartment = false;

      if (m_EnableTemplateParameter)
      {
         ITemplateParametersCompartment pTemplateParametersCompartment = getCompartmentByKind(ITemplateParametersCompartment.class);
         IClassifier pNamedElement = null;
         int parametersCount = 0;
         if (pElement instanceof IClassifier)
         {
            pNamedElement = (IClassifier) pElement;
            ETList < IParameterableElement > pParameters = pNamedElement.getTemplateParameters();
            if (pParameters != null)
            {
               parametersCount = pParameters.getCount();
            }
         }

         if (parametersCount > 0)
         {
            boolean bDoAdd = false;
            if (pTemplateParametersCompartment == null)
            {
               // Create the compartment
               //					pTemplateParametersCompartment = new ETTemplateParametersCompartment();
               //					pTemplateParametersCompartment.setEngine(m_engine);
               bDoAdd = true;
            }

            if (pTemplateParametersCompartment != null)
            {
               if (bDoAdd)
               {
                  //						super.addCompartment( pTemplateParametersCompartment, getCompartmentListIndex(OC_TEMPLATE_PARAMETERS), false);
                  bAddedOrRemovedCompartment = true;
                  //						pTemplateParametersCompartment.onGraphEvent(IGraphEventKind.GEK_POST_CREATE);

                  // If we're connected go ahead and initialize the resources, otherwise
                  // they'll get initialized when the draw engine's get initialized.
                  if (m_ResourceUser.getResourceMgr() != null)
                  {
                     //							pTemplateParametersCompartment.initResources();
                  }
               }
               // Adding the template parameter compartment will recalculate the name
               //					pTemplateParametersCompartment.addModelElement( pElement,-1 );

               //					pTemplateParametersCompartment.setReadOnly(true);
            }
         }
         else if (pTemplateParametersCompartment != null)
         {
            // No modelelement, we need to remove this compartment
            //				removeCompartment(pTemplateParametersCompartment, false);
            bAddedOrRemovedCompartment = true;
         }
      }

      return bAddedOrRemovedCompartment;
   }

   public boolean updateStaticTextCompartment()
   {
      boolean bAddedOrRemovedCompartment = false;

      IADStaticTextCompartment pStaticTextCompartment = getCompartmentByKind(IADStaticTextCompartment.class);
      if (m_StaticText != null && m_StaticText.length() > 0)
      {
         boolean bDoAdd = false;
         if (pStaticTextCompartment == null)
         {
            // Create the compartment
            pStaticTextCompartment = new ETStaticTextCompartment();
            pStaticTextCompartment.setEngine(m_engine);
            bDoAdd = true;
         }

         if (pStaticTextCompartment != null)
         {
            if (bDoAdd)
            {
               super.addCompartment(pStaticTextCompartment, getCompartmentListIndex(OC_STATICTEXT), false);
               bAddedOrRemovedCompartment = true;
               pStaticTextCompartment.onGraphEvent(IGraphEventKind.GEK_POST_CREATE);

               // If we're connected go ahead and initialize the resources, otherwise
               // they'll get initialized when the draw engine's get initialized.
               if (m_ResourceUser.getResourceMgr() != null)
               {
                  pStaticTextCompartment.initResources();
               }
            }
            pStaticTextCompartment.setName(m_StaticText);
            pStaticTextCompartment.setReadOnly(true);
         }
      }
      else if (pStaticTextCompartment != null)
      {
         // No modelelement, we need to remove this compartment
         removeCompartment(pStaticTextCompartment, false);
         bAddedOrRemovedCompartment = true;
      }

      return bAddedOrRemovedCompartment;
   }

   public boolean updateAllOptionalCompartments(IElement pElement)
   {
      boolean bAddedOrRemovedCompartment = false;

      IElement pTempElement = pElement;
      if (pTempElement == null)
      {
         IDrawEngine pEngine = getEngine();
         if (pEngine != null)
         {
            pTempElement = TypeConversions.getElement(pEngine);
         }
      }

      if (pTempElement != null)
      {
         boolean bAddedStereo = updateStereotypeCompartment(pTempElement);
         boolean bAddedTaggedValues = updateTaggedValuesCompartment(pTempElement);
         boolean bAddedPackageImport = updatePackageImportCompartment(pTempElement);
         boolean bAddedTemplateParameters = false; //updateTemplateParametersCompartment(pTempElement);
         boolean bAddedStaticTextCompartment = updateStaticTextCompartment();
         if (bAddedPackageImport || bAddedTaggedValues || bAddedPackageImport || bAddedTemplateParameters || bAddedStaticTextCompartment)
         {
            bAddedOrRemovedCompartment = true;
            getEngine().invalidate();
         }
      }

      return bAddedOrRemovedCompartment;
   }

   /**
    * Called when the context menu is about to be displayed.  The compartment should add whatever buttons
    * it might need.
    *
    * @param pContextMenu [in] The context menu about to be displayed
    * @param logicalX [in] The logical x location of the context menu event
    * @param logicalY [in] The logical y location of the context menu event
    */
   public void onContextMenu(IMenuManager manager)
   {
      if (getEnableContextMenu())
      {
         Point point = manager.getLocation();
         
         // (LLS) Adding the buildContext logic to support A11Y issues.  The
         // user should be able to use the SHIFT-F10 keystroke to activate
         // the context menu.  In the case of the keystroke the location
         // will not be valid.  Therefore, we have to just check if the
         // compartment is selected.
         //
         // A list compartment can not be selected.  Therefore, when
         // SHIFT-F10 is pressed, we must always show the list compartment
         // menu items.
         boolean buildMenu = true;
         if(point != null)
         {
             buildMenu = containsPoint(point);
         }
         
         if (buildMenu == true)
         {
            int count = getNumCompartments();
            for (int i = 0; i < count; i++)
            {
               ICompartment pComp = getCompartment(i);
               pComp.onContextMenu(manager);
            }
         }
      }
   }

   /**
    * Adds a compartment. The type of compartment added is determined by its contents, therefore pCompartment must
    * not be null.  If the compartment contains a model element it is considered to the a name compartment for the
    * class.  If there is no model element the compartment's is considered to be either a stereotype or property and
    * it is merely inserted at the bottom of the list (or the position indicated).
    *
    * @param pCompartment - The compartment to add.  If NULL nothing happens
    * @param nIndex - The position in the visible list to place this compartment
    */
   public long addCompartment(ICompartment pCompartment, int nIndex, boolean bRedrawNow)
   {
      // if the compartment in a classnamecompartment
      // otherwise it might be a stereotype or a property
      if (pCompartment != null)
      {
         String xmiid = pCompartment.getModelElementXMIID();
         if (xmiid != null && xmiid.length() > 0)
         {
            String compartmentID = pCompartment.getCompartmentID();
            if (compartmentID != null && compartmentID.equals(m_NameCompartmentID))
            {
               if (pCompartment instanceof IADEditableCompartment)
               {
                  setDefaultCompartment(pCompartment);
               }
            }
         }

         //call addCompartment from ListCompartment
         super.addCompartment(pCompartment, nIndex, bRedrawNow);
      }
      return 0;
   }

   /**
    *
    * Sets the default compartment
    *
    * @param *pCompartment[in] The default compartment
    *
    * @return HRESULT
    *
    */
   public void setDefaultCompartment(ICompartment pComp)
   {
      m_DefaultCompartment = (IADEditableCompartment) pComp;
   }

   /**
    *
    * Returns the name compartment in this list. By default the name compartment 
    * receives keyboard input when none other has been selected.
    *
    * @param *pCompartment[out] A pointer to the name compartment
    *
    * @return HRESULT
    *
    */
   public ICompartment getDefaultCompartment()
   {
      return m_DefaultCompartment;
   }

   /**
    * Update from archive.
    *
    * @param pProductArchive [in] The archive we're reading from
    * @param pCompartmentElement [in] The element where this compartment's information should exist
    */
   public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {
      if (pProductArchive != null && pElement != null)
      {
         //call from super class ADListCompartmentImpl
         super.readFromArchive(pProductArchive, pElement);

         // read in our stuff
         m_NameCompartmentID = pElement.getAttributeString(DEFAULTCOMPARTMENTKIND_STRING);
         m_StaticText = pElement.getAttributeString(STATICTEXT_STRING);
      }
   }

   /**
    * Write ourselves to archive.
    *
    * @param pProductArchive [in] The archive we're saving to
    * @param pElement [in] The current element, or parent for any new attributes or elements
    * @param pCompartmentElement [out] The created element for this compartment's information
    */
   public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {
      IProductArchiveElement retObj = super.writeToArchive(pProductArchive, pElement);
      if (retObj != null)
      {
         retObj.addAttributeString(DEFAULTCOMPARTMENTKIND_STRING, m_NameCompartmentID);
         retObj.addAttributeString(STATICTEXT_STRING, m_StaticText);
      }
      return retObj;
   }

   /**
    * This is the name of the drawengine used when storing and reading from the product archive.
    *
    * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
    * product archive (etlp file).
    */
   public String getCompartmentID()
   {
      return "ADNameListCompartment";
   }

   /**
    * Notifier that the model element has changed.
    *
    * @param pTargets [in] Information about what has changed.
    */
   public long modelElementHasChanged(INotificationTargets pTargets)
   {
      // only allow the name compartment to use it's own resources
      if (m_DefaultCompartment != null)
      {
         m_DefaultCompartment.modelElementHasChanged(pTargets);

         // If the edit compartment wraps then don't resize to fit or it'll
         // end up fitting to one big long compartment
         boolean textWrap = m_DefaultCompartment.getTextWrapping();
         if (!textWrap)
         {
            ensureVisible(this, m_resizeable);
         }
      }

      if (updateAllOptionalCompartments(null))
      {
         setIsDirty();
      }

      return 0;
   }

   public long modelElementDeleted(INotificationTargets pTargets)
   {
      // Update the optional compartments
      if (updateAllOptionalCompartments(null))
      {
         setIsDirty();
      }

      return 0;
   }
   /**
    * Adds a model element to this compartment.
    *
    * @param pElement [in] The element to add
    * @param nIndex [in] Not Used
    */
   public void addModelElement(IElement pElement, int nIndex)
   {
      m_modelElement = pElement;

      clearVisibleCompartments();

      if (m_engine != null)
      {
  
         // Reset the name compartment
         setDefaultCompartment(null);

         if (m_NameCompartmentID == null)
         {
            m_NameCompartmentID = "ADClassNameCompartment";
         }

         // Get or create the name compartment based on the string the metatype 
         // set in put_NameCompartment
         ICompartment pComp = getCompartmentByCompartmentName(m_NameCompartmentID);
		
			boolean addComp = pComp == null;

         if (addComp)
         {
            // Create the compartment
            pComp = DrawingFactory.retrieveCompartment(m_NameCompartmentID);
         }
				

         // Set the default compartment
         setDefaultCompartment(pComp);

         if (m_DefaultCompartment != null)
         {
            m_DefaultCompartment.setEngine(m_engine);
            m_DefaultCompartment.addModelElement(pElement, -1);

            if (addComp)
            {
               super.addCompartment(m_DefaultCompartment, -1, true);
            }

            if (m_ResourceUser.getResourceMgr() != null)
            {
               m_DefaultCompartment.initResources();
            }

            // Fix W6837:  Engine's default compartment should not be set by zone compartments
            if (m_SetEnginesDefaultCompartment)
            {
               m_engine.setDefaultCompartment(m_DefaultCompartment);
            }
         }

         ///////////////////////////////////////////////////////////////////
         // Create the optional compartments (stereotype, tagged values and package import)
         updateAllOptionalCompartments(pElement);
      }
   }

   public < Type > Type getCompartmentByKind(Class interfacetype)
   {
      try
      {
         IteratorT < ICompartment > iter = new IteratorT < ICompartment > (this.getCompartments());
         while (iter.hasNext())
         {
            ICompartment comp = iter.next();
            if (interfacetype.isAssignableFrom(comp.getClass()))
               return (Type) comp;
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;
   }

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#validate(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
   public boolean validate(IElement pElement)
   {
      // attaching occurs after the compartment has been initialized
      return m_DefaultCompartment != null && m_DefaultCompartment.validate(pElement);
   }
}
