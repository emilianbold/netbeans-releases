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
package org.netbeans.modules.websvc.rest.support;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.projects.WebProjectRestSupport;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

/**
 *
 * @author PeterLiu
 */
public class WebXmlHelper {

    private static final String PERSISTENCE_UNIT_REF_PREFIX = "persistence/";       //NOI81N

    private static final String PERSISTENCE_UNIT_REF_TAG = "persistence-unit-ref";      //NOI18N

    private static final String PERSISTENCE_UNIT_REF_NAME_TAG = "persistence-unit-ref-name";        //NOI18N

    private static final String PERSISTENCE_UNIT_NAME_TAG = "persistence-unit-name";    //NOI18N

    private static final String PERSISTENCE_CONTEXT_REF_TAG = "persistence-context-ref";      //NOI18N

    private static final String PERSISTENCE_CONTEXT_REF_NAME_TAG = "persistence-context-ref-name";        //NOI18N


    public static void addPersistenceUnitRef(Project project, String puName) {
        FileObject fobj = getWebXml(project);

        if (fobj != null) {
            DOMHelper helper = new DOMHelper(fobj);
            String refName = PERSISTENCE_UNIT_REF_PREFIX + puName;
            Element refElement = helper.findElement(PERSISTENCE_UNIT_REF_NAME_TAG, refName);

            if (refElement != null) {
                return;
            }
            refElement = helper.createElement(PERSISTENCE_UNIT_REF_TAG);
            Element refNameElement = helper.createElement(PERSISTENCE_UNIT_REF_NAME_TAG, refName);
            Element puNameElement = helper.createElement(PERSISTENCE_UNIT_NAME_TAG, puName);

            refElement.appendChild(refNameElement);
            refElement.appendChild(puNameElement);
            helper.appendChild(refElement);
            helper.save();
        }
    }

    public static void addPersistenceContextRef(Project project, String puName) {
        FileObject fobj = getWebXml(project);

        if (fobj != null) {
            DOMHelper helper = new DOMHelper(fobj);
            String refName = PERSISTENCE_UNIT_REF_PREFIX + puName;
            Element refElement = helper.findElement(PERSISTENCE_CONTEXT_REF_NAME_TAG, refName);

            if (refElement != null) {
                return;
            }
    
            refElement = helper.createElement(PERSISTENCE_CONTEXT_REF_TAG);
            Element refNameElement = helper.createElement(PERSISTENCE_CONTEXT_REF_NAME_TAG, refName);
            Element puNameElement = helper.createElement(PERSISTENCE_UNIT_NAME_TAG, puName);

            refElement.appendChild(refNameElement);
            refElement.appendChild(puNameElement);
            helper.appendChild(refElement);
            helper.save();
        }
    }

    private static FileObject getWebXml(Project project) {
        RestSupport rs = RestUtils.getRestSupport(project);
        if (rs != null) {
            return ((WebProjectRestSupport) rs).getWebXml();
        }
        return null;
    }
}
