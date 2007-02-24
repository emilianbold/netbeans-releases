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


//	 $Date$

package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.SynchStateKindEnum;
import org.netbeans.modules.uml.ui.support.accessibility.UMLAccessibleRole;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveAttribute;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISimpleListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

import com.tomsawyer.drawing.TSPNode;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.graph.TSGraph;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;
import com.tomsawyer.editor.TSEInteractiveConstants;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import javax.swing.SwingConstants;

/**
 * @author Embarcadero Technologies Inc.
 *
 */
public abstract class ETNodeDrawEngine extends ETDrawEngine implements INodeDrawEngine
{
   private Properties m_properties;

   private int m_lastDrawPointY;
   private double m_lastDrawPointWorldY;

   private ETHiddenNodeList m_HiddenQualifiers = null;

   int m_nBorderThickness = 2;

   public static final String PARENT_WIDTH = "PARENT_WIDTH";
   public static final String PARENT_HEIGHT = "PARENT_HEIGHT";
   public static final String FILL_COLOR = "FILL_COLOR";
   public static final String FONT = "FONT";

   public static final String PSK_SHOWSTEREOTYPEICONS = "PSK_SHOWSTEREOTYPEICONS";
   public static final String PSK_SHOWEMPTYLISTS = "PSK_SHOWEMPTYLISTS";
   public static final String PSK_DISPLAYCOMPARTMENTTITLE = "PSK_DISPLAYCOMPARTMENTTITLE";
   public static final String PSK_AUTORESIZE = "PSK_AUTORESIZE";
   public static final String PSK_SHOWEDITTOOLTIP = "PSK_SHOWEDITTOOLTIP";

   public static final String PSK_RESIZE_ASNEEDED = "PSK_RESIZE_ASNEEDED";
   public static final String PSK_RESIZE_EXPANDONLY = "PSK_RESIZE_EXPANDONLY";
   public static final String PSK_RESIZE_UNLESSMANUAL = "PSK_RESIZE_UNLESSMANUAL";
   public static final String PSK_RESIZE_NEVER = "PSK_RESIZE_NEVER";
   public static final String PSK_ASK = "PSK_RESIZE_NEVER";
   public static final String PSK_ALWAYS = "PSK_ALWAYS";
   public static final String PSK_NEVER = "PSK_NEVER";
   public static final String PSK_SELECTED = "PSK_SELECTED";
   public static final String PSK_YES = "PSK_YES";
   public static final String PSK_NO = "PSK_NO";

   public static final int MIN_NODE_WIDTH = 40;
   public static final int MIN_NODE_HEIGHT = 40;

   protected int m_borderThickness;

   // Set this flag to true so that doubleclicks aren't passed to the compartments
   protected boolean m_lockEditing = false;

   // Fill and border colors
   private int m_nFillStringID = -1;
   private int m_nLightFillStringID = -1;
   private int m_nBorderStringID = -1;

