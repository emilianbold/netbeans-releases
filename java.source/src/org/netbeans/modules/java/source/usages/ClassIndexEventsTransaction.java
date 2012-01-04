/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.indexing.TransactionContext;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class ClassIndexEventsTransaction extends TransactionContext.Service {

    private final Set<URL> removedRoots;
    private final Collection<ElementHandle<TypeElement>> addedTypes;
    private final Collection<ElementHandle<TypeElement>> removedTypes;
    private final Collection<ElementHandle<TypeElement>> changedTypes;
    private URL addedRoot;
    private URL changesInRoot;

    private ClassIndexEventsTransaction() {
        removedRoots = new HashSet<URL>();
        addedTypes = new HashSet<ElementHandle<TypeElement>>();
        removedTypes = new HashSet<ElementHandle<TypeElement>>();
        changedTypes = new HashSet<ElementHandle<TypeElement>>();
    }


    public void rootAdded(@NonNull final URL root) {
        assert root != null;
        assert addedRoot == null;
        addedRoot = root;
    }

    public void rootRemoved(@NonNull final URL root) {
        assert root != null;
        removedRoots.add(root);
    }

    public void addedTypes(
        @NonNull final URL root,
        @NonNull final Collection<? extends ElementHandle<TypeElement>> added) {
        assert root != null;
        assert added != null;
        assert changesInRoot == null || changesInRoot.equals(root);
        assert addedRoot == null || addedRoot.equals(root);
        addedTypes.addAll(added);
        changesInRoot = root;
    }

    public void removedTypes(
        @NonNull final URL root,
        @NonNull final Collection<? extends ElementHandle<TypeElement>> removed) {
        assert root != null;
        assert removed != null;
        assert changesInRoot == null || changesInRoot.equals(root);
        assert addedRoot == null || addedRoot.equals(root);
        removedTypes.addAll(removed);
        changesInRoot = root;
    }

    public void changedTypes(
        @NonNull final URL root,
        @NonNull final Collection<? extends ElementHandle<TypeElement>> changed) {
        assert root != null;
        assert changed != null;
        assert changesInRoot == null || changesInRoot.equals(root);
        assert addedRoot == null || addedRoot.equals(root);
        changedTypes.addAll(changed);
        changesInRoot = root;
    }

    @Override
    protected void commit() throws IOException {
        final ClassIndexManager ciManager = ClassIndexManager.getDefault();
        ciManager.fire(Collections.<URL>singleton(addedRoot), removedRoots);
        final ClassIndexImpl ci = changesInRoot == null ?
            null:
            ciManager.getUsagesQuery(changesInRoot, false);
        if (ci != null) {
            ci.typesEvent(addedTypes, removedTypes, changedTypes);
        }
    }

    @Override
    protected void rollBack() throws IOException {
    }

    @NonNull
    public static ClassIndexEventsTransaction create() {
        return new ClassIndexEventsTransaction();
    }

}
