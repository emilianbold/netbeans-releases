/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted 2006 Ricoh Corporation"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
