/* The contents of this file are subject to the terms of the Common Development
/* and Distribution License (the License). You may not use this file except in
/* compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
/* or http://www.netbeans.org/cddl.txt.
/*
/* When distributing Covered Code, include this CDDL Header Notice in each file
/* and include the License file at http://www.netbeans.org/cddl.txt.
/* If applicable, add the following below the CDDL Header, with the fields
/* enclosed by brackets [] replaced by your own identifying information:
/* "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
/* Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.syntax.dom;

import org.netbeans.editor.TokenItem;

/**
 * Library of shared complexities.
 *
 * @author  Petr Kuzel
 * @author  asgeir@dimonsoftware.com
 */
public class Util {

    public static String[] knownEntityStrings = {"&lt;", "&gt;", "&apos;", "&quot;", "&amp;"};

    public static char[] knownEntityChars = {'<', '>', '\'', '"', '&'};

    /**
     * Handle fuzziness of attribute end detection.
     * @return TokenItem after attribute value or null.
     */
    public static TokenItem skipAttributeValue(TokenItem attribute, char delim) {
        TokenItem next = attribute;
        for (; next != null; next = next.getNext()) {
            String image = next.getImage();
            if (image.endsWith("" + delim)) {
                return next.getNext();
            }
        }
        return null;
    }
    
    /**
     * This method looks for '<' and '>' characters in attributes values and
     * returns whitespace-stripped substring which does not contain '<' or '>'.
     * This method should be used to calculate an attribute value which has
     * not currently been closed.
     * @param attributeValue an original attribute value
     * @return the same value of stripped substring of it.
     */
    public static String actualAttributeValue(String attributeValue) {
        int ltIndex = attributeValue.indexOf('<');
        int gtIndex = attributeValue.indexOf('>');
        int firstUnwantedIndex = -1;
        if (gtIndex != -1) {
            if (ltIndex != -1 && ltIndex < gtIndex) {
                firstUnwantedIndex = ltIndex;
            } else {
                firstUnwantedIndex = gtIndex;
            }
        } else {
            firstUnwantedIndex = ltIndex;
        }
        
        if (firstUnwantedIndex != -1) {
            char charAtIndex = attributeValue.charAt(firstUnwantedIndex);
            while (charAtIndex == ' ' || charAtIndex == '\t' || charAtIndex  == '\n' ||
            charAtIndex == '\r' || charAtIndex == '<' || charAtIndex == '>') {
                firstUnwantedIndex--;
                if (firstUnwantedIndex < 0) {
                    break;
                }
                charAtIndex = attributeValue.charAt(firstUnwantedIndex);
            }
            
            return attributeValue.substring(0, firstUnwantedIndex + 1);
        } else {
            return attributeValue;
        }
    }
    
    /**
     * Replaces "&lt;", "&gt;", "&apos;", "&quot;", "&amp;" with
     * '<', '>', '\'', '"', '&'.
     * @param a string that may contain &lt;", "&gt;", "&apos;", "&quot;" and "&amp;"
     * @return a string that may contain '<', '>', '\'', '"', '&'.
     */
    public static String replaceEntityStringsWithChars(String value) {
        StringBuffer buf = new StringBuffer(value);
        for (int entity = 0; entity < knownEntityStrings.length; entity++) {
            String curEntityString = knownEntityStrings[entity];
            int indexOfEntity = buf.toString().indexOf(curEntityString);
            while (indexOfEntity != -1) {
                buf.replace(indexOfEntity, indexOfEntity + curEntityString.length(),
                new String(new char[]{knownEntityChars[entity]}));
                indexOfEntity = buf.toString().indexOf(curEntityString);
            }
        }
        
        return buf.toString();
    }
    
    /**
     * Replaces '<', '>', '\'', '"', '&' with
     * "&lt;", "&gt;", "&apos;", "&quot;", "&amp;".
     * @param a string that may contain '<', '>', '\'', '"', '&'.
     * @return a string that may contain &lt;", "&gt;", "&apos;", "&quot;" and "&amp;"
     */
    public static String replaceCharsWithEntityStrings(String value) {
    	if (value == null) {
    		return null;
    	}
        StringBuffer replBuf = new StringBuffer(value.length());
        for (int ind = 0; ind < value.length(); ind++) {
            boolean charReplaced = false;
            char curChar = value.charAt(ind);
            for (int entity = 0; entity < knownEntityChars.length; entity++) {
                if (curChar == knownEntityChars[entity]) {
                    replBuf.append(knownEntityStrings[entity]);
                    charReplaced = true;
                    break;
                }
            }
            
            if (!charReplaced) {
                replBuf.append(curChar);
            }
        }
        
        return replBuf.toString();
    }
    
}
