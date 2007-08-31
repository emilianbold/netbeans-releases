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

package org.netbeans.modules.java.source.usages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 *
 * @author Tomas Zezula
 */
class DocumentUtil {
    
    private static final String ROOT_NAME="/";                           //NOI18N    
    private static final String FIELD_RESOURCE_NAME = "resName";        //NOI18N
    private static final String FIELD_BINARY_NAME = "binaryName";         //NOI18N
    private static final String FIELD_PACKAGE_NAME = "packageName";     //NOI18N
    private static final String FIELD_TIME_STAMP = "timeStamp";         //NOI18N
    private static final String FIELD_REFERENCES = "references";        //NOI18N
    private static final String FIELD_SIMPLE_NAME = "simpleName";       //NOI18N
    private static final String FIELD_CASE_INSENSITIVE_NAME = "ciName"; //NOI18N
    private static final String FIELD_SOURCE = "source";                //NOI18N
    
    private static final char NO = '-';                                 //NOI18N
    private static final char YES = '+';                                //NOI18N
    private static final char WILDCARD = '?';                           //NOI18N
    private static final char PKG_SEPARATOR = '.';                      //NOI18N
    
    private static final char EK_CLASS = 'C';                           //NOI18N
    private static final char EK_INTERFACE = 'I';                       //NOI18N
    private static final char EK_ENUM = 'E';                            //NOI18N
    private static final char EK_ANNOTATION = 'A';                      //NOI18N
        
    private static final int SIZE = ClassIndexImpl.UsageType.values().length;    
    private static final char[] MASK_ANY_USAGE = new char[SIZE];        
    
    static {
        Arrays.fill(MASK_ANY_USAGE, WILDCARD);    //NOI18N
    }
    
    private DocumentUtil () {
    }
    
    
    //Document field getters
    public static String getBinaryName (final Document doc) {
        return getBinaryName(doc, null);
    }
    
    public static String getBinaryName (final Document doc, final ElementKind[] kind) {
        assert doc != null;
        final Field pkgField = doc.getField(FIELD_PACKAGE_NAME);
        final Field snField = doc.getField (FIELD_BINARY_NAME);
        if (snField == null) {
            return null;
        }
        final String tmp = snField.stringValue();
        final String snName = tmp.substring(0,tmp.length()-1);
        if (kind != null) {
            assert kind.length == 1;
            kind[0] = decodeKind (tmp.charAt(tmp.length()-1));
        }
        if (pkgField == null) {
            return snName;
        }        
        String pkg = pkgField.stringValue();
        if (pkg.length() == 0) {
            return snName;
        }
        return  pkg + PKG_SEPARATOR + snName;   //NO I18N
    }
    
    public static String getSimpleBinaryName (final Document doc) {
        assert doc != null;
        Field field = doc.getField(FIELD_BINARY_NAME);
        if (field == null) {
            return null;
        }
        else {
            return field.stringValue();
        }
    }
    
    public static String getSourceName (final Document doc) {
        assert doc != null;
        Field field = doc.getField(FIELD_SOURCE);
        if (field == null) {
            return null;
        }
        else {
            return field.stringValue();
        }
    }
    
    public static String getPackageName (final Document doc) {
        assert doc != null;
        Field field = doc.getField(FIELD_PACKAGE_NAME);
        return field == null ? null : field.stringValue();
    }
    
    static String getRefereneType (final Document doc, final String className) {
        assert doc != null;
        assert className != null;
        Field[] fields = doc.getFields(FIELD_REFERENCES);
        assert fields != null;
        for (Field field : fields) {
            final String rawUsage = field.stringValue();            
            final int rawUsageLen = rawUsage.length();
            assert rawUsageLen>SIZE;
            final int index = rawUsageLen - SIZE;
            final String usageName = rawUsage.substring(0,index);
            final String map = rawUsage.substring (index);
            if (className.equals(usageName)) {
                return map;
            }
        }
        return null;
    }
    
    public static List<String> getReferences (final Document doc) {
        assert doc != null;
        Field[] fields = doc.getFields(FIELD_REFERENCES);
        assert fields != null;
        List<String> result = new ArrayList<String> (fields.length);
        for (Field field : fields) {
            result.add (field.stringValue());
        }
        return result;
    }
    
