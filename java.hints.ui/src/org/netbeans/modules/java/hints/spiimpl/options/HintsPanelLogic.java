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

package org.netbeans.modules.java.hints.spiimpl.options;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.spiimpl.options.DepScanningSettings.DependencyTracking;
import org.netbeans.modules.java.hints.spiimpl.options.HintsPanel.State;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Configuration;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/** Contains all important listeners and logic of the Hints Panel.
 *
 * @author Petr Hrebejk
 */
public class HintsPanelLogic implements MouseListener, KeyListener, TreeSelectionListener, ChangeListener, ActionListener, ItemListener {

    private Map<String,ModifiedPreferences> changes = new HashMap<String, ModifiedPreferences>();
    private DependencyTracking depScn = null;
    
    private static final Map<Severity,Integer> severity2index;
    private static final Map<Integer,Severity> index2Severity;
    private static final Map<DependencyTracking,Integer> deptracking2index;
    
    private static final String DESCRIPTION_HEADER = 
        "<html><head>" + // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbdocs://org.netbeans.modules.usersguide/org/netbeans/modules/usersguide/ide.css\" type=\"text/css\">" // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbresloc:/org/netbeans/modules/java/hints/resources/ide.css\" type=\"text/css\">" + // NOI18N
        "</head><body>"; // NOI18N

    private static final String DESCRIPTION_FOOTER = "</body></html>"; // NOI18N
    
    
    static {
        severity2index = new EnumMap<Severity, Integer>(Severity.class);
        severity2index.put( Severity.ERROR, 0  );
        severity2index.put( Severity.VERIFIER, 1  );
        severity2index.put( Severity.HINT, 2  );
        severity2index.put( Severity.WARNING, 1  );
        index2Severity = new HashMap<Integer, Severity>();
        index2Severity.put(0, Severity.ERROR);
        index2Severity.put(1, Severity.VERIFIER);
        index2Severity.put(2, Severity.HINT);
        deptracking2index = new EnumMap<DepScanningSettings.DependencyTracking, Integer>(DepScanningSettings.DependencyTracking.class);
        deptracking2index.put(DependencyTracking.ENABLED, 0);
        deptracking2index.put(DependencyTracking.ENABLED_WITHIN_PROJECT, 1);
        deptracking2index.put(DependencyTracking.ENABLED_WITHIN_ROOT, 2);
    }
    
    private JTree errorTree;
    DefaultTreeModel errorTreeModel;
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
    private JComboBox configCombo;
    private String currentProfileId = HintsSettings.getCurrentProfileId();
    private JButton editScript;
    private Preferences overlayPreferences;
    
    HintsPanelLogic() {
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsError")); //NOI18N
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsWarning")); //NOI18N
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_WarningOnCurrentLine")); //NOI18N

        depScanningModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AllProjects")); //NOI18N
        depScanningModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_Project")); //NOI18N
        depScanningModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_SrcRoot")); //NOI18N
    }
    
    void connect( final JTree errorTree, DefaultTreeModel errorTreeModel, JLabel severityLabel, JComboBox severityComboBox,
                  JCheckBox tasklistCheckBox, JPanel customizerPanel,
                  JEditorPane descriptionTextArea, final JComboBox configCombo, JButton editScript) {
        
        this.errorTree = errorTree;
        this.errorTreeModel = errorTreeModel;
        this.severityLabel = severityLabel;
        this.severityComboBox = severityComboBox;
        this.tasklistCheckBox = tasklistCheckBox;
        this.customizerPanel = customizerPanel;
        this.descriptionTextArea = descriptionTextArea;        
        this.configCombo = configCombo;
        this.editScript = editScript;
        
        
        if (configCombo.getSelectedItem() !=null) {
            currentProfileId = ((Configuration) configCombo.getSelectedItem()).id();
        }        
        valueChanged( null );
        
        errorTree.addKeyListener(this);
        errorTree.addMouseListener(this);
        errorTree.getSelectionModel().addTreeSelectionListener(this);
            
        this.configCombo.addItemListener(this);
        severityComboBox.addActionListener(this);
        tasklistCheckBox.addChangeListener(this);
        
    }
    
    void disconnect() {
        
        errorTree.removeKeyListener(this);
        errorTree.removeMouseListener(this);
        errorTree.getSelectionModel().removeTreeSelectionListener(this);
            
        severityComboBox.removeActionListener(this);
        tasklistCheckBox.removeChangeListener(this);
        configCombo.removeItemListener(this);
        
        componentsSetEnabled( false );
    }
    
    String getCurrentProfileId() {
        return currentProfileId;
    }

    synchronized void setOverlayPreferences(Preferences prefs) {
        applyChanges();
        this.overlayPreferences = prefs;
        valueChanged(null);
        errorTree.repaint();
    }
    
    synchronized void applyChanges() {
	boolean containsChanges = false;
        for (String hint : changes.keySet()) {
            ModifiedPreferences mn = changes.get(hint);
	    containsChanges |= !mn.isEmpty();
            mn.store(HintsSettings.getPreferences(hint, getCurrentProfileId()));
        }
	if (containsChanges) {
	    HintsSettings.fireChangeEvent();
	}
        if (depScn != null)
            DepScanningSettings.setDependencyTracking(depScn);
        changes.clear();
    }
    
    /** Were there any changes in the settings
     */
    boolean isChanged() {
        return !changes.isEmpty() || depScn != null;
    }
    
    synchronized Preferences getCurrentPrefernces( String id ) {
        if (overlayPreferences != null) return overlayPreferences.node(id);
        Preferences node = changes.get(id);
        return node == null ? HintsSettings.getPreferences(id, getCurrentProfileId() ) : node;
    }
    
