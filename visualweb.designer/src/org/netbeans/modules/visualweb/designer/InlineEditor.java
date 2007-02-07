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

import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.List;

import org.openide.ErrorManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.css2.CssBox;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.extension.openide.awt.MouseUtils_RAVE;
import com.sun.rave.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.text.Position;


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
    /**
     * Flag to turn off vb expression editing in inline editing mode
     */
    protected static final boolean NO_EDIT_VB_EXPR = !Boolean.getBoolean("rave.allow-vb-editing");
    protected WebForm webform;
    protected MarkupDesignBean bean;
    protected Position begin;
    protected Position end;
    protected String propertyName;

    /** Timestamp when we last entered inline editing. */
    private long editingStarted = 0L;

    /** Timestamp for the last mouse click received */
    private long lastClick = 0L;

    protected InlineEditor(WebForm webform, MarkupDesignBean bean, String propertyName) {
        this.webform = webform;
        this.bean = bean;
        this.propertyName = propertyName;

        assert bean.getElement() != null;
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
    static InlineEditor getInlineEditor(WebForm webform, CssBox box, DesignBean bean,
        String propertyName, boolean useDefault) {
        if (!(bean instanceof MarkupDesignBean)) {
            return null;
        }

        MarkupDesignBean markupBean = (MarkupDesignBean)bean;

        String[] properties = getEditableProperties(markupBean);

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
                    Element sourceElement = markupBean.getElement();
                    Element root = MarkupService.getRenderedElementForElement(sourceElement);
                    
                    if (root != null) {
                        Node node = findPropertyNode(root, xpaths[i]);

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
            return null;
        }

        try {
            String name = propertyNames[chosenProperty];
            String xpath = xpaths[chosenProperty];

            DesignProperty property = bean.getProperty(name);

            if (property == null) {
                assert false : property;

                return null;
            }

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

            if ((tag == null) && (box != null) && (box.getElement() != null)) {
                tag = HtmlTag.getTag(box.getElement().getTagName());
            }

            if ((tag != null) && tag.isFormMemberTag()) {
                editor = FormComponentEditor.get(webform, xpath, box, markupBean, property);

                if (editor != null) {
                    return editor;
                }
            }

            editor = AttributeInlineEditor.get(webform, xpath, markupBean, property);

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

    /**
     * If the given component supports inline text editing, return the
     * String property name which stores the text that is inline
     * editable.
     */
    public static String[] getEditablePropertyNames(DesignBean bean) {
//        BeanInfo bi = bean.getBeanInfo();
//
//        if (bi != null) {
//            BeanDescriptor bd = bi.getBeanDescriptor();
//            Object o = bd.getValue(Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES);
//
//            if (o instanceof String[]) {
//                String[] source = (String[])o;
        String[] source = getEditableProperties(bean);
        if (source != null) {
            List names = new ArrayList(source.length);

            for (int i = 0; i < source.length; i++) {
                String name;
                int index = source[i].indexOf(':');

                if (index == -1) {
                    if ((source.length > 0) && (source[i].charAt(0) == '*')) {
                        name = source[i].substring(1);
                    } else {
                        name = source[i];
                    }
                } else {
                    int start = 0;

                    if ((source.length > 0) && (source[i].charAt(0) == '*')) {
                        start = 1;
                    }

                    name = source[i].substring(start, index);
                }

                DesignProperty property = bean.getProperty(name);

                if ((property != null) && isEditingAllowed(property)) {
                    names.add(name);
                }
            }

            return (String[])names.toArray(new String[names.size()]);
        }
//        }

        return new String[0];
    }

    static String[] getEditableProperties(DesignBean bean) {
        BeanInfo bi = bean.getBeanInfo();

        if (bi != null) {
            BeanDescriptor bd = bi.getBeanDescriptor();
            Object o = bd.getValue(Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES);

            if (o instanceof String[]) {
                return (String[])o;
            }
        }

        return null;
    }

    /**
     * Return true if inline editing is allowed for the given property (assuming it
     * has already been marked via metadata for inline editing; -that- is not checked here)
     */
    protected static boolean isEditingAllowed(DesignProperty property) {
        // TODO: Change types above from DesignProperty to FacesDesignProperty, and
        // call property.isBound() instead of the below!
        if (NO_EDIT_VB_EXPR) {
            String value = property.getValueSource();

            // TODO: Change types above from DesignProperty to FacesDesignProperty, and
            // call property.isBound() instead of the below!
//            if ((value != null) && FacesSupport.isValueBindingExpression(value, false)) {
            if ((value != null) && WebForm.getHtmlDomProviderService().isValueBindingExpression(value, false)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return the given node corresponding to the given xpath.
     * NOTE: The xpath parameter may actually contain multiple xpaths
     * separated by colons.
     * NOTE: Only a simple subset of XPATH is supported/implemented!!
     * I support EXACTLY the following formats:
     *    //tagname
     *    //tagname[@attribute='value']
     * and
     *    /tagname1/tagname2/.../tagnameN
     *    /tagname1/tagname2/.../tagnameN[@attribute='value']
     * Note - combinations of these (e.g. //foo/bar[@baz='boo']/nei are not valid yet).
     *
     * @todo Hook up to xalan or other XPATH parser to get this working properly
     */
    protected static Node findPropertyNode(Node root, String xpaths) {
        int next = 0;
        int xpathsLength = xpaths.length();

        while (next <= xpathsLength) {
            String xpath;
            int xpathEnd = xpaths.indexOf(':', next);

            if (xpathEnd == -1) {
                xpath = xpaths.substring(next);
                next = xpathsLength + 1;
            } else {
                xpath = xpaths.substring(next, xpathEnd);
                next = xpathEnd + 1;
            }

            // Dumb/simple parser algorithm for now
            if (xpath.startsWith("//")) { // NOI18N

                int length = xpath.length();
                int begin = 2;
                int end = begin;

                while ((end < length) && Character.isLetter(xpath.charAt(end))) {
                    end++;
                }

                String attributeName = null;
                String attributeValue = null;
                String tagName = xpath.substring(begin, end);

                if ((end < length) && xpath.startsWith("[@", end)) { // NOI18N
                    begin = end + 2;
                    end = begin;

                    while ((end < length) && Character.isLetter(xpath.charAt(end))) {
                        end++;
                    }

                    attributeName = xpath.substring(begin, end);

                    if ((end < length) && xpath.startsWith("='", end)) { // NOI18N
                        begin = end + 2;
                        end = begin;

                        while ((end < length) && (xpath.charAt(end) != '\'')) {
                            end++;
                        }

                        attributeValue = xpath.substring(begin, end);
                        end++;
                    }
                }

                //            if (end != length) {
                //                // Looks like the xpath expession is not of the simple form used
                //                // for most of our own components...  so do a fullblown
                //                // xpath parse looking for the node instead...
                //                // TODO
                //            }
                Element element = findElement(root, tagName, attributeName, attributeValue);

                if (element != null) {
                    return element;
                }
            } else {
                ErrorManager.getDefault().log("Inline editing xpath expression not understood: " + // NOI18N
                    xpath);
            }
        }

        return null;
    }

    private static Element findElement(Node node, String tagName, String attribute, String value) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element)node;

            if (element.getTagName().equals(tagName)) {
                if (attribute != null) {
                    if ((value == null) && element.hasAttribute(attribute)) {
                        return element;
                    } else if (element.getAttribute(attribute).indexOf(value) != -1) {
                        //} else if (element.getAttribute(attribute).equals(value)) {
                        // Match substring, not =: appropriate for class attribute only
                        // PENDING: What is the correct xpath to express
                        //   element e has a class attribute which INCLUDES substring foo?
                        return element;
                    }
                } else {
                    return element;
                }
            }
        }

        NodeList children = node.getChildNodes();

        for (int i = 0, n = children.getLength(); i < n; i++) {
            Node child = children.item(i);
            Element element = findElement(child, tagName, attribute, value);

            if (element != null) {
                return element;
            }
        }

        return null;
    }

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
            Element componentRootElement = WebForm.getHtmlDomProviderService().getRenderedElement(bean);
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
    public Position getBegin() {
        return begin;
    }

    /**
     * Report the end position being edited in the inline editor
     * (or Position.NONE if we're not inline editing.)
     */
    public Position getEnd() {
        return end;
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
                WebForm.getHtmlDomProviderService().getComponentRootElementForMarkupDesignBean(bean));
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
        if (MouseUtils_RAVE.isDoubleClick(e) && (lastClick <= editingStarted)) {
            webform.getManager().notifyEditedDoubleClick();
        }

        lastClick = e.getWhen();
    }

    /** Return true if the given position is a position within
     * this editor's range.
     */

    // TODO
    public abstract boolean checkPosition(Position pos);

    /** Get the currently edited & selected text in the editor */
    public abstract Transferable copyText(boolean cut);

    /** Return true iff this inline editor is editing the given property on the given bean */
    public boolean isEditing(DesignBean bean, String propertyName) {
        return ((bean == this.bean) &&
        ((this.propertyName != null) && this.propertyName.equals(propertyName))) ||
        (this.propertyName == propertyName);
    }
    
    /** XXX Invokes delete next char action.
     * FIXME There shouldn't be an action invocation, but rather utility method call. */
    public abstract void invokeDeleteNextCharAction(ActionEvent evt);
}
