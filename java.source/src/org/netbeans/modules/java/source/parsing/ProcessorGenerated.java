/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.indexing.TransactionContext;

/**
 *
 * @author Tomas Zezula
 */
public final class ProcessorGenerated extends TransactionContext.Service {
    
    private static final Map<URL,Set<javax.tools.FileObject>> generatedSources =
            new HashMap<URL,Set<javax.tools.FileObject>>();
    
    private static final Map<URL, Set<javax.tools.FileObject>> generatedResources =
            new HashMap<URL, Set<javax.tools.FileObject>>();
    
    
    
    private ProcessorGenerated() {}
    
    @CheckForNull
    public Set<javax.tools.FileObject> getGeneratedSources(@NonNull final URL forSource) {
        return generatedSources.get(forSource);
    }
    
    @CheckForNull
    public Set<javax.tools.FileObject> getGeneratedResources(@NonNull final URL forSource) {
        return generatedResources.get(forSource);
    }

    @Override
    protected void commit() throws IOException {
        clear();
    }

    @Override
    protected void rollBack() throws IOException {
        clear();
    }
    
    public void register(
        @NonNull final URL forSource,
        @NonNull final javax.tools.FileObject file,
        @NonNull final GeneratedFileMarker.Type type) {
        Set<javax.tools.FileObject> insertInto;
        switch (type) {
            case SOURCE:
                insertInto = generatedSources.get(forSource);
                if (insertInto == null) {
                    insertInto = new HashSet<javax.tools.FileObject>();
                    generatedSources.put(forSource, insertInto);
                }
                break;
            case RESOURCE:
                insertInto = generatedResources.get(forSource);
                if (insertInto == null) {
                    insertInto = new HashSet<javax.tools.FileObject>();
                    generatedResources.put(forSource, insertInto);
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
        insertInto.add(file);
    }
    
    private void clear() {
        generatedSources.clear();
        generatedResources.clear();
    }
    
    public static ProcessorGenerated create() {
        return new ProcessorGenerated();
    }
}
