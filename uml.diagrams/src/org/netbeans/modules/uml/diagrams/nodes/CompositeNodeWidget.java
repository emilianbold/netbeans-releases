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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.actions.CompositeWidgetSelectProvider;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.util.Lookup;

/**
 *
 * @author Sheryl Su
 */
public abstract class CompositeNodeWidget extends UMLNodeWidget implements CompositeWidget
{
    public CompositeNodeWidget(Scene scene)
    {
        super(scene, true);
        addToLookup(initializeContextPalette());
        addToLookup(new CompositeWidgetSelectProvider(this));
    }

    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize(getContextPalettePath());
        return paletteModel;
    }


    public void addChildrenInBounds()
    {
        for (CompartmentWidget w : getCompartmentWidgets())
        {
            w.getContainerWidget().calculateChildren(false);//only add, do not check removal
        }
    }
 
    
    @Override
    protected void notifyFontChanged(Font font)
    {
        if (font == null || getNameWidget() == null)
        {
            return;
        }
        getNameWidget().setNameFont(font);
        for (CompartmentWidget w : getCompartmentWidgets())
        {
            w.setFont(font);            
        }
        revalidate();
    }

    @Override
    public void duplicate(boolean setBounds, Widget target)
    {
        assert target instanceof CompositeNodeWidget;

        DesignerScene targetScene = (DesignerScene) target.getScene();
        DesignerScene sourceScene = (DesignerScene) getScene();
        super.duplicate(setBounds, target);
        CompositeWidget cloned = (CompositeWidget) target;
        for (CompartmentWidget w : getCompartmentWidgets())
        {
            for (CompartmentWidget rw : cloned.getCompartmentWidgets())
            {
                if (rw.getElement().equals(w.getElement()))
                {
                    Rectangle rec = w.getBounds();
                    rw.setPreferredSize(new Dimension(rec.width, rec.height));
                    w.getContainerWidget().duplicate(setBounds, rw.getContainerWidget());
                    rw.revalidate();
                    break;
                }
            }
        }

        targetScene.validate();
        for (ConnectionWidget w : Util.getAllContainedEdges(target))
        {
            targetScene.removeEdge((IPresentationElement) targetScene.findObject(w));
        }

        for (ConnectionWidget cw : Util.getAllContainedEdges(this))
        {
            if (cw instanceof UMLEdgeWidget)
            {
                UMLEdgeWidget originalCW = (UMLEdgeWidget) cw;
                IPresentationElement sourcePE = sourceScene.getEdgeSource(originalCW.getObject());
                IPresentationElement targetPE = sourceScene.getEdgeTarget(originalCW.getObject());

                IPresentationElement newSourcePE = null;
                IPresentationElement newTargetPE = null;

                for (Object obj : Util.getAllNodeChildren(target))
                {
                    if (((IPresentationElement) obj).getFirstSubject().getXMIID().equals(sourcePE.getFirstSubject().getXMIID()))
                    {
                        newSourcePE = (IPresentationElement) obj;
                        break;
                    }
                }
                for (Object obj : Util.getAllNodeChildren(target))
                {
                    if (((IPresentationElement) obj).getFirstSubject().getXMIID().equals(targetPE.getFirstSubject().getXMIID()))
                    {
                        newTargetPE = (IPresentationElement) obj;
                        break;
                    }
                }

                IPresentationElement clonedEdgePE = Util.createNodePresentationElement();
                // Workaround for nested link. Unlike other relationships, it does not
                // have its own designated IElement, the IPresentationElement.getFirstSubject
                // returns an element at one end. Use this mechanism (multiple subjects) for 
                // DefaultDiagramEngine.createConnectionWidget() to identify the connector type
                if (((UMLEdgeWidget) cw).getWidgetID().
                        equals(UMLWidgetIDString.NESTEDLINKCONNECTIONWIDGET.toString()))
                {
                    clonedEdgePE.addSubject(sourcePE.getFirstSubject());
                    clonedEdgePE.addSubject(targetPE.getFirstSubject());
                } else
                {
                    clonedEdgePE.addSubject(originalCW.getObject().getFirstSubject());
                }

                Widget clonedEdge = targetScene.addEdge(clonedEdgePE);

                targetScene.setEdgeSource(clonedEdgePE, newSourcePE);
                targetScene.setEdgeTarget(clonedEdgePE, newTargetPE);
                Lookup lookup = clonedEdge.getLookup();
                if (lookup != null)
                {
                    LabelManager manager = lookup.lookup(LabelManager.class);
                    if (manager != null)
                    {
                        manager.createInitialLabels();
                    }
                }
                ((UMLEdgeWidget) originalCW).duplicate(clonedEdge);
            }
        }

        target.revalidate();
    }
    
    
    public abstract String getContextPalettePath();
    public abstract UMLNameWidget getNameWidget();
}
