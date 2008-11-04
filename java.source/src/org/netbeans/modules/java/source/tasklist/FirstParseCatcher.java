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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.source.tasklist;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
public class FirstParseCatcher extends EditorAwareJavaSourceTaskFactory {
    
    /** Creates a new instance of FirstParseCatcher */
    public FirstParseCatcher() {
        super(Phase.ELEMENTS_RESOLVED, Priority.ABOVE_NORMAL);
    }
    
    //XXX: public for RepositoryUpdaterTest:
    public CancellableTask<CompilationInfo> createTask(FileObject file) {
        return new CancellableTask<CompilationInfo>() {
            public void cancel() {
                //XXX
            }
            public void run(final CompilationInfo info) throws Exception {
                Logger.getLogger(FirstParseCatcher.class.getName()).entering(FirstParseCatcher.class.getName(), "run", info.getFileObject());
                
                long startTime = System.currentTimeMillis();
                URL file = info.getFileObject().getURL();
                
                if (RebuildOraculum.isInitialized(file)) {
                    Logger.getLogger(FirstParseCatcher.class.getName()).finer("Oraculum is initialized.");
                    Logger.getLogger(FirstParseCatcher.class.getName()).exiting(FirstParseCatcher.class.getName(), "run");
                    return ;
                }
                
                final List<TypeElement> types = new ArrayList<TypeElement>();
                
                new TreePathScanner() {
                    @Override
                    public Object visitClass(ClassTree node, Object p) {
                        types.add((TypeElement) info.getTrees().getElement(getCurrentPath()));
                        return null;
                    }
                }.scan(info.getCompilationUnit(), null);
                
                Logger.getLogger(FirstParseCatcher.class.getName()).log(Level.FINER, "Found type={0}.", types);
                
                RebuildOraculum.putMembers(file, RebuildOraculum.sortOut(info.getElements(), types));
                
                if (info.getFileObject() != null) {
                    Logger.getLogger("TIMER").log(Level.FINE, "First Parse Catcher",
                            new Object[] {info.getFileObject(), System.currentTimeMillis() - startTime});
                }
                
                Logger.getLogger(FirstParseCatcher.class.getName()).exiting(FirstParseCatcher.class.getName(), "run");
            }
        };
    }

}
