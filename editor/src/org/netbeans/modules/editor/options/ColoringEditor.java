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

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import com.netbeans.editor.Coloring;
import org.openide.util.HelpCtx;

/**
* Settings for editor
*
* @author Miloslav Metelka
*/
public class ColoringEditor extends PropertyEditorSupport {

  /** The value */
  private ColoringBean value;

  /** Editor for font and color component. */
  private ColoringEditorPanel editor;

  /** Construct new instance */
  public ColoringEditor() {
  }

  /** This editor is paintable */
  public boolean isPaintable() {
    return true;
  }

  public ColoringEditorPanel getEditor() {
    if (editor == null) {
      editor = new ColoringEditorPanel();
      refreshEditor();
      editor.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == "value") {
              ColoringBean newValue = new ColoringBean();
              newValue.setColoring(editor.getColoring());
              newValue.defaultColoring = value.defaultColoring;
              newValue.example = value.example;
              setValue(newValue);
              firePropertyChange();
            }
          }
        }
      );
    }
    return editor;
  }
  
  private Coloring getAppliedColoring() {
    Coloring dc = value.defaultColoring;
    Coloring c = value.getColoring();
    Coloring ret = null;
    if (dc != null && c != null) {
      ret = c.apply(dc);
    }
    return ret;
  }

  /** Paint the current value */
  public void paintValue(Graphics g, Rectangle box) {
    Coloring c = getAppliedColoring();
    if (c != null) {
      // clear background
      g.setColor(c.getBackColor());
      g.fillRect(box.x, box.y, box.width - 1, box.height - 1);

      // draw example text
      g.setColor(c.getForeColor());
      g.setFont(c.getFont());
      String text = value.example;
      FontMetrics fm = g.getFontMetrics();
      int x = Math.max((box.width - fm.stringWidth(text)) / 2, 0);
      int y = Math.max((box.height - fm.getHeight()) / 2 + fm.getAscent(), 0);
      g.drawString(text, x, y);
    }
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

  private void refreshEditor() {
    editor.setColoring(value.getColoring());
    editor.setDefaultColoring(value.defaultColoring);
    editor.setExample(value.example);
  }
  
  /** Set the new value into property editor */
  public void setValue(Object value) {
    this.value = (ColoringBean) value;
    if (editor != null) {
      refreshEditor();
    }
  }

  /** Get the current value */
  public Object getValue() {
    return value;
  }

}

/*
 * Log
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         8/17/99  Miloslav Metelka 
 *  7    Gandalf   1.6         7/26/99  Miloslav Metelka 
 *  6    Gandalf   1.5         7/21/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/20/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/9/99   Ales Novak      NullPointerException
 *  3    Gandalf   1.2         7/8/99   Jesse Glick     Context help.
 *  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package 
 *       statement to make it compilable
 *  1    Gandalf   1.0         6/30/99  Ales Novak      
 * $
 */
