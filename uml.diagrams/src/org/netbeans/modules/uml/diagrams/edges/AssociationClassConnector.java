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

import org.netbeans.modules.uml.drawingarea.ConnectionAnchor;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.AssociationClassWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;

/**
 *
 * @author treyspiva
 */
public class AssociationClassConnector extends AssociationConnector
{
    
    private ConnectToAssociationClass bridge = null;
    
    public AssociationClassConnector(Scene scene)
    {
        super(scene);

    }

    @Override
    public void remove()
    {
        super.remove();
        
        ConnectToAssociationClass connectTo = getBridge();
        if(connectTo != null)
        {
            Widget target = connectTo.getTargetAnchor().getRelatedWidget();
            connectTo.removeFromParent();
            if (target instanceof UMLNodeWidget)
            {
                UMLNodeWidget node = (UMLNodeWidget) target;
                node.remove();
            }
        }
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
        boolean newasocnode=false;
        if(node == null)
        {
            node = createPresentationElement();
            node.addSubject(getObject().getFirstSubject());
        }
        
        Widget nodeWidget = scene.findWidget(node);
        
        boolean createBridge = true;
        if(nodeWidget == null)
        {
            newasocnode=true;
            nodeWidget = scene.addNode(node);
        }
        else
        {
            if(getBridge() != null)
            {
                createBridge = false;
            }
        }
        
        if((createBridge == true) && (nodeWidget instanceof AssociationClassWidget))
        {
            Rectangle bounds = getBounds();
            if (bounds != null && newasocnode)//reposition only if node was created, existred should stay
                nodeWidget.setPreferredLocation(new Point(bounds.x + bounds.width / 2,
                                                  bounds.y + bounds.height * 2)); 

            ConnectToAssociationClass connectTo = new ConnectToAssociationClass(scene);
            connectTo.setSourceAnchor(new ConnectionAnchor(AssociationClassConnector.this));
            connectTo.setTargetAnchor(AnchorFactory.createRectangularAnchor(nodeWidget));

            ((AssociationClassWidget)nodeWidget).setBridgeConnection(connectTo);
            getScene().addChild(connectTo);
            bridge = connectTo;
            //addChild(connectTo);
        }
    }

    private ConnectToAssociationClass getBridge()
    {
//        ConnectToAssociationClass retVal = null;
//        
//        for(Widget child : getChildren())
//        {
//            if(child instanceof ConnectToAssociationClass)
//            {
//                retVal = (ConnectToAssociationClass)child;
//                break;
//            }
//        }
//        
//        return retVal;
        
        return bridge;
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

    @Override
    public void load(EdgeInfo edgeReader)
    {
        super.load(edgeReader);
        LabelManager manager = getLabelManager();
        if (manager != null)
        {
            manager.createInitialLabels();
        }        
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
            
            // By default I do not want to show the name label.
            hideLabel(NAME);
            
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
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            String propName = evt.getPropertyName();

            if(propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()) == true)
            {
                if(isVisible(NAME) == true)
                {
                    super.propertyChange(evt);
                }
            }
            else
            {
                super.propertyChange(evt);
            }
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
