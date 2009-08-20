/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gizmo;

import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = SourceFileInfoProvider.class)
public final class CodeModelSourceFileInfoProvider implements SourceFileInfoProvider {

    public SourceFileInfo fileName(String functionName, int lineNumber, long offset, Map<String, String> serviceInfo) {
        try {
            //get project current name
            String projectFolderName = serviceInfo.get(GizmoServiceInfo.GIZMO_PROJECT_FOLDER);
            if (projectFolderName == null) {
                return null;
            }
            Project prj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(new File(projectFolderName)));
            if (prj.getLookup().lookup(NativeProject.class) == null) {
                return null;
            }
            CsmProject csmProject = CsmModelAccessor.getModel().getProject(prj);
            if (csmProject == null) {
                return null;
            }
            String name = functionName.indexOf("(") != -1 ? functionName.substring(0, functionName.indexOf("(")) : functionName; // NOI18N
            CsmFunction function = getFunction(csmProject, name);
            if (function == null){
                return null;
            }
            String sourceFile =    function.getContainingFile().getAbsolutePath().toString();
            int startOffset = function.getStartOffset();
            if (lineNumber > 0){
                return new SourceFileInfo(sourceFile,  lineNumber, 0);
            }
            return new SourceFileInfo(sourceFile, startOffset);//) + offset);
//            CsmDeclaration csmDeclaration = csmProject.findDeclaration(functionName);
//            if (csmDeclaration == null) {
//                Collection<CsmProject> libraries = csmProject.getLibraries();
//                for (CsmProject library : libraries) {
//                    csmDeclaration = library.findDeclaration(functionName);
//                    if (csmDeclaration != null) {
//                        break;
//                    }
//                }
//            }
//            if (csmDeclaration == null) {
//                throw new SourceFileInfoCannotBeProvided();
////            }
//            if (!CsmKindUtilities.isOffsetableDeclaration(csmDeclaration)) {
//                //do not know how to deal with this
//                throw new SourceFileInfoCannotBeProvided();
//            }
//            return new SourceFileInfoProvider.SourceFileInfo(((CsmOffsetable) csmDeclaration).getContainingFile().getAbsolutePath().toString(), ((CsmOffsetable) csmDeclaration).getStartOffset());
        } catch (IOException ex) {
            return null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }


    private static CsmFunction getFunction(CsmProject project, CharSequence qualifiedName) {
       Iterator<CsmFunction> iter = CsmSelect.getFunctions(project, qualifiedName);
       CsmFunction declaration = null;
       while (iter.hasNext()) {
           CsmFunction function = iter.next();
           if (CsmKindUtilities.isFunctionDefinition(function)) {
               return function;
           } else { // declaration
               CsmFunctionDefinition definition = function.getDefinition();
               if (definition != null) {
                   return definition;
               } else {
                   declaration = function;
               }
           }
       }
       return declaration;
   }

}
