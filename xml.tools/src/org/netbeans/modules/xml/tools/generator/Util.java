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
package org.netbeans.modules.xml.tools.generator;

import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.*;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.modules.xml.core.lib.AbstractUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Libor Kramolis
 * @version 0.2
 */
class Util extends AbstractUtil {

    public static final NameCheck JAVA_CHECK = new JavaIdentifierNameCheck();

    public static final NameCheck NONEMPTY_CHECK = new StringNameCheck();

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

    /** Passes for any non-empty string. */
    public static class StringNameCheck implements NameCheck {
        public boolean checkName (String name) {
            return name.length() > 0;
        }
    }

    /**
     * Calculate JTable cell height for textual rows.
     */
    public static int getTextCellHeight(JTable table) {
        JComboBox template = new JComboBox();
        return template.getPreferredSize().height;
    }
    
    /**
     * Finds Java package name for the given folder.
     * @return package name separated by dots or null if package name
     *     could not be find for the given file
     */
    public static String findJavaPackage(FileObject fo) {
        assert fo.isFolder() : fo;
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        return cp.getResourceName(fo, '.', false);
    }
    
}
