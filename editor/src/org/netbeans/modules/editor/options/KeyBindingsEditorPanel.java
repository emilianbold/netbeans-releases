/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.text.options;

import java.beans.*;
import java.awt.event.*;
import java.awt.Dialog;
import java.awt.Component;
import javax.swing.*;
import java.text.MessageFormat;
import java.util.*;

import org.openide.*;
import org.openide.util.NbBundle;
import com.netbeans.editor.*;


/** 
 * @author  Petr Nejedly
 */
public class KeyBindingsEditorPanel extends javax.swing.JPanel {

  private static ResourceBundle bundle = NbBundle.getBundle( KeyBindingsEditorPanel.class );

  private ActionDescriptor[] acts;
  private int actionIndex;
  private String kitClassName;
  private KeyBindingsEditor editor;
  private String defaultActionName;
    
  /** Creates new form KeyBindingsEditorPanel */
  public KeyBindingsEditorPanel( KeyBindingsEditor editor ) {
    this.editor = editor;
    initComponents ();
  }

  /**
   * Sets the current editorKit and action->Vector(KeyStroke[]) mapping.
   * Note: first item points to proper EditorKit class.
   */
  public void setValue( List l ) {
    kitClassName = (String)l.get( 0 );
    Class kitClass = null;
    try {
      kitClass = Class.forName( kitClassName );
    } catch( ClassNotFoundException e ) {
      if( Boolean.getBoolean( "netbeans.debug.exceptions" ) )
        e.printStackTrace();
      return;
    }
    
    defaultActionName = null;

    // Get all actions available in given kit, sort them and store their
    // ActionDescriptors. Prepare mapping for looking them up by their names.
    Action[] actions = BaseKit.getKit( kitClass ).getActions();
    
    // Create our sorter, ActionDescriptors knows themselves how to sort
    TreeMap treeMap = new TreeMap( );

    // Fill it with new ActionDescriptors for actions, they'll be in-sorted
    for( int i=0; i<actions.length; i++ ) {
      ActionDescriptor val = new ActionDescriptor( actions[i] );
      treeMap.put( val.name, val );
    }
    
    // add all inherited bindings
    Class parent = kitClass.getSuperclass();
    Settings.KitAndValue[] kv = Settings.getValueHierarchy( parent, Settings.KEY_BINDING_LIST );
    // go through all levels and add inherited bindings
    for( int i=kv.length - 1; i >= 0; i--)
      addKeyBindingList( treeMap, ((List)kv[i].value).iterator(), true );

    
    // add bindings of current kit - couple ActionDescriptors with proper KeySequences
    addKeyBindingList( treeMap, l.listIterator( 1 ), false );
    
    // Create our sorted list of ActionDescriptors
    acts = (ActionDescriptor[])treeMap.values().toArray( new ActionDescriptor[0] );
    
    // do we have anything to manage?
    if( acts.length > 0 ) addSequenceButton.setEnabled( true );
    
    actionsList.setListData( acts );
    actionsList.setSelectedIndex( actionIndex );
    updateSequences( 0 );
  }

