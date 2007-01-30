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

package org.netbeans.modules.j2me.cdc.project.ricoh;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;

/**
 *
 * @author Adam Sotona
 */
public class RicohPropertiesDescriptor implements ProjectPropertiesDescriptor {

    //ToDo - move selected properties to Ricoh deployment plugin 
    public static final String RICOH_APPLICATION_EMAIL = "ricoh.application.email"; //NOI18N                                               
    public static final String RICOH_APPLICATION_FAX = "ricoh.application.fax"; //NOI18N                                               
    public static final String RICOH_APPLICATION_TELEPHONE = "ricoh.application.telephone"; //NOI18N                                               
    public static final String RICOH_APPLICATION_TYPE = "ricoh.application.type"; //NOI18N                                               
    public static final String RICOH_APPLICATION_UID = "ricoh.application.uid"; //NOI18N                                               
    public static final String RICOH_DALP_APPLICATION_DESC_AUTO_RUN = "ricoh.dalp.application-desc.auto-run"; //NOI18N                                               
    public static final String RICOH_DALP_APPLICATION_DESC_VISIBLE = "ricoh.dalp.application-desc.visible"; //NOI18N                                               
    public static final String RICOH_DALP_DISPLAY_MODE_IS_4LINE_SUPPORT = "ricoh.dalp.display-mode.is-4line-support"; //NOI18N                                               
    public static final String RICOH_DALP_DISPLAY_MODE_IS_HVGA_SUPPORT = "ricoh.dalp.display-mode.is-hvga-support"; //NOI18N                                               
    public static final String RICOH_DALP_DISPLAY_MODE_IS_VGA_SUPPORT = "ricoh.dalp.display-mode.is-vga-support"; //NOI18N                                               
    public static final String RICOH_DALP_DISPLAY_MODE_IS_WVGA_SUPPORT = "ricoh.dalp.display-mode.is-wvga-support"; //NOI18N                                               
    public static final String RICOH_DALP_INFORMATION_IS_ICON_USED = "ricoh.dalp.information.is-icon-used"; //NOI18N                                               
    public static final String RICOH_DALP_INSTALL_DESTINATION = "ricoh.dalp.install.destination"; //NOI18N                                               
    public static final String RICOH_DALP_INSTALL_MODE = "ricoh.dalp.install.mode"; //NOI18N                                               
    public static final String RICOH_DALP_INSTALL_WORK_DIR = "ricoh.dalp.install.work-dir"; //NOI18N                                               
    public static final String RICOH_DALP_IS_MANAGED = "ricoh.dalp.is-managed"; //NOI18N                                               
    public static final String RICOH_DALP_RESOURCES_DSDK_VERSION = "ricoh.dalp.resources.dsdk.version"; //NOI18N                                               
    public static final String RICOH_ICON_INVERT = "ricoh.icon.invert"; //NOI18N                                               
    public static final String RICOH_INSTALL_SERVER_DEPLOY_METHOD = "ricoh.install-server.deploy-method"; //NOI18N                                               
    public static final String RICOH_SCP_INSTALL_SERVER_SSH_PORT = "ricoh.scp.install-server.ssh-port"; //NOI18N                                               
    public static final String RICOH_SCP_INSTALL_SERVER_WEB_PATH = "ricoh.scp.install-server.web-path"; //NOI18N                                               
    public static final String RICOH_SCP_INSTALL_SERVER_WEB_PORT = "ricoh.scp.install-server.web-port"; //NOI18N                                               
    public static final String RICOH_SMB_INSTALL_SERVER_SMB_PORT = "ricoh.smb.install-server.smb-port"; //NOI18N                                               
    public static final String RICOH_SMB_INSTALL_SERVER_WEB_PATH = "ricoh.smb.install-server.web-path"; //NOI18N                                               
    public static final String RICOH_SMB_INSTALL_SERVER_WEB_PORT = "ricoh.smb.install-server.web-port"; //NOI18N      

