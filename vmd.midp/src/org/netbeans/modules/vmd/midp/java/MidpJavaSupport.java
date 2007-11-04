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

package org.netbeans.modules.vmd.midp.java;

import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.filesystems.FileObject;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for checking whether given typeID is in classpath of current project
 *
 * @author Anton Chechel
 */
final class MidpJavaSupport {
    
    private MidpJavaSupport() {
    }
    
    /**
     * Checks whether given fqName is in classpath of current project
     * 
     * @param document container with given classpath context
     * @param fqName to be checked
     * @return isValid
     */
    static boolean checkValidity(DesignDocument document, String fqName) {
        DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
        if (context == null) { // document is loading
            return true;
        }
        
        List<SourceGroup> sg = ProjectUtils.getSourceGroups(context);
        boolean isValid = false;
        CheckingTask ct = new CheckingTask();
        Collection<FileObject> collection = Collections.emptySet();
        for (SourceGroup sourceGroup : sg) {
            ct.setFQName(fqName);
            
            ClasspathInfo cpi = ClasspathInfo.create(sourceGroup.getRootFolder());
            try {
                JavaSource.create(cpi, collection).runUserActionTask(ct, true);
            } catch (Exception ex) {
                Debug.warning("Can't create javasource for", fqName); // NOI18N
            }
            isValid = ct.getResult();
            if (!isValid) {
                break;
            }
        }
        
        return isValid;
    }

    private static final class CheckingTask implements Task<CompilationController> {
        private boolean result;
        private String fqName;
        
        public void run(CompilationController controller) throws Exception {
            Elements elements = controller.getElements();
            TypeElement te = elements.getTypeElement(fqName);
            result = (te != null);
        }
        
        public boolean getResult() {
            return result;
        }
        
        public void setFQName(String fqName) {
            this.fqName = fqName;
        }
    }
}
