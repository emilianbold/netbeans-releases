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
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;

import sun.security.x509.IssuerAlternativeNameExtension;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlImpl;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditEventPayload;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveAttribute;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontChooser;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import com.tomsawyer.graph.TSGraphObject;
import org.openide.util.NbBundle;

public class ETEditableCompartment extends ETCompartment implements IADEditableCompartment, IEditControlEventSink
{
   protected boolean m_bNew = false; // flag indicating that we're editing a new compartment
   protected boolean m_bIsStatic = false; // the element should be drawn as static
   protected boolean m_bIsAbstract = false; // the element should be drawn as abstract
   protected boolean m_bItalic = false; // previous settings of our font prior to drawing abstract/static
   protected boolean m_bUnderline = false;
   protected boolean m_bAutoExpand = false; // informs single line edit controls to expand as characters are typed in.
   protected long m_nEditStyle = 0; // style flags for the edit control
   protected boolean m_bInEditCreate = false; // indicates that we're beginning an edit operation

   protected int m_NameCompartmentBorderKind = IADNameCompartment.NCBK_DRAW_JUST_NAME;
   private static EditControlImpl m_EditControl = null;
   private static JDialog m_EditDialog = null;

   public ETEditableCompartment()
   {
      super();

   }

   public ETEditableCompartment(IDrawEngine pDrawEngine)
   {
      super(pDrawEngine);
   }

   /**
    * Called when the context menu is about to be displayed.  The compartment should add whatever buttons
    * it might need.
    *
    * @param pContextMenu[in] The context menu about to be displayed
    * @param logicalX[in] The logical x location of the context menu event
    * @param logicalY[in] The logical y location of the context menu event
    */
   public void onContextMenu(IMenuManager manager)
   {
      if (getEnableContextMenu())
      {
         
         IETSize compSize = getAbsoluteSize();

         // It's possible that we get into here without the compartment being
         // drawn.  If that's the case then the m_size in the transform is set to
         // expand to node and the compartment thinks it's as large as the node.  Dont
         // go into here if the compartment size hasn't been set.
         if (compSize.getHeight() != EXPAND_TO_NODE && compSize.getWidth() != EXPAND_TO_NODE)
         {
            if (m_selected)
            {
               // ok now create the button handler on demand
               addColorAndFontMenuButton(manager);
            }
         }
      }
   }

