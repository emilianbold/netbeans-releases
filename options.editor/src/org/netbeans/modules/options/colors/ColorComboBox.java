/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
public class ColorComboBox {
    
    public static final String PROP_COLOR = "color"; //NOI18N
    
    private static Object[] content = new Object[] {
	new ColorValue (Color.BLACK), 
	new ColorValue (Color.BLUE), 
	new ColorValue (Color.CYAN), 
	new ColorValue (Color.DARK_GRAY), 
	new ColorValue (Color.GRAY), 
	new ColorValue (Color.GREEN), 
	new ColorValue (Color.LIGHT_GRAY), 
	new ColorValue (Color.MAGENTA), 
	new ColorValue (Color.ORANGE), 
	new ColorValue (Color.PINK), 
	new ColorValue (Color.RED), 
	new ColorValue (Color.WHITE), 
	new ColorValue (Color.YELLOW), 
	ColorValue.CUSTOM_COLOR, 
	new ColorValue (loc ("CTL_None_Color"), null)                  //NOI18N
    };
    
    
    /** Creates a new instance of ColorChooser */
    static void init (final JComboBox combo) {
        combo.setModel (new DefaultComboBoxModel (content));
        combo.setRenderer (new ColorComboBoxRenderer (combo));
        combo.setEditable (true);
        combo.setEditor (new ColorComboBoxRenderer (combo));
	combo.setSelectedItem (new ColorValue (null, null));
        combo.addActionListener (new ComboBoxListener (combo));
    }
    
    static void setInheritedColor (JComboBox combo, Color color) {
	Object[] ncontent = new Object [content.length];
	System.arraycopy (content, 0, ncontent, 0, content.length);
        if (color != null)
            ncontent [content.length - 1] = new ColorValue (
                loc ("CTL_Inherited_Color"), color                   //NOI18N
            );
        else
            ncontent [content.length - 1] = new ColorValue (
                loc ("CTL_None_Color"), null                       //NOI18N
            );
	combo.setModel (new DefaultComboBoxModel (ncontent));
    }
    
    static void setColor (JComboBox combo, Color color) {
        if (color == null) {
            combo.setSelectedIndex (content.length - 1);
        } else {
            combo.setSelectedItem (new ColorValue (color));
        }
    }
    
    static Color getColor (JComboBox combo) {
	if (combo.getSelectedIndex () == (content.length - 1)) return null;
        return ((ColorValue) combo.getSelectedItem ()).color;
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ColorComboBox.class, key);
    }
    
    // ..........................................................................
    private static class ComboBoxListener implements ActionListener {
        
        private JComboBox combo;
        private Object lastSelection;
        
        ComboBoxListener(JComboBox combo) {
            this.combo = combo;
            lastSelection = combo.getSelectedItem();
        }
        
        public void actionPerformed(ActionEvent ev) {
            if (combo.getSelectedItem() == ColorValue.CUSTOM_COLOR) {
                Color c = JColorChooser.showDialog(
                        SwingUtilities.getAncestorOfClass
                        (Dialog.class, combo),
                        loc("SelectColor"),
                        null
                        );
                if (c != null) {
                    setColor (combo, c);
                } else if (lastSelection != null) {
                    combo.setSelectedItem(lastSelection);
                }
            }
            lastSelection = combo.getSelectedItem();
        }
        
    } // ComboListener
    
}
