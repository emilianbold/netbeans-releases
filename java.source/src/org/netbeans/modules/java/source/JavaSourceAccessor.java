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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.util.Collection;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.PositionConverter;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.parsing.api.GenericUserTask;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.TaskScheduler;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class JavaSourceAccessor {

        
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
        
    public void runSpecialTask (final GenericUserTask task, final JavaSource.Priority priority) {
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
        final int tp = tmp;
        final ParserResultTask wrapper = new ParserResultTask() {            
            @Override
            public void run(Result _null, Snapshot _null2) {
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
            public Class<? extends TaskScheduler> getSchedulerClass() {
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
    
    /**
     * Expert: Locks java compiler. Private API for indentation engine only!
     */
    public void lockJavaCompiler () {
        Utilities.acquireParserLock();
    }
    
    /**
     * Expert: Unlocks java compiler. Private API for indentation engine only!
     */
    public void unlockJavaCompiler () {
        Utilities.releaseParserLock();
    }
    
    public boolean isJavaCompilerLocked () {
        return Utilities.holdsParserLock();
    }
    
    public abstract Collection<Source> getSources(final JavaSource js);              
        
    /**
     * Returns the JavacTaskImpl associated with given {@link CompilationInfo},
     * when it's not available it's created.
     * Expert: May violate confinement
     * @param compilationInfo which {@link JavacTaskImpl} should be obtained.
     * @return {@link JavacTaskImpl} never returns null
     */
    public abstract JavacTaskImpl getJavacTask (CompilationInfo compilationInfo);
           
    public abstract JavaSource create(final ClasspathInfo cpInfo, final PositionConverter binding, final Collection<? extends FileObject> files) throws IllegalArgumentException;
    
    public abstract PositionConverter create(final FileObject fo, int offset, int length, final JTextComponent component);
    
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
}
