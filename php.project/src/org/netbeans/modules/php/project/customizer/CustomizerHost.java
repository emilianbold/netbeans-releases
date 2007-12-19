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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.customizer;

import javax.swing.JPanel;
import org.netbeans.modules.php.project.Utils;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.ProjectCustomizerComponent;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 *
 * @author avk
 */
public class CustomizerHost extends CustomizerHostVisual{
    
    CustomizerHost(PhpProjectProperties properties) {
        
        myProjectProperties = properties;
        read();
        store();
    }

    @Override
    protected void initHostsPanel() {
        super.initHostsPanel();
        read();
    }

    private void read() {
        if (getProjectProperties() != null){
            loadProjectProps();
        }

    }

    @Override
    protected void update() {
        super.update();
        store();
    }


    protected void store(){
            Host host = getSelectedHost();
            getProjectProperties().remove(PhpProjectProperties.STATUS_USE_NO_HOST);
            getProjectProperties().remove(PhpProjectProperties.STATUS_ABSENT_HOST);
            if (host == null || host instanceof NoHost){
                getProjectProperties().setProperty(PhpProjectProperties.STATUS_USE_NO_HOST, "");
            } 
            else if (host instanceof AbsentHost){
                //getProjectProperties().setProperty(PhpProjectProperties.STATUS_ABSENT_HOST, "");
                getProjectProperties().setProperty(WebServerProvider.HOST_ID, host.getId());
            } 
            else {
                getProjectProperties().setProperty(WebServerProvider.HOST_ID, host.getId());
            }
    }
    
    private void loadProjectProps() {
        String hostId = getProjectProperties().getProperty(WebServerProvider.HOST_ID);
        WebServerProvider provider = Utils.getProvider(getProjectProperties().getProject());
        Host host = null;
        if (hostId != null && provider != null) {
            
            host = provider.findHost(hostId);
            if (host == null){
                host = new AbsentHost(hostId);
            }
            setSelectedHost(host);
        } else {
            setSelectedHost(getNoHost());
        }
    }

    protected void loadProviderProperties() {
        Host host = getSelectedHost();
        if (    host == null 
                || host instanceof NoHost
                || host instanceof AbsentHost) 
        {
            return;
        } 
        EditableProperties props = getProjectProperties().getProperties();
        props.setProperty(WebServerProvider.HOST_ID, host.getId());
        ProjectCustomizerComponent customizer = getProviderComoponent();
        customizer.read(props);
    }

    protected JPanel loadProviderPanel(Host host){
        myProviderComponent = loadProviderComponent(host);
        if (myProviderComponent != null){
            return myProviderComponent.getPanel();
        }
        return null;
    }
    

    private ProjectCustomizerComponent loadProviderComponent(Host host){
        if (host == null){
            return null;
        }
        if (host instanceof NoHost){
            return null;
        }
        if (host instanceof AbsentHost){
            return null;
        }
        
        ProjectConfigProvider configProvider = host.getProvider().getProjectConfigProvider();
        
        myProviderComponent = configProvider.getCustomizerPanel(
                getProjectProperties().getProperties());
        return myProviderComponent;
    }
    
    
    private PhpProjectProperties getProjectProperties() {
        return myProjectProperties;
    }

    private ProjectCustomizerComponent getProviderComoponent() {
        return myProviderComponent;
    }

    private ProjectCustomizerComponent myProviderComponent;
    
    private PhpProjectProperties myProjectProperties;

}
