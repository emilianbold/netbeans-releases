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

package org.netbeans.modules.java.source.usages;

import javax.tools.JavaFileManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public abstract class ClasspathInfoAccessor {

    static {
        try {
            Class.forName("org.netbeans.api.java.source.ClasspathInfo",true,ClasspathInfoAccessor.class.getClassLoader());
        } catch (ClassNotFoundException cnfe) {
            ErrorManager.getDefault().notify(cnfe);
        }
    }

    public static ClasspathInfoAccessor INSTANCE;
       
    
    public abstract JavaFileManager getFileManager(ClasspathInfo cpInfo);
    
    public abstract ClasspathInfo create (FileObject fo, JavaFileFilterImplementation filter, boolean backgroundCompilation, boolean ignoreExcludes);
    
    public abstract ClasspathInfo create (ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath, JavaFileFilterImplementation filter,
                                          boolean backgroundCompilation, boolean ignoreExcludes);
    
}
