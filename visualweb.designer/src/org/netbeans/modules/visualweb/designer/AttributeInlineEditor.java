/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.visualweb.designer;

import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.text.DesignerPaneBase;
import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.Action;
import javax.swing.JRootPane;
import org.apache.xerces.dom.events.MutationEventImpl;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.SpaceBox;
import org.netbeans.modules.visualweb.css2.TextBox;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;


/**
 * Editor for inline-editing text of components where the "model" is
 * just an attribute on a component. For example, the JSF "HtmlOutputText"
 * component has the text stored in the "value" attribute; when you inline
 * edit the rendered text from the output text you are really editing
 * a DOM element attribute.
 *
 * For now this is only for JSF components.
 *
 * @todo Reinstate the <br> creation code in start()
 *
 * @author  Tor Norbye
 */
class AttributeInlineEditor extends InlineEditor /*implements org.w3c.dom.events.EventListener*/ {
    // XXX Moved to designer/jsf/../InlineEditorSupportImpl.
//    private boolean hasBeenEdited = false;
//    private DesignProperty property;
//    private DocumentFragment fragment;
    // XXX Moved to designer/jsf/../InlineEditorSupportImpl.
//    private FacesPageUnit facesPageUnit;
    // XXX Moved to designer/jsf/../InlineEditorSupportImpl.
//    private Node text;
//    private Node br;
    // XXX Moved to designer/jsf/../InlineEditorSupportImpl.
//    private String xpath;

    private final FocusListener focusListener = new AttributeInlineEditorFocusListener(this);


//    AttributeInlineEditor(WebForm webform, /*MarkupDesignBean bean, DesignProperty property,*/
//    String xpath,DomProvider.InlineEditorSupport inlineEditorSupport) {
    AttributeInlineEditor(WebForm webform, /*MarkupDesignBean bean, DesignProperty property,
    String xpath,*/DomProvider.InlineEditorSupport inlineEditorSupport) {
//        super(webform, bean, property.getPropertyDescriptor().getName());
//        super(webform, /*bean,*/ inlineEditorSupport);
//        this.property = property;
//        this.xpath = xpath;
        super(webform, /*bean,*/ inlineEditorSupport);
    }

//    public static AttributeInlineEditor get(WebForm webform, String xpath, Element componentRootElement,DomProvider.InlineEditorSupport inlineEditorSupport) {
    public static AttributeInlineEditor get(WebForm webform, /*String xpath,*/ Element componentRootElement,DomProvider.InlineEditorSupport inlineEditorSupport) {
//        if (!isEditingAllowed(property)) {
        if (!inlineEditorSupport.isEditingAllowed()) {
            return null;
        }

        // TEMPORARY RESTRICTION, not inherent. Gotta address
        // issues like how to identify the text node initially,
        // how to create linebreaks in the document fragment
        // and have them added to as DesignBeans (br is a DesignBean)
        // when committed, etc.
//        if (!isEscaped(bean)) {
        if (!isEscaped(webform, componentRootElement)) {
            // Don't support editing unescaped properties yet...
            return null;
        }

        // TODO: Ensure that the property is of String type?? I don't
        // support anything else... (and how could you inline text edit
        // anything else?)
//        return new AttributeInlineEditor(webform, /*bean, property,*/ xpath, inlineEditorSupport);
        return new AttributeInlineEditor(webform, /*bean, property, xpath,*/ inlineEditorSupport);
    }

