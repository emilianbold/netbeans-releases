/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.designer;

import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import org.openide.ErrorManager;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.CustomButtonBox;
import org.netbeans.modules.visualweb.css2.FormComponentBox;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * Editor for form components like buttons, text fields and text areas. These
 * are similar to AttributeInlineEditor in that an attribute value is being
 * edited (e.g. "value" for a button) but unlike AttributeInlineEditor components
 * such as StaticText, these components also -render- to attributes rather than
 * text nodes.  Second, the visualization is a bit different; we cannot use the normal
 * caret support etc. since this content is not flow editable and we don't know
 * the internal position of a caret inside a text area for example. It uses an
 * inplace JTextField instead (or in the case of a text area, a JTextArea)
 *
 * @author  Tor Norbye
 */
class FormComponentEditor extends InlineEditor {
    /** Set whether we should try to place the text field on top of the regular component
     * and attempt to emulate its font and colors rather than using a text field below it.
     * (Text fields are always handled this way; this flag controls other components).
     */
    private static final boolean EDIT_ADJACENT = Boolean.getBoolean("rave.edit-adjacent");

    /** The component being added to the designpane (temporarily) as an editing component -
     * usually a JTextField or a JTextArea, but possibly wrapped inside a JScrollPane */
    protected JComponent inlineEditor;

    /** The actual text component serving as the editor - always a JTextComponent such as
     * a text field or a text area. This component may be wrapped in another in the
     * design surface (such as a scroll pane) */
    protected JTextComponent inlineTextEditor;
    private final Handler handler = new Handler();
    private CssBox box;
//    private String xpath;
    private boolean hasBeenEdited = false;
//    private DesignProperty property;

    private FormComponentEditor(WebForm webform, /*MarkupDesignBean bean, DesignProperty property,*/
    CssBox box/*, String xpath*/,DomProvider.InlineEditorSupport inlineEditorSupport) {
//        super(webform, bean, property.getPropertyDescriptor().getName());
        super(webform, /*bean,*/ inlineEditorSupport);
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + "()");
        }
        if(box == null) {
            throw(new IllegalArgumentException("Null CSS box."));
        }
//        this.property = property;
        this.box = box;
