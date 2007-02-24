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



package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine;
import org.netbeans.modules.uml.ui.support.contextmenusupport.ProductButtonHandler;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/**
 * @author sumitabhk
 *
 */
public class ETCornerLabelCompartment extends ETCompartment implements ICornerLabelCompartment
{
	private int m_nNameTagColorStringID = -1;
	private int m_nCornerFillColorStringID = -1;
	
   private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.modules.uml.ui.products.ad.compartments.Bundle");

   private static final String[] strIOs =
   {
      "IDS_CF_IO_ALT",
      "IDS_CF_IO_ELSE",
      "IDS_CF_IO_OPT",
      "IDS_CF_IO_PAR",
      "IDS_CF_IO_LOOP",
      "IDS_CF_IO_REGION",
      "IDS_CF_IO_NEG",
      "IDS_CF_IO_ASSERT",
      "IDS_CF_IO_SEQ",
      "IDS_CF_IO_STRICT"
   };

	/**
	 * 
	 */
	public ETCornerLabelCompartment()
	{
		super();
	}

	public void initResources()
	{
		setResourceID("cornerlabeltext", Color.BLACK);
		m_nCornerFillColorStringID = m_ResourceUser.setResourceStringID(m_nCornerFillColorStringID, "cornerlabelfill", ((new Color(255,255,255)).getRGB()));
		m_nNameTagColorStringID = m_ResourceUser.setResourceStringID(m_nNameTagColorStringID, "nametagcolor", 0);
		
		// Now call the base class so it can setup any string ids we haven't already set
		super.initResources();
	}

