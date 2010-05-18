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

package org.netbeans.modules.versioning;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class FSInterceptorTest extends NbTestCase {

    public FSInterceptorTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }   
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir() + "/userdir");
        FileObject fo = FileUtil.toFileObject(getWorkDir());
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    /**
     * ther whether all methods are overriden or not
     * @throws IOException
     */
    public void testTestInterceptorComplete() throws IOException {
        Set<String> testInterceptorMethods = new HashSet<String>();
        Method[]  methods = TestInterceptor.class.getDeclaredMethods();
        for (Method method : methods) {
            if((method.getModifiers() & Modifier.PUBLIC) != 0) {
                System.out.println(" test interceptor method: " + method.getName());
                testInterceptorMethods.add(method.getName());
            }
        }

        methods = VCSInterceptor.class.getDeclaredMethods();
        for (Method method : methods) {
            if((method.getModifiers() & Modifier.PUBLIC) != 0) {
                System.out.println(" vcsinterceptor method: " + method.getName());
                if(!testInterceptorMethods.contains(method.getName())) {
                    fail("" + TestInterceptor.class.getName() + " should override method " + method.getName());
                }
            }
        }
    }  

    public void testCorrectNeedsLHMethodName() throws IOException {
        Set<String> calledMethods = new HashSet<String>();

        final File roFile = new File(getWorkDir(), "testFile") {
            @Override
            public boolean canWrite() {
                return false;
            }
        };
        roFile.createNewFile();

        final File wFile = new File(getWorkDir(), "testFile");
        wFile.createNewFile();

//        LogHandler lh = new LogHandler();
//        VersioningManager.LOG.addHandler(lh);

        // canWrite
        W w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.canWrite(roFile);
            }
        };
        TestVCS.instance.file = roFile;
        w.test();
        
        TestVCS.instance.file = wFile;

        // beforeCreate
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.beforeCreate(FileUtil.toFileObject(wFile.getParentFile()), wFile.getName(), false);
            }
        };
        w.test();

        // doCreate
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.fileDataCreated(new FileEvent(FileUtil.toFileObject(wFile)));
            }
        };
        w.test(new String[] {"afterCreate", "doCreate"}); // ignore both

        // afterCreate
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.fileDataCreated(new FileEvent(FileUtil.toFileObject(wFile)));
            }
        };
        w.test();

        // beforeDelete
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.getDeleteHandler(wFile);
            }
        };
        w.test();

        // doDelete
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.getDeleteHandler(wFile).delete(wFile);
            }
        };
        w.test(new String[] {"doDelete"});

        // afterDelete
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.fileDeleted(new FileEvent(FileUtil.toFileObject(wFile)));
            }
        };
        w.test();

        wFile.createNewFile();

        // beforeChange
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.beforeChange(FileUtil.toFileObject(wFile));
            }
        };
        w.test();

        // afterChange
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.fileChanged(new FileEvent(FileUtil.toFileObject(wFile)));
            }
        };
        w.test();

        // beforeMove
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.getMoveHandler(wFile, wFile);
            }
        };
        w.test();

        // doMove
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                try {
                    fi.getMoveHandler(wFile, wFile).handle();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        };
        w.test(new String[] {"doMove", "afterMove"});

        // afterMove
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.fileRenamed(new FileRenameEvent(FileUtil.toFileObject(wFile), "wFile", null));
            }
        };
        w.test();

        // fileLocked
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.fileLocked(FileUtil.toFileObject(wFile));
            }
        };
        w.test();

        // getAttribute
        w = new W() {
            void callInterceptorMethod(FilesystemInterceptor fi) {
                fi.getAttribute(wFile, "ProvidedExtensions.RemoteLocation");
            }
        };
        w.test();


        Method[] methods = VCSInterceptor.class.getDeclaredMethods();
        for (Method method : methods) {
            if((method.getModifiers() & Modifier.PUBLIC) != 0) {
                System.out.println(" vcsinterceptor method: " + method.getName());
                if(!W.calledMethods.contains(method.getName())) {
                    fail(" missing test for interceptor method: " + method.getName());
                }
            }
        }

    }

    private static abstract class W {
        private static LogHandler lh;
        private static Set<String> calledMethods = new HashSet<String>();
        private static FilesystemInterceptor fi;

        W() {
            if(lh == null) {
                lh = new LogHandler();
                VersioningManager.LOG.addHandler(lh);
                fi = new FilesystemInterceptor();
                fi.init(VersioningManager.getInstance());
            }
        }

        abstract void callInterceptorMethod(FilesystemInterceptor fi);

        void test() {
            test(null);
        }
        void test(String[] mustHaveIntercepted) {
            lh.reset();
            TestInterceptor.instance.methodNames.clear();

            callInterceptorMethod(fi);

            assertNotNull(lh.methodNames);
            for (String m : lh.methodNames) {
                if(!TestInterceptor.instance.methodNames.contains(m)) {
                   fail(" missing logged method name " + m + " between intercepted");
                }
            }
            for (String m : TestInterceptor.instance.methodNames) {
                if(!lh.methodNames.contains(m) && !ignore(m, mustHaveIntercepted)) {
                    fail(" missing intercepted method name " + m + " between logged");
                }
            }
            if(mustHaveIntercepted != null) {
                for (String m : mustHaveIntercepted) {
                    if(!TestInterceptor.instance.methodNames.contains(m)) {
                       fail(" missing must have intercepted method name " + m);
                    }
                }
            }
            calledMethods.addAll(TestInterceptor.instance.methodNames);
        }

        private boolean ignore(String name, String[] toBeIgnored) {
            for (String i : toBeIgnored) {
                if(i.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }


    public static class TestInterceptor extends VCSInterceptor {
        Set<String> methodNames = new HashSet<String>();
        static TestInterceptor instance;
        public TestInterceptor() {
            instance = this;
        }

        @Override
        public void afterChange(File file) {
            storeMethodName();
            super.afterChange(file);
        }

        @Override
        public void afterCreate(File file) {
            storeMethodName();
            super.afterCreate(file);
        }

        @Override
        public void afterDelete(File file) {
            storeMethodName();
            super.afterDelete(file);
        }

        @Override
        public void afterMove(File from, File to) {
            storeMethodName();
            super.afterMove(from, to);
        }

        @Override
        public void beforeChange(File file) {
            storeMethodName();
            super.beforeChange(file);
        }

        @Override
        public boolean beforeCreate(File file, boolean isDirectory) {
            storeMethodName();
            return true;
        }

        @Override
        public boolean beforeDelete(File file) {
            storeMethodName();
            return true;
        }

        @Override
        public void beforeEdit(File file) {
            storeMethodName();
            super.beforeEdit(file);
        }

        @Override
        public boolean beforeMove(File from, File to) {
            storeMethodName();
            return true;
        }

        @Override
        public void doCreate(File file, boolean isDirectory) throws IOException {
            storeMethodName();
            super.doCreate(file, isDirectory);
        }

        @Override
        public void doDelete(File file) throws IOException {
            storeMethodName();
            super.doDelete(file);
        }

        @Override
        public void doMove(File from, File to) throws IOException {
            storeMethodName();
            super.doMove(from, to);
        }

        @Override
        public Object getAttribute(File file, String attrName) {
            storeMethodName();
            return super.getAttribute(file, attrName);
        }

        @Override
        public boolean isMutable(File file) {
            storeMethodName();
            return super.isMutable(file);
        }

        private void storeMethodName() {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            for (int i = 0; i < st.length; i++) {
                StackTraceElement e = st[i];
                if(e.getClassName().equals(this.getClass().getName())) {
                    methodNames.add(st[i+1].getMethodName());
                    return;
                }

            }
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.spi.VersioningSystem.class)
    public static class TestVCS extends VersioningSystem {

        private VCSInterceptor interceptor;
        private static TestVCS instance;
        File file;

        public TestVCS() {
            instance = this;
            interceptor = new TestInterceptor();
        }

        public static TestVCS getInstance() {
            return instance;
        }

        @Override
        public File getTopmostManagedAncestor(File file) {
            if(this.file == null) return null;
            if(file.equals(this.file.getParentFile())) {
                return file;
            }
            if(file.equals(this.file)) {
                return file.getParentFile();
            }
            return null;
        }

        @Override
        public VCSInterceptor getVCSInterceptor() {
            return interceptor;
        }

    }

    private static class LogHandler extends Handler {
        Set<String> methodNames = new HashSet<String>();
        @Override
        public void publish(LogRecord record) {            
            String msg = record.getMessage();
            if(msg == null || msg.trim().equals("") || !msg.startsWith("needsLocalHistory")) return;
            methodNames.add((String) record.getParameters()[0]);
        }

        void reset () {
            methodNames.clear();
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }


    }

}

