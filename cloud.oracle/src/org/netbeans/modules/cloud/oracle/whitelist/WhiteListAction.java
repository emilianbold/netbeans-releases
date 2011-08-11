/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.oracle.whitelist;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleJ2eePlatformImpl2;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.cloud.WhiteListTool;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * XXXX This is just a temporary helper action to sort out Whitelisting issues.
 */
public class WhiteListAction extends AbstractAction {

    private Project project;
    private J2eePlatform j2eePlatform;
    
    public WhiteListAction(Project project, J2eePlatform j2eePlatform) {
        putValue(Action.NAME, "Whitelist Test");
        setEnabled(true);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        this.project = project;
        this.j2eePlatform = j2eePlatform;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //project.getLookup().lookup(ActionProvider.class).invokeAction(ActionProvider.COMMAND_REBUILD, Lookup.EMPTY);
        // will not work with Maven but hopefully this action is just temporary solution:
        final FileObject fo[] = project.getLookup().lookup(AntArtifactProvider.class).getBuildArtifacts()[0].getArtifactFiles();
        if (fo == null || fo.length == 0) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("[temporary] Could you build your WAR first please?? Thanks."));
            return;
        }
        OracleJ2eePlatformImpl2.WeblogicJar wj = j2eePlatform.getLookup().lookup(OracleJ2eePlatformImpl2.WeblogicJar.class);
        final File weblogic = (wj != null ? wj.getWeglobicJar() : WLPluginProperties.getWeblogicJar(j2eePlatform.getServerHome()));
        if (weblogic == null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Whitelist tool cannot be run without Weblogic 10.3.6 registered in the IDE."));
            return;
        }
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                WhiteListTool.execute(FileUtil.toFile(fo[0]), weblogic);
            }
        });
    }
    
    

    public static Action actionFactory() {
        return new Factory();
    }
    
    private static final class Factory extends AbstractAction implements ContextAwareAction {

        private Factory() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        public @Override Action createContextAwareInstance(Lookup actionContext) {
            Collection<? extends Project> p = actionContext.lookupAll(Project.class);
            if (p.size() != 1) {
                return this;
            }
            Project project = p.iterator().next();
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider == null) {
                return this;
            }
            J2eePlatform j2eePlatformLocal = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
            if (j2eePlatformLocal == null || j2eePlatformLocal.getLookup().lookup(WhiteListQueryImplementation.class) == null) {
                return this;
            }
            return new WhiteListAction(project, j2eePlatformLocal);
        }

    }

}