    synchronized Preferences getPreferences4Modification(String hint ) {
        if (overlayPreferences != null) return overlayPreferences.node(hint);
        Preferences node = changes.get(hint);        
        if ( node == null ) {
            node = new ModifiedPreferences(HintsSettings.getPreferences(hint, getCurrentProfileId() ) );
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
    
    State isSelected( DefaultMutableTreeNode node ) {
        boolean hasEnabled = false;
        boolean hasDisabled = false;
        for( int i = 0; i < node.getChildCount(); i++ ) {
            DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);
            Object o = ch.getUserObject();
            if ( o instanceof HintMetadata ) {
                HintMetadata hint = (HintMetadata)o;
                if ( HintsSettings.isEnabled(hint, getCurrentPrefernces(hint.id)) ) {
                    hasEnabled = true;
                } else {
                    hasDisabled = true;
                }
            }
        }
        return hasEnabled ? hasDisabled ? State.OTHER : State.SELECTED : State.NOT_SELECTED;
    }
    
    // MouseListener implementation --------------------------------------------
    
    @Override
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

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}
    
    // KeyListener implementation ----------------------------------------------

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {

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
    
    @Override
    public void valueChanged(TreeSelectionEvent ex) {            
        Object o = getUserObject(errorTree.getSelectionPath());
        
        editScript.setEnabled(false);
        if ( o instanceof HintMetadata ) {
            if (defModel != severityComboBox.getModel()) {
                severityComboBox.setModel(defModel);
                Mnemonics.setLocalizedText(severityLabel, defLabel);
            }

            HintMetadata hint = (HintMetadata) o;
            
            // Enable components
            componentsSetEnabled(true);
            
            editScript.setEnabled(hint.category.equals(HintCategory.CUSTOM_CATEGORY));
            
            // Set proper values to the componetnts
            
            Preferences p = getCurrentPrefernces(hint.id);

            if (hint.kind == Kind.ACTION) {
                severityComboBox.setSelectedIndex(severity2index.get(Severity.HINT));
                severityComboBox.setEnabled(false);
            } else {
                Severity severity = HintsSettings.getSeverity(hint, p);
                if (severity != null) {
                    severityComboBox.setSelectedIndex(severity2index.get(severity));
                    severityComboBox.setEnabled(true);
                } else {
                    severityComboBox.setSelectedIndex(severity2index.get(Severity.ERROR));
                    severityComboBox.setEnabled(false);
                }
            }
            
            boolean toTasklist = HintsSettings.isShowInTaskList(hint, p);
            tasklistCheckBox.setSelected(toTasklist);
            
            String description = hint.description;
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description, hint)); // NOI18N
                                    
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
            descriptionTextArea.setText(wrapDescription(depScanningDescription, null));
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
    
    @Override
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
    
    @Override
    public void stateChanged(ChangeEvent e) {
        // System.out.println("Task list box changed ");
    }
   
    // Private methods ---------------------------------------------------------

    private String wrapDescription( String description, HintMetadata hint ) {
        return new StringBuffer( DESCRIPTION_HEADER ).append(description).append(getQueryWarning(hint)).append(DESCRIPTION_FOOTER).toString();        
    }
    
    public static String getQueryWarning(HintMetadata hint) {
        if (hint==null || !hint.options.contains(Options.QUERY)) {
            return "";
        }
        return NbBundle.getMessage(HintsPanelLogic.class, "NO_REFACTORING");
        
    }
    
    private Severity index2severity( int index ) {
        Severity s = index2Severity.get(index);

        if (s == null) {
            throw new IllegalStateException( "Unknown severity");
        }

        return s;
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
        
        if (! (errorTree.getCellRenderer() instanceof HintsPanel.CheckBoxRenderer)) {
            //no checkboxes, no toggle
            return false;
        }

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
            boolean value = isSelected(node) == State.NOT_SELECTED;
                                   
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
        }

        return false;
    }
    
    private void componentsSetEnabled( boolean enabled ) {
        
        if ( !enabled ) {
            customizerPanel.removeAll();
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
            severityComboBox.setSelectedIndex(severity2index.get(Severity.VERIFIER));
            tasklistCheckBox.setSelected(false);
            descriptionTextArea.setText(""); // NOI18N
        }
        
        severityComboBox.setEnabled(enabled);
        tasklistCheckBox.setEnabled(enabled);
        descriptionTextArea.setEnabled(enabled);
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        Object o = configCombo.getSelectedItem();
        if (o instanceof Configuration) {
            applyChanges();
            currentProfileId = ((Configuration) o).id();
            valueChanged(null);
            errorTree.repaint();
        }
    }

    public static final class HintCategory {
        private  static final String HINTS_FOLDER = "org-netbeans-modules-java-hints/rules/hints/";  // NOI18N
        public static final String CUSTOM_CATEGORY ="custom";

        public final String codeName;
        public final String displayName;

        public HintCategory(String codeName) {
            this.codeName = codeName;
            FileObject catFO = FileUtil.getConfigFile(HINTS_FOLDER + codeName);
            this.displayName = catFO != null ? HintsPanel.getFileObjectLocalizedName(catFO) :
             CUSTOM_CATEGORY.equals(codeName)?NbBundle.getBundle("org.netbeans.modules.java.hints.resources.Bundle").getString("org-netbeans-modules-java-hints/rules/hints/custom"):codeName;
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
        
        @Override
        protected void putSpi(String key, String value) {
            map.put(key, value);            
        }

        @Override
        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }

        @Override
        protected void removeSpi(String key) {
            map.remove(key);
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

	boolean isEmpty() {
	    return map.isEmpty();
	}
    }

}
