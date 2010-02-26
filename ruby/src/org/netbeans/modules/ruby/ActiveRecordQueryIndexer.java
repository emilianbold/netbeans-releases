/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;

/**
 * Indexes dynamic methods provided by the Rails 3 AR Query Interface.
 *
 * @author Erno Mononen
 */
final class ActiveRecordQueryIndexer {

    private static final String[] RELATION_METHODS = {"includes", "eager_load", "preload",
                                                "select", "group", "order", "joins", "where", "having",
                                                 "limit", "offset", "lock", "readonly", "create_with", "from"};

    private static final RubyType RELATION_TYPE = RubyType.create(RubyIndex.ACTIVE_RECORD_RELATION);
    private final RubyIndex index;
    private final String prefix;
    private final QuerySupport.Kind kind;
    private final String classFqn;
    private final Set<IndexedMethod> methods;

    private ActiveRecordQueryIndexer(RubyIndex index, String prefix, QuerySupport.Kind kind, String classFqn, Set<IndexedMethod> methods) {
        this.index = index;
        this.prefix = prefix;
        this.kind = kind;
        this.classFqn = classFqn;
        this.methods = methods;
    }

    static void indexQueryMehods(RubyIndex index, String prefix, QuerySupport.Kind kind,
            String classFqn, Set<IndexedMethod> methods) {
        ActiveRecordQueryIndexer indexer = new ActiveRecordQueryIndexer(index, prefix, kind, classFqn, methods);
        indexer.addQueryMethods();
    }

    static boolean isQueryMethod(String name) {
        for (String each : RELATION_METHODS) {
            if (each.equals(name)) {
                return true;
            }
        }
        return false;
    }

    static RubyType getReturnType(String name) {
        assert isQueryMethod(name);
        return RELATION_TYPE;
    }

    private void addQueryMethods() {

        if (kind == QuerySupport.Kind.EXACT) {
            return;
        }

        for (String each : RELATION_METHODS) {
            if (!each.startsWith(prefix)) {
                continue;
            }
            int flags = IndexedElement.STATIC;
            String attributes = IndexedElement.flagToString(flags) + ";;;" + "conditions";

            String fqn = classFqn + "#" + each;
            IndexedMethod method =
                    IndexedMethod.create(index, each, fqn, classFqn, null, null, attributes, flags, index.getContext());
            method.setMethodType(IndexedMethod.MethodType.DYNAMIC_FINDER);
            method.setType(RELATION_TYPE);
            method.setInherited(false);
            method.setSmart(true);
            methods.add(method);
        }
    }
}
