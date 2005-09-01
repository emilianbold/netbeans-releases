/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.config;

import java.lang.reflect.InvocationTargetException;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;


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
        setIconBase ("org/netbeans/modules/j2ee/sun/share/config/ui/resources/ConfigFile");
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
    private final class VersionProperty extends PropertySupport.ReadWrite {
        
        public VersionProperty() {
            super("DDVersion" /*NOI18N*/, VersionEditor.class,
                NbBundle.getBundle(ConfigDataNode.class).getString("LBL_ConfigVersionPropertyName"), // NOI18N
                NbBundle.getBundle(ConfigDataNode.class).getString("LBL_ConfigVersionPropertyDescription")); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
//            return dataObject.getServicePackageName();
            return VersionEditor.availableChoices[VersionEditor.APP_SERVER_8_1];
        }

        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if(val instanceof String) {
//                dataObject.setServicePackageName((String) val);
            } else {
                throw new IllegalArgumentException("value must be non null and type String");
            }
        }

        public java.beans.PropertyEditor getPropertyEditor() {
            // !PW FIXME
            //   1. get dd version.
            //   2. get connected server version
            //   3. calculate min/max version for editor.
            return new VersionEditor(VersionEditor.APP_SERVER_8_1, VersionEditor.APP_SERVER_8_1);
        }
    }
}
