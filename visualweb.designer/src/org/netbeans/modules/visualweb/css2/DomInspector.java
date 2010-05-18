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
/*
 * BoxTreeComponent.java
 *
 * Created on September 1, 2005, 9:49 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */


package org.netbeans.modules.visualweb.css2;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openide.ErrorManager;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.SelectionManager;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.css2.BoxType;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.LineBoxGroup;
import org.netbeans.modules.visualweb.css2.PageBox;
import org.netbeans.modules.visualweb.css2.TextBox;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;

// Originally in org/netbeans/modules/visualweb/designer package, but moved here to have better access to package private data.
/**
 * TopComponent displaying the box tree - used for debugging only via the DOM inspector
 *
 *
 * @author Tor Norbye
 */
public class DomInspector extends TopComponent implements TreeSelectionListener {
    private WebForm webform;
    private JTree tree;
    private JScrollPane scrollPane;
    private DefaultTreeModel treeModel;
    protected DefaultTreeSelectionModel treeSelectionModel;
    private boolean ignoreSelectionEvents;

    /**
     * Creates a new instance of DomInspector
     */
    public DomInspector(WebForm webform) {
        this.webform = webform;

        setName("LayoutInspector"); // NOI18N
        setDisplayName(getBundleString("LBL_DomInspector"));
        // XXX
        setToolTipText(getDisplayName());
    }

    /** Make sure we have a window showing the boxes for the given webform */
    public static void show(CssBox box) {
        WebForm webform = box.getWebForm();

        DomInspector btc = createWindowIfNecessary(webform);
        //btc.requestActive();

        // Select box
        btc.selectBox(box);

        // XXX Copied from InteractionManager and refined to select the activated nodes initially.
        // Also set nodes locally such that if this top component
        // retains focus the node selection reflects the selected box
        SelectionManager sm = webform.getSelection();
        List<Node> nodes = new ArrayList<Node>(sm.getNumSelected());
//        DataObject dobj = webform.getDataObject();
//        DataObject dobj = webform.getJspDataObject();
        Node n = new BoxNode(box/*, dobj*/);

        //n.setDataObject(dobj);
        nodes.add(n);
        Node[] nds = nodes.toArray(new Node[nodes.size()]);

        DesignerUtils.setActivatedNodes(btc, nds);
    }

    private void selectBox(CssBox box) {
        TreePath path = getPath(box);

        if (path != null) {
            ignoreSelectionEvents = true;

            try {
                //TreeSelectionModel model = tree.getSelectionModel();
                //TreePath[] paths = new TreePath[] { path };
                //model.setSelectionPaths(paths);
                tree.setSelectionPath(path);

                //tree.expandPath(path);
                tree.makeVisible(path);
                tree.scrollPathToVisible(path);
            } finally {
                ignoreSelectionEvents = false;
            }
        }
    }

    private static DomInspector createWindowIfNecessary(WebForm webform) {
        // Search editor modes
        // Search through workspaces, then modes, then topcomponents
        Set modes = WindowManager.getDefault().getModes();
        Iterator it2 = modes.iterator();

        while (it2.hasNext()) {
            Mode m = (Mode)it2.next();
            TopComponent[] tcs = m.getTopComponents();

            if (tcs != null) {
                for (int i = 0; i < tcs.length; i++) {
                    if (tcs[i] instanceof DomInspector) {
                        DomInspector btc = (DomInspector)tcs[i];

                        if (btc.webform == webform) {
                            // Force refresh if the box tree has changed
//                            if (((BoxTreeNode)btc.treeModel.getRoot()).getBox() != webform.getPane()
//                                                                                              .getPageBox()) {
                            // XXX #121239 Avoiding possible NPE.
                            if (btc.treeModel != null && btc.treeModel.getRoot() instanceof BoxTreeNode && webform.getPane() != null
                            && ((BoxTreeNode)btc.treeModel.getRoot()).getBox() != webform.getPane().getPageBox()) {
                                btc.refresh();
                            }

                            // PENDING: refresh();
                            if (!btc.isShowing()) {
                                btc.requestVisible();
                            }

                            return btc;
                        }
                    }
                }
            }
        }

        DomInspector btc = new DomInspector(webform);

        // TODO - pick a mode?
        Mode mode = WindowManager.getDefault().findMode("explorer"); // NOI18N

        if (mode != null) {
            mode.dockInto(btc);
        }

        btc.open();
        btc.requestActive();

        return btc;
    }

