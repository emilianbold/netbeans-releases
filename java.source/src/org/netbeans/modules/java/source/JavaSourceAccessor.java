/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source;

import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.IOException;
import java.util.Collection;
import javax.swing.text.JTextComponent;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.PositionConverter;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.builder.DefaultEnvironment;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public abstract class JavaSourceAccessor {


    static {
        try {
            Class.forName("org.netbeans.api.java.source.JavaSource", true, JavaSourceAccessor.class.getClassLoader());   //NOI18N
        } catch (ClassNotFoundException e) {
            ErrorManager.getDefault().notify (e);
        }
    }
    
    public static JavaSourceAccessor INSTANCE;
    
    
    public void runSpecialTask (final CancellableTask<CompilationInfo> task, final JavaSource.Priority priority) {
        INSTANCE.runSpecialTaskImpl (task, priority);
    }
    
    protected abstract void runSpecialTaskImpl (CancellableTask<CompilationInfo> task, JavaSource.Priority priority);
        
    public abstract JavacTaskImpl createJavacTask(ClasspathInfo cpInfo, DiagnosticListener<? super JavaFileObject> diagnosticListener, String sourceLevel);
    
    
    /**
     * Returns the JavacTaskImpl associated with given {@link CompilationInfo},
     * when it's not available it's created.
     * @param compilationInfo which {@link JavacTaskImpl} should be obtained.
     * @return {@link JavacTaskImpl} never returns null
     */
    public abstract JavacTaskImpl getJavacTask (CompilationInfo compilationInfo);
    
    /**
     * Returns {@link QueryEnvironment} associated with the given {@link WorkingCopy}.
     * 
     * @param copy {@link WorkingCopy} for which {@link QueryEnvironment} should be obtained
     * @return {@link CQueryEnvironment associated with the given working copy
     */
    public abstract DefaultEnvironment getCommandEnvironment(WorkingCopy copy);
    
    /**
     * Returns a cached compilation info when available or null
     * @param js {@link JavaSource} which {@CompilationInfo} should be returned
     * @param phase to which the compilation info should be moved
     * Can be called only from the dispatch thread!
     * @return {@link CompilationInfo} or null
     */
    public abstract CompilationInfo getCurrentCompilationInfo (JavaSource js, JavaSource.Phase phase) throws IOException;
    
    public abstract void revalidate(JavaSource js); 
    
    public abstract JavaSource create(final ClasspathInfo cpInfo, final PositionConverter binding, final Collection<? extends FileObject> files) throws IllegalArgumentException;
    
    public abstract PositionConverter create(final FileObject fo, int offset, int length, final JTextComponent component);
    
    /**
     * Returns true when the caller is a {@link JavaSource} worker thread
     * @return boolean
     */
    public abstract boolean isDispatchThread ();
    
    /**
     * Locks java compiler. Private API for indentation engine only!
     */
    public abstract void lockJavaCompiler ();
    
    /**
     * Unlocks java compiler. Private API for indentation engine only!
     */
    public abstract void unlockJavaCompiler ();
}
