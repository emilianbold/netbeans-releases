/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.javaee.sunresources.tool.archive;

/**
 * @author echou
 *
 */
public final class ArchiveConstants {

    // accepted archive extensions
    public static final String EAR = "EAR"; // NOI18N
    // ejb jar
    public static final String JAR = "JAR"; // NOI18N
    public static final String WAR = "WAR"; // NOI18N
    public static final String RAR = "RAR"; // NOI18N
    
    // archive types
    public enum ArchiveType { EAR, EJB, WAR, RAR, CLIENT, UNKNOWN }
    
    // DD path
    public static final String EAR_DESCRIPTOR_PATH = "META-INF/application.xml"; // NOI18N
    public static final String SUN_RESOURCES_DESCRIPTOR_PATH = "META-INF/sun-resources.xml"; // NOI18N
    public static final String EJB_DESCRIPTOR_PATH = "META-INF/ejb-jar.xml"; // NOI18N
    public static final String SUN_EJB_DESCRIPTOR_PATH = "META-INF/sun-ejb-jar.xml"; // NOI18N
    public static final String WEB_SERVICES_DESCRIPTOR_PATH = "META-INF/webservices.xml"; // NOI18N
    public static final String JBI_DESCRIPTOR_PATH = "META-INF/jbi.xml"; // NOI18N
    public static final String GRAPH_DESCRIPTOR_PATH = "META-INF/graph.xml"; // NOI18N
    
    // XML tagnames
    // application.xml
    public static final String TAG_APPLICATION = "application"; // NOI18N
    public static final String TAG_APP_MODULE = "module"; // NOI18N
    public static final String TAG_APP_WEB = "web"; // NOI18N
    public static final String TAG_APP_WEBURI = "web-uri"; // NOI18N
    public static final String TAG_APP_EJB = "ejb"; // NOI18N
    public static final String TAG_APP_RAR = "connector"; // NOI18N
    public static final String TAG_APP_CLIENT = "java"; // NOI18N
    
    
    // XML attrs
    public static final String ATTR_VERSION = "version"; // NOI18N
    
}
