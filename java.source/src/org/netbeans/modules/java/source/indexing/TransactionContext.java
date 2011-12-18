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
package org.netbeans.modules.java.source.indexing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
public final class TransactionContext {

    private static final ThreadLocal<TransactionContext> ctx = new ThreadLocal<TransactionContext>();
    private final Map<Class<? extends Service>,Service> services;

    private TransactionContext() {
        services = new HashMap<Class<? extends Service>, Service>();
    }

    public final void commit() throws IOException {
        if (ctx.get() != this) {
            throw new IllegalStateException();
        }
        try {
            for (Service s : services.values()) {
                s.commit();
            }
        } finally {
            services.clear();
            ctx.remove();
        }
    }

    public final void rollBack() throws IOException {
        if (ctx.get() != this) {
            throw new IllegalStateException();
        }
        try {
            for (Service s : services.values()) {
                s.rollBack();
            }
        } finally {
            services.clear();
            ctx.remove();
        }
    }

    public TransactionContext register(
        @NonNull final Class<? extends Service> type,
        @NonNull final Service service) {
        assert type != null;
        assert service != null;
        services.put(type,service);
        return this;
    }

    @CheckForNull
    public <T extends Service> T get(@NonNull final Class<T> type) {
        return (T) services.get(type);
    }

    @NonNull
    public static TransactionContext beginTrans() {
        if (ctx.get() != null) {
            throw new IllegalStateException();
        }
        final TransactionContext res = new TransactionContext();
        ctx.set(res);
        return res;
    }

    @NonNull
    public static TransactionContext get() throws IllegalStateException {
        final TransactionContext res = ctx.get();
        if (res == null) {
            throw new IllegalStateException();
        }
        return res;
    }


    public static abstract class Service {
        protected abstract void commit() throws IOException;
        protected abstract void rollBack() throws IOException;
    }

}
