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
package org.netbeans.modules.java.editor.overridden;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class ReverseSourceRootsLookup {
    
    /** Creates a new instance of ReverseSourceRootsLookup */
    private ReverseSourceRootsLookup() {
    }
    
    public static Set<FileObject> reverseSourceRootsLookup(FileObject baseSourceRoot) {
//        System.err.println("baseSourceRoot=" + baseSourceRoot);
        Set<FileObject> result = new HashSet<FileObject>();
        
        MAIN_LOOP: for (FileObject sourceRoot : GlobalPathRegistry.getDefault().getSourceRoots()) {
//            System.err.println("sourceRoot=" + sourceRoot);
//            System.err.println("cp=" + ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE));
            for (Entry compileClassPathElement : ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE).entries()) {
//                System.err.println("checking cp element=" + compileClassPathElement);
                for (FileObject proposedSourceRoot : SourceForBinaryQuery.findSourceRoots(compileClassPathElement.getURL()).getRoots()) {
//                    System.err.println("proposedSourceRoot=" + FileUtil.getFileDisplayName(proposedSourceRoot));
                    if (baseSourceRoot.equals(proposedSourceRoot)) {
                        result.add(sourceRoot);
                        continue MAIN_LOOP;
                    }
                }
            }
        }
        
        return result;
    }
    
}
