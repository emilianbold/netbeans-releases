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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.nodes.sqd;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.diagrams.Util;
import org.netbeans.modules.uml.diagrams.nodes.FeatureWidget;
import org.netbeans.modules.uml.diagrams.nodes.MovableLabelWidget;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;

/**
 *
 * @author sp153251
 */
public class ExpressionWidget extends FeatureWidget implements PropertyChangeListener {
    private IValueSpecification spec;
    private MovableLabelWidget label;
    public ExpressionWidget(Scene scene,IValueSpecification spec)
    {
        super(scene);
        this.spec=spec;
        //initialize(spec);
        
        WidgetAction action = ((ObjectScene)scene).createSelectAction();
        createActions(DesignerTools.SELECT).addAction(action);
    }

    @Override
    protected void updateUI() {

        Widget parent=Util.getParentByClass(this, InteractionOperandWidget.class);
        Widget cf=Util.getParentByClass(this, CombinedFragmentWidget.class);
        if(parent==null || cf==null)
        {
            removeChildren();
        }
        else
        {
            if(label==null)
            {
                removeChildren();
                label = new MovableLabelWidget (getScene(),parent, spec,UMLWidgetIDString.EXPRESSIONWIDGET.toString(), "Expression",0,10);
                cf.getParentWidget().addChild(label,cf.getParentWidget().getChildren().indexOf(cf)+1);
                label.setVisible(false);
                label.setFont(getFont());
                label.setForeground(getForeground());
                //
                getScene().validate();
            }
            else if(label.getParentWidget()!=cf.getParentWidget())
            {
                label.removeFromParent();
                cf.getParentWidget().addChild(label,cf.getParentWidget().getChildren().indexOf(cf)+1);
            }
            label.setLabel(formatElement());
        }
        
        setBorder(BorderFactory.createEmptyBorder(1));
    }

    @Override
    protected boolean canSelect()
    {
        return false;
    }    
    

    public void propertyChange(PropertyChangeEvent evt) {
        updateUI();
    }

    @Override
    public void refresh(boolean resizetocontent)
    {
        updateUI();
    }

    public MovableLabelWidget getLable() {
        return label;
    }

    void hideLabel() {
        if(label!=null)label.setVisible(false);
    }

    boolean isLabelVisible() {
        return label!=null && label.isVisible();
    }
    
    void setLabel(String expression) {
        setText(expression);
    }

    @Override
    protected void setText(String value) {
        if(label != null)
        {
            label.setLabel(value);
        }
    }

    @Override
     public String getText()
    {
        return label.getLabel();
    }

    void showLabel() {
        updateUI();
        label.setVisible(true);
    }
   
    
    public String getWidgetID() {
        return UMLWidgetIDString.EXPRESSIONWIDGET.toString();
    }

    @Override
    public void save(NodeWriter nodeWriter) {
        super.save(nodeWriter);
    }

}
