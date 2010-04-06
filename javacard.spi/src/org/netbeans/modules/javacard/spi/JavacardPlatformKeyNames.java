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
 * The constants in this file are a list of keys which are found in properties
 * files.  A JavacardPlatform in NetBeans is stored as a properties file consisting
 * of key/value pairs.  This class defines the official properties which may be
 * found in such a file.
 * <p>
 * The Java Card Reference Implementation and other SDKs provide a template
 * file with some of these keys and values, as a file <code>platform.properties</code>,
 * underneath their root directory (the presence of a <code>platform.properties</code>
 * file is also how the IDE identifies a directory that might be a Java Card platform
 * in Tools &gt; Java Platforms, though additional mechanisms may be plugged in).
 * Such a template file will contain <i>relative</i> paths for classpaths and
 * similar, using some of the keys found as constants in this class.  When the IDE
 * installs such an SDK as a Java Card Platform, it will make its own internal
 * copy of this file, additionally translating paths to platform-specific absolute
 * paths and adding properties such as the user-entered display name for the
 * platform, its unique ID within NetBeans, a path for eeprom files if necessary,
 * etc.
 *  <p/>
 *  These are keys used in the .jcplatform (properties format) file that defines
 *  a platform, and the platform.properties file under the root of a Java Card
 *  SDK which is read and copied when that SDK is installed as a JavacardPlatform.
 *  They must match what any Ant script or task expects.
 * <p>
 *  All path and path-like properties in an SDK's platform.properties file
 *  should be relative paths using unix-style path-delimiters, e.g.
 *  <code>lib/foo.jar:lib/bar.jar</code>.  The IDE will translate these into
 *  fully qualified paths usable by Ant when it copies the platform.properties
 *  file into its own configuration on installation.
 *
 * @author Tim Boudreau
 */
