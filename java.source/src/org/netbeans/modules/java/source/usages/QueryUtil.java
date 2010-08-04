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

import java.io.IOException;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.OpenBitSet;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClassIndex;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class QueryUtil {
    
    static Collector createBitSetCollector(final BitSet bits) {
        return new BitSetCollector(bits);
    }
    
    static Query[] createQueries (
            final @NonNull Pair<String,String> termNames,
            final @NonNull String value,
            final @NonNull ClassIndex.NameKind kind) {
        Parameters.notNull("termNames", termNames);
        Parameters.notNull("termNames.first", termNames.first);
        Parameters.notNull("termNames.second", termNames.second);
        Parameters.notNull("value", value);
        Parameters.notNull("kind", kind);
        switch (kind) {
            case SIMPLE_NAME:
                    return new Query[] {new TermQuery(new Term (termNames.first, value))};
            case PREFIX:
                if (value.length() == 0) {
                    return new Query[] {new MatchAllDocsQuery()};
                }
                else {
                    final PrefixQuery pq = new PrefixQuery(new Term(termNames.first, value));
                    pq.setRewriteMethod(PrefixQuery.CONSTANT_SCORE_FILTER_REWRITE);
                    return new Query[] {pq};
                }
            case CASE_INSENSITIVE_PREFIX:
                if (value.length() == 0) {
                    return new Query[] {new MatchAllDocsQuery()};
                }
                else {
                    final PrefixQuery pq = new PrefixQuery(new Term(termNames.second, value.toLowerCase()));
                    pq.setRewriteMethod(PrefixQuery.CONSTANT_SCORE_FILTER_REWRITE);
                    return new Query[] {pq};
                }
            case CAMEL_CASE:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    StringBuilder sb = new StringBuilder();
                    int lastIndex = 0;
                    int index;
                    do {
                        index = findNextUpper(value, lastIndex + 1);
                        String token = value.substring(lastIndex, index == -1 ? value.length(): index);
                        sb.append(token);
                        sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                        lastIndex = index;
                    } while(index != -1);
                    return new Query[] {new FilteredQuery(new MatchAllDocsQuery(), new RegexpFilter(termNames.first,sb.toString(),true))};
                }
            case CASE_INSENSITIVE_REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                }
                return new Query[] {new FilteredQuery(new MatchAllDocsQuery(), new RegexpFilter(termNames.second, value.toLowerCase(), false))};
            case REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                }
                return new Query[] {new FilteredQuery(new MatchAllDocsQuery(), new RegexpFilter(termNames.first, value, true))};
            case CAMEL_CASE_INSENSITIVE:
                if (value.length() == 0) {
                    //Special case (all) handle in different way
                    return new Query[] {new MatchAllDocsQuery()};
                }
                else {
                    final PrefixQuery pq = new PrefixQuery(new Term(termNames.second, value.toLowerCase()));
                    pq.setRewriteMethod(PrefixQuery.CONSTANT_SCORE_FILTER_REWRITE);
                                        
                    StringBuilder sb = new StringBuilder();
                    int lastIndex = 0;
                    int index;
                    do {
                        index = findNextUpper(value, lastIndex + 1);
                        String token = value.substring(lastIndex, index == -1 ? value.length(): index);                        
                        sb.append(token.toLowerCase());
                        sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                        lastIndex = index;
                    } while(index != -1);
                    final Query fq = new FilteredQuery(new MatchAllDocsQuery(), new RegexpFilter(termNames.second,sb.toString(),false));                    
                    return new Query[]{pq,fq};
                }
            default:
                throw new UnsupportedOperationException (kind.toString());
        }
    }  
    
    static Query createUsagesQuery(
            final @NonNull String resourceName,
            final @NonNull Set<ClassIndexImpl.UsageType> mask,
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
    
    
    private static abstract class DocumentVisitor {

        public void generate(IndexReader reader, TermEnum enumerator) throws IOException {
            final int[] docs = new int[32];
            final int[] freqs = new int[32];
            final TermDocs termDocs = reader.termDocs();
            try {
                do {
                    final Term term = enumerator.term();
                    if (term == null) {
                        break;
                    }
                    termDocs.seek(term);
                    while (true) {
                        final int count = termDocs.read(docs, freqs);
                        if (count != 0) {
                            for (int i = 0; i < count; i++) {
                                visit(docs[i]);
                            }
                        } else {
                            break;
                        }
                    }
                } while (enumerator.next());
            } finally {
                termDocs.close();
            }
        }

        abstract public void visit(int doc);
    }
    
    private static int findNextUpper(String text, int offset ) {
        for( int i = offset; i < text.length(); i++ ) {
            if ( Character.isUpperCase(text.charAt(i)) ) {
                return i;
            }
        }
        return -1;
    }
    
    private static class RegexpTermEnum extends FilteredTermEnum {
        
        private final String fieldName;
        private final String startPrefix;
        private final Pattern pattern;
        private boolean endEnum;
        
        public RegexpTermEnum(
                final IndexReader in,
                final String  fieldName,
                final Pattern pattern,
                final String  startPrefix) throws IOException {
            final Term term = new Term(fieldName,startPrefix);
            this.fieldName = term.field();
            this.pattern = pattern;
            this.startPrefix = startPrefix;
            setEnum(in.terms(term));
        }

        @Override
        protected boolean termCompare(Term term) {
            if (fieldName == term.field()) {
                String searchText = term.text();
                if (searchText.startsWith(startPrefix)) {
                    return pattern.matcher(term.text()).matches();
                }
            }
            endEnum = true;
            return false;
        }

        @Override
        public float difference() {
            return 1.0f;
        }

        @Override
        protected boolean endEnum() {
            return endEnum;
        }
    }
    
    private static class RegexpFilter extends Filter {
        
        private final String fieldName;
        private final String startPrefix;
        private final Pattern pattern;
                        
        public RegexpFilter(final String fieldName, final String  regexp, final boolean caseSensitive) {
            this.fieldName = fieldName;
            this.pattern = caseSensitive ? Pattern.compile(regexp) : Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
            this.startPrefix = getStartText(regexp);
        }
        
        private static String getStartText(final String regexp) {
            if (!Character.isJavaIdentifierStart(regexp.charAt(0))) {
                return "";  //NOI18N
            }
            final StringBuilder startBuilder = new StringBuilder ();
            startBuilder.append(regexp.charAt(0));
            for (int i=1; i<regexp.length(); i++) {
                char c = regexp.charAt(i);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                startBuilder.append(c);
            }
            return startBuilder.toString();
        }
                
        @Override
        public BitSet bits(IndexReader reader) throws IOException {
            RegexpTermEnum enumerator = new RegexpTermEnum(reader, fieldName, pattern, startPrefix);
            try {
                final BitSet bitSet = new BitSet(reader.maxDoc());
                new DocumentVisitor() {

                    @Override
                    public void visit(int doc) {
                        bitSet.set(doc);
                    }
                }.generate(reader, enumerator);
                return bitSet;
            } finally {
                enumerator.close();
            }
        }

        @Override
        public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
            RegexpTermEnum enumerator = new RegexpTermEnum(reader, fieldName, pattern, startPrefix);
            try {
                // if current term in enum is null, the enum is empty -> shortcut
                if (enumerator.term() == null) {
                    return DocIdSet.EMPTY_DOCIDSET;
                }
                // else fill into a OpenBitSet
                final OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
                new DocumentVisitor() {

                    @Override
                    public void visit(int doc) {
                        bitSet.set(doc);
                    }
                }.generate(reader, enumerator);
                return bitSet;
            } finally {
                enumerator.close();
            }
        }
                        
    }
    
    private static class BitSetCollector extends Collector {

        private int docBase;
        public final BitSet bits;
        
        public BitSetCollector(final BitSet bitSet) {
            assert bitSet != null;
            bits = bitSet;
        }
 
        // ignore scorer
        public void setScorer(Scorer scorer) {
        }

        // accept docs out of order (for a BitSet it doesn't matter)
        public boolean acceptsDocsOutOfOrder() {
          return true;
        }

        public void collect(int doc) {
          bits.set(doc + docBase);
        }

        public void setNextReader(IndexReader reader, int docBase) {
          this.docBase = docBase;
        }
        
    }
            
}
