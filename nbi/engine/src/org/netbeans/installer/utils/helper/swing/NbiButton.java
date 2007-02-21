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

import java.awt.Toolkit;
import javax.swing.JButton;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiButton extends JButton {
    public NbiButton() {
        super();
        
        if (SystemUtils.isWindows()) {
            final Object object = Toolkit.
                    getDefaultToolkit().
                    getDesktopProperty("win.xpstyle.themeActive");
            
            if (object != null) {
                boolean xpThemeActive = (Boolean) object;
                if (xpThemeActive) {
                    setOpaque(false);
                }
            }
        }
    }
    
    public void setText(String text) {
        super.setText(StringUtils.stripMnemonic(text));
        super.setMnemonic(StringUtils.fetchMnemonic(text));
    }
}
