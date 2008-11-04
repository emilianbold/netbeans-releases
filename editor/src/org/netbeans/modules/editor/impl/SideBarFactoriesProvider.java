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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.editor.mimelookup.Class2LayerFolder.class)
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
