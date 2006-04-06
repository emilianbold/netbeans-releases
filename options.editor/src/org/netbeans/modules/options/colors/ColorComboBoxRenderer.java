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

package org.netbeans.modules.options.colors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;


/**
 * Renderer and editor for color JComboBox.
 *
 * @author Jan Jancura
 */
class ColorComboBoxRenderer extends JComponent implements 
ListCellRenderer, ComboBoxEditor {

    private int             SIZE = 9;
    private ColorValue      value;
    private JComboBox       comboBox;

    ColorComboBoxRenderer (JComboBox comboBox) {
        this.comboBox = comboBox;
        setPreferredSize (new Dimension (
            50, 
            comboBox.getFontMetrics (comboBox.getFont ()).
                getHeight () + 2
        ));
        setOpaque (true);
        setFocusable (true);
    }

    public void paint (Graphics g) {
        Color oldColor = g.getColor ();
        Dimension size = getSize ();
        if (isFocusOwner ())
            g.setColor (SystemColor.textHighlight);
        else
            g.setColor (getBackground ());
        g.fillRect (0, 0, size.width, size.height);
        int i = (size.height - SIZE) / 2;
        if (value.color != null) {
            g.setColor (Color.black);
            g.drawRect (i, i, SIZE, SIZE);
            g.setColor (value.color);
            g.fillRect (i + 1, i + 1, SIZE - 1, SIZE - 1);
        }
        if (value.text != null) {
            if (isFocusOwner ())
                g.setColor (SystemColor.textHighlightText);
            else
                g.setColor (getForeground ());
            if (value.color != null)
                g.drawString (value.text, i + SIZE + 5, i + SIZE);
            else
                g.drawString (value.text, 5, i + SIZE);
        }
        g.setColor (oldColor);
    }

    public void setEnabled (boolean enabled) {
        setBackground (enabled ? 
            SystemColor.text : SystemColor.control
        );
        super.setEnabled (enabled);
    }

    public Component getListCellRendererComponent (
        JList       list,
        Object      value,
        int         index,
        boolean     isSelected,
        boolean     cellHasFocus
    ) {
        this.value = (ColorValue) value;
        setEnabled (list.isEnabled ());
        setBackground (isSelected ? 
            SystemColor.textHighlight : SystemColor.text
        );
        setForeground (isSelected ? 
            SystemColor.textHighlightText : SystemColor.textText
        );
        return this;
    }

    public Component getEditorComponent () {
        setEnabled (comboBox.isEnabled ());
        setBackground (comboBox.isFocusOwner () ? 
            SystemColor.textHighlight : SystemColor.text
        );
        setForeground (comboBox.isFocusOwner () ? 
            SystemColor.textHighlightText : SystemColor.textText
        );
        return this;
    }

    public void setItem (Object anObject) {
        Object oldValue = this.value;
        this.value = (ColorValue) anObject;
        firePropertyChange(ColorComboBox.PROP_COLOR, oldValue, anObject);
    }

    public Object getItem () {
        return value;
    }
    
    public void selectAll() {}
    public void addActionListener (ActionListener l) {}
    public void removeActionListener (ActionListener l) {}   
}
