/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.lib.collab.util;

import java.io.UnsupportedEncodingException;
import java.lang.*;
import java.util.*;
import java.text.*;
import org.jabberstudio.jso.JID;
import org.netbeans.lib.collab.xmpp.JIDUtil;


/**
 * String Utility class for performing common String operations such as sort,
 * substitution, etc...
 *
 */
public class StringUtility {

    public static final String UTIL_PROVIDER =
            "org.netbeans.lib.collab.xmpp.JIDUtil.Provider";
    public static final String LEGACY_PROVIDER =
            "org.netbeans.lib.collab.xmpp.util.LegacyJIDUtil";
    public static final String XEP106_PROVIDER =
            "org.netbeans.lib.collab.xmpp.util.XEP106JIDUtil";

    /**
     * platform-specific line separator
     */
    public final static String lineSeparator = System.getProperty("line.separator");
    private final static Collator collator = Collator.getInstance();

    /**
     *
     *
     * @param
     */
    final static public String getBooleanString(boolean b){
        if(b)  return "true";
        return "false";
    }
    
    
    /**
     *
     *
     * @param input string
     */
    final static public boolean getBoolean(String str) throws IllegalArgumentException {
        if(str.equalsIgnoreCase("false") ||
	   str.equalsIgnoreCase("n") ||
	   str.equalsIgnoreCase("no") ||
	   str.equalsIgnoreCase("deny") ||
	   str.equalsIgnoreCase("0") ||
	   str.equalsIgnoreCase("off") ) {
            return false;
        } else if (str.equalsIgnoreCase("true") ||
		   str.equalsIgnoreCase("y") ||
		   str.equalsIgnoreCase("yes") ||
		   str.equalsIgnoreCase("allow") ||
		   str.equalsIgnoreCase("1") ||
		   str.equalsIgnoreCase("on")) {
            return true;
        } else {
            throw new IllegalArgumentException("Not a boolean: " + str);
        }
    }
    
    
    
    /**
     *
     *
     * @param defaultValue value used if the input string does not map
     * to a boolean value
     * @param input string
     */
    final static public boolean getBoolean(String str, boolean defaultValue) {
        if (str == null) {
            return defaultValue;
        } else if (str.equalsIgnoreCase("false") ||
	   str.equalsIgnoreCase("n") ||
	   str.equalsIgnoreCase("no") ||
	   str.equalsIgnoreCase("deny") ||
	   str.equalsIgnoreCase("off") ) {
            return false;
        } else if (str.equalsIgnoreCase("true") ||
		   str.equalsIgnoreCase("y") ||
		   str.equalsIgnoreCase("yes") ||
		   str.equalsIgnoreCase("allow") ||
		   str.equalsIgnoreCase("on")) {
            return true;
        } else {
            return defaultValue;
        }
    }
    
        
    /**
     *
     *
     * @param
     */
    //replaces all occurences of string a with string b
    final static public String replaceString(String replace, String with, String in){
        int start = 0;
        int foundAt = in.indexOf(replace, start);
        if (foundAt < 0) return in; //if nothing found then just return
        StringBuffer ret = new StringBuffer();
        int len = replace.length();
        while(true){
            ret.append(in.substring(start, foundAt));
            ret.append(with);
            start = foundAt + len;
            foundAt = in.indexOf(replace, start);
            if(foundAt < 0) {
                ret.append(in.substring(start));
                break;
            }
        }
        return ret.toString();
    }
    
    
    
    /**
     *
     *
     * @param
     */
    final static public Object [] sort(Object []a) {
        if((a==null) || (a.length<=1))  return a;
        return _sort(a, 0, a.length-1);
    }
    
    
    
    
    /**
     *
     *
     * @param
     */
    final static public Vector sort(Vector v){
        if((v==null) || (v.size()<=1))  return v;
        return _sort(v, 0, v.size()-1);
    }
    
    
    
    /**
     *
     *
     * @param
     */
    final static private Object [] _sort(Object a[], int lo0, int hi0){
        if((a==null) || (a.length<=1))    return a;
        if(lo0 < hi0){
            int q = partition(a, lo0, hi0);
            if (q == hi0){
                q--;
            }
            _sort(a, lo0, q);
            _sort(a, q+1, hi0);
        }
        return a;
    }
    
    
    
