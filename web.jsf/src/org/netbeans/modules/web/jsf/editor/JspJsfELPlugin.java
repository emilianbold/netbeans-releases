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
package org.netbeans.modules.web.jsf.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.ImplicitObject;
import org.netbeans.modules.web.el.spi.ImplicitObjectType;
import static org.netbeans.modules.web.el.spi.ImplicitObjectType.*;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = ELPlugin.class)
public class JspJsfELPlugin implements ELPlugin {

    private static final String PLUGIN_NAME = "JSP JSF EL Plugin"; //NOI18N
    private Collection<String> MIMETYPES = Arrays.asList(new String[]{"text/x-jsp", "text/x-tag"});
    private Collection<ImplicitObject> implicitObjects;

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public Collection<String> getMimeTypes() {
        return MIMETYPES;
    }

    @Override
    public Collection<ImplicitObject> getImplicitObjects(FileObject file) {
        return MIMETYPES.contains(file.getMIMEType())
                ? getImplicitObjects()
                : Collections.<ImplicitObject>emptyList();
    }

    @Override
    public List<String> getResourceBundles(FileObject file) {
        return Collections.emptyList();
    }

    static class FacesContextObject extends JsfImplicitObject {

        public FacesContextObject() {
            super("facesContext", "javax.faces.context.FacesContext", OBJECT_TYPE); //NOI18N
        }
    }

    static class ViewObject extends JsfImplicitObject {

        public ViewObject() {
            super("view", "javax.faces.component.UIViewRoot", OBJECT_TYPE); //NOI18N
        }
    }

    private synchronized Collection<ImplicitObject> getImplicitObjects() {
        if (implicitObjects == null) {
            initImplicitObjects();
        }
        return implicitObjects;
    }

    private synchronized void initImplicitObjects() {
        implicitObjects = new ArrayList<ImplicitObject>(2);
//        implicitObjects.addAll(getScopeObjects());
        implicitObjects.add(new ViewObject());
        implicitObjects.add(new FacesContextObject());
    }

//    /**
//     * @return the implicit scope objects, i.e. {@code requestScope, sessionScope} etc.
//     */
//    private static Collection<ELImplicitObject> getScopeObjects() {
//        Collection<ELImplicitObject> result = new ArrayList<ELImplicitObject>(4);
//        result.add(new ELImplicitObject("pageScope")); // NOI18N
//        result.add(new ELImplicitObject("sessionScope")); // NOI18N
//        result.add(new ELImplicitObject("applicationScope")); // NOI18N
//        result.add(new ELImplicitObject("requestScope"));
//        for (ELImplicitObject each : result) {
//            each.setType(ImplicitObjectType.SCOPE_TYPE);
//        }
//        return result;
//
//    }

    private static class JsfImplicitObject implements ImplicitObject {

        private String name, clazz;
        private ImplicitObjectType type;

        public JsfImplicitObject(String name, String clazz, ImplicitObjectType type) {
            this.name = name;
            this.clazz = clazz;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ImplicitObjectType getType() {
            return type;
        }

        @Override
        public String getClazz() {
            return clazz;
        }
    }
}
