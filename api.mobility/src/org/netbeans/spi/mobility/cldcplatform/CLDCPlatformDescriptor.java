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

/*
 * CLDCPlatformDescriptor.java
 *
 */
package org.netbeans.spi.mobility.cldcplatform;

import java.util.Collections;
import java.util.List;

/**
 * CLDCPlatformDescriptor is used to describe a platform detected by CustomCLDCPlatformConfigurator.
 * This class is only a holder of information about platform type, devices, classpath, command lines, etc...
 * @author Adam Sotona
 */
public final class CLDCPlatformDescriptor {
    
    /**
     * CLDC Platform Device Profile Types Enumeration.
     */
    public static enum ProfileType {
        /**
         * Device Configuration (f.ex.: CLDC)
         */
        Configuration, 
        /**
         * Device Profile (f.ex.: MIDP)
         */
        Profile, 
        /**
         * Device Optional API (f.ex.: MMAPI)
         */
        Optional};
    /**
     * Display name of the platform.
     * May never be null.
     */
    public final String displayName;
    /**
     * Home directory of the platform.
     * May never be null.
     */
    public final String home;
    /**
     * Type of the platform (currently supported types are UEI-1.0, UEI-1.0.1, and CUSTOM).
     * It is likely that CustomCLDCPlatformConfgigurator will provide CUSTOM platform type with all the necessary information to use the platform.
     * May never be null.
     */
    public final String type;
    /**
     * Comma-separated list of platform source roots. Each of the root can be defined as a relative path prefixed with ${platform.home}
     * Definition of source roots is optional and most of the platforms are provided without sources available however it is usefull for Java development to have direct access to all sources.
     */
    public final String srcPath;
    /**
     * Comma-separated list of platform JavaDoc roots (might also include zip and jar files with the platform JavaDoc). Each of the root can be defined as a relative path prefixed with ${platform.home}
     * Definition of JavaDoc roots is optional however it is usefull for Java development to have direct access to JavaDoc.
     */
    public final String docPath;
    /**
     * Definition of preverify command line is mandatory for CUSTOM platofrm type. For other then CUSTOM platform types is this field ignored.
     * 
     * Sample preverify command line for all UEI compatible platforms is:
     * "{platformhome}{/}bin{/}preverify" {classpath|-classpath "{classpath}"} -d "{destdir}" "{srcdir}"
     * 
     * Parameters:
     *    platformhome - platform home filled above
     *    srcdir - source directory of classes to preverify
     *    destdir - destination directory for preverified classes
     *    classpath - platform classpath will be defined on the next wizard step
     *    / - OS specific directory separator
     * 
     * Parameter formats:
     * {argument} is a simple replacement, so for example {jadfile} will be replaced by the real path to the jad file.
     * {argument|command line part} is a condition, if argument is set to some value the command line part will be added to the command line.
     * Combination of condition and simple replacement is allowed. For example: {jadfile|-Xdescriptor:"{jadfile}"} means that if the jadfile argument contains /myfolder/my.jad then -Xdescriptor:"/myfolder/my.jad" will be added to the command line.
     */
    public final String preverifyCmd;
    /**
     * Definition of run command line is mandatory for CUSTOM platofrm type. For other then CUSTOM platform types is this field ignored.
     * 
     * Sample run command line for UEI compatible platforms is:
     * "{platformhome}{/}bin{/}emulator" {device|-Xdevice:"{device}"} {jadfile|-Xdescriptor:"{jadfile}"} {securitydomain|-Xdomain:{securitydomain}} {cmdoptions}
     * 
     * Parameters:
     *    platformhome - platform home filled above
     *    device - device name filled above
     *    classpath - platform classpath will be defined on the next wizard step
     *    jadfile - jad file name
     *    jadurl - jad url for OTA execution
     *    securitydomain - security domain for OTA execution
     *    cmdoptions - command line options propagated from project settings
     *    / - OS specific directory separator
     * 
     * Parameter formats:
     * {argument} is a simple replacement, so for example {jadfile} will be replaced by the real path to the jad file.
     * {argument|command line part} is a condition, if argument is set to some value the command line part will be added to the command line.
     * Combination of condition and simple replacement is allowed. For example: {jadfile|-Xdescriptor:"{jadfile}"} means that if the jadfile argument contains /myfolder/my.jad then -Xdescriptor:"/myfolder/my.jad" will be added to the command line.
     */
    public final String runCmd;
    
