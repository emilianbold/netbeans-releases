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

package org.netbeans.modules.uml.ui.controls.editcontrol;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.ref.WeakReference;
import java.text.CharacterIterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.drawingproperties.DrawingPropertyResource;
import org.netbeans.modules.uml.ui.swing.projecttree.ProjectTreeCellEditor;

/**
 * @author sumitabhk
 * TODO: meteora
 */
public class EditControlImpl extends JPanel implements IEditControl, InputMethodListener
{
   static final JTextComponent.KeyBinding[] defaultBindings =
   {
      new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), DefaultEditorKit.copyAction),
      new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK), DefaultEditorKit.copyAction),
      //new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), DefaultEditorKit.pasteAction),
      //new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), DefaultEditorKit.cutAction)
   };

   private JTextComponent m_Field = null;
   private JButton m_Button = null;
   private JPanel m_Panel = null;
   private JPopupMenu m_TooltipMenu = null;

   private ITranslator m_Translator = null;
   private IEditControlEventDispatcher m_EventDispatcher = null;
   private String m_InitialData = "";
   private boolean m_Modified = false;
   private String m_SeparatorList = "";

   private boolean m_VeryFirstTime = false;
   private boolean m_IgnoreTextUpdate = false;
   private boolean m_UpdatingField = false;
   private Point m_LocationOnScreen = null;
   private String m_TooltipText = "";
   //private Color m_TooltipBGColor = UIManager.getColor("ToolTip.foreground");
   private Color m_TooltipBGColor = new Color(232, 228, 232); //UIManager.getColor("controlLtHighlight");

   private WeakReference m_Parent = null;

   private int m_SelectionStartPos = 0;
   private int m_SelectionEndPos = 0;

   private int m_InitialLoc = 0;

   private boolean m_ShiftDown = false;
   private boolean m_ControlDown = false;
   private int m_LastKey = 0;
   private Color m_BackgroundColor = null;

   private boolean m_IsMultiline = false;
   private boolean m_ShowTooltips = true;

   private IStrings m_List = null;


   // state maintenance variables for in-between InputMethodTextChanged calls
   private int ime_SelectionStartPos = 0;
   private int ime_SelectionEndPos = 0;
   private int ime_InitialLoc = 0;
   private boolean ime_Cached = false; 
   private StringBuffer ime_CachedChars = null;
   
   /**
    * 
    */
   public EditControlImpl(Object parent)
   {
       this(parent, false);
   }
   
   public EditControlImpl(Object parent, boolean multiline)
   {
      super();

      m_IsMultiline = multiline;
      setAssociatedParent(parent);
      establishPreferences();
      initComponents();
      initControl();
   }

   public EditControlImpl()
   {
      this(false);
   }
   
   public EditControlImpl(boolean multiline)
   {
      super();
      
      m_IsMultiline = multiline;
      establishPreferences();
      initComponents();
      initControl();
   }

   public void addDocumentListener(DocumentListener listener)
   {
       if(m_Field != null)
       {
           m_Field.getDocument().addDocumentListener(listener);
       }
   }
   
   public void removeDocumentListener(DocumentListener listener)
   {
       if(m_Field != null)
       {
           m_Field.getDocument().removeDocumentListener(listener);
       }
   }
   
   private void establishPreferences()
   {
      //kris richards - "ShowEditToolTip" pref expunged. Set to "PSK_YES".
       //this method no longer does anything.
   }

   private class CutAction extends AbstractAction
   {
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e)
      {
         setSel(m_Field.getSelectionStart(), m_Field.getSelectionEnd());
         m_Translator.cutToClipboard();
         //         m_Field.paste();
      }
   }

   private class PasteAction extends AbstractAction
   {
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e)
      {
         setSel(m_Field.getSelectionStart(), m_Field.getSelectionEnd());
         m_Translator.pasteFromClipboard();
         //         m_Field.paste();
      }
   }

   protected void initControl()
   {
      //now instantiate the event dispatcher
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         // Get the edit control event dispatcher
         DispatchHelper disp = new DispatchHelper();
         IEventDispatcher dispatcher = disp.getEditControlDispatcher();
         putEventDispatcher(dispatcher);
      }

      // Added support for copy, cut & paste
      Keymap map = m_Field.getKeymap();
      JTextComponent.loadKeymap(map, defaultBindings, m_Field.getActions());
      {
         KeyStroke keyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_V, InputEvent.CTRL_MASK );
         map.addActionForKeyStroke( keyStroke, new PasteAction());
         map.addActionForKeyStroke( KeyStroke.getKeyStroke( KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK ), new PasteAction());
         keyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_X, InputEvent.CTRL_MASK );
         map.addActionForKeyStroke( keyStroke, new CutAction() );
         
         m_Field.setKeymap(map);
      }
      
      addFocusListener(new FocusListener()
      {

         public void focusGained(FocusEvent e)
         {
            m_Field.requestFocusInWindow();
         }

         public void focusLost(FocusEvent e)
         {
            
         }
      });

      m_Field.addKeyListener(new KeyListener()
          {
             //we want this key listener to handle "ENTER" and "ESCAPE" key presses -
             // "ENTER" - commits the changes
             // "ESCAPE" - cancels the changes.

             public void keyTyped(KeyEvent e)
             {
                handleTypedKey(e);
                e.consume();
             }

             public void keyPressed(KeyEvent e)
             {
                handleKeyDown(e);
                //e.consume();
             }

             public void keyReleased(KeyEvent e)
             {
                e.consume();
                //handleKey(e);
             }
          });
          
      if(m_IsMultiline == false)
      {
          m_Field.addCaretListener(new CaretListener()
          {

             public void caretUpdate(CaretEvent e)
             {
                if (m_Translator != null)
                {
                   if (m_ShowTooltips)
                   {
                      m_Translator.updateHints();
                   }
                }
             }
          });

          //		m_Field.getDocument().addDocumentListener(m_docListener);

          //I want the tooltips to show continuously.
          ToolTipManager.sharedInstance().setInitialDelay(0);
          ToolTipManager.sharedInstance().setDismissDelay(1000000);

          m_Field.addFocusListener(new FocusListener()
          {

             public void focusGained(FocusEvent e)
             {
                try
                {
                   showToolTip(e);
                }
                catch (Exception exp)
                {
                   exp.printStackTrace();
                }
             }

             public void focusLost(FocusEvent e)
             {
                hideToolTip(e);
             }
          });

          m_Field.addMouseListener(new MouseListener()
          {

             public void mouseClicked(MouseEvent arg0)
             {
                //				if (arg0.getClickCount() == 2)
                //				{
                //					//some field will be selected, so set the selection start and end accordingly.
                //					Object source = arg0.getSource();
                //					if (source != null && source instanceof JTextField)
                //					{
                //						JTextField field = (JTextField)source;
                //						int start = field.getSelectionStart();
                //						int end = field.getSelectionEnd();
                //						setSel(start, end);
                //					}
                //				}
                arg0.consume();
             }

             public void mousePressed(MouseEvent arg0)
             {
                //I want to set the selection start and end positions if its single click.
                if (arg0.getClickCount() == 1)
                {
                   int pos = getCurrentPosition();
                   setSel(pos, pos);
                }
             }

             public void mouseReleased(MouseEvent arg0)
             {
                arg0.consume();
             }

             public void mouseEntered(MouseEvent arg0)
             {
                arg0.consume();
             }

             public void mouseExited(MouseEvent arg0)
             {
                arg0.consume();
             }
          });

      }
      m_Field.addInputMethodListener(this);
   }

   private void handleTypedKey(KeyEvent e)
   {
       if ( ! (e.isAltDown() || e.isControlDown() || e.isMetaDown())) 
       {
	   char ch = e.getKeyChar();
	   if ((int)ch != 8       // don't need backspace key_typed 
	       && (int)ch != 127  // don't need delete key_typed, see 4904441
	       && (int)ch != 10)  // don't need enter key_typed either
	   {
	       handleTypedChar(ch);
	   }
       }
   }

   public void handleTypedChar(char ch) {
      //something is typed
      int currPos = getCurrentPosition();
      m_InitialLoc = currPos;
      //System.out.println("handleTypedChar:currPos1="+currPos);
      
      IEditControlField field = getCurrentField();
      String toIns = Character.toString(ch);
      //    String toIns = "";
      //      if (e.isShiftDown())
      //      {
      //         toIns = String.valueOf(Character.toString(ch));
      //      }
      //      else
      //      {
      //         toIns = String.valueOf(Character.toLowerCase(ch));
      //      }
      boolean selectedText = false;
      setSel(m_Field.getSelectionStart(), m_Field.getSelectionEnd());
      if (m_SelectionEndPos != m_SelectionStartPos)
      {
         selectedText = true;
         currPos = m_SelectionStartPos;
      }
      if (m_Translator != null)
      {
         int index = m_SeparatorList.indexOf(toIns);
         boolean isHandled = false;
         if (index >= 0)
         {
            isHandled = m_Translator.handleTopLevelSeparators(ch);
         }
         if (!isHandled)
         {
            isHandled = m_Translator.handleChar(toIns);

            if (isHandled)
            {
               //if I have handled here, then we are going to move caret position by one.
               String text = m_Field.getText();
               if (text != null)
               {
                  if (selectedText)
                  {
                     //System.out.println("handleTypedChar:currPos2="+currPos);
                     m_Field.setCaretPosition(currPos + 1);
                  }
                  else
                  {
                     if (text.length() > m_InitialLoc)
                     {
                       //System.out.println("handleTypedChar:m_InitialLoc="+m_InitialLoc);
                        m_Field.setCaretPosition(m_InitialLoc + 1);
                     }
                     else
                     {
                        m_Field.setCaretPosition(text.length());
                     }
                  }
               }
            }
         }
      }
   }

   public void handleKeyDown(int keyCode, int nShift)
   {
      m_InitialLoc = getCurrentPosition();
      boolean consumeEvent = true;
      boolean selectedText = false;
      Object associateParents = getAssociatedParent();
      
      if (m_SelectionEndPos != m_SelectionStartPos)
      {
         selectedText = true;
      }
      int pos = getCurrentPosition();
      if (keyCode == KeyEvent.VK_ENTER)
      {
         //commit the changes to the edit control
         if (associateParents != null)
         {
            if (associateParents instanceof ProjectTreeCellEditor)
            {
               ((ProjectTreeCellEditor)associateParents).stopCellEditing();
            }
            else if (associateParents instanceof InplaceEditorProvider.EditorController)
            {
                Container parent = getParent();
                ((InplaceEditorProvider.EditorController)associateParents).closeEditor(true);
                parent.requestFocusInWindow ();
            }
         }
         consumeEvent = false;
      }
      else if (keyCode == KeyEvent.VK_ESCAPE)
      {
         //cancel out the changes made to edit control
         if (associateParents != null)
         {
             if (associateParents instanceof ProjectTreeCellEditor)
            {
               ((ProjectTreeCellEditor)associateParents).cancelCellEditing();
            }
            else if (associateParents instanceof InplaceEditorProvider.EditorController)
            {
                Container parent = getParent();
                ((InplaceEditorProvider.EditorController)associateParents).closeEditor(false);
                 parent.requestFocusInWindow ();
            }
         }
         consumeEvent = false;
      }
      else if (keyCode == KeyEvent.VK_DELETE)
      {
         if (m_Translator != null)
         {
            m_Translator.handleDelete(true);

            //we need to reposition caret at the original position if nothing is selected
            if (selectedText)
            {
               m_Field.setCaretPosition(m_SelectionEndPos);
            }
            else
            {
               m_Field.setCaretPosition(m_InitialLoc);
            }
         }
      }
      else if (keyCode == KeyEvent.VK_BACK_SPACE)
      {
         if (m_Translator != null)
         {
            m_Translator.handleDelete(false);

            //we need to reposition caret at one less than original position if nothing is selected
            if (m_InitialLoc > 0)
            {
               if (selectedText)
               {
                  m_Field.setCaretPosition(m_SelectionEndPos);
               }
               else
               {
                  m_Field.setCaretPosition(m_InitialLoc - 1);
               }
            }
            else
            {
               m_Field.setCaretPosition(m_InitialLoc);
            }
         }
      }
      else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_TAB)
      {
         if (m_Translator != null)
         {
            m_Translator.handleKeyDown(keyCode);
         }
      }
      else if (keyCode == KeyEvent.VK_HOME || keyCode == KeyEvent.VK_END ||
      keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_INSERT ||
      keyCode == KeyEvent.VK_F1 || keyCode == KeyEvent.VK_F2 ||
      keyCode == KeyEvent.VK_F3 || keyCode == KeyEvent.VK_F4 ||
      keyCode == KeyEvent.VK_F5 || keyCode == KeyEvent.VK_F6 ||
      keyCode == KeyEvent.VK_F7 || keyCode == KeyEvent.VK_F8 ||
      keyCode == KeyEvent.VK_F9 || keyCode == KeyEvent.VK_F10 ||
      keyCode == KeyEvent.VK_F11 || keyCode == KeyEvent.VK_F12 ||
      keyCode == KeyEvent.VK_F13 || keyCode == KeyEvent.VK_F14 ||
      keyCode == KeyEvent.VK_F15 || keyCode == KeyEvent.VK_F16 ||
      keyCode == KeyEvent.VK_F17 || keyCode == KeyEvent.VK_F18 ||
      keyCode == KeyEvent.VK_F19 || keyCode == KeyEvent.VK_F20 ||
      keyCode == KeyEvent.VK_F21 || keyCode == KeyEvent.VK_F22 ||
      keyCode == KeyEvent.VK_F23 || keyCode == KeyEvent.VK_F24 ||
      keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_CONTROL )
      {
         consumeEvent = false;
      }
      else
      {
         //we will get here when the edit control is not yet shown but the user keeps typing/ a fast typer.
         if (m_Translator != null)
         {
            if (nShift == 1)
            {
               String str = String.valueOf(Character.toLowerCase((char)keyCode));
               m_Translator.handleChar(str);
            }
            else
            {
               String str = String.valueOf(Character.toString((char)keyCode));
               m_Translator.handleChar(str);
            }
         }
      }

      //		if (consumeEvent && !e.isConsumed())
      //		{
      //			e.consume();
      //		}

      //I want to set the selection start and end positions.
      pos = getCurrentPosition();
      setSel(pos, pos);
   }

    public void handleKeyDown(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        m_InitialLoc = getCurrentPosition();
        IEditControlField initField = getCurrentField();
        Object associatedParent = getAssociatedParent();
         
        //in case of mouse selection, we will come here with selected text.
        boolean consumeEvent = true;
        boolean resetSel = true;
        boolean selectedText = false;
        m_ControlDown = e.isControlDown();
        m_ShiftDown = e.isShiftDown();
        m_LastKey = keyCode;
        int pos = m_InitialLoc;
        if (keyCode == KeyEvent.VK_ENTER)
        {
            //Ctrl-Enter create a newline in multi-line editor
            if (isControlDown())
            {
                if( m_IsMultiline)
                {
                    IEditControlField ecf = getCurrentField();  
                    m_Field.replaceSelection("\n");
                   
                    if (ecf != null)
                    {
                        ecf.setText(m_Field.getText());
                    } 
                }
                consumeEvent = true;
                resetSel = false;
//                m_Field.setCaretPosition(m_Field.getCaretPosition() + 1);
            }
            else
            {
                //commit the changes to the edit control
                if (associatedParent != null)
                {
                    if (associatedParent instanceof ProjectTreeCellEditor)
                    {
                        if (!((ProjectTreeCellEditor) associatedParent).stopCellEditing())
                        {
                            //cancel cell editing and go back to prev state.
                            cancelCellEditing();
                        }
                        IProduct prod = ProductHelper.getProduct();
                        if (prod instanceof IADProduct)
                        {
                            IADProduct adProd = (IADProduct) prod;
                            IProjectTreeControl tree = adProd.getProjectTree();
                            if (tree != null)
                            {
                                tree.refresh(true);
                            }
                        }
                    }
                    else
                    {
                        if (associatedParent instanceof InplaceEditorProvider.EditorController)
                        {
                            Container parent = getParent();
                            ((InplaceEditorProvider.EditorController) associatedParent).closeEditor(true);
                            parent.requestFocusInWindow();
                        }
                    }
                }
                consumeEvent = true;
            }
        }
        else if (keyCode == KeyEvent.VK_ESCAPE)
        {
            //cancel cell editing and go back to prev state.
            cancelCellEditing();
            if (associatedParent instanceof InplaceEditorProvider.EditorController)
            {
                Container parent = getParent();
                ((InplaceEditorProvider.EditorController)associatedParent).closeEditor(false);
                parent.requestFocusInWindow ();
            }
            consumeEvent = true;
        }
        else if (keyCode == KeyEvent.VK_HOME || keyCode == KeyEvent.VK_END ||
                keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_INSERT ||
                keyCode == KeyEvent.VK_F1 || keyCode == KeyEvent.VK_F2 ||
                keyCode == KeyEvent.VK_F3 || keyCode == KeyEvent.VK_F4 ||
                keyCode == KeyEvent.VK_F5 || keyCode == KeyEvent.VK_F6 ||
                keyCode == KeyEvent.VK_F7 || keyCode == KeyEvent.VK_F8 ||
                keyCode == KeyEvent.VK_F9 || keyCode == KeyEvent.VK_F10 ||
                keyCode == KeyEvent.VK_F11 || keyCode == KeyEvent.VK_F12 ||
                keyCode == KeyEvent.VK_F13 || keyCode == KeyEvent.VK_F14 ||
                keyCode == KeyEvent.VK_F15 || keyCode == KeyEvent.VK_F16 ||
                keyCode == KeyEvent.VK_F17 || keyCode == KeyEvent.VK_F18 ||
                keyCode == KeyEvent.VK_F19 || keyCode == KeyEvent.VK_F20 ||
                keyCode == KeyEvent.VK_F21 || keyCode == KeyEvent.VK_F22 ||
                keyCode == KeyEvent.VK_F23 || keyCode == KeyEvent.VK_F24 ||
                keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_CONTROL)
        {
            consumeEvent = false;
            resetSel = false;
        }
        else
        {
            if (keyCode == KeyEvent.VK_DOWN)
            {
                if (m_IsMultiline)
                {
                    consumeEvent = false;
                    resetSel = false;
                }
                else
                {
                    if (isControlDown())
                    {
                        handleHint();
                    }
                }
            }
            else if (keyCode == KeyEvent.VK_UP)
            {
                if (m_IsMultiline)
                {
                    consumeEvent = false;
                    resetSel = false;
                }
            }
            else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_TAB)
            {

                if (isShiftDown() && (keyCode != KeyEvent.VK_TAB))
                {
                    //let the text field handle it.
                    resetSel = false;
                    consumeEvent = false;
                }
                else
                {
                    if (m_IsMultiline)
                    {
                        consumeEvent = false;
                        resetSel = false;
                    }
                    else
                    {
                        if (m_Translator != null)
                        {
                            if (m_Translator.getEditControl() == null)
                                m_Translator.setEditControl(this);
                            m_Translator.handleKeyDown(keyCode);
                        }
                    }
                }
            }
            else if (m_ControlDown)
            {
                // The above condition is a HACK to allow copy, cut & paste to work properly.
                resetSel = false;
                consumeEvent = false;
            }
            else
            {
                //we might have something selected on the field, so reset our selection start and end.
                setSel(m_Field.getSelectionStart(), m_Field.getSelectionEnd());
                if (m_SelectionEndPos != m_SelectionStartPos)
                {
                    selectedText = true;
                }
                if (keyCode == KeyEvent.VK_DELETE)
                {
                    if (m_Translator != null)
                    {
                        int oldStart = -1;
                        if (initField != null)
                        {
                            oldStart = initField.getTextStartPos();
                        } 
                            
                        m_Translator.handleKeyDown(keyCode);

                        //we need to reposition caret at the original position if nothing was selected to start with.
                        if (selectedText)
                        {
                            m_Field.setCaretPosition(m_SelectionEndPos);
                        }
                        else
                        {

                            //if we have removed the field we started with, I want to position myself
                            //at the end of previous visible field
                            if (initField != null)
                            {
                                if (initField.getVisible())
                                {
                                    m_Field.setCaretPosition(m_InitialLoc 
                                                             + (initField.getTextStartPos() - oldStart));
                                }
                                else
                                {
                                    m_Field.setCaretPosition(initField.getFieldStartPos());
                                }
                            }
                        }
                    }
                }
                else if (keyCode == KeyEvent.VK_BACK_SPACE)
                {
                    if (m_Translator != null)
                    {
                        m_Translator.handleKeyDown(keyCode);

                        //we need to reposition caret at one less than original position if nothing was selected.
                        if (m_InitialLoc > 0)
                        {
                            if (selectedText)
                            {
                                m_Field.setCaretPosition(m_SelectionEndPos);
                            }
                            else
                            {
                                //if we have removed the field we started with, I want to position myself
                                //at the end of previous visible field
                                if (initField != null)
                                {
                                    if (initField.getVisible())
                                    {
                                        m_Field.setCaretPosition(m_InitialLoc - 1);
                                    }
                                    else
                                    {
                                        m_Field.setCaretPosition(initField.getFieldStartPos());
                                    }
                                }
                            }
                        }
                        else
                        {
                            m_Field.setCaretPosition(m_InitialLoc);
                        }
                    }
                }
                else
                {
                    //handleTypedKey(e);
                    consumeEvent = false;
                    resetSel = false;
                }
            }
        }

        if (consumeEvent && !e.isConsumed())
        {
            e.consume();
        }

        if (resetSel)
        {
            //I want to set the selection start and end positions.
            pos = getCurrentPosition();
            setSel(pos, pos);
        }
    }

   private void hideToolTip(FocusEvent e)
   {
      Component other = e.getOppositeComponent();
      if (other == null || (!other.equals(m_Button) && !other.equals(m_Panel) && !other.equals(this)))
      {
         m_TooltipMenu.setBounds(0, 0, 0, 0);
         m_TooltipMenu.setVisible(false);
      }
   }

   private void cancelCellEditing()
   {
      //deactivate the edit control
      deactivate();

      //cancel out the changes made to edit control
      if (getAssociatedParent() != null)
      {
//         if (getAssociatedParent() instanceof ETCompartment)
//         {
//            ((ETCompartment)getAssociatedParent()).cancelEditing();
//         }
//         else 
             if (getAssociatedParent() instanceof ProjectTreeCellEditor)
         {
            ((ProjectTreeCellEditor)getAssociatedParent()).cancelCellEditing();
         }
      }

      return;
   }

   private void showToolTip(FocusEvent e)
   {
      try
      {
         if (m_ShowTooltips)
         {
            Point p = e.getComponent().getLocationOnScreen();

            FontMetrics metrix = m_Field.getFontMetrics(m_Field.getFont());
            int fieldHeight = metrix.getHeight();

            metrix = m_TooltipMenu.getFontMetrics(m_TooltipMenu.getFont());
            int width = metrix.stringWidth(m_TooltipText);
            int height = (int)metrix.getHeight();
            
            m_TooltipMenu.setBounds(p.x, p.y - height - fieldHeight/2, width, height);
            m_TooltipMenu.setLocation(p.x, p.y - height - fieldHeight/2);
            m_TooltipMenu.setVisible(true);
            m_TooltipMenu.updateUI();
         }
      }
      catch (Exception exc)
      {
         //do nothing
      }
   }

   private void handleFieldModified(DocumentEvent e)
   {
      try
      {
         m_UpdatingField = true;
         if (m_VeryFirstTime || m_IgnoreTextUpdate)
         {
            m_VeryFirstTime = false;
            return;
         }
         int length = e.getLength();
         int pos = e.getOffset();
         IEditControlField pField = getCurrentField();
         if (pField != null)
         {
            String text = m_Field.getText();
            String initText = pField.getText();
            String newText = "";
            boolean lastCharDeleted = false;
            if (e.getType().equals(DocumentEvent.EventType.INSERT))
            {
               m_SelectionStartPos = pos;
               m_SelectionEndPos = pos;
               //process the insert of text
               String changedText = text.substring(pos, pos + length);

               //there is a possibility that user is entering an optional field
               int nEnd = pField.getTextEndPos();
               boolean isHandled = false;
               if (m_Translator != null)
               {
                  char toIns = changedText.charAt(0);
                  int index = m_SeparatorList.indexOf(toIns);
                  if (index >= 0)
                  {
                     isHandled = m_Translator.handleTopLevelSeparators(changedText.charAt(0));
                  }
                  if (!isHandled)
                  {
                     isHandled = m_Translator.handleChar(changedText);
                  }
               }

               if (!isHandled)
               {
                  if (pos >= nEnd)
                  {
                  }
                  else
                  {
                     if (changedText.trim().length() > 0)
                     {
                        String leftText = "";
                        String rightText = "";
                        int nStart = pField.getTextStartPos();
                        //we might be adding value for a new field in which case
                        //initText will be "".
                        if (pos > nStart && initText.length() > (pos - nStart))
                        {
                           leftText = initText.substring(0, pos - nStart);
                           rightText = initText.substring(pos - nStart);
                        }
                        else if (pos == nStart)
                        {
                           //we are adding somthing to the start of this field
                           rightText = initText;
                        }
                        newText = leftText + changedText + rightText;
                        pField.setText(newText);
                     }
                  }
               }
            }
            else if (e.getType().equals(DocumentEvent.EventType.REMOVE))
            {
               if (m_Translator != null)
               {
                  m_SelectionStartPos = pos;
                  m_SelectionEndPos = pos + length;
                  m_Translator.handleChar("");
               }
               else
               {
                  //process the remove of text
                  String leftText = "";
                  String rightText = "";
                  int nStart = pField.getTextStartPos();
                  //if this is the last character in this field that is removed,
                  //I cannot do proper substrings
                  if (pos >= nStart)
                  {
                     leftText = initText.substring(0, pos - nStart);
                  }
                  if (initText.length() >= (pos - nStart + length))
                  {
                     rightText = initText.substring(pos - nStart + length);
                  }
                  newText = leftText + rightText;
                  if (newText.length() == 0)
                  {
                     lastCharDeleted = true;
                  }
                  pField.setText(newText);
               }
            }

            if (!m_Modified)
            {
               setModified(true);
            }

            //I need to move all the fields after this field, so that
            //they have right Text and Field positions
            if (m_Translator != null)
            {
               m_Translator.updateFieldPositions(pField);
            }
         }
      }
      catch (Exception exc)
      {
         exc.printStackTrace();
      }
      finally
      {
         m_UpdatingField = false;
      }
   }

   DefaultStyledDocument doc = new DefaultStyledDocument();

   private void initComponents()
   {
      setBorder(null);
      m_Panel = new JPanel();
      m_Panel.setBorder(null);
      m_Panel.setLayout(null);
      m_Panel.setOpaque(false);
      //m_Panel.setPreferredSize(new Dimension(200, 3));

      m_TooltipMenu = new JPopupMenu();
      m_TooltipMenu.setBackground(m_TooltipBGColor);
      m_TooltipMenu.setOpaque(true);
      m_TooltipMenu.setBorder(LineBorder.createGrayLineBorder());
      m_TooltipMenu.setAlignmentX(0);
      m_TooltipMenu.setAlignmentY(0);
      m_TooltipMenu.setBounds(0, 0, 0, 0);

      setLayout(new BorderLayout());
      
      //TBD: see above, not al cases may be covered below
      
      if(m_IsMultiline == false)
      {
          JTextField field = new JTextField();
          field.setDocument(doc);
          m_Field = field;
          add(m_Field, BorderLayout.CENTER);      
      }
      else
       {
           JTextArea field = new JTextArea();
           field.setLineWrap(true);
           field.setWrapStyleWord(true);
           //field.setDocument(doc);
           m_Field = field;

           //Fix 132234
           // creating the vertical scrolling effect without the vertical scrollbar
           JScrollPane scrollPane = new JScrollPane(m_Field);
           scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
           scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
           scrollPane.setBorder(null);

           scrollPane.setVerticalScrollBar(new javax.swing.JScrollBar()
           {
               public java.awt.Dimension getPreferredSize()
               {
                   return new java.awt.Dimension(0, 0);
               }

               public java.awt.Dimension getMinimumSize()
               {
                   return new java.awt.Dimension(0, 0);
               }

               public java.awt.Dimension getMaximumSize()
               {
                   return new java.awt.Dimension(0, 0);
               }
           });

           add(scrollPane, BorderLayout.CENTER);
       }
      
      m_Field.setBorder(null);

      m_Button = new JButton();
      m_Button.setEnabled(true);
      m_Button.setPreferredSize(new Dimension(2, 2));
      m_Button.setOpaque(false);
      m_Button.setBackground(m_TooltipBGColor);
      m_Button.setFocusable(false); //I do not want this button to steal focus from the field
      final Point p = this.getLocation();
      final Component comp = this;
      m_Button.addActionListener(new ActionListener()
      {

         public void actionPerformed(ActionEvent e)
         {
            handleHint();
         }
      });
      
      m_Button.setBounds(0, 0, 15, 3);
      setOpaque(false);
      add(m_Panel, BorderLayout.SOUTH);
      m_Panel.setBounds(0, 0, 200, 3);
      m_Panel.setOpaque(false);
      
   }

   public void setCaretPosition(int pos)
   {
      m_Field.setCaretPosition(pos);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setAutoSize(boolean)
    */
   public void setAutoSize(boolean value)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getAutoSize()
    */
   public boolean getAutoSize()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setrefFont(java.awt.Font)
    */
   public void setrefFont(Font value)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setHFont(int)
    */
   public void setHFont(int value)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getBackColor()
    */
   public void getBackColor()
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setrefBackColor(long)
    */
   public void setrefBackColor(long value)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setBackColor(long)
    */
   public void setBackColor(long value)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getForeColor()
    */
   public Color getForeColor()
   {
      Color retVal = null;

      if (m_Field != null)
      {
         retVal = m_Field.getForeground();
      }

      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setForeColor(long)
    */
   public void setForeColor(Color value)
   {
      if (m_Field != null)
      {
         m_Field.setForeground(value);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setrefForeColor(long)
    */
   public void setrefForeColor(long value)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#activate(int, int)
    */
   public void activate(int KeyCode, int nPos)
   {
      // TODO Auto-generated method stub

   }

   /**
    * Deactivate the edit control
    */
   public void deactivate()
   {
      // prevent recursion
      boolean bInDeactivate = false;
      if (!bInDeactivate)
      {
         bInDeactivate = true;

         // notify listeners we're shutting down
         if (m_EventDispatcher != null)
         {
            IEditEventPayload payload = m_EventDispatcher.createEventPayload();
            if (payload != null)
            {
               String sText = getText();
               payload.setKey(m_LastKey);
               payload.setModified(getModified());
               payload.setText(sText);
            }
            m_EventDispatcher.fireDeactivate(this, payload);
            m_EventDispatcher = null;
         }

         // Fix for Sun issue #6185901:
         // When the edit control is completed it needs to remove the key bindings so that,
         // in this issue as an example, the paste operation is not handled by this edit control.

         // Remove our key bindings
         m_Field.getKeymap().removeBindings();         
      }
   }

   /**
    * Returns the translator attached to this control.
    *
    * @param pTranslator
    */
   public ITranslator getTranslator()
   {
      return m_Translator;
   }

   /**
    * Attaches a translator to this control.
    *
    * @param pTranslator
    */
   public void setTranslator(ITranslator pTranslator)
   {
      // set our translator then set reverse pointer
      m_Translator = pTranslator;

      m_Translator.setEditControl(this);
      String str = m_Field.getText();
      m_InitialData = str;
      m_Modified = false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getMultiline()
    */
   public boolean isMultiline()
   {
      return m_IsMultiline;
   }
//   
//   public void setMultiline(boolean value)
//   {
//       m_IsMultiline = value;
//   }
   

   public int getSelStartPos()
   {
      if (m_SelectionStartPos >= 0)
      {
         return m_SelectionStartPos;
      }
      else
      {
         return getCurrentPosition();
      }
   }

   public int getSelEndPos()
   {
      if (m_SelectionEndPos >= 0)
      {
         return m_SelectionEndPos;
      }
      else
      {
         return getCurrentPosition();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getSel(int, int)
    */
   public long getSel(int nStartChar, int nEndChar)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setSel(int, int)
    */
   public long setSel(int startPos, int endPos)
   {
      //if (m_Modified)
      {
         m_Field.setSelectionStart(startPos);
         m_Field.setSelectionEnd(endPos);
         m_SelectionStartPos = startPos;
         m_SelectionEndPos = endPos;
      }
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#replaceSel(java.lang.String)
    */
   public void replaceSel(String sText)
   {
      // TODO Auto-generated method stub

   }

   /**
    * Establishes the control's event dispatcher (used internally only).
    *
    * @param pDispatcher
    */
   public void putEventDispatcher(IEventDispatcher pDispatcher)
   {
      m_EventDispatcher = null;
      if (pDispatcher != null && pDispatcher instanceof IEditControlEventDispatcher)
      {
         m_EventDispatcher = (IEditControlEventDispatcher)pDispatcher;
      }
   }

   /**
    * Gets the control's event dispatcher (used internally only).
    *
    * @param pDispatcher
    */
   public IEventDispatcher getEventDispatcher()
   {
      return m_EventDispatcher;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getModified()
    */
   public boolean getModified()
   {
      return m_Modified;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setModified(boolean)
    */
   public void setModified(boolean value)
   {
      m_Modified = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#lineLength(int)
    */
   public int lineLength(int nLine)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#lineIndex(int)
    */
   public int lineIndex(int nLine)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getStyle()
    */
   public int getStyle()
   {

      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setStyle(int)
    */
   public void setStyle(long value)
   {
      if (m_Field instanceof JTextField)
      {
         JTextField field = (JTextField)m_Field;
         int alignment = JTextField.LEFT;
//         if ((value & IADEditableCompartment.RIGHT) == IADEditableCompartment.RIGHT)
//         {
//            alignment = JTextField.RIGHT;
//         }
//         else if ((value & IADCompartment.CENTER) == IADCompartment.CENTER)
//         {
//            alignment = JTextField.CENTER;
//         }

         field.setHorizontalAlignment(alignment);
      }
   }

   /**
    * Updates the control's window with the current contents of the translator.
    *
    * @return HRESULT
    */
   public void refresh()
   {
      if (m_Translator != null)
      {
         m_Translator.updateFieldPositions(null);
         String str = m_Translator.getCurrent();
         setText(str);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getOverstrike()
    */
   public boolean getOverstrike()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setOverstrike(boolean)
    */
   public void setOverstrike(boolean value)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getTooltipText(java.lang.StringBuffer, java.lang.StringBuffer, java.lang.StringBuffer)
    */
   public long getTooltipText(StringBuffer sLeft, StringBuffer sSubject, StringBuffer sRight)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    *
    * Update the tooltip's text.  If all 3 text parts are empty the tooltip window is dismissed.
    *
    * @param sLeft Left part of the tooltip text
    * @param sSubject Subject part, this will be displayed in a bold font
    * @param sRight Right part of the tooltip text.
    *
    * @return HRESULT
    *
    */
   public void setTooltipText(String sLeft, String sSubject, String sRight)
   {
      sLeft = StringUtilities.replaceAllSubstrings(sLeft, "<", "&lt;");
      sSubject = StringUtilities.replaceAllSubstrings(sSubject, "<", "&lt;");
      sRight = StringUtilities.replaceAllSubstrings(sRight, "<", "&lt;");
      sLeft = StringUtilities.replaceAllSubstrings(sLeft, ">", "&gt;");
      sSubject = StringUtilities.replaceAllSubstrings(sSubject, ">", "&gt;");
      sRight = StringUtilities.replaceAllSubstrings(sRight, ">", "&gt;");
      String tooltip = "<html>" + sLeft + "<b>" + sSubject + "</b>" + sRight + "</html>";

      //m_Field.setToolTipText(tooltip);
      m_TooltipText = tooltip;

      //tooltip = sLeft + "<b>" + sSubject + "</b>" + sRight;

      Point p = this.getLocation();
      if (m_LocationOnScreen != null)
      {
         p = m_LocationOnScreen;
      }
      m_TooltipMenu.removeAll();
      Font f = m_TooltipMenu.getFont();

      m_TooltipMenu.setFont(f.deriveFont(Font.PLAIN));
      JMenuItem item = m_TooltipMenu.add(tooltip);
      item.setFont(f.deriveFont(Font.PLAIN));
      item.setAlignmentX(0);
      item.setAlignmentY(0);
      item.setBorder(null);
      item.setBackground(m_TooltipBGColor);
      FontMetrics metrix = item.getFontMetrics(item.getFont());
      int width = metrix.stringWidth(tooltip);

      item.setOpaque(false);
      p = m_TooltipMenu.getLocation();
      if (p != null)
      {
         m_TooltipMenu.setBounds(p.x, p.y - metrix.getHeight(), width, metrix.getHeight());
         IEditControlField pField = m_Translator.getCurrentField();
         if (pField != null)
         {
            int nStart = pField.getTextStartPos();
            int nEnd = pField.getTextEndPos();
            int curPos = getCurrentPosition();

            if ((curPos == nStart) || (curPos == nEnd))
            {
               //There is potentially tooltip length change, so update its size
               //till I hide and reshow the tooltip, size does not change.
               //m_TooltipMenu.setVisible(false);
               //m_TooltipMenu.setVisible(true);
            }
         }

         //m_TooltipMenu.setPreferredSize(new Dimension(width,20));
         //m_TooltipMenu.updateUI();
      }

      //if (!m_TooltipMenu.isVisible())
      //{
      //	m_TooltipMenu.setVisible(true);
      //}
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setEnableTooltip(boolean)
    */
   public void setEnableTooltip(boolean value)
   {
      m_ShowTooltips = value;
      m_TooltipMenu.setVisible(value);
   }

   /**
    * Force the tooltip to be re-loaded and re-displayed.
    *
    * @return HRESULT
    */
   public void updateToolTip()
   {
      if (m_Translator != null)
      {
         if (m_ShowTooltips)
         {
            m_Translator.updateToolTip();
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setCurrentPosition(int)
    */
   public void setCurrentPosition(int value)
   {
      String text = m_Field.getText();
      if (text != null && text.length() >= value)
      {
         m_Field.setCaretPosition(value);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getCurrentPosition()
    */
   public int getCurrentPosition()
   {
      return m_Field.getCaretPosition();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getAutoExpand()
    */
   public boolean getAutoExpand()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#setAutoExpand(boolean)
    */
   public void setAutoExpand(boolean value)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#showHintBar(int)
    */
   public void showHintBar(int nPos)
   {
      FontMetrics metrix = m_Field.getFontMetrics(m_Field.getFont());
      String text = m_Field.getText();
      int length = 0;
      if (text != null && text.length() >= nPos)
      {
         String str = text.substring(0, nPos);
         length = metrix.stringWidth(str);
      }
      m_Panel.removeAll();
      m_Panel.setBounds(0, 0, 200, 3);
      m_Panel.repaint();
      m_Button.setBounds(length, 0, 15, 3);
      m_Panel.add(m_Button);
      if (m_BackgroundColor != null)
      {
         m_Panel.setForeground(m_BackgroundColor);
         m_Panel.setBackground(m_BackgroundColor);
         m_Panel.setUI(this.getUI());
      }
      m_Panel.invalidate();
      m_Panel.repaint();
   }

   /*
    * hides the hint bar
    */
   public void hideHintBar()
   {
      m_Panel.removeAll();
      m_Panel.invalidate();
      m_Panel.repaint();
      if (m_BackgroundColor != null)
      {
         m_Panel.setForeground(m_BackgroundColor);
         m_Panel.setBackground(m_BackgroundColor);
         m_Panel.setUI(this.getUI());
      }
      m_Panel.setBounds(0, 0, 200, 3);
      m_Panel.repaint();
      //m_Button.reshape(0,0,0,0);
      //m_Panel.add(m_Button);
   }

   /**
    * Deactivate the edit control and save any information
    * edited by user.
    */
   public void handleSave()
   {
      m_TooltipMenu.setVisible(false);
      boolean bModified = getModified();
      if (m_Translator != null && bModified)
      {
         m_Translator.saveModelElement();
      }

      //get related element and see if the change was really made
      //if the element name did not change we have a problem, in which case,
      //revert back to the old name
      String sText = getText();
      if (m_Translator != null)
      {
         Object object = m_Translator.getElement();
         if (object instanceof INamedElement)
         {
            INamedElement element = (INamedElement)object;
            String name = element.getName();
            if (!name.equals(sText))
            {
               setText(name);
            }
         }
      }
      
      // Fix for Sun issue #6185901:
      // When the edit control is completed it needs to deactivate so
      // that, in this issue as an example, the paste operation is
      // not handled by this edit control.
      // Before fixing this issue there was code to call
      // m_EventDispatcher.fireDeactivate(), but that is no longer necessary
      // since that code is in deactivate().

      // notify listeners we're shutting down
      deactivate();
   }

   public void handleRollback()
   {
      m_TooltipMenu.setVisible(false);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#handleHint()
    */
   public void handleHint()
   {
      if (m_Translator != null)
      {
         m_Translator.handleHint();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#displayList(boolean, com.embarcadero.describe.umlsupport.IStrings, int, java.lang.String)
    */
   public void displayList(boolean bList, IStrings pList, int nStart, String sInitialText)
   {
      if (pList != null)
      {
         long count = pList.getCount();
         JPopupMenu menu = new JPopupMenu();
         if (count > 0)
         {
            boolean addMoreItems = false;
            if (count > 25)
            {
               count = 25;
               addMoreItems = true;
               m_List = pList;
            }
            for (int i = 0; i < count; i++)
            {
               String str = pList.item(i);
               //menu.add(str);
               JMenuItem item = new JMenuItem(str);
               item.addActionListener(new ActionListener()
               {

                  public void actionPerformed(ActionEvent e)
                  {
                     performActionOnMenuItemSelected(e);
                  }
               });
               menu.add(item);
            }

            if (addMoreItems)
            {
               JMenuItem item = new JMenuItem(DrawingPropertyResource.getString("IDS_MOREITEMS"));
               item.addActionListener(new ActionListener()
               {

                  public void actionPerformed(ActionEvent e)
                  {
                     performActionOnMenuItemSelected(e);
                  }
               });
               menu.add(item);
            }
         }
         Point p = m_Button.getLocation();
         //menu.show(this, p.x, p.y+20);
         menu.show(m_Button, 0, 0);
      }
   }

   private String showSelectDialog()
   {
      String retVal = "";
      EditControlClassChooser chooser = new EditControlClassChooser(m_List);
      retVal = chooser.selectClass();
      return retVal;
   }

   public void performActionOnMenuItemSelected(ActionEvent e)
   {
      Object obj = e.getSource();
      if (obj != null)
      {
         String newText = ((JMenuItem)obj).getText();

         if (newText != null && newText.equals(DrawingPropertyResource.getString("IDS_MOREITEMS")))
         {
            //show a dialog to the user listing all the types found.
            newText = showSelectDialog();
         }

         if (m_Translator != null && newText != null && newText.length() > 0)
         {
            IEditControlField pField = m_Translator.handleHintText(newText);

            if (pField != null)
            {
               int nStart = pField.getTextStartPos();
               int nEnd = pField.getTextEndPos();
               //update the value on IEditControlField
               //pField.setText(newText);
               if (!m_Modified)
               {
                  setModified(true);
               }

               //also update the text on the Edit control display
               String currText = m_Field.getText();
               String textToDisplay = currText.substring(0, nStart) + newText + currText.substring(nEnd);

               //since I am going to set whole text for this edit field, I
               //somehow need to ignore update of the EditcontrolFields
               setText(textToDisplay);

               //I need to move all the fields after this field, so that
               //they have right Text and Field positions
               if (m_Translator != null)
               {
                  m_Translator.updateFieldPositions(pField);
               }

               //now I want to reset my cursor to the end of this field's end pos
               int newPos = pField.getTextEndPos();
               m_Field.setCaretPosition(newPos);
               setSel(newPos, newPos);
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getCurrentField()
    */
   public IEditControlField getCurrentField()
   {
      IEditControlField retField = null;
      if (m_Translator != null)
      {
         retField = m_Translator.getCurrentField();
      }
      return retField;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#registerAccelerator(int, long)
    */
   public void registerAccelerator(int nChar, long nModifier)
   {
      // TODO Auto-generated method stub

   }

   /**
    * Sets the element this edit control will edit.
    *
    * @param pElement [in] The edit control
    */
   public void setElement(IElement pElement)
   {
      // I need to block all the events at this point, so that while building property elements, we do not
      //fire events.
      boolean origVal = EventBlocker.startBlocking();
      try
      {
         //we do not want to show tooltips if we are editing a classifier or activity node or package.
         if (pElement instanceof IClassifier || pElement instanceof IActivityNode || pElement instanceof IPackage)
         {
            setEnableTooltip(false);
         }
         m_Translator = new TranslatorImpl();
         m_Translator.setElement(pElement);
         setTranslator(m_Translator);
         m_Translator.setEditControl(this);

         if (pElement instanceof IComment)
         {
            //this will select the comment text
            getCommentField();
         }
         else if (pElement instanceof ILifeline)
         {
            //this will select the comment text
            getRepClassifierField();
         }
         else
         {
            //this will select the name text.
            IEditControlField nameField = getNameField();
            if (nameField == null)
            {
               //we could not find a name field to select so select the first field.
               if (m_Translator != null)
               {
                  IEditControlField field = m_Translator.getNextField(null);
                  if (field != null)
                  {
                     int startPos = field.getTextStartPos();
                     int endPos = field.getTextEndPos();
                     setSel(startPos, endPos);
                  }
               }
            }
         }
         //m_Field.setCaretPosition(nameField.getTextEndPos());
      }
      finally
      {
         EventBlocker.stopBlocking(origVal);
      }
   }

   /**
    * A list of separator characters used by the fields
    * @param sList
    *
    * @return HRESULT
    */
   public String getSeparatorList()
   {
      return m_SeparatorList;
   }

   /**
    * A list of separator characters used by the fields
    * @param sList
    *
    * @return HRESULT
    */
   public void setSeparatorList(String sList)
   {
      m_SeparatorList = sList;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#calcNewPos(int)
    */
   public int calcNewPos(int changeLineBy)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getText()
    */
   public String getText()
   {
      // TODO Auto-generated method stub
      return m_Field.getText();
   }

   /*
    * sets the text that is displayed on the edit control.
    * While setting the text, change events are fired,
    * I need to prevent them so am setting ignoreTextUpdate.
    */
   public void setText(String value)
   {
      try
      {
         if (m_UpdatingField)
         {
            final String str = value;
            //we are right now updating the text, so do invokeLater
            SwingUtilities.invokeLater(new Runnable()
            {

               public void run()
               {
                  if (m_Translator != null)
                  {
                     IEditControlField pField = m_Translator.getCurrentField();
                     int pos = getCurrentPosition();
                     if (pField != null)
                     {
                        String name = pField.getName();
                        int nStart = pField.getLastTextStartPos();
                        if (nStart != pos)
                        {
                           pos = nStart;
                        }
                     }
                     m_IgnoreTextUpdate = true;
                     m_Field.setText(str);
                     m_IgnoreTextUpdate = false;
                     setCaretPosition(pos);
                  }
               }
            });

         }
         else
         {
            m_Field.setText(value);
            //select the name field
            getNameField();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void setCharacter(int nKeyCode, int nShift)
   {
      //if we are editing a lifeline we need to select the representing classifier.
      IEditControlField field = null;
      if (m_Translator != null)
      {
         IElement pEle = m_Translator.getElement();
         if (pEle != null && pEle instanceof ILifeline)
         {
            field = getRepClassifierField();
         }
      }
      if (field == null)
      {
         field = getNameField();
         if (field == null)
         {
            //try to see if this is a comment field
            field = getCommentField();

            if (field == null)
            {
               //we want to select the first field
               if (m_Translator != null)
               {
                  field = m_Translator.getNextField(null);
                  if (field != null)
                  {
                     int startPos = field.getTextStartPos();
                     int endPos = field.getTextEndPos();
                     setSel(startPos, endPos);
                  }
               }
            }
         }
      }

      if (field != null)
      {
         String str = "";
         if (nShift == 1)
         {
            str = String.valueOf(Character.toLowerCase((char)nKeyCode));
         }
         else
         {
            str = String.valueOf(Character.toString((char)nKeyCode));
         }

         ITranslator pTrans = getTranslator();
         int pos = field.getTextStartPos();
         if (pTrans != null)
         {
            pTrans.handleChar(str);
         }
         else
         {
            field.setText(str);
         }
         //m_Field.replaceSelection(str);

         //I want to set the selection start and end positions.
         pos += str.length();
         setSel(pos, pos);
         m_Field.setCaretPosition(pos);
      }
   }

   protected void assignAction(KeyStroke key, String actionName)
   {
       m_Field.getInputMap().put(key, actionName);
   }
   
   protected void assignAction(KeyStroke key, Action action, String actionName)
   {
       m_Field.getActionMap().put(actionName, action);
       m_Field.getInputMap().put(key, actionName);
   }
   
   protected void removeAction(KeyStroke key)
   {
       // The remove method only removes the key from this map.  If a parent
       // map owns the keystroke you have to go up the tree.
       
       InputMap map = m_Field.getInputMap();
       while(map != null)
       {
           map.remove(key);
           map = map.getParent();
       }
   }
   
   private IEditControlField getNameField()
   {
      IEditControlField field = null;
      if (m_Translator != null)
      {
         Vector fields = m_Translator.getTextFields();
         if (fields != null)
         {
            int count = fields.size();
            for (int i = 0; i < count; i++)
            {
               IEditControlField tempField = (IEditControlField)fields.get(i);
               IPropertyDefinition pDef = tempField.getPropertyDefinition();
               if (pDef != null)
               {
                  String name = pDef.getName();
                  if (name != null && name.equals("NameWithAlias"))
                  {
                     field = tempField;
                     break;
                  }
               }
            }

            if (field != null)
            {
               int startPos = field.getTextStartPos();
               int endPos = field.getTextEndPos();
               setSel(startPos, endPos);
            }
         }
      }
      return field;
   }

   private IEditControlField getRepClassifierField()
   {
      IEditControlField field = null;
      if (m_Translator != null)
      {
         Vector fields = m_Translator.getTextFields();
         if (fields != null)
         {
            int count = fields.size();
            for (int i = 0; i < count; i++)
            {
               IEditControlField tempField = (IEditControlField)fields.get(i);
               IPropertyDefinition pDef = tempField.getPropertyDefinition();
               if (pDef != null)
               {
                  String name = pDef.getName();
                  if (name != null && name.equals("RepresentingClassifier"))
                  {
                     field = tempField;
                     break;
                  }
               }
            }

            if (field != null)
            {
               int startPos = field.getTextStartPos();
               int endPos = field.getTextEndPos();
               setSel(startPos, endPos);
            }
         }
      }
      return field;
   }

   private IEditControlField getCommentField()
   {
      IEditControlField field = null;
      if (m_Translator != null)
      {
         Vector fields = m_Translator.getTextFields();
         if (fields != null)
         {
            int count = fields.size();
            for (int i = 0; i < count; i++)
            {
               IEditControlField tempField = (IEditControlField)fields.get(i);
               IPropertyDefinition pDef = tempField.getPropertyDefinition();
               if (pDef != null)
               {
                  String name = pDef.getName();
                  if (name != null && name.equals("Comment"))
                  {
                     field = tempField;
                     break;
                  }
               }
            }

            if (field != null)
            {
               int startPos = field.getTextStartPos();
               int endPos = field.getTextEndPos();
               setSel(startPos, endPos);
            }
         }
      }

      //if we are getting the comment field, we do not want to show the tooltips.
      if (field != null)
      {
         setEnableTooltip(false);
      }

      return field;
   }

   public boolean isShiftDown()
   {
      return m_ShiftDown;
   }

   public boolean isControlDown()
   {
      return m_ControlDown;
   }

   private class EditControlDocumentListener implements DocumentListener
   {
       InplaceEditorProvider.EditorController editorController = null;
       
      public EditControlDocumentListener(Object controller) 
      {
          if (controller  instanceof InplaceEditorProvider.EditorController)
          {
              editorController = (InplaceEditorProvider.EditorController) controller;
          }
      }
      public void insertUpdate(DocumentEvent e)
      {
          changedUpdate(e);
      }

      public void removeUpdate(DocumentEvent e)
      {
          changedUpdate(e);
      }

      public void changedUpdate(DocumentEvent e)
      {
//         handleFieldModified(e);
          setModified(true);
          m_Translator.setModified(true);
          getCurrentField().setText(m_Field.getText());
          if (editorController != null && editorController.isEditorVisible())
            {
                editorController.notifyEditorComponentBoundsChanged();
            }
      }
   }

   public void setEditControlBackground(Color c)
   {
      m_Field.setBackground(c);
      m_Panel.setBackground(c);
      m_Button.setBackground(c);
      this.setBackground(c);
      m_BackgroundColor = c;
   }

   /**
    * 
   */
   @Override
   public void setFont(Font value)
   {
      if (m_Field != null)
      {
         m_Field.setFont(value);
      }
   }

   /**
    * 
   */
   @Override
   public Font getFont()
   {
      Font retVal = null;
      if (m_Field != null)
      {
         retVal = m_Field.getFont();
      }

      return retVal;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl#getAssociatedParent()
    */
   public Object getAssociatedParent()
   {
      return (m_Parent != null) ? m_Parent.get() : null;
   }

   public void setAssociatedParent(Object parent)
   {
       if(parent != null)
       {
           m_Parent = new WeakReference( parent );
       }
   }
   
   /* (non-Javadoc)
    * @see java.awt.event.InputMethodListener#caretPositionChanged(java.awt.event.InputMethodEvent)
    */
   public void caretPositionChanged(InputMethodEvent event)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see java.awt.event.InputMethodListener# (java.awt.event.InputMethodEvent)
    */
   public void inputMethodTextChanged(InputMethodEvent event)
   {
       if (!ime_Cached) 
       {
	   ime_SelectionStartPos = m_Field.getSelectionStart();
	   ime_SelectionEndPos = m_Field.getSelectionEnd();
	   if (ime_SelectionStartPos != ime_SelectionEndPos) 
           {
	       ime_InitialLoc = Math.min(ime_SelectionStartPos, ime_SelectionEndPos);
	   } 
	   else 
	   {
	       ime_InitialLoc = m_Field.getCaretPosition();
	   }
	   ime_Cached = true;
	   ime_CachedChars = new StringBuffer();
       }

      int committedCharacterCount = event.getCommittedCharacterCount();
      CharacterIterator iter = event.getText();
      if (iter != null && ( (iter.getEndIndex() - iter.getBeginIndex()) > committedCharacterCount)) 
      {
	  if (ime_CachedChars != null) 
	  { 
	      char ch = iter.first();
	      for (int i = 0; i < committedCharacterCount; i++)
	      {	      
		  ime_CachedChars.append(ch);
		  ch = iter.next();
	      }
	  }
      } 
      else 
      {

	 // Push the caret back as part of faking the text input.
	 int pos = ime_InitialLoc;
	 setCurrentPosition(pos);
	 // Fake the key input since handleTypedChar() does the right things.
	   
	 if (ime_CachedChars != null) 
	 { 
	     for (int i = 0; i < ime_CachedChars.length() ; i++) 
	     {
		 handleTypedChar(ime_CachedChars.charAt(i));
		 setCurrentPosition(++pos);	       
	     }
	 }

	 if (iter != null) 
	 {
	     for (char ch = iter.first(); ch != CharacterIterator.DONE; ch = iter.next())
	     {
		 handleTypedChar(ch);
		 setCurrentPosition(++pos);
	     }
	 }	 
	 for(int i = ime_SelectionStartPos; i < ime_SelectionEndPos; i++) {
	     m_Translator.handleKeyDown(127);
	 }	   
	 ime_Cached = false;
	 ime_CachedChars = null;
        
	 event.consume();
       
         //commit the changes to the edit control
         setModified(true);
         m_Translator.setModified(true);
         if (getCurrentField() != null)
         {
            getCurrentField().setModified(true);
         }
         if (getAssociatedParent() != null)
         {
                if (getAssociatedParent() instanceof ProjectTreeCellEditor)
            {
               ((ProjectTreeCellEditor)getAssociatedParent()).stopCellEditing();
            }
         }
      }
   }
}
