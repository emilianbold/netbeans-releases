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
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerEvent;

import org.openide.awt.MouseUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.designer.WebForm.DefaultDesignerEvent;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * <p>
 * Editor for inline-editing text of components.
 * The UI for this is specified in
 *   http://ucsd.sfbay/projects/developer-tools/projects/RAVE/reef/UISpecs/designer-03.html
 * </p>
 * <p>
 * One of the complications of inline editing is that (as the above spec calls for)
 * it is invoked if you click on an already selected component. However, DOUBLE
 * clicking on such a component should instead open the default event handler of
 * the component. But note that a double click includes a single click, so if you
 * double click on a selected component you immediately enter inline
 * editing and the second click is then handled by the inline editor. At this point
 * it needs to see if this click is a double click that was initiated before inline
 * editing started, and if so, attempt to do event edits.
 * </p>
 *
 * @todo Support editing unescaped properties. Currently disabled in
 *    factory method, since the code which needs to identify the editable
 *    text underneath the wrapping elements (e.g. &lt;span&gt; for an output
 *    text) needs to be enhanced since the value no longer corresponds
 *    to a single text node (and entities have to be expanded, etc.) Also,
 *    the newline handling in Document.insertString needs to be fixed; it's
 *    currently going through the design time apis to add a &lt;br&gt; but
 *    that's not right for a temporary DocumentFragment model.
 *
 * @todo If you press Tab while editing a component with multiple inline editable
 *    properties, can I automatically switch to the next?
 * @todo The context menu should list each eligible property
 * @todo Clicking in an area should pick the appropriate inline editable property (if none,
 *   use the default)
 *
 * @author  Tor Norbye
 */
public abstract class InlineEditor {
//    /**
//     * Flag to turn off vb expression editing in inline editing mode
//     */
//    protected static final boolean NO_EDIT_VB_EXPR = !Boolean.getBoolean("rave.allow-vb-editing");
    protected WebForm webform;
//    protected MarkupDesignBean bean;
//    protected DesignProperty property;
//    protected Position begin;
//    protected Position end;
//    protected DomPosition begin;
//    protected DomPosition end;

    
    protected final DomProvider.InlineEditorSupport inlineEditorSupport;
//    protected String propertyName;

    /** Timestamp when we last entered inline editing. */
    private long editingStarted = 0L;

    /** Timestamp for the last mouse click received */
    private long lastClick = 0L;

//    protected InlineEditor(WebForm webform, MarkupDesignBean bean, String propertyName) {
    InlineEditor(WebForm webform,DomProvider.InlineEditorSupport inlineEditorSupport) {
        this.webform = webform;
//        this.bean = bean;
//        this.propertyName = propertyName;
//        this.property = property;
        
        this.inlineEditorSupport = inlineEditorSupport;

//        assert bean.getElement() != null;
    }