    private TreePath getPath(CssBox box) {
        LinkedList<Object> pathList = new LinkedList<Object>();
        // #123989 possible NPE.
        if (treeModel == null) {
            return null;
        }
        BoxTreeNode root = (BoxTreeNode)treeModel.getRoot();
        boolean found = findBox(box, root, pathList);

        if (found) {
            //pathList.removeFirst();
            if (pathList.size() > 0) {
                return new TreePath(pathList.toArray(new Object[pathList.size()]));
            }
        }

        return null;
    }

    // SUPER INEFFICIENT!
    private boolean findBox(CssBox box, BoxTreeNode node, LinkedList<Object> path) {
        boolean containsBox = false;

        for (int k = 0; k < node.getChildCount(); k++) {
            BoxTreeNode child = (BoxTreeNode)node.getChildAt(k);

            containsBox |= findBox(box, child, path);
        }

        if (containsBox || (node.getBox() == box)) {
            path.addFirst(node);

            return true;
        }

        return false;
    }

    public void refresh() {
        PageBox pageBox = webform.getPane().getPageBox();
        TreeNode root = new BoxTreeNode(pageBox, null);
        treeModel = new DefaultTreeModel(root);
        treeSelectionModel = new DefaultTreeSelectionModel();
        treeSelectionModel.addTreeSelectionListener(this);
        tree.setSelectionModel(treeSelectionModel);
        tree.setModel(treeModel);
    }

//    public String getName() {
//        // Not internationalized - not part of the product
//        return "Layout Inspector";
//    }

    protected void componentClosed() {
        remove(scrollPane);
        scrollPane = null;
        tree = null;
        treeModel = null;
        treeSelectionModel = null;

        super.componentClosed();
    }

    protected void componentOpened() {
        super.componentOpened();

        tree = new JTree();
        tree.setRootVisible(true);
        scrollPane = new JScrollPane(tree);
        refresh();

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    /** Never preserve these windows across sessions etc. */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
        if (ignoreSelectionEvents) {
            return;
        }

        try {
            ignoreSelectionEvents = true;

            TreePath[] paths = tree.getSelectionPaths();

            if ((paths == null) || (paths.length == 0)) {
                return;
            }

            for (int i = 0; i < paths.length; i++) {
                TreePath p = paths[i];
                Object o = p.getLastPathComponent();

                if (o instanceof BoxTreeNode) {
                    BoxTreeNode btn = (BoxTreeNode)o;
                    CssBox box = btn.getBox();

                    SelectionManager sm = webform.getSelection();
                    List<Node> nodes = new ArrayList<Node>(sm.getNumSelected());
//                    DataObject dobj = webform.getDataObject();
//                    DataObject dobj = webform.getJspDataObject();
                    Node n = new BoxNode(box/*, dobj*/);

                    //n.setDataObject(dobj);
                    nodes.add(n);

                    Node[] nds = nodes.toArray(new Node[nodes.size()]);
                    requestActive();
                    DesignerUtils.setActivatedNodes(this, nds);
                    webform.getPane().getPageBox().setSelected(box);

                    break;
                }
            }
        } finally {
            ignoreSelectionEvents = false;
        }
    }

    protected String preferredID() {
        return "dom-inspector";
    }

    private static class BoxTreeNode implements TreeNode {
        private CssBox box;
        private BoxTreeNode parent;
        private List<TreeNode> children;

        private BoxTreeNode(CssBox box, BoxTreeNode parent) {
            this.box = box;
            this.parent = parent;
        }

