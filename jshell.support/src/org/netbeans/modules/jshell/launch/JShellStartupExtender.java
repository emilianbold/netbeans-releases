/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.launch;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.startup.StartupExtender.StartMode;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.launch.ShellLaunchManager.ShellAgent;
import org.netbeans.modules.jshell.project.LaunchedProjectOpener;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
@StartupExtenderImplementation.Registration(displayName = "JShell", startMode = StartMode.DEBUG)
public class JShellStartupExtender implements StartupExtenderImplementation {
    private static final Logger LOG = Logger.getLogger(JShellStartupExtender.class.getName());
    
    @Override
    public List<String> getArguments(Lookup context, StartMode mode) {
        LaunchedProjectOpener.init();
        
        if (mode != StartMode.DEBUG &&
            mode != StartMode.TEST_DEBUG) {
            return Collections.emptyList();
        }
        Project p = context.lookup(Project.class);
        if (p == null) {
            return Collections.emptyList();
        }
        
        LOG.log(Level.FINE, "Augmenting {0} of project {1}", new Object[] { mode, p });
        
        InetSocketAddress isa;
        ShellAgent agent;
        try {
            agent = ShellLaunchManager.getInstance().openForProject(p, true);
            if (agent == null) {
                return Collections.emptyList();
            }
            isa = agent.getHandshakeAddress();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Could not obtain handshake address and key: ", ex);
            return Collections.emptyList();
        }
        LOG.log(Level.FINE, "Connect address is: {0}:{1}", new Object[] { isa.getHostString(), isa.getPort() });
        
        File agentJar = InstalledFileLocator.getDefault().locate("modules/ext/nb-custom-jshell-probe.jar", "org.netbeans.modules.jshell.support", false);
        File owasm = InstalledFileLocator.getDefault().locate("core/asm-all-5.0.1.jar", "org.netbeans.core", false);
        
        String arg = String.format(
                "-javaagent:%1$s=address=%2$s,port=%3$d,libraries=%4$s,key=%5$s", 
                agentJar.toPath().toString(),
                isa.getHostString(),
                isa.getPort(),
                owasm.toPath().toString(),
                agent.getAuthorizationKey()
                
        );
        LOG.log(Level.FINE, "Final arg: {0}", arg);

        List<String> args = new ArrayList<>();
        if (LOG.isLoggable(Level.FINE)) {
            args.add("-Dorg.netbeans.lib.jshell.agent.level=400");
        }
        args.add(arg);
        return args;
    }
}