//        this.xpath = xpath;
    }

    public static FormComponentEditor get(WebForm webform, String xpath, CssBox box,
    Element componentRootElement,DomProvider.InlineEditorSupport inlineEditorSupport) {
        if (xpath != null) {
//            RaveElement sourceElement = (RaveElement)bean.getElement();
//            RaveElement root = sourceElement.getRendered();
//            Element sourceElement = bean.getElement();
//            Element root = MarkupService.getRenderedElementForElement(sourceElement);

            Element root = componentRootElement;
            if (root != null) {
                Node node = findPropertyNode(root, xpath);

                if ((node != null) && (node.getNodeType() == Node.ELEMENT_NODE)) {
                    Element element = (Element)node;
                    HtmlTag tag = HtmlTag.getTag(element.getTagName());

                    if ((tag != null) && tag.isFormMemberTag()) {
//                        CssBox b = CssBox.getBox(element);
                        CssBox b = webform.findCssBoxForElement(element);

                        if (b != null) {
                            box = b;
                        }
                    }
                }
            }
        }

        if (!(box instanceof FormComponentBox || box instanceof CustomButtonBox)) {
            return null;
        }

        // Skip input-hiddens
        if (box instanceof FormComponentBox && (((FormComponentBox)box).getComponent() == null)) {
            return null;
        }

//        if (!isEditingAllowed(property)) {
        if (!inlineEditorSupport.isEditingAllowed()) {
            return null;
        }

        // TODO - need metadata to specify the descriptor to use!
        return new FormComponentEditor(webform, /*bean, property,*/ box/*, xpath*/, inlineEditorSupport);
    }

    public void start(boolean selectText, String initialEdit) {
        finish(true); // is cancelling the previous edit the right thing to do here?

        JComponent component = null;
        Element element = null;

        if (box instanceof FormComponentBox) {
            FormComponentBox formComp = (FormComponentBox)box;
            component = formComp.getComponent();
            element = formComp.getElement();
        } else {
//            element = bean.getElement();
//
////            RaveElement xel = (RaveElement)element;
////            if (xel.getRendered() != null) {
////                element = xel.getRendered();
////            }
//            Element rendered = MarkupService.getRenderedElementForElement(element);
//            if (rendered != null) {
//                element = rendered;
//            }
            element = inlineEditorSupport.getRenderedElement();
        }

        int width;
        int height;
        boolean positionAdjacent = EDIT_ADJACENT;

        // XXX The font size should match the computed value including the stylesheet definitions.
        // See stripDesignStyleClasses below.
//        float fontSize = CssProvider.getValueService().getFontSizeForElement(element, DesignerSettings.getInstance().getDefaultFontSize());
        float fontSize = CssProvider.getValueService().getFontSizeForElement(element, webform.getDefaultFontSize());
        if (component instanceof JTextField) {
            // XXX #6491646 Don't use the rendered component for inline editing.
            // It has problems with fonts.
            JTextField renderedTextField = (JTextField)component;
//            JTextField inlineTextField = new JTextField(renderedTextField.getText(), renderedTextField.getColumns());
            JTextField inlineTextField = FormComponentBox.createTextField(renderedTextField.getText(), renderedTextField.getColumns());
            Font font = UIManager.getFont("TextField.font"); // NOI18N
            inlineTextField.setFont(font.deriveFont(fontSize));
            
//            inlineTextEditor = (JTextField)component;
            inlineTextEditor = inlineTextField;
            inlineEditor = inlineTextEditor;
            
            positionAdjacent = false;

            width = component.getWidth();
            height = component.getHeight();

            Dimension prefSize = inlineEditor.getPreferredSize();

            if (width < 10) {
                width = prefSize.width;
            }

            if (height < 8) {
                height = prefSize.height;
            }
        } else if (component instanceof JScrollPane &&
        ((JScrollPane)component).getViewport().getView() instanceof JTextArea) {
            // XXX #6491646 Don't use the rendered component for inline editing.
            // It has problems with fonts.
            JScrollPane renderedScrollPane = (JScrollPane)component;
            JTextArea renderedTextArea = (JTextArea)((JScrollPane)component).getViewport().getView();
//            JTextArea inlineTextArea = new JTextArea(renderedTextArea.getText(), renderedTextArea.getRows(), renderedTextArea.getColumns());
            JTextArea inlineTextArea = FormComponentBox.createTextArea(renderedTextArea.getText(), renderedTextArea.getRows(), renderedTextArea.getColumns());
            JScrollPane inlineScrollPane = new JScrollPane(inlineTextArea, renderedScrollPane.getVerticalScrollBarPolicy(), renderedScrollPane.getHorizontalScrollBarPolicy());
            Font font = UIManager.getFont("TextArea.font"); // NOI18N
            inlineTextArea.setFont(font.deriveFont(fontSize));

//            inlineEditor = component;
//            inlineTextEditor = (JTextArea)((JScrollPane)component).getViewport().getView();
            inlineEditor = inlineScrollPane;
            inlineTextEditor = inlineTextArea;
            positionAdjacent = false;

            width = component.getWidth();
            height = component.getHeight();

            Dimension prefSize = inlineEditor.getPreferredSize();

            if (width < 10) {
                width = prefSize.width;
            }

            if (height < 8) {
                height = prefSize.height;
            }
        } else {
//            JTextField field = new JTextField();
            JTextField field = FormComponentBox.createTextField();
            field.setColumns(9);
            inlineTextEditor = field;
            inlineEditor = inlineTextEditor;

            if (!positionAdjacent) { // For adjacent edits, use normal text field size, font, etc.


//                DocumentFragment fragment = webform.getDomSynchronizer().createSourceFragment(bean);
//                DocumentFragment fragment = webform.createSourceFragment(bean);
                DocumentFragment fragment = inlineEditorSupport.createSourceFragment();
                
                NodeList nl = fragment.getChildNodes();
                
                for (int i = 0, n = nl.getLength(); i < n; i++) {
                    Node child = nl.item(i);

                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element)child;

                        HtmlTag tag = HtmlTag.getTag(e.getTagName());

                        if ((tag != null) && tag.isFormMemberTag()) {
                            if (e.getAttribute(HtmlAttribute.TYPE).equals("hidden")) { // NOI18N

                                continue;
                            }

                            // XXX What is this for a hack? Revise.
                            DesignerUtils.stripDesignStyleClasses(fragment);
                            element = e;

                            break;
                        }
                    }
                }

//                Color bg = CssLookup.getColor(element, XhtmlCss.BACKGROUND_COLOR_INDEX);
//                Color fg = CssLookup.getColor(element, XhtmlCss.COLOR_INDEX);
                Color bg = CssProvider.getValueService().getColorForElement(element, XhtmlCss.BACKGROUND_COLOR_INDEX);
                Color fg = CssProvider.getValueService().getColorForElement(element, XhtmlCss.COLOR_INDEX);
//                Font font = CssLookup.getFont(element, fontSize);
//                Font font = CssProvider.getValueService().getFontForElement(element, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//                Font font = CssBoxUtilities.getDesignerFontForElement(element, initialEdit);
//                
//                // XXX The above returns the default size because of the stripped stylesheets. We need to adjust it to the original size.
//                font = font.deriveFont((float)fontSize);

                if (bg != null) { // XXX TODO: find other background from ancestor hierarchy?
                    inlineEditor.setBackground(bg);
                }

                if (fg != null) {
                    inlineEditor.setForeground(fg);
                }

//                if (font != null) {
//                    inlineEditor.setFont(font);
//                }
                // XXX #6461942 Don't change the font, it might not be able to show multibyte chars.
                // Now we adjust the size only.
                Font oldFont = inlineEditor.getFont();
                inlineEditor.setFont(oldFont.deriveFont(fontSize));
            }

            Dimension prefSize = inlineEditor.getPreferredSize();

            if (box != null) {
                width = box.getWidth();
                height = box.getHeight();
            } else {
//                width = CssLookup.getLength(element, XhtmlCss.WIDTH_INDEX);
//                height = CssLookup.getLength(element, XhtmlCss.HEIGHT_INDEX);
                width = CssUtilities.getCssLength(element, XhtmlCss.WIDTH_INDEX);
                height = CssUtilities.getCssLength(element, XhtmlCss.HEIGHT_INDEX);
            }

            if (width == CssBox.AUTO) {
                width = prefSize.width;
            }

            if (positionAdjacent || (height == CssBox.AUTO)) {
                height = prefSize.height;
            }
        }