    /**
     * Definition of debug command line is mandatory for CUSTOM platofrm type. For other then CUSTOM platform types is this field ignored.
     * 
     * Sample debug command line for UEI compatible platforms is:
     * "{platformhome}{/}bin{/}emulator" {device|-Xdevice:"{device}"} {jadfile|-Xdescriptor:"{jadfile}"} {securitydomain|-Xdomain:{securitydomain}} {debug|-Xdebug -Xrunjdwp:transport={debugtransport},server={debugserver},suspend={debugsuspend},address={debugaddress}} {cmdoptions}
     * 
     * Parameters:
     *    platformhome - platform home filled above
     *    device - device name filled above
     *    classpath - platform classpath will be defined on the next wizard step
     *    jadfile - jad file name
     *    jadurl - jad url for OTA execution
     *    securitydomain - security domain for OTA execution
     *    debug - this is just a condition indicating debug mode
     *    debugaddress - debug address
     *    debugtransport - debug transport
     *    debugserver - debug server mode - true by default
     *    debugsuspend - debug suspend mode - true by default
     *    cmdoptions - command line options propagated from project settings
     *    / - OS specific directory separator
     * 
     * Parameter formats:
     * {argument} is a simple replacement, so for example {jadfile} will be replaced by the real path to the jad file.
     * {argument|command line part} is a condition, if argument is set to some value the command line part will be added to the command line.
     * Combination of condition and simple replacement is allowed. For example: {jadfile|-Xdescriptor:"{jadfile}"} means that if the jadfile argument contains /myfolder/my.jad then -Xdescriptor:"/myfolder/my.jad" will be added to the command line.
     */
    public final String debugCmd;
    /**
     * Fixed list of CLDCPlatformDescriptor.Device instances that describe platform devices.
     * The list must always contain at least one device.
     */
    public final List<Device> devices;
    
