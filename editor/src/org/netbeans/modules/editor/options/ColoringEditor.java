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
 * Coloring Editor for editor settings. Operates over one ColoringBean
 *
 * @author Miloslav Metelka
 * @author Petr Nejedly
 */
public class ColoringEditor extends PropertyEditorSupport {

  /** Editor for font and color components. */
  private ColoringEditorPanel editor;

  /** Construct new instance */
  public ColoringEditor() {
  }

  /** Get value as text is not supported */
  public String getAsText() {
    return null;
  }

  /** Set value as text is not supported */
  public void setAsText(String text) {
    throw new IllegalArgumentException();
  }

    /** Set the new value into property editor */
  public void setValue(Object value) {
    super.setValue( value );
    if (editor != null) {
      editor.setValue( (ColoringBean)getValue() );
    }
  }

  /** It supports custom editor */
  public boolean supportsCustomEditor() {
    return true;
  }

  /** Get custom editor */
  public Component getCustomEditor() {
    if (editor == null) {

      // If we don't have any, create one
      editor = new ColoringEditorPanel();

      // fill it with our current value
      editor.setValue( (ColoringBean)getValue() );

      // register listener, which will propagate editor changes to our interval value with firing
      editor.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getPropertyName() == "value") // NOI18N
            superSetValue( editor.getValue()); // skip updating editor
        }
      });
    }
    
    return editor;
  }
  

  /** when we don't need to update editor, use this */
  void superSetValue( Object value ) {
    super.setValue( value );
  }

  /** This editor is paintable */
  public boolean isPaintable() {
    return true;
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
      String text = ((ColoringBean)getValue()).example;
      FontMetrics fm = g.getFontMetrics();
      int x = Math.max((box.width - fm.stringWidth(text)) / 2, 0);
      int y = Math.max((box.height - fm.getHeight()) / 2 + fm.getAscent(), 0);
      g.drawString(text, x, y);
    }
  }
  
  private Coloring getAppliedColoring() {
    ColoringBean value = ((ColoringBean)getValue());
    if( value == null ) return null;
    Coloring dc = value.defaultColoring;
    Coloring c = value.coloring;
    Coloring ret = null;
    if (dc != null && c != null) {
      ret = c.apply(dc);
    }
    return ret;
  }
      
}

/*
 * Log
 *  12   Gandalf   1.11        1/13/00  Miloslav Metelka Localization
 *  11   Gandalf   1.10        1/11/00  Petr Nejedly    ScrollPane, distribution
 *       of changes
 *  10   Gandalf   1.9         12/28/99 Miloslav Metelka 
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
