/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject;

import org.netbeans.modules.web.clientproject.spi.ClientProjectConfiguration;
import org.netbeans.modules.web.clientproject.spi.ProjectConfigurationCustomizer;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Becicka
 */
public class ClientSideProjectActionProvider implements ActionProvider {

    private ClientSideProject p;

    public ClientSideProjectActionProvider(ClientSideProject p) {
        this.p = p;
    }
    
    @Override
    public String[] getSupportedActions() {
        return new String[]{
                    COMMAND_RUN_SINGLE,
                    COMMAND_BUILD,
                    COMMAND_CLEAN,
                    COMMAND_RUN
                };
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        ClientSideConfigurationProvider provider = p.getLookup().lookup(ClientSideConfigurationProvider.class);
        final ClientProjectConfiguration activeConfiguration = provider.getActiveConfiguration();
        //TODO: hack for default
        String type = activeConfiguration == null ? "browser" : activeConfiguration.getType();

        Lookup providers = LookupProviderSupport.createCompositeLookup(Lookups.fixed(p), "Projects/" + ProjectConfigurationCustomizer.PATH + "/"+ type + "/Lookup");
        ActionProvider action = providers.lookup(ActionProvider.class);
        if (action != null) {
            action.invokeAction(command, context);
            return;
        }
        NotifyDescriptor desc = new NotifyDescriptor("Action not supported for this configuration",
                "Action not supported",
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE,
                new Object[]{NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(desc);
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return true;
    }
}
