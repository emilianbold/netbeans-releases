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

package org.netbeans.modules.java;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Data loader for .class files.
 * @author Jan Pokorsky
 */
public final class ClassDataLoader extends UniFileLoader {

    /** The standard extension for Java class files. */
    public static final String CLASS_EXTENSION = "class"; // NOI18N

    protected ClassDataLoader() {
        super("org.netbeans.modules.java.ClassDataObject"); // NOI18N
        getExtensions().addExtension(CLASS_EXTENSION);
    }
    

    protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, IOException {

        if (primaryFile.getExt().equals(CLASS_EXTENSION)) {
            return new ClassDataObject(primaryFile, this);
        }
        return null;
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(ClassDataLoader.class,
                "PROP_ClassLoader_Name"); // NOI18N
    }
    
}
