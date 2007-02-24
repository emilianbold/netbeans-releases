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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.BaseAction;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler;
//import org.netbeans.modules.uml.ui.products.ad.application.action.Separator;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.diagramActivityEngine.ETInvocationNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.diagramActivityEngine.ObjectNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.LifelineDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETClassDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETCommentDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETLabelDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETStateDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETUseCaseDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveAttribute;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransformOwner;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IResourceUserHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.CompartmentResourceUser;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.UIResources;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.JSeparator;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.Separator;

/**
 * @author Embarcadero Technologies Inc
 *
 *
 */
public abstract class ETCompartment extends ETTransformOwner implements IADCompartment, IETContextMenuHandler, IDrawingPropertyProvider, IResourceUserHelper, Accessible
{
   protected CompartmentResourceUser m_ResourceUser = new CompartmentResourceUser((IResourceUserHelper) this);
   protected int m_nNameFontStringID = -1;
   protected static final int HOLLOW_EDGE_WIDTH = 15;
   
   private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.products.ad.diagramengines.Bundle"; //$NON-NLS-1$
   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
   // The parent draw engine.
   protected IDrawEngine m_engine = null;
   
   // Should the compartment display its name?
   boolean m_showName = true;
   
   // true if compartment cannot be edited
   boolean m_readOnly = false;
   
   protected IDrawInfo m_drawInfo = null;
   
   // Is this compartment selected?
   boolean m_selected = false;
   
   // indicates whether this compartment has any presentation data to save
   boolean m_hasOverride = false;
   
   // The name of this compartment
   protected String m_name = null;
   protected String m_aliasName = null;
   
   // The model element this compartment represents
   protected IElement m_modelElement = null;
   
   // The model element id.  When loading from archive this is used to cache up the model element that should be attached to
   protected String m_XMIID = "";
   
   // Is this compartment Visible
   boolean m_visible = true;
   
   // Can this compartment add context menus?
   boolean m_contextMenuEnabled = true;
   
   boolean m_resizeable = false;
   boolean m_collapsible = false;
   boolean m_collapsed = false;
   boolean m_textWrapping = false;
   
   // the compartment static text
   //   String m_staticText = null;
   
   // defines the area actually occupied by the compartment
   protected IETRect m_boundingRect = new ETDeviceRect();
   
   // defines the area actually occupied by text
   protected IETRect m_textRect = null;
   
   //protected long m_style = SINGLELINE | VCENTER | HCENTER | END_ELLIPSIS;
   
   protected String m_fontString = "";
   
   // The cashed optimum size must be calculated at least once
   // Indicates that the optimum size has been determined
   boolean m_hasOptimumSizeBeenSet = false;
   
   // The last minimum size calculated, this is the size at 100%
   protected IETSize m_cachedOptimumSize = new ETSize(0, 0);
   
   /// The current bounding rect in 100% coordinates
   protected IETSize m_cachedVisibleSize = new ETSize(0, 0);
   
   /// The user defined size, -1,-1 if not to be used.  This is the size at 100%.  You must zoom it at other levels.
   protected IETSize m_cachedUserSize = new ETSize(0, 0);
   
   IETPoint m_ptLogicalOffsetInDrawEngineRect = new ETPoint();
   
   // flag indicating that a mouse click over our text should select us
   boolean m_singleClickSelect = true;
   
   /**
    * The vertical alignment value. Defaults to IADEditableCompartment.CENTER
    *
    * @see  IADEditableCompartment
    */
   private int m_VerticalAlignment = CENTER;
   
   /**
    * The horizontal alignment value. Defaults to IADEditableCompartment.CENTER
    *
    * @see  IADEditableCompartment
    */
   private int m_HorizontalAlignment = CENTER;
   
   /**
    * The border style to use when rendering the compartment.  Defaults to
    * IADNameCompartment.NCBK_DRAW_JUST_NAME.
    *
    * @see IADNameCompartment
    */
   private int m_BorderKind = IADNameCompartment.NCBK_DRAW_JUST_NAME;
   
   //   private IETSize m_CachedOptimumSize = null;
   
   private UIResources m_resources = new UIResources();
   
   public ETCompartment()
   {
      this.init();
   }
   
   public ETCompartment(IDrawEngine pDrawEngine)
   {
      this.setEngine(pDrawEngine);
      this.init();
   }
   
   private void init()
   {
      this.m_readOnly = false;
      this.m_singleClickSelect = true;
      this.setSelected(false);
      this.setCollapsed(false);
   }
   
   public TSGraphObject getOwnerGraphObject()
   {
      IDrawEngine engine = getEngine();
      if (engine != null)
      {
         ITSGraphObject object = engine.getParentETElement();
         return object instanceof TSGraphObject ? (TSGraphObject) object : null;
      }
      
      return null;
   }
   
   public void addDecoration(String sDecorationType, IETPoint pLocation)
   {
      // TODO Auto-generated method stub
   }
   
   public TSEGraphics getGraphics(IDrawInfo pDrawInfo)
   {
      if (pDrawInfo != null)
      {
         return pDrawInfo.getTSEGraphics();
      } else if (m_drawInfo != null)
      {
         return m_drawInfo.getTSEGraphics();
      } else if (getDrawingArea() != null)
      {
         TSEGraphWindow wnd = getDrawingArea().getGraphWindow();
         if (wnd != null)
         {
            Graphics g = wnd.getGraphics();
            if (g != null)
            {
               return wnd.newGraphics(g);
            }
         }
      }
      
      return null;
   }
   
   /**
    * Set the compartment's model element.
    */
   public void addModelElement(IElement pElement, int nIndex)
   {
      this.m_name = "";
      this.m_modelElement = pElement;
      
      this.m_XMIID = "";
      this.getModelElementXMIID();
      this.initResources();
      
      // read the formatted name
      if (m_modelElement != null)
      {
         IDataFormatter dataFormatter = ProductHelper.getDataFormatter();
         if (dataFormatter != null)
         {
            this.setName(dataFormatter.formatElement(m_modelElement));
         }
      }
   }
   
   /**
    * Calculates the "best" size for this compartment.  The calculation sets
    * the member variable m_szCachedOptimumSize, which represents the "best"
    * size of the compartment at 100%.
    *
    * @param pDrawInfo The draw info used to perform the calculation.
    * @param bAt100Pct pMinSize is either in current zoom or 100% based on this
    *                  flag.  If bAt100Pct then it's at 100%
    * @return The optimum size of the compartment.
    */
   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
      
      int height = 0;
      int width = 0;
      
      String sCompartmentName = getName();
      boolean bShowName = getShowName();
      TSEGraphics graphics = getGraphics(pDrawInfo);
      
      
      if (bShowName && (sCompartmentName != null) && (sCompartmentName.length() > 0))
      {
         
         if (graphics != null)
         {
            
            Font originalFont = graphics.getFont();
            
            Font compartmentFont = getCompartmentFont(1.0);
            graphics.setFont(compartmentFont);
            
            height = graphics.getFontMetrics().getHeight() + 2;
            
            if (this.getName() != null)
            {
               width = graphics.getFontMetrics().stringWidth(sCompartmentName) + 2;
            }
            
            graphics.setFont(originalFont);
         }
      }
      
      // Make sure only 1 to 1 ratio is the set in the internal size,
      internalSetOptimumSize(width, height);
      
