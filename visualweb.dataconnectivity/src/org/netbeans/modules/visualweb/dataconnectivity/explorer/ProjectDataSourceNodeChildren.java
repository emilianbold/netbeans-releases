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
package org.netbeans.modules.visualweb.dataconnectivity.explorer;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.api.project.Project;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceListener;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceChangeEvent;
import java.awt.Image;
import java.beans.PropertyEditor;
import java.io.CharConversionException;
import java.util.logging.Logger;
import javax.naming.NamingException;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.BrokenDataSourceSupport;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;

/**
 * This class builds the visual representation of data sources (list) in the Project Navigator
 * @author Winston Prakash
 */
public class ProjectDataSourceNodeChildren extends Children.Keys implements Node.Cookie, ProjectDataSourceListener {

    Project nbProject = null;
    String url;
    
    private final String DYNAMIC_DATA_SOURCES = "dynamic_data_sources"; // NOI18N
    private final String HARD_CODED_DATA_SOURCES = "hard_coded_data_sources"; // NOI18N

    public ProjectDataSourceNodeChildren(Project project) {
        nbProject = project;
    }
    protected void addNotify() {
        super.addNotify();
        updateKeys();
        ProjectDataSourceTracker.addListener(nbProject, this);
    }

    protected void removeNotify() {
        ProjectDataSourceTracker.removeListener( nbProject,this);

        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }

    private void updateKeys() {
        List dataSourceTypes = new ArrayList();

        dataSourceTypes.add(DYNAMIC_DATA_SOURCES);
        refreshKey(DYNAMIC_DATA_SOURCES);
        dataSourceTypes.add(HARD_CODED_DATA_SOURCES);
        refreshKey(HARD_CODED_DATA_SOURCES);

        if (!dataSourceTypes.isEmpty()) {
            setKeys(dataSourceTypes);
        }else{
            setKeys(Collections.EMPTY_SET);
        }
    }

    protected Node[] createNodes(Object key) {
        String type = (String) key;
        Node[] nodeArray = null;
        if (type.equals(DYNAMIC_DATA_SOURCES)) {

            String[] dynamicDataSources
                    = ProjectDataSourceTracker.getDynamicDataSources( nbProject );

            if(dynamicDataSources != null && dynamicDataSources.length > 0){
                nodeArray = new Node[dynamicDataSources.length];
                for(int i=0; i< dynamicDataSources.length; i++){
                    nodeArray[i] = createDatasourceNode(dynamicDataSources[i]);
                }
            }
        }else if (type.equals(HARD_CODED_DATA_SOURCES)) {

            String[] hardCodedDataSources
                    = ProjectDataSourceTracker.getHardcodedDataSources( nbProject);

            if(hardCodedDataSources != null && hardCodedDataSources.length > 0){
                nodeArray = new Node[hardCodedDataSources.length];
                for(int i=0; i< hardCodedDataSources.length; i++){
                    nodeArray[i] = createDatasourceNode(hardCodedDataSources[i]);
                }
            }
        }
        return nodeArray;
    }

    private Node createDatasourceNode(String datasourceName){
        AbstractNode node = new AbstractNode(Children.LEAF){
            final Image icon =  ImageUtilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource_container.png"); // NOI18N
            final Image disconnectedIcon =  ImageUtilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/disconnected.png"); // NOI18N
            Image brokenBadgedImage = ImageUtilities.mergeImages(icon, disconnectedIcon, 8, 0);
            Image datasourceImage = ImageUtilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource.png");  // NOI18N
            String dispName;
            
            public Image getIcon(int type) {
                
                dispName = super.getDisplayName();
                try {
                     if (BrokenDataSourceSupport.isBroken(nbProject)) {
                        dispName = "<font color=\"#A40000\">" + dispName + "</font>"; //NOI18N;
                    } else {
                        dispName = XMLUtil.toElementContent(dispName);
                    }
                } catch (CharConversionException ex) {
                    // ignore
                }
                
                if (BrokenDataSourceSupport.isBroken(nbProject)) {
                    return brokenBadgedImage;
                } else {
                    return datasourceImage;
                }                
            }
            
            public Image getOpenedIcon(int type){
                return getIcon(type);
            }     
            
            protected Sheet createSheet() {
                Sheet result = super.createSheet();
                Sheet.Set set = result.createPropertiesSet();
                set.put(new DataSourceURLProperty());
                result.put(set);
                return result;
            }

            final class DataSourceURLProperty extends PropertySupport.ReadOnly {
                private String url;
                
                DataSourceURLProperty() {
                    super("URL", DataSourceURLProperty.class, NbBundle.getMessage(DataSourceURLProperty.class, "LBL_URL"), NbBundle.getMessage(DataSourceURLProperty.class, "DESC_URL") + ", " + dispName);
                    try {
                        url = (String) getValue();
                    } catch (IllegalAccessException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                public Object getValue() throws IllegalAccessException, InvocationTargetException {                    
                    ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(nbProject);
                    RequestedJdbcResource jdbcResource = null;
                    jdbcResource = projectDataSourceManager.getDataSourceWithName(dispName);
                    
                    if (jdbcResource != null) {
                        return jdbcResource.getUrl();
                    } else {
                        return "";
                    }
                }

                public PropertyEditor getPropertyEditor() {
                    return new DataSourcePropertyEditor(url);
                }
            }                                          
        };
        

        String hoser = "java:comp/env/jdbc/" ;
        int pos = datasourceName.indexOf(hoser) ;
        String dsName = datasourceName.substring(hoser.length()) ;
        node.setName(datasourceName);
        node.setDisplayName(dsName);
        node.setShortDescription(dsName);
        
        Image icon = ImageUtilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource.png");  // NOI18N
        node.setIconBaseWithExtension("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource.png"); //NOI18N
        
        new ProjectDataSourceNode(nbProject);
        
        return node;
    }
    
    public void dataSourceChange(ProjectDataSourceChangeEvent evt) {
        updateKeys();                
    }
    
}