    public static long getTimeStamp (final Document doc) throws java.text.ParseException {
        assert doc != null;
        Field field = doc.getField(FIELD_TIME_STAMP);
        assert field != null;
        String data = field.stringValue();
        assert data != null;
        return DateTools.stringToTime(data);
    }
    
    
    //Term and query factories
    public static Query binaryNameQuery (final String resourceName) {
        BooleanQuery query = new BooleanQuery ();
        int index = resourceName.lastIndexOf(PKG_SEPARATOR);  // NOI18N
        String pkgName, sName;
        if (index < 0) {
            pkgName = "";   // NOI18N
            sName = resourceName;
        }
        else {
            pkgName = resourceName.substring(0,index);
            sName = resourceName.substring(index+1);
        }
        sName = sName + WILDCARD;
        query.add (new TermQuery (new Term (FIELD_PACKAGE_NAME, pkgName)),BooleanClause.Occur.MUST);
        query.add (new WildcardQuery (new Term (FIELD_BINARY_NAME, sName)),BooleanClause.Occur.MUST);
        return query;
    }
    
    public static Query binaryContentNameQuery (final String resourceName) {        
        int index = resourceName.lastIndexOf(PKG_SEPARATOR);  // NOI18N
        String pkgName, sName;
        if (index < 0) {
            pkgName = "";   // NOI18N
            sName = resourceName;
        }
        else {
            pkgName = resourceName.substring(0,index);
            sName = resourceName.substring(index+1);
        }
        BooleanQuery query = new BooleanQuery ();
        BooleanQuery subQuery = new BooleanQuery();
        subQuery.add (new WildcardQuery (new Term (FIELD_BINARY_NAME, sName + WILDCARD)),BooleanClause.Occur.SHOULD);
        subQuery.add (new PrefixQuery (new Term (FIELD_BINARY_NAME, sName + '$')),BooleanClause.Occur.SHOULD);
        query.add (new TermQuery (new Term (FIELD_PACKAGE_NAME, pkgName)),BooleanClause.Occur.MUST);
        query.add (subQuery,BooleanClause.Occur.MUST);
        return query;
    }
    
    
    public static Term rootDocumentTerm () {
        return new Term (FIELD_RESOURCE_NAME,ROOT_NAME);
    }
    
    public static Term simpleBinaryNameTerm (final String resourceFileName) {
        assert resourceFileName != null;
        return new Term (FIELD_BINARY_NAME, resourceFileName);
    }   
    
    public static Term packageNameTerm (final String packageName) {
        assert packageName != null;
        return new Term (FIELD_PACKAGE_NAME, packageName);
    }
    
    public static Term referencesTerm (String resourceName, final Set<ClassIndexImpl.UsageType> usageType) {
        assert resourceName  != null;
        if (usageType != null) {
            resourceName = encodeUsage (resourceName, usageType, WILDCARD).toString();
        }
        else {
            StringBuilder sb = new StringBuilder (resourceName);
            sb.append(MASK_ANY_USAGE);
            resourceName = sb.toString();
        }
        return new Term (FIELD_REFERENCES, resourceName);
    }
    
    public static Term simpleNameTerm (final String resourceSimpleName) {
        assert resourceSimpleName != null;
        return new Term (FIELD_SIMPLE_NAME, resourceSimpleName);
    }
    
    public static Term caseInsensitiveNameTerm (final String caseInsensitiveName) {
        assert caseInsensitiveName != null;
        return new Term (FIELD_CASE_INSENSITIVE_NAME, caseInsensitiveName);
    }    
    
    //Factories for lucene document
    public static Document createDocument (final String binaryName, final long timeStamp, List<String> references, String source) {
        assert binaryName != null;
        assert references != null;
        int index = binaryName.lastIndexOf(PKG_SEPARATOR);  //NOI18N
        String fileName, pkgName, simpleName, caseInsensitiveName;
        if (index<0) {
            fileName = binaryName;
            pkgName = "";                           //NOI18N
        }
        else {
            fileName = binaryName.substring(index+1);
            pkgName = binaryName.substring(0,index);
        }
        index = fileName.lastIndexOf('$');  //NOI18N
        if (index<0) {
            simpleName = fileName.substring(0, fileName.length()-1);
        }
        else {
            simpleName = fileName.substring(index+1,fileName.length()-1);
        }
        caseInsensitiveName = simpleName.toLowerCase();         //XXX: I18N, Locale
        Document doc = new Document ();        
        Field field = new Field (FIELD_BINARY_NAME,fileName,Field.Store.YES, Field.Index.UN_TOKENIZED);
        doc.add (field);
        field = new Field (FIELD_PACKAGE_NAME,pkgName,Field.Store.YES, Field.Index.UN_TOKENIZED);
        doc.add (field);
        field = new Field (FIELD_TIME_STAMP,DateTools.timeToString(timeStamp,DateTools.Resolution.MILLISECOND),Field.Store.YES,Field.Index.NO);
        doc.add (field);
        field = new Field (FIELD_SIMPLE_NAME,simpleName, Field.Store.YES, Field.Index.UN_TOKENIZED);
        doc.add (field);
        field = new Field (FIELD_CASE_INSENSITIVE_NAME, caseInsensitiveName, Field.Store.YES, Field.Index.UN_TOKENIZED);
        doc.add (field);
        for (String reference : references) {
            field = new Field (FIELD_REFERENCES,reference,Field.Store.YES,Field.Index.UN_TOKENIZED);
            doc.add(field);
        }
        if (source != null) {
            field = new Field (FIELD_SOURCE,source,Field.Store.YES,Field.Index.UN_TOKENIZED);
            doc.add(field);
        }
        return doc;
    }
    
