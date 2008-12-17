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

package org.netbeans.modules.cnd.navigation.macroview;

import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Registry;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.explorer.ExplorerManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Alexander Simon
 */
public class MacroExpansionPanel extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider  {
    public static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/includeview/resources/tree.png"; // NOI18N

    private transient ExplorerManager explorerManager = new ExplorerManager();
    
    /** Creates new form MacroExpansionPanel */
    public MacroExpansionPanel(boolean isView) {
        initComponents();


        //jCodeExpansionEditorPane;

       // OpenedEditors e;



//        if (!isView){
//            // refresh
//            toolBar.remove(0);
//            // separstor
//            toolBar.remove(0);
//            // a11n
//            directOnlyButton.setFocusable(true);
//            treeButton.setFocusable(true);
//            whoIncludesButton.setFocusable(true);
//            whoIsIncludedButton.setFocusable(true);
//        }
        setName(NbBundle.getMessage(getClass(), "CTL_MacroExpansionTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_MacroExpansionTopComponent")); // NOI18N
//        setIcon(Utilities.loadImage(ICON_PATH, true));
//        getTreeView().setRootVisible(false);
//        Children.Array children = new Children.SortedArray();
//        if (isView) {
//            actions = new Action[]{new RefreshAction(),
//                                   null, new WhoIncludesAction(), new WhoIsIncludedAction(),
//                                   null, new DirectOnlyAction(), new TreeAction()};
//        } else {
//            actions = new Action[]{new WhoIncludesAction(), new WhoIsIncludedAction(),
//                                   null, new DirectOnlyAction(), new TreeAction()};
//        }
//        root = new AbstractNode(children){
//            @Override
//            public Action[] getActions(boolean context) {
//                return actions;
//            }
//        };
//        getExplorerManager().setRootContext(root);
    }

    public void setMacroExpansionText(String text) {
        jMacroExpansionEditorPane.setText(text);
    }

    public void setCodeExpansionDocument(Document doc) {
        Object mimeTypeObj = doc.getProperty(NbEditorDocument.MIME_TYPE_PROP);
        String mimeType = MIMENames.CPLUSPLUS_MIME_TYPE;
        if (mimeTypeObj != null) {
            mimeType = (String) mimeTypeObj;
        }
        jCodeExpansionEditorPane.setContentType(mimeType);
        jCodeExpansionEditorPane.setDocument(doc);

        Registry.addDocument((BaseDocument)doc);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        macroExpansionPane = new javax.swing.JScrollPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jCodeExpansionPane = new javax.swing.JScrollPane();
        jCodeExpansionEditorPane = new javax.swing.JEditorPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMacroExpansionEditorPane = new javax.swing.JEditorPane();

        setLayout(new java.awt.GridBagLayout());

        macroExpansionPane.setFocusable(false);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setFocusable(false);
        jSplitPane1.setOneTouchExpandable(true);

        jCodeExpansionPane.setBorder(null);

        jCodeExpansionEditorPane.setBorder(null);
        jCodeExpansionEditorPane.setEditable(false);
        jCodeExpansionPane.setViewportView(jCodeExpansionEditorPane);

        jSplitPane1.setLeftComponent(jCodeExpansionPane);

        jScrollPane1.setBorder(null);

        jMacroExpansionEditorPane.setBorder(null);
        jMacroExpansionEditorPane.setEditable(false);
        jScrollPane1.setViewportView(jMacroExpansionEditorPane);

        jSplitPane1.setRightComponent(jScrollPane1);

        macroExpansionPane.setViewportView(jSplitPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(macroExpansionPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return macroExpansionPane.requestFocusInWindow();
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jCodeExpansionEditorPane;
    private javax.swing.JScrollPane jCodeExpansionPane;
    private javax.swing.JEditorPane jMacroExpansionEditorPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    javax.swing.JScrollPane macroExpansionPane;
    // End of variables declaration//GEN-END:variables

    public HelpCtx getHelpCtx() {
        return new HelpCtx("MacroExpansionView"); // NOI18N
    }
}
