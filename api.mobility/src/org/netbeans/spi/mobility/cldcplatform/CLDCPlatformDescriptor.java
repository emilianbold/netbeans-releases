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

/*
 * CLDCPlatformDescriptor.java
 *
 */
package org.netbeans.spi.mobility.cldcplatform;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Adam
 */
public final class CLDCPlatformDescriptor {
    
    public static enum ProfileType {Configuration, Profile, Optional};
    
    public final String displayName, home, type, srcPath, docPath, preverifyCmd, runCmd, debugCmd;
    public final List<Device> devices;
    
    /** Creates a new instance of CLDCPlatformDescriptor */
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
    
    
    public static final class Device {
        
        public final String name, description;
        public final List<String> securityDomains;
        public final List<Profile> profiles;
        public final Screen screen;
        
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
    
    public static final class Profile {
        
        public final String name, version, displayName, dependencies, classPath;
        public final ProfileType type;
        public final boolean def;
        
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
    
    public static final class Screen {
        
        public final int width, height, bitDepth;
        public final boolean color, touch;
        
        public Screen(int width, int height, int bitDepth, boolean color, boolean touch) {
            this.width = width;
            this.height = height;
            this.bitDepth = bitDepth;
            this.color = color;
            this.touch = touch;
        }
    }
}
