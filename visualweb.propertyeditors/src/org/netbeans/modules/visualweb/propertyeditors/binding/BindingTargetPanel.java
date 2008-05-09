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
package org.netbeans.modules.visualweb.propertyeditors.binding;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.faces.component.UIData;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetNode.Null;
import org.netbeans.modules.visualweb.propertyeditors.binding.nodes.ContextTargetNode;
import org.netbeans.modules.visualweb.propertyeditors.binding.nodes.DataProviderTargetNodeFactory;
import org.netbeans.modules.visualweb.propertyeditors.binding.nodes.MapTargetNodeFactory;
import org.netbeans.modules.visualweb.propertyeditors.binding.nodes.PropertyTargetNode;
import org.netbeans.modules.visualweb.propertyeditors.binding.nodes.ResultSetTargetNodeFactory;
import org.netbeans.modules.visualweb.propertyeditors.binding.nodes.UIDataVarNode;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;
import com.sun.data.provider.DataProvider;
import javax.sql.RowSet;

public class BindingTargetPanel extends JPanel {
    JLabel targetLabel = new JLabel();
    JScrollPane targetScroll = new JScrollPane();
    JTree tree = new HiddenSelectionJTree();
    BindingTargetNode.Root rootNode = new BindingTargetNode.Root();
    DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    DefaultTreeSelectionModel treeSelectionModel = new DefaultTreeSelectionModel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    GridBagConstraints customPanelConstraints = new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(-4, 12, 11, 0), 0, 0);

    private DesignBean targetBean = null;

    private static final Bundle bundle = Bundle.getBundle(BindingTargetPanel.class);

    public BindingTargetPanel() {
        try {
            jbInit();
        }
        catch (Exception ex) {
           ex.printStackTrace();
        }
        rootNode.setTreeModel(treeModel);
        tree.getAccessibleContext().setAccessibleName(bundle.getMessage("TARGET_BINDING_TREE_ACCESS_NAME"));
        tree.getAccessibleContext().setAccessibleDescription(bundle.getMessage("TARGET_BINDING_TREE_ACCESS_DESC"));
        targetLabel.setLabelFor(tree);
        targetLabel.setDisplayedMnemonic(bundle.getMessage("TARGET_LABEL_DISPLAYED_MNEMONIC").charAt(0));
    }

    protected BindingTargetCallback bindingCallback;
    public BindingTargetPanel(BindingTargetCallback bindingCallback) {
        this();
        this.bindingCallback = bindingCallback;
    }

    protected DesignContext[] sortContexts(DesignContext[] contexts) {
        ArrayList sortList = new ArrayList();

        TreeMap nameMap = new TreeMap();
        for (int i = 0; i < contexts.length; i++) {
            nameMap.put(contexts[i].getDisplayName(), contexts[i]);
        }

        String[] names = (String[])nameMap.keySet().toArray(new String[nameMap.size()]);

        // request scope
        for (int i = 0; i < names.length; i++) {
            DesignContext c = (DesignContext)nameMap.get(names[i]);
            if ("request".equals(c.getContextData(Constants.ContextData.SCOPE))) { //NOI18N
                sortList.add(c);
            }
        }
        // session scope
        for (int i = 0; i < names.length; i++) {
            DesignContext c = (DesignContext)nameMap.get(names[i]);
            if ("session".equals(c.getContextData(Constants.ContextData.SCOPE))) { //NOI18N
                sortList.add(c);
            }
        }
        // application scope
        for (int i = 0; i < names.length; i++) {
            DesignContext c = (DesignContext)nameMap.get(names[i]);
            if ("application".equals(c.getContextData(Constants.ContextData.SCOPE))) { //NOI18N
                sortList.add(c);
            }
        }
        // none scope
        for (int i = 0; i < names.length; i++) {
            DesignContext c = (DesignContext)nameMap.get(names[i]);
            if ("none".equals(c.getContextData(Constants.ContextData.SCOPE))) { //NOI18N
                sortList.add(c);
            }
        }

        return (DesignContext[])sortList.toArray(new DesignContext[sortList.size()]);
    }

    protected DesignBean findUIDataParentWithVar(DesignBean fromBean) {
        DesignBean b = fromBean.getBeanParent();
        while (b != null && !(b.getInstance() instanceof UIData)) {
            b = b.getBeanParent();
        }
        if (b != null && b.getInstance() instanceof UIData) {
            DesignProperty varProp = b.getProperty("var"); // NOI18N
            if (varProp != null) {
                Object var = varProp.getValue();
                if (var != null) {
                    return b;
                }
            }
        }
        return null;
    }

    protected DesignContext showingContext = null;
    public void sourceContextChanged(DesignContext context) {
        if (showingContext != null && showingContext == context) return;
        showingContext = context;
        rootNode.removeAll();
        rootNode.add(new Null(rootNode));
        ArrayList expands = new ArrayList();
        if (context != null && context.getProject() != null) {
            //DesignContext[] acs = context.getProject().getDesignContexts();
            DesignContext[] acs = getDesignContexts(context);
            acs = sortContexts(acs);
            for (int i = 0; acs != null && i < acs.length; i++) {
                 
                //System.out.println("ADDING NEW CONTEXT: " + context.getDisplayName());
                if ((acs[i].getBeans() != null) && (acs[i].getRootContainer().getChildBeans().length > 0)) {
                    DesignBean[] dpKids = acs[i].getBeansOfType(DataProvider.class);
                    DesignBean[] rsKids = acs[i].getBeansOfType(RowSet.class);
                    // Do not show the data provider in the object binding dialog.
                    // We have explicit data provider binding dialog
                    if (acs[i].getRootContainer().getChildBeans().length > (dpKids.length + rsKids.length)) {
                        BindingTargetNode node = new ContextTargetNode(rootNode, acs[i]);
                        expands.add(new TreePath(new Object[]{rootNode, node}));
                        rootNode.add(node);
                    }
                }
            }
        }
        treeModel.reload();
        tree.validate();
        for (int i = 0; i < expands.size(); i++) {
            tree.expandPath((TreePath)expands.get(i));
        }
        refreshTarget();
    }
    
    // For performance improvement. No need to get all the contexts in the project
    private DesignContext[] getDesignContexts(DesignContext context){
        DesignProject designProject = context.getProject();
        DesignContext[] contexts;
        if (designProject instanceof FacesDesignProject) {
            contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                "request",
                "session",
                "application"
            });
        } else {
            contexts = new DesignContext[0];
        }
        DesignContext[] designContexts = new DesignContext[contexts.length + 1];
        designContexts[0] = context;
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }

    protected UIDataVarNode varNode = null;

    protected DesignBean showingBean = null;
    public void sourceBeanChanged(DesignBean bean) {
        if (showingBean == bean) return;
        showingBean = bean;
        if (varNode != null) {
            rootNode.remove(varNode);
            treeModel.reload();
            tree.validate();
            varNode = null;
        }
        if (showingBean != null) {
            DesignBean uiDataBean = findUIDataParentWithVar(showingBean);
            if (uiDataBean != null) {
                varNode = new UIDataVarNode(rootNode, uiDataBean);
                rootNode.add(1, varNode);
                treeModel.reload();
                tree.validate();
            }
        }
        refreshTarget();
    }

    protected DesignProperty showingProp = null;
    public void sourcePropertyChanged(DesignProperty prop) {
        if (showingProp == prop) return;
        showingProp = prop;
        refreshTarget();
    }

    public void refreshTarget() {
        if (showingProp != null) {
            String vx = showingProp.getValueSource();
            boolean bound = vx != null && vx.startsWith("#{") && vx.endsWith("}"); //NOI18N
            if (bound) {
                selectNodeForExpression(vx.substring(2, vx.length() - 1));
            } else {
                tree.setSelectionRow(0);
            }
        } else {
            tree.setSelectionRow(0);
        }
        repaint(100);
    }

    TreeNode findChildNodeForExprPart(TreeNode node, String exprPart) {
        Enumeration e = node.children();
        while (e.hasMoreElements()) {
            TreeNode n = (TreeNode)e.nextElement();
            if (n instanceof BindingTargetNode) {
                BindingTargetNode btn = (BindingTargetNode)n;
                if (exprPart.equals(btn.getBindingExpressionPart())) {
                    return n;
                }
            }
        }
        return null;
    }

    void selectNodeForExpression(String expr) {
        //System.out.println("expr="+expr);
        StringTokenizer st = new StringTokenizer(expr, ".");
        ArrayList parts = new ArrayList();
        while (st.hasMoreElements()) {
            parts.add(st.nextElement());
            //System.out.println(" part: " + parts.get(parts.size() - 1));
        }
        TreeNode node = rootNode;
        TreeNode lastNode = node;
        while (parts.size() > 0) {
            node = findChildNodeForExprPart(lastNode, (String)parts.get(0));
            if (node != null) {
                parts.remove(0);
                lastNode = node;
            }
            else {
                break;
            }
        }
        if (lastNode != null && lastNode != rootNode) {
            tree.setSelectionPath(createPath(lastNode));
        }
        else {
            tree.setSelectionRow(0);
        }
    }

    TreePath createPath(TreeNode node) {
        ArrayList path = new ArrayList();
        while (node != null) {
            path.add(0, node);
            node = node.getParent();
        }
        return new TreePath((TreeNode[])path.toArray(new TreeNode[path.size()]));
    }

    ActionListener updateCallback = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            refreshExpr();
        }
    };

    String calcExpression(BindingTargetNode targetNode) {
        targetBean = null;
        ArrayList parts = new ArrayList();
        TreeNode n = targetNode;
        while (n != null) {
            if (n instanceof PropertyTargetNode) {
                PropertyTargetNode ptn = (PropertyTargetNode)n;
                if (ptn.isValidBindingTarget()) {
                    PropertyDescriptor[] propPath = ptn.getPropPath();
                    if (propPath != null) {
                        for (int i = propPath.length - 1; i >= 0; i--) {
                            parts.add(0, propPath[i].getName());
                        }
                    }
                    targetBean = ptn.getBean();
                    parts.add(0, ptn.getBean().getInstanceName());
                    DesignContext c = ptn.getBean().getDesignContext();
                    if (c instanceof FacesDesignContext) {
                        parts.add(0, ((FacesDesignContext)c).getReferenceName());
                    }
                    else {
                        parts.add(0, c.getDisplayName());
                    }
                    break;
                }
            }
            else if (n instanceof BindingTargetNode) {
                BindingTargetNode btn = (BindingTargetNode)n;
                if (btn.isValidBindingTarget()) {
                    String ep = btn.getBindingExpressionPart();
                    if (ep != null) {
                        //MBOHM fix 5086833
                        //escape single quotes within selectItems[' ... '], say, for selectItems['personid || \'-\' }} jobtitle']
                        String siStart = "selectItems['"; //NOI18N
                        String siEnd = "']";    //NOI18N
                        if (ep.startsWith(siStart) && ep.endsWith(siEnd) && ep.length() > siStart.length()+siEnd.length()) {
                            String epCrux = ep.substring(siStart.length(), ep.length() - siEnd.length());
                            epCrux = epCrux.replaceAll("\\'", "\\\\'");
                            ep = siStart + epCrux + siEnd;
                        }
                        parts.add(0, ep);
                    }
                }
            }
            n = n.getParent();
        }
        if (parts.size() > 0) {
            StringBuffer expr = new StringBuffer();
            expr.append("#{"); // NOI18N
            for (int i = 0; i < parts.size(); i++) {
                String part = "" + parts.get(i);
                if (i > 0 && !part.startsWith("[")) { // NOI18N
                    expr.append("."); // NOI18N
                }
                expr.append(part);
            }
            expr.append("}"); // NOI18N
            return expr.toString();
        }
        return "";
    }

    JComponent customPanel = null;
    boolean needsRefresh = false;
    void refreshExpr() {
        if (customPanel != null) {
            this.remove(customPanel);
            customPanel = null;
            needsRefresh = true;
        }
        TreePath tp = tree.getSelectionPath();
        if (tp != null) {
            Object o = tp.getLastPathComponent();
            if (o instanceof BindingTargetNode) {
                BindingTargetNode btn = (BindingTargetNode)o;
                if (!(btn instanceof Null)) {
                    bindingCallback.setNewExpressionText(calcExpression(btn));
                }
                customPanel = btn.getCustomDisplayPanel(updateCallback);
                if (customPanel != null) {
                    this.add(customPanel, customPanelConstraints);
                    needsRefresh = true;
                }
            }
            else {
                bindingCallback.setNewExpressionText("");  //NOI18N
            }
        }
        else {
            bindingCallback.setNewExpressionText("");  //NOI18N
        }
        if (needsRefresh) {
            bindingCallback.refresh();
        }
    }
    
    public DesignBean getTargetBean(){
        return targetBean;
    }

    static {
        //!JOE HACK HACK HACK! I don't have a static 'hook' to register node factories
        BindingTargetNode._registerBindingTargetNodeFactory(new ResultSetTargetNodeFactory());
        BindingTargetNode._registerBindingTargetNodeFactory(new MapTargetNodeFactory());
        BindingTargetNode._registerBindingTargetNodeFactory(new DataProviderTargetNodeFactory());
    }

    void jbInit() throws Exception {

        targetLabel.setText(bundle.getMessage("selectTarget")); //NOI18N
        targetLabel.setDisplayedMnemonic(bundle.getMessage("selectTargeDisplayedMnemonic").charAt(0)); //NOI18N
        tree.setModel(treeModel);
        tree.setEditable(false);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        //tree.setLargeModel(true);
        tree.setCellRenderer(new TargetTreeRenderer());
        tree.setSelectionModel(treeSelectionModel);
        treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeSelectionModel.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                refreshExpr();
            }
        });
        treeModel.addTreeModelListener(new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) {
                updateTreePainting();
            }
            public void treeNodesInserted(TreeModelEvent e) {
                updateTreePainting();
            }
            public void treeNodesRemoved(TreeModelEvent e) {
                updateTreePainting();
            }
            public void treeStructureChanged(TreeModelEvent e) {
                updateTreePainting();
            }
        });
        this.setLayout(gridBagLayout1);
        this.add(targetLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(8, 8, 2, 8), 0, 0));
        this.add(targetScroll, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));
        targetScroll.getViewport().add(tree, null);
    }

    private void updateTreePainting() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tree.validate();
                targetScroll.validate();
            }
        });
    }

    class TargetTreeRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            boolean enableNode = true;
            if (value instanceof BindingTargetNode) {
                BindingTargetNode btn = (BindingTargetNode)value;
                if (showingProp != null) {
                    Class showingPropType = showingProp.getPropertyDescriptor().getPropertyType();
                    if (showingPropType != null) {
                        // EAT: There has to be a better way :(
                        // Not handling Array types of these at the moment
                        if (showingPropType.isPrimitive()) {
                            if (showingPropType == Boolean.TYPE) {
                                showingPropType = Boolean.class;
                            } else if (showingPropType == Character.TYPE) {
                                showingPropType = Character.class;
                            } else if (showingPropType == Byte.TYPE) {
                                showingPropType = Byte.class;
                            } else if (showingPropType == Short.TYPE) {
                                showingPropType = Short.class;
                            } else if (showingPropType == Integer.TYPE) {
                                showingPropType = Integer.class;
                            } else if (showingPropType == Long.TYPE) {
                                showingPropType = Long.class;
                            } else if (showingPropType == Float.TYPE) {
                                showingPropType = Float.class;
                            } else if (showingPropType == Double.TYPE) {
                                showingPropType = Double.class;
                            }
                        }
                        Class tc = btn.getTargetTypeClass();
                        if (tc != null) {
                            enableNode = showingPropType.isAssignableFrom(tc);
                        }
                    }
                }
                String customText = btn.getDisplayText(enableNode);
                if (customText != null) {
                    this.setText(customText);
                }
                if (btn.hasDisplayIcon()) {
                    Icon customIcon = btn.getDisplayIcon(enableNode);
                    if (customIcon != null) {
                        this.setIcon(customIcon);
                    }
                }
            }
//            String txt = getText();
//            if (txt != null && txt.startsWith("<html>")) {
//                this.setText(txt.substring(6));
//            }
            return this;
        }
    }
    
    /**
     * An extension of JTree that allows a selected node to remain selected even
     * if one of its parent nodes is toggled shut. This extension is intended to
     * fix CR 6288174.
     */
    static class HiddenSelectionJTree extends JTree {
        
        protected boolean removeDescendantSelectedPaths(TreePath path, boolean includePath) {
            return false;
        }
        
    }

    public static Icon BEAN_ICON = new ImageIcon(BindingTargetPanel.class.getResource("img/property.gif")); //NOI18N
    public static Icon TAG_ICON = new ImageIcon(BindingTargetPanel.class.getResource("img/html_element.png")); //NOI18N
}
