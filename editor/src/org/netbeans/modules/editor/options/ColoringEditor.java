/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;

import org.netbeans.editor.Coloring;

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
