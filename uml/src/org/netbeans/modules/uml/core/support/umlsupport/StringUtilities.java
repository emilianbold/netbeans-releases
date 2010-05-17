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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.core.support.umlsupport;

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 */
public class StringUtilities
{
    /**
     * Given a delimited string and the delimiter, this function removes the first
     * delimited token from @a delimitedString and returns it.
     *
     * Example Usage:
     *
     * String X         = "A::B::C";
     * String delimiter = "::";
     * String token     = StringUtilities.removeToken( X, delimiter );
     *
     *  results:
     *    token == "A"
     *
     * @param delimitedString[in,out] the delimited string (e.g. "A::B::C")
     * @param delimiter[in] the delimiter (e.g. "::")
     *
     * @return the first token
     */
    public static ETPairT<String,String> removeToken(String text, String token)
    {
        if (text == null || token == null)
            return new ETPairT<String,String>( text, "" );
        
        int pos = text.indexOf(token);
        String tok = pos != -1? text.substring(0, pos) : text;
        text = pos != -1? text.substring(pos + token.length()) : "";
        return new ETPairT<String,String>( tok, text );
    }
    
    /**
     * 'Increments' a String that contains only lowercase alphabets. The last
     * letter in the String is 'incremented' to the next letter in alphabetical
     * order.<br/>
     * <p>
     * Examples:
     * <code>
     *  null and ""   become    "a"
     *  "e"           becomes   "f"
     *  "abc"         becomes   "abd"
     *  "z"           becomes   "aa"
     *  "abz"         becomes   "aca"
     * </code>
     * </p>
     *
     * The behaviour if the given String does not contain only lowercase letters
     * is undefined.
     *
     * @param id The String to increment.
     * @return The incremented String.
     */
    public static String incrementString(String id)
    {
        if (id == null || id.length() == 0) return "a";
        
        StringBuffer buf = new StringBuffer(id);
        for (int pos = buf.length() - 1; pos >= 0; --pos)
        {
            char c = buf.charAt(pos);
            c = c == 'z'? 'a' : (char) (c + 1);
            buf.setCharAt(pos, c);
            
            // If we didn't wrap a character around from 'z'->'a', we can exit
            // early.
            if (c != 'a') break;
            
            // If we wrapped a character and this is the first character in the
            // String, we need to insert a new character.
            if (pos == 0) buf.insert(0, "a");
        }
        
        return buf.toString();
    }
    
