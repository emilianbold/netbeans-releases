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
package org.netbeans.modules.javacard.constants;

public class ProjectPropertyNames {
    //These are property names that appear in JC project.properties and build-impl.xml files -
    //careful about changing them
    public static final String PROJECT_PROP_BUILD_CLASSES_DIR = "build.classes.dir"; //NOI18N
    public static final String PROJECT_PROP_DIST_JAR = "dist.jar"; //NOI18N
    public static final String PROJECT_PROP_BUILD_SCRIPT ="buildfile"; //NOI18N
    @Deprecated
    public static final String PROJECT_PROP_CLASS_PATH = "class.path"; //NOI18N
    public static final String PROJECT_PROP_SOURCE_ENCODING = "source.encoding"; //NOI18N
    public static final String PROJECT_PROP_JAVAC_TARGET = "javac.target"; //NOI18N
    public static final String PROJECT_PROP_JAVAC_SOURCE = "javac.source"; //NOI18N
    public static final String PROJECT_PROP_JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    public static final String PROJECT_PROP_JAVAC_ADDITIONAL_ARGS = "javac.args"; //NOI18N
    public static final String PROJECT_PROP_COMPILE_ON_SAVE = "compile.on.save"; //NOI18N
    public static final String PROJECT_PROP_JAVAC_DEBUG = "javac.debug"; //NOI18N
    public static final String PROJECT_PROP_DIST_DIR = "dist.dir"; //NOI18N
    public static final String PROJECT_PROP_SRC_DIR = "src.dir"; //NOI18N
    public static final String PROJECT_PROP_BUILD_DIR = "build.dir"; //NOI18N
    public static final String PROJECT_PROP_WEB_CONTEXT_PATH = "webcontextpath"; //NOI18N
    public static final String PROJECT_PROP_LAUNCH_EXTERNAL_BROSER = "run.browser"; //NOI18N
    public static final String PROJECT_PROP_RUN_APDUTOOL = "run.apdutool"; //NOI18N
    public static final String PROJECT_PROP_ACTIVE_PLATFORM = "platform.active"; //NOI18N
    public static final String PROJECT_PROP_ACTIVE_DEVICE = "active.device"; //NOI18N
    public static final String PROJECT_PROP_USE_PAGE = "use.page"; //NOI18N
    public static final String PROJECT_PROP_USE_SERVLET = "use.servlet"; //NOI18N
    public static final String PROJECT_PROP_USE_URL = "use.url"; //NOI18N
    public static final String PROJECT_PROP_EXPLICIT_URL = "run.explicit.url"; //NOI18N
    public static final String PROJECT_PROP_WEB_INF_DIR = "web.inf.dir"; //NOI18N
    public static final String PROJECT_PROP_APPLET_INF_DIR = "applet.inf.dir"; //NOI18N
    public static final String PROJECT_PROP_SCRIPTS_DIR = "scripts.dir"; //NOI18N
    public static final String PROJECT_PROP_STATICPAGES_DIR = "staticpages.dir"; //NOI18N
    public static final String PROJECT_PROP_META_INF_DIR = "meta.inf.dir"; //NOI18N
    public static final String PROJECT_PROP_CLASSIC_PACKAGE_AID = "package.aid"; //NOI18N
    public static final String PROJECT_PROP_MAIN_SCRIPT_FILE = "run.script"; //NOI18N
    public static final String PROJECT_PROP_DISPLAYNAME = "displayname"; //NOI18N
    public static final String PROJECT_PROP_MAIN_URL = "run.servlet.url"; //NOI18N
    public static final String PROJECT_PROP_DISPLAY_NAME = "displayname"; //NOI18N
    public static final String PROJECT_PROP_SIGN_JAR = "sign.bundle"; //NOI18N
    public static final String PROJECT_PROP_KEYSTORE_PATH = "sign.keystore"; //NOI18N
    public static final String PROJECT_PROP_KEYSTORE_PASSWORD = "sign.storepass"; //NOI18N
    public static final String PROJECT_PROP_KEYSTORE_ALIAS = "sign.alias"; //NOI18N
    public static final String PROJECT_PROP_KEYSTORE_ALIAS_PASSWORD = "sign.passkey"; //NOI18N
    public static final String PROJECT_PROP_PAGE_URL = "run.page.url"; //NOI18N
    public static final String PROJECT_PROP_USER_PROPERTIES_FILE = "user.properties.file"; //NOI18N
    public static final String PROJECT_PROP_CLASSIC_USE_MY_PROXIES = "use.my.proxies"; //NOI18N
    public static final String PROJECT_PROP_PROXY_SRC_DIR = "src.proxies.dir";
    private ProjectPropertyNames(){}
}
