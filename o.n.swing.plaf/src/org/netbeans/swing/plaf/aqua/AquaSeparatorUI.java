/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * AquaSeparatorUI.java
 *
 * Created on March 14, 2004, 4:57 AM
 */

package org.netbeans.swing.plaf.aqua;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.SeparatorUI;
import java.awt.*;

/**
Aqua separators calculate height based on the system font size,
 * resulting in huge white spaces in menus.  
 *
 * @author  Tim Boudreau
 */
public class AquaSeparatorUI extends SeparatorUI {
    
    private static ComponentUI separatorui = new AquaSeparatorUI();
    
    public static ComponentUI createUI(JComponent c) {
        return separatorui;
    }        

    public Dimension getPreferredSize(JComponent c) {
        return new Dimension (10, 4);
    }    
}
