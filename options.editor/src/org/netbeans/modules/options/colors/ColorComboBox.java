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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.util.NbBundle;


/**
 *
 * @author Administrator
 */
public class ColorComboBox extends JComboBox {
    
    public static final String PROP_COLOR = "color";
    public static final Value  CUSTOM_COLOR = 
            new Value (loc ("Custom"), null); //NOI18N
    
    private static Map colorMap = new HashMap ();
    static {
        colorMap.put (Color.BLACK,      loc ("Black"));         //NOI18N
        colorMap.put (Color.BLUE,       loc ("Blue"));          //NOI18N
        colorMap.put (Color.CYAN,       loc ("Cyan"));          //NOI18N
        colorMap.put (Color.DARK_GRAY,  loc ("Dark_Gray"));     //NOI18N
        colorMap.put (Color.GRAY,       loc ("Gray"));          //NOI18N
        colorMap.put (Color.GREEN,      loc ("Green"));         //NOI18N
        colorMap.put (Color.LIGHT_GRAY, loc ("Light_Gray"));    //NOI18N
        colorMap.put (Color.MAGENTA,    loc ("Magneta"));       //NOI18N
        colorMap.put (Color.ORANGE,     loc ("Orange"));        //NOI18N
        colorMap.put (Color.PINK,       loc ("Pink"));          //NOI18N
        colorMap.put (Color.RED,        loc ("Red"));           //NOI18N
        colorMap.put (Color.WHITE,      loc ("White"));         //NOI18N
        colorMap.put (Color.YELLOW,     loc ("Yellow"));        //NOI18N
    }
    
    private static Object[] content = new Object[] {
	new Value (Color.BLACK), 
	new Value (Color.BLUE), 
	new Value (Color.CYAN), 
	new Value (Color.DARK_GRAY), 
	new Value (Color.GRAY), 
	new Value (Color.GREEN), 
	new Value (Color.LIGHT_GRAY), 
	new Value (Color.MAGENTA), 
	new Value (Color.ORANGE), 
	new Value (Color.PINK), 
	new Value (Color.RED), 
	new Value (Color.WHITE), 
	new Value (Color.YELLOW), 
	CUSTOM_COLOR, 
	new Value ("None", null)
    };
    
    
    /** Creates a new instance of ColorChooser */
    public ColorComboBox () {
        super (content);
        setRenderer (new Renderer ());
        setEditable (true);
        setEditor (new Renderer ());
	setSelectedItem (new Value (null, null));
        addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent ev) {
                if (getSelectedItem () == CUSTOM_COLOR) {
                    Color c = JColorChooser.showDialog (
                        SwingUtilities.getAncestorOfClass 
                            (Dialog.class, ColorComboBox.this),
                        loc ("SelectColor"),
                        null
                    );
                    setColor (c);
                }
                ColorComboBox.this.firePropertyChange (PROP_COLOR, null, null);
            }
        });
    }
    
    public void setDefaultColor (Color color) {
	Object[] ncontent = new Object [content.length];
	System.arraycopy (content, 0, ncontent, 0, content.length);
	ncontent [content.length - 1] = new Value (
	    "Default", color
	);
	setModel (new DefaultComboBoxModel (ncontent));
    }
    
    public void setColor (Color color) {
        if (color == null)
            setSelectedIndex (content.length - 1);
        else
            setSelectedItem (new Value (color));
    }
    
    public Color getColor () {
	if (getSelectedIndex () == (content.length - 1)) return null;
        return ((Value) getSelectedItem ()).color;
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ColorComboBox.class, key);
    }

    
    // innerclasses ............................................................
    
    public static class Value {
        String text;
        Color color;
        
        Value (Color color) {
            this.color = color;
	    text = (String) colorMap.get (color);
	    if (text != null) return;
            StringBuffer sb = new StringBuffer ();
            sb.append ('[').append (color.getRed ()).
                append (',').append (color.getGreen ()).
                append (',').append (color.getBlue ()).
                append (']');
            text = sb.toString ();
        }
        
        Value (String text, Color color) {
            this.text = text;
            this.color = color;
        }
    }
    
    private static class Editor extends JLabel implements ComboBoxEditor {

        private Object value;
        
        Editor () {
            //setOpaque (false);
        }
        
        public Component getEditorComponent () {
            return this;
        }

        public void setItem (Object anObject) {
            value = anObject;
            if (value instanceof String) {
                setText ("Default");
                super.setForeground (SystemColor.textText);
                super.setBackground (SystemColor.text);
            } else {
                setText ("");
                super.setBackground ((Color) value);
            }
        }

        public Object getItem () {
            return value;
        }
        
	public void setBackground (Color c) {}
	public void setForeground (Color c) {}
        
        public void selectAll() {}
        public void addActionListener (ActionListener l) {}
        public void removeActionListener (ActionListener l) {}
    }
    
    private class Renderer extends JComponent implements 
    ListCellRenderer, ComboBoxEditor {
	
        private int             SIZE = 9;
	private Value           value;
        
        Renderer () {
            setPreferredSize (new Dimension (
                50, getFontMetrics (ColorComboBox.this.getFont ()).getHeight () + 2
            ));
	    setOpaque (true);
        }
        
        public void paint (Graphics g) {
            Color oldColor = g.getColor ();
            Dimension size = getSize ();
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
                g.setColor (Color.black);
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
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
        ) {
	    this.value = (Value) value;
	    setEnabled (list.isEnabled ());
            return this;
        }
        
        public Component getEditorComponent () {
	    setEnabled (ColorComboBox.this.isEnabled ());
            return this;
        }

        public void setItem (Object anObject) {
	    this.value = (Value) anObject;
        }

        public Object getItem () {
            return value;
        }         
        public void selectAll() {}
        public void addActionListener (ActionListener l) {}
        public void removeActionListener (ActionListener l) {}   }
}
