/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
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

package org.netbeans.modules.j2me.cdc.project.ricoh;

import com.sun.org.apache.bcel.internal.generic.FASTORE;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor.class, position=20)
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
