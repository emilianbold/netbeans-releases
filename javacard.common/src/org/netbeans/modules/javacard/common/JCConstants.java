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
package org.netbeans.modules.javacard.common;

/**
 * General constants used in this module
 */
public final class JCConstants {
    private JCConstants(){}
    //File extensions used by loaders inside NetBeans.  These are .properties files with
    //the following extensions, identifying a platform and a device
    /**
     * File extension <code>jcplatform</code> used by the Java Card RI integration
     * module to identify platforms it created (.properties format)
     */
    public static final String JAVACARD_PLATFORM_FILE_EXTENSION = "jcplatform"; //NOI18N
    /**
     * File extension <code>jcard</code>, used by the Java Card RI integration
     * module to identify devices belonging to a card (.properties format)
     */
    public static final String JAVACARD_DEVICE_FILE_EXTENSION = "jcard"; //NOI18N
    /**
     * File extension <code>eeprom</code>, used by the Java Card RI integration
     * module to identify files that store the non-volatile memory of a virtual
     * card that runs in the emulator
     */
    public static final String EEPROM_FILE_EXTENSION = "eprom"; //NOI18N
    //Not sure what these are but they seem to be in use
    public static final String  RUNTIME_DESCRIPTOR = "runtimedescriptor"; //NOI18N
    //Used by Updater and project generator - this is the default name used
    //for the filename of the first installed javacard platform, and the default
    //for new projects
    /**
     * File name for the default <code>.jcplatform</code>.  This is the default
     * platform in new projects
     */
    public static final String DEFAULT_JAVACARD_PLATFORM_FILE_NAME = "javacard_default"; //NOI18N
    /** Explicit name used as active.device value in newly created projects,
     * which should correspond to the file name (less the file extension) for
     * the initial device created for any platform
     **/
    public static final String TEMPLATE_DEFAULT_DEVICE_NAME = "Default Device"; //NOI18N
    public static final String GLOBAL_PROPERTIES_JCPLATFORM_DEFINITION_PREFIX = "jcplatform."; //NOI18N
    public static final String GLOBAL_PROPERTIES_DEVICE_FOLDER_PATH_KEY_SUFFIX = ".devicespath"; //NOI18N
    public static final String JAVACARD_SERVER_DEFINITION_FILE_MIME_TYPE = "application/x-javacardserver"; //NOI18N
    //Classic Applet Manifest Entries
    public static final String MANIFEST_ENTRY_CLASSIC_PACKAGE_AID = "Classic-Package-AID"; //NOI18N
    public static final String MANIFEST_ENTRY_CLASSIC_RUNTIME_DESCRIPTOR_VERSION = "Runtime-Descriptor-Version"; //NOI18N
    public static final String MANIFEST_APPLICATION_TYPE = "Application-Type"; //NOI18N
    public static final String DEPLOYMENT_XML_PATH = "nbproject/deployment.xml"; //NOI18N
    public static final String APPLET_DESCRIPTOR_PATH = "APPLET-INF/applet.xml"; //NOI18N
    public static final String MANIFEST_PATH = "META-INF/MANIFEST.MF"; //NOI18N
    public static final String JAVACARD_XML_PATH = "META-INF/javacard.xml"; //NOI18N
    public static final String WEB_DESCRIPTOR_PATH = "WEB-INF/web.xml"; //NOI18N
    public static final String SCRIPTS_DIR_PATH = "scripts/"; //NOI18N
    public static final String HTML_FILE_PATH = "html/"; //NOI18N
    public static final String GLOBAL_BUILD_PROPERTIES_RI_PROPERTIES_PATH_KEY = "javacard.ri.properties.path"; //NOI18N
    public static final String PLATFORM_FILE_DEVICE_PROTOTYPE_PREFIX = "prototype."; //NOI18N
    public static final String PLATFORM_PROPERTIES_PATH="platform.properties"; //NOI18N

}
