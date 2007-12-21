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

package org.netbeans.modules.iep.model.lib;

import java.awt.Color;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * DOCUMENT ME!
 *
 * @author  Bing Lu
 */
public class GenUtil {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(GenUtil.class.getName());

    /** DOCUMENT ME!  */
    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("yyyy.MM.dd 'at' hh:mm:ss z");

    /**
     * DOCUMENT ME!
     *
     * @param file      file
     * @param ancestor  ancestor
     * @return          DOCUMENT ME!
     *      class
     */
    public static boolean isAncestor(File file, File ancestor) {

        if ((file == null) || (ancestor == null)) {
            return false;
        }

        File p = file;

        while (true) {
            if (p != null) {
                if (p.equals(ancestor)) {
                    return true;
                }

                p = p.getParentFile();
            } else {
                return false;
            }
        }
    }

    /**
     * Returns the relative path from file to ancestor. Returns null if file or
     * ancestor is null, or ancestor is not an ancestor of file
     *
     * @param file      the descendent file
     * @param ancestor  the anscestor file
     * @return          The relativePath value
     */
    public static String getRelativePath(File file, File ancestor) {

        if ((file == null) || (ancestor == null)) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        File p = file;

        while (true) {
            if (p != null) {
                if (p.equals(ancestor)) {

                    // remove the first '/' charactor
                    return reverseString(sb.substring(1), '/');
                }

                sb.append("/" + p.getName());

                p = p.getParentFile();
            } else {
                return null;
            }
        }
    }

    /**
     * Return the fields from a delimiter Separated List. For example: ("a,b,c",
     * ",") -> String[]{"a", "b", "c"}
     *
     * @param s      the string to parse
     * @param delim  what separates the tokens in the string
     * @return       an array of the tokens that were extracted from the given
     *      string.
     */
    public static String[] getTokens(String s, String delim) {
        String[] r = null;
        if (s == null) {
            r = new String[0];
        } else {
            StringTokenizer st = new StringTokenizer(s, delim);
            r = new String[st.countTokens()];
            for (int i = 0; i < r.length; i++) {
                r[i] = st.nextToken();
            }
        }
        return r;
    }

    /**
     * Creates the file
     *
     * @param file   java.io.File
     * @param isDir  Description of the Parameter
     * @return       boolean
     */
    public static boolean createFile(File file, boolean isDir) {

        boolean ok = true;

        if (file != null) {
            if (isDir) {
                if (!file.exists()) {
                    ok = file.mkdirs();
                }
            } else {
                if (!file.exists()) {
                    File dir = file.getParentFile();

                    if (dir != null) {
                        if (!dir.exists()) {
                            ok = dir.mkdirs();
                        }
                    }

                    try {
                        file.createNewFile();
                    } catch (Exception e) {
                        ok = false;
                    }
                }
            }
        }

        return ok;
    }

    /**
     * DOCUMENT ME!
     *
     * @param time  Description of the Parameter
     * @return      example result: 1996.07.10 at 15:08:56 PDT
     */
    public static String formatTime(long time) {
        return SDF.format(new Date(time));
    }

    /**
     * Given directory root exists, create all the components on the path
     * relative to root. The pkg is a '.' seperated list of subpackage names
     * Returns the File.separatorChar seperated absolute path Example: On
     * Windows platform, mkdir("c:\\r", "a.b.c") will create c:\\r\\a\\b\\c
     *
     * @param root  Description of the Parameter
     * @param pkg   Description of the Parameter
     * @return      Description of the Return Value
     */
    public static String mkdir(String root, String pkg) {

        StringBuffer sb = new StringBuffer(root);
        StringTokenizer st = new StringTokenizer(pkg, ".");

        while (st.hasMoreTokens()) {
            sb.append(File.separatorChar);
            sb.append(st.nextToken());

            File d = new File(sb.toString());

            d.mkdir();
        }

        return sb.toString();
    }

    /**
     * Constructs a list containing the elements of the specified array, in the
     * order they are in the original array The <tt>ArrayList</tt> instance has
     * an initial capacity of 110% the size of the specified collection. If a is
     * null or a has length 0, then constructs a list containing no element. The
     * list returned can be changed independently from the original array a. For
     * example, one can add/remove elements from the list without affecting
     * array a.
     *
     * @param a  the array whose elements are to be placed into this list.
     * @return   Description of the Return Value
     */
    public static ArrayList newArrayList(Object[] a) {

        ArrayList list = new ArrayList();

        if ((a == null) || (a.length == 0)) {
            return list;
        }

        for (int i = 0, ii = a.length; i < ii; i++) {
            list.add(a[i]);
        }

        return list;
    }

