// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
//</editor-fold>

package org.netbeans.modules.j2ee.sun.ide;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.glassfish.eecommon.api.ProgressEventSupport;

/**
 *
 * @author vkraemer
 */
public class ShortCircuitProgressObject implements ProgressObject {

    private CommandType ct;
    private String message;
    private StateType st;
    private TargetModuleID[] tmids;
    ProgressEventSupport pes = new ProgressEventSupport(this);

    /**
     *
     * @param ct
     * @param message
     * @param st
     * @param tmids
     */
    public ShortCircuitProgressObject(CommandType ct, String message, StateType st, TargetModuleID[] tmids) {
        this.ct = ct;
        this.message = message;
        this.st = st;
        this.tmids = tmids;
    }

    /**
     *
     * @return
     */
    public DeploymentStatus getDeploymentStatus() {
        return new DeploymentStatus() {

            public ActionType getAction() {
                return ActionType.EXECUTE;
            }

            public CommandType getCommand() {
                return ct;
            }

            public String getMessage() {
                return message;
            }

            public StateType getState() {
                return st;
            }

            public boolean isCompleted() {
                return st.equals(StateType.COMPLETED);
            }

            public boolean isFailed() {
                return st.equals(StateType.FAILED);
            }

            public boolean isRunning() {
                return st.equals(StateType.RUNNING);
            }
        };
    }

    /**
     *
     * @return
     */
    public TargetModuleID[] getResultTargetModuleIDs() {
        return tmids;
    }

    /**
     *
     * @param targetModuleID
     * @return
     */
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }

    /**
     *
     * @return
     */
    public boolean isCancelSupported() {
        return false;
    }

    /**
     *
     * @throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException
     */
    public void cancel() throws OperationUnsupportedException {
    }

    /**
     *
     * @return
     */
    public boolean isStopSupported() {
        return false;
    }

    /**
     *
     * @throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException
     */
    public void stop() throws OperationUnsupportedException {
    }

    /**
     *
     * @param progressListener
     */
    public void addProgressListener(ProgressListener progressListener) {
        pes.addProgressListener(progressListener);
    }

    /**
     *
     * @param progressListener
     */
    public void removeProgressListener(ProgressListener progressListener) {
        pes.removeProgressListener(progressListener);
    }
}
