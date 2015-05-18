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

package org.netbeans.modules.java.source.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.parsing.FileManagerTransaction;
import org.netbeans.modules.java.source.parsing.ProcessorGenerated;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.spi.jumpto.support.AsyncDescriptor;
import org.netbeans.spi.jumpto.support.DescriptorChangeEvent;
import org.netbeans.spi.jumpto.support.DescriptorChangeListener;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
final class AsyncJavaSymbolDescriptor extends JavaSymbolDescriptorBase implements AsyncDescriptor<SymbolDescriptor> {

    private static final RequestProcessor WORKER = new RequestProcessor(AsyncJavaSymbolDescriptor.class);
    private static final String INIT = "<init>"; //NOI18N

    private final String ident;
    private final boolean caseSensitive;
    private final List<DescriptorChangeListener<SymbolDescriptor>> listeners;
    private final AtomicBoolean initialized;

    AsyncJavaSymbolDescriptor (
            @NullAllowed final Project project,
            @NonNull final FileObject root,
            @NonNull final ClassIndexImpl ci,
            @NonNull final ElementHandle<TypeElement> owner,
            @NonNull final String ident,
            final boolean caseSensitive) {
        super(owner, project, root, ci);
        assert ident != null;
        this.ident = ident;
        this.listeners = new CopyOnWriteArrayList<>();
        this.initialized = new AtomicBoolean();
        this.caseSensitive = caseSensitive;
    }

    @Override
    public Icon getIcon() {
        initialize();
        return null;
    }

    @Override
    public String getSymbolName() {
        initialize();
        return ident;
    }

    @Override
    public String getSimpleName() {
        return ident;
    }

    @Override
    public void open() {
        final Collection<? extends SymbolDescriptor> symbols = resolve();
        if (!symbols.isEmpty()) {
            symbols.iterator().next().open();
        }
    }

    private void initialize() {
        if (initialized.compareAndSet(false, true)) {
            final Runnable action = new Runnable() {
                @Override
                public void run() {
                    final Collection<? extends SymbolDescriptor> symbols = resolve();
                    fireDescriptorChange(symbols);
                }
            };
            WORKER.execute(action);
        }
    }

    @Override
    public void addDescriptorChangeListener(@NonNull final DescriptorChangeListener<SymbolDescriptor> listener) {
        Parameters.notNull("listener", listener);
        listeners.add(listener);
    }

    @Override
    public void removeDescriptorChangeListener(@NonNull final DescriptorChangeListener<SymbolDescriptor> listener) {
        Parameters.notNull("listener", listener);
        listeners.remove(listener);
    }

    private void fireDescriptorChange(Collection<? extends SymbolDescriptor> replacement) {
        final DescriptorChangeEvent<SymbolDescriptor> event = new DescriptorChangeEvent<>(
            this,
            replacement);
        for (DescriptorChangeListener<SymbolDescriptor> l : listeners) {
            l.descriptorChanged(event);
        }
    }

    @NonNull
    private Collection<? extends SymbolDescriptor> resolve() {
        try {
            final List<SymbolDescriptor> symbols = new ArrayList<>();
            TransactionContext.
                beginTrans().
                register(FileManagerTransaction.class, FileManagerTransaction.read()).
                register(ProcessorGenerated.class, ProcessorGenerated.nullWrite());
            try {
                final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(getRoot(),null,true,true,false,false);
                final JavaSource js = JavaSource.create(cpInfo);
                js.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run (final CompilationController controller) {
                        final TypeElement te = getOwner().resolve(controller);
                        if (te != null) {
                            if (ident.equals(getSimpleName(te, null, caseSensitive))) {
                                final String simpleName = te.getSimpleName().toString();
                                final String simpleNameSuffix = null;
                                final ElementKind kind = te.getKind();
                                final Set<Modifier> modifiers = te.getModifiers();
                                final ElementHandle<?> me = ElementHandle.create(te);
                                symbols.add(new ResolvedJavaSymbolDescriptor(
                                        AsyncJavaSymbolDescriptor.this,
                                        simpleName,
                                        simpleNameSuffix,
                                        kind,
                                        modifiers,
                                        me));
                            }
                            for (Element ne : te.getEnclosedElements()) {
                                if (ident.equals(getSimpleName(ne, te, caseSensitive))) {
                                    final Pair<String,String> name = JavaSymbolProvider.getDisplayName(ne, te);
                                    final String simpleName = name.first();
                                    final String simpleNameSuffix = name.second();
                                    final ElementKind kind = ne.getKind();
                                    final Set<Modifier> modifiers = ne.getModifiers();
                                    final ElementHandle<?> me = ElementHandle.create(ne);
                                    symbols.add(new ResolvedJavaSymbolDescriptor(
                                            AsyncJavaSymbolDescriptor.this,
                                            simpleName,
                                            simpleNameSuffix,
                                            kind,
                                            modifiers,
                                            me));
                                }
                            }
                        }
                    }
                }, true);
            }finally {
                TransactionContext.get().commit();
            }
            return symbols;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
           return Collections.<SymbolDescriptor>emptyList();
        }
    }

    @NonNull
    private static String getSimpleName (
            @NonNull final Element element,
            @NullAllowed final Element enclosingElement,
            final boolean caseSensitive) {
        String result = element.getSimpleName().toString();
        if (enclosingElement != null && INIT.equals(result)) {
            result = enclosingElement.getSimpleName().toString();
        }
        if (!caseSensitive) {
            result = result.toLowerCase();
        }
        return result;
    }
}
