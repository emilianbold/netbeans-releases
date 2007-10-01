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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.options;

import java.beans.*;
import java.awt.event.*;
import java.awt.Dialog;
import java.awt.Component;
import javax.swing.*;
import java.util.*;

import org.openide.*;
import org.openide.util.NbBundle;
import org.netbeans.editor.*;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 * @author  Petr Nejedly
 * @deprecated Without any replacement.
 */
public class KeyBindingsEditorPanel extends javax.swing.JPanel {

    private ActionDescriptor[] acts;
    private int actionIndex;
    private String kitClassName;
    private KeyBindingsEditor editor;
    private ButtonGroup sortGroup;

    /** Creates new form KeyBindingsEditorPanel */
    public KeyBindingsEditorPanel( KeyBindingsEditor editor ) {
        this.editor = editor;
        initComponents ();
        
        getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_KBEP_Panel")); // NOI18N
        sequencesLabel.setDisplayedMnemonic(getBundleString("KBEP_Sequences_Mnemonic").charAt (0)); // NOI18N
        actionsList.getAccessibleContext().setAccessibleName(getBundleString("ACSN_KBEP_Actions")); // NOI18N
        actionsList.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_KBEP_Actions")); // NOI18N
        sequencesList.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_KBEP_Sequences")); // NOI18N
        nameSortRadioButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_KBEP_name_sort_button")); // NOI18N
        actionSortRadioButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_KBEP_action_sort_button")); // NOI18N
        addSequenceButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_KBEP_Add")); // NOI18N
        removeSequenceButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_KBEP_Remove")); // NOI18N
        
        // set up ButtonGroup for sort Buttons
        sortGroup = new ButtonGroup ();
        sortGroup.add (actionSortRadioButton);
        sortGroup.add (nameSortRadioButton);
    }

    private String getBundleString(String s) {
        return NbBundle.getMessage(KeyBindingsEditorPanel.class, s);
    }        
    
    
    /**
     * Sets the current editorKit and action->Vector(KeyStroke[]) mapping.
     * Note: first item points to proper EditorKit class.
     */
    public void setValue( List l ) {
        if (l != null)
            kitClassName = (String)l.get( 0 );
        else
            return;
        
        Class kitClass = null;
        try {
            ClassLoader loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            kitClass = Class.forName( kitClassName, true, loader);
        } catch( ClassNotFoundException e ) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
            return;
        }

        // Get all actions available in given kit, sort them and store their
        // ActionDescriptors. Prepare mapping for looking them up by their names.
        Class actionKitClass = kitClass;
        if( actionKitClass == BaseKit.class ) 
            actionKitClass =  NbEditorKit.class; // Hack to get actions from higher-located kits too
        Action[] actions = BaseKit.getKit( actionKitClass ).getActions();

        // Create our sorter, ActionDescriptors knows themselves how to sort
        TreeMap treeMap = new TreeMap( );
        // Fill it with new ActionDescriptors for actions, they'll be in-sorted
        for( int i=0; i<actions.length; i++ ) {
            if (actions[i]!=null){
                Object internalActionObj = actions[i].getValue(BaseAction.NO_KEYBINDING);
                if (internalActionObj instanceof Boolean){
                    if (((Boolean)internalActionObj).booleanValue()){
                        continue; //filter out editor internal actions - #49589
                    }
                }
            }
            ActionDescriptor val = new ActionDescriptor( actions[i] );
            treeMap.put( val.name, val );
        }

        // add all inherited bindings
        Class parent = kitClass.getSuperclass();
        Settings.KitAndValue[] kv = Settings.getValueHierarchy( parent, SettingsNames.KEY_BINDING_LIST );
        // go through all levels and add inherited bindings
        for( int i=kv.length - 1; i >= 0; i--)
            addKeyBindingList( treeMap, ((List)kv[i].value).iterator(), true );


        // add bindings of current kit - couple ActionDescriptors with proper KeySequences
        addKeyBindingList( treeMap, l.listIterator( 1 ), false );

