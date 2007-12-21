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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.editors.controls;

import java.awt.Component;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.xml.namespace.QName;

/**
 *
 * @author nk160297
 */
public class QNameComboChooser extends JComboBox {

    static final long serialVersionUID = 3571417773818237794L;

    private QName qName;

    public QNameComboChooser() {
        super();
        //
        this.setRenderer(new DefaultListCellRenderer() {
            static final long serialVersionUID = 1L;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, 
                        isSelected, cellHasFocus);
                if (value != null) {
                    QName qName = (QName)value;
                    setText(qName.getLocalPart());
                    // setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
                }
                return this;
            }
        });
    }
    
    public void setPossibleElements(QName[] elements) {
        setModel(new DefaultComboBoxModel(elements));
    }
    
    public QName getQName() {
        return (QName)getSelectedItem();
    }
    
    public void setQName(QName newValue) {
        qName = newValue;
        setSelectedItem(qName);
    }
}
