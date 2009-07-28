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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.openide.filesystems;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarOutputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;

/**
 * @author  Jiri Skrivanek
 */
public class JarFileSystemPerfTest extends NbTestCase {

    /** Creates new JarFileSystemPerfTest */
    public JarFileSystemPerfTest(String name) {
        super(name);
    }

    /** Tests that delay before closing of the jar file is adaptively adjusted
     * according to time between request for reopening of the jar file
     * (see #167527).
     */
    public void testAdaptiveCloseDelay() throws Exception {
        final AtomicInteger openCount = new AtomicInteger(0);
        Logger logger = Logger.getLogger(JarFileSystem.class.getName());
        logger.setLevel(Level.FINEST);
        logger.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().startsWith("opened:")) {
                    openCount.incrementAndGet();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });

        File f = new File(getWorkDir(), "jfstest.jar");
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
        String entryName = "file1";
        jos.putNextEntry(new ZipEntry(entryName));
        jos.close();

        JarFileSystem jfs = new JarFileSystem();
        jfs.setJarFile(f);

        jfs.size(entryName);  // just to call JarFileSystem.reOpenJarFile
        assertEquals(1, openCount.get());

        jfs.size(entryName);
        assertEquals(1, openCount.get());

        Thread.sleep(500);  //expects JarFileSystem.CLOSE_DELAY_MIN == 300
        jfs.size(entryName);
        assertEquals("Should be reopened.", 2, openCount.get());

        Thread.sleep(500);  //expects JarFileSystem.CLOSE_DELAY_MIN == 300
        jfs.size(entryName);
        assertEquals("Should not be reopened - closeDelay not adjusted.", 2, openCount.get());

        jfs.size(entryName);
        assertEquals("Should not be reopened.", 2, openCount.get());

        Thread.sleep(500);  //expects JarFileSystem.CLOSE_DELAY_MIN == 300
        jfs.size(entryName);
        assertEquals("Should be reopened - closeDelay not adjusted back to min value.", 3, openCount.get());
    }
}
