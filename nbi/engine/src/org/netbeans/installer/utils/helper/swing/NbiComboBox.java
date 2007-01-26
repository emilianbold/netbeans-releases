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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper.swing;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiComboBox extends JComboBox {
    public NbiComboBox() {
        super();
        
        setRenderer(new NbiDefaultComboBoxRenderer());
    }
    
    private static class NbiDefaultComboBoxRenderer extends DefaultListCellRenderer {
        private boolean opaque;
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focus) {
            super.getListCellRendererComponent(list, value, index, selected, focus);
            
            if (selected) {
                setOpaque(true);
            } else {
                setOpaque(false);
            }
            
            return this;
        }
        
        public boolean isOpaque() {
            return opaque;
        }
        
        public void setOpaque(final boolean opaque) {
            this.opaque = opaque;
        }
    }
}
