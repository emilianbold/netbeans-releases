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

package org.netbeans.modules.mobility.project;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;

/**
 *
 * @author Adam Sotona
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor.class, position=1)
public class DefaultPropertiesDescriptor implements ProjectPropertiesDescriptor {
    
    public static final String CONFIG_ACTIVE = "config.active"; //NOI18N
    public static final String USE_PREPROCESSOR = "use.preprocessor"; //NOI18N
    public static final String ALL_CONFIGURATIONS = "all.configurations"; //NOI18N
    public static final String SELECTED_CONFIGURATIONS = "selected.configurations"; //NOI18N
    public static final String CONFIG_DISPLAY_NAME = "display.name"; //NOI18N
    public static final String CONFIG_DISPLAY_COLOR = "display.color"; //NOI18N
    public static final String ABILITIES = "abilities";//NOI18N
    public static final String DEBUG_LEVEL = "debug.level"; //NOI18N
    
    public static final String LIBS_CLASSPATH = "libs.classpath"; //NOI18N
    public static final String EXTRA_CLASSPATH = "extra.classpath"; //NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; //NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    public static final String JAVAC_OPTIMIZATION = "javac.optimize"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; //NOI18N
    public static final String JAVAC_TARGET = "javac.target"; //NOI18N
    public static final String JAVAC_ENCODING = "javac.encoding"; //NOI18N
    public static final String OBFUSCATION_LEVEL = "obfuscation.level"; //NOI18N
    public static final String OBFUSCATION_CUSTOM = "obfuscation.custom"; //NOI18N
    public static final String USE_EMPTYAPIS = "use.emptyapis"; //NOI18N
    public static final String SRC_DIR = "src.dir"; //NOI18N
    public static final String BUILD_DIR = "build.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir"; //NOI18N
    public static final String DIST_JAR = "dist.jar"; //NOI18N
    public static final String DIST_JAD = "dist.jad"; //NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; //NOI18N
    public static final String BUILD_ROOT_DIR = "build.root.dir"; //NOI18N
    public static final String DIST_ROOT_DIR = "dist.root.dir"; //NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; //NOI18N
    public static final String NO_DEPENDENCIES = "no.dependencies"; //NOI18N
    public static final String PLATFORM_TRIGGER = "platform.trigger"; //NOI18N
    public static final String PLATFORM_ACTIVE = "platform.active"; //NOI18N
    public static final String PLATFORM_ACTIVE_DESCRIPTION = "platform.active.description";
    public static final String PLATFORM_DEVICE = "platform.device"; //NOI18N
    public static final String PLATFORM_CONFIGURATION = "platform.configuration"; //NOI18N
    public static final String PLATFORM_PROFILE = "platform.profile"; //NOI18N
    public static final String PLATFORM_APIS = "platform.apis"; //NOI18N
    public static final String PLATFORM_BOOTCLASSPATH = "platform.bootclasspath"; //NOI18N
    public static final String PLATFORM_TYPE = "platform.type"; //NOI18N
    public static final String RUN_METHOD = "run.method"; //NOI18N
    public static final String RUN_USE_SECURITY_DOMAIN = "run.use.security.domain"; //NOI18N
    public static final String RUN_SECURITY_DOMAIN = "run.security.domain"; //NOI18N
    public static final String MANIFEST_IS_LIBLET = "manifest.is.liblet"; //NOI18N
    public static final String MANIFEST_MIDLETS = "manifest.midlets"; //NOI18N
    public static final String MANIFEST_APIPERMISSIONS = "manifest.apipermissions"; //NOI18N
    public static final String MANIFEST_PUSHREGISTRY = "manifest.pushregistry"; //NOI18N
    public static final String MANIFEST_OTHERS = "manifest.others"; //NOI18N
    public static final String MANIFEST_MANIFEST = "manifest.manifest"; //NOI18N
    public static final String MANIFEST_JAD = "manifest.jad"; //NOI18N
    public static final String FILTER_USE_STANDARD = "filter.use.standard"; //NOI18N
    public static final String FILTER_EXCLUDE_TESTS = "filter.exclude.tests"; //NOI18N
    public static final String FILTER_EXCLUDES = "filter.excludes"; //NOI18N
    public static final String FILTER_MORE_EXCLUDES = "filter.more.excludes"; //NOI18N
    public static final String RUN_CMD_OPTIONS = "run.cmd.options"; //NOI18N
    public static final String APP_VERSION_AUTOINCREMENT = "app-version.autoincrement"; //NOI18N
    public static final String APP_VERSION_COUNTER = "deployment.counter"; //NOI18N
    public static final String APP_VERSION_NUMBER = "deployment.number"; //NOI18N
    
    public static final String JAVADOC_PRIVATE="javadoc.private"; //NOI18N
    public static final String JAVADOC_NO_TREE="javadoc.notree"; //NOI18N
    public static final String JAVADOC_USE="javadoc.use"; //NOI18N
    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar"; //NOI18N
    public static final String JAVADOC_NO_INDEX="javadoc.noindex"; //NOI18N
    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex"; //NOI18N
    public static final String JAVADOC_AUTHOR="javadoc.author"; //NOI18N
    public static final String JAVADOC_VERSION="javadoc.version"; //NOI18N
    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle"; //NOI18N
    public static final String JAVADOC_ENCODING="javadoc.encoding"; //NOI18N
    
    public static final String SIGN_ENABLED = "sign.enabled"; //NOI18N
    public static final String SIGN_KEYSTORE = "sign.keystore"; //NOI18N
    public static final String SIGN_KEYSTORE_PASSWORD = "sign.keystore.password"; //NOI18N
    public static final String SIGN_ALIAS = "sign.alias"; //NOI18N
    public static final String SIGN_ALIAS_PASSWORD = "sign.alias.password"; //NOI18N
    
    public static final String DEPLOYMENT_METHOD = "deployment.method"; //MOI18N
    public static final String DEPLOYMENT_INSTANCE = "deployment.instance"; //MOI18N
    public static final String DEPLOYMENT_OVERRIDE_JARURL = "deployment.override.jarurl"; //MOI18N
    public static final String DEPLOYMENT_JARURL = "deployment.jarurl"; //MOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String JAVADOC_PREVIEW="javadoc.preview"; //NOI18N
    
    private Reference<Set<PropertyDescriptor>> ref = new WeakReference(null);
    
    /**
     * Creates a new instance of DefaultPropertiesDescriptor
     */
    public DefaultPropertiesDescriptor() {
    }

    public Set<PropertyDescriptor> getPropertyDescriptors() {
        String FALSE = "false"; //NOI18N
        String TRUE = "true"; //NOI18N
        String EMPTY = ""; //NOI18N
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            set = new HashSet();
            set.add(new PropertyDescriptor(CONFIG_ACTIVE, false, DefaultPropertyParsers.STRING_PARSER,  EMPTY));
            set.add(new PropertyDescriptor(USE_PREPROCESSOR, true, DefaultPropertyParsers.BOOLEAN_PARSER,  TRUE));
            set.add(new PropertyDescriptor(ALL_CONFIGURATIONS, true, DefaultPropertyParsers.STRING_PARSER,  " "));//NOI18N
            set.add(new PropertyDescriptor(SELECTED_CONFIGURATIONS, false, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(CONFIG_DISPLAY_NAME, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(CONFIG_DISPLAY_COLOR, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(ABILITIES, true, DefaultPropertyParsers.ABILITIES_PARSER));
            set.add(new PropertyDescriptor(DEBUG_LEVEL, true, DefaultPropertyParsers.DEBUG_LEVEL_PARSER, "debug")); //NOI18N
            set.add(new PropertyDescriptor(DIST_DIR, true, DefaultPropertyParsers.STRING_PARSER, "dist/${config.active}")); //NOI18N
            set.add(new PropertyDescriptor(DIST_JAR, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(DIST_JAD, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(LIBS_CLASSPATH, true, DefaultPropertyParsers.PATH_PARSER, EMPTY));
            set.add(new PropertyDescriptor(EXTRA_CLASSPATH, true, DefaultPropertyParsers.PATH_PARSER, EMPTY));
            set.add(new PropertyDescriptor(JAR_COMPRESS, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));
            set.add(new PropertyDescriptor(JAVAC_DEBUG, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));
            set.add(new PropertyDescriptor(JAVAC_OPTIMIZATION, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(JAVAC_DEPRECATION, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(JAVAC_SOURCE, true, DefaultPropertyParsers.STRING_PARSER, "1.3")); //NOI18N
            set.add(new PropertyDescriptor(JAVAC_TARGET, true, DefaultPropertyParsers.STRING_PARSER, "1.1")); //NOI18N
            set.add(new PropertyDescriptor(JAVAC_ENCODING, true, DefaultPropertyParsers.STRING_PARSER, Charset.defaultCharset().name())); 
            set.add(new PropertyDescriptor(OBFUSCATION_LEVEL, true, DefaultPropertyParsers.INTEGER_PARSER, "0")); //NOI18N
            set.add(new PropertyDescriptor(OBFUSCATION_CUSTOM, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));
            set.add(new PropertyDescriptor(USE_EMPTYAPIS, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));
            set.add(new PropertyDescriptor(SRC_DIR, true, DefaultPropertyParsers.FILE_REFERENCE_PARSER, "src")); //NOI18N
            set.add(new PropertyDescriptor(BUILD_DIR, true, DefaultPropertyParsers.STRING_PARSER, "build/${config.active}")); //NOI18N
            set.add(new PropertyDescriptor(BUILD_CLASSES_EXCLUDES, true, DefaultPropertyParsers.STRING_PARSER, "**/*.java,**/*.form,**/*.class,**/.nbintdb,**/*.mvd,**/*.wsclient,**/*.vmd")); //NOI18N
            set.add(new PropertyDescriptor(NO_DEPENDENCIES, true, DefaultPropertyParsers.INVERSE_BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(PLATFORM_TRIGGER, true, DefaultPropertyParsers.STRING_PARSER, "CLDC")); //NOI18N
            set.add(new PropertyDescriptor(PLATFORM_ACTIVE, true, DefaultPropertyParsers.PLATFORM_PARSER));
            set.add(new PropertyDescriptor(PLATFORM_ACTIVE_DESCRIPTION, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(PLATFORM_DEVICE, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(PLATFORM_CONFIGURATION, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(PLATFORM_PROFILE, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(PLATFORM_APIS, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(PLATFORM_BOOTCLASSPATH, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(PLATFORM_TYPE, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(RUN_METHOD, true, DefaultPropertyParsers.STRING_PARSER, "STANDARD")); //NOI18N
            set.add(new PropertyDescriptor(RUN_USE_SECURITY_DOMAIN, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(RUN_SECURITY_DOMAIN, true, DefaultPropertyParsers.STRING_PARSER, "trusted")); //NOI18N
            set.add(new PropertyDescriptor(RUN_CMD_OPTIONS, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));
            set.add(new PropertyDescriptor(APP_VERSION_AUTOINCREMENT, false, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));
            set.add(new PropertyDescriptor(APP_VERSION_NUMBER, false, DefaultPropertyParsers.STRING_PARSER, "0.0.1")); //NOI18N
            set.add(new PropertyDescriptor(APP_VERSION_COUNTER, false, DefaultPropertyParsers.INTEGER_PARSER, "2")); //NOI18N
            set.add(new PropertyDescriptor(MANIFEST_IS_LIBLET, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(MANIFEST_MIDLETS, true, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER, EMPTY));
            set.add(new PropertyDescriptor(MANIFEST_APIPERMISSIONS, true, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER, EMPTY));
            set.add(new PropertyDescriptor(MANIFEST_PUSHREGISTRY, true, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER, EMPTY));
            set.add(new PropertyDescriptor(MANIFEST_OTHERS, true, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER));
            set.add(new PropertyDescriptor(MANIFEST_JAD, true, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER, EMPTY));
            set.add(new PropertyDescriptor(MANIFEST_MANIFEST, true, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER, EMPTY));
            set.add(new PropertyDescriptor(FILTER_USE_STANDARD, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));
            set.add(new PropertyDescriptor(FILTER_EXCLUDE_TESTS, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(FILTER_EXCLUDES, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));
            set.add(new PropertyDescriptor(FILTER_MORE_EXCLUDES, true, DefaultPropertyParsers.STRING_PARSER, "**/overview.html,**/package.html"));
        
            set.add(new PropertyDescriptor(JAVADOC_PRIVATE, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(JAVADOC_NO_TREE, true, DefaultPropertyParsers.INVERSE_BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(JAVADOC_USE, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));
            set.add(new PropertyDescriptor(JAVADOC_NO_NAVBAR, true, DefaultPropertyParsers.INVERSE_BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(JAVADOC_NO_INDEX, true, DefaultPropertyParsers.INVERSE_BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(JAVADOC_SPLIT_INDEX, true, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));
            set.add(new PropertyDescriptor(JAVADOC_AUTHOR, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(JAVADOC_VERSION, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(JAVADOC_WINDOW_TITLE, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));
            set.add(new PropertyDescriptor(JAVADOC_ENCODING, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));
        
            set.add(new PropertyDescriptor(SIGN_ENABLED, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(SIGN_KEYSTORE, true, DefaultPropertyParsers.FILE_REFERENCE_PARSER, EMPTY));
            set.add(new PropertyDescriptor(SIGN_KEYSTORE_PASSWORD, true, DefaultPropertyParsers.STRING_PARSER));
            set.add(new PropertyDescriptor(SIGN_ALIAS, true, DefaultPropertyParsers.STRING_PARSER, EMPTY));
            set.add(new PropertyDescriptor(SIGN_ALIAS_PASSWORD, true, DefaultPropertyParsers.STRING_PARSER));
        
            set.add(new PropertyDescriptor(JAVADOC_PREVIEW, false, DefaultPropertyParsers.BOOLEAN_PARSER, TRUE));
        
            set.add(new PropertyDescriptor(DEPLOYMENT_METHOD, true, DefaultPropertyParsers.DEPLOYMENT_TYPE_PARSER, "NONE")); //NOI18N
            set.add(new PropertyDescriptor(DEPLOYMENT_INSTANCE, true, DefaultPropertyParsers.STRING_PARSER, "default")); //NOI18N
            set.add(new PropertyDescriptor(DEPLOYMENT_OVERRIDE_JARURL, true, DefaultPropertyParsers.BOOLEAN_PARSER, FALSE));
            set.add(new PropertyDescriptor(DEPLOYMENT_JARURL, true, DefaultPropertyParsers.STRING_PARSER, "${dist.jar}")); //NOI18N
            ref = new WeakReference(set);
        }
        //Defensive copy - getting CMEs when creating new configurations
        return new HashSet(set);
    }
    
}
