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

package org.netbeans.modules.uml.drawingarea.persistence;

import java.awt.Point;
import java.util.ArrayList;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeReader;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo.NodeLabel;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;

/**
 *
 * @author sp153251
 */
public class LoadInteractionOperandsProvider implements ActionProvider{
    private UMLNodeWidget combinedfragment;
    private ArrayList<String> offsets;
    private ICombinedFragment cf;
    private ArrayList<NodeLabel> labels;

    public LoadInteractionOperandsProvider(UMLNodeWidget combinedfragment,ICombinedFragment cf,ArrayList<NodeInfo.NodeLabel> labels,ArrayList<String> offsets)
    {
        this.combinedfragment=combinedfragment;
        this.offsets=offsets;
        this.cf=cf;
        this.labels=labels;
    }
    
    public void perfomeAction() {
        DesignerScene scene=(DesignerScene) combinedfragment.getScene();
        for(int i=0;i<cf.getOperands().size();i++)
        {
            IInteractionOperand ioE=cf.getOperands().get(i);
            NodeInfo ioI=new NodeInfo();
            ioI.setModelElement(ioE);
            if(i==0)ioI.setPosition(new Point(0,0));
            else ioI.setPosition(new Point(0,Integer.parseInt(offsets.get(i-1))-10));//deviders to operands position convertion
            for(NodeLabel nL:labels)
            {
                if(nL.getElement().equals(ioE.getGuard().getSpecification()))
                {
                    ioI.addNodeLabel(nL);
                }
            }
            combinedfragment.load(ioI);
            //we have one cf per diagram, but we can have several diagrams
            Widget ioW=null;
            for(int j=0;j<ioE.getPresentationElements().size();j++)
            {
                IPresentationElement ioPE=ioE.getPresentationElements().get(j);
                ioW=scene.findWidget(ioPE);
                if(ioW!=null)break;
            }
            if(ioW!=null && ioW instanceof DiagramNodeReader)
            {
                ((DiagramNodeReader) ioW).loadDependencies(ioI);
            }
        }
    }

}