    /**
     * Creates a new instance of CLDCPlatformDescriptor
     * @param displayName Display name of the platform.
     * The argument is mandatory.
     * @param home Home directory of the platform.
     * The argument is mandatory.
     * @param type Type of the platform (currently supported types are UEI-1.0, UEI-1.0.1, and CUSTOM).
     * It is likely that CustomCLDCPlatformConfgigurator will provide CUSTOM platform type with all the necessary information to use the platform.
     * The argument is mandatory.
     * @param srcPath Comma-separated list of platform source roots. Each of the root can be defined as a relative path prefixed with ${platform.home}
     * Definition of source roots is optional and most of the platforms are provided without sources available however it is usefull for Java development to have direct access to all sources.
     * @param docPath Comma-separated list of platform JavaDoc roots (might also include zip and jar files with the platform JavaDoc). Each of the root can be defined as a relative path prefixed with ${platform.home}
     * Definition of JavaDoc roots is optional however it is usefull for Java development to have direct access to JavaDoc.
     * @param preverifyCmd Definition of preverify command line is mandatory for CUSTOM platofrm type. For other then CUSTOM platform types is this field ignored.
     * 
     * Sample preverify command line for all UEI compatible platforms is:
     * "{platformhome}{/}bin{/}preverify" {classpath|-classpath "{classpath}"} -d "{destdir}" "{srcdir}"
     * 
     * Parameters:
     *    platformhome - platform home filled above
     *    srcdir - source directory of classes to preverify
     *    destdir - destination directory for preverified classes
     *    classpath - platform classpath will be defined on the next wizard step
     *    / - OS specific directory separator
     * 
     * Parameter formats:
     * {argument} is a simple replacement, so for example {jadfile} will be replaced by the real path to the jad file.
     * {argument|command line part} is a condition, if argument is set to some value the command line part will be added to the command line.
     * Combination of condition and simple replacement is allowed. For example: {jadfile|-Xdescriptor:"{jadfile}"} means that if the jadfile argument contains /myfolder/my.jad then -Xdescriptor:"/myfolder/my.jad" will be added to the command line.
     * @param runCmd Definition of run command line is mandatory for CUSTOM platofrm type. For other then CUSTOM platform types is this field ignored.
     * 
     * Sample run command line for UEI compatible platforms is:
     * "{platformhome}{/}bin{/}emulator" {device|-Xdevice:"{device}"} {jadfile|-Xdescriptor:"{jadfile}"} {securitydomain|-Xdomain:{securitydomain}} {cmdoptions}
     * 
     * Parameters:
     *    platformhome - platform home filled above
     *    device - device name filled above
     *    classpath - platform classpath will be defined on the next wizard step
     *    jadfile - jad file name
     *    jadurl - jad url for OTA execution
     *    securitydomain - security domain for OTA execution
     *    cmdoptions - command line options propagated from project settings
     *    / - OS specific directory separator
     * 
     * Parameter formats:
     * {argument} is a simple replacement, so for example {jadfile} will be replaced by the real path to the jad file.
     * {argument|command line part} is a condition, if argument is set to some value the command line part will be added to the command line.
     * Combination of condition and simple replacement is allowed. For example: {jadfile|-Xdescriptor:"{jadfile}"} means that if the jadfile argument contains /myfolder/my.jad then -Xdescriptor:"/myfolder/my.jad" will be added to the command line.
     * @param debugCmd Definition of debug command line is mandatory for CUSTOM platofrm type. For other then CUSTOM platform types is this field ignored.
     * 
     * Sample debug command line for UEI compatible platforms is:
     * "{platformhome}{/}bin{/}emulator" {device|-Xdevice:"{device}"} {jadfile|-Xdescriptor:"{jadfile}"} {securitydomain|-Xdomain:{securitydomain}} {debug|-Xdebug -Xrunjdwp:transport={debugtransport},server={debugserver},suspend={debugsuspend},address={debugaddress}} {cmdoptions}
     * 
     * Parameters:
     *    platformhome - platform home filled above
     *    device - device name filled above
     *    classpath - platform classpath will be defined on the next wizard step
     *    jadfile - jad file name
     *    jadurl - jad url for OTA execution
     *    securitydomain - security domain for OTA execution
     *    debug - this is just a condition indicating debug mode
     *    debugaddress - debug address
     *    debugtransport - debug transport
     *    debugserver - debug server mode - true by default
     *    debugsuspend - debug suspend mode - true by default
     *    cmdoptions - command line options propagated from project settings
     *    / - OS specific directory separator
     * 
     * Parameter formats:
     * {argument} is a simple replacement, so for example {jadfile} will be replaced by the real path to the jad file.
     * {argument|command line part} is a condition, if argument is set to some value the command line part will be added to the command line.
     * Combination of condition and simple replacement is allowed. For example: {jadfile|-Xdescriptor:"{jadfile}"} means that if the jadfile argument contains /myfolder/my.jad then -Xdescriptor:"/myfolder/my.jad" will be added to the command line.
     * @param devices List of CLDCPlatformDescriptor.Device instances that describe platform devices.
     * The list must always contain at least one device.
     */
    public CLDCPlatformDescriptor(String displayName, String home, String type, String srcPath, String docPath, String preverifyCmd, String runCmd, String debugCmd, List<Device> devices) {
        assert displayName != null;
        assert home != null;
        assert type != null;
        assert devices != null;
        assert devices.size() > 0;
        this.displayName = displayName;
        this.home = home;
        this.type = type;
        this.srcPath = srcPath;
        this.docPath = docPath;
        this.preverifyCmd = preverifyCmd;
        this.runCmd = runCmd;
        this.debugCmd = debugCmd;
        this.devices = Collections.unmodifiableList(devices);
    }
    
    
    /**
     * CLDCPlatform.Device holds information about one particular device provided by the platform.
     */
    public static final class Device {
        /**
         * Device name as used for example in command line.
         * May never be null.
         */
        public final String name;
        
        /**
         * Device description.
         */
        public final String description;
        /**
         * List of device security domains.
         */
        public final List<String> securityDomains;
        /**
         * Fixed ist of all device profiles (it means configurations, profiles, and optional APIs).
         * Each device must contains at least one configuration and one profile.
         */
        public final List<Profile> profiles;
        /**
         * Screen parameters record of the device.
         * This field is optional and may be null.
         */
        public final Screen screen;
        
