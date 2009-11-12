/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.spi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 *  These are keys used in the .jcplatform (properties format) file that defines
 *  a platform.  They must match what any Ant script or task expects!
 *
 * @author Tim Boudreau
 */
public final class JavacardPlatformKeyNames {
    public static final String PLATFORM_MAJORVERSION = "javacard.java.majorVersion"; //NOI18N
    public static final String PLATFORM_MINORVERSION = "javacard.java.minorVersion"; //NOI18N
    public static final String PLATFORM_VENDOR = "javacard.vendor"; //NOI18N
    public static final String PLATFORM_NAME = "javacard.name"; //NOI18N
    public static final String PLATFORM_EEPROM_FOLDER = "javacard.device.eeprom.folder"; //NOI18N
    public static final String PLATFORM_SPECIFICATION_NAME = "JCRE"; //NOI18N
    public static final String PLATFORM_TOOLS_PATH = "javacard.toolClassPath"; //NOI18N
    public static final String PLATFORM_OFF_CARD_INSTALLER_CLASS = "javacard.offCardInstallerClass"; //NOI18N
    public static final String PLATFORM_APDU_TOOL_CLASS = "javacard.apduToolClass"; //NOI18N
    public static final String PLATFORM_PACKAGER_CLASS = "javacard.packagerClass"; //NOI18N
    //All classpaths should be : deliminated with a / file separator
    public static final String PLATFORM_BOOT_CLASSPATH = "javacard.bootclasspath"; //NOI18N
    public static final String PLATFORM_CLASSIC_BOOT_CLASSPATH = "javacard.classic.bootclasspath"; //NOI18N
    //Optional additional classpath
    public static final String PLATFORM_CLASSPATH = "javacard.classpath"; //NOI18N
    public static final String PLATFORM_JAVADOC_PATH = "javacard.javadocpath"; //NOI18N
    public static final String PLATFORM_SRC_PATH = "javacard.sourcepath"; //NOI18N
    public static final String PLATFORM_IS_REFERENCE_IMPLEMENTATION = "javacard.isRI"; //NOI18N
    //The following three are optional as part of the specification definition
    public static final String PLATFORM_PROFILE = "javacard.profile"; //NOI18N
    public static final String PLATFORM_PROFILE_MAJOR_VERSION = "javacard.profileMajorVersion"; //NOI18N
    public static final String PLATFORM_PROFILE_MINOR_VERSION = "javacard.profileMinorVersion"; //NOI18N
    //This is the file extension the build script will look for when it
    //tries to find a properties file in the devices dir for the specified platform
    public static final String PLATFORM_DEVICE_FILE_EXTENSION = "javacard.device.file.extension";//Kind of platform, e.g. RI or RC
    public static final String PLATFORM_KIND = "javacard.platform.kind"; //NOI18N
    public static final String PLATFORM_KIND_RI = "RI"; //NOI18N
    //These are keys which may be configured by the user and do not need to be
    //defined by a platform vendor, but which will be in the .jcplatform files the
    //IDE generates
    public static final String PLATFORM_DISPLAYNAME = "javacard.displayName"; //NOI18N
    public static final String PLATFORM_HOME = "javacard.home"; //NOI18N
    public static final String RI_HOME = "javacard.ri.home"; //NOI18N
    public static final String PLATFORM_EMULATOR_PATH = "javacard.emulator"; //NOI18N
    public static final String PLATFORM_ID = "javacard.instance.id"; //NOI18N
    public static final String PLATFORM_DEBUG_PROXY = "javacard.debug.proxy"; //NOI18N

    public static final String PLATFORM_TASKS_CLASSPATH = "javacard.nbtasksClassPath"; //NOI18N
    public static final String PLATFORM_TOOLS_CLASSPATH = "javacard.toolClassPath"; //NOI18N
    public static final String PLATFORM_IS_RI_WRAPPER = "javacard.wrap.ri"; //NOI18N
    public static final String PLATFORM_DEVICE_FILE_NOT_REQUIRED = "javacard.build.no.device.file"; //NOI18N
    public static final String PLATFORM_JAVACARD_SPECIFICATION_VERSION = "javacard.specification.version"; //NOI18N
    public static final String PLATFORM_JAVACARD_VERSION = "javacard.version"; //NOI18N

    /**
     * Get the list of property names that need to be absolutized
     * @param props Contents of a platform.properties file as found in its
     * raw state in an SDK
     * @return A set of strings representing property names which should be
     * converted to absolute paths for use with Ant
     */
    public static Set<String> getPathPropertyNames (Map<String,String> props) {
        String pathVal = props.get(PLATFORM_PATH_PROPERTIES);
        Set<String> result = new HashSet<String>();
        if (pathVal == null) {
            return getPathPropertyNames();
        } else {
            String[] p = pathVal.split(","); //NOI18N
            if (p != null) {
                for (String s : p) {
                    result.add(s.trim());
                }
            } else {
                result.add(pathVal.trim());
            }
        }
        return result;
    }

    /**
     * Indicates those keys in the platform.properties file in an RI-like
     * SDK which represent file paths, and should be absolutized for use by
     * Ant.
     */
    public static final String PLATFORM_PATH_PROPERTIES = "javacard.path.properties"; //NOI18N

    public static final Set<String> getRequiredProperties() {
        return new HashSet<String> (Arrays.asList(
            PLATFORM_BOOT_CLASSPATH,
            PLATFORM_CLASSIC_BOOT_CLASSPATH,
            PLATFORM_TASKS_CLASSPATH,
            PLATFORM_TOOLS_CLASSPATH,
            PLATFORM_VENDOR,
            PLATFORM_NAME,
            PLATFORM_JAVACARD_SPECIFICATION_VERSION
        ));
    }

    public static final Set<String> getPathPropertyNames() {
        return new HashSet<String> (Arrays.asList(
            PLATFORM_CLASSPATH,
            PLATFORM_BOOT_CLASSPATH,
            PLATFORM_CLASSIC_BOOT_CLASSPATH,
            PLATFORM_SRC_PATH,
            PLATFORM_JAVADOC_PATH,
            PLATFORM_EMULATOR_PATH,
            PLATFORM_TASKS_CLASSPATH,
            PLATFORM_TOOLS_CLASSPATH,
            PLATFORM_HOME,
            RI_HOME
        ));
    }

    private JavacardPlatformKeyNames() {}
}
