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

package org.netbeans.modules.editor.indent;

import org.netbeans.spi.editor.indent.Context;

/**
 * Accessor for the package-private functionality of bookmarks API.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class IndentSpiPackageAccessor {
    
    private static IndentSpiPackageAccessor INSTANCE;
    
    public static IndentSpiPackageAccessor get() {
        if (INSTANCE == null) {
            // Enforce the static initializer in Context class to be run
            try {
                Class.forName(Context.class.getName(), true, Context.class.getClassLoader());
            } catch (ClassNotFoundException e) { }
        }
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(IndentSpiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }
    
    public abstract Context createContext(TaskHandler.MimeItem mimeItem);
    
}
