/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.LifecycleManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Martin Entlicher
 */
class AvatarJSExecutor {
    
    private static final Logger LOG = Logger.getLogger(AvatarJSExecutor.class.getCanonicalName());
    
    private static final String AVATAR_SERVER = "com.oracle.avatar.js.Server";    // NOI18N
    private static final String AVATAR_JAR_PROP = "avatar-js.jar";              // NOI18N
    
    private final FileObject js;
    private final boolean debug;
    
    public AvatarJSExecutor(FileObject js, boolean debug) {
        this.js = js;
        this.debug = debug;
    }
    
    public Future<Integer> run(JavaPlatform javaPlatform) throws IOException, UnsupportedOperationException {
        LifecycleManager.getDefault().saveAll();
        Map<String, Object> properties = new HashMap<>();
        properties.put(JavaRunner.PROP_PLATFORM, javaPlatform);
        properties.put(JavaRunner.PROP_CLASSNAME, AVATAR_SERVER);
        String[] libs = { null };
        properties.put(JavaRunner.PROP_EXECUTE_CLASSPATH, getClassPath(js, libs));
        properties.put(JavaRunner.PROP_WORK_DIR, js.getParent());
        properties.put(JavaRunner.PROP_APPLICATION_ARGS, getApplicationArgs(js));
        if (libs[0] != null) {
            properties.put(JavaRunner.PROP_RUN_JVMARGS, Collections.singletonList("-Djava.library.path=" + libs[0]));
        }
        
        ExecutorTask task;
        if (debug) {
            task = JavaRunner.execute(JavaRunner.QUICK_DEBUG, properties);
        } else {
            task = JavaRunner.execute(JavaRunner.QUICK_RUN, properties);
        }
        return new ExecTaskFuture(task);
    }
    
    private static ClassPath getClassPath(FileObject js, String[] libs) {
        ClassPath cp = ClassPath.getClassPath(js, ClassPath.EXECUTE);
        if (cp == null) {
            cp = ClassPath.EMPTY;
        }
        String avatarJar = System.getProperty(AVATAR_JAR_PROP);
        if (avatarJar == null) {
            File found = InstalledFileLocator.getDefault().locate("avatar/" + AVATAR_JAR_PROP, null, false);
            if (found != null) {
                avatarJar = found.getPath();
            }
        }
        if (avatarJar != null) {
            try {
                ClassPath avatarJarCP = ClassPathSupport.createClassPath(avatarJar);
                if (cp == ClassPath.EMPTY) {
                    cp = avatarJarCP;
                } else {
                    cp = ClassPathSupport.createProxyClassPath(avatarJarCP, cp);
                }
            } catch (IllegalArgumentException iaex) {
                LOG.log(Level.WARNING, avatarJar, iaex);
            }

            if (libs != null) {
                int last = avatarJar.lastIndexOf(File.separatorChar);
                libs[0] = avatarJar.substring(0, last + 1);
            }
        }
        return cp;
    }
    
    private static List<String> getApplicationArgs(FileObject js) {
        String options = null;//Settings.getPreferences().get(Settings.PREF_NASHORN_OPTIONS, null);
        String arguments = null;//Settings.getPreferences().get(Settings.PREF_NASHORN_ARGUMENTS, null);
        if (options == null && arguments == null) {
            return Collections.singletonList(js.getNameExt());
        }
        List<String> args = new LinkedList<>();
        if (options != null && !(options = options.trim()).isEmpty()) {
            args.add(options);
        }
        args.add(js.getNameExt());
        if (arguments != null && !(arguments = arguments.trim()).isEmpty()) {
            args.add("--");     // NOI18N
            args.add(arguments);
        }
        return args;
    }
    
    private static class ExecTaskFuture implements Future<Integer> {
        
        private final ExecutorTask task;
        private volatile boolean canceled;
        
        ExecTaskFuture(ExecutorTask task) {
            this.task = task;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            task.stop();
            canceled = true;
            return task.isFinished();
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            return task.isFinished();
        }

        @Override
        public Integer get() throws InterruptedException, ExecutionException {
            return task.result();
        }

        @Override
        public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return task.result();
        }
    }
    
}
