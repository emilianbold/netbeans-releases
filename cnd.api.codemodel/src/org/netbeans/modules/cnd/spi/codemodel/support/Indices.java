/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.spi.codemodel.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.spi.codemodel.CMIndexImplementation;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIAccessor;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
/*package*/ final class Indices {

    private static final Indices INSTANCE = new Indices();

    private final HashMap<Object, CMIndex> byExtKey = new HashMap<>();
    private final HashMap<CMIndexImplementation, CMIndex> byImpl = new HashMap<>();
    private final Object lock = new Object();

    public static Indices getInstance(){
        return INSTANCE;
    }

    public void registerIndex(Object key, CMIndex index) {
        CMIndexImplementation impl = APIAccessor.get().getIndexImpl(index);
        synchronized (lock) {
            // TODO: replace with WeakReferences
            if (byExtKey.get(key) != null) {
                Exceptions.printStackTrace(new IllegalStateException("Index for key " + key + " already exists!")); //NOI18N
            }
            if (byImpl.get(impl) != null) {
                Exceptions.printStackTrace(new IllegalStateException("Index for impl " + impl + " already exists!")); //NOI18N
            }
            byExtKey.put(key, index);
            byImpl.put(impl, index);
        }
        if (impl != null) {
            impl.onRegister();
        }
    }

    public CMIndex getIndex(Object key) {
        synchronized (lock) {
            return byExtKey.get(key);
        }
    }

    public void unregisterIndex(Object key) {
        CMIndex removed;
        synchronized (lock) {
            removed = byExtKey.remove(key);
        }
        if (removed == null) {
            Exceptions.printStackTrace(new IllegalStateException("Index for key " + key + " does not exist")); //NOI18N
        } else {
            CMIndexImplementation impl = APIAccessor.get().getIndexImpl(removed);
            removed = byImpl.remove(impl);
            if (removed == null) {
                Exceptions.printStackTrace(new IllegalStateException("Index for impl " + key + " does not exist")); //NOI18N
            }
            if (impl != null) {
                impl.onUnregister();
            }
        }
    }

    public Collection<CMIndex> getIndices() {
        synchronized (lock) {
            return new ArrayList<>(byExtKey.values());
        }
    }

    public CMIndex getIndexByImpl(CMIndexImplementation impl) {
        synchronized (lock) {
            return byImpl.get(impl);
        }
    }
}
