/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.jbuilder.parsing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;
import org.netbeans.modules.java.j2seplatform.wizard.NewJ2SEPlatform;
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
final class JdkSupport {
    static final String INSTALLER_REGISTRY_FOLDER = "org-netbeans-api-java/platform/installers"; // NOI18N
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(JdkSupport.class);
    
    
    /** Creates a new instance of JDKSupport */
    public static File getJKDDirectory(final String jdkLibraryName)  {
        AbstractProject.UserLibrary aulb = null;
        try {
            File homeFile = UserLibrarySupport.getUserHomeLib();
            Collection installers = getPlatformInstallers();
            if (installers.size() == 0) {
                logger.finest("No registered PlatformInstall");//NOI18N
            }

            if (installers.size() > 0 && homeFile != null) {
                aulb = UserLibrarySupport.getInstance(jdkLibraryName, homeFile);
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
                                NewJ2SEPlatform platform = NewJ2SEPlatform.create(foToTest);
                                platform.run();
                                if (platform.isValid()) {
                                    return toTest;
                                }
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