public final class JavacardPlatformKeyNames {
    /**
     * Major version of <i>java</i> (not Java Card)
     * that this Java Card platform supports.
     * Will most likely always be 1.
     */
    public static final String PLATFORM_MAJORVERSION = "javacard.java.majorVersion"; //NOI18N
    /**
     * Minor version of <i>java</i> that this Java Card platform supports.
     * Composed with the value of PLATFORM_MAJORVERSION to determine what source
     * Java levels a project is allowed to use on a platform, i.e. 1.5, 1.6, etc.
     */
    public static final String PLATFORM_MINORVERSION = "javacard.java.minorVersion"; //NOI18N
    /**
     * Name of the vendor of the SDK
     */
    public static final String PLATFORM_VENDOR = "javacard.vendor"; //NOI18N
    /**
     * Official name of the SDK
     */
    public static final String PLATFORM_NAME = "javacard.name"; //NOI18N
    /**
     * Fully qualified path on disk to where the IDE stores .eprom files representing
     * the memory contents of virtual files.
     * <p/>
     * <i>The IDE will supply this value when it installs a platform;  SDKs
     * should not specify it in their platform.properties</i>
     */
    public static final String PLATFORM_EEPROM_FOLDER = "javacard.device.eeprom.folder"; //NOI18N
    /**
     * Constant for the platform specification of Java Card - JCRE.  Not
     * needed in SDK platform.properties files.
     */
    public static final String PLATFORM_SPECIFICATION_NAME = "JCRE"; //NOI18N
    /**
     * Key for the classpath for Ant tasks provided by this SDK.  This should
     * be specified in an SDK's platform.properties,
     */
    public static final String PLATFORM_TOOLS_PATH = "javacard.toolClassPath"; //NOI18N
    /**
     * Key for class which the Ant script will invoke to install a bundle (JAR) onto
     * a card.  Must be in one of the JARs on the tools classpath.
     */
    public static final String PLATFORM_OFF_CARD_INSTALLER_CLASS = "javacard.offCardInstallerClass"; //NOI18N
    /**
     * Key for class wich the Ant script will invoke to run an APDU script against
     * a card.  Must be in one of the JARs on the tools classpath.
     */
    public static final String PLATFORM_APDU_TOOL_CLASS = "javacard.apduToolClass"; //NOI18N
    /**
     * Key for class which the Ant script will invoke to package a Java Card bundle (JAR) for deployment to
     * a card.  Must be in one of the JARs on the tools classpath.
     */
    public static final String PLATFORM_PACKAGER_CLASS = "javacard.packagerClass"; //NOI18N
    /**
     * Key for the boot classpath for <i>extended</i> (Java Card 3) applets, libraries
     * and web applications.  The value of this property will be the boot classpath
     * such projects use for code-completion, etc.
     */
    public static final String PLATFORM_BOOT_CLASSPATH = "javacard.bootclasspath"; //NOI18N
    /**
     * Key for the boot classpath for <i>classic</i> (Java Card 2 and older) applets, libraries
     * and web applications.  The value of this property will be the boot classpath
     * projects use for code-completion, etc.
     */
    public static final String PLATFORM_CLASSIC_BOOT_CLASSPATH = "javacard.classic.bootclasspath"; //NOI18N
    /**
     * Key for optional additional classpath JARs which should be available in the IDE's
     * code completion for <i>all</i> (classic and extended) Java Card projects.
     * The value of this is an appropriate place to include vendor-specific APIs.
     */
    public static final String PLATFORM_CLASSPATH = "javacard.classpath"; //NOI18N
    /**
     * Path to Javadoc for this Java Card SDK.  May be omitted by SDK platform.properties if
     * not present.
     */
    public static final String PLATFORM_JAVADOC_PATH = "javacard.javadocpath"; //NOI18N
    /**
     * Path to Javadoc for this Java Card SDK.  May be omitted by SDK platform.properties if
     * not present.
     */
    public static final String PLATFORM_SRC_PATH = "javacard.sourcepath"; //NOI18N
    @Deprecated
    public static final String PLATFORM_PROFILE = "javacard.profile"; //NOI18N
    @Deprecated
    public static final String PLATFORM_PROFILE_MAJOR_VERSION = "javacard.profileMajorVersion"; //NOI18N
    @Deprecated
    public static final String PLATFORM_PROFILE_MINOR_VERSION = "javacard.profileMinorVersion"; //NOI18N
    /**
     * Key for the file extension the IDE and Ant scripts will use to locate a
     * file defining a Card.  Probably only relevant for the Java Card RI, where
     * virtual cards are defined in properties files.
     */
    public static final String PLATFORM_DEVICE_FILE_EXTENSION = "javacard.device.file.extension";//Kind of platform, e.g. RI or RC
    /**
     * Key for the <i>kind</i> of this platform.  This is a proprietary value
     * specific to a particular card/SDK/vendor, and is used by the IDE to
     * look up objects registered by an SDK vendor's modules.  For example,
     * the ACME card vendor might write a module that supports deployment to
     * their card in NetBeans.  They would specify, say, "acme" for the <i>kind</i>
     * of their SDK.
     * <p/>
     * When the IDE encounters an ACME card, it will, for example, look in
     * org-netbeans-modules-javacard-spi/kinds/acme in the system filesystem
     * for an instance of <code>org.netbeans.modules.javacard.ri.spi.CardsFactory</code>,
     * which is an object registered by ACME's NetBeans module, which knows how
     * to query the ACME Java Card Platform to find out what cards are attached,
     * so that the IDE can provide a user interface for them.
     */
    public static final String PLATFORM_KIND = "javacard.platform.kind"; //NOI18N
    /**
     * Constant for the Java Card Reference Implementation's platform kind,
     * <code>RI</code>.
     */
    public static final String PLATFORM_KIND_RI = "RI"; //NOI18N
    //These are keys which may be configured by the user and do not need to be
    //defined by a platform vendor, but which will be in the .jcplatform files the
    //IDE generates
    /**
     * Key for display name.  Not needed in SDK platform.properties files.  On platform
     * set-up, this will be set to the user-specified name they want to see for
     * this platform.
     */
    public static final String PLATFORM_DISPLAYNAME = "javacard.displayName"; //NOI18N
    /**
     * Key for the root directory of an SDK installation.  This value will be
     * filled in by the IDE when installing a platform.
     */
    public static final String PLATFORM_HOME = "javacard.home"; //NOI18N
    /**
     * Key for the home directory of the Java Card Reference implementation.
     * Not needed in SDK platform.properties files.  This value is used in the
     * case that this SDK only provides some of the libraries or tools needed
     * to deploy, and relies on the presence of a copy of the Reference Implementation
     * to provide the rest.
     */
    public static final String PLATFORM_RI_HOME = "javacard.ri.home"; //NOI18N

