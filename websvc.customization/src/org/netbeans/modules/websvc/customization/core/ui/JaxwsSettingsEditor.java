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
package org.netbeans.modules.websvc.customization.core.ui;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.netbeans.modules.websvc.customization.jaxwssettings.panel.WsimportOptionsPanel;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author rico
 */
public class JaxwsSettingsEditor implements WSEditor {

    private Service service;
    private Client client;
    private WsimportOptionsPanel panel;

    public JComponent createWSEditorComponent(Node node, JaxWsModel jaxWsModel) {
        service = node.getLookup().lookup(Service.class);
        client = node.getLookup().lookup(Client.class);
        WsimportOptions wsimportOptions = null;
        if (service != null) {
            wsimportOptions = service.getWsImportOptions();
        } else if (client != null) {
            wsimportOptions = client.getWsImportOptions();
        }
        List<WsimportOption> options = new ArrayList<WsimportOption>();
        List<WsimportOption> jaxbOptions = new ArrayList<WsimportOption>();
        if (wsimportOptions != null) {
            WsimportOption[] wsoptions = wsimportOptions.getWsimportOptions();
            for (int i = 0; i < wsoptions.length; i++) {
                WsimportOption wsimportOption = wsoptions[i];
                if (wsimportOption.getJaxbOption() != null && wsimportOption.getJaxbOption()) {
                    jaxbOptions.add(wsimportOption);
                } else {
                    options.add(wsimportOption);
                }
            }
        }
        panel = new WsimportOptionsPanel(options, jaxbOptions, wsimportOptions);

        return panel;
    }

    public String getTitle() {
        return NbBundle.getMessage(JaxwsSettingsEditor.class, "JAXWS_SETTINGS_TITLE");
    }

    public void save(Node node, JaxWsModel jaxWsModel) {
        try {           
            WsimportOptions wsimportOptions = null;
            if (service != null) {
                wsimportOptions = service.getWsImportOptions();
                if (wsimportOptions == null) {
                    wsimportOptions = service.newWsimportOptions();
                }
            } else if (client != null) {
                wsimportOptions = client.getWsImportOptions();
                if (wsimportOptions == null) {
                    wsimportOptions = client.newWsimportOptions();
                }
            }

            wsimportOptions.clearWsimportOptions();

            List<WsimportOption> options = getWsimportOptions();
            for(WsimportOption option : options){
                wsimportOptions.addWsimportOption(option);
            }
            
            options = getJaxbOptions();
            for(WsimportOption option : options){
                option.setJaxbOption(true);
                wsimportOptions.addWsimportOption(option);
            }
   
            jaxWsModel.write();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    public void cancel(Node node, JaxWsModel jaxWsModel) {
    }

    public String getDescription() {
        return NbBundle.getMessage(JaxwsSettingsEditor.class, "JAXWS_SETTINGS_DESC");
    }

    private List<WsimportOption> getWsimportOptions() {
        return panel.getWsimportOptions();
    }

    private List<WsimportOption> getJaxbOptions() {
        return panel.getJaxbOptions();
    }
}
