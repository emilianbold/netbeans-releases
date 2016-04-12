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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.ClassFinder;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.util.Name;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl.class)
public final class JavaSourceUtilImpl extends org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl {

    @Override
    protected long createTaggedCompilationController(FileObject file, long currenTag, Object[] out) throws IOException {
        assert file != null;
        final JavaSource js = JavaSource.forFileObject(file);
        return JavaSourceAccessor.getINSTANCE().createTaggedCompilationController(js, currenTag, out);
    }

    @CheckForNull
    @Override
    protected TypeElement readClassFile(@NonNull final FileObject classFile) throws IOException {
        final JavaSource js = JavaSource.forFileObject(classFile);
        final List<TypeElement> out = new ArrayList<>(1);
        js.runUserActionTask(
                (cc) -> {
                    final JavacTaskImpl jt = JavaSourceAccessor.getINSTANCE()
                            .getCompilationInfoImpl(cc).getJavacTask();
                    final Symtab syms = Symtab.instance(jt.getContext());
                    final Symbol.ClassSymbol sym;
                    if (FileObjects.MODULE_INFO.equals(classFile.getName())) {
                        final String moduleName = SourceUtils.getModuleName(classFile.getParent().toURL());
                        if (moduleName != null) {
                            final Symbol.ModuleSymbol msym = syms.enterModule((Name)cc.getElements().getName(moduleName));
                            sym = msym.module_info;
                            if (sym.classfile == null) {
                                sym.classfile = FileObjects.fileObjectFileObject(classFile, classFile.getParent(), null, null);
                                sym.owner = msym;
                                msym.owner = syms.noSymbol;
                                sym.completer = ClassFinder.instance(jt.getContext()).getCompleter();
                                msym.classLocation = StandardLocation.CLASS_PATH;
                            }
                            msym.complete();
                        } else {
                            sym = null;
                        }
                    } else {
                        throw new UnsupportedOperationException("Not supported yet.");  //NOI18N
                    }
                    out.add(sym);
                },
                true);
        return out.get(0);
    }

}
