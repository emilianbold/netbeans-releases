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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * Customizer.java
 *
 * Created on 23.Mar 2004, 11:31
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicBorders;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.customizer.regex.CheckedTreeBeanView;
import org.netbeans.modules.mobility.project.ui.customizer.regex.FileObjectCookie;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona
 */
public class CustomizerFiltering extends JPanel implements CustomizerPanel, VisualPropertyGroup, ActionListener {
    
    private static final String GENERAL_EXCLUDES = "^(.*/)?(([^/]*\\.class)|([^/]*\\.form)|(\\.nbintdb)|([^/]*\\.mvd)|([^/]*\\.wsclient)"; //NOI18N
    private static final String STANDARD_ANT_EXCLUDES = "([^/]*~)|(#[^/]*#)|(\\.#[^/]*)|(%[^/]*%)|(\\._[^/]*)|(CVS)|(CVS/.*)|(\\.cvsignore)|(SCCS)|(SCCS/.*)|(vssver\\.scc)|(\\.svn)|(\\.svn/.*)|(\\.DS_Store)";//NOI18N
    private static final String TEST_EXCLUDES = "([^/]*Test\\.java)|(test)|(test/.*)";//NOI18N
    
    static final String[] PROPERTY_GROUP = new String[] {DefaultPropertiesDescriptor.FILTER_USE_STANDARD, DefaultPropertiesDescriptor.FILTER_EXCLUDE_TESTS, DefaultPropertiesDescriptor.FILTER_EXCLUDES, DefaultPropertiesDescriptor.FILTER_MORE_EXCLUDES};
    
    private VisualPropertySupport vps;
    private CheckedTreeBeanView treeView;
    private ExplorerManager manager;
    /** The treee where to choose from */
    private ExplorerPanel sourceExplorer;
    private Pattern filter;
    private Map<String,Object> properties;
    private String configuration;
    private FileObject srcRoot;
    private String excludesTranslatedPropertyName;
    
