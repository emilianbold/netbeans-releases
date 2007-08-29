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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.SwingUtilities;

import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;


// For CVS archaeology: This file used to be called com.sun.rave.css2.Utillities

/**
 * Miscellaneous utilities for the designer
 *
 * @author Tor Norbye
 */
public class DesignerUtils {

    /** Debugging flag. */
    static final boolean DEBUG = ErrorManager.getDefault()
    .getInstance(DesignerUtils.class.getName()).isLoggable(ErrorManager.INFORMATIONAL);


    // XXX Moved into insync/DesignBeanNodeHelper.
//    static char[] hexdigits =
//    { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    // Same as style set in Text renderer and in default style sheet
    private static final String ignoreClass = "rave-uninitialized-text"; // NOI18N

//    /** Return true if the extension indicates that this is an image */
//    public static boolean isImage(String extension) {
//        return (extension.equalsIgnoreCase("jpg") || // NOI18N
//                extension.equalsIgnoreCase("gif") || // NOI18N
//                extension.equalsIgnoreCase("png") || // NOI18N
//                extension.equalsIgnoreCase("jpeg")); // NOI18N
//    }

//    public static boolean isStylesheet(String extension) {
//        return extension.equalsIgnoreCase("css"); // NOI18N
//    }

//    /** Return the relative path of the given GenericItem to the page folder */
//    public static String getPageRelativePath(WebForm webform, FileObject fo) {
//        FileObject webroot;
//        webroot = JSFProjectUtil.getDocumentRoot(webform.getProject());
//
//        String rootName = webroot.getPath();
//        String fileName = fo.getPath();
//
//        if (fileName.startsWith(rootName)) {
//            return fileName.substring(rootName.length());
//        }
//
//        return null;
//    }

    // XXX Moved to FormComponentBox.
//    /**
//     * Add all the text content you can find under the given node into
//     * the given StringBuffer.
//     */
//    public static void addNodeText(StringBuffer sb, Node n, boolean skipSpace) {
//        int type = n.getNodeType();
//
//        if (type == Node.TEXT_NODE) {
//            if (skipSpace && onlyWhitespace(n.getNodeValue())) {
//                return;
//            }
//
//            sb.append(n.getNodeValue());
//        } else if (type == Node.COMMENT_NODE) {
//            String comment = n.getNodeValue();
//            int newline = comment.indexOf('\n');
//
//            if (newline != -1) {
//                sb.append(comment.substring(newline + 1));
//            }
//        } else if (type == Node.CDATA_SECTION_NODE) {
//            if (skipSpace && onlyWhitespace(n.getNodeValue())) {
//                return;
//            }
//            
//            sb.append(n.getNodeValue());
//        } else {
//            NodeList children = n.getChildNodes();
//            
//            for (int i = 0; i < children.getLength(); i++) {
//                addNodeText(sb, children.item(i), skipSpace);
//            }
//        }
//    }
    
    // This function is used by the experimental designer JavaScript support
    
    /** Return the literal string in the given node. If comments are encountered, everything on the
     * first line is ignored, then the rest is included. This matches browser behavior where
     * for example
     * <pre>
     *    &lt;script&gt;&lt;-- Here's some javascript code:<br/>
     *       alert('hello world');
     *    &lt;/script&gt;
     * </pre>
     * is going to add the text "alert('hello world')" only.
     */
    
    //static void addScriptNodeText(StringBuffer sb, Node n) {
    //    int type = n.getNodeType();
    //
    //    if (type == Node.TEXT_NODE) {
    //        sb.append(n.getNodeValue());
    //    } else if (type == Node.COMMENT_NODE) {
    //        String comment = n.getNodeValue();
    //        int newline = comment.indexOf('\n');
    //
    //        if (newline != -1) {
    //            sb.append(comment.substring(newline + 1));
    //        }
    //    } else if (type == Node.ELEMENT_NODE) {
    //        // TODO - insetead of formatting, simply pull attributes out directly
    //        sb.append(FacesSupport.getHtmlStream((Element)n));
    //    } else if (type == Node.CDATA_SECTION_NODE) {
    //        sb.append(n.getNodeValue());
    //    } else if ((type == Node.DOCUMENT_FRAGMENT_NODE) && (n.getChildNodes().getLength() == 1) &&
    //            ((n.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) ||
    //            (n.getChildNodes().item(0).getNodeType() == Node.COMMENT_NODE) ||
    //            (n.getChildNodes().item(0).getNodeType() == Node.CDATA_SECTION_NODE))) {
    //        sb.append(n.getChildNodes().item(0).getNodeValue());
    //    }
    //}
    
    /** XXX Copy also in insync/FacesDnDSupport.
     * Return true iff the string contains only whitespace */
    public static boolean onlyWhitespace(String s) {
        if(DEBUG) {
            debugLog(DesignerUtils.class.getName() + ".onlyWhitespace(String)");
        }
        if(s == null) {
            return true;
        }
        int n = s.length();
        
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            
            /* See the "empty-cells" documentation in CSS2.1 for example:
             * it sounds like only SOME of the whitespace characters are
             * truly considered ignorable whitespace: \r, \n, \t, and space.
             * So do something more clever in some of these cases.
             */
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        
        return true;
    }

