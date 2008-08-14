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
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.State;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.diagrams.UMLRelationshipDiscovery;
import org.netbeans.modules.uml.diagrams.actions.CompositeWidgetSelectProvider;
import org.netbeans.modules.uml.diagrams.border.UMLRoundedBorder;
import org.netbeans.modules.uml.diagrams.nodes.CompartmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.CompositeWidget;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;

/**
 *
 * @author Sheryl Su
 */
public class CompositeStateWidget extends UMLNodeWidget implements CompositeWidget
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
    private IElementLocator locator = new ElementLocator();
    
    public CompositeStateWidget(Scene scene)
    {
        super(scene,true);
        this.scene = scene;
        addToLookup(initializeContextPalette());
        addToLookup(new CompositeWidgetSelectProvider(this));
    }
    
    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/State");
        return paletteModel;
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        IElement element = presentation.getFirstSubject();
        elements.clear();
        
        if (element instanceof State && ((State) element).getIsComposite())
        {
            state = (State) presentation.getFirstSubject();
            if (!isInitialized())
            {
                init();
                Widget widget = new ViewWidget(scene);
                widget.setLayout(LayoutFactory.createVerticalFlowLayout());
                widget.addChild(tabWidget, 0);
                widget.addChild(bodyWidget, 100);
                setCurrentView(widget);
                setIsInitialized(true);
                setFont(getCurrentView().getFont());
            }
           else
            {
                if (!PersistenceUtil.isDiagramLoading())
                {
                    initRegions();
                    addRegionElements();
                }
            }
        }
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
        if (!PersistenceUtil.isDiagramLoading())
        {
            initRegions();
            addRegionElements();
        }
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
            boolean found = false;
            
            for (RegionWidget rw: regionWidgets)
            {
               if (rw.getElement().equals(region))
               {
                   found = true;
                   break;
               }
            }
            if (!found)
                addRegion(region);
        }
        
        for (RegionWidget rw : regionWidgets)
        {
            if (!PersistenceUtil.isDiagramLoading())
            {
                rw.initContainedElements();
            }
        }
    }

    protected void addRegionElements()
    {
        List<IRegion> regions = state.getContents();
        for (IRegion region : regions)
        {
            for (IElement e : region.getElements())
            {
                if (!(e instanceof ITransition))
                {
                    elements.add(e);
                }
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

        setFont(getFont());
        updateConstraint();
//        updateSizeWithOptions();
        updateSize();
    }
    
    private void updateSize()
    {
        setPreferredBounds(null);
        setPreferredSize(null);    
        if (getBounds() != null)
            setMinimumSize(getBounds().getSize());    
    }
    
    
    public void propertyChange(PropertyChangeEvent event)
    {
        String propName = event.getPropertyName();

        if (propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
        {
            if (getNameWidget() instanceof PropertyChangeListener)
            {
                PropertyChangeListener listener = (PropertyChangeListener) getNameWidget();
                listener.propertyChange(event);
            }
        }      
    }

    protected void updateName(PropertyChangeEvent event)
    {
        nameWidget.propertyChange(event);
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
        {
            bodyWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        } else
        {
            bodyWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        }
        for (RegionWidget widget : regionWidgets)
        {
            widget.updateOrientation(horizontal);
        }
    }

    public State getElement()
    {
        return state;
    }

    public void discoverRelationship()
    {
        // discover relationships (inter or inner) after all regions are loaded
        UMLRelationshipDiscovery relationshipD = new UMLRelationshipDiscovery((GraphScene) scene);
        relationshipD.discoverCommonRelations(elements);
    }
    

    @Override
    public void load(NodeInfo nodeReader)
    {
        IElement elt = nodeReader.getModelElement();
        if (elt == null)
        {
            elt = locator.findByID(nodeReader.getProject(), nodeReader.getMEID());
        }
        if (elt != null && elt instanceof IState)
        {
            String or = nodeReader.getProperties().get("Orientation").toString();
            if (or.contains(SeparatorWidget.Orientation.VERTICAL.toString()))
            {
                this.setHorizontalLayout(false);
            } else
            {
                this.setHorizontalLayout(true);
            }
            initRegions();
            this.setPreferredLocation(nodeReader.getPosition());
            this.setPreferredSize(nodeReader.getSize());

        }
        if (elt != null && elt instanceof IRegion)
        {
            RegionWidget regionW = findRegionWidget((IRegion) elt);
            if (regionW != null)
            {
                //fix the size/location/properties
                regionW.setPreferredSize(nodeReader.getSize());
                IPresentationElement pElt = PersistenceUtil.getPresentationElement(regionW);
                nodeReader.setPresentationElement(pElt);
                nodeReader.setModelElement(elt);
            }
        }
    }

    private RegionWidget findRegionWidget(IRegion region)
    {
        RegionWidget retVal = null;
        if (region != null)
        {
            Collection<RegionWidget> list = getRegionWidgets();
            for (Iterator<RegionWidget> it = list.iterator(); it.hasNext();)
            {
                RegionWidget regionWidget = it.next();
                if (regionWidget.getElement().equals(region))
                {
                    retVal = regionWidget;
                    break;
                }
            }
        }
        return retVal;
    }

    @Override
    public void save(NodeWriter nodeWriter)
    {
        String layout = "";
        if (isHorizontalLayout())
        {
            layout = SeparatorWidget.Orientation.HORIZONTAL.toString();
        } else
        {
            layout = SeparatorWidget.Orientation.VERTICAL.toString();
        }
        HashMap map = nodeWriter.getProperties();
        map.put("Orientation", layout);
        nodeWriter.setProperties(map);
        super.save(nodeWriter);
    }

    public Collection<CompartmentWidget> getCompartmentWidgets()
    {
        ArrayList<CompartmentWidget> result = new ArrayList<CompartmentWidget>();
        result.addAll(getRegionWidgets());
        return result;
    }

    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.STATEWIDGET.toString();
    }

    public void removeCompartment(CompartmentWidget widget)
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
    
    
    public void addChildrenInBounds() {
        for(CompartmentWidget w:regionWidgets)
        {
            w.getContainerWidget().calculateChildren(false);//only add, do not check removal
        }
    }

    @Override
    protected void notifyFontChanged(Font font) {
        if(font==null || nameWidget==null)return;
        nameWidget.setNameFont(font);
        for(Widget w:regionWidgets)
        {
            if(w instanceof RegionWidget)
            {
                w.setFont(font);
            }
        }
        revalidate();
    }
    
    // this main purpose of this class is to capture the moment to execute relationship discovery
    // when all regions and their contained elements are created
    private class ViewWidget extends Widget
    {
        public ViewWidget(Scene scene)
        {
            super(scene);
            setForeground(null);
            setBackground(null);
            setFont(null);
        }

        public void notifyAdded()
        {
            UMLRelationshipDiscovery relationshipD = new UMLRelationshipDiscovery((GraphScene) scene);
            relationshipD.discoverCommonRelations(elements);
        }
    }
}
