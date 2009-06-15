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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;

/**
 * @author Petr Slechta
 */
public class AnnotationHelpers {

    private AnnotationModelHelper helper;
    private PersistentObjectManager<WebServlet> webServletsPOM;

    public AnnotationHelpers(AnnotationModelHelper helper) {
        this.helper = helper;
    }

    public PersistentObjectManager<WebServlet> getWebServletPOM() {
        if (webServletsPOM == null)
            webServletsPOM = helper.createPersistentObjectManager(new WebServletAnnotationProvider());
        return webServletsPOM;
    }

    // -------------------------------------------------------------------------
    private final class WebServletAnnotationProvider implements ObjectProvider<WebServlet> {

        public List<WebServlet> createInitialObjects() throws InterruptedException {
            final List<WebServlet> result = new ArrayList<WebServlet>();
            helper.getAnnotationScanner().findAnnotations("javax.servlet.annotation.WebServlet", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new WebServlet(helper, type));
                }
            });
            return result;
        }

        public List<WebServlet> createObjects(TypeElement type) {
            final List<WebServlet> result = new ArrayList<WebServlet>();
            if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.servlet.annotation.WebServlet")) { // NOI18N
                result.add(new WebServlet(helper, type));
            }
            return result;
        }

        public boolean modifyObjects(TypeElement type, List<WebServlet> objects) {
            assert objects.size() == 1;
            WebServlet ws = objects.get(0);
            if (!ws.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }
    }

}
