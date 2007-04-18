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

/*
 * Util.java
 *
 * Created on March 25, 2005, 2:56 PM
 */

package org.netbeans.modules.compapp.catd.util;

import java.io.*;

/**
 *
 * @author blu
 */
public class Util {

    public static String getFileContent(File f) {
        String ret = null;
        InputStreamReader input = null;
        StringWriter output = null;
        try {
            input = new InputStreamReader(new FileInputStream(f), "UTF-8");
            output = new StringWriter();
            char[] buf = new char[1024];
            int n = 0;
            while ((n = input.read(buf)) != -1) {
                output.write(buf, 0, n);
            }
            output.flush();
            ret = output.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return ret;
    }

    public static String getFileContentWithoutCRNL(File f) {
        String ret = null;
        InputStreamReader input = null;
        StringWriter output = null;
        try {
            input = new InputStreamReader(new FileInputStream(f), "UTF-8");
            output = new StringWriter();
            char[] buf = new char[1024];
            int n = 0;
            while ((n = input.read(buf)) != -1) {
                output.write(buf, 0, n);
            }
            output.flush();
            ret = output.toString();
            ret = ret.replaceAll("\n","");
            ret = ret.replaceAll("\r","");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return ret;
    }


    public static String replaceAll(String s, String match, String replacement) {
        StringBuffer sb = new StringBuffer();
        String temp = s;
        while (true) {
            int i = temp.indexOf(match);
            if (i < 0) {
                sb.append(temp);
                return sb.toString();
            }
            sb.append(temp.substring(0, i));
            sb.append(replacement);
            temp = temp.substring(i + match.length());
        }
    }

}
