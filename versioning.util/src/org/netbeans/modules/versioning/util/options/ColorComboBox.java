/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.versioning.util.options;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;

/**
 *
 * copied from editor/options.
 * @author Maros Sandor
 */
class ColorComboBox {
    
    public static final String PROP_COLOR = "color"; //NOI18N
    
    private static Object[] content = new Object[] {
	new ColorValue(Color.BLACK), 
	new ColorValue(Color.BLUE), 
	new ColorValue(Color.CYAN), 
	new ColorValue(Color.DARK_GRAY), 
	new ColorValue(Color.GRAY), 
	new ColorValue(Color.GREEN), 
	new ColorValue(Color.LIGHT_GRAY), 
	new ColorValue(Color.MAGENTA), 
	new ColorValue(Color.ORANGE), 
	new ColorValue(Color.PINK), 
	new ColorValue(Color.RED), 
	new ColorValue(Color.WHITE), 
	new ColorValue(Color.YELLOW), 
	ColorValue.CUSTOM_COLOR, 
    };
    
    
    /** Creates a new instance of ColorChooser */
    public static void init (final JComboBox combo) {
        combo.setModel (new DefaultComboBoxModel (content));
        combo.setRenderer (new ColorComboBoxRenderer(combo));
        combo.setEditable (true);
        combo.setEditor (new ColorComboBoxRenderer(combo));
	combo.setSelectedItem (new ColorValue(null, null));
        combo.addActionListener (new ColorComboBox.ComboBoxListener(combo));
    }
    
    public static void setColor (JComboBox combo, Color color) {
        if (color == null) {
            combo.setSelectedIndex (content.length - 1);
        } else {
            combo.setSelectedItem (new ColorValue(color));
        }
    }
    
    public static Color getColor (JComboBox combo) {
        // The last item is Inherited Color or None
        if (combo.getSelectedIndex() < combo.getItemCount() - 1) {
            return ((ColorValue) combo.getSelectedItem()).color;
        } else {
            return null;
        }
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
                    SwingUtilities.getAncestorOfClass(Dialog.class, combo),
                    loc("SelectColor"), //NOI18N
                    lastSelection != null ? ((ColorValue) lastSelection).color : null
                );
                if (c != null) {
                    setColor(combo, c);
                } else if (lastSelection != null) {
                    combo.setSelectedItem(lastSelection);
                }
            }
            lastSelection = combo.getSelectedItem();
        }
        
    } // ComboListener
    
}
