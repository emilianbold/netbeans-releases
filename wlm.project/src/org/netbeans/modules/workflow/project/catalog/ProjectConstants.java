/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.workflow.project.catalog;

/**
 * Commond project constants.
 *
 * @author Nam Nguyen
 */
public interface ProjectConstants {
    /** 
     * The source group type for XML context.  References to XML files reside
     * in other project is only supported if that project declare the
     * following source types though SourceHelper.addTypedSourceRoot: 
     *    JavaProjectConstants.SOURCES_TYPE_JAVA,
     *    WebProjectConstants.TYPE_DOC_ROOT,
     *    WebProjectConstants.TYPE_WEB_INF,
     *    SOURCES_TYPE_XML
     */
    public static final String NBURI_SCHEME = "nb-uri";
    public static final String SOURCES_TYPE_XML = "xml";
    public static final String SOURCES_TYPE_JAVA = "java";
    public static final String TYPE_DOC_ROOT = "doc_root";
    public static final String TYPE_WEB_INF = "web_inf";
    public static final String ARTIFACT_TYPE_JAR = "jar";
    public static final String ARTIFACT_TYPE_WAR = "war";
}