    // XXX Moved to designer/jsf/../InlineEditorSupportImpl
//    /** Return the text node containing the value attribute.
//     * This is simplified. Later, what if you have an output text
//     * with this value: "<b>Hello World</b>". I won't find that; I need
//     * to search for the <b> node etc.
//     */
//    private Node findTextNode(Node root) {
//        assert root != null;
//
////        // String assumption should be checked in beandescriptor search for TEXT_NODE_PROPERTY,
////        // especially if we publish this property. Or we could at least specify that the
////        // property MUST be a String.
////        String value = (String)property.getValue();
//        String value = inlineEditorSupport.getValue();
//
//        // Search for the special node which contains the text.
//        // Special case empty... look for span with a CSS class of
//        // "rave-uninitialized-text".
//        if ((value == null) || (value.length() == 0)) {
//            // User has not set text; renderer may have done something
//            // like insert "Text" with a special CSS style so look for
//            // the element with that style and take its children to
//            // be the content to be edited.
//            // First see if we can find an eligible text node
//            Node text = findFirstTextNode(root);
//
//            if (text == null) {
//                text = findDesignStyleClass(root);
//            }
//
//            return text;
//        } else {
//            return findText(root, value);
//        }
//    }
//
//    private Node findFirstTextNode(Node node) {
//        if (node.getNodeType() == Node.TEXT_NODE) {
//            String s = node.getNodeValue();
//
//            if (!DesignerUtils.onlyWhitespace(s)) {
//                return node;
//            }
//        }
//
//        NodeList nl = node.getChildNodes();
//
//        for (int i = 0, n = nl.getLength(); i < n; i++) {
//            Node child = nl.item(i);
//
//            // Don't pick text children in JavaScript or CSS subtrees
//            if ((child.getNodeType() == Node.ELEMENT_NODE) &&
//                    (node.getNodeName().equals(HtmlTag.SCRIPT.name) ||
//                    node.getNodeName().equals(HtmlTag.STYLE.name))) {
//                continue;
//            }
//
//            Node match = findFirstTextNode(child);
//
//            if (match != null) {
//                return match;
//            }
//        }
//
//        return null;
//    }
//
//    private Node findDesignStyleClass(Node node) {
//        if (node.getNodeType() == Node.ELEMENT_NODE) {
//            Element element = (Element)node;
//
//            if (element.getAttribute(HtmlAttribute.CLASS).indexOf("rave-uninitialized-text") != -1) { // NOI18N
//
//                return node;
//            }
//        }
//
//        NodeList nl = node.getChildNodes();
//
//        for (int i = 0, n = nl.getLength(); i < n; i++) {
//            Node match = findDesignStyleClass(nl.item(i));
//
//            if (match != null) {
//                return match;
//            }
//        }
//
//        return null;
//    }
//
//    private Node findText(Node node, String value) {
//        //XXX rewrite this!!!
//        if (node.getNodeType() == Node.TEXT_NODE) {
//            String nodeText = node.getNodeValue();
//            int index = nodeText.indexOf(value);
//
//            if ((index != -1) && (value.length() > 0)) {
//                // Possibly split the text node node, in case it contains 
//                // "additional junk". For example, the table header will 
//                // render a row count into this text node too, which is not 
//                // part of the property value, so we don't want it included
//                // in our text editing efforts.
//                org.w3c.dom.Text text = (org.w3c.dom.Text)node;
//
//                if (text.getLength() != value.length()) {
//                    // Strip text before the value text
//                    if (index != 0) {
//                        node = text.splitText(index);
//                        text = (org.w3c.dom.Text)node;
//                    }
//
//                    // Strip off text after the value text
//                    if (text.getLength() > value.length()) {
//                        text.splitText(value.length());
//                    }
//                }
//
//                return node;
//            }
//        }
//
//        NodeList nl = node.getChildNodes();
//
//        for (int i = 0, n = nl.getLength(); i < n; i++) {
//            Node match = findText(nl.item(i), value);
//
//            if (match != null) {
//                return match;
//            }
//        }
//
//        return null;
//    }

