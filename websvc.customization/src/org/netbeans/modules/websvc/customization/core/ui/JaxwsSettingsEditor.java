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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
        if(service != null){
            wsimportOptions = service.getWsImportOptions();
        }else if (client != null){
            wsimportOptions = client.getWsImportOptions();
        }
        Map<String, String> options = new HashMap<String, String>();
        if(wsimportOptions != null){
            WsimportOption[] wsoptions = wsimportOptions.getWsimportOptions();
            for(int i = 0; i < wsoptions.length; i++){
                WsimportOption wsimportOption = wsoptions[i];
                options.put(wsimportOption.getWsimportOptionName(), wsimportOption.getWsimportOptionValue());
            }
        }
        panel = new WsimportOptionsPanel(options);

        return panel;
    }

    public String getTitle() {
        return NbBundle.getMessage(JaxwsSettingsEditor.class, "JAXWS_SETTINGS_TITLE");
    }

    public void save(Node node, JaxWsModel jaxWsModel) {
        try {
            Map<String, String> options = getWsimportOptions();
            Set<String> keys = options.keySet();
            WsimportOptions wsimportOptions = null;
            if (service != null) {
                wsimportOptions = service.getWsImportOptions();
            } else if (client != null) {
                wsimportOptions = client.getWsImportOptions();
            }
            if (wsimportOptions != null) {
                wsimportOptions.clearWsimportOptions();
                for (String key : keys) {
                    WsimportOption wsimportOption = wsimportOptions.newWsimportOption();
                    wsimportOption.setWsimportOptionName(key);
                    wsimportOption.setWsimportOptionValue(options.get(key));
                    wsimportOptions.addWsimportOption(wsimportOption);
                }
                jaxWsModel.write();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    public void cancel(Node node, JaxWsModel jaxWsModel) {
    }

    public String getDescription() {
        return NbBundle.getMessage(JaxwsSettingsEditor.class, "JAXWS_SETTINGS_DESC");
    }

    private Map<String, String> getWsimportOptions() {
        return panel.getWsimportOptions();
    }
}
