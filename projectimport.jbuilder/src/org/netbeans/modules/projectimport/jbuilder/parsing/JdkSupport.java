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

package org.netbeans.modules.projectimport.jbuilder.parsing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;
import org.netbeans.modules.java.j2seplatform.api.J2SEPlatformCreator;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;
import org.netbeans.modules.projectimport.j2seimport.LoggerFactory;
import org.netbeans.spi.java.platform.PlatformInstall;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 *
 * @author Radek Matous
 */
public final class JdkSupport {
    static final String INSTALLER_REGISTRY_FOLDER = "org-netbeans-api-java/platform/installers"; // NOI18N
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(JdkSupport.class);
    
    
    /** Creates a new instance of JDKSupport */
    public static File getJKDDirectory(final String jdkLibraryName, File projectDir)  {
        AbstractProject.UserLibrary aulb = null;
        try {
            projectDir = (projectDir == null) ? UserLibrarySupport.getUserHomeLib() : projectDir;
            Collection installers = getPlatformInstallers();
            if (installers.size() == 0) {
                logger.finest("No registered PlatformInstall");//NOI18N
            }

            if (installers.size() > 0 && projectDir != null) {
                aulb = UserLibrarySupport.getInstance(jdkLibraryName, projectDir);
            }

            if (aulb != null) {
                Iterator it = aulb.getLibraries().iterator();
                if (it.hasNext()) {
                    AbstractProject.Library al = (AbstractProject.Library)it.next();
                    File arc = al.getArchiv();
                    if (arc == null) {
                        logger.finest("user library: "+aulb.getName()+ " contains no archiv (reference is null) ");//NOI18N
                    }
                    
                    for (Iterator instances =  installers.iterator(); instances.hasNext();) {
                        PlatformInstall pi = (PlatformInstall)instances.next();
                        for (File toTest = arc; toTest != null; toTest = toTest.getParentFile()) {
                            FileObject foToTest = FileUtil.toFileObject(toTest);
                            if (foToTest != null && pi.accept(foToTest)) {
                                J2SEPlatformCreator.createJ2SEPlatform(foToTest);
                                return toTest;
                            } else {
                                if (foToTest == null) {
                                    logger.finest("for archiv: " + arc + " toTest: " + toTest);//NOI18
                                } else if (!pi.accept(foToTest)) {
                                    logger.finest("foToTest: " + foToTest + " not accepted by  " + pi.getClass());//NOI18
                                }
                            }
                        }

                    }
                }

            }
        } catch (IOException iex) {
                org.openide.ErrorManager.getDefault().notify(iex);            
        } /*catch (SAXException sax) {
                org.openide.ErrorManager.getDefault().notify(sax);            
        }*/
        

        return null;
    }
            
    private static Collection getPlatformInstallers() {
        Collection result =  new HashSet();
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(INSTALLER_REGISTRY_FOLDER);
        assert fo != null;
        assert fo.isFolder();
        FileObject[] children = fo.getChildren();
        for (int i = 0; i < children.length; i++) {
            try {
                DataObject doi = DataObject.find(children[i]);
                InstanceCookie ic = (InstanceCookie)doi.getCookie(InstanceCookie.class);
                if (ic != null) {
                    if (PlatformInstall.class.isAssignableFrom(ic.instanceClass())) {
                        result.add(ic.instanceCreate());
                    }
                }
            } catch(DataObjectNotFoundException dex) {
                org.openide.ErrorManager.getDefault().notify(dex);
                continue;
            } catch (IOException iex) {
                org.openide.ErrorManager.getDefault().notify(iex);
                continue;
            } catch (ClassNotFoundException cex) {
                org.openide.ErrorManager.getDefault().notify(cex);                
                continue;
            }
        }
        
        return result;
    }
}
