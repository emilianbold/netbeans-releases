/*
 * Profile.java
 *
 * Created on April 30, 2007, 3:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import org.openide.util.NbBundle;

/**
 *
 * @author vkraemer
 */
enum Profile {
    DEFAULT(NbBundle.getMessage(Profile.class, "DEFAULT_DISPLAY_VALUE"), ""),
    DEVELOPER(NbBundle.getMessage(Profile.class, "DEVELOPER_DISPLAY_VALUE"), "developer"),
    CLUSTER(NbBundle.getMessage(Profile.class, "CLUSTER_DISPLAY_VALUE"), "cluster"),
    ENTERPRISE(NbBundle.getMessage(Profile.class, "ENTERPRISE_DISPLAY_VALUE"), "enterprise");
    
    private String displayName;
    private String value;
    Profile(String displayName, String value) {
        this.displayName = displayName;
        this.value = value;
    }
    
    public String toString() {
        return displayName;
    }
    
    public String value() {
        return value;
    }
    
}
