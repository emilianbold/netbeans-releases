/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
