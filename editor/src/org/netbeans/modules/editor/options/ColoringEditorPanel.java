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

import java.awt.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;

import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.explorer.propertysheet.PropertyModel;
import com.netbeans.editor.Coloring;
import com.netbeans.editor.Settings;
import com.netbeans.editor.DefaultSettings;
import com.netbeans.developer.editors.FontEditor;
import com.netbeans.developer.editors.ColorEditor;

/** 
 * ColoringEditorPanel is custom property editor operating
 * over ColoringBean, it is interfaced only through ColoringEditor
 * @author  Petr Nejedly
 * 
 * TODO: remove updateUIs as Hans will repair PropertyPanel
 */
public class ColoringEditorPanel extends javax.swing.JPanel {

  /** Access to our localized texts */
  static java.util.ResourceBundle bundle = 
    org.openide.util.NbBundle.getBundle( ColoringEditorPanel.class );

  /** support for distributing change events. */                                    
  private PropertyChangeSupport support;   
  private boolean firing = false;
  
  /** the value we're operating over. */
  private ColoringBean value;
  
  /** PropertyModels for Coloring's properties */
  private PropertyModel fontEditor;
  private PropertyModel fgColorEditor;
  private PropertyModel bgColorEditor;
  
  /** PropertyPanels for visual editing of Coloring's properties.
   * We need'em for enabling/disabling  
   */
  private PropertyPanel fontEditPanel;
  private PropertyPanel fgEditPanel;
  private PropertyPanel bgEditPanel;
  
  /** Component for preview actual coloring composed of value and defaultColoring
   */
  private ColoringPreview preview;
  
  /** Creates new form ColoringEditorPanel */
  public ColoringEditorPanel() {
    support = new PropertyChangeSupport(this); 

    value = new ColoringBean(DefaultSettings.defaultColoring, "null",
        DefaultSettings.defaultColoring, true );
    
    initComponents ();

    preview = new ColoringPreview();
    previewPanel.add( preview, BorderLayout.CENTER );
    
    fontEditor = new PropertyModelSupport( Font.class, FontEditor.class );
    fontEditor.addPropertyChangeListener( new PropertyChangeListener() {
      public void propertyChange( PropertyChangeEvent evt ) {
        Font newFont = null;
        Font actFont = value.coloring.getFont();

        // ShortHand if editor is only showing inherited value, ignore changes
        if( actFont == null ) return;

        // Request the value currenly displayed
        try {
          newFont = (Font)fontEditor.getValue();
        } catch( InvocationTargetException e ) {
          if( Boolean.getBoolean( "com.netbeans.exceptions" ) ) e.printStackTrace();  
        }

        if( newFont == null ) return;
        
        // If displayed value is different, user have changed it, not setValue
        if( ! newFont.equals( actFont ) ) {
//          System.err.println( "Fonts aren't equal, newFont = " + newFont + ", oldFont = " + oldFont );
//          System.err.println( "newFont.class = " + newFont.getClass() + ", oldFont.class = " + oldFont.getClass() );
          setValueImpl( Coloring.changeFont( value.coloring, newFont ) );
        }
      }
    });
    fontEditPanel = new PropertyPanel( fontEditor, 0 );
    fontPanel.add( fontEditPanel, java.awt.BorderLayout.CENTER );
    
    fgColorEditor = new PropertyModelSupport( Color.class, ColorEditor.class );
    fgColorEditor.addPropertyChangeListener( new PropertyChangeListener() {
      public void propertyChange( PropertyChangeEvent evt ) {
        Color newColor = null;        
        Color foreColor = value.coloring.getForeColor();

        // ShortHand if editor is only showing inherited value
        if( foreColor == null ) return;

        // Request the value currenly displayed
        try {
          newColor = (Color)fgColorEditor.getValue();
        } catch( InvocationTargetException e ) {
          if( Boolean.getBoolean( "com.netbeans.exceptions" ) ) e.printStackTrace();  
        }

        if( newColor == null ) return;  // Yep; editor is able to send us null color
        
        // If displayed value is different, user have changed it, not setValue
        if( ! newColor.equals( foreColor ) )
          setValueImpl( Coloring.changeForeColor( value.coloring, newColor ) );
      }
    });
    fgEditPanel = new PropertyPanel( fgColorEditor, 0 );
    fgColorPanel.add( fgEditPanel, java.awt.BorderLayout.CENTER );
    
    bgColorEditor = new PropertyModelSupport( Color.class, ColorEditor.class );
    bgColorEditor.addPropertyChangeListener( new PropertyChangeListener() {
      public void propertyChange( PropertyChangeEvent evt ) {
        Color newColor = null;
        Color backColor = value.coloring.getBackColor();

        // ShortHand if editor is only showing inherited value
        if( backColor == null ) return;

        // Request the value currenly displayed
        try {
          newColor = (Color)bgColorEditor.getValue();
        } catch( InvocationTargetException e ) {
          if( Boolean.getBoolean( "com.netbeans.exceptions" ) ) e.printStackTrace();  
        }

        if( newColor == null ) return; // Yep; editor is able to send us null color
        
        // If displayed value is different, user have changed it, not setValue
        if( ! newColor.equals( backColor ) ) {
          setValueImpl( Coloring.changeBackColor( value.coloring, newColor ) );
        }
      }
    });
    bgEditPanel = new PropertyPanel( bgColorEditor, 0 );
    bgColorPanel.add( bgEditPanel, java.awt.BorderLayout.CENTER );  

    updateEditors();
  }
  
