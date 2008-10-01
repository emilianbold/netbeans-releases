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


package org.netbeans.modules.uml.ui.swing.commondialogs;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.UserSettings;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;
import org.netbeans.modules.uml.ui.support.presentationnavigation.JNavigationTreeTable;
import org.netbeans.modules.uml.ui.support.presentationnavigation.NavigationTreeTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author  Thuy
 */
public class SwingNavigationView extends javax.swing.JPanel
{
    private IElement element;
    private JNavigationTreeTable treeTableView;
    private SwingNavigationDialog parent;
    private UserSettings settings = new UserSettings();
    
    /** Creates new form SwingNavigationView */
    public SwingNavigationView(SwingNavigationDialog parent)
    {
        this.parent = parent;
        this.element = parent.getElement();
        initComponents();
        setComponentValues();
    }
    
    
    public void setComponentValues()
    {
        String name = "";
        String label = "";
        
        if (parent.getIsProject())
        {
            label = NbBundle.getMessage(SwingNavigationView.class, "DS_PACKAGE_TITLE");
        }
        else
        {
            // set shiftCheckBox
            shiftCheckBox.setSelected(settings.isOnlyShowNavigateWhenShift(element));
            
            if (element instanceof INamedElement)
            {
                name = ((INamedElement)element).getName();
            }
            label = NbBundle.getMessage(SwingNavigationView.class, "IDS_TITLE");
            label = StringUtilities.replaceSubString(label, "%s", name);
        }
        
        // set instructionLabel
        instructionLabel.setText(label);
        instructionLabel.setLabelFor(this.treeTableView);
        instructionLabel.getAccessibleContext().setAccessibleName(label);
        instructionLabel.getAccessibleContext().setAccessibleDescription(label);
    }
    
    public void setOptions(int settings)
    {
        shiftCheckBox.setEnabled(settings == UserSettings.OPEN_OPTION);
    }
    
