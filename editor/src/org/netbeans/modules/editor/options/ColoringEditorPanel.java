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

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.netbeans.editor.Coloring;
import com.netbeans.editor.DefaultSettings;

/** 
 *
 * @author  mmetelka
 * @version 
 */
public class ColoringEditorPanel extends javax.swing.JPanel {
  
  private static final Coloring DEFAULT_COLORING
      = new Coloring(
          "default",
          DefaultSettings.defaultFont,
          DefaultSettings.defaultForeColor,
          DefaultSettings.defaultBackColor
      );

  private Coloring coloring;
  
  private Coloring defaultColoring = DEFAULT_COLORING;
  
  private boolean canFire;
  
  private Editor fontEditor;
  
  private Editor foreColorEditor;
  
  private Editor backColorEditor;
  
  /** Creates new form ColoringEditorPanel */
  public ColoringEditorPanel() {
    initComponents ();
    preview.setOpaque(true);
    fontPreview.setForeground(Color.black);
    fontPreview.setBackground(Color.white);
    canFire = true;
    addListeners();
  }
  
  public Coloring getColoring() {
    return coloring;
  }
  
  public void setColoring(Coloring coloring) {
    this.coloring = coloring;
    updatePanel();
  }
  
  public void setDefaultColoring(Coloring defaultColoring) {
    if (defaultColoring != null) { // must be non-null
      this.defaultColoring = defaultColoring;
      updatePanel();
    }
  }

  public void setExample(String example) {
    fontPreview.setText(example);
    preview.setText(example);
  }
  
  private void updatePanel() {
    canFire = false;

    Font f = coloring.getFont();
    boolean t = false;
    if (f == null) {
      t = true;
      f = defaultColoring.getFont();
    }
    fontPreview.setFont(f);
    fontTransparent.setSelected(t);
    fontChange.setEnabled(!t);
    
    Color c = coloring.getForeColor();
    t = false;
    if (c == null) {
      t = true;
      c = defaultColoring.getForeColor();
    }
    foreColorPreview.setBackground(c);
    foreColorTransparent.setSelected(t);
    foreColorChange.setEnabled(!t);
    
    c = coloring.getBackColor();
    t = false;
    if (c == null) {
      t = true;
      c = defaultColoring.getBackColor();
    }
    backColorPreview.setBackground(c);
    backColorTransparent.setSelected(t);
    backColorTransparent.setEnabled(!t);

    updatePreview();

    canFire = true;
  }

  private void updatePreview() {
    preview.setFont(fontPreview.getFont());
    preview.setBackground(backColorPreview.getBackground());
    preview.setForeground(foreColorPreview.getBackground());
  }
   
  private void updateColoring() {
    Coloring c = coloring;
    c = Coloring.changeFont(c, fontTransparent.isSelected()
        ? null : fontPreview.getFont());
    c = Coloring.changeForeColor(c, foreColorTransparent.isSelected()
        ? null : foreColorPreview.getBackground());
    c = Coloring.changeBackColor(c, backColorTransparent.isSelected()
        ? null : backColorPreview.getBackground());

    if (fontEditor != null) {
      fontEditor.setValue(fontPreview.getFont());
    }
    if (foreColorEditor != null) {
      foreColorEditor.setValue(foreColorPreview.getBackground());
    }
    if (backColorEditor != null) {
      backColorEditor.setValue(backColorPreview.getBackground());
    }

    if (!c.equals(coloring)) {
      Coloring old = coloring;
      coloring = c;
      updatePreview();
      firePropertyChange("value", old, coloring);
    }
  }

