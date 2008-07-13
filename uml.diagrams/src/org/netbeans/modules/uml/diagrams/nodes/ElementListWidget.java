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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color; 
import java.util.List;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.widgets.ListWidget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;

/**
 *
 * @author treyspiva
 */
public class ElementListWidget extends ListWidget implements DiagramNodeWriter, UMLWidget
{

    public ElementListWidget(Scene scene)
    {
        super(scene);
        setForeground((Color)null);
    }
    
    /**
     * Removes the widgets that represent the specified model element.
     * 
     * @param element the model element to remove.
     */
    public void removeElement(IElement element)
    {
        if(getScene() instanceof ObjectScene)
        {
            ObjectScene scene = (ObjectScene)getScene();
            List < Widget > children = getChildren();
            for(int i = children.size() - 1; i >= 0; i--)
            {
                Object obj = scene.findObject(children.get(i));
                if(obj instanceof IPresentationElement)
                {
                    IPresentationElement presentation = (IPresentationElement)obj;
                    IElement ref = presentation.getFirstSubject();
                    if(element.isSame(ref) == true)
                    {
                        removeChild(children.get(i));
                        presentation.removeSubject(element);
                    }
                }
            }
        }
    }
    
    /**
     * Retreives the number of element widgets that are contained by the list 
     * widget.
     * 
     * @return the number of element widgets.
     */
    public int getSize()
    {
        // The first widget is going to be the label widget.  Therefore do not 
        // count the first widget.
        List < Widget > children = getChildren();
        return children.size() - 1;
    }
    
    public void save(NodeWriter nodeWriter)
    {
        PersistenceUtil.populateNodeWriter(nodeWriter, this);
        nodeWriter.setHasPositionSize(false);
        PersistenceUtil.populateProperties(nodeWriter, this);
        nodeWriter.setVisible(this.isVisible());
        nodeWriter.setTypeInfo(this.getLabel());
        nodeWriter.setPresentation("");
        nodeWriter.beginGraphNode();
        nodeWriter.beginContained();
        //now loop thru all children and write them..
        saveChildren(this, nodeWriter);

        nodeWriter.endContained();
        nodeWriter.endGraphNode();
    }

    public void saveChildren(Widget widget, NodeWriter nodeWriter)
    {
        List<Widget> children = getChildren();
        if (children != null & children.size() > 0)
        {
            for (Widget child : children)
            {
                if (child instanceof FeatureWidget)
                {
                    ((FeatureWidget) child).save(nodeWriter);
                }
            }
        }
    }

    public String getWidgetID() {
        return UMLWidgetIDString.ELEMENTLISTWIDGET.toString();
    }

    public void remove() 
    {
        super.removeFromParent();
    }

    public void refresh(boolean resizetocontent)
    {
    }
}
