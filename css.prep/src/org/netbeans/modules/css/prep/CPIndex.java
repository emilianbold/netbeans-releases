/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.css.prep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * @author mfukala@netbeans.org
 */
public class CPIndex {

    private static final Map<Project, CPIndex> INDEXES = new WeakHashMap<Project, CPIndex>();
    
    /**
     * Gets an instance of {@link CPIndex}. The instance may be cached.
     * 
     * @param project The project for which you want to get the index for.
     * @return non null instance of the {@link CPIndex}
     * @throws IOException 
     */
    public static CPIndex get(Project project) throws IOException {
        if(project == null) {
            throw new NullPointerException();
        }
        synchronized (INDEXES) {
            CPIndex index = INDEXES.get(project);
            if(index == null) {
                index = new CPIndex(project);
                INDEXES.put(project, index);
            } 
            return index;
        }
    }
    
    private final QuerySupport querySupport;
    
     /** Creates a new instance of JsfIndex */
    private CPIndex(Project project) throws IOException {
        Collection<FileObject> sourceRoots = QuerySupport.findRoots(project,
                null /* all source roots */,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
        this.querySupport = QuerySupport.forRoots(CPCustomIndexer.INDEXER_NAME, CPCustomIndexer.INDEXER_VERSION, sourceRoots.toArray(new FileObject[]{}));
    }

    /**
     * Gets a collection of valid files of the given {@link CPFileType} type for the
     * index scope.
     * 
     * @param type
     * @return 
     */
    public Collection<FileObject> findFiles(CPFileType type) {
        try {
            Collection<FileObject> files = new ArrayList<FileObject>();
            
            Collection<? extends IndexResult> results =
                       querySupport.query(
                    CPCustomIndexer.CP_TYPE_KEY, 
                    type.name(), 
                    QuerySupport.Kind.EXACT);
            
            for (IndexResult result : results) {
                FileObject file = result.getFile();
                if (file != null
                        && file.isValid()) {
                    files.add(file);
                }
            }
            
            return files;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

}
