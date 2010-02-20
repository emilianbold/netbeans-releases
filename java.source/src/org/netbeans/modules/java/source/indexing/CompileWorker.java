/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.indexing;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
abstract class CompileWorker {

    abstract ParsingOutput compile(ParsingOutput previous, Context context, JavaParsingContext javaContext, Iterable<? extends CompileTuple> files);
    
    protected void computeFQNs(final Map<JavaFileObject, List<String>> file2FQNs, CompilationUnitTree cut, CompileTuple tuple) {
        String pack;
        if (cut.getPackageName() != null) {
            pack = cut.getPackageName().toString() + "."; //XXX
        } else {
            pack = "";
        }
        String path = tuple.indexable.getRelativePath();
        int i = path.lastIndexOf('.');
        if (i >= 0)
            path = path.substring(0, i);
        path = FileObjects.convertFolder2Package(path);
        List<String> fqns = new LinkedList<String>();
        boolean hasClassesLivingElsewhere = false;
        for (Tree t : cut.getTypeDecls()) {
            if (t.getKind() == Tree.Kind.CLASS) {
                String fqn = pack + ((ClassTree) t).getSimpleName().toString();
                fqns.add(fqn);
                if (!path.equals(fqn)) {
                    hasClassesLivingElsewhere = true;
                }
            }
        }
        
        if (hasClassesLivingElsewhere) {
            file2FQNs.put(tuple.jfo, fqns);
        }
    }

    static class ParsingOutput {
        final boolean success;
        final Map<JavaFileObject, List<String>> file2FQNs;
        final Set<ElementHandle<TypeElement>> addedTypes;
        final Set<File> createdFiles;
        final Set<Indexable> finishedFiles;
        final Set<ElementHandle<TypeElement>> modifiedTypes;
        final Set<CompileTuple> aptGenerated;

        public ParsingOutput(boolean success, Map<JavaFileObject, List<String>> file2FQNs, Set<ElementHandle<TypeElement>> addedTypes, Set<File> createdFiles, Set<Indexable> finishedFiles, Set<ElementHandle<TypeElement>> modifiedTypes, Set<CompileTuple> aptGenerated) {
            this.success = success;
            this.file2FQNs = file2FQNs;
            this.addedTypes = addedTypes;
            this.createdFiles = createdFiles;
            this.finishedFiles = finishedFiles;
            this.modifiedTypes = modifiedTypes;
            this.aptGenerated = aptGenerated;
        }
    }    
}
