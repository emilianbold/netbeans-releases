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

package org.netbeans.modules.java.hints.options;

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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

import org.netbeans.modules.java.hints.spi.AbstractHint;
import static org.netbeans.modules.java.hints.spi.AbstractHint.*;


/** Contains all important listeners and logic of the Hints Panel.
 *
 * @author Petr Hrebejk
 */
class HintsPanelLogic implements MouseListener, KeyListener, TreeSelectionListener, ChangeListener, ActionListener {

    private static final String HINTS = "hints"; // NOI18N
    private static final String DEFAULT_PROFILE = "default"; // NOI18N

    private static final JPanel EMPTY_PANEL = new JPanel();
    
    private Map<AbstractHint,ModifiedPreferences> changes;
    
    private static Map<Integer,Integer> severity2index;
    
    static {
        severity2index = new HashMap<Integer, Integer>();
        severity2index.put( HintSeverity.ERROR.ordinal(), 0 );
        severity2index.put( HintSeverity.WARNING.ordinal(), 1 );
        severity2index.put( HintSeverity.CURRENT_LINE_WARNING.ordinal(), 2 );        
    }
    
    private JTree errorTree;
    private JComboBox severityComboBox;
    private JCheckBox tasklistCheckBox;
    private JPanel customizerPanel;
    private JTextArea descriptionTextArea;
    
    HintsPanelLogic() {
        changes = new HashMap<AbstractHint, ModifiedPreferences>();        
    }
    
    void connect( JTree errorTree, JComboBox severityComboBox, 
                  JCheckBox tasklistCheckBox, JPanel customizerPanel,
                  JTextArea descriptionTextArea) {
        
        this.errorTree = errorTree;
        this.severityComboBox = severityComboBox;
        this.tasklistCheckBox = tasklistCheckBox;
        this.customizerPanel = customizerPanel;
        this.descriptionTextArea = descriptionTextArea;        
        
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
                
    }
    
    synchronized void applyChanges() {
        for (AbstractHint hint : changes.keySet()) {
            ModifiedPreferences mn = changes.get(hint);
            mn.store(getPreferences(hint));            
        }
    }
    
    /** Were there any changes in the settings
     */
    boolean isChanged() {
        return !changes.isEmpty();
    }
    
    synchronized Preferences getCurrentPrefernces( AbstractHint hint ) {
        Preferences node = changes.get(hint);
        return node == null ? getPreferences( hint ) : node;
    }
    
    synchronized Preferences getPreferences4Modification( AbstractHint hint ) {        
        Preferences node = changes.get(hint);        
        if ( node == null ) {
            node = new ModifiedPreferences( getPreferences( hint ) );
            changes.put( hint, (ModifiedPreferences)node);
        }        
        return node;                
    }
    
    
    
    static Object getUserObject( TreePath path ) {
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
            if ( o instanceof AbstractHint ) {
                AbstractHint hint = (AbstractHint)o;
                if ( getCurrentPrefernces(hint).getBoolean(ENABLED_KEY, ENABLED_DEFAULT)) {
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
    
    public void valueChanged(TreeSelectionEvent e) {            
        Object o = getUserObject(e.getPath());
        
        customizerPanel.setVisible(false);
        
        if ( o instanceof AbstractHint ) {
            AbstractHint hint = (AbstractHint) o;
            
            // Enable components
            componentsSetEnabled(true);
            
            // Set proper values to the componetnts
            
            Preferences p = getCurrentPrefernces(hint);
            
            int severity = p.getInt(AbstractHint.SEVERITY_KEY, AbstractHint.SEVERITY_DEFAULT.ordinal());
            severityComboBox.setSelectedIndex(severity2index.get(severity));
            
            boolean toTasklist = p.getBoolean(AbstractHint.IN_TASK_LIST_KEY, AbstractHint.IN_TASK_LIST_DEFAULT);
            tasklistCheckBox.setSelected(toTasklist);
            
            String description = hint.getDescription();
            descriptionTextArea.setText( description == null ? "" : description); // NOI18N
                                    
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = hint.getCustomizer(getPreferences4Modification(hint));
            
            if ( c == null ) {               
                customizerPanel.add(EMPTY_PANEL, BorderLayout.CENTER);;
            }
            else {
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
        }
        else { // Category or nonsense selected.
            componentsSetEnabled(false);
        }
        
        customizerPanel.setVisible(true);
        
    }
    
    // ActionListener implementation -------------------------------------------
    
    public void actionPerformed(ActionEvent e) {
        // System.out.println("Item selected " + severityComboBox.getSelectedIndex() );
    }

   
    // ChangeListener implementation -------------------------------------------
    
    public void stateChanged(ChangeEvent e) {
        // System.out.println("Task list box changed ");
    }
   
    // Private methods ---------------------------------------------------------
    
       
    private synchronized Preferences getPreferences( AbstractHint hint ) {
                        
        Preferences node = hint.getPreferences();
        
        if ( node == null ) {
            Preferences preferences = NbPreferences.forModule(hint.getClass());
            node = preferences.node(HINTS).node(getCurrentProfileId()).node(hint.getId());
        }
                
        return node;
    }
    
    private static String getCurrentProfileId() {
        return DEFAULT_PROFILE;
    }
    
    private boolean toggle( TreePath treePath ) {

        if( treePath == null )
            return false;

        Object o = getUserObject(treePath);

        DefaultTreeModel model = (DefaultTreeModel) errorTree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();


        if ( o instanceof AbstractHint ) {
            AbstractHint hint = (AbstractHint)o;
            boolean value = getCurrentPrefernces(hint).getBoolean(ENABLED_KEY, ENABLED_DEFAULT);
            Preferences mn = getPreferences4Modification(hint);
            mn.putBoolean(ENABLED_KEY, !value);                
            model.nodeChanged(node);
            model.nodeChanged(node.getParent());
        }
        else if ( o instanceof FileObject ) {
            boolean value = !isSelected(node);
                                   
            for( int i = 0; i < node.getChildCount(); i++ ) {
                DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);                
                Object cho = ch.getUserObject();
                if ( cho instanceof AbstractHint ) {
                    AbstractHint hint = (AbstractHint)cho;
                    boolean cv = getCurrentPrefernces(hint).getBoolean(ENABLED_KEY, ENABLED_DEFAULT);
                    if ( cv != value ) {                    
                        Preferences mn = getPreferences4Modification(hint);
                        mn.putBoolean(ENABLED_KEY, value);
                        model.nodeChanged( ch );
                    }
                }
            }            
            model.nodeChanged(node);
        }

        return false;
    }
    
    private void toggleHint( AbstractHint hint ) {        
        
    }
    
    private void componentsSetEnabled( boolean enabled ) {
        
        if ( !enabled ) {
            customizerPanel.removeAll();
            customizerPanel.add(EMPTY_PANEL, BorderLayout.CENTER);
            severityComboBox.setSelectedIndex(severity2index.get(AbstractHint.SEVERITY_DEFAULT.ordinal()));
            tasklistCheckBox.setSelected(AbstractHint.IN_TASK_LIST_DEFAULT);
            descriptionTextArea.setText(""); // NOI18N
        }
        
        severityComboBox.setEnabled(enabled);
        tasklistCheckBox.setEnabled(enabled);
        descriptionTextArea.setEnabled(enabled);
        
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