    public void buildList(ETList<IProxyDiagram> diagrams,
            ETList<IPresentationTarget> targets,
            ETList<IProxyDiagram> assocDiagrams,
            ETList<IElement> assocElements)
    {
        //first we need to create a root element
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");
        
        NavigationTreeTableModel model = new NavigationTreeTableModel(rootNode, null);
        
        treeTableView = new JNavigationTreeTable(model, parent);
        
        if (diagrams != null && diagrams.size() > 0)
        {
            //create node for diagrams
            String text = "";
            if (element instanceof IProject)
            {
                text = NbBundle.getMessage(SwingNavigationView.class, "IDS_DIAGRAMS");
            }
            else
            {
                text = NbBundle.getMessage(SwingNavigationView.class, "IDS_SCOPEDDIAGRAMS");
            }
            DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(text);
            
            //now we need to build this node to show all the elements under it.
            int count = diagrams.size();
            for (int i=0; i<count; i++)
            {
                IProxyDiagram dia = diagrams.get(i);
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(dia);
                node1.add(childNode);
            }
            rootNode.add(node1);
        }
        
        if (targets != null && targets.size() > 0)
        {
            //create node for diagrams
            String text = NbBundle.getMessage(SwingNavigationView.class, "IDS_TARGETS");
            DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(text);
            
            //now we need to build this node to show all the elements under it.
            int count = targets.size();
            for (int i=0; i<count; i++)
            {
                IPresentationTarget target = targets.get(i);
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(target);
                node2.add(childNode);
            }
            rootNode.add(node2);
        }
        
        if (assocDiagrams != null && assocDiagrams.size() > 0)
        {
            //create node for diagrams
            String text = NbBundle.getMessage(SwingNavigationView.class,"IDS_ASSOCIATEDDIAGRAMS");
            DefaultMutableTreeNode node3 = new DefaultMutableTreeNode(text);
            
            //now we need to build this node to show all the elements under it.
            int count = assocDiagrams.size();
            for (int i=0; i<count; i++)
            {
                IProxyDiagram dia = assocDiagrams.get(i);
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(dia);
                node3.add(childNode);
            }
            rootNode.add(node3);
        }
        
        if (assocElements != null)
        {
            //create node for element
            String text = NbBundle.getMessage(SwingNavigationView.class,"IDS_ASSOCIATEDELEMENTS");
            DefaultMutableTreeNode node4 = new DefaultMutableTreeNode(text);
            
            //now we need to build this node to show all the elements under it.
            int count = assocElements.size();
            for (int i=0; i<count; i++)
            {
                IElement ele = assocElements.get(i);
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(ele);
                node4.add(childNode);
            }
            rootNode.add(node4);
        }
        treeTableView.updateUI();
        
        //now we want to expand the first level nodes by default
        treeTableView.expandFirstLevelNodes();
        treeTableView.updateUI();
        
        treeTableView.addFocusListener(new TreeFocusHandler());
        
        treeTableView.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LArrowKeysHandler");
        treeTableView.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),"RArrowKeysHandler");
        treeTableView.getActionMap().put("LArrowKeysHandler", new KeyActionPerformer(KeyEvent.VK_LEFT));
        treeTableView.getActionMap().put("RArrowKeysHandler", new KeyActionPerformer(KeyEvent.VK_RIGHT));
        treeTableView.getAccessibleContext().setAccessibleName("");
        treeTableView.getAccessibleContext().setAccessibleDescription("");
        
        // add the tree to the scrollpane
        scrollPane.setViewportView(treeTableView);
    }
    
    public void performOKAction()
    {
        try
        {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            //we need to navigate to the selected element
            if (treeTableView != null)
            {
                TreePath selected = treeTableView.getTree().getSelectionPath();
                
                if (selected != null)
                {
                    //System.out.println("PerFormOKAction - selected path="+ selected.toString());
                    treeTableView.handleNavigation(selected);
                }
            }
            
            //also we need to save the shift checkbox status
            if (!parent.getIsProject())
            {
                savePreferenceDefaults();
            }
        }
        finally
        {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    
    public void savePreferenceDefaults()
    {
        // Save the status of the only show when shift is down button
        UserSettings settings = new UserSettings();
        
        if (shiftCheckBox.isSelected())
        {
            settings.setIsOnlyShowNavigateWhenShift(element, true);
            if (parent.getIsDiagram())
            {
                settings.setDefaultDiagram(element, parent.getTargetXMIID());
            }
            else
            {
                settings.setDefaultPE(element, parent.getTargetXMIID());
            }
        }
        else
        {
            settings.setIsOnlyShowNavigateWhenShift(element, false);
            settings.clearDefaultTarget(element);
        }
    }
    
    class TreeFocusHandler implements FocusListener
    {
        private TreePath selectedPath;
        
        public void focusGained(FocusEvent e)
        {
            Component comp = e.getComponent();
            if ( comp != null && comp instanceof JNavigationTreeTable)
            {
                JNavigationTreeTable ntt = (JNavigationTreeTable) comp;
                if (selectedPath == null) {
                    selectedPath = ntt.getTree().getPathForRow(0);
                }
                ntt.getTree().setSelectionPath(selectedPath);
            }
        }
        
        public void focusLost(FocusEvent e)
        {
            Component comp = e.getComponent();
            if ( comp != null && comp instanceof JNavigationTreeTable)
            {
                JNavigationTreeTable ntt = (JNavigationTreeTable) comp;
                selectedPath = ntt.getTree().getSelectionPath();
            }
        }
    }
    
    class KeyActionPerformer extends AbstractAction
    {
        private int keyCode;
        
        public KeyActionPerformer(int keyCode)
        {
            this.keyCode = keyCode;
        }
        
        public void actionPerformed(ActionEvent e)
        {
            int selRow = treeTableView.getSelectedRow();
            if ( selRow >= 0)  //some row is selected
            {
                //System.out.println("keyCode="+KeyStroke.getKeyStroke(keyCode, 0) + " selRow="+selRow);
                switch (keyCode)
                {
                    case KeyEvent.VK_LEFT:
                        if (treeTableView.getTree().isExpanded(selRow))
                            treeTableView.getTree().collapseRow(selRow);
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (!treeTableView.getTree().isExpanded(selRow))
                            treeTableView.getTree().expandRow(selRow);
                        break;
                    default:
                        break;
                }
                treeTableView.updateUI();
                treeTableView.getTree().setSelectionRow(selRow);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        instructionLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        bottomPanel = new javax.swing.JPanel();
        bottomLabel = new javax.swing.JLabel();
        shiftCheckBox = new javax.swing.JCheckBox();

        instructionLabel.setText(org.openide.util.NbBundle.getMessage(SwingNavigationView.class, "IDS_TITLE"));
        instructionLabel.setFocusable(false);

        scrollPane.setFocusable(false);

        bottomLabel.setText(org.openide.util.NbBundle.getMessage(SwingNavigationView.class, "IDS_CHECKBOXTEXT"));
        bottomLabel.setFocusable(false);
        bottomLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SwingNavigationView.class, "IDS_CHECKBOXTEXT"));
        bottomLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SwingNavigationView.class, "IDS_CHECKBOXTEXT"));

        org.openide.awt.Mnemonics.setLocalizedText(shiftCheckBox, org.openide.util.NbBundle.getMessage(SwingNavigationView.class, "IDS_CHECKBOX"));
        shiftCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        shiftCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        shiftCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SwingNavigationView.class, "IDS_CHECKBOX"));
        shiftCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SwingNavigationView.class, "IDS_CHECKBOX"));

        org.jdesktop.layout.GroupLayout bottomPanelLayout = new org.jdesktop.layout.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, bottomLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
            .add(bottomPanelLayout.createSequentialGroup()
                .add(shiftCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                .addContainerGap())
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(bottomLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(shiftCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, bottomPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, instructionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(instructionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bottomPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(60, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bottomLabel;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JLabel instructionLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JCheckBox shiftCheckBox;
    // End of variables declaration//GEN-END:variables
    
}
