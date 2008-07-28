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

package org.netbeans.tests.j2eeserver.plugin.jsr88;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import javax.enterprise.deploy.shared.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author  gfink
 * @author nn136682
 */
public class ProgObject extends  ServerProgress {

    private TargetModuleID[] tmIDs;
    
    public ProgObject(DeploymentManager dm, Target[] targets, File archive, Object plan, ModuleType type) {
        this(dm, createTargetModuleIDs(targets, archive, type));
    }
    
    public ProgObject(DeploymentManager dm, Target[] targets, File archive, Object plan) {
        this(dm, createTargetModuleIDs(targets, archive, null));
    }

    public ProgObject(DeploymentManager dm, Target[] targets, Object archive, Object plan) {
        this(dm, new TargetModuleID[0]);
    }
    
    public ProgObject(DeploymentManager dm, TargetModuleID[] modules) {
        super(dm);
        tmIDs = modules;
    }
    
    public static TargetModuleID[] createTargetModuleIDs(Target[] targets, File archive, ModuleType type) {
        TargetModuleID [] ret = new TargetModuleID[targets.length];
        for (int i=0; i<ret.length; i++) {
            ret[i] = new TestTargetMoid(targets[i], archive.getName(), type != null ? type : getType(archive.getName()));
            ((Targ)targets[i]).add(ret[i]);
        }
        return ret;
    }
    static ModuleType getType(String name) {
        if (name.endsWith(".ear")) return ModuleType.EAR;
        else if (name.endsWith(".jar") || name.equals("jar")) return ModuleType.EJB; //PENDING: libraries and client
        else if (name.endsWith(".war") || name.equals("web")) return ModuleType.WAR;
        else if (name.endsWith(".rar")) return ModuleType.RAR;
        else throw new IllegalArgumentException("Invalid archive name: " + name);
    }
    public void setStatusDistributeRunning(String message) {
        notify(createRunningProgressEvent(CommandType.DISTRIBUTE, message));
    }
    public void setStatusDistributeFailed(String message) {
        notify(createFailedProgressEvent(CommandType.DISTRIBUTE, message));
    }
    public void setStatusDistributeCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.DISTRIBUTE, message)); 
    }

    public void setStatusRedeployRunning(String message) {
        notify(createRunningProgressEvent(CommandType.REDEPLOY, message));
    }
    public void setStatusRedeployFailed(String message) {
        notify(createFailedProgressEvent(CommandType.REDEPLOY, message));
    }
    public void setStatusRedeployCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.REDEPLOY, message)); 
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Test plugin does not support cancel!");
    }
    
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Test plugin does not support stop!");
    }
    
    public boolean isCancelSupported() {
        return false;  // PENDING parameterize?
    }
    
    public boolean isStopSupported() {
        return false; // PENDING see above
    }
    
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null; // PENDING client support
    }
    
    public DeploymentStatus getDeploymentStatus() {
        return super.getDeploymentStatus();
    }
    
    public TargetModuleID[] getResultTargetModuleIDs() {
        List ret = new Vector();
        for (int i=0; i<tmIDs.length; i++) {
            if (tmIDs[i].getChildTargetModuleID() != null)
                ret.addAll(Arrays.asList(tmIDs[i].getChildTargetModuleID()));
            ret.add(tmIDs[i]);
        }
        return (TargetModuleID[]) ret.toArray(new TargetModuleID[ret.size()]);
    }
}
