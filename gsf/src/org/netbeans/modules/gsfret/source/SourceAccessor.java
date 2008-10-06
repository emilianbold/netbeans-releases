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

package org.netbeans.modules.gsfret.source;

import java.io.IOException;
import org.netbeans.napi.gsfret.source.ParserTaskImpl;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.modules.gsf.Language;
import org.openide.util.Exceptions;

/**
 * This class is based on JavaSourceAccessor in Retouche
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author tom
 */
public abstract class SourceAccessor {

    
    public static synchronized SourceAccessor getINSTANCE () {
        if (INSTANCE == null) {
            try {
                Class.forName("org.netbeans.napi.gsfret.source.Source", true, SourceAccessor.class.getClassLoader());   //NOI18N
                assert INSTANCE != null;
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return INSTANCE;
    }
    
    public static void setINSTANCE (SourceAccessor instance) {
        assert instance != null;
        INSTANCE = instance;
    }

    // This was a quickfix for a similar bug to 126558; see
    //  http://hg.netbeans.org/main/rev/63c10f6d307b
    // for a better way to fix it
    public static int dummy;
    
    private static volatile SourceAccessor INSTANCE;
    
    
    public void runSpecialTask (final CancellableTask<CompilationInfo> task, final Source.Priority priority) {
        INSTANCE.runSpecialTaskImpl (task, priority);
    }
    
    protected abstract void runSpecialTaskImpl (CancellableTask<CompilationInfo> task, Source.Priority priority);
        
    public abstract ParserTaskImpl createParserTask(Language language, ClasspathInfo cpInfo);
    
    
//    /**
//     * Returns the JavacTaskImpl associated with given {@link CompilationInfo},
//     * when it's not available it's created.
//     * @param compilationInfo which {@link JavacTaskImpl} should be obtained.
//     * @return {@link JavacTaskImpl} never returns null
//     */
    public abstract ParserTaskImpl getParserTask (CompilationInfo compilationInfo);
    
    /**
     * Returns a cached compilation info when available or null
     * @param js {@link JavaSource} which {@CompilationInfo} should be returned
     * @param phase to which the compilation info should be moved
     * Can be called only from the dispatch thread!     
     * @return {@link CompilationInfo} or null
     */
    public abstract CompilationInfo getCurrentCompilationInfo (Source js, Phase phase) throws IOException;
    
    public abstract void revalidate(Source js);    
    
    
    /**
     * Returns true when the caller is a {@link JavaSource} worker thread
     * @return boolean
     */
    public abstract boolean isDispatchThread ();
    
    /**
     * Expert: Locks java compiler. Private API for indentation engine only!
     */
    public abstract void lockParser();
    
    /**
     * Expert: Unlocks java compiler. Private API for indentation engine only!
     */
    public abstract void unlockParser();
    
    /**
     * For check confinement.
     * @return
     */
    public abstract boolean isParserLocked();
}
