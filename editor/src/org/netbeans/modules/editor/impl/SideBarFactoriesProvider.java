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


package org.netbeans.modules.editor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.modules.editor.impl.CustomizableSideBar.SideBarPosition;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Martin Roskanin
 */
public final class SideBarFactoriesProvider implements Class2LayerFolder<SideBarFactoriesProvider>, InstanceProvider<SideBarFactoriesProvider> {

    private static final Logger LOG = Logger.getLogger(SideBarFactoriesProvider.class.getName());
    
    public static final String SIDEBAR_COMPONENTS_FOLDER_NAME = "SideBar"; //NOI18N
    
    private final List<FileObject> instanceFiles;
    private Map<CustomizableSideBar.SideBarPosition, List<SideBarFactory>> factories;

    /** Creates a new instance of TestClass2LayerFolderInitializer */
    public SideBarFactoriesProvider() {
        this(Collections.<FileObject>emptyList());
    }

    private SideBarFactoriesProvider(List<FileObject> instanceFiles) {
        this.instanceFiles = instanceFiles;
    }
    
    public Map<CustomizableSideBar.SideBarPosition, List<SideBarFactory>> getFactories() {
        if (factories == null) {
            factories = computeInstances();
        }
        return factories;
    }
    
    public Class<SideBarFactoriesProvider> getClazz(){
        return SideBarFactoriesProvider.class;
    }
    
    /** Gets layer folder name, where the class should be found.
     *  Folder should be located in the appropriate mime type path, i.e.
     *  Editors/text/x-java/@lt;desired-layer-folder-name@gt;
     *  
     *  @return layer folder name
     */
    public String getLayerFolderName(){
        return SIDEBAR_COMPONENTS_FOLDER_NAME;
    }

    public InstanceProvider<SideBarFactoriesProvider> getInstanceProvider() {
        return new SideBarFactoriesProvider();
    }

    public SideBarFactoriesProvider createInstance(List<FileObject> fileObjectList) {
        return new SideBarFactoriesProvider(fileObjectList);
    }
    
    private Map<CustomizableSideBar.SideBarPosition, List<SideBarFactory>> computeInstances() {
        Map <CustomizableSideBar.SideBarPosition, List<SideBarFactory>> factoriesMap = new HashMap<CustomizableSideBar.SideBarPosition, List<SideBarFactory>>();
        
        for(FileObject f : instanceFiles) {
            SideBarFactory factory = null;
            
            if (!f.isValid() || !f.isData()) {
                continue;
            }
            
            try {
                DataObject dob = DataObject.find(f);
                InstanceCookie ic = dob.getCookie(InstanceCookie.class);
                if (ic != null && SideBarFactory.class.isAssignableFrom(ic.instanceClass())) {
                    factory = (SideBarFactory) ic.instanceCreate();
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
                continue;
            }

            if (factory != null) {
                SideBarPosition position = new SideBarPosition(f);
                List<SideBarFactory> factoriesList = factoriesMap.get(position);

                if (factoriesList == null) {
                    factoriesList = new ArrayList<SideBarFactory>();
                    factoriesMap.put(position, factoriesList);
                }

                factoriesList.add(factory);
            }
        }
        
        return factoriesMap;
    }
}
