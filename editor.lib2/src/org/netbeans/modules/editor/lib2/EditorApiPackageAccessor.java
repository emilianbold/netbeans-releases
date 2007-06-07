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

package org.netbeans.modules.editor.lib2;

import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;

/**
 * Accessor for the package-private functionality of bookmarks API.
 *
 * @author Miloslav Metelka
 */

public abstract class EditorApiPackageAccessor {
    
    private static EditorApiPackageAccessor INSTANCE;
    
    public static EditorApiPackageAccessor get() {
        if (INSTANCE == null) {
            // Force instance registration
            try {
                Class.forName(EditorRegistry.class.getName(), true, EditorRegistry.class.getClassLoader());
            } catch (ClassNotFoundException e) {
            }
        }
        return INSTANCE;
    }

    public static void register(EditorApiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }
    
    /** Register text component to registry. */
    public abstract void register(JTextComponent c);
    
}
