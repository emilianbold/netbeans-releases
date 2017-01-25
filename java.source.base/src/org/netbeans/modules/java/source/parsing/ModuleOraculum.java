/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.source.parsing;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = CompilerOptionsQueryImplementation.class, position = Integer.MAX_VALUE)
public final class ModuleOraculum implements CompilerOptionsQueryImplementation, Closeable {
    private final ThreadLocal<String> moduleName = new ThreadLocal<>();

    @Override
    @CheckForNull
    public Result getOptions(@NonNull final FileObject file) {
        final String name = moduleName.get();
        return name == null ?
                null :
                new R(name);
    }

    @Override
    public void close() {
        moduleName.remove();
    }

    boolean installModuleName(
            @NullAllowed FileObject root,
            @NullAllowed final FileObject fo) {
        if (root == null && fo != null) {
            final ClassPath src = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            root = src != null ?
                    src.findOwnerRoot(fo) :
                    null;
        }
        if (root == null || JavaIndex.hasSourceCache(root.toURL(), false)) {
            return false;
        }
        final FileObject moduleInfo = root.getFileObject(FileObjects.MODULE_INFO,FileObjects.JAVA);
        if (moduleInfo == null) {
            return false;
        }
        final String name = SourceUtils.parseModuleName(moduleInfo);
        moduleName.set(name);
        return true;
    }

    private static final class R extends CompilerOptionsQueryImplementation.Result {
        private final List<? extends String> ops;

        R(@NonNull final String moduleName) {
            Parameters.notNull("moduleName", moduleName);   //NOI18N
            this.ops = Collections.singletonList(String.format(
                    "-Xmodule:%s",  //NOI18N
                    moduleName));
        }

        @Override
        public List<? extends String> getArguments() {
            return ops;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }

    @CheckForNull
    static ModuleOraculum getInstance() {
        for (CompilerOptionsQueryImplementation impl : Lookup.getDefault().lookupAll(CompilerOptionsQueryImplementation.class)) {
            if (impl.getClass() == ModuleOraculum.class) {
                return (ModuleOraculum) impl;
            }
        }
        return null;
    }
}
