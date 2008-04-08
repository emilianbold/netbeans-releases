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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xpath.ext;

import java.util.HashMap;
import org.netbeans.modules.xml.xpath.ext.metadata.CoreFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.CoreFunctionMetadataImpl.*;

/**
 * Types of the core XPath functions.
 * 
 * @author nk160297
 * @version 
 */
public enum CoreFunctionType { 
    FUNC_LAST(new LastFuncMetadata()), // Function code: last
    FUNC_POSITION(new PositionFuncMetadata()), // Function code: position
    FUNC_COUNT(new CountFuncMetadata()), // Function code: count
    FUNC_ID(new IdFuncMetadata()), // Function code: id
    FUNC_LOCAL_NAME(new LocalNameFuncMetadata()), // Function code: local-name
    FUNC_NAMESPACE_URI(new NamespaceUriFuncMetadata()), // Function code: namespace-uri
    FUNC_NAME(new NameFuncMetadata()), // Function code: name
    FUNC_STRING(new StringFuncMetadata()), // Function code: string
    FUNC_CONCAT(new ConcatFuncMetadata()), // Function code: concat
    FUNC_STARTS_WITH(new StartsWithFuncMetadata()), // Function code: starts-with
    FUNC_CONTAINS(new ContainsFuncMetadata()), // Function code: contains
    FUNC_SUBSTRING_BEFORE(new SubstringBeforeFuncMetadata()), // Function code: substring-before
    FUNC_SUBSTRING_AFTER(new SubstringAfterFuncMetadata()), // Function code: substring-after
    FUNC_SUBSTRING(new SubstringFuncMetadata()), // Function code: substring
    FUNC_STRING_LENGTH(new StringLengthFuncMetadata()), // Function code: string-length
    FUNC_NORMALIZE_SPACE(new NormalizeSpaceFuncMetadata()), // Function code: normalize-space
    FUNC_TRANSLATE(new TranslateFuncMetadata()), // Function code: translate
    FUNC_BOOLEAN(new BooleanFuncMetadata()), // Function code: boolean
    FUNC_NOT(new NotFuncMetadata()), // Function code: not
    FUNC_TRUE(new TrueFuncMetadata()), // Function code: true
    FUNC_FALSE(new FalseFuncMetadata()), // Function code: false
    FUNC_LANG(new LangFuncMetadata()), // Function code: lang
    FUNC_NUMBER(new NumberFuncMetadata()), // Function code: number
    FUNC_SUM(new SumFuncMetadata()), // Function code: sum
    FUNC_FLOOR(new FloorFuncMetadata()), // Function code: floor
    FUNC_CEILING(new CeilingFuncMetadata()), // Function code: ceiling
    FUNC_ROUND(new RoundFuncMetadata()); // Function code: round
    //
    // Following functions are extenstion but they are resolved by JXPath. 
    // So they are considered as core functions here.
    // COMMENTED BECAUSE THE RUNTIME DOESN'T SUPPORT THEM!
    // FUNC_NULL(new NullFuncMetadata()), // Function code: null
    // FUNC_KEY(new KeyFuncMetadata()), // Function code: key
    // FUNC_FORMAT_NUMBER(new FormatNumberFuncMetadata()); // Function code: format-number


    private CoreFunctionMetadata myMetadata;

    private CoreFunctionType(CoreFunctionMetadata metadata) {
        myMetadata = metadata;
    }
    
    public CoreFunctionMetadata getMetadata() {
        return myMetadata;
    }

    //==========================================================================
    
    private static HashMap<String, CoreFunctionType> sNameTypeMap;
    
    public static synchronized CoreFunctionType getTypeByName(String funcName) {
        if (sNameTypeMap == null) {
            sNameTypeMap = new HashMap<String, CoreFunctionType>();
            //
            for(CoreFunctionType type : CoreFunctionType.values()) {
                String typeName = type.getMetadata().getName();
                sNameTypeMap.put(typeName, type);
            }
        }
        //
        return sNameTypeMap.get(funcName);
    }
}
