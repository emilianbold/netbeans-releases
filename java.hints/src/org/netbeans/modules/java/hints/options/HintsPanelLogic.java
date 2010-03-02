/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.java.hints.options;

import org.openide.filesystems.FileUtil;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;

import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import org.netbeans.modules.java.hints.options.DepScanningSettings.DependencyTracking;
import static org.netbeans.modules.java.hints.spi.AbstractHint.*;


/** Contains all important listeners and logic of the Hints Panel.
 *
 * @author Petr Hrebejk
 */
class HintsPanelLogic implements MouseListener, KeyListener, TreeSelectionListener, ChangeListener, ActionListener {

    private Map<String,ModifiedPreferences> changes = new HashMap<String, ModifiedPreferences>();
    private DependencyTracking depScn = null;
    
    private static Map<HintSeverity,Integer> severity2index;
    private static Map<DependencyTracking,Integer> deptracking2index;
    
    private static final String DESCRIPTION_HEADER = 
        "<html><head>" + // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbdocs://org.netbeans.modules.usersguide/org/netbeans/modules/usersguide/ide.css\" type=\"text/css\">" // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbresloc:/org/netbeans/modules/java/hints/resources/ide.css\" type=\"text/css\">" + // NOI18N
        "</head><body>"; // NOI18N

    private static final String DESCRIPTION_FOOTER = "</body></html>"; // NOI18N
    
    
    static {
        severity2index = new HashMap<HintSeverity, Integer>();
        severity2index.put( HintSeverity.ERROR, 0  );
        severity2index.put( HintSeverity.WARNING, 1  );
        severity2index.put( HintSeverity.CURRENT_LINE_WARNING, 2  );
        deptracking2index = new HashMap<DepScanningSettings.DependencyTracking, Integer>();
        deptracking2index.put(DependencyTracking.ENABLED, 0);
        deptracking2index.put(DependencyTracking.ENABLED_WITHIN_PROJECT, 1);
        deptracking2index.put(DependencyTracking.ENABLED_WITHIN_ROOT, 2);
    }
    
    private JTree errorTree;
    private DefaultTreeModel errorTreeModel;
    private JLabel severityLabel;
    private JComboBox severityComboBox;
    private JCheckBox tasklistCheckBox;
    private JPanel customizerPanel;
    private JEditorPane descriptionTextArea;
    private DefaultComboBoxModel defModel = new DefaultComboBoxModel();
    private DefaultComboBoxModel depScanningModel = new DefaultComboBoxModel();
    private String defLabel = NbBundle.getMessage(HintsPanel.class, "CTL_ShowAs_Label"); //NOI18N
    private String depScanningLabel = NbBundle.getMessage(HintsPanel.class, "CTL_Scope_Label"); //NOI18N
    private String depScanningDescription = NbBundle.getMessage(HintsPanel.class, "CTL_Scope_Desc"); //NOI18N
    
    HintsPanelLogic() {
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsError")); //NOI18N
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsWarning")); //NOI18N
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_WarningOnCurrentLine")); //NOI18N

        depScanningModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AllProjects")); //NOI18N
        depScanningModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_Project")); //NOI18N
        depScanningModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_SrcRoot")); //NOI18N
    }
    
    void connect( JTree errorTree, DefaultTreeModel errorTreeModel, JLabel severityLabel, JComboBox severityComboBox,
                  JCheckBox tasklistCheckBox, JPanel customizerPanel,
                  JEditorPane descriptionTextArea) {
        
        this.errorTree = errorTree;
        this.errorTreeModel = errorTreeModel;
        this.severityLabel = severityLabel;
        this.severityComboBox = severityComboBox;
        this.tasklistCheckBox = tasklistCheckBox;
        this.customizerPanel = customizerPanel;
        this.descriptionTextArea = descriptionTextArea;        
        
        valueChanged( null );
        
        errorTree.addKeyListener(this);
        errorTree.addMouseListener(this);
        errorTree.getSelectionModel().addTreeSelectionListener(this);
            
        severityComboBox.addActionListener(this);
        tasklistCheckBox.addChangeListener(this);
        
    }
    
    void disconnect() {
        
        errorTree.removeKeyListener(this);
        errorTree.removeMouseListener(this);
        errorTree.getSelectionModel().removeTreeSelectionListener(this);
            
        severityComboBox.removeActionListener(this);
        tasklistCheckBox.removeChangeListener(this);
                
        componentsSetEnabled( false );
    }
    
    synchronized void applyChanges() {
        for (String hint : changes.keySet()) {
            ModifiedPreferences mn = changes.get(hint);
            mn.store(RulesManager.getPreferences(hint, HintsSettings.getCurrentProfileId()));
        }
        if (depScn != null)
            DepScanningSettings.setDependencyTracking(depScn);
    }
    