        public TreeNode getChildAt(int i) {
            initializeChildren();

            return children.get(i);
        }

        public int getIndex(TreeNode treeNode) {
            initializeChildren();

            for (int i = 0, n = children.size(); i < n; i++) {
                if (children.get(i) == treeNode) {
                    return i;
                }
            }

            return -1;
        }

        public boolean isLeaf() {
            return box.getBoxCount() == 0;
        }

        public TreeNode getParent() {
            return parent;
        }

        public int getChildCount() {
            initializeChildren();
            
            return children.size();
        }

        public boolean getAllowsChildren() {
            return isLeaf();
        }

        public java.util.Enumeration<TreeNode> children() {
            initializeChildren();

            return Collections.enumeration(children);
        }

        private void initializeChildren() {
            if (children != null) {
                return;
            }

            int n = box.getBoxCount();
            children = new ArrayList<TreeNode>(n);

            for (int i = 0; i < n; i++) {
                children.add(new BoxTreeNode(box.getBox(i), this));
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer(40);
            sb.append("<html>"); // NOI18N

//            RaveElement element = (RaveElement)box.getElement();
            Element element = box.getElement();

            if (box instanceof LineBoxGroup) {
                sb.append("<i>" + getBundleString("TXT_LineboxGroup") + "</i>"); // NOI18N
            } else if (box.getBoxType() == BoxType.LINEBOX) {
                sb.append("<i>" + getBundleString("TXT_Linebox") + "</i>"); // NOI18N
            } else if (box.getBoxType() == BoxType.TEXT) {
                sb.append("<b>"); // NOI18N
                sb.append(((TextBox)box).getText());
                sb.append("</b>"); // NOI18N
            } else if (box.getBoxType() == BoxType.SPACE) {
                sb.append(getBundleString("TXT_Space"));
            } else if (element != null) {
                sb.append("&lt;"); // NOI18N
                sb.append(element.getTagName());
                sb.append("&gt;"); // NOI18N
                sb.append(' '); // NOI18N

                WebForm webForm = box.getWebForm();
//                if (element.getDesignBean() != null && (box.getParent() == null ||
//                        element.getDesignBean() != box.getParent().getDesignBean())) {
//                MarkupDesignBean markupDesignBean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//                MarkupDesignBean markupDesignBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
//                if (markupDesignBean != null
                if (element != null
//                && (box.getParent() == null || markupDesignBean != box.getParent().getDesignBean())) {
//                && (box.getParent() == null || markupDesignBean != CssBox.getMarkupDesignBeanForCssBox(box))) {
                && (box.getParent() == null || element != CssBox.getElementForComponentRootCssBox(box))) {
                    // TODO - filter out XHTML beans
                    sb.append("<i>"); // NOI18N
//                    sb.append(element.getDesignBean().getInstanceName());
//                    sb.append(markupDesignBean.getInstanceName());
                    sb.append(webForm.getDomProviderService().getInstanceName(element));
                    sb.append("</i>"); // NOI18N
                }
            }

            sb.append("</html>"); // NOI18N

            return sb.toString();
        }

        public CssBox getBox() {
            return box;
        }
    }

    private static class BoxNode extends AbstractNode {
        /** Name of property set for general properties */
        public static final String GENERAL = "general"; // NOI18N
        private CssBox box;
//        protected DataObject dobj;

        public BoxNode(CssBox box/*, DataObject dobj*/) {
            super(Children.LEAF);
            this.box = box;
//            this.dobj = dobj;
        }

        public Action[] getActions(boolean context) {
            return new Action[] { SystemAction.get(PropertiesAction.class) };
        }