  private void addKeyBindingList( Map target, Iterator source, boolean inherited ) {
    while( source.hasNext() ) {
      MultiKeyBinding b = (MultiKeyBinding)source.next();
      ActionDescriptor ad = (ActionDescriptor)target.get( b.actionName );
      
      if( ad != null ) {  // we've found proper action
        KeySequence sequence = getKeySequenceForBinding( inherited, b ); 
      
        if( sequence == null ) {
          if( !inherited ) defaultActionName = b.actionName;
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

    // add default action if we have one
    if( defaultActionName != null)
      val.add( new MultiKeyBinding( (KeyStroke)null, defaultActionName ) );
    
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
  
  /**
   * Create our visual representation.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    actionsPanel = new javax.swing.JPanel ();
    actionsScrollPane = new javax.swing.JScrollPane ();
    actionsList = new javax.swing.JList ();
    sequencesPanel = new javax.swing.JPanel ();
    sequencesScrollPane = new javax.swing.JScrollPane ();
    sequencesList = new javax.swing.JList ();
    addSequenceButton = new javax.swing.JButton ();
    removeSequenceButton = new javax.swing.JButton ();
    setLayout (new javax.swing.BoxLayout (this, 1));
    setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));

    actionsPanel.setLayout (new javax.swing.BoxLayout (actionsPanel, 0));
    actionsPanel.setBorder (new javax.swing.border.CompoundBorder( new javax.swing.border.TitledBorder( bundle.getString( "KBEP_Actions" ) ), new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8) ) ));

  
        actionsList.addListSelectionListener (new javax.swing.event.ListSelectionListener () {
          public void valueChanged (javax.swing.event.ListSelectionEvent evt) {
            actionsListValueChanged (evt);
          }
        }
        );
    
        actionsScrollPane.setViewportView (actionsList);
    
      actionsPanel.add (actionsScrollPane);
  

    add (actionsPanel);

    sequencesPanel.setLayout (new java.awt.GridBagLayout ());
    java.awt.GridBagConstraints gridBagConstraints1;
    sequencesPanel.setBorder (new javax.swing.border.CompoundBorder( new javax.swing.border.TitledBorder( bundle.getString( "KBEP_Sequences" ) ), new javax.swing.border.EmptyBorder( new java.awt.Insets( 8, 8, 8, 8 ) ) ));

  
        sequencesList.setCellRenderer (new KeySequenceCellRenderer());
        sequencesList.addListSelectionListener (new javax.swing.event.ListSelectionListener () {
          public void valueChanged (javax.swing.event.ListSelectionEvent evt) {
            sequencesListValueChanged (evt);
          }
        }
        );
    
        sequencesScrollPane.setViewportView (sequencesList);
    
      gridBagConstraints1 = new java.awt.GridBagConstraints ();
      gridBagConstraints1.gridheight = 3;
      gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints1.insets = new java.awt.Insets (0, 0, 0, 8);
      gridBagConstraints1.weightx = 1.0;
      gridBagConstraints1.weighty = 1.0;
      sequencesPanel.add (sequencesScrollPane, gridBagConstraints1);
  
      addSequenceButton.setText (bundle.getString( "KBEP_Add" ));
      addSequenceButton.setEnabled (false);
      addSequenceButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          addSequenceButtonActionPerformed (evt);
        }
      }
      );
  
      gridBagConstraints1 = new java.awt.GridBagConstraints ();
      gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints1.insets = new java.awt.Insets (0, 0, 5, 0);
      sequencesPanel.add (addSequenceButton, gridBagConstraints1);
  
      removeSequenceButton.setText (bundle.getString( "KBEP_Remove" ));
      removeSequenceButton.setEnabled (false);
      removeSequenceButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          removeSequenceButtonActionPerformed (evt);
        }
      }
      );
  
      gridBagConstraints1 = new java.awt.GridBagConstraints ();
      gridBagConstraints1.gridx = 1;
      gridBagConstraints1.gridy = 1;
      gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
      sequencesPanel.add (removeSequenceButton, gridBagConstraints1);
  

    add (sequencesPanel);

  }//GEN-END:initComponents

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
    actionIndex = actionsList.getSelectedIndex();
    updateSequences( 0 );
  }//GEN-LAST:event_actionsListValueChanged


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel actionsPanel;
  private javax.swing.JScrollPane actionsScrollPane;
  private javax.swing.JList actionsList;
  private javax.swing.JPanel sequencesPanel;
  private javax.swing.JScrollPane sequencesScrollPane;
  private javax.swing.JList sequencesList;
  private javax.swing.JButton addSequenceButton;
  private javax.swing.JButton removeSequenceButton;
  // End of variables declaration//GEN-END:variables

  /**
   * Encapsulation for components of dialog asking for new KeySequence
   */
  private class KeySequenceRequester {
    
    KeySequenceInputPanel input;
    DialogDescriptor dd;
    Dialog dial;

    Object[] buttons = { new JButton( bundle.getString( "KBEP_OK_LABEL" ) ),
                         new JButton( bundle.getString( "KBEP_CLEAR_LABEL" ) ),
                         DialogDescriptor.CANCEL_OPTION };
    KeySequence retVal = null;


    KeySequenceRequester() {
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
      
      dd = new DialogDescriptor ( input, bundle.getString( "KBEP_AddSequence" ),
        true, buttons, buttons[0], DialogDescriptor.BOTTOM_ALIGN, null, new ActionListener(){
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
      dial = TopManager.getDefault().createDialog(dd);
      input.requestFocus();  // Place caret in it, hopefully
      dial.show(); // let the user tell us their wish, result will be stored in retVal
      return retVal;
    }
    
    String getCollisionString( KeyStroke[] seq ) {
      if( seq.length == 0 ) return ""; // NOI18N   not valid sequence, but don't alert user

      for( int i=0; i<acts.length; i++ ) { // for all actions
        Iterator iter = acts[i].sequences.iterator();
        while( iter.hasNext() ) {
          KeyStroke[] s1 = ((KeySequence)iter.next()).getKeyStrokes();
          if( isOverlapingSequence( s1, seq ) ) {
            Object[] values = { KeySequenceInputPanel.keySequenceToString( s1 ), acts[i] };
            return MessageFormat.format( bundle.getString( "KBEP_FMT_Collision" ), values );
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
    String name;
    String displayName;
    Vector sequences;
    
    ActionDescriptor( Action a ) {
      name = (String)a.getValue( Action.NAME );
      displayName = a.getValue( Action.SHORT_DESCRIPTION ) + " [" + name + "]"; // NOI18N
      sequences = new Vector();
    }
    
    public String toString() {
      return displayName;
    }
    
    // Naturaly ordered by its name
    public int compareTo( Object o ) {
      return name.compareTo( ((ActionDescriptor)o).name );
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
        return KeySequenceInputPanel.keySequenceToString( sequence );
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

/*
 * Log
 *  5    Gandalf-post-FCS1.4         3/17/00  Petr Nejedly    Rolled back to compile 
 *       under post-FCS
 *  4    Gandalf-post-FCS1.3         3/16/00  Miloslav Metelka renamings
 *  3    Gandalf-post-FCS1.2         3/15/00  Miloslav Metelka reverted previous 
 *       version - ST error?
 *  2    Gandalf-post-FCS1.1         3/15/00  Miloslav Metelka 
 *  1    Gandalf-post-FCS1.0         2/28/00  Petr Nejedly    initial revision
 * $
 */
 