    /**
     * Return an inline editor for the given bean, if it supports one.
     * Returns null otherwise.
     *
     * @param webform The webform being edited
     * @param box The box corresponding to the component click, if any
     * @param bean The component to be inline edited
     * @param propertyName May be null; if so, either the property corresponding to the box clicked should be edited,
     *    or the default property provided allow default is true
     * @param useDefault If true, start editing the property marked default (if any) (marked with a "*" in the metadata)
     * @return An InlineEditor for editing the chosen property, or null if none apply
     */
    static InlineEditor getInlineEditor(WebForm webform, CssBox box, Element componentRootElement, /*DesignBean bean,*/
    String propertyName, boolean useDefault) {
//        if (!(bean instanceof MarkupDesignBean)) {
//            return null;
//        }
//
//        MarkupDesignBean markupBean = (MarkupDesignBean)bean;
        if (componentRootElement == null) {
            return null;
        }

//        String[] properties = getEditableProperties(markupBean);
        WebForm webForm = box == null ? null : box.getWebForm();
        if (webForm == null) {
            return null;
        }
        String[] properties = webForm.getDomProviderService().getEditableProperties(componentRootElement);

        if ((properties == null) || (properties.length == 0)) {
            return null;
        }

        InlineEditor editor = null;
        String[] propertyNames = new String[properties.length];
        String[] xpaths = new String[properties.length];
        int chosenProperty = -1;
        int matchDepth = Integer.MAX_VALUE;

        // Try to find out which of the properties (if more than one) is the
        // one we want - by seeing if the clicked box rendered under an element
        // corresponding to the xpaths for the properties
        if ((box != null) && (box.getElement() != null)) {
            Element boxElement = box.getElement();

            for (int i = 0; i < properties.length; i++) {
                String name = properties[i];
                int xpathIndex = name.indexOf(':');

                if (xpathIndex != -1) {
                    xpaths[i] = name.substring(xpathIndex + 1);
                    name = name.substring(0, xpathIndex);
                }

                if ((name.length() > 0) && (name.charAt(0) == '*')) {
                    name = name.substring(1);

                    if ((useDefault || (xpaths[i] == null)) && (chosenProperty == -1)) {
                        chosenProperty = i;
                    }
                }

                propertyNames[i] = name;

                // If a specific property was requested and this is it, mark
                // it as the target property, otherwise, if we've clicked at a particular
                // mouse position, see if that corresponds to this particular property
                if (useDefault) {
                    assert propertyName == null;
                } else if (propertyName != null) {
                    if (name.equals(propertyName)) {
                        chosenProperty = i;

                        break;
                    }
                } else if (xpaths[i] != null) {
//                    RaveElement sourceElement = (RaveElement)markupBean.getElement();
//                    RaveElement root = sourceElement.getRendered();
//                    Element sourceElement = markupBean.getElement();
//                    Element root = MarkupService.getRenderedElementForElement(sourceElement);
                    Element root = componentRootElement;
                    
                    if (root != null) {
//                        Node node = findPropertyNode(root, xpaths[i]);
                        Node node = webForm.getDomProviderService().findPropertyNode(root, xpaths[i]);

                        // Is boxElement under the given node?
                        Node curr = boxElement;
                        int depth = 0;

                        while (curr != null) {
                            if ((curr == node) && (depth < matchDepth)) {
                                chosenProperty = i;
                                matchDepth = depth;

                                break;
                            }

                            curr = curr.getParentNode();
                            depth++;
                        }
                    }
                }
            }
        }

        if (chosenProperty == -1) {
            if (!useDefault) {
                // #104462 No default inline editable property specified.
                // XXX #118293.
                warn("There is no value found for specified inline editable properties, properties=" + Arrays.asList(properties) // NOI18N
                        + ", component root element=" + componentRootElement); // NOI18N
            }
            return null;
        }

        try {
            String name = propertyNames[chosenProperty];
            String xpath = xpaths[chosenProperty];

//            DesignProperty property = bean.getProperty(name);
//
//            if (property == null) {
//                assert false : property;
//
//                return null;
//            }
            
//            DomProvider.InlineEditorSupport inlineEditorSupport = webform.createInlineEditorSupport(
//                    componentRootElement,
//                    name);
//            if (inlineEditorSupport == null) {
//                log(new NullPointerException("Missing inline editor support, componentRootElement=" + componentRootElement + ", propertyName=" + name)); // NOI18N
//                return null;
//            }
            

            HtmlTag tag = null;

            if (xpath != null) {
                // See if I can figure it out from the xpath expression
                if (xpath.startsWith("//")) {
                    int max = xpath.length();
                    int j = 2;

                    while ((j < max) && Character.isLetter(xpath.charAt(j))) {
                        j++;
                    }

                    tag = HtmlTag.getTag(xpath.substring(2, j));
                }
            }

            DomProvider.InlineEditorSupport inlineEditorSupport = webform.createInlineEditorSupport(componentRootElement, name, xpath);
            if (inlineEditorSupport == null) {
                log(new NullPointerException("Missing inline editor support, componentRootElement=" + componentRootElement + ", propertyName=" + name)); // NOI18N
                return null;
            }
            
            if ((tag == null) && (box != null) && (box.getElement() != null)) {
                tag = HtmlTag.getTag(box.getElement().getTagName());
            }

            if ((tag != null) && tag.isFormMemberTag()) {
//                editor = FormComponentEditor.get(webform, xpath, box, componentRootElement, /*markupBean, property,*/ inlineEditorSupport);
                editor = FormComponentEditor.get(webform, /*xpath,*/ box, componentRootElement, /*markupBean, property,*/ inlineEditorSupport);

                if (editor != null) {
                    return editor;
                }
            }

            editor = AttributeInlineEditor.get(webform, /*xpath,*/ componentRootElement, /*markupBean, property,*/ inlineEditorSupport);

            if (editor != null) {
                return editor;
            }

            // Consider TextInlineEditor here when implemented (for editing
            // direct JSP text inside html "components" like p, div etc.)
            return null;
        } finally {
            if (editor != null) {
                editor.editingStarted = System.currentTimeMillis();
                editor.lastClick = editor.editingStarted;
            }
        }
    }

//    /**
//     * If the given component supports inline text editing, return the
//     * String property name which stores the text that is inline
//     * editable.
//     */
//    public static String[] getEditablePropertyNames(DesignBean bean) {
////        BeanInfo bi = bean.getBeanInfo();
////
////        if (bi != null) {
////            BeanDescriptor bd = bi.getBeanDescriptor();
////            Object o = bd.getValue(Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES);
////
////            if (o instanceof String[]) {
////                String[] source = (String[])o;
//        String[] source = getEditableProperties(bean);
//        if (source != null) {
//            List names = new ArrayList(source.length);
//
//            for (int i = 0; i < source.length; i++) {
//                String name;
//                int index = source[i].indexOf(':');
//
//                if (index == -1) {
//                    if ((source.length > 0) && (source[i].charAt(0) == '*')) {
//                        name = source[i].substring(1);
//                    } else {
//                        name = source[i];
//                    }
//                } else {
//                    int start = 0;
//
//                    if ((source.length > 0) && (source[i].charAt(0) == '*')) {
//                        start = 1;
//                    }
//
//                    name = source[i].substring(start, index);
//                }
//
//                DesignProperty property = bean.getProperty(name);
//
//                if ((property != null) && isEditingAllowed(property)) {
//                    names.add(name);
//                }
//            }
//
//            return (String[])names.toArray(new String[names.size()]);
//        }
////        }
//
//        return new String[0];
//    }
//
//    private static String[] getEditableProperties(DesignBean bean) {
//        BeanInfo bi = bean.getBeanInfo();
//
//        if (bi != null) {
//            BeanDescriptor bd = bi.getBeanDescriptor();
//            Object o = bd.getValue(Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES);
//
//            if (o instanceof String[]) {
//                return (String[])o;
//            }
//        }
//
//        return null;
//    }
//
//    /**
//     * Return true if inline editing is allowed for the given property (assuming it
//     * has already been marked via metadata for inline editing; -that- is not checked here)
//     */
//    /*protected*/private static boolean isEditingAllowed(DesignProperty property) {
//        // TODO: Change types above from DesignProperty to FacesDesignProperty, and
//        // call property.isBound() instead of the below!
//        if (NO_EDIT_VB_EXPR) {
//            String value = property.getValueSource();
//
//            // TODO: Change types above from DesignProperty to FacesDesignProperty, and
//            // call property.isBound() instead of the below!
////            if ((value != null) && FacesSupport.isValueBindingExpression(value, false)) {
//            if ((value != null) && WebForm.getDomProviderService().isValueBindingExpression(value, false)) {
//                return false;
//            }
//        }
//
//        return true;
//    }

//    /**
//     * Return the given node corresponding to the given xpath.
//     * NOTE: The xpath parameter may actually contain multiple xpaths
//     * separated by colons.
//     * NOTE: Only a simple subset of XPATH is supported/implemented!!
//     * I support EXACTLY the following formats:
//     *    //tagname
//     *    //tagname[@attribute='value']
//     * and
//     *    /tagname1/tagname2/.../tagnameN
//     *    /tagname1/tagname2/.../tagnameN[@attribute='value']
//     * Note - combinations of these (e.g. //foo/bar[@baz='boo']/nei are not valid yet).
//     *
//     * @todo Hook up to xalan or other XPATH parser to get this working properly
//     */
//    protected static Node findPropertyNode(Node root, String xpaths) {
//        int next = 0;
//        int xpathsLength = xpaths.length();
//
//        while (next <= xpathsLength) {
//            String xpath;
//            int xpathEnd = xpaths.indexOf(':', next);
//
//            if (xpathEnd == -1) {
//                xpath = xpaths.substring(next);
//                next = xpathsLength + 1;
//            } else {
//                xpath = xpaths.substring(next, xpathEnd);
//                next = xpathEnd + 1;
//            }
//
//            // Dumb/simple parser algorithm for now
//            if (xpath.startsWith("//")) { // NOI18N
//
//                int length = xpath.length();
//                int begin = 2;
//                int end = begin;
//
//                while ((end < length) && Character.isLetter(xpath.charAt(end))) {
//                    end++;
//                }
//
//                String attributeName = null;
//                String attributeValue = null;
//                String tagName = xpath.substring(begin, end);
//
//                if ((end < length) && xpath.startsWith("[@", end)) { // NOI18N
//                    begin = end + 2;
//                    end = begin;
//
//                    while ((end < length) && Character.isLetter(xpath.charAt(end))) {
//                        end++;
//                    }
//
//                    attributeName = xpath.substring(begin, end);
//
//                    if ((end < length) && xpath.startsWith("='", end)) { // NOI18N
//                        begin = end + 2;
//                        end = begin;
//
//                        while ((end < length) && (xpath.charAt(end) != '\'')) {
//                            end++;
//                        }
//
//                        attributeValue = xpath.substring(begin, end);
//                        end++;
//                    }
//                }
//
//                //            if (end != length) {
//                //                // Looks like the xpath expession is not of the simple form used
//                //                // for most of our own components...  so do a fullblown
//                //                // xpath parse looking for the node instead...
//                //                // TODO
//                //            }
//                Element element = findElement(root, tagName, attributeName, attributeValue);
//
//                if (element != null) {
//                    return element;
//                }
//            } else {
//                info("Inline editing xpath expression not understood: " + xpath); // NOI18N
//            }
//        }
//
//        return null;
//    }
//
//    private static Element findElement(Node node, String tagName, String attribute, String value) {
//        if (node.getNodeType() == Node.ELEMENT_NODE) {
//            Element element = (Element)node;
//
//            if (element.getTagName().equals(tagName)) {
//                if (attribute != null) {
//                    if ((value == null) && element.hasAttribute(attribute)) {
//                        return element;
//                    } else if (element.getAttribute(attribute).indexOf(value) != -1) {
//                        //} else if (element.getAttribute(attribute).equals(value)) {
//                        // Match substring, not =: appropriate for class attribute only
//                        // PENDING: What is the correct xpath to express
//                        //   element e has a class attribute which INCLUDES substring foo?
//                        return element;
//                    }
//                } else {
//                    return element;
//                }
//            }
//        }
//
//        NodeList children = node.getChildNodes();
//
//        for (int i = 0, n = children.getLength(); i < n; i++) {
//            Node child = children.item(i);
//            Element element = findElement(child, tagName, attribute, value);
//
//            if (element != null) {
//                return element;
//            }
//        }
//
//        return null;
//    }

