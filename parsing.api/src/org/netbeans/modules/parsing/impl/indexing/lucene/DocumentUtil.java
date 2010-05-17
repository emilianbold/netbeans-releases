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

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 *
 * @author Tomas Zezula
 */
public class DocumentUtil {

    static final String FIELD_SOURCE_NAME = "_sn";  //NOI18N

    @SuppressWarnings("deprecation") //NOI18N
    static Fieldable sourceNameField(String relativePath) {
        return new Field(DocumentUtil.FIELD_SOURCE_NAME, relativePath, Field.Store.YES, Field.Index.NO_NORMS);
    }
    static Query sourceNameQuery(String relativePath) {
        return new TermQuery(sourceNameTerm(relativePath));
    }

    static Term sourceNameTerm (final String relativePath) {
        assert relativePath != null;
        return new Term (FIELD_SOURCE_NAME, relativePath);
    }

    // when fields == null load all fields
    static FieldSelector selector (String... fieldNames) {
        if (fieldNames != null && fieldNames.length > 0) {
            final Set<String> fields = new HashSet<String>(Arrays.asList(fieldNames));
            fields.add(FIELD_SOURCE_NAME);
            final FieldSelector selector = new SetBasedFieldSelector(fields,
                    Collections.<String>emptySet());
            return selector;
        } else {
            return ALL;
        }
    }

    private static final FieldSelector ALL = new FieldSelector() {
        public FieldSelectorResult accept(String arg0) {
            return FieldSelectorResult.LOAD;
        }
    };
}