    // XXX Copied into insync/DesignBeanNodeHelper.
//    public static String colorToHex(Color c) {
//        if (c == null) {
//            return null;
//        }
//        
//        int r = c.getRed();
//        int g = c.getGreen();
//        int b = c.getBlue();
//        
//        return "#" + hexdigits[(r & 0xF0) >> 4] + hexdigits[r & 0x0F] + hexdigits[(g & 0xF0) >> 4] +
//                hexdigits[g & 0x0F] + hexdigits[(b & 0xF0) >> 4] + hexdigits[b & 0x0F];
//    }
    
//    public static String colorToRgb(Color c) {
//        if (c == null) {
//            return null;
//        }
//        
//        int r = c.getRed();
//        int g = c.getGreen();
//        int b = c.getBlue();
//        
//        return "rgb(" + r + "," + g + "," + b + ")";
//    }

    // XXX Moved to TextBox.
//    /**
//     * Unlike the methods in javax.swing.text I'm returning the text offset itself, not the distance
//     * from the passed in segment/string offset
//     */
//    public static final int getNonTabbedTextOffset(char[] s, int txtOffset, int len,
//            FontMetrics metrics, int x0, int x) {
//        if (x0 >= x) {
//            // x before x0, return.
//            return txtOffset;
//        }
//        
//        int currX = x0;
//        int nextX = currX;
//        
//        // s may be a shared segment, so it is copied prior to calling
//        // the tab expander
//        int n = txtOffset + len;
//        final boolean round = true;
//        
//        for (int i = txtOffset; i < n; i++) {
//            char c = s[i];
//            
//            // TODO if there are successive spaces, ignore them
//            // TODO count a newline as a space!
//            if ((c == '\t') || (c == '\n')) {
//                nextX += metrics.charWidth(' ');
//            } else {
//                nextX += metrics.charWidth(c);
//            }
//            
//            if ((x >= currX) && (x < nextX)) {
//                // found the hit position... return the appropriate side
//                if ((round == false) || ((x - currX) < (nextX - x))) {
//                    return i;
//                } else {
//                    return i + 1;
//                }
//            }
//            
//            currX = nextX;
//        }
//        
//        return txtOffset;
//    }
    
    /** Based on similar routine in javax.swing.text.Utilities */
    public static final int getNonTabbedTextWidth(char[] s, int beginOffset, int endOffset,
            FontMetrics metrics) {
        int nextX = 0;
        
        for (int i = beginOffset; i < endOffset; i++) {
            char c = s[i];
            
            if ((c == '\t') || (c == '\n')) {
                nextX += metrics.charWidth(' ');
            } else {
                nextX += metrics.charWidth(c);
            }
            
            // Ignore newlines, they take up space and we shouldn't be
            // counting them.
        }
        
        return nextX;
    }
  
    // XXX Moved to DomInspector.
//    /**
//     * Get the name of the given bean, e.g. "button1", "textField5", etc.
//     *
//     * @return the name of the bean, or null if a bean can not be found for this view's element
//     */
//    public static String getBeanName(DesignBean bean) {
//        if (bean != null) {
//            return "<" + bean.getInstanceName() + ">";
//        }
//        
//        return null;
//    }
//    
//    /**
//     * If the element corresponds to a bean, return the bean name.
//     *
//     * @return the name of the bean, or null if a bean can not be found for this element
//     */
//    public static String getBeanName(Element element) {
////        DesignBean bean = FacesSupport.getDesignBean(element);
//        DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//        
//        if (bean != null) {
//            return getBeanName(bean);
//        } else {
//            return null;
//        }
//    }

    // XXX Moved to SelectionManager.
//    /** Locate the JTable within the property sheet in the IDE.
//     * WARNING: Implementation hacks!
//     * @param focus If set, focus the top component
//     * @param visible If set, ensure the top component is fronted
//     */
//    public static JTable findPropSheetTable(boolean focus, boolean visible) {
//        WindowManager mgr = WindowManager.getDefault();
//        TopComponent properties = mgr.findTopComponent("properties"); // NOI18N
//        
//        if ((properties != null) && (visible || properties.isShowing())) {
//            if (focus) {
//                properties.requestActive();
//            }
//            
//            if (visible) {
//                properties.requestVisible();
//            }
//            
//            return findTable(properties);
//        }
//        
//        return null;
//    }
//    
//    /** Fish the given Container hierarchy for a JTable */
//    private static JTable findTable(Container c) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".findTable(Container)");
//        }
//        if(c == null) {
//            return(null);
//        }
//        if (c instanceof JTable) {
//            return (JTable)c;
//        }
//        
//        int n = c.getComponentCount();
//        
//        for (int i = 0; i < n; i++) {
//            Component comp = c.getComponent(i);
//            
//            if (comp instanceof JTable) {
//                return (JTable)comp;
//            }
//            
//            if (comp instanceof Container) {
//                JTable table = findTable((Container)comp);
//                
//                if (table != null) {
//                    return table;
//                }
//            }
//        }
//        
//        return null;
//    }

    // XXX Moved to SelectionManager.
//    /** Find the first TextNode child under this element, or null
//     * if no such node is found.
//     * @todo How do we avoid returning for example the blank
//     * textnode between <table> and <tr> in a table? I want
//     * the <td> !
//     */
//    public static Node findFirstTextChild(Node node) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".findFirstTextChild(Node)");
//        }
//        if(node == null) {
//            return(null);
//        }
//        if ((node.getNodeType() == Node.TEXT_NODE) ||
//                (node.getNodeType() == Node.CDATA_SECTION_NODE)) {
//            return node;
//        }
//        
//        NodeList nl = node.getChildNodes();
//        
//        for (int i = 0, n = nl.getLength(); i < n; i++) {
//            Node result = findFirstTextChild(nl.item(i));
//            
//            if (result != null) {
//                return result;
//            }
//        }
//        
//        return null;
//    }

