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

import java.io.IOException;
import java.nio.charset.Charset;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.SourceAnalyser;
import org.openide.filesystems.FileObject;

class JavaParsingContext {

    final ClasspathInfo cpInfo;
    final String sourceLevel;
    final JavaFileFilterImplementation filter;
    final Charset encoding;
    final ClassIndexImpl uq;
    final SourceAnalyser sa;

    public JavaParsingContext(final FileObject root) throws IOException {
        cpInfo = ClasspathInfo.create(root);
        sourceLevel = SourceLevelQuery.getSourceLevel(root);
        filter = JavaFileFilterQuery.getFilter(root);
        encoding = FileEncodingQuery.getEncoding(root);
        uq = ClassIndexManager.getDefault().createUsagesQuery(root.getURL(), true);
        sa = uq != null ? uq.getSourceAnalyser() : null;
    }

    public JavaParsingContext(final FileObject root, final ClassPath bootPath, final ClassPath compilePath, final ClassPath sourcePath, boolean checkForModifications) throws IOException {
        cpInfo = ClasspathInfoAccessor.getINSTANCE().create(bootPath, compilePath, sourcePath, null, !checkForModifications, false, false);
        sourceLevel = SourceLevelQuery.getSourceLevel(root);
        filter = JavaFileFilterQuery.getFilter(root);
        encoding = FileEncodingQuery.getEncoding(root);
        uq = ClassIndexManager.getDefault().createUsagesQuery(root.getURL(), true);
        sa = uq != null ? uq.getSourceAnalyser() : null;
    }
}