  /**
   *  Used by underlaying ColoringEditor to query actual Coloring
   */
  public ColoringBean getValue() {
    return value;
  }

  /**
   * Used to adjust current coloring from underlaying
   * ColoringEditor - initial setup of displayed / edited value
   */
  public void setValue( ColoringBean value ) {
    if( (value == null) || (value.coloring == null) ) {
//      System.err.println("null value rejected" );
      return;
    }
    if( this.value != value ) {
      this.value = value;
      updateEditors();
updateUI();
      preview.setValue( value );

      if( !firing ) {
        firing = true;
        support.firePropertyChange( "value", null, null );
        firing = false;
      } else {
//        System.err.println( "!!!! Already firing: ColoringEditorPanel[163] !!!!" );
      }
    }
  }

    
  private void setValueImpl( Coloring newColoring ) {   

    value = value.changeColoring( newColoring );

    preview.setValue( value );
updateUI();

    if( !firing ) {
      firing = true;
      support.firePropertyChange( "value", null, null );
      firing = false;
    } else {
//      System.err.println( "!!!!! Already firing: ColoringEditorPanel[205] !!!!!" );
    }
  }

  
  private void updateEditors() {
    if( value == null ) {
      return;
    }
    
    boolean fontInherit = value.coloring.getFont() == null;
    boolean fgInherit = value.coloring.getForeColor() == null;
    boolean bgInherit = value.coloring.getBackColor() == null;

    try {
      fontEditor.setValue( (fontInherit ? value.defaultColoring : value.coloring).getFont() );
      fgColorEditor.setValue( (fgInherit ? value.defaultColoring : value.coloring).getForeColor() );            
      bgColorEditor.setValue( (bgInherit ? value.defaultColoring : value.coloring).getBackColor() );
    } catch( InvocationTargetException e ) {
      if( Boolean.getBoolean( "com.netbeans.exceptions" ) ) e.printStackTrace();  
    }
    

    if( value.isDefault ) {
      // default coloring can't inherit values from itself
      fontCheckBox.setSelected( false );
      fontCheckBox.setEnabled( false ); // don't inherit
      fontEditPanel.setPreferences( 0 );  // is editable

      fgCheckBox.setSelected( false );
      fgCheckBox.setEnabled( false );
      fgEditPanel.setPreferences( 0 );

      bgCheckBox.setSelected( false );
      bgCheckBox.setEnabled( false );      
      bgEditPanel.setPreferences( 0 );

    } else {
      // non-default coloring - can inherit, some fields set to inherited
      fontCheckBox.setSelected( fontInherit );
      fontCheckBox.setEnabled( true );
      fontEditPanel.setPreferences( fontInherit ? PropertyPanel.PREF_READ_ONLY : 0 );
      
      fgCheckBox.setSelected( fgInherit );
      fgCheckBox.setEnabled( true );
      fgEditPanel.setPreferences( fgInherit ? PropertyPanel.PREF_READ_ONLY : 0 );

      bgCheckBox.setSelected( bgInherit );
      bgCheckBox.setEnabled( true );      
      bgEditPanel.setPreferences( bgInherit ? PropertyPanel.PREF_READ_ONLY : 0 );
    }      
    
  }
  

  /** Adds listener to change of the value.                                     
   */                                                                           
  public void addPropertyChangeListener(PropertyChangeListener l) {             
    support.addPropertyChangeListener(l);                                       
  }                                                                             
                                                                                
