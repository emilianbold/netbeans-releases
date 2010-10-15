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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.PrefixTermEnum;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.OpenBitSet;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.modules.java.source.usages.ResultConvertor.Stop;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class QueryUtil {
    
    static Collector createBitSetCollector(final BitSet bits) {
        return new BitSetCollector(bits);
    }
    
    static Query createQuery (
            final @NonNull Pair<String,String> termNames,
            final @NonNull String value,
            final @NonNull ClassIndex.NameKind kind) {
        Parameters.notNull("termNames", termNames);     //NOI18N
        Parameters.notNull("termNames.first", termNames.first); //NOI18N
        Parameters.notNull("termNames.second", termNames.second);   //NOI18N
        Parameters.notNull("value", value); //NOI18N
        Parameters.notNull("kind", kind);   //NOI18N
        return createQueryImpl(termNames, value, kind, new StandardQueryFactory());
    }  
    
    static Query createTermCollectingQuery(
            final @NonNull Pair<String,String> termNames,
            final @NonNull String value,
            final @NonNull ClassIndex.NameKind kind) {
        Parameters.notNull("termNames", termNames);     //NOI18N
        Parameters.notNull("termNames.first", termNames.first); //NOI18N
        Parameters.notNull("termNames.second", termNames.second); //NOI18N
        Parameters.notNull("value", value); //NOI18N
        Parameters.notNull("kind", kind);   //NOI18N
        return createQueryImpl(termNames, value, kind, new TCQueryFactory());
    }
    
    private static Query createQueryImpl(
            final @NonNull Pair<String,String> termNames,
            final @NonNull String value,
            final @NonNull ClassIndex.NameKind kind,
            final @NonNull QueryFactory f) {
        switch (kind) {
            case SIMPLE_NAME:
                    return f.createTermQuery(termNames.first, value);
            case PREFIX:
                if (value.length() == 0) {
                    return f.createAllDocsQuery();
                }
                else {
                    return f.createPrefixQuery(termNames.first, value);
                }
            case CASE_INSENSITIVE_PREFIX:
                if (value.length() == 0) {
                    return f.createAllDocsQuery();
                }
                else {
                    return f.createPrefixQuery(termNames.second, value.toLowerCase());
                }
            case CAMEL_CASE:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    return f.createRegExpQuery(termNames.first,createCamelCaseRegExp(value, true), true);
                }
            case CASE_INSENSITIVE_REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    return f.createRegExpQuery(termNames.second, value.toLowerCase(), false);
                }
            case REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    return f.createRegExpQuery(termNames.first, value, true);
                }
            case CAMEL_CASE_INSENSITIVE:
                if (value.length() == 0) {
                    //Special case (all) handle in different way
                    return f.createAllDocsQuery();
                }
                else {
                    final Query pq = f.createPrefixQuery(termNames.second, value.toLowerCase());
                    final Query fq = f.createRegExpQuery(termNames.second, createCamelCaseRegExp(value, false), false);
                    final BooleanQuery result = f.createBooleanQuery();
                    result.add(pq, Occur.SHOULD);
                    result.add(fq, Occur.SHOULD);
                    return result;
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
        
    static Pair<ResultConvertor<Term,String>,Term> createPackageFilter(
            final @NullAllowed String prefix,
            final boolean directOnly) {
        final Term startTerm = new Term (DocumentUtil.FIELD_PACKAGE_NAME, prefix);
        final ResultConvertor<Term,String> filter = new PackageFilter(startTerm, directOnly);
        return Pair.of(filter,startTerm);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Private implementation">
    
    private static String createCamelCaseRegExp(final String camel, final boolean caseSensitive) {
        final StringBuilder sb = new StringBuilder();
        int lastIndex = 0;
        int index;
        do {
            index = findNextUpper(camel, lastIndex + 1);
            String token = camel.substring(lastIndex, index == -1 ? camel.length(): index);
            sb.append(Pattern.quote(caseSensitive ? token : token.toLowerCase()));
            sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
            lastIndex = index;
        } while(index != -1);
        return sb.toString();
    }
    
    private static int findNextUpper(String text, int offset ) {
        for( int i = offset; i < text.length(); i++ ) {
            if ( Character.isUpperCase(text.charAt(i)) ) {
                return i;
            }
        }
        return -1;
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
                                visit(term, docs[i]);
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

        abstract public void visit(Term term, int doc);
    }    
    
    private static abstract class TCFilter extends Filter {
        public abstract void attach (TermCollector collector);
    }
    
    private static abstract class AbstractTCFilter extends TCFilter {
        
        private  TermCollector termCollector;
                
        @Override
        public final BitSet bits(IndexReader reader) throws IOException {
            final FilteredTermEnum enumerator = getTermEnum(reader);
            try {
                final BitSet bitSet = new BitSet(reader.maxDoc());
                new DocumentVisitor() {
                    @Override
                    public void visit(Term term, int doc) {
                        bitSet.set(doc);
                        if (termCollector != null) {
                            termCollector.add(doc, term);
                        }
                    }
                }.generate(reader, enumerator);
                return bitSet;
            } finally {
                enumerator.close();
            }
        }

        @Override
        public final DocIdSet getDocIdSet(IndexReader reader) throws IOException {
            final FilteredTermEnum enumerator = getTermEnum(reader);
            try {
                // if current term in enum is null, the enum is empty -> shortcut
                if (enumerator.term() == null) {
                    return DocIdSet.EMPTY_DOCIDSET;
                }
                // else fill into a OpenBitSet
                final OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
                new DocumentVisitor() {
                    @Override
                    public void visit(Term term, int doc) {
                        bitSet.set(doc);
                        if (termCollector != null) {
                            termCollector.add(doc, term);
                        }
                    }
                }.generate(reader, enumerator);
                return bitSet;
            } finally {
                enumerator.close();
            }
        }
        
        @Override
        public final void attach(final TermCollector tc) {
            this.termCollector = tc;
        }
        
        protected abstract FilteredTermEnum getTermEnum(IndexReader reader) throws IOException;
        
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
    
    private static class RegexpFilter extends AbstractTCFilter {
        
        private final String fieldName;
        private final String startPrefix;
        private final Pattern pattern;
                        
        public RegexpFilter(final String fieldName, final String  regexp, final boolean caseSensitive) {
            this.fieldName = fieldName;
            this.pattern = caseSensitive ? Pattern.compile(regexp) : Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
            this.startPrefix = getStartText(regexp);
        }
        
        protected FilteredTermEnum getTermEnum(final @NonNull IndexReader reader) throws IOException {
            return new RegexpTermEnum(reader, fieldName, pattern, startPrefix);
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
    }
    
    private static class PrefixFilter extends AbstractTCFilter {
        
        protected final Term term;
        
        public PrefixFilter(final @NonNull String fieldName, final @NonNull String prefix) {
            this.term = new Term(fieldName, prefix);
        }
        
        protected FilteredTermEnum getTermEnum(final @NonNull IndexReader reader) throws IOException {
            return new PrefixTermEnum(reader, term);
        }
    }
    
    private static class TermFilter extends PrefixFilter {
                
        public TermFilter (final String fieldName, final String value) {
            super(fieldName, value);
        }

        @Override
        protected FilteredTermEnum getTermEnum(IndexReader reader) throws IOException {
            return new PrefixTermEnum(reader, term) {
                
                private boolean endEnum;
                
                @Override
                protected boolean termCompare(Term term) {
                    if (TermFilter.this.term.field() == term.field() && TermFilter.this.term.text().equals(term.text())) {                                                                              
                        return true;
                    }
                    endEnum = true;
                    return false;
                }

                @Override
                protected boolean endEnum() {
                    return endEnum;
                }
                
                
                
            };
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
    
    private static class PackageFilter implements ResultConvertor<Term, String> {
        
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
    
    private static class TCFilteredQuery extends FilteredQuery implements TermCollector.TermCollecting {        
        private TCFilteredQuery(final Query query, final TCFilter filter) {
            super (query, filter);
        }
        
        @Override
        public void attach(TermCollector collector) {
            ((TCFilter)getFilter()).attach(collector);
        }
    }
    
    private static class TCBooleanQuery extends BooleanQuery implements TermCollector.TermCollecting {
        
        private TermCollector collector;
        
        @Override
        public void attach(TermCollector collector) {
            this.collector = collector;
            if (this.collector != null) {
                attach(this, this.collector);
            }
        }

        @Override
        public Query rewrite(IndexReader reader) throws IOException {
            final Query result =  super.rewrite(reader);
            if (this.collector != null) {
                attach(this,this.collector);
            }
            return result;
        }
        
        private static void attach (final BooleanQuery query, final TermCollector collector) {
            for (BooleanClause clause : query.getClauses()) {
                if (!(clause instanceof TermCollector.TermCollecting)) {
                    throw new IllegalArgumentException();
                }
                ((TermCollector.TermCollecting)clause.getQuery()).attach(collector);
            }
        }
        
    }
    
    private static interface QueryFactory {
        Query createTermQuery(@NonNull String name, @NonNull String value);
        Query createPrefixQuery(@NonNull String name, @NonNull String value);
        Query createRegExpQuery(@NonNull String name, @NonNull String value, boolean caseSensitive);
        Query createAllDocsQuery();
        BooleanQuery createBooleanQuery();
    }
    
    private static class StandardQueryFactory implements QueryFactory {
        
        @Override
        public Query createTermQuery(final @NonNull String name, final @NonNull String value) {
            return new TermQuery(new Term (name, value));
        }
        
        @Override
        public Query createPrefixQuery(final @NonNull String name, final @NonNull String value) {
            final PrefixQuery pq = new PrefixQuery(new Term(name, value));
            pq.setRewriteMethod(PrefixQuery.CONSTANT_SCORE_FILTER_REWRITE);
            return pq;
        }
        
        @Override
        public Query createRegExpQuery(final @NonNull String name, final @NonNull String value, final boolean caseSensitive) {
            return new FilteredQuery(new MatchAllDocsQuery(), new RegexpFilter(name, value, caseSensitive));
        }
        
        @Override
        public Query createAllDocsQuery() {
            return new MatchAllDocsQuery();
        }
        
        @Override
        public BooleanQuery createBooleanQuery() {
            return new BooleanQuery();
        }
    }
    
    private static class TCQueryFactory implements QueryFactory {
        
        @Override
        public Query createTermQuery(final @NonNull String name, final @NonNull String value) {
            return new TCFilteredQuery(new MatchAllDocsQuery(), new TermFilter(name,value));
        }
        
        @Override
        public Query createPrefixQuery(final @NonNull String name, final @NonNull String value) {
            return new TCFilteredQuery(new MatchAllDocsQuery(), new PrefixFilter(name, value));
        }
        
        @Override
        public Query createRegExpQuery(final @NonNull String name, final @NonNull String value, final boolean caseSensitive) {
            return new TCFilteredQuery(new MatchAllDocsQuery(), new RegexpFilter(name, value, caseSensitive));
        }
        
        @Override
        public Query createAllDocsQuery() {
            throw new IllegalArgumentException ();
        }
        
        @Override
        public BooleanQuery createBooleanQuery() {
            return new TCBooleanQuery();
        }
    }
    //</editor-fold>
            
}
