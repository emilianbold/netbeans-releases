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
import javax.swing.JEditorPane;
import javax.swing.JPanel;
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

import org.netbeans.modules.java.hints.spi.AbstractHint;
import static org.netbeans.modules.java.hints.spi.AbstractHint.*;


/** Contains all important listeners and logic of the Hints Panel.
 *
 * @author Petr Hrebejk
 */
class HintsPanelLogic implements MouseListener, KeyListener, TreeSelectionListener, ChangeListener, ActionListener {

    private Map<AbstractHint,ModifiedPreferences> changes;
    
    private static Map<HintSeverity,Integer> severity2index;
    
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
    }
    
    private JTree errorTree;
    private JComboBox severityComboBox;
    private JCheckBox tasklistCheckBox;
    private JPanel customizerPanel;
    private JEditorPane descriptionTextArea;
    
    HintsPanelLogic() {
        changes = new HashMap<AbstractHint, ModifiedPreferences>();        
    }
    
    void connect( JTree errorTree, JComboBox severityComboBox, 
                  JCheckBox tasklistCheckBox, JPanel customizerPanel,
                  JEditorPane descriptionTextArea) {
        
        this.errorTree = errorTree;
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
        for (AbstractHint hint : changes.keySet()) {
            ModifiedPreferences mn = changes.get(hint);
            mn.store(hint.getPreferences(HintsSettings.getCurrentProfileId()));            
        }
    }
    
    /** Were there any changes in the settings
     */
    boolean isChanged() {
        return !changes.isEmpty();
    }
    
    synchronized Preferences getCurrentPrefernces( AbstractHint hint ) {
        Preferences node = changes.get(hint);
        return node == null ? hint.getPreferences( HintsSettings.getCurrentProfileId() ) : node;
    }
    
    synchronized Preferences getPreferences4Modification( AbstractHint hint ) {        
        Preferences node = changes.get(hint);        
        if ( node == null ) {
            node = new ModifiedPreferences( hint.getPreferences( HintsSettings.getCurrentProfileId() ) );
            changes.put( hint, (ModifiedPreferences)node);
        }        
        return node;                
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
            if ( o instanceof AbstractHint ) {
                AbstractHint hint = (AbstractHint)o;
                if ( HintsSettings.isEnabled(hint, getCurrentPrefernces(hint)) ) {
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
        
        if ( o instanceof AbstractHint ) {
            AbstractHint hint = (AbstractHint) o;
            
            // Enable components
            componentsSetEnabled(true);
            
            // Set proper values to the componetnts
            
            Preferences p = getCurrentPrefernces(hint);
            
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
            
            String description = hint.getDescription();
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description)); // NOI18N
                                    
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = hint.getCustomizer(ex == null ? 
                getCurrentPrefernces(hint) :
                getPreferences4Modification(hint));
            
            if ( c != null ) {               
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
        }
        else { // Category or nonsense selected.
            componentsSetEnabled(false);
        }
    }
    
    // ActionListener implementation -------------------------------------------
    
    public void actionPerformed(ActionEvent e) {
        if( errorTree.getSelectionPath() == null )
            return;
        
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof AbstractHint ) {
            AbstractHint hint = (AbstractHint) o;
            Preferences p = getPreferences4Modification(hint);
            
            if(HintsSettings.getSeverity(hint, p) != null && severityComboBox.equals( e.getSource() ) )
                HintsSettings.setSeverity(p, index2severity(severityComboBox.getSelectedIndex()));            
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
       
    private boolean toggle( TreePath treePath ) {

        if( treePath == null )
            return false;

        Object o = getUserObject(treePath);

        DefaultTreeModel model = (DefaultTreeModel) errorTree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();


        if ( o instanceof AbstractHint ) {
            AbstractHint hint = (AbstractHint)o;
            boolean value = HintsSettings.isEnabled(hint,getCurrentPrefernces(hint));
            Preferences mn = getPreferences4Modification(hint);
            HintsSettings.setEnabled(mn, !value);
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
                    boolean cv = HintsSettings.isEnabled(hint,getCurrentPrefernces(hint));
                    if ( cv != value ) {                    
                        Preferences mn = getPreferences4Modification(hint);
                        HintsSettings.setEnabled(mn, value);
                        model.nodeChanged( ch );
                    }
                }
            }            
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
            severityComboBox.setSelectedIndex(severity2index.get(HintsSettings.SEVERITY_DEFAUT));
            tasklistCheckBox.setSelected(HintsSettings.IN_TASK_LIST_DEFAULT);
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