  private void addListeners() {
    fontPreview.addPropertyChangeListener(
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (canFire && "font".equals(evt.getPropertyName())) {
            updateColoring();
          }
        }
      }
    );
    fontTransparent.addItemListener(
      new ItemListener() {
        public void itemStateChanged(ItemEvent evt) {
          if (canFire) {
            boolean s = fontTransparent.isSelected();
            fontChange.setEnabled(!s);
            if (s) {
              fontPreview.setFont(defaultColoring.getFont());
            }
            updateColoring();
          }
        }
      }
    );
    
    foreColorPreview.addPropertyChangeListener(
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (canFire && "background".equals(evt.getPropertyName())) {
            updateColoring();
          }
        }
      }
    );
    foreColorTransparent.addItemListener(
      new ItemListener() {
        public void itemStateChanged(ItemEvent evt) {
          if (canFire) {
            boolean s = foreColorTransparent.isSelected();
            foreColorChange.setEnabled(!s);
            if (s) {
              foreColorPreview.setBackground(defaultColoring.getForeColor());
            }
            updateColoring();
          }
        }
      }
    );

    backColorPreview.addPropertyChangeListener(
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (canFire && "background".equals(evt.getPropertyName())) {
            updateColoring();
          }
        }
      }
    );
    backColorTransparent.addItemListener(
      new ItemListener() {
        public void itemStateChanged(ItemEvent evt) {
          if (canFire) {
            boolean s = backColorTransparent.isSelected();
            backColorChange.setEnabled(!s);
            if (s) {
              backColorPreview.setBackground(defaultColoring.getBackColor());
            }
            updateColoring();
          }
        }
      }
    );
  }
    
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
private void initComponents () {//GEN-BEGIN:initComponents
setLayout (new java.awt.GridBagLayout ());
java.awt.GridBagConstraints gridBagConstraints1;

fontPanel = new javax.swing.JPanel ();
fontPanel.setLayout (new java.awt.GridBagLayout ());
java.awt.GridBagConstraints gridBagConstraints2;
fontPanel.setBorder (new javax.swing.border.TitledBorder(
  new javax.swing.border.EtchedBorder(), "Font"));

  fontPreview = new javax.swing.JLabel ();
  fontPreview.setText ("Text");
  fontPreview.setHorizontalAlignment (0);
  
  gridBagConstraints2 = new java.awt.GridBagConstraints ();
  gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
  gridBagConstraints2.weightx = 1.0;
  fontPanel.add (fontPreview, gridBagConstraints2);
  
  fontChange = new javax.swing.JButton ();
  fontChange.setText ("Change ...");
  fontChange.addActionListener (new java.awt.event.ActionListener () {
  public void actionPerformed (java.awt.event.ActionEvent evt) {
  fontChangeActionPerformed (evt);
  }
  }
  );
  
  gridBagConstraints2 = new java.awt.GridBagConstraints ();
  gridBagConstraints2.gridwidth = 0;
  gridBagConstraints2.insets = new java.awt.Insets (5, 5, 5, 5);
  fontPanel.add (fontChange, gridBagConstraints2);
  
  fontTransparent = new javax.swing.JCheckBox ();
  fontTransparent.setText ("Transparent");
  
  gridBagConstraints2 = new java.awt.GridBagConstraints ();
  gridBagConstraints2.gridwidth = 0;
  gridBagConstraints2.insets = new java.awt.Insets (5, 5, 5, 5);
  fontPanel.add (fontTransparent, gridBagConstraints2);
  

gridBagConstraints1 = new java.awt.GridBagConstraints ();
gridBagConstraints1.gridwidth = 0;
gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
gridBagConstraints1.weightx = 1.0;
add (fontPanel, gridBagConstraints1);

foreColorPanel = new javax.swing.JPanel ();
foreColorPanel.setLayout (new java.awt.GridBagLayout ());
java.awt.GridBagConstraints gridBagConstraints3;
foreColorPanel.setBorder (new javax.swing.border.TitledBorder(
  new javax.swing.border.EtchedBorder(), "Foreground Color"));

  foreColorPreview = new javax.swing.JPanel ();
  foreColorPreview.setLayout (new java.awt.FlowLayout ());
  foreColorPreview.setPreferredSize (new java.awt.Dimension(50, 27));
  foreColorPreview.setBackground (java.awt.Color.red);
  
  gridBagConstraints3 = new java.awt.GridBagConstraints ();
  gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
  gridBagConstraints3.insets = new java.awt.Insets (5, 5, 5, 5);
  gridBagConstraints3.weightx = 1.0;
  foreColorPanel.add (foreColorPreview, gridBagConstraints3);
  
  foreColorChange = new javax.swing.JButton ();
  foreColorChange.setText ("Change ...");
  foreColorChange.addActionListener (new java.awt.event.ActionListener () {
  public void actionPerformed (java.awt.event.ActionEvent evt) {
  foreColorChangeActionPerformed (evt);
  }
  }
  );
  
  gridBagConstraints3 = new java.awt.GridBagConstraints ();
  gridBagConstraints3.gridwidth = 0;
  gridBagConstraints3.insets = new java.awt.Insets (5, 5, 5, 5);
  foreColorPanel.add (foreColorChange, gridBagConstraints3);
  
  foreColorTransparent = new javax.swing.JCheckBox ();
  foreColorTransparent.setText ("Transparent");
  
  gridBagConstraints3 = new java.awt.GridBagConstraints ();
  gridBagConstraints3.gridwidth = 0;
  gridBagConstraints3.insets = new java.awt.Insets (5, 5, 5, 5);
  foreColorPanel.add (foreColorTransparent, gridBagConstraints3);
  

gridBagConstraints1 = new java.awt.GridBagConstraints ();
gridBagConstraints1.gridwidth = 0;
gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
gridBagConstraints1.weightx = 1.0;
add (foreColorPanel, gridBagConstraints1);

backColorPanel = new javax.swing.JPanel ();
backColorPanel.setLayout (new java.awt.GridBagLayout ());
java.awt.GridBagConstraints gridBagConstraints4;
backColorPanel.setBorder (new javax.swing.border.TitledBorder(
  new javax.swing.border.EtchedBorder(), "Background Color"));

  backColorPreview = new javax.swing.JPanel ();
  backColorPreview.setLayout (new java.awt.FlowLayout ());
  backColorPreview.setPreferredSize (new java.awt.Dimension(50, 27));
  backColorPreview.setBackground (java.awt.Color.red);
  
  gridBagConstraints4 = new java.awt.GridBagConstraints ();
  gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
  gridBagConstraints4.insets = new java.awt.Insets (5, 5, 5, 5);
  gridBagConstraints4.weightx = 1.0;
  backColorPanel.add (backColorPreview, gridBagConstraints4);
  
  backColorChange = new javax.swing.JButton ();
  backColorChange.setText ("Change ...");
  backColorChange.addActionListener (new java.awt.event.ActionListener () {
  public void actionPerformed (java.awt.event.ActionEvent evt) {
  backColorChangeActionPerformed (evt);
  }
  }
  );
  
  gridBagConstraints4 = new java.awt.GridBagConstraints ();
  gridBagConstraints4.gridwidth = 0;
  gridBagConstraints4.insets = new java.awt.Insets (5, 5, 5, 5);
  backColorPanel.add (backColorChange, gridBagConstraints4);
  
  backColorTransparent = new javax.swing.JCheckBox ();
  backColorTransparent.setText ("Transparent");
  
  gridBagConstraints4 = new java.awt.GridBagConstraints ();
  gridBagConstraints4.gridwidth = 0;
  gridBagConstraints4.insets = new java.awt.Insets (5, 5, 5, 5);
  backColorPanel.add (backColorTransparent, gridBagConstraints4);
  

gridBagConstraints1 = new java.awt.GridBagConstraints ();
gridBagConstraints1.gridwidth = 0;
gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
gridBagConstraints1.weightx = 1.0;
add (backColorPanel, gridBagConstraints1);

previewPanel = new javax.swing.JPanel ();
previewPanel.setLayout (new java.awt.GridBagLayout ());
java.awt.GridBagConstraints gridBagConstraints5;
previewPanel.setBorder (new javax.swing.border.TitledBorder(
  new javax.swing.border.EtchedBorder(), "Preview"));

  preview = new javax.swing.JLabel ();
  preview.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
  preview.setHorizontalTextPosition (0);
  preview.setText ("Text");
  preview.setHorizontalAlignment (0);
  
  gridBagConstraints5 = new java.awt.GridBagConstraints ();
  gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
  gridBagConstraints5.insets = new java.awt.Insets (5, 5, 5, 5);
  gridBagConstraints5.weightx = 1.0;
  previewPanel.add (preview, gridBagConstraints5);
  

gridBagConstraints1 = new java.awt.GridBagConstraints ();
gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
gridBagConstraints1.weightx = 1.0;
add (previewPanel, gridBagConstraints1);

}//GEN-END:initComponents

  private void backColorChangeActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backColorChangeActionPerformed
    // Add your handling code here:
    if (backColorEditor == null) {
      backColorEditor = new Editor(Color.class);
      backColorEditor.addListener(
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt2) {
            backColorPreview.setBackground((Color)backColorEditor.getValue());
          }
        }
      );
      Point p = new Point(getX() + getWidth(), backColorPanel.getY());
      SwingUtilities.convertPointToScreen(p, this);
      backColorEditor.setLocation(p);
    }
    backColorEditor.setValue(backColorPreview.getBackground());
    backColorEditor.setVisible(true);
    backColorEditor.requestFocus();
    
  }//GEN-LAST:event_backColorChangeActionPerformed

  private void foreColorChangeActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foreColorChangeActionPerformed
    // Add your handling code here:
    if (foreColorEditor == null) {
      foreColorEditor = new Editor(Color.class);
      foreColorEditor.addListener(
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt2) {
            foreColorPreview.setBackground((Color)foreColorEditor.getValue());
          }
        }
      );
      Point p = new Point(getX() + getWidth(), foreColorPanel.getY());
      SwingUtilities.convertPointToScreen(p, this);
      foreColorEditor.setLocation(p);
    }
    foreColorEditor.setValue(foreColorPreview.getBackground());
    foreColorEditor.setVisible(true);
    foreColorEditor.requestFocus();
    
  }//GEN-LAST:event_foreColorChangeActionPerformed

  private void fontChangeActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontChangeActionPerformed
    // Add your handling code here:
    if (fontEditor == null) {
      fontEditor = new Editor(Font.class);
      fontEditor.addListener(
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt2) {
            fontPreview.setFont((Font)fontEditor.getValue());
          }
        }
      );
      Point p = new Point(getX() + getWidth(), fontPanel.getY());
      SwingUtilities.convertPointToScreen(p, this);
      fontEditor.setLocation(p);
    }
    fontEditor.setValue(fontPreview.getFont());
    fontEditor.setVisible(true);
    fontEditor.requestFocus();
  
  }//GEN-LAST:event_fontChangeActionPerformed