    /**
     * Parses a base-10 integer from the given String. Trailing non-numeric
     * characters will be ignored, and NumberFormatExceptions will be caught
     * and silently suppressed.
     *
     * @param text The String containing the number to be parsed.
     * @return The parsed <code>int</code>, defaulting to 0 if the String is
     *         unparseable.
     */
    public static int parseInt(String text)
    {
        if (text == null || text.length() == 0) return 0;
        
        int pos = findFirstNonDigit(text);
        if (pos != -1)
            text = text.substring(0, pos);
        
        try
        {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }
    
    /**
     * Returns a String with the leading digits in 'text' stripped.
     * @param text The String to strip of leading digits.
     * @return 'text' without any leading digits.
     */
    public static String stripLeadingInteger(String text)
    {
        if (text == null || text.length() == 0) return text;
        
        int pos = findFirstNonDigit(text);
        return pos == -1? text : text.substring(pos);
    }
    
    /**
     * Returns the index of the first non-digit character in the given String.
     * Non-digit characters are anything other than 0-9, or a leading '-'.
     *
     * @param text The String to search.
     * @return The index of the first non-digit character, or -1 if the String
     *         contains only digits (or is empty).
     */
    public static int findFirstNonDigit(String text)
    {
        for (int pos = 0; pos < text.length(); ++pos)
        {
            char ch = text.charAt(pos);
            if (!Character.isDigit(ch) && (pos != 0 || ch != '-'))
                return pos;
        }
        
        return -1;
    }
    
    /**
     * Changes the final directory of the path to be that of the argument
     *
     * @param basePath[in] the base path (e.g. C:\Temp\Bar\)
     * @param replacement[in] the string to append (e.g. Foo)
     *
     * @return the path (e.g. C:\Temp\Foo)
     */
    public static String changeFinalDirectory(String loc, String replacement)
    {
        String retLoc = "";
        
        File floc = new File(loc);
        floc = floc.getParentFile();
        if (floc != null)
            retLoc = new File(floc, replacement).toString();
        else
            retLoc = replacement;
        
        //      Unportable code:
        
        //		String[] strs = loc.split("\\");
        //		if (strs != null && strs.length > 0)
        //		{
        //			int size = strs.length;
        //			for(int i=0; i<size; i++)
        //			{
        //				if (size > i + 2)
        //				{
        //					if (retLoc.length() > 0)
        //					{
        //						retLoc += "\\";
        //					}
        //					retLoc += strs[i];
        //				}
        //			}
        //		}
        //		retLoc += replacement;
        return retLoc;
    }
    
    /**
     * This method takes toSplice and for every delimiter in it, replaces that delimiter
     * with value, returning the results in result.
     *
     * @param toSplice the string to parse
     * @param delimiter the delimiter to replace
     * @param value the value that is replacing every occurrence of delimiter
     * @return The resultant string after substitutions.
     */
    public static String splice( String toSplice, String delimiter, String value )
    {
        //String[] strs = toSplice.split(delimiter);
        StringTokenizer tokenizer = new StringTokenizer(toSplice, delimiter);
        return join(tokenizer, value);
    }
    
    public static String join(String[] strs, String value)
    {
        String retStr = "";
        if (strs != null)
        {
            int size = strs.length;
            for(int i=0; i<size; i++)
            {
                if (size == i+1)
                {
                    retStr += strs[i];
                }
                else
                {
                    retStr += strs[i];
                    retStr += value;
                }
            }
        }
        return retStr;
    }
    
    public static String join(StringTokenizer tokens, String value)
    {
        // String concatnation is suppose to be really slow.  StringBuffer
        // appends are suppose to be faster.
        StringBuffer retStr = new StringBuffer("");
        if (tokens != null)
        {
            while(tokens.hasMoreTokens() == true)
            {
                retStr.append(tokens.nextToken());
                if (tokens.hasMoreTokens() == true)
                {
                    retStr.append(value);
                }
            }
        }
        return retStr.toString();
    }
    
    /**
     * Retrieves the parent directory of the given file.
     * @param file An absolute file name.
     * @return The parent directory, or the <code>""</code> if we can't work out
     *         what the parent directory is (for instance, if <code>file</code>
     *         is <code>null</code>). If the directory name is not empty, it is
     *         guaranteed to end with the filename separator character.
     */
    public static String getPath(String file)
    {
        String path = "";
        if (file != null && file.length() > 0)
        {
            path = new File(file).getParent();
            if (path == null)
                path = "";
        }
        
        if (path != null && path.length() > 0 && !path.endsWith(File.separator))
            path += File.separator;
        return path;
    }
    
    /**
     * Returns the name of the file or directory.  Neiter path nor
     * the extention is used.  You can use the File class to perform
     * the same action.  However that requires a File object to be
     * created first.
     *
     * @param file The full path to the file or directory.
     * @return The file name.
     * @see java.io.File#getName
     */
    public static String getFileName(String file)
    {
        String name = "";
        if (file != null && file.length() > 0)
        {
            name = new File(file).getName();
            int dotPos  = name.lastIndexOf('.');
            if (dotPos >=0)
            {
                name = name.substring(0, dotPos);
            }
        }
        return name;
    }
    
    /**
     * @param thepath
     * @param thefile
     * @param string
     * @return
     */
    public static String createFullPath(String thepath, String thefile, String ext)
    {
        String path = thepath;
        if (thefile != null && thefile.length()>0)
        {
            int pos = thefile.indexOf('.');
            if (pos >= 0)
            {
                path += thefile.subSequence(0, pos) + ext;
            }
            else
            {
                if((thefile.startsWith(File.separator) == false) &&
                    (path.endsWith(File.separator) == false))
                {
                    path += File.separator;
                }
                path += thefile + ext;
            }
        }
        return path;
    }
    
    /**
     *
     * Replaces all occurrences of the strToReplace substring in str with the passed in string
     * in replaceWith. For example, "File %1 was saved.  Please copy %1 to a safe place"
     * Use this method to replace the %1 with another string.
     *
     * @param str[in] The string that will be searched.
     * @param strToReplace[in] The sub string in str to replace
     * @param replaceWith[in] The string to replace the sub string with.
     *
     * @return The modified string.
     *
     */
    public static String replaceAllSubstrings(String str, String strToReplace, String replaceWith)
    {
        StringBuffer buffer = new StringBuffer(str);
        //replacedStr = replacedStr.replaceAll(strToReplace, replaceWith);
        
        if (strToReplace.length() > 0)
        {
            int pos = buffer.indexOf(strToReplace);
            while (pos >= 0)
            {
                buffer.replace( pos, pos + strToReplace.length(), replaceWith );
                
                pos = buffer.indexOf( strToReplace, pos+replaceWith.length() );
            }
        }
        
        return buffer.toString();
    }
    
    /**
     *
     * Replaces the strToReplace substring in str with the passed in string in replaceWith. For example,
     * "File %1 was saved." Use this method to replace the %1 with another string.
     *
     * @param str[in] The string that will be searched.
     * @param strToReplace[in] The sub string in str to replace
     * @param replaceWith[in] The string to replace the sub string with.
     *
     * @return The modified string.
     *
     */
    public static String replaceSubString(String str, String strToReplace, String replaceWith)
    {
        //String replacedStr = str;
        //replacedStr = replacedStr.replaceFirst(strToReplace, replaceWith);
        StringBuffer buffer = new StringBuffer(str);
        int pos = buffer.indexOf(strToReplace);
        if(pos >= 0)
        {
            buffer.replace(pos, pos + strToReplace.length(), replaceWith);
        }
        
        return buffer.toString();
    }
    
    public static boolean hasExtension(String fullFileName, String extension)
    {
        boolean retVal = false;
        
        int pos = fullFileName.lastIndexOf('.');
        if(pos > 0)
        {
            // First check with out the dot in the extension.  If the check
            // fails then check with the dot in the extension.
            String ext = fullFileName.substring(pos + 1);
            retVal = ext.toLowerCase().equals(extension.toLowerCase());
            
            if(retVal == false)
            {
                ext = fullFileName.substring(pos);
                retVal = ext.toLowerCase().equals(extension.toLowerCase());
            }
        }
        
        return retVal;
    }
    
    /**
     * Returns the argument filename with the argument extension
     *
     * @param fullFilename The full path to the file
     * @param extension The extension for the file
     * @return The full filename for the file with the indicated extension
     */
    public static String ensureExtension(String filename, String ext)
    {
        String retVal = "";
        
        int index = filename.lastIndexOf('.');
        if(index > 0)
        {
            retVal = filename.substring(0, index);
        }
        else
        {
            retVal = filename;
        }
        retVal += ext;
        return retVal;
    }
    
    
    /**
     * Splits the given text into substrings around the given delimiter, and
     * adds each substring to the given Collection.
     *
     * @param text The string to split.
     * @param out  The Collection to populate with substrings.
     * @param delimiter The delimiter to use for splitting. If
     *                  <code>null</code>, the default delimiter is assumed.
     * @return The number of substrings found.
     */
    public static ETList<String> splitOnDelimiter(String text, String delimiter)
    {
        ETList<String> retStrs = new ETArrayList<String>();
        StringTokenizer tok = delimiter != null?
            new StringTokenizer(text, delimiter) : new StringTokenizer(text);
        
        while (tok.hasMoreTokens())
        {
            retStrs.add(tok.nextToken());
        }
        return retStrs;
    }
    
    /**
     * Retrieves the extension of the passed in filename.
     *
     * @param fullFileName The file coming in. Can be relative or absolute.
     *
     * @return The extension, else "" if no extension found. The
     *         returned extension will NOT contain the '.'
     */
    public static String getExtension(String filename)
    {
        String ext = "";
        int pos = filename.lastIndexOf(".");
        if (pos >= 0)
        {
            ext = filename.substring(pos+1);
        }
        return ext;
    }
    
   /*
    * Searches through a string for the first character that matches any element of a specified string.
    *
    * @param str The string for which the member function is to search.
    *
    * @return The index of the first character of the substring searched for when successful; otherwise -1
    */
    public static int findFirstOf( final String strThis, final String str )
    {
        return findFirstOf( strThis, str, 0 );
    }
    
   /*
    * Searches for the first occurrence of any of the chars in str within strThis.
    *
    * @param str The string containing chars to find in strThis.
    * @param off Index of the position in strThis at which the search is to begin.
    *
    * @return The index of the first character of the substring searched for when successful; otherwise -1
    */
    public static int findFirstOf(final String strThis, final String str, int off)
    {
        int iFirstOf = -1;
        
        for( int iPos=off; iPos<strThis.length(); iPos++ )
        {
            int iTest = str.indexOf( strThis.charAt( iPos ) );
            if( iTest != -1 )
            {
                iFirstOf = iPos;
                break;
            }
        }
        
        return iFirstOf;
    }
    
    public static String timeToString(long T, int prec)
    {
        if ((prec < 0) || (prec > 3))
            prec = 3;
        long ts = T / 1000; // total seconds
        long ms = T % 1000; // milliseconds
        long h = 0;
        if (ts >= 360000)
        {
            h = ts / 3600;
            ts = ts % 3600;
        }
        long m = 0;
        if (ts >= 6000)
        {
            m = ts / 60;
            ts = ts % 60;
        }
        StringBuffer B = new StringBuffer();
        if (h > 0)
        {
            B.append(String.valueOf(h));
            B.append("h ");
        }
        if (m > 0)
        {
            B.append(String.valueOf(m));
            B.append("m ");
        }
        B.append(String.valueOf(ts));
        if (prec == 0)
        {
            B.append("s");
            return B.toString();
        }
        B.append(".");
        switch (prec)
        {
        case 1 :
            B.append(String.valueOf(ms / 100));
            break;
        case 2 :
            B.append(String.valueOf(ms / 10));
            break;
        case 3 :
            B.append(String.valueOf(ms));
            break;
        }
        B.append("s");
        return B.toString();
    }
    
    
    
    public static String unescapeHTML(String html) 
    {
        StringBuffer retVal = new StringBuffer();
        PushbackReader reader = new PushbackReader(new StringReader(html), 20);
        try
        {
            int ch = reader.read();
            while(ch != -1)
            {
                if(ch == '&')
                {
                    int newCh = getEscapeCode(reader);
                    while(newCh != -1)
                    {
                        ch = -1;
                        if(newCh == '&')
                        {
                            newCh = getEscapeCode(reader);
                            if(newCh == -1)
                            {
                                retVal.append('&');
                            }
                            else if(newCh == '&')
                            {
                                // Now handle the case where more than one amper
                                // appears in a row.
                                retVal.append('&');

                            }
                        }
                        else
                        {
                            retVal.append((char)newCh);
                            newCh = getEscapeCode(reader);
                        }
                    }

                    // If a second escape did not appear, then we need to add the
                    // amper;
                    if(ch != -1)
                    {
                        retVal.append('&');
                    }
                }
                else
                {
                    retVal.append((char)ch);
                }

                ch = reader.read();
            }

            return retVal.toString();
        }
        
        catch (IOException ex)
        {
            Log.impossible(ex.getMessage());
            return html;
        }
    }
    
    private static int getEscapeCode(final PushbackReader reader) 
        throws IOException
    {
        int ch = -1;
        
        StringBuffer escapeString = new StringBuffer();
        int escapeChar = reader.read();
        while((escapeChar != ';') && (escapeChar != -1))
        {
            if((escapeChar == '&') || (escapeString.length() >= 10))
            {
                escapeString.append((char)escapeChar);
                escapeChar = -1;
                break;
            }
            
            escapeString.append((char)escapeChar);
            escapeChar = reader.read();
        }
        
        if(escapeChar != -1)
        {
            ch = escapeStrings.get(escapeString.toString());
        }
        else
        {
            if(escapeString.length() > 0)
            {
                char[] buffer = new char[escapeString.length()];
                escapeString.getChars(0, buffer.length, buffer, 0);
                //                reader.unread(buffer, 0, buffer.length);
                reader.unread(buffer);
            }
        }
        return ch;
    }
    
    private static Map<String, Integer> escapeStrings =
        new HashMap<String, Integer>();
    
    static
    {
        escapeStrings.put("quot", 34);
        escapeStrings.put("amp", 38);
        escapeStrings.put("lt", 60);
        escapeStrings.put("gt", 62);
        escapeStrings.put("nbsp", 160);
        escapeStrings.put("iexcl", 161);
        escapeStrings.put("cent", 162);
        escapeStrings.put("pound", 163);
        escapeStrings.put("curren", 164);
        escapeStrings.put("yen", 165);
        escapeStrings.put("brvbar", 166);
        escapeStrings.put("sect", 167);
        escapeStrings.put("uml", 168);
        escapeStrings.put("copy", 169);
        escapeStrings.put("ordf", 170);
        escapeStrings.put("laquo", 171);
        escapeStrings.put("not", 172);
        escapeStrings.put("shy", 173);
        escapeStrings.put("reg", 174);
        escapeStrings.put("macr", 175);
        escapeStrings.put("deg", 176);
        escapeStrings.put("plusmn", 177);
        escapeStrings.put("sup2", 178);
        escapeStrings.put("sup3", 179);
        escapeStrings.put("acute", 180);
        escapeStrings.put("micro", 181);
        escapeStrings.put("para", 182);
        escapeStrings.put("middot", 183);
        escapeStrings.put("cedil", 184);
        escapeStrings.put("sup1", 185);
        escapeStrings.put("ordm", 186);
        escapeStrings.put("raquo", 187);
        escapeStrings.put("frac14", 188);
        escapeStrings.put("frac12", 189);
        escapeStrings.put("frac34", 190);
        escapeStrings.put("iquest", 191);
        escapeStrings.put("Agrave", 192);
        escapeStrings.put("Aacute", 193);
        escapeStrings.put("Acirc", 194);
        escapeStrings.put("Atilde", 195);
        escapeStrings.put("Auml", 196);
        escapeStrings.put("Aring", 197);
        escapeStrings.put("AElig", 198);
        escapeStrings.put("Ccedil", 199);
        escapeStrings.put("Egrave", 200);
        escapeStrings.put("Eacute", 201);
        escapeStrings.put("Ecirc", 202);
        escapeStrings.put("Euml", 203);
        escapeStrings.put("Igrave", 204);
        escapeStrings.put("Iacute", 205);
        escapeStrings.put("Icirc", 206);
        escapeStrings.put("Iuml", 207);
        escapeStrings.put("ETH", 208);
        escapeStrings.put("Ntilde", 209);
        escapeStrings.put("Ograve", 210);
        escapeStrings.put("Oacute", 211);
        escapeStrings.put("Ocirc", 212);
        escapeStrings.put("Otilde", 213);
        escapeStrings.put("Ouml", 214);
        escapeStrings.put("times", 215);
        escapeStrings.put("Oslash", 216);
        escapeStrings.put("Ugrave", 217);
        escapeStrings.put("Uacute", 218);
        escapeStrings.put("Ucirc", 219);
        escapeStrings.put("Uuml", 220);
        escapeStrings.put("Yacute", 221);
        escapeStrings.put("THORN", 222);
        escapeStrings.put("szlig", 223);
        escapeStrings.put("agrave", 224);
        escapeStrings.put("aacute", 225);
        escapeStrings.put("acirc", 226);
        escapeStrings.put("atilde", 227);
        escapeStrings.put("auml", 228);
        escapeStrings.put("aring", 229);
        escapeStrings.put("aelig", 230);
        escapeStrings.put("ccedil", 231);
        escapeStrings.put("egrave", 232);
        escapeStrings.put("eacute", 233);
        escapeStrings.put("ecirc", 234);
        escapeStrings.put("euml", 235);
        escapeStrings.put("igrave", 236);
        escapeStrings.put("iacute", 237);
        escapeStrings.put("icirc", 238);
        escapeStrings.put("iuml", 239);
        escapeStrings.put("eth", 240);
        escapeStrings.put("ntilde", 241);
        escapeStrings.put("ograve", 242);
        escapeStrings.put("oacute", 243);
        escapeStrings.put("ocirc", 244);
        escapeStrings.put("otilde", 245);
        escapeStrings.put("ouml", 246);
        escapeStrings.put("divide", 247);
        escapeStrings.put("oslash", 248);
        escapeStrings.put("ugrave", 249);
        escapeStrings.put("uacute", 250);
        escapeStrings.put("ucirc", 251);
        escapeStrings.put("uuml", 252);
        escapeStrings.put("yacute", 253);
        escapeStrings.put("thorn", 254);
        escapeStrings.put("yuml", 255);
        escapeStrings.put("OElig", 338);
        escapeStrings.put("oelig", 339);
        escapeStrings.put("Scaron", 352);
        escapeStrings.put("scaron", 353);
        escapeStrings.put("Yuml", 376);
        escapeStrings.put("fnof", 402);
        escapeStrings.put("circ", 710);
        escapeStrings.put("tilde", 732);
        escapeStrings.put("Alpha", 913);
        escapeStrings.put("Beta", 914);
        escapeStrings.put("Gamma", 915);
        escapeStrings.put("Delta", 916);
        escapeStrings.put("Epsilon", 917);
        escapeStrings.put("Zeta", 918);
        escapeStrings.put("Eta", 919);
        escapeStrings.put("Theta", 920);
        escapeStrings.put("Iota", 921);
        escapeStrings.put("Kappa", 922);
        escapeStrings.put("Lambda", 923);
        escapeStrings.put("Mu", 924);
        escapeStrings.put("Nu", 925);
        escapeStrings.put("Xi", 926);
        escapeStrings.put("Omicron", 927);
        escapeStrings.put("Pi", 928);
        escapeStrings.put("Rho", 929);
        escapeStrings.put("Sigma", 931);
        escapeStrings.put("Tau", 932);
        escapeStrings.put("Upsilon", 933);
        escapeStrings.put("Phi", 934);
        escapeStrings.put("Chi", 935);
        escapeStrings.put("Psi", 936);
        escapeStrings.put("Omega", 937);
        escapeStrings.put("alpha", 945);
        escapeStrings.put("beta", 946);
        escapeStrings.put("gamma", 947);
        escapeStrings.put("delta", 948);
        escapeStrings.put("epsilon", 949);
        escapeStrings.put("zeta", 950);
        escapeStrings.put("eta", 951);
        escapeStrings.put("theta", 952);
        escapeStrings.put("iota", 953);
        escapeStrings.put("kappa", 954);
        escapeStrings.put("lambda", 955);
        escapeStrings.put("mu", 956);
        escapeStrings.put("nu", 957);
        escapeStrings.put("xi", 958);
        escapeStrings.put("omicron", 959);
        escapeStrings.put("pi", 960);
        escapeStrings.put("rho", 961);
        escapeStrings.put("sigmaf", 962);
        escapeStrings.put("sigma", 963);
        escapeStrings.put("tau", 964);
        escapeStrings.put("upsilon", 965);
        escapeStrings.put("phi", 966);
        escapeStrings.put("chi", 967);
        escapeStrings.put("psi", 968);
        escapeStrings.put("omega", 969);
        escapeStrings.put("thetasym", 977);
        escapeStrings.put("upsih", 978);
        escapeStrings.put("piv", 982);
        escapeStrings.put("ensp", 8194);
        escapeStrings.put("emsp", 8195);
        escapeStrings.put("thinsp", 8201);
        escapeStrings.put("zwnj", 8204);
        escapeStrings.put("zwj", 8205);
        escapeStrings.put("lrm", 8206);
        escapeStrings.put("rlm", 8207);
        escapeStrings.put("ndash", 8211);
        escapeStrings.put("mdash", 8212);
        escapeStrings.put("lsquo", 8216);
        escapeStrings.put("rsquo", 8217);
        escapeStrings.put("sbquo", 8218);
        escapeStrings.put("ldquo", 8220);
        escapeStrings.put("rdquo", 8221);
        escapeStrings.put("bdquo", 8222);
        escapeStrings.put("dagger", 8224);
        escapeStrings.put("Dagger", 8225);
        escapeStrings.put("permil", 8240);
        escapeStrings.put("lsaquo", 8249);
        escapeStrings.put("rsaquo", 8250);
        escapeStrings.put("bull", 8226);
        escapeStrings.put("hellip", 8230);
        escapeStrings.put("prime", 8242);
        escapeStrings.put("Prime", 8243);
        escapeStrings.put("oline", 8254);
        escapeStrings.put("frasl", 8260);
        escapeStrings.put("image", 8465);
        escapeStrings.put("weierp", 8472);
        escapeStrings.put("real", 8476);
        escapeStrings.put("trade", 8482);
        escapeStrings.put("alefsym", 8501);
        escapeStrings.put("larr", 8592);
        escapeStrings.put("uarr", 8593);
        escapeStrings.put("rarr", 8594);
        escapeStrings.put("darr", 8595);
        escapeStrings.put("harr", 8596);
        escapeStrings.put("crarr", 8629);
        escapeStrings.put("lArr", 8656);
        escapeStrings.put("uArr", 8657);
        escapeStrings.put("rArr", 8658);
        escapeStrings.put("dArr", 8659);
        escapeStrings.put("hArr", 8660);
        escapeStrings.put("forall", 8704);
        escapeStrings.put("part", 8706);
        escapeStrings.put("exist", 8707);
        escapeStrings.put("empty", 8709);
        escapeStrings.put("nabla", 8711);
        escapeStrings.put("isin", 8712);
        escapeStrings.put("notin", 8713);
        escapeStrings.put("ni", 8715);
        escapeStrings.put("prod", 8719);
        escapeStrings.put("sum", 8721);
        escapeStrings.put("minus", 8722);
        escapeStrings.put("lowast", 8727);
        escapeStrings.put("radic", 8730);
        escapeStrings.put("prop", 8733);
        escapeStrings.put("infin", 8734);
        escapeStrings.put("ang", 8736);
        escapeStrings.put("and", 8743);
        escapeStrings.put("or", 8744);
        escapeStrings.put("cap", 8745);
        escapeStrings.put("cup", 8746);
        escapeStrings.put("int", 8747);
        escapeStrings.put("there4", 8756);
        escapeStrings.put("sim", 8764);
        escapeStrings.put("cong", 8773);
        escapeStrings.put("asymp", 8776);
        escapeStrings.put("ne", 8800);
        escapeStrings.put("equiv", 8801);
        escapeStrings.put("le", 8804);
        escapeStrings.put("ge", 8805);
        escapeStrings.put("sub", 8834);
        escapeStrings.put("sup", 8835);
        escapeStrings.put("nsub", 8836);
        escapeStrings.put("sube", 8838);
        escapeStrings.put("supe", 8839);
        escapeStrings.put("oplus", 8853);
        escapeStrings.put("otimes", 8855);
        escapeStrings.put("perp", 8869);
        escapeStrings.put("sdot", 8901);
        escapeStrings.put("lceil", 8968);
        escapeStrings.put("rceil", 8969);
        escapeStrings.put("lfloor", 8970);
        escapeStrings.put("rfloor", 8971);
        escapeStrings.put("lang", 9001);
        escapeStrings.put("rang", 9002);
        escapeStrings.put("loz", 9674);
        escapeStrings.put("spades", 9824);
        escapeStrings.put("clubs", 9827);
        escapeStrings.put("hearts", 9829);
        escapeStrings.put("diams", 9830);
    }
    
}