    /** Initiate inline editing.
     * @param selectText If true, select the text in the inline editing context,
     *    else it will simply put the caret at the end of the text range.
     * @param initialEdit If a non empty string, set the edited value to this string
     *    as if the user had typed the characters at the beginning of the editing
     *    operation.
     */
    public abstract void start(boolean selectText, String initialEdit);

    /** If the edited bean supports multiple fields to be inline edited, switch to
     * editing the next property
     * @param cancel If true, cancel the current value before cycling. If false, commit value.
     * @todo IMPLEMENT THIS
     */
    public void cycleEditableProperty(boolean cancel) {
    }

    /**
     * Stop inline editing. If cancel is true, throw away changes,
     * otherwise commit them.
     */
    abstract void finish(boolean cancel);

    /** Return true iff the box passed in is within the box being edited
     * by this inline editor.
     */
    public boolean isEdited(CssBox box) {
        while (box != null) {
//            if (box.getDesignBean() == bean) {
//            if (CssBox.getMarkupDesignBeanForCssBox(box) == bean) {
//            Element componentRootElement = WebForm.getDomProviderService().getRenderedElement(bean);
            Element componentRootElement = inlineEditorSupport.getRenderedElement();
            if (componentRootElement != null && componentRootElement == CssBox.getElementForComponentRootCssBox(box)) {
                return true;
            }

            box = box.getParent();
        }

        return false;
    }

