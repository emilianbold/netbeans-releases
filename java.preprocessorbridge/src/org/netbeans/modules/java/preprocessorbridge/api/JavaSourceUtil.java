/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.preprocessorbridge.api;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.java.preprocessorbridge.JavaSourceUtilImplAccessor;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * @author Tomas Zezula
 * Contains utility method to create private copy of javac, used by debugger.jpda
 * @since 1.5
 */
public class JavaSourceUtil {
   
    /**
     * Helper method used by debugger.jpda to create its own private javac compiler.
     * The caller is responsible for freeing handles holding the compiler.
     * As the caller operates on the private copy of compiler the calls to
     * the JavaSource.runUserActionTask, JavaSource.runModificationTask may no work
     * correctly and shouldn't be used in combination with this method.
     * @since 1.5
     */
    private static final Lookup.Result<JavaSourceUtilImpl> result = Lookup.getDefault().lookupResult(JavaSourceUtilImpl.class);
    
    private JavaSourceUtil () {}
    
    
    public static class Handle {

        private final long id;
        private final Object compilationController;

        private Handle (Object compilaionController, long id) {
            this.compilationController = compilaionController;
            this.id = id;
        }

        public Object getCompilationController () {
            return compilationController;
        }
    }


    public Handle createControllerHandle (final FileObject file, final Handle handle) throws IOException {
        assert file != null;
        final JavaSourceUtilImpl impl = getSPI();
        assert impl != null;
        final long id = handle == null ? -1 : handle.id;
        final Object[] param = new Object[] {
          handle == null ? null : handle.compilationController
        };
        final long newId = JavaSourceUtilImplAccessor.getInstance().createTaggedCompilationController(impl, file, id, param);
        if (newId == id) {
            return handle;
        }
        else {
            return new Handle(param[0], newId);
        }
    }
    
    private static JavaSourceUtilImpl getSPI () {
        Collection<? extends JavaSourceUtilImpl> instances = result.allInstances();
        int size = instances.size();
        assert  size < 2;
        return size == 0 ? null : instances.iterator().next();
    }
    
}
