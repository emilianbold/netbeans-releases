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

import com.sun.tools.javac.api.ClassNamesForFileOraculum;
import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.IOException;
import java.util.Collection;
import javax.swing.text.JTextComponent;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.PositionConverter;
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
        
    public void runSpecialTask (final CancellableTask<CompilationInfo> task, final JavaSource.Priority priority) {
        INSTANCE.runSpecialTaskImpl (task, priority);
    }
    
    protected abstract void runSpecialTaskImpl (CancellableTask<CompilationInfo> task, JavaSource.Priority priority);
        
    public final JavacTaskImpl createJavacTask(ClasspathInfo cpInfo, DiagnosticListener<? super JavaFileObject> diagnosticListener, String sourceLevel) {
        return createJavacTask(cpInfo, diagnosticListener, sourceLevel, null);
    }
    
    public abstract JavacTaskImpl createJavacTask(ClasspathInfo cpInfo, DiagnosticListener<? super JavaFileObject> diagnosticListener, String sourceLevel, ClassNamesForFileOraculum cnih);
    
    
    /**
     * Returns the JavacTaskImpl associated with given {@link CompilationInfo},
     * when it's not available it's created.
     * Expert: May violate confinement
     * @param compilationInfo which {@link JavacTaskImpl} should be obtained.
     * @return {@link JavacTaskImpl} never returns null
     */
    public abstract JavacTaskImpl getJavacTask (CompilationInfo compilationInfo);
    
    /**
     * Returns a cached compilation info when available or null
     * Expert: Violates confinement
     * @param js {@link JavaSource} which {@CompilationInfo} should be returned
     * @param phase to which the compilation info should be moved
     * Can be called only from the dispatch thread!
     * @return {@link CompilationInfo} or null
     */
    public abstract CompilationInfo getCurrentCompilationInfo (JavaSource js, JavaSource.Phase phase) throws IOException;

    /**
     * Expert: Private API for indentation engine only!
     */
    public abstract CompilationController createCompilationController (JavaSource js) throws IOException;
    
    
    public abstract long createTaggedCompilationController (JavaSource js, long currentTag, Object[] out) throws IOException;

    public abstract void revalidate(JavaSource js); 
    
    public abstract JavaSource create(final ClasspathInfo cpInfo, final PositionConverter binding, final Collection<? extends FileObject> files) throws IllegalArgumentException;
    
    public abstract PositionConverter create(final FileObject fo, int offset, int length, final JTextComponent component);
    
    /**
     * Returns true when the caller is a {@link JavaSource} worker thread
     * @return boolean
     */
    public abstract boolean isDispatchThread ();
    
    /**
     * Expert: Locks java compiler. Private API for indentation engine only!
     */
    public abstract void lockJavaCompiler ();
    
    /**
     * Expert: Unlocks java compiler. Private API for indentation engine only!
     */
    public abstract void unlockJavaCompiler ();
    
    /**
     * For check confinement.
     * @return
     */
    public abstract boolean isJavaCompilerLocked();
}
