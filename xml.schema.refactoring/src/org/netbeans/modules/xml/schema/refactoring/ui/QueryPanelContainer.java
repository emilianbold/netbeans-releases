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
package org.netbeans.modules.xml.schema.refactoring.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.openide.awt.MouseUtils;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
//import org.openide.awt.JPopupMenuPlus;
import org.openide.util.NbBundle;
//import org.netbeans.modules.xml.refactoring.ui.j.ui.CloseButtonTabbedPane;

/**
 *
 * @author  Jan Becicka
 * @author  Jeri Lockhart
 */
public class QueryPanelContainer extends TopComponent  
         {
    public static final long serialVersionUID = 1L;
    
    private static QueryPanelContainer usages = null;
    private static QueryPanelContainer refactorings = null;
    private transient boolean isVisible = false;
    private JPopupMenu pop;
    /** Popup menu listener */
    private PopupListener listener;
    private CloseListener closeL;
    private boolean isRefactoring;
    private static Image QUERY_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/refactoring/resources/"+
            "findusages.png" ); // NOI18N
    private static final String XML_SCHEMA_QUERY = "xml-schema-query"; // NOI18N
//    private transient InstanceContent selectedNode;
//    public static final String SCHEMA_COMPONENT_SELECTION =
//            "schema-component-selection";
    public static final String PROP_SCHEMA_COMPONENT_CHANGED =
            "prop-schema-component-changed";
    public static final String PROP_REQUEST_HIGHLIGHT_IN_SCHEMA_VIEW =
            "prop-request-highlight-in-schema-view";
    
    
    private QueryPanelContainer() {
        this("", false);
    }
    /**
     * Creates new form QueryPanelContainer
     */
    private QueryPanelContainer(String name, boolean isRefactoring) {
        setName(name);
        setToolTipText(name);
        setFocusable(true);
        setLayout(new GridBagLayout());
        getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(QueryPanelContainer.class, 
                "ACSD_usagesPanel")
        );
        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        listener = new PopupListener();
        closeL = new CloseListener();
        this.isRefactoring = isRefactoring;
        setFocusCycleRoot(true);
        JLabel label = new JLabel(NbBundle.getMessage(
                QueryPanelContainer.class, "LBL_NoUsages"));
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER; 
        this.add(label, gridBagConstraints);
    }
  
    
    
    
    public void addPanel(JPanel panel) {
        QueryPanel.checkEventThread();
        if (panel == null){
            return;
        }
//        panel.addPropertyChangeListener(
//                QueryPanel.NODE_SELECTION_CHANGE, 
//                this);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER; 
        if (getComponentCount() == 0) {
            add(panel, gridBagConstraints);
        } else {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                ((JTabbedPane) comp).addTab(panel.getName() + "  ", 
                        null, panel, panel.getToolTipText()); //NOI18N
                ((JTabbedPane) comp).setSelectedComponent(panel);
                comp.validate();
            } else if (comp instanceof JLabel) {
                remove(comp);
                add(panel, gridBagConstraints);
            } else {
                remove(comp);
                JTabbedPane pane = new CloseButtonTabbedPane();
                pane.addMouseListener(listener);
                pane.addPropertyChangeListener(closeL);
                add(pane, gridBagConstraints);
                pane.addTab(comp.getName() + "  ", null, comp, 
                        ((JPanel) comp).getToolTipText()); //NOI18N
                pane.addTab(panel.getName() + "  ", null, panel, 
                        panel.getToolTipText()); //NOI18N
                pane.setSelectedComponent(panel);
                pane.validate();
                repaint();
            }
        }
        if (!isVisible) {
            isVisible = true;
            open();
        }
        validate();
        requestActive();
    }

    protected void componentActivated () {
        super.componentActivated();
        JPanel panel = getCurrentPanel();
        if (panel!=null)
            panel.requestFocus();
    }
    
    void removePanel(JPanel panel) {        
//        HighlightManager hm = HighlightManager.getDefault();
//        Lookup hmLookup = Lookups.singleton(hm);
//        HighlightProvider.hideResults(hmLookup);
        QueryPanel.checkEventThread();
        Component comp = getComponentCount() > 0 ? getComponent(0) : null;
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            if (panel == null) {
                panel = (JPanel) tabs.getSelectedComponent();
            }
            tabs.remove(panel);
            if (tabs.getComponentCount() == 1) {
                Component c = tabs.getComponent(0);
                tabs.removeMouseListener(listener);
                tabs.removePropertyChangeListener(closeL);
                remove(tabs);
                GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.weightx = 0.2;
                gridBagConstraints.weighty = 0.2;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;        
                add(c, gridBagConstraints);
            }
        } else {
            if (comp != null)
                remove(comp);
            isVisible = false;
            close();
        }
        validate();
        repaint();
    }
    
    void closeAllButCurrent() {
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            Component current = tabs.getSelectedComponent();
            Component[] c =  tabs.getComponents();
            for (int i = 0; i< c.length; i++) {
                if (c[i]!=current) {
                    ((QueryPanel) c[i]).close();
                }
            }
        }
    }
    
    public static synchronized QueryPanelContainer getUsagesComponent() {
        if ( usages == null ) {
            usages = 
                    (QueryPanelContainer)
                    WindowManager.getDefault().findTopComponent( 
                    XML_SCHEMA_QUERY ); 
        } 
        return usages;
    }
    
    public static synchronized QueryPanelContainer 
            createUsagesComponent() {
        if (usages == null)
            usages = new QueryPanelContainer(
                    NbBundle.getMessage(QueryPanelContainer.class, 
                    "LBL_Usages"), false);
        return usages;
    }
    
    protected void componentClosed() {
        isVisible = false;
        if (getComponentCount() == 0) {
            return ;
        }
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane pane = (JTabbedPane) comp;
            Component[] c =  pane.getComponents();
            for (int i = 0; i< c.length; i++) {
                ((QueryPanel) c[i]).close();
            }
        } else if (comp instanceof QueryPanel) {
            ((QueryPanel) comp).close();
        }
    }
    
    protected String preferredID() {
        return "QueryPanel"; // NOI18N
    }

    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
    
