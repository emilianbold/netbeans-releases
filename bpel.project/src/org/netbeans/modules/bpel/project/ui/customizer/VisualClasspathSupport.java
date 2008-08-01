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
package org.netbeans.modules.bpel.project.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URI;

import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.util.Utilities;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.project.spi.JbiArtifactProvider;

final class VisualClasspathSupport {

    private final Project master;
    private final JList classpathList;
    private final JButton addArtifactButton;
    private final JButton removeButton;
    private final JButton upButton;
    private final JButton downButton;
    private final DefaultListModel classpathModel;
    private final ArrayList actionListeners = new ArrayList();

    public VisualClasspathSupport(Project p, JList l, JButton a, JButton r, JButton u, JButton d) {
        this.master = p;
        this.classpathList = l;
        this.classpathModel = new DefaultListModel();
        this.classpathList.setModel( classpathModel );
        this.classpathList.setCellRenderer( new ClassPathCellRenderer() );

        this.addArtifactButton = a;
        this.removeButton = r;
        this.upButton = u;
        this.downButton = d;

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
        ArrayList listeners;

        synchronized ( this ) {
             listeners = new ArrayList( actionListeners );
        }

        ActionEvent ae = new ActionEvent( this, 0, null );

        for( Iterator it = listeners.iterator(); it.hasNext(); ) {
            ActionListener al = (ActionListener)it.next();
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

    private void addArtifacts(AntArtifact [] artifacts) {

        int[] si = classpathList.getSelectedIndices();

        int lastIndex = si == null || si.length == 0 ? -1 : si[si.length - 1];
        int[] indexes = new int[artifacts.length];
        for( int i = 0; i < artifacts.length; i++ ) {
            int current = lastIndex + 1 + i;
            classpathModel.add(current, new VisualClassPathItem(artifacts[i], VisualClassPathItem.TYPE_ARTIFACT, null, getArtifactLocation(artifacts[i]).toString(), true));
            indexes[i] = current;
        }
        this.classpathList.setSelectedIndices(indexes);

        fireActionPerformed();
    }

    private void removeElements() {
        int[] si = classpathList.getSelectedIndices();

        if (si == null || si.length == 0 ) {
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
        int [] si = classpathList.getSelectedIndices();

        if (si == null || si.length == 0 ) {
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
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            if ( source == addArtifactButton ) {
                AntArtifact [] artifacts = AntArtifactChooser.showDialog(JbiArtifactProvider.ARTIFACT_TYPE_JBI_ASA, classpathModel, master);
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

        public void valueChanged( ListSelectionEvent e ) {
            int[] si = classpathList.getSelectedIndices();

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

        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            setIcon( ((VisualClassPathItem)value).getIcon() );
            setToolTipText( value.toString() );
            return this;
        }
    }
}
