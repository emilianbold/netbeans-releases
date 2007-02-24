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


package org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram;

import com.tomsawyer.drawing.geometry.TSConstPoint;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.ITopographyChangeAction;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ConvertRectToPercent;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADEditableCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IGateCompartment;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ContainmentTypeEnum;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETContainerDrawEngine;
import org.netbeans.modules.uml.ui.support.DiagramAndPresentationNavigator;
import org.netbeans.modules.uml.ui.support.IDiagramAndPresentationNavigator;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSPolygonShape;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/**
 * @author brettb
 *
 */
public class InteractionFragmentDrawEngine extends ETContainerDrawEngine implements IInteractionFragmentDrawEngine
{
   private final static long MIN_NODE_WIDTH  = 5;
   private final static long MIN_NODE_HEIGHT = 5;

   private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.Bundle"; //$NON-NLS-1$
   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

   /**
    * Constructor
    */
   public InteractionFragmentDrawEngine()
   {
      m_rectLabel = new ETDeviceRect();
      m_rectText = new ETDeviceRect();
      m_rectName = new ETDeviceRect();
      m_nameLocation = _NameLocation.NL_OUTSIDE_LABEL;
      m_bIsHollow = false;
      m_nFontStringID = -1;

      // We are a container controlling the child's location
      setContainmentType( ContainmentTypeEnum.CT_GRAPHICAL );

      m_maintainContainment = true;
   }


   // IDrawEngine
   
   /**
    * This is the name of the drawengine used when storing and reading from the product archive.
    *
    * @return A unique identifier for this draw engine.  Used when persisting to the etlp file.
    */
   public String getDrawEngineID()
   {
      return "InteractionFragmentDrawEngine";
   }
   
   /**
    * Notifies the node an event has been generated at the graph.
    */
   public void onGraphEvent( int /* IGraphEventKind */ nKind )
   {
      super.onGraphEvent( nKind );

      switch (nKind)
      {
         case IGraphEventKind.GEK_POST_MOVE :
            {
               IConnectorsCompartment connectorsCompartment = getCompartmentByKind( IConnectorsCompartment.class );
               if (connectorsCompartment != null)
               {
                  connectorsCompartment.updateConnectors( null );
               }
            }
            break;

         case IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED :
            // Fix 2895:  Make sure any interactions are not deleted,
            // by deselecting the presentation element.

            // UPDATE:  The use may want to clear the entire diagram,
            // so we should process this via affectModelElementDeletion()?

            if (getMetaTypeOfElement().equals("Interaction"))
            {
               TSENode tseNode = getOwnerNode();
               if( tseNode != null )
               {
                  IDrawingAreaControl control = getDrawingArea();
                  if (control != null)
                  {
                     ADGraphWindow window = control.getGraphWindow();
                     if (window != null)
                     {
                        window.deselectObject( tseNode, true );
                     }
                  }
               }
            }
            break;

         default :
            // do nothing
            break;
      }
   }
   
