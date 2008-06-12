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


import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.dom4j.Node;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.Diagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramTypesManager;
import org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.dataobject.UMLDiagramDataObject;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
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
    public IGraphicExportDetails saveAsGraphic(String sFilename, int nKind, double scale) {
        IGraphicExportDetails retVal = null;
        return retVal;
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
        
        return alias;
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
        if(value.isSame(space) == false)
        {
            space = value;
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
        Rectangle bnd=w.getBounds();
        bnd=w.convertLocalToScene(bnd);
        bnd=scene.convertSceneToView(bnd);
        Rectangle viewRect=getScene().getView().getVisibleRect();
        //
        viewRect.width-=20;
        if(bnd.width<viewRect.width)
        {
            int diff=viewRect.width-bnd.width;
            bnd.width=viewRect.width;
            bnd.x-=diff/2;
            if(bnd.x<0)bnd.x=0;
        }
        viewRect.height=20;
        if(bnd.height<viewRect.height)
        {
            int diff=viewRect.height-bnd.height;
            bnd.height=viewRect.height;
            bnd.y-=diff/2;
            if(bnd.y<0)bnd.y=0;
        }
        //
        getScene().getView().scrollRectToVisible(bnd);
        //
        //now about selection
        Set selected=new HashSet(bDeselectAllOthers ? new HashSet() : scene.getSelectedObjects());
        selected.add(pPresentationElement);
        scene.setSelectedObjects(selected);
    }
    
    /**
     * Centers the diagram on this presentation object
     */
    public void centerPresentationElement2(String sXMIID, boolean bSelectIt, boolean bDeselectAllOthers) {
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
    public ETList<IPresentationElement> getAllItems() {
        ETList < IPresentationElement > retVal = null;
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAllItems2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public ETList<IPresentationElement> getAllItems(IElement pModelElement) {
        ETList < IPresentationElement > retVal = null;
        
        return retVal;
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#receiveBroadcast(org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction)
         */
    public void receiveBroadcast(IBroadcastAction pAction) {
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
}
