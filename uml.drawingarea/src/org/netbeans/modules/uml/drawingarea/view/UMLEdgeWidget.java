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
package org.netbeans.modules.uml.drawingarea.view;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.AnchorShapeFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeReader;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.persistence.EdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jyothi
 */
public abstract class UMLEdgeWidget extends ConnectionWidget implements DiagramEdgeWriter, DiagramEdgeReader, UMLWidget
{

    DesignerScene scene;
    protected static final String STEREOTYPE = "Stereotype"; //NOI18N
    protected static final String NAME = "Name"; //NOI18N
    protected static final String TAGGEDVALUE = "TaggedValue"; //NOI18N
    protected static final String OPERATION = "Operation"; //NOI18N
    public final static AnchorShape ARROW_END = AnchorShapeFactory.createArrowAnchorShape(50, 10);

    public UMLEdgeWidget(Scene scene)
    {
        super(scene);
        this.scene = (DesignerScene) scene;
    }

    protected LabelManager getLabelManager()
    {
        LabelManager manager = null;
        Lookup lookup = this.getLookup();
        if (lookup != null)
        {
            manager = lookup.lookup(LabelManager.class);
            if (manager != null)
            {
                return manager;
            }
        }
        return manager;
    }

    public void save(EdgeWriter edgeWriter)
    {
        IElement modElt = PersistenceUtil.getModelElement(this);
        if (modElt == null)
            return;
        else if (modElt != null)
            edgeWriter.setElementType(modElt.getElementType());
        IPresentationElement pElt = PersistenceUtil.getPresentationElement(this);
        if (pElt != null)
            edgeWriter.setPresentation(pElt.getFirstSubjectsType());
        
        edgeWriter.setLocation(this.getLocation());
        List<Point> controlPts = ((ConnectionWidget) this).getControlPoints();
        edgeWriter.setWayPoints(controlPts);
        edgeWriter.setPEID(PersistenceUtil.getPEID(this));
        edgeWriter.setMEID(PersistenceUtil.getMEID(this));
        
        edgeWriter.setSrcAnchorID(PersistenceUtil.findAnchor(this.getSourceAnchor()));
        edgeWriter.setTargetAnchorID(PersistenceUtil.findAnchor(this.getTargetAnchor()));

        edgeWriter.beginGraphEdge();
        LabelManager manager = getLabelManager();
        if (manager != null)
        {
            HashMap<String, Widget> labMap = manager.getLabelMap();
            System.out.println(" labMap = " + labMap);
            edgeWriter.beginContained();
            for (String child : labMap.keySet())
            {
                Widget childWidget = labMap.get(child);
                String childType = child.substring(0, child.indexOf('_'));
                if (childWidget instanceof DiagramEdgeWriter)
                {
                    //begin contained  
                    edgeWriter.setTypeInfo(childType);
                    ((DiagramEdgeWriter) childWidget).save(edgeWriter);
                }
                else
                {
                    System.out.println(" not a label... ");
                }
            }
            edgeWriter.endContained();
        }
        //write anchor
        edgeWriter.writeEdgeAnchors();

        edgeWriter.endGraphEdge();
    }

    public void load(EdgeInfo edgeReader)
    {
        //scene.setEdgeSource(pE, srcTargetPEs.get("Source"));
        if (scene != null)
        {
            scene.setEdgeSource(PersistenceUtil.getPresentationElement(this), edgeReader.getSourcePE());
            scene.setEdgeTarget(PersistenceUtil.getPresentationElement(this), edgeReader.getTargetPE());
        }
        LabelManager manager = getLabelManager();
        if (manager != null)
        {
            List<EdgeInfo.EdgeLabel> edgeLabels = edgeReader.getLabels();
            
            for (Iterator<EdgeInfo.EdgeLabel> it = edgeLabels.iterator(); it.hasNext();)
            {
                EdgeInfo.EdgeLabel edgeLabel = it.next();
                manager.showLabel(edgeLabel.getLabel());
//                if (edgeLabel.getLabel().equalsIgnoreCase(NAME))
//                {
//                    manager.showLabel(NAME);
//                }
//                else if (edgeLabel.getLabel().equalsIgnoreCase(STEREOTYPE))
//                {
//                    manager.showLabel(STEREOTYPE);
//                }
//                else if (edgeLabel.getLabel().equalsIgnoreCase(TAGGEDVALUE))
//                {
//                    manager.showLabel(TAGGEDVALUE);
//                }
//                else if (edgeLabel.getLabel().equalsIgnoreCase("GuardCondition"))
//                {
//                    manager.showLabel("GuardCondition");
//                }
            }
        }
    }

    public void duplicate(Widget copy)
    {
        // TODO: 
        if (copy instanceof ConnectionWidget)
        {
            ((ConnectionWidget) copy).setControlPointsCursor(this.getControlPointsCursor());
            ((ConnectionWidget) copy).setControlPoints(this.getControlPoints(), true);
        }
    }

    protected IPresentationElement getObject()
    {
        return (IPresentationElement) scene.findObject(this);
    }

    public void remove()
    {
        scene.removeEdge(getObject());
    }

    public void refresh()
    {
        IPresentationElement pe = getObject();
        if (pe == null || pe.getFirstSubject() == null)
        {
            remove();
            scene.validate();
        }
    }
}