  /** Removes listener to change of the value.                                  
   */                                                                           
  public void removePropertyChangeListener(PropertyChangeListener l) {          
    support.removePropertyChangeListener(l);                                    
  } 

  /** To make us looking bigger */
  public Dimension getPreferredSize() {
    Dimension small = super.getPreferredSize();
//    small.width *= 1.2;
    small.height *= 1.2;
    return small;
  }
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    fontPanel = new javax.swing.JPanel ();
    fontCheckBox = new javax.swing.JCheckBox ();
    fgColorPanel = new javax.swing.JPanel ();
    fgCheckBox = new javax.swing.JCheckBox ();
    bgColorPanel = new javax.swing.JPanel ();
    bgCheckBox = new javax.swing.JCheckBox ();
    previewPanel = new javax.swing.JPanel ();
    setLayout (new java.awt.GridBagLayout ());
    java.awt.GridBagConstraints gridBagConstraints1;

    fontPanel.setLayout (new java.awt.BorderLayout ());
    fontPanel.setBorder (new javax.swing.border.CompoundBorder( new javax.swing.border.TitledBorder(bundle.getString( "CEP_FontTitle" )), new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8))));

      fontCheckBox.setText (bundle.getString("CEP_FontTrans"));
      fontCheckBox.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          fontCheckBoxActionPerformed (evt);
        }
      }
      );
  
      fontPanel.add (fontCheckBox, java.awt.BorderLayout.SOUTH);
  

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints1.insets = new java.awt.Insets (8, 8, 0, 8);
    gridBagConstraints1.weightx = 1.0;
    gridBagConstraints1.weighty = 0.3;
    add (fontPanel, gridBagConstraints1);

    fgColorPanel.setLayout (new java.awt.BorderLayout ());
    fgColorPanel.setBorder (new javax.swing.border.CompoundBorder( new javax.swing.border.TitledBorder(bundle.getString( "CEP_FgTitle" )), new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8))));

      fgCheckBox.setText (bundle.getString("CEP_FgTrans"));
      fgCheckBox.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          fgCheckBoxActionPerformed (evt);
        }
      }
      );
  
      fgColorPanel.add (fgCheckBox, java.awt.BorderLayout.SOUTH);
  

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridx = 0;
    gridBagConstraints1.gridy = 1;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.insets = new java.awt.Insets (0, 8, 0, 8);
    gridBagConstraints1.weightx = 1.0;
    add (fgColorPanel, gridBagConstraints1);

    bgColorPanel.setLayout (new java.awt.BorderLayout ());
    bgColorPanel.setBorder (new javax.swing.border.CompoundBorder( new javax.swing.border.TitledBorder(bundle.getString( "CEP_BgTitle" )), new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8))));

      bgCheckBox.setText (bundle.getString("CEP_BgTrans"));
      bgCheckBox.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          bgCheckBoxActionPerformed (evt);
        }
      }
      );
  
      bgColorPanel.add (bgCheckBox, java.awt.BorderLayout.SOUTH);
  

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridx = 0;
    gridBagConstraints1.gridy = 2;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.insets = new java.awt.Insets (0, 8, 0, 8);
    gridBagConstraints1.weightx = 1.0;
    add (bgColorPanel, gridBagConstraints1);

    previewPanel.setLayout (new java.awt.BorderLayout ());
    previewPanel.setBorder (new javax.swing.border.CompoundBorder( new javax.swing.border.TitledBorder(bundle.getString( "CEP_PreviewTitle" )), new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8))));


    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridx = 0;
    gridBagConstraints1.gridy = 3;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints1.insets = new java.awt.Insets (0, 8, 8, 8);
    gridBagConstraints1.weightx = 1.0;
    gridBagConstraints1.weighty = 1.0;
    add (previewPanel, gridBagConstraints1);

  }//GEN-END:initComponents

private void bgCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgCheckBoxActionPerformed
    Color valueColor = null;
    Color editorColor = value.defaultColoring.getBackColor(); 

    if( !bgCheckBox.isSelected() ) {
      // unchecked - set real value 
      valueColor = editorColor;
    }

    value = value.changeColoring( Coloring.changeBackColor( value.coloring, valueColor ) ) ;
    
    try {
      bgColorEditor.setValue( editorColor );
    } catch( InvocationTargetException e ) {
      if( Boolean.getBoolean( "com.netbeans.exceptions" ) ) e.printStackTrace();  
    }
    
    // reset editable state
    bgEditPanel.setPreferences( bgCheckBox.isSelected() ? PropertyPanel.PREF_READ_ONLY : 0 );
    // update preview and fire change
    setValueImpl( value.coloring );
  }//GEN-LAST:event_bgCheckBoxActionPerformed