    /** Were there any changes in the settings
     */
    boolean isChanged() {
        return !changes.isEmpty() || depScn != null;
    }
    
    synchronized Preferences getCurrentPrefernces( String id ) {
        Preferences node = changes.get(id);
        return node == null ? RulesManager.getPreferences(id, HintsSettings.getCurrentProfileId() ) : node;
    }
    
    synchronized Preferences getPreferences4Modification(String hint ) {
        Preferences node = changes.get(hint);        
        if ( node == null ) {
            node = new ModifiedPreferences(RulesManager.getPreferences(hint, HintsSettings.getCurrentProfileId() ) );
            changes.put( hint, (ModifiedPreferences)node);
        }        
        return node;                
    }
    
    synchronized DependencyTracking getCurrentDependencyTracking() {
        return depScn != null ? depScn : DepScanningSettings.getDependencyTracking();
    }

    static Object getUserObject( TreePath path ) {
        if( path == null )
            return null;
        DefaultMutableTreeNode tn = (DefaultMutableTreeNode)path.getLastPathComponent();
        return tn.getUserObject();
    }
    
    static Object getUserObject( DefaultMutableTreeNode node ) {
        return node.getUserObject();
    }
    
    boolean isSelected( DefaultMutableTreeNode node ) {
        for( int i = 0; i < node.getChildCount(); i++ ) {
            DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);
            Object o = ch.getUserObject();
            if ( o instanceof HintMetadata ) {
                HintMetadata hint = (HintMetadata)o;
                if ( HintsSettings.isEnabled(hint, getCurrentPrefernces(hint.id)) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // MouseListener implementation --------------------------------------------
    
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        TreePath path = errorTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
        if ( path != null ) {
            Rectangle r = errorTree.getPathBounds(path);
            if (r != null) {
                r.width = r.height;
                if ( r.contains(p)) {
                    toggle( path );
                }
            }
        }
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}
    
    // KeyListener implementation ----------------------------------------------

    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER ) {

            if ( e.getSource() instanceof JTree ) {
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getSelectionPath();

                if ( toggle( path )) {
                    e.consume();
                }
            }
        }
    }
    
    // TreeSelectionListener implementation ------------------------------------
    
