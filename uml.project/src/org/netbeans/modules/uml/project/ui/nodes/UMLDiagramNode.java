/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.project.ui.nodes;

import org.netbeans.modules.uml.common.ui.SaveNotifier;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.NotifyDescriptor;
import org.openide.cookies.PrintCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaPropertyKind;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import javax.swing.Action;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.common.ui.SaveNotifierOkCancel;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.project.ui.nodes.actions.CopyDiagramAction;
import org.netbeans.modules.uml.ui.controls.newdialog.AddPackageVisualPanel1;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.openide.DialogDisplayer;
import org.openide.util.actions.SystemAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.RenameAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Trey Spiva
 */
public class UMLDiagramNode extends UMLElementNode
        implements ITreeDiagram, IDrawingAreaEventsSink
{
    private IProxyDiagram mDiagram = null;
    private String mDiagramType = ELEMENT_TYPE_DIAGRAM;
    private DiagramPrintCookie mPrintCookie = null;
    private DispatchHelper dispatchHelper = null;
    private boolean bCancelSaveDialog = false;
    private boolean listenersRegistered = false;
    
    public UMLDiagramNode(IProxyDiagram diagram)
    {
        super();
        initializeNode(diagram);
    }
    
    public UMLDiagramNode(Lookup l, IProxyDiagram diagram)
    {
        super(l);
        initializeNode(diagram);
    }
    
    private void initializeNode(IProxyDiagram diagram)
    {
        setDiagram(diagram);
        setFilename(diagram.getFilename());
        setDiagramType(diagram.getDiagramKindName());
        registerListeners();
        getCookieSet().add(this);
    }
    
    public String getShortDescription()
    {
        StringBuffer fullNameSpace = new StringBuffer();
        IProject project = getDiagram().getProject();
        String qname = getDiagram().getQualifiedName();
        fullNameSpace.append(project + "::" + qname); // NOI18N
        
        return fullNameSpace.toString();
    }
    
    
    public void registerListeners()
    {
        if (isListenersRegistered())
            return;
        
        getDispatchHelper().registerDrawingAreaEvents(this);
        setListenersRegistered(true);
    }
    
    public void unregisterListeners()
    {
        getDispatchHelper().revokeDrawingAreaSink(this);
        setListenersRegistered(false);
    }
    
    
    // enables the node to be renamed
    public boolean canRename()
    {
        return true;
    }
    
    public void destroy() throws IOException
    {
        ProxyDiagramManager proxyDiagramManager = ProxyDiagramManager.instance();
        proxyDiagramManager.removeDiagram(getDiagram().getFilename());
        
        super.destroy();
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram#getDiagram()
    */
    public IProxyDiagram getDiagram()
    {
        return mDiagram;
    }
    
    /**
     * Set the diagram that is wrapped by the node.
     *
     * @param diagram The diagram.
     */
    public void setDiagram(IProxyDiagram diagram)
    {
        mDiagram = diagram;
    }
    
    /**
     * Sets the diagram type.  The diagram type name is also used to determine
     * the icon that represents the node.  If <i>_CLOSED</i> is appended to the
     * diagram name then the closed diagram icon is used instead.
     *
     * @param name The diagram type name.
     */
    public void setDiagramType(String name)
    {
        mDiagramType = name;
        fireIconChange();
    }

    
    public DiagramPrintCookie getPrintCookie()
    {
        if (mPrintCookie == null)
            mPrintCookie = new DiagramPrintCookie();
        
        return mPrintCookie;
    }
    
    /**
     * Retrieves the diagram type.  The diagram type name is also used to
     * determine the icon that represents the node.  If <i>_CLOSED</i> is
     * appended to the diagram name then the closed diagram icon is used instead.
     *
     * @return The diagram type name.
     */
    public String getDiagramType()
    {
        return mDiagramType;
    }
    
    public Transferable clipboardCopy()
    throws IOException
    {
        ADTransferable retVal = new ADTransferable("DRAGGEDITEMS"); // NOI18N
        retVal.addDiagramLocation(getDiagram().getFilename());
        
        DispatchHelper heleper = new DispatchHelper();
        org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher disp = heleper.getProjectTreeDispatcher();
        
        if (disp != null)
        {
            org.netbeans.modules.uml.core.eventframework.IEventPayload payload =
                    disp.createPayload("ProjectTreeBeginDrag"); //$NON-NLS-1$
            
            org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify context =
                    new org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeDragVerifyImpl();
            
            if (this instanceof ITreeItem)
            {
                IProjectTreeItem[] items = {((ITreeItem)this).getData()};
                disp.fireBeginDrag(null, items, context, payload);
            }
            
        }
        return retVal;
    }
    
    
    
    public Action[] getActions(boolean context)
    {
        ArrayList<Action> actions = new ArrayList <Action>();
        
        actions.add(SystemAction.get(OpenAction.class));
        actions.add(null);
        int kind = getDiagram().getDiagramKind();
        // see #102294
        if ( kind != IDiagramKind.DK_SEQUENCE_DIAGRAM &&
                kind != IDiagramKind.DK_COLLABORATION_DIAGRAM )
        {
            actions.add(SystemAction.get(CopyDiagramAction.class));
        }
        actions.add(SystemAction.get(RenameAction.class));
        actions.add(SystemAction.get(DeleteAction.class));
        actions.add(null);
        addContextMenus(actions);
        actions.add(null);
        actions.add(SystemAction.get(PropertiesAction.class));;

        Action[] retVal = new Action[actions.size()];
        actions.toArray(retVal);
        return retVal;
    }
    
    //**************************************************
    // Helper Methods
    //**************************************************
    
    /**
     * @param string
     */
    protected void setFilename(String value)
    {
        IProjectTreeItem data = getData();
        
        if (data != null)
        {
            String filename = StringUtilities
                    .ensureExtension(value, FileExtensions.DIAGRAM_LAYOUT_EXT);
            
            if (filename.length() > 0)
            {
                data.setDescription(filename);
            }
        }
    }
    
    public boolean equals(Object obj)
    {
        boolean retVal = false;
        
        if (obj instanceof ITreeDiagram)
        {
            ITreeDiagram diagram = (ITreeDiagram)obj;
            
            String testDescription = diagram.getData().getDescription();
            String myDescription   = getData().getDescription();
            
            retVal = myDescription.equals(testDescription);
        }
        
        else if (obj instanceof String)
        {
            String myDescription   = getData().getDescription();
            retVal = myDescription.equals((String)obj);
        }
        
        else if (obj instanceof IDrawingAreaControl)
        {
            IDrawingAreaControl control = (IDrawingAreaControl)obj;
            
            String testDescription = control.getFilename();
            String myDescription   = getData().getDescription();
            
            retVal = myDescription.equals(testDescription);
        }
        
        else if (obj instanceof IProxyDiagram)
        {
            IProxyDiagram control = (IProxyDiagram)obj;
            
            String testDescription = control.getFilename();
            String myDescription   = getData().getDescription();
            
            retVal = myDescription.equals(testDescription);
        }
        
        else if (obj instanceof IDiagram)
        {
            IDiagram control = (IDiagram)obj;
            
            String testDescription = control.getFilename();
            String myDescription   = getData().getDescription();
            
            retVal = myDescription.equals(testDescription);
        }
        
        else
        {
            retVal = super.equals(obj);
        }
        
        return retVal;
    }
    
    public String getType()
    {
        return mDiagramType;
    }
    
    
    
    public void setName(String val)
    throws IllegalArgumentException
    {
        
        if (!Util.isDiagramNameValid(val))
        {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(
                    AddPackageVisualPanel1.class,
                    "MSG_Invalid_Diagram_Name", val)); // NOI18N
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        
        
        // cvc - 6288598
        // the name can be "" (empty string) when it is enclosed inside a
        // parent node that is expanding (Activity Diagram) and it is getting
        // its name assigned to it as it is initialized. This appears to be
        // a rename scenario, but it's not, so don't do the "save before rename"
        String nodeName = getName();
        IProxyDiagram pDiagram = null;
        IDiagram diagram = null;
        boolean isNoNulls = false;
        
        if (getDiagram() != null && getDiagram().getDiagram() != null)
        {
            isNoNulls = true;
            pDiagram = getDiagram();
            diagram = pDiagram.getDiagram();
        }
        
        if (isNoNulls && // no NPEs to worry about
                pDiagram.isOpen() && // diagram is currently open
                diagram.getIsDirty() && // diagram is modified
                !pDiagram.getName().equals(val) && // diagram name not new name
                !nodeName.equals("") && // diagram's node name not empty string
                !nodeName.equals(val)) // diagram's node name not new name
        {
            // cvc - CR 6275795
            // the diagram is open and modifed, so we must prompt the user
            //  to save the diagram before the rename, because after it is
            //  renamed, we will autosave it for them so that the name change
            //  will persist properly, otherwise the user will be prompted to
            //  save when closing the diagram after a name change. If they
            //  don't save, then the display name (getDisplayName) of the node
            //  will be out of sync with the system name (getName)
            
            String msg = NbBundle.getMessage(
                    UMLDiagramNode.class,
                    "LBL_DIALOG_MSG_RenamePreSaveDiagram", diagram.getName()); // NOI18N
            
            if (!save(diagram, true, msg)) // NOI18N
            {
                // open/modified diagram not save by user
                
                // when the user clicked "No" on the Save dialog, this
                //  results in the diagram being set to not dirty, but in
                //  this case we just want to leave it dirty and prevent
                //  the rename from happening
                diagram.setIsDirty(true);
                
                // prevent rename from happening
                return;
            }
        }
        
        // diagram was saved or wasn't open/modified, continue with rename
        // setDisplayedName(val);
        setDisplayName(val);
        super.setName(val);
        
        firePropertySetsChange(null, retreiveProperties());
    }
    
    public void setDisplayName(String val)
    {
        IProjectTreeItem item = getData();
        if (item != null)
        {
            IProxyDiagram dia = item.getDiagram();
            if (dia != null)
            {
                //its an unopen diagram
                String curName = dia.getName();
                
                if (!curName.equals(val))
                {
                    dia.setName(val);
                    if(ProductHelper.getShowAliasedNames())
                    {
			dia.setAlias(val);
                    }
                    //dia.setNameWithAlias(val);
                    
                    fireNameChange(curName, val);
                    fireDisplayNameChange(curName, val);
                    
                    // cvc - CR 6275795
                    // autosave the diagram after the rename
                    if (getDiagram() != null &&
                            getDiagram().getDiagram() != null &&
                            getDiagram().getDiagram().getIsDirty())
                    {
                        getDiagram().getDiagram().save();
                    }
                }
            }
        }
    }
    
    
    public String getDisplayName()
    {
        String retVal = ""; // NOI18N
        IProjectTreeItem item = getData();
        
        if (item != null)
        {
            IProxyDiagram dia = item.getDiagram();
            
            if (dia != null)
            {
                //its an unopen diagram
                retVal = dia.getNameWithAlias();
            }
        }
        
        return retVal;
    }
    
    
    public String getElementType()
    {
        String retVal = ""; // NOI18N
        IProjectTreeItem item = getData();
        
        if (item != null)
        {
            IProxyDiagram dia = item.getDiagram();
            if (dia != null)
            {
                IDiagram openDia = dia.getDiagram();
                if (openDia != null)
                {
                    retVal = ELEMENT_TYPE_DIAGRAM;
                }
                
                else
                {
                    //its an unopen diagram
                    retVal = ELEMENT_TYPE_PROXY_DIAGRAM;
                }
            }
        }
        return retVal;
    }
    
    public IElement getModelElement()
    {
        IElement retVal = null;
        
        IProjectTreeItem item = getData();
        if (item != null)
        {
            IProxyDiagram dia = item.getDiagram();
            if (dia != null)
            {
                //its an unopen diagram
                retVal = dia.getDiagram();
            }
        }
        
        return retVal;
    }
    
    /**
     * Builds the proerty set structure.
     */
    protected Node.PropertySet[] buildProperties()
    {
        Node.PropertySet[] retVal = null;
        
        IElement element = getModelElement();
        
        DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
        if (element != null)
        {
            // In this case we actually have a IDiagram Instance.
            retVal = builder.retreiveProperties(element.getElementType(), element);
        }
        
        else
        {
            // In this case we only have a IProxyDiagram instance.
            IProjectTreeItem item = getData();
            if (item != null)
            {
                IProxyDiagram proxy = item.getDiagram();
                retVal = builder.retreiveProperties(getElementType(), proxy);
            }
        }
        
        return retVal;
    }
    
    public void onDrawingAreaTooltipPreDisplay(
            IDiagram pParentDiagram,
            IPresentationElement pPE,
            IToolTipData pTooltip,
            IResultCell cell)
    {
    }
    
    public void onDrawingAreaPreSave(
            IProxyDiagram pParentDiagram, IResultCell cell)
    {}
    
    public void onDrawingAreaPrePropertyChange(
            IProxyDiagram pProxyDiagram,
            int nPropertyKindChanged,
            IResultCell cell)
    {}
    
    public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
    {}
    
    public void onDrawingAreaPreDrop(
            IDiagram pParentDiagram,
            IDrawingAreaDropContext pContext,
            IResultCell cell)
    {}
    
    public void onDrawingAreaPreCreated(
            IDrawingAreaControl pDiagramControl, IResultCell cell)
    {}
    
    public void onDrawingAreaPostSave(
            IProxyDiagram pParentDiagram, IResultCell cell)
    {
    }
    
    public void onDrawingAreaPostPropertyChange(
            IProxyDiagram pProxyDiagram,
            int nPropertyKindChanged,
            IResultCell cell)
    {
        if (nPropertyKindChanged == IDrawingAreaPropertyKind.DAPK_NAME &&
                pProxyDiagram.getXMIID().equals(getDiagram().getXMIID()))
        {
            String name = pProxyDiagram.getNameWithAlias();
            fireNameChange("", name);
            fireDisplayNameChange("", name);
        }
    }
    
    public void onDrawingAreaPostDrop(
            IDiagram pParentDiagram,
            IDrawingAreaDropContext pContext,
            IResultCell cell)
    {
    }
    
    public void onDrawingAreaPostCreated(
            IDrawingAreaControl pDiagramControl, IResultCell cell)
    {
        registerListeners();
    }
    
    public void onDrawingAreaOpened(IDiagram parentDiagram, IResultCell cell)
    {
        registerListeners();
        if (getCookieSet().getCookie(DiagramPrintCookie.class) == null)
            getCookieSet().add(getPrintCookie());
    }
    
    public void onDrawingAreaKeyDown(
            IDiagram pParentDiagram,
            int nKeyCode,
            boolean bControlIsDown,
            boolean bShiftIsDown,
            boolean bAltIsDown,
            IResultCell cell)
    {}
    
    public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
    {}
    
    public void onDrawingAreaClosed(
            IDiagram parentDiagram, boolean isDirty, IResultCell cell)
    {
        if (parentDiagram.getFilename()
        .equals(mDiagram.getDiagram().getFilename()))
        {
            getCookieSet().remove(getPrintCookie());
            
            unregisterListeners();
        }
    }
    
    public void onDrawingAreaActivated(
            IDiagram pParentDiagram, IResultCell cell)
    {}
    
    
    protected boolean save(IDiagram diag, boolean confirm)
    {
        return save(diag, confirm, null);
    }
    
    
    protected boolean save(IDiagram diag, boolean confirm, String dialogMsg)
    {
        //prompt to save..
        String name = getDiagram().getName();
        boolean confirmSaveFromDialog = userConfirmedSave(name, dialogMsg);
        
        if (getDiagram() != null && getDiagram().getDiagram().getIsDirty() &&
                (!confirm || confirmSaveFromDialog))
        {
            diag.save();
            return true;
        }
        
        // cvc - 6263501
        // if user canceled/escaped/closed out of Save dialog (didn't click Yes),
        //  then we want to keep the diagram dirty so that when they close
        //  again, they will be prompted again.
        else if (!confirmSaveFromDialog && !bCancelSaveDialog)
        {
            // do NOT save the diagram
            getDiagram().getDiagram().setIsDirty(false);
            return false;
        }
        
        return false;
    }
    
    
    private static boolean alwaysSaveProject = false;
    
    private boolean userConfirmedSave(String name, String dialogMsg)
    {
        //Kris Richards - this preference is now always "PSK_YES"

        
        String title = NbBundle.getMessage(UMLDiagramNode.class,
                "LBL_DIALOG_TITLE_SaveDiagram"); // NOI18N
        
        if (dialogMsg == null || dialogMsg.equals("")) // NOI18N
        {
            dialogMsg = NbBundle.getMessage(
                UMLDiagramNode.class,
                "LBL_DIALOG_MSG_RenamePreSaveDiagram",  // NOI18N
                getDiagram().getDiagram().getName());
        }
        
        boolean success = false;
        
        // prompt user to save the target UML diagram
        
        Object result = SaveNotifierOkCancel.getDefault()
            .displayNotifier(title, dialogMsg);
        
        if (result == NotifyDescriptor.OK_OPTION)
            success = true;
        
        else // cancel or closed (x button)
        {
            success = false;
            bCancelSaveDialog = true;
        }
        
        return success;
    }

    
    public class DiagramCookie implements Cookie
    { 
        public IProxyDiagram getDiagram()
        {
            return UMLDiagramNode.this.getDiagram();
        }
    }
    
    
    public class DiagramPrintCookie implements PrintCookie
    {
        
        public void print()
        {
            IProjectTreeItem item = getData();
            IProductDiagramManager pDiaMgr =
                    ProductHelper.getProductDiagramManager();
            
            if (item != null && pDiaMgr != null)
            {
                IProxyDiagram proxyDia = item.getDiagram();
                if (proxyDia != null)
                {
                    IDiagram diagram = proxyDia.getDiagram();
                    if (diagram != null)
                    {
                        diagram.printGraph(true);
                    }
                }
            }
        }
    }
    
    public boolean isListenersRegistered()
    {
        return listenersRegistered;
    }
    
    public void setListenersRegistered(boolean listenersRegistered)
    {
        this.listenersRegistered = listenersRegistered;
    }
    
    public DispatchHelper getDispatchHelper()
    {
        if (dispatchHelper == null)
            dispatchHelper = new DispatchHelper();
        
        return dispatchHelper;
    }
}
