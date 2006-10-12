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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Component;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.netbeans.api.project.SourceGroup;

/**
 *
 * @author Andrei Badea
 */
public class SourceGroupUISupport {
    
    private SourceGroupUISupport() {
    }
    
    public static void connect(JComboBox comboBox, SourceGroup[] sourceGroups) {
        comboBox.setModel(new DefaultComboBoxModel(sourceGroups));
        comboBox.setRenderer(new SourceGroupRenderer());
    }
    
    private static final class SourceGroupRenderer extends DefaultListCellRenderer {
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object displayName = null;
            
            if (value instanceof SourceGroup) {
                displayName = ((SourceGroup)value).getDisplayName();
            } else {
                displayName = value;
            }
            
            return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
        }
    }
}