    public void valueChanged(TreeSelectionEvent ex) {            
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof HintMetadata ) {
            if (defModel != severityComboBox.getModel()) {
                severityComboBox.setModel(defModel);
                Mnemonics.setLocalizedText(severityLabel, defLabel);
            }

            HintMetadata hint = (HintMetadata) o;
            
            // Enable components
            componentsSetEnabled(true);
            
            // Set proper values to the componetnts
            
            Preferences p = getCurrentPrefernces(hint.id);

            HintSeverity severity = HintsSettings.getSeverity(hint, p);
            if (severity != null) {
                severityComboBox.setSelectedIndex(severity2index.get(severity));
                severityComboBox.setEnabled(true);
            } else {
                severityComboBox.setSelectedIndex(severity2index.get(HintSeverity.ERROR));
                severityComboBox.setEnabled(false);
            }
            
            boolean toTasklist = HintsSettings.isShowInTaskList(hint, p);
            tasklistCheckBox.setSelected(toTasklist);
            
            String description = hint.description;
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description)); // NOI18N
                                    
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = hint.customizer != null ? hint.customizer.getCustomizer(ex == null ?
                getCurrentPrefernces(hint.id) :
                getPreferences4Modification(hint.id)) : null;

            if ( c != null ) {               
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
        }
        else if (o instanceof String) {
            DependencyTracking dt = getCurrentDependencyTracking();
            if (depScanningModel != severityComboBox.getModel()) {
                severityComboBox.setModel(depScanningModel);
                Mnemonics.setLocalizedText(severityLabel, depScanningLabel);
            }
            componentsSetEnabled(false);
            severityComboBox.setEnabled(true);
            descriptionTextArea.setEnabled(true);
            descriptionTextArea.setText(wrapDescription(depScanningDescription));
            descriptionTextArea.setCaretPosition(0);
            if (dt != DependencyTracking.DISABLED)
                severityComboBox.setSelectedIndex(deptracking2index.get(dt));
        }
        else { // Category or nonsense selected.
            if (defModel != severityComboBox.getModel()) {
                severityComboBox.setModel(defModel);
                Mnemonics.setLocalizedText(severityLabel, defLabel);
            }
            componentsSetEnabled(false);
        }
    }
    
    // ActionListener implementation -------------------------------------------
    
    public void actionPerformed(ActionEvent e) {
        if( errorTree.getSelectionPath() == null || !severityComboBox.equals(e.getSource()))
            return;
        
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof HintMetadata ) {
            HintMetadata hint = (HintMetadata) o;
            Preferences p = getPreferences4Modification(hint.id);
            
            if(HintsSettings.getSeverity(hint, p) != null)
                HintsSettings.setSeverity(p, index2severity(severityComboBox.getSelectedIndex()));            
        } else if (o instanceof String) {
            if (getCurrentDependencyTracking() != DependencyTracking.DISABLED)
                depScn = index2deptracking(severityComboBox.getSelectedIndex());
        }
    }

   
    // ChangeListener implementation -------------------------------------------
    
    public void stateChanged(ChangeEvent e) {
        // System.out.println("Task list box changed ");
    }
   
    // Private methods ---------------------------------------------------------

    private String wrapDescription( String description ) {
        return new StringBuffer( DESCRIPTION_HEADER ).append(description).append(DESCRIPTION_FOOTER).toString();        
    }
    
    private HintSeverity index2severity( int index ) {
        for( Map.Entry<HintSeverity,Integer> e : severity2index.entrySet()) {
            if ( e.getValue() == index ) {
                return e.getKey();
            }
        }
        throw new IllegalStateException( "Unknown severity");
    }
       
    private DependencyTracking index2deptracking( int index ) {
        for( Map.Entry<DependencyTracking,Integer> e : deptracking2index.entrySet()) {
            if ( e.getValue() == index ) {
                return e.getKey();
            }
        }
        throw new IllegalStateException( "Unknown severity");
    }

    private boolean toggle( TreePath treePath ) {

        if( treePath == null )
            return false;

        Object o = getUserObject(treePath);

        DefaultTreeModel model = errorTreeModel;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();


        if ( o instanceof HintMetadata ) {
            HintMetadata hint = (HintMetadata)o;
            boolean value = HintsSettings.isEnabled(hint,getCurrentPrefernces(hint.id));
            Preferences mn = getPreferences4Modification(hint.id);
            HintsSettings.setEnabled(mn, !value);
            model.nodeChanged(node);
            model.nodeChanged(node.getParent());
        }
        else if ( o instanceof HintCategory ) {
            boolean value = !isSelected(node);
                                   
            for( int i = 0; i < node.getChildCount(); i++ ) {
                DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);                
                Object cho = ch.getUserObject();
                if ( cho instanceof HintMetadata ) {
                    HintMetadata hint = (HintMetadata)cho;
                    boolean cv = HintsSettings.isEnabled(hint,getCurrentPrefernces(hint.id));
                    if ( cv != value ) {                    
                        Preferences mn = getPreferences4Modification(hint.id);
                        HintsSettings.setEnabled(mn, value);
                        model.nodeChanged( ch );
                    }
                }
            }            
            model.nodeChanged(node);
        }
        else if (o instanceof String) {
            DependencyTracking value = getCurrentDependencyTracking();
            depScn = value != DependencyTracking.DISABLED ? DependencyTracking.DISABLED : index2deptracking(severityComboBox.getSelectedIndex());
            model.nodeChanged(node);
            model.nodeChanged(node.getParent());
        }

        return false;
    }
    
    private void componentsSetEnabled( boolean enabled ) {
        
        if ( !enabled ) {
            customizerPanel.removeAll();
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
            severityComboBox.setSelectedIndex(severity2index.get(HintsSettings.SEVERITY_DEFAUT));
            tasklistCheckBox.setSelected(HintsSettings.IN_TASK_LIST_DEFAULT);
            descriptionTextArea.setText(""); // NOI18N
        }
        
        severityComboBox.setEnabled(enabled);
        tasklistCheckBox.setEnabled(enabled);
        descriptionTextArea.setEnabled(enabled);
    }

    public static final class HintCategory {
        private  static final String HINTS_FOLDER = "org-netbeans-modules-java-hints/rules/hints/";  // NOI18N

        public final String codeName;
        public final String displayName;

        public HintCategory(String codeName) {
            this.codeName = codeName;
            FileObject catFO = FileUtil.getConfigFile(HINTS_FOLDER + codeName);
            this.displayName = catFO != null ? HintsPanel.getFileObjectLocalizedName(catFO) : codeName;
        }

    }

    // Inner classes -----------------------------------------------------------
           
    private static class ModifiedPreferences extends AbstractPreferences {
        
        private Map<String,Object> map = new HashMap<String, Object>();

        public ModifiedPreferences( Preferences node ) {
            super(null, ""); // NOI18N
            try {                
                for (java.lang.String key : node.keys()) {
                    put(key, node.get(key, null));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
             
        
        public void store( Preferences target ) {
            
            try {
                for (String key : keys()) {
                    target.put(key, get(key, null));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
        
        protected void putSpi(String key, String value) {
            map.put(key, value);            
        }

        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }

        protected void removeSpi(String key) {
            map.remove(key);
        }

        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

}
