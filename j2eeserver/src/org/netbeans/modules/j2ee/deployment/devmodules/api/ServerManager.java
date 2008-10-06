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
package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.awt.EventQueue;
import java.util.Collection;
import org.netbeans.api.server.CommonServerUIs;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.bridge.BridgingServerInstanceProvider;
import org.netbeans.modules.j2ee.deployment.impl.bridge.ServerInstanceProviderLookup;
import org.netbeans.modules.j2ee.deployment.impl.ui.wizard.AddServerInstanceWizard;

/**
 * ServerManager class provides access to the Server Manager dialog.
 *
 * @author sherold
 * @since  1.7
 */
public final class ServerManager {

    /** Do not allow to create instances of this class */
    private ServerManager() {
    }
    
    /**
     * Display the modal Server Manager dialog with the specified server instance 
     * preselected. This method should be called form the AWT event dispatch 
     * thread.
     *
     * @param serverInstanceID server instance which should be preselected, if 
     *        null the first server instance will be preselected.
     * 
     * @throws IllegalThreadStateException if the method is not called from the 
     *         event dispatch thread.
     * @deprecated use {@link org.netbeans.api.server.CommonServerUIs#showCustomizer} instead
     */
    public static void showCustomizer(String serverInstanceID) {
        // bridge to new infrastructure (common server)
        ServerInstance bridgingInstance = null;
        org.netbeans.modules.j2ee.deployment.impl.ServerInstance j2eeInstance =
                ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        if (j2eeInstance != null) {
            Collection<? extends org.netbeans.spi.server.ServerInstanceProvider> providers = ServerInstanceProviderLookup.getInstance().lookupAll(org.netbeans.spi.server.ServerInstanceProvider.class);
            for (org.netbeans.spi.server.ServerInstanceProvider provider : providers) {
                if (provider instanceof BridgingServerInstanceProvider) {
                    bridgingInstance = ((BridgingServerInstanceProvider) provider).getBridge(j2eeInstance);
                    if (bridgingInstance != null) {
                        break;
                    }
                }
            }
        }
        CommonServerUIs.showCustomizer(bridgingInstance);
    }
    
    /**
     * Displays the add server instance wizarad and returns the ID of the added
     * server instance. It is intended for J2EE related code only.
     * 
     * @return server instance ID of the new server instance, or <code>null</code>
     *         if the wizard was cancelled.
     * 
     * @throws IllegalThreadStateException if the method is not called from the 
     *         event dispatch thread.
     * 
     * @since  1.28
     */
    public static String showAddServerInstanceWizard() {
        checkDispatchThread();
        return AddServerInstanceWizard.showAddServerInstanceWizard();
    }
    
    private static void checkDispatchThread() {
	if (!EventQueue.isDispatchThread()) {
	    throw new IllegalThreadStateException("Can only be called from the event dispatch thread."); // NOI18N
	}
    }
}