    /**
     * Version of the <i>Reference Implementation</i> - used to determine if the
     * Ant tasks JAR needs to be upgraded only for the Java Card RI 3.0.2.
     */
    public static final String PLATFORM_RI_VERSION = "javacard.referenceimplementation.version";
    /**
     * Path on disk to the Java Card Reference implementation's definition properties
     * file.  Not needed in SDK platform.properties files.  The IDE will set this
     * value in the case that the SDK in question depends on the presence of
     * the Java Card Reference Implementation for deployment.
     */
    public static final String PLATFORM_RI_PROPERTIES_PATH = "javacard.ri.properties.path"; //NOI18N
    /**
     * Path on disk to the Java Card emulator (if any).  Supplied by SDK
     * project.properties files if relevant.  If present, an attempt will be
     * made on installation to run the emulator to verify that the platform is
     * valid.
     */
    public static final String PLATFORM_EMULATOR_PATH = "javacard.emulator"; //NOI18N
    /**
     * Unique ID within the IDE for this Java Card platform.  Should always be
     * set to the <i>file</i> (not display) name of the properties file that
     * represents the Java Card Platform. Not needed in SDK platform.properties files.
     */
    public static final String PLATFORM_ID = "javacard.instance.id"; //NOI18N
    public static final String PLATFORM_DEBUG_PROXY = "javacard.debug.proxy"; //NOI18N
    public static final String PLATFORM_DEBUG_PROXY_CLASSPATH = "javacard.debug.proxy.classpath"; //NOI18N

    public static final String PLATFORM_TASKS_CLASSPATH = "javacard.nbtasksClassPath"; //NOI18N
    public static final String PLATFORM_TOOLS_CLASSPATH = "javacard.toolClassPath"; //NOI18N
    public static final String PLATFORM_IS_RI_WRAPPER = "javacard.wrap.ri"; //NOI18N
    public static final String PLATFORM_DEVICE_FILE_NOT_REQUIRED = "javacard.build.no.device.file"; //NOI18N
    public static final String PLATFORM_JAVACARD_VERSION = "javacard.version"; //NOI18N
    public static final String PLATFORM_SUPPORTED_PROJECT_KINDS = "javacard.supported.project.kinds"; //NOI18N

    public static final String PLATFORM_302_ANT_TASKS_UPDATED = "javacard.platform.302.ant.tasks.updated"; //NOI18N

    public static final String PLATFORM_PROCESSOR_CLASSIC_CLASSPATH = "javacard.platform.classic.processorpath"; //NOI18N
    public static final String PLATFORM_PROCESSOR_EXT_CLASSPATH = "javacard.platform.processorpath"; //NOI18N
    /**
     * Get the list of property names representing unix-style, : delimited 
     * relative paths underneath the platform root, which need to be absolutized.
     * The default is the same as the return value of getPathPropertyNames().
     * A platform.properties can override this by providing a comma delimited
     * list in the property <code>javacard.path.properties</code>.
     *
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
            PLATFORM_JAVACARD_VERSION,
            PLATFORM_KIND
        ));
    }

    /**
     * Get the default list of keys in a platform.properties which are defined
     * as unix-style relative paths, and should be translated on platform
     * installation into platform-specific absolute paths.
     *
     * @return A set of strings
     */
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
            PLATFORM_RI_HOME,
            PLATFORM_DEBUG_PROXY,
            PLATFORM_DEBUG_PROXY_CLASSPATH,
            PLATFORM_PROCESSOR_CLASSIC_CLASSPATH,
            PLATFORM_PROCESSOR_EXT_CLASSPATH
        ));
    }

    private JavacardPlatformKeyNames() {}
}