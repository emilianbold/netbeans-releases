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
    
    public static final String RICOH_EMAIL               = "ricoh.application.email"; //NOI18N                         
    public static final String RICOH_FAX                 = "ricoh.application.fax"; //NOI18N                           
    public static final String RICOH_PHONE               = "ricoh.application.telephone"; //NOI18N                     
    public static final String RICOH_UID                 = "ricoh.application.uid"; //NOI18N                           
    public static final String RICOH_ICON                = "ricoh.application.icon"; //NOI18N                          
    public static final String RICOH_TARGET_JAR          = "ricoh.application.target-jar"; //NOI18N                    
    public static final String RICOH_APP_VERSION         = "ricoh.application.version"; //NOI18N                       
                                                                                                      
    public static final String RICOH_PLATFORM_TARGET_VER = "ricoh.platform.target.version"; //NOI18N                   
    public static final String RICOH_ICON_INVERT                 = "ricoh.icon.invert"; //NOI18N                       
                                                                                                      
    //dalp properties                                                                                 
    public static final String RICOH_DALP_MANAGE_DISABLE         = "ricoh.dalp.is-managed"; //NOI18N                   
    public static final String RICOH_DALP_VERSION                = "ricoh.dalp.version"; //NOI18N                      
    public static final String RICOH_DALP_CODEBASE               = "ricoh.dalp.codebase"; //NOI18N                     
    public static final String RICOH_DALP_INFO_ICON_BASEPATH     = "ricoh.dalp.information.icon.basepath"; //NOI18N    
    public static final String RICOH_DALP_INFO_ICON_LOCATION     = "ricoh.dalp.information.icon.location"; //NOI18N    
    public static final String RICOH_DALP_INFO_ABBREVIATION      = "ricoh.dalp.information.abbreviation"; //NOI18N     
    public static final String RICOH_DALP_INFO_IS_ABREVIATION_USED  = "ricoh.dalp.information.is-icon-used"; //NOI18N     
    public static final String RICOH_DALP_RESOURCES_DSDK_VERSION = "ricoh.dalp.resources.dsdk.version"; //NOI18N       
    public static final String RICOH_DALP_RESOURCES_JAR_VERSION  = "ricoh.dalp.resources.jar.version"; //NOI18N        
    public static final String RICOH_DALP_RESOURCES_JAR_BASEPATH = "ricoh.dalp.resources.jar.basepath"; //NOI18N       
    public static final String RICOH_DALP_APPDESC_VISIBLE        = "ricoh.dalp.application-desc.visible"; //NOI18N     
    public static final String RICOH_DALP_APPDESC_AUTORUN        = "ricoh.dalp.application-desc.auto-run"; //NOI18N    
    public static final String RICOH_DALP_APPDESC_EXECAUTH       = "ricoh.dalp.application-desc.exec-auth"; //NOI18N   
    public static final String RICOH_DALP_APPDESC_ENERGYSAVE     = "ricoh.dalp.application-desc.energy-save"; //NOI18N 
    public static final String RICOH_DALP_INSTALL_MODE_AUTO      = "ricoh.dalp.install.mode.auto"; //NOI18N                 
    public static final String RICOH_DALP_INSTALL_DESTINATION    = "ricoh.dalp.install.destination"; //NOI18N          
    public static final String RICOH_DALP_INSTALL_WORKDIR        = "ricoh.dalp.install.work-dir"; //NOI18N             
    public static final String RICOH_DALP_DISPLAYMODE_HVGA       = "ricoh.dalp.display-mode.is-hvga-support"; //NOI18N 
    public static final String RICOH_DALP_DISPLAYMODE_VGA        = "ricoh.dalp.display-mode.is-vga-support"; //NOI18N  
    public static final String RICOH_DALP_DISPLAYMODE_WVGA       = "ricoh.dalp.display-mode.is-wvga-support"; //NOI18N 
    public static final String RICOH_DALP_DISPLAYMODE_4LINE      = "ricoh.dalp.display-mode.is-4line-support"; //NOI18N
    public static final String RICOH_DALP_DISPLAYMODE_COLOR      = "ricoh.dalp.display-mode.color"; //NOI18N            
    public static final String RICOH_DALP_ARGUMENT               = "ricoh.dalp.argument"; //NOI18N                     
                                                                                                      
    private Reference<Set<PropertyDescriptor>> ref = new WeakReference(null);
    private PropertyDescriptor uid;
   
    /** Creates a new instance of RicohPropertiesDescriptor */
    public RicohPropertiesDescriptor() {
    }
     
    private String randomUID() {
        String s = "00000000" + String.valueOf((long)(Math.random()*100000000)); //NOI18N
        return s.substring(s.length()-8, s.length());
    }
   
    public synchronized Set<PropertyDescriptor> getPropertyDescriptors() {
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            String FALSE = "false"; //NOI18N
            String TRUE = "true"; //NOI18N
            String EMPTY = ""; //NOI18N
            set = new HashSet<PropertyDescriptor>();
            set.add(new PropertyDescriptor(RICOH_EMAIL, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                            
            set.add(new PropertyDescriptor(RICOH_FAX, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                            
            set.add(new PropertyDescriptor(RICOH_PHONE, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                            
            set.add(new PropertyDescriptor(RICOH_ICON, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                            
            set.add(new PropertyDescriptor(RICOH_TARGET_JAR, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                            
            set.add(new PropertyDescriptor(RICOH_APP_VERSION, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                                        
            set.add(new PropertyDescriptor(RICOH_PLATFORM_TARGET_VER, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                            
            set.add(new PropertyDescriptor(RICOH_ICON_INVERT, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));          
            set.add(new PropertyDescriptor(RICOH_DALP_MANAGE_DISABLE, true, DefaultPropertyParsers.INVERSE_BOOLEAN_PARSER, TRUE));                    
            set.add(new PropertyDescriptor(RICOH_DALP_VERSION, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            set.add(new PropertyDescriptor(RICOH_DALP_CODEBASE, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            set.add(new PropertyDescriptor(RICOH_DALP_INFO_ICON_BASEPATH, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            set.add(new PropertyDescriptor(RICOH_DALP_INFO_ICON_LOCATION, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            set.add(new PropertyDescriptor(RICOH_DALP_INFO_ABBREVIATION, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            set.add(new PropertyDescriptor(RICOH_DALP_INFO_IS_ABREVIATION_USED, true, DefaultPropertyParsers.INVERSE_BOOLEAN_PARSER, TRUE));                    
            set.add(new PropertyDescriptor(RICOH_DALP_RESOURCES_DSDK_VERSION, true, DefaultPropertyParsers.STRING_PARSER, "2.0")); //NOI18N                    
            set.add(new PropertyDescriptor(RICOH_DALP_RESOURCES_JAR_VERSION, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            set.add(new PropertyDescriptor(RICOH_DALP_RESOURCES_JAR_BASEPATH, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            set.add(new PropertyDescriptor(RICOH_DALP_APPDESC_VISIBLE, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));                    
            set.add(new PropertyDescriptor(RICOH_DALP_APPDESC_AUTORUN, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));                    
            set.add(new PropertyDescriptor(RICOH_DALP_APPDESC_EXECAUTH, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            set.add(new PropertyDescriptor(RICOH_DALP_APPDESC_ENERGYSAVE, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            set.add(new PropertyDescriptor(RICOH_DALP_INSTALL_MODE_AUTO, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE)); //NOI18N                    
            set.add(new PropertyDescriptor(RICOH_DALP_INSTALL_DESTINATION, true, DefaultPropertyParsers.STRING_PARSER, "hdd")); //NOI18N                    
            set.add(new PropertyDescriptor(RICOH_DALP_INSTALL_WORKDIR, true, DefaultPropertyParsers.STRING_PARSER, "hdd")); //NOI18N                    
            set.add(new PropertyDescriptor(RICOH_DALP_DISPLAYMODE_HVGA, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));                    
            set.add(new PropertyDescriptor(RICOH_DALP_DISPLAYMODE_VGA, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));                    
            set.add(new PropertyDescriptor(RICOH_DALP_DISPLAYMODE_WVGA, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));                    
            set.add(new PropertyDescriptor(RICOH_DALP_DISPLAYMODE_4LINE, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));                    
            set.add(new PropertyDescriptor(RICOH_DALP_DISPLAYMODE_COLOR, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));                    
            set.add(new PropertyDescriptor(RICOH_DALP_ARGUMENT, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));                    
            ref = new WeakReference(set);
        }
        if (uid != null) set.remove(uid);
        uid = new PropertyDescriptor(RICOH_UID, true, DefaultPropertyParsers.STRING_PARSER,  randomUID());
        set.add(uid);
        return set;
    }
}