private void fgCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgCheckBoxActionPerformed
    Color valueColor = null;
    Color editorColor = value.defaultColoring.getForeColor(); 

    if( !fgCheckBox.isSelected() ) {
      // unchecked - set real value 
      valueColor = editorColor;
    }

    value = value.changeColoring( Coloring.changeForeColor( value.coloring, valueColor ) );
    
    try {
      fgColorEditor.setValue( editorColor );
    } catch( InvocationTargetException e ) {
      if( Boolean.getBoolean( "com.netbeans.exceptions" ) ) e.printStackTrace();  
    }
    
    // reset editable state
    fgEditPanel.setPreferences( fgCheckBox.isSelected() ? PropertyPanel.PREF_READ_ONLY : 0 );
    // update preview and fire change
    setValueImpl( value.coloring );
  }//GEN-LAST:event_fgCheckBoxActionPerformed

private void fontCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontCheckBoxActionPerformed
    Font valueFont = null;
    Font editorFont = value.defaultColoring.getFont(); 

    if( !fontCheckBox.isSelected() ) {
      // unchecked - set real value 
      valueFont = editorFont;
    }

    value = value.changeColoring( Coloring.changeFont( value.coloring, valueFont ) );
    
    try {
      fontEditor.setValue( editorFont );
    } catch( InvocationTargetException e ) {
      if( Boolean.getBoolean( "com.netbeans.exceptions" ) ) e.printStackTrace();  
    }
    
    // reset editable state
    fontEditPanel.setPreferences( fontCheckBox.isSelected() ? PropertyPanel.PREF_READ_ONLY : 0 );
    // update preview and fire change
    setValueImpl( value.coloring );
  }//GEN-LAST:event_fontCheckBoxActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel fontPanel;
  private javax.swing.JCheckBox fontCheckBox;
  private javax.swing.JPanel fgColorPanel;
  private javax.swing.JCheckBox fgCheckBox;
  private javax.swing.JPanel bgColorPanel;
  private javax.swing.JCheckBox bgCheckBox;
  private javax.swing.JPanel previewPanel;
  // End of variables declaration//GEN-END:variables


  
  
  private class PropertyModelSupport implements PropertyModel {
    
    /** support for the properties changes. */                                    
    private PropertyChangeSupport support;   

    Class type;
    Class editor;
    Object value;
    
    public PropertyModelSupport( Class propertyType, Class propertyEditor ) {
      support = new PropertyChangeSupport(this); 
      this.type = propertyType;
      this.editor = propertyEditor;
    }
                                                                                
    public Class getPropertyType() {
      return type;
    }
      
    public Class getPropertyEditorClass() {
      return editor;
    }

    public Object getValue() {
      return value;
    }                    
                                                                                
    public void setValue(Object v) {
      if( v != null && (!v.equals( value )) ) {
        value = v;
        support.firePropertyChange( PROP_VALUE, null, null );
      }
    }                                                                                   

    
    /** Adds listener to change of the value.                                     
     */                                                                           
    public void addPropertyChangeListener(PropertyChangeListener l) {             
      support.addPropertyChangeListener(l);                                       
    }                                                                             
                                                                                
    /** Removes listener to change of the value.                                  
     */                                                                           
    public void removePropertyChangeListener(PropertyChangeListener l) {          
      support.removePropertyChangeListener(l);                                    
    } 
  }
  
//----------------------------------------------------------------

  private class ColoringPreview extends javax.swing.JComponent {
    ColoringBean value;
    
    void setValue( ColoringBean c ) {
      value = c;
      repaint();
    }
    
    public void paint( java.awt.Graphics g ) {
      if (value != null) {
        Coloring coloring = value.coloring.apply( value.defaultColoring );
        
        java.awt.Rectangle box = getBounds();

        // clear background
        g.setColor(coloring.getBackColor());
        g.fillRect(0, 0 /*box.x, box.y*/, box.width - 1, box.height - 1);

        // draw example text
        g.setColor(coloring.getForeColor());
        g.setFont(coloring.getFont());
        FontMetrics fm = g.getFontMetrics();
        int x = Math.max((box.width - fm.stringWidth(value.example)) / 2, 0);
        int y = Math.max((box.height - fm.getHeight()) / 2 + fm.getAscent(), 0);
        g.drawString(value.example, x, y);
      }
    }
  }
  
}