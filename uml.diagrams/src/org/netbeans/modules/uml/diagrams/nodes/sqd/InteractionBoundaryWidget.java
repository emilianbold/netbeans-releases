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
package org.netbeans.modules.uml.diagrams.nodes.sqd;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.visual.action.ResizeProvider.ControlPoint;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

    //
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
    

/**
 *
 * @author sp153251
 */
public class InteractionBoundaryWidget extends CombinedFragmentWidget {

    private boolean isBoundary;
    private ContainerResizeProvider resizeProvider = new ContainerResizeProvider(getResizeControlPoints());

    public InteractionBoundaryWidget(Scene scene) {
        super(scene, "ref");
    }
    
    
    public InteractionBoundaryWidget(Scene scene,String dgrName) {
        super(scene, "sd "+dgrName);
        getMainWidget().setMinimumSize(new Dimension(250,200));
//        if(childContainer!=null)
//        {
//            getMainWidget().removeChild(childContainer);
//            childContainer=null;
//        }  
    }

    @Override
    public void initializeNode(IPresentationElement presentation) {
        if (getScene() instanceof DesignerScene)
        {
            DesignerScene scene = (DesignerScene) getScene();
            //check if it's reference of interaction boundary
            INamespace ns = scene.getDiagram().getNamespace();
            if (ns instanceof IInteraction)
            {
                IInteraction thisInteraction = (IInteraction) ns;
                IInteraction initInteraction = (IInteraction) presentation.getFirstSubject();
                //
                if (thisInteraction.equals(initInteraction))
                {
                    isBoundary = true;
                    setOperator("sd " + initInteraction.getNameWithAlias());
                } else
                {
                    isBoundary = false;
                    setOperator("ref " + initInteraction.getNameWithAlias());
                }
            }
        } else
        {
            IInteraction initInteraction = (IInteraction) presentation.getFirstSubject();

            isBoundary = false;
            setOperator("ref " + initInteraction.getNameWithAlias());
        }
        getContainer();
        super.initializeNode(presentation);
    }

    public boolean isBoundary()
    {
        return isBoundary;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    protected ControlPoint[] getResizeControlPoints() {
        if(isBoundary)return new ControlPoint[]{};
        else return super.getResizeControlPoints();
    }
  
    private DefaultContextPaletteModel initializeContextPalette() {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/Interaction");
        return paletteModel;
    }
    /**
     * additioon of operand to the widget
     * 
     * 
     * @param op
     */

    @Override
    public InteractionOperandWidget addOperand(IInteractionOperand op) {
        return null;
    }

    @Override
    public InteractionOperandWidget addOperand(IInteractionOperand op, IInteractionOperand beforeOperand) {
        return null;
    }

    @Override
    public boolean removeOperand(IInteractionOperand op) {
        return false;
    }

    @Override
    public ContainerWidget getContainer() {
        Widget cont=super.getContainer();
        if(cont!=null && cont.getParentWidget()!=null)//we do not want any containment for interaction boundary, let it be simple frame
        {
            cont.setMaximumSize(new Dimension(0,0));
            cont.setPreferredSize(new Dimension(0,0));
            cont.setPreferredBounds(new Rectangle());
            cont.setVisible(false);
            getMainWidget().removeChild(cont);
        }
        return null;
    }

    @Override
    public boolean isCopyCutDeletable() {
        return !isBoundary;
    }

    @Override
    public void load(NodeInfo nodeReader) {
        super.load(nodeReader);
    }
    
    
    
}
