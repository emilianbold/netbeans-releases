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

package org.netbeans.modules.j2ee.sun.share.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;


/** Loader for deployment plan files
 * Permits viewing/editing of them.
 * @author Pavel Buzek
 */
public class ConfigDataLoader extends UniFileLoader {
    
    /** Generated serial version UID. */
    //    private static final long serialVersionUID = ;
    
    private static final String GENERIC_EXTENSION = "dpf"; //NOI18N
    //    private static final String PRIMARY = "primary"; //NOI18N
    //    private static final String SECONDARY = "secondary"; //NOI18N
    //    private static final String SERVER = "server"; //NOI18N
    
    private static HashMap primaryByName;
    private static HashMap secondaryByName;
    
    static {
        primaryByName = new HashMap();
        secondaryByName = new HashMap();
        
        // Sun Application Server specific files
        primaryByName.put("sun-web.xml", "WEB-INF/sun-web.xml");
        primaryByName.put("sun-ejb-jar.xml", "META-INF/sun-ejb-jar.xml");
        secondaryByName.put("sun-cmp-mappings.xml", "META-INF/sun-cmp-mappings.xml");
        primaryByName.put("sun-application.xml", "META-INF/sun-application.xml");
        primaryByName.put("sun-application-client.xml", "META-INF/sun-application-client.xml");
    }
    
    /** Creates loader. */
    public ConfigDataLoader() {
        super("org.netbeans.modules.j2ee.sun.share.config.ConfigDataObject"); // NOI18N
    }
    
    /** Initizalized loader, i.e. its extension list. Overrides superclass method. */
    protected void initialize() {
        super.initialize();
    }
    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(ConfigDataLoader.class, "LBL_LoaderName");
    }
    
    /** Action available for sun specific deployment descriptor files.  See
     *  layer file.
     */
    protected String actionsContext() {
        return "Loaders/text/x-sun-dd/Actions/"; // NOI18N
    }
    
    /** Creates multi data object for specified primary file.
     * Implements superclass abstract method. */
    protected MultiDataObject createMultiObject(FileObject fo)
            throws DataObjectExistsException, IOException {
        MultiDataObject retVal;
        if (isPrimaryDescriptor(fo)) {
            retVal = new ConfigDataObject(fo, this);
        } else {
            retVal = new SecondaryConfigDataObject(fo, this);
        }
        return retVal;
    }
    
    private boolean isPrimaryDescriptor(FileObject fo) {
        String filename = fo.getNameExt();
        return getPrimaryByName(filename) != null;
    }
    
    protected FileObject findPrimaryFile(FileObject fo) {
        // never recognize folders.
        FileObject retVal = null;
        if (!fo.isFolder()) {
            
            String ext = fo.getExt();
            String filename = fo.getNameExt();
            FileObject primaryFO = null;
            String secondaryName = null;
            if (getPrimaryByName(filename) != null || ext.equals(GENERIC_EXTENSION)) {
                primaryFO = fo;
            } else if (getPrimaryBySecondaryName(filename) != null) { // check for secondary file
                secondaryName = filename;
            }
            
            if (primaryFO != null || secondaryName != null) {
                
                Project owner = FileOwnerQuery.getOwner(fo);
                if (owner != null) {
                    Lookup l = owner.getLookup();
                    J2eeModuleProvider projectModule = (J2eeModuleProvider) l.lookup(J2eeModuleProvider.class);
                    if (projectModule != null) {
                        J2eeModule mod = projectModule.getJ2eeModule();
                        if (primaryFO != null) {
                            //DDBean Removal
                            primaryFO = FileUtil.toFileObject(mod.getDeploymentConfigurationFile(filename));
                            if (primaryFO != null) {
                                File primary = FileUtil.toFile(primaryFO);
                                if (primary != null && primary.equals(FileUtil.toFile(fo))) {
                                    retVal = fo;
                                }
                            }
                        } else { // look for secondary FO
                            // DDBean Removal
                            FileObject secondaryFO = FileUtil.toFileObject(mod.getDeploymentConfigurationFile(secondaryName));
                            if(secondaryFO != null) {
                                File secondary = FileUtil.toFile(secondaryFO);
                                if (secondary != null && secondary.equals(FileUtil.toFile(fo))) {
                                    retVal = fo;
                                }
                            }
                        }
                    }
                }
            }
        }
        return retVal;
    }
    
    private String getPrimaryByName(String name) {
        return (String) primaryByName.get(name);
    }
    
    private String getPrimaryBySecondaryName(String name) {
        return (String) secondaryByName.get(name);
    }
}
