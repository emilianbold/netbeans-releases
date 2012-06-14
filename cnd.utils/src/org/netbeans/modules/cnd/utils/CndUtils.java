/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.utils;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Alexey Vladykin
 */
public class CndUtils {

    private static final Logger LOG = Logger.getLogger("cnd.logger"); // NOI18N

    private static boolean releaseMode;
    private static volatile Exception lastAssertion;

    static {
        String text = System.getProperty("cnd.release.mode");
        if (text == null) {
            releaseMode = true;
            assert ((releaseMode = false) == false);
        } else {
            releaseMode = Boolean.parseBoolean(text);
        }
    }

    private CndUtils() {
    }

    public static boolean isStandalone() {
        if ("true".equals(System.getProperty ("cnd.command.line.utility"))) { // NOI18N
            return true;
        }
        return !CndUtils.class.getClassLoader().getClass().getName().startsWith("org.netbeans."); // NOI18N
    }
    
    public static boolean isReleaseMode() {
        return releaseMode;
    }

    public static boolean isDebugMode() {
        return ! isReleaseMode();
    }

    public static boolean isUnitTestMode() {
        return Boolean.getBoolean("cnd.mode.unittest"); // NOI18N
    }

    public static boolean isCodeCompletionUnitTestMode() {
        return Boolean.getBoolean("cnd.mode.completion.unittest"); // NOI18N
    }

    public static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if (text != null) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }

    public static void threadsDump(){
        final Set<Entry<Thread, StackTraceElement[]>> stack = Thread.getAllStackTraces().entrySet();
        System.err.println("-----Start Thread Dump-----");
        for (Map.Entry<Thread, StackTraceElement[]> entry : stack) {
            System.err.println(entry.getKey().getName());
            for (StackTraceElement element : entry.getValue()) {
                System.err.println("\tat " + element.toString());
            }
            System.err.println();
        }
        System.err.println("-----End Thread Dump-----");
    }

    public static void assertTrue(boolean value) {
        if (!value && isDebugMode()) {
            severe("Assertion error"); //NOI18N
        }
    }

    public static void assertNotNull(Object object, String message) {
        if (object == null && isDebugMode()) {
            severe(message);
        }
    }

    public static void assertNotNull(Object object, CharSequence prefix, Object message) {
        if (object == null && isDebugMode()) {
            severe(prefix.toString() + message);
        }
    }

    public static void assertNotNullInConsole(Object object, String message) {
        if (object == null && isDebugMode()) {
            info(message);
        }
    }

    public static void assertNull(Object object, String message) {
        if (object != null && isDebugMode()) {
            severe(message);
        }
    }

    public static int getNumberCndWorkerThreads() {
        int threadCount = Math.min(4, Runtime.getRuntime().availableProcessors()-2);
        if (System.getProperty("cnd.modelimpl.parser.threads") != null) { // NOI18N
            threadCount = Integer.getInteger("cnd.modelimpl.parser.threads").intValue(); // NOI18N
        }
        return Math.max(threadCount, 1);
    }

    public static int getConcurrencyLevel() {
        return getNumberCndWorkerThreads();
    }

    public static void assertFalse(boolean value) {
       if (value && isDebugMode()) {
           severe("Assertion error"); //NOI18N
       }
   }

    public static void assertFalse(boolean value, String message) {
        assertTrue(!value, message);
    }

    public static void assertTrue(boolean value, String message) {
        if (isDebugMode() && !value) {
            severe(message);
        }
    }

    public static void assertTrue(boolean value, String prefix, Object message) {
        if (isDebugMode() && !value) {
            LOG.log(Level.SEVERE, prefix + message, lastAssertion = new Exception(prefix + message));
        }
    }

    private static void severe(String message) {
        LOG.log(Level.SEVERE, message, lastAssertion = new Exception(message));
    }

    private static void info(String message) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, message, lastAssertion = new Exception(message));
        }
    }
    
    public static void assertTrueInConsole(boolean value, String message) {
        if (isDebugMode() && !value && LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, message, lastAssertion = new Exception(message));
        }
    }

    public static void assertTrueInConsole(boolean value, String prefix, Object message) {
        if (isDebugMode() && !value && LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, prefix + message, lastAssertion = new Exception(prefix + message));
        }
    }

    public static Exception getLastAssertion() {
        return lastAssertion;
    }

    public static void clearLastAssertion() {
        lastAssertion = null;
    }

    public static void assertAbsolutePathInConsole(String path) {
        if (CndUtils.isDebugMode()) {
            if (! CndPathUtilitities.isPathAbsolute(path)) {
                CndUtils.assertTrueInConsole(false, "path must be absolute " + path);
            }
        }
    }

    public static void assertAbsolutePathInConsole(String path, String message) {
        if (CndUtils.isDebugMode()) {
            if (! CndPathUtilitities.isPathAbsolute(path)) {
                CndUtils.assertTrueInConsole(false, message + ' ' + path);
            }
        }
    }

    public static void assertAbsoluteFileInConsole(File file) {
        assertAbsoluteFileInConsole(file, "Absolute path should be used"); //NOI18N
    }

    public static void assertAbsoluteFileInConsole(File file, String message) {
        if (CndUtils.isDebugMode()) {
            if (! file.isAbsolute()) {
                CndUtils.assertTrueInConsole(false, message + ' ' + file.getPath());
            }
        }
    }

    public static void assertNonUiThread() {
        assertFalse(SwingUtilities.isEventDispatchThread(), "Should not be called from UI thread"); //NOI18N
    }

    public static void assertUiThread() {
        assertTrue(SwingUtilities.isEventDispatchThread(), "Should be called only from UI thread"); //NOI18N
    }

    public static void assertNormalized(File file) {
        if (isDebugMode()) {
            File normFile = CndFileUtils.normalizeFile(file);
            if (!file.equals(normFile)) {
                assertTrueInConsole(false, "Parameter file was not normalized. Was " + file + " instead of " + normFile); // NOI18N
            }
        }
    }

    public static void assertNormalized(FileSystem fs, CharSequence absPath) {
        if (isDebugMode()) {
            String normFile = CndFileUtils.normalizeAbsolutePath(fs, absPath.toString());
            if (!normFile.contentEquals(absPath)) {
                assertTrueInConsole(false, "Parameter file was not normalized. Was " + absPath + " instead of " + normFile); // NOI18N
            }
        }
    }
    
    public static Logger getLogger() {
        return LOG;
    }
}
