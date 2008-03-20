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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileDescriptor;
import java.security.Permission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;

/**
 *
 * @author rmatous
 */
public class StatFiles extends SecurityManager {

    public static final int ALL = 0;
    public static final int READ = 1;
    public static final int WRITE = 2;
    public static final int DELETE = 3;
    private Results results;
    private Monitor monitor;

    StatFiles() {
        reset();
    }

    void reset() {
        results = new Results();
    }
    
    Results getResults() {
        return results;
    }

    void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void checkPermission(Permission perm) {
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        super.checkRead(fd);
    }

    @Override
    public void checkRead(String file) {
        super.checkRead(file);
        File f = new File(file);
        if (!canBeSkipped()) {
            if (monitor != null) {
                monitor.checkRead(f);
                monitor.checkAll(f);
            }
            results.forRead.put(f, results.statResult(f, READ) + 1);
        }
    }

    @Override
    public void checkRead(String file, Object context) {
        super.checkRead(file, context);
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        super.checkWrite(fd);
    }

    @Override
    public void checkWrite(String file) {
        super.checkWrite(file);
        File f = new File(file);
        if (!canBeSkipped()) {
            if (monitor != null) {
                monitor.checkAll(f);
            }
            results.forWrite.put(f, results.statResult(f, WRITE) + 1);
        }

    }

    @Override
    public void checkDelete(String file) {
        super.checkDelete(file);
        File f = new File(file);
        if (!canBeSkipped()) {
            if (monitor != null) {
                monitor.checkAll(f);
            }
            results.forDelete.put(f, results.statResult(f, DELETE) + 1);
        }
    }

    private boolean canBeSkipped() {
        boolean result = false;
        Throwable th = new Throwable();
        StackTraceElement[] elems = th.getStackTrace();
        for (StackTraceElement stackTraceElement : elems) {
            if (stackTraceElement.getClassName().endsWith("ClassLoader") &&
                    stackTraceElement.getMethodName().endsWith("loadClass")) {
                result = true;
                break;
            }

        }
        return result;
    }

    static interface Monitor {

        void checkRead(File file);

        void checkAll(File file);
    }

    static class Results {

        private Map<File, Integer> forRead = new HashMap<File, Integer>();
        private Map<File, Integer> forWrite = new HashMap<File, Integer>();
        private Map<File, Integer> forDelete = new HashMap<File, Integer>();
        
        Results addResult(Results results) {
            if (results == this) {
                throw new IllegalArgumentException();
            }
            forRead.putAll(results.forRead);
            forWrite.putAll(results.forWrite);
            forDelete.putAll(results.forDelete);
            return this;
        }

        void assertResult(int cnt, int type) {
            int real = statResult(type);
            if (cnt != real) {
                Assert.fail("Expected " + cnt + " but was " + real + "\n  Read: " + forRead + "\n  Write: " + forWrite + "\n  Delete: " + forDelete);
            }
        }

        Set<File> getFiles() {
            Set<File> result = new HashSet<File>();
            result.addAll(forRead.keySet());
            result.addAll(forWrite.keySet());
            result.addAll(forDelete.keySet());
            return result;
        }

        int statResult(int type) {
            Set<File> files = getFiles();
            int result = 0;
            for (File file : files) {
                result += statResult(file, type);
            }
            return result;
        }

        int statResult(File file, int type) {
            switch (type) {
                case READ:
                    Integer read = forRead.get(file);
                    return (read != null) ? read : 0;
                case WRITE:
                    Integer write = forWrite.get(file);
                    return (write != null) ? write : 0;
                case DELETE:
                    Integer delete = forDelete.get(file);
                    return (delete != null) ? delete : 0;
                case ALL:
                    int all = statResult(file, READ);
                    all += statResult(file, WRITE);
                    all += statResult(file, DELETE);
                    return all;
            }
            return -1;
        }
    }
}