        protected Sheet createSheet() {
            Sheet s = Sheet.createDefault();
            Sheet.Set ss = s.get("general"); // NOI18N

            if (ss == null) {
                ss = new Sheet.Set();
                ss.setName("general"); // NOI18N
                ss.setDisplayName(getBundleString("LBL_General")); // NOI18N
                ss.setShortDescription(getBundleString("HINT_General")); // NOI18N
                s.put(ss);
            }

            // Element name
	    Node.Property p;

	    if (box instanceof TextBox) {
		p = getElementNameProperty(box);
		p.setName("Text"); // NOI18N
		p.setDisplayName(getBundleString("LBL_Text")); // parens: ensure first
		ss.put(p);
	    } else {
		p = getElementNameProperty(box);
		p.setName("ElementName"); // NOI18N
		p.setDisplayName(getBundleString("LBL_Element")); // parens: ensure first

		//p.setShortDescription(NbBundle.getMessage(BoxNode.class, "ComponentIdHint")); // NOI18N
		ss.put(p);

		p = getElementIdProperty(box);
		p.setName("ElementId"); // NOI18N
		p.setDisplayName(getBundleString("LBL_Id"));
		ss.put(p);

		p = getAttributesProperty(box);
		p.setName("attributes"); // NOI18N
		p.setDisplayName(getBundleString("LBL_Attributes"));
		ss.put(p);

		p = getBeanNameDebugProperty(box);
		p.setName("BeanName"); // NOI18N
		p.setDisplayName(getBundleString("LBL_BeanName"));
		ss.put(p);
	    }

//            if (box.getDesignBean() != null) {
//            if (CssBox.getMarkupDesignBeanForCssBox(box) != null) {
            if (CssBox.getElementForComponentRootCssBox(box) != null) {
                ss = s.get("jsf"); // NOI18N

                if (ss == null) {
                    ss = new Sheet.Set();
                    ss.setName("jsf"); // NOI18N
                    ss.setDisplayName(getBundleString("LBL_Jsf")); // NOI18N
                    s.put(ss);
                }

		p = getRenderStreamProperty(box);
		p.setName("renderStream"); // NOI18N
		p.setDisplayName(getBundleString("LBL_RenderedHtml")); // NOI18N
		ss.put(p);
            }

            ss = s.get("css"); // NOI18N

            if (ss == null) {
                ss = new Sheet.Set();
                ss.setName("css"); // NOI18N
                ss.setDisplayName(getBundleString("LBL_CssStyles"));
                s.put(ss);
            }

	    try {
	    p = getLocalStylesProperty(box);
	    p.setName("localStyles"); // NOI18N
	    p.setDisplayName(getBundleString("LBL_LocalStyles"));
	    ss.put(p);

	    p = new Reflection<String>(box, String.class, "getStyles", null); // NOI18N
	    p.setName("styles"); // NOI18N
	    p.setDisplayName(getBundleString("LBL_Styles"));
	    ss.put(p);

	    p = new Reflection<String>(box, String.class, "getComputedStyles", null); // NOI18N
	    p.setName("compstyles"); // NOI18N
	    p.setDisplayName(getBundleString("LBL_ComputedStyles"));
	    ss.put(p);

	    p = new Reflection<String>(box, String.class, "getRules", null); // NOI18N
	    p.setName("rules"); // NOI18N
	    p.setDisplayName(getBundleString("LBL_Rules"));
	    ss.put(p);

	    p = new Reflection<Color>(box, Color.class, "getBg", null); // NOI18N
	    p.setName("bg"); // NOI18N
	    p.setDisplayName(getBundleString("LBL_Background"));
	    ss.put(p);
	    } catch(NoSuchMethodException nsme) {
		nsme.printStackTrace();
	    }

            ss = s.get("boxmodel"); // NOI18N

            if (ss == null) {
                ss = new Sheet.Set();
                ss.setName("boxmodel"); // NOI18N
                ss.setDisplayName(getBundleString("LBL_BoxModel"));

                //ss.setShortDescription(NbBundle.getMessage(BoxNode.class, "FormatHint")); // NOI18N
                s.put(ss);
            }

            try {
                //containing block, extents
                p = new Reflection<Point>(box, Point.class, "getPosition", null); // NOI18N
                p.setName("position"); // NOI18N
                p.setDisplayName(getBundleString("LBL_Position"));
                ss.put(p);

                p = new Reflection<Point>(box, Point.class, "getRelPosition", null); // NOI18N
                p.setName("relposition"); // NOI18N
                p.setDisplayName(getBundleString("LBL_RelPosition"));
                ss.put(p);

                p = new Reflection<Dimension>(box, Dimension.class, "getSize", null); // NOI18N
                p.setName("size"); // NOI18N
                p.setDisplayName(getBundleString("LBL_Size"));
                ss.put(p);

                p = new Reflection<Dimension>(box, Dimension.class, "getContentSize", null); // NOI18N
                p.setName("contentsize"); // NOI18N
                p.setDisplayName(getBundleString("LBL_ContentSize"));
                ss.put(p);

                p = new Reflection<String>(box, String.class, "getBoxTypeName", null); // NOI18N
                p.setName("boxtype"); // NOI18N
                p.setDisplayName(getBundleString("LBL_Positioning"));
                ss.put(p);

                p = new Reflection<Rectangle>(box, Rectangle.class, "getPositionRect", null); // NOI18N
                p.setName("positionrect"); // NOI18N
                p.setDisplayName(getBundleString("LBL_PosConstraints"));
                ss.put(p);

                p = new Reflection<Rectangle>(box, Rectangle.class, "getMarginRectangle", null); // NOI18N
                p.setName("marginrectangle"); // NOI18N
                p.setDisplayName(getBundleString("LBL_Margins"));
                ss.put(p);

                p = new Reflection<Rectangle>(box, Rectangle.class, "getCBRectangle", null); // NOI18N
                p.setName("cbrectangle"); // NOI18N
                p.setDisplayName(getBundleString("LBL_ContainingBlock"));
                ss.put(p);

                p = new Reflection<Rectangle>(box, Rectangle.class, "getExtentsRectangle", null); // NOI18N
                p.setName("extentsrectangle"); // NOI18N
                p.setDisplayName(getBundleString("LBL_Extents"));
                ss.put(p);

                p = new Reflection<Rectangle>(box, Rectangle.class, "getPaddingRectangle", null); // NOI18N
                p.setName("paddingrectangle"); // NOI18N
                p.setDisplayName(getBundleString("LBL_Padding"));
                ss.put(p);

                p = new Reflection<Rectangle>(box, Rectangle.class, "getBorderWidthRectangle", null); // NOI18N
                p.setName("borderWidthrectangle"); // NOI18N
                p.setDisplayName(getBundleString("LBL_BorderWidths"));
                ss.put(p);

                p = new Reflection<Boolean>(box, Boolean.TYPE, "isInlineBox", null); // NOI18N
                p.setName("inline"); // NOI18N
                p.setDisplayName(getBundleString("LBL_Inline"));
                ss.put(p);

                p = new Reflection<Boolean>(box, Boolean.TYPE, "isReplacedBox", null); // NOI18N
                p.setName("replaced"); // NOI18N
                p.setDisplayName(getBundleString("LBL_Replaced"));
                ss.put(p);

                p = new Reflection<Integer>(box, Integer.TYPE, "getBoxCount", null); // NOI18N
                p.setName("boxcount"); // NOI18N
                p.setDisplayName(getBundleString("LBL_Children"));
                ss.put(p);
            } catch (NoSuchMethodException nsme) {
                ErrorManager.getDefault().notify(nsme);
            }

            ss = s.get("csspainting"); // NOI18N

            if (ss == null) {
                ss = new Sheet.Set();
                ss.setName("csspainting"); // NOI18N
                ss.setDisplayName(getBundleString("LBL_BoxPainting"));
                s.put(ss);
            }

            try {
                p = new Reflection<Boolean>(box, Boolean.TYPE, "getPaintPositions", "setPaintPositions"); // NOI18N
                p.setName("paintpos"); // NOI18N
                p.setDisplayName(getBundleString("LBL_PaintPositioning"));
                ss.put(p);

                p = new Reflection<Boolean>(box, Boolean.TYPE, "getPaintText", "setPaintText"); // NOI18N
                p.setName("painttext"); // NOI18N
                p.setDisplayName(getBundleString("LBL_PaintText"));
                ss.put(p);

                p = new Reflection<Boolean>(box, Boolean.TYPE, "getPaintSpaces", "setPaintSpaces"); // NOI18N
                p.setName("paintspaces"); // NOI18N
                p.setDisplayName(getBundleString("LBL_PaintSpaces"));
                ss.put(p);

            
            } catch (NoSuchMethodException nsme) {
                ErrorManager.getDefault().notify(nsme);
            }

            return s;
        }

//        /**
//         * Set the data object this component is associated with (if any).
//         * When set, this node will return the cookies of the data object as well.
//         * Therefore, as an example, if you select an image component in a page,
//         * the
//         */
//        public void setDataObject(DataObject dobj) {
//            this.dobj = dobj;
//        }

