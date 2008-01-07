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

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.view.widget.AbstractTitledWidget;

/**
 *
 * @author lukas
 */
public class AbstractTitledWidgetOperator extends WidgetOperator {

    private final AbstractTitledWidget widget;
    private ImageLabelWidgetOperator headerOp;

    AbstractTitledWidgetOperator(Widget w) {
        super(w);
        widget = (AbstractTitledWidget) w;
        headerOp = new ImageLabelWidgetOperator(widget.getChildren().get(0).getChildren().get(0));
    }

    protected ImageLabelWidgetOperator getImageLabelWidgetOperator() {
        return headerOp;
    }

    public String getLabel() {
        return getImageLabelWidgetOperator().getLabel();
    }

    public void setLabel(String label) {
        getImageLabelWidgetOperator().setLabel(label);
    }

    public String getComment() {
        return getImageLabelWidgetOperator().getComment();
    }

    public void setComment(String comment) {
        getImageLabelWidgetOperator().setComment(comment);
    }

    @Override
    public String toString() {
        return getLabel() + "[" + super.toString() + "]";
    }

    public void expand() {
        if (!widget.isExpanded()) {
            btnExpand().performAction();
        }
    }

    public void collaps() {
        if (widget.isExpanded()) {
            btnExpand().performAction();
        }
    }

    public boolean isExpanded() {
        return widget.isExpanded();
    }

    public ExpanderWidgetOperator btnExpand() {
        return findExpanderForWidget(getWidget());
    }

    private ExpanderWidgetOperator findExpanderForWidget(Widget w) {
        System.out.println("!!!Starting:");
        Widget ret = traverseWidgets(w.getScene(), this.getClass().getSimpleName());
        ret = traverseWidgets(w.getScene(), "ExpanderWidget");
        return ret != null ? new ExpanderWidgetOperator(ret) : null;
    }

    private Widget traverseWidgets(Widget from, String to) {
        Widget w = null;
        System.out.println(from.getClass().getName());
        if (from.getClass().getName().endsWith(to)) {
            return from;
        }
        for (Widget w1 : from.getChildren()) {
            if (w1.getClass().getName().endsWith(to)) {
                w = w1;
                break;
            }
            traverseWidgets(w1, to);
        }
        return w;
    }
}
