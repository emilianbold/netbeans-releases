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
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.openide.util.Exceptions;

/**
 * a ui wrapper for server instances..
 * @author mkleint
 */
public class Wrapper {

    private String id;
    private String sessionServerId;

    public Wrapper(String serverid) {
        id = serverid;
    }

    public Wrapper(String serverid, String sessionServerId) {
        this(serverid);
        assert ExecutionChecker.DEV_NULL.equals(serverid);
        this.sessionServerId = sessionServerId;
    }

    public String getServerInstanceID() {
        return id;
    }

    public String getServerID() {
        if (ExecutionChecker.DEV_NULL.equals(id)) {
            return ExecutionChecker.DEV_NULL;
        }
        return POHImpl.privateGetServerId(id);
    }

    @Override
    public String toString() {
        if (ExecutionChecker.DEV_NULL.equals(id)) {
            if (sessionServerId != null) {
                ServerInstance si = Deployment.getDefault().getServerInstance(sessionServerId);
                String dn = sessionServerId;
                try {
                    dn = si.getDisplayName();
                } catch (InstanceRemovedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return org.openide.util.NbBundle.getMessage(Wrapper.class, "MSG_No_Permanent_Server", dn);
            } else {
                return org.openide.util.NbBundle.getMessage(Wrapper.class, "MSG_No_Server");
            }
        }
        ServerInstance si = Deployment.getDefault().getServerInstance(id);
        if (si != null) {
            try {
                return si.getDisplayName();
            } catch (InstanceRemovedException ex) {
                Logger.getLogger(Wrapper.class.getName()).log(Level.FINE, "", ex);
            }
        }
        return id;
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

    
    public static ComboBoxUpdater<Wrapper> createComboBoxUpdater(final ModelHandle handle, final JComboBox combo, JLabel label) {
        return  new ComboBoxUpdater<Wrapper>(combo, label) {
            public Wrapper getDefaultValue() {
                Wrapper wr = null;
                String id = handle.getProject().getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER_ID);
                if (id != null) {
                    wr = Wrapper.findWrapperByInstance(id, combo);
                }
                if (wr == null) {
                    String str = handle.getProject().getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER);
                    if (str == null) {
                        str = handle.getProject().getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER_OLD);
                    }
                    if (str != null) {
                        wr = findWrapperByType(str, combo);
                    }
                }
                return wr;
            }

            public Wrapper getValue() {
                Wrapper wr = null;
                org.netbeans.modules.maven.model.profile.Profile privprof = handle.getNetbeansPrivateProfile(false);
                if (privprof != null) {
                    org.netbeans.modules.maven.model.profile.Properties privprops = privprof.getProperties();
                    if (privprops != null) {
                        String id = privprops.getProperty(Constants.HINT_DEPLOY_J2EE_SERVER_ID);
                        if (id != null) {
                            wr = findWrapperByInstance(id, combo);
                        }
                    }
                }
                if (wr == null) {
                    POMModel model = handle.getPOMModel();
                    Properties props = model.getProject().getProperties();
                    String str = null;
                    if (props != null) {
                        str = props.getProperty(Constants.HINT_DEPLOY_J2EE_SERVER);
                    }
                    if (str == null) {
                        org.netbeans.modules.maven.model.pom.Profile prof = handle.getNetbeansPublicProfile(false);
                        if (prof != null) {
                            props = prof.getProperties();
                            if (props != null) {
                                str = props.getProperty(Constants.HINT_DEPLOY_J2EE_SERVER_OLD);
                            }
                        }
                    }
                    if (str != null) {
                        wr = findWrapperByType(str, combo);
                    }
                }
                return wr;
            }

            public void setValue(Wrapper wr) {
                if (wr == null) {
                    return;
                }
                String sID = wr.getServerID();
                String iID = wr.getServerInstanceID();
                org.netbeans.modules.maven.model.profile.Profile privateProf = handle.getNetbeansPrivateProfile(false);
                //remove old deprecated data.
                org.netbeans.modules.maven.model.pom.Profile pub = handle.getNetbeansPublicProfile(false);
                if (pub != null) {
                    Properties props = pub.getProperties();
                    if (props != null) {
                        pub.getProperties().setProperty(Constants.HINT_DEPLOY_J2EE_SERVER_OLD, null);
                    }
                }
                POMModel model = handle.getPOMModel();
                if (ExecutionChecker.DEV_NULL.equals(iID)) {
                    //check if someone moved the property to netbeans-private profile, remove from there then.
                    Properties props = model.getProject().getProperties();
                    if (privateProf != null) {
                        org.netbeans.modules.maven.model.profile.Properties privprops = privateProf.getProperties();
                        if (privprops != null && privprops.getProperty(Constants.HINT_DEPLOY_J2EE_SERVER) != null) {
                            privprops.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, null);
                        } else {
                            if (props != null) {
                                props.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, null);
                                handle.markAsModified(handle.getPOMModel());
                            }
                        }
                        if (privprops != null) {
                            privprops.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER_ID, null);
                        }
                        handle.markAsModified(handle.getProfileModel());
                    } else {
                        if (props != null) {
                            props.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, null);
                            handle.markAsModified(handle.getPOMModel());
                        }
                    }
                } else {

                    //check if someone moved the property to netbeans-private profile, set there.
                    if (privateProf != null && privateProf.getProperties() != null &&
                            privateProf.getProperties().getProperty(Constants.HINT_DEPLOY_J2EE_SERVER) != null) {
                        privateProf.getProperties().setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, sID);
                    } else {
                        Properties props = model.getProject().getProperties();
                        if (props == null) {
                            props = model.getFactory().createProperties();
                            model.getProject().setProperties(props);
                        }
                        props.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, sID);
                        handle.markAsModified(handle.getPOMModel());
                    }
                    privateProf = handle.getNetbeansPrivateProfile();
                    org.netbeans.modules.maven.model.profile.Properties privs = privateProf.getProperties();
                    if (privs == null) {
                        privs = handle.getProfileModel().getFactory().createProperties();
                        privateProf.setProperties(privs);
                    }
                    privs.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER_ID, iID);
                    handle.markAsModified(handle.getProfileModel());
                }
            }
        };
    }

}