    /**
     * Constructs a map containing the elements of the specified array, in such
     * a way that the 2k-th element is the key for the(2k+1)-th element The <tt>
     * HashMap</tt> instance has default load factor (0.75). If a is null or a
     * has length 0, then constructs a map containing no element. If a has odd
     * length, then a's last element is ignored. The map returned can be changed
     * independently from the original array a. For example, one can add/remove
     * elements from the map without affecting array a
     *
     * @param a  the array whose elements are to be placed into this map.
     * @return   Description of the Return Value
     */
    public static HashMap newHashMap(Object[] a) {

        HashMap map = new HashMap();

        if ((a == null) || (a.length == 0)) {
            return map;
        }

        for (int i = 0, ii = GenUtil.quotient(a.length, 2); i < ii; i++) {
            map.put(a[2 * i], a[(2 * i) + 1]);
        }

        return map;
    }

    /**
     * Returns "" if s is null. Else returns s as is
     *
     * @param s  Description of the Parameter
     * @return   Description of the Return Value
     */
    public static String null2EmptyStr(String s) {

        return (s == null)
                 ? ""
                 : s;
    }

    /**
     * Parses the full name of a class and returns the package and classname.
     *
     * @param fullName  the full name of the class to parse
     * @return          an array whose first element contains the package that
     *      the class is in or null if no package is specified and then 2nd
     *      element contains the name of the class
     */
    public static String[] parseClassName(String fullName) {

        String[] ret = new String[2];
        int idx = fullName.lastIndexOf('.');

        if (idx >= 0) {
            ret[0] = fullName.substring(0, idx);
            ret[1] = fullName.substring(idx + 1);
        } else {
            ret[0] = null;
            ret[1] = fullName;
        }

        return ret;
    }

