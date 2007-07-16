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
 * Microsystems, Inc. 
 * Portions Copyrighted 2006 Ricoh Corporation 
 * All Rights Reserved.
 */
/*
 * PortVerifier.java
 *
 * Created on September 12, 2006, 6:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.deployment.ricoh;

import java.awt.Toolkit;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author esanchez
 */
public class PortVerifier extends InputVerifier
{
    public static int MIN_PORT_VALUE = 0;
    public static int MAX_PORT_VALUE = 65535;
    
    public boolean verify(JComponent input)
    {
        if (input instanceof JTextField)
        {
            JTextField field = (JTextField)input;
            try
            {
                if (field.getText().trim().equals(""))
                    return true;
                int result = Integer.parseInt(field.getText());
                if ((result >= MIN_PORT_VALUE) && (result <= MAX_PORT_VALUE))
                    return true;
                else
                    throw new NumberFormatException("Outside port range");
            }
            catch(NumberFormatException nfe)
            {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog((Component)input.getTopLevelAncestor(), 
                                              "Enter a valid port number (" + MIN_PORT_VALUE + " to " + MAX_PORT_VALUE + ")",
                                              "Invalid port number",
                                              JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }
}
