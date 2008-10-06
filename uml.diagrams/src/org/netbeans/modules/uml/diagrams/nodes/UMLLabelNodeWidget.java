/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.view.LabelNode;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public abstract class UMLLabelNodeWidget extends UMLNodeWidget implements LabelNode
{
    private MovableLabelWidget labelWidget;
    
    public UMLLabelNodeWidget(Scene scene)
    {
        super(scene);        
    }
    
    public UMLLabelNodeWidget(Scene scene,boolean defResource)
    {
        super(scene,defResource);        
    }
    
    public MovableLabelWidget getLabelWidget()
    {
        if (labelWidget == null && getObject()!=null)
        {
            labelWidget = new MovableLabelWidget(getScene(), this, getObject().getFirstSubject(), getResourcePath(), loc("NodeLabel"));
            labelWidget.setVisible(false);
            labelWidget.setForeground(null);
            Widget parent = getParentWidget();
            if (parent != null)
                parent.addChild(parent.getChildren().indexOf(this) + 1, labelWidget);
        }
        return labelWidget;
    }
    

    public void showLabel(boolean show)
    {
        Widget label = getLabelWidget();
        label.setVisible(show);
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        IElement element = getObject().getFirstSubject();
        if (element instanceof INamedElement && getLabelWidget() != null)
        {
            getLabelWidget().setLabel(((INamedElement) element).getName());
        }

        super.propertyChange(event);
    }

    @Override
    protected void notifyForegroundChanged(Color newColor)
    {
        super.notifyForegroundChanged(newColor);
        
        Widget label = getLabelWidget();
        label.setForeground(newColor);
    }

    
    @Override
    protected void notifyAdded () 
    {
        // this is invoked when this widget or its parent gets added, only need to
        // process the case when this widget is changed, same for notifyRemoved to 
        // avoid concurrent modification to children list
        if (labelWidget == null || getParentWidget() == labelWidget.getParentWidget())
        {
            return;
        }
        labelWidget.removeFromParent();
        int index = getParentWidget().getChildren().indexOf(this);
        getParentWidget().addChild(index + 1, labelWidget);
    }
    
    @Override
    protected void notifyRemoved()
    {
        if (labelWidget != null && getParentWidget() == null)
        {           
            labelWidget.removeFromParent();
        }
    }
    
    private String loc(String key)
    {
        return NbBundle.getMessage(UMLLabelNodeWidget.class, key);
    }

    @Override
    public void load(NodeInfo nodeReader) {
        super.load(nodeReader);
        if(nodeReader.getLabels().size()==1)
        {
            showLabel(true);
        }
    }

    @Override
    protected void notifyFontChanged(Font font) {
        if(getLabelWidget()!=null){
            getLabelWidget().setFont(font);
            revalidate();//to update dependencies
       }
    }
    
    public void duplicate(boolean setBounds, Widget target)
    {
        assert target instanceof UMLLabelNodeWidget;
        super.duplicate(setBounds, target);
        MovableLabelWidget oldLabel = getLabelWidget();
        UMLLabelWidget newLabel = ((UMLLabelNodeWidget) target).getLabelWidget();
        newLabel.setVisible(oldLabel.isVisible());

        double dx = oldLabel.getCenterDx();
        double dy = oldLabel.getCenterDy();
        ((MovableLabelWidget) newLabel).updateDistance(dx, dy);

        Point p = new Point(target.convertLocalToScene(target.getPreferredLocation()).x + (int) dx,
                target.convertLocalToScene(target.getPreferredLocation()).y + (int) dy);
        p = newLabel.getParentWidget().convertSceneToLocal(p);
        newLabel.setPreferredLocation(p);
    }
}
