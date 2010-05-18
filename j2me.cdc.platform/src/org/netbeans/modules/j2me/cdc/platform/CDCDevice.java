/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2me.cdc.platform;

import java.util.Map;

/**
 * Desribes CDCDevice
 * @author suchys
 */
public class CDCDevice {

    private String name;

    private String description;

    private CDCDevice.CDCProfile[] profiles;

    private CDCDevice.Screen[] screens;
 
    /**
     * Create new default CDCDevice without any profile and screen
     */
    public CDCDevice(){        
        this ("Default", "Default device"); //NOI18N
    }
    
    /**
     * Create new CDCDevice without any profile and screen
     * @param name name of the device
     * @param description description of the device
     */
    public CDCDevice(String name, String description){
        this (name, description, null, null);
    }
    
    /**
     * Create new default CDCDevice
     * @param name name of the device
     * @param description description of the device
     * @param profiles array of supported profiles
     * @param screens array of supported screens
     */
    public CDCDevice(String name, String description, 
            CDCProfile[] profiles, 
            Screen[] screens){
        this.name = name;
        this.description = description;
        this.profiles = profiles;
        this.screens = screens;
    }
    
    /**
     * @return name of the device
     */
    public String getName(){
        return name;
    }
    
    /**
     * @return description of the device
     */
    public String getDescription(){
        return description;
    }
    
    /**
     * @return array of CDCProfile supported by platfrom
     */
    public CDCProfile[] getProfiles(){
        return profiles;
    }

    /**
     * @param profiles array of CDCProfile supported by platfrom
     */
    public void setProfiles(CDCProfile[] profiles){
        this.profiles = profiles;
    }

    /**
     * @return array of Screens supported by platfrom
     */
    public Screen[] getScreens(){
        return screens;
    }
    
    /**
     * @param screens array of Screens supported by platfrom
     */
    public void setScreens(Screen[] screens){
        this.screens = screens;
    }
    
    public String toString(){
        return getName();
    }
    /**
     * Describing CDCProfile of platform
     */
    public static class CDCProfile {
        
        private String name;
        
        private String version;
        
        private String description;

        private Map<String,String> executionModes;

        private String bootClassPath;

        private String runClassPath;
        
        private boolean isDefault;
        
        public CDCProfile( String name,
                           String description,
                           String version, 
                           Map<String,String> executionModes, 
                           String bootClassPath, 
                           String runClassPath,
                           boolean isDefault){
            this.name = name;
            this.version = version;
            this.description = description;
            this.executionModes = executionModes;
            this.bootClassPath = bootClassPath;
            this.runClassPath = runClassPath;
            this.isDefault = isDefault;
        }

        public String getName(){
            return name;
        }
        
        public String getVersion(){
            return version;            
        }
        
        public String getDescription() {
            return description;
        }

        public Map<String,String> getExecutionModes() {
            return executionModes;
        }
        
        public void setExecutionModes(Map<String,String> executionModes) {
            this.executionModes = executionModes;
        }
        
        public String getBootClassPath() {
            return bootClassPath;
        }
        
        public void setBootClassPath(String bootClassPath) {
            this.bootClassPath = bootClassPath;
        }
        
        public String getRunClassPath() {
            return runClassPath;
        }
        
        public void setRunClassPath(String runClassPath) {
            this.runClassPath = runClassPath;
        }        
        
        public boolean isDefault(){
            return isDefault;
        }
        
        public void setDefault(boolean isDefault){
            this.isDefault = isDefault;
        }
        
        public String toString() {
            return this.getName();
        }
    }
    
    /**
     * Describing Screen of platform
     */
    public static class Screen {
        
        private Integer width;
        private Integer height;
        private Integer bitDepth;
        private Boolean color = Boolean.TRUE;
        private Boolean touch = Boolean.FALSE;
        private Boolean main  = Boolean.TRUE;
        
        public Screen(String width, String height, String bitDepth, String color, String touch, String main) {
            Object o;
            try {
                o = new Integer(width);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.width = (Integer) o;
            
            try {
                o = new Integer(height);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.height = (Integer) o;
            
            try {
                o = new Integer(bitDepth);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.bitDepth = (Integer) o;
            
            try {
                o = new Boolean(color);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.color = (Boolean) o;
            
            try {
                o = new Boolean(touch);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.touch = (Boolean) o;

            try {
                o = new Boolean(main);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.main = (Boolean) o;
        }
        
        public Integer getBitDepth() {
            return bitDepth;
        }
        
        public Boolean getColor() {
            return color;
        }
        
        public Integer getHeight() {
            return height;
        }
        
        public Boolean getTouch() {
            return touch;
        }
        
        public Integer getWidth() {
            return width;
        }
        
        public Boolean istMain(){
            return main;
        }
    }
}
