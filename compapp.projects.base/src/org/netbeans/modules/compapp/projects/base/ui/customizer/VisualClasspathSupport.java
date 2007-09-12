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


package org.netbeans.modules.compapp.projects.base.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.util.Utilities;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.base.spi.JbiArtifactProvider;


/** Handles adding, removing, editing and reordering of classpath.
 */
public final class VisualClasspathSupport {

    final Project master;
    final JList  classpathList;
    final JButton addArtifactButton;
    final JButton removeButton;
    final JButton upButton;
    final JButton downButton;

    private final DefaultListModel classpathModel;

    private final List<ActionListener> actionListeners = new ArrayList<ActionListener>();

    public VisualClasspathSupport(Project master, JList classpathList, JButton addArtifactButton, JButton removeButton, JButton upButton, JButton downButton) {
        this.master = master;

        this.classpathList = classpathList;
        this.classpathModel = new DefaultListModel();
        this.classpathList.setModel( classpathModel );
        this.classpathList.setCellRenderer( new ClassPathCellRenderer() );

        this.addArtifactButton = addArtifactButton;
        this.removeButton = removeButton;
        this.upButton = upButton;
        this.downButton = downButton;

        // Register the listeners
        ClasspathSupportListener csl = new ClasspathSupportListener();

        // On all buttons
        addArtifactButton.addActionListener(csl);
        removeButton.addActionListener(csl);
        upButton.addActionListener(csl);
        downButton.addActionListener(csl);

        // On list selection
        classpathList.getSelectionModel().addListSelectionListener( csl );

        // Set the initial state of the buttons
        csl.valueChanged(null);
    }

    public void setVisualClassPathItems(List items) {

        classpathModel.clear();
        for( Iterator it = items.iterator(); it.hasNext(); ) {
            VisualClassPathItem cpItem = (VisualClassPathItem)it.next();
            classpathModel.addElement( cpItem );
        }
    }

    public List getVisualClassPathItems() {

        ArrayList items = new ArrayList();
        for( Enumeration e = classpathModel.elements(); e.hasMoreElements(); ) {
            VisualClassPathItem cpItem = (VisualClassPathItem)e.nextElement();
            items.add( cpItem );
        }

        return items;

    }

    /** Action listeners will be informed when the value of the
     * list changes.
     */
    public void addActionListener( ActionListener listener ) {
        actionListeners.add( listener );
    }

    public void removeActionListener( ActionListener listener ) {
        actionListeners.remove( listener );
    }

    private void fireActionPerformed() {
        List<ActionListener> listeners;

        synchronized ( this ) {
             listeners = new ArrayList<ActionListener>( actionListeners );
        }

        ActionEvent ae = new ActionEvent( this, 0, null );

        for( Iterator<ActionListener> it = listeners.iterator(); it.hasNext(); ) {
            ActionListener al = it.next();
            al.actionPerformed( ae );
        }
    }

    // Private methods ---------------------------------------------------------

    private URI getArtifactLocation(AntArtifact artifact) {
        URI[] us = artifact.getArtifactLocations();
        if ((us != null) && (us.length > 0)) {
            return us[0];
        }
        return null;
    }

    private void addArtifacts(AntArtifact artifacts[]) {

        int[] si = classpathList.getSelectedIndices();

        int lastIndex = si == null || si.length == 0 ? -1 : si[si.length - 1];
        int[] indexes = new int[artifacts.length];
        for( int i = 0; i < artifacts.length; i++ ) {
            int current = lastIndex + 1 + i;
            classpathModel.add(current,
                new VisualClassPathItem(artifacts[i], VisualClassPathItem.TYPE_ARTIFACT, null, getArtifactLocation(artifacts[i]).toString(), true));
                    // VisualClassPathItem.create(artifacts[i]));
            indexes[i] = current;
        }
        this.classpathList.setSelectedIndices(indexes);

        fireActionPerformed();
    }

