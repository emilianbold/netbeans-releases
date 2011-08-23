/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.java.source;

import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.IOException;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.ModificationResult.Difference.Kind;
import org.netbeans.api.java.source.PositionConverter;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public abstract class JavaSourceAccessor {

    private Map<CancellableTask<CompilationInfo>,ParserResultTask<?>>tasks = new IdentityHashMap<CancellableTask<CompilationInfo>,ParserResultTask<?>>();
        
    public static synchronized JavaSourceAccessor getINSTANCE () {
        if (INSTANCE == null) {
            try {
                Class.forName("org.netbeans.api.java.source.JavaSource", true, JavaSourceAccessor.class.getClassLoader());   //NOI18N            
                assert INSTANCE != null;
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return INSTANCE;
    }
    
    public static void setINSTANCE (JavaSourceAccessor instance) {
        assert instance != null;
        INSTANCE = instance;
    }
    
    private static volatile JavaSourceAccessor INSTANCE;
    
    private int translatePriority (final JavaSource.Priority priority) {
        assert priority != null;
        int tmp;
        if (priority == JavaSource.Priority.MAX) {
            tmp = 0;
        }
        else if (priority == JavaSource.Priority.MIN) {
            tmp = Integer.MAX_VALUE;
        }
        else {
            tmp = priority.ordinal() * 100;
        }
        return tmp;
    }
        
    public void runSpecialTask (final Mutex.ExceptionAction task, final JavaSource.Priority priority) {        
        final int tp = translatePriority(priority);
        final ParserResultTask wrapper = new ParserResultTask() {            
            @Override
            public void run(Result _null, SchedulerEvent event) {
                try {
                    task.run();
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }

            @Override
            public int getPriority() {
                return tp;
            }

            @Override
            public Class<? extends Scheduler> getSchedulerClass() {
                return null;
            }

            @Override
            public void cancel() {
            }
        };
        Utilities.scheduleSpecialTask(wrapper);
    }
    
    
    public void revalidate(final JavaSource js) {
        final Collection<Source> sources = getSources(js);
        assert sources != null;
        if (sources.size() == 1) {
            Utilities.revalidate(sources.iterator().next());
        }
    }
        
    public boolean isJavaCompilerLocked () {
        return Utilities.holdsParserLock();
    }

    public void lockJavaCompiler () {
        Utilities.acquireParserLock();
    }

    public void unlockJavaCompiler () {
        Utilities.releaseParserLock();
    }
    
    public void addPhaseCompletionTask (final JavaSource js,
            final CancellableTask<CompilationInfo> task,
            final JavaSource.Phase phase,
            final JavaSource.Priority priority) {
        
        final Collection<Source> sources = getSources(js);
        assert sources.size() == 1;
        final int pp = translatePriority(priority);
        if (tasks.keySet().contains(task)) {
            throw new IllegalArgumentException(String.format("Task: %s is already scheduled", task.toString()));   //NOI18N
        }
        final ParserResultTask<?> hanz = new CancelableTaskWrapper(task, pp, phase, js);
        tasks.put(task, hanz);
        Utilities.addParserResultTask(hanz, sources.iterator().next());
    }
    
    public void removePhaseCompletionTask (final JavaSource js,
            final CancellableTask<CompilationInfo> task) {        
        final Collection<Source> sources = getSources(js);
        assert sources.size() == 1;
        final ParserResultTask<?> hanz = tasks.remove(task);
        if (hanz == null) {
            throw new IllegalArgumentException(String.format("Task: %s is not scheduled", task.toString()));    //NOI18N
        }
        Utilities.removeParserResultTask(hanz, sources.iterator().next());
    }
    
    public void rescheduleTask (final JavaSource js,
            final CancellableTask<CompilationInfo> task) {
        final Collection<Source> sources = getSources(js);
        assert sources.size() == 1;
        final ParserResultTask<?> hanz = tasks.get(task);
        if (hanz != null)
            Utilities.rescheduleTask(hanz, sources.iterator().next());
    }
    
    public abstract Collection<Source> getSources(final JavaSource js);
    
    public abstract void setJavaSource (final CompilationInfo info, final JavaSource js);
        
    /**
     * Returns the JavacTaskImpl associated with given {@link CompilationInfo},
     * when it's not available it's created.
     * Expert: May violate confinement
     * @param compilationInfo which {@link JavacTaskImpl} should be obtained.
     * @return {@link JavacTaskImpl} never returns null
     */
    public abstract JavacTaskImpl getJavacTask (CompilationInfo compilationInfo);
       
    
    /**
     * Expert: Private API for indentation engine only!
     */
    public abstract CompilationController createCompilationController (Source s) throws IOException, ParseException;
    
    
    public abstract long createTaggedCompilationController (JavaSource js, long currentTag, Object[] out) throws IOException;
    
    public abstract JavaSource create(final ClasspathInfo cpInfo, final PositionConverter binding, final Collection<? extends FileObject> files) throws IllegalArgumentException;
    
    /**
     * Creates CompilationInfo for given CompilationInfoImpl
     * @param impl the spi
     * @return the api wrapper
     */
    public abstract CompilationInfo createCompilationInfo (CompilationInfoImpl impl);
    
    /**
     * Creates CompilationController for given CompilationInfoImpl
     * @param impl the spi
     * @return the api wrapper
     */
    public abstract CompilationController createCompilationController (CompilationInfoImpl impl);                    
    
    
    /**
     * Invalidates cached ClasspathInfo in the JavaSource for file
     * @param file for which ClasspathInfo should be invalidated.
     */
    public abstract void invalidateCachedClasspathInfo (final FileObject file);
    
    
    public abstract CompilationInfoImpl getCompilationInfoImpl (final CompilationInfo info);

    public abstract @NonNull String generateReadableParameterName (@NonNull String typeName, @NonNull Set<String> used);
    
    /**
     * Invalidates given {@link CompilationInfo}
     * @param info
     */
    public abstract void invalidate (CompilationInfo info);

    public static boolean holdsParserLock() {
	return Utilities.holdsParserLock();
    }

    public abstract Difference createDifference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText, String description);
    public abstract Difference createNewFileDifference(JavaFileObject fileObject, String text);
    public abstract ModificationResult createModificationResult(Map<FileObject, List<Difference>> diffs, Map<?, int[]> tag2Span);
    public abstract ElementUtilities createElementUtilities(@NonNull JavacTaskImpl jt);
    public abstract Map<FileObject, List<Difference>> getDiffsFromModificationResult(ModificationResult mr);
    public abstract Map<?, int[]> getTagsFromModificationResult(ModificationResult mr);
    public abstract ClassIndex createClassIndex (@NonNull ClassPath bootPath, @NonNull ClassPath classPath, @NonNull ClassPath sourcePath, boolean supportsChanges);

    private static class CancelableTaskWrapper extends JavaParserResultTask implements ClasspathInfoProvider {
        
        private final JavaSource javaSource;
        private final int priority;
        private final CancellableTask<CompilationInfo> task;
        
        public CancelableTaskWrapper (final CancellableTask<CompilationInfo> task,
                final int priority, final Phase phase,
                final JavaSource javaSource) {
            super (phase);
            assert phase != null;
            assert javaSource != null;
            this.task = task;
            this.priority = priority;
            this.javaSource = javaSource;
        }

        @Override
        public int getPriority() {
            return this.priority;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return null;
        }

        @Override
        public void cancel() {
            this.task.cancel();
        }

        @Override
        public void run(@NonNull Result result, SchedulerEvent event) {
            Parameters.notNull("result", result);   //NOI18N
            final CompilationInfo info = CompilationInfo.get(result);
            if (info == null) {
                throw new IllegalArgumentException(String.format("Result %s [%s] does not provide CompilationInfo",    //NOI18N
                        result.toString(),
                        result.getClass().getName()));
            }
            try {
                JavaSourceAccessor.getINSTANCE().setJavaSource(info, javaSource);
                this.task.run(info);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }        
        
        public ClasspathInfo getClasspathInfo () {
            return javaSource.getClasspathInfo();
        }
        
        @Override
        public String toString () {
            return this.getClass().getSimpleName()+"[task: "+ task +    //NOI18N
                    ", phase: "+getPhase()+", priority: "+priority+"]";      //NOI18N
        }
    }
}
