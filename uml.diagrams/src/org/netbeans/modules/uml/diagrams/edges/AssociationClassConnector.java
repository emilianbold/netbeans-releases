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
package org.netbeans.modules.uml.diagrams.edges;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.LabelManager;

/**
 *
 * @author treyspiva
 */
public class AssociationClassConnector extends AssociationConnector
{
    

    public AssociationClassConnector(Scene scene)
    {
        super(scene);

    }

    @Override
    public void initialize(IPresentationElement element)
    {
        super.initialize(element);
    }

    public String getWidgetID()
    {
        return UMLWidgetIDString.ASSOCIATIONCLASSCONNECTORWIDGET.toString();
    }
    
    @Override
    protected LabelManager createLabelManager()
    {
        return new AssociationClassLabelManager(this);
    }
    
    public void buildBridge(IPresentationElement node)
    {
        GraphScene scene = (GraphScene)getScene();
        
        if(node == null)
        {
            node = createPresentationElement();
            node.addSubject(getObject().getFirstSubject());
        }
        
        Widget nodeWidget = scene.findWidget(node);
        
        boolean createBridge = true;
        if(nodeWidget == null)
        {
            nodeWidget = scene.addNode(node);
        }
        else
        {
            for(Widget child : getChildren())
            {
                if(child instanceof ConnectToAssociationClass)
                {
                    createBridge = false;
                    break;
                }
            }
        }
        
        if(createBridge == true)
        {
            Rectangle bounds = getBounds();

            nodeWidget.setPreferredLocation(new Point(bounds.x + bounds.width / 2,
                                                  bounds.y + bounds.height * 2)); 

            ConnectToAssociationClass connectTo = new ConnectToAssociationClass(scene);
            connectTo.setSourceAnchor(new ConnectionAnchor(AssociationClassConnector.this));
            connectTo.setTargetAnchor(AnchorFactory.createRectangularAnchor(nodeWidget));

            addChild(connectTo);
        }
    }

    private IPresentationElement createPresentationElement()
    {
        IPresentationElement retVal = null;

        ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
        if(factory != null)
        {
           Object presentationObj = factory.retrieveMetaType("NodePresentation", null);
           if (presentationObj instanceof IPresentationElement)
           {
                  retVal = (IPresentationElement)presentationObj;    
           }
        }

        return retVal;
    }
        
    public class AssociationClassLabelManager extends AssociationLabelManager
    {
        public AssociationClassLabelManager(AssociationClassConnector connector)
        {
            super(connector);
        }

        @Override
        public void createInitialLabels()
        {
            super.createInitialLabels();
            
            GraphScene scene = (GraphScene)getScene();
            
            IPresentationElement node = null;
            for(IPresentationElement element : getObject().getFirstSubject().getPresentationElements())
            {
                if(scene.isNode(element) == true)
                {
                    node = element;
                    break;
                }
            }
            
            buildBridge(node);
            
//            IPresentationElement element = createPresentationElement();
//            element.addSubject(getObject().getFirstSubject());
//            Widget widget = scene.addNode(element);
//            
//            Rectangle bounds = getBounds();
//            
//            widget.setPreferredLocation(new Point(bounds.x + bounds.width / 2,
//                                                  bounds.y + bounds.height * 2)); 
//            
//            ConnectToAssociationClass connectTo = new ConnectToAssociationClass(scene);
//            connectTo.setSourceAnchor(new ConnectionAnchor(AssociationClassConnector.this));
//            connectTo.setTargetAnchor(AnchorFactory.createRectangularAnchor(widget));
//
////            scene.getChildren().get(1).addChild(connectTo);
//            addChild(connectTo);
            
        }
    }
    
    public class ConnectToAssociationClass extends AbstractUMLConnectionWidget
    {
        public ConnectToAssociationClass(Scene scene)
        {
            super(scene);
            
            setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{5.0f, 5.0f}, 0));

            setSourceAnchorShape(AnchorShape.NONE);
            setTargetAnchorShape(AnchorShape.NONE);
            setControlPointShape(PointShape.SQUARE_FILLED_BIG);
            setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        }

        public String getWidgetID()
        {
            return UMLWidgetIDString.CONNECT_TO_ASSOCIATION_CLASS_CONNECTORWIDGET.toString();
        }
    }
}
