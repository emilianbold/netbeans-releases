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
package org.netbeans.modules.maven.j2ee;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;

/**
 * a ui wrapper for server instances..
 * @author mkleint
 */
public class Wrapper {

    private String serverInstanceId;
    private String sessionServerInstanceId;

    public Wrapper(String serverInstanceId) {
        this.serverInstanceId = serverInstanceId;
    }

    public Wrapper(String serverInstanceId, String sessionServerInstanceId) {
        this(serverInstanceId);
        assert ExecutionChecker.DEV_NULL.equals(serverInstanceId);
        this.sessionServerInstanceId = sessionServerInstanceId;
    }

    public String getServerInstanceID() {
        return serverInstanceId;
    }

    public String getServerID() {
        if (ExecutionChecker.DEV_NULL.equals(serverInstanceId)) {
            return ExecutionChecker.DEV_NULL;
        }
        return MavenProjectSupport.obtainServerID(serverInstanceId);
    }

    public String getSessionServerInstanceId() {
        return sessionServerInstanceId;
    }
    
    @Override
    public String toString() {
        if (ExecutionChecker.DEV_NULL.equals(serverInstanceId)) {
            if (sessionServerInstanceId != null) {
                ServerInstance si = Deployment.getDefault().getServerInstance(sessionServerInstanceId);
                String dn;
                try {
                    dn = si.getDisplayName();
                } catch (InstanceRemovedException ex) {
                    return org.openide.util.NbBundle.getMessage(Wrapper.class, "MSG_Invalid_Server");
                }
                return org.openide.util.NbBundle.getMessage(Wrapper.class, "MSG_No_Permanent_Server", dn);
            } else {
                return org.openide.util.NbBundle.getMessage(Wrapper.class, "MSG_No_Server");
            }
        }
        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceId);
        if (si != null) {
            try {
                return si.getDisplayName();
            } catch (InstanceRemovedException ex) {
                Logger.getLogger(Wrapper.class.getName()).log(Level.FINE, "", ex);
            }
        }
        return serverInstanceId;
    }

    static Wrapper findWrapperByType(String serverId, JComboBox combo) {
        for (int i = 0; i < combo.getModel().getSize(); i++) {
            Wrapper wr = (Wrapper)combo.getModel().getElementAt(i);
            if (serverId.equals(wr.getServerID())) {
                return wr;
            }
        }
        return null;
    }

    static Wrapper findWrapperByInstance(String instanceId, JComboBox combo) {
        for (int i = 0; i < combo.getModel().getSize(); i++) {
            Wrapper wr = (Wrapper)combo.getModel().getElementAt(i);
            if (instanceId.equals(wr.getServerInstanceID())) {
                return wr;
            }
        }
        return null;
    }

    
    public static ComboBoxUpdater<Wrapper> createComboBoxUpdater(final ModelHandle2 handle, final JComboBox combo, JLabel label) {
        return  new ComboBoxUpdater<Wrapper>(combo, label) {
            
            private Wrapper modified;
            private ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

            @Override
                public void performOperation(POMModel model) {
                    String sID = modified.getServerID();
                    String iID = modified.getServerInstanceID();
                    
                    if (ExecutionChecker.DEV_NULL.equals(iID)) {
                        Properties props = model.getProject().getProperties();
                        if (props != null) {
                            props.setProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, null);
                        }
                } else {
                    Properties props = model.getProject().getProperties();
                    if (props == null) {
                        props = model.getFactory().createProperties();
                        model.getProject().setProperties(props);
                    }
                    props.setProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, sID);
                }
                }
            };
            
            @Override
            public Wrapper getDefaultValue() {
                return null;
            }

            @Override
            public Wrapper getValue() {
                Wrapper wr = modified;
                if (wr == null) {
                String id = handle.getRawAuxiliaryProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, false);
                if (id != null) {
                    wr = findWrapperByInstance(id, combo);
                }
                }
                if (wr == null) {
                    POMModel model = handle.getPOMModel();
                    Properties props = model.getProject().getProperties();
                    String str = null;
                    if (props != null) {
                        str = props.getProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER);
                    }
                    if (str == null) {
                        str = handle.getRawAuxiliaryProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_OLD, true);
                    }
                    if (str != null) {
                        wr = findWrapperByType(str, combo);
                    }
                }
                return wr;
            }

            @Override
            public void setValue(Wrapper wr) {
                if (wr == null) {
                    return;
                }
                modified = wr;
                handle.removePOMModification(operation);
                handle.addPOMModification(operation);
                String iID = wr.getServerInstanceID();
                //remove old deprecated data.
                handle.setRawAuxiliaryProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_OLD, null, true);
                if (ExecutionChecker.DEV_NULL.equals(iID)) {
                    handle.setRawAuxiliaryProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, null, false);
                } else {
                    handle.setRawAuxiliaryProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, iID, false);
                }
            }
        };
    }

}