//        String value = property.getValueSource();
        String value = inlineEditorSupport.getValueSource();

        if ((initialEdit != null) && (initialEdit.length() > 0)) {
            // If we have shadow text, replace the text. Otherwise, append at the end
            if (selectText) {
                // #6323571 It means the original text is replaced by the initial edit.
                value = initialEdit;
            } else {
                if (value == null) {
                    value = initialEdit;
                } else {
                    value = value + initialEdit;
                }
            }

            inlineTextEditor.setText(value);
        } else if (value != null) {
            inlineTextEditor.setText(value);

            if (selectText) {
                inlineTextEditor.selectAll();
            }
        } else {
            String tag = element.getTagName();

            // Start editing shadow text, if any
            if (tag.equals(HtmlTag.INPUT.name) || tag.equals(HtmlTag.TEXTAREA.name)) {
                inlineTextEditor.setText(element.getAttribute(HtmlAttribute.VALUE));

                // We want to set the shadow text as solid text even if the user just
                // hits Return
                hasBeenEdited = true;

                if (selectText) {
                    inlineTextEditor.selectAll();
                }
            } else {
                inlineTextEditor.setText("");
            }
        }

        if (inlineTextEditor instanceof JTextField) {
            ((JTextField)inlineTextEditor).addActionListener(handler);
        } else if (inlineTextEditor instanceof JTextArea) {
            ((JTextArea)inlineTextEditor).getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, 0), new EnterAction());
            ((JTextArea)inlineTextEditor).getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, InputEvent.CTRL_MASK), new FinishAction());
            ((JTextArea)inlineTextEditor).getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK), new FinishAction());
        }

        inlineTextEditor.addFocusListener(handler);
        inlineTextEditor.getDocument().addDocumentListener(handler);
        inlineTextEditor.addMouseListener(handler);

        DesignerPane pane = webform.getPane();
        pane.add(inlineEditor);

        int x = box.getAbsoluteX(); // TODO - adjust for borders?
        int y = box.getAbsoluteY();

        if (positionAdjacent) {
            y += box.getHeight();
        }

        Rectangle bounds = new Rectangle(x, y, width, height);

        inlineEditor.setBounds(bounds);
        inlineEditor.validate();
        pane.scrollRectToVisible(new Rectangle(bounds));
        pane.repaint();

        if (webform.getSelection().isNodeUpdatePending()) {
            // Ensure that we handle node updates before requesting focus
            // on the text field; otherwise the pending node update will do
            // its own focus grab which will take focus away from the textfield
            // (and since I listen for focus loss as commit, this will terminate
            // inline editing!)
            webform.getSelection().updateNodesImmediate();
        }

        inlineTextEditor.requestFocus();
    }

    public void finish(boolean cancel) {
        if (inlineEditor == null) {
            return;
        }

        JComponent oldEditor = inlineEditor;
        JTextComponent oldTextEditor = inlineTextEditor;

        // Don't apply changes if we haven't made any edits; that way,
        // we won't change a null value (rendered into "Text") into "Text"
        // which really has a "null" value
        if (hasBeenEdited && !cancel) {
            String value = getText();

            if ((value != null) && (value.length() == 0)) {
//                property.unset();
                inlineEditorSupport.unset();
            } else {
//                property.setValue(value);
                inlineEditorSupport.setValue(value);
            }
        }

        DesignerPane pane = webform.getPane();
        
        // XXX #6475780 Possible NPE.
        boolean requestFocus = (inlineTextEditor != null && inlineTextEditor.hasFocus()) || pane.hasFocus();
        
        inlineEditor = null;
        inlineTextEditor = null;
        pane.repaint();
        pane.remove(oldEditor);

        if (oldTextEditor instanceof JTextField) {
            ((JTextField)oldTextEditor).removeActionListener(handler);
        } else if (inlineTextEditor instanceof JTextArea) {
            ((JTextArea)inlineTextEditor).getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, 0));
            ((JTextArea)inlineTextEditor).getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK));
            ((JTextArea)inlineTextEditor).getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, InputEvent.CTRL_MASK));
        }

        // XXX #6475780 Possible NPE.
        if (oldTextEditor != null) {
            oldTextEditor.removeMouseListener(handler);
            oldTextEditor.removeFocusListener(handler);
            oldTextEditor.getDocument().removeDocumentListener(handler);
        }

        if (requestFocus) {
            pane.requestFocus();
        }
    }

    public boolean isDocumentEditor() {
        return false;
    }

    public boolean isEscaped() {
        return true;
    }

    public boolean isMultiLine() {
        return !isEscaped();
    }