   /**
    * Adds the font and color menu buttons.
    *
    * @param pContextMenu[in] The menu about to be displayed
    */
   public void addColorAndFontMenuButton(IMenuManager manager)
   {
      IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_COMPARTMENT_TITLE"), "");
      if (subMenu != null)
      {
         //subMenu.add(createMenuAction(loadString("IDS_POPUPMENU_BKCOLOR"), "MBK_BKCOLOR"));
         subMenu.add(createMenuAction(loadString("IDS_POPUP_FONT"), "MBK_FONT"));
         subMenu.add(createMenuAction(loadString("IDS_POPUPMENU_FONTCOLOR"), "MBK_FONTCOLOR"));

         //manager.add(subMenu);
      }
   }

   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean readOnly = true;
      IDrawingAreaControl control = getDrawingArea();
      if (control != null)
      {
         readOnly = control.getReadOnly();
      }
      return !readOnly;
   }

   public boolean onHandleButton(ActionEvent e, String id)
   {
      boolean bHandled = false;
      String engineID = getEngine().getDrawEngineID();

      String resourceName = "";
      IDrawingPropertyProvider pDrawingPropertyProvider = (IDrawingPropertyProvider)this;
      ETList < IDrawingProperty > pDrawingProperties = pDrawingPropertyProvider.getDrawingProperties();
      IDrawingProperty pDrawingProperty = null;
      for (int i = 0; i < pDrawingProperties.size(); i++)
      {
         pDrawingProperty = pDrawingProperties.get(i);
         if (id.equals("MBK_FONT") && pDrawingProperty.getResourceType().equals("font"))
         {
            resourceName = pDrawingProperty.getResourceName();
            break;
         }
         else if (id.equals("MBK_FONTCOLOR") && pDrawingProperty.getResourceType().equals("color"))
         {
            resourceName = pDrawingProperty.getResourceName();
            break;
         }
      }

      if (id.equals("MBK_FONT"))
      {
         Font oldFont = null;
         if (pDrawingProperty instanceof IFontProperty)
         {
            IFontProperty pFontProperty = (IFontProperty)pDrawingProperty;
            int style = Font.PLAIN;
            if (pFontProperty.getItalic())
            {
               style |= Font.ITALIC;
            }
            if (pFontProperty.getWeight() >= 700)
            {
               style |= Font.BOLD;
            }
            oldFont = new Font(pFontProperty.getFaceName(), style, pFontProperty.getSize());
         }

         Font font = FontChooser.selectFont(oldFont);
         if (font != null)
         {
            int weight = 400;
            if (font.isBold())
            {
               weight = 700;
            }
            Color colorRef = getCompartmentFontColor();
            pDrawingPropertyProvider.saveFont(engineID, resourceName, font.getName(), font.getSize(), weight, font.isItalic(), colorRef.getRGB());
            pDrawingPropertyProvider.invalidateProvider();
         }
         bHandled = true;
      }
      else if (id.equals("MBK_FONTCOLOR"))
      {
         Color color = JColorChooser.showDialog(null, 
                 NbBundle.getMessage(ETEditableCompartment.class, "TITLE_Color_Chooser"), null);
         if (color != null)
         {
            pDrawingPropertyProvider.saveColor(engineID, resourceName, color.getRGB());
            pDrawingPropertyProvider.invalidateProvider();
         }

         bHandled = true;
      }

      return bHandled;
   }

   /**
    * Update from archive.
    *
    * @param pProductArchive [in] The archive we're reading from
    * @param pCompartmentElement [in] The element where this compartment's information should exist
    */
   public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {
      //first call super's method from ADCompartmentImpl
      super.readFromArchive(pProductArchive, pElement);

      IProductArchiveAttribute pAttr = pElement.getAttribute(IProductArchiveDefinitions.ADNAMECOMPARTMENTNAMECOMPARTMENTBORDERKIND_STRING);
      if (pAttr != null)
      {
         m_NameCompartmentBorderKind = (int)pElement.getAttributeLong(IProductArchiveDefinitions.ADNAMECOMPARTMENTNAMECOMPARTMENTBORDERKIND_STRING);
      }

      long lVal = pElement.getAttributeLong(IProductArchiveDefinitions.ADNAMECOMPARTMENTISSTATIC_STRING);
      m_bIsStatic = (lVal == 1);

      lVal = pElement.getAttributeLong(IProductArchiveDefinitions.ADNAMECOMPARTMENTISABSTRACT_STRING);
      m_bIsAbstract = (lVal == 1);
   }

   /**
    * Write ourselves to archive, returns the compartment element.
    *
    * @param pProductArchive[in] The archive we're saving to
    * @param pElement[in] The current element, or parent for any new attributes or elements
    * @param pCompartmentElement[out] The created element for this compartment's information
    */
   public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {
      IProductArchiveElement retObj = super.writeToArchive(pProductArchive, pElement);
      if (retObj != null)
      {
         retObj.addAttributeLong(IProductArchiveDefinitions.ADNAMECOMPARTMENTNAMECOMPARTMENTBORDERKIND_STRING, m_NameCompartmentBorderKind);
         if (m_bIsStatic)
         {
            retObj.addAttributeLong(IProductArchiveDefinitions.ADNAMECOMPARTMENTISSTATIC_STRING, 1);
         }
         if (m_bIsAbstract)
         {
            retObj.addAttributeLong(IProductArchiveDefinitions.ADNAMECOMPARTMENTISABSTRACT_STRING, 1);
         }
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
      return "ADEditableCompartment";
   }

   /**
    * Notifier that the model element has changed.
    *
    * @param pTargets [in] Information about what has changed.
    */
   public long modelElementHasChanged(INotificationTargets pTargets)
   {
      reattach(null);
      return 0;
   }

   /**
    * Handle a keydown event.
    *
    * @param KeyCode
    * @param Shift
    * @param bHandled
    */
   public boolean handleKeyDown(int nKeyCode, int nShift)
   {
      boolean handled = super.handleKeyDown(nKeyCode, nShift);

      if (!handled && m_EditControl != null)
      {
         m_EditControl.handleKeyDown(nKeyCode, nShift);
         handled = true;
      }

      // only handle if we're selected
      if (!handled && m_selected)
      {
         // 2 situations where we can edit, an alpha/num or enter
         // since we are coming here only on KeyPressed
	 // we willn't handle alpha as it be handled in handleCharTyped
         if (nKeyCode == KeyEvent.VK_ENTER
            || nKeyCode == KeyEvent.VK_F2 )
         {
            editCompartment(false, nKeyCode, nShift, -1);
            handled = true;
         }
      }
      return handled;
   }

   /**
    * Handle a keytyped event.
    *
    */
   public boolean handleCharTyped(char ch)
   {
      boolean handled = super.handleCharTyped(ch);

      if (!handled && m_EditControl != null)
      {
         m_EditControl.handleTypedChar(ch);
         handled = true;
      }

      // only handle if we're selected
      if (!handled && m_selected)
      {
         if (Character.isJavaIdentifierStart(ch))
         {
            editCompartment(false, ch, 0, -1, true);
            handled = true;
         }
      }
      return handled;
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
      return editCompartment(bNew, nKeyCode, nShift, nPos, false);
   }

   public long editCompartment(boolean bNew, int nKeyCode, int nShift, int nPos, boolean isCharTyped)
   {
      super.editCompartment(bNew, nKeyCode, nShift, nPos);
      m_bNew = (bNew == true);
      if (m_EditDialog == null)
      {
         m_EditDialog = new JDialog();
         m_EditControl = new EditControlImpl(this);
         m_EditControl.setOpaque(false);
         m_EditControl.setForeColor(getCompartmentFontColor());
         //m_EditControl.setStyle(m_style);
         m_EditControl.setStyle(getHorizontalAlignment());
         if (this instanceof IADEditableCompartment)
         {
            ((IADEditableCompartment)this).connectEditControl(m_EditControl);
         }

         if (m_engine != null && m_engine instanceof ETNodeDrawEngine)
         {
            m_EditControl.setEditControlBackground(((ETNodeDrawEngine)m_engine).getBkColor());
            m_EditDialog.setBackground(((ETNodeDrawEngine)m_engine).getBkColor());
            m_EditDialog.getRootPane().setBackground(((ETNodeDrawEngine)m_engine).getBkColor());
         }
         m_EditDialog.setUndecorated(true);

         if (m_modelElement == null)
         {
            reattach();
         }
         m_EditControl.setElement(m_modelElement);
         IDrawingAreaControl pDrawingArea = getDrawingArea();
         if (pDrawingArea != null)
         {
            m_EditControl.setFont(getCompartmentFont(pDrawingArea.getCurrentZoom()));

            pDrawingArea.beginEditContext(this);
         }
         //if a key is pressed, we do not want to lose that, so select the content of edit control and
         //put the pressed key name - we can come in edit mode when pressing enter too.
         if (isCharTyped || (nKeyCode != 0 && nKeyCode != KeyEvent.VK_ENTER && nKeyCode != KeyEvent.VK_F2))
         {
            m_EditControl.setCharacter(nKeyCode, nShift);
         }

         this.getEngine().getDrawingArea().setEditCompartment(this);
         //editCtrl.setPreferredSize(new Dimension(300,25));
         m_EditDialog.setContentPane(m_EditControl);
         if (m_boundingRect != null)
         {
            Rectangle rect = (Rectangle)m_boundingRect;
            if (rect != null && rect.x == 0 && rect.y == 0)
            {
               rect = getDeviceBoundingRectangle();
            }
            Point p1 = this.getEngine().getDrawingArea().getGraphWindow().getCanvas().getLocationOnScreen();
            //m_EditDialog.setBounds(p1.x + rect.x, p1.y + rect.y, rect.width-5, 20);

            // in some case the height is 0, retrieve the hight of the font.  To
            // retrieve the hieght of the font we need to use the font metrics
            // from the edit control.
            int height = rect.height;
            if (height <= 0)
            {
               Font f = m_EditControl.getFont();
               LineMetrics metrics = f.getLineMetrics("Qpz", new FontRenderContext(null, true, false));
               height = (int)metrics.getHeight();
            }

            if (rect.getWidth() >= 0)
            {
               m_EditDialog.setBounds(p1.x + rect.x, p1.y + rect.y, rect.width /*-5*/
               , height);
            }
            else if (m_cachedOptimumSize != null)
            {
               int width = m_cachedOptimumSize.getWidth();
               if (width > 5)
               {
                  m_EditDialog.setBounds(p1.x + rect.x, p1.y + rect.y, width - 5, height);
               }
               else
               {
                  //hardcode width to 25
                  m_EditDialog.setBounds(p1.x + rect.x, p1.y + rect.y, 25, height);
               }
            }
            else
            {
               //optimum size was null, so use the width we have on rect.
               m_EditDialog.setBounds(p1.x + rect.x, p1.y + rect.y, rect.width - 5, height);
            }
         }
         m_EditDialog.getRootPane().setBorder(null);
         m_EditDialog.getRootPane().setOpaque(false);
         //dialog.setModal(true);
         m_EditControl.requestFocus();
         m_EditDialog.show();
      }
      else
      {
         save();

         //now edit the new compartment
         editCompartment(bNew, nKeyCode, nShift, nPos);
      }

      return 0;
   }

   public void connectEditControl(EditControlImpl editCtrl)
   {
      if (editCtrl != null)
      {
         // connect handler to application's event dispatcher
         DispatchHelper helper = new DispatchHelper();
         helper.registerEditCtrlEvents(this);
      }
   }

   public void disconnectEditControl(EditControlImpl editCtrl)
   {
      if (editCtrl != null)
      {
         // connect handler to application's event dispatcher
         DispatchHelper helper = new DispatchHelper();
         helper.revokeEditCtrlSink(this);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCenterText()
    */
   public boolean getCenterText()
   {
      return getHorizontalAlignment() == IADCompartment.CENTER;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#setCenterText(boolean)
    */
   public void setCenterText(boolean center)
   {
      if (center)
         setHorizontalAlignment(IADCompartment.CENTER);
      else
         setHorizontalAlignment(IADEditableCompartment.LEFT); // Default to left.
   }

   /**
    * Fired when data not consistent with the selected mask is passed
    */
   public void onPreInvalidData(String ErrorData, IResultCell cell)
   {
   }

   /**
    * Fired when data not consistent with the selected mask is passed
    */
   public void onInvalidData(String ErrorData, IResultCell cell)
   {
   }

   /**
    * Fired when user toggles Insert/Overstrike mode via the Insert key
    */
   public void onPreOverstrike(boolean bOverstrike, IResultCell cell)
   {
   }

   /**
    * Fired when user toggles Insert/Overstrike mode via the Insert key
    */
   public void onOverstrike(boolean bOverstrike, IResultCell cell)
   {
   }

   /**
    * The control is about to gain focus
    */
   public void onPreActivate(IEditControl pControl, IResultCell cell)
   {
   }

   /**
    * The control has gained focus
    */
   public void onActivate(IEditControl pControl, IResultCell cell)
   {
   }

   /**
    * Edit control Deactivate handler, if user aborted editing a new compartment the compartment
    * is removed and the element deleted.  Also the exit keycode is looked at, if up or down
    * the appropriate compartment is selected.
    *
    * @param pControl[in] The control firing the event
    */
   public void onDeactivate(IEditControl pControl, IResultCell cell)
   {
      if (cell != null)
      {
         Object data = cell.getContextData();
         if (data != null && data instanceof IEditEventPayload)
         {
            IEditEventPayload payload = (IEditEventPayload)data;
            boolean modified = false;

            // don't save if the user cancelled out
            modified = payload.getModified();

            int key = payload.getKey();
            if (!modified && m_bNew)
            {
               // user is cancelling out of a new compartment, destroy the underlying element
               // destroy ourselves if they cancelled
               IDrawEngine engine = getEngine();
               if (engine != null)
               {
                  // Get the parent product element, if there is one
                  IETLabel pETLabel = getParentETLabel();
                  if (pETLabel != null)
                  {
                     // got a label, find its presentation element
                     IDrawingAreaControl pDrawingArea = getDrawingArea();
                     if (pDrawingArea != null)
                     {
                        ILabelPresentation pPres = TypeConversions.getLabelPresentation(pETLabel);
                        if (pPres != null)
                        {
                           // instruct draw area to remove this label
                           pDrawingArea.postDeletePresentationElement(pPres);
                        }
                     }
                  }
                  else if (key == KeyEvent.VK_ESCAPE)
                  {
                     // not a label, just delete our element
                     IElement pEle = getModelElement();
                     if (pEle != null)
                     {
                        pEle.delete();
                     }
                  }
               }
            }
            m_bNew = false;
         }
      }
   }

   /**
    * Sets an AxEditEvents object as owner of this event sink. Events will be routed to the owner
    */
   public void setEventOwner(/* long */
   int pOwner)
   {
   }

   /**
    * Model element data is about to be saved.
    */
   public void onPreCommit(IResultCell cell)
   {
   }

   /**
    * Model element data has been saved.
    */
   public void onPostCommit(IResultCell cell)
   {
   }

   public void save() {
       super.save();
       if (m_EditControl != null) {
           disconnectEditControl(m_EditControl);
           
           m_EditDialog.setVisible(false);
           m_EditControl.handleSave();
           
           //Jyothi: A hack to fix Bug#6258627
           IDrawingAreaControl pDrawingArea = getDrawingArea();
           if (pDrawingArea != null) {
//               Debug.out.println("########## Jyothi: ETEditableCompartment : save--firing select event.. ");
               if (pDrawingArea instanceof ADDrawingAreaControl) {
                   ETList selectedList = ((ADDrawingAreaControl)pDrawingArea).getSelectedNodesAndEdges();
                   printSelectedObjects(selectedList);
                   getDrawingArea().fireSelectEvent(selectedList);
                   getDrawingArea().refresh(true);
               }
//               Debug.out.println("########## Jyothi: ETEditableCompartment : save--firing select event.. DONE!! ");
           }
           
           m_EditControl = null;
           m_EditDialog = null;
       }
   }
   
   //Jyothi
   public void printSelectedObjects(ETList list) {
       if (list == null) {
//           Debug.out.println(" ETEditableCompartment : printSelectedObjects...... LIST EMPTY @@@@@@@@@@@@@@@@@@@@@@@@");
           return;
       }
       java.util.Iterator iter = list.iterator();
		while (iter.hasNext())
		{
			TSGraphObject go = (TSGraphObject)iter.next();
//			Debug.out.println(" next element's = "+go);							
		}
   }

   public void cancelEditing()
   {
      super.cancelEditing();
      if (m_EditControl != null)
      {
         disconnectEditControl(m_EditControl);

         m_EditDialog.setVisible(false);
         m_EditControl.handleRollback();
         m_EditControl = null;
         m_EditDialog = null;
      }
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment#isMouseInTextRect(java.awt.event.MouseEvent, boolean)
    */
   public boolean isMouseInTextRect(MouseEvent pEvent, boolean pHandled)
   {
      Rectangle rect = this.m_textRect != null ? this.m_textRect.getRectangle() : null;
      Point point = pEvent.getPoint();

      return !pHandled && !this.m_readOnly && rect != null && rect.contains(point.x, point.y);
   }

   /*
    * Returns true if the text editor is active on any compartment
    */
   public static boolean isEditorActive()
   {
      return m_EditControl != null;
   }

   /*
    * Returns true if the text editor is actively editing this compartment.
    */
   public boolean isEditing()
   {
      return m_EditControl != null && m_EditControl.getAssociatedParent() == this;
   }


    /////////////
    // Accessible
    /////////////


    AccessibleContext accessibleContext;
    
    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleETEditableCompartment();
	}
	return accessibleContext;
    }
    
    
    public class AccessibleETEditableCompartment extends AccessibleETCompartment {
	
	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.TEXT;
	}
	
	public AccessibleStateSet getAccessibleStateSet() {
	    AccessibleStateSet stateSet = super.getAccessibleStateSet();
	    if (stateSet != null) {
		stateSet.add(AccessibleState.SELECTABLE);
		return stateSet;
	    }
	    return null;
	}

	///////////////////////////
	// interface AccessibleText
	///////////////////////////

	public int getIndexAtPoint(java.awt.Point point) {
	    return getAccessibleName().charAt(0);
	}

	public java.awt.Rectangle getCharacterBounds(int index) {
	    return null;
	}
 
	public int getCharCount() {
	    return getAccessibleName().length();
	}

	public int getCaretPosition() {
	    return 0;
	}
	
	public java.lang.String getAtIndex(int part, int index) {	    
	    return getAccessibleName().substring(index, index + 1);
	}

	public java.lang.String getAfterIndex(int part, int index) {
	    return getAccessibleName().substring(index + 1);
	}

	public java.lang.String getBeforeIndex(int part, int index) {
	    return getAccessibleName().substring(0, index - 1);
	}

	public javax.swing.text.AttributeSet getCharacterAttribute(int index) {
	    return null;
	}

	public int getSelectionStart() {
	    return 0;
	}
	public int getSelectionEnd() {
	    return 0;
	}
	public java.lang.String getSelectedText() {
	    return null;
	}

    }


}
