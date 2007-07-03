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
package org.netbeans.modules.refactoring.ruby;

import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 * Module installation class for Refactoring module.
 * 
 * (This came from the Java refactoring module, but it does not appear to be run as
 * a module install, so I've removed that part)
 *
 * @author Jan Becicka
 * @author Pavel Flaska
 */
public class RefactoringModule {

    /** Holds the file objects whose attributes represents options */
    private static FileObject optionsFile = null;

    /**
     * Gets the attribute of options fileobject. Attribute name is represented
     * by key parameter. If attribute value is not found, defaultValue parameter
     * is used in method return.
     * 
     * @param  key           key whose associated value is to be returned.
     * @param  defaultValue  value used when attribute is not found
     *
     * @return attribute value or defaultValue if attribute is not found
     */
    public static Object getOption(String key, Object defaultValue) {
        if (optionsFile == null) {
            findOptionsFile();
        }
        if (optionsFile == null)
            return defaultValue;
        Object o = optionsFile.getAttribute(key);
        return o != null ? o : defaultValue;
    }

    /**
     * Sets the attribute to options fileobject. This attribute is persitent
     * and allows to re-read it when IDE is restarted. Key and value pair
     * is used in the same way as Map works.
     *
     * @param key    key with which the specified value is to be associated.
     * @param value  value to be associated with the specified key.
     */
    public static void setOption(String key, Object value) {
        if (optionsFile == null) {
            findOptionsFile();
        }
        try {
            optionsFile.setAttribute(key, value);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private static void findOptionsFile() {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        optionsFile = fs.findResource("Services/org-netbeans-modules-refactoring-ruby/options"); // NOI18N
    }
}
