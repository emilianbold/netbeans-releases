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

package org.netbeans.modules.uml.diagrams.nodes.state;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.State;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.diagrams.UMLRelationshipDiscovery;
import org.netbeans.modules.uml.diagrams.border.UMLRoundedBorder;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;

/**
 *
 * @author Sheryl Su
 */
public class CompositeStateWidget extends Widget 
{
    private State state;
    private Scene scene;
    private Widget bodyWidget;
    private Widget tabWidget;
    private boolean horizontal = true;
    private LinkedHashSet<RegionWidget> regionWidgets = new LinkedHashSet<RegionWidget>();
    private UMLNameWidget nameWidget;
    // variable to hold all region contained elements for discovering relationships after the all regions
    // are initialized
    private ArrayList<IElement> elements = new ArrayList<IElement>(); 

    public CompositeStateWidget(Scene scene, State state)
    {
        super(scene);
        setForeground(null);
        setBackground(null);
        this.state = state;
        this.scene = scene;
        init();
    }
    
    private void init()
    {
        nameWidget = new UMLNameWidget(scene, false, getWidgetID());
        nameWidget.initialize(state);
        tabWidget = new LayerWidget(scene);
        tabWidget.setLayout(LayoutFactory.createHorizontalFlowLayout());
        Widget tabbg = new TabWidget(
                scene, getWidgetID() + "." + UMLNodeWidget.DEFAULT, "Default");

        tabbg.addChild(nameWidget);
        tabWidget.addChild(tabbg);
        bodyWidget = new BackgroundWidget(
                scene, getWidgetID() + "." + UMLNodeWidget.DEFAULT, "Default", 15, 15);

        UMLRoundedBorder border = new UMLRoundedBorder(15, 15, 0, 0, null, Color.BLACK);
        bodyWidget.setBorder(border);
        bodyWidget.setMinimumSize(new Dimension(150, 80));
        setHorizontalLayout(horizontal);
        initRegions();
        setLayout(LayoutFactory.createVerticalFlowLayout());
        addChild(tabWidget, 0);
        addChild(bodyWidget, 100);
        
    }
    

    public UMLNameWidget getNameWidget()
    {
        return nameWidget;
    }

    protected void initRegions()
    {      
        List<IRegion> regions = state.getContents();
        
        for (int i = 0; i < regions.size(); i++)
        {
            IRegion region = regions.get(i);
            addRegion(region);
            for (IElement e: region.getElements())
            {
                if (!(e instanceof ITransition))
                    elements.add(e);                    
            }
        }
    }
    
    private void updateConstraint()
    {
        RegionWidget[] regions = new RegionWidget[regionWidgets.size()];

        regionWidgets.toArray(regions);
        for (int i = 0; i < regions.length; i++)
        {
            RegionWidget w = regions[i];
            bodyWidget.setChildConstraint(w, i == regions.length - 1 ? 1 : 0);
            w.showSeparator(i == regions.length - 1 ? false : true);
            w.getNameWidget().setVisible(true);
        }
        if (regions.length == 1)
        {
            regions[0].getNameWidget().setVisible(false);
        }
        scene.validate();
    }
    
    
    public void removeRegion(RegionWidget widget)
    {
        regionWidgets.remove(widget);  
        if (regionWidgets.isEmpty())
        {
            IRegion region = new TypedFactoryRetriever<IRegion>().createType("Region");
            state.addContent(region);
            addRegion(region);
        }
        updateConstraint();
    }
    
    public void addRegion(IRegion region)
    {   
        RegionWidget regionWidget = new RegionWidget(scene, region, this);

        IPresentationElement pe = Util.createNodePresentationElement();
        pe.addSubject(region);
        if (scene instanceof ObjectScene)
        {
            ((ObjectScene) scene).addObject(pe, regionWidget);
        }
        regionWidgets.add(regionWidget);
        bodyWidget.addChild(regionWidget);

        updateConstraint();
    }
    
    protected void updateName(PropertyChangeEvent event)
    {
        nameWidget.propertyChange(event);
    }
    
    private String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.STATEWIDGET.toString();
    }

    public boolean isHorizontalLayout()
    {      
        return horizontal;
    }
    
    public Collection<RegionWidget> getRegionWidgets()
    {
        return regionWidgets;
    }
    
    public void setHorizontalLayout(boolean val)
    {
        horizontal = val;
        if (horizontal)
            bodyWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY,0));
        else
            bodyWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY,0));
        
        for (RegionWidget widget: regionWidgets)
        {
            widget.updateOrientation(horizontal);
        }
    }
    
    public State getElement()
    {
        return state;
    }
    
    public void notifyAdded()
    {
        // discover relationships (inter or inner) after all regions are loaded
        UMLRelationshipDiscovery relationshipD = new UMLRelationshipDiscovery((GraphScene) scene);
        relationshipD.discoverCommonRelations(elements);
    }
}