//    private void initActions() {
//        ActionMap map = getActionMap();
//
//        map.put("jumpNext", new PrevNextAction (false)); // NOI18N
//        map.put("jumpPrev", new PrevNextAction (true)); // NOI18N
//    }
    
    private QueryPanel getCurrentPanel() {
        if (getComponentCount() > 0) {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                JTabbedPane tabs = (JTabbedPane) comp;
                return (QueryPanel) tabs.getSelectedComponent();
            } else {
                if (comp instanceof QueryPanel)
                    return (QueryPanel) comp;
            }
        }
        return null;
    }
    
//    private final class PrevNextAction extends javax.swing.AbstractAction {
//    public static final long serialVersionUID = 1L;
//        private boolean prev;
//        
//        public PrevNextAction (boolean prev) {
//            this.prev = prev;
//        }
//
//        public void actionPerformed (java.awt.event.ActionEvent actionEvent) {
//            QueryPanel panel = getCurrentPanel();
//            if (panel != null) {
//                if (prev) {
//                    panel.selectPrevUsage();
//                } else {
//                    panel.selectNextUsage(); 
//                }
//            }
//        }
//    }
    
    
    private class CloseListener implements PropertyChangeListener {
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
         //   if (CloseButtonTabbedPane.PROP_CLOSE.equals(
           //         evt.getPropertyName())) {
            //    removePanel((JPanel) evt.getNewValue());
            //}
        }
        
    }
    /**
    * Class to showing popup menu
    */
    private class PopupListener extends MouseUtils.PopupMouseAdapter {        

        /**
         * Called when the sequence of mouse events should lead to 
         * actual showing popup menu
         */
        protected void showPopup (MouseEvent e) {
            pop.show(QueryPanelContainer.this, e.getX(), e.getY());
        }
    } // end of PopupListener
        
    private class Close extends AbstractAction {
    public static final long serialVersionUID = 1L;
        
        public Close() {
            super(NbBundle.getMessage(QueryPanelContainer.class, 
                    "LBL_CloseWindow"));
        }
        
        public void actionPerformed(ActionEvent e) {
            removePanel(null);
        }
    }
    
    private final class CloseAll extends AbstractAction {
    public static final long serialVersionUID = 1L;
        
        public CloseAll() {
            super(NbBundle.getMessage(QueryPanelContainer.class,
                    "LBL_CloseAll"));
        }
        
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }
    
    private class CloseAllButCurrent extends AbstractAction {
    public static final long serialVersionUID = 1L;
        
        public CloseAllButCurrent() {
            super(NbBundle.getMessage(QueryPanelContainer.class, 
                    "LBL_CloseAllButCurrent"));
        }
        
        public void actionPerformed(ActionEvent e) {
            closeAllButCurrent();
        }
    }
    
    public HelpCtx getHelpCtx() {
        //return HelpCtx.DEFAULT_HELP;
        return new HelpCtx(QueryPanelContainer.class.getName() 
        + (isRefactoring ? ".refactoring-preview" : ".find-usages") );//NOI18N
    }

    public java.awt.Image getIcon() {
         return QUERY_BADGE;
    }


    
    /**
     *  Implement PropertyChangeListener
     *  NODE_SELECTION_CHANGE
     */
//    public void propertyChange(PropertyChangeEvent evt) {
//        String propName = evt.getPropertyName();
//        if (propName.equals(
//                QueryPanel.NODE_SELECTION_CHANGE)){
////            selectedNode.set(Arrays.asList(evt.getNewValue()), null);
//            if (evt.getNewValue() != null){
//                Object newVal = evt.getNewValue();
//                // The newVal could also be a Project, FileObject, SourceGroup
//                
//                assert newVal != null:
//                    "RefactoringPanel should not fire NODE_SELECTION_CHANGE" + 
//                        " with a null new value";
//                firePropertyChange(PROP_SCHEMA_COMPONENT_CHANGED, null, newVal);
//            }
//        }
//    }
    

    
}