    /** Creates new form CustomizerConfigs */
    public CustomizerFiltering() {
        initComponents();
        
        jPanelTree.setBorder(BasicBorders.getTextFieldBorder());
        sourceExplorer = new ExplorerPanel();
        
        manager = sourceExplorer.getExplorerManager();
        
        treeView = new CheckedTreeBeanView();

        initAccessibility();

        jLabelTree.setLabelFor(treeView);
        treeView.setPopupAllowed(false);
        treeView.setRootVisible(false);
        treeView.setDefaultActionAllowed(false);
        sourceExplorer.setLayout(new BorderLayout());
        sourceExplorer.add(treeView, BorderLayout.CENTER);
        sourceExplorer.setPreferredSize(new Dimension(200, 250));
        jPanelTree.add(sourceExplorer, BorderLayout.CENTER);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        defaultCheck = new javax.swing.JCheckBox();
        jCheckBoxCVS = new javax.swing.JCheckBox();
        jCheckBoxTests = new javax.swing.JCheckBox();
        jLabelTree = new javax.swing.JLabel();
        jPanelTree = new javax.swing.JPanel();
        jLabelExcludes = new javax.swing.JLabel();
        jTextFieldExcludes = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(defaultCheck, NbBundle.getMessage(CustomizerFiltering.class, "LBL_Use_Default")); // NOI18N
        defaultCheck.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        add(defaultCheck, gridBagConstraints);
        defaultCheck.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerFiltering.class, "ASCN_UseValuesFromDefault")); // NOI18N
        defaultCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerFiltering.class, "ASCD_UseValuesFromDefault")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxCVS, NbBundle.getMessage(CustomizerFiltering.class, "LBL_CustFilter_DefaultExcludes")); // NOI18N
        jCheckBoxCVS.setToolTipText(NbBundle.getMessage(CustomizerFiltering.class, "TTT_CustFilter_DefaultExcludes")); // NOI18N
        jCheckBoxCVS.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jCheckBoxCVS, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxTests, NbBundle.getMessage(CustomizerFiltering.class, "LBL_CustFilter_ExcludeTests")); // NOI18N
        jCheckBoxTests.setToolTipText(NbBundle.getMessage(CustomizerFiltering.class, "TTT_CustFilter_ExcludeTests")); // NOI18N
        jCheckBoxTests.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jCheckBoxTests, gridBagConstraints);

        jLabelTree.setLabelFor(sourceExplorer);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTree, NbBundle.getMessage(CustomizerFiltering.class, "LBL_CustFilter_SelectFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabelTree, gridBagConstraints);

        jPanelTree.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jPanelTree, gridBagConstraints);

        jLabelExcludes.setLabelFor(jTextFieldExcludes);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelExcludes, NbBundle.getMessage(CustomizerFiltering.class, "LBL_CustFilter_Excludes")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabelExcludes, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 0);
        add(jTextFieldExcludes, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerFiltering.class, "ACSN_CustFilter"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerFiltering.class, "ACSD_CustFilter"));

        treeView.getAccessibleContext().setAccessibleName( jLabelTree.getText());
        treeView.getAccessibleContext().setAccessibleDescription( jLabelTree.getText() );
    }
    
    public void initValues(ProjectProperties props, String configuration) {
        this.vps = VisualPropertySupport.getDefault(props);
        this.properties = props;
        this.configuration = configuration;
        this.srcRoot = props.getSourceRoot();
        treeView.setSrcRoot(srcRoot);
        vps.register(defaultCheck, configuration, this);
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_GROUP;
    }
    
    public void initGroupValues(final boolean useDefault) {
        jCheckBoxCVS.removeActionListener(this);
        jCheckBoxTests.removeActionListener(this);
        vps.register(jCheckBoxCVS, DefaultPropertiesDescriptor.FILTER_USE_STANDARD, useDefault);
        vps.register(jCheckBoxTests, DefaultPropertiesDescriptor.FILTER_EXCLUDE_TESTS, useDefault);
        vps.register(jTextFieldExcludes, DefaultPropertiesDescriptor.FILTER_MORE_EXCLUDES, useDefault);
        this.excludesTranslatedPropertyName = VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.FILTER_EXCLUDES, useDefault);
        initTree();
        treeView.setEditable(!useDefault);
        jLabelTree.setEnabled(!useDefault);
        jLabelExcludes.setEnabled(!useDefault);
        jCheckBoxCVS.addActionListener(this);
        jCheckBoxTests.addActionListener(this);
    }
    
    private void initTree() {
        String sFilter;
        if (jCheckBoxCVS.isSelected()) {
            if (jCheckBoxTests.isSelected()) {
                sFilter = GENERAL_EXCLUDES + "|" + STANDARD_ANT_EXCLUDES + "|" + TEST_EXCLUDES + ")$"; //NOI18N
            } else {
                sFilter = GENERAL_EXCLUDES + "|" + STANDARD_ANT_EXCLUDES + ")$"; //NOI18N
            }
        } else {
            if (jCheckBoxTests.isSelected()) {
                sFilter = GENERAL_EXCLUDES + "|" + TEST_EXCLUDES + ")$"; //NOI18N
            } else {
                sFilter = GENERAL_EXCLUDES + ")$"; //NOI18N
            }
        }
        this.filter = Pattern.compile(sFilter);
        try {
            final DataObject dob = DataObject.find(srcRoot);
            manager.setRootContext(new FOBNode(dob.getNodeDelegate().cloneNode(), dob.getPrimaryFile()));
        } catch (DataObjectNotFoundException dnfe) {
            manager.setRootContext(Node.EMPTY);
        }
        treeView.registerProperty(properties, excludesTranslatedPropertyName, filter);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultCheck;
    private javax.swing.JCheckBox jCheckBoxCVS;
    private javax.swing.JCheckBox jCheckBoxTests;
    private javax.swing.JLabel jLabelExcludes;
    private javax.swing.JLabel jLabelTree;
    private javax.swing.JPanel jPanelTree;
    private javax.swing.JTextField jTextFieldExcludes;
    // End of variables declaration//GEN-END:variables
    
    boolean acceptFileObject(final FileObject fo) {
        final String path = FileUtil.getRelativePath(srcRoot, fo);
        return path != null && !filter.matcher(path).matches();
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final ActionEvent e) {
        initTree();
    }
    
    private class FOBNode extends FilterNode implements FileObjectCookie {
        private final FileObject fo;
        public FOBNode(Node n, FileObject fo) {
            super(n, fo.isData() ? org.openide.nodes.Children.LEAF : new FOBChildren(n));
            this.fo = fo;
            disableDelegation(DELEGATE_SET_NAME | DELEGATE_GET_NAME | DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_DISPLAY_NAME);
            setName(fo.getNameExt());
            setDisplayName(fo.getNameExt());
        }
        
        public Node.Cookie getCookie(final Class type) {
            if (FileObjectCookie.class.isAssignableFrom(type)) return this;
            return super.getCookie(type);
        }
        
        public FileObject getFileObject() {
            return fo;
        }
    }
    
    private class FOBChildren extends FilterNode.Children {
        
        public FOBChildren(Node or) {
            super(or);
        }
        
        
        protected Node[] createNodes(final Node n) {
            final DataObject dob = (DataObject) n.getCookie(DataObject.class);
            if (dob == null) return new Node[0];
            final ArrayList<Node> nodes = new ArrayList<Node>();
            for (FileObject fo : (java.util.Set<FileObject>)dob.files()) {
                if (acceptFileObject(fo)) {
                    nodes.add(new FOBNode(n.cloneNode(), fo));
                }
            }
            return nodes.toArray(new Node[nodes.size()]);
        }
    }
    
    private static class ExplorerPanel extends JPanel implements ExplorerManager.Provider {
        private final ExplorerManager manager = new ExplorerManager();

        private ExplorerPanel() {
            //Just to avoid creation of accessor class
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
    }
}