    public void start(boolean selectText, String initialEdit) {
////        facesPageUnit = FacesSupport.getFacesUnit(webform.getModel().getLiveUnit());
////        facesPageUnit = getFacesUnit(webform.getModel().getLiveUnit());
////
////        if (facesPageUnit == null) {
////            return;
////        }
//
////        fragment = webform.getDomSynchronizer().createSourceFragment(bean);
////        fragment = webform.createSourceFragment(bean);
//        fragment = inlineEditorSupport.createSourceFragment();
//
//        if (fragment == null) {
//            return;
//        }
//
////        facesPageUnit.setPreRendered(bean, fragment);
////        if (!webform.setPrerenderedBean(bean, fragment)) {
//        if (!inlineEditorSupport.setPrerendered(fragment)) {
//            return;
//        }
//
//        // Select the text in the component and set the
//        // caret to the end
//        DesignerPane pane = webform.getPane();
//
////        if (pane.getCaret() == null) {
////            DesignerCaret dc = pane.getPaneUI().createCaret();
////            webform.getPane().setCaret(dc);
////        }
//        if (!pane.hasCaret()) {
//            pane.createCaret();
//        }
//
//        Node n = null;
//
//        if (xpath != null) {
////            n = findPropertyNode(fragment, xpath);
//            n = WebForm.getDomProviderService().findPropertyNode(fragment, xpath);
//
////            if ((n == null) && (property.getValueSource() == null)) {
//            if ((n == null) && (inlineEditorSupport.getValueSource() == null)) {
//                // It's possible that we're editing a property that hasn't
//                // resulted in any markup in the component yet if it's not
//                // set. For example, an attempt to edit the "label" property
//                // of a text field. This should edit xpath "//span", but there's
//                // no <span> rendered until label is set to something. So,
//                // if this is the case, set the property temporarily, render
//                // the fragment, and unset it.
////                String oldPropertyValue = (String)property.getValue();
//                String oldPropertyValue = inlineEditorSupport.getValue();
//
////                final String MARKER = property.getPropertyDescriptor().getDisplayName();
//                final String MARKER = inlineEditorSupport.getDisplayName();
//
////                try {
////                    Method m = property.getPropertyDescriptor().getWriteMethod();
////                    m.invoke(bean.getInstance(), new Object[] { MARKER });
//                    inlineEditorSupport.setViaWriteMethod(MARKER);
////                } catch (Exception ex) {
////                    ErrorManager.getDefault().notify(ex);
////                }
//
//                try {
////                    facesPageUnit.setPreRendered(null, null);
////                    webform.setPrerenderedBean(null, null);
//                    inlineEditorSupport.clearPrerendered();
//
//                    // XXX TODO There is not needed webform here.
////                    FileObject markupFile = webform.getModel().getMarkupFile();
////                    fragment = FacesSupport.renderHtml(markupFile, bean, false);
////                    fragment = InSyncService.getProvider().renderHtml(markupFile, bean);
////                    fragment = webform.renderHtmlForMarkupDesignBean(bean);
//                    fragment = inlineEditorSupport.renderDomFragment();
//                    // XXX Moved into the impl (of the above method).
////                    // XXX To get it into source document so it can work (Positions work only against source doc!).
////                    fragment = (DocumentFragment)webform.getJspDom().importNode(fragment, true);
//                    
////                    // XXX FIXME Is this correct here?
//                    // XXX Moving into designer/jsf/../InlineEditorSupport
////                    webform.updateErrorsInComponent();
//                    
////                    facesPageUnit.setPreRendered(bean, fragment);
////                    webform.setPrerenderedBean(bean, fragment);
//                    inlineEditorSupport.setPrerendered(fragment);
//                    
////                    n = findPropertyNode(fragment, xpath);
//                    n = WebForm.getDomProviderService().findPropertyNode(fragment, xpath);
//                    selectText = true;
////                    webform.getDomSynchronizer().requestChange(bean);
////                    webform.requestChange(bean);
//                    inlineEditorSupport.requestChange();
//                } finally {
////                    try {
////                        Method m = property.getPropertyDescriptor().getWriteMethod();
////                        m.invoke(bean.getInstance(), new Object[] { oldPropertyValue });
//                        inlineEditorSupport.setViaWriteMethod(oldPropertyValue);
////                    } catch (Exception ex) {
////                        ErrorManager.getDefault().notify(ex);
////                    }
//                }
//            }
//        }
//
//        if (n != null) {
//            // See if I can find a text node inside it
//            text = findTextNode(n);
//
//            if (text == null) {
//                text = n;
//            }
//        } else {
//            text = findTextNode(fragment);
//        }
//
//        if (text != null) {
//            // Put a line break at the end of the text to ensure that we have a possible caret position
//            // if the user erases all text
//            //// XXX unfortunately this leads to comparisons failing in LineBoxGroup.paintBackground
//            // so the selection winds up not getting painted. I've gotta investigate this.
//            //if (isEscaped()) {
//            //    br = text.getParentNode().appendChild(text.getOwnerDocument().createElement("br"));
//            //}
//            if (text.getNodeType() == Node.TEXT_NODE) {
////                begin = new Position(text, 0, Bias.FORWARD);
////                begin = Position.create(text, 0, Bias.FORWARD);
//                begin = webform.createDomPosition(text, 0, Bias.FORWARD);
//
////                if (br != null) {
////                    end = Position.create(br, false);
////                } else {
////                    end = new Position(text, text.getNodeValue().length(), Bias.BACKWARD);
////                end = Position.create(text, text.getNodeValue().length(), Bias.BACKWARD);
//                end = webform.createDomPosition(text, text.getNodeValue().length(), Bias.BACKWARD);
////                }
//            } else {
//                NodeList children = text.getChildNodes();
//
//                if (children.getLength() > 0) {
////                    begin = new Position(children.item(0), 0, Bias.FORWARD);
////                    begin = Position.create(children.item(0), 0, Bias.FORWARD);
//                    begin = webform.createDomPosition(children.item(0), 0, Bias.FORWARD);
//
////                    if (br != null) {
////                        end = Position.create(br, false);
////                    } else {
//                        Node last = children.item(children.getLength() - 1);
//
//                        if (last.getNodeType() == Node.TEXT_NODE) {
////                            end = new Position(last, last.getNodeValue().length(), Bias.BACKWARD);
////                            end = Position.create(last, last.getNodeValue().length(), Bias.BACKWARD);
//                            end = webform.createDomPosition(last, last.getNodeValue().length(), Bias.BACKWARD);
//                        } else {
////                            end = new Position(last, last.getChildNodes().getLength(), Bias.BACKWARD);
////                            end = Position.create(last, last.getChildNodes().getLength(), Bias.BACKWARD);
//                            end = webform.createDomPosition(last, last.getChildNodes().getLength(), Bias.BACKWARD);
//                        }
////                    }
//                } else {
////                    begin = new Position(text, 0, Bias.FORWARD);
////                    begin = Position.create(text, 0, Bias.FORWARD);
//                    begin = webform.createDomPosition(text, 0, Bias.FORWARD);
//
////                    if (br != null) {
////                        end = Position.create(br, false);
////                    } else {
////                        end = new Position(text, text.getChildNodes().getLength(), Bias.BACKWARD);
////                    end = Position.create(text, text.getChildNodes().getLength(), Bias.BACKWARD);
//                    end = webform.createDomPosition(text, text.getChildNodes().getLength(), Bias.BACKWARD);
////                    }
//                }
//            }
//        } else {
////            begin = new Position(fragment, 0, Bias.FORWARD);
////            end = new Position(fragment, fragment.getChildNodes().getLength(), Bias.FORWARD);
////            begin = Position.create(fragment, 0, Bias.FORWARD);
////            end = Position.create(fragment, fragment.getChildNodes().getLength(), Bias.FORWARD);
//            begin = webform.createDomPosition(fragment, 0, Bias.FORWARD);
//            end = webform.createDomPosition(fragment, fragment.getChildNodes().getLength(), Bias.FORWARD);
//        }
        selectText = inlineEditorSupport.prepareAttributeInlineEditor(selectText);

        // Select the text in the component and set the
        // caret to the end
        DesignerPane pane = webform.getPane();
//        if (pane.getCaret() == null) {
//            DesignerCaret dc = pane.getPaneUI().createCaret();
//            webform.getPane().setCaret(dc);
//        }
        if (!pane.hasCaret()) {
            pane.createCaret();
        }
        
         // XXX #94269 It seems it is working better withou these style juggling.
//        boolean changed = DesignerUtils.stripDesignStyleClasses(fragment);
//
////        NodeList children = fragment.getChildNodes();
////
////        for (int i = 0; i < children.getLength(); i++) {
////            Node child = children.item(i);
////
////            if (child.getNodeType() == Node.ELEMENT_NODE) {
//////                RaveElement e = (RaveElement)child;
////                Element e = (Element)child;
//////                CssLookup.getCssEngine(e).clearComputedStyles(e, null);
//////                CssProvider.getEngineService().clearComputedStylesForElement(e);
////                Element beanElement = bean.getElement();
////                // XXX #6489063 Inherit the style from the original element.
////                // Maybe there should be just the size of the font inherited.
////                CssProvider.getEngineService().setStyleParentForElement(e, beanElement);
////                
//////                e = e.getRendered();
////                e = MarkupService.getRenderedElementForElement(e);
////                Element beanRenderedElement = MarkupService.getRenderedElementForElement(beanElement);
////                if (e != null && beanRenderedElement != null) {
//////                    CssLookup.getCssEngine(e).clearComputedStyles(e, null);
//////                    CssProvider.getEngineService().clearComputedStylesForElement(e);
////                    // XXX #6489063 Inherit the style from the original element.
////                    // Maybe there should be just the size of the font inherited.
////                    CssProvider.getEngineService().setStyleParentForElement(e, beanRenderedElement);
////                }
////            }
////        }
//        inlineEditorSupport.setStyleParent(fragment);
//
//        registerDomListeners();

        boolean handled = false;

//        if (!NO_EDIT_VB_EXPR) {
//            String value = property.getValueSource();
//
////            if ((value != null) && FacesSupport.isValueBindingExpression(value, false)) {
//            if ((value != null) && WebForm.getDomProviderService().isValueBindingExpression(value, false)) {
//                // Show vb text in rendered view
//                pane.select(begin, end);
//                pane.getCaret().replaceSelection(value);
//                handled = true;
//            }
//        }
        DomPosition begin = inlineEditorSupport.getBeginPosition();
        DomPosition end = inlineEditorSupport.getEndPosition();
        
        String specialValue = inlineEditorSupport.getSpecialInitValue();
        if (specialValue != null) {
            // Show vb text in rendered view
            pane.select(begin, end);
//            pane.getCaret().replaceSelection(specialValue);
            pane.replaceSelection(specialValue);
            
            handled = true;
        }

        if (handled) {
            // Done
        } else if ((initialEdit != null) && (initialEdit.length() > 0)) {
//            String value = property.getValueSource();
            String value = inlineEditorSupport.getValueSource();

            if (value == null) {
                // Shadow text -- replace it completely
                pane.select(begin, end);
//                pane.getCaret().replaceSelection(initialEdit);
                pane.replaceSelection(initialEdit);
            } else {
                // User has already entered a value - simply append
//                pane.setCaretPosition(end);
                pane.setCaretDot(end);
//                pane.getCaret().replaceSelection(initialEdit);
                pane.replaceSelection(initialEdit);
            }
        } else if (selectText) {
            pane.select(begin, end);
        } else {
            pane.showCaret(end);
        }

//        if (changed) {
////            webform.getDomSynchronizer().requestChange(bean);
////            webform.requestChange(bean);
//            inlineEditorSupport.requestChange();
//        }

        // TODO Set up key bindings such that Ctrl-B will toggle bold,
        // Ctrl-I will toggle italics, Enter will insert a newline <br>,
        // etc
        
        // XXX #6431953.
        webform.getPane().addFocusListener(focusListener);
    }
    
//    // XXX Moved from FacesSupport.
//    /** Find the faces unit associated with the given context (if any) */
//    private static FacesPageUnit getFacesUnit(DesignContext context) {
//        LiveUnit lu = (LiveUnit)context;
//        BeansUnit bu = lu.getBeansUnit();
//
//        if (bu instanceof FacesPageUnit) {
//            FacesPageUnit fu = (FacesPageUnit)bu;
//
//            return fu;
//        }
//
//        return null;
//    }


