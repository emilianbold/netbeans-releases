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
// RAVE only file!

package org.netbeans.modules.visualweb.extension.openide.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Copied from openide/io/src/org/openide/io where it may not be.
/**
 * Class <code>FileCopy</code> is an utility class to be used
 * whereever files need to be copied.  There are a lot of different
 * places where this happens and each place did it differently.
 *
 * Currently we have a problem with
 * <code>java.nio.channels.FileChannel.transferTo</code> in the j2se
 * 1.4.2 and the Linux kernel > 2.4.*.  So creating the central class
 * to do the copies seems like a good way to go.
 *
 * @author <a href="mailto:marco.walther@sun.com">Marco Walther</a>
 * @version 1.0
 */
public class RaveFileCopy {
    private static boolean use_nio = true;

    /**
     * Method <code>fileCopy</code> is the only usable method of this
     * class.
     *
     * @param src a <code>File</code> value
     * @param dest a <code>File</code> value
     * @return a <code>boolean</code> value. This is <code>true</code>
     * if the copy was successful or <code>false</code> if there was
     * no copy.
     * @exception IOException if an error occurs
     */
    public static boolean fileCopy(File src, File dest) throws IOException {
        if (!src.exists() || dest.exists()) {
            return false;
        } // end of if (!src.exists() || dest.exists())

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        dest.createNewFile();

        if (use_nio) {
            FileChannel in = new FileInputStream(src).getChannel();
            FileChannel out = new FileOutputStream(dest).getChannel();
            in.transferTo(0, in.size(), out);
            in.close();
            out.close();
        } // end of if (use_nio)
        else {
            int bufferSize = 64 * 1024;
            int bytes;
            byte[] buffer = new byte[bufferSize];

            InputStream is = new
                BufferedInputStream(new FileInputStream(src),
                                    bufferSize);
            OutputStream os = new
                BufferedOutputStream(new FileOutputStream(dest),
                                     bufferSize);

            while ((bytes = is.read(buffer, 0, bufferSize)) > -1) {
                os.write(buffer, 0, bytes);
            } // end of while ((bytes = is.read(buffer, 0,
              //    bufferSize)) > -1)

            os.flush();
            os.close();
            is.close();
        } // end of if (use_nio) else

        return true;
    }


    static {
        try {
            if (System.getProperty("os.name"). // NOI18N
                equalsIgnoreCase("Linux")) { // NOI18N

                Pattern p = Pattern.compile("(\\d)\\.(\\d+)\\..*"); // NOI18N
                Matcher m = p.matcher(System.getProperty("os.version")); // NOI18N
                if (m.matches()) {
                    if (Integer.parseInt(m.group(1)) == 2 &&
                        Integer.parseInt(m.group(2)) > 4) {
                                // We know about the problem with the
                                // Linux 2.6.* release kernels.  But
                                // the change was probably introduced
                                // somewhen in the 2.5.* development
                                // kernels.
                        use_nio = false;
                    } // end of if (Integer.parseInt(m.group(1)) == 2 &&
                      //    Integer.parseInt(m.group(2)) > 4)
                } // end of if (m.matches())
            } // end of if (System.getProperty("os.name").
              //    equalsIgnoreCase("Linux"))
        
            if (System.getProperty("openide.noniocopy") != null) { // NOI18N
                use_nio = false;
            } // end of if (System.getProperty("openide.noniocopy") !=
              //    null)
        
            if (System.getProperty("openide.niocopy") != null) { // NOI18N
                use_nio = true;
            } // end of if (System.getProperty("openide.niocopy") != null)
        } catch (Exception e) {
            use_nio = false;
        } // end of try-catch
    }
}
