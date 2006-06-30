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
/*
 * setFocus.java
 *
 * Created on November 4, 2003, 6:35 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import javax.swing.SwingUtilities;

/**
 *
 * @author  nityad
 * Class to move fous between wizard panels
 * new setFocus(firstPanel.getComponent(1));
 */

public class setFocus implements Runnable {
    private Component comp;
    public setFocus(Component comp) {
        this.comp = comp;
        try {
            SwingUtilities.invokeLater(this);
        } catch(java.lang.Exception e) {
            e.printStackTrace();
        }
    }
    public void run() {
        comp.requestFocus();
    }
}
