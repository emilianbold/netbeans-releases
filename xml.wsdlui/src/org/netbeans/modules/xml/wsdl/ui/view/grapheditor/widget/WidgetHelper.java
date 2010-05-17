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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.util.Lookup;

public class WidgetHelper {

    /**
     * Remove this widget and all children abstract widgets and updates the object-widget mappings.
     * If there are no more widgets associated with this object, then the object is removed. 
     * @param scn
     * @param widget
     */
    public static void removeWidgetFromScene(Scene scn, AbstractWidget widget) {
        if (scn instanceof ObjectScene) {
            ObjectScene scene = (ObjectScene) scn;
            List<Widget> widgetChildren = new ArrayList<Widget>(widget.getChildren());
            for (Widget w : widgetChildren) {
                if (w instanceof AbstractWidget) {
                    removeWidgetFromScene(scene, (AbstractWidget) w);
                } else {
                    removeWidgetFromScene(scene, w);
                }
            }
            WSDLComponent comp = widget.getWSDLComponent();
            if (comp != null) {
                // Add the object-widget mapping in the scene.
                List<Widget> widgets = scene.findWidgets(comp);

                if (widgets != null) {
                    // The List that comes back is immutable...
                    widgets = new ArrayList<Widget>(widgets);
                    widgets.remove(widget);
                    if (widgets.isEmpty()) {
                        scene.removeObject(comp);
                    } else {
                        scene.removeObject(comp);
                        // Remove the original mapping.
                        scene.addObject(comp, widgets.toArray(
                                new Widget[widgets.size()]));

                    }
                }
            }
        }
        widget.removeFromParent();

    }
    
    
    /**
     * Removes all widgets and all abstractwidgets contained in the widgets associated with this object from scene.
     * 
     * @param scn the Scene
     * @param obj the WSDLComponet that is to be deleted.
     */
    public static void removeObjectFromScene(Scene scn, Object obj) {
        if (!(scn instanceof ObjectScene)) return;
        
        ObjectScene scene = (ObjectScene) scn;
        if (obj instanceof WSDLComponent) {
            List<Widget> widgets = scene.findWidgets(obj);
            if (widgets != null) {
                widgets = new ArrayList<Widget>(widgets);
                for (Widget w : widgets) {
                    if (w instanceof AbstractWidget) {
                        removeWidgetFromScene(scene, (AbstractWidget) w);
                    }
                }
            }
        }
    }
    
    
    /**
     * Removes all AbstractWidget's from this widget and its children. Does not remove the passed on widget from the parent.
     * @param scn
     * @param widget
     */
    public static void removeWidgetFromScene(Scene scn, Widget widget) {
        List<Widget> widgets = new ArrayList<Widget>(widget.getChildren());
        for (Widget w : widgets) {
            if (w instanceof AbstractWidget) {
                removeWidgetFromScene(scn, (AbstractWidget) w);
            } else {
                removeWidgetFromScene(scn, w);
            }
        }
    }
    
    /**
     * Get the widget associate with the wsdlcomponent from the scene, and if visible, return the associated lookup 
     * @param comp the wsdl component
     * @param scene the scene
     * @return Lookup for the widget
     */
    public static Lookup getWidgetLookup(WSDLComponent comp, Scene scene) {
        if (!(scene instanceof ObjectScene)) return null;
        ObjectScene pScene = (ObjectScene) scene;
        
        Widget widget = pScene.findWidget(comp);
        if (widget != null) {
            if (widget.isVisible()) {
                return widget.getLookup();
            }
        }
        return Lookup.EMPTY;
    }
    
    
    /**
     * Makes the widget visible, if any of the widget in its parental hierarchy is not visible, make them visible too.
     * 
     * @param w the widget.
     */
    public static void makeWidgetVisible(Widget w) {
        if (w != null) {
            if (!w.isVisible()) {
                w.setVisible(true);
            }

            Widget parent = w.getParentWidget();
            while (parent != null) {
                if (!parent.isVisible()) {
                    parent.setVisible(true);
                }
                parent = parent.getParentWidget();
            }
        }
        
    }
}
