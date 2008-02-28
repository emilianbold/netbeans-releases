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

package org.netbeans.modules.mobility.project.deployment;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Utilities;

/**
 *
 * @author suchys
 */
public class DeploymentPropertiesHandler {
    public static void loadDeploymentProperties(final Collection<Lookup.Result> result){
        try {
            ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Object>() {

                        public Object run() throws Exception {
                            MobilityDeploymentProperties mp = new MobilityDeploymentProperties();
                            final EditableProperties props = PropertyUtils.getGlobalProperties();
                            final Iterator it = result.iterator();
                            while (it.hasNext()) {
                                final DeploymentPlugin plugin = (DeploymentPlugin) it.next();
                                final String name = plugin.getDeploymentMethodName();
                                final String loc = plugin.getAntScriptLocation();
                                if (loc != null) {
                                    File f = new File(loc);
                                    if (!f.isFile()) {
                                        f = InstalledFileLocator.getDefault().locate(loc, null, false);
                                    }
                                    if (name != null && f != null && Utilities.isJavaIdentifier(name)) {
                                        props.setProperty("deployment." + name + ".scriptfile", f.getAbsolutePath()); //NOI18N
                                    }
                                }
                                if (!mp.getInstanceList(name).contains("default")) {
                                    mp.createInstance(name, "default"); //NOI18N
                                }
                            }
                            PropertyUtils.putGlobalProperties(props);
                            return null;
                        }
                    });
        } catch (MutexException me) {
            ErrorManager.getDefault().notify(me.getException());
        }
    }
}
