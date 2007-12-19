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
package org.netbeans.modules.php.rt.providers.impl.ftp;

import java.util.Set;

import java.util.logging.Logger;
import org.netbeans.modules.php.rt.providers.impl.AbstractUiConfigProvider;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.ui.AddHostWizard;
import org.openide.WizardDescriptor.Panel;


/**
 * @author ads
 *
 */
class FtpUiConfigProvider extends AbstractUiConfigProvider{

    static final String HOST = AddHostWizard.HOST;
            //"ftp-host-impl";            // NOI18N
    
    private static Logger LOGGER = Logger.getLogger(FtpUiConfigProvider.class.getName());

    FtpUiConfigProvider( FtpServerProvider provider ) {
        myProvider =provider;
        initPanels();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.UiConfigProvider#getPanels()
     */
    public Panel[] getPanels() {
        return myPanels;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.UiConfigProvider#hasHelperPanels()
     */
    public boolean hasHelperPanels() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.UiConfigProvider#instantiate(org.netbeans.modules.php.rt.ui.AddHostWizard)
     */
    public Set instantiate( AddHostWizard wizard ) {
        FtpHostImpl impl = createHost(wizard);

        return instantiate(impl);
    }

    private FtpHostImpl createHost(AddHostWizard wizard){
        HostImpl fromWizard = null;
        String name = (String)wizard.getProperty(AddHostWizard.NAME);
        
        Object object = wizard.getProperty(HOST);
        if (object instanceof HostImpl){
            fromWizard = (HostImpl)object;
        }
        
        return (FtpHostImpl) copyHost(name, fromWizard);
    }
    
    public HostImpl copyHost(String newName, HostImpl fromHost) {
        FtpHostImpl newHost = new FtpHostImpl( newName, getProvider() );
        
        copyHostContent(fromHost, newHost);
        
        return newHost;
    }
    
    // todo move to parent
    protected void copyHostContent( HostImpl fromHost, FtpHostImpl toHost){
        if (toHost == null){
            return;
        }
        
        super.copyHostContent(fromHost, toHost);
               
        copyHostProperty(fromHost, toHost, FtpHostImpl.FTP_SERVER);
        copyHostProperty(fromHost, toHost, FtpHostImpl.FTP_USER_NAME);
        copyHostProperty(fromHost, toHost, FtpHostImpl.FTP_PASSWORD);
        copyHostProperty(fromHost, toHost, FtpHostImpl.FTP_DIRECTORY);
    }
    
    private void copyHostProperty(HostImpl fromHost, HostImpl toHost, 
            String key)
    {
        Object value = null;
        if (fromHost != null && fromHost instanceof FtpHostImpl){
            value = fromHost.getProperty(key);
        }
        if (value != null){
            toHost.setProperty(key, value);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.UiConfigProvider#uninitilize()
     */
    public void uninitilize() {
        getHttpPanel().uninitialize();
        getFilePanel().uninitialize();
    }
    
    FtpServerHttpWizardPanel getHttpPanel(){
        return myHttpPanel;
    }
    
    FtpServerFileWizardPanel getFilePanel(){
        return myFilePanel;
    }
    
    private FtpServerProvider getProvider(){
        return myProvider;
    }
    
    private void initPanels() {
        myHttpPanel = new FtpServerHttpWizardPanel();
        myFilePanel = new FtpServerFileWizardPanel();
        myPanels = new Panel[ ]{ myHttpPanel, myFilePanel };
    }
    
    private FtpServerProvider myProvider;
    
    private FtpServerHttpWizardPanel myHttpPanel;
    private FtpServerFileWizardPanel myFilePanel;
    
    private Panel[] myPanels;

}
