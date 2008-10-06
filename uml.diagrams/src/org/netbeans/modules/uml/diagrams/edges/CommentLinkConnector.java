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
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import javax.swing.Action;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;

/**
 *
 * @author treyspiva
 */
public class CommentLinkConnector extends AbstractUMLConnectionWidget
{
    public CommentLinkConnector(Scene scene)
    {
        super(scene);
        
        setForeground(Color.BLACK);
        setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{6.0f, 3.0f}, 0));

        setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        setEndPointShape(PointShape.SQUARE_FILLED_BIG);
    }
    
    public String getWidgetID() {
        return UMLWidgetIDString.COMMENTLINKCONNECTORWIDGET.toString();
    }
    
    @Override
    protected LabelManager createLabelManager()
    {
        return new CommentLinkLableManager(this);
    }
    
    public class CommentLinkLableManager extends BasicUMLLabelManager
    {
        public CommentLinkLableManager(CommentLinkConnector widget)
        {
            super(widget);
        }
        
        @Override
        public void createInitialLabels()
        {
            super.createInitialLabels();

            // By default I do not want to show the name label.
            hideLabel(NAME);
        }

        @Override
        public Action[] getContextActions(LabelType type)
        {
            Action[] superActions = super.getContextActions(type);

            // The first action is always the name action.  Therefore remove 
            // that action.
            Action[] retVal = new Action[superActions.length - 1];
            for(int index = 0; index < retVal.length; index++)
            {
                retVal[index] = superActions[index + 1];
            }

            return retVal;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            String propName = evt.getPropertyName();

            if(propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()) == false)
            {
                super.propertyChange(evt);
            }
        }
    };
}
