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
package org.netbeans.modules.sun.manager.jbi.management.wrapper.api;

import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import com.sun.jbi.ui.common.ServiceUnitInfo;
import java.util.List;
import org.netbeans.modules.sun.manager.jbi.management.JBIComponentType;

/**
 * Defines operations for common runtime management services. Common runtime
 * management operations include listing component containers available in the
 * runtime, composite applications deployed, controlling lifecycle across the
 * runtime and composite applications, getting state of each container and
 * composite application, etc.
 * 
 * @author graj
 */
public interface RuntimeManagementServiceWrapper {
    /**
     * Checks to see if the Target (server, cluster) is up or down.
     * 
     * @param targetName
     *            name of the target (e.g., cluster1, server, etc.)
     * @return true if Target is up, false if not
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
//    public boolean isTargetUp(String targetName)
//            throws ManagementRemoteException;
    
    /**
     * return component info xml text that has only binding component infos.
     * 
     * @param targetName
     *            name of the target for this operation
     * @return the component info xml text.
     * @throws ManagementRemoteException
     *             on error
     */
    public List<JBIComponentInfo> listBindingComponents(String targetName)
            throws ManagementRemoteException;
    
    /**
     * return component info xml text that has only binding component infos
     * which satisfies the options passed to the method.
     * 
     * @param state
     *            return all the binding components that are in the specified
     *            state. valid states are JBIComponentInfo.STARTED, STOPPED,
     *            INSTALLED or null for ANY state
     * @param sharedLibraryName
     *            return all the binding components that have a dependency on
     *            the specified shared library. null value to ignore this
     *            option.
     * @param serviceAssemblyName
     *            return all the binding components that have the specified
     *            service assembly deployed on them. null value to ignore this
     *            option.
     * @param targetName
     *            name of the target for this operation
     * @return xml text contain the list of binding component infos
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
//    public String listBindingComponents(String state, String sharedLibraryName,
//            String serviceAssemblyName, String targetName)
//            throws ManagementRemoteException;
    
    /**
     * returns a list of Service Assembly Infos in a xml format.
     * 
     * @param targetName
     *            name of the target for this operation
     * @return xml text containing the Service Assembly infos
     * @throws ManagementRemoteException
     *             on error
     */
    public List<ServiceAssemblyInfo> listServiceAssemblies(String targetName)
            throws ManagementRemoteException;
    
    /**
     * returns the list of service asssembly infos in a xml format that have the
     * service unit deployed on the specified component.
     * 
     * @param componentName
     *            to list all the service assemblies that have some deployments
     *            on this component.
     * @param targetName
     *            name of the target for this operation
     * @return xml string contain the list of service assembly infos
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
    public List<ServiceAssemblyInfo> listServiceAssemblies(
            String componentName, String targetName)
            throws ManagementRemoteException;
    
    /**
     * returns the list of service asssembly infos in a xml format that have the
     * service unit deployed on the specified component.
     * 
     * @param state
     *            to return all the service assemblies that are in the specified
     *            state. JBIServiceAssemblyInfo.STARTED, STOPPED, SHUTDOWN or
     *            null for ANY state
     * @param componentName
     *            to list all the service assemblies that have some deployments
     *            on this component.
     * @param targetName
     *            name of the target for this operation
     * @return xml string contain the list of service assembly infos
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
//    public String listServiceAssemblies(String state, String componentName,
//            String targetName) throws ManagementRemoteException;
    
    /**
     * Returns the component info xml text that has only service engine
     * information
     * 
     * @param targetName
     *            name of the target for this operation
     * @return the component info xml text.
     * @throws ManagementRemoteException
     *             on error
     */
    public List<JBIComponentInfo> listServiceEngines(String targetName)
            throws ManagementRemoteException;
    