        /** Get a cookie. Call super first, but if null, also check the
         * associated data object.
         */
        public <T extends Node.Cookie> T getCookie(Class<T> cl) {
            T cookie = super.getCookie(cl);

            if (cookie != null) {
                return cookie;
            }

//            if (dobj != null) {
//                return dobj.getCookie(cl);
//            }

            return null;
        }

        public boolean canRename() {
            return false;
        }

        public boolean canDestroy() {
            // No point since it gets recreated after every edit
            return false;
        }

        /** Can this node be copied?
        * @return <code>true</code>
        */
        public boolean canCopy() {
            return false;
        }

        /** Can this node be cut?
        * @return <code>false</code>
        */
        public boolean canCut() {
            // No point since it gets recreated after every edit
            return false;
        }

        /** Don't allow pastes */
        protected void createPasteTypes(Transferable t, List s) {
        }

        /*
        public Image getIcon(int type) {
            // set icon base lazily. The icon should only be needed by nodes
            // displayed in for example the nonvisual gutter
            if (iconbase != null) {
                setIconBase(iconbase);
                iconbase = null;
            }
            return super.getIcon(type);
        }
        */

        /** The nodes should display the component name. This ensures
         * that when you're looking at the node in the tray for example
         * (an explorer which shows the node names) you see the component
         * id.)
         */
        public final String getDisplayName() {
            return getBundleString("LBL_SelectedBox"); // What do we put here?
        }
    }
    
