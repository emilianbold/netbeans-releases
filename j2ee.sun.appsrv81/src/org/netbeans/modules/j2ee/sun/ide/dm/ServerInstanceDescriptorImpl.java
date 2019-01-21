/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2ee.sun.ide.dm;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;

/**
 *
 * 
 */
public class ServerInstanceDescriptorImpl implements ServerInstanceDescriptor {

    private final SunDeploymentManager sunDm;

    public ServerInstanceDescriptorImpl(SunDeploymentManager dm) {
        sunDm = dm;
    }

    // I thought about using DeploymentManagerProperties here, but it looks like
    // that would simply resolve to this code here, except with more overhead
    //
    public int getHttpPort() {
        int httpPort = 8080;
        String httpPortStr = sunDm.getNonAdminPortNumber();
        if(httpPortStr != null && httpPortStr.length() > 0) {
            try {
                httpPort = Integer.parseInt(sunDm.getNonAdminPortNumber());
            } catch(NumberFormatException ex) {
                Logger.getLogger(ServerInstanceDescriptorImpl.class.getName()).log(
                        Level.INFO, "HTTP port is malformed, using 8080 as default ("  +
                        ex.getLocalizedMessage() + ")", ex);
            }
        } else {
            Logger.getLogger(ServerInstanceDescriptorImpl.class.getName()).log(
                    Level.INFO, "HTTP port is null, using 8080 as default");
        }
        return httpPort;
    }

    public String getHostname() {
        return sunDm.getHost();
    }

    public boolean isLocal() {
        return sunDm.isLocal();
    }

}