// Variables declaration - do not modify//GEN-BEGIN:variables
private javax.swing.JPanel fontPanel;
private javax.swing.JLabel fontPreview;
private javax.swing.JButton fontChange;
private javax.swing.JCheckBox fontTransparent;
private javax.swing.JPanel foreColorPanel;
private javax.swing.JPanel foreColorPreview;
private javax.swing.JButton foreColorChange;
private javax.swing.JCheckBox foreColorTransparent;
private javax.swing.JPanel backColorPanel;
private javax.swing.JPanel backColorPreview;
private javax.swing.JButton backColorChange;
private javax.swing.JCheckBox backColorTransparent;
private javax.swing.JPanel previewPanel;
private javax.swing.JLabel preview;
// End of variables declaration//GEN-END:variables
  
  class Editor extends JDialog implements ActionListener {
    
    PropertyEditor editor;
    
    Object oldValue;
    
    JButton okButton;
    
    JButton cancelButton;
    
    Editor(Class c) {
      editor = PropertyEditorManager.findEditor(c);
      getContentPane().add(editor.getCustomEditor());
      okButton = new JButton("OK");
      cancelButton = new JButton("Cancel");
      JPanel bp = new JPanel();
      bp.add(okButton);
      bp.add(cancelButton);
      getContentPane().add(bp, BorderLayout.SOUTH);
      okButton.addActionListener(this);
      cancelButton.addActionListener(this);
      pack();
    }
    
    public void addListener(PropertyChangeListener l) {
      editor.addPropertyChangeListener(l);
    }
    
    public void setValue(Object value) {
      oldValue = value;
      editor.setValue(value);
    }
    
    public Object getValue() {
      return editor.getValue();
    }
    
    public void actionPerformed(ActionEvent evt) {
      if (evt.getSource() == okButton) {
        this.setVisible(false);
      } else if (evt.getSource() == cancelButton) {
        this.setVisible(false);
        editor.setValue(oldValue);
      }
    }
    
  }
  
}

/*
 * Log
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
