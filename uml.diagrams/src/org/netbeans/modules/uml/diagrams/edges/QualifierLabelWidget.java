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
package org.netbeans.modules.uml.diagrams.edges;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.diagrams.nodes.AttributeWidget;
import org.netbeans.modules.uml.drawingarea.util.Util;

/**
 *
 * @author treyspiva
 */
public class QualifierLabelWidget extends Widget 
        implements PropertyChangeListener
{
    private static final Border QUALIFIER_BORDER = BorderFactory.createCompositeBorder(
            BorderFactory.createLineBorder(),
            BorderFactory.createOpaqueBorder(5, 5, 5, 5));
    
    public QualifierLabelWidget(Scene scene)
    {
        super(scene);
        
        setOpaque(true);
        setBorder(QUALIFIER_BORDER);
        
        setLayout(LayoutFactory.createVerticalFlowLayout());
        
    }
    
    public void propertyChange(PropertyChangeEvent evt)
    {
        IAssociationEnd end = (IAssociationEnd) evt.getSource();
        refreshQualifiers(end);
    }
    
    protected ConnectionWidget getConnection()
    {
        ConnectionWidget retVal = null;
        
        if (getParentWidget() instanceof ConnectionWidget)
        {
            retVal = (ConnectionWidget) getParentWidget();
        }
        
        return retVal;
    }

    public void refreshQualifiers(IAssociationEnd end)
    {
        removeChildren();
        Scene scene = getScene();
        for (IAttribute attr : end.getQualifiers())
        {
            AttributeWidget attrWidget = new AttributeWidget(scene);
            attrWidget.initialize(attr);
            addChild(attrWidget);
        }
        revalidate();
    }
    
    public class QualifierSelectAction implements SelectProvider
    {   
        public boolean isAimingAllowed(Widget widget, 
                                       Point localLocation, 
                                       boolean invertSelection)
        {
            return false;
        }

        public boolean isSelectionAllowed(Widget widget, 
                                          Point localLocation, 
                                          boolean invertSelection)
        {
            return true;
        }

        public void select(Widget widget, Point localLocation, boolean invertSelection)
        {
            ConnectionWidget parent = getConnection();
            
            ObjectState state = parent.getState();
            if (state.isSelected() == false)
            {
                ObjectScene scene = (ObjectScene) parent.getScene();
                Object data = scene.findObject(parent);
                scene.userSelectionSuggested(Collections.singleton(data), 
                                             invertSelection);
                scene.setFocusedObject (data);
            }
            else
            {
                Object data = Util.findChildObject(widget, localLocation); 
                if (data != null)
                {

                    ObjectScene scene = (ObjectScene) widget.getScene();
                    scene.userSelectionSuggested(Collections.singleton(data), 
                                                 invertSelection);
                    scene.setFocusedObject (data);
                }
            }
        }

    }
    
}