    /**
     * return component info xml text that has only service engine infos which
     * satisfies the options passed to the method.
     * 
     * @param state
     *            return all the service engines that are in the specified
     *            state. valid states are JBIComponentInfo.STARTED, STOPPED,
     *            INSTALLED or null for ANY state
     * @param sharedLibraryName
     *            return all the service engines that have a dependency on the
     *            specified shared library. null value to ignore this option.
     * @param serviceAssemblyName
     *            return all the service engines that have the specified service
     *            assembly deployed on them. null value to ignore this option.
     * @param targetName
     *            name of the target for this operation
     * @return xml text contain the list of service engine component infos
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
//    public String listServiceEngines(String state, String sharedLibraryName,
//            String serviceAssemblyName, String targetName)
//            throws ManagementRemoteException;
    
    /**
     * return component info xml text that has only shared library infos.
     * 
     * @param targetName
     *            name of the target for this operation
     * @return the component info xml text.
     * @throws ManagementRemoteException
     *             on error
     */
    public List<JBIComponentInfo> listSharedLibraries(String targetName)
            throws ManagementRemoteException;
    
    /**
     * returns the list of Shared Library infos in the in a xml format
     * 
     * @param componentName
     *            to return only the shared libraries that are this component
     *            dependents. null for listing all the shared libraries in the
     *            system.
     * @param targetName
     *            name of the target for this operation
     * @return xml string contain the list of componentinfos for shared
     *         libraries.
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
//    public String listSharedLibraries(String componentName, String targetName)
//            throws ManagementRemoteException;
    
    /**
     * returns a list of Binding Component and Service Engine infos in xml
     * format, that are dependent upon a specified Shared Library
     * 
     * @param sharedLibraryName
     *            the shared library name
     * @param targetName
     *            name of the target for this operation
     * @return xml string containing the list of componentInfos
     * @throws ManagementRemoteException
     *             on error
     */
//    public String listSharedLibraryDependents(String sharedLibraryName,
//            String targetName) throws ManagementRemoteException;
    
    /**
     * return component info xml text for the specified binding component if
     * exists. If no binding component with that name exists, it returns the xml
     * with empty list.
     * 
     * @param name
     *            name of the binding component to lookup
     * @param state
     *            return the binding component that is in the specified state.
     *            valid states are JBIComponentInfo.STARTED, STOPPED, INSTALLED
     *            or null for ANY state
     * @param sharedLibraryName
     *            return the binding component that has a dependency on the
     *            specified shared library. null value to ignore this option.
     * @param serviceAssemblyName
     *            return the binding component that has the specified service
     *            assembly deployed on it. null value to ignore this option.
     * @param targetName
     *            name of the target for this operation
     * @return xml text contain the binding component info that confirms to the
     *         component info list xml grammer.
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
//    public String showBindingComponent(String name, String state,
//            String sharedLibraryName, String serviceAssemblyName,
//            String targetName) throws ManagementRemoteException;
    
    /**
     * return service assembly info xml text for the specified service assembly
     * if exists. If no service assembly with that name exists, it returns the
     * xml with empty list.
     * 
     * @param name
     *            name of the service assembly to lookup
     * @param state
     *            return the service assembly that is in the specified state.
     *            JBIServiceAssemblyInfo.STARTED, STOPPED, SHUTDOWN or null for
     *            ANY state
     * @param componentName
     *            return the service assembly that has service units on this
     *            component.
     * @param targetName
     *            name of the target for this operation
     * @return xml string contain service assembly info that confirms to the
     *         service assembly list xml grammer.
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
//    public String showServiceAssembly(String name, String state,
//            String componentName, String targetName)
//            throws ManagementRemoteException;
    
    /**
     * return component info xml text for the specified service engine if
     * exists. If no service engine with that name exists, it returns the xml
     * with empty list.
     * 
     * @param name
     *            name of the service engine to lookup
     * @param state
     *            return service engine that is in the specified state. valid
     *            states are JBIComponentInfo.STARTED, STOPPED, INSTALLED or
     *            null for ANY state
     * @param sharedLibraryName
     *            return service engine that has a dependency on the specified
     *            shared library. null value to ignore this option.
     * @param serviceAssemblyName
     *            return the service engine that has the specified service
     *            assembly deployed on it. null value to ignore this option.
     * @param targetName
     *            name of the target for this operation
     * @return xml text contain the service engine component info that confirms
     *         to the component info list xml grammer.
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
//    public String showServiceEngine(String name, String state,
//            String sharedLibraryName, String serviceAssemblyName,
//            String targetName) throws ManagementRemoteException;
    
    /**
     * return component info xml text for the specified shared library if
     * exists. If no shared library with that name exists, it returns the xml
     * with empty list.
     * 
     * @param name
     *            name of the shared library to lookup
     * @param componentName
     *            return the shared library that is this component dependents.
     *            null to ignore this option.
     * @param targetName
     *            name of the target for this operation
     * @return xml string contain shared library component info that confirms to
     *         the component info list xml grammer.
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     */
//    public String showSharedLibrary(String name, String componentName,
//            String targetName) throws ManagementRemoteException;
    