    // XXX Moved to Document.
//    /** For the given node, locate a parent list item element, or return
//     * null if no such parent is found.
//     */
//    public static Element getListItemParent(Node node) {
//        while (node != null) {
//            if (node.getNodeType() == Node.ELEMENT_NODE) {
//                Element element = (Element)node;
//                
//                if (element.getTagName().equals(HtmlTag.LI.name)) {
//                    return element;
//                }
//            }
//            
//            node = node.getParentNode();
//        }
//        
//        return null;
//    }

    // XXX Moved to ModelViewMapper.
//    /**
//     * Given a position in the DOM, find the closest valid position.
//     * In particular, the position is not allowed to be inside any
//     * "renders children" nodes.  It also doesn't allow positions
//     * that are "adjacent" (before, after) an absolutely positioned
//     * element.
//     *
//     * @param pos Position to be checked
//     * @param adjust If true, adjust the position to the nearest (above)
//     *   position that is valid.
//     * @param inline inlineEditor which is in the game in the designer or null.
//     * @todo This method is mostly used to determine if a position is a valid
//     *   caret position now. Perhaps rename it to that (isValidCaretPosition).
//     * @param dom The JSPX document DOM
//     */
//    public static Position checkPosition(Position pos, boolean adjust, /*WebForm webform*/InlineEditor inline) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".checkPosition(Position, boolean, WebForm)");
//        }
////        if(pos == null || webform == null) {
//        if (pos == null) {
//            return null;
//        }
//        if (pos == Position.NONE) {
//            return pos;
//        }
//        
//        Node node = pos.getNode();
//        
//        if (!adjust) {
////            InlineEditor inline = webform.getManager().getInlineEditor();
//            
//            if (inline != null) {
//                if (inline.checkPosition(pos)) {
//                    return pos;
//                } else {
//                    return Position.NONE;
//                }
//            }
//        }
//        
//        // Don't accept positions adjacent to an absolutely or relatively positioned container
//        if (!adjust) {
////            RaveElement target = pos.getTargetElement();
////            if ((target != null) && target.isRendered()) {
//            Element target = pos.getTargetElement();
//            if (MarkupService.isRenderedNode(target)) {
////                Value val = CssLookup.getValue(target, XhtmlCss.POSITION_INDEX);
//                CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(target, XhtmlCss.POSITION_INDEX);
//                
////                if ((val == CssValueConstants.ABSOLUTE_VALUE) ||
////                        (val == CssValueConstants.RELATIVE_VALUE) ||
////                        (val == CssValueConstants.FIXED_VALUE)) {
//                if (CssProvider.getValueService().isAbsoluteValue(cssValue)
//                || CssProvider.getValueService().isRelativeValue(cssValue)
//                || CssProvider.getValueService().isFixedValue(cssValue)) {
//                    return Position.NONE;
//                }
//            }
//        }
//        
//        while (node != null) {
////            if (node instanceof RaveRenderNode) {
////                RaveRenderNode rn = (RaveRenderNode)node;
////                if (rn.isRendered() && (rn.getSourceNode() == null)) {
//            if (MarkupService.isRenderedNode(node) && MarkupService.getSourceNodeForNode(node) == null) {
//                    if (adjust) {
//                        Node curr = node;
//                        
//                        while (curr != null) {
//                            if (curr.getNodeType() == Node.ELEMENT_NODE) {
////                                RaveElement e = (RaveElement)curr;
//                                Element e = (Element)curr;
//                                
////                                if (e.getSource() != null) {
//                                if (MarkupService.getSourceElementForElement(e) != null) {
////                                    MarkupDesignBean bean = e.getDesignBean();
////                                    MarkupDesignBean bean = InSyncService.getProvider().getMarkupDesignBeanForElement(e);
//                                    MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(e);
//                                    
//                                    if (bean != null) {
//                                        bean = /*FacesSupport.*/findRendersChildren(bean);
////                                        e = (RaveElement)bean.getElement();
//                                        e = bean.getElement();
//                                    }
//                                    
//                                    return Position.create(e, pos.getOffset() > 0);
//                                }
//                            }
//                            
//                            curr = curr.getParentNode();
//                            
//                            if (curr == null) {
//                                return Position.NONE;
//                            }
//                        }
//                    } else {
//                        return Position.NONE;
//                    }
////                }
//            }
//            
////            if (node instanceof RaveElement) {
////                RaveElement element = (RaveElement)node;
//            if (node instanceof Element) {
//                Element element = (Element)node;
//                
////                if (element.getDesignBean() != null) {
////                    MarkupDesignBean bean = element.getDesignBean();
////                MarkupDesignBean bean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//                MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//                
//                if (bean != null) {
//                    MarkupDesignBean parent = /*FacesSupport.*/findRendersChildren(bean);
//                    
//                    // XXX what if bean itself is a renders children?
//                    if (parent != bean) {
//                        if (adjust) {
//                            // There was a renders-children parent we
//                            // should skip
//                            Element parentElement = parent.getElement();
//                            
//                            return Position.create(parentElement, pos.getOffset() > 0);
//                        } else {
//                            return Position.NONE;
//                        }
//                    }
//                    
//                    break;
//                }
//            }
//            
//            node = node.getParentNode();
//        }
//        
////        InlineEditor inline = webform.getManager().getInlineEditor();
//        
//        if (((pos != Position.NONE) && ((inline != null) && inline.checkPosition(pos))) ||
////                !pos.isRendered()) {
//        !MarkupService.isRenderedNode(pos.getNode())) {
//            return pos;
//        } else if (adjust) {
//            // Try to find the corresponding source
//            node = pos.getNode();
//            
//            while (node != null) {
////                if (node instanceof RaveElement) {
////                    RaveElement element = (RaveElement)node;
//                if (node instanceof Element) {
//                    Element element = (Element)node;
//                    
////                    if (element.getDesignBean() != null) {
////                        DesignBean bean = element.getDesignBean();
////                    DesignBean bean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//                    DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//                    if (bean != null) {
//                        Element el = FacesSupport.getElement(bean);
//                        
//                        return Position.create(el, pos.getOffset() > 0);
//                    }
//                }
//                
//                node = node.getParentNode();
//            }
//            
//            return Position.NONE;
//        } else {
//            //            // XXX shouldn't this be return pos; ? Try to click somewhere in BoxModelTest
//            //            // layout-floats3.html
//            //            return Position.NONE;
//            //        }
//            return pos;
//        }
//    }
//
//    // XXX Moved from FacesSupport.
//    /** Find outermost renders-children bean above the given bean, or
//     * the bean itself if there is no such parent.
//     */
//    private /*public*/ static MarkupDesignBean findRendersChildren(MarkupDesignBean bean) {
//        // Similar to FacesSupport.findHtmlContainer(bean), but
//        // we need to return the outermost html container itself, not
//        // the parent, since we're not looking for its container but
//        // the bean to be moved itself.
//        MarkupDesignBean curr = bean;
//
////        for (; curr != null; curr = FacesSupport.getBeanParent(curr)) {
//        for (; curr != null; curr = getBeanParent(curr)) {
//            if (curr.getInstance() instanceof F_Verbatim) {
//                // If you have a verbatim, we're okay to add html comps below it
//                return bean;
//            }
//
//            if (curr.getInstance() instanceof UIComponent) {
//                // Need to set the Thread's context classloader to be the Project's ClassLoader.
//            	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
//            	try {
////                    Thread.currentThread().setContextClassLoader(InSyncService.getProvider().getContextClassLoader(curr));
//                    Thread.currentThread().setContextClassLoader(WebForm.getDomProviderService().getContextClassLoaderForDesignContext(curr.getDesignContext()));
//                    if (((UIComponent)curr.getInstance()).getRendersChildren()) {
//                    	bean = curr;
//                        // Can't break here - there could be an outer
//                        // renders-children parent
//                    }               
//            	} finally {
//                    Thread.currentThread().setContextClassLoader(oldContextClassLoader);
//            	}                
//            }
//        }
//
//        return bean;
//    }
//    
//    // XXX Moved from FacesSupport.
//    /**
//     * Return the parent of the given markup design bean, if the parent is
//     * a MarkupDesignBean.
//     */
//    private static MarkupDesignBean getBeanParent(MarkupDesignBean bean) {
//        DesignBean parent = bean.getBeanParent();
//
//        if (parent instanceof MarkupDesignBean) {
//            return (MarkupDesignBean)parent;
//        }
//
//        return null;
//    }

    
    
//    /** Add item to a popup menu or a menu. Only one of the fields
//     *  should be non null. */
//    public static void addMenuItem(JPopupMenu popup, JMenu menu, JMenuItem item) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".addMenuItem(JPopupMenu, JMenu, JMenuItem)");
//        }
//        if(item == null) {
//            throw(new IllegalArgumentException("Null menu item"));
//        }
//        if (menu != null) {
//            menu.add(item);
//        } else {
//            if(popup != null) {
//                popup.add(item);
//            } else {
//                throw(new IllegalArgumentException("Both menu and popup are null."));// NOI18N
//            }
//        }
//    }
//    
//    /** Add a separator a popup menu or a menu - unless the previous item
//     *  was a popup! Only one of the fields should be non null. */
//    public static void addSeparator(JPopupMenu popup, JMenu menu) {
//        if (menu != null) {
//            // Make sure previous item wasn't a separator
//            int n = menu.getItemCount();
//            
//            if ((n > 0) && (menu.getMenuComponent(n - 1) instanceof JSeparator)) {
//                return;
//            }
//            
//            menu.addSeparator();
//        } else {
//            if(popup != null) {
//                int n = popup.getComponentCount();
//                
//                if ((n > 0) && (popup.getComponent(n - 1) instanceof JSeparator)) {
//                    return;
//                }
//                
//                popup.addSeparator();
//            } else {
//                throw(new IllegalArgumentException("Both menu and popup are null."));// NOI18N
//            }
//        }
//    }
//    
//    /** Add menu items to the iven popup menu for the given DesignBean.
//     * @param bean The DesignBean whose context menu items plus customizer action
//     * @param menu The popup menu to add items to
//     * @param showAncestors If true, include context menu items in parents of the given bean.
//     *    These will be pullright menus whose menu name is the bean name of the ancestor.
//     * @param webform May be null, but can also point to a webform for this bean. This is used
//     *    to do batch processing on the element when a live customizer is run on the bean.
//     * @param mouseElement May be null, but can also point to a node in
//     *    the render tree which has been marked up with a MarkupMouseRegion.
//     *    which could contribute menu items.
//     *  */
//    public static void addMenuItems(final DesignBean bean, JPopupMenu menu,
//            org.openide.nodes.Node[] nodes, JComponent parent, boolean showAncestors,
//            final WebForm webform, Element mouseElement, JComponent addItem) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".addMenuItems(DesignBean, JPopupMenu, Node[], JComponent, boolean, WebForm, Element, JComponent");
//        }
//        if(bean == null) {
//            throw(new IllegalArgumentException("Null menu."));// NOI18N
//        }
//        if(parent == null) {
//            throw(new IllegalArgumentException("Null parent component."));// NOI18N
//        }
//        while (mouseElement != null) {
////            if (mouseElement.getMarkupMouseRegion() != null) {
////                MarkupMouseRegion region = mouseElement.getMarkupMouseRegion();
//            MarkupMouseRegion region = InSyncService.getProvider().getMarkupMouseRegionForElement(mouseElement);
//            if (region != null) {
//                DisplayAction[] context = region.getContextItems();
//                
//                if ((context != null) && (context.length > 0)) {
//                    buildMenu(menu, null, context, webform);
//                    DesignerUtils.addSeparator(menu, null);
//                }
//            }
//            
////            if (mouseElement.getParentNode() instanceof RaveElement) {
////                mouseElement = (RaveElement)mouseElement.getParentNode();
//            if (mouseElement.getParentNode() instanceof Element) {
//                mouseElement = (Element)mouseElement.getParentNode();
//            } else {
//                break;
//            }
//        }
//        
//        if (nodes != null) {
//            Action[] actions = NodeOp.findActions(nodes);
//            
//            if ((actions.length > 0) && ((actions.length > 1) || (actions[0] != null))) {
//                Utilities_RAVE.actionsToPopup(actions, parent,
//                        menu);
//                menu.addSeparator();
//            }
//        }
//        
//        if(bean == null) {
//            throw(new IllegalArgumentException("Null bean."));// NOI18N
//        }
//        // Construct context menu.
//        boolean separate = false;
//        DesignInfo bi = bean.getDesignInfo();
//        
//        if (bi != null) {
//            DisplayAction[] context = bi.getContextItems(bean);
//            
//            if ((context != null) && (context.length > 0)) {
//                buildMenu(menu, null, context, webform);
//                separate = true;
//            }
//        }
//        
//        // "Add" pullright
//        if (addItem != null) {
//            menu.add(addItem);
//        }
//        
//        // Add a Property Binding dialog? (this is not separated from other
//        // context menus)
//        if (bean instanceof FacesDesignBean) {
//            buildMenu(menu, null,
//                    new DisplayAction[] { PropertyBindingHelper.getContextItem(bean) }, webform);
//            separate = true;
//        }
//        
//        if (separate) {
//            DesignerUtils.addSeparator(menu, null);
//        }
//        
//        // Add Customizer?
//        BeanDescriptor descriptor = bean.getBeanInfo().getBeanDescriptor();
//        
//        // XXX where do I look for Customizer2?
//        Class customizer = descriptor.getCustomizerClass();
//        
//        if (customizer != null) {
//            String label = NbBundle.getMessage(SelectionManager.class, "Customize"); // NOI18N
//            JMenuItem customizeItem = new JMenuItem(label);
//            menu.add(customizeItem);
//            customizeItem.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent evt) {
//                    webform.getManager().finishInlineEditing(false);
//                    customize(bean);
//                }
//            });
//            menu.addSeparator();
//        }
//        
//        // For each LiveParent that has menu items, add in their
//        // actions
//        DesignerUtils.addSeparator(menu, null);
//        
//        if (showAncestors) {
//            DesignBean bparent = bean.getBeanParent();
//            
//            while (bparent != null) {
//                // Cut off at form bean!
//                // Construct context menu
//                bi = bparent.getDesignInfo();
//                
//                if (bi != null) {
//                    DisplayAction[] context = bi.getContextItems(bparent);
//                    
//                    if ((context != null) && (context.length > 0)) {
//                        JMenu submenu = new JMenu(bparent.getInstanceName());
//                        DesignerUtils.addMenuItem(menu, null, submenu);
//                        buildMenu(null, submenu, context, webform); // recurse
//                    }
//                }
//                
//                bparent = bparent.getBeanParent();
//            }
//            
//            DesignerUtils.addSeparator(menu, null);
//        }
//    }
//    
//    /**
//     * Add a single menu item to the given menu
//     */
//    public static void addMenuItem(JPopupMenu popup, DisplayAction item, WebForm webform) {
//        buildMenu(popup, null, new DisplayAction[] { item }, webform);
//    }
//    
//    /** Recursively build up a potentially hierarchical popup menu
//     * structure for the given bean's DesignInfo
//     * @todo The fact that I'm passing in BOTH a JPopup and a JMenu
//     *  is really lame. The problem is that at the top level, I need
//     *  to populate a JPopupMenu - but I can't add JPopupMenu items
//     *  for submenus - they must be JMenus. Annoyingly, a JPopupMenu
//     *  isn't a JMenu. Thus, I have to pass in one or the other,
//     *  and check which is non-null to figure out what this recursive
//     *  method is adding to now. To avoid duplicating this logic
//     *  this method will call addMenuItem for insertion.
//     * @todo Properly handle separators such that if I have a submenu
//     *   with only a popup set for example, I don't get separators at
//     *   the top and the bottom of the menu
//     * @return true iff the last item added was a separator
//     */
//    private static void buildMenu(JPopupMenu popup, JMenu menu, DisplayAction[] items,
//            WebForm webform) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".addMenuItems(DesignBean, JPopupMenu, Node[], JComponent, boolean, WebForm, Element, JComponent");
//        }
//        if(items == null) {
//            throw(new IllegalArgumentException("Null display actions array."));// NOI18N
//        }
//        if(webform == null) {
//            throw(new IllegalArgumentException("Null webform."));// NOI18N
//        }
//        for (int i = 0; i < items.length; i++) {
//            DisplayAction action = items[i];
//            
//            if (action instanceof DisplayActionSet) {
//                DisplayActionSet set = (DisplayActionSet)action;
//                
//                if (set.isPopup()) {
//                    // It's a pullright menu
//                    //JPopupMenu submenu = new org.openide.awt.JPopupMenuPlus();
//                    JMenu submenu = new JMenu(set.getDisplayName());
//                    
//                    // XXX set tooltip? action.getDescription()
//                    // XXX set icon?  action.getSmallIcon()
//                    //item.setEnabled(action.isEnabled()); // XXX?
//                    DesignerUtils.addMenuItem(popup, menu, submenu);
//                    
//                    /* XXX JPopupMenu doesn't have action listeners....
//                    if (set.isInvokable()) {
//                        // XXX this doesn't get called on menu traverse,
//                        // right? I don't think this is really compatible
//                        // with swing menus...
//                        submenu.addActionListener(new ActionListener() {
//                                public void actionPerformed(ActionEvent event) {
//                                    webform.getManager().finishEditing(false);
//                                    action.invoke();
//                                }
//                            }
//                        );
//                    }
//                     */
//                    DisplayAction[] actions = set.getDisplayActions();
//                    
//                    if (actions.length > 0) {
//                        buildMenu(null, submenu, actions, webform); // recurse
//                    }
//                } else {
//                    // It's a flat container - inline it in this menu
//                    DisplayAction[] actions = set.getDisplayActions();
//                    
//                    if (actions.length > 0) {
//                        DesignerUtils.addSeparator(popup, menu);
//                        buildMenu(popup, menu, actions, webform); // recurse
//                        
//                        // Only add separator if it's not the end of the menu
//                        // (Except in the popup/toplevel case, where we d
//                        if (i < (items.length - 1)) {
//                            DesignerUtils.addSeparator(popup, menu);
//                        }
//                    }
//                    
//                    // XXX ignoring set.isInvokable in this case...
//                    // There's no "user visible" representation
//                    // of the group, other than the fact that its
//                    // items are displayed within a pair of separators
//                    // (but the items themselves of course have their
//                    // own actions that are invokable when the items
//                    // are selected)
//                }
//            } else if (action instanceof CheckedDisplayAction) {
//                // Plain item
//                JMenuItem item = new JCheckBoxMenuItem(action.getDisplayName());
//                
//                // XXX set tooltip? action.getDescription()
//                // XXX set icon?  action.getSmallIcon()
//                item.setEnabled(action.isEnabled());
//                item.setSelected(((CheckedDisplayAction)action).isChecked());
//                DesignerUtils.addMenuItem(popup, menu, item);
//                
//                item.addActionListener(new MenuActionListener(action, webform));
//            } else {
//                // Plain item
//                JMenuItem item = new JMenuItem(action.getDisplayName());
//                
//                // XXX set tooltip? action.getDescription()
//                // XXX set icon?  action.getSmallIcon()
//                item.setEnabled(action.isEnabled());
//                DesignerUtils.addMenuItem(popup, menu, item);
//                
//                item.addActionListener(new MenuActionListener(action, webform));
//            }
//        }
//    }
//    
//    private static void customize(DesignBean bean) {
//        // Instead of stashing the object to operate on
//        // along with the menu item, can we just look up
//        // the selection set?
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".customize(DesignBean)");
//        }
//        if (bean == null) {
//            return;
//        }
//        
//        BeanDescriptor descriptor = bean.getBeanInfo().getBeanDescriptor();
//        
//        // The following code based on
//        // core/src/org/netbeans/core/ModuleFSSection.java
//        Class customizer = descriptor.getCustomizerClass();
//        
//        if (customizer == null) {
//            // XXX should not happen - we only add/enable
//            // the action if we've found one
//            return;
//        }
//        
//        if (Customizer2.class.isAssignableFrom(customizer)) {
//            // Found live customizer
//            try {
//                Customizer2 cust = (Customizer2)customizer.newInstance();
//                Component panel = cust.getCustomizerPanel(bean);
//                DialogDescriptor dd = new DialogDescriptor(panel, cust.getDisplayName());
//                
//                //                DialogDescriptor dd = new DialogDescriptor(panel,
//                //                              NbBundle.getMessage(SelectionManager.class,
//                //                                                  "CustomizeBean"));
//                DialogDisplayer.getDefault().createDialog(dd).show();
//                
//                return;
//            } catch (Exception ex) {
//                ErrorManager.getDefault().notify(ex);
//                
//                return;
//            }
//        }
//        
//        try {
//            Customizer cust = (Customizer)customizer.newInstance();
//            cust.setObject(bean.getInstance());
//            
//            if (cust instanceof Window) {
//                // Customizer was already a window (probably a
//                // dialog).  Just show it. Presumably it has
//                // some window listener and knows to do
//                // something when it is closed.
//                ((Window)cust).setVisible(true);
//            } else if (cust instanceof Component) {
//                // Some customizer panel. Show it in an
//                // OK/Cancel dialog.
//                Component c = (Component)cust;
//                DialogDescriptor dd =
//                        new DialogDescriptor(c,
//                        NbBundle.getMessage(SelectionManager.class, "CustomizeBean"));
//                DialogDisplayer.getDefault().createDialog(dd).show();
//            } else {
//                ErrorManager.getDefault().log("Non-Component Customizer for bean " +
//                        bean.getClass().getName());
//                
//                return;
//            }
//        } catch (Exception ex) {
//            //IOException e = new IOException (ex.toString ());
//            //ErrorManager.getDefault ().annotate (e, ex);
//            //throw e;
//            ErrorManager.getDefault().notify(ex);
//        }
//    }
    
    /** Recursively remove the rave-uninitialized-text class attribute
     *  from a node tree.
     * @return True iff any nodes were actually changed
     */
    public static boolean stripDesignStyleClasses(Node node) {
        boolean changedStyles = false;
        
        if(DEBUG) {
            debugLog(DesignerUtils.class.getName() + ".stripDesignStyleClasses(Node)");
        }
        if(node == null) {
            throw(new IllegalArgumentException("Null node."));// NOI18N
        }
        
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element)node;
            
            if (e.getAttribute(HtmlAttribute.CLASS).indexOf(ignoreClass) != -1) {
                String newClass = e.getAttribute(HtmlAttribute.CLASS).replaceAll(ignoreClass, ""); // ignore stripped out
                e.setAttribute(HtmlAttribute.CLASS, newClass);
                changedStyles = true;
            }
        }
        
        NodeList nl = node.getChildNodes();
        
        for (int i = 0, n = nl.getLength(); i < n; i++) {
            changedStyles |= stripDesignStyleClasses(nl.item(i)); // recurse
        }
        
        return changedStyles;
    }
    
