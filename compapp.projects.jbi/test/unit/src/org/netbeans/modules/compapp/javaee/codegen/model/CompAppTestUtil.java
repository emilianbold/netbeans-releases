/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.javaee.codegen.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpatil
 */
public class CompAppTestUtil {
    private static Logger logger = Logger.getLogger(CompAppTestUtil.class.getName());
    
    private static void close(JarFile jarFile) {
        try {
            jarFile.close();
        } catch (Exception ex) {
            //NOP
        }
    }

    private static void close(InputStream is) {
        try {
            is.close();
        } catch (Exception ex) {
            //NOP
        }
    }

    private static boolean compareStreams(InputStream eIs, InputStream aIs) throws IOException {
        boolean ret = true;
        int bufferSize = 1024 * 50;
        byte[] b1 = new byte[bufferSize]; // 50K
        byte[] b2 = new byte[bufferSize]; // 50K
        int read1 = 0;
        int read2 = 0;
        read1 = eIs.read(b1);
        read2 = aIs.read(b2);

        if (read1 != read2) {
            return false;
        } else {
            while (read1 != -1) {
                if (read1 != read2) {
                    ret = false;
                    break;
                }
                if (!Arrays.equals(b1, b2)) {
                    ret = false;
                    break;
                }

                read1 = eIs.read(b1);
                read2 = aIs.read(b2);
            }
        }
        return ret;
    }

    private static boolean isComparableJarEntry(JarEntry expectedEntry) {
        boolean ret = true;
        if (expectedEntry.isDirectory()) {
            ret = false;
        }
        return ret;
    }

    public static boolean compareJar(JarFile expectedJar, JarFile actualJar) {
        boolean ret = true;
        try {
            Enumeration<JarEntry> entries = expectedJar.entries();
            JarEntry expectedEntry = null;
            JarEntry actualEntry = null;
            while (entries.hasMoreElements()) {
                expectedEntry = entries.nextElement();
                if (isComparableJarEntry(expectedEntry)) {
                    actualEntry = actualJar.getJarEntry(expectedEntry.getName());
                    if (actualEntry == null) {
                        ret = false;
                        logger.log(Level.WARNING, "Expected Jar entry not found:" + expectedEntry.getName());
                        break;
                    } else {
                        InputStream eIs = null;
                        InputStream aIs = null;
                        try {
                            eIs = expectedJar.getInputStream(expectedEntry);
                            aIs = actualJar.getInputStream(actualEntry);
                            //logger.log(Level.INFO, "Comparing:" + expectedEntry.getName());
                            if (!compareStreams(eIs, aIs)) {
                                ret = false;
                                logger.log(Level.WARNING, "jar entry compare failed for:" + expectedEntry.getName());
                                break;
                            }
                        } catch (IOException ex) {
                            logger.log(Level.WARNING, "While comparing jar entry:", ex);
                        } finally {
                            close(eIs);
                            close(aIs);
                        }
                    }
                }
            }
        } finally {
            close(expectedJar);
            close(actualJar);
        }

        return ret;
    }
}