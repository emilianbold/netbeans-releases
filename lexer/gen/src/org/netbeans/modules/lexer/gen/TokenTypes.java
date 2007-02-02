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

package org.netbeans.modules.lexer.gen;

import java.lang.reflect.Field;
import java.lang.SecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import org.netbeans.modules.lexer.gen.util.LexerGenUtilities;

/**
 * The lexer generators often generate a class or interface
 * that contains integer fields of token types
 * named e.g. xxxConstants or xxxTokenTypes etc.
 * <BR>The <CODE>TokenConstants</CODE> class encapsulates the information
 * contained in such token types class.
 * <P>The reflection is used to collect
 * the "public static final int" fields in the token types class.
 * All these fields are collected but subclasses
 * may wish to hide some of the fields (e.g. some fields
 * may be related to states of an automaton instead of token types
 * identification).
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TokenTypes {
    
    private final Class tokenTypesClass;
    
    private boolean inspected;

    /** Map of [tokenTypeName, tokenTypeValue] */
    protected final Map name2value = new HashMap();
    
    /** Map of [tokenTypeValue, tokenTypeName] */
    protected final Map value2name = new HashMap();
    
    public TokenTypes(Class tokenTypesClass) {
        this.tokenTypesClass = tokenTypesClass;
    }
    
    /**
     * Called by <CODE>LanguageData.registerTokenTypes()</CODE>
     * to update the language data into which it's being registered.
     * By default it adds mutable token ids that correspond
     * to the constants discovered in token types.
     * Can be overriden by subclasses to provide some more functionality.
     */
    protected void updateData(LanguageData languageData) {
        inspect();

        for (Iterator it = tokenTypeNamesIterator(); it.hasNext();) {
            String tokenTypeName = (String)it.next();
            MutableTokenId id = languageData.findIdByTokenTypeName(tokenTypeName);
            
            if (id == null) {
                String idName = LexerGenUtilities.idToLowerCase(tokenTypeName);
                id = languageData.newId(idName);
                id.updateByTokenType(tokenTypeName); // updateId() called automatically

            } else {
                updateId(id);
            }
        }
    }
    
    /**
     * Update a newly created or an existing token-id by the information
     * contained in this token-types.
     * The passed token-id already has tokenTypeName
     * filled in.
     */
    protected void updateId(MutableTokenId id) {
        String tokenTypeName = id.getTokenTypeName();
        if (tokenTypeName != null) { // no associated tokenTypeName
            Integer value = getTokenTypeValue(tokenTypeName);
            if (value == null) {
                throw new IllegalArgumentException("tokenTypeName=" + tokenTypeName
                    + " is not declared in " + getTokenTypesClass().getName());
            }

            // assign intId
            id.setIntId(value.intValue());
        }
    }
    
    public Class getTokenTypesClass() {
        return tokenTypesClass;
    }
    
    /**
     * @return Integer value of the static field with the given name
     *  or null if the field does not exist.
     */
    public Integer getTokenTypeValue(String tokenTypeName) {
        inspect();

        return (Integer)name2value.get(tokenTypeName);
    }

    public String getTokenTypeName(int tokenTypeValue) {
        inspect();

        return (String)value2name.get(new Integer(tokenTypeValue));
    }

    /**
     * @return all the field names 
     */
    public Iterator tokenTypeNamesIterator() {
        inspect();

        return name2value.keySet().iterator();
    }
    
    
    public int findMaxTokenTypeValue() {
        inspect();

        int maxValue = 0;
        for (Iterator it = value2name.keySet().iterator(); it.hasNext();) {
            Integer i = (Integer)it.next();
            maxValue = Math.max(maxValue, i.intValue());
        }
        return maxValue;
    }
            
    /** Inspect the token types class.
     * This method can be overriden by children if necessary.
     * The method goes through the class
     * and puts the [field-name, integer-constant-value]
     * for all the static fields into the info map.
     * The <CODE>null</CODE> key is mapped to maximum constant value
     * found in the token types class.
     * The <CODE>List.class</CODE> key is mapped to the list
     * of all the field names in the order in which they
     * were found in the token types class.
     * @return true if the inspection was really done
     *  or false if the inspection was already done previously.
     */
    protected boolean inspect() {
        if (inspected) {
            return false;
        }
        inspected = true;

        try {
            Field[] fields = getTokenTypesClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                if (f.getType() == int.class) {
                    int value = f.getInt(null);
                    String fieldName = f.getName();
                    if (isAccepted(fieldName, value)) {
                        Integer valueInteger = new Integer(value);
                        name2value.put(fieldName, valueInteger);
                        value2name.put(valueInteger, fieldName);
                    }
                }
            }
            
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.toString());

        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.toString());
        }
        
        return true; // inspection really done
    }

    /**
     * Whether it's ok to add the given field name to the list
     * of the [tokenTypeName, Integer] pairs.
     * <BR>Subclasses can exclude some field(s) if necessary.
     */
    protected boolean isAccepted(String tokenTypeName, int tokenTypeValue) {
        return true;
    }

}

