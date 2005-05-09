/*
 * ClosePopupButtonListener.java
 *
 * Created on April 20, 2005, 2:49 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
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
