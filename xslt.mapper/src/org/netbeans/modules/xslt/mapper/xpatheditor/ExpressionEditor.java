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
package org.netbeans.modules.xslt.mapper.xpatheditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.soa.ui.axinodes.AxiomChildren;
import org.netbeans.modules.soa.ui.axinodes.AxiomNode;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.form.ChooserLifeCycle;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.soa.ui.axinodes.AxiomTreeNodeFactory;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.nodes.NodesTreeModel;
import org.netbeans.modules.soa.ui.nodes.NodesTreeRenderer;
import org.netbeans.modules.xslt.mapper.methoid.Constants;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.modules.xslt.mapper.model.SourceTreeModel;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author  nk160297
 */
public class ExpressionEditor extends JPanel implements ChooserLifeCycle<String> {
    
    private XsltMapper myMapper;
    
    private final static String INPUT_PARAM = "InputParam";
    
    
    public ExpressionEditor(IBasicMapper basicMapper) {
        assert basicMapper instanceof XsltMapper;
        myMapper = (XsltMapper)basicMapper;
        createContent();
        initControls();
    }
    
    public void createContent() {
        initComponents();
        Lookup lookup = myMapper.getLookup();
        //
        MapperContext context = myMapper.getContext();
        AXIComponent sourceAxiComponent = context.getSourceType();
        if (sourceAxiComponent == null) {
            sourceAxiComponent = SourceTreeModel.constructFakeComponent();
        }
        if (sourceAxiComponent != null) {
            Lookup axiLookup = ExtendedLookup.createExtendedLookup(
                    lookup, new AxiomTreeNodeFactory());
            Children children = new AxiomChildren(sourceAxiComponent, axiLookup);
            Node rootNode = new NodeFactory.TextNode(children, "Source Schema model"); // TODO I18N
            //
            TreeModel variablesModel = new NodesTreeModel(rootNode);
            treeSchema.setModel(variablesModel);
            treeSchema.setCellRenderer(new NodesTreeRenderer());
            treeSchema.setRootVisible(false);
            treeSchema.setShowsRootHandles(true);
        }
        //
        FileObject paletteRootFo = getPaletteFolder(Constants.XSLT_PALETTE_FOLDER);
        PaletteTreeNodeFactory factory = new PaletteTreeNodeFactory();
        Lookup paletteLookup = ExtendedLookup.createExtendedLookup(lookup, factory);
        Node paletteRootNode = factory.createNode(
                PaletteTreeNodeFactory.NodeType.ROOT,
                paletteRootFo, paletteLookup);
        TreeModel functionsModel = new NodesTreeModel(paletteRootNode);
        //
        treeFunctions.setModel(functionsModel);
        treeFunctions.setCellRenderer(new NodesTreeRenderer());
        treeFunctions.setRootVisible(false);
        treeFunctions.setShowsRootHandles(true);
        //
        treeSchema.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 /**&& e.isAltDown() */) {
                    TreePath tp = treeSchema.getSelectionPath();
                    // TreePath tp = treeSchema.getPathForLocation(e.getX(), e.getY());
                    if (tp != null) {
                        Object lastPathComp = tp.getLastPathComponent();
                        if (lastPathComp != null &&
                                lastPathComp instanceof AxiomNode) {
                            addXPath((AxiomNode)lastPathComp);
                        }
                    }
                }
            }
        });
        //
        treeFunctions.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath tp = treeFunctions.getSelectionPath();
                    // TreePath tp = treeFunctions.getPathForLocation(e.getX(), e.getY());
                    if (tp != null) {
                        Object lastPathComp = tp.getLastPathComponent();
                        if (lastPathComp != null &&
                                lastPathComp instanceof ItemNode) {
                            addFunction((ItemNode)lastPathComp);
                        }
                    }
                }
            }
        });
        //
        btnCheckSyntax.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkSyntax();
            }
        });
    }
    
    public boolean initControls() {
        return true;
    }
    
    public void setSelectedValue(String newValue) {
        txtExpression.setText(newValue);
    }
    
    public String getSelectedValue() {
        return txtExpression.getText();
    }
    
    public boolean afterClose() {
        return true;
    }
    
    public boolean unsubscribeListeners() {
        return true;
    }
    
    public boolean subscribeListeners() {
        return true;
    }
    
    private static FileObject getPaletteFolder(String folderName) {
        FileObject paletteFolder = null;
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        paletteFolder = fs.findResource( folderName );
        return paletteFolder;
    }
    
    
    private void addXPath(AxiomNode node) {
        // String defaultNamespace = myBpelProcess.getTargetNamespace();
        String defaultNamespace = null;
        String text = AxiomUtils.calculateSimpleXPath(node, defaultNamespace);
        //
        int oldSelectionStart = txtExpression.getSelectionStart();
        int selectionStart = oldSelectionStart;
        int selectionEnd = oldSelectionStart + text.length();
        //
        txtExpression.replaceSelection(text);
        txtExpression.setCaretPosition(selectionEnd);
        txtExpression.setSelectionStart(selectionStart);
        txtExpression.setSelectionEnd(selectionEnd);
        txtExpression.requestFocus();
    }
    
    private void addFunction(ItemNode funcItemNode) {
        XpathPaletteItemInfo info = funcItemNode.getItemInfo();
        //
        if (info != null) {
            int oldSelectionStart = txtExpression.getSelectionStart();
            int selectionStart = oldSelectionStart;
            int selectionEnd = oldSelectionStart;
            //
            String text = null;
            if (info.isOperator()) {
                text = " " + info.getOperation() + " "; // NOI18N
                selectionStart = oldSelectionStart + text.length();
                selectionEnd = selectionStart;
            } else if (info.isFunction()) {
                int maxInput = info.getMaxInput();
                String inp1, inp2, inp3, inp4;
                switch (maxInput) {
                    case 1:
                        inp1 = (String)info.getItemAttribute(INPUT_PARAM + "1"); // NOI18N
                        text = info.getOperation() + "("; // NOI18N
                        selectionStart = oldSelectionStart + text.length();
                        text = text + inp1; // NOI18N
                        selectionEnd = oldSelectionStart + text.length();
                        text = text + ")"; // NOI18N
                        break;
                    case 2:
                        inp1 = (String)info.getItemAttribute(INPUT_PARAM + "1"); // NOI18N
                        text = info.getOperation() + "("; // NOI18N
                        selectionStart = oldSelectionStart + text.length();
                        text = text + inp1; // NOI18N
                        selectionEnd = oldSelectionStart + text.length();
                        //
                        inp2 = (String)info.getItemAttribute(INPUT_PARAM + "2"); // NOI18N
                        text = text + ", " + inp2; // NOI18N
                        //
                        text = text + ")"; // NOI18N
                        break;
                    case 3:
                        inp1 = (String)info.getItemAttribute(INPUT_PARAM + "1"); // NOI18N
                        text = info.getOperation() + "("; // NOI18N
                        selectionStart = oldSelectionStart + text.length();
                        text = text + inp1; // NOI18N
                        selectionEnd = oldSelectionStart + text.length();
                        //
                        inp2 = (String)info.getItemAttribute(INPUT_PARAM + "2"); // NOI18N
                        text = text + ", " + inp2; // NOI18N
                        //
                        inp3 = (String)info.getItemAttribute(INPUT_PARAM + "3"); // NOI18N
                        text = text + ", " + inp3; // NOI18N
                        //
                        text = text + ")"; // NOI18N
                        break;
                    case 0:
                    default:
                        inp1 = (String)info.getItemAttribute(INPUT_PARAM + "1"); // NOI18N
                        text = info.getOperation() + "("; // NOI18N
                        selectionStart = oldSelectionStart + text.length();
                        selectionEnd = selectionStart;
                        text = text + ")"; // NOI18N
                        break;
                }
            }
            //
            if (text != null && text.length() > 0) {
                txtExpression.replaceSelection(text);
                txtExpression.setCaretPosition(selectionStart);
                txtExpression.setSelectionStart(selectionStart);
                txtExpression.setSelectionEnd(selectionEnd);
                txtExpression.requestFocus();
            }
            //
            // TODO set focus to the expression field
            // TODO set cursor to appropriate place!!!
        }
    }
    
    private void checkSyntax() {
        boolean result = true;
        //
        String expr = txtExpression.getText();
        if (expr == null || expr.length() == 0) {
            return;
        }
        XPathModel xpImpl = AbstractXPathModelHelper.getInstance().newXPathModel();
        try {
            XPathExpression xPath = xpImpl.parseExpression(expr);
//            String exprString = xPath.getExpressionString();
//            UserNotification.showMessage(exprString);
        } catch (XPathException xpe) {
            String errorMessage = null;
            Throwable cause = xpe.getCause();
            if (cause != null) {
                errorMessage = cause.getMessage();
            }
            if (errorMessage == null) {
                errorMessage = xpe.getMessage();
            }
            //
            UserNotification.showMessage(errorMessage);
            result = false;
            // ErrorManager.getDefault().notify(xpe);
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblSchema = new javax.swing.JLabel();
        lblFunctions = new javax.swing.JLabel();
        lblExpression = new javax.swing.JLabel();
        scrSchema = new javax.swing.JScrollPane();
        treeSchema = new javax.swing.JTree();
        scrFunctions = new javax.swing.JScrollPane();
        treeFunctions = new javax.swing.JTree();
        scrExpression = new javax.swing.JScrollPane();
        txtExpression = new javax.swing.JTextArea();
        btnCheckSyntax = new javax.swing.JButton();

        lblSchema.setLabelFor(treeSchema);
        lblSchema.setText(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "LBL_Schema"));
        lblSchema.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "ACSN_LBL_Schema"));
        lblSchema.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "ACSD_LBL_Schema"));

        lblFunctions.setLabelFor(treeFunctions);
        lblFunctions.setText(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "LBL_Functions"));
        lblFunctions.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "ACSN_LBL_Functions"));
        lblFunctions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "ACSD_LBL_Functions"));

        lblExpression.setLabelFor(txtExpression);
        lblExpression.setText(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "LBL_Expression"));
        lblExpression.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "ACSN_LBL_Expression"));
        lblExpression.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "ACSD_LBL_Expression"));

        scrSchema.setViewportView(treeSchema);

        scrFunctions.setViewportView(treeFunctions);

        txtExpression.setColumns(20);
        txtExpression.setRows(5);
        scrExpression.setViewportView(txtExpression);

        btnCheckSyntax.setText(org.openide.util.NbBundle.getMessage(ExpressionEditor.class, "BTN_CheckSyntax"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, scrExpression, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(scrSchema, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                                .add(7, 7, 7))
                            .add(layout.createSequentialGroup()
                                .add(lblSchema)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblFunctions)
                            .add(scrFunctions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 213, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(lblExpression)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 478, Short.MAX_VALUE)
                        .add(btnCheckSyntax)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblSchema)
                    .add(lblFunctions))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrFunctions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                    .add(scrSchema, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnCheckSyntax)
                    .add(lblExpression))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrExpression, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCheckSyntax;
    private javax.swing.JLabel lblExpression;
    private javax.swing.JLabel lblFunctions;
    private javax.swing.JLabel lblSchema;
    private javax.swing.JScrollPane scrExpression;
    private javax.swing.JScrollPane scrFunctions;
    private javax.swing.JScrollPane scrSchema;
    private javax.swing.JTree treeFunctions;
    private javax.swing.JTree treeSchema;
    private javax.swing.JTextArea txtExpression;
    // End of variables declaration//GEN-END:variables
    
}
