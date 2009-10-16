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

package org.netbeans.modules.sun.manager.jbi.management.wrapper.api;

import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.esb.management.common.data.ComponentStatisticsData;
import com.sun.esb.management.common.data.FrameworkStatisticsData;
import com.sun.esb.management.common.data.IEndpointStatisticsData;
import com.sun.esb.management.common.data.NMRStatisticsData;
import com.sun.esb.management.common.data.ServiceAssemblyStatisticsData;
import javax.management.openmbean.TabularData;


/**
 * Defines operations to measure performance statistics. e.g., time taken to
 * normalize/denormalize, encode/decode, wire-to-NMR on the endpoints, etc.
 * 
 * @author graj
 */
public interface PerformanceMeasurementServiceWrapper {
    /**
     * Resets the performance measurements on the endpoint.
     * @param componentName
     * @param endpoint
     * @param targetName
     * @throws ManagementRemoteException
     */
    public void clearPeformaceInstrumentationMeasurement(
            String componentName, String endpoint, String targetName) 
            throws ManagementRemoteException;
    

    /**
     * Retrieves the performance measurement enabling flag.
     * @param componentName
     * @param targetName
     * @return true if measurement enabled, false if not
     * @throws ManagementRemoteException
     */
    public boolean getPerformanceInstrumentationEnabled(
            String componentName, String targetName) 
            throws ManagementRemoteException;

    /**
     * Retrieves the performance measurement data for the specified endpoint.
     * @param componentName
     * @param endpoint
     * @param targetName
     * @return XML String representing PerformanceData Map
     * @throws ManagementRemoteException
     */
    public String getPerformanceInstrumentationMeasurement(
            String componentName, String endpoint, String targetName) 
            throws ManagementRemoteException;

    /**
     * Retrieves the performance statistics categories. Each item in the array is the key to the composite performance data, which also indicates the type (e.g. normalization) of measurement.
     * @param componentName
     * @param targetName
     * @return array of performance measurement categories
     * @throws ManagementRemoteException
     */
    public String[] getPerformanceMeasurementCategories(
            String componentName, String targetName) 
            throws ManagementRemoteException;

    /**
     * Sets the performance measurement enabling flag.
     * @param componentName
     * @param flag
     * @param targetName
     * @throws ManagementRemoteException
     */
    public void setPerformanceInstrumentationEnabled(
            String componentName, boolean flag, String targetName) 
            throws ManagementRemoteException;
    
    /**
     * This method is used to provide JBIFramework statistics in the
     * given target.
     * @param target target name.
     * @return TabularData table of framework statistics in the given target.
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     */
    public TabularData getFrameworkStatisticsAsTabularData(String targetName)
            throws ManagementRemoteException;
    
    /**
     * This method is used to provide JBIFramework statistics in the
     * given target.
     * @param target target name.
     * @return String table of framework statistics in the given target.
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     */
    public FrameworkStatisticsData getFrameworkStatistics(
            String targetName)
            throws ManagementRemoteException;
    
    /**
     * This method is used to provide statistics for the given component
     * in the given target
     * @param targetName target name
     * @param componentName component name
     * @return String table of component statistics
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     *
     */
    public ComponentStatisticsData getComponentStatistics(
            String componentName, String targetName)
            throws ManagementRemoteException;
    
    /**
     * This method is used to provide statistics for the given component
     * in the given target
     * @param targetName target name
     * @param componentName component name
     * @return TabularData table of component statistics
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     *
     */
    public TabularData getComponentStatisticsAsTabularData(
            String componentName, String targetName)
            throws ManagementRemoteException;
                
    /**
     * This method is used to provide statistic information about the given 
     * endpoint in the given target
     * @param targetName target name
     * @param endpointName the endpoint Name
     * @return TabularData table of endpoint statistics
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     */
    public TabularData getEndpointStatisticsAsTabularData(
            String endpointName, String targetName)
            throws ManagementRemoteException;

    /**
     * This method is used to provide statistic information about the given 
     * endpoint in the given target
     * @param targetName target name
     * @param endpointName the endpoint Name
     * @return String table of endpoint statistics
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     */
    public IEndpointStatisticsData getEndpointStatistics(
            String endpointName, String targetName)
            throws ManagementRemoteException;
    
    /**
     * This method is used to provide statistics about the message service in the
     * given target.
     * @param target target name.
     * @return TabularData table of NMR statistics in the given target.
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     */
    public TabularData getNMRStatisticsAsTabularData(String targetName)
            throws ManagementRemoteException;
    
    /**
     * This method is used to provide statistics about the message service in the
     * given target.
     * @param target target name.
     * @return String table of NMR statistics in the given target.
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     */
    public NMRStatisticsData getNMRStatistics(String targetName)
            throws ManagementRemoteException;
    
    /**
     * This method is used to provide statistics about a Service Assembly
     * in the given target.
     * @param target target name.
     * @param assemblyName the service assembly name.
     * @return TabularData table of NMR statistics in the given target.
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     */
    public TabularData getServiceAssemblyStatisticsAsTabularData(
            String assemblyName, String targetName)
            throws ManagementRemoteException;
    
    /**
     * This method is used to provide statistics about a Service Assembly
     * in the given target.
     * @param target target name.
     * @param assemblyName the service assembly name.
     * @return String table of NMR statistics in the given target.
     *
     * If the target is a standalone instance the table will have one entry.
     * If the target is a cluster the table will have an entry for each instance.
     *
     * For more information about the type of the entries in table please refer
     * to <code>JBIStatisticsMBean</code>
     */
    public ServiceAssemblyStatisticsData getServiceAssemblyStatistics(
            String assemblyName, String targetName)
            throws ManagementRemoteException;

    /**
     * This method is used to enable monitoring of timing 
     * information about message exchanges
     * @param targetName the target name
     */
    public void enableMessageExchangeMonitoring(String targetName)
            throws ManagementRemoteException;
    
    
    /**
     * This method is used to disable monitoring of timing 
     * information about message exchanges
     * @param targetName the target name
     */
    public void disableMessageExchangeMonitoring(String targetName)
            throws ManagementRemoteException;
}