        /**
         * Creates a new instance of CLDCPlatformDescriptor.Device
         * @param name Device name as used for example in command line.
         * The argument is mandatory.
         * @param description Device description.
         * @param securityDomains List of device security domains.
         * @param profiles Fixed ist of all device profiles (it means configurations, profiles, and optional APIs).
         * Each device must contains at least one configuration and one profile.
         * @param screen Screen parameters record of the device.
         * This field is optional and may be null.
         */
        public Device(String name, String description, List<String> securityDomains, List<Profile> profiles, Screen screen) {
            assert name != null;
            assert profiles != null;
            this.name = name;
            this.description = description;
            this.securityDomains = securityDomains;
            this.profiles = profiles;
            this.screen = screen;
        }
    }
    
    /**
     * CLDCPlatformDescriptor.Profile is used to describe one particular profile of one device.
     */
    public static final class Profile {
        /**
         * Profile name (for example MMAPI).
         * This name is a key used for matching of device abilities.
         * The name must not contain spaces nor other special characters.
         * May never be null.
         */
        public final String name;
        /**
         * Profile version number (f.ex.: 1.0).
         * May never be null.
         */
        public final String version;
        /**
         * Profile display name used during the customization (f.ex. Multimedia API).
         */
        public final String displayName;
        /**
         * Comma-separated list of dependencies on other profiles of this device.
         * The exact format is &lt;name&gt;-&lt;version&gt;,&lt;name&gt;-&lt;version&gt;,...
         */
        public final String dependencies;
        
        /**
         * Comma-separated classpath of the profile.
         * Each of the path element can be defined as a relative path prefixed with ${platform.home}
         * The same path element can be referenced by more profiles if they share the same jar or zip path element.
         */
        public final String classPath;
        /**
         * Profile type (Configuration, Profiles, or Optional API).
         * May never be null.
         */
        public final ProfileType type;
        /**
         * True if the profile is a part of the device default classpath.
         */
        public final boolean def;
        
        /**
         * Creates new instance of CLDCPlatformDescriptor.Profile
         * @param name Profile name (for example MMAPI).
         * This name is a key used for matching of device abilities.
         * The name must not contain spaces nor other special characters.
         * The argument is mandatory.
         * @param version Profile version number (f.ex.: 1.0).
         * The argument is mandatory.
         * @param displayName Profile display name used during the customization (f.ex. Multimedia API).
         * @param type Profile type (Configuration, Profiles, or Optional API).
         * The argument is mandatory.
         * @param dependencies Comma-separated list of dependencies on other profiles of this device.
         * The exact format is &lt;name&gt;-&lt;version&gt;,&lt;name&gt;-&lt;version&gt;,...
         * @param classPath Comma-separated classpath of the profile.
         * Each of the path element can be defined as a relative path prefixed with ${platform.home}
         * The same path element can be referenced by more profiles if they share the same jar or zip path element.
         * @param def True if the profile is a part of the device default classpath.
         */
        public Profile(String name, String version, String displayName, ProfileType type, String dependencies, String classPath, boolean def) {
            assert name != null;
            assert version != null;
            assert type != null;
            this.name = name;
            this.version = version;
            this.displayName = displayName;
            this.type = type;
            this.dependencies = dependencies;
            this.classPath = classPath;
            this.def = def;
        }
    }
    
    /**
     * CLDCPlatformDescriptor.Screen is used to describe screen attributes of the device.
     */
    public static final class Screen {
        /**
         * Screen width in pixels.
         */
        public final int width;
        /**
         * Screen height in pixels.
         */
        public final int height;
        
        /**
         * Screen color bit depth.
         */
        public final int bitDepth;
        /**
         * True if the device has color display.
         */
        public final boolean color;
        /**
         * True if the device has touch screen.
         */
        public final boolean touch;
        
        /**
         * Creates new instance of CLDCPlatformDescriptor.
         * @param width Screen width in pixels.
         * @param height Screen height in pixels.
         * @param bitDepth Screen color bit depth.
         * @param color True is the device has color display.
         * @param touch True if the device has touch screen.
         */
        public Screen(int width, int height, int bitDepth, boolean color, boolean touch) {
            this.width = width;
            this.height = height;
            this.bitDepth = bitDepth;
            this.color = color;
            this.touch = touch;
        }
    }
}
