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
package org.netbeans.modules.dlight.perfan.storage.impl;

import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.util.CollectionToStringConvertor;

public class Metrics {

    final String mspec;
    final String msort;
    private final static CollectionToStringConvertor<Column> convertor;


    static {
        convertor = new CollectionToStringConvertor<Column>(":", // NOI18N
                new CollectionToStringConvertor.Convertor<Column>() {

            public String itemToString(Column item) {
                return item.getColumnName();
            }
        });
    }

    Metrics(String mspec, String msort) {
        this.mspec = mspec;
        this.msort = msort;
    }

    public static Metrics constructFrom(final List<Column> columns, final List<Column> orderBy) {
        String mspecResult = convertor.collectionToString(columns);
        String msortResult = convertor.collectionToString(orderBy);

        if ("".equals(msortResult)) { // NOI18N
            msortResult = columns.get(0).getColumnName();
        }

        return new Metrics(mspecResult, msortResult);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Metrics)) {
            throw new IllegalArgumentException();
        }
        Metrics o = (Metrics) obj;
        return o.msort.equals(msort) && o.mspec.equals(mspec);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.mspec != null ? this.mspec.hashCode() : 0);
        hash = 89 * hash + (this.msort != null ? this.msort.hashCode() : 0);
        return hash;
    }
}
