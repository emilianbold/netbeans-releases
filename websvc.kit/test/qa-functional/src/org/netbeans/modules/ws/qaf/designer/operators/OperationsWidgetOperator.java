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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.view.widget.OperationWidget;
import org.netbeans.modules.websvc.design.view.widget.OperationsWidget;

/**
 *
 * @author lukas
 */
public class OperationsWidgetOperator extends AbstractTitledWidgetOperator {

    private final OperationsWidget widget;
    private final ButtonWidgetOperator btnAddOperation;
    private final ButtonWidgetOperator btnRemoveOperation;
    private final ExpanderWidgetOperator btnExpand;

    OperationsWidgetOperator(Widget w) {
        super(w);
        widget = (OperationsWidget) w;
        Widget buttonsWidget = widget.getChildren().get(0).getChildren().get(2);
        btnAddOperation = new ButtonWidgetOperator(buttonsWidget.getChildren().get(0));
        btnRemoveOperation = new ButtonWidgetOperator(buttonsWidget.getChildren().get(1));
        btnExpand = new ExpanderWidgetOperator(buttonsWidget.getChildren().get(2));
    }

    public OperationWidgetOperator findOperationWidgetOperator(String operationName) {
        OperationWidgetOperator owo = null;
        for (OperationWidgetOperator o : getOperationWidgetsOperators()) {
            if (o.getLabel().equals(operationName)) {
                owo = o;
                break;
            }
        }
        return owo;
    }

    public boolean containsOperation(String operationName) {
        boolean found = false;
        for (OperationWidgetOperator o : getOperationWidgetsOperators()) {
            if (o.getLabel().equals(operationName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public ButtonWidgetOperator btnAddOperation() {
        return btnAddOperation;
    }

    public ButtonWidgetOperator btnRemoveOperation() {
        return btnRemoveOperation;
    }

    public List<OperationWidgetOperator> getOperationWidgetsOperators() {
        //dumpWidgets(this.widget);
        List<OperationWidgetOperator> widgets = new ArrayList<OperationWidgetOperator>();
        for (Widget w : widget.getChildren().get(2).getChildren()) {
            if (w instanceof OperationWidget) {
                widgets.add(new OperationWidgetOperator(w));
            }
        }
        return widgets;
    }

    public ExpanderWidgetOperator btnExpander() {
        return btnExpand;
    }
}
