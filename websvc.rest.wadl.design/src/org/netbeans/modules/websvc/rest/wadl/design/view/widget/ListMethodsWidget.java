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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.*;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.util.Exceptions;

/**
 * WebService Designer
 *
 * @author Ayub Khan
 */
public class ListMethodsWidget extends Widget implements PropertyChangeListener {
    public static final Object messageLayerKey = new Object();

    private WadlModel model;
    /** Manages the state of the widgets and corresponding objects. */
    private ObjectScene scene;
    
    private static Map<Method, MethodWidget> map = new WeakHashMap<Method, MethodWidget>();
    
    /**
     * Creates a new instance of GraphView.
     * @param service
     * @param implementationClass
     */
    public ListMethodsWidget(Scene scene, int size, WadlModel model) {
        super(scene);
        this.model = model;
        initUI();
    }
    
    private void initUI() {
        setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, AbstractTitledWidget.RADIUS/2));
        List<Method> rList = new ArrayList<Method>();
        Collection<Resources> resources = model.getApplication().getResources();
        if(!resources.isEmpty()) {
            getMethodRecursively(model.getApplication(), rList);
        }
        for(Method m:model.getApplication().getMethod()) {
                rList.add(m);
        }
        for(ResourceType rType:model.getApplication().getResourceType()) {
            for(Method child:rType.getMethod()) {
                    rList.add(child);
            }
        }
        for(Method m:rList) {
            MethodWidget methodWidget = null;
            try {
                methodWidget = new MethodWidget((ObjectScene) getScene(), this, m, model);
                addChild(methodWidget);
                map.put(m, methodWidget);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void getResourceRecursively(Application application, List<Resource> rList) {
        for(Resource child:application.getResources().iterator().next().getResource()) {
                rList.add(child);
            getResourceRecursively(child, rList);
        }
    }
    
    private void getResourceRecursively(Resource resource, List<Resource> rList) {
        for(Resource child:resource.getResource()) {
                rList.add(child);
            getResourceRecursively(child, rList);
        }
    }
    
    private void getMethodRecursively(Application application, List<Method> rList) {
        for(Resource child:application.getResources().iterator().next().getResource()) {
            getMethodRecursively(child, rList);
        }
    }
    
    private void getMethodRecursively(Resource resource, List<Method> rList) {
        for(Method child:resource.getMethod()) {
                rList.add(child);
        }
        for(Resource child:resource.getResource()) {
            getMethodRecursively(child, rList);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(AddMethodAction.ADD_METHOD)) {
            try {
                Method m = (Method) evt.getNewValue();
                MethodWidget methodWidget = new MethodWidget((ObjectScene)getScene(), this, m, model);
                addChild(methodWidget);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if(evt.getPropertyName().equals(AddResourceAction.ADD_RESOURCE)) {
            try {
                Resource r = (Resource) evt.getNewValue();
                ResourceWidget resourceWidget = new ResourceWidget((ObjectScene)getScene(), this, r, model);
                addChild(resourceWidget);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if(evt.getPropertyName().equals(RemoveMethodAction.REMOVE_METHODS)) {
            List<Widget> childs = getChildren();
            Set<Method> methods = (Set<Method>) evt.getOldValue();
            List<Widget> removeList = new ArrayList<Widget>();
            for(Method m:methods){
                for(Widget w:childs) {
                    if(w instanceof MethodWidget && 
                            ((MethodWidget)w).getMethodName().equals(m.getName()) &&
                            m.getId() != null && ((MethodWidget)w).getMethod().getId().equals(m.getId()))
                        removeList.add(w);
                }
            }
            removeChildren(removeList);
        } else if(evt.getPropertyName().equals(RemoveResourceAction.REMOVE_RESOURCES)) {
            List<Widget> childs = getChildren();
            Set<Resource> resources = (Set<Resource>) evt.getOldValue();
            List<Widget> removeList = new ArrayList<Widget>();
            for(Resource r:resources){
                for(Widget w:childs) {
                    if(w instanceof ResourceWidget && ((ResourceWidget)w).getPath().equals(r.getPath()))
                        removeList.add(w);
                }
            }
            removeChildren(removeList);
        }
        getScene().validate();
    }
}
