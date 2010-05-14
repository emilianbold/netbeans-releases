/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.project.anttasks;

import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

public class IOUtil {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger("com.sun.jbi.ui.devtool.tcg.util");

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    // UTF-8
    public static void encode(
        InputStream input, String srcEncoding, OutputStream output, String targetEncoding)
        throws IOException {
        Reader in = new InputStreamReader(
            input, srcEncoding);
        Writer out = new OutputStreamWriter(
            output, targetEncoding);
        copy(in, out);
    }

    // UTF-8
    public static byte[] encode(
        byte[] srcBuf, String srcEncoding, String targetEncoding)
        throws IOException {
        String s = new String(srcBuf, srcEncoding);
        byte[] ret = s.getBytes(targetEncoding);
        return ret;
    }

    /**
     * Copys the input stream to the output stream
     *
     * @param input            the input stream
     * @param output           the output stream
     * @exception IOException  Description of the Exception
     */
    public static void copy(InputStream input, OutputStream output)
        throws IOException {
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        int n = 0;
        while ((n = input.read(buf)) != -1) {
            output.write(buf, 0, n);
        }
        output.flush();
    }

    /**
     * Copys the input bytes to the output stream
     *
     * @param input            the input bytes
     * @param output           the output stream
     * @exception IOException  Description of the Exception
     */
    public static void copy(byte[] input, OutputStream output)
        throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        copy(in, output);
    }

    /**
     * Copys the input stream to the output stream
     *
     * @param input            the input stream
     * @param output           the output stream
     * @exception IOException  Description of the Exception
     */
    public static void copy(Reader input, Writer output)
        throws IOException {
        char[] buf = new char[DEFAULT_BUFFER_SIZE];
        int n = 0;
        while ((n = input.read(buf)) != -1) {
            output.write(buf, 0, n);
        }
        output.flush();
    }

    /**
     * Copys the input bytes to the output stream
     *
     * @param input            the input bytes
     * @param output           the output stream
     * @exception IOException  Description of the Exception
     */
    public static void copy(byte[] input, Writer output)
        throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        copy(in, output);
    }

    /**
     * Copys the input stream to the output stream
     *
     * @param input            the input stream
     * @param output           the output stream
     * @exception IOException  Description of the Exception
     */
    public static void copy(InputStream input, Writer output)
        throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    /**
     * Copys the input stream to the output stream
     *
     * @param input            the input stream
     * @param output           the output stream
     * @exception IOException  Description of the Exception
     */
    public static void copy(Reader input, OutputStream output)
        throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(output);
        copy(input, out);
    }

    /**
     * Returns the contents of the input stream as a String
     *
     * @param input            the input stream
     * @return                 The text value
     * @exception IOException  Description of the Exception
     */
    /*public static String getText(InputStream input)
        throws IOException {
        StringWriter out = new StringWriter();
        copy(input, out);
        String ret = out.toString();
        //mLog.debug("ret: " + ret);
        return ret;
    }*/

    // UTF-8
    public static String getText(InputStream input, String srcEncoding)
        throws IOException {
        InputStreamReader in = new InputStreamReader(input, srcEncoding);
        StringWriter out = new StringWriter();
        copy(in, out);
        String ret = out.toString();
        //mLog.debug("ret: " + ret);
        return ret;
    }

    public static String getText(String name, String srcEncoding)
        throws IOException {
        InputStream inputStream = null;
        String ret = null;
        try {
            inputStream = getResourceAsStream(name);
            ret = getText(inputStream, srcEncoding);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        //mLog.debug("ret: " + ret);
        return ret;
    }
    
    /**
     * Returns the contents of the input stream as a String
     *
     * @param input            the input stream
     * @return                 The text value
     * @exception IOException  Description of the Exception
     */
    public static String getText(Reader input)
        throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw);
        return sw.toString();
    }

    /**
     * Returns the contents of the input stream as a byte array
     *
     * @param input            the input stream
     * @return                 The byte array
     * @exception IOException  Description of the Exception
     */
    public static byte[] getBytes(InputStream input)
        throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static byte[] getBytes(String name)
        throws IOException {
        return getBytes(getResourceAsStream(name));
    }

    /**
     * Returns the contents of the input stream as a byte array
     *
     * @param input            the input stream
     * @return                 The byte array
     * @exception IOException  Description of the Exception
     */
    public static byte[] getBytes(Reader input)
        throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    //==========================================================================
    public static InputStream getResourceAsStream(String name) {
        InputStream ret = null;
        try {
            if (new java.io.File(name).exists()) {
                ret = new java.io.FileInputStream(name);
            } else {
                ret = new java.net.URL(name).openStream();
            }
        } catch (Exception e) {
            ret = IOUtil.class.getClassLoader(
                ).getResourceAsStream(name);
            if (ret == null) {
                mLog.warning("resource " + name + " not found");
            } else {
                //mLog.debug("IOUtil.getResourceAsStream " + name + " found");
            }
        }
        return ret;
    }

    public static Enumeration getResources() {
        return null; // ClassLoader.getResources(name);
    }

    public static void unzip(String zipFilePath, String targetDirPath)
        throws IOException {
        ZipFile zipFile = new ZipFile(zipFilePath);
        for (Enumeration e = zipFile.entries(); e.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) e.nextElement();

            String path;
            if (targetDirPath != null) {
                path = targetDirPath + "/" + entry.getName();
            } else {
                path = entry.getName();
            }

            if (entry.isDirectory()) {
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            } else {
                InputStream input =
                    new BufferedInputStream(zipFile.getInputStream(entry));
                OutputStream output =
                    new BufferedOutputStream(
                        new FileOutputStream(path));
                copy(input, output);
                input.close();
                output.close();
            }
        }
        zipFile.close();
    }
    
    public static final void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java IOUtil zipfile targetdirectory");
        } else {
            try {
                unzip(args[0], args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //mLog.debug("args " + args[0]);
        //mLog.debug(IOUtil.class.getClassLoader().getResource(args[0]));
        //mLog.debug(IOUtil.getResourceAsStream(args[0]));
    }
}