    /**
     * shutdown the component
     * 
     * @param componentName
     *            name of the runtime component. The name should uniquely
     *            identify within the runtime application server
     * @param force
     *            forcefully shutdown the component
     * @param target -
     *            name of server target for which this operation is invoked
     * @return result string of the operation
     * @throws ManagementRemoteException
     *             on error
     */
    public String shutdownComponent(String componentName, boolean force,
            String target) throws ManagementRemoteException;
    
    /**
     * shutdown the component
     * 
     * @param componentName
     *            name of the runtime component. The name should uniquely
     *            identify within the runtime application server
     * @param target -
     *            name of server target for which this operation is invoked
     * @return result string of the operation
     * @throws ManagementRemoteException
     *             on error
     */
//    public String shutdownComponent(String componentName, String target)
//            throws ManagementRemoteException;
    
    /**
     * shuts down service assembly
     * 
     * @param serviceAssemblyName
     *            name of the service assembly
     * @param forceShutdown
     * @param targetName
     *            name of the target for this operation
     * @return result as a management message xml text
     * @throws ManagementRemoteException
     *             on error
     */
    public String shutdownServiceAssembly(String serviceAssemblyName,
            boolean forceShutdown, String targetName)
            throws ManagementRemoteException;
    
    /**
     * shuts down service assembly
     * 
     * @param serviceAssemblyName
     *            name of the service assembly
     * @param targetName
     *            name of the target for this operation
     * @return result as a management message xml text
     * @throws ManagementRemoteException
     *             on error
     */
//    public String shutdownServiceAssembly(String serviceAssemblyName,
//            String targetName) throws ManagementRemoteException;
    
    /**
     * start the component
     * 
     * @param componentName
     *            name of the runtime component. The name should uniquely
     *            identify within the runtime application server
     * @param target -
     *            name of server target for which this operation is invoked
     * @return result string of the operation
     * @throws ManagementRemoteException
     *             on error
     */
    public String startComponent(String componentName, String target)
            throws ManagementRemoteException;
    
    /**
     * starts service assembly
     * 
     * @param serviceAssemblyName
     *            name of the service assembly
     * @param targetName
     *            name of the target for this operation
     * @return result as a management message xml text
     * @throws ManagementRemoteException
     *             on error
     */
    public String startServiceAssembly(String serviceAssemblyName,
            String targetName) throws ManagementRemoteException;
    
    /**
     * stop the component
     * 
     * @param componentName
     *            name of the runtime component. The name should uniquely
     *            identify within the runtime application server
     * @param target -
     *            name of server target for which this operation is invoked
     * @return result string of the operation
     * @throws ManagementRemoteException
     *             on error
     */
    public String stopComponent(String componentName, String target)
            throws ManagementRemoteException;
    
    /**
     * stops service assembly
     * 
     * @param serviceAssemblyName
     *            name of the service assembly
     * @param targetName
     *            name of the target for this operation
     * @return result as a management message xml text
     * @throws ManagementRemoteException
     *             on error
     */
    public String stopServiceAssembly(String serviceAssemblyName,
            String targetName) throws ManagementRemoteException;
    