//    /** Strip the given string to the given maximum length of
//     * characters. If the string is not that long, just return
//     * it.  If it needs to be truncated, truncate it and append
//     * "...".  maxLength must be at least 4. */
//    public static String truncateString(String s, int maxLength) {
//        assert maxLength >= 4;
//        
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".truncateString(String, int)");
//        }
//        if(s == null) {
//            throw(new IllegalArgumentException("Null string to truncate."));// NOI18N
//        }
//        
//        if (s.length() > maxLength) {
//            // Should "..." be localizable?
//            return s.substring(0, maxLength - 3) + "...";
//        } else {
//            return s;
//        }
//    }

    // XXX Copy also in designer/jsf/../DomProviderImpl
    /** Thread-safe method to set the activated nodes of a TopComponent;
     * this can only be done from the event dispatch thread. If called
     * from another thread it will post a runnable on the event dispatch
     * thread instead.
     */
    public static void setActivatedNodes(final TopComponent tc, final org.openide.nodes.Node[] nodes) {
        if(DEBUG) {
            debugLog(DesignerUtils.class.getName() + ".setActivatedNodes(TopComponent, Node[])");
        }
        if(nodes == null) {
            throw(new IllegalArgumentException("Null node array."));// NOI18N
        }
        if(tc == null) {
            throw(new IllegalArgumentException("Null TopComponent."));// NOI18N
        }
        if (SwingUtilities.isEventDispatchThread()) {
            tc.setActivatedNodes(nodes);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tc.setActivatedNodes(nodes);
                }
            });
        }
    }
    
    // Moved to WebForm.