   // ICompartment method overrides
   
	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "CornerLabelCompartment";
	}

   public void draw(IDrawInfo drawInfo, IETRect rectIn )
   {
		Color cornerFillColor = new Color(m_ResourceUser.getCOLORREFForStringID(m_nCornerFillColorStringID));
		Color borderColor = new Color(m_ResourceUser.getCOLORREFForStringID(m_nNameTagColorStringID));
      
      TSEGraphics graphics = drawInfo.getTSEGraphics();
      TSTransform transform = graphics.getTSTransform();

      // if the scale used is 100% we do not need to reset the font
      Font originalFont = graphics.getFont();

      // Compartment specific font
      Font compartmentFont = getCompartmentFont(drawInfo.getFontScaleFactor());
      graphics.setFont( compartmentFont );
		
      // In C++ this passed in rectangle would be 0,0 relative to the upper left of the node
      // However, in java we have to use device coordinates with 0,0 being the upper left of the
      // client window.

      // Make sure we don't modify the input rectangle, and that we are using a device rectangle
      ETDeviceRect rectBounding = ETDeviceRect.ensureDeviceRect( (IETRect)rectIn.clone() );
      
      // Determine the size of the label we are about to draw
      final String strInteractionOperator = getInteractionOperatorString();
      final String strExtentTest = strInteractionOperator;

      IETSize sizeLabelExtent = getTextExtent( drawInfo, strInteractionOperator );

      // Calculate the rectangle used to display the label's text
      final Dimension dimension = new Dimension( sizeLabelExtent.getWidth(), sizeLabelExtent.getHeight() ); 
      IETRect rectLabelText = new ETDeviceRect( rectBounding.getTopLeft(), dimension );

      // Calculate the rectangle used to draw the outline of the label
      IETRect rectLabel = rectLabelText;
      rectLabel.setRight( (int)(rectLabel.getRight() + rectLabel.getHeight()/2) );

      // Determine the name tag's width height
      final int iNameWidth = (int)Math.min( rectBounding.getWidth(), rectLabel.getWidth() );
      final int iNameHeight = (int)Math.min( rectBounding.getHeight(), rectLabel.getHeight() );

      rectBounding.setRight( rectBounding.getLeft() + iNameWidth );
      rectBounding.setBottom( rectBounding.getTop() + iNameHeight );

      // Call the base class to ensure the size of this compartment is remembered
      super.draw( drawInfo, rectBounding );

      // IETRect returns values for the y axis going up
      int iNameTop    = rectLabel.getTop();
      int iNameBottom = (int)(rectLabel.getTop() + rectLabel.getHeight());     //rectLabel.getBottom();

      // Prepare the points for the name tag
      final int dx = rectBounding.getLeft();
      m_aptLabel.clear();
      m_aptLabel.add( new ETPoint( dx, iNameTop ) );
      m_aptLabel.add( new ETPoint( dx, iNameBottom ) );
      m_aptLabel.add( new ETPoint( dx + Math.max( 0, iNameWidth - iNameHeight/2 ), iNameBottom ));
      m_aptLabel.add( new ETPoint( dx + iNameWidth, iNameTop + iNameHeight/2 ));
      m_aptLabel.add( new ETPoint( dx + iNameWidth, iNameTop ));
      m_aptLabel.add( m_aptLabel.item( 0 ));

/* TODO get the color stuff working
      COLORREF crBorder = getCOLORREFForStringID( m_nNameTagColorStringID);

      CBrush* pBrush = getBrush( m_nCornerFillColorStringID );
      GDISupport.DrawPolygon( drawInfo, m_aptLabel, 6, crBorder,0,pBrush );
      delete pBrush;

      // Draw the interaction operator text
*/
      //GDISupport.drawPolygon( drawInfo.getTSEGraphics().getGraphics(), m_aptLabel, Color.BLACK, 1, Color.WHITE );
		GDISupport.drawPolygon( drawInfo.getTSEGraphics().getGraphics(), m_aptLabel, borderColor, 1, cornerFillColor );
      GDISupport.drawText( drawInfo.getTSEGraphics().getGraphics(), strInteractionOperator, rectLabelText );
      
      graphics.setFont( originalFont );
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
         super.onContextMenu(manager);
         
         Point point = manager.getLocation();
         
         // (LLS) Adding the buildContext logic to support A11Y issues.  The
         // user should be able to use the CTRL-F10 keystroke to activate
         // the context menu.  In the case of the keystroke the location
         // will not be valid.  Therefore, we have to just check if the
         // compartment is selected.
         boolean buildContext = false;
         if(point != null)
         {
             buildContext = containsPoint(point);
         }
         else
         {
             buildContext = isSelected();
         }
         
         if(buildContext == true)
         {
            addInteractionOperatorButtons(manager);
         }
      }
   }
   /**
    * Sets the sensitivity and check state of the buttons created and owned by this implementor.  By default the
    * buttons are created so they are not checked.
    *
    * @param id The string id of the button whose sensitivity we are checking
    * @param pClass The button class
    * 
    * @return True have the button be enabled.
    */
   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean bFlag = isParentDiagramReadOnly() ? false : true;

      final int interactionOperatorToTest = getInteractionOperator( id );
      if( interactionOperatorToTest != -1 )
      {
         // Determine the interaction operator from the combined fragment model element
         ICombinedFragment combinedFragment = getCombinedFragment();
         if( combinedFragment != null )
         {
            final int interactionOperatorActual  = combinedFragment.getOperator();
            if( interactionOperatorToTest == interactionOperatorActual )
            {
               pClass.setChecked(true);
            }
         }
      }
      
      return bFlag;
   }

   public boolean onHandleButton(ActionEvent event, String id)
   {
      final int interactionOperator = getInteractionOperator( id );
      if ( interactionOperator != -1 )
      {
         if( m_engine instanceof ICombinedFragmentDrawEngine )
         {
            ICombinedFragmentDrawEngine engine = (ICombinedFragmentDrawEngine)m_engine;
            engine.setOperator( interactionOperator );
         }
      }
      else
      {
         return super.onHandleButton(event, id);
      }
      
      return false;
   }
   
   
   // protected methods

   /**
    * Determine the interaction operator associated with the button.
    */
   protected int getInteractionOperator( String id )
   {
      // We should use an "unknown" value here
      int interactionOperator = -1;

      if (id.equals("MBK_CF_IO_ALT"))
      {
         interactionOperator = IInteractionOperator.IO_ALT;
      }
      else if (id.equals("MBK_CF_IO_ELSE"))
      {
         interactionOperator = IInteractionOperator.IO_ELSE;
      }
      else if (id.equals("MBK_CF_IO_OPT"))
      {
         interactionOperator = IInteractionOperator.IO_OPT;
      }
      else if (id.equals("MBK_CF_IO_PAR"))
      {
         interactionOperator = IInteractionOperator.IO_PAR;
      }
      else if (id.equals("MBK_CF_IO_LOOP"))
      {
         interactionOperator = IInteractionOperator.IO_LOOP;
      }
      else if (id.equals("MBK_CF_IO_REGION"))
      {
         interactionOperator = IInteractionOperator.IO_REGION;
      }
      else if (id.equals("MBK_CF_IO_NEG"))
      {
         interactionOperator = IInteractionOperator.IO_NEG;
      }
      else if (id.equals("MBK_CF_IO_ASSERT"))
      {
         interactionOperator = IInteractionOperator.IO_ASSERT;
      }
      else if (id.equals("MBK_CF_IO_SEQ"))
      {
         interactionOperator = IInteractionOperator.IO_SEQ;
      }
      else if (id.equals("MBK_CF_IO_STRICT"))
      {
         interactionOperator = IInteractionOperator.IO_STRICT;
      }

      return interactionOperator;
   }

   /**
    * Determine the interaction operator String from its value.
    */
   protected String getInteractionOperatorString()
   {
      String strInteractionOperator = "ALT";
      int interactionOperator = IInteractionOperator.IO_ASSERT;  // default to assert?

      // Determine the interaction operator from the combined fragment model element
      ICombinedFragment combinedFragment = getCombinedFragment();
      if( combinedFragment != null )
      {
         interactionOperator = combinedFragment.getOperator();

         if( (interactionOperator >= IInteractionOperator.IO_ALT) &&
             (interactionOperator <= IInteractionOperator.IO_STRICT) )
         {
            strInteractionOperator = messages.getString( strIOs[interactionOperator] );

            // Remove any ampersand from the interation operator string
            strInteractionOperator = StringUtilities.replaceSubString( strInteractionOperator, "&", "" );
         }

         if( strInteractionOperator.length() > 0 )
         {
             setName( strInteractionOperator );
         }
      }
      else
      {
         // Fix W1761:  When dragging Combined Fragment onto diagram, "ref" displays in the Name Compartment
         //             For the change below (W5493) I was not able to test dragging the CF onto the diagram.

         // Fix W5493:  When reading back from the archive, we have to get the name
         strInteractionOperator = getName();
      }

      return strInteractionOperator;
   }


   /**
    * Get the CombinedFragment from the associated model element.
    *
    * @param ppCombinedFragment
    */
   protected ICombinedFragment getCombinedFragment()
   {
      ICombinedFragment combinedFragment = null;
      {
         IElement element = getModelElement();
         if( element instanceof ICombinedFragment )
         {
            combinedFragment = (ICombinedFragment)element;
         }
      }

      return combinedFragment;
   }


   private ETList < IETPoint > m_aptLabel = new ETArrayList < IETPoint >();
}