    // ///////////////////////////////////////////
    // Start of Cumulative Operation Definitions
    // ///////////////////////////////////////////
    
    /**
     * return component info xml text that has only binding component infos.
     * 
     * @param targetNames
     * @return the component info xml text as a Map of [targetName, xmlString].
     * @throws ManagementRemoteException
     *             on error
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listBindingComponents(
//            String[] targetNames) throws ManagementRemoteException;
    
    /**
     * return component info xml text that has only binding component infos
     * which satisfies the options passed to the method.
     * 
     * @param state
     *            return all the binding components that are in the specified
     *            state. valid states are JBIComponentInfo.STARTED, STOPPED,
     *            INSTALLED or null for ANY state
     * @param sharedLibraryName
     *            return all the binding components that have a dependency on
     *            the specified shared library. null value to ignore this
     *            option.
     * @param serviceAssemblyName
     *            return all the binding components that have the specified
     *            service assembly deployed on them. null value to ignore this
     *            option.
     * @param targetName
     * @return xml text contain the list of binding component infos as map of
     *         [targetName, xmlString]
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listBindingComponents(
//            String state, String sharedLibraryName, String serviceAssemblyName,
//            String[] targetNames) throws ManagementRemoteException;
    
    /**
     * returns a list of Service Assembly Infos in a xml format.
     * 
     * @param targetNames
     * @return xml text containing the Service Assembly infos as map of
     *         [targetName, xmlString]
     * @throws ManagementRemoteException
     *             on error
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listServiceAssemblies(
//            String[] targetNames) throws ManagementRemoteException;
    
    /**
     * returns the list of service assembly infos in a xml format that have the
     * service unit deployed on the specified component.
     * 
     * @param componentName
     *            to list all the service assemblies that have some deployments
     *            on this component.
     * @param targetNames
     * @return xml string contain the list of service assembly infos as map of
     *         [targetName, xmlString]
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listServiceAssemblies(
//            String componentName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * returns the list of service asssembly infos in a xml format that have the
     * service unit deployed on the specified component.
     * 
     * @param state
     *            to return all the service assemblies that are in the specified
     *            state. JBIServiceAssemblyInfo.STARTED, STOPPED, SHUTDOWN or
     *            null for ANY state
     * @param componentName
     *            to list all the service assemblies that have some deployments
     *            on this component.
     * @param targetNames
     * @return xml string contain the list of service assembly infos as map of
     *         [targetName, xmlString]
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listServiceAssemblies(
//            String state, String componentName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * return component info xml text that has only service engine infos.
     * 
     * @param targetName
     * @return the component info xml text as map of [targetName,xmlString].
     * @throws ManagementRemoteException
     *             on error
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listServiceEngines(
//            String[] targetNames) throws ManagementRemoteException;
    
    /**
     * return component info xml text that has only service engine infos which
     * satisfies the options passed to the method.
     * 
     * @param state
     *            return all the service engines that are in the specified
     *            state. valid states are JBIComponentInfo.STARTED, STOPPED,
     *            INSTALLED or null for ANY state
     * @param sharedLibraryName
     *            return all the service engines that have a dependency on the
     *            specified shared library. null value to ignore this option.
     * @param serviceAssemblyName
     *            return all the service engines that have the specified service
     *            assembly deployed on them. null value to ignore this option.
     * @param targetName
     * @return xml text contain the map of service engine component infos as
     *         [targetName, xmlString]
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listServiceEngines(
//            String state, String sharedLibraryName, String serviceAssemblyName,
//            String[] targetNames) throws ManagementRemoteException;
    
    /**
     * return component info xml text that has only shared library infos.
     * 
     * @param targetName
     * @return the component info xml text as a map of [targetName, xmlString].
     * @throws ManagementRemoteException
     *             on error
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listSharedLibraries(
//            String[] targetNames) throws ManagementRemoteException;
    
    /**
     * returns the list of Shared Library infos in the in a xml format
     * 
     * @param componentName
     *            to return only the shared libraries that are this component
     *            dependents. null for listing all the shared libraries in the
     *            system.
     * @param targetName
     * @return xml string contains the map of componentinfos for shared
     *         libraries as [targetName, xmlString].
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listSharedLibraries(
//            String componentName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * returns a list of Binding Component and Service Engine infos in xml
     * format, that are dependent upon a specified Shared Library
     * 
     * @param sharedLibraryName
     *            the shared library name
     * @param targetName
     * @return xml string containing the map of componentInfos as [targetName,
     *         xmlString]
     * @throws ManagementRemoteException
     *             on error
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> listSharedLibraryDependents(
//            String sharedLibraryName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * return component info xml text for the specified binding component if
     * exists. If no binding component with that name exists, it returns the xml
     * with empty list.
     * 
     * @param name
     *            name of the binding component to lookup
     * @param state
     *            return the binding component that is in the specified state.
     *            valid states are JBIComponentInfo.STARTED, STOPPED, INSTALLED
     *            or null for ANY state
     * @param sharedLibraryName
     *            return the binding component that has a dependency on the
     *            specified shared library. null value to ignore this option.
     * @param serviceAssemblyName
     *            return the binding component that has the specified service
     *            assembly deployed on it. null value to ignore this option.
     * @param targetName
     * @return xml text contain the binding component info that confirms to the
     *         component info list xml grammer as a map of [targetName,
     *         xmlString].
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> showBindingComponent(
//            String name, String state, String sharedLibraryName,
//            String serviceAssemblyName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * return service assembly info xml text for the specified service assembly
     * if exists. If no service assembly with that name exists, it returns the
     * xml with empty list.
     * 
     * @param name
     *            name of the service assembly to lookup
     * @param state
     *            return the service assembly that is in the specified state.
     *            JBIServiceAssemblyInfo.STARTED, STOPPED, SHUTDOWN or null for
     *            ANY state
     * @param componentName
     *            return the service assembly that has service units on this
     *            component.
     * @param targetNames
     * @return xml string contain service assembly info that confirms to the
     *         service assembly list xml grammer as [targetName, xmlString] map.
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> showServiceAssembly(
//            String name, String state, String componentName,
//            String[] targetNames) throws ManagementRemoteException;
    
    /**
     * return component info xml text for the specified service engine if
     * exists. If no service engine with that name exists, it returns the xml
     * with empty list.
     * 
     * @param name
     *            name of the service engine to lookup
     * @param state
     *            return service engine that is in the specified state. valid
     *            states are JBIComponentInfo.STARTED, STOPPED, INSTALLED or
     *            null for ANY state
     * @param sharedLibraryName
     *            return service engine that has a dependency on the specified
     *            shared library. null value to ignore this option.
     * @param serviceAssemblyName
     *            return the service engine that has the specified service
     *            assembly deployed on it. null value to ignore this option.
     * @param targetName
     * @return xml text contain the service engine component info that confirms
     *         to the component info list xml grammer as a map of [targetName,
     *         xmlString].
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> showServiceEngine(
//            String name, String state, String sharedLibraryName,
//            String serviceAssemblyName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * return component info xml text for the specified shared library if
     * exists. If no shared library with that name exists, it returns the xml
     * with empty list.
     * 
     * @param name
     *            name of the shared library to lookup
     * @param componentName
     *            return the shared library that is this component dependents.
     *            null to ignore this option.
     * @param targetName
     * @return xml string contain shared library component info that confirms to
     *         the component info list xml grammer as a map of [targetName,
     *         xmlString].
     * @throws ManagementRemoteException
     *             if error or exception occurs.
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> showSharedLibrary(
//            String name, String componentName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * shuts down component (service engine, binding component)
     * 
     * @param componentName
     *            name of the component
     * @param targetNames
     * @return name of the component as [targetName, string] map
     * @throws ManagementRemoteException
     *             on error
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> shutdownComponent(
//            String componentName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * shuts down component (service engine, binding component)
     * 
     * @param componentName
     *            name of the component
     * @param force
     *            true to force shutdown
     * @param targetNames
     * @return name of the component as [targetName, string] map
     * @throws ManagementRemoteException
     *             on error
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> shutdownComponent(
//            String componentName, boolean force, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * shuts down service assembly
     * 
     * @param serviceAssemblyName
     *            name of the service assembly
     * @param targetNames
     * @throws ManagementRemoteException
     *             on error
     * @return result as a management message xml text as [targetName, string]
     *         map
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> shutdownServiceAssembly(
//            String serviceAssemblyName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * shuts down service assembly
     * 
     * @param serviceAssemblyName
     *            name of the service assembly
     * @param forceShutdown
     * @param targetName
     *            name of the target for this operation
     * @return Map of targetName and result as a management message xml text
     *         strings.
     * @throws ManagementRemoteException
     *             on error
     */
//    public Map<String /* targetName */, String /* targetResult */> shutdownServiceAssembly(
//            String serviceAssemblyName, boolean forceShutdown,
//            String[] targetNames) throws ManagementRemoteException;
    
