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

package org.netbeans.modules.j2ee.sun.share.config;

import java.util.Collections;
import java.util.Set;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
//import org.netbeans.modules.j2ee.deployment.plugins.api.ConfigurationSupport;

import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiFileLoader;

import org.netbeans.modules.j2ee.sun.share.config.ui.ConfigBeanTopComponent;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;


/**
 * @author nn136682
 */
public class SecondaryConfigDataObject extends ConfigDataObject {
    
    private ConfigDataObject primary;
    
    /** Creates a new instance of SecondaryConfigDataObject */
    public SecondaryConfigDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
    }
    
    public boolean isSecondaryOf(ConfigDataObject primary) {
        return getPrimaryDataObject() == primary;
    }
    
    public SunONEDeploymentConfiguration getDeploymentConfiguration() throws ConfigurationException {
        // Request deployment configuration for SJSAS from j2eeserver module
        ConfigDataObject p = getPrimaryDataObject();
        FileObject fo = p.getPrimaryFile();
        String serverId = getProvider().getServerID();
        // DDBean Removal
        //ConfigurationSupport.requestCreateConfiguration(fo, serverId);
        return SunONEDeploymentConfiguration.getConfiguration(FileUtil.toFile(fo));
    }
    
    private ConfigDataObject getPrimaryDataObject() {
        if (primary == null || !primary.isValid()) {
            // The only JSR-88 secondary configuration file supported by SJSAS 8.x 
            // or 9.x is sun-cmp-mappings.xml which is secondary for sun-ejb-jar.xml
            // AND that they will always reside in the same directory.  So we can find
            // the primary data object by doing a find("sun-ejb-jar.xml") here.
            FileObject folder = getPrimaryFile().getParent();
            FileObject sejFO = folder.getFileObject("sun-ejb-jar", "xml");
            if(sejFO != null) {
                try {
                    DataObject dObj = DataObject.find(sejFO);
                    primary = (ConfigDataObject) dObj.getCookie(ConfigDataObject.class);
                    if(primary != null) {
                        primary.addSecondary(this);
                    }
                } catch(DataObjectNotFoundException ex) {
                }
            }
        }
        
        return primary;
    }
    
    private EditCookie _getEditCookie() {
        ConfigDataObject cdo = getPrimaryDataObject();
        EditCookie primaryEdit = cdo == null ? null : cdo.getEditCookie();
        EditCookie myEdit = super.getEditCookie();
        if (primaryEdit != null) {
            return myEdit;
        } else {
            return null;
        }
    }

    private OpenCookie _getOpenCookie() {
        ConfigDataObject cdo = getPrimaryDataObject();
        return cdo == null ? null : (OpenCookie) cdo.getCookie(OpenCookie.class);
    }

    public <T extends Node.Cookie> T getCookie(Class<T> c) {
        if (OpenCookie.class.isAssignableFrom(c)) {
            return (T) _getOpenCookie();
        } else if (EditCookie.class.isAssignableFrom(c)) {
            return (T) _getEditCookie();
        }
        return super.getCookie(c);
    }
    
    protected Set getSecondaries() {
        return Collections.EMPTY_SET;
    }
    
    protected ConfigurationStorage getStorage() {
        ConfigDataObject cdo = getPrimaryDataObject();
        return cdo == null ? null : cdo.getStorage();
    }
    
    protected void openConfigEditor() {
        ConfigDataObject cdo = getPrimaryDataObject();
        if (cdo != null) {
            getPrimaryDataObject().openConfigEditor();
            firePropertyChange(PROP_COOKIE, null, null);
        }
    }
    
    protected ConfigBeanTopComponent findOpenedConfigEditor() {
        ConfigDataObject cdo = getPrimaryDataObject();
        return cdo == null ? null : cdo.findOpenedConfigEditor();
    }
    
    public boolean closeConfigEditors() {
        ConfigDataObject cdo = getPrimaryDataObject();
        return cdo == null ? false : cdo.closeConfigEditors();
    }
    
    public void fileDeleted(org.openide.filesystems.FileEvent fe) {
        if (fe.getFile().equals(this.getPrimaryFile()) && getPrimaryDataObject() != null) {
            primary.removeSecondary(this);
        }
    }

    protected void fireCookieChange() {
        fireLimitedCookieChange();
        ConfigDataObject cdo = getPrimaryDataObject();
        if (cdo != null) {
            cdo.fireLimitedCookieChange();
        }
    }

    //warn: is called from primary ConfigDataObject, don't delegate back
    public void setChanged() {
        addSaveCookie(new S0());
    }

   //No actual save until we have SPI to notify individual config descriptor change.
    private class S0 implements SaveCookie {
        public void save() throws java.io.IOException {
            ConfigDataObject cdo = getPrimaryDataObject();
            if (cdo != null) {
                cdo.resetAllChanged();
            }
        }
    }
}
