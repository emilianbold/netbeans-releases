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

package org.netbeans.modules.uml.drawingarea;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.UIManager;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.drawingarea.persistence.EdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeWriter;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;

/**
 * The AbstractLabelManger provides a basic implementation of the label manager.
 * This implementation will control how to display the labels on a connection. 
 * It is up to the subclasses to specify how to create and initialize the
 * labels.
 * 
 * @author treyspiva
 */
public abstract class AbstractLabelManager implements LabelManager
{
    private final static Border NON_SELECTED_BORDER = BorderFactory.createOpaqueBorder(1, 1, 1, 1);
    private final static Border SELECTED_BORDER = BorderFactory.createLineBorder(1, new Color(0xFFA400));
        
    private ConnectionWidget connector = null;
    private HashMap < String, Widget > labelMap = new HashMap < String, Widget >();
    
    /**
     * The name of stereotype labels.
     */
    public static final String STEREOTYPE = "Stereotype"; //NOI18N
    
    /**
     * The name of Name labels.
     */
    public static final String NAME = "Name"; //NOI18N
    
    
    /**
     * Creates an AbstractLabelManager and associates it to a connection 
     * widget.
     * @param widget
     */
    public AbstractLabelManager(ConnectionWidget widget)
    {
        connector = widget;
    }
    
    //////////////////////////////////////////////////////////////////
    // LabelManager Implementation
    
    public void showLabel(String name)
    {
        showLabel(name, LabelType.EDGE);
    }
    
    public void showLabel(final String name, final LabelType type)
    {
        String completeName = name + "_" + type.toString();
        Widget label = labelMap.get(completeName);
        
        if(label == null)
        {
            ObjectScene scene = (ObjectScene) getConnector().getScene();
            
            
            label = createLabel(name, type);
            if(label==null)throw new IllegalArgumentException("Unsupported label name-type combination, can't create label. name=\""+name+"\"; type=\""+type+"\".");
            ConnectionLabelWidget child = new ConnectionLabelWidget(scene, label);
            Object data = createAttachedData(name, type);
            if(data == null)
            {
                data = scene.findObject(label);  
                scene.removeObject(data);
            }
            
            WidgetAction.Chain chain = child.createActions(DesignerTools.SELECT);
            chain.addAction(scene.createSelectAction());
            chain.addAction(ActionFactory.createMoveAction());
            chain.addAction(new WidgetAction.Adapter()
            {
                public WidgetAction.State keyPressed(Widget widget,
                                                     WidgetAction.WidgetKeyEvent event)
                {
                    WidgetAction.State retVal = WidgetAction.State.REJECTED;
                    
                    if((event.getKeyCode() == KeyEvent.VK_DELETE) ||
                       (event.getKeyCode() == KeyEvent.VK_BACK_SPACE))
                    {
                        hideLabel(name, type);
                        retVal = WidgetAction.State.CONSUMED;
                    }
                    
                    return retVal;
                }
            });
            
            if(label != null)
            {
                labelMap.put(completeName, child);
                label.setVisible(true);
                connector.addChild(child);
                
                if ((connector.getScene() instanceof ObjectScene) &&
                    (data != null))
                {
                    scene.addObject(data, child);
                }

                
                if(type == LabelType.EDGE)
                {
                    connector.setConstraint(child, 
                                        getDefaultAlignment(name, type),
                                        0.5f);
                }
                else
                {
                    connector.setConstraint(child, 
                                            getDefaultAlignment(name, type),
                                            getAlignmentDistance(type));
                }
                
                scene.validate();
             }
        }
        else
        {
            label.setVisible(true);
        }
    }
    
    public void hideLabel(String name)
    {
        hideLabel(name, LabelType.EDGE);
    }
    
    public void hideLabel(String name, LabelType type)
    {
        String completeName = name + "_" + type.toString();
        Widget label = labelMap.get(completeName);
        
        if(label != null)
        {
            Scene scene = connector.getScene();
            if (scene instanceof ObjectScene)
            {
                ObjectScene objScene = (ObjectScene)scene;
                Object data = objScene.findObject(label);
                objScene.removeObject(data);
            }
            
            label.setVisible(false);
            
            connector.removeConstraint(label);
            Widget parent = label.getParentWidget();
            if(parent != null)
            {
                parent.removeChild(label);
            }
            
            labelMap.remove(completeName);
            scene.validate();
            
        }
    }

    public boolean isVisible(String name)
    {
        return isVisible(name, LabelType.EDGE);
    }
    
    public boolean isVisible(String name, LabelType type)
    {
        boolean retVal = false;
        
        String completeName = name + "_" + type.toString();
        Widget label = labelMap.get(completeName);
        
        if(label != null)
        {
            retVal = label.isVisible();
        }
        
        return retVal;
    }
    
    //////////////////////////////////////////////////////////////////
    // Helper Methods
    
    /**
     * Creates and initialize a new label.  Subclasses will usally create a
     * LabelWidget and sets the widgets text.  However that is not required, 
     * any widget can be returned.
     * 
     * @param name the name of the widget.
     * @param type the type of the widget
     * @return The widget that will be used as the label.
     */
    protected abstract Widget createLabel(String name, LabelType type);
    
    /**
     * Allows an implementation to create a data element that is to be 
     * assoicated with the label widget.  If a data element is associated with 
     * the widget it will be selectable, and the label will be highlighted when
     * it is selected. When the label is selected the property editor will 
     * display the elements properties.
     *  
     * If the a null is returned then the label will not be selectable, and the 
     * property editor will be connected to the widget.
     * 
     * @param name the name of the label.
     * @param type the type of the label.
     * @return The data object that is to be associated to the label.  Null 
     *         can be returned if no data object is to be associated to the 
     *         label.
     */
    protected abstract Object createAttachedData(String name, LabelType type);
    
