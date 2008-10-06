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


import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.SwingUtilities;
import org.dom4j.Node;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.Diagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramTypesManager;
import org.netbeans.modules.uml.core.metamodel.diagrams.GraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.NodeMapLocation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.drawingarea.dataobject.UMLDiagramDataObject;
import org.netbeans.modules.uml.drawingarea.image.DiagramImageWriter;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.diagramsupport.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDrawingAreaEventDispatcher;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jyothi
 */
public class UIDiagram extends Diagram { 
    
//    private WeakReference < UITopComponent > rawTopComponent = null;
    private DispatchHelper dispatchHelper = new DispatchHelper();
    private IDrawingAreaEventDispatcher diagramAreaDispatcher;
    private UMLDiagramDataObject data = null;
    private DesignerScene scene;
    private FileLock lock = null;
    private INamespace space = null;
    private String name = "";
    private String alias = "";
    //private String xmiIDStr = "";
    private String doc;
    private boolean notify = true;
    
    // TODO: Convert to enumeration.
    private int diagramType = IDiagramKind.DK_UNKNOWN;
    
    public UIDiagram()
    {
        
    }
    
    public UIDiagram(UMLDiagramDataObject dobj) {
        super();
        
        setDataObject(dobj);
    }
    
    public void setDataObject(UMLDiagramDataObject dobj)
    {
       data = dobj; 
    }
    
    /**
     * similr to setDrawingArea in 6.0
     * sometimes it's required to know engine/scene/top component based on diagram
     * @param engine
     */
    public void setScene(DesignerScene scene)
    {
        this.scene=scene;
    }
    /**
     * similr to getDrawingArea in 6.0
     * sometimes it's required to know engine/scene/top component based on diagram
     * @param engine
     */
    public DesignerScene getScene()
    {
        return scene;
    }
    
    /**
     * Saves the diagram
     */
    public void save() throws IOException { 
        if (isDirty() == true) {
            SaveCookie cookie = data.getLookup().lookup(SaveCookie.class);
            if (cookie != null) {
                cookie.save();
            }
        }
    }
    
    /**
     * Is this diagram readonly?
     *
     * @param pVal [out,retval] true if the diagram is readonly
     */
    public boolean getReadOnly() {
        boolean retVal = false;
        
        if(data != null)
        {
            FileObject fo = data.getPrimaryFile();
            retVal = fo.canWrite() == false;
        }
        
        return retVal;
    }
    
    /**
     * Is this diagram readonly?
     *
     * @param newVal [in] true to make the diagram readonly
     */
    public FileLock setReadOnly(boolean value) {
        FileLock ret=null;
        if(data != null)
        {
            MultiDataObject.Entry entry = data.getPrimaryEntry();
            
            try
            {
                if((value == true) && (entry.isLocked() == false))
                {
                    lock = entry.takeLock();
                    ret=lock;
                }
                else if((value == false) && (entry.isLocked() == true))
                {
                    if(lock == null)
                    {
                        lock = entry.takeLock();
                    }

                    if(lock != null)
                    {
                        lock.releaseLock();
                    }
                    ret=lock;
                }  
            }
            catch(IOException e)
            {
                Exceptions.printStackTrace(e);
            }
        }
        return ret;
    }
    
    /**
     * Saves the diagram as a graphic
     */
    public void saveAsGraphic(String sFilename, int nKind) {
    }
    