    /**
     *
     *
     * @param
     */
    final static private Vector _sort(Vector v, int lo0, int hi0){
        if((v==null) || (v.size()<=1))  return v;
        if(lo0 < hi0){
            int q = partition(v, lo0, hi0);
            if (q == hi0){
                q--;
            }
            _sort(v, lo0, q);
            _sort(v, q+1, hi0);
        }
        return v;
    }
    
    
    
    /**
     *
     *
     * @param
     */
    final static private String getString(Object o) {
        if(o == null){
            return "";
        } else {
            return o.toString();
        }
        
        /*} else if (o instanceof String){
            return (String)o;
        } else {
            return o.toString();
        }*/
    }
    
    
    
    /**
     *
     *
     * @param
     */
    final static private int partition(Vector v, int p, int r) {
        String pivot = getString(v.elementAt(p));
        int lo = p;
        int hi = r;
        
        while (true) {
            String strhi, strlo;
            strhi = getString(v.elementAt(hi));
            while(collator.compare(strhi,pivot) >= 0 && lo < hi){
                hi--;
                strhi = getString(v.elementAt(hi));
            }
            strlo = getString(v.elementAt(lo));
            while (collator.compare(strlo, pivot) < 0 && lo < hi){
                lo++;
                strlo = getString(v.elementAt(lo));
            }
            if (lo < hi){
                Object temp = v.elementAt(lo);
                v.setElementAt(v.elementAt(hi), lo);
                v.setElementAt(temp, hi);
            }  else
                return hi;
        }
    }
    
    
    
    /**
     *
     *
     * @param
     */
    
    final static private int partition(Object a[], int p, int r) {
        String pivot = getString(a[p]);
        int lo = p;
        int hi = r;
        
        while(true) {
            String strhi, strlo;
            strhi = getString(a[hi]);
            while(collator.compare(strhi,pivot) >= 0 && lo<hi){
                hi--;
                strhi = getString(a[hi]);
            }
            strlo = getString(a[lo]);
            while (collator.compare(strlo,pivot) < 0 && lo < hi){
                lo++;
                strlo = getString(a[lo]);
            }
            if (lo<hi){
                Object temp = a[lo];
                a[lo] = a[hi];
                a[hi] = temp;
            } else
                return hi;
        }
    }
    
    /**
     * substitutes all instances of a pattern within a String, by another
     * String
     * @param in input string
     * @param swapOut pattern to replace
     * @param swapIn String to replace instances of the pattern with
     * @return new String in which all instances of swapOut have been replaced
     * substituted with swapIn
     */
    public static String substitute(final String in, final String swapOut, final String swapIn) 
    {
        if (in      == null || in.length()      <= 0)   return in;
        if (swapOut == null || swapOut.length() <= 0)   return in;
        if (swapIn  == null)                            return in;
        if (swapOut.equals(swapIn))                     return in;

        String       cur        = in;
        StringBuffer out        = new StringBuffer();
        int          swapOutLen = swapOut.length();
        int          offset     = -1;
        
        while ((offset = cur.indexOf(swapOut)) >= 0) {	    
            out.append(cur.substring(0, offset));
            out.append(swapIn);
            cur = cur.substring(offset + swapOutLen);
        }
        
        if (out != null) {
            out.append(cur);
            return out.toString();
        } else {
            return in;
        }
    }

    public static String substitute(final String in, final char c, final String swapIn) {
        return substitute(in, String.valueOf(c), swapIn);
    }

    public static String substitute(final String in, final String swapOut, final char c) {
        return substitute(in, swapOut, String.valueOf(c));
    }
    
