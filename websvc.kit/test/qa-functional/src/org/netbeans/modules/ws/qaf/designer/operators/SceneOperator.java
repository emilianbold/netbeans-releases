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

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author lukas
 */
public class SceneOperator {

    private final Scene scene;
    private final LayerWidgetOperator layer;

    SceneOperator(Scene s) {
        this.scene = s;
        this.layer = new LayerWidgetOperator(scene.getChildren().get(0));
    }

    public String getWebServiceName() {
        return layer.getLabel();
    }

    public void setOperationsExpanded(boolean expanded) {
        setExpanded(layer.getOperationsWidgetOperator(), expanded);
    }

    public void setOperationExpanded(String opName, boolean expanded) {
        setExpanded(getOp(opName), expanded);
    }

    public void setQoSExpanded(boolean expanded) {
        setExpanded(layer.getQoSWidgetOperator(), expanded);
    }

    public boolean isOperationsExpanded() {
        return isExpanded(layer.getOperationsWidgetOperator());
    }

    public boolean isOperationExpanded(String opName) {
        return isExpanded(getOp(opName));
    }

    public boolean isQoSExpanded() {
        return isExpanded(layer.getQoSWidgetOperator());
    }

    public Widget getOperation(String name) {
        return getOp(name).getWidget();
    }

    public boolean containsOperation(String name) {
        return layer.getOperationsWidgetOperator().containsOperation(name);
    }

    public int getOperationsSize() {
        return layer.getOperationsWidgetOperator().getOperationWidgetsOperators().size();
    }

    public void addOperation() {
        layer.getOperationsWidgetOperator().btnAddOperation().performAction();
    }

    public void removeOperation() {
        layer.getOperationsWidgetOperator().btnRemoveOperation().performAction();
    }

    public void selectOperation(String name) {
        getOp(name).setSelected(true);
    }

    private void setExpanded(AbstractTitledWidgetOperator w, boolean expanded) {
        if (w == null) {
            return;
        }
        if (expanded) {
            w.expand();
        } else {
            w.collaps();
        }
    }

    private boolean isExpanded(AbstractTitledWidgetOperator w) {
        if (w == null) {
            return false;
        }
        return w.isExpanded();
    }

    private OperationWidgetOperator getOp(String name) {
        return layer.getOperationsWidgetOperator().findOperationWidgetOperator(name);
    }
}