//    /** Locate the webform associated with the given data object. Will return
//     *  null if no such object is found.
//     * @param dobj The data object for which a webform should be located
//     * @param initialize If false, don't initialize a webform that has
//     *  not yet been open - return null instead.
//     */
//    public static WebForm getWebForm(DomProvider domProvider, DataObject dobj) {
//        if (WebForm.isWebFormDataObject(dobj)) {
//            return WebForm.getWebFormForDataObject(domProvider, dobj);
//        }
//        
//        return null;
//    }
    
//    /**
//     * Return true iff the drag is coming from a NetBeans "explorer" window,
//     * since drags with this origin have funky behavior
//     */
//    public static boolean isExplorerDrag(DropTargetDragEvent dtde) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".isExplorerDrag(DropTargetDragEvent)");
//        }
//        if(dtde == null) {
//            throw(new IllegalArgumentException("Null DropTargetDragEvent."));// NOI18N
//        }
//        DataFlavor[] df = dtde.getCurrentDataFlavors();
//        
//        for (int i = 0; i < df.length; i++) {
//            DataFlavor f = df[i];
//            
//            if (f.getRepresentationClass() == org.openide.nodes.Node.class) {
//                return true;
//            }
//        }
//        
//        return false;
//    }
    
//    /** Fidn a markup bean by id! */
//    public static DesignBean findById(WebForm webform, String id) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".findById(WebForm)");
//        }
//        if(webform == null) {
//            throw(new IllegalArgumentException("Null DropTargetDragEvent."));// NOI18N
//        }
//        return webform.getModel().getLiveUnit().getBeanByName(id);
//    }
    
    /** Check to see if the given point is inside of the rectangle
     * positioned at (tx,ty) of dimensions (w,h). */
    public static final boolean inside(int x, int y, int tx, int ty, int w, int h) {
        if (x < tx) {
            return false;
        }
        
        if (y < ty) {
            return false;
        }
        
        if (x > (tx + w)) {
            return false;
        }
        
        if (y > (ty + h)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Taken from java.awt.Rectangle; changed to be static and
     * to compare with specified x,y,w,h instead of another Rectangle
     *
     * Determines whether or not this <code>Rectangle</code> and the specified
     * <code>Rectangle</code> intersect. Two rectangles intersect if
     * their intersection is nonempty.
     *
     * @param r the specified <code>Rectangle</code>
     * @return    <code>true</code> if the specified <code>Rectangle</code>
     *            and this <code>Rectangle</code> intersect;
     *            <code>false</code> otherwise.
     */
    public static boolean intersects(Rectangle r, int rx, int ry, int rw, int rh) {
        if(DEBUG) {
            debugLog(DesignerUtils.class.getName() + ".intersects(Rectangle, int, int, int, int)");
        }
        if(r == null) {
            throw(new IllegalArgumentException("Null rectangle."));// NOI18N
        }
        int tw = r.width;
        int th = r.height;
        
        if ((rw <= 0) || (rh <= 0) || (tw <= 0) || (th <= 0)) {
            return false;
        }
        
        int tx = r.x;
        int ty = r.y;
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;
        
        //      overflow || intersect
        return (((rw < rx) || (rw > tx)) && ((rh < ry) || (rh > ty)) && ((tw < tx) || (tw > rx)) &&
                ((th < ty) || (th > ry)));
    }
    
//    /**
//     * Return true iff the given document represents a Braveheart page. A braveheart
//     * page is one using Braveheart components
//     * @param document The document to be checked
//     */
//    public static boolean isBraveheartPage(Document document) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".isBraveheartPage(Document)");
//        }
//        if(document == null) {
//            throw(new IllegalArgumentException("Null document."));// NOI18N
//        }
//        // Many possibilities here:
//        // (1) Scan through all tags, look to see if we find any ui: tags
//        // (2) Look on the jsp:root, see if we include the braveheart taglib
//        // (3) See if the top most tag under f:view is a braveheart one
//        // (4) See if the body is a plain html <body> tag or if it's rendered from
//        //    another component
//        Element element = document.getDocumentElement();
//        
//        if (element.hasAttribute("xmlns:ui")) { // NOI18N
//            assert element.getAttribute("xmlns:ui").equals("http://www.sun.com/web/ui"); // NOI18N
//            
//            return true;
//        }
//        
//        return false;
//    }
    
//    /** Class which is attached to display actions in context menus */
//    private static class MenuActionListener implements ActionListener {
//        private DisplayAction action;
//        private WebForm webform;
//        
//        MenuActionListener(DisplayAction action, WebForm webform) {
//            if(DEBUG) {
//                debugLog(DesignerUtils.class.getName() + ".MenuActionListener");
//            }
//            if(action == null) {
//                throw(new IllegalStateException("Null action."));// NOI18N
//            }
//            if(webform == null) {
//                throw(new IllegalStateException("Null webform."));// NOI18N
//            }
//            this.action = action;
//            this.webform = webform;
//        }
//        
//        public void actionPerformed(ActionEvent event) {
//            webform.getManager().finishInlineEditing(false);
//            
//            String label = action.getDisplayName();
//            
//            if (label.endsWith("...")) { // NOI18N
//                label = label.substring(0, label.length() - 3);
//            }
//            
////            webform.getDocument().writeLock("\"" + label + "\""); // NOI18N
//            UndoEvent undoEvent = webform.getModel().writeLock("\"" + label + "\""); // NOI18N
//            
//            try {
//                Result r = action.invoke();
//                ResultHandler.handleResult(r, (webform != null) ? webform.getModel() : null);
//            } finally {
////                webform.getDocument().writeUnlock();
//                webform.getModel().writeUnlock(undoEvent);
//            }
//        }
//    }
    
    /** Logs debug message. Use only after checking <code>DEBUG</code> flag. */
    static void debugLog(String message) {
        ErrorManager.getDefault().getInstance(DesignerUtils.class.getName()).log(message);
    }
    

    // XXX Moved to BasicDragGestureRecognizer.
//    /** XXX Copied from SunDragSourceContextPeer, to avoid dependency on sun jdk. */
//    public static int convertModifiersToDropAction(final int modifiers, 
//                                                   final int supportedActions) {
//        int dropAction = DnDConstants.ACTION_NONE;
//
//        /*
//         * Fix for 4285634.
//         * Calculate the drop action to match Motif DnD behavior.
//         * If the user selects an operation (by pressing a modifier key),
//         * return the selected operation or ACTION_NONE if the selected
//         * operation is not supported by the drag source. 
//         * If the user doesn't select an operation search the set of operations
//         * supported by the drag source for ACTION_MOVE, then for 
//         * ACTION_COPY, then for ACTION_LINK and return the first operation
//         * found.
//         */
//        switch (modifiers & (InputEvent.SHIFT_DOWN_MASK |
//                             InputEvent.CTRL_DOWN_MASK)) { 
//        case InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK:
//            dropAction = DnDConstants.ACTION_LINK; break;
//        case InputEvent.CTRL_DOWN_MASK:
//            dropAction = DnDConstants.ACTION_COPY; break;
//        case InputEvent.SHIFT_DOWN_MASK:
//            dropAction = DnDConstants.ACTION_MOVE; break;
//        default:
//            if ((supportedActions & DnDConstants.ACTION_MOVE) != 0) {
//                dropAction = DnDConstants.ACTION_MOVE; 
//            } else if ((supportedActions & DnDConstants.ACTION_COPY) != 0) {
//                dropAction = DnDConstants.ACTION_COPY; 
//            } else if ((supportedActions & DnDConstants.ACTION_LINK) != 0) {
//                dropAction = DnDConstants.ACTION_LINK; 
//            }
//        }
//
//        return dropAction & supportedActions;
//    }    

    /** XXX To keep the deprecated Toolkit.getFrontMetrics method separated.
    /* TODO Find out how actually is one suppose replace the Toolkit.getFontMetrics method. */
    public static FontMetrics getFontMetrics(Font font) {
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }
  
    // This seems to be not needed yet.
//    public static Element getNextSiblingElement(Element element) {
//        if (element == null) {
//            return null;
//        }
//        Node sibling = element.getNextSibling();
//        while (sibling != null) {
//            if (sibling instanceof Element) {
//                return (Element)sibling;
//            }
//            sibling = sibling.getNextSibling();
//        }
//        return null;
//    }
//    
//    public static Element getPreviousSiblingElement(Element element) {
//        if (element == null) {
//            return null;
//        }
//        Node sibling = element.getPreviousSibling();
//        while (sibling != null) {
//            if (sibling instanceof Element) {
//                return (Element)sibling;
//            }
//            sibling = sibling.getPreviousSibling();
//        }
//        return null;
//    }

    
}