    /**
     * Performs a series of substitution on a String.
     * Given a table of attribute value pairs, it replaces
     * patterns referring to attributes in the table with those
     * attributes' values.
     * Patterns to substitute are identified by a constant
     * header followed by the attribute name followed by a
     * constant trailer.
     * <p>Example:</p>
     * <p>Given the following table
     * <ul>
     * <li>predator = cat</li>
     * <li>prey = mouse</li>
     * </ul>
     * </p>
     * <p>Given the following header : "${attr:"
     * </p>
     * <p>Given the following header : "}"
     * </p>
     * <p>Given the following input String: <br>
     * <dd>The ${attr:color} ${attr:predator} eats the ${attr:prey}.
     * </p>
     * <p>The returned string will be:<br>
     * <dd>The ${attr:color} cat eats the mouse.
     * </p>
     * Note that ${attr:color} is not replaced because the color
     * attribute is not present in the attributes table.  Remaining
     * macros can be processed using the other substituteMacros
     * method.
     *
     * @param in input string
     * @param attributes attributes/values Map
     * @param header macro header
     * @param trailer macro trailer
     *
     * @return new String in which no unprocessed macro remains
     */
    public static String substituteMacros(String in,
    Map attributes,
    String header,
    String trailer) {
        String out = in;
        for (Iterator i = attributes.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            out = substitute(out, header + key + trailer,
                             attributes.get(key).toString());
        }
        return out;
        
    }
    
    
    /**
     * Substitute patterns identified by a trailer and a header
     * with a fixed substitution string.  The characters that
     * are between the header and the trailer are removed.
     * This method can be used to replace or remove attribute
     * macros that have not bee substituted by substituteMacros
     *
     * @param in input string
     * @param substitution substitution string
     * @param header macro header
     * @param trailer macro trailer
     */
    public static String substituteMacros(String in,
    String header, String trailer,
    String substitution) {
        String cur = in;
        StringBuffer buf = null;
        int headerLen = header.length();
        int trailerLen = trailer.length();
        int offset;
        while ((offset = cur.indexOf(header)) >= 0) {
            if (buf == null) {
                buf = new StringBuffer(cur.substring(0, offset));
            } else {
                buf.append(cur.substring(0, offset));
            }
            buf.append(substitution);
            
            cur = cur.substring(offset + headerLen);
            
            offset = cur.indexOf(trailer);
            if (offset > 0) {
                cur = cur.substring(offset + trailerLen);
            }
        }
        
        if (buf != null) {
            buf.append(cur);
            return buf.toString();
        } else {
            return in;
        }
    }
    
    public static String quoteSpecialCharacters(String in) {
        return JIDUtil.getProvider().quoteSpecialCharacters(in);
    }
    
    public static String unquoteSpecialCharacters(String in) {
        return JIDUtil.getProvider().unquoteSpecialCharacters(in);
    }
    
    /**
     * extract the domain component of an address.
     * If no domain component is found, the specified
     * default domain is returned
     */
    public static String getDomainFromAddress(String in, String defaultDomain) {
        return JIDUtil.getProvider().getDomainFromAddress(in, defaultDomain);
    }
    
    public static boolean hasDomain(String in) {
        return JIDUtil.getProvider().hasDomain(in);
    }

    public static String getLocalPartFromAddress(String in) {
        return JIDUtil.getProvider().getLocalPartFromAddress(in);
    }
    
    /**
     * append specified domain component if no domain is present.
     */
    public static String appendDomainToAddress(String in, String defaultDomain) {
        return JIDUtil.getProvider().appendDomainToAddress(in, defaultDomain);
    }

    /**
     * removes the resource string from the uid
     */
    public static String removeResource(String str) {
        if (str == null) return null;
        int index = str.indexOf('/');
        if (index != -1) 
            return str.substring(0,index);
        return str;
    }
    
    /**
     * gets the resource string from the uid
     */
    public static String getResource(String str) {
        if (str == null) return null;
        int index = str.indexOf('/');
        if (index != -1)
            return str.substring(index + 1);
        return null;
    }

    public static boolean hasResource(String str) {
        return null == str || -1 != str.indexOf('/');
    }

