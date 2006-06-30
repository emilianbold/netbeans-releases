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

package org.netbeans.modules.j2ee.sun.share.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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
    private static final String PRIMARY = "primary"; //NOI18N
    private static final String SECONDARY = "secondary"; //NOI18N
//    private static final String SERVER = "server"; //NOI18N
    
    private static HashMap primaryByName;
    private static HashMap secondaryByName;
    
    /** Creates loader. */
    public ConfigDataLoader () {
        super("org.netbeans.modules.j2ee.sun.share.config.ConfigDataObject"); // NOI18N
        
        initMaps();
    }

    /** Initizalized loader, i.e. its extension list. Overrides superclass method. */
    protected void initialize () {
        super.initialize();
    }
    
    private void initMaps() {
        primaryByName = new HashMap();
        secondaryByName = new HashMap();
        
        // Sun Application Server specific files for 4.2.  !PW FIXME add appclient and connector in NB 5.0/Glassfish support
        primaryByName.put("sun-web.xml", "WEB-INF/sun-web.xml");
        primaryByName.put("sun-ejb-jar.xml", "META-INF/sun-ejb-jar.xml");
        secondaryByName.put("sun-cmp-mappings.xml", "META-INF/sun-cmp-mappings.xml");
        primaryByName.put("sun-application.xml", "META-INF/sun-application.xml");
    }
    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getMessage (ConfigDataLoader.class, "LBL_LoaderName");
    }

    /** Action available for sun specific deployment descriptor files.  See
     *  layer file.
     */
    protected String actionsContext() {
        return "Loaders/xml/sun-dd/Actions/"; // NOI18N
    }
    
    /** Creates multi data object for specified primary file.
     * Implements superclass abstract method. */
    protected MultiDataObject createMultiObject (FileObject fo)
    throws DataObjectExistsException, IOException {
        if (isPrimaryDescriptor(fo)) {
            return new ConfigDataObject(fo, this);
        } else {
            return new SecondaryConfigDataObject(fo, this);
        }
    }
    
    private boolean isPrimaryDescriptor(FileObject fo) {
        String filename = fo.getNameExt();
        return getPrimaryByName(filename) != null;
    }
    
    protected FileObject findPrimaryFile (FileObject fo) {
        // never recognize folders.
        if (fo.isFolder()) {
            return null;
        }
        
        String ext = fo.getExt();
        String filename = fo.getNameExt ();
        FileObject primaryFO = null;
        String secondaryName = null;
        if (getPrimaryByName(filename) != null || ext.equals(GENERIC_EXTENSION)) {
            primaryFO = fo;
        } else if (getPrimaryBySecondaryName (filename) != null) { // check for secondary file 
            secondaryName = filename;
        }
        
        if (primaryFO == null && secondaryName == null) {
            return null;
        }
        
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner != null) {
            Lookup l = owner.getLookup();
            J2eeModuleProvider projectModule = (J2eeModuleProvider) l.lookup(J2eeModuleProvider.class);
            if (projectModule != null) {
                if (primaryFO != null) {
                    primaryFO = projectModule.findDeploymentConfigurationFile(filename);
                    if (primaryFO != null) {
                        File primary = FileUtil.toFile(primaryFO);
                        if (primary != null && primary.equals(FileUtil.toFile(fo))) {
                            return fo;
                        }
                    }
                } else { // look for secondary FO 
                    FileObject secondaryFO = projectModule.findDeploymentConfigurationFile(secondaryName);
                    if(secondaryFO != null) {
                        File secondary = FileUtil.toFile(secondaryFO);
                        if (secondary != null && secondary.equals(FileUtil.toFile(fo))) {
                            return fo;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private String getPrimaryByName (String name) {
        return (String) primaryByName.get(name);
    }
    
    private String getPrimaryBySecondaryName (String name) {
        return (String) secondaryByName.get(name);
    }
}
