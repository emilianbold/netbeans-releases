/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.config;

import java.lang.reflect.InvocationTargetException;

import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.EditCookie;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.sun.share.configbean.ASDDVersion;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;


/** Simple node to represent a deployment plan file.
 * @author Pavel Buzek
 */
public class ConfigDataNode extends DataNode {

    private ConfigDataObject dataObject;
    
    public ConfigDataNode (ConfigDataObject obj) {
        this (obj, Children.LEAF);
        this.dataObject = obj;
    }

    public ConfigDataNode (ConfigDataObject obj, Children ch) {
        super (obj, ch);
        setIconBaseWithExtension("org/netbeans/modules/j2ee/sun/share/config/ui/resources/ConfigFile.gif"); // NOI18N
    }


    public void destroy() throws java.io.IOException {
        this.dataObject = null;
        super.destroy();
    }

    // Support for appserver version property
    protected org.openide.nodes.Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set ss = new Sheet.Set();

        ss.setName("deploymentdescriptor"); // NOI18N
        ss.setDisplayName(NbBundle.getBundle(ConfigDataNode.class).getString("LBL_ConfigPropertiesName")); // NOI18N
        ss.setShortDescription(NbBundle.getBundle(ConfigDataNode.class).getString("LBL_ConfigPropertiesDescription")); // NOI18N
        ss.put(new VersionProperty());

        sheet.put(ss);
        return sheet;
    }
    
    /** Property to allow editing of the version of the deployment descriptor file.
     *  e.g. from 2.4.0 to 2.4.1 (SJSAS 8.0 -> SJSAS 8.1), etc.
     */
    private final class VersionProperty extends PropertySupport {
        
        public VersionProperty() {
            super("DDVersion" /*NOI18N*/, VersionEditor.class,
                NbBundle.getBundle(ConfigDataNode.class).getString("LBL_ConfigVersionPropertyName"), // NOI18N
                NbBundle.getBundle(ConfigDataNode.class).getString("LBL_ConfigVersionPropertyDescription"), // NOI18N
                true, false);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            String result = ASDDVersion.SUN_APPSERVER_8_1.toString();
            
            try {
                SunONEDeploymentConfiguration config = dataObject.getDeploymentConfiguration();
                result = config.getAppServerVersion().toString();
            } catch(ConfigurationException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
            return result;
        }

        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if(val instanceof String) {
                try {
                    ASDDVersion asDDVersion = ASDDVersion.getASDDVersion((String) val);
                    SunONEDeploymentConfiguration config = dataObject.getDeploymentConfiguration();
                    config.setAppServerVersion(asDDVersion);
                } catch(ConfigurationException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            } else {
                throw new IllegalArgumentException("value must be non null and type String"); // NOI18N
            }
        }

        public java.beans.PropertyEditor getPropertyEditor() {
            VersionEditor result = null;
            
            try {
                SunONEDeploymentConfiguration config = dataObject.getDeploymentConfiguration();
                int minAS = VersionEditor.fromASDDVersion(config.getMinASVersion());
                int maxAS = VersionEditor.fromASDDVersion(config.getMaxASVersion());
                result = new VersionEditor(minAS, maxAS);
            } catch (ConfigurationException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        
            return result;
        }

        public boolean canWrite() {
            // If we can open either the configuration editor or the XML text editor
            // then neither editor is open (they are exclusive).  If neither editor
            // is open, then this field is editable.
            return dataObject.getCookie(OpenCookie.class) != null && 
                dataObject.getCookie(EditCookie.class) != null;
        }

        public boolean canRead() {
            return true;
        }

    }
}
