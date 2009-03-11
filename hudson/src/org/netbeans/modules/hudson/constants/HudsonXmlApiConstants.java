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

package org.netbeans.modules.hudson.constants;

/**
 * Constants provider for Hudson XML_API
 *
 * @author Michal Mocnak
 */
public class HudsonXmlApiConstants {

    private HudsonXmlApiConstants() {}
    
    // XML API Suffix
    public static final String XML_API_URL ="api/xml";
    
    // Hudson Instance Element
    public static final String XML_API_VIEW_ELEMENT = "view";
    public static final String XML_API_JOB_ELEMENT = "job";
    public static final String XML_API_NAME_ELEMENT = "name";
    public static final String XML_API_URL_ELEMENT = "url";
    public static final String XML_API_COLOR_ELEMENT = "color";
    
    // Hudson Job Elements
    public static final String XML_API_DESCRIPTION_ELEMENT = "description";
    public static final String XML_API_DISPLAY_NAME_ELEMENT = "displayName";
    public static final String XML_API_BUILDABLE_ELEMENT = "buildable";
    public static final String XML_API_INQUEUE_ELEMENT = "inQueue";
    public static final String XML_API_LAST_BUILD_ELEMENT = "lastBuild";
    public static final String XML_API_LAST_STABLE_BUILD_ELEMENT = "lastStableBuild";
    public static final String XML_API_LAST_SUCCESSFUL_BUILD_ELEMENT = "lastSuccessfulBuild";
    public static final String XML_API_LAST_FAILED_BUILD_ELEMENT = "lastFailedBuild";
    public static final String XML_API_LAST_COMPLETED_BUILD_ELEMENT = "lastCompletedBuild";

}