        // Create our sorted list of ActionDescriptors
        acts = (ActionDescriptor[])treeMap.values().toArray( new ActionDescriptor[0] );

        // do we have anything to manage?
        if( acts.length > 0 ) addSequenceButton.setEnabled( true );

        // sort all Actions
        Arrays.sort (acts);
        
        actionsList.setListData( acts );
        actionsList.setSelectedIndex( actionIndex );
        updateSequences( 0 );
        
        // select the right sort button
        if (ActionDescriptor.getSortMode () == ActionDescriptor.SORT_BY_ACTION) {
            actionSortRadioButton.setSelected (true);
        } else {
            nameSortRadioButton.setSelected (true);
        }
    }

    private void addKeyBindingList( Map target, Iterator source, boolean inherited ) {
        while( source.hasNext() ) {
            MultiKeyBinding b = (MultiKeyBinding)source.next();
            ActionDescriptor ad = (ActionDescriptor)target.get( b.actionName );

            if( ad != null ) {  // we've found proper action
                KeySequence sequence = getKeySequenceForBinding( inherited, b );

                if( sequence == null ) {
                } else {
                    ad.sequences.add( sequence );
                }
            } else {
                // complain for weird mapping
                //System.err.println( "Weird mapping" );
            }
        }
    }

    private KeySequence getKeySequenceForBinding( boolean inherited, MultiKeyBinding binding ) {
        KeyStroke[] sequence = binding.keys;
        if( sequence == null ) { // convert simple KeyStroke to KeyStroke[1]
            if( binding.key == null ) return null;
            sequence = new KeyStroke[1];
            sequence[0] = binding.key;
        }
        return new KeySequence( inherited, sequence );
    }

    /**
     * Return the list of MultiKeyBindings
     */
    public List getValue() {
        Vector val = new Vector();
        // add the kitClass of current kit
        val.add( kitClassName );

        // go through whole array of Actions and add all KeySequences for every Action
        for( int i=0; i<acts.length; i++ ) {
            String name = acts[i].name;
            for( Iterator iter=acts[i].sequences.iterator(); iter.hasNext(); ) {
                KeySequence seq = (KeySequence)iter.next();
                if( !seq.isInherited() ) { // add only our bindings, not inherited
                    val.add( new MultiKeyBinding( seq.getKeyStrokes(), name ) );
                }
            }
        }

        // that's it, done
        return val;
    }

    // index tells which sequence to select
    private void updateSequences( int index ) {
        Vector bindings = acts[actionIndex].sequences;
        // reflect the change in actionIndex or actual sequenceList
        sequencesList.setListData( bindings );
        // select proper line, this will also fire ValueChanged on sequencesList
        if( bindings.size() > 0 ) sequencesList.setSelectedIndex( index );
    }

    // index tells which sequence is selected - to which we are bound
    private void updateRemoveButton() {
        int id = sequencesList.getSelectedIndex() ;
        Vector b = acts[actionIndex].sequences;

        boolean enable = id >= 0 && id < b.size() && !((KeySequence)b.get( id )).isInherited();
        removeSequenceButton.setEnabled( enable );
    }


    private void notifyEditor() {
        if( editor != null ) editor.customEditorChange();
    }

    private void sortActionsList () {
        // set sort mode of ActionDescriptor
        int mode = actionSortRadioButton.isSelected () ? ActionDescriptor.SORT_BY_ACTION
                                                     : ActionDescriptor.SORT_BY_NAME;
        ActionDescriptor.setSortMode (mode);
        ActionDescriptor ad = acts [actionIndex];
        // resort array
        Arrays.sort (acts);
        
        // refresh list content and select right item
        int newIndex = 0;
        for (int x=0; x < acts.length; x++) {
            if (acts [x] == ad) {
                newIndex = x;
                break;
            }
        }
        actionsList.setListData (acts);
        // note: setListData will call actionsListValueChanged which will set
        // actionIndex to -1 because the selection is empty.
        actionIndex = newIndex;
        actionsList.setSelectedIndex (actionIndex);
        actionsList.ensureIndexIsVisible (actionIndex);
        actionsList.requestFocus ();
    }
    
    /**
     * Create our visual representation.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        actionsPanel = new javax.swing.JPanel();
        sortButtonsPanel = new javax.swing.JPanel();
        nameSortRadioButton = new javax.swing.JRadioButton();
        actionSortRadioButton = new javax.swing.JRadioButton();
        actionsScrollPane = new javax.swing.JScrollPane();
        actionsList = new javax.swing.JList();
        sequencesPanel = new javax.swing.JPanel();
        sequencesLabel = new javax.swing.JLabel();
        sequencesScrollPane = new javax.swing.JScrollPane();
        sequencesList = new javax.swing.JList();
        addSequenceButton = new javax.swing.JButton();
        removeSequenceButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout(0, 12));

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 11, 11)));
        actionsPanel.setLayout(new java.awt.GridBagLayout());

        actionsPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)), getBundleString("KBEP_Actions")));
        sortButtonsPanel.setLayout(new java.awt.GridBagLayout());

        nameSortRadioButton.setMnemonic(getBundleString ("KBEP_name_sort_button_mnemonic").charAt (0));
        nameSortRadioButton.setText(getBundleString ("KBEP_name_sort_button"));
        nameSortRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameSortRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sortButtonsPanel.add(nameSortRadioButton, gridBagConstraints);

        actionSortRadioButton.setMnemonic(getBundleString ("KBEP_action_sort_button_mnemonic").charAt (0));
        actionSortRadioButton.setText(getBundleString ("KBEP_action_sort_button"));
        actionSortRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionSortRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        sortButtonsPanel.add(actionSortRadioButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        actionsPanel.add(sortButtonsPanel, gridBagConstraints);

        actionsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                actionsListValueChanged(evt);
            }
        });

        actionsScrollPane.setViewportView(actionsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        actionsPanel.add(actionsScrollPane, gridBagConstraints);

        add(actionsPanel, java.awt.BorderLayout.CENTER);

        sequencesPanel.setLayout(new java.awt.GridBagLayout());

        sequencesLabel.setLabelFor(sequencesList);
        sequencesLabel.setText(getBundleString("KBEP_Sequences"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 12);
        sequencesPanel.add(sequencesLabel, gridBagConstraints);

        sequencesScrollPane.setPreferredSize(new java.awt.Dimension(259, 80));
        sequencesList.setCellRenderer(new KeySequenceCellRenderer());
        sequencesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                sequencesListValueChanged(evt);
            }
        });

        sequencesScrollPane.setViewportView(sequencesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        sequencesPanel.add(sequencesScrollPane, gridBagConstraints);

        addSequenceButton.setMnemonic(getBundleString ("KBEP_Add_Mnemonic").charAt (0));
        addSequenceButton.setText(getBundleString( "KBEP_Add" ));
        addSequenceButton.setEnabled(false);
        addSequenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSequenceButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        sequencesPanel.add(addSequenceButton, gridBagConstraints);

        removeSequenceButton.setMnemonic(getBundleString ("KBEP_Remove_Mnemonic").charAt (0));
        removeSequenceButton.setText(getBundleString( "KBEP_Remove" ));
        removeSequenceButton.setEnabled(false);
        removeSequenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSequenceButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        sequencesPanel.add(removeSequenceButton, gridBagConstraints);

        add(sequencesPanel, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void nameSortRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameSortRadioButtonActionPerformed
        sortActionsList ();
    }//GEN-LAST:event_nameSortRadioButtonActionPerformed

    private void actionSortRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionSortRadioButtonActionPerformed
        sortActionsList ();
    }//GEN-LAST:event_actionSortRadioButtonActionPerformed

    private void sequencesListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_sequencesListValueChanged
        updateRemoveButton();
    }//GEN-LAST:event_sequencesListValueChanged

    private void addSequenceButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSequenceButtonActionPerformed
        // Create KeySequence input dialog and ask user for value
        KeySequence newSequence = new KeySequenceRequester().getKeySequence();
        // If user canceled action, stop entering
        if( newSequence == null ) return;
        // Add new KeySequence to proper list
        acts[actionIndex].sequences.add( newSequence );
        // Render and select the last added item
        updateSequences( acts[actionIndex].sequences.size()-1 );
        notifyEditor();
    }//GEN-LAST:event_addSequenceButtonActionPerformed

    private void removeSequenceButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSequenceButtonActionPerformed
        //  Remove selected sequences from sequenceList
        int index = sequencesList.getSelectedIndex();
        if( index >= 0 ) {
            acts[actionIndex].sequences.remove( index );
            if( index >= acts[actionIndex].sequences.size() ) index--;
            updateSequences( index );
            notifyEditor();
        }
    }//GEN-LAST:event_removeSequenceButtonActionPerformed

    private void actionsListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_actionsListValueChanged
        if (actionsList.getSelectedIndex () < 0) return;
        actionIndex = actionsList.getSelectedIndex();
        updateSequences( 0 );
    }//GEN-LAST:event_actionsListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList sequencesList;
    private javax.swing.JButton addSequenceButton;
    private javax.swing.JPanel sortButtonsPanel;
    private javax.swing.JPanel actionsPanel;
    private javax.swing.JScrollPane sequencesScrollPane;
    private javax.swing.JRadioButton actionSortRadioButton;
    private javax.swing.JPanel sequencesPanel;
    private javax.swing.JScrollPane actionsScrollPane;
    private javax.swing.JRadioButton nameSortRadioButton;
    private javax.swing.JButton removeSequenceButton;
    private javax.swing.JLabel sequencesLabel;
    private javax.swing.JList actionsList;
    // End of variables declaration//GEN-END:variables

    /**
     * Encapsulation for components of dialog asking for new KeySequence
     */
    private class KeySequenceRequester {

        KeySequenceInputPanel input;
        DialogDescriptor dd;
        Dialog dial;

        Object[] buttons = { new JButton( getBundleString( "KBEP_OK_LABEL" ) ), // NOI18N
                             new JButton( getBundleString("KBEP_CLEAR_LABEL" ) ), // NOI18N
                             DialogDescriptor.CANCEL_OPTION };
        KeySequence retVal = null;

        KeySequenceRequester() {
            ((JButton)buttons[0]).getAccessibleContext().setAccessibleDescription(LocaleSupport.getString("ACSD_KBEP_OK")); // NOI18N
            ((JButton)buttons[1]).getAccessibleContext().setAccessibleDescription(LocaleSupport.getString("ACSD_KBEP_CLEAR")); // NOI18N
            ((JButton)buttons[1]).setMnemonic (getBundleString("KBEP_CLEAR_Mnemonic").charAt (0)); // NOI18N
            ((JButton)buttons[0]).setEnabled( false ); // default initial state

            // Prepare KeySequence input dialog
            input = new KeySequenceInputPanel();
            input.addPropertyChangeListener( new PropertyChangeListener() {
                                                 public void propertyChange( PropertyChangeEvent evt ) {
                                                     if( KeySequenceInputPanel.PROP_KEYSEQUENCE != evt.getPropertyName() ) return;
                                                     KeyStroke[] seq = input.getKeySequence();
                                                     String warn = getCollisionString( seq );
                                                     input.setInfoText( warn == null ? "" : warn );  // NOI18N
                                                     ((JButton)buttons[0]).setEnabled( warn == null );
                                                 }
                                             } );

            dd = new DialogDescriptor ( input, getBundleString( "KBEP_AddSequence" ), // NOI18N
                                        true, buttons, buttons[0], DialogDescriptor.BOTTOM_ALIGN, HelpCtx.DEFAULT_HELP, new ActionListener(){
                                            public void actionPerformed( ActionEvent evt ) {
                                                if( evt.getSource() == buttons[1] ) { // Clear pressed
                                                    input.clear();          // Clear entered KeyStrokes, start again
                                                    input.requestFocus();   // Make user imediately able to enter new strokes
                                                } else if( evt.getSource() == buttons[0] ) { // OK pressed
                                                    retVal = new KeySequence( false, input.getKeySequence() );
                                                    dial.dispose();  // Done
                                                }
                                            }
                                        });

        }

        KeySequence getKeySequence() {
            dial = org.openide.DialogDisplayer.getDefault().createDialog(dd);
            input.requestFocus();  // Place caret in it, hopefully
            dial.setVisible(true); // let the user tell us their wish, result will be stored in retVal
            return retVal;
        }

        String getCollisionString( KeyStroke[] seq ) {
            if( seq.length == 0 ) return ""; // NOI18N   not valid sequence, but don't alert user

            for( int i=0; i<acts.length; i++ ) { // for all actions
                Iterator iter = acts[i].sequences.iterator();
                while( iter.hasNext() ) {
                    KeyStroke[] s1 = ((KeySequence)iter.next()).getKeyStrokes();
                    if( isOverlapingSequence( s1, seq ) ) {
                        Object[] values = { Utilities.keySequenceToString( s1 ), acts[i] };
                        return NbBundle.getMessage(KeyBindingsEditorPanel.class, "KBEP_FMT_Collision" , values );
                    }
                }
            }
            return null;  // no colliding sequence
        }

        private boolean isOverlapingSequence( KeyStroke[] s1, KeyStroke[] s2 ) {
            int l = Math.min( s1.length, s2.length );
            while( l-- > 0 ) if( !s1[l].equals( s2[l] ) ) return false;
            return true;
        }
    }



    /**
     * Information holder class for Action, it knows it's Action name, which
     * sequences is this Action bound to, and how to correctly present
     * it as String (it's displayName ).
     * It also knows how to sort it's instances (Comparable)
     * As it is private, all members could be directly read.
     */
    private static final class ActionDescriptor implements Comparable {
        
        public static final int SORT_BY_ACTION = 0;
        public static final int SORT_BY_NAME = 1;
        private static int sortMode = SORT_BY_NAME;
        
        String name;
        String displayName;
        Vector sequences;

        ActionDescriptor( Action a ) {
            name = (String)a.getValue( Action.NAME );
            String shortDesc = (String)a.getValue( Action.SHORT_DESCRIPTION );
            displayName = shortDesc == null ? name : shortDesc + " [" + name + "]"; // NOI18N
            sequences = new Vector();
        }

        public String toString() {
            return displayName;
        }

        // Naturaly ordered by its name
        public int compareTo( Object o ) {
            if (sortMode == SORT_BY_ACTION) {
                return name.compareTo( ((ActionDescriptor)o).name );
            } else {
                return displayName.compareToIgnoreCase (((ActionDescriptor)o).displayName);
            }
        }
        
        public static void setSortMode (int sMode) {
            sortMode = sMode;
        }
        public static int getSortMode () {
            return sortMode;
        }
    }

    /**
     * Container class for KeyStroke[], which knows if this KeyStroke is inherited
     * and how to correctly present it as String.
     */
    private final static class KeySequence {
        private boolean inherited;
        private KeyStroke[] sequence;

        KeySequence( boolean inherited, KeyStroke[] sequence) {
            this.inherited = inherited;
            this.sequence = sequence;
        }

        KeyStroke[] getKeyStrokes() {
            return sequence;
        }

        boolean isInherited() {
            return inherited;
        }

        public String toString() {
            return Utilities.keySequenceToString( sequence );
        }
    }

    /**
     * Special cell renderer for sequencesList, which renders inherited KeySequences
     * differently from not inherited to visually notify user which sequences
     * couldn't be removed.
     */
    private final static class KeySequenceCellRenderer extends JLabel implements ListCellRenderer {

        public KeySequenceCellRenderer() {
            setOpaque( true );
        }

        public Component getListCellRendererComponent( JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus
                                                     ) {
            setText( value.toString() );
            setBackground( isSelected ? list.getSelectionBackground() : list.getBackground() );

            if( (value instanceof KeySequence) && ((KeySequence)value).isInherited() )
                setForeground( java.awt.Color.gray );
            else
                setForeground( isSelected ? list.getSelectionForeground() : list.getForeground() );

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            return this;
        }
    }

}