    /**
     * Specifies the alignment along the edge.
     * 
     * @param name the name of the label.
     * @param type the type of the label
     * @return the alignment of the label.
     */
    protected abstract LayoutFactory.ConnectionWidgetLayoutAlignment getDefaultAlignment(String name, 
                                                                                         LabelType type);
    
    /**
     * Retreives the model element associated with the connector.
     * @return
     */
    protected IElement getModelElement()
    {
        IElement retVal = null;
        
        if(connector != null)
        {
            ObjectScene scene = (ObjectScene)connector.getScene();
            if(scene != null)
            {
                IPresentationElement presentation = (IPresentationElement) scene.findObject(connector);
                if(presentation != null)
                {
                    retVal = presentation.getFirstSubject();
                }
            }
        }
        
        return retVal;
    }

    /**
     * Retrieves a label that is of type EDGE.
     * 
     * @param name the name of the label.
     * @return the label.
     */
    protected Widget getLabel(String name)
    {
        return getLabel(name, LabelType.EDGE);
    }
    
    /**
     * Retrieves a label.
     * 
     * @param name the name of the label.
     * @param type the label type.
     * @return the label.
     */
    protected Widget getLabel(String name, LabelType type)
    {
        String completeName = name + "_" + type.toString();
        Widget label = labelMap.get(completeName);
        
        // Since I know only ConnectionLabelWidget are added to the labelMap
        // I need to get the first child, which will be the actual label
        // widget created by the derived class.
        
        return label.getChildren().get(0);
    }
    
    /**
     * Specifies where to place the label on the connection.
     * 
     * @param type the type of label.
     * @return the location of the label.
     */
    protected float getAlignmentPrecent(LabelType type)
    {
        float retVal = 0.5f;
        
        if(type == LabelType.SOURCE)
        {
            retVal = 0f;
        }
        else if(type == LabelType.TARGET)
        {
            retVal = 1f;
        }
        
        return retVal;
    }
    
    /**
     * Specifies where to place the label on the connection.
     * 
     * @param type the type of label.
     * @return the location of the label.
     */
    protected int getAlignmentDistance(LabelType type)
    {
        int retVal = 0;
        
        if(type == LabelType.SOURCE)
        {
            retVal = 5;
        }
        else if(type == LabelType.TARGET)
        {
            retVal = -5;
        }
        
        return retVal;
    }
    
    /**
     * Retreives the associated connection.
     * @return the connection
     */
    protected ConnectionWidget getConnector()
    {
        return connector;
    }
    
    /**
     * Retreives the associated scene.
     * @return the scene.
     */
    protected Scene getScene()
    {
        return connector.getScene();
    }

    /**
     * Returns the default name from the preference manager
     */ 
    public String retrieveDefaultName()
    {
        String retValue = null;

        PreferenceAccessor pPref = PreferenceAccessor.instance();
        if (pPref != null)
        {
            retValue = pPref.getDefaultElementName();
        }

        return retValue;
    }
    
    public HashMap<String, Widget> getLabelMap() 
    {
        return labelMap;
    }
    
    /**
     * The ConnectionLabelWidget provides some basic features for all label
     * widgets.  For example the connection widget will has the ability to 
     * highlight when selected.
     */
    private class ConnectionLabelWidget extends Widget 
            implements DiagramEdgeWriter, PropertyChangeListener
    {
        private Color previousColor = Color.BLACK;
        
        public ConnectionLabelWidget(Scene scene, Widget label)
        {
            super(scene);
            
            setBorder(NON_SELECTED_BORDER);
            addChild(label);
            setLayout(LayoutFactory.createVerticalFlowLayout());
        }
        
        @Override
        protected void notifyStateChanged(ObjectState previousState, ObjectState state)
        {
            if((previousState.isSelected() == false) && (state.isSelected() == true))
            {
                // Going from not selected to selected.
                // Need to remove the background and changed the font back to the 
                // standard color.
                setOpaque(true);
                previousColor = getForeground();
                
                setBackground(UIManager.getColor("List.selectionBackground"));
                setForeground(UIManager.getColor("List.selectionForeground"));

                setBorder(SELECTED_BORDER);
            }
            else if((previousState.isSelected() == true) && (state.isSelected() == false))
            {
                // Going from selected to not selected
                setOpaque(false);
                if((getParentWidget() != null) && (previousColor != null))
                {
                    setForeground(previousColor);
                }
                previousColor = null;

                setBorder(NON_SELECTED_BORDER);
            }
        }

        public void save(EdgeWriter edgeWriter)
        {
            // Since this is a wrapper widget, we will simply forward the 
            // save to the child widget.
            Widget child = getChildren().get(0);
            if (child instanceof DiagramEdgeWriter)
            {
                DiagramEdgeWriter writerWidget = (DiagramEdgeWriter) child;
                writerWidget.save(edgeWriter);
            }

        }
         
        public void propertyChange(PropertyChangeEvent event)
        {
            // Since this is a wrapper widget, we will simply forward the 
            // event to the child widget.
            Widget child = getChildren().get(0);
            if (child instanceof PropertyChangeListener)
            {
                PropertyChangeListener listener = (PropertyChangeListener) child;
                listener.propertyChange(event);
            }

        }
    }
}
