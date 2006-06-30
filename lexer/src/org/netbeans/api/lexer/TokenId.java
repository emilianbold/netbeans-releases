/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.lexer;

import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.AbstractList;
import java.util.ArrayList;

/**
 * Identifier of a token (could also be called a token-type).
 * <BR>It is not a token, because in general it does not contain
 * the text (also called image) of the token.
 * <BR>For example "var1", "var2" aret tokens (token instances)
 * that occur in a file or document
 * while JavaLanguage.IDENTIFIER is tokenId for the above
 * token instances.
 * 
 *
 * <P>TokenIds are typically defined
 * as public static final constants in subtypes
 * of {@link Language}.
 * <BR>All tokenIds in a language must have both
 * unique intId and name.
 *
 * <P>Detailed information and rules for naming can be found
 * in <A href="http://lexer.netbeans.org/doc/token-id-naming.html">TokenId Naming</A>.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenId {
    
    public static final Comparator NAME_COMPARATOR = NameComparator.INSTANCE;
    
    public static final Comparator INT_ID_COMPARATOR = IntIdComparator.INSTANCE;
    
    private static final List EMPTY_CATEGORY_NAMES_LIST
        = new ReadonlyList(new String[0]);
    
    /** Interned category names lists cache */
    private static final List categoryCache = new ArrayList();


    private final String name;
    
    private final int intId;
    
    private final List categoryNamesList;
    
    private final SampleTextMatcher sampleTextMatcher;
    
    public TokenId(String name, int intId) {
        this(name, intId, null, null);
    }
    
    public TokenId(String name, int intId, String[] categoryNames) {
        this(name, intId, categoryNames, null);
    }

    /** Construct new TokenId.
     * @param name non-null name of the TokenId unique among the tokenIDs
     *  in the language where this TokenId is defined.
     *  It can be retrieved by {@link #getName()}.
     * @param intId integer identification unique among the tokenIDs
     *  in the language where this TokenId is defined. It can
     *  It can be retrieved by {@link #getIntId()}.
     * @param categoryNames names of categories into which this TokenId belongs.
     *  It can be retrieved by {@link #getCategoryNames()}. It can be null
     *  to indicate that tokenId does not belong to any token category.
     * @param sampleTextMatcher matcher to one or more sample texts
     *  or null if there is no matcher (the tokens with this tokenId have always
     *  variable text).
     *
     */
    public TokenId(String name, int intId, String[] categoryNames,
    SampleTextMatcher sampleTextMatcher) {
        
        if (name == null) {
            throw new NullPointerException("TokenId name cannot be null");
        }

        if (intId < 0) {
            throw new IllegalArgumentException("intId=" + intId
                + " of token=" + name + " is < 0");
        }
        
        if (name.indexOf('.') >= 0) {
            throw new IllegalArgumentException("TokenId name="
                + name + " cannot contain '.' character.");
        }

        this.name = name;
        this.intId = intId;
        this.categoryNamesList = internCategoryNames(categoryNames);
        this.sampleTextMatcher = sampleTextMatcher;
    }
    
    /**
     * Get name of this tokenId.
     * @return the unique name of the TokenId. The name must be unique
     * among other TokenId instances of the language where
     * it is defined. The name should consist of
     * lowercase alphanumeric letters and hyphens only.
     *
     * <P>It can serve for several purposes such as finding
     * a possible style information for the given token.
     * The name is always non-null.
     */
    public String getName() {
        return name;
    }

    /**
     * Get integer identification of this tokenId.
     * @return unique numeric identification of this TokenId.
     *  <BR>IntId must be a non-negative
     *  integer unique among all the tokenIDs inside the language
     *  where it is declared.
     *  <BR>The intIds are usually defined and adopted from lexer
     *  generator tool that generates the lexer for the given language.
     *  <BR>The ids do not have to be consecutive and they should
     *  not be unnecessarily high (e.g. 1000) because
     *  indexing arrays are constructed based on the id values
     *  so the length of the indexing array corresponds
     *  to the highest intId of all the tokenIDs declared
     *  for the particular language.
     *  <BR>The intIds allow more efficient use
     *  of the tokenIds in switch-case statements.
     */
    public int getIntId() {
        return intId;
    }

    /**
     * Get names of all categories to which this tokenId belongs.
     * @return non-null list of category names to which this token belongs.
     * They can be e.g. "operator", "separator" etc.
     * "error" category marks errorneous lexical construction.
     * "incomplete" category marks incomplete tokens such
     * as unclosed string-literal or block-comment.
     * <BR>If the token belongs to no categories
     * an empty list will be returned.
     * <BR>The order or the list items corresponds
     * to the order of the items given in TokenId's constructor.
     * <BR>Although there is no strict rule the first
     * token category in the list should be
     * the most "natural" one for the given tokenId.
     */
    public List getCategoryNames() {
        return categoryNamesList;
    }
    
    /**
     * Get matcher that allows to match text found by lexer
     * to a set of sample texts.
     * @return a valid matcher if the tokens with this tokenId
     * have some sample text(s) (e.g. keywords or operators)
     * or null if the text of the tokens always varies.
     */
    public SampleTextMatcher getSampleTextMatcher() {
        return sampleTextMatcher;
    }
    
    /** Get possibly reused copy of the categoryNames list. */
    private List internCategoryNames(String[] categoryNames) {
        List ret = EMPTY_CATEGORY_NAMES_LIST;
        
        if (categoryNames != null && categoryNames.length > 0) {
            int index = Collections.binarySearch(categoryCache,
                categoryNames, CategoryNamesComparator.INSTANCE);
            
            if (index < 0) { // not found
                index = -index - 1;
                categoryCache.add(index,
                    new ReadonlyList((String[])categoryNames.clone()));
            }
            
            ret = (List)categoryCache.get(index);
        }
        
        return ret;
    }
    
    public String toString() {
        return getName() + "[" + getIntId() + "]";
    }
    
    public String toStringDetail() {
        StringBuffer sb = new StringBuffer();
        sb.append(toString());
        if (categoryNamesList.size() > 0) {
            sb.append(", cats=");
            sb.append(categoryNamesList.toString());
        }
        if (sampleTextMatcher != null) {
            sb.append(", sampleTextMatcher=");
            sb.append(sampleTextMatcher);
        }
        
        return sb.toString();
    }
    
    /**
     * Dump current cateogory lists cache into string
     * for debugging purposes.
     */
    public static String categoryCacheToString() {
        return categoryCache.toString();
    }
    
    static final class ReadonlyList extends AbstractList {
        
        Object[] objs;

        ReadonlyList(Object[] objs) {
            this.objs = objs;
        }

        public int size() {
            return objs.length;
        }

        public Object get(int index) {
            return objs[index];
        }
        
    }
    
    private static final class CategoryNamesComparator implements Comparator {
        
        static Comparator INSTANCE = new CategoryNamesComparator();
     
        public int compare(Object o1, Object o2) {
            Object[] o1Array = (o1 instanceof List)
                ? ((ReadonlyList)o1).objs
                : (Object[])o1;

            Object[] o2Array = (o2 instanceof List)
                ? ((ReadonlyList)o2).objs
                : (Object[])o2;

            int o1ArrayLength = o1Array.length;
            int lengthDiff  = o1ArrayLength - o2Array.length;
            
            if (lengthDiff != 0) {
                return lengthDiff;
            }
            
            for (int i = 0; i < o1ArrayLength; i++) {
                int diff = ((String)o1Array[i]).compareTo(o2Array[i]);
                if (diff != 0) {
                    return diff;
                }
            }
            
            return 0;
        }
    }
    
    private static final class NameComparator implements Comparator {
        
        static final Comparator INSTANCE = new NameComparator();
        
        public int compare(Object o1, Object o2) {
            return ((TokenId)o1).getName().compareTo(((TokenId)o2).getName());
        }
    }

    private static final class IntIdComparator implements Comparator {
        
        static final Comparator INSTANCE = new IntIdComparator();
        
        public int compare(Object o1, Object o2) {
            return ((TokenId)o1).getIntId() - ((TokenId)o2).getIntId();
        }
    }

}