    private void removeElements() {

        int[] si = classpathList.getSelectedIndices();

        if(  si == null || si.length == 0 ) {
            assert false : "Remove button should be disabled"; // NOI18N
        }

        // Remove the items
        for( int i = si.length - 1 ; i >= 0 ; i-- ) {
            classpathModel.remove( si[i] );
        }


        if ( !classpathModel.isEmpty() ) {
            // Select reasonable item
            int selectedIndex = si[si.length - 1] - si.length  + 1;
            if ( selectedIndex > classpathModel.size() - 1) {
                selectedIndex = classpathModel.size() - 1;
            }
            classpathList.setSelectedIndex( selectedIndex );
        }

        fireActionPerformed();
    }

    private void moveUp() {

        int[] si = classpathList.getSelectedIndices();

        if(  si == null || si.length == 0 ) {
            assert false : "MoveUp button should be disabled"; // NOI18N
        }

        // Move the items up
        for( int i = 0; i < si.length; i++ ) {
            Object item = classpathModel.get( si[i] );
            classpathModel.remove( si[i] );
            classpathModel.add( si[i] - 1, item );
        }

        // Keep the selection a before
        for( int i = 0; i < si.length; i++ ) {
            si[i] -= 1;
        }
        classpathList.setSelectedIndices( si );

        fireActionPerformed();
    }

    private void moveDown() {

        int[] si = classpathList.getSelectedIndices();

        if(  si == null || si.length == 0 ) {
            assert false : "MoveDown button should be disabled"; // NOI18N
        }

        // Move the items up
        for( int i = si.length -1 ; i >= 0 ; i-- ) {
            Object item = classpathModel.get( si[i] );
            classpathModel.remove( si[i] );
            classpathModel.add( si[i] + 1, item );
        }

        // Keep the selection a before
        for( int i = 0; i < si.length; i++ ) {
            si[i] += 1;
        }
        classpathList.setSelectedIndices( si );

        fireActionPerformed();
    }

    // Private innerclasses ----------------------------------------------------

    private class ClasspathSupportListener implements ActionListener, ListSelectionListener {

        // Implementation of ActionListener ------------------------------------
        /** Handles button events
         */
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            if ( source == addArtifactButton ) {
                AntArtifact artifacts[] = AntArtifactChooser.showDialog(JbiArtifactProvider.ARTIFACT_TYPE_JBI_ASA, classpathModel, master);
                if ( artifacts != null ) {
                    addArtifacts( artifacts );
                }
            } else if ( source == removeButton ) {
                removeElements();
            } else if ( source == upButton ) {
                moveUp();
            } else if ( source == downButton ) {
                moveDown();
            }
        }

        // ListSelectionModel --------------------------------------------------
        /** Handles changes in the selection
         */
        public void valueChanged( ListSelectionEvent e ) {

            int[] si = classpathList.getSelectedIndices();

            // addJar allways enabled

            // addLibrary allways enabled

            // addArtifact allways enabled

            // edit enabled only if selection is not empty
            boolean edit = si != null && si.length > 0;

            // remove enabled only if selection is not empty
            boolean remove = si != null && si.length > 0;
            // and when the selection does not contain unremovable item
            if ( remove ) {
                for ( int i = 0; i < si.length; i++ ) {
                    assert si[i] < classpathModel.getSize () : "The selected indices " + Arrays.asList (Utilities.toObjectArray (si)) + // NOI18N
                                                                " at " + i +  // NOI18N
                                                                " must fit into size of classpathModel" + classpathModel.getSize (); // NOI18N
                    VisualClassPathItem vcpi = (VisualClassPathItem)classpathModel.get( si[i] );
                    if ( !vcpi.canDelete() ) {
                        remove = false;
                        break;
                    }
                }
            }

            // up button enabled if selection is not empty
            // and the first selected index is not the first row
            boolean up = si != null && si.length > 0 && si[0] != 0;

            // up button enabled if selection is not empty
            // and the laset selected index is not the last row
            boolean down = si != null && si.length > 0 && si[si.length-1] != classpathModel.size() - 1;

            removeButton.setEnabled( remove );
            upButton.setEnabled( up );
            downButton.setEnabled( down );

        }
    }


    private static class ClassPathCellRenderer extends DefaultListCellRenderer {


        @Override
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            setIcon( ((VisualClassPathItem)value).getIcon() );
            setToolTipText( value.toString() );

            return this;
        }

    }
}