    /////////////
    // Properties 
    
    private static Node.Property getElementNameProperty(final CssBox box) {
	return new PropertySupport.ReadOnly<String>("elementName", String.class, "elementName", getBundleString("LBL_NameElement")) {
	    public String getValue() {
		Element element = box.getElement();
		if (element != null) {
		    return element.getTagName();
		} else if (box.getBoxType() == BoxType.TEXT) {
		    return ((TextBox)box).getText();
		} else if (box.getBoxType() == BoxType.SPACE) {
		    return getBundleString("TXT_Whitespace");
		} else {
		    return getBundleString("TXT_NotElement");
		}
	    }
	};
    }
    
    private static Node.Property getElementIdProperty(final CssBox box) {
	return new PropertySupport.ReadOnly<String>("elementId", String.class, "elementId", getBundleString("LBL_IdElement")) {
	    public String getValue() {
		Element element = box.getElement();
		if (element != null) {
		    String id = element.getAttribute("id"); // NOI18N

		    if (id != null) {
			return id;
		    }

		    id = element.getAttribute("name"); // NOI18N

		    if (id != null) {
			return id;
		    }
		}

		return ""; // NOI18N
	    }
	};
    }
    
    private static Node.Property getAttributesProperty(final CssBox box) {
	return new PropertySupport.ReadOnly<String>("attributes", String.class, "attributes", getBundleString("LBL_AttributesElement")) {
	    public String getValue() {
		Element element = box.getElement();
	        if (element != null) {
	            //return element.getAttributes().toString();
	            org.w3c.dom.NamedNodeMap map = element.getAttributes();
	            StringBuffer sb = new StringBuffer(200);
	
	            for (int i = 0, n = map.getLength(); i < n; i++) {
	                if (i > 0) {
	                    sb.append(','); // NOI18N
	                    sb.append(' '); // NOI18N
	                }
	
	                org.w3c.dom.Node node = map.item(i);
	                sb.append(node.getNodeName());
	                sb.append('='); // NOI18N
	                sb.append('\"'); // NOI18N
	                sb.append(node.getNodeValue());
	                sb.append('\"'); // NOI18N
	            }
	
	            return sb.toString();
	        } else {
	            return ""; // NOI18N
	        }
	    }
	};
    }
    
