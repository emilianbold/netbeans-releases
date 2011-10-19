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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClassIndex.SearchScopeType;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.modules.parsing.lucene.support.StoppableConvertor;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class QueryUtil {
        
    
    
    static Query createUsagesQuery(
            final @NonNull String resourceName,
            final @NonNull Set<? extends ClassIndexImpl.UsageType> mask,
            final @NonNull Occur operator) {
        Parameters.notNull("resourceName", resourceName);
        Parameters.notNull("mask", mask);
        Parameters.notNull("operator", operator);
        if (operator == Occur.SHOULD) {
            final BooleanQuery query = new BooleanQuery ();
            for (ClassIndexImpl.UsageType ut : mask) {
                final Query subQuery = new WildcardQuery(DocumentUtil.referencesTerm (resourceName, EnumSet.of(ut)));
                query.add(subQuery, operator);
            }
            return query;
        } else if (operator == Occur.MUST) {
            return new WildcardQuery(DocumentUtil.referencesTerm (resourceName, mask));
        } else {
            throw new IllegalArgumentException();
        }
    }

    @NonNull
    static Query createPackageUsagesQuery (
            @NonNull final String packageName,
            @NonNull final Set<? extends UsageType> mask,
            @NonNull Occur operator) {
        Parameters.notNull("packageName", packageName); //NOI18N
        Parameters.notNull("mask", mask); //NOI18N
        final String pattern = Pattern.quote(packageName) + "\\.[^\\.]+";   //NOI18N
        if (operator == Occur.SHOULD) {
            final BooleanQuery query = new BooleanQuery ();
            for (ClassIndexImpl.UsageType ut : mask) {
                final Term t = DocumentUtil.referencesTerm (pattern, EnumSet.of(ut));
                query.add(Queries.createQuery(t.field(), t.field(), t.text(), Queries.QueryKind.REGEXP), operator);
            }
            return query;
        } else if (operator == Occur.MUST) {
            final Term t = DocumentUtil.referencesTerm (pattern, mask);
            return Queries.createQuery(t.field(), t.field(), t.text(), Queries.QueryKind.REGEXP);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @CheckForNull
    static Query scopeFilter (
            @NonNull final Query q,
            @NonNull final Set<? extends SearchScopeType> scope) {
        assert q != null;
        assert scope != null;
        Set<String> pkgs = null;
        for (SearchScopeType s : scope) {
            Set<? extends String> sp = s.getPackages();
            if (sp != null) {
                if (pkgs == null) {
                    pkgs = new HashSet<String>();
                }
                pkgs.addAll(sp);
            }
        }
        if (pkgs == null) {
            return q;
        }
        switch (pkgs.size()) {
            case 0:
                return null;
            case 1:
            {
                //Todo perf: Use filter query
                final BooleanQuery qFiltered = new BooleanQuery();
                qFiltered.add(
                    new TermQuery(
                        new Term (
                            DocumentUtil.FIELD_PACKAGE_NAME,
                            pkgs.iterator().next())),
                    Occur.MUST);
                qFiltered.add(q, Occur.MUST);
                return qFiltered;
            }
            default:
            {
                final BooleanQuery qPkgs = new BooleanQuery();
                for (String pkg : pkgs) {
                    qPkgs.add(
                        new TermQuery(
                            new Term(
                                DocumentUtil.FIELD_PACKAGE_NAME,
                                pkg)),
                            Occur.SHOULD);
                }
                final BooleanQuery qFiltered = new BooleanQuery();
                qFiltered.add(q, Occur.MUST);
                qFiltered.add(qPkgs, Occur.MUST);
                return qFiltered;
            }
        }
    }

    static Pair<StoppableConvertor<Term,String>,Term> createPackageFilter(
            final @NullAllowed String prefix,
            final boolean directOnly) {
        final Term startTerm = new Term (DocumentUtil.FIELD_PACKAGE_NAME, prefix);
        final StoppableConvertor<Term,String> filter = new PackageFilter(startTerm, directOnly);
        return Pair.of(filter,startTerm);
    }

    // <editor-fold defaultstate="collapsed" desc="Private implementation">
                            
                                    
    private static class PackageFilter implements StoppableConvertor<Term, String> {
        
        private static final Stop STOP = new Stop();
        
        private final boolean directOnly;
        private final boolean all;
        private final String fieldName;
        private final String value;
        
        PackageFilter(final @NonNull Term startTerm, final boolean directOnly) {
            this.fieldName = startTerm.field();
            this.value = startTerm.text();
            this.directOnly = directOnly;
            this.all = value.length() == 0;
        }
        
        @Override
        public String convert(Term currentTerm) throws Stop {
            if (fieldName != currentTerm.field()) {
                throw STOP;
            }
            String currentText = currentTerm.text();
            if (all || currentText.startsWith(value)) {
                if (directOnly) {
                    int index = currentText.indexOf('.', value.length());    //NOI18N
                    if (index>0) {
                        currentText = currentText.substring(0,index);
                    }
                }
                return currentText;
            }
            return null;
        }
    }
    //</editor-fold>
}