    /**
     * very weak verification
     */
    public static boolean isValidEmailAddress(String s) {
        int len = s.length();
        int i = s.lastIndexOf('@');
        if (len > 3 && i > 0 && i < len-1 && s.charAt(i-1) != '\\') {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * very weak verification
     */
    public static boolean isValidPhoneNumber(String s) {
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (!Character.isDigit(c[i])
            && c[i] != '+' && c[i] != '-'
            && !Character.isWhitespace(c[i])) return false;
        }
        if (s.length() < 3) return false;
        else return true;
    }

    
   /**
     * This function search a string for the characters passed through _charToSearch
     * and return the first occurence of one of them.
     * @param s The string to search
     * @param _charToSearch the characters to search for
     * @return Return a array containing :
     *			- index 0 : the position of the character in the string.
     *			- index 1 : the character found at this position.
     */
    public static LinkedList getFirstCharAndIndexOf(String s, LinkedList charToSearch)
    {
        int i, tmpPos;
        LinkedList toReturn = new LinkedList();
        toReturn.add(0, new Integer(-1));
        toReturn.add(1, new String (""));
        //		System.out.println("getFirstCharAndIndexOf : s="+s+"; v="+_charToSearch.toString());
        for (i=0; i<charToSearch.size(); i++)
        {
            tmpPos=s.indexOf((String) charToSearch.get(i));
            if (tmpPos>=0)
            {
                if ((((Integer)toReturn.get(0)).intValue() > tmpPos) || (((Integer)toReturn.get(0)).intValue()==-1))
                {
                    toReturn.set(0, new Integer(tmpPos));
                    toReturn.set(1, new String ((String)charToSearch.get(i)));
                }
            }
        }
        return(toReturn);
    }    
    
    /*
     * This reads a Set and returns the first attribute of the set.
     *@param Object o - The set from which the first attribute is required.
     *@return Returns the first attribute of the set as a String.
     */
    final static public String getFirstAttr(Object o) {
        Set e = null;
        try {
           e = (Set)o;
           if ((e == null) || e.isEmpty()) {
               return null;
           }
           return (String)e.iterator().next();
        } catch (NoSuchElementException nsee) {
           //e.printStackTrace(nsee);
           return null;
        } catch (ClassCastException cce) {
           //e.printStackTrace(cce);
           return null;
        }
    }
   
    /*
     * This reads a Set and returns the first attribute of the set.
     *@param Object o - The set from which the first attribute is required.
     *@param def - The default value if the Set is empty.
     *@return Returns the first attribute of the set as a String.
     */
    final static public String getStringAttr(Object o, String def) {
        String val = getFirstAttr(o);
        return ((val == null) ? def : val);
    }
    
    /**
     * This methods determines if a given string is present in an array of strings
     * @param strArray An array of strings to search
     * @param str The string which needs to be searched
     * @return true if the str is present in strArray
     */
    final static public boolean contains(String[] strArray, String str) {
        for(int i = 0; i < strArray.length; i++) {
            if (strArray[i].equals(str)) return true;
        }
        return false;
    }
    
    public static final int getMajorVersion(String str){
        if (null == str){
            return 0;
        }
        str = str.trim();
        int indx = str.indexOf('.');
        try{
	    if (-1 != indx){
                return Integer.parseInt(str.substring(0 , indx).trim());
	    }
        }catch(NumberFormatException nEx){
        }
        return 0;
    }
    
    private static final int MAX_JID_PORION_ALLOWED_SIZE = 1023;
    private static boolean isJIDPartLengthValid(String str , String charset){
        try{
            // treat null's as valid.
            return null == str ||
                    str.getBytes(charset).length <= MAX_JID_PORION_ALLOWED_SIZE;
        }catch(UnsupportedEncodingException iEx){
            // fallback.
            return str.length() <= MAX_JID_PORION_ALLOWED_SIZE;
        }
    }
    
    // Add other validations later.
    // As of now : 
    // 1) Length validity.
    public static boolean isJIDValid(JID jid , String charset){
        
        if (null == charset){
            charset = "UTF-8";
        }
        
        return null == jid ? true :
            isJIDPartLengthValid(jid.getDomain(), charset) &&
                isJIDPartLengthValid(jid.getNode(), charset) &&
                isJIDPartLengthValid(jid.getResource(), charset);
    }
}