    public void finish(boolean cancel) {
        // XXX #6431953.
        webform.getPane().removeFocusListener(focusListener);

        // XXX This logic should be moved somewhere else.
        // XXX in flow mode, gotta set it to some other valid position
        webform.getPane().setCaret(null);
        
////        if (facesPageUnit != null) {
////            facesPageUnit.setPreRendered(null, null);
////        }
////        webform.setPrerenderedBean(null, null);
//        inlineEditorSupport.clearPrerendered();
//
//        DocumentFragment fragment = inlineEditorSupport.getFragment();
//        if (fragment != null) {
//            unregisterDomListeners();
//        }
//
////        // XXX #6431953.
////        webform.getPane().removeFocusListener(focusListener);
////
////        // XXX This logic should be moved somewhere else.
////        // XXX in flow mode, gotta set it to some other valid position
////        webform.getPane().setCaret(null);
//
//        if (cancel) {
//            // Update component rendering the rendered html may contain the
//            // rendered value
////            webform.getDomSynchronizer().beanChanged(bean);
////            webform.beanChanged(bean);
//            inlineEditorSupport.beanChanged();
//
//            return; // don't apply value
//        }
//
//        // Don't apply changes if we haven't made any edits; that way,
//        // we won't change a null value (rendered into "Text") into "Text"
//        // which really has a "null" value
//        if (!hasBeenEdited) {
//            // Ensure that we re-render the component anyway
//            // such that the rendered-references are correct again
////            webform.getDomSynchronizer().requestChange(bean);
////            webform.requestChange(bean);
//            inlineEditorSupport.requestChange();
//
//            return;
//        }
//
//        // Determine what part of the fragment should be part of the
//        // value text!
//        // XXX for now use the whole enchilada!!
//        String value = null;
//        Node text = inlineEditorSupport.getText();
//        Node origin = text;
//
//        if (origin == null) {
//            origin = fragment;
//
//            if (origin == null) {
//                return;
//            }
//        } else {
//            if (origin.getNodeType() == Node.TEXT_NODE) {
//                //origin = origin.getParentNode();
//                value = text.getNodeValue();
//            }
//        }
//
//        if (value == null) {
//            // Get rid of our temporary newline
////            if (br != null) {
////                br.getParentNode().removeChild(br);
////            }
//
//            // Commit value
//            StringBuffer sb = new StringBuffer(300);
//            NodeList children = origin.getChildNodes();
//
//            for (int i = 0, n = children.getLength(); i < n; i++) {
//                Node child = children.item(i);
////                sb.append(InSyncService.getProvider().getHtmlStream(child));
//                sb.append(WebForm.getDomProviderService().getHtmlStream(child));
//            }
//
//            value = sb.toString();
//        }
//
//        if (isEscaped()) {
////            value =
////                    // <markup_separation>
//////                MarkupServiceProvider.getDefault().expandHtmlEntities(value, false,
//////                    bean.getElement());
////                    // ====
//////                InSyncService.getProvider().expandHtmlEntities(value, false, bean.getElement());
////                WebForm.getDomProviderService().expandHtmlEntities(value, false, bean.getElement());
////                // </markup_separation>\
//            value = inlineEditorSupport.expandHtmlEntities(value, false);
//        }
//
//        if ((value != null) && (value.length() == 0)) {
////            property.unset();
//            inlineEditorSupport.unset();
//        } else {
////            property.setValue(value);
//            inlineEditorSupport.setValue(value);
//        }
        inlineEditorSupport.cleanAttributeInlineEditor(cancel);
    }

