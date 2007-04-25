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

package org.netbeans.modules.vmd.midp.screen.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.TextBoxCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;

/**
 *
 * @author Anton Chechel
 */
public class TextBoxDisplayPresenter extends DisplayableDisplayPresenter {
    
    private static final Border LABEL_BORDER = BorderFactory.createLineBorder(Color.GRAY);
    
    private JLabel label;
    
    public TextBoxDisplayPresenter() {
        label = new JLabel();
        label.setBorder(LABEL_BORDER);
        JPanel contentPanel = getPanel().getContentPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(label, BorderLayout.NORTH);
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        String text = MidpTypes.getString(getComponent().readProperty(TextBoxCD.PROP_STRING));
        if (text != null)
            label.setText(ScreenSupport.wrapWithHtml(text));
        else 
            label.setText("<empty text>"); //NOI18N
    }
    
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        return Collections.singleton(
                new ScreenPropertyDescriptor(getComponent(), label, new ScreenStringPropertyEditor(TextBoxCD.PROP_STRING, JTextField.CENTER))
                );
    }
    
}
