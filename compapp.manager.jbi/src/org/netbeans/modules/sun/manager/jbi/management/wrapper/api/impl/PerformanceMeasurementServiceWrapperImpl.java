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

import com.sun.esb.management.api.performance.PerformanceMeasurementService;
import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.esb.management.common.data.ComponentStatisticsData;
import com.sun.esb.management.common.data.FrameworkStatisticsData;
import com.sun.esb.management.common.data.IEndpointStatisticsData;
import com.sun.esb.management.common.data.NMRStatisticsData;
import com.sun.esb.management.common.data.ServiceAssemblyStatisticsData;
import com.sun.esb.management.common.data.helper.ComponentStatisticsDataReader;
import com.sun.esb.management.common.data.helper.EndpointStatisticsDataReader;
import com.sun.esb.management.common.data.helper.FrameworkStatisticsDataReader;
import com.sun.esb.management.common.data.helper.NMRStatisticsDataReader;
import com.sun.esb.management.common.data.helper.ServiceAssemblyStatisticsDataReader;
import java.util.Map;
import javax.management.openmbean.TabularData;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;

/**
 *
 * @author jqian
 */
public class PerformanceMeasurementServiceWrapperImpl
        implements PerformanceMeasurementServiceWrapper {

    private PerformanceMeasurementService service;
    
    public PerformanceMeasurementServiceWrapperImpl(
            PerformanceMeasurementService service) {
        this.service = service;
    }
    
    public void clearPeformaceInstrumentationMeasurement(
            String componentName, String endpoint, String targetName)
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getPerformanceInstrumentationEnabled(
            String componentName, String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPerformanceInstrumentationMeasurement(
            String componentName, String endpoint, String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getPerformanceMeasurementCategories(
            String componentName, String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPerformanceInstrumentationEnabled(
            String componentName, boolean flag, String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TabularData getFrameworkStatisticsAsTabularData(String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FrameworkStatisticsData getFrameworkStatistics(
            String targetName) 
            throws ManagementRemoteException {
        String statistics = service.getFrameworkStatistics(targetName);
        try {
            Map<String, FrameworkStatisticsData> statisticsMap =
                    FrameworkStatisticsDataReader.parseFromXMLData(statistics);
            return statisticsMap.get(targetName);
        } catch (Exception e) {
            throw new ManagementRemoteException(e);
        }
    }

    public ComponentStatisticsData getComponentStatistics(
            String componentName, String targetName) 
            throws ManagementRemoteException {
        String statistics = service.getComponentStatistics(
                componentName, targetName);
        try {
            Map<String, ComponentStatisticsData> statisticsMap =
                    ComponentStatisticsDataReader.parseFromXMLData(statistics);
            return statisticsMap.get(targetName);
        } catch (Exception e) {
            throw new ManagementRemoteException(e);
        }
    }

    public TabularData getComponentStatisticsAsTabularData(
            String componentName, String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TabularData getEndpointStatisticsAsTabularData(
            String endpointName, String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IEndpointStatisticsData getEndpointStatistics(
            String endpointName, String targetName) 
            throws ManagementRemoteException {
        String statistics = service.getEndpointStatistics(endpointName, targetName);
        try {
            Map<String, IEndpointStatisticsData> statisticsMap =
                    EndpointStatisticsDataReader.parseFromXMLData(statistics);
            return statisticsMap.get(targetName);
        } catch (Exception e) {
            throw new ManagementRemoteException(e);
        }
    }

    public TabularData getNMRStatisticsAsTabularData(String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NMRStatisticsData getNMRStatistics(String targetName) 
            throws ManagementRemoteException {
        String statistics = service.getNMRStatistics(targetName);
        try {
            Map<String, NMRStatisticsData> statisticsMap =
                    NMRStatisticsDataReader.parseFromXMLData(statistics);
            return statisticsMap.get(targetName);
        } catch (Exception e) {
            throw new ManagementRemoteException(e);
        }
    }

    public TabularData getServiceAssemblyStatisticsAsTabularData(
            String assemblyName, String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServiceAssemblyStatisticsData getServiceAssemblyStatistics(
            String assemblyName, String targetName) 
            throws ManagementRemoteException {
        String statistics = service.getServiceAssemblyStatistics(
                assemblyName, targetName);
        try {
            Map<String, ServiceAssemblyStatisticsData> statisticsMap =
                    ServiceAssemblyStatisticsDataReader.parseFromXMLData(statistics);
            return statisticsMap.get(targetName);
        } catch (Exception e) {
            throw new ManagementRemoteException(e);
        }
    }

    public void enableMessageExchangeMonitoring(String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void disableMessageExchangeMonitoring(String targetName) 
            throws ManagementRemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