    public boolean isDocumentEditor() {
        return true;
    }

    public boolean isEscaped() {
//        return isEscaped(bean);
        return inlineEditorSupport.isEscaped();
    }

    /** Determine if the given bean is escaped */
    private /*public*/ static boolean isEscaped(WebForm webForm, Element componentRootElement) {
//        // See if the bean looks like an output text that has escape
//        // turned off. If so, it's multiline. All others are considered
//        // single line.
//        if (bean.getInstance() instanceof javax.faces.component.html.HtmlOutputText) {
//            DesignProperty escape = bean.getProperty("escape"); // NOI18N
//
//            if (escape != null) {
//                Object o = escape.getValue();
//
//                if (o instanceof Boolean) {
//                    return ((Boolean)o).booleanValue();
//                }
//            }
//        }
//
//        return true;
        return webForm.getDomProviderService().isEscapedComponent(componentRootElement);
    }

    public boolean isMultiLine() {
        // See if the bean looks like an output text that has escape
        // turned off. If so, it's multiline. All others are considered
        // single line.
        return !isEscaped();
    }

    // XXX Moved to designer/jsf/../InlineEditorSupportImpl.
//    private void registerDomListeners() {
//        DocumentFragment fragment = inlineEditorSupport.getFragment();
//        if (fragment == null) {
//            return;
//        }
//
//        EventTarget target = (EventTarget)fragment;
//        EventListener adapter = this;
//        target.addEventListener(MutationEventImpl.DOM_ATTR_MODIFIED, adapter, false);
//
//        /* This event seems to be redundant.
//        target.addEventListener(MutationEventImpl.DOM_SUBTREE_MODIFIED, adapter, false);
//        */
//        target.addEventListener(MutationEventImpl.DOM_NODE_INSERTED, adapter, false);
//        target.addEventListener(MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT, adapter, false);
//        target.addEventListener(MutationEventImpl.DOM_NODE_REMOVED, adapter, false);
//        target.addEventListener(MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT, adapter, false);
//        target.addEventListener(MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED, adapter, false);
//
////        target.addEventListener(InSyncService.DOM_DOCUMENT_REPLACED, adapter, false);
//        target.addEventListener(WebForm.getDomProviderService().getDomDocumentReplacedEventConstant(), adapter, false);
//    }
//
//    private void unregisterDomListeners() {
//        DocumentFragment fragment = inlineEditorSupport.getFragment();
//        if (fragment == null) {
//            return;
//        }
//
//        EventTarget target = (EventTarget)fragment;
//        EventListener adapter = this;
//        target.removeEventListener(MutationEventImpl.DOM_ATTR_MODIFIED, adapter, false);
//
//        /* This event seems to be redundant.
//        target.removeEventListener(MutationEventImpl.DOM_SUBTREE_MODIFIED, adapter, false);
//        */
//        target.removeEventListener(MutationEventImpl.DOM_NODE_INSERTED, adapter, false);
//        target.removeEventListener(MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT, adapter, false);
//        target.removeEventListener(MutationEventImpl.DOM_NODE_REMOVED, adapter, false);
//        target.removeEventListener(MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT, adapter, false);
//        target.removeEventListener(MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED, adapter, false);
//
////        target.removeEventListener(InSyncService.DOM_DOCUMENT_REPLACED, adapter, false);
//        target.removeEventListener(WebForm.getDomProviderService().getDomDocumentReplacedEventConstant(), adapter, false);
//    }

//    public void handleEvent(final org.w3c.dom.events.Event e) {
//        hasBeenEdited = true;
//
//        if (e instanceof org.w3c.dom.events.MutationEvent) {
//            org.w3c.dom.events.MutationEvent me = (org.w3c.dom.events.MutationEvent)e;
//            String old = me.getPrevValue();
//            String nw = me.getNewValue();
//
//            if (((old != null) && (nw != null) && (old.equals(nw)))) {
//                return;
//            }
//        }
//
//        Node n = (Node)e.getTarget();
//
//        DomPosition end = inlineEditorSupport.getEndPosition();
//        if (n == end.getNode()) {
//            // XXX hack Don't do this, I should have Positions be
//            // immutable.
////            end.setOffset(n.getNodeValue().length());
////            end = new Position(end.getNode(), n.getNodeValue().length(), end.getBias());
////            end = Position.create(end.getNode(), n.getNodeValue().length(), end.getBias());
////            end = webform.createDomPosition(end.getNode(), n.getNodeValue().length(), end.getBias());
//            inlineEditorSupport.setEndPosition(webform.createDomPosition(end.getNode(), n.getNodeValue().length(), end.getBias()));
//        }
//
//        //        /*
//        //          Node node = (org.w3c.dom.Node)e.getTarget();
//        //          String type = e.getType();
//        //          Node parent = node.getParentNode(); // XXX or use getRelatedNode?
//        //
//        //        */
//        //        dispatchEvent(bean);
//        Node node = (org.w3c.dom.Node)e.getTarget();
//        Node parent = node.getParentNode(); // XXX or use getRelatedNode?
//
//        // Text node or entity node changes should get translated
//        // into a change event on their surrounding element...
//        // XXX I could possibly handle to rebreak only
//        // the LineBreakGroup.... That would save work -ESPECIALLY-
//        // for text right within the <body> tag... but optimize that
//        // later
//        if (!(node instanceof Element) || ((Element)node).getTagName().equals(HtmlTag.BR.name)) { // text, cdata, entity, ...
//            node = parent;
//            parent = parent.getParentNode();
//
//            if (node instanceof Element) {
////                MarkupDesignBean b = ((RaveElement)node).getDesignBean();
////                MarkupDesignBean b = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)node);
//                MarkupDesignBean b = WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)node);
//
//                if (b == null) {
//                    b = bean;
//                }
//
////                webform.getDomSynchronizer().requestTextUpdate(b);
//                webform.requestTextUpdate(b);
//            }
//        } else {
////            webform.getDomSynchronizer().requestChange(bean);
//            webform.requestChange(bean);
//        }
//        inlineEditorSupport.handleEvent(e);
//    }

