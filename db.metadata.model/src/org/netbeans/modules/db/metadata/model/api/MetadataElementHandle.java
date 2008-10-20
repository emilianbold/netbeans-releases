/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents the handle of a metadata element.
 *
 * <p>Metadata elements cannot escape the {@link MetadataModel#runReadAction} method.
 * Handles can be used to pass information about metadata elements out of this method.
 * The handle can be {@link #resolve resolved} to the corresponding
 * metadata element in another {@code runReadAction} method.</p>
 *
 * @param <T> the type of the metadata element that this handle was created for.
 *
 * @author Andrei Badea
 */
public class MetadataElementHandle<T extends MetadataElement> {

    private static final int CATALOG = 0;
    private static final int SCHEMA = 1;
    private static final int TABLE = 2;
    private static final int COLUMN = 3;

    private final String[] names;
    private final Kind kind;

    /**
     * Creates a handle for a metadata element.
     *
     * @param  <T> the type of the metadata element to create this handle for.
     * @param  element a metadata element.
     * @return the handle for the given metadata element.
     */
    public static <T extends MetadataElement> MetadataElementHandle<T> create(T element) {
        List<String> names = new ArrayList<String>();
        MetadataElement current = element;
        while (current != null) {
            names.add(current.getName());
            current = current.getParent();
        }
        Collections.reverse(names);
        Kind kind = Kind.of(element);
        return new MetadataElementHandle<T>(names.toArray(new String[names.size()]), kind);
    }

    // For use in unit tests.
    static <T extends MetadataElement> MetadataElementHandle<T> create(Class<T> clazz, String... names) {
        return new MetadataElementHandle<T>(names, Kind.of(clazz));
    }

    private MetadataElementHandle(String[] names, Kind kind) {
        this.names = names;
        this.kind = kind;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MetadataElementHandle<?> other = (MetadataElementHandle<?>) obj;
        if (this.kind != other.kind) {
            return false;
        }
        if (!Arrays.equals(this.names, other.names)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (String name : names) {
            if (name != null) {
                hash ^= name.hashCode();
            } else {
                // Do not XOR with a constant, since multiple nulls would
                // cause the hash to just flip-flop between two values.
                hash++;
            }
        }
        hash ^= kind.hashCode();
        return hash;
    }

    /**
     * Resolves this handle to the corresponding metadata element, if any.
     *
     * @param  metadata the {@link Metadata} instance to resolve this element against.
     * @return the corresponding metadata element or null if it could not be found
     *         (for example because it is not present in the given {@code Metadata}
     *         instance, or because it has been removed).
     */
    @SuppressWarnings("unchecked")
    public T resolve(Metadata metadata) {
        switch (kind) {
            case CATALOG:
                return (T) resolveCatalog(metadata);
            case SCHEMA:
                return (T) resolveSchema(metadata);
            case TABLE:
                return (T) resolveTable(metadata);
            case COLUMN:
                return (T) resolveColumn(metadata);
            default:
                throw new IllegalStateException("Unhandled kind " + kind);
        }
    }

    private Catalog resolveCatalog(Metadata metadata) {
        return metadata.getCatalog(names[CATALOG]);
    }

    private Schema resolveSchema(Metadata metadata) {
        Catalog catalog = resolveCatalog(metadata);
        if (catalog != null) {
            return catalog.getSchema(names[SCHEMA]);
        }
        return null;
    }

    private Table resolveTable(Metadata metadata) {
        Schema schema = resolveSchema(metadata);
        if (schema != null) {
            return schema.getTable(names[TABLE]);
        }
        return null;
    }

    private Column resolveColumn(Metadata metadata) {
        Table table = resolveTable(metadata);
        if (table != null) {
            return table.getColumn(names[COLUMN]);
        }
        return null;
    }

    private enum Kind {

        CATALOG(Catalog.class),
        SCHEMA(Schema.class),
        TABLE(Table.class),
        COLUMN(Column.class);

        public static Kind of(MetadataElement element) {
            return of(element.getClass());
        }

        public static Kind of(Class<? extends MetadataElement> clazz) {
            for (Kind kind : Kind.values()) {
                if (kind.clazz.equals(clazz)) {
                    return kind;
                }
            }
            throw new IllegalStateException("Unhandled class " + clazz);
        }

        private final Class<? extends MetadataElement> clazz;

        private Kind(Class<? extends MetadataElement> clazz) {
            this.clazz = clazz;
        }
    }
}
