/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.*;

import org.netbeans.modules.xml.core.lib.AbstractUtil;

/**
 *
 * @author Libor Kramolis
 * @version 0.2
 */
class Util extends AbstractUtil {

    public static final NameCheck JAVA_CHECK = new JavaIdentifierNameCheck();


    /** Default and only one instance of this class. */
    public static final Util THIS = new Util();

    /** Nobody can create instance of it, just me. */
    private Util () {
    }

    
    /** A name checker interface. */
    public static interface NameCheck {
        public boolean checkName (String name);        
    }

    /** Passes for java identifiers. */
    public static class JavaIdentifierNameCheck implements NameCheck {
        public boolean checkName (String name) {
            return name.length() > 0 && org.openide.util.Utilities.isJavaIdentifier(name);
        }
    }

    /**
     * Calculate JTable cell height for textual rows.
     */
    public static int getTextCellHeight(JTable table) {
        JComboBox template = new JComboBox();
        return template.getPreferredSize().height;
    }
}