    //    /** Good for changes only
    //     ** @todo Rename dispatchBeanChangeEvent */
    //    private void dispatchEvent(DesignBean bean) {
    //
    //        // Gotta walk up to the most distant ancestor below the form tag
    //        // E.g. if you change a command button that's in a data table,
    //        // we need to re-render the entire data table, not just the button,
    //        // since the button is replicated in every cell!
    //        FacesPageUnit facesUnit = webform.getModel().getFacesUnit();
    //        MarkupBean formBean = facesUnit.getDefaultParent();
    //
    //        DesignBean originalBean = bean;
    //        DesignBean parent = bean.getBeanParent();
    //        while (parent != null && FacesSupport.getFacesBean(parent) != formBean) {
    //            bean = parent;
    //            parent = parent.getBeanParent();
    //        }
    //        if (bean == webform.getModel().getRootBean()) {
    //            // Looks like the bean was not below the form!
    //            bean = originalBean;
    //        }
    //
    //        //final Node element = text;
    //        final Element element = FacesSupport.getElement(bean);
    //        if (element == null) {
    //            Thread.dumpStack();
    //            return;
    //        }
    //        SwingUtilities.invokeLater(new Runnable() {
    //            public void run() {
    //                org.w3c.dom.Node parent = element.getParentNode();
    //                //fireChangedUpdate(fElement, EventType.CHANGE, parent, false, null);
    //                //docListener.changedUpdate(fElement, Document.EventType.CHANGE, parent, false, null);
    //                PageBox pageBox = webform.getSelection().getPageBox();
    //                if (pageBox != null) {
    //                    pageBox.changed(element, parent, false);
    //                }
    //            }
    //        }
    //        );
    //    }
//    public boolean checkPosition(Position pos) {
    public boolean checkPosition(DomPosition pos) {
        // The TextInlineEditor ought to do this instead:
        // return !pos.isRendered()
        // Positions can be in the document fragment! Actually, I
        // ought to check that it is
        Node node = pos.getNode();

        if (node == null) {
            return false;
        }

//        if (pos.isRendered()) {
//        if (MarkupService.isRenderedNode(pos.getNode())) {
        if (webform.isRenderedNode(pos.getNode())) {
            pos = pos.getSourcePosition();
            node = pos.getNode();
        }

        if (node == null) {
            return false;
        }

        Node parent = node.getParentNode();

        DocumentFragment fragment = inlineEditorSupport.getFragment();
        while (parent != null) {
            if (parent == fragment) {
                return true;
            }

            parent = parent.getParentNode();
        }

        return false;
    }