//    public boolean checkPosition(Position pos) {
    public boolean checkPosition(DomPosition pos) {
        return true;
    }

    private String getText() {
        String value = inlineTextEditor.getText();

        if (isEscaped()) {
//            value =
//                    // <markup_separation>
////                MarkupServiceProvider.getDefault().expandHtmlEntities(value, false,
////                    bean.getElement());
//                    // ====
////                InSyncService.getProvider().expandHtmlEntities(value, false, bean.getElement());
//                WebForm.getDomProviderService().expandHtmlEntities(value, false, bean.getElement());
//                    // </markup_separation>
            value = inlineEditorSupport.expandHtmlEntities(value, false);
        }

        return value;
    }

    public Transferable copyText(boolean cut) {
        String text = inlineTextEditor.getSelectedText();

        Transferable transferable = new StringSelection(text);

        if (cut) {
            inlineTextEditor.replaceSelection("");
        }

        return transferable;
    }
    
    public void invokeDeleteNextCharAction(ActionEvent evt) {
        JTextComponent textComponent;
        if (inlineEditor instanceof JTextComponent) {
            textComponent = (JTextComponent)inlineEditor;
        } else if (inlineEditor instanceof JScrollPane
        && ((JScrollPane)inlineEditor).getViewport().getView() instanceof JTextComponent) {
            textComponent = (JTextComponent)((JScrollPane)inlineEditor).getViewport().getView();
        } else {
            textComponent = null;
        }
        
        if (textComponent == null) {
            return;
        }
        
        Action deleteAction = textComponent.getActionMap().get(
                DefaultEditorKit.deleteNextCharAction);
        if(deleteAction != null) {
            deleteAction.actionPerformed(evt);
        }
    }

    /**
     * Handler class for focus, action, mouse, document, etc. events on the embedded text field
     * or text area.
     */
    class Handler extends MouseInputAdapter implements FocusListener, ActionListener,
        DocumentListener {
        // -------------- implements ActionListener ------------------
        public void actionPerformed(ActionEvent e) {
            webform.getManager().finishInlineEditing(false);
        }

        // -------------- implements FocusListener ------------------
        public void focusGained(java.awt.event.FocusEvent evt) {
            //if (!evt.isTemporary()) {
            //    ((JTextComponent)evt.getComponent()).selectAll();
            //}
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
//            if (!evt.isTemporary()) {
//                webform.getManager().finishInlineEditing(false);
//            }
            // XXX See AttributeInlineEditor#AttributeInlineEditorFocusListener.
            if (!evt.isTemporary() || evt.getComponent() instanceof JRootPane) {
//                attributeInlineEditor.finish(false);
                webform.getManager().finishInlineEditing(false);
                evt.getComponent().removeFocusListener(this);
                return;
            }
            
            if (evt.isTemporary()) {
                Component oppositeComponent = evt.getOppositeComponent();
                // XXX #6480841 jdk transfering the focus to JRootPane when invoked popup.
                if (oppositeComponent instanceof JRootPane) {
                    oppositeComponent.addFocusListener(this);
                }
            }
        }

        // -------------- implements DocumentListener ------------------
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            hasBeenEdited = true;
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            hasBeenEdited = true;
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
        }

        // -------------- implements MouseListener / Extends MouseInputAdapter -------
        //public void mousePressed(java.awt.event.MouseEvent e) {
        //}
        public void mouseClicked(java.awt.event.MouseEvent e) {
            checkInitialDoubleClick(e);
        }

        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e);
            }
        }

        public void doPopup(MouseEvent e) {
//            Point p =
//                SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), inlineTextEditor);
//
//            org.openide.nodes.Node[] nodes = webform.getSelection().getSelectedNodes();
//            webform.getActions().createPopup((int)p.getX(), (int)p.getY(), inlineTextEditor, nodes,
//                false, null);

//            // #6442386
//            DesignerTopComp designerTC = webform.getTopComponent();
//            Point p =
//                SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), designerTC);
//            designerTC.showPopupMenu(p.x, p.y);
//            webform.tcShowPopupMenuForEvent(e);
            webform.fireUserPopupActionPerformed(new InteractionManager.DefaultDesignerPopupEvent(
                    webform,
                    e.getComponent(),
                    null,
                    null,
                    e.getX(),
                    e.getY()                    
            ));
        }
    }
    

    class FinishAction extends AbstractAction {
        public void actionPerformed(ActionEvent evt) {
            webform.getManager().finishInlineEditing(false);
        }
    }

    class EnterAction extends AbstractAction {
        public void actionPerformed(ActionEvent evt) {
            // XXX #6373507 Possible NPE.
            if (inlineTextEditor == null) {
                return;
            }
            
            javax.swing.text.Document doc = inlineTextEditor.getDocument();

            try {
                int caret = inlineTextEditor.getCaretPosition();
                doc.insertString(caret, "\n", null);
            } catch (BadLocationException ble) {
                ErrorManager.getDefault().notify(ble);
            }
        }
    }
}
