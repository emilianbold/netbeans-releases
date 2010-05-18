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

package org.netbeans.modules.websvc.rest.wadl.design.view.widget;

import java.io.IOException;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.modules.websvc.rest.wadl.model.*;

/**
 *
 * @author Ayub Khan
 */
public class WadlComponentWidget<T extends WadlComponent> extends AbstractTitledWidget {
    private WadlModel model;
    private T component;

    /**
     * Creates a new instance of MethodWidget
     * @param scene
     * @param operation
     */
    public WadlComponentWidget(ObjectScene scene, T component, WadlModel model) throws IOException {
        super(scene,RADIUS,RADIUS,RADIUS/2,BORDER_COLOR);
        this.component = component;
        this.model = model;
    }
    
    public WadlModel getModel() {
        return model;
    }

    public WadlComponent getWadlComponent() {
        return component;
    }
    
    public Doc createDoc() {
        Doc doc = component.getDoc();
        if (doc == null) {
            try {
                model.startTransaction();
                doc = model.getFactory().createDoc();
                component.setDoc(doc);
            } finally {
                model.endTransaction();
            }
        }
        return doc;
    }
    
    public void setDoc(String title, String text) {
        Doc doc = createDoc();
        try {
            model.startTransaction();
            doc.setTitle(title);
            doc.setContent(text);
        } finally {
            model.endTransaction();
        }
    }
    
    public DocWidget createDocWidget() throws IOException {
        return new DocWidget(getObjectScene(), this, component.getDoc(), model);
    }
    
    public void initUI() throws IOException {
        createHeader();
        createContent();
    }
    
    public void createHeader() throws IOException {
    }
    
    public void createContent() throws IOException {
        getContentWidget().setBorder(BorderFactory.createEmptyBorder(RADIUS));
        getContentWidget().addChild(createDocWidget());
    }
    
    public void findPath(WadlComponent c, StringBuffer path) {
        if(c instanceof Application) {
            createPath(path, "");
        } else if(c instanceof Resources) {
            createPath(path, ((Resources)c).getBase());
        } else if(c instanceof Resource) {
            findPath(c.getParent(), path);
            createPath(path, ((Resource) c).getPath());
        } else if(c instanceof Method) {
            findPath(c.getParent(), path);
        } else if(c instanceof ResourceType) {
            createPath(path, ((ResourceType)c).getId());
        }
    }
    
    public void createPath(StringBuffer path, String seg) {
        if(seg == null)
            seg = "";
        if(seg.startsWith("/"))
            seg = seg.substring(1);
        if(path.toString().endsWith("/"))
            path.append(seg);
        else {
            if(path.length() == 0)
                path.append(seg);
            else
                path.append("/"+seg);
        }
    }
}