    public Transferable copyText(boolean cut) {
//        DesignerCaret caret = webform.getPane().getCaret();
//        if (caret != null) {
//            return caret.copySelection(cut);
//        }
        DesignerPane pane = webform.getPane();
        if (pane.hasCaret()) {
            return pane.copySelection(cut);
        }

        return null;
    }
    
    public void invokeDeleteNextCharAction(ActionEvent evt) {
        DesignerPane designerPane = webform.getPane();
        Action[] actions = designerPane.getActions();
        for (Action action : actions) {
            if (action != null && action.getValue(Action.NAME) == DesignerPaneBase.deleteNextCharAction) {
                // XXX Pretend the event originated from the DesignerPane, otherwise the action wouldn't work.
                ActionEvent fakeEvt = new ActionEvent(
                        designerPane,
                        evt.getID(),
                        evt.getActionCommand(),
                        evt.getWhen(),
                        evt.getModifiers());
                action.actionPerformed(fakeEvt);
                break;
            }
        }
    }
    
    
    /** XXX #6431953 When losing focus, the inline editing is finishing.
     * XXX All this just tries to impl the strange Leopard behaviour, when popup is invoked
     * the inline editing is not finished, but only when some action from the popup is invoked it is finished.
     * It needs to be revisited by HIE. */
    private static class AttributeInlineEditorFocusListener implements FocusListener {
        