    /**
     * Saves the diagram as a graphic
     */
    public IGraphicExportDetails saveAsGraphic(final String sFilename, int nKind, final double scale) {
        
        IGraphicExportDetails retVal = new GraphicExportDetails();
        retVal.setFrameBoundingRect(new ETRect(scene.getLocation().x, scene.getLocation().y,
                                    scene.getClientArea().width * scale, 
                                    scene.getClientArea().height * scale));
        retVal.setGraphicBoundingRect(new ETRect(scene.getLocation().x, scene.getLocation().y,
                                    scene.getClientArea().width * scale, 
                                    scene.getClientArea().height * scale));
        ETArrayList<IGraphicMapLocation> locations = new ETArrayList<IGraphicMapLocation>();

        ArrayList<Widget> list = new ArrayList<Widget>();
        getNodes(scene, scene, list);
        // put the top widget at the front of list
        Collections.reverse(list);
        
        for (Widget w: list)
        {
            IPresentationElement pe = (IPresentationElement)scene.findObject(w);
            IElement e = pe.getFirstSubject();
            Rectangle rect = new Rectangle(w.convertLocalToScene(w.getBounds()));
            rect.translate(scene.getLocation().x, scene.getLocation().y);
            rect = new Rectangle((int)(rect.x * scale), (int)(rect.y * scale), 
                    (int)(rect.width * scale), (int)(rect.height * scale));
            NodeMapLocation nodeM = new NodeMapLocation(e, rect);
            locations.add(nodeM);
        }
        
        // edge map is not used for now
//        for (IPresentationElement pe: scene.getEdges())
//        {
//            EdgeMapLocation nodeM = new EdgeMapLocation();
//            IElement e = pe.getFirstSubject();
//
//            nodeM.setElement(e);
//            Widget w = scene.findWidget(pe);
//            if (w instanceof ConnectionWidget)
//            {
//                ETArrayList<IETPoint> points = new ETArrayList<IETPoint>();
//                for (Point p : ((ConnectionWidget) w).getControlPoints())
//                {
//                    p.translate(scene.getLocation().x, scene.getLocation().y);
//
//                    points.add(new ETPoint(p));
//                }
//                nodeM.setPoints(points);
//                locations.add(nodeM);
//            }
//        }
        
        retVal.setMapLocations(locations);        
                 
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    FileImageOutputStream fo = new FileImageOutputStream(new File(sFilename));
                    DiagramImageWriter.write(scene, fo, scale);
                } catch (Exception e)
                {
                    Logger.getLogger("UML").severe("unable to create diagram image file: " + e.getMessage());
                }
            }
        });
            
        return retVal;
    }
    
    private ArrayList<Widget> getNodes(DesignerScene scene, Widget widget, ArrayList<Widget> list)
    {
        for (Widget w: widget.getChildren())
        {
            if (scene.isNode(scene.findObject(w)) && w instanceof UMLNodeWidget)
            {
                list.add(w);             
            }
            getNodes(scene, w, list);
        }       
        return list;
    }
    
    public String getName() {
        String retVal = name;
        
        return retVal;
    }
    
    public void setName(String value) 
    {
        if ( !getName().equals(value))
        {
            name = value;
            if (getNotify()) 
            {
                fireDrawingAreaPropertyChange("FireDrawingAreaPostPropertyChange", 
                        DiagramAreaEnumerations.DAPK_NAME);
                Node node = null;
                if ((node = getNode()) != null )
                {
                    XMLManip.setAttributeValue(node, "name",  name);
                }
            }
        }
    }
    
    public String getAlias() 
    {
        String retVal = alias;
        
        if((retVal == null) || (retVal.length() <= 0))
        {
            retVal = getName();
        }
        
        return retVal;
    }
    
    public void setAlias(String value) 
    {
        if ( !getAlias().equals(value))
        {
            alias = value;
             if (getNotify()) 
            {
                fireDrawingAreaPropertyChange("FireDrawingAreaPostPropertyChange", 
                        DiagramAreaEnumerations.DAPK_ALIAS);
             }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getNameWithAlias()
         */
    public String getNameWithAlias() {
        String retVal = "";
        
        if (ProductHelper.getShowAliasedNames() == true)
        {
            retVal = getAlias();
            if (retVal.length() <= 0)
            {
                retVal = getName();
            }
        }
        else
        {
            retVal = getName();
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setNameWithAlias(java.lang.String)
         */
    public void setNameWithAlias(String value) 
    {
        if (ProductHelper.getShowAliasedNames() == true)
        {
            setAlias(value);
        }
        else
        {
            setName(value);
        }
    }
    
    public String getQualifiedName() {
        String retVal = "";
        
        boolean bIncludeProjectName = ProductHelper.useProjectInQualifiedName();
        INamespace space = getNamespace();
        String diagramName = getName();
        if (diagramName.length() > 0)
        {
            if (space instanceof IProject && !bIncludeProjectName)
            {
                retVal = diagramName;
            }
            else
            {
                if (space != null)
                {
                    retVal = space.getQualifiedName();
                    retVal += "::";
                }
                retVal += diagramName;
            }
        }

        return retVal;
    }
    
    public String getFilename()
    {
        String retVal = "";

        if (data != null)
        {
            try
            {
                FileObject fo = data.getPrimaryFile(); 
                retVal = FileUtil.toFile(fo).getCanonicalPath();
//                retVal = fo.getURL().toURI().getPath();
            }
            catch (Exception e)
            {
                Exceptions.printStackTrace(e);
            }
        }

        return retVal;
    }
    
    public INamespace getNamespace() {
        INamespace retVal = space;
        
        return retVal;
    }
    
    public void setNamespace(INamespace value) 
    {   
        boolean isSame = true;
        if (space != null)
        {
            isSame = space.isSame(value);
        } 
        else if (value != null) 
        {
            isSame = false;
        }
        
        if (!isSame)  //if the namespace is being changed, fire an event
        {
             space = value;
             if (getNotify()) 
            {
                fireDrawingAreaPropertyChange("FireDrawingAreaPostPropertyChange", 
                        DiagramAreaEnumerations.DAPK_NAMESPACE);
             }
             setOwner(space);
        }
    }
    
    public INamespace getNamespaceForCreatedElements() {
//        INamespace retVal = space;
//        
//        return retVal;
        if (this.getDiagramKind() == IDiagram.DK_STATE_DIAGRAM)
        {
            IElement owner = getOwner();
            if (owner instanceof IStateMachine)
            {
                return ((IStateMachine)owner).getFirstRegion();
            }
        }
        return space;
    }
    
    /**
     * Selects all the items on the diagram
     */
    public void selectAll(boolean bSelect) {
    }
    /**
     * Centers the diagram on this presentation object
     */
    public void centerPresentationElement(IPresentationElement pPresentationElement, 
                                          boolean bSelectIt, 
                                          boolean bDeselectAllOthers) 
    {
        Widget w=scene.findWidget(pPresentationElement);
        if(w!=null)
        {
            Rectangle bnd=w.getBounds();
            bnd=w.convertLocalToScene(bnd);
            centerRectangle(bnd);

            ContextPaletteManager manager = scene.getLookup().lookup(ContextPaletteManager.class);
            if(manager != null)
            {
                manager.cancelPalette();
            }

            Set selected=new HashSet(bDeselectAllOthers ? new HashSet() : scene.getSelectedObjects());
            selected.add(pPresentationElement);
            scene.userSelectionSuggested(selected, false);
            scene.validate();

            if(manager != null)
            {
                manager.selectionChanged(null);
            }
        }
    }
    
    /**
     * Centers the diagram on this presentation object
     */
    public void centerPresentationElement(String sXMIID, 
                                           boolean bSelectIt, 
                                           boolean bDeselectAllOthers) 
    {
        ElementLocator locator = new ElementLocator();
        IElement element = locator.findElementByID(getProject(), sXMIID);
        if (element instanceof IPresentationElement)
        {
            IPresentationElement presentation = (IPresentationElement) element;
            centerPresentationElement(presentation, bSelectIt, bDeselectAllOthers);
        }
    }
    
    /**
     * Centers the diagram on this presentation object
     * 
     * @param point The point in scene coordinates.
     */
    public void centerPoint(Point point) 
    {
        Point viewPoint = scene.convertSceneToView(point);
        Rectangle viewRect = getScene().getView().getVisibleRect();
        
        viewRect.x = viewPoint.x - (viewRect.width / 2);
        if(viewRect.x < 0)
        {
            viewRect.x = 0;
        }

        viewRect.y = viewPoint.y - (viewRect.height / 2);
        if(viewRect.y < 0)
        {
            viewRect.y = 0;
        }
        
        getScene().getView().scrollRectToVisible(viewRect);
    }
    
    /**
     * Centers the diagram on a specified rectangle
     * 
     * @param sceneRect The rectangle in scene coordinates.
     */
    public void centerRectangle(Rectangle sceneRect)
    {
//        Rectangle viewRect = getScene().convertSceneToView(sceneRect);
        centerPoint(new Point(sceneRect.x + (sceneRect.width / 2),
                              sceneRect.y + (sceneRect.height / 2)));
    }
    
    public void hasGraphObjects(boolean bHasObjects) {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getDiagramKind()
         */
    public int getDiagramKind() {
        int retVal = diagramType;
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setDiagramKind(int)
         */
    public void setDiagramKind(int value) 
    {
        diagramType = value;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getDiagramKind2()
         */
    public String getDiagramKindAsString() 
    {
        DiagramTypesManager diaMgr = DiagramTypesManager.instance();
        return diaMgr.getDiagramTypeName(getDiagramKind());
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getSelected()
         */
    public ETList<IPresentationElement> getSelected() {
        ETList < IPresentationElement > retVal = null;
        return retVal;
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAllItems()
         */
    public ETList<IPresentationElement> getAllItems() 
    {
        ETList < IPresentationElement > retVal = 
                new ETArrayList < IPresentationElement >();
        
        for(Object item : getScene().getObjects())
        {
            if (item instanceof IPresentationElement)
            {
                retVal.add((IPresentationElement) item);
                
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAllItems(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public ETList<IPresentationElement> getAllItems(IElement pModelElement) 
    {
        ETList < IPresentationElement > retVal = 
                new ETArrayList < IPresentationElement >();
        
        for(IPresentationElement item : pModelElement.getPresentationElements())
        {
            if(scene.findWidget(item) != null)
            {
                retVal.add(item);
            }
        }
        
        return retVal;
    }
    
    /**
     * Returns a list of all the model elements on the diagram.
    */
    public ETList<IElement> getModelElements()
    {
        ETList < IElement > retVal = 
                new ETArrayList < IElement >();
        
        for(Object item : getScene().getObjects())
        {
            if (item instanceof IPresentationElement)
            {
                IPresentationElement element = (IPresentationElement)item;
                retVal.add(element.getFirstSubject());
                
            }
        }
        
        return retVal;
    }

    /**
     * Select all the objects on the diagram that are of the indicated type
     * 
     * @param type The type of the model element.
     * @return  The list of presentation elements that represent the specified
     *          model element type.
    */
    public ETList<IPresentationElement> getAllByType( String type )
    {
        ETList < IPresentationElement > retVal = 
                new ETArrayList < IPresentationElement >();
        
        for(Object item : getScene().getObjects())
        {
            if (item instanceof IPresentationElement)
            {
                IPresentationElement element = (IPresentationElement)item;
                if(type.equals(element.getFirstSubjectsType()) == true)
                {
                    retVal.add(element);
                }
                
            }
        }
        
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#receiveBroadcast(org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction)
     */
    public void receiveBroadcast(IBroadcastAction pAction) {
    }
    
    /**
     * Notifies the diagram to refresh the node graphical object that 
     * is associated with the presentation element.
     * 
     * @param presentation The presentation element that needs to be refreshed.
     * @param resizetocontent resize elements to content after update
     * @return true if the presenation element was found and refreshed.
     */
    public boolean refresh(IPresentationElement presentation,boolean resizetocontent)
    {
        boolean retVal = false;
        
        Widget widget = scene.findWidget(presentation);
        if (widget instanceof UMLNodeWidget)
        {
            UMLNodeWidget node = (UMLNodeWidget) widget;
            node.refresh(resizetocontent);
            retVal = true;
        }
        else if (widget instanceof UMLEdgeWidget)
        {
            UMLEdgeWidget edge = (UMLEdgeWidget) widget;
            edge.refresh(resizetocontent);
            retVal = true;
        }
        
        return retVal;
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getIsDirty()
         */
    @Override
    public boolean isDirty() {
        boolean retVal = false;
        
        if(data != null)
        {
            retVal = data.isModified();
        }
        
        return retVal;
    }
    
    public void setDirty(boolean value) {
        
        if(data != null)
        {
            data.setModified(value);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isSame(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
         */
    public boolean isSame(IDiagram pDiagram) {
        if (pDiagram != null) {
            String thisFilename = getFilename();
            String otherFilename = pDiagram.getFilename();
            return thisFilename != null && otherFilename != null && thisFilename.equals(otherFilename);
        }
        return false;
    }
    
    /**
     * Search for, and return if found, the presentation element on this diagram that has the specified xmi.id
     *
     * @param sXMIID [in] The xmi.id of the presentation to search for, and find
     * @param pPresentationElement [out,retval] Returns the presentation element on the diagram with the specified xmi.id
     *
     * @return HRESULT
     */
    public IPresentationElement findPresentationElement(String sXMLID) {
        IPresentationElement retVal = null;
        return retVal;
    }
    
    /**
     * Returns the relationship discovery object
     *
     * @param pDiscoverer [out,retval] Returns the relationship discovery object
     *
     * @return HRESULT
     */
    public ICoreRelationshipDiscovery getRelationshipDiscovery() {
        ICoreRelationshipDiscovery retVal = null;
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#addAssociatedDiagram(java.lang.String)
         */
    public void addAssociatedDiagram(String sDiagramXMIID) {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#addAssociatedDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
         */
    public void addAssociatedDiagram2(IProxyDiagram pDiagram) {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#removeAssociatedDiagram(java.lang.String)
         */
    public void removeAssociatedDiagram(String sDiagramXMIID) {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#removeAssociatedDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
         */
    public void removeAssociatedDiagram2(IProxyDiagram pDiagram) {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isAssociatedDiagram(java.lang.String)
         */
    public boolean isAssociatedDiagram(String sDiagramXMIID) {
        boolean retVal = false;
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isAssociatedDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
         */
    public boolean isAssociatedDiagram2(IProxyDiagram pDiagram) {
        boolean retVal = false;
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#addAssociatedElement(java.lang.String, java.lang.String)
         */
    public void addAssociatedElement(String sTopLevelElementXMIID, String sModelElementXMIID) {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#addAssociatedElement2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public void addAssociatedElement2(IElement pElement) {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#removeAssociatedElement(java.lang.String, java.lang.String)
         */
    public void removeAssociatedElement(String sTopLevelElementXMIID, String sModelElementXMIID) {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#removeAssociatedElement2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public void removeAssociatedElement2(IElement pElement) {
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isAssociatedElement(java.lang.String)
         */
    public boolean isAssociatedElement(String sModelElementXMIID) {
        boolean retVal = false;
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isAssociatedElement2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public boolean isAssociatedElement2(IElement pElement) {
        boolean retVal = false;
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAssociatedDiagrams()
         */
    public ETList<IProxyDiagram> getAssociatedDiagrams() {
        ETList < IProxyDiagram > retVal = null;
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAssociatedElements()
         */
    public ETList<IElement> getAssociatedElements() {
        ETList < IElement > retVal = null;
        return retVal;
    }
    
    public ETList<IElement> getAllItems3() {
        ETList < IElement > retVal = null;
        
        return retVal;
    }
    
    @Override
    public ETList<IElement> getElements() {
        
        return null;
    }
    
    @Override
    public IProject getProject() {
        IProject retVal = null;
        
        INamespace owner = getNamespace();
        if(owner != null)
        {
            retVal = owner.getProject();
        }
        
        return retVal;
    }
    
    @Override
    public String getDocumentation() {
       return doc;
    }
    
    @Override
    public void setDocumentation(String newDoc) {
        doc = newDoc;
    }
    
    @Override
    public IElement getOwner() {
        return getNamespace();
    }
    
    @Override
    public void setOwner(IElement newOwner) {
        if (newOwner != null && newOwner instanceof INamespace) {
            setNamespace((INamespace)newOwner);
        }
    }

//    @Override
//    public String getXMIID() {
//        xmiIDStr = super.getXMIID();
//        if (xmiIDStr.equals("")) {
//            xmiIDStr = UMLXMLManip.generateId(true);
//        }
//        return xmiIDStr;
//    }
//
//    @Override
//    public void setXMIID(String newVal) {
////        this.xmiIDStr = newVal;
//    }
    public IElement getFirstSubject()
    {
        return this;
    }

    public IProxyDiagram getProxyDiagram()
    {
        ProxyDiagramManager proxyDiagramManager = ProxyDiagramManager.instance();
        return proxyDiagramManager.getDiagram(this);

    }
    
    /**
     * Fires the property change event
     */
    private void fireDrawingAreaPropertyChange(String payload, int propKind)
    {
        if (diagramAreaDispatcher == null)
        {
            DispatchHelper helper = new DispatchHelper();
            diagramAreaDispatcher = helper.getDrawingAreaDispatcher();
        }

        if (payload != null && payload.trim().length() > 0)
        {
            IProxyDiagram proxyDiagram = getProxyDiagram();
            IEventPayload ePayload = diagramAreaDispatcher.createPayload(payload);

            if (proxyDiagram != null)
            {
                diagramAreaDispatcher.fireDrawingAreaPostPropertyChange(
                        proxyDiagram, propKind, ePayload);
            }
        }
    }

    public void setNotify(boolean val)
    {
        this.notify = val;
    }

    public boolean getNotify()
    {
        return this.notify;
    }
    
    public int getFrameWidth()
    {
        return scene.getClientArea().width;
    }
    
    public int getFrameHeight()
    {
        return scene.getClientArea().height;
    }
    
    public double getCurrentZoom()
    {
        return scene.getZoomFactor();
    }
}
