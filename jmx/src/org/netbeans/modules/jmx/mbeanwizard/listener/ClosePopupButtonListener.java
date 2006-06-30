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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.listener;

import org.netbeans.modules.jmx.mbeanwizard.popup.AbstractPopup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;

/**
 * Class handling the event when the OK button is pressed on a popup window
 *
 */
public class ClosePopupButtonListener implements ActionListener{

    private AbstractPopup popup = null;
    private JTextField text = null;

    /**
     * Constructor
     * @param popup the popup concerned
     * @param text the textfield to fill with the popup contents
     */
    public ClosePopupButtonListener(AbstractPopup popup, JTextField text) {
        
        this.popup = popup;
        this.text = text;
    }
    
    /**
     * Method handling what to do if the listener has been invoked
     * Here: close the popup and fill the text field
     * @param evt an ActionEvent
     */
    public void actionPerformed(ActionEvent evt) {
        
        text.setText(popup.storeSettings());
        popup.dispose();
    }
}