    /**
     * The pkg is a '.' seperated list of subpackage names Returns the
     * File.separatorChar seperated absolute path Example: On Windows platform,
     * pkg2path("a.b.c") will return a\\b\\c
     *
     * @param pkg  Description of the Parameter
     * @return     Description of the Return Value
     */
    public static String pkg2path(String pkg) {

        if (pkg.indexOf('.') < 0) {
            return pkg;
        }

        String[] pkgComponents = getTokens(pkg, ".");
        StringBuffer sb = new StringBuffer();

        for (int i = 0, ii = pkgComponents.length; i < ii; i++) {
            sb.append(pkgComponents[i]);
            sb.append(File.separatorChar);
        }

        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /** print out the stack trace without throwing any exceptions  */
    public static void printStackTrace() {

        Exception e = new Exception();

        e.fillInStackTrace();
        e.printStackTrace();
    }

    /**
     * Returns the quotient of dividend / divider Example: returns 1 for 3/2 and
     * 2/2
     *
     * @param dividend  the dividend
     * @param divider   the divider
     * @return          Description of the Return Value
     */
    public static int quotient(int dividend, int divider) {
        return (dividend - (dividend % divider)) / divider;
    }

    /**
     * Delegate to String.replace(oldChar, newChar) to support char replacement
     * for Velocity engine
     *
     * @param s        the String to operate on
     * @param oldChar  the old character.
     * @param newChar  the new character.
     * @return         a string derived from this string by replacing every
     *      occurrence of <code>oldChar</code> with <code>newChar</code>.
     */
    public static String replace(String s, String oldChar, String newChar) {
        return s.replace(oldChar.charAt(0), newChar.charAt(0));
    }

    /**
     * getReverse() this method reverses the order of the file returned by
     * GenUtil.getAbsolutePath()
     *
     * @param sourceString  sourceString
     * @param delimiter     delimiter
     * @return              String
     */
    public static String reverseString(String sourceString, char delimiter) {

        if (sourceString == null) {
            return null;
        }

        String delimiter1 = new String(new char[]{delimiter});
        StringTokenizer strToken1 = new StringTokenizer(sourceString,
                delimiter1);
        String retVal = "";

        while (strToken1.hasMoreTokens()) {
            String currToken1 = (String) strToken1.nextToken();

            retVal = delimiter1 + currToken1 + retVal;
        }

        return retVal.substring(1);
    }

    /**
     * Recursively delete given directory and its subdirectories.
     *
     * @param dir  Description of the Parameter
     */
    public static void rmdir(File dir) {

        if (dir.isDirectory()) {
            File[] contents = dir.listFiles();

            for (int i = 0; i < contents.length; i++) {
                if (contents[i].isDirectory()) {
                    rmdir(contents[i]);
                } else {
                    if (!contents[i].delete()) {
                        mLog.warning("Couldn't delete: " + contents[i].getPath());
                    }
                }
            }
        }

        // dir.delete() returns true if dir is successfully deleted
        if (!dir.delete()) {
            mLog.warning("Couldn't delete: " + dir.getPath());
        }
    }

    /**
     * Returns a string of len number of spaces
     *
     * @param len  Description of the Parameter
     * @return     Returns a string of len number of spaces
     */
    public static String spaces(int len) {

        char[] c = new char[len];

        for (int i = 0; i < len; i++) {
            c[i] = ' ';
        }

        return new String(c);
    }

    /**
     * Chop the last portion of the given string off at the right most
     * separator
     *
     * @param s the string to chop
     * @param separator the separator to remove along with the text after it
     *
     * @return the chopped string
     */
    public static String chop(String s, char separator) {
        String ret = "";
        if (s != null) {
            int rindex = s.lastIndexOf(separator);
            if (rindex >= 0) {
                ret = s.substring(0, rindex);
            }
        }
        return ret;
    }

    /**
     * Split the given string after the nth deliminator. For example: ("a,b,c",
     * ",", 2) -> String[]{"a.b", "c"}
     *
     * @param s      the string to parse
     * @param delim  what separates the tokens in the string
     * @param nth    which deliminator to split on. If the value is negative
     *      then it will be counted from the right.
     * @return       an array containing the text to the left of the stated
     *      token and the the text to the right of the stated token.
     */
    public static String[] split(String s, String delim, int nth) {

        StringTokenizer st = new StringTokenizer(s, delim);
        int n = nth;

        if (nth < 0) {
            n = st.countTokens() + nth;
        }

        String[] r = new String[2];
        StringBuffer left = new StringBuffer();
        StringBuffer right = new StringBuffer();

        for (int i = 0; (i < n) && st.hasMoreTokens(); i++) {
            if (i > 0) {
                left.append(delim);
            }

            left.append(st.nextToken());
        }

        r[0] = left.toString();

        boolean first = true;

        while (st.hasMoreTokens()) {
            if (!first) {
                right.append(delim);
            } else {
                first = false;
            }

            right.append(st.nextToken());
        }

        r[1] = right.toString();

        return r;
    }

    /**
     * Unzip a file in zip format or Jar format
     *
     * @param zipFileFullName  zip file's full name
     * @param destDirFullName  destination directory to unzip the zip file
     * @return                 a list of unziped file full names
     * @exception Exception    Description of the Exception
     */
    public static List unzip(String zipFileFullName, String destDirFullName)
             throws Exception {

        List fileList = new ArrayList();
        InputStream in =
                new BufferedInputStream(new FileInputStream(zipFileFullName));
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry e = null;

        // zin.getNextEntry() reads the next ZIP file entry and
        // positions stream at the beginning of the entry data.
        while ((e = zin.getNextEntry()) != null) {
            String fileToCreate = e.getName();
            String filename = e.getName();
            int index = 0;

            index = filename.indexOf('/');

            if (index != -1) {
                filename = filename.replace('/', File.separatorChar);
            }

            if ((filename.startsWith(File.separator))) {
                fileToCreate = destDirFullName + filename;
            } else {
                fileToCreate = destDirFullName + File.separator + filename;
            }

            File toCreate = new File(fileToCreate);

            createFile(toCreate, e.isDirectory());

            if (!toCreate.isDirectory()) {
                fileList.add(fileToCreate);

                FileOutputStream out = new FileOutputStream(toCreate);
                byte[] b = new byte[512];
                int len = 0;

                while ((len = zin.read(b)) != -1) {
                    out.write(b, 0, len);
                }

                out.close();
            }
        }

        zin.close();

        return fileList;
    }

    /**
     * Obtain a String guaranteed not to be <code>null</code>
     * @param s the string to process
     * @return the processed string
     */
    public static String safeStr(String s) {
        return s == null ? "" : s;
    }

    /**
     * Obtain an array as a String guaranteed not to be <code>null</code>
     * @param sa the string array to process
     * @return the processed string
     */
    public static String safeStrArray(String[] sa) {
        StringBuffer buf = new StringBuffer();
        if (sa != null) {
            for (int i = 0; i < sa.length; i++) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append('<');
                buf.append(sa[i]);
                buf.append('>');
            }
        }
        return buf.toString();
    }
}

/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/

/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/