    /**
     * Report the start position being edited in the inline editor
     * (or Position.NONE if we're not inline editing.)
     */
//    public Position getBegin() {
    public DomPosition getBegin() {
//        return begin;
        return inlineEditorSupport.getBeginPosition();
    }

    /**
     * Report the end position being edited in the inline editor
     * (or Position.NONE if we're not inline editing.)
     */
//    public Position getEnd() {
    public DomPosition getEnd() {
//        return end;
        return inlineEditorSupport.getEndPosition();
    }

    /*
     * Report whether the property being edited is "escaped" or not,
     * meaning should the text shown in the browser be literally
     * the same as in the value.
     */
    public abstract boolean isEscaped();

    /** Return true if this editor is editing document positions that our outside
     * of the document (e.g. typically in a rendered fragment). If this is the
     * case document positions won't be adjusted outwards to the nearest surrounding
     * document position.
     */
    public boolean isDocumentEditor() {
        return false;
    }

    /**
     * Report whether this inline editor is in multi line mode.
     * Pressing Enter in a multiline mode should add a <br/> element,
     * whereas in a single line editor it will complete input.
     */
    public abstract boolean isMultiLine();

    /** Paint the inline editing box */
    public void paint(Graphics2D g2d) {
        InteractionManager sm = webform.getManager();

        //Rectangle bounds = sm.getComponentBounds(bean);
        //if (bounds != null) {
//        ArrayList rectangles = webform.getMapper().getComponentRectangles(bean);
//        List rectangles = ModelViewMapper.getComponentRectangles(webform.getPane().getPageBox(), bean);
        List rectangles = ModelViewMapper.getComponentRectangles(webform.getPane().getPageBox(),
//                WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean(bean));
                inlineEditorSupport.getRenderedElement());
        int n = rectangles.size();

        if (n > 0) {
            Rectangle bounds = (Rectangle)rectangles.get(n - 1);

            //sm.drawSelected(g2d, true, false, bounds, Constants.ResizeConstraints.ANY);
            sm.drawInlineEditorBox(g2d, bounds);
        }
    }

