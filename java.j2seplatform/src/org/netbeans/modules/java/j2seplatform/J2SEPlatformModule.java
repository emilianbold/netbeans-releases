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
package org.netbeans.modules.java.j2seplatform;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.Exceptions;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;


public class J2SEPlatformModule extends ModuleInstall {
    
    private static final String DEFAULT_PLATFORM = "Services/Platforms/org-netbeans-api-java-Platform/default_platform.xml";    //NOI18N

    public void restored() {
        super.restored();
        // update source level and J2SE platforms in build.properties file
        updateBuildProperties();
    }

    // implemented as separate method for simpler unit testing
    public static void updateBuildProperties() {
        ProjectManager.mutex().postWriteRequest(
            new Runnable () {
                public void run () {
                    try {
                        recoverDefaultPlatform ();
                        EditableProperties ep = PropertyUtils.getGlobalProperties();
                        boolean save = updateSourceLevel(ep);
                        save |= updateBuildProperties (ep);
                        if (save) {
                            PropertyUtils.putGlobalProperties (ep);
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            });
    }
    
    private static boolean updateSourceLevel(EditableProperties ep) {
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        String ver = platform.getSpecification().getVersion().toString();
        if (!ver.equals(ep.getProperty("default.javac.source"))) { //NOI18N
            ep.setProperty("default.javac.source", ver); //NOI18N
            ep.setProperty("default.javac.target", ver); //NOI18N
            return true;
        } else {
            return false;
        }
    }


    private static boolean updateBuildProperties (EditableProperties ep) {
        boolean changed = false;
        JavaPlatform[] installedPlatforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification ("j2se",null));   //NOI18N
        for (int i=0; i<installedPlatforms.length; i++) {
            //Handle only platforms created by this module
            if (!installedPlatforms[i].equals (JavaPlatformManager.getDefault().getDefaultPlatform()) && installedPlatforms[i] instanceof J2SEPlatformImpl) {
                String systemName = ((J2SEPlatformImpl)installedPlatforms[i]).getAntName();
                String key = PlatformConvertor.createName(systemName,"home");   //NOI18N
                if (!ep.containsKey (key)) {
                    try {
                        PlatformConvertor.generatePlatformProperties(installedPlatforms[i], systemName, ep);
                        changed = true;
                    } catch (PlatformConvertor.BrokenPlatformException b) {
                        Logger.getLogger(J2SEPlatformModule.class.getName()).info("Platform: " + installedPlatforms[i].getDisplayName() +" is missing: " + b.getMissingTool());
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                  }
                }
            }
        }
        return changed;
    }

    private static void recoverDefaultPlatform () {
        final FileObject defaultPlatform = Repository.getDefault().getDefaultFileSystem().findResource(DEFAULT_PLATFORM);
        if (defaultPlatform != null) {
            try {
                DataObject dobj = DataObject.find(defaultPlatform);
                boolean valid = false;
                InstanceCookie ic = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
                if (ic != null) {
                    try {
                        ic.instanceCreate();
                        valid = true;
                    } catch (Exception e) {
                        //Ignore it, logged bellow
                    }
                }
                if (!valid) {
                    Logger.getLogger("global").log(Level.WARNING,"default_platform.xml is broken, regenerating.");
                    Object attr = defaultPlatform.getAttribute("removeWritables");      //NOI18N
                    if (attr instanceof Callable) {
                        ((Callable)attr).call ();
                    }
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        else {
            Logger.getLogger("global").log(Level.WARNING,"The default platform is hidden.");  //NOI18N
        }
    }
}
