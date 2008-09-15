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
package org.netbeans.modules.uml.diagrams.palette.sqd;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.actions.sqd.ConnectAction;
import org.netbeans.modules.uml.diagrams.actions.sqd.LifelineSynchMessageConnectorDecorator;
import org.netbeans.modules.uml.diagrams.actions.sqd.MessagesConnectProvider;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.diagrams.engines.SequenceDiagramEngine;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteButtonModel;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;


/**
 *
 * @author psb
 */
public class MessageRapidButtonDescription implements ContextPaletteButtonModel
{
    private Image image = null;
    private String name = "";
    private String connectionType = "";
    private String defaultTargetType = "";
    private String tooltip = "";
    private String kind="";
    private int iKind;
    private RelationshipFactory factory = null;
    private ContextPaletteModel paletteModel = null;
        
    public MessageRapidButtonDescription()
    {
        
    }
    
    public MessageRapidButtonDescription(Image  image,
                                  String tooltip)
    {
        setImage(image);
        this.tooltip = tooltip;
    }
    
    public void initialize(DataObject dObj)
    {
        FileObject fo = dObj.getPrimaryFile();
        connectionType = (String)fo.getAttribute("element_type");
        defaultTargetType = (String)fo.getAttribute("default-node");
        factory = (RelationshipFactory) fo.getAttribute("factory");
        kind= (String)fo.getAttribute("kind");
        iKind=BaseElement.MK_SYNCHRONOUS;//let it be default
        if(kind.equals("asynchronous"))iKind=BaseElement.MK_ASYNCHRONOUS;
        else if(kind.equals("create"))iKind=BaseElement.MK_CREATE;
        
        // If the factory does not exist it may because we have a shadow.
        if((factory == null) && (dObj instanceof DataShadow))
        {
            DataObject original = ((DataShadow)dObj).getOriginal();
            FileObject originalFO = original.getPrimaryFile();
            factory = (RelationshipFactory) originalFO.getAttribute("factory");
        } 
    }
    
    public WidgetAction[] createActions(Scene scene)
    {
        WidgetAction[] retVal = new WidgetAction[0];
        
        if(scene instanceof DesignerScene)
        {
            DesignerScene designScene = (DesignerScene)scene;
            MessagesConnectProvider connector = new MessagesConnectProvider(//designScene, 
                                                                      getConnectionType(),
                                                                      iKind,
                                                                      getDefaultTargetType());
            connector.setRelationshipFactory(getFactory());

            LayerWidget layer = designScene.getInterractionLayer();
            
            
            WidgetAction action = new ConnectAction(new LifelineSynchMessageConnectorDecorator(),
                                                                    layer, 
                                                                    connector,iKind);
            
            retVal = new WidgetAction[] {action, new KeyExecutor()};
        }

        return retVal; 
    }
    
    public boolean isGroup()
    {
        return false;
    }
    
    public ArrayList<ContextPaletteButtonModel> getChildren()
    {
        return new ArrayList<ContextPaletteButtonModel>();
    }
    
    public void setPaletteModel(ContextPaletteModel model)
    {
        paletteModel = model;
    }
    
    public ContextPaletteModel getPaletteModel()
    {
        return paletteModel;
    }
    ///////////////////////////////////////////////////////////////
    // Data Accessors
    
    public Image getImage()
    {
        return image;
    }

    public void setImage(Image image)
    {
        this.image = image;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getConnectionType()
    {
        return connectionType;
    }
    
    public String getConnectionKind()
    {
        return kind;
    }

    public String getDefaultTargetType()
    {
        return defaultTargetType;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public void setTooltip(String tooltip)
    {
        this.tooltip = tooltip;
    }

    public RelationshipFactory getFactory()
    {
        return factory;
    }

    public void setFactory(RelationshipFactory factory)
    {
        this.factory = factory;
    }

    protected class KeyExecutor extends WidgetAction.Adapter
    {
        @Override
        public State keyPressed(Widget widget, WidgetKeyEvent event)
        {
            if(Util.isPaletteExecute(event) == true)
            {
                if (widget.getScene() instanceof DesignerScene)
                {
                    DesignerScene scene = (DesignerScene) widget.getScene();
                    List<IPresentationElement> selectedObjects = scene.getOrderedSelection();
                    if (selectedObjects.size() >= 2)
                    {
                        Widget from = null;
                        Widget to = null;
                        MessageWidget message = null;

                        for (IPresentationElement curElement : selectedObjects)
                        {
                            if ((scene.isNode(curElement) == true) &&
                                    (from == null))
                            {
                                from = scene.findWidget(curElement);
                            }
                            else if ((scene.isNode(curElement) == true) &&
                                    (to == null))
                            {
                                to = scene.findWidget(curElement);
                            }
                            else if ((scene.isEdge(curElement) == true) &&
                                    (message == null))
                            {
                                Widget testWidget = scene.findWidget(curElement);
                                if (testWidget instanceof MessageWidget)
                                {
                                    message = (MessageWidget) testWidget;
                                }
                            }
                        }


                        //if ((from != null) && (to != null))
                        {
                            if (scene.getEngine() instanceof SequenceDiagramEngine)
                            {
                                SequenceDiagramEngine engine = (SequenceDiagramEngine) scene.getEngine();
                                engine.createMessageOnSelection(from, to, message, iKind);
                            }
                        }
                    }
                }
            }

            return super.keyPressed(widget, event);
        }
    }
}
