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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import java.util.StringTokenizer;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngineFactory;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.ElementReloader;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.GraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypeDetails;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdgeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphManager;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.graph.TSGraph;
import com.tomsawyer.graph.TSGraphManager;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSConstSize;
import com.tomsawyer.drawing.geometry.TSConstSize;
import com.tomsawyer.util.TSObject;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/**
 * @author sumitabhk
 *
 */
public class ETBaseUI
{
   public static void readFromArchive(IProductArchive prodArch, IProductArchiveElement archEle, IETGraphObjectUI ui)
   {
      //Debug		long timeStart = System.currentTimeMillis();

      String tempStr = archEle.getAttributeString(IProductArchiveDefinitions.MEID_STRING);
      if (tempStr != null && tempStr.length() > 0)
      {
         ui.setReloadedModelElementXMIID(tempStr);
      }

      tempStr = archEle.getAttributeString(IProductArchiveDefinitions.TOPLEVELID_STRING);
      if (tempStr != null && tempStr.length() > 0)
      {
         ui.setReloadedTopLevelXMIID(tempStr);
      }

      tempStr = archEle.getAttributeString(IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING);
      if (tempStr != null && tempStr.length() > 0)
      {
         ui.setReloadedPresentationXMIID(tempStr);
      }

      tempStr = archEle.getAttributeString(IProductArchiveDefinitions.OWNER_PRESENTATIONELEMENT);
      if (tempStr != null && tempStr.length() > 0)
      {
         ui.setReloadedOwnerPresentationXMIID(tempStr);
      }

      tempStr = archEle.getAttributeString(IProductArchiveDefinitions.REFERREDELEMENTS_STRING);
      if (tempStr != null && tempStr.length() > 0)
      {
         // Split on the delimiter (" ") and add it to the m_PresentationReferenceReferredElements
         // list
         StringTokenizer tokenizer = new StringTokenizer(tempStr, " ");
         IStrings m_PresentationReferenceReferredElements = new Strings();
         while (tokenizer.hasMoreTokens())
         {
            m_PresentationReferenceReferredElements.add(tokenizer.nextToken());
         }
         ui.setReferredElements(m_PresentationReferenceReferredElements);
      }

      tempStr = archEle.getAttributeString(IProductArchiveDefinitions.INITIALIZATIONSTRING_STRING);
      if (tempStr != null && tempStr.length() > 0)
      {
         ui.setInitStringValue(tempStr);
      }

      IProductArchiveElement engineElement = archEle.getElement(IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
      ui.setDrawEngineClass(engineElement.getAttributeString(IProductArchiveDefinitions.ENGINENAMEATTRIBUTE_STRING));

      ElementReloader reloader = new ElementReloader();

      // See if this element has been deleted
      if (archEle != null)
      {
         ui.setWasModelElementDeleted(archEle.getAttributeBool(IProductArchiveDefinitions.PE_DELETED));
      }

      String reloadedXMIID = ui.getReloadedModelElementXMIID();
      String reloadedTopId = ui.getReloadedTopLevelXMIID();

      if (reloadedXMIID.length() > 0 && reloadedTopId.length() > 0)
      {
         // Need to reload the model element and reattach
         IElement modEle = reloader.getElement(reloadedTopId, reloadedXMIID);
         if (modEle != null)
         {
            attachAndCreatePresentationElement(modEle, ui.getInitStringValue(), false, ui);
            ui.setModelElement(modEle);
         }
      }

      // Create a new engine from the archive
      try
      {
         IDrawEngine de = DrawingFactory.createDrawEngine(ui, prodArch, engineElement);

         ui.setDrawEngine(de);
         //			if (de != null)
         //			{
         //				de.init();
         //			}
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      //Debug
      //		long timeFinish = System.currentTimeMillis();
      //		long tsDelta = timeFinish - timeStart;
      //		ETSystem.out.println(tempStr + " with ID: " + reloadedXMIID + " " + " initialized in ( " + StringUtilities.timeToString(tsDelta, 3) + " )");
   }

   /**
    * This routine is called when a node or edge needs to be attached to an existing model element.
    * For example, during a drag and drop operation a model element could be dragged off of the
    * project tree and dropped onto the drawing area.  The drawing area activeX control calls
    * attach on the node so that the node can create a presentation element, attach the model element
    * presentation element and then create the appropriate draw engine.
    *
    * This version allows you to specify if the draw engine should be created.  Right now you only
    * specify false when reloading from a file because the .etld file is read in first and then the
    * .etlp.  We don't want the .etld file to create an engine only to destroy it when we re-created one based on
    * the .etlp file.
    *
    * @param pElement [in] The element to attach to
    * @param sInitializationString [in] A string coming from the presentation types file telling us how
    * to initialize
    * @param bCreateEngine [in] true to create the draw engine.
    *
    */
   public static void attachAndCreatePresentationElement(IElement pElem, String initStr, boolean bCreateEngine, IETGraphObjectUI ui)
   {
      // This routine is called when a model element is dragged from the project tree onto the
      // drawing area (for example).  We have a model element and we need to create a presentation
      // element and hook things up.
      ui.setInitStringValue(initStr);
      IETGraphObject etObj = null;
      ITSGraphObject graphObj = ui.getTSObject();
      if (graphObj != null)
      {
         if (graphObj.isNode())
         {
            etObj = (IETNode)graphObj;
         }
         else if (graphObj.isEdge())
         {
            etObj = (IETEdge)graphObj;
         }
         else if (graphObj.isLabel())
         {
            etObj = (IETLabel)graphObj;
         }
      }

      if (bCreateEngine)
      {
         createDrawEngine(initStr, ui);
      }

      if (pElem != null)
      {
         IPresentationElement pEle = null;
         if (etObj != null)
         {
            pEle = etObj.getPresentationElement();
         }

         if (pEle != null)
         {
            // We have a presentation element already, just initialize the engine
            initializeEngine(pEle, ui);
         }
         else
         {
            IPresentationElement presEle = ui.createPresentationElement(pElem);
            if (presEle != null)
            {
               // If we have a reloaded presentation XMIID then this node has been
               // previously saved to a .etld and .etlp file.  We need to reuse this id
               // so we call a method on IVersionableElement to force the id back to
               // what it was when the node was originally saved.
               String reloadedXMIID = ui.getReloadedPresentationXMIID();
               if (reloadedXMIID != null && reloadedXMIID.length() > 0)
               {
                  presEle.setXMIID(reloadedXMIID);
               }

               // Attach the PE up to the model element if not already present - already present
               // happens in the case of a transformed element

               if (!pElem.isPresent(presEle))
               {
                  // Make sure to remove any existing subjects
                  IElement firstSubject = presEle.getFirstSubject();
                  if (firstSubject != null)
                  {
                     presEle.removeSubject(firstSubject);
                  }
                  presEle.addSubject(pElem);
               }

               // Tell the IETElement (the TSNode) that it is being represented
               // by this presentation element
               if (etObj != null)
               {
                  etObj.setPresentationElement(presEle);
               }
            }
         }
      }
   }

   /**
    * Initializes the draw engine and resource user
    */
   public static void initializeEngine(IPresentationElement pElement, IETGraphObjectUI ui)
   {
      IDrawEngine drawEngine = ui.getDrawEngine();
      if (drawEngine != null && pElement != null)
      {
         try
         {
            drawEngine.init();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   /**
    * Creates a drawing engine based on the initialization string
    *
    * @param szInitializationString [in] The initialization string used to create the engine
    */
   public static void createDrawEngine(String initStr, IETGraphObjectUI ui)
   {
      IDrawEngine drawEngine = null; //ui.getDrawEngine();
      if (ui != null)
      {
         if (initStr != null && initStr.length() > 0)
         {
            ui.setInitStringValue(initStr);
         }

         // First see if we have a draw engine type in the presentation file.
         IDrawingAreaControl control = ui.getDrawingArea();
         if (control != null)
         {
            IPresentationTypesMgr presMgr = control.getPresentationTypesMgr();
            if (presMgr != null)
            {
               String fullInitStr = ui.getInitStringValue();

               int diaKind = control.getDiagramKind();
               PresentationTypeDetails details = presMgr.getInitStringDetails(fullInitStr, diaKind);
               if (details != null)
               {
                  String drawEngineType = details.getEngineName();
                  if (drawEngineType != null && drawEngineType.length() > 0)
                  {
                     // Create a new engine from the archive
                     try
                     {
                        drawEngine = ETDrawEngineFactory.createDrawEngine(drawEngineType);
                        if (drawEngine != null)
                        {
                           drawEngine.setParent(ui);
                           ui.setDrawEngine(drawEngine);
                           drawEngine.init();
                        }
                        else
                        {
                           ETSystem.out.print("Warning failed to create Object Drawing ");
                           ETSystem.out.println(drawEngineType);
                        }
                     }
                     catch (ETException e)
                     {
                        e.printStackTrace();
                     }
                  }
               }
            }
         }
      }
   }

   /*
    * 
    */
   public static void createDrawEngineOneTime(IETGraphObjectUI ui)
   {
      IDrawEngine drawEngine = ui.getDrawEngine();
      boolean failedToCreateDrawEngine = ui.getFailedToCreateDrawEngine();
      if (drawEngine == null && !failedToCreateDrawEngine)
      {
         createDrawEngine(ui.getInitStringValue(), ui);
         drawEngine = ui.getDrawEngine();
         if (drawEngine == null)
         {
            // We failed to create the engine.  Don't try again or draw might constantly be
            // pumping out messages or asserting.
            ui.setFailedToCreateDrawEngine(true);
         }
         else
         {
            IPresentationElement pEle = ((IETGraphObject)ui.getTSObject()).getPresentationElement();
            if (pEle != null)
            {
               initializeEngine(pEle, ui);
            }
         }
      }
   }

   public static void writeToArchive(IProductArchive prodArch, IProductArchiveElement archEle, IETGraphObjectUI ui)
   {
      // 1.  This may be duplicated in the .etld file, but the .etld file is not readable by us.  It's a 
      // serialized BSTR that is different under unicode vs non-unicode.  ARG!! So we also put out the
      // model element and toplevel element here.
      String meid = "";
      String topLevelId = "";
      String presEleId = "";
      String ownerPresEleId = "";
      IPresentationElement pPE = ((IETGraphObject)ui.getTSObject()).getPresentationElement();
      IElement modEle = null;
      if (pPE != null)
      {
         modEle = pPE.getFirstSubject();

         // Get the presentation element id
         presEleId = pPE.getXMIID();

         IElement owner = pPE.getOwner();
         if (owner != null && owner instanceof IPresentationElement)
         {
            ownerPresEleId = ((IPresentationElement)owner).getXMIID();
         }
      }

      if (modEle != null)
      {
         meid = modEle.getXMIID();
         topLevelId = modEle.getTopLevelId();
         if (topLevelId == null) 
             topLevelId = "";
      }

      // If we're disconnected from the presentation element we don't want to loose our ids.  So
      // preserve the ids.
      if (meid.length() == 0 && topLevelId.length() == 0 && presEleId.length() == 0 && ownerPresEleId.length() == 0)
      {
         meid = ui.getReloadedModelElementXMIID();
         topLevelId = ui.getReloadedTopLevelXMIID();
         presEleId = ui.getReloadedPresentationXMIID();
         ownerPresEleId = ui.getReloadedOwnerPresentationXMIID();
      }

      if (presEleId.length() > 0)
      {
         archEle.addAttributeString(IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING, presEleId);
      }

      if (meid.length() > 0)
      {
         archEle.addAttributeString(IProductArchiveDefinitions.MEID_STRING, meid);
      }

      if (topLevelId.length() > 0)
      {
         archEle.addAttributeString(IProductArchiveDefinitions.TOPLEVELID_STRING, topLevelId);
      }

      if (ownerPresEleId.length() > 0)
      {
         archEle.addAttributeString(IProductArchiveDefinitions.OWNER_PRESENTATIONELEMENT, ownerPresEleId);
      }

      // 2.  PresentationReference relationships are deleted because they are owned by presentation elements
      // which do not get saved in the model file.  So we store the information necessary to recreate them here.
      // The get recreated when the diagram is reloaded.
      if (pPE != null)
      {
         ETList < IPresentationElement > referredElems = PresentationReferenceHelper.getHasReferredElements(pPE);
         if (referredElems != null)
         {
            ui.setReferredElements(null);
            IStrings refElems = new Strings();
            int count = referredElems.size();
            for (int i = 0; i < count; i++)
            {
               IPresentationElement refEle = referredElems.get(i);
               String id = refEle.getXMIID();
               if (id != null && id.length() > 0)
               {
                  refElems.add(id);
               }
            }
            ui.setReferredElements(refElems);
         }
      }

      // 3. Save what's currently in the m_PresentationReferenceReferredElements list.
      IStrings strs = ui.getReferredElements();
      if (strs != null)
      {
         String attrVal = strs.getListAsDelimitedString(" ");
         if (attrVal != null && attrVal.length() > 0)
         {
            // Save off this attribute
            archEle.addAttributeString(IProductArchiveDefinitions.REFERREDELEMENTS_STRING, attrVal);
         }
      }

      // Add our initialization string
      String initStr = ui.getInitStringValue();
      if (initStr != null && initStr.length() > 0)
      {
         archEle.addAttributeString(IProductArchiveDefinitions.INITIALIZATIONSTRING_STRING, initStr);
      }

      // Save the graphics engine stuff
      IDrawEngine engine = ui.getDrawEngine();
      if (engine != null && prodArch != null && archEle != null)
      {
         // This call will allow the drawengine and each compartment to save stuff to the
         // product archive.
         engine.writeToArchive(prodArch, archEle);
      }
   }

   public static void onContextMenu(IMenuManager manager, IETGraphObjectUI ui)
   {
      if (ui != null)
      {
         IDrawEngine engine = ui.getDrawEngine();
         if (engine != null)
         {
            // Forward the context menu message to the engine
            engine.onContextMenu(manager);

            // If we have a label manager then pass the message to it as well
            ILabelManager labelMgr = engine.getLabelManager();
            if (labelMgr != null)
            {
               labelMgr.onContextMenu(manager);
            }

            // If we have a edge manager then pass the message to it as well
            IEventManager eventMgr = engine.getEventManager();
            if (eventMgr != null)
            {
               eventMgr.onContextMenu(manager);
            }
         }
      }
   }

   /**
    * Returns the model element id and toplevel id for the presentation element this object represents
    *
    * @param sPresentationElementID [in] The presentation element XMIID
    * @param sMEID [in] The model element XMIID
    * @param sTopLevelID [in] The toplevel element XMIID (the IProject XMIID)
    * @param sOwnerPresentationElementID [in] The owner presentation element XMIID, if this object has an owner.
    */
   private static void getXMIIDs(String presEleId, String meid, String topLevelId, String ownerId, IETGraphObjectUI ui)
   {
      //This method needs to return all this, for the time being I am copying this method at one place I needed it.
      IPresentationElement pPE = ((IETGraphObject)ui.getTSObject()).getPresentationElement();
      IElement modEle = null;
      if (pPE != null)
      {
         modEle = pPE.getFirstSubject();

         // Get the presentation element id
         presEleId = pPE.getXMIID();

         IElement owner = pPE.getOwner();
         if (owner != null && owner instanceof IPresentationElement)
         {
            ownerId = ((IPresentationElement)owner).getXMIID();
         }
      }

      if (modEle != null)
      {
         meid = modEle.getXMIID();
         topLevelId = modEle.getTopLevelId();
      }

   }

   public static void save(IProductArchive prodArch, IETGraphObjectUI ui)
   {
      if (ui != null)
      {
         ITSGraphObject obj = ui.getTSObject();
         if (obj != null && obj instanceof IETGraphObject)
         {
            IETGraphObject etObj = (IETGraphObject)obj;
            IPresentationElement presEle = etObj.getPresentationElement();
            if (presEle != null)
            {
               String xmiid = presEle.getXMIID();
               if (xmiid != null && xmiid.length() > 0)
               {
                  IProductArchiveElement createdEle = prodArch.createElement(xmiid);
                  if (createdEle != null)
                  {
                     etObj.writeToArchive(prodArch, createdEle);
                  }
               }
            }
         }
      }
   }

   /**
    * This routine is called when a TSENodeView needs to be created from scratch.  The user has dropped
    * a TS node on the tree and we need to create the appropriate model element and presentation elements and
    * tie them together.  After all that is done look at the initialization string and create the correct engine.
    */
   public static IElement create(INamespace pNamespace, String initStr, IETGraphObjectUI ui)
   {
      IElement createdEle = null;

      // Go through the base create logic which will create the correct IElement, stick it
      // in the namespace and attach it to this node
      createdEle = ui.createNew(pNamespace, initStr);
      // If we have a good model element then create the presentation element and
      // hook the two up.
      if ( createdEle != null )
      {
      IDrawEngine drawEngine = ui.getDrawEngine();
      ITSGraphObject tsObj = ui.getTSObject();
      if(tsObj instanceof IETGraphObject)
      {
         IETGraphObject etObj = (IETGraphObject)tsObj;
         IPresentationElement presEle = etObj.getPresentationElement();
         if (presEle != null)
         {
            initializeEngine(presEle, ui);
         }
      }

      // Create our draw engine
      createDrawEngineOneTime(ui);

      // Create our label manager in case we have labels
      IDrawEngine pEng = ui.getDrawEngine();
      if (pEng != null)
      {
         ILabelManager labelMgr = pEng.getLabelManager();

         if (labelMgr != null)
         {
            labelMgr.createInitialLabels();
         }

         // Tell the draw engine to draw with a red border if it's not well.
         pEng.setCheckSyncStateDuringDraw(true);
      }
      }

      return createdEle;
   }

   /**
    * This routine is called when a node or edge needs to be attached to an existing model element.
    * For example, during a drag and drop operation a model element could be dragged off of the
    * project tree and dropped onto the drawing area.  The drawing area activeX control calls
    * attach on the node so that the node can create a presentation element, attach the model element
    * presentation element and then create the appropriate draw engine.
    *
    * @param pElement [in] The element to attach to
    * @param sInitializationString [in] A string coming from the presentation types file telling us how
    * to initialize
    */
   public static void attach(IElement modEle, String initStr, IETGraphObjectUI ui)
   {
      attachAndCreatePresentationElement(modEle, initStr, true, ui);

      // Create our draw engine
      IDrawEngine pEng = ui.getDrawEngine();
      if (pEng == null)
      {
         createDrawEngine(initStr, ui);

         // Tell the draw engine to draw with a red border if it's not well.
         pEng = ui.getDrawEngine();
         if (pEng != null)
         {
            pEng.setCheckSyncStateDuringDraw(true);
         }
      }
   }

   /**
    * Called to notify the node that a link has been added.
    *
    * @param pNewLink [in] The link about to be added
    * @param bIsFromNode [in] true if this is the from node.
    */
   public static void onPostAddLink(IETGraphObject newLink, boolean isFromNode, IETGraphObjectUI ui)
   {
      IDrawEngine pEng = ui.getDrawEngine();
      if (pEng != null)
      {
         IEventManager eventMgr = pEng.getEventManager();
         if (eventMgr != null)
         {
            eventMgr.onPostAddLink(newLink, isFromNode);
         }
      }
   }

   /**
    * Returns the type of model element to create based on the initialization string
    *
    * @return The metatype that should be created when this graph object is created.
    */
   public static String getMetaType(IETGraphObjectUI ui)
   {
      IDrawingAreaControl control = ui.getDrawingArea();
      if (control != null)
      {
         IPresentationTypesMgr presMgr = control.getPresentationTypesMgr();
         if (presMgr != null)
         {
            PresentationTypeDetails details = presMgr.getInitStringDetails(ui.getInitStringValue(), control.getDiagramKind());
            if (details != null)
            {
               return details.getMetaType();
            }
         }
      }
      return null;
   }

   public static void onGraphEvent(int nKind, IETGraphObjectUI ui)
   {
      if (ui == null)
         return;

      // If we have a postpaste or postduplicate then we need to get rid of the presentation element
      // so we don't duplicate the presentation element of the guy we were copied from.
      if (nKind == IGraphEventKind.GEK_POST_PASTE_VIEW)
      {
         // Clear out the reloaded stuff.  Not really necessary, but we haven't been
         // reloaded so lets just clear them out just in case.
         //IETGraphObject pETGraphObject = TypeConversions.getETGraphObject((TSEObjectUI)ui.);

			IETGraphObject pETGraphObject = (IETGraphObject)ui.getOwner();

         if (pETGraphObject != null)
         {
            pETGraphObject.setReloadedModelElementXMIID("");
            pETGraphObject.setReloadedTopLevelXMIID("");
            pETGraphObject.setReloadedPresentationXMIID("");
            pETGraphObject.setReloadedOwnerPresentationXMIID("");
         }

         // Clear the user object
         //ClearTSUserObject();

         IElement pElement = null;
         IElement pOwner = null;

         IPresentationElement cpPE = pETGraphObject.getPresentationElement();

         if (cpPE != null)
         {         	
            // Get the model element and owner for the copied presentation element
            pElement = cpPE.getFirstSubject();

            if (pElement != null)
            {
               // Create a new presentation element
               IPresentationElement newPE = ui.createPresentationElement(pElement);

               // Preserve the ownership of the original presentation element
               pOwner = cpPE.getOwner();

               IPresentationElement pOwnerPE = (pOwner instanceof IPresentationElement) ? (IPresentationElement)pOwner : null;

               if (pOwnerPE != null)
               {
                  pOwnerPE.addElement(newPE);
               }

               // Reconnect up the new presentation element
               newPE.addSubject(pElement);

					// Tell the product element that this is the new presentation element
					pETGraphObject.setPresentationElement(newPE);

               ((TSEObject)pETGraphObject).setTag(new String("I have been pasted"));

               // make sure we are not pointing to the original node

					ui.setDrawEngine(null);

               if (ui instanceof ETGenericNodeUI)
               {
                  ((ETGenericNodeUI)ui).setOwner((TSENode)pETGraphObject);
               }
               else if (ui instanceof ETGenericEdgeUI)
               {
                  ((ETGenericEdgeUI)ui).setOwner((TSEEdge)pETGraphObject);
               }
               else if (ui instanceof ETGenericEdgeLabelUI)
               {
                  ((ETGenericEdgeLabelUI)ui).setOwner((TSEEdgeLabel)pETGraphObject);
               }
               else if (ui instanceof ETGenericNodeLabelUI)
               {
                  ((ETGenericNodeLabelUI)ui).setOwner((TSENodeLabel)pETGraphObject);
               }
					             
               pETGraphObject.setObjectView((TSEObjectUI)ui);

                // Tell the product element that this is the new presentation element
                pETGraphObject.setPresentationElement(newPE);

            }
         }
      }

      IDrawEngine engine = ui.getDrawEngine();

      if (engine == null)
      {
         return;
      }

      engine.onGraphEvent(nKind);

      IEventManager eventManager = engine.getEventManager();
      if (eventManager != null)
      {
         eventManager.onGraphEvent(nKind);
      }

      ILabelManager labelManager = engine.getLabelManager();
      if (labelManager != null)
      {
         labelManager.onGraphEvent(nKind);
      }
   }

   public static boolean onKeyDown(int nKeyCode, int nShift, IETGraphObjectUI ui)
   {
      boolean handled = false;
      if (ui != null)
      {
         IDrawEngine engine = ui.getDrawEngine();
         if (engine != null)
         {
            handled = engine.onKeydown(nKeyCode, nShift);
         }
      }
      return handled;
   }

   public static boolean onCharTyped(char ch, IETGraphObjectUI ui)
   {
      boolean handled = false;
      if (ui != null)
      {
         IDrawEngine engine = ui.getDrawEngine();
         if (engine != null)
         {
            handled = engine.onCharTyped(ch);
         }
      }
      return handled;
   }

   public static void resetDrawEngine(String sInitializationString, IETGraphObjectUI ui)
   {
      if (ui != null)
      {
         IETGraphObject pThis = (IETGraphObject)ui.getTSObject();
         IDrawEngine m_Engine = ui.getDrawEngine();
         ILabelManager pLabelManager = null;

         // Remove all the labels on this element, we will reset them again
         // once the draw engine has been re-created
         if (m_Engine != null)
         {
            pLabelManager = m_Engine.getLabelManager();
            if (pLabelManager != null)
            {
               pLabelManager.discardAllLabels();
               pLabelManager = null;
            }

            // NULL out the draw engine
            ui.setDrawEngine(null);
         }

         IPresentationElement presEle = ((IETGraphObject)ui.getTSObject()).getPresentationElement();
         IElement pElement = null;
         if (presEle != null)
         {
            pElement = presEle.getFirstSubject();
         }

         // By default views should be resizable
         if (ui instanceof IETNodeUI)
         {
            ((IETNodeUI)ui).setResizable(true);
         }

         // Go through the attach logic to reinitialize the draw engine
         if (pElement != null)
         {
            // Call our base attach which will attach to this IElement - unlike Create this does not
            // create a new IElement, it uses the argument 'pVal'
            ui.setModelElement(pElement);
            attach(pElement, sInitializationString, ui);
            m_Engine = ui.getDrawEngine();
         }

         // Reset the size of this node
         pThis.sizeToContents();

         // Reset the node shape
         if (m_Engine != null)
         {
            if (m_Engine instanceof INodeDrawEngine)
            {
               INodeDrawEngine pNodeDrawEngine = (INodeDrawEngine)m_Engine;
               pNodeDrawEngine.setNodeShape(null);
            }
         }

         // Reset the labels and edges
         pLabelManager = m_Engine.getLabelManager();
         if (pLabelManager != null)
         {
            pLabelManager.resetLabels();
         }
         IEventManager pEventManager = m_Engine.getEventManager();
         if (pEventManager != null)
         {
            pEventManager.resetEdges();
         }
      }
   }

   public static void reattach(IElement pElement, String sInitializationString, IETGraphObjectUI ui)
   {
      // Discard labels
      ILabelManager pLabelManager = null;
      IDrawEngine pEngine = ui.getDrawEngine();
      if (pEngine != null)
      {
         pLabelManager = pEngine.getLabelManager();
         if (pLabelManager != null)
         {
            pLabelManager.discardAllLabels();
         }
      }

      // Reset presentation
      IETGraphObject etObj = null;
      ITSGraphObject graphObj = ui.getTSObject();
      if (graphObj != null)
      {
         if (graphObj instanceof IETNode)
         {
            etObj = (IETNode)graphObj;
         }
         else if (graphObj instanceof IETEdge)
         {
            etObj = (IETEdge)graphObj;
         }

         IPresentationElement cpPE = etObj.getPresentationElement();
         if (cpPE != null)
         {
            cpPE.setXMIID("");
            etObj.setPresentationElement(null);
         }

         // Re-attach to the IElement, because we cleared out the presentation element this should re-create the presentation element
         attachAndCreatePresentationElement(pElement, sInitializationString, true, ui);

         cpPE = etObj.getPresentationElement();
         if (cpPE instanceof IProductGraphPresentation)
         {
            IProductGraphPresentation cpProductGraphPresentation = (IProductGraphPresentation)cpPE;
            cpProductGraphPresentation.invalidate();
         }
      }

      // Reset labels
      if (pEngine != null)
      {
         pLabelManager = pEngine.getLabelManager();
         if (pLabelManager != null)
         {
            pLabelManager.resetLabels();
         }
      }
   }

   static public IETRect getDeviceBounds(TSTransform transform, IETGraphObjectUI ui)
   {
      IETRect deviceRect = transform != null && ui.getOwner() != null ? new ETDeviceRect(transform.boundsToDevice(ui.getOwner().getBounds())) : null;
      if (deviceRect != null)
      {
         // We want the inside bounds of the clipping region.
         deviceRect.deflateRect(1, 1);
      }
      return deviceRect;
   }

   /*
    * Returns the device bounding rectangle.
    */
   static public IETRect getDeviceBounds(TSEGraphics graphics, IETGraphObjectUI ui)
   {
      return graphics != null ? getDeviceBounds(graphics.getTSTransform(), ui) : null;
   }

   /*
    * Returns the device bounding rect.
    */
   static public IETRect getDeviceBounds(IETGraphObjectUI ui)
   {
      TSEGraphWindow wnd = getGraphWindow(ui);
      return wnd != null ? getDeviceBounds(wnd.getTransform(), ui) : null;
   }

   /*
    * Returns the IDrawingArea Interface.
    */
   static public IDrawingAreaControl getDrawingArea(IETGraphObjectUI ui)
   {
      return getDrawingArea(getGraphWindow(ui));
   }

   /*
    * Returns the TSEGraphWindow.
    */
   static public TSEGraphWindow getGraphWindow(IETGraphObjectUI ui)
   {
      TSEGraphWindow retVal = null;
      
      TSEObject owner = ui.getOwner();
      if (ui != null && owner != null)
      {                  
         TSGraph graph =  owner.getOwnerGraph();
         if(graph != null)
         {
            TSGraphManager manager = graph.getOwnerGraphManager();         
            if(manager instanceof TSEGraphManager)
            {
               TSEGraphManager tseManager = (TSEGraphManager)manager;
               retVal = tseManager.getGraphWindow();
               //return ((TSEGraphManager)ui.getOwner().getOwnerGraph().getOwnerGraphManager()).getGraphWindow();
            }
         }
      }
      
      return retVal;
   }

   /*
    * Returns World points, (Logical)
    */
   static public IETRect getLogicalBounds(IETGraphObjectUI ui)
   {
      return ui != null && ui.getOwner() != null ? new ETRectEx(ui.getOwner().getBounds()) : null;
   }

   /*
    * This method returns the bounding box that fully encloses this object in local coordinate system. 
    */
   static public IETRect getLogicalUIBounds(IETGraphObjectUI ui)
   {
      return ui != null && ui.getOwner() != null ? new ETRectEx(ui.getBounds()) : null;
   }

   static public IDrawingAreaControl getDrawingArea(TSEGraphWindow graphWindow)
   {
      return graphWindow instanceof ADGraphWindow ? ((ADGraphWindow)graphWindow).getDrawingArea() : null;
   }

   static public IDrawInfo getDrawInfo(TSEGraphics graphics, IETGraphObjectUI ui)
   {
      // Don't draw invalid DrawEngines.
      if (ui.getDrawEngine() == null)
         return null;

      // Make sure the draw engine is initialized, 
      if (!ui.getDrawEngine().isInitialized())
      {
         // Interactive edges don't have DrawEngines let them continue.
         if (!(ui instanceof IETEdgeUI))
            return null;
      }

      IDrawInfo retVal;
      TSEGraphWindow graphWindow = graphics != null ? graphics.getGraphWindow() : null;
      if (graphWindow != null)
      {
         /*
         if (!graphWindow.getGraph().isBoundsUpdatingEnabled())
         {
         	// Don't the objects draw on graphs with unknow bounds.
         	return null;
         }
         */
         retVal = new ETDrawInfo(graphics);

         if (ui.getDrawingArea() == null)
         {
            // Make sure the ui has a valid poiter to the drawing area
            // This can happend durning the loading process.
            ui.setDrawingArea(getDrawingArea(graphWindow));
         }

         if (ui.getOwner() != null)
         {
            retVal.setBoundingRect(getLogicalBounds(ui));
            retVal.setDeviceBounds(getDeviceBounds(graphics, ui));
            retVal.setGraphObject((IETGraphObject)ui.getOwner());
         }
      }
      else
      {
         retVal = null;
      }

      // TODO: Determine what the DrawinToMainDrawingArea and AlwaysSetFont
      //       Should be set to.

      return retVal;
   }

   /*
    * Returns true if any portion of the rect touches the view port.
    */
   public static boolean isLogicalRectOnScreen(TSEGraphWindow graphWindow, TSConstRect rect)
   {
      if (graphWindow != null)
      {
         TSConstRect worldBounds = graphWindow.getTransform().getWorldBounds();
         TSRect uiBounds = new TSRect();
         graphWindow.getGraph().getLocalToMainDisplayGraphTransform().transformRect(rect, uiBounds);
         return worldBounds.intersects(uiBounds);
      }
      return false;
   }

   /*
    * Returns true if any portion of the rect touches the view port.
    */
   public static boolean isLogicalRectOnScreen(TSEGraph graph, TSConstRect rect)
   {
      return graph != null ? isLogicalRectOnScreen(((TSEGraphManager)graph.getOwnerGraphManager()).getGraphWindow(), rect) : false;
   }

   /*
    * Returns true if any portion of the rect touches the view port.
    */
   public static boolean isLogicalRectOnScreen(TSEGraphics g, TSConstRect rect)
   {
      TSEGraphWindow graphWindow = g != null ? g.getGraphWindow() : null;
      if (graphWindow != null)
      {
         return isLogicalRectOnScreen(graphWindow, rect);
      }
      return false;
   }

   /*
    * Returns true if any portion of the UI is visible within the visible display area (view port). Graphics can be null.
    */
   public static boolean isOnTheScreen(TSEGraphics g, IETGraphObjectUI ui)
   {
      if (ui != null && ui.getOwner() != null && ui.getOwner().isVisible())
      {
         TSEGraphWindow graphWindow = g != null ? g.getGraphWindow() : ui.getDrawEngine().getDrawingArea().getGraphWindow();
         return isLogicalRectOnScreen(graphWindow, ui.getBounds());
      }
      return false;
   }

   /*
   	public static TSConstRect getLogicalClippingRect(TSEGraphics graphics, IETGraphObjectUI ui)
   	{
   		TSConstRect logicalRect = ui.getBounds();
   				
   		TSRect clipRect = new TSRect();
   		TSConstSize clipSize = new TSConstSize(logicalRect.getWidth() + 1.51, logicalRect.getHeight() + 1.51);
   		clipRect.setBoundsFromCenter(logicalRect.getCenter(), clipSize);				
   		return clipRect;
   		//return RectConversions.etRectToTSRect(logicalRect);
   	}
   */
}
