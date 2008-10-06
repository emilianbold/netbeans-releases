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

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.Refreshable;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the Web Applications node. Implements
 * Refreshable interface and due to it can be refreshed via ResreshModulesAction.
 *
 * @author Michal Mocnak
 */
public class JBWebApplicationsChildren extends Children.Keys implements Refreshable {

    private static final Logger LOGGER = Logger.getLogger(JBWebApplicationsChildren.class.getName());

    private static final Set<String> SYSTEM_WEB_APPLICATIONS = new HashSet<String>();
    static {
        Collections.addAll(SYSTEM_WEB_APPLICATIONS,
                "jbossws-context", "jmx-console", "jbossws", "jbossws",
                "web-console", "invoker", "jbossmq-httpil");
    }

    private final JBAbilitiesSupport abilitiesSupport;

    private final Lookup lookup;

    public JBWebApplicationsChildren(Lookup lookup) {
        this.lookup = lookup;
        this.abilitiesSupport = new JBAbilitiesSupport(lookup);
    }

    public void updateKeys(){
        setKeys(new Object[] {Util.WAIT_NODE});

        RequestProcessor.getDefault().post(new Runnable() {
            Vector keys = new Vector();

            public void run() {
                ClassLoader orig = Thread.currentThread().getContextClassLoader();
                try {
                    // Query to the jboss server
                    ObjectName searchPattern;
                    if (abilitiesSupport.isRemoteManagementSupported() && abilitiesSupport.isJB4x()) {
                        searchPattern = new ObjectName("jboss.management.local:j2eeType=WebModule,J2EEApplication=null,*"); // NOI18N
                    }
                    else {
                        searchPattern = new ObjectName("jboss.web:j2eeType=WebModule,J2EEApplication=none,*"); // NOI18N
                    }

                    Object server = Util.getRMIServer(lookup);
                    Thread.currentThread().setContextClassLoader(server.getClass().getClassLoader());

                    Set managedObj = (Set) server.getClass().getMethod("queryMBeans", new Class[]  {ObjectName.class, QueryExp.class}).invoke(server, new Object[]  {searchPattern, null});

                    JBDeploymentManager dm = (JBDeploymentManager) lookup.lookup(JBDeploymentManager.class);

                    // Query results processing
                    for (Iterator it = managedObj.iterator(); it.hasNext();) {
                        try {
                            ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                            String name = elem.getKeyProperty("name");
                            String url = "http://" + dm.getHost() + ":" + dm.getPort();
                            String context = null;

                            if (name.endsWith(".war")) {
                                name = name.substring(0, name.lastIndexOf(".war"));
                            }

                            if (abilitiesSupport.isRemoteManagementSupported() && abilitiesSupport.isJB4x()) {
                                if (SYSTEM_WEB_APPLICATIONS.contains(name)) { // Excluding it. It's system package
                                    continue;
                                }
                                String descr = (String) Util.getMBeanParameter(dm, "jbossWebDeploymentDescriptor", elem.getCanonicalName()); // NOI18N
                                context = Util.getWebContextRoot(descr, name);
                            } else {
                                if (name.startsWith("//localhost/")) { // NOI18N
                                    name = name.substring("//localhost/".length()); // NOI18N
                                }
                                if ("".equals(name)) {
                                    name = "ROOT"; // NOI18N // consistent with JBoss4
                                }
                                if (SYSTEM_WEB_APPLICATIONS.contains(name)) { // Excluding it. It's system package
                                    continue;
                                }

                                context = (String) Util.getMBeanParameter(dm, "path", elem.getCanonicalName()); // NOI18N
                            }

                            name += ".war"; // NOI18N
                            keys.add(new JBWebModuleNode(name, lookup, (context == null ? null : url + context)));
                        } catch (Exception ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, null, ex);
                } finally {
                    Thread.currentThread().setContextClassLoader(orig);
                }

                setKeys(keys);
            }
        }, 0);
    }

    protected void addNotify() {
        updateKeys();
    }

    protected void removeNotify() {
        setKeys(java.util.Collections.EMPTY_SET);
    }

    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof JBWebModuleNode){
            return new Node[]{(JBWebModuleNode)key};
        }

        if (key instanceof String && key.equals(Util.WAIT_NODE)){
            return new Node[]{Util.createWaitNode()};
        }

        return null;
    }

}
