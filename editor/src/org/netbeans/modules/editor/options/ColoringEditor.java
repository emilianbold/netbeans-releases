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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JColorChooser;

import com.netbeans.developer.editors.ColorEditor;
import com.netbeans.editor.Coloring;

/**
* Settings for editor
*
* @author Miloslav Metelka
*/
public class ColoringEditor extends PropertyEditorSupport {

  /** The value */
  private ColoringBean value;

  /** Editor for font and color component. */
  private ColoringComponent editor;

  /** Construct new instance */
  public ColoringEditor() {
  }

  /** This editor is paintable */
  public boolean isPaintable() {
    return true;
  }

  public ColoringComponent getEditor() {
    if (editor == null) {
      editor = new ColoringComponent();
      editor.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == "value") {
              firePropertyChange();
            }
          }
        }
      );
    }

    return editor;
  }

  /** Paint the current value */
  public void paintValue(Graphics gfx, Rectangle box) {
    Color oldColor = gfx.getColor ();
    Font oldFont = gfx.getFont ();

    gfx.setColor(value.getBackColor());
    gfx.fillRect(box.x, box.y, box.width - 1, box.height - 1);

//    gfx.setColor(Color.black);
//    gfx.drawRect(box.x, box.y, box.width - 1, box.height - 1);

    gfx.setColor(value.getForeColor());
    Font f = value.getFont();
    gfx.setFont(f);

    String text = value.example;
    FontMetrics fm = gfx.getFontMetrics ();

    Dimension charDim = new Dimension (fm.charWidth ('W'),
      (box.height - fm.getHeight()) / 2 + fm.getAscent());

    gfx.drawString(text, Math.max(0, (box.width - text.length()*charDim.width)/2),
      charDim.height);
    gfx.setColor (oldColor);
    gfx.setFont (oldFont);
  }

  /** Get value as text is not supported */
  public String getAsText() {
    return null;
  }

  /** Set value as text is not supported */
  public void setAsText(String text) {
    throw new IllegalArgumentException();
  }

  /** It supports custom editor */
  public boolean supportsCustomEditor() {
    return true;
  }

  /** Get custom editor */
  public Component getCustomEditor() {
    return getEditor();
  }

  /** Set the new value into property editor */
  public void setValue(Object value) {
    this.value = (ColoringBean) value;
  }

  /** Get the current value */
  public Object getValue() {
    return value;
  }


  /** Component used for property editor */
  private class ColoringComponent extends JComponent {

    /** Property editor that is used for editing of color */
    private PropertyEditor colorEditor;
    /** Checkbox for bold font */
    private JCheckBox boldCheck = new JCheckBox(PlainOptions.getString("FontBold"));
    /** Checkbox for italic font */
    private JCheckBox italicCheck = new JCheckBox(PlainOptions.getString("FontItalic"));
    /** Radio button for foreground color editing */
    private JRadioButton foreRButton = new JRadioButton(PlainOptions.getString("Foreground"), true);
    /** Radio button for background color editing */
    private JRadioButton backRButton = new JRadioButton(PlainOptions.getString("Background"), false);
    /** Property change listener used for color editor changes */
    private ChangeListener colorListener;
    /** Listener used for font changes */
    private ChangeListener fontChangeL;
    /** used to set color when foreground/background is switched */
    private JColorChooser chooser;

    /** Construct new component */
    ColoringComponent() {
      setLayout(new BorderLayout());
      JPanel pEast = new JPanel(new FlowLayout());
      JPanel pRadio = new JPanel(new BorderLayout());
      pRadio.setBorder(BorderFactory.createTitledBorder(PlainOptions.getString("ChangeColorFor")));
      pRadio.add(foreRButton, BorderLayout.NORTH);
      pRadio.add(backRButton, BorderLayout.SOUTH);
      ButtonGroup bgrp = new ButtonGroup();
      bgrp.add(foreRButton);
      bgrp.add(backRButton);
      pEast.add(pRadio);

      chooser = ColorEditor.getStaticChooser ();
      foreRButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent evt) {
            setEditorColor();
            // update color chooser from Swing
            Color c;
            if(foreRButton.isSelected()) {
              c = value.getForeColor();
            } else {
              c = value.getBackColor();
            }
            chooser.setColor(c);
          }
        }
      );

      JPanel pCheck = new JPanel(new BorderLayout());
      pCheck.setBorder(BorderFactory.createTitledBorder(PlainOptions.getString("FontType")));
      pCheck.add(boldCheck, BorderLayout.NORTH);
      pCheck.add(italicCheck, BorderLayout.CENTER);
      pEast.add(pCheck);

      fontChangeL = new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
          updateFont();
        }
      };
      boldCheck.addChangeListener(fontChangeL);
      italicCheck.addChangeListener(fontChangeL);

      colorListener = new ChangeListener() {
          public void stateChanged(ChangeEvent evt) {
            if(foreRButton.isSelected()) {
              value.setForeColor(chooser.getColor());
            } else {
              value.setBackColor(chooser.getColor());
            }
            firePropertyChange("value", null, null);
          }
      };
      chooser.getSelectionModel().addChangeListener(colorListener);

      add(new ChooserPanel (chooser), BorderLayout.CENTER);

      updateValue();
      add(pEast, BorderLayout.EAST);
    }

    class ChooserPanel extends JPanel {
      private JColorChooser chooser;

      public ChooserPanel (JColorChooser chooser) {
        setLayout (new BorderLayout ());
        add (chooser, BorderLayout.CENTER);
        this.chooser = chooser;
      }

      public void removeNotify () {
        chooser.getSelectionModel().removeChangeListener(colorListener);
      }

      public Dimension getPreferredSize () {
        Dimension s = super.getPreferredSize ();
        return new Dimension (s.width + 50, s.height + 10);
      }

    }

    private void setEditorColor() {
      chooser.getSelectionModel().removeChangeListener(colorListener);
      if(foreRButton.isSelected()) {
        chooser.setColor(value.getForeColor());
      } else {
        chooser.setColor(value.getBackColor());
      }
      chooser.getSelectionModel().addChangeListener(colorListener);
    }

    private void updateFont() {
      int fdcFont = (boldCheck.isSelected() ? 1 : 0)
        + (italicCheck.isSelected() ? 2 : 0);
      setFontFromInt(fdcFont);
      firePropertyChange("value", null, null);
    }

    private void updateValue() {
      if(value != null) {
        setEditorColor();

        boldCheck.removeChangeListener(fontChangeL);
        italicCheck.removeChangeListener(fontChangeL);

        int fdcFont = getFontFromValue();
        boldCheck.setSelected((fdcFont & 1) > 0);
        italicCheck.setSelected((fdcFont & 2) > 0);

        boldCheck.addChangeListener(fontChangeL);
        italicCheck.addChangeListener(fontChangeL);
      }
    }

    /** Redefined fire property change because of use from listener */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
      super.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  // this editor is old prop editor from tuborg
  // following constants/methods bridges differencies

  static final int FONT_PLAIN = 0;
  static final int FONT_BOLD = 1;
  static final int FONT_ITALIC = 2;
  static final int FONT_BOLD_ITALIC = 3;

  int getFontFromValue() {
    Font xfont = value.getFont();
    int ret = FONT_PLAIN;
    if (xfont.isBold()) {
      ret = FONT_BOLD;
    }
    if (xfont.isItalic()) {
      ret +=  FONT_ITALIC;
    }
    return ret;
  }

  void setFontFromInt(int i) {
    Font xfont = value.getFont();
    int mask = 0;
    
    if (i == FONT_PLAIN) {
      mask |= Font.PLAIN;
    }
    if (i == FONT_BOLD) {
      mask |= Font.BOLD;
    }
    if (i == FONT_ITALIC) {
      mask |= Font.ITALIC;
    }
    xfont = xfont.deriveFont(mask);
    value.setFont(xfont);
  }
}

/*
 * Log
 *  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package 
 *       statement to make it compilable
 *  1    Gandalf   1.0         6/30/99  Ales Novak      
 * $
 */