    private Reference<Set<PropertyDescriptor>> ref = new WeakReference(null);
    
    /** Creates a new instance of RicohPropertiesDescriptor */
    public RicohPropertiesDescriptor() {
    }
    
    public Set getPropertyDescriptors() {
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            String FALSE = "false"; //NOI18N
            String TRUE = "true"; //NOI18N
            set = new HashSet();
            set.add(new PropertyDescriptor(RICOH_APPLICATION_EMAIL, true, DefaultPropertyParsers.STRING_PARSER, "xx")); //NOI18N
            set.add(new PropertyDescriptor(RICOH_APPLICATION_FAX, true, DefaultPropertyParsers.STRING_PARSER, "xx")); //NOI18N
            set.add(new PropertyDescriptor(RICOH_APPLICATION_TELEPHONE, true, DefaultPropertyParsers.STRING_PARSER, "xx")); //NOI18N
            set.add(new PropertyDescriptor(RICOH_APPLICATION_TYPE, true, DefaultPropertyParsers.STRING_PARSER, "xlet")); //NOI18N
            set.add(new PropertyDescriptor(RICOH_APPLICATION_UID, true, DefaultPropertyParsers.STRING_PARSER, "")); //NOI18N
            set.add(new PropertyDescriptor(RICOH_DALP_APPLICATION_DESC_AUTO_RUN, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));     
            set.add(new PropertyDescriptor(RICOH_DALP_APPLICATION_DESC_VISIBLE, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));     
            set.add(new PropertyDescriptor(RICOH_DALP_DISPLAY_MODE_IS_4LINE_SUPPORT, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));     
            set.add(new PropertyDescriptor(RICOH_DALP_DISPLAY_MODE_IS_HVGA_SUPPORT, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));     
            set.add(new PropertyDescriptor(RICOH_DALP_DISPLAY_MODE_IS_VGA_SUPPORT, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));     
            set.add(new PropertyDescriptor(RICOH_DALP_DISPLAY_MODE_IS_WVGA_SUPPORT, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));     
            set.add(new PropertyDescriptor(RICOH_DALP_INFORMATION_IS_ICON_USED, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));     
            set.add(new PropertyDescriptor(RICOH_DALP_INSTALL_DESTINATION, true, DefaultPropertyParsers.STRING_PARSER, "hdd")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_DALP_INSTALL_MODE, true, DefaultPropertyParsers.STRING_PARSER, "auto")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_DALP_INSTALL_WORK_DIR, true, DefaultPropertyParsers.STRING_PARSER, "hdd")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_DALP_IS_MANAGED, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));     
            set.add(new PropertyDescriptor(RICOH_DALP_RESOURCES_DSDK_VERSION, true, DefaultPropertyParsers.STRING_PARSER, "2.0")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_ICON_INVERT, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));     
            set.add(new PropertyDescriptor(RICOH_INSTALL_SERVER_DEPLOY_METHOD, true, DefaultPropertyParsers.STRING_PARSER, "sdcard")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_SCP_INSTALL_SERVER_SSH_PORT, true, DefaultPropertyParsers.STRING_PARSER, "22")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_SCP_INSTALL_SERVER_WEB_PATH, true, DefaultPropertyParsers.STRING_PARSER, "/")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_SCP_INSTALL_SERVER_WEB_PORT, true, DefaultPropertyParsers.STRING_PARSER, "80")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_SMB_INSTALL_SERVER_SMB_PORT, true, DefaultPropertyParsers.STRING_PARSER, "139")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_SMB_INSTALL_SERVER_WEB_PATH, true, DefaultPropertyParsers.STRING_PARSER, "/")); //NOI18N     
            set.add(new PropertyDescriptor(RICOH_SMB_INSTALL_SERVER_WEB_PORT, true, DefaultPropertyParsers.STRING_PARSER, "80")); //NOI18N
            ref = new WeakReference(set);
        }
        return set;
    }
}