    /**
     * starts component ( service engine, binding component)
     * 
     * @param componentName
     *            name of the component
     * @param targetNames
     * @throws ManagementRemoteException
     *             on error
     * @return name of the component as [targetName, string] map
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> startComponent(
//            String componentName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * starts service assembly
     * 
     * @param serviceAssemblyName
     *            name of the service assembly
     * @param targetNames
     * @throws ManagementRemoteException
     *             on error
     * @return result as a management message xml text as [targetName, string]
     *         map
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> startServiceAssembly(
//            String serviceAssemblyName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * stops component ( service engine, binding component)
     * 
     * @param componentName
     *            name of the component
     * @param targetNames
     * @return name of the component as [targetName, string] map
     * @throws ManagementRemoteException
     *             on error
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> stopComponent(
//            String componentName, String[] targetNames)
//            throws ManagementRemoteException;
    
    /**
     * stops service assembly
     * 
     * @param serviceAssemblyName
     *            name of the service assembly
     * @param targetNames
     * @return result as a management message xml text as [targetName, string]
     *         map
     * @throws ManagementRemoteException
     *             on error
     * 
     */
//    public Map<String /* targetName */, String /* targetResult */> stopServiceAssembly(
//            String serviceAssemblyName, String[] targetNames)
//            throws ManagementRemoteException;
    
