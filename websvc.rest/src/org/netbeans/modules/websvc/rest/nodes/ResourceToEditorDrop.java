/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.websvc.rest.nodes;

import com.sun.source.tree.ClassTree;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;

/** Implementation of ActiveEditorDrop
 *
 * @author mkuchtiak
 */
public class ResourceToEditorDrop implements ActiveEditorDrop {
    
    RestServiceNode resourceNode;
    
    public ResourceToEditorDrop(RestServiceNode resourceNode) {
        this.resourceNode=resourceNode;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        Object mimeType = targetComponent.getDocument().getProperty("mimeType"); //NOI18N
        ResourceUriProvider resourceUriProvider = resourceNode.getLookup().lookup(ResourceUriProvider.class);
        if (resourceUriProvider != null &&
            resourceUriProvider.getResourceUri().length() > 0 &&
            mimeType!=null &&
            "text/x-java".equals(mimeType)) { //NOI18N
            
            try {
                FileObject targetFo = NbEditorUtilities.getFileObject(targetComponent.getDocument());
                if (targetFo != null) {

                    // add REST and Jersey dependencies
                    ClassPath cp = ClassPath.getClassPath(targetFo, ClassPath.COMPILE);
                    List<Library> restLibs = new ArrayList<Library>();
                    if (cp.findResource("javax/ws/rs/WebApplicationException.class") == null) {
                        Library lib = LibraryManager.getDefault().getLibrary("restapi"); //NOI18N
                        if (lib != null) {
                            restLibs.add(lib);
                        }
                    }
                    if (cp.findResource("com/sun/jersey/api/clientWebResource.class") == null) {
                        Library lib = LibraryManager.getDefault().getLibrary("restlib"); //NOI18N
                        if (lib != null) {
                            restLibs.add(lib);
                        }
                    }
                    if (restLibs.size() > 0) {
                        try {
                            ProjectClassPathModifier.addLibraries(
                                    restLibs.toArray(new Library[restLibs.size()]),
                                    targetFo,
                                    ClassPath.COMPILE);
                        } catch (java.io.IOException ex) {
                            Logger.getLogger(ResourceToEditorDrop.class.getName()).log(Level.INFO, "Cannot add Jersey libraries" , ex);
                        }
                    }

                    RestServiceDescription desc = resourceNode.getLookup().lookup(RestServiceDescription.class);
                    List<RestMethodDescription> methods =  desc.getMethods();
                    String uriTemplate = desc.getUriTemplate();
                    if (!uriTemplate.startsWith("/")) {
                        uriTemplate = "/"+uriTemplate;
                    }
                    Project prj = resourceNode.getLookup().lookup(Project.class);
                    String resourceUri =
                            (prj == null ? uriTemplate : ClientJavaSourceHelper.getResourceURL(prj, uriTemplate));

                    addJerseyClientClass(
                            JavaSource.forFileObject(targetFo),
                            desc.getName(),
                            resourceUri,
                            methods);

                    // logging usage of action
                    Object[] params = new Object[2];
                    params[0] = LogUtils.WS_STACK_JAXRS;
                    params[1] = "DRAG & DROP REST RESOURCE"; // NOI18N
                    LogUtils.logWsAction(params);
                    
                    return true;
                }
            } catch (Exception ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
        }
        return false;
    }

    private void addJerseyClientClass(
            JavaSource source,
            final String resourceName,
            final String resourceUri,
            final List<RestMethodDescription> restMethodDesc) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws java.io.IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    String className = resourceName+"_JerseyClient"; //NOI18N
                    ClassTree modifiedTree = ClientJavaSourceHelper.addJerseyClientClass(copy, tree, className, resourceUri, restMethodDesc);

                    copy.rewrite(tree, modifiedTree);
                }
            });

            result.commit();
        } catch (java.io.IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}
