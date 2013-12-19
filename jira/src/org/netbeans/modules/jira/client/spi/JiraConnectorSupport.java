/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.client.spi;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider.JiraConnectorFactory;
import static org.netbeans.modules.jira.client.spi.JiraConnectorProvider.Type.REST;
import static org.netbeans.modules.jira.client.spi.JiraConnectorProvider.Type.XMLRPC;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public final class JiraConnectorSupport {
    
    private static JiraConnectorSupport instance;

    public final static Logger LOG = Logger.getLogger(JiraConnectorProvider.class.getName());
    private JiraConnectorProvider connector;
    private JiraConnectorProvider.Type connectorType;

    private JiraConnectorSupport() { }
    
    public static synchronized JiraConnectorSupport getInstance() {
        if(instance == null) {
            instance = new JiraConnectorSupport();
        }
        return instance;
    }

    public synchronized JiraConnectorProvider getConnector() {
        if (connector == null) {
            if(connectorType == null) {
                connectorType = JiraConfig.getInstance().getActiveConnector();
            }
            connector = forType(connectorType);
            if(connector == null) {
                Jira.LOG.log(Level.WARNING, "Connector {0} not available for JIRA", connectorType.getCnb());
                switch(connectorType) {
                    case REST:
                        connectorType = XMLRPC;
                        tryFallback();
                        break;
                    case XMLRPC:
                        connectorType = REST;
                        tryFallback();
                        break;
        }
            }
        }
        return connector;
    }

    private void tryFallback() {
        Jira.LOG.log(Level.WARNING, "Falling back on ", connectorType.getCnb());
        JiraConfig.getInstance().setActiveConnector(connectorType);
        connector = forType(connectorType);
    }
    
    private JiraConnectorProvider forType(JiraConnectorProvider.Type type) {
        Collection<? extends JiraConnectorFactory> connectors = Lookup.getDefault().lookupAll(JiraConnectorFactory.class);
        if(LOG.isLoggable(Level.FINE)) {
            for (JiraConnectorFactory p : connectors) {
                LOG.log(Level.FINE, "registered JIRA Connector : {0}", p.toString());
            }
        }
        
        for (JiraConnectorFactory f : connectors) {
            if(f.forType() == type) {
                // we made sure there will be only 1 conector for a session
                JiraConnectorProvider c = f.create(); 
                Jira.LOG.log(Level.INFO, "Selected JIRA connector is {0}", c != null ? f.forType() : "NULL");
                return c;
            }
        }
        return null; 
    }

    public static JiraConnectorProvider.Type getActiveConnector() {
        return JiraConfig.getInstance().getActiveConnector();
}
}