   public ETNodeDrawEngine()
   {
      super();
      this.m_properties = new Properties();

   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransform#getOwnerGraphObject()
    */
   public TSGraphObject getOwnerGraphObject()
   {
      ITSGraphObject object = getParentETElement();
      return object instanceof TSGraphObject ? (TSGraphObject)object : null;

   }

   // Get the user specified diagram preferences
   private void getPreferences()
   {
      IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();

      this.getProperties().setProperty(PSK_SHOWSTEREOTYPEICONS, prefMan.getPreferenceValue("Diagrams", "ShowStereotypeIcons"));
      this.getProperties().setProperty(PSK_SHOWEMPTYLISTS, prefMan.getPreferenceValue("Diagrams", "ShowEmptyLists"));
      this.getProperties().setProperty(PSK_DISPLAYCOMPARTMENTTITLE, prefMan.getPreferenceValue("Diagrams", "ShowCompartmentTitles"));
      this.getProperties().setProperty(PSK_AUTORESIZE, prefMan.getPreferenceValue("Diagrams", "AutoResize"));
      this.getProperties().setProperty(PSK_SHOWEDITTOOLTIP, prefMan.getPreferenceValue("Diagrams", "ShowEditToolTip"));
   }

   /**
    *
    * Resizes the node so that the given compartment is shown fully.  Normally during drawing
    * the node draws each compartment until it runs out of room.  This function causes the node
    * to grow or shrink so that the compartment is fully visible.  Does not resize horizontally.
    *
    * @param pCompartment[in] The compartment that is forcing the resize.  If NULL then the operation performs a
    * resize to contents according to the current preference settings.
    *
    * @param bKeepUpperLeftPoint[in] If TRUE, the resize grows down and right from the top left corner, if FALSE
    * the centerpoint is fixed and the sides are expanded in all directions.  Default is FALSE;
    *
    * @param bIgnorePreferences[in] If TRUE the node is resized to fit the compartment according to the preferences setting
    * AutoResize, TRUE means resizing always occurs.  Default is FALSE.
    *
    * If bIgnorePreferences is FALSE, resizing follows the preference AutoResize:
    *
    * PSK_RESIZE_ASNEEDED     :  Always resize to fit. May grow or shrink.
    * PSK_RESIZE_EXPANDONLY   :  Grows only if necessary, never shrinks.
    * PSK_RESIZE_UNLESSMANUAL :  Grows only if the user has not manually resized. Never shrinks.
    * PSK_RESIZE_NEVER        :  Never resize.
    *
    * @return HRESULT
    *
    */
   public void resizeToFitCompartment(ICompartment pCompartment, boolean bKeepUpperLeftPoint, boolean bIgnorePreferences)
   {
      if (getOwnerNode() != null)
      {
         // default is to always resize

         String sPreference = bIgnorePreferences ? "PSK_RESIZE_ASNEEDED" : getResizeBehavior();

         if (sPreference != null && !sPreference.equals("PSK_RESIZE_NEVER"))
         {
/*         	
            IETSize szDesired;

            // pointer to the compartment dictating our new size
            ICompartment theCompartment = pCompartment;

            if (theCompartment != null)
            {
               // possibly going to resize, get the size of the compartment in question
               // find a containing compartment 
               IListCompartment pListCompartment = findListCompartmentContainingCompartment(theCompartment);

               // if found ask the containing compartment for its new preferred size based on this compartment
               if (pListCompartment != null)
               {
                  theCompartment = pListCompartment;
                  szDesired = pListCompartment.getDesiredSizeToFit();   
               }
               else
               {
                  szDesired = theCompartment.calculateOptimumSize(null, true);
               }
            }
            else
            {
					szDesired = new ETSize(0, 0);
            }

            // loop through all compartments, get current size except for the containing compartment
            // build desired size based on max of current width, current height
            int numCompartments = getNumCompartments();
            for (int i = 0; i < numCompartments; i++)
            {
               ICompartment comp = getCompartment(i);

               if (comp != null)
               {
                  boolean bCollapsed = comp.getCollapsed();

                  // all other compartment's use their existing size unless they are collapsed
                  if (bCollapsed == false && comp != theCompartment)
                  {
                     IETSize tempSize = comp.getCurrentSize(true);
                     if (tempSize != null)
                     {
                        szDesired.setWidth(Math.max(szDesired.getWidth(), tempSize.getWidth()));
                        szDesired.setHeight(szDesired.getHeight() + tempSize.getHeight());
                     }
                  }
               }
            }

            // Scale this first.
            szDesired = this.scaleSize(szDesired, this.getTransform());
*/
				IETSize szDesired = this.calculateOptimumSize(null, false);
            // Get our current size
            // if we're to expand only, new size is max of current vs desire size
            // if max is not equal to current then we resize
            // resize to desired size
            IETRect rect = this.getDeviceBoundingRect();

            // this size is in zoomed coordinates
            IETSize szOrig = new ETSize(rect.getIntWidth(), rect.getIntHeight());

            // choices at this point are either expandonly or as needed
            if (sPreference.equals("PSK_RESIZE_EXPANDONLY"))
            {
               // Grow if necessary, never shrink
               szDesired.setWidth(Math.max(szDesired.getWidth(), szOrig.getWidth()));
               szDesired.setHeight(Math.max(szDesired.getHeight(), szOrig.getHeight()));
            }
            else
            {
               // Adjust size to allow for border thickness (nodes that don't have borders 
               // or rectangular borders should override)
               szDesired.setWidth(szDesired.getWidth() + (2 * m_nBorderThickness));
               szDesired.setHeight(szDesired.getHeight() + (2 * m_nBorderThickness));
            }

            // resize if we've changed
            if (szDesired.getWidth() != szOrig.getWidth() || szDesired.getHeight() != szOrig.getHeight())
            {
               // Retrieve the graphical container before the resize
               INodePresentation cpContainer = TypeConversions.getGraphicalContainer(this);

               // perform resize
               resize(szDesired.getWidth(), szDesired.getHeight(), bKeepUpperLeftPoint);

               // Make sure the container is resized
               if (cpContainer != null)
               {
                  INodePresentation cpNodePE = TypeConversions.getNodePresentation(this);

                  if (cpNodePE != null)
                  {
                     cpContainer.resizeToContain(cpNodePE);
                  }
               }

               // Make sure any qualifiers are relocated
               this.relocateQualifiers(false);
            }
         }
      }
   }

   /**
    * Used in ResizeToFitCompartment.  Returns the resize behavior
    * PSK_RESIZE_ASNEEDED     :  Always resize to fit. May grow or shrink.
    * PSK_RESIZE_EXPANDONLY   :  Grows only if necessary, never shrinks.
    * PSK_RESIZE_UNLESSMANUAL :  Grows only if the user has not manually resized. Never shrinks.
    * PSK_RESIZE_NEVER        :  Never resize.
    *
    * @param sBehavior [out,retval] The behavior when resize to fit compartment is called.
    */
   private String getResizeBehavior()
   {
      IPreferenceManager2 pMgr = ProductHelper.getPreferenceManager();

      String sPreference = pMgr != null ? pMgr.getPreferenceValue("Diagrams", "AutoResize") : "PSK_RESIZE_ASNEEDED";

      if (sPreference != null && sPreference.equals("PSK_RESIZE_UNLESSMANUAL"))
      {

         if (m_LastResizeOriginator == TSE_NODE_RESIZE_ORIG_INTERACTIVE)
         {
            // user has resized, we're done
            sPreference = "PSK_RESIZE_NEVER";
         }
         else
         {
            sPreference = "PSK_RESIZE_ASNEEDED";
         }
      }

      return sPreference;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getLastDrawPointY()
    */
   public int getLastDrawPointY()
   {
      return m_lastDrawPointY;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setLastDrawPointY(int)
    */
   public void setLastDrawPointY(int i)
   {
      this.m_lastDrawPointY = i;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#updateLastDrawPointY(double)
    */
   public void updateLastDrawPointY(double d)
   {
      this.m_lastDrawPointY += d;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getLastDrawPointWorldY()
    */
   public double getLastDrawPointWorldY()
   {
      return m_lastDrawPointWorldY;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setLastDrawPointWorldY(double)
    */
   public void setLastDrawPointWorldY(double i)
   {
      this.m_lastDrawPointWorldY = i;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#updateLastDrawPointWorldY(double)
    */
   public void updateLastDrawPointWorldY(double d)
   {
      this.m_lastDrawPointWorldY -= d;
   }

   /*
    * 
    */
   public String getProperty(String name)
   {
      return this.m_properties.getProperty(name);
   }

   public Properties getProperties()
   {
      return m_properties;
   }

   public void setProperties(Properties properties)
   {
      this.m_properties = properties;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButton(java.awt.event.MouseEvent)
    */
   public boolean handleLeftMouseButton(MouseEvent pEvent)
   {
      boolean eventHandled = false;
      Iterator < ICompartment > iterator = this.getCompartments().iterator();
      while (iterator.hasNext() && !eventHandled)
      {
         eventHandled = iterator.next().handleLeftMouseButton(pEvent);
      }

      if (!eventHandled && this.hasSelectedCompartments())
      {
         this.selectAllCompartments(false);
      }

      return eventHandled;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseBeginDrag(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos)
   {
      boolean eventHandled = false;

      Iterator < ICompartment > iterator = this.getCompartments().iterator();
      while (iterator.hasNext() && !eventHandled)
      {
         ICompartment curCompartment = iterator.next();
         if (curCompartment instanceof ISimpleListCompartment)
         {
            ISimpleListCompartment listCompartment = (ISimpleListCompartment)curCompartment;

            eventHandled = listCompartment.handleLeftMouseBeginDrag(pStartPos, pCurrentPos, false);
         }
      }

      return eventHandled;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButtonDoubleClick(java.awt.event.MouseEvent)
    */
   public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent)
   {
      boolean eventHandled = false;

      if (!m_lockEditing)
      {
         Iterator < ICompartment > iterator = this.getCompartments().iterator();
         while (iterator.hasNext() && !eventHandled)
         {
            eventHandled = iterator.next().handleLeftMouseButtonDoubleClick(pEvent);
         }
      }

      if (!eventHandled)
      {
      	// Bring up a dialog allowing user to go to either diagrams or PE's
      	displayNavigationDialog();
      }

      return eventHandled;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseDrag(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos)
   {
      boolean eventHandled = false;

      Iterator < ICompartment > iterator2 = this.getCompartments().iterator();
      while (iterator2.hasNext() && !eventHandled)
      {
         ICompartment curCompartment = iterator2.next();
         eventHandled = curCompartment.handleLeftMouseDrag(pStartPos, pCurrentPos);
      }

      return eventHandled;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseDrop(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, java.util.List, boolean)
    */
   public boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pElements, boolean bMoving)
   {
      boolean eventHandled = false;

      ETList < ICompartment > compartments = new ETArrayList();
      compartments.addAll(this.getCompartments());

      Iterator < ICompartment > iterator = compartments.iterator();
      while (iterator.hasNext())
      {

         ICompartment curCompartment = iterator.next();

         if (curCompartment instanceof IListCompartment)
         {
            IListCompartment listCompartment = (IListCompartment)curCompartment;
            eventHandled = listCompartment.handleLeftMouseDrop(pCurrentPos, pElements, bMoving);
         }
      }
      return eventHandled;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleRightMouseButton(java.awt.event.MouseEvent)
    */
   public boolean handleRightMouseButton(MouseEvent pEvent)
   {
      //TODO We don't seem to be doing anything with the right mouse button
      // therefore just use the leftmouse button handler to allow for selection
      boolean leftHandled = this.handleLeftMouseButton(pEvent);

      boolean compartmentHandled = false;
      Iterator < ICompartment > iterator = this.getCompartments().iterator();
      while (iterator.hasNext() && !compartmentHandled)
      {
         compartmentHandled = iterator.next().handleRightMouseButton(pEvent);
      }

      return (leftHandled || compartmentHandled);
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButtonPressed(java.awt.event.MouseEvent)
    */
   public boolean handleLeftMouseButtonPressed(MouseEvent pEvent)
   {
      boolean eventHandled = false;

      Iterator < ICompartment > iterator = this.getCompartments().iterator();
      while (iterator.hasNext() && !eventHandled)
      {
         eventHandled = iterator.next().handleLeftMouseButtonPressed(pEvent);
      }

      return eventHandled;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#addConnector(com.tomsawyer.editor.TSEConnector, org.netbeans.modules.uml.core.support.umlsupport.IETPoint, boolean)
    */
   public TSEConnector addConnector()
   {
      TSEConnector connector = null;

      TSENode node = getOwnerNode();
      if (node != null)
      {
         connector = (TSEConnector)node.addConnector();

         // Make the connector invisible
         if (connector != null)
         {
            connector.setVisible(false);
         }
      }

      return connector;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#addDecoration(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public void addDecoration(String sDecorationType, IETPoint pLocation)
   {
      // TODO Auto-generated method stub

   }

   public ETList < IPresentationElement > getAttachedQualifiers()
   {
      ETList < IPresentationElement > pFoundQualifiers = null;
      try
      {
         IPresentationElement pThisPE = getPresentationElement();

         if (pThisPE != null)
         {
            ETList < IPresentationElement > pAllReferredElements = PresentationReferenceHelper.getAllReferredElements(pThisPE);
            long count = pAllReferredElements != null ? pAllReferredElements.getCount() : 0;

            pFoundQualifiers = new ETArrayList < IPresentationElement > ();
            for (int i = 0; i < count; i++)
            {
               IPresentationElement pPossibleQualifier = pAllReferredElements.get(i);

               // Now make sure that this element is a QualifierDrawEngine
               IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pPossibleQualifier);
               if (pDrawEngine != null)
               {
                  String sID = pDrawEngine.getDrawEngineID();

                  if (sID != null && sID.equals("QualifierDrawEngine"))
                  {
                     pFoundQualifiers.add(pPossibleQualifier);
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return pFoundQualifiers;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#getLockEdit()
    */
   public boolean getLockEdit()
   {
      return m_lockEditing;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#getNode()
    */
   public TSENode getNode()
   {
       TSEObject owner = null;
       IETGraphObjectUI ui = getParent();
       if(ui != null)
       {
           owner = ui.getOwner();
       }
       return owner instanceof TSENode ? (TSENode)owner : null;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#getParentClassifier()
    */
   public IClassifier getParentClassifier()
   {
      IETGraphObjectUI parentUI = this.getParent();
      IElement element = parentUI != null ? parentUI.getModelElement() : null;
      return element instanceof IClassifier ? (IClassifier)element : null;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#getReconnectConnector(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public int getReconnectConnector(IPresentationElement pEdgePE)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /*
    * 
    * @author KevinM
    *
    * This class hides/unhides all the nodes the in presentation list from the layout engine.
    * 
    */
   public class ETHiddenNodeList extends Object
   {
      public ETHiddenNodeList(ETList < IPresentationElement > pPES)
      {
         m_pPES = pPES;
      }

      /*
       * Hides all the non hidden nodes from the layout out engine.
       */
      public void hide()
      {
         if (m_pPES != null)
         {
            Iterator < IPresentationElement > iter = m_pPES.iterator();
            while (iter.hasNext())
            {
               IETNode pNode = TypeConversions.getETNode(iter.next());
               if (pNode != null)
               {
                  hideNode(pNode);
               }
            }

            if (getDrawingArea() != null)
            {
               getDrawingArea().refresh(true);
            }
         }
      }

      /*
       * Unhides all the  hidden nodes.
       */
      public void unHide()
      {
         IteratorT < IETNode > iter = new IteratorT < IETNode > (m_hddenNodes);

         while (iter.hasNext())
         {
            try
            {
               IETNode pNode = iter.next();
               if (pNode != null)
               {
                  unHideNode(pNode);
                  iter.reset(m_hddenNodes); // We might have removed an item.
               }
            }
            catch (Exception e)
            {
            }
         }
         if (getDrawingArea() != null)
         {
            getDrawingArea().refresh(true);
         }
      }

      /*
       * Hides a node.
       */
      protected boolean hideNode(IETNode pNode)
      {
         // Don't hide an already hidden node.
         if (canHideNode(pNode))
         {
            try
            {
               pNode.getEngine().invalidate();
               pNode.invalidateEdges();
               pNode.getGraphObject().getOwnerGraph().remove((TSENode)pNode.getGraphObject());
               m_hddenNodes.add(pNode);
               return true;
            }
            catch (Exception e)
            {
               // Is 
               // e.printStackTrace();
               return false;
            }
         }
         return false;
      }

      protected boolean canHideNode(IETNode pNode)
      {
         boolean retval;
         if (pNode != null && m_hddenNodes.find(pNode) == false)
         {
            // We need to watch out for nested containors, if we are contained this node might have hidden  already
            // so treat it like a delete operation.
            ETNodeDrawEngine engine = pNode.getEngine() instanceof ETNodeDrawEngine ? (ETNodeDrawEngine)pNode.getEngine() : null;
            if (engine != null)
            {
               retval = engine.isOwnerOnDrawingArea();
            }
            else
            {
               retval = true;
            }
         }
         else
            retval = false;
         return retval;
      }

      /*
       * Unhides a node.
       */
      protected boolean unHideNode(IETNode pNode)
      {
         if (pNode != null)
         {
            m_hddenNodes.removeItem(pNode);
            m_ownerGraph.insert((TSENode)pNode);
            return true;
         }
         return false;
      }

      protected TSGraph m_ownerGraph = getOwnerNode().getOwnerGraph();
      protected ETList < IPresentationElement > m_pPES;
      protected ETList < IETNode > m_hddenNodes = new ETArrayList < IETNode > ();
   };

   /*
    * 
    * @author KevinM
    *
    * Extends the ETHiddenNodeList, it adds support for hidding the connected
    * Incident edges
    */
   public class ETHiddenNodesAndEdges extends ETHiddenNodeList
   {
      /*
       * 
       * @author KevinM
       *
       * Stores the per node connection information for all Incident edges.
       */
      class ConnectionData
      {
         ConnectionData(IETNode node)
         {
            m_node = node;
            inEdges = new ETArrayList < IETEdge > ();
            outEdges = new ETArrayList < IETEdge > ();
            appendThese(inEdges, node.getInEdges());
            appendThese(outEdges, node.getOutEdges());
            m_ptCenterPreLayout = node.getBounds().getCenter();
         }

         /*
          * Appends all the edges to the addTo list.
          */
         protected void appendThese(ETList < IETEdge > addTo, final List edges)
         {
            IteratorT < IETEdge > edgeIter = new IteratorT < IETEdge > (edges);
            while (edgeIter.hasNext())
            {
               addTo.add(edgeIter.next());
            }
         }

         public IETNode getNode()
         {
            return m_node;
         }

         public ETList < IETEdge > getInEdges()
         {
            return inEdges;
         }

         public ETList < IETEdge > getOutEdges()
         {
            return outEdges;
         }

         public TSConstPoint getPreviousCenter()
         {
            return m_ptCenterPreLayout;
         }

         ETList < IETEdge > inEdges;
         ETList < IETEdge > outEdges;
         IETNode m_node;
         private TSConstPoint m_ptCenterPreLayout;
      };

      /*
       * All nodes and edges associated with the pPES will be hidden.
       */
      public ETHiddenNodesAndEdges(ETList < IPresentationElement > pPES)
      {
         this(pPES, true);
      }

      /*
       * All nodes and edges associated with the pPES will be hidden. If moveNodesOnRestore is true
       * the hidden nodes are offset by the amount the owner node has moved since it was hidden generally by the
       * layout engine.
       */
      public ETHiddenNodesAndEdges(ETList < IPresentationElement > pPES, boolean moveNodesOnRestore)
      {
         super(pPES);
         m_moveNodesOnRestore = moveNodesOnRestore;
      }

      public void hide()
      {
         // Save the owners center point.
         m_ptCenterPreLayout = getOwnerNode().getCenter();
         // Edges must be hidden before the nodes.
         hideEdges();
         super.hide();
      }

      public void unHide()
      {
         super.unHide();
         // Restore all the edges after the nodes are restored.
         this.unHideEdges();
      }

      protected boolean canHideEdge(IETEdge pEdge)
      {
         return pEdge != null && m_hiddenEdges.find(pEdge) == false;
      }

      /*
       * Physically removes the edge from the owner graph, only if not all ready hidden.
       */
      protected boolean hideEdge(IETEdge pEdge)
      {
         // Don't hide an already hidden Edge.
         if (canHideEdge(pEdge))
         {
            pEdge.invalidate();
            pEdge.getGraphObject().getOwnerGraph().remove((TSEEdge)pEdge.getGraphObject());
            m_hiddenEdges.add(pEdge);
            return true;
         }
         return false;
      }

      /*
       * Hides all the Incident edges stored in the connection data.
       */
      protected void hideEdges(ConnectionData data)
      {
         IteratorT < IETEdge > edgeIter = new IteratorT < IETEdge > (data.getInEdges());
         while (edgeIter.hasNext())
         {
            hideEdge(edgeIter.next());
         }

         edgeIter.reset(data.getOutEdges());

         while (edgeIter.hasNext())
         {
            hideEdge(edgeIter.next());
         }
      }

      /*
       * Builds the list of Connection information for all presentation elements.
       */
      protected void buildConnectionData()
      {
         Iterator < IPresentationElement > iter = m_pPES.iterator();
         while (iter.hasNext())
         {
            IETNode pNode = TypeConversions.getETNode(iter.next());
            if (pNode != null)
               m_connectionData.add(new ConnectionData(pNode));
         }
      }

      /*
       * Hides all the connected edges for all the presentation elements.
       */
      protected void hideEdges()
      {
         buildConnectionData();
         Iterator < ConnectionData > iter = m_connectionData.iterator();
         while (iter.hasNext())
         {
            this.hideEdges(iter.next());
         }
      }

      protected boolean unHideNode(IETNode pNode)
      {
         boolean restored = super.unHideNode(pNode);
         return restored;
      }

      protected boolean unHideEdge(IETEdge pEdge)
      {
         if (pEdge != null)
         {
            m_hiddenEdges.removeItem(pEdge);
            m_ownerGraph.insert((TSEEdge)pEdge);
            return true;
         }
         return false;
      }

      protected ETPairT < Double, Double > updateNodePosition(ConnectionData connectionData)
      {
         if (m_moveNodesOnRestore)
         {
            TSENode pNode = (TSENode)connectionData.getNode();
            // use this draw engines owners centerpoint.
            TSConstPoint ptNewCenter = getOwnerNode().getCenter();

            double lDx = ptNewCenter.getX() - m_ptCenterPreLayout.getX();
            double lDy = ptNewCenter.getY() - m_ptCenterPreLayout.getY();

            pNode.moveBy(lDx, lDy);
            return new ETPairT < Double, Double > (new Double(lDx), new Double(lDy));
         }
         return null;
      }

      protected void restore(ConnectionData connectionData)
      {
         // Update the position first so nodes connection points are correct.
         ETPairT < Double, Double > delta = updateNodePosition(connectionData);
         if (delta != null)
         {
            // Now move the path nodes by the same delta.
            IteratorT < ETEdge > iter = new IteratorT < ETEdge > (connectionData.getOutEdges());
            double lDx = delta.getParamOne().doubleValue();
            double lDy = delta.getParamTwo().doubleValue();
            while (iter.hasNext())
            {
               IteratorT < TSPNode > pathNodeIter = new IteratorT < TSPNode > (iter.next().pathNodes());
               while (pathNodeIter.hasNext())
               {
                  pathNodeIter.next().moveBy(lDx, lDy);
               }
            }
         }
      }

      protected void unHideEdges()
      {
         // Make sure the node positions and edge ends are set.
         Iterator < ConnectionData > connectionInfoIter = m_connectionData.iterator();
         while (connectionInfoIter.hasNext())
         {
            restore(connectionInfoIter.next());
            connectionInfoIter.remove();
         }

         // Now unhide all the edges.
         IteratorT < IETEdge > iter = new IteratorT < IETEdge > (m_hiddenEdges);
         while (iter.hasNext())
         {
            if (this.unHideEdge(iter.next()))
               iter.reset(m_hiddenEdges);
         }
      }

      /*
       * lookup function, gets the stored connection data for a hidden node.
       */
      protected ConnectionData getConnectionData(IETNode pNode)
      {
         Iterator < ConnectionData > iter = m_connectionData.iterator();
         while (iter.hasNext())
         {
            ConnectionData data = iter.next();
            if (data.getNode() == pNode)
            {
               return data;
            }
         }
         return null;
      }

      protected ETList < IETEdge > m_hiddenEdges = new ETArrayList < IETEdge > ();
      ETList < ConnectionData > m_connectionData = new ETArrayList < ConnectionData > ();
      boolean m_moveNodesOnRestore = true;
      TSConstPoint m_ptCenterPreLayout = null;
   }

   /*
    * 
    * @author KevinM
    *
    * Special class for hidding Qualifiers.
    * 
    */
   class ETQualifierHiddenNodeList extends ETHiddenNodeList
   {
      public ETQualifierHiddenNodeList(ETList < IPresentationElement > pPES)
      {
         super(pPES);
      }

      protected boolean hideNode(IETNode pNode)
      {
         if (canHideNode(pNode))
         {
            List inEdges = pNode.getInEdges();
            if (inEdges != null && inEdges.size() == 1)
            {
               ETEdge edge = (ETEdge)inEdges.get(0);
               m_qualifierEdges.add(edge);
               edge.setTargetNode(getOwnerNode());
            }
         }

         if (super.hideNode(pNode))
         {
            return true;
         }
         return false;

      }

      protected boolean unHideNode(IETNode pNode)
      {
         int index = m_hddenNodes.indexOf(pNode);

         // Lookup the Edge.
         ETEdge edge = index >= 0 ? (ETEdge)m_qualifierEdges.get(index) : null;
         if (super.unHideNode(pNode))
         {
            // Retarget the edge.

            m_qualifierEdges.removeItem(edge);

            if (edge != null)
            {
               pNode.moveTo(edge.getTargetPoint());

               if (edge != null)
               {
                  edge.setTargetNode((TSENode)pNode.getObject());
               }
            }
            return true;
         }
         return false;
      }

      protected ETList < ETEdge > m_qualifierEdges = new ETArrayList < ETEdge > ();
   };

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#hideAllAttachedQualifiers(boolean)
    */
   public void hideAllAttachedQualifiers(boolean bHide)
   {
      if (bHide)
      {
         ETList < IPresentationElement > pPES = this.getAttachedQualifiers();
         if (pPES != null)
         {
            m_HiddenQualifiers = new ETQualifierHiddenNodeList(pPES);
            m_HiddenQualifiers.hide();
         }
      }
      else if (m_HiddenQualifiers != null)
      {
         m_HiddenQualifiers.unHide();
         m_HiddenQualifiers = null;
         relocateQualifiers(false);
      }
   }

   /*
    * Default override relocateQualifiers(boolean bAutoRouteAssociationEdge) 
    */
   public void relocateQualifiers()
   {
      relocateQualifiers(true);
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#relocateQualifiers(boolean)
    */
   public void relocateQualifiers(boolean bAutoRouteAssociationEdge)
   {
      try
      {
         ETList < IPresentationElement > pPEs = getAttachedQualifiers();
         long count = pPEs != null ? pPEs.getCount() : 0;

         TSENode pThisNode = getOwnerNode();
         if (count > 0 && pThisNode != null)
         {
             TSRect thisRect = new TSRect(pThisNode.getBounds());

             for (int i = 0; i < count; i++)
             {
                 IPresentationElement pQualPE = pPEs.item(i);
                 
                 ETNode pQualifierNode = (ETNode)TypeConversions.getOwnerNode(pQualPE);
                 if (pQualifierNode != null)
                 {
                     TSConstRect qualifierNodeRect = pQualifierNode.getBounds();
                     TSRect qualifierNodeRect2 = new TSRect(qualifierNodeRect);
                     qualifierNodeRect2.merge(qualifierNodeRect.getLeft() + 2.5, qualifierNodeRect.getTop() + 2.5, qualifierNodeRect.getRight() + 2.5, qualifierNodeRect.getBottom() + 2.5);
                     
                     TSConstRect intersectRect = qualifierNodeRect2.intersection(thisRect);
                     
                     if (intersectRect==null)
                     {
                         IProductGraphPresentation pGraphPresentation = pQualPE instanceof IProductGraphPresentation ? (IProductGraphPresentation)pQualPE : null;

                        if (pGraphPresentation != null)
                        {
                           // remove the invalid attempted move of the qualifier node and refresh the diagram
                           pGraphPresentation.invalidate();
                           invalidate();
                        }
                         TSConstPoint point=null;
                         
                         int code = thisRect.outcode(qualifierNodeRect.getCenterX(), qualifierNodeRect.getCenterY());
                         if (code==TSConstRect.OUT_TOP || code==TSConstRect.OUT_TOP + TSConstRect.OUT_LEFT ||
                                 code==TSConstRect.OUT_TOP + TSConstRect.OUT_RIGHT)
                             point = new TSConstPoint(thisRect.getCenterX(), thisRect.getTop()+qualifierNodeRect.getHeight()/2);
                         else if (code==TSConstRect.OUT_BOTTOM || code==TSConstRect.OUT_RIGHT + TSConstRect.OUT_BOTTOM ||
                                 code==TSConstRect.OUT_LEFT + TSConstRect.OUT_BOTTOM)
                             point = new TSConstPoint(thisRect.getCenterX(), thisRect.getBottom()-qualifierNodeRect.getHeight()/2);
                         else if (code==TSConstRect.OUT_LEFT)
                             point = new TSConstPoint(thisRect.getLeft()-qualifierNodeRect.getWidth()/2, thisRect.getCenterY());
                         else if (code==TSConstRect.OUT_RIGHT)
                             point = new TSConstPoint(thisRect.getRight()+qualifierNodeRect.getWidth()/2, thisRect.getCenterY());
                         else
                             point = new TSConstPoint(thisRect.getCenter());
                         
                         pQualifierNode.moveTo(point);                        
                     }                     
                 }
//                  // Get the nearest point from this qualifier node to the current node
//                  TSConstPoint qualifierCenterPoint = pQualifierNode.getCenter();
//                  if (qualifierCenterPoint != null)
//                  {
//                     boolean bDidMove = false;
//                     int side = thisRect.closestSide(qualifierCenterPoint);
//
////                     ETTSRectEx qualifierRect = new ETTSRectEx(pQualifierNode.getBounds());
//                     TSRect qualifierRect = new TSRect(pQualifierNode.getBounds());
//
//                     // Now move the qualifier to butt up against this node
//                     if (side == TSSide.TS_SIDE_RIGHT || side == TSSide.TS_SIDE_LEFT)
//                     {
//                        // Make sure the top of the qualifier is no lower then the bottom
//                        // of this node and the bottom of the qualifier is not higher then
//                        // the top of this node
//                        if (qualifierRect.getTop() < thisRect.getBottom())
//                        {
//                           // Move the qualifier rect
//                           qualifierRect.moveBy(0.0, thisRect.getBottom() - qualifierRect.getTop());
//                           bDidMove = true;
//                        }
//                        if (qualifierRect.getBottom() > thisRect.getTop())
//                        {
//                           // Move the qualifier rect
//                           qualifierRect.moveBy(0.0, thisRect.getTop() - qualifierRect.getBottom());
//                           bDidMove = true;
//                        }
//
//                        // Now make sure we're attached to the left or right size
//                        if (side == TSSide.TS_SIDE_RIGHT)
//                        {
//                           if (qualifierRect.getLeft() != thisRect.getRight())
//                           {
//                              // Move the qualifier rect
//                              qualifierRect.moveBy(thisRect.getRight() - qualifierRect.getLeft(), 0.0);
//                              bDidMove = true;
//                           }
//                        }
//                        else if (side == TSSide.TS_SIDE_LEFT)
//                        {
//                           if (qualifierRect.getRight() != thisRect.getLeft())
//                           {
//                              // Move the qualifier rect
//                              qualifierRect.moveBy(thisRect.getLeft() - qualifierRect.getRight(), 0.0);
//                              bDidMove = true;
//                           }
//                        }
//                        pQualifierNode.moveTo(qualifierRect.getCenter());
//                     }
//                     else
//                     {
//                        // Make sure the left of the qualifier is not to the right of the
//                        // right side of this node.  The right side of the qualifier should be
//                        // not to the right of this node.
//                        if (qualifierRect.getRight() < thisRect.getLeft())
//                        {
//                           // Move the qualifier rect
//                           qualifierRect.moveBy(thisRect.getLeft() - qualifierRect.getRight(), 0.0);
//                           bDidMove = true;
//                        }
//                        if (qualifierRect.getLeft() > thisRect.getRight())
//                        {
//                           // Move the qualifier rect
//                           qualifierRect.moveBy(thisRect.getRight() - qualifierRect.getLeft(), 0.0);
//                           bDidMove = true;
//                        }
//
//                        // Now make sure we're attached to the top or bottom
//                        if (side == TSSide.TS_SIDE_TOP)
//                        {
//                           if (qualifierRect.getBottom() != thisRect.getTop())
//                           {
//                              // Move the qualifier rect
//                              qualifierRect.moveBy(0.0, thisRect.getTop() - qualifierRect.getBottom());
//                              bDidMove = true;
//                           }
//                        }
//                        else
//                        {
//                           if (qualifierRect.getTop() != thisRect.getBottom())
//                           {
//                              // Move the qualifier rect
//                              qualifierRect.moveBy(0.0, thisRect.getBottom() - qualifierRect.getTop());
//                              bDidMove = true;
//                           }
//                        }
//                        pQualifierNode.moveTo(qualifierRect.getCenter());
//                     }

//                     if (bDidMove)
//                     {
//                        IProductGraphPresentation pGraphPresentation = pQualPE instanceof IProductGraphPresentation ? (IProductGraphPresentation)pQualPE : null;
//
//                        if (pGraphPresentation != null)
//                        {
//                           // Tell the qualifier node to invalidate
//                           pGraphPresentation.invalidate();
//                        }
//                     }
//
//                     if (bAutoRouteAssociationEdge)
//                     {
//                        /*
//                        final List inEdgeList = pQualifierNode.inEdges();
//                        TSEdgeSListIter inEdgeListIter(pInEdgeList);
//                        while (inEdgeListIter) {
//                        	TSObjectobject = inEdgeListIter.pObject();
//                        
//                        	TSEEdgeedge = (TSEEdge *) dynamic_cast < TSEEdge * > (pObject);
//                        	// Per Salil at TS 7/31/2003, make sure
//                        	// that we don't autoroute any disconnected edges
//                        	if (pEdge & !pEdge.disconnected()) {
//                        		pEdge.autoRoute();
//                        		pEdge.repairOrthogonalRouting();
//                        	}
//                        	inEdgeListIter++;
//                        }
//                        
//                        final TSEdgeSList outEdgeList = pQualifierNode.outEdges();
//                        TSEdgeSListIter outEdgeListIter(pOutEdgeList);
//                        while (outEdgeListIter) {
//                        	TSObjectobject = outEdgeListIter.pObject();
//                        
//                        	TSEEdgeedge = (TSEEdge *) dynamic_cast < TSEEdge * > (pObject);
//                        	// Per Salil at TS 7/31/2003, make sure
//                        	// that we don't autoroute any disconnected edges
//                        	if (pEdge & !pEdge.disconnected()) {
//                        		pEdge.autoRoute();
//                        		pEdge.repairOrthogonalRouting();
//                        	}
//                        	outEdgeListIter++;
//                        }
//                        */
//                     }
//                  }
//               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Restore draw engine presentation attributes from the product archive.
    *
    * @param pProductArchive [in] The archive we're reading from
    * @param pEngineElement [in] The element where this draw engine's information should exist
    */
   public long readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pEngineElement)
   {
      // Call the base class
      super.readFromArchive(pProductArchive, pEngineElement);

      // Save off our lock flag
      IProductArchiveElement cpEngineEle = pEngineElement.getElement(IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
      if (cpEngineEle != null)
      {
         boolean bTemp = cpEngineEle.getAttributeBool(IProductArchiveDefinitions.EDITLOCKED_STRING);
         IProductArchiveAttribute pAttr = cpEngineEle.getAttribute(IProductArchiveDefinitions.EDITLOCKED_STRING);
         if (pAttr != null)
         {
            m_lockEditing = bTemp;
         }
      }
      return 0;
   }

   /**
    * Resizes the node.  Pass the coordinates in actual pixels (zoomed).
    *
    * @param nWidth The new width for the node
    * @param nHeight The new heightfor the node
    * @param bKeepUpperLeftPoint <code>true</code> if the resize should anchor 
    *                            the top left corner, otherwise the node is 
    *                            stretched from its centerpoint. 
    */
   public void resize(int nWidth, int nHeight, boolean bKeepUpperLeftPoint)
   {

      INodePresentation presentation = getNodePresentation();
      if (presentation != null)
      {
         // nodepresentation requires size in 100% coordinates.

         // Invalidate before and after the resize.  This won't cause
         // two draws cause the invalidate on the GET side unions the rectangles
         // until the paint is received.  We have to post an invalidate here, 
         // because this gets called during left mouse button doubleclick - 
         // because we're in TS, the GET blocks and ignores invalidate calls.
         invalidate();

         TSTransform transform = getTransform();
         if (transform == null)
         {
            // This is the code that was being used, so I (BDB) left it here in case it is needed.
            IDrawInfo info = getParent() != null ? getParent().getDrawInfo() : null;
            if (info == null && getDrawingArea() != null && getDrawingArea().getGraphWindow() != null)
					transform = this.getDrawingArea().getGraphWindow().getTransform();
            else if (info != null)
            	transform = info.getTSTransform();
            	
            if (transform == null)
            	return;	// We cannot continue with the resize;
         }
         presentation.resize(transform.widthToWorld(nWidth), transform.heightToWorld(nHeight), bKeepUpperLeftPoint);
         invalidate();
      }
   }

   /**
    * Resizes the node.  Pass the coordinates in actual pixels (zoomed).
    *
    * @param size The size of the node.
    * @param bKeepUpperLeftPoint <code>true</code> if the resize should anchor 
    *                            the top left corner, otherwise the node is 
    *                            stretched from its centerpoint. 
    */
   public void resize(IETSize size, boolean bKeepUpperLeftPoint)
   {
       if(size != null)
       {
           resize(size.getWidth(), size.getHeight(), bKeepUpperLeftPoint);
       }
   }

   public void resizeTo(IETSize pSizeNew)
   {
      resize(pSizeNew, true);
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#selectAllAttachedQualifiers(boolean)
    */
   public void selectAllAttachedQualifiers(boolean bSelect)
   {
      try
      {
         ETList < IPresentationElement > pPEs = getAttachedQualifiers();
         if (pPEs == null)
            return;

         Iterator < IPresentationElement > iter = pPEs.iterator();
         boolean anyChanged = false;
         while (iter.hasNext())
         {
            IPresentationElement pThisPE = iter.next();
            IETNode pNode = TypeConversions.getETNode(pThisPE);
            if (pNode != null)
            {
               if (pNode.isSelected() != bSelect)
               {
                  pNode.getEngine().invalidate();
                  pNode.setSelected(bSelect);
                  anyChanged = true;
               }
            }
         }

         if (anyChanged && this.getDrawingArea() != null)
         {
            this.getDrawingArea().refresh(true);
         }

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void setLockEdit(boolean value)
   {
      this.m_lockEditing = value;
      //
      invalidate();
   }

   public void setNodeShape(IDrawInfo pDrawInfo)
   {
      // TODO Auto-generated method stub

   }

   public void stretch(IStretchContext stretchContext)
   {
      if (null == stretchContext)
         throw new IllegalArgumentException();

      ICompartment stretchCompartment = stretchContext.getCompartment();
      if (stretchCompartment != null)
      {
         stretchCompartment.stretch(stretchContext);
      }
      else
      {
         // No specific compartment was given, therefore "stretch" them all
         ETList < ICompartment > compartments = getCompartments();
         for (Iterator iter = compartments.iterator(); iter.hasNext();)
         {
            ICompartment compartment = (ICompartment)iter.next();

            compartment.stretch(stretchContext);
         }
      }
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
    */
   public void doDraw(IDrawInfo pDrawInfo)
   {
      IETGraphObjectUI parentUI = getParent();
      this.getPreferences();

      if (pDrawInfo.isTransparent() == false)
      {
         drawBackground(pDrawInfo);
      }

      if (pDrawInfo.isBorderDrawn() == true)
      {
         drawBorder(pDrawInfo);
      }

      drawContents(pDrawInfo);
      
      // Draw a padlock at the top right corner of class node
      // if (getLockEdit() && graphics.getDeviceConfiguration().getDevice().getType() != GraphicsDevice.TYPE_PRINTER) {
      if (isPadlockAllawed() && getLockEdit()) {
          drawPadlock(pDrawInfo);
      }
   }

   /**
   * Draws the contents of the node.  The contents of the node is decided by
   * each draw engine. 
   * 
   * @param pDrawInfo The information needed to draw.
   */
   protected void drawContents(IDrawInfo pDrawInfo)
   {

   }

   /**
    * Draws the draw engines border.  The border is only told to be drawn when
    * the node is drawing borders.
    * 
    * @param pDrawInfo  The information needed to draw.
    */
   protected void drawBorder(IDrawInfo pDrawInfo)
   {
      // TODO Auto-generated method stub

   }

   /**
    * Draws the draw engines background.  The background is only drawn when
    * the node is not transparent.
    * 
    * @param pDrawInfo  The information needed to draw.
    */
   protected void drawBackground(IDrawInfo pDrawInfo)
   {
      // TODO Auto-generated method stub

   }

   /**
   	 * Size node to the contents of the draw engine.  Collapsed or
   	 * partially collapsed compartments are expanded.
   	 */
   public void sizeToContents()
   {
      if (getOwnerNode() != null)
      {
         IDrawInfo info = getParent().getDrawInfo();
         ETList < ICompartment > compartments = getCompartments();
         for (Iterator iter = compartments.iterator(); iter.hasNext();)
         {
            ICompartment curCompartment = (ICompartment)iter.next();
            //curCompartment.setCollapsed(false);
            curCompartment.clearStretch(info);
         }

         if ((info != null) && (info.getTSEGraphics() != null))
         {
            // call our Resize() method, holding the top left corner fixed.  
            // Otherwise when right-clicking on a class node and selected Resize 
            // to fit the node appears to jump around its centerpoint

            IETSize optimumSize = calculateOptimumSize(info, true); // One hundred %

            // Now Scale them
            TSTransform transform = info.getTSTransform();
            IETSize retVal = this.scaleSize(optimumSize, transform);

            resize(retVal, true);
         }
      }
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getPresentationType()
    */
   public String getPresentationType()
   {
      return "NodePresentation";
   }

   /*
    * Returns the iterface to the INodePresentation, associated with this drawEngine.
    */
   public INodePresentation getNodePresentation()
   {
      return getPresentation() instanceof INodePresentation ? (INodePresentation)getPresentation() : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
    */
   public IETSize calculateOptimumSize(IDrawInfo drawInfo, boolean bAt100Pct)
   {
      IETSize size = super.calculateOptimumSize(drawInfo, bAt100Pct);

      size.setWidth(size.getWidth() + 2 * m_nBorderThickness);
      size.setHeight(size.getHeight() + 2 * m_nBorderThickness);

      return size;
   }

   /**
    * Converts the parent TSEObject to a TSENode and returns a TSENode.
    *
    * @return The TSENode this draw engine represents
    */
   protected TSENode getOwnerNode()
   {
      IETGraphObjectUI ui = getParent();
      return ui != null ? (TSENode)ui.getOwner() : null;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onContextMenu(org.netbeans.modules.uml.ui.products.ad.application.IMenuManager)
    */
   public void onContextMenu(IMenuManager manager)
   {
      int count = getNumCompartments();
      for (int i = 0; i < count; i++)
      {
         ICompartment pCompartment = getCompartment(i);
         if (pCompartment != null)
         {
            pCompartment.onContextMenu(manager);
         }
      }
   }

   /**
    * Handles the stereotype and name sensitivity and check
    *
    * @param pContextMenu [in] The parent context menu that was displayed.
    * @param pMenuItem [in] The button that the sensitivity is being requested for
    * @param buttonKind [in] The ID of the button above.  This ID is the one used when creating the button.
    * @param bHandled [out] true if the button happened to be a stereotype and we set its state
    */
   protected boolean handleStandardLabelSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean handled = false;
      ILabelManager labelMgr = getLabelManager();
      boolean isReadOnly = isParentDiagramReadOnly();
      if (id.equals("MBK_SHOW_STEREOTYPE"))
      {
         boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_STEREOTYPE);
         pClass.setChecked(isDisplayed);

         handled = isReadOnly ? false : true;
      }
      else if (id.equals("MBK_SHOW_NAME_LABEL"))
      {
         boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_NAME);
         pClass.setChecked(isDisplayed);

         handled = isReadOnly ? false : true;
      }
      return handled;
   }

   /**
    * Adds a stereotype and or name label pullright to the context menu
    *
    * @param nKind [in] The kind of standard label to add
    * @param pContextMenu[in] The context menu about to be displayed
    */
   public void addStandardLabelsToPullright(int kind, IMenuManager manager)
   {
      if (kind == StandardLabelKind.SLK_STEREOTYPE || kind == StandardLabelKind.SLK_ALL)
      {
         addStereotypeLabelPullright(this, manager);
      }
      if (kind == StandardLabelKind.SLK_NAME || kind == StandardLabelKind.SLK_ALL)
      {
         IElement pEle = getFirstModelElement();
         if (pEle != null && pEle instanceof INamedElement)
         {
            addNameLabelPullright(this, manager);
         }
      }
   }

   /**
    * Handles the stereotype and name selections
    *
    * @param pContextMenu[in] The context menu that was displayed to the user
    * @param pMenuItem[in] The menu that was just selected
    * @param bHandled[out] true if the stereotype selection was handled
    */
   protected boolean handleStandardLabelSelection(ActionEvent e, String id)
   {
      boolean handled = false;
      IDrawingAreaControl pDiagram = getDrawingArea();
      ILabelManager labelMgr = getLabelManager();
      if (pDiagram != null)
      {
         if (id.equals("MBK_SHOW_STEREOTYPE"))
         {
            if (labelMgr != null)
            {
               boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_STEREOTYPE);
               labelMgr.showLabel(TSLabelKind.TSLK_STEREOTYPE, isDisplayed ? false : true);
            }
            pDiagram.refresh(false);
            handled = true;
         }
         else if (id.equals("MBK_SHOW_NAME_LABEL"))
         {
            if (labelMgr != null)
            {
               boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_NAME);
               labelMgr.showLabel(TSLabelKind.TSLK_NAME, isDisplayed ? false : true);
               if (!isDisplayed)
               {
                  IPresentationElement pPE = labelMgr.getLabel(TSLabelKind.TSLK_NAME);
                  if (pPE != null && pPE instanceof ILabelPresentation)
                  {
                     pDiagram.postEditLabel((ILabelPresentation)pPE);
                  }
               }
            }
            pDiagram.refresh(false);
            handled = true;
         }
      }
      return handled;
   }

   /**
    * Saves the draw engine and compartment stuff to the product archive.
    *
    * @param pProductArchive [in] The archive we're saving to
    * @param pElement [in] The current element, or parent for any new attributes or elements
    */
   public long writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {
      // Call the base class
      super.writeToArchive(pProductArchive, pElement);

      // Save off our lock flag
      IProductArchiveElement engEle = pElement.getElement(IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
      if (engEle != null)
      {
         engEle.addAttributeBool(IProductArchiveDefinitions.EDITLOCKED_STRING, m_lockEditing);
      }
      return 0;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
    */
   public String getDrawEngineID()
   {
      return "NodeDrawEngine";
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onGraphEvent(int)
    */
   public void onGraphEvent(int nKind)
   {
      super.onGraphEvent(nKind);

      switch (nKind)
      {
         case IGraphEventKind.GEK_POST_SELECT :
            {
               this.handlePostSelect();
            }
            break;
         case IGraphEventKind.GEK_POST_MOVE :
            {
               selectAllAttachedQualifiers(false);
            }
            break;

         case IGraphEventKind.GEK_PRE_MOVE :
            {
               selectAllAttachedQualifiers(true);
            }
            break;
         case IGraphEventKind.GEK_POST_SMARTDRAW_MOVE :
            {
               selectAllAttachedQualifiers(false);
               relocateQualifiers();
            }
            break;
         case IGraphEventKind.GEK_PRE_RESIZE :
            {
//               hideAllAttachedQualifiers(true);
            }
            break;
         case IGraphEventKind.GEK_POST_RESIZE :
            {
//               hideAllAttachedQualifiers(false);
               relocateQualifiers();
            }
            break;
         case IGraphEventKind.GEK_PRE_LAYOUT :
            {
//               hideAllAttachedQualifiers(true);
            }
            break;
         case IGraphEventKind.GEK_POST_LAYOUT :
            {
//               hideAllAttachedQualifiers(false);
               relocateQualifiers(true);
            }
            break;
         case IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED :
            {
               selectAllAttachedQualifiers(true);
            }
            break;
      }
   }

   private void handlePostSelect()
   {
      if (getOwnerNode() != null && !getOwnerNode().isSelected() && hasSelectedCompartments())
      {
         selectAllCompartments(false);
      }
   }

   /*
    * Returns the color used to fill this node.
    */
   public Color getFillColor()
   {
      return new Color(m_ResourceUser.getCOLORREFForStringID(m_nFillStringID));
   }

   /*
    * Sets the Fill Color Given a Resource name and the RBG Values.
    */
   public Color getLightGradientFillColor()
   {
      return new Color(m_ResourceUser.getCOLORREFForStringID(m_nLightFillStringID));
   }
   
   /*
    * Returns the Color used to to fill this node.
    */
   public Color getBkColor()
   {
      return getFillColor();
   }

   /*
    * Retuns the Color used to draw the border.
    */
   public Color getBorderColor()
   {
      return new Color(m_ResourceUser.getCOLORREFForStringID(m_nBorderStringID));
   }

   /*
    * Returns the Color used to draw the border.
    */
   public Color getBorderBoundsColor()
   {
      return getBorderColor();
   }

   /*
    * Returns the color of the text.
    */
   public TSEColor getTextColor()
   {
      ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
      return parentUI != null ? parentUI.getTextColor() : null;
   }

   /**
    * Retreives the thickness of the border.
    */
   public int getBorderThickness()
   {
      return m_nBorderThickness;
   }

   /**
    * Sets the thickness of the border.
    */
   public void setBorderThickness(int value)
   {
      m_nBorderThickness = value;
   }

   /**
   * Helper for those draw engines that use the INameListCompartment.
   *
   * @param pTargets [in] Information about what has changed.
   */
   public long handleNameListModelElementHasChanged(INotificationTargets pTargets)
   {

      try
      {
         if (pTargets != null)
         {
            IElement pModelElement = pTargets.getChangedModelElement();
            int nKind = pTargets.getKind();

            // See if the model element that changed was an ITaggedValue
            ITaggedValue pTaggedValue = pModelElement instanceof ITaggedValue ? (ITaggedValue)pModelElement : null;
            if ((nKind == ModelElementChangedKind.MECK_STEREOTYPEDELETED || nKind == ModelElementChangedKind.MECK_STEREOTYPEAPPLIED || nKind == ModelElementChangedKind.MECK_ELEMENTADDEDTONAMESPACE)
               || pTaggedValue != null)
            {
               // Update the optional compartments, including stereotype
               INameListCompartment pNameCompartment = (INameListCompartment)getCompartmentByKind(INameListCompartment.class);
               if (pNameCompartment != null)
               {
                  boolean bAddedOrRemovedCompartment = pNameCompartment.updateAllOptionalCompartments(null);
                  if (bAddedOrRemovedCompartment)
                  {
                     setIsDirty();
                  }
               }
            }
            else if (nKind == ModelElementChangedKind.MECK_NAMEMODIFIED)
            {

               // Get all the compartments
               INameListCompartment pNameCompartment = (INameListCompartment)getCompartmentByKind(INameListCompartment.class);
               if (pNameCompartment != null)
               {
                  pNameCompartment.modelElementHasChanged(pTargets);
               }
            } // element modified

            postInvalidate();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return 0;
   }

   // Returns the ListCompartment Iterator.
   protected IteratorT < ICompartment > getCompartmentIterator()
   {
      try
      {
         return new IteratorT < ICompartment > (getCompartments());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;
   }

   /**
    * Helper for those draw engines that use the INameListCompartment.
    *
    * @param pTargets [in] Details about what got deleted
    */
   protected long handleNameListModelElementDeleted(INotificationTargets pTargets)
   {
      try
      {

         IElement pModelElement = pTargets.getChangedModelElement();

         // pModelElement and pFeature are valid, find which compartment should handle it
         // Don't _VH here because we don't want to throw.
         IClassifier pClassifier = pModelElement instanceof IClassifier ? (IClassifier)pModelElement : null;
         if (pClassifier != null)
         {
            IteratorT < ICompartment > iter = getCompartmentIterator();

            while (iter.hasNext())
            {
               ICompartment pCompartment = iter.next();
               pCompartment.modelElementDeleted(pTargets);

               // if compartment is a list compartment and if empty and if DeleteWhenEmpty
               //whack it
               IListCompartment pListCompartment = pCompartment instanceof IListCompartment ? (IListCompartment)pCompartment : null;
               if (pListCompartment != null && pListCompartment.getNumCompartments() == 0 && pListCompartment.getDeleteIfEmpty())
               {
                  removeCompartment(pCompartment); // remove the empty list compartment.
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return 0;
   }

   /**
    * Node draws either a simple rectangle or a hatched (dragging) rectangle.
    *
    * @param pInfo [in] Information about this drawing operation (the DC, are we printing...)
    * @param bHandled [out] Have we handled this event?
    */
   protected boolean drawInvalidRectangle(IDrawInfo pInfo)
   {
      boolean handled = false;
      try
      {
         if (m_checkSyncStateDuringDraw && pInfo != null)
         {
            IElement pElement = TypeConversions.getElement(this);
            IProductGraphPresentation pETElement = getPresentationElement() instanceof IProductGraphPresentation ? (IProductGraphPresentation)getPresentationElement() : null;

            // See if we are attached to an IElement
            // Get the synch state from the ETElement
            int nSynchState = pETElement != null ? pETElement.getSynchState() : SynchStateKindEnum.SSK_OUT_OF_SYNCH;

            if (pElement == null || nSynchState == SynchStateKindEnum.SSK_OUT_OF_SYNCH)
            {
               IETRect boundingRect = pInfo.getDeviceBounds();

               // Create a hatched bit pattern.
               //	WORD HatchBits[8] = { 0xaa, 0x55, 0xaa, 0x55, 0xaa, 0x55, 0xaa, 0x55 };

               GDISupport.drawHatchedRectangle(pInfo.getTSEGraphics().getGraphics(), boundingRect.getRectangle(), Color.RED, Color.WHITE);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return handled;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine#getNodeUI()
    */
   public IETNodeUI getNodeUI()
   {
      return getParent() instanceof IETNodeUI ? (IETNodeUI)getParent() : null;
   }

   /*
    * 
    */
   public void sizeToContentsWithMin(long nMinWidth, long nMinHeight)
   {
      sizeToContentsWithMin(nMinWidth, nMinHeight, false, false);
   }

   /**
    * This does a size to contents, but with a minimum size
    *
    * @param nMinWidth [in] The minimum width
    * @param nMinHeight [in] The minimum height
    * @param bKeepUpperLeftPoint [in] true to resize about the upper left rather then the center
    */
   public void sizeToContentsWithMin(long nMinWidth, long nMinHeight, boolean bKeepUpperLeftPoint, boolean bKeepCurrentSizeIfBigger)
   {
      try
      {
         if (getOwnerNode() != null && getNodeUI() != null)
         {

            IETSize optimumSize = calculateOptimumSize(getNodeUI().getDrawInfo(), false);
            if (optimumSize == null)
               return;

            TSTransform transform = this.getTransform();

            ETSize size =
               new ETSize((int)Math.max(optimumSize.getWidth(), transform.widthToDevice((double)nMinWidth)), (int)Math.max(optimumSize.getHeight(), transform.heightToDevice((double)nMinHeight)));

            // Make sure we don't shrink the rectangle the user created.
            if (bKeepCurrentSizeIfBigger)
            {
               IETRect tsrectNewBounding = this.getDeviceBoundingRect();
               size.setSize((int)Math.max(size.getWidth(), tsrectNewBounding.getWidth()), (int)Math.max(size.getHeight(), tsrectNewBounding.getHeight()));
            }

            resize(size.getWidth(), size.getHeight(), bKeepUpperLeftPoint);

            IDiagram diagram = getDiagram();
            if (diagram != null)
            {
               // Update the invalid regions.
               diagram.refresh(true);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public long modelElementDeleted(INotificationTargets pTargets)
   {

      dispatchModelElementDeletedToCompartments(pTargets);
      //super.modelElementDeleted(pTargets);

      return 0;
   }

   /**
   	* Notifier that the model element has changed, if available the changed IFeature is passed along.
   	*
   	* @param pTargets[in] Information about what has changed
   	*/
   public long modelElementHasChanged(INotificationTargets pTargets)
   {
      INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
      if (pNameCompartment != null)
      {
         handleNameListModelElementHasChanged(pTargets);
      }

      return super.modelElementHasChanged(pTargets);
   }

   /**
    * Sends the deleted event to all compartments.
    *
    * @param pTargets [in] Details about what got deleted
    */
   private void dispatchModelElementDeletedToCompartments(INotificationTargets pTargets)
   {

      IElement pModelElement = pTargets.getChangedModelElement();

      // Send the deleted element to the compartments until one handles it.
      if (pModelElement != null)
      {

         int numCompartments = getNumCompartments();

         for (int i = 0; i < numCompartments; i++)
         {

            ICompartment pCompartment = getCompartment(i);

            if (pCompartment != null)
            {

               pCompartment.modelElementDeleted(pTargets);

               // if compartment is a list compartment and if empty and if DeleteWhenEmptywhack it

               IListCompartment pListCompartment = null;

               if (pCompartment instanceof IListCompartment)
               {
                  pListCompartment = (IListCompartment)pCompartment;
               }

               if (pListCompartment != null)
               {
                  int nCount = pListCompartment.getNumCompartments();

                  if (nCount == 0)
                  {

                     boolean bDelete = false;

                     bDelete = pListCompartment.getDeleteIfEmpty();

                     if (bDelete == true)
                     {
                        removeCompartment(pCompartment);
                     }
                  }
               }
            }
         }
      }
   }

   /*
    * Sets the Fill Color Given a Resource name and the RBG Values.
    */
   public int setFillColor(String resourceName, int r, int g, int b)
   {
      return setFillColor(resourceName, new Color(r, g, b));
   }

   public int setLightGradientFillColor(String resourceName, int r, int g, int b)
   {
      return setLightGradientFillColor(resourceName, new Color(r, g, b));
   }
   
   /*
    * Sets the Fill Color Given a Resource name and the RBG Values.
    */
   public int setFillColor(String resourceName, Color color)
   {
      if (color != null)
      {
         m_nFillStringID = m_ResourceUser.setResourceStringID(m_nFillStringID, resourceName, color.getRGB());
      }

      return m_nFillStringID;
   }

   /*
    * Sets the Fill Color Given a Resource name and the RBG Values.
    */
   public int setLightGradientFillColor(String resourceName, Color color)
   {
      if (color != null)
      {
         m_nLightFillStringID = m_ResourceUser.setResourceStringID(m_nLightFillStringID, 
                                                                   resourceName, 
                                                                   color.getRGB());
      }

      return m_nLightFillStringID;
   }
   
   /*
    * Sets the Border color for a Given Resource name and the RGB values.
    */
   public int setBorderColor(String resourceName, int r, int g, int b)
   {
      return setBorderColor(resourceName, new Color(r, g, b));
   }

   /*
    * Sets the Border color for a Given Resource name and the RGB values.
    */
   public int setBorderColor(String resourceName, Color color)
   {
      m_nBorderStringID = m_ResourceUser.setResourceStringID(m_nBorderStringID, resourceName, color != null ? color.getRGB() : Color.BLACK.getRGB());
      return m_nBorderStringID;
   }

   /*
    * Hides the m_resourceUser lookup.
    */
   public Color getColor(int colorID)
   {
      return new Color(m_ResourceUser.getCOLORREFForStringID(colorID));
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#validateNode()
    */
   public boolean validateNode()
   {
      boolean bValid = false;

      IPresentationElement pe = getPresentationElement();

      if (pe != null)
      {
         bValid = true;
         IElement modelElement = pe.getFirstSubject();

         // Don't _VH here because we don't want to throw.
         IClassifier classifier = modelElement instanceof IClassifier ? (IClassifier)modelElement : null;

         if (classifier != null)
         {
            int numCompartments = getNumCompartments();
            for (int i = 0; i < numCompartments && bValid; i++)
            {
               ICompartment compartment = getCompartment(i);
               if (compartment != null)
               {
                  bValid &= compartment.validate(modelElement);
               }
            }
         }
      }
      return bValid;
   }

   /*
    * Factory function, this object is used to hide nodes and edges from the layoutengine.
    */
   protected ETNodeDrawEngine.ETHiddenNodeList createHiddenList(ETList < IPresentationElement > pPES)
   {
      return new ETHiddenNodeList(pPES);
   }

   /*
    * Returns true if our owner is selected.
    */
   public boolean isSelected()
   {
      IETGraphObject owner = this.getObject();
      return owner != null && owner.isSelected();
   }

   public IETRect getLogicalGrappleBounds()
   {
      IETRect bounds = getLogicalBoundingRect(true);
      if (bounds != null)
      {
         int grapple = 0;
         IETNodeUI nodeUI = this.getNodeUI();
         if (nodeUI != null)
         {
            grapple = nodeUI.getGrappleSize();
         }
         bounds.inflate(grapple + 10);
      }
      return bounds;
   }

   public long invalidate()
   {
      if (!this.isSelected())
      {
         return super.invalidate();
      }
      else
      {
         IETRect bounds = getLogicalGrappleBounds();

         if (bounds != null)
         {
            this.invalidateRect(bounds);
         }
         else
         {
            return super.invalidate();
         }
      }
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#postLoad()
    */
   public long postLoad()
   {
      super.postLoad();

      //		//NL Currently label info is not read from the archive. As a result the labels get deleted in 
      //		// postLoadVerification because there not loaded with presentation element.
      //		// Until readFromArchive is complete, the following provides a workaround for
      //		// initializing the labels with PEs and attaching them to their parent edge model elements
      //		//
      //		ILabelManager labelMgr = getLabelManager();
      //		if (labelMgr != null)
      //		{
      //			labelMgr.resetLabels();
      //		}

      return 0;

   }
   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#invalidateRect(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public long invalidateRect(IETRect rect)
   {
      long retval = super.invalidateRect(rect);
      IETNode node = (IETNode)this.getNode() instanceof IETNode ? (IETNode)this.getNode() : null;

      if (node != null)
         node.invalidateEdges();

      return retval;
   }

   /**
    * Collapses the compartment and resizes the draw engine if necessary
    */
   public boolean collapseCompartment(ICompartment pCompartmentToCollapse, boolean bCollapse)
   {
      boolean bDidResize = false;

      boolean bCurrentState = false;

      bCurrentState = pCompartmentToCollapse.getCollapsed();

      if (bCurrentState != bCollapse)
      {
         // Tell the compartment to collapse/expand
         pCompartmentToCollapse.setCollapsed(bCollapse);

         String sResizeBehavior = this.getResizeBehavior();

         if (sResizeBehavior.equals("PSK_RESIZE_ASNEEDED"))
         {
            // Resize this draw engine
            IETSize size = calculateOptimumSize(null, true);
            // call our Resize() method, holding the top left corner fixed.  Otherwise when right-clicking
            // on a class node and selected Resize to fit the node appears to jump around its centerpoint
            if (size != null)
            {
               resize(size, true);
            }
         }

         bDidResize = true;
      }

      return bDidResize;
   }

   protected boolean isOwnerOnDrawingArea()
   {
      if (this.getDrawingArea() instanceof ADDrawingAreaControl)
      {
         ADDrawingAreaControl da = (ADDrawingAreaControl)getDrawingArea();
         return da.getIsOnDiagram(getParentETElement());
      }
      return false;
   }

   /**
    * Specifies if the padlock drawing supported. 
    */
   protected boolean isPadlockAllawed() {
       return true;
   }
   
   protected void drawPadlock(IDrawInfo pDrawInfo) {
       TSEGraphics graphics = pDrawInfo.getTSEGraphics();
       TSTransform tsTransform = pDrawInfo.getTSTransform();
       //
       Padlock padlock = new Padlock(); 
       setPadlockLocation(pDrawInfo, padlock);
       //
       AffineTransform savedTransform = graphics.getTransform();
       AffineTransform transformToWorld = convertTransform(tsTransform);
       try {
           graphics.transform(transformToWorld);
           padlock.paint(graphics);
       } finally {
           // Restore previous transformation
           graphics.setTransform(savedTransform);
       }
   }
   
   /**
    * Sets the default location of the padlock for current node in world coordinates.
    * 
    * The method can be overridden to specify another location.
    */
   protected void setPadlockLocation(IDrawInfo pDrawInfo, Padlock padlock) {
       TSTransform tsTransform = pDrawInfo.getTSTransform();
       //
       IETRect deviceRect = pDrawInfo.getDeviceBounds();
       TSConstRect worldRect = tsTransform.boundsToWorld(deviceRect.getRectangle());
       //
       padlock.setOriginalPoint(SwingConstants.NORTH_EAST);
       padlock.setLocation(worldRect.getRight() - 2d, worldRect.getTop() - 2d);
   }



    /////////////
    // Accessible
    /////////////

    AccessibleContext accessibleContext;

    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleETNodeDrawEngine();
	} 
	return accessibleContext;
    }


    public class AccessibleETNodeDrawEngine extends AccessibleETDrawEngine {

	////////////////////////////////
	// interface AccessibleComponent
	////////////////////////////////

	public java.awt.Color getBackground() {
	    return getFillColor();
	}

	public void setBackground(java.awt.Color color) {
	    ;
	}
 
	public AccessibleRole getAccessibleRole() {
	    return UMLAccessibleRole.UML_NODE;
	}


    }


}