        private final AttributeInlineEditor attributeInlineEditor;
        
        public AttributeInlineEditorFocusListener(AttributeInlineEditor attributeInlineEditor) {
            this.attributeInlineEditor = attributeInlineEditor;
        }
        
        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            if (!e.isTemporary() || e.getComponent() instanceof JRootPane) {
//                attributeInlineEditor.finish(false);
                attributeInlineEditor.webform.getManager().finishInlineEditing(false);
                e.getComponent().removeFocusListener(this);
                return;
            }
            
            if (e.isTemporary()) {
                Component oppositeComponent = e.getOppositeComponent();
                // XXX #6480841 jdk transfering the focus to JRootPane when invoked popup.
                if (oppositeComponent instanceof JRootPane) {
                    oppositeComponent.addFocusListener(this);
                }
            }
        }
    } // End of AttributeInlineEditorFocusListener.
    
    @Override
    public boolean isEdited(CssBox box) {
        // XXX #94103 Comparing exactly against the text node edited.
        // For now checked against textual boxes (TextBox, SpaceBox) only,
        // that should be enough.
        Node boxText;
        if (box instanceof TextBox) {
            boxText = ((TextBox)box).getNode();
        } else if (box instanceof SpaceBox) {
            boxText = ((SpaceBox)box).getNode();
        } else {
            return false;
        }
        
        Node text = inlineEditorSupport.getText();
        // XXX text is source node.
        Node editedRenderedText = MarkupService.getRenderedNodeForNode(text);
        if (editedRenderedText == null) {
            return false;
        }
        
        while (boxText != null) {
            if (boxText == editedRenderedText) {
                return true;
            }
            boxText = boxText.getParentNode();
        }
        return false;
    }
}