      // Now Scale the device units
      return bAt100Pct ? getOptimumSize(bAt100Pct) : this.scaleSize(this.m_cachedOptimumSize, graphics != null ? graphics.getTSTransform() : this.getTransform());
   }
   
   /**
    * Retrieves the cached optimum size.
    *
    * @return The optimum size.
    */
   protected IETSize getCachedOptimumSize()
   {
      return m_cachedOptimumSize;
   }
   
   /**
    * Sets the internally cached optimum size member. {PROTECTED]
    *
    * @param size The size passed in is the actual zoomed
    *              size at the current zoom level.
    */
   protected void internalSetOptimumSize(IETSize size)
   {
      m_cachedOptimumSize = size;
      m_hasOptimumSizeBeenSet = true;
   }
   
   /**
    * Sets the internally cached optimum size member. {PROTECTED]
    *
    * @param size The size passed in is the actual zoomed
    *              size at the current zoom level.
    */
   protected void internalSetOptimumSize(int width, int height)
   {
      internalSetOptimumSize(new ETSize(width, height));
   }
   
    /*
     * Returns a scaled size in Device untils using the input transform you must pass a size calculated at zoom level at Factor it was calculated at.
     */
   protected IETSize scaleSize(final IETSize atOneHundred, final TSTransform windowTransform, double fromFactor)
   {
      if (atOneHundred != null && windowTransform != null)
      {
         TSTransform transform = (TSTransform) windowTransform.clone();
         transform.setScale(fromFactor);
         double worldSizeX = transform.widthToWorld(atOneHundred.getWidth());
         double worldSizeY = transform.heightToWorld(atOneHundred.getHeight());
         Dimension d = windowTransform.sizeToDevice(worldSizeX, worldSizeY);
         
         return new ETSize(d.width, d.height);
      }
      return null;
   }
   
    /*
     * Returns a scaled size in Device untils using the input transform, you must pass a size calculated at zoom level 1.0
     */
   protected IETSize scaleSize(final IETSize atOneHundred, final TSTransform windowTransform)
   {
      return scaleSize(atOneHundred, windowTransform, 1.0);
   }
   
    /*
     * Returns a scaled size in Device untils, you must pass a size calculated at zoom level 1.0
     */
   protected IETSize scaleSize(final IETSize atOneHundred)
   {
      return scaleSize(atOneHundred, this.getTransform());
   }
   
   /**
    * Returns the size of the compartment if it could draw itself with no
    * restrictions (does not recalculate, should not be called between zoom
    * operations).  If the optimum size has not been set, CalculateOptimumSize()
    * is called
    *
    * @param bAt100Pct
    * @return The optimum size.
    */
   public IETSize getOptimumSize(boolean bAt100Pct)
   {
      if (this.m_collapsed)
      {
         return new ETSize(0, 0);
      } else if (!m_hasOptimumSizeBeenSet)
      {
         return calculateOptimumSize(null, bAt100Pct);
      } else if (bAt100Pct)
      {
         return new ETSize(m_cachedOptimumSize.getWidth(), m_cachedOptimumSize.getHeight());
         
      } else
      {
         return scaleSize(m_cachedOptimumSize);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#clearStretch()
     */
   public long clearStretch(IDrawInfo drawInfo)
   {
      m_cachedUserSize.setSize(-1,-1);
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#clone(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine, org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
     */
   public ICompartment clone(IDrawEngine pParentDrawEngine)
   {
      // TODO Auto-generated method stub
      return null;
   }
   public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
      m_drawInfo = pDrawInfo;
      
      IETRect rectBounding = (IETRect)pBoundingRect.clone();
      
      // if user has resized the compartment apply those now
      if (m_collapsed)
      {
         rectBounding.setBottom(rectBounding.getTop());
      }
      else
      {
         final double dZoomLevel = this.getZoomLevel(pDrawInfo);
         
         if (m_cachedUserSize.getHeight() > 0)
         {
            final int userSizeAtThisZoomLevel = (int)Math.round(m_cachedUserSize.getHeight() * dZoomLevel);
            rectBounding.setBottom(Math.min(rectBounding.getBottom(), rectBounding.getTop() + userSizeAtThisZoomLevel));
         }
      }
      
      m_cachedVisibleSize.setWidth(rectBounding.getIntWidth());
      m_cachedVisibleSize.setHeight(rectBounding.getIntHeight());
      
      setBoundingRect(pDrawInfo, (IETRect)rectBounding.clone());
      
      IETRect originRect = (IETRect)rectBounding.clone();
      
      if (isCollapsible())
      {
         originRect.setBottom(originRect.getTop());
      }
      setWinClientRectangle(originRect);
   }
   
   private void setBoundingRect(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
      
      TSEGraphics graphics = getGraphics(pDrawInfo);
      
      if (graphics != null && pBoundingRect != null)
      {
         
         Font originalFont = graphics.getFont();
         
         Font compartmentFont = getCompartmentFont(pDrawInfo != null ? pDrawInfo.getFontScaleFactor() : graphics.getTSTransform().getScaleX());
         graphics.setFont(compartmentFont);
         
//         int textWidth = 0;
//         
//         if (getName() != null)
//         {
//            textWidth = graphics.getFontMetrics().stringWidth(getName());
//         } else
//         {
//            textWidth = pBoundingRect.getIntWidth();
//         }
         
         m_boundingRect = (IETRect) pBoundingRect.clone();
         
//         m_textRect = new ETDeviceRect(pBoundingRect.getIntX(), pBoundingRect.getIntY(), textWidth, pBoundingRect.getIntHeight());
         m_textRect = new ETDeviceRect(pBoundingRect.getIntX(), pBoundingRect.getIntY(), pBoundingRect.getIntWidth(), pBoundingRect.getIntHeight());
         graphics.setFont(originalFont);
      }
   }
   
   /**
    * Invokes the in=place editor for this compartment.
    *
    * @param bNew[in] - Flag indicating that this is a new compartment and should be destroyed if the edit is cancelled.
    * Default is FALSE.
    * @param KeyCode[in] - The key pressed that invoked editing, NULL if none.  Default is NULL.
    * @param nPos[in] - The horizontal position for the cursor, used if editing was activated via the mouse. The position value
    * is in pixels in client coordinates, e.g. the left edge of the control is position 0.  Default is -1 which does not position
    * the cursor (some translators may select a field by default).
    */
   public long editCompartment(boolean bNew, int nKeyCode, int nShift, int nPos)
   {
      //nothing to do - editable compartment will handle it, a compartment by default is non-editable.
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#get_DefaultCompartment()
     */
   public ICompartment getDefaultCompartment()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   /**
    * Returns the bounding rect for this compartment
    */
   public IETRect getBoundingRect()
   {
      return this.m_boundingRect;
   }
   
   /**
    * Returns the bounding rect for this compartment as a device rect
    */
   protected ETDeviceRect getBoundingAsDeviceRect()
   {
      if (m_boundingRect instanceof ETRect)
      {
         // This special case is for all the code that depends
         // on the bounding rectangle in device coordinates.
         return ((ETRect) m_boundingRect).getAsDeviceRect();
      } else if (m_boundingRect instanceof ETDeviceRect)
      {
         return (ETDeviceRect) m_boundingRect;
      }
      
      return null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCenterText()
     */
   public boolean getCenterText()
   {
      // Please note that is overidden,.
      return false;
   }
   
   public boolean getCollapsed()
   {
      return this.m_collapsed;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentHasNonRectangularShape()
     */
   public boolean getCompartmentHasNonRectangularShape()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   /**
    * This is the name of the drawengine used when storing and reading from the product archive.
    *
    * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
    * product archive (etlp file).
    */
   public String getCompartmentID()
   {
      return "Compartment";
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentShape()
     */
   public ETList < IETPoint > getCompartmentShape()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public IETSize getTextExtent(IDrawInfo drawInfo, String sText)
   {
      return drawInfo != null ? GDISupport.getTextExtent(drawInfo.getTSEGraphics(), sText) : null;
   }
   
	public IETSize getCurrentSize(TSTransform transform, boolean bAt100Pct )
	{
		IETSize retValue = null;
		 IETSize returnSize = null;
		 long nX = 0;
		 long nY = 0;
      
		 // fetch the last calculated size
		 returnSize = this.m_cachedOptimumSize;
		 if (returnSize == null)
		 {
			 return new ETSize((int) nX, (int) nY);
		 }
      
		 // if any sizes have been changed by the user report those instead
		 if (m_cachedUserSize != null && m_cachedUserSize.getWidth() > 0)
		 {
			 returnSize.setWidth(m_cachedUserSize.getWidth());
		 }
		 if (m_cachedUserSize != null && m_cachedUserSize.getHeight() > 0)
		 {
			 returnSize.setHeight(m_cachedUserSize.getHeight());
		 }
      
		 // apply zooming if requested
		 if (bAt100Pct == false)
		 {
			 // scale the size
			 return scaleSize(returnSize, transform);
		 } else
		 {
			 nX = returnSize.getWidth();
			 nY = returnSize.getHeight();
		 }
      
		 retValue = new ETSize((int) nX, (int) nY);
		 return retValue;		
	}
	
   public IETSize getCurrentSize(boolean bAt100Pct)
   {
      return this.getCurrentSize(this.getTransform(), bAt100Pct);
   }
   
   /**
    * Returns the best fit size for this compartment.  Single compartments
    * return the optimum size.
    *
    * @return The desired size.  The desired size will be equal to the optimum
    *         size if scrolling is not in effect, otherwise it will be equal to
    *         the optimum width and the current height.
    */
   public IETSize getDesiredSizeToFit()
   {
      return calculateOptimumSize(null, true);
   }
   
   /**
    * Enables or disables the compartments context menu
    *
    * @param pVal [out,retval] true if this compartment can add context menu items
    */
   public boolean getEnableContextMenu()
   {
      return m_contextMenuEnabled;
   }
   
   public IDrawEngine getEngine()
   {
      return this.m_engine;
   }
   
   /**
    * Converts a diagram logical point to a logical location within the compartment.
    *
    * @param pPosition Logical location within the diagram
    *
    * @return A non-zoomed location within the compartment (0,0) is the upper left
    */
   public IETPoint logicalToCompartmentLogical(IETPoint position)
   {
      return logicalToCompartmentLogical(position.getX(), position.getY());
   }
   
   /**
    * Converts a diagram logical point to a logical location within the compartment.
    *
    * @param pPosition Logical location within the diagram
    *
    * @return A non-zoomed location within the compartment (0,0) is the upper left
    */
   public IETPoint logicalToCompartmentLogical(int x, int y)
   {
      IETPoint retVal = null;
      
      IETRect logicalBounding = getLogicalBoundingRect();
      if (logicalBounding != null)
      {
         retVal = new ETPoint();
         retVal.setX(x - logicalBounding.getLeft());
         retVal.setY(logicalBounding.getTop() - y);
      } else
      {
         retVal = new ETPoint(x, y);
      }
      
      return retVal;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getLogicalOffsetInDrawEngineRect()
     */
   public IETPoint getLogicalOffsetInDrawEngineRect()
   {
      return m_ptLogicalOffsetInDrawEngineRect;
   }
   
   public IElement getModelElement()
   {
      
      if (this.m_modelElement == null)
      {
         if (this.m_XMIID.length() > 0)
         {
            this.reattach(this.m_XMIID);
            return this.m_modelElement;
         } else
         {
            return null;
         }
      } else
      {
         return this.m_modelElement;
      }
   }
   
   public String getModelElementXMIID()
   {
      if (m_XMIID == null || m_XMIID.length() == 0 && m_modelElement != null)
      {
         setModelElementXMIID(m_modelElement.getXMIID());
      }
      
      return m_XMIID != null ? m_XMIID : "";
   }
   
   public void setModelElementXMIID(String newVal)
   {
      this.m_XMIID = newVal;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getParentResource()
     */
   public int getParentResource()
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
   /**
    * Get the parent product element, if there is one.
    */
   public IETLabel getParentETLabel()
   {
      IETLabel retVal = null;
      IDrawEngine pEngine = getEngine();
      if (pEngine != null && pEngine instanceof ILabelDrawEngine)
      {
         retVal = ((ILabelDrawEngine)pEngine).getParentETLabel();
      }
      return retVal;
   }
   
   public boolean getReadOnly()
   {
      boolean isReadOnly = m_readOnly;
      
      //  readonly drawengine implies readonly compartment
      if( (m_engine != null) &&
          !isReadOnly )
      {
         isReadOnly = m_engine.getReadOnly(); 
      }
      
      return isReadOnly;
   }
   
   public boolean isSelected()
   {
      return this.m_selected;
   }
   
   public boolean getShowName()
   {
      return this.m_showName;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getTextWrapping()
     */
   public boolean getTextWrapping()
   {
      return m_textWrapping;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getVerticallyCenterText()
     */
   public boolean getVerticallyCenterText()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   public boolean getVisible()
   {
      return this.m_visible;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getVisibleSize(int, int, boolean)
     */
   public IETSize getVisibleSize(boolean bAt100Pct)
   {
      
      int nX = m_cachedVisibleSize.getWidth();
      int nY = m_cachedVisibleSize.getHeight();
      
      // Remove zooming if requested
      if( bAt100Pct == true )
      {
         // scale the size
         final double dZoomLevel = getZoomLevel();
         nX = (int)Math.round( m_cachedVisibleSize.getWidth() / dZoomLevel );
         nY = (int)Math.round( m_cachedVisibleSize.getHeight() / dZoomLevel );
      }
      
      return new ETSize(nX,nY);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleKeyDown(int, int)
     */
   public boolean handleKeyDown(int keyCode, int Shift)
   {
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleCharTyped(char)
     */
   public boolean handleCharTyped(char ch)
   {
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleKeyUp(int, int)
     */
   public boolean handleKeyUp(int KeyCode, int Shift)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleRightMouseButton(org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent)
     */
   public boolean handleRightMouseButton(MouseEvent pEvent)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleSetCursor(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
     */
   public boolean handleSetCursor(IETPoint point, ISetCursorEvent event)
   {
      boolean bHandled = false;
      
        /* TODO get the m_textRect working properly. It is not working in C++ at this point
              final IETRect rect = getWinScaledOwnerRect();
         
              if( getWinScaledOwnerRect().contains( point ) )
              {
                 bHandled = true;
         
                 // set cursor to an arrow only when over text
                 if( (m_textRect.getHeight() >= 8) && m_textRect.contains( point ) )
                 {
                    event.setCursor( Cursor.getPredefinedCursor( Cursor.TEXT_CURSOR ) );
                 }
              }
         */
      
      return bHandled;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#hasOverride()
     */
   public boolean hasOverride()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#initResources()
     */
   private boolean m_bInitResources = false;
   public void initResources()
   {
      if (m_bInitResources == false)
      {
         m_resources.setResourceID(UIResources.CK_FONT);
         m_resources.setResourceID(UIResources.CK_TEXTCOLOR);
         m_resources.setResourceID(UIResources.CK_FILLCOLOR);
         m_bInitResources = true;
      }
   }
   
   /**
    * Toggle selection status.
    */
   public void invertSelected()
   {
      this.m_selected = !this.m_selected;
   }
   
   public boolean isCollapsible()
   {
      return this.m_collapsible;
   }
   
   public boolean isPointInCompartment(IETPoint pPoint)
   {
      IETRect deviceRect = getBoundingAsDeviceRect();
      return deviceRect != null ? deviceRect.contains(pPoint) : false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#isPointInCompartmentYAxis(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
     */
   public boolean isPointInCompartmentYAxis(IETPoint pLogical)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#isPointInOptimum(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
     */
   public boolean isPointInOptimum(IETPoint pLogical)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   public void setResizeToFitCompartments(boolean resize)
   {
      m_resizeable = resize;
   }
   
   public boolean isResizeable()
   {
      return this.m_resizeable;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#isResizing()
     */
   public boolean isResizing()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#layout(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
     */
   public long layout(IETRect pCompartmentInDE)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
     */
   public long modelElementDeleted(INotificationTargets pTargets)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
     */
   public long modelElementHasChanged(INotificationTargets pTargets)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#nodeResized(int)
     */
   public long nodeResized(int nodeResizeOriginator)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
     */
   public long onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
   public void onContextMenu(IMenuManager manager)
   {
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
     */
   public long onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#onGraphEvent(int)
     */
   public long onGraphEvent(int nKind)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#postLoad()
     */
   public long postLoad()
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#queryToolTipData(org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData)
     */
   public long queryToolTipData(IToolTipData pToolTipData)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
   /**
    * Update from archive.
    *
    * @param pProductArchive [in] The archive we're reading from
    * @param pCompartmentElement [in] The element where this compartment's information should exist
    */
   public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pCompartmentElement)
   {
      if (pProductArchive != null && pCompartmentElement != null)
      {
         // set the default resources for this compartment
         initResources();
         
         // get compartment's XMI ID
         //this.m_XMIID = pCompartmentElement.getAttributeString(IProductArchiveDefinitions.COMPARTMENTXMIIDATTRIBUTE_STRING);
         IProductArchiveAttribute idAttr = pCompartmentElement.getAttribute(IProductArchiveDefinitions.COMPARTMENTXMIIDATTRIBUTE_STRING);
         if (idAttr != null)
         {
            String str = idAttr.getStringValue();
            if (str != null && str.length() > 0)
            {
               m_XMIID = str;
            } else
            {
               m_XMIID = "";
            }
         }
         
         // get our text
         String text = pCompartmentElement.getAttributeString(IProductArchiveDefinitions.COMPARTMENTTEXTATTRIBUTE_STRING);
         if (text != null && text.length() > 0)
         {
            setName(text);
         }
         //this.m_staticText = pCompartmentElement.getAttributeString(IProductArchiveDefinitions.COMPARTMENTTEXTATTRIBUTE_STRING);
         this.setName(pCompartmentElement.getAttributeString(IProductArchiveDefinitions.COMPARTMENTTEXTATTRIBUTE_STRING));
         
         //this.m_aliasName = pCompartmentElement.getAttributeString(IProductArchiveDefinitions.COMPARTMENTALIASATTRIBUTE_STRING);
         //this.m_style = pCompartmentElement.getAttributeString(IProductArchiveDefinitions.COMPARTMENTTEXTSTYLE_STRING);
         
         // Get our font
         ETPairT < IProductArchiveElement, String > result = pProductArchive.getTableEntry(pCompartmentElement, IProductArchiveDefinitions.COMPARTMENTFONTATTRIBUTE_STRING, IProductArchiveDefinitions.COMPARTMENTFONTTABLE_STRING);
         if (result != null)
         {
            String foundId = result.getParamTwo();
            if (foundId != null && foundId.length() > 0)
            {
               //setResourceID(CK_FONT, foundId);
            }
         }
         
         // Get our forground color
         result = null;
         result = pProductArchive.getTableEntry(pCompartmentElement, IProductArchiveDefinitions.COMPARTMENTFOREATTRIBUTE_STRING, IProductArchiveDefinitions.COMPARTMENTFONTCOLORTABLE_STRING);
         if (result != null)
         {
            String foundId = result.getParamTwo();
            if (foundId != null && foundId.length() > 0)
            {
               //setResourceID(CK_TEXTCOLOR, foundId);
            }
         }
         
         // Get our text style
         //m_style = pCompartmentElement.getAttributeLong(IProductArchiveDefinitions.COMPARTMENTTEXTSTYLE_STRING);
         String styles = pCompartmentElement.getAttributeString(IProductArchiveDefinitions.COMPARTMENTTEXTSTYLE_STRING);
         setTextStyles(styles);
         
         // Get our collapsed state
         m_collapsed = pCompartmentElement.getAttributeBool(IProductArchiveDefinitions.COMPARTMENTCOLLAPSED_STRING);
         
         // Tell the drawing factory that we need to load our resources.  This has to happen
         // though after all the compartments and the drawengine is created.  The drawengine
         // needs to call InitResources first to setup the basic resources - then we overlay
         // and overwrite those basic resources with the ones in the file.  So we need to
         // delay the loading of resources until after the draw engine is done.  See
         // DrawEngineImpl::ReadFromArchive for where we complete the loading of the compartment.
         DrawingFactory.addCompartmentResourcePair(this, pCompartmentElement);
      }
   }
   
   protected String getTextStylesAsString()
   {
      StringBuffer retVal = new StringBuffer();
      
      int vAlignment = getVerticalAlignment();
      if((vAlignment & CENTER) == CENTER)
      {
         addPipedDelementedString(retVal, "VCENTER");
      }
      
      if((vAlignment & LEFT) == LEFT)
      {
         addPipedDelementedString(retVal, "LEFT");
      }
      
      if((vAlignment & RIGHT) == RIGHT)
      {
         addPipedDelementedString(retVal, "RIGHT");
      }
      
      int hAlignment = getHorizontalAlignment();
      if((hAlignment & CENTER) == CENTER)
      {
         addPipedDelementedString(retVal, "HCENTER");
      }
      
      if((hAlignment & TOP) == TOP)
      {
         addPipedDelementedString(retVal, "TOP");
      }
      
      if((hAlignment & BOTTOM) == BOTTOM)
      {
         addPipedDelementedString(retVal, "BOTTOM");
      }
      
      if(getTextWrapping() == true)
      {
         addPipedDelementedString(retVal, "MULTILINE");
      }
      else
      {
         addPipedDelementedString(retVal, "SINGLELINE");
      }
      
      return retVal.toString();
   }
   
   protected void addPipedDelementedString(StringBuffer buffer, String value)
   {
      if(buffer.length() > 0)
      {
         buffer.append("|");
      }
      buffer.append(value);
   }
   
   protected void setTextStyles(String styles)
   {
      StringTokenizer tokenizer = new StringTokenizer(styles, "|");
      
      m_VerticalAlignment = 0;
      m_HorizontalAlignment = 0;
      while(tokenizer.hasMoreTokens() == true)
      {
         String curToken = tokenizer.nextToken();
         if(curToken.equals("VCENTER") == true)
         {
            m_VerticalAlignment |= CENTER;
         }
         else if(curToken.equals("HCENTER") == true)
         {
            m_HorizontalAlignment |= CENTER;
         }
         else if(curToken.equals("LEFT") == true)
         {
            m_VerticalAlignment |= LEFT;
         }
         else if(curToken.equals("RIGHT") == true)
         {
            m_VerticalAlignment |= RIGHT;
         }
         else if(curToken.equals("TOP") == true)
         {
            m_HorizontalAlignment |= TOP;
         }
         else if(curToken.equals("BOTTOM") == true)
         {
            m_HorizontalAlignment |= BOTTOM;
         }
         else if(curToken.equals("SINGLELINE") == true)
         {
            setTextWrapping(false);
         }
         else if(curToken.equals("MULTILINE") == true)
         {
            setTextWrapping(true);
         }
         else if(curToken.equals("END_ELLIPSIS") == true)
         {
            
         }
      }
   }
   
   /**
    * Reattachs to the model element whose XMIID matches that specified.
    */
   public void reattach()
   {
      reattach(null);
   }
   
   /**
    * Reattachs to the model element whose XMIID matches that specified.
    */
   public void reattach(String pCompartmentID)
   {
      String compartmentId = pCompartmentID;
      if (compartmentId == null || compartmentId.length() == 0)
      {
         compartmentId = getXMIID();
      }
      
      ElementLocator elementLocator = new ElementLocator();
      IETGraphObjectUI ui = this.getEngine().getParent();
      
      IElement element = elementLocator.findElementByID(ui.getTopLevelMEIDValue(), compartmentId);
      if (element != null)
      {
         this.addModelElement(element, -1);
         this.getEngine().invalidate();
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#saveModelElement()
     */
   public long saveModelElement()
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#selectExtended(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
     */
   public boolean selectExtended(IETRect rect)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#setCenterText(boolean)
     */
   public void setCenterText(boolean value)
   {
      // TODO Auto-generated method stub
      
   }
   
   public void setCollapsed(boolean value)
   {
      this.m_collapsed = value;
   }
   
   public void setCurrentSize(IETSize pNewSize)
   {
      
/*
      if (m_cachedOptimumSize != null && m_cachedOptimumSize == pNewSize)
      {
         clearStretch(drawInfo);
      }
      else
      {
         m_cachedUserSize = new ETSize(pNewSize.getWidth(), pNewSize.getHeight());
         m_cachedVisibleSize = new ETSize(pNewSize.getWidth(), pNewSize.getHeight());
      }
*/
      //
      m_cachedUserSize = new ETSize(pNewSize);
      m_cachedVisibleSize = new ETSize(pNewSize);
   }
   
   /**
    * Enables or disables the compartments context menu
    *
    * @param bVisible [in] true if this compartment can add context menu items
    */
   public void setEnableContextMenu(boolean value)
   {
      m_contextMenuEnabled = value;
   }
   
   public void setEngine(IDrawEngine pEngine)
   {
      this.m_engine = pEngine;
      
      // m_Resources.ClearResourceManager();
      
      if (m_engine != null)
      {
         m_resources.setParentResources(m_engine.getResources());
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#setLogicalOffsetInDrawEngineRect(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
     */
   public void setLogicalOffsetInDrawEngineRect(IETPoint value)
   {
      m_ptLogicalOffsetInDrawEngineRect = value;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#setParentResource(int)
     */
   public long setParentResource(int pParentResource)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
   public void setReadOnly(boolean pValue)
   {
      this.m_readOnly = pValue;
   }
   
   public void setSelected(boolean pValue)
   {
      this.m_selected = pValue;
      if ((m_selected == true) && (m_engine != null))
      {
          m_engine.setAnchoredCompartment(this);
      }
   }
   
   public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   public void setShowName(boolean pValue)
   {
      this.m_showName = pValue;
   }
   
//   public void setStyle(int value)
//   {
//      m_style = value;
//   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#setTextWrapping(boolean)
     */
   public void setTextWrapping(boolean value)
   {
      m_textWrapping = value;
   }
   
   public void setTransformSize(IETSize pNewSize)
   {
      if (pNewSize != null)
      {
         setTransformSize(pNewSize.getWidth(), pNewSize.getHeight());
      }
   }
   
   public void setTransformSize(int width, int height)
   {
      setAbsoluteSize(width, height);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#setVerticallyCenterText(boolean)
     */
   public void setVerticallyCenterText(boolean value)
   {
      // TODO Auto-generated method stub
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getContained()
     */
   public ETList < IPresentationElement > getContained()
   {
      return getContained(this);
   }
   
   /**
    * Retrieves the presentation elements graphically contained by this compartment.
    */
   public static ETList < IPresentationElement > getContained(ICompartment compartment)
   {
      ETList < IPresentationElement > pes = null;
      
      INodePresentation thisNodePE = TypeConversions.getNodePresentation(compartment);
      if (thisNodePE != null)
      {
         // Find all the presentation elements inside this compartment's bounding rectangle
         
         IETRect rect = compartment.getLogicalBoundingRect();
         pes = thisNodePE.getPEsViaRect(false, rect);
      }
      
      return pes;
   }
   
   public void setVisible(boolean pValue)
   {
      this.m_visible = pValue;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#stretch(org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext)
     */
   public long stretch(IStretchContext pStretchContext)
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#validate(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
   public boolean validate(IElement pElement)
   {
      String sID = pElement != null ? pElement.getXMIID() : null;
      
      if (sID != null && sID.length() > 0)
      {
         String sOurID = getXMIID();
         return sOurID != null && sOurID.length() > 0 && sOurID.equals(sID);
      }
      return false;
   }
   
   /**
    * Write ourselves to archive, returns the compartment element.
    *
    * @param pProductArchive [in] The archive we're saving to
    * @param pElement [in] The current element, or parent for any new attributes or elements
    * @param pCompartmentElement [out] The created element for this compartment's information
    */
   public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pEngineElement)
   {
      IProductArchiveElement retObj = null;
      if (pEngineElement != null)
      {
         // create an element for this compartment
         String compEleId = getXMIID();
         String compName = getCompartmentID();
         IProductArchiveElement pElement = pEngineElement.createElement(IProductArchiveDefinitions.COMPARTMENTNAMEELEMENT_STRING);
         if (pElement != null)
         {
            // Add the compartment name to the table of compartment names, store just the id so we
            // can store more compactly.
            IProductArchiveAttribute createdAttr = pProductArchive.insertIntoTable(IProductArchiveDefinitions.COMPARTMENTNAMETABLE_STRING, compName, IProductArchiveDefinitions.COMPARTMENTNAMETABLEINDEXATTRIBUTE_STRING, pElement);
            createdAttr = null;
            if (compEleId != null && compEleId.length() > 0)
            {
               // write xml id
               pElement.addAttributeString(IProductArchiveDefinitions.COMPARTMENTXMIIDATTRIBUTE_STRING, compEleId);
            }
            
            // return element if requested
            retObj = pElement;
            
            // write out changes to text fields
            // write out our text
            String str = getName();
            pElement.addAttributeString(IProductArchiveDefinitions.COMPARTMENTTEXTATTRIBUTE_STRING, str);
            
            // Write our text style
            pElement.addAttributeString(IProductArchiveDefinitions.COMPARTMENTTEXTSTYLE_STRING, getTextStylesAsString());
            
            // Set our collapsed state
            if (m_collapsed)
            {
               pElement.addAttributeBool(IProductArchiveDefinitions.COMPARTMENTCOLLAPSED_STRING, m_collapsed);
            }
            
            // now write out our resourceid's
            m_ResourceUser.writeResourcesToArchive(pProductArchive, pElement);
         }
      }
      return retObj;
   }
   
   /**
    * @param pProductArchive
    * @param pElement
    */
   private void writeResourcesToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * Get the compartment's XMI ID.
    */
   public String getXMIID()
   {
      if (m_XMIID == null || m_XMIID.length() == 0)
      {
         if (m_modelElement != null)
         {
            m_XMIID = m_modelElement.getXMIID();
         }
      }
      return m_XMIID;
   }
   
   // Is the mouse unhandled and within the bounding rect of an editable compartment?
   public boolean isMouseInBoundingRect(MouseEvent pEvent, boolean pHandled)
   {
      ETDeviceRect rect = getBoundingAsDeviceRect();
      Point point = pEvent.getPoint();
      return !pHandled && !this.m_readOnly && rect != null && rect.contains(point.x, point.y);
   }
   
   // Is the mouse unhandled and within the text area of an editable compartment?
   public boolean isMouseInTextRect(MouseEvent pEvent, boolean pHandled)
   {
   	return false;
   }
   
   public boolean handleLeftMouseButton(MouseEvent pEvent)
   {
      boolean retVal = false;
      if (this.m_singleClickSelect && this.isMouseInTextRect(pEvent, false))
      {
         if (pEvent.isControlDown())
         {
            this.invertSelected();
         } else if (pEvent.isShiftDown())
         {
            this.m_engine.selectExtendCompartments(pEvent);
         } else
         {
            this.m_engine.selectAllCompartments(false);
            this.m_engine.anchorMouseEvent(pEvent, this);
            this.invertSelected();
         }
         retVal = true;
      }
      return retVal;
   }
   
   public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos, boolean bCancel)
   {
      return this.m_selected && this.isPointInCompartment(pStartPos);
   }
   
   public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent)
   {
      boolean isHandled = false;
      boolean bBeginEdit = false;
      if (isMouseInBoundingRect(pEvent, isHandled))
      {
         bBeginEdit = true;
      } else if (isMouseInTextRect(pEvent, isHandled))
      {
         bBeginEdit = true;
      }
      
      if (bBeginEdit)
      {
         //int x = pEvent.getX() - getWinScaledOwnerRect().left;
         editCompartment(false, 0, 0, pEvent.getX());
         isHandled = true;
		 	if (!(this.m_engine instanceof ETCommentDrawEngine) &&
                !(this.m_engine instanceof ETUseCaseDrawEngine) &&
                !(this.m_engine instanceof ETInvocationNodeDrawEngine) &&
                !(this.m_engine instanceof ObjectNodeDrawEngine) &&
                !(this.m_engine instanceof LifelineDrawEngine) &&
                !(this.m_engine instanceof ETStateDrawEngine)&&
                !(this.m_engine instanceof ETClassDrawEngine)&&
                !(this.m_engine instanceof ETLabelDrawEngine))
         	{
         this.m_engine.selectAllCompartments(false);
         this.m_engine.anchorMouseEvent(pEvent, this);
         this.invertSelected();
      }
      }
      return isHandled;
   }
   
   public boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos)
   {
      return false;
   }
   
   public boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pElements, boolean bMoving)
   {
      return false;
   }
   
   public boolean handleLeftMouseButtonPressed(MouseEvent pEvent)
   {
      return isMouseInTextRect(pEvent, false);
   }
   
   public String getName()
   {
      return this.m_name;
   }
   
   public void setName(String pNewName)
   {
      this.m_name = pNewName;
   }
   
   public String getFontString()
   {
      return m_fontString;
   }
   
   public void setFontString(String string)
   {
      m_fontString = string;
   }
   
   // Returns the text for the stereotype compartment
   public String getStereotypeText(IElement pElement)
   {
      return pElement != null ? pElement.getAppliedStereotypesAsString(false) : null;
   }
   
   public void save()
   {
      //editable compartment will handle it.
   }
   
   public void cancelEditing()
   {
      //editable compartment will handle it.
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADEditableCompartment#setHorizontalAlignment(int)
     */
   public void setHorizontalAlignment(int alignment)
   {
      m_HorizontalAlignment = alignment;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADEditableCompartment#getHorizontalAlignment()
     */
   public int getHorizontalAlignment()
   {
      return m_HorizontalAlignment;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADEditableCompartment#setVerticalAlignment(int)
     */
   public void setVerticalAlignment(int alignment)
   {
      m_VerticalAlignment = alignment;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADEditableCompartment#getVerticalAlignment()
     */
   public int getVerticalAlignment()
   {
      return m_VerticalAlignment;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment#setNameCompartmentBorderKind(int)
     */
   public void setNameCompartmentBorderKind(int value)
   {
      if (value >= 0 && value <= IADNameCompartment.NCBK_DRAW_BORDER_TOTAL)
      {
         m_BorderKind = value;
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment#getNameCompartmentBorderKind()
     */
   public int getNameCompartmentBorderKind()
   {
      return m_BorderKind;
   }
   
   /**
    * Adds the interaction operand choices for the combined fragment.
    *
    * @param pContextMenu[in] The menu about to be displayed
    */
   public void addInteractionOperandButtons(IMenuManager manager)
   {
      IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_POPUP_INTERACTION_OPERAND"), "");
      if (subMenu != null)
      {
         subMenu.add(createMenuAction(loadString("IDS_CF_EDIT_INTERACTION_CONSTRAINT"), "MBK_CF_EDIT_INTERACTION_CONSTRAINT"));
      }
   }
   
   /**
    * Adds the interaction operands choices for the IADZonesCompartment.
    *
    * @param pContextMenu[in] The menu about to be displayed
    */
   protected void addInteractionOperandsButtons(IMenuManager manager)
   {
      IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_POPUP_INTERACTION_OPERAND"), "");
      if (subMenu != null)
      {
         subMenu.add(createMenuAction(loadString("IDS_CF_ADD_INTERACTION_OPERAND"), "MBK_Z_ADD_ROW"));
         subMenu.add(createMenuAction(loadString("IDS_CF_DELETE_INTERACTION_OPERAND"), "MBK_Z_DELETE_ROW"));
      }
   }
   
   /**
    * Adds the lifeline remove destroy element button.
    *
    * @param pContextMenu[in] The menu about to be displayed
    */
   public void addLifelineRemoveDestroyButton(IMenuManager manager)
   {
      manager.add(createMenuAction(loadString("IDS_LL_REMOVE_DESTROY"), "MBK_LL_REMOVE_DESTROY"));
   }
   
   /**
    * Adds the partions choices for the IADZonesCompartment.
    *
    * @param pContextMenu[in] The menu about to be displayed
    */
   void addActivityPartionsButtons(IMenuManager manager, int /*ORIENTATION*/
   orientation)
   {
      IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_POPUP_PARTITIONS"), "");
      if (subMenu != null)
      {
         if (orientation != IETZoneDividers.DMO_HORIZONTAL)
         {
            subMenu.add(createMenuAction(loadString("IDS_PARTITION_ADD_COLUMN"), "MBK_Z_ADD_COLUMN"));
            if (orientation != IETZoneDividers.DMO_UNKNOWN)
            {
               subMenu.add(createMenuAction(loadString("IDS_PARTITION_DELETE_COLUMN"), "MBK_Z_DELETE_COLUMN"));
            }
         }
         
         addSeparatorMenuItem(subMenu);
         
         if (orientation != IETZoneDividers.DMO_VERTICAL)
         {
            subMenu.add(createMenuAction(loadString("IDS_PARTITION_ADD_ROW"), "MBK_Z_ADD_ROW"));
            if (orientation != IETZoneDividers.DMO_UNKNOWN)
            {
               subMenu.add(createMenuAction(loadString("IDS_PARTITION_DELETE_ROW"), "MBK_Z_DELETE_ROW"));
            }
         }
         
         // Add the ability to populate the partitions
            /* TODO
                       // Add the ability to populate the partitions
                       addSeparatorMenuItem( subMenu );
             
                       subMenu.add(createMenuAction(loadString("IDS_POPULATE_THIS_PARTITION"), "MBK_POPULATE_THIS_Z"));
                       subMenu.add(createMenuAction(loadString("IDS_POPULATE_ALL_PARTITIONS"), "MBK_POPULATE_ALL_ZS"));
             */
      }
   }
   
   /**
    * Adds the DELETE/delete buttons for state events and internal transitions
    *
    * @param pContextMenu[in] The menu about to be displayed
    */
   public void addStateEventsAndTransitionsButton(IMenuManager manager)
   {
      IElement pElem = getDrawEngineModelElement();
      if (pElem != null && pElem instanceof IState)
      {
         IState pState = (IState) pElem;
         IProcedure pEntry = pState.getEntry();
         IProcedure pExit = pState.getExit();
         IProcedure pDoActivity = pState.getDoActivity();
         
         IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_POPUP_STATE_EVENTS"), "");
         if (subMenu != null)
         {
            if (pEntry == null)
            {
               subMenu.add(createMenuAction(loadString("IDS_INSERT_ENTRY"), "MBK_INSERT_ENTRY"));
            } else
            {
               subMenu.add(createMenuAction(loadString("IDS_DELETE_ENTRY"), "MBK_DELETE_ENTRY"));
            }
            
            if (pExit == null)
            {
               subMenu.add(createMenuAction(loadString("IDS_INSERT_EXIT"), "MBK_INSERT_EXIT"));
            } else
            {
               subMenu.add(createMenuAction(loadString("IDS_DELETE_EXIT"), "MBK_DELETE_EXIT"));
            }
            
            if (pDoActivity == null)
            {
               subMenu.add(createMenuAction(loadString("IDS_INSERT_DOACTIVITY"), "MBK_INSERT_DOACTIVITY"));
            } else
            {
               subMenu.add(createMenuAction(loadString("IDS_DELETE_DOACTIVITY"), "MBK_DELETE_DOACTIVITY"));
            }
            
            subMenu.add(createMenuAction(loadString("IDS_INSERT_INCOMING_INTERNALTRANSITION"), "MBK_INSERT_INCOMING_INTERNALTRANSITION"));
            subMenu.add(createMenuAction(loadString("IDS_INSERT_OUTGOING_INTERNALTRANSITION"), "MBK_INSERT_OUTGOING_INTERNALTRANSITION"));
            subMenu.add(createMenuAction(loadString("IDS_DELETE_INTERNALTRANSITION"), "MBK_DELETE_INTERNALTRANSITION"));
         }
      }
   }
   
   /**
    * Returns the model element attached to the draw engine this compartment is associated with
    *
    * @param pElement [out,retval] The IElement the draw engine is attached to.
    */
   public IElement getDrawEngineModelElement()
   {
      return TypeConversions.getElement(m_engine);
   }
   
   /**
    * Adds the interaction operator choices for the combined fragment.
    *
    * @param pContextMenu[in] The menu about to be displayed
    */
   public void addInteractionOperatorButtons(IMenuManager manager)
   {
      IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_POPUP_INTERACTION_OPERATOR"), "");
      if (subMenu != null)
      {
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_ALT"), "MBK_CF_IO_ALT", BaseAction.AS_CHECK_BOX));
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_ASSERT"), "MBK_CF_IO_ASSERT", BaseAction.AS_CHECK_BOX));
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_ELSE"), "MBK_CF_IO_ELSE", BaseAction.AS_CHECK_BOX));
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_LOOP"), "MBK_CF_IO_LOOP", BaseAction.AS_CHECK_BOX));
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_NEG"), "MBK_CF_IO_NEG", BaseAction.AS_CHECK_BOX));
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_OPT"), "MBK_CF_IO_OPT", BaseAction.AS_CHECK_BOX));
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_PAR"), "MBK_CF_IO_PAR", BaseAction.AS_CHECK_BOX));
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_REGION"), "MBK_CF_IO_REGION", BaseAction.AS_CHECK_BOX));
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_SEQ"), "MBK_CF_IO_SEQ", BaseAction.AS_CHECK_BOX));
         subMenu.add(createMenuAction(loadString("IDS_CF_IO_STRICT"), "MBK_CF_IO_STRICT", BaseAction.AS_CHECK_BOX));
      }
   }
   
   public String loadString(String key)
   {
      try
      {
         return RESOURCE_BUNDLE.getString(key);
      } catch (MissingResourceException e)
      {
         return '!' + key + '!';
      }
   }
   
   public boolean containsPoint(Point p)
   {
      ETDeviceRect rect = getBoundingAsDeviceRect();
      return rect != null ? rect.contains(p) : false;
   }
   
   /**
    * Force Tom Sawyer to redraw the entire draw engine's node
    */
   public void refresh()
   {
      try
      {
         if( m_engine != null )
         {
            m_engine.invalidate() ;
            
            // Fix J1558:  The above invalidate() is not repainting the window when the user
            //             removes a destroy element from a lifeline.
            //             This fixes that problem, but was not needed in the C++ code.
            IDrawingAreaControl control = m_engine.getDrawingArea();
            if (control != null)
            {
               control.refresh(true);
            }
         }
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }
   
   /**
    * Forces the GUI to repaint by invalidating the node and pumping messages.
    */
   public void redrawNow()
   {
      if (m_engine != null)
      {
         m_engine.invalidate();
         IDrawingAreaControl drawingArea = m_engine.getDrawingArea();
         if (drawingArea != null)
         {
            // The C++ code calls refresh (which refreshs the entire diagram)
            // then pumps the messages to cause the events to occur.  I have 
            // not been able to find out how to pump the messages in Java
            // Therefore, I am going to refresh the draw engine now.
            ADGraphWindow window = drawingArea.getGraphWindow();
            if(window != null)
            {
               window.updateInvalidRegions(true);
            }
         }
         pumpMessages();
      }
   }
   
   /**
    * Message pump.
    */
   public void pumpMessages()
   {
      //to do implement
   }
   
   /**
    * Make the drawing dirty
    */
   public void setIsDirty()
   {
      if (m_engine != null)
      {
         m_engine.setIsDirty();
      }
   }
   
   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pMenuAction)
   {
      return false;
   }
   
   public boolean onHandleButton(ActionEvent e, String id)
   {
      return false;
   }
   
   protected boolean isParentDiagramReadOnly()
   {
      if (m_engine != null)
      {
         IDiagram pDiagram = m_engine.getDiagram();
         return pDiagram != null ? pDiagram.getReadOnly() : true;
      }
      return true;
   }
   
   public ContextMenuActionClass createMenuAction(String text, String menuID, int style)
   {
      ContextMenuActionClass menu = new ContextMenuActionClass(this, text, menuID);
      if (menu != null)
      {
         menu.setStyle(style);
      }
      
      return menu;
   }
   
   public ContextMenuActionClass createMenuAction(String text, String menuID, String shortcut)
   {
      ContextMenuActionClass menu = new ContextMenuActionClass(this, text, menuID);
      if (menu != null)
      {
         menu.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortcut));
      }
      
      return menu;
   }
   
   public ContextMenuActionClass createMenuAction(String text, String menuID)
   {
      return new ContextMenuActionClass(this, text, menuID);
   }
   
   // Resource user
   public ETList < IDrawingProperty > getDrawingProperties()
   {
      ETList < IDrawingProperty > pProperties = null;
      
      IDrawEngine pDrawEngine = getEngine();
      if (pDrawEngine != null)
      {
         String sDrawEngineID = pDrawEngine.getDrawEngineID();
         if (sDrawEngineID != null && sDrawEngineID.length() > 0)
         {
            pProperties = m_ResourceUser.getDrawingProperties(this, sDrawEngineID);
         }
      }
      
      return pProperties;
   }
   
   public void saveColor(String sDrawEngineType, String sResourceName, int nColor)
   {
      setIsDirty();
      m_ResourceUser.saveColor(sDrawEngineType, sResourceName, nColor);
   }
   
   public void saveColor2(IColorProperty pProperty)
   {
      setIsDirty();
      m_ResourceUser.saveColor2(pProperty);
   }
   
   public void saveFont(String sDrawEngineName, String sResourceName, String sFaceName, int nHeight, int nWeight, boolean bItalic, int nColor)
   {
      setIsDirty();
      m_ResourceUser.saveFont(sDrawEngineName, sResourceName, sFaceName, nHeight, nWeight, bItalic, nColor);
   }
   
   public void saveFont2(IFontProperty pProperty)
   {
      setIsDirty();
      m_ResourceUser.saveFont2(pProperty);
   }
   
   public void resetToDefaultResource(String sDrawEngineName, String sResourceName, String sResourceType)
   {
      setIsDirty();
      m_ResourceUser.resetToDefaultResource(sDrawEngineName, sResourceName, sResourceType);
      initResources();
      invalidateProvider();
   }
   
   public void resetToDefaultResources()
   {
      setIsDirty();
      m_ResourceUser.resetToDefaultResources();
   }
   
   public void resetToDefaultResources2(String sDrawEngineName)
   {
      // The draw engine should have handled this message and if we're inside of this
      // kind of draw engine then it calls ResetToDefaultResources.
   }
   
   public void dumpToFile(String sFile, boolean bDumpChildren, boolean bAppendToExistingFile)
   {
      m_ResourceUser.dumpToFile(sFile, bAppendToExistingFile);
   }
   
   public boolean displayFontDialog(IFontProperty pProperty)
   {
      return m_ResourceUser.displayFontDialog(pProperty);
   }
   
   public boolean displayColorDialog(IColorProperty pProperty)
   {
      return m_ResourceUser.displayColorDialog(pProperty);
   }
   
   public void invalidateProvider()
   {
      getEngine().invalidate();
   }
   
   // IResourceUserHelper
   public IDrawingAreaControl getDrawingArea()
   {
      IDrawEngine pDrawEngine = getEngine();
      return pDrawEngine != null ? pDrawEngine.getDrawingArea() : null;
   }
   
   public int getColorID(int nColorStringID)
   {
      int nID = -1;
      
      Integer iterator = m_ResourceUser.m_Colors.get(new Integer(nColorStringID));
      if (iterator != null)
      {
         // Our color has been cached from the last time
         int nTempID = iterator.intValue();
         
         // Make sure the id is valid, if not then re-get a good
         // id from our draw engine.
         if (nTempID == -1 || m_ResourceUser.getResourceMgr().isValidColorID(nTempID) == false)
         {
            m_ResourceUser.m_Colors.remove(new Integer(nColorStringID));
         } else
         {
            nID = nTempID;
         }
      }
      
      // Get the color from the draw engine if necessary
      if (nID == -1)
      {
         IDrawEngine pDrawEngine = getEngine();
         if (pDrawEngine != null)
         {
            // See if the draw engine has an override, go to the
            // diagram if necessary
            nID = ((IResourceUserHelper) pDrawEngine).getColorID(nColorStringID);
            
            if (nID != -1)
            {
               m_ResourceUser.m_Colors.put(new Integer(nColorStringID), new Integer(nID));
            }
         }
      }
      return nID;
   }
   
   public int getFontID(int nFontStringID)
   {
      int nID = -1;
      
      Integer iterator = m_ResourceUser.m_Fonts.get(new Integer(nFontStringID));
      if (iterator != null)
      {
         // Our font has been cached from the last time
         int nTempID = iterator.intValue();
         
         // Make sure the id is valid, if not then re-get a good
         // id from our draw engine.
         if (nTempID == -1 || m_ResourceUser.getResourceMgr().isValidFontID(nTempID) == false)
         {
            m_ResourceUser.m_Fonts.remove(new Integer(nFontStringID));
         } else
         {
            nID = nTempID;
         }
      }
      
      // Get the font from the draw engine if necessary
      if (nID == -1)
      {
         IDrawEngine pDrawEngine = getEngine();
         if (pDrawEngine != null)
         {
            // Else see if the draw engine has an override
            nID = ((IResourceUserHelper) pDrawEngine).getFontID(nFontStringID);
            
            if (nID != -1)
            {
               m_ResourceUser.m_Fonts.put(new Integer(nFontStringID), new Integer(nID));
            }
         }
      }
      return nID;
   }
   
   public boolean verifyDrawEngineStringID()
   {
      boolean bIDOK = true;
      if (m_ResourceUser.m_nDrawEngineStringID == -1)
      {
         IDrawEngine pEngine = getEngine();
         
         if (pEngine != null)
         {
            String sDrawEngineID = pEngine.getDrawEngineID();
            if (sDrawEngineID.length() > 0)
            {
               // Set the draw engine string id on the resource user
               m_ResourceUser.setDrawEngineStringID(sDrawEngineID);
               if (m_ResourceUser.m_nDrawEngineStringID == -1)
               {
                  bIDOK = false;
               }
            }
         }
      }
      
      return bIDOK;
   }
   
   public int setResourceID(String resourceName, Color color)
   {
      m_nNameFontStringID = m_ResourceUser.setResourceStringID(m_nNameFontStringID, resourceName, color.getRGB());
      return m_nNameFontStringID;
   }
   
   public Font getCompartmentFont(double zoomLevel)
   {
      return m_ResourceUser.getZoomedFontForStringID(m_nNameFontStringID, zoomLevel);
   }
   
   public Color getCompartmentFontColor()
   {
      return new Color(m_ResourceUser.getCOLORREFForStringID(m_nNameFontStringID));
   }
   
   public void setDefaultColor(String resourceName, int colorRef)
   {
      m_ResourceUser.setDefaultColor(resourceName, colorRef);
   }
   
   public void setDefaultColor(String resourceName, Color color)
   {
      m_ResourceUser.setDefaultColor(resourceName, color.getRGB());
   }
   
   public CompartmentResourceUser getCompartmentResourceUser()
   {
      return m_ResourceUser;
   }
   
   /**
    * Helper function to determine the mouse location
    * during the processing of a context menu.
    */
   protected TSConstPoint getLogicalMouseLocation(ActionEvent event)
   {
      try
      {
         Point pointDevice = ((ContextMenuActionClass) ((javax.swing.JMenuItem) event.getSource()).getAction()).getMenuManager().getLocation();
         return getTransform().pointToWorld(pointDevice);
      } 
      catch (Exception ex)
      {
      }
      
      return null;
   }
   
   protected void addSeparatorMenuItem(IMenuManager manager)
   {
       manager.add(new Separator());
   }
   
//   /**
//    * Sets the horizontal alignment property of the compartment.  Valid
//    * values are:
//    * <ul>
//    *    <li>IADEditableCompartment.LEFT</li>
//    *    <li>IADEditableCompartment.CENTER</li>
//    *    <li>IADEditableCompartment.RIGHT</li>
//    * </ul>
//    * @param alignment The horizonal alignment.
//    */
//   public void setHorizontalAlignment(int alignment)
//   {
//      long newStyle = m_style | ;
//   }
//   
//   /**
//    * Retrieves the horizontal alignment property of the compartment.  Valid
//    * values are:
//    * <ul>
//    *    <li>IADEditableCompartment.LEFT</li>
//    *    <li>IADEditableCompartment.CENTER</li>
//    *    <li>IADEditableCompartment.RIGHT</li>
//    * </ul>
//    * @return The horizonal alignment.
//    */
//   public int getHorizontalAlignment()
//   {
//      
//   }
//   
//   /**
//    * Sets the vertical alignment property of the compartment.  Valid
//    * values are:
//    * <ul>
//    *    <li>IADEditableCompartment.TOP</li>
//    *    <li>IADEditableCompartment.CENTER</li>
//    *    <li>IADEditableCompartment.BOTTOM</li>
//    * </ul>
//    * @param alignment The vertical alignment.
//    */
//   public void setVerticalAlignment(int alignment)
//   {
//      
//   }
//   
//   /**
//    * Retrieves the vertical alignment property of the compartment.  Valid
//    * values are:
//    * <ul>
//    *    <li>IADEditableCompartment.TOP</li>
//    *    <li>IADEditableCompartment.CENTER</li>
//    *    <li>IADEditableCompartment.BOTTOM</li>
//    * </ul>
//    * @return The vertical alignment.
//    */
//   public int getVerticalAlignment()
//   {
//      
//   }
   


    /////////////
    // Accessible
    /////////////


    AccessibleContext accessibleContext;
    
    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleETCompartment();
	}
	return accessibleContext;
    }
    
    
    public class AccessibleETCompartment extends AccessibleContext implements AccessibleComponent {
	

	////////////////////
	// AccessibleContext
	////////////////////

	public AccessibleStateSet getAccessibleStateSet() {
	    return new AccessibleStateSet(new AccessibleState[] {
		AccessibleState.SHOWING,
		AccessibleState.VISIBLE,
		AccessibleState.ENABLED, 		
		AccessibleState.FOCUSABLE	
	    });	    
	}


	public String getAccessibleName(){
	    return getName();
	}

	public String getAccessibleDescription(){
	    return getAccessibleName();
	}

	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.UNKNOWN;
	}
	
	public int getAccessibleChildrenCount() {
	    return 0; 
	}

	
	public Accessible getAccessibleChild(int i) {	
	    return null;
	}

	public int getAccessibleIndexInParent() {
	    return 0;
	}

	public Locale getLocale() {
	    return getGraphWindow().getLocale();
	}

	public AccessibleComponent getAccessibleComponent() {
	    return this;
	}
	

	////////////////////////////////
	// interface AccessibleComponent
	////////////////////////////////

	public java.awt.Color getBackground() {
	    return null;
	}

	public void setBackground(java.awt.Color color) {
	    ;
	}

	public java.awt.Color getForeground() {
	    return null;
	}

	public void setForeground(java.awt.Color color) {
	    ;
	}
	public java.awt.Cursor getCursor() {
	    return null;
	}
	
	public void setCursor(java.awt.Cursor cursor) {
	    ;
	}
	public java.awt.Font getFont() {
	    return null;
	}
	public void setFont(java.awt.Font font) {
	    ;
	}
	public java.awt.FontMetrics getFontMetrics(java.awt.Font font) {
	    return null;
	}
	public boolean isEnabled() {
	    return true;
	}

	public void setEnabled(boolean enabled) {

	}
	public boolean isVisible() {
	    return true;
	}
	public void setVisible(boolean visible) {
	    ;
	}

	public boolean isShowing() {
	    return true;
	}
	
	public boolean contains(java.awt.Point point) {
            Rectangle r = getBounds();
            return r.contains(point);
	}
	
	public java.awt.Point getLocationOnScreen() {
	    IETRect scRect = getWinScreenRect();
	    if (scRect != null) {
		return new java.awt.Point(scRect.getIntX(), scRect.getIntY());
	    }
	    return null;
	}
	
	// wrt parent
	public java.awt.Point getLocation() {	    
	    AccessibleComponent parentComponent 
		= accessibleParent.getAccessibleContext().getAccessibleComponent();
	    if (parentComponent != null) {
		java.awt.Point parentLocation = parentComponent.getLocationOnScreen();		
		java.awt.Point componentLocation = getLocationOnScreen();
		if (parentLocation != null && componentLocation != null) { 
		    return new java.awt.Point(parentLocation.x - componentLocation.x,
					      parentLocation.y - componentLocation.y);
		}
	    }
	    return null;
	}
	
	// wrt parent
	public void setLocation(java.awt.Point point) {
	    ;
	}
	
	// wrt parent
	public java.awt.Rectangle getBounds() {
	    IETRect clientRect = getWinClientRect();
	    java.awt.Point loc = getLocation();
	    if (clientRect != null && loc != null) {
		return new Rectangle(loc.x, loc.y, clientRect.getIntWidth(),clientRect.getIntHeight());
	    }
	    return null;
	}
	
	public void setBounds(java.awt.Rectangle bounds) {
	    //setWinClientRectangle(new ETRect());
	}

	public java.awt.Dimension getSize() {
            Rectangle r = getBounds();
            return new Dimension(r.width, r.height);
	}
	
	public void setSize(java.awt.Dimension dim) {
	    setScaledSize(new ETSize(dim.width, dim.height));
	}
	
	public javax.accessibility.Accessible getAccessibleAt(java.awt.Point point) {
	    return null;
	}

	public boolean isFocusTraversable() {
	    return true;
	}
	public void requestFocus() {
	    ;
	}
	public void addFocusListener(java.awt.event.FocusListener listener) {
	    ;
	}
	public void removeFocusListener(java.awt.event.FocusListener listener) {
	    ;
	}
	

    }

  
}
