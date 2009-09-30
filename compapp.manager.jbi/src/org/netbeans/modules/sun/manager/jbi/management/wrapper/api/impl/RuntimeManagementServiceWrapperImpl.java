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
package org.netbeans.modules.sun.manager.jbi.management.wrapper.api.impl;

import com.sun.esb.management.api.runtime.RuntimeManagementService;
import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import com.sun.jbi.ui.common.ServiceUnitInfo;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.sun.manager.jbi.management.JBIComponentType;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;

/**
 *
 * @author jqian
 */
public class RuntimeManagementServiceWrapperImpl
        implements RuntimeManagementServiceWrapper {

    private RuntimeManagementService service;
    
    // cache
    private List<JBIComponentInfo> serviceEngineStatusList;
    private List<JBIComponentInfo> bindingComponentStatusList;
    private List<JBIComponentInfo> sharedLibraryStatusList;
    private List<ServiceAssemblyInfo> serviceAssemblyStatusList;
    

    public RuntimeManagementServiceWrapperImpl(RuntimeManagementService service) {
        this.service = service;
    }

    public List<JBIComponentInfo> listBindingComponents(String target)
            throws ManagementRemoteException {
        if (bindingComponentStatusList == null) {
            String xml = service.listBindingComponents(target);
            bindingComponentStatusList = JBIComponentInfo.readFromXmlText(xml);
        }
        return bindingComponentStatusList;
    }

    public List<JBIComponentInfo> listServiceEngines(String target)
            throws ManagementRemoteException {
        if (serviceEngineStatusList == null) {
            String xml = service.listServiceEngines(target);
            serviceEngineStatusList = JBIComponentInfo.readFromXmlText(xml);
        }
        return serviceEngineStatusList;
    }

    public List<JBIComponentInfo> listSharedLibraries(String target)
            throws ManagementRemoteException {
        if (sharedLibraryStatusList == null) {
            String xml = service.listSharedLibraries(target);
            sharedLibraryStatusList = JBIComponentInfo.readFromXmlText(xml);
        }
        return sharedLibraryStatusList;
    }

    public List<ServiceAssemblyInfo> listServiceAssemblies(String target)
            throws ManagementRemoteException {
        if (serviceAssemblyStatusList == null) {
            String xml = service.listServiceAssemblies(target);
            serviceAssemblyStatusList = ServiceAssemblyInfo.readFromXmlTextWithProlog(xml);
        }
        return serviceAssemblyStatusList;
    }
    
    public List<ServiceAssemblyInfo> listServiceAssemblies(
            String componentName, String targetName)
            throws ManagementRemoteException {        
        String xml = service.listServiceAssemblies(componentName, targetName);
        return ServiceAssemblyInfo.readFromXmlTextWithProlog(xml);
    }
    
    public String shutdownComponent(String componentName,
            boolean force, String target)
            throws ManagementRemoteException {
        return service.shutdownComponent(componentName, force, target);
    }

    public String shutdownServiceAssembly(String serviceAssemblyName,
            boolean forceShutdown, String targetName)
            throws ManagementRemoteException {
        return service.shutdownServiceAssembly(serviceAssemblyName,
                forceShutdown, targetName);
    }

    public String startComponent(String componentName, String target)
            throws ManagementRemoteException {
        return service.startComponent(componentName, target);
    }

    public String startServiceAssembly(String serviceAssemblyName, String target)
            throws ManagementRemoteException {
        return service.startServiceAssembly(serviceAssemblyName, target);
    }

    public String stopComponent(String componentName, String target)
            throws ManagementRemoteException {
        return service.stopComponent(componentName, target);
    }

    public String stopServiceAssembly(String serviceAssemblyName, String target)
            throws ManagementRemoteException {
        return service.stopServiceAssembly(serviceAssemblyName, target);
    }

    // EXTRA
    
    public List<JBIComponentInfo> listJBIComponents(JBIComponentType compType,
            String target) throws ManagementRemoteException {
         List<JBIComponentInfo> compList = null;
        
        if (JBIComponentType.SERVICE_ENGINE.equals(compType)) {
            compList = listServiceEngines(target);
        } else if (JBIComponentType.BINDING_COMPONENT.equals(compType)) {
            compList = listBindingComponents(target);
        } else if (JBIComponentType.SHARED_LIBRARY.equals(compType)) {            
            compList = listSharedLibraries(target);
        } else {
            assert false;
        }
          
        return compList;
    }
    
    public void clearServiceAssemblyStatusCache() {
        serviceAssemblyStatusList = null;
    }

    public void clearJBIComponentStatusCache(JBIComponentType compType) {
        switch (compType) {
            case SERVICE_ENGINE:
                serviceEngineStatusList = null;
                break;
            case BINDING_COMPONENT:
                bindingComponentStatusList = null;
                break;
            case SHARED_LIBRARY:
                sharedLibraryStatusList = null;
                break;
            default:
                assert false : "Unknown component type: " + compType;
        }
    }
        
    public List<String> getServiceAssemblyNames(
            String componentName, String targetName)
            throws ManagementRemoteException {
        List<String> ret = new ArrayList<String>();
        
        List<ServiceAssemblyInfo> saInfos = 
                listServiceAssemblies(componentName, targetName);
        
        for (ServiceAssemblyInfo info : saInfos) {
            ret.add(info.getName());
        }
        
        return ret;
    }
    
    public ServiceAssemblyInfo getServiceAssembly(String assemblyName,
            String target) throws ManagementRemoteException {
        
        for (ServiceAssemblyInfo assembly : listServiceAssemblies(target)) {
            if (assembly.getName().equals(assemblyName)) {
                return assembly;
            }
        }
        return null;
    }
    
    public ServiceUnitInfo getServiceUnit(String saName,
            String suName, String target) throws ManagementRemoteException {      
        
        ServiceAssemblyInfo assembly = getServiceAssembly(saName, target);        
        if (assembly != null) {
            List<ServiceUnitInfo> units = assembly.getServiceUnitInfoList();
            for (ServiceUnitInfo unit : units) {
                if (unit.getName().equals(suName)) {
                    return unit;
                }
            }
        }        
        return null;
    }

    public JBIComponentInfo getJBIComponent(JBIComponentType compType, 
            String compName, String target) throws ManagementRemoteException {
        List<JBIComponentInfo> compList = null;
        
        if (JBIComponentType.SERVICE_ENGINE.equals(compType)) {
            compList = listServiceEngines(target);
        } else if (JBIComponentType.BINDING_COMPONENT.equals(compType)) {
            compList = listBindingComponents(target);
        } else if (JBIComponentType.SHARED_LIBRARY.equals(compType)) {            
            compList = listSharedLibraries(target);
        } else {
            assert false;
        }
        
        for (JBIComponentInfo comp : compList) {
            if (comp.getName().equals(compName)) {
                return comp;
            }
        }
        return null;
    }
}