    private static Node.Property getBeanNameDebugProperty(final CssBox box) {
	return new PropertySupport.ReadOnly<String>("beanNameDebug", String.class, "beanNameDebug", getBundleString("LBL_BeanNameDebug")) {
	    public String getValue() {
//		MarkupDesignBean bean = box.getDesignBean();
//                MarkupDesignBean bean = CssBox.getMarkupDesignBeanForCssBox(box);
                Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
//		String s = DesignerUtils.getBeanName(bean);
//                String s = getBeanName(bean);
                WebForm webForm = box == null ? null : box.getWebForm();
                String s = getComponentName(webForm, componentRootElement);

		if (s == null) {
		    return ""; // NOI18N
		}

		return s;
	    }
	};
    }
    
    // XXX Moved from DesignerUtils.
    /**
     * Get the name of the given bean, e.g. "button1", "textField5", etc.
     *
     * @return the name of the bean, or null if a bean can not be found for this view's element
     */
//    private static String getBeanName(DesignBean bean) {
    private static String getComponentName(WebForm webForm, Element componentRootElement) {
        if (webForm == null) {
            return null;
        }
//        if (bean != null) {
        if (componentRootElement != null) {
//            return "<" + bean.getInstanceName() + ">";
            return "<" + webForm.getDomProviderService().getInstanceName(componentRootElement) + ">";
        }
        
        return null;
    }

    
    private static Node.Property getRenderStreamProperty(final CssBox box) {
	return new PropertySupport.ReadOnly<String>("renderStream", String.class, "renderStream", getBundleString("LBL_RenderStream")) {
	    public String getValue() {
//		MarkupDesignBean bean = box.getDesignBean();
//                MarkupDesignBean bean = CssBox.getMarkupDesignBeanForCssBox(box);
                Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
//		if (bean == null) {
                if (componentRootElement == null) {
		    return ""; // NOI18N
		}

//		FacesModel model = box.getWebForm().getModel();
//
//		if (model == null) { // testsuite
//
//		    return ""; // NOI18N
//		}

//		if (bean == null) {
//		    return ""; // NOI18N
//		}

//		FacesPageUnit facesunit = model.getFacesUnit();
//		DocumentFragment df = facesunit.getFacesRenderTree(bean, model.getLiveUnit());
//
//		// TODO - strip out designtime attributes
//		if (df == null) {
//		    return ""; // NOI18N
//		}
                
//                Element element = bean.getElement();

//		return InSyncService.getProvider().getHtmlStream(df);
//                return WebForm.getDomProviderService().getHtmlStream(df);
                WebForm webForm = box == null ? null : box.getWebForm();
                if (webForm == null) {
                    return ""; // NOI18N
                }
                return webForm.getDomProviderService().getHtmlStream(componentRootElement);
	    }
	};
    }
    
    private static Node.Property getLocalStylesProperty(final CssBox box) {
	return new PropertySupport.ReadOnly<String>("localStyles", String.class, "localStyles", getBundleString("LBL_LocalStyles")) {
	    public String getValue() {
		Element element = box.getElement();
		if (element != null) {
		    String style = element.getAttribute(HtmlAttribute.STYLE);

		    if (style != null) {
			return style;
		    }
		}

		return ""; // NOI18N
	    }
	};
    }
    
    
    private static String getBundleString(String key) {
        return NbBundle.getMessage(DomInspector.class, key);
    }
}
