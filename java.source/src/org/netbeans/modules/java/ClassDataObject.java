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

package org.netbeans.modules.java;

import java.net.URL;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

    
public final class ClassDataObject extends MultiDataObject {
    
    public ClassDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().add(new OpenSourceCookie());
    }

    public @Override Node createNodeDelegate() {
        return new JavaNode (this, false);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    private final class OpenSourceCookie implements OpenCookie {
        
        public void open() {
            try {
                FileObject fo = getPrimaryFile();
                FileObject binaryRoot = null;
                String resourceName = null;
                ClassPath cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
                if (cp == null || (binaryRoot = cp.findOwnerRoot(fo))==null) {
                    cp = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
                    if (cp != null) {
                        binaryRoot = cp.findOwnerRoot(fo);
                        resourceName = cp.getResourceName(fo,'.',false);  //NOI18N
                    }
                } else if (binaryRoot != null) {
                    resourceName = cp.getResourceName(fo,'.',false);  //NOI18N
                }
                ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
                if (bootPath == null) {
                    //No boot cp, try the default platform boot cp
                    bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
                }
                FileObject resource = null;
                final ElementHandle<TypeElement> handle = resourceName != null ? ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, resourceName.replace('/', '.')) : null;
                final ClasspathInfo cpInfo = cp != null && bootPath != null ? ClasspathInfo.create(bootPath, cp, ClassPathSupport.createClassPath(new URL[0])) : null;
                if (binaryRoot != null) {
                    //Todo: Ideally it should do the same as ElementOpen.open () but it will require a copy of it because of the reverese module dep.
                    resource = SourceUtils.getFile(handle, cpInfo);
                }
                if (resource !=null ) {
                    DataObject sourceFile = DataObject.find(resource);
                    OpenCookie oc = sourceFile.getCookie(OpenCookie.class);
                    if (oc != null) {
                        oc.open();
                    } else {
                        Logger.getLogger(ClassDataObject.class.getName()).warning("SourceFile: "+FileUtil.getFileDisplayName (resource) +" has no OpenCookie"); //NOI18N
                    }
                } else {
                    BinaryElementOpen beo = Lookup.getDefault().lookup(BinaryElementOpen.class);
                    
                    if (beo == null || handle == null || cpInfo == null || !beo.open(cpInfo, handle)) {
                        if (resourceName == null) {
                            resourceName = fo.getName();
                        }
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ClassDataObject.class, "TXT_NoSources",
                                resourceName.replace('/', '.'))); //NOI18N
                    }
                }
            } catch (DataObjectNotFoundException nf) {
                Exceptions.printStackTrace(nf);
            }
        }
    }
}
