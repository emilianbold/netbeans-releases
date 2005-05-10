/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.listener;

import org.netbeans.modules.jmx.mbeanwizard.popup.AbstractPopup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;

/**
 *
 * @author an156382
 */
public class ClosePopupButtonListener implements ActionListener{
    
    private AbstractPopup popup = null;
    private JTextField text = null;
    
    /** Creates a new instance of ClosePopupButtonListener */
    public ClosePopupButtonListener(AbstractPopup popup, JTextField text) {
        
        this.popup = popup;
        this.text = text;
    }
    
    public void actionPerformed(ActionEvent evt) {
        
        text.setText(popup.storeSettings());
        popup.dispose();
    }
}