    //  ///////////////////////////////////////////
    //  End of Cumulative Operation Definitions
    //  ///////////////////////////////////////////
    
    
    //  ///////////////////////////////////////////
    //  Start of Extra APIs
    //  ///////////////////////////////////////////   
    
     public void clearServiceAssemblyStatusCache();
         
     public void clearJBIComponentStatusCache(JBIComponentType compType);
     
     public List<JBIComponentInfo> listJBIComponents(JBIComponentType compType,
            String target) throws ManagementRemoteException;
     
     public JBIComponentInfo getJBIComponent(JBIComponentType compType, 
            String compName, String target) throws ManagementRemoteException;
          
     public List<String> getServiceAssemblyNames(
            String componentName, String targetName)
            throws ManagementRemoteException;
    /**
     * Gets the Info for a Service Assembly.
     *
     * @param serviceAssemblyName   name of a service assembly 
     * @param targetName    name of the target for this operation
     * @return the Service Assembly info
     * @throws ManagementRemoteException
     *             on error
     */
    public ServiceAssemblyInfo getServiceAssembly(String saName,
            String targetName) throws ManagementRemoteException;
    
    public ServiceUnitInfo getServiceUnit(String saName,
            String suName, String targetName) throws ManagementRemoteException;
    
    //  ///////////////////////////////////////////
    //  End of Extra APIs
    //  ///////////////////////////////////////////   
}