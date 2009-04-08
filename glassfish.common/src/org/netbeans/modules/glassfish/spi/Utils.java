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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.spi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vbk
 */
public class Utils {

    /**
     * A canWrite test that may tell the truth on Windows.
     *
     * This is a work around for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
     * @param f the file or directory to test
     * @return true when the file is writable...
     */
    public static boolean canWrite(File f) {
        if (org.openide.util.Utilities.isWindows()) {
            // File.canWrite() has lots of bogus return cases associated with
            // read-only directories and files...
            boolean retVal = true;
            java.io.File tmp = null;
            if (f.isDirectory()) {
                try             {
                    tmp = java.io.File.createTempFile("foo", ".tmp", f);
                }
                catch (IOException ex) {
                    // I hate using exceptions for flow of control
                    retVal = false;
                } finally {
                    if (null != tmp) {
                        tmp.delete();
                    }
                }
            } else {
                java.io.FileOutputStream fos = null;
                try {
                    fos = new java.io.FileOutputStream(f, true);
                }
                catch (FileNotFoundException ex) {
                    // I hate using exceptions for flow of control
                    retVal = false;
                } finally {
                    if (null != fos) {
                        try {
                            fos.close();
                        } catch (java.io.IOException ioe) {
                            Logger.getLogger(Utils.class.getName()).log(Level.FINEST,
                                    null, ioe);
                        }
                    }
                }
            }
            return retVal;
        } else {
            // we can actually trust the canWrite() implementation...
            return f.canWrite();
        }
    }

}
