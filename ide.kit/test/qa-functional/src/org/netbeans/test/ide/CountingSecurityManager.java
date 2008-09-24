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

package org.netbeans.test.ide;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.security.Permission;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import junit.framework.Assert;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
final class CountingSecurityManager extends SecurityManager implements Callable<Integer> {
    private static int cnt;
    private static StringWriter msgs;
    private static PrintWriter pw;
    private static String prefix;
    private static Map<String,Exception> who = new HashMap<String, Exception>();
    private static Set<String> allowed = Collections.emptySet();
    private static boolean disabled;
    private static SecurityManager man;
    
    public static void initialize(String prefix, Set<String> allowedFiles) {
        if (System.getSecurityManager() instanceof CountingSecurityManager) {
            // ok
        } else {
            System.setSecurityManager(new CountingSecurityManager());
        }
        setCnt(0);
        msgs = new StringWriter();
        pw = new PrintWriter(msgs);
        CountingSecurityManager.prefix = prefix;
        allowed = allowedFiles;
    }

    @Override
    public String toString() {
        return msgs.toString();
    }

    public Integer call() throws Exception {
        return cnt;
    }
    
    public static void assertCounts(String msg, int expectedCnt) throws Exception {
        int c = (Integer)((Callable<?>)System.getSecurityManager()).call();
        Assert.assertEquals(msg + "\n" + System.getSecurityManager().toString(), expectedCnt,c);
        setCnt(0);
        msgs = new StringWriter();
        pw = new PrintWriter(msgs);
    }

    /**
     * @return the cnt
     */
    public static int getCnt() {
        return cnt;
    }

    /**
     * @param aCnt the cnt to set
     */
    public static void setCnt(int aCnt) {
        cnt = aCnt;
    }

    @Override
    public void checkPermission(Permission p) {
        if (disabled) {
            return;
        }
        if (p instanceof RuntimePermission && "setSecurityManager".equals(p.getName())) {
            try {
                ClassLoader l = Thread.currentThread().getContextClassLoader();
                Class<?> manClass = Class.forName("org.netbeans.TopSecurityManager", false, l);
                man = (SecurityManager) manClass.newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            throw new SecurityException();
        }
    }

    @Override
    public final void checkPropertyAccess(String x) {
        if (man != null) {
            man.checkPropertyAccess(x);
        }
    }
    
    @Override
    public void checkRead(String file) {
        /*
        if (file.startsWith(prefix)) {
            cnt++;
            pw.println("checkRead: " + file);
            new Exception().printStackTrace(pw);
        }
         */
    }

    @Override
    public void checkRead(String file, Object context) {
        /*
        if (file.startsWith(prefix)) {
            cnt++;
            pw.println("checkRead2: " + file);
        }
         */
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        //setCnt(getCnt() + 1);
        //pw.println("Fd: " + fd);
    }

    @Override
    public void checkWrite(String file) {
        if (acceptFile(file)) {
            setCnt(getCnt() + 1);
            pw.println("checkWrite: " + file);
            if (who.get(file) == null) {
                Exception now = new Exception("checkWrite: " + file);
                who.put(file, now);
                now.printStackTrace(pw);
            }
        }
    }

    @Override
    public void checkDelete(String file) {
        if (acceptFile(file)) {
            setCnt(getCnt() + 1);
            pw.println("checkDelete: " + file);
        }
    }
    
    private boolean acceptFile(String file) {
        String ud = System.getProperty("netbeans.user");
        if (ud == null) {
            // still initializing
            return false;
        }
        if (!file.startsWith(ud)) {
            return false;
        }

        String f = file.substring(ud.length()).replace(File.separatorChar, '/');
        if (f.contains("config/Modules")) {
            return false;
        }
        if (f.contains("config/Windows2Local")) {
            return false;
        }
        if (f.endsWith(".hg")) {
            try {
                Class<?> ref = Class.forName("org.netbeans.modules.versioning.util.Utils", true, Thread.currentThread().getContextClassLoader());
                Field unver = ref.getDeclaredField("unversionedFolders");
                unver.setAccessible(true);
                unver.set(null, new File[]{new File(ud).getParentFile()});
                return false;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        
        if (file.startsWith(ud)) {
            if (f.startsWith("/")) {
                f = f.substring(1);
            }
            if (allowed.contains(f)) {
                return false;
            }
        }

        return prefix == null || file.startsWith(prefix);
    }

    @Override
    public void checkExec(String cmd) {
        if (cmd.contains("chmod")) {
            return;
        }
        if (cmd.equals("hg")) {
            return;
        }
        if (cmd.endsWith("/hg")) {
            return;
        }

        super.checkExec(cmd);
        setCnt(getCnt() + 1);
        pw.println("checkExec: " + cmd);
        new Exception().printStackTrace(pw);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
    }
}
