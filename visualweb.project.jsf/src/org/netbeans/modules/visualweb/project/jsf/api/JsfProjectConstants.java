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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.visualweb.project.jsf.api;

/** Constants used for JSF projects
 *
 * @author  dey
 */
public class JsfProjectConstants {

    private JsfProjectConstants () {}

    /**
     * JSF Project version produced by this release
     */
    public static final String CURRENT_JSF_PROJECT_VERSION = "2.0";  // NOI18N

    /**
     * JSF Project version prior to Thresher (imported project)
     */
    public static final String REEF_JSF_PROJECT_VERSION = "1.0";  // NOI18N

    /**
     * Document root (source folders for JSPs, HTML ...)
     */
    public static final String PATH_DOC_ROOT = "web";  // NOI18N

    /**
     * Resources folder
     */
    public static final String PATH_RESOURCES = "web/resources";  // NOI18N

    /**
     * WEB-INF folder
     */
    public static final String PATH_WEB_INF = "web/WEB-INF";  // NOI18N

    /**
     * Libraries folder
     */
    public static final String PATH_LIBRARIES = "lib";  // NOI18N

    /**
     * Source structures
     */
    public static final String WEB_DOCBASE_DIR = "web.docbase.dir"; // NOI18N
    public static final String SRC_DIR = "src.dir"; // NOI18N
    public static final String SRC_STRUCT_BLUEPRINTS = "BluePrints"; // NOI18N
    public static final String SRC_STRUCT_JAKARTA = "Jakarta"; // NOI18N

    /**
     * Page bean property
     */
    public final static String PROP_JSF_PAGEBEAN_PACKAGE = "jsf.pagebean.package"; // NOI18N

    /**
     * Context path
     */
    public static final String PROP_CONTEXT_PATH = "jsf.contextPath"; // NOI18N

    /**
     * Start Page
     */
    public static final String PROP_START_PAGE = "jsf.startPage"; // NOI18N

    /**
     * Project version
     */
    public static final String PROP_JSF_PROJECT_VERSION = "jsf.project.version"; // NOI18N

    /**
     * Project libraries
     */
    public static final String PROP_JSF_PROJECT_LIBRARIES_DIR = "jsf.project.libraries.dir"; // NOI18N

    /**
     * Current theme
     */
    public static final String PROP_CURRENT_THEME = "jsf.current.theme"; // NOI18N

    /**
     * Special No-Start-Page
     */
    public final static String NO_START_PAGE = "__no_start_page__"; // NOI18N

    /**
     * Document root root sources type (source folders for JSPs, HTML ...)
     * @see org.netbeans.api.project.Sources
     */
    public static final String TYPE_DOC_ROOT = "doc_root"; // NOI18N

    /**
     * This property is needed for template manipulation
     * BEFORE the project is created.
     * -David Botterill 5/19/2005
     */
    public static final String PROJECT_SUPPORTS_PORTLETS = "project.supports.portlets"; // NO18N

    /**
     * This property is needed for portal server deployment
     * - David Botterill 7/27/2005
     */
    public static final String PORTLET_NAME = "portlet.name"; // NO18N

    /**
     * This property is needed for portal server deployment
     * - David Botterill 8//32005
     */
    public static final String PORTLET_CONTEXTPATH = "portlet.context_path"; // NO18N

    /**
     * Filesystem boilerplate to use for creating user JSF project templates
     */
    public static final String USER_TEMPLATE_FS_BOILERPLATE = "org-netbeans-modules-visualweb-project-jsf-template/boilerplate"; // NO18N

    /**
     * Filesystem root folder for creating user JSF project templates
     */
    public static final String USER_TEMPLATE_FS_ROOT = "Templates/Project/MyTemplates"; // NO18N

    /**
     * File attribute tag for the template path
     */
    public static final String USER_TEMPLATE_DIR_TAG = "templateDir"; // NO18N

}
