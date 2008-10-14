// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
//</editor-fold>

package org.netbeans.modules.j2ee.sun.ide.dm;

import java.io.File;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.glassfish.eecommon.api.JDBCDriverDeployHelper;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.openide.util.RequestProcessor;

public class SunJDBCDriverDeployer implements JDBCDriverDeployer {

    private DeploymentManager dm;
    private SunDeploymentManager sunDm;
    
    public SunJDBCDriverDeployer(DeploymentManager dm) {
        this.dm = dm;
        this.sunDm = (SunDeploymentManager)this.dm;
    }

    public boolean supportsDeployJDBCDrivers(Target target) {
        boolean supported = true;
        if(! this.sunDm.isLocal()){
            supported = false;
        }
        DeploymentManagerProperties dmp = new DeploymentManagerProperties(this.dm);
        if(! dmp.isDriverDeploymentEnabled()){
            supported = false;
        }
        return supported;
    }

    public ProgressObject deployJDBCDrivers(Target target, Set<Datasource> datasources) {
        DeploymentManagerProperties dmp = new DeploymentManagerProperties(this.dm);
        File driverLoc = dmp.getDriverLocation();
        File installLib = new File (this.sunDm.getPlatformRoot().getAbsolutePath() + File.separator + "lib");
        File[] locs = {driverLoc, installLib};
        List urls = JDBCDriverDeployHelper.getMissingDrivers(locs, datasources);
        ProgressObject retVal = JDBCDriverDeployHelper.getProgressObject(driverLoc, urls);
        if (urls.size() > 0) {
            retVal.addProgressListener(new ProgressListener() {
                public void handleProgressEvent(ProgressEvent arg0) {
                    if (arg0.getDeploymentStatus().isCompleted()) {
                        sunDm.setRestartForDriverDeployment(true);
                    }
                }
            });
        }
        RequestProcessor.getDefault().post((Runnable) retVal, 200);
        return retVal; // new JDBCDriverDeployHelper.getProgressObject(dmp.getDriverLocation(), datasources);
    }

}