    public static Document createRootTimeStampDocument (final long timeStamp) {
        Document doc = new Document ();
        Field field = new Field (FIELD_RESOURCE_NAME, ROOT_NAME,Field.Store.YES, Field.Index.UN_TOKENIZED);
        doc.add (field);        
        field = new Field (FIELD_TIME_STAMP,DateTools.timeToString(timeStamp,DateTools.Resolution.MILLISECOND),Field.Store.YES,Field.Index.NO);
        doc.add (field);
        return doc;
    }
    
    // Functions for encoding and decoding of UsageType
    public static StringBuilder createUsage (final String className) {
        Set<ClassIndexImpl.UsageType> EMPTY = Collections.emptySet();
        return encodeUsage (className, EMPTY,NO);
    }
    
    public static void addUsage (final StringBuilder rawUsage, final ClassIndexImpl.UsageType type) {
        assert rawUsage != null;
        assert type != null;
        final int rawUsageLen = rawUsage.length();
        final int startIndex = rawUsageLen - SIZE;
        rawUsage.setCharAt (startIndex + type.getOffset(),YES);
    }
    
    public static String encodeUsage (final String className, final Set<ClassIndexImpl.UsageType> usageTypes) {
        return encodeUsage (className, usageTypes, NO).toString();
    }
    
    private static StringBuilder encodeUsage (final String className, final Set<ClassIndexImpl.UsageType> usageTypes, char fill) {
        assert className != null;
        assert usageTypes != null;
        StringBuilder builder = new StringBuilder ();
        builder.append(className);
        char[] map = new char [SIZE];
        Arrays.fill(map,fill);
        for (ClassIndexImpl.UsageType usageType : usageTypes) {
            int offset = usageType.getOffset ();
            assert offset >= 0 && offset < SIZE;
            map[offset] = YES;
        }
        builder.append(map);
        return builder;
    }
    
    public static String encodeUsage (final String className, final String usageMap) {
        assert className != null;
        assert usageMap != null;
        StringBuilder sb = new StringBuilder ();
        sb.append(className);
        sb.append(usageMap);
        return sb.toString();
    }
    
    public static String decodeUsage (final String rawUsage, final Set<ClassIndexImpl.UsageType> usageTypes) {
        assert rawUsage != null;
        assert usageTypes != null;
        assert usageTypes.isEmpty();
        final int rawUsageLen = rawUsage.length();
        assert rawUsageLen>SIZE;
        final int index = rawUsageLen - SIZE;
        final String className = rawUsage.substring(0,index);
        final String map = rawUsage.substring (index);
        for (ClassIndexImpl.UsageType usageType : ClassIndexImpl.UsageType.values()) {
            if (map.charAt(usageType.getOffset()) == YES) {
                usageTypes.add (usageType);
            }
        }        
        return className;
    }
    
    public static ElementKind decodeKind (char kind) {
        switch (kind) {
            case EK_CLASS:
                return ElementKind.CLASS;
            case EK_INTERFACE:
                return ElementKind.INTERFACE;
            case EK_ENUM:
                return ElementKind.ENUM;
            case EK_ANNOTATION:
                return ElementKind.ANNOTATION_TYPE;
            default:
                throw new IllegalArgumentException ();
        }
    }
    
    public static char encodeKind (ElementKind kind) {
        switch (kind) {
            case CLASS:
                return EK_CLASS;
            case INTERFACE:
                return EK_INTERFACE;
            case ENUM:
                return EK_ENUM;
            case ANNOTATION_TYPE:
                return EK_ANNOTATION;
            default:
                throw new IllegalArgumentException ();
        }
    }       
    
    
}
