/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.sun.manager.jbi.management;

import com.sun.esb.management.api.performance.PerformanceMeasurementService;
import com.sun.esb.management.api.runtime.RuntimeManagementService;
import com.sun.esb.management.client.ManagementClient;
import com.sun.esb.management.common.ManagementRemoteException;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.impl.PerformanceMeasurementServiceWrapperImpl;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.impl.RuntimeManagementServiceWrapperImpl;

/**
 *
 * @author jqian
 */
public class ServiceFactory {

    public static RuntimeManagementServiceWrapper getRuntimeManagementServiceWrapper(
            ManagementClient mgmtClient) throws ManagementRemoteException {
        RuntimeManagementService service = 
                mgmtClient.getRuntimeManagementService();
        return new RuntimeManagementServiceWrapperImpl(service);
    }
    
    public static PerformanceMeasurementServiceWrapper gePerformanceMeasurementServiceWrapper(
            ManagementClient mgmtClient) throws ManagementRemoteException {
        PerformanceMeasurementService service = 
                mgmtClient.getPerformanceMeasurementService();
        return new PerformanceMeasurementServiceWrapperImpl(service);
    }
}
