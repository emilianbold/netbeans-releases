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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf.designer.operators;

import java.awt.Component;
import java.lang.reflect.Field;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.openide.util.Exceptions;

/**
 *
 * @author lukas
 */
public class WsDesignerOperator extends TopComponentOperator {

    private JToggleButtonOperator _tbSource;
    private JToggleButtonOperator _tbDesign;
    private SceneOperator scene;

    /** Waits for the Web Service Designer appearence and creates operator for it.
     * It is activated by defalt.
     * @param name name of form designer
     */
    public WsDesignerOperator(String name) {
        this(name, 0);
    }

    /** Waits for the Web Service Designer appearence and creates operator for it.
     * It is activated by defalt.
     * @param name name of form designer
     * @param index wait for index-th form designer
     */
    public WsDesignerOperator(String name, int index) {
        super(waitTopComponent(null, name, index, new WsDesignerSubchooser()));
    }

    /** Returns JToggleButtonOperator instance of Source button
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbSource() {
        if (_tbSource == null) {
            _tbSource = new JToggleButtonOperator(findParentTopComponent(),
                    Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.multiview.Bundle",
                    "LBL_sourceView_name"));
        }
        return _tbSource;
    }

    /** Returns JToggleButtonOperator instance of Design button
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbDesign() {
        if (_tbDesign == null) {
            _tbDesign = new JToggleButtonOperator(findParentTopComponent(),
                    Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.multiview.Bundle",
                    "LBL_designView_name"));
        }
        return _tbDesign;
    }

    /** Switches to the source editor. It pushes Source toggle button if we
     * are not in source editor already.
     */
    public void source() {
        if (!tbSource().isSelected()) {
            tbSource().push();
        }
        waitState(new ComponentChooser() {

            public boolean checkComponent(Component comp) {
                return tbSource().isSelected();
            }

            public String getDescription() {
                return "Source toggle button is selected";
            }
        });
    }

    /** Switches to the form designer. It pushes Design toggle button if we
     * are not in form designer already.
     */
    public void design() {
        if (!tbDesign().isSelected()) {
            tbDesign().push();
        }
        waitState(new ComponentChooser() {

            public boolean checkComponent(Component comp) {
                return tbDesign().isSelected();
            }

            public String getDescription() {
                return "Design toggle button is selected";
            }
        });
    }

    /** Clicks Source button and returns EditorOperator to handle form source
     * code.
     * @return EditorOperator instance
     */
    public EditorOperator editor() {
        source();
        return new EditorOperator(findParentTopComponent(), "");
    }

    public String getWebServiceName() {
        return getScene().getWebServiceName();
    }

    public void setOperationsExpanded(boolean expanded) {
        getScene().setOperationsExpanded(expanded);
    }

    public void setOperationExpanded(String opName, boolean expanded) {
        getScene().setOperationExpanded(opName, expanded);
    }

    public void setQoSExpanded(boolean expanded) {
        getScene().setQoSExpanded(expanded);
    }

    public boolean isOperationsExpanded() {
        return getScene().isOperationsExpanded();
    }

    public boolean isOperationExpanded(String opName) {
        return getScene().isOperationExpanded(opName);
    }

    public boolean isQoSExpanded() {
        return getScene().isQoSExpanded();
    }

    public Widget getOperation(String name) {
        return getScene().getOperation(name);
    }

    public boolean containsOperation(String name) {
        return getScene().containsOperation(name);
    }

    public int getOperationsSize() {
        return getScene().getOperationsSize();
    }

    public void addOperation() {
        design();
        getScene().addOperation();
    }

    public void removeOperation() {
        getScene().removeOperation();
    }

    public void selectOperation(String name) {
        getScene().selectOperation(name);
    }

    public SceneOperator getScene() {
        if (scene == null) {
            scene = new SceneOperator(getSceneFromField());
        }
        return scene;
    }

    /**
     * SubChooser to determine Web Service Designer TopComponent
     * Used in findTopComponent method.
     */
    public static final class WsDesignerSubchooser implements ComponentChooser {

        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("MultiViewElement");
        }

        public String getDescription() {
            return " org.netbeans.modules.websvc.design.multiview.*MultiViewElement";
        }
    }

    private Scene getSceneFromField() {
        Scene w = null;
        Component c = findParentTopComponent().findSubComponent(new ComponentChooser() {

            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().equals("org.netbeans.modules.websvc.design.view.DesignView");
            }

            public String getDescription() {
                return "org.netbeans.modules.websvc.design.view.DesignView";
            }
        });
        try {
            Field f = c.getClass().getDeclaredField("scene"); //NOI18N
            f.setAccessible(true);
            w = (Scene) f.get(c);
            f.setAccessible(false);
        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        }
        return w;
    }
}