   /**
    * Tells the draw engine to write its data to the IProductArchive.
    *
    * @param pProductArchive[in] The archive we're saving to
    * @param pParentElement[in] The current element, or parent for any new attributes or elements.
    */
   public long writeToArchive( IProductArchive productArchive, IProductArchiveElement parentElement)
   {
      if( null == productArchive )  throw new IllegalArgumentException();
      if( null == parentElement )   throw new IllegalArgumentException();

      super.writeToArchive( productArchive , parentElement );

      IProductArchiveElement engineElement = parentElement.getElement(
              IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
      if( engineElement != null )
      {
         String strLabel = m_strLabelText;
          engineElement.addAttributeString(
            "INTERACTIONFRAGMENTENGINE_LABEL_STRING", m_strLabelText );
          engineElement.addAttributeLong(
            "INTERACTIONFRAGMENTENGINE_NAMELOCATION_STRING", m_nameLocation );
          engineElement.addAttributeBool(
            "INTERACTIONFRAGMENTENGINE_ISHOLLOW_STRING", m_bIsHollow );
      }
      
      return 0;
   }
   
   /**
    * Tells the draw engine to read its data to the IProductArchive.
    *
    * @param pProductArchive[in] The archive we're reading from
    * @param pEngineElement[in] The element where this draw engine's information should exist.
    */
   public long readFromArchive( IProductArchive productArchive, IProductArchiveElement parentElement)
   {
      if( null == productArchive ) throw new IllegalArgumentException();
      if( null == parentElement )  throw new IllegalArgumentException();

      super.readFromArchive( productArchive , parentElement );

      String strLabel = parentElement.getAttributeString( "INTERACTIONFRAGMENTENGINE_LABEL_STRING" );
      m_nameLocation = (int)parentElement.getAttributeLong( "INTERACTIONFRAGMENTENGINE_NAMELOCATION_STRING" );
      m_bIsHollow = parentElement.getAttributeBool( "INTERACTIONFRAGMENTENGINE_ISHOLLOW_STRING" );
   
      if( strLabel.length() > 0 )
      {
         setLabelText( strLabel );
      }
         
      return 0;
   }
   
   public boolean handleLeftMouseButtonDoubleClick( MouseEvent mouseEvent )
   {
      // Allow the base class to try and handle the double-click,
      // which passes it to all the compartments, e.g. allow the name compartment to be edited.
      boolean bHandled = super.handleLeftMouseButtonDoubleClick( mouseEvent );

      // Get the behavior off the interactionoccurrence
      IBehavior behavior = null;
      {
         // When the model element is an interaction occurrence,
         // the double-click will open the interaction occurrence's interaction's diagram
         // or the activity diagram.

         IElement element = getFirstModelElement();
         if (element instanceof IInteractionOccurrence)
         {
            IInteractionOccurrence occurrence = (IInteractionOccurrence)element;
            behavior = occurrence.getBehavior();
         }
      }
      
      // If not handled then bring up the diagram navigation dialog
      if (  (bHandled == false) &&
            (behavior != null) )
      {
         IDiagramAndPresentationNavigator navigator = new DiagramAndPresentationNavigator();
         if (navigator != null)
         {
            bHandled = navigator.handleNavigation( 0, behavior, false /*?*/);
         }
      }
      
      return bHandled;
   }
   
   public void initResources()
   {
      m_nFontStringID = m_ResourceUser.setResourceStringID( m_nFontStringID, "interactionfragmentfont" );
      setBorderColor("interactionfragmentborder", Color.BLACK );
      setFillColor("graphicfill", Color.WHITE );
      
      super.initResources();
   }
   
   // TODO public void onContextMenu( IProductContextMenu contextMenu, long logicalX, long logicalY );
   
   /**
    * Initializes our compartments.
    */
   public void initCompartments( IPresentationElement pe )
   {
      if( null == pe )  throw new IllegalArgumentException();
      
      // We may get here with no compartments.  This happens if we've been created
      // by the user.  If we read from a file then the compartments have been pre-created and
      // we just need to initialize them.
      long numCompartments = getNumCompartments();
      if ( numCompartments < 1 )
      {
         createCompartments();
         numCompartments = getNumCompartments();
      }

      IElement modelElement = pe.getFirstSubject();
      if ( modelElement != null )
      {
         for( int nIndx=0; nIndx < numCompartments; nIndx++ )
         {
            ICompartment compartment = getCompartment( nIndx );
            if ( compartment != null )
            {
               compartment.addModelElement( modelElement, -1 );
            }
         }

         // Make the name compartment the default compartment, 
         // so autotype will select the name compartment
         IADNameCompartment nameCompartment = getCompartmentByKind( IADNameCompartment.class );
         if ( nameCompartment != null )
         {
            setDefaultCompartment( nameCompartment );
         }

         // Determine the draw engine settings based on the model element
         String strLabelID = "IDS_IF_REFERENCE_LABEL";
         if( modelElement instanceof IInteractionOccurrence )
         {
            strLabelID = "IDS_IF_REFERENCE_LABEL";
            m_nameLocation = _NameLocation.NL_OUTSIDE_LABEL;
            m_bIsHollow = false;
            m_isGraphicalContainer = false;
            m_maintainContainment = false;
         }
         else
         {
            strLabelID = "IDS_IF_DIAGRAM_LABEL";
            m_nameLocation = _NameLocation.NL_INSIDE_LABEL;
            m_bIsHollow = true;  // Fix W9665:  need a hollow interaction boundary
            m_isGraphicalContainer = true;
            m_maintainContainment = true;
         }

         final String strLabel = RESOURCE_BUNDLE.getString( strLabelID );
         setLabelText( strLabel );
      }
   }
   
   /**
    * Create the compartments for this node..
    */
   public void createCompartments()
   {
      clearCompartments();

      createAndAddCompartment( "GateCompartment" );
      IADEditableCompartment nameCompartment = (IADEditableCompartment)createAndAddCompartment( "ADNameCompartment" );
      if ( nameCompartment != null )
      {
         // Fix J1461:  We need to ensure that the text is located on the left side of this compartment
         nameCompartment.setHorizontalAlignment( IADCompartment.LEFT );
      }
   }
   
   /**
    * Calculates the "best" size for this compartment.  The calculation sets the member variable m_szCachedOptimumSize,
    * which represents the "best" size of the compartment at 100%.
    */
   public IETSize calculateOptimumSize( IDrawInfo pDrawInfo, boolean bAt100Pct )
   {
      ETSize sizeReturn = new ETSize( 0, 0 );
      
      ETList< ICompartment > compartments = getCompartments();
      if( compartments != null )
      {
         final long lCnt = compartments.getCount();

         updatePreferedLabelSize( pDrawInfo, bAt100Pct );

         if( lCnt <= 2 )
         {
            sizeReturn.setWidth( Math.max( m_rectLabel.getRight(), m_rectName.getRight() ));
            sizeReturn.setHeight( Math.max( m_rectLabel.getBottom(), m_rectName.getBottom() ));
         }
         else
         {
            // start at 1 to skip the label compartment
            for( int nIndx=1; nIndx<lCnt; nIndx++ )
            {
               ICompartment compartment = compartments.get( nIndx );
               if( compartment != null )
               {
                  compartment.calculateOptimumSize( pDrawInfo, bAt100Pct );
                  IETSize sizeCompartment = compartment.getOptimumSize( bAt100Pct );

                  // Keep the widest dimension, and sum the heights
                  if ( sizeReturn.getWidth() < sizeCompartment.getWidth() )
                  {
                     sizeReturn.setWidth( sizeCompartment.getWidth() );
                  }
                  sizeReturn.setHeight( sizeReturn.getHeight() + sizeCompartment.getHeight() );
               }
            }
         }

         // CLEAN setCalculatedOptimumSize( sizeReturn );
      }
      
      return sizeReturn;
   }
   
   /**
    * Returns the optimum size for an item.  This is used when an item is created from the toolbar.
    */
   public void sizeToContents()
   {
      // Size but keep the current size if possible
      sizeToContentsWithMin( MIN_NODE_WIDTH,
                             MIN_NODE_HEIGHT,
                             false,
                             true );
   }
   
   /**
    * Draws each of the individual compartments.
    *
    * @param drawInfo Information about the draw event (ie the DC, are we printing...)
    */
   public void doDraw( IDrawInfo drawInfo )
   {
      if( null == drawInfo )  throw new IllegalArgumentException();
      
      // Retain the IETRect for use by the compartment draw calls
      // Get the bounding rectangle of the node.
      IETRect rectBounding = drawInfo.getDeviceBounds();
      TSEGraphics graphics = drawInfo.getTSEGraphics();

      int numCompartments = getNumCompartments();

      if( !m_bIsHollow )
      {
         GDISupport.fillRectangle( graphics.getGraphics(), rectBounding, getBkColor() );
      }

      IGateCompartment gateCompartment = getCompartmentByKind( IGateCompartment.class );
      if ( gateCompartment != null )
      {
          gateCompartment.draw( drawInfo, rectBounding );
      }

      drawLabel( drawInfo, rectBounding );

      // Draw the name after the label, so the it is located properly
      IADNameCompartment nameCompartment = getCompartmentByKind( IADNameCompartment.class );
      if ( nameCompartment != null )
      {
         // calculate the rectangle for the name compartment
         IETRect rectName = (IETRect)m_rectName.clone();
         rectName.offsetRect( rectBounding.getTopLeft() );  // necessary because java output is based in the client
         if( _NameLocation.NL_OUTSIDE_LABEL == m_nameLocation )
         {
            rectName.setRight( rectBounding.getRight() );
            rectName.setBottom( rectBounding.getBottom() );
         }

         nameCompartment.draw( drawInfo, rectName );
      }

      // This call must come after the drawing of the compartments, because
      // the shape is determined within the draw of the compartments.
      if( m_bIsHollow && (isSettingShape() == false))
      {          
         setDrawEngineShape();
      }

      GDISupport.frameRectangle( graphics.getGraphics(),
                                 rectBounding,
                                 DrawEngineLineKindEnum.DELK_SOLID, 
                                 1,
                                 getBorderColor() );

		// Give the container a chance to draw
//		super.doDraw(drawInfo);
   }
   
   public boolean isDrawEngineValidForModelElement()
   {
      boolean bIsValid = false;

      String currentMetaType = getMetaTypeOfElement();
      if ( (currentMetaType.equals("Interaction")) ||
           (currentMetaType.equals("InteractionOccurrence")) )
      {
         bIsValid = true;
      }
      
      return bIsValid;
   }


   protected static final class _NameLocation
   {
      public static final int NL_NONE = 0;
      public static final int NL_OUTSIDE_LABEL = 1;
      public static final int NL_INSIDE_LABEL = 2;
   };

   /// Sets the location of the name
   protected void setNameLocation( int /*_NameLocation*/ nameLocation )
   {
      m_nameLocation = nameLocation;
   }

   /// Get the InteractionFragment from the parent product element
   protected IInteractionFragment getInteractionFragment()
   {
      IInteractionFragment interactionFragment = null;

      IElement element = getFirstModelElement();
      if (element instanceof IInteractionFragment)
      {
         interactionFragment = (IInteractionFragment)element;
      }
      
      return interactionFragment;
   }

   /// Determine the prefered size for the label in the upper left corner
   protected final void updatePreferedLabelSize( IDrawInfo drawInfo, boolean bAt100Pct )
   {
      if( drawInfo != null )
      {
         // Determine the size of the label we are about to draw
         final String strLabel = m_strLabelText;
         final String strReferenceLabel = strLabel;
         final String strExtentTest = strReferenceLabel + "  ";
         
         TSEGraphics graphics = drawInfo.getTSEGraphics();
         TSTransform transform = graphics.getTSTransform();
         Font fontCorner = m_ResourceUser.getZoomedFontForStringID( m_nFontStringID, drawInfo.getFontScaleFactor() );
         
         final IETSize sizeLabelExtent = GDISupport.getTextExtent( graphics.getGraphics(), fontCorner,
                                                                   strExtentTest );

         // Calculate the rectangle used to display the label's text
         m_rectText = new ETDeviceRect( new Point( 0, 0 ), sizeLabelExtent.asDimension() );

         // Calculate the rectangle used to draw the outline of the label
         m_rectLabel = (IETRect)m_rectText.clone();

         // Size the member rectangles based on the the location of the label
         switch( m_nameLocation )
         {
         case _NameLocation.NL_INSIDE_LABEL:
            {
               // Add the size of the name compartment to the label size
               IETSize sizeName = getNameCompartmentMinimumSize( drawInfo, bAt100Pct );
               m_rectLabel.setRight( m_rectLabel.getRight() + sizeName.getWidth() );
               
               // Fix J1461:  For some reason we need to adjust this text in a different way from C++
               final int dy = -2;
               
               // Place the name label inside the label to the right of the label's text
               m_rectName = new ETDeviceRect( m_rectText.getRight(), dy, 
                                              m_rectText.getRight() + sizeName.getWidth(),
                                              m_rectLabel.getBottom() + dy );

               m_rectLabel.setRight( (int)Math.round(m_rectLabel.getRight() + m_rectLabel.getHeight()/2) );
            }
            break;

         case _NameLocation.NL_OUTSIDE_LABEL:
            {
               m_rectLabel.setRight( (int)Math.round(m_rectLabel.getRight() + m_rectLabel.getHeight()/2) );

               // Place the name compartment to the right of the label
               IETSize sizeName = getNameCompartmentMinimumSize( drawInfo, bAt100Pct );

               m_rectName = new ETDeviceRect( m_rectText.getRight(), 0, 
                                              m_rectText.getRight() + sizeName.getWidth(),
                                              m_rectLabel.getBottom() );
            }
            break;

         default:
            m_rectLabel.setRight( (int)Math.round(m_rectLabel.getRight() + m_rectLabel.getHeight()/2) );
            m_rectName.setRectEmpty();
            break;
         }
      }
   }

   /// Draws the reference label using the text from getLabelText()
   protected void drawLabel( IDrawInfo drawInfo, final IETRect rectBounding )
   {
      // In C++ this passed in rectangle would be 0,0 relative to the upper left of the node
      // However, in java we have to use device coordinates with 0,0 being the upper left of the
      // client window.

      assert ( m_strLabelText.length() > 0 );  // there should always be a label

      updatePreferedLabelSize( drawInfo, false );

      assert ( !m_rectLabel.isZero() );
      assert ( !m_rectText.isZero() );

      if( drawInfo != null )
      {
         TSEGraphics graphics = drawInfo.getTSEGraphics();
         TSTransform transform = graphics.getTSTransform();
         Font fontCorner = m_ResourceUser.getZoomedFontForStringID( m_nFontStringID, drawInfo.getFontScaleFactor() );
         
         // Determine the label's width height
         final int iLabelWidth = Math.min( rectBounding.getIntWidth(), m_rectLabel.getIntWidth() );
         final int iLabelHeight = Math.min( rectBounding.getIntHeight(), m_rectLabel.getIntHeight() );
         
         final int dx = rectBounding.getLeft();
         final int dy = rectBounding.getTop();

         int iLabelTop    = m_rectLabel.getTop() + dy;
         int iLabelBottom = m_rectLabel.getBottom() + dy;

         // Prepare the points for the label
         m_aptLabel.clear();
         m_aptLabel.add( new ETPoint( dx, iLabelTop ));
         m_aptLabel.add( new ETPoint( dx, iLabelBottom ));
         m_aptLabel.add( new ETPoint( dx + Math.max( 0, iLabelWidth - iLabelHeight/2 ), iLabelBottom ));
         m_aptLabel.add( new ETPoint( dx + iLabelWidth, iLabelTop + iLabelHeight/2 ));
         m_aptLabel.add( new ETPoint( dx + iLabelWidth, iLabelTop ));
         
         GDISupport.drawPolygon( graphics.getGraphics(), m_aptLabel, getBorderColor(), 1, getFillColor() );
      
         IETRect rectText = (IETRect)m_rectText.clone();
         rectText.offsetRect( dx, dy );
         
         GDISupport.drawText( graphics.getGraphics(), fontCorner, m_strLabelText, rectText );
      }
   }

   /// Sets the text that is displayed inside the label in the upper left corner
   protected void setLabelText( final String strLabelText )
   {
      m_strLabelText = strLabelText;

      // Clear the label size, so it will be recalculated in UpdatePreferedLabelSize()
      m_rectLabel.setRectEmpty();
   }

   /// Determines the minimum size of the name compartment
   protected IETSize getNameCompartmentMinimumSize( IDrawInfo drawInfo, boolean bAt100Pct )
   {
      IETSize sizeName = null;

      // Add the name compartment's size to the right of the label
      IADNameCompartment nameCompartment = getCompartmentByKind( IADNameCompartment.class );
      if ( nameCompartment != null )
      {
         sizeName = nameCompartment.calculateOptimumSize( drawInfo, bAt100Pct );
      }
      
      return sizeName;
   }

   private boolean m_SettingShape = false;
   protected synchronized boolean isSettingShape()
   {
      return m_SettingShape;
   }
   
   /// Tell Tom Sawyer the active shape for node selection
   protected synchronized void setDrawEngineShape()
   {      
      m_SettingShape = true;
      TSENode tseNode = getOwnerNode();
      if (tseNode != null) {
          // Here's our list of shapes that we're sending to tomsawyer
          TSPolygonShape shape = new TSPolygonShape();
          
          List ptList = new Vector();
          ptList.add(new TSConstPoint(0, 100));
          ptList.add(new TSConstPoint(100, 100));
          ptList.add(new TSConstPoint(100, 0));
          ptList.add(new TSConstPoint(0, 0));
          ptList.add(new TSConstPoint(0, 95));
          ptList.add(new TSConstPoint(4, 95));
          ptList.add(new TSConstPoint(4, 4));
          ptList.add(new TSConstPoint(96, 4));
          ptList.add(new TSConstPoint(96, 97));
          ptList.add(new TSConstPoint(0, 97));
          ptList.add(new TSConstPoint(0, 100));
          
          IETRect rectInner = getWinScaledOwnerRect();
          
          // Now convert the points to a -1000 to 1000 box.
          // 0,0 . -1000,1000
          // totalHeight . 1000
          // totalWidth . 1000
          // We also need to flip the y axis
          
          // Set up the coordinate conversion parameters
          ConvertRectToPercent converter = new ConvertRectToPercent( this );
          
          // make sure the label area is solid
         getLabelShape( converter, shape );

         IADNameCompartment nameCompartment = getCompartmentByKind(IADNameCompartment.class);

         // Loop through all the compartments
         final int iCompartmentCnt = getNumCompartments();
         for (int iCompartmentIndx = 0; iCompartmentIndx < iCompartmentCnt; iCompartmentIndx++)
         {
            ICompartment compartment = getCompartment( iCompartmentIndx );
            if ( (compartment != null) &&
                 (compartment != nameCompartment)) // Don't process the name compartment
            {
               // Get the shape of the compartment
               // The compartment should assume that the point before its list of points
               // will be in the upper left corner of its bounding rect
               ETList< IETPoint > pointList = compartment.getCompartmentShape();

               // Process the list of points
               if ( pointList != null )
               {
                  int iPointsCnt = pointList.getCount();

                  if (iPointsCnt > 0)
                  {
                     // Convert each point from its bounding rect coordinates to percent
                     if ( pointList != null )
                     {
                        for (Iterator iterPoints = pointList.iterator(); iterPoints.hasNext();)
                        {
                           IETPoint ptCompartment = (IETPoint)iterPoints.next();
                     
                           Point point = converter.ConvertToPercent( ptCompartment.asPoint() );
                           //shape.addPoint( point.x, point.y ); //jyothi
                        }
                     }
                  }
               }
            }
         }

         // Give TS our new shape list
         TSPolygonShape shape1 = new TSPolygonShape(ptList);
         tseNode.setShape( shape1 );
         m_SettingShape = false;
      }
   }

   /// Returns the shape of the label
   protected void getLabelShape( final ConvertRectToPercent converter, TSPolygonShape shape )
   {
      // Note the paths must be in clockwise order, to indicate a solid area

      // Loop around the name tag part of the compartment
      appendPoint( shape, converter.ConvertToPercent( m_aptLabel.get(0).asPoint() ));
      appendPoint( shape, converter.ConvertToPercent( m_aptLabel.get(1).asPoint() ));
      appendPoint( shape, converter.ConvertToPercent( m_aptLabel.get(2).asPoint() ));
      appendPoint( shape, converter.ConvertToPercent( m_aptLabel.get(3).asPoint() ));
      appendPoint( shape, converter.ConvertToPercent( m_aptLabel.get(4).asPoint() ));
      appendPoint( shape, converter.ConvertToPercent( m_aptLabel.get(0).asPoint() ));
   }

   /**
    * Used to clean up the code for appending a point to a list of points.
    *
    * @param pPointList[in,out] The list of points to which the point is being added
    * @param ptPoint[in] The point to add to the list
    */
   protected void appendPoint( TSPolygonShape shape, final Point point )
   {
      // jyothi shape.addPoint( point.getX(), point.getY() );
   }


   private String   m_strLabelText = "";
   private ETList< IETPoint > m_aptLabel = new ETArrayList< IETPoint >();   // points used to define the label's shape
   private IETRect  m_rectLabel;   /// The rectangle where the label is located
   private IETRect  m_rectText;    /// The rectangle where the label's text is located
   private IETRect  m_rectName;    /// The rectanble where the name is located, if displayed

   private int /*_NameLocation*/ m_nameLocation;

   // When true, the user will be able to click on objects "under" the interaction fragment
   private boolean m_bIsHollow;

   private int /*STRINGID*/ m_nFontStringID = -1;
}


