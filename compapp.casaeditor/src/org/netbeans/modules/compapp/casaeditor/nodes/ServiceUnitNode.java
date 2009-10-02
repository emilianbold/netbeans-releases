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

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.Action;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.LoadWSDLPortsAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddConsumesPinAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddProvidesPinAction;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaAttribute;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.openide.util.Exceptions;

/**
 *
 * @author Josh Sandusky
 */
public class ServiceUnitNode extends CasaNode {
    
    private static final Image DEFAULT_ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/ServiceUnitNode.png");     // NOI18N
    
    private static final String CHILD_ID_PROVIDES_LIST = "ProvidesList";        // NOI18N
    private static final String CHILD_ID_CONSUMES_LIST = "ConsumesList";        // NOI18N
        
    public ServiceUnitNode(CasaServiceEngineServiceUnit component, CasaNodeFactory factory) {
        super(component, new MyChildren(component, factory), factory);
    }
        
    @Override
    protected void addCustomActions(List<Action> actions) {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su == null) {
            return;
        }
        
        if (su.isInternal()) {
            actions.add(SystemAction.get(LoadWSDLPortsAction.class));
        } else {
            actions.add(SystemAction.get(AddConsumesPinAction.class));
            actions.add(SystemAction.get(AddProvidesPinAction.class));
        }
    }

    @Override
    public String getName() {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData(); 
        if (su != null) {
            //return NbBundle.getMessage(getClass(), "LBL_ServiceUnit");      // NOI18N
            return su.getUnitName();
        }
        return super.getName();
    }

    /*
    @Override
    public String getHtmlDisplayName() {
        try {
            String htmlDisplayName = getName();
            CasaServiceEngineServiceUnit casaSU = (CasaServiceEngineServiceUnit) getData();
            String decoration = null;
            if (casaSU != null) {
                decoration = NbBundle.getMessage(WSDLEndpointNode.class, "LBL_NameAttr",        // NOI18N
                        casaSU.getUnitName());
            }
            if (decoration == null) {
                return htmlDisplayName;
            }
            return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";        // NOI18N
        } catch (Throwable t) {
            // getHtmlDisplayName MUST recover gracefully.
            return getBadName();
        }        
    }
    */
    
    @Override
    protected void setupPropertySheet(Sheet sheet) {
        final CasaServiceEngineServiceUnit casaSU = (CasaServiceEngineServiceUnit) getData();
        if (casaSU == null) {
            return;
        }        
        Sheet.Set identificationProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.IDENTIFICATION_SET);
        
        PropertyUtils.installServiceUnitNameProperty(
                identificationProperties, this, casaSU,
                CasaAttribute.UNIT_NAME.getName(),
                "serviceUnitName",                                      // NOI18N
                NbBundle.getMessage(getClass(), "PROP_Name"),           // NOI18N
                NbBundle.getMessage(getClass(), "PROP_Name"));          // NOI18N
        
        Node.Property<String> descriptionSupport = new PropertySupport.ReadOnly<String>(
                "description", // NOI18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_Description"),    // NOI18N
                Constants.EMPTY_STRING) {
            public String getValue() {
                return casaSU.getDescription();
            }
        };
        identificationProperties.put(descriptionSupport);
        
        Sheet.Set targetProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.TARGET_SET);
        Node.Property<String> artifactsZipSupport = new PropertySupport.ReadOnly<String>(
                "artifactsZip", // NOI18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_ArtifactsZip"),   // NOI18N
                Constants.EMPTY_STRING) {
            public String getValue() {
                return casaSU.getArtifactsZip();
            }
        };
        targetProperties.put(artifactsZipSupport);
        
        Node.Property<String> componentNameSupport = new PropertySupport.ReadOnly<String>(
                "componentName", // NOI18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_ComponentName"),  // NOI18N
                Constants.EMPTY_STRING) {
            public String getValue() {
                return casaSU.getComponentName();
            }
        };
        targetProperties.put(componentNameSupport);
    }

    
    private static class MyChildren extends CasaNodeChildren {
        public MyChildren(CasaComponent component, CasaNodeFactory factory) {
            super(component, factory);
        }
        protected Node[] createNodes(Object key) {
            assert key instanceof String;
            CasaServiceEngineServiceUnit serviceUnit = (CasaServiceEngineServiceUnit) getData();
            if (serviceUnit != null) {
                CasaWrapperModel model = mNodeFactory.getCasaModel();
                if (model != null) {
                    String keyName = (String) key;
                    if (keyName.equals(CHILD_ID_CONSUMES_LIST)) {
                        return new Node[] { mNodeFactory.createNode_consumesList(serviceUnit.getConsumes()) };
                    } else if (keyName.equals(CHILD_ID_PROVIDES_LIST)) {
                        return new Node[] { mNodeFactory.createNode_providesList(serviceUnit.getProvides()) };
                    } else {
                        CasaEndpoint endpoint = getEndpoint(keyName);
                        return new Node[] { mNodeFactory.createNode_process(endpoint) };
                    }
                }
            }
            return null;
        }
        @Override
        public Object getChildKeys(Object data)  {
            List<String> children = new ArrayList<String>();
            Set<String> processInfo = getProcessNames();
            if (!CasaFactory.getCasaCustomizer().getBOOLEAN_CLASSIC_SESU_LAYOUT_STYLE() 
                    || processInfo.size() == 0) {
                children.add(CHILD_ID_CONSUMES_LIST);
                children.add(CHILD_ID_PROVIDES_LIST);
            } else {
                children.addAll(processInfo);
            }
            return children;
        }
        private Set<String> getProcessNames() {
            Set<String> ret = new HashSet<String>();
            
            List<CasaEndpointRef> endpointRefs = new ArrayList<CasaEndpointRef>();
            CasaServiceEngineServiceUnit serviceUnit = (CasaServiceEngineServiceUnit) getData();
            if (serviceUnit != null) {
                endpointRefs.addAll(serviceUnit.getConsumes());
                endpointRefs.addAll(serviceUnit.getProvides());
                for (CasaEndpointRef endpointRef : endpointRefs) {
                    CasaEndpoint endpoint = endpointRef.getEndpoint().get();
                    String processName = endpoint.getProcessName();
                    if (processName != null && processName.length() > 0) { 
                        ret.add(processName);
                    }
                }
            }
            return ret;
        }
        
        private CasaEndpoint getEndpoint(String processName) {
            
            List<CasaEndpointRef> endpointRefs = new ArrayList<CasaEndpointRef>();
            CasaServiceEngineServiceUnit serviceUnit = (CasaServiceEngineServiceUnit) getData();
            if (serviceUnit != null) {
                endpointRefs.addAll(serviceUnit.getConsumes());
                endpointRefs.addAll(serviceUnit.getProvides());
                for (CasaEndpointRef endpointRef : endpointRefs) {
                    CasaEndpoint endpoint = endpointRef.getEndpoint().get();
                    String myProcessName = endpoint.getProcessName();
                    if (myProcessName != null && myProcessName.equals(processName)) { 
                        return endpoint;
                    }
                }
            }
            return null;
        }
    }
    
    @Override
    public Image getIcon(int type) {
        CasaServiceEngineServiceUnit sesu = (CasaServiceEngineServiceUnit) getData();
        return getProjectIconImage(sesu.getComponentName());
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    @Override
    public boolean isEditable(String propertyType) {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su != null) {
            return getModel().isEditable(su, propertyType);
        }
        return false;
    }
    
    @Override
    public boolean isDeletable() {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su != null) {
            return getModel().isDeletable(su);
        }
        return false;
    }
    
    public static Image getProjectIconImage(String compName) {
        Image ret = DEFAULT_ICON;       
      
        JbiDefaultComponentInfo defaultCompInfo = 
                JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        JBIComponentStatus compStatus = defaultCompInfo.getComponentHash().get(compName);
        
        URL projectIconURL = null;        
        if (compStatus != null) {
            projectIconURL = compStatus.getProjectIconURL();
            if (projectIconURL != null) {
                try {
                    ret = ImageIO.read(projectIconURL);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        return ret;
    }
}
