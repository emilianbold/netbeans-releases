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

package org.netbeans.modules.uml.common;

import java.util.*;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.net.*;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.openide.util.NbBundle;

/**
 * Contains commonly used functions.
 *
 */
public class Util
{
    
    /** Static constant for -1. */
    public final static Integer MINUS_ONE = new Integer(-1);
    
    /** Static constant for 0. */
    public final static Integer ZERO = new Integer(0);
    
    /** Gets the platform dependent line separator. */
    public final static String getLineSeparator()
    {
        return cLineSeparator;
    }
    
    private final static String cLineSeparator;
    
    /* Load the line separator. */
    static
    {
        cLineSeparator = (String) java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));
    }
    
    /**
     * Replaces occurences of the given pattern in the source string with the replacement value.
     * The resulting string is returned.
     *
     * @param source source string
     * @param pattern pattern to search for
     * @param replacement replacement string
     * @return string with pattern replaced.
     */
    public final static String replace(String source, String pattern, String replacement)
    {
        // replaced occurrences of %x with the appropriate parameter
        if (source == null)
            return null;
        int pos = source.indexOf(pattern);
        if (pos == -1)
            return source;
        StringBuffer buffer = new StringBuffer((int) (source.length() * 1.5));
        int index = 0;
        int plength = pattern.length();
        
        while (pos != -1)
        {
            buffer.append(source.substring(index, pos));
            buffer.append(replacement);
            index = pos + plength;
            pos = source.indexOf(pattern, index);
        }
        
        buffer.append(source.substring(index));
        return buffer.toString();
    }
    
    /**
     * Converts the given object to a String.
     *
     * @return "" if null is given
     */
    public final static String toString(Object value)
    {
        if (value == null)
            return "";
        return value.toString();
    }
    
    /** Convert a given string to the equivalent HTML string
     * @param s The string to be converted.
     * @return The converted string */
    public final static String convertToHTML(String s)
    {
        StringBuffer str = new StringBuffer();
        int len = (s != null) ? s.length() : 0;
        
        for (int i = 0; i < len; i++)
        {
            char ch = s.charAt(i);
            
            switch (ch)
            {
            case '<' :
                str.append("&lt;");
                break;
                
            case '>' :
                str.append("&gt;");
                break;
                
            case '&' :
                str.append("&amp;");
                break;
                
            case '"' :
                str.append("&quot;");
                break;
                
                // Append character
            default :
                str.append(ch);
                
            } // END switch
        } // END for
        
        return (str.toString());
        
    } // END convertToHTML
    
    /** Converts the given object to an int.
       @return 0 if null is given */
    public final static int toInt(Object value)
    {
        return toInt(value, 0);
    }
    
    /** Converts the given object to an int.
       @return def if null is given */
    public final static int toInt(Object value, int def)
    {
        if (value == null)
            return def;
        if (value instanceof Number)
            return ((Number) value).intValue();
        String tmp = value.toString().trim();
        if (tmp.equals(""))
            return 0;
        return Integer.parseInt(tmp);
    }
    
    /** Converts the given object to an long.
     * @return 0 if null is given */
    public final static long toLong(Object value)
    {
        if (value == null)
            return 0;
        if (value instanceof Number)
            return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }
    
    /** Converts the given object to an float.
     * @return 0 if null is given */
    public final static float toFloat(Object value)
    {
        if (value == null)
            return (float) 0.0;
        if (value instanceof Number)
            return ((Number) value).floatValue();
        return Float.parseFloat(value.toString());
    }
    
    /** Converts the given object to an double.
     * @return 0 if null is given */
    public final static double toDouble(Object value)
    {
        if (value == null)
            return 0.0;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
    
    /** Converts the given object to a Number.
     * return 0 if null is given */
    public final static Number toNumber(Object value)
    {
        if (value instanceof Number)
            return (Number) value;
        if (value == null)
            return ZERO;
        
        // on EBIS true evaluates to -1 and false evaluates to 0
        if (value instanceof Boolean)
        {
            if (((Boolean) value).booleanValue())
                return MINUS_ONE;
            return ZERO;
        }
        
        String tmp = value.toString();
        if (tmp.indexOf('.') == -1 || tmp.endsWith(".0"))
            return new Long(tmp);
        else
            return new Double(tmp);
    }
    
    /** Converts the given object to a Number.  This will return an Integer if the
     * float has no decimal value, Float otherwise. */
    public final static Number toNumber(float value)
    {
        // Numbers are output incorrectly where the result is really an integer value
        // Java adds a .0 to the end of the number when the value is a Float or Double
        // Convert where this is really an integer value...
        if (String.valueOf(value).endsWith(".0"))
            return new Long((long) value);
        return new Float(value);
    }
    
    /** Converts the given object to a Number.  This will return a Long if the
     * double has no decimal value, Double otherwise. */
    public final static Number toNumber(double value)
    {
        // Numbers are output incorrectly where the result is really an integer value
        // Java adds a .0 to the end of the number when the value is a Float or Double
        // Convert where this is really an integer value...
        if (String.valueOf(value).endsWith(".0"))
            return new Long((long) value);
        return new Double(value);
    }
    
    /** Converts the given object to a boolean.
       @return false if null is given */
    public final static boolean toBoolean(Object value)
    {
        return toBoolean(value, false);
    }
    
    /** Converts the given object to a boolean.
       @return def if null is given */
    public final static boolean toBoolean(Object value, boolean def)
    {
        if (value == null)
            return def;
        else if (value instanceof Boolean)
            return ((Boolean) value).booleanValue();
        else if (value instanceof Number)
        {
            if (((Number) value).doubleValue() == 0)
                return false;
        }
        else if (value.toString().trim().equals(""))
            return false;
        else if (value.toString().trim().equals("0"))
            return false;
        else if (value.toString().trim().equals("N"))
            return false;
        else if (value.toString().trim().equalsIgnoreCase("false"))
            return false;
        
        return true;
    }
    
    /** Converts the given object to a boolean object.
     * @return false if null is given */
    public final static Boolean toBooleanObj(Object value)
    {
        if (value == null)
            return Boolean.FALSE;
        else if (value instanceof Boolean)
            return (Boolean) value;
        else if (value instanceof Number)
        {
            if (((Number) value).doubleValue() == 0)
                return Boolean.FALSE;
        }
        else if (value.toString().trim().equals(""))
            return Boolean.FALSE;
        else if (value.toString().trim().equals("0"))
            return Boolean.FALSE;
        else if (value.toString().trim().equalsIgnoreCase("false"))
            return Boolean.FALSE;
        
        return Boolean.TRUE;
    }
    
    /**
     * Converts the given object to a locale.
     *
     * @return false if null is given
     */
    public final static Locale toLocale(Object value)
    {
        if (value == null)
            return Locale.US;
        else if (value instanceof Locale)
            return (Locale) value;
        else
        {
            String lang = "", country = "", variant = "";
            String loc = value.toString();
            
            if (loc.length() >= 2)
                lang = loc.substring(0, 2);
            if (loc.length() >= 5)
                country = loc.substring(3, 5);
            if (loc.length() >= 6)
                variant = loc.substring(6);
            
            return (new Locale(lang, country, variant));
        }
    }
    
    /** Converts the days, hours, minutes and seconds to milli-seconds. */
    public final static long toMillis(int days, int hours, int minutes, int seconds)
    {
        final long HOURS_PER_DAY = 24;
        final long MINUTES_PER_HOUR = 60;
        final long SECONDS_PER_MINUTE = 60;
        final long MILLIS_PER_SECOND = 1000;
        
        long result = 0;
        result = (days) * HOURS_PER_DAY; // units are now hours
        result = (result + hours) * MINUTES_PER_HOUR; // units are now minutes
        result = (result + minutes) * SECONDS_PER_MINUTE; // units are now seconds
        result = (result + seconds) * MILLIS_PER_SECOND; // units are now milliseconds
        
        return result;
    }
    
    /**
     * Given the class, this method returns the static field value corresponding
     * to the given field name.
     */
    public final static int getStaticIntField(Class c, String fieldName)
    {
        try
        {
            Field f = c.getField(fieldName);
            return f.getInt(null);
        }
        catch (Throwable exc)
        {
            /* assume it is a numeric value */
            return toInt(fieldName);
        }
    }
    
    /** Checks to see if the string starts with the
     * the given string (ignoring case and whitespace)
     * @param source source string
     * @param pattern pattern to search for
     * @param ws indicator for ignoring leading whitespace */
    public final static boolean startsWithIgnoreCase(String source, String pattern, boolean ws)
    {
        int slen = source.length();
        int sind = 0;
        
        // skip leading white space
        if (ws)
        {
            while (sind < slen)
            {
                if (Character.isWhitespace(source.charAt(sind)))
                    sind++;
                else
                    break;
            }
        }
        
        // search for pattern
        int pind = 0;
        int plen = pattern.length();
        if (plen > (slen - sind))
            return false; // pattern length is larger than remaining string
        
        while (pind < plen && sind < slen)
        {
            char sc = Character.toLowerCase(source.charAt(sind));
            char pc = Character.toLowerCase(pattern.charAt(pind));
            if (sc != pc)
                return false;
            pind++;
            sind++;
        }
        
        return true;
    }
    
    private static int toByte(char c1, int radix)
    {
        int j;
        if (c1 >= '0' && c1 <= '9')
        {
            j = c1 - (int) '0';
        }
        else if (c1 >= 'A' && c1 <= 'Z')
        {
            j = (c1 - (int) 'A') + 10;
        }
        else if (c1 >= 'a' && c1 <= 'z')
        {
            j = (c1 - (int) 'a') + 10;
        }
        else
        {
            j = -1;
        }
        if (j < 0 || j >= radix)
            return -1;
        else
            return j;
    }
    
    /**
     * Converts separators '/' and '.' to File.separator.
     * Note: for '.' it converts all but the last '.'
     */
    public static final String convertFilename(String filename)
    {
        /* replace occurences of . with File.separator (except last ccurence)*/
        int pos = filename.lastIndexOf(".");
        
        if (pos == -1)
            return null;
        
        String extension = filename.substring(pos);
        String prefix = filename.substring(0, pos);
        
        /* change "/" to real file separator */
        prefix = replace(prefix, "/", File.separator);
        /* assume extra '.' refer to file separator */
        prefix = replace(prefix, ".", File.separator);
        
        return prefix + extension;
    }
    
    /**
     * Converts separators '\' to '/'.
     */
    public static final String convertFilenameToUnix(String filename)
    {
        return convertDirnameToUnix(filename);
    }
    
    /**
     * Converts separators '\' to '/'.
     */
    public static final String convertDirnameToUnix(String dirname)
    {
        /* change "/" to unix standard file separator */
        dirname = replace(dirname, "\\", "/");
        if (!File.separator.equals("/"))
        {
            /* change File.separator to unix standard file separator */
            dirname = replace(dirname, File.separator, "/");
        }
        // remove any trailing slash
        if (dirname.endsWith("/") && dirname.length() > 1 && !dirname.equals("~/"))
        {
            dirname = dirname.substring(0, dirname.length() - 1);
        }
        return dirname;
    }
    
    /**
     * Searches the given path for the given filename.
     *
     * @param filename name of file to search for
     * @param path filesystem path to search
     * @return the file, null if not found
     */
    public static final File findFile(String filename, String path)
    {
        filename = convertFilename(filename);
        
        if (filename == null)
            return null;
        
        // case where separator is set for the Windows platform and we are on unix
        if (File.pathSeparator.equals(":"))
        {
            path = replace(path, ";", ":");
        }
        
        File file = new File(filename);
        StringTokenizer strtok = new StringTokenizer(path, File.pathSeparator, false);
        
        /* check if file already has the full path */
        if (file.exists())
            return file;
        
        /* search the given path */
        while (strtok.hasMoreTokens())
        {
            file = new File(strtok.nextToken(), filename);
            if (file.exists() && file.isFile())
                return file;
        }
        
        return null;
    }
    
    /** Reads the contents of the reader and puts it into a string. */
    public final static String toString(Reader reader) throws IOException
    {
        if (reader == null)
            return "";
        StringBuffer buffer = new StringBuffer();
        
        int c = reader.read();
        while (c != -1)
        {
            buffer.append((char) c);
            c = reader.read();
        }
        
        return buffer.toString();
    }
    
    /** Reads the file with the given name and returns the contents as a String. */
    public final static String readFile(String filename) throws IOException
    {
        return toString(new FileReader(filename));
    }
    
   /*
    * Convert a string to the javascript equivalent.
    */
    public final static String toJavaScriptString(String str)
    {
        if (str == null)
            return "";
        String newString = str.toString();
        
        newString = replace(newString, "\\", "\\\\");
        newString = replace(newString, "\'", "\\'");
        newString = replace(newString, "\"", "\\\"");
        newString = replace(newString, "\r\n", "\\n");
        newString = replace(newString, "\n", "\\n");
        newString = replace(newString, "\r", "\\n");
        
        return newString;
    }
    
    /**
     * Tests if a string is empty or null
     */
    public final static boolean isEmpty(String str)
    {
        return (str == null || str.length() == 0 || str.trim().equals("")) ? true : false;
    }
    
    /**
     * Adds two numbers together.
     */
    public final static Number add(Number x, Number y)
    {
        if (x instanceof Integer && y instanceof Integer)
        {
            return new Integer(x.intValue() + y.intValue());
        }
        
        if (x instanceof Float && y instanceof Float)
        {
            return new Float(x.floatValue() + y.floatValue());
        }
        
        if (x instanceof Double && y instanceof Double)
        {
            return new Double(x.doubleValue() + y.doubleValue());
        }
        
        if (x instanceof BigInteger && y instanceof BigInteger)
        {
            return ((BigInteger) x).add((BigInteger) y);
        }
        
        if (x instanceof BigDecimal && y instanceof BigDecimal)
        {
            return ((BigDecimal) x).add((BigDecimal) y);
        }
        
        return new Long(x.longValue() + y.longValue());
    }
    
    /** Compares the two objects.  */
    public final static boolean compare(Object x, Object y)
    {
        if (x == null && y == null)
            return true;
        if (x == null)
            return false;
        if (y == null)
            return false;
        return x.equals(y);
    }
    
    /** Compares the two strings after the whitespace is trimmed.  */
    public final static boolean compareTrimmedStrings(String x, String y)
    {
        if (x == null && y == null)
            return true;
        if (x == null)
            return false;
        if (y == null)
            return false;
        
        return x.trim().equals(y.trim());
    }
    
    /** Compares the two numbers and returns true if they are equal. */
    public final static boolean compareNumbers(Number x, Number y)
    {
        if (x instanceof BigDecimal && y instanceof BigDecimal)
        {
            return ((BigDecimal) x).compareTo((BigDecimal) y) == 0;
        }
        if (x instanceof BigInteger && y instanceof BigInteger)
        {
            return ((BigInteger) x).compareTo((BigInteger) y) == 0;
        }
        else if (x instanceof Short && y instanceof Short)
        {
            return ((Number) x).shortValue() == ((Number) y).shortValue();
        }
        else if (x instanceof Integer && y instanceof Integer)
        {
            return ((Number) x).intValue() == ((Number) y).intValue();
        }
        else if (x instanceof Long && y instanceof Long)
        {
            return ((Number) x).longValue() == ((Number) y).longValue();
        }
        else if (x instanceof Float && y instanceof Float)
        {
            return ((Number) x).floatValue() == ((Number) y).floatValue();
        }
        else
        {
            // comparing of double values seems to cause rounding errors
            // float seems to work fine...
            return ((Number) x).floatValue() == ((Number) y).floatValue();
        }
    }
    
    /** Prints the stack trace of the exception to a string. */
    public final static String printStackTrace(Throwable exc)
    {
        StringWriter source = new StringWriter();
        exc.printStackTrace(new PrintWriter(source));
        return source.toString();
    }
    
    /** Convert the date in long to yyyy-MM-dd format String */
    public final static String toDate(long dateInMillis)
    {
        return toDate(dateInMillis, cDateFormat);
    }
    
    /** Parse the date from yyyy-MM-dd to long */
    public final static Date parseDate(String date)
    {
        if (date == null)
            return null;
        return parseDate(date, cDateFormat);
    }
    
    /** Converts the date in long to yyyy-MM-dd hh:mm:ss format string */
    public final static String toDateTime(long dateInMillis)
    {
        return toDate(dateInMillis, cDateTimeFormat);
    }
    
    /** Parses the date from yyyy-MM-dd hh:mm:ss to long */
    public final static Date parseDateTime(String date)
    {
        return parseDate(date, cDateTimeFormat);
    }
    
    /** Parse the date format given by the DateFormat object */
    public final static Date parseDate(String date, DateFormat format)
    {
        if (isEmpty(date) || format == null)
            return null;
        Date d = null;
        try
        {
            d = format.parse(date);
        }
        catch (Throwable exc)
        {
        }
        
        return d;
    }
    
    /** Format the date given by the DateFormat object */
    public final static String toDate(long dateInMillis, DateFormat format)
    {
        if (format == null)
            return "";
        Date date = new Date(dateInMillis);
        try
        {
            return format.format(date);
        }
        catch (Throwable exc)
        {
            return "";
        }
    }
    
    /** Pads the given string to the given length. Whitespace is added to the
     * end of the string. */
    public final static String pad(Object value, int length)
    {
        StringBuffer result = new StringBuffer();
        if (value == null)
            result.append("null");
        else
            result.append(value.toString());
        
        int num = length - result.length();
        for (int i = 0; i < num; i++)
        {
            result.append(" ");
        }
        
        return result.toString();
    }
    
    /** Pads the given string to the given length. Whitespace is added to the
     * end of the string. */
    public final static String pad(Object value, int length, boolean trim)
    {
        String result = "null";
        if (value != null)
            result = value.toString();
        
        int num = length - result.length();
        for (int i = 0; i < num; i++)
        {
            result += " ";
        }
        
        if (trim)
            return result.substring(0, length - 1);
        return result;
    }
    
    /** Pads the given string to the given length.  Whitespace is added to the
     * front of the string. */
    public final static String lpad(Object value, int length)
    {
        String result = "null";
        if (value != null)
            result = value.toString();
        
        int num = length - result.length();
        for (int i = 0; i < num; i++)
        {
            result = " " + result;
        }
        
        return result;
    }
    
    public static double getTimeDiffInSeconds(long startInMillis, long endInMillis)
    {
        long timeDiff = endInMillis - startInMillis;
        double sec = timeDiff / 1000.0;
        return sec;
    }
    
    /** Reads in the contents of the url stream and returns the result
     * as a string. */
    public static String getURLContentAsString(URL url) throws IOException
    {
        if (url == null)
            return null;
        InputStream in = url.openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        final int KILOBYTE = 1024;
        byte buf[] = new byte[4 * KILOBYTE];
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1)
        {
            out.write(buf, 0, bytesRead);
        }
        
        return out.toString();
    }
    
    private static final String[] cRandomWords = { "a", "b", "c", "d", "e", "f", "g", "h", "j", "k", "m", "n", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", //ambiguous letters removed
    "Alpha",
    "Omega",
    "Theta",
    "Beta",
    "Delta",
    "Sun",
    "Mercury",
    "Venus",
    "Moon",
    "Earth",
    "Mars",
    "Jupiter",
    "Saturn",
    "Neptune",
    "Color",
    "Red",
    "Blue",
    "Green",
    "Black",
    "White",
    "Brown",
    "Yellow",
    "Orange",
    "Direction",
    "North",
    "South",
    "East",
    "West",
    "Up",
    "Down",
    "Number",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    //ambiguous numbers removed
    "Idea", "Empty", "Time", "Space", "Friend", "Spice", "Salt", "Pepper", "Sugar", "Knife", "Fork", "Spoon", "Plate", "Glass", "Bowl" };
    
    /** Returns a random word from a built in dictionary */
    private static String randomWord()
    {
        return cRandomWords[(int) Math.floor(Math.random() * cRandomWords.length)];
    }
    
    /** Returns a random entry from an array */
    public static Object randomEntry(Object array[])
    {
        return array[(int) Math.floor(Math.random() * array.length)];
    }
    
    /** Returns a random number within the given range */
    public static int random(int low, int high)
    {
        int x = (int) Math.floor(Math.random() * (high - low));
        return x + low;
    }
    
    /** Creates over 35 million unique passwords.
     * Each password is easy to remember consisting of 4 words */
    public static String createRandomPassword()
    {
        String password = "";
        int length = 4;
        for (int count = 0; count < length; count++)
        {
            if (count != 0)
            {
                password = password + "-";
            }
            password = password + randomWord();
        }
        return password.toLowerCase();
    }
    
    /** Gets the current working directory. */
    public static String getCurrentWorkingDirectory()
    {
        File f = new File(".");
        String path = f.getAbsolutePath();
        return path.substring(0, path.indexOf('.'));
    }
    
    /** Creates a mapping of the given set of message IDs. */
    public static Map createMap(MsgID ids[])
    {
        if (ids == null)
            return null;
        
        Map map = new HashMap();
        for (int i = 0; i < ids.length; i++)
        {
            map.put(ids[i].getID(), ids[i]);
        }
        
        return map;
    }
    
    /** Creates a mapping of the given set of message IDs. */
    public static MsgID[] concat(MsgID x[], MsgID y[])
    {
        if (x == null)
            return y;
        if (y == null)
            return x;
        
        MsgID z[] = new MsgID[x.length + y.length];
        
        int ind = 0;
        for (int i = 0; i < x.length; i++)
        {
            z[ind++] = x[i];
        }
        for (int i = 0; i < y.length; i++)
        {
            z[ind++] = y[i];
        }
        
        return z;
    }
    
    /** Creates a Date object with the given information. */
    public static Date newDate(int year, int month, int day)
    {
        return (new GregorianCalendar(year, month - 1, day)).getTime();
    }
    
    /** Trims the string to ensure that is has a max number of characters. */
    public static String trim(String src, int max)
    {
        if (src == null)
            return null;
        if (src.length() <= max)
            return src;
        return src.substring(0, max);
    }
    
    /** Proper case a string*/
    public static String properCase(String value)
    {
        String properValue = "";
        StringTokenizer st = new StringTokenizer(value, " ");
        
        String firstCharacterUpperCase = "", restOfWord = "";
        while (st.hasMoreTokens())
        {
            String token = st.nextToken();
            firstCharacterUpperCase = (token.substring(0, 1)).toUpperCase();
            restOfWord = token.substring(1);
            properValue += firstCharacterUpperCase + restOfWord.toLowerCase();
        }
        return properValue;
    }
    
    public static void forceGC()
    {
        runBackgroundGC();
    }
    
    public static void forceGCWait()
    {
        System.gc();
        System.runFinalization();
        System.gc();
    }
    
    public static void runBackgroundGC()
    {
        Thread gcThread = new Thread(new Runnable()
        {
            public void run()
            {
                forceGCWait();
            }
        });
        gcThread.setPriority(Thread.MIN_PRIORITY);
        gcThread.start();
    }
    
    /**
     * Strips spaces in a type name, or any string.
     */
    public static String stripSpacesInString(String s)
    {
        StringBuffer sb = new StringBuffer(s.length());
        for (int i=0; i<s.length(); i++)
        {
            char c = s.charAt(i);
            if (c != ' ')
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    
    /**
     * Checks to see if the namespace already has a child of the same name and type.
     * @param space The target namespace being searched.
     * @param name The name of the element being searched for.
     * @param newType The type of the element being searched for.
     * @param self An instance of the NamedElement being search for.
     * @return true, if a name collision is detected; false, otherwise.
     */
    public static boolean hasNameCollision(
        INamespace space, String name, String newType, INamedElement self)
    {
        if (space==null)
            return false;
        
        //kris richards - "DefaultElementName" pref expunged. Set to "Unnamed".
        String defaultName = NbBundle.getMessage (Util.class, "UNNAMED");
                
        
        // skip validation for the newly created unnamed element
        if (defaultName != null && defaultName.equals(name))
            return false;
        
        IElementLocator pElementLocator = new ElementLocator();
    
        ETList<INamedElement> pFoundElements = pElementLocator.findByName(space, name);
        
        if (pFoundElements != null)
        {
            String foundType;
            int count = pFoundElements.getCount();
            for (int i = 0 ; i < count ; i++)
            {
                INamedElement pFoundElement = pFoundElements.get(i);
                
                if (pFoundElement != null)
                {
                    if (self==null || pFoundElement.getXMIID() != self.getXMIID())
                    {
                        foundType = pFoundElement.getElementType();
                        if (foundType.equals(newType))
                            return true;
                        if (types.contains(newType) && types.contains(foundType))
                            return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Checks to see if the namespace already has a <em>similar</em> child.
     * @param space The target namespace being searched.
     * @param name The name of the element being searched for.
     * @param newType The type of the element being searched for.
     * @param self An instance of the NamedElement being search for.
     * @return true, if a elements are <em>similar</em>; false, otherwise.
     */
    public static boolean containsSimilarElement(
        INamespace space, String name, String newType, INamedElement self)
    {
        IElementLocator pElementLocator = new ElementLocator();
        if (space==null)
            return false;
        
        ETList<INamedElement> pFoundElements = pElementLocator.findByName(space, name);
        
        if (pFoundElements != null)
        {
            String foundType;
            int count = pFoundElements.getCount();
            
            for (int i = 0 ; i < count ; i++)
            {
                INamedElement pFoundElement = pFoundElements.get(i);
                
                if (pFoundElement != null)
                {
                    if (self==null || pFoundElement.getXMIID() != self.getXMIID())
                    {
                        foundType = pFoundElement.getElementType();
                        if (pFoundElement.isSimilar(self))
                            return true;
                    }
                }
            }
        }
        return false;
    }
    
    
    public static ETList<INamedElement> getCollidingElements(
        INamedElement curElement, String newName)
    {
        ETList<INamedElement> collidingElements = new ETArrayList<INamedElement>();
        if ( newName != null && newName.length() > 0 && curElement.getNamespace() != null)
        {
            ElementLocator locator = new ElementLocator();
            ETList<INamedElement> pFoundElements = locator.findByName(curElement.getNamespace(), newName);
            
            if (pFoundElements != null)
            {
                String foundType;
                int count = pFoundElements.getCount();
                for (int i = 0 ; i < count ; i++)
                {
                    INamedElement pFoundElement = pFoundElements.get(i);
                    
                    if (pFoundElement != null)
                    {
                        if (pFoundElement.getXMIID() != curElement.getXMIID())
                        {
                            foundType = pFoundElement.getElementType();
                            String type = curElement.getElementType();
                            if ((foundType.equals(type) ||
                                (types.contains(type) && types.contains(foundType))))
                                collidingElements.add(pFoundElement);
                        }
                    }
                }
            }
        }
        return collidingElements;
    }
    
    /**
     * Determines if the passed in type is a known Collection data type.
     * @param type the data type to be tested
     * @return true if java.util.Collection is assignable
     *   from (is a superclass instance of) the parameter type that is passed 
     *   in; false otherwise, including in the event of an exception.
     */
    public static boolean isValidCollectionDataType(String type)
    {
        if (type == null || type.length() < 1)
            return false;
        
        // TODO: should we declare the CNFE and force the invoking class
        //       to handle the exception?
        try
        {
            return (Collection.class).isAssignableFrom(Class.forName(type));
        }
        
        catch (ClassNotFoundException ex)
        {
            // Log exception, but ignore it; we'll just return false
            //  if class is not found (this happens for primitives as well)
            Log.stackTrace(ex);
        }
        
        return false;
    }
    
    /**
     * 
     * @param diagramName diagram name to check for validity
     * @return true if valid diagram name; false otherwise
     */
    public static boolean isDiagramNameValid(String diagramName)
    {
        if (diagramName == null || diagramName.trim().length() == 0)
            return false;
        
        String name = diagramName.trim();
        int count = name.length();
        for (int i=0; i<count; i++)
        {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c))
            {
                if (c == '_' || c== '(' || c==')' || c == '{' || c == '}' || 
                    c == '[' || c == ']' || c == ' ')
                    continue;
                else
                    return false;        
            }
        }
        return true;
        
        //        for (String c : IDS_INVALID_CHARS)
        //        {
        //            if ( diagramName.indexOf(c) != -1)
        //                return false;
        //        }
        //        return true;
    }
    
    /**
     * 
     * @param name Name to check for identifier validity
     * @return true if invalid identifier; false otherwise
     */
    public static boolean invalidIdentifier(String name)
    {
        boolean retval = true;
        
        // Before we look for more complex problems, make sure that this
        // name is not a keyword.
        retval = isKeyword( name );
        
        if ( !retval )
        {
            // First character must be alpha, underscore, or dollar
            // Rest of characters must be alphanum, underscore, or dollar
            
            // isJavaIdentifierStart and isJavaIdentifierPart will support unicode in JDK1.5
            if ( name != null && name.length() > 0 )
            {
                if (Locale.getDefault().getDisplayLanguage().equals("English"))
                {
                    retval = true;
                    char firstchar = name.charAt(0);
                    if ( Character.isJavaIdentifierStart(firstchar))
                    {
                        retval = false;
                        for ( int i = 1; i<name.length() && retval == false; i++ )
                        {
                            char namechar = name.charAt(i);
                            if (!Character.isJavaIdentifierPart(namechar))
                                retval = true;
                        }
                    }
                }
                else
                {
                    retval = true;
                    char firstchar = name.charAt(0);
                    if (Character.isLetter(firstchar) || firstchar=='_' || firstchar=='$')
                    {
                        retval = false;
                        for ( int i = 1; i<name.length() && retval == false; i++ )
                        {
                            char namechar = name.charAt(i);
                            if (!(Character.isLetterOrDigit(namechar) || namechar=='_' || namechar=='$'))
                            {
                                retval = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return retval;
    }
    
    
    
   /**
    *
    * Is the name a keyword in this language
    *
    * @param name [in] The name
    *
    * @return true if the name is a language keyword.
    *
    */
    public static boolean isKeyword(String name)
    {
        boolean retval = false;
        
        try
        {
            ILanguage pLang = getLanguage2();
            
            if (pLang != null)
                retval = pLang.isKeyword(name);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return retval;
    }
    
    
    /**
     *
     * Retrieves the language this processor supports.
     *
     * @return pLang[out] The actual ILanguage associated with this processor
     */
    public static ILanguage getLanguage2()
    {
        ILanguage language = null;
        
        try
        {
            if (language == null)
            {
                String mylang = "Java"; // NOI18N
                
                ICoreProduct pProduct = ProductRetriever.retrieveProduct();
                
                if (pProduct != null)
                {
                    ILanguageManager pManager =
                            pProduct.getLanguageManager();
                    
                    if (pManager != null)
                        language = pManager.getLanguage(mylang);
                }
            }
        }
        
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return language;
    }
    
    
//    public static String[] IDS_INVALID_CHARS = {"\\",  "/",  "*", ":", "?", ".", "&"};
    
    private static SimpleDateFormat cDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat cDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static List types = Arrays.asList("Interface", "Class", "Enumeration");
    
}