    /** This method checks whether the given mouse event represents a double click
     * that occurred immediately in an inline editor - if so, we should cancel out
     * of inline editing and initiate event editing (if any) */
    protected void checkInitialDoubleClick(MouseEvent e) {
        // Lets say you have a selected button, and you want to
        // double click on it to go to its event handler. The first
        // click in the double click will cause inline editing to
        // be entered. The second click just arrived. We need to detect
        // that this is a double click that means our assumption about
        // inline editing was wrong - in other words that it appeared
        // shortly after the inline edit request, and if so
//        if (MouseUtils_RAVE.isDoubleClick(e) && (lastClick <= editingStarted)) {
        if (MouseUtils.isDoubleClick(e) && (lastClick <= editingStarted)) {
//            webform.getManager().notifyEditedDoubleClick();
            DesignerEvent evt = new DefaultDesignerEvent(webform, null);
            webform.fireUserActionPerformed(evt);
        }

        lastClick = e.getWhen();
    }

    /** Return true if the given position is a position within
     * this editor's range.
     */

    // TODO
//    public abstract boolean checkPosition(Position pos);
    public abstract boolean checkPosition(DomPosition pos);

    /** Get the currently edited & selected text in the editor */
    public abstract Transferable copyText(boolean cut);

    /** Return true iff this inline editor is editing the given property on the given bean */
    public boolean isEditing(/*DesignBean bean,*/Element componentRootElement, String propertyName) {
//        return ((bean == this.bean) &&
//        ((this.propertyName != null) && this.propertyName.equals(propertyName))) ||
//        (this.propertyName == propertyName);
//        String currPropertyName = property == null ? null : property.getPropertyDescriptor().getName();
        String currPropertyName = inlineEditorSupport.getName();
//        return ((bean == this.bean) &&
        return ((componentRootElement == inlineEditorSupport.getRenderedElement()) &&
        ((currPropertyName != null) && currPropertyName.equals(propertyName))) ||
        (currPropertyName == propertyName);
    }
    
    /** XXX Invokes delete next char action.
     * FIXME There shouldn't be an action invocation, but rather utility method call. */
    public abstract void invokeDeleteNextCharAction(ActionEvent evt);

    
    private static void log(Exception ex) {
        Logger logger = getLogger();
        logger.log(Level.INFO, null, ex);
    }
    
    private static void info(String message) {
        Logger logger = getLogger();
        logger.info(message);
    }
    
    private static void warn(String message) {
        Logger logger = getLogger();
        logger.warning(message);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(InlineEditor.class.getName());
    }
}
