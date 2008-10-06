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

package org.netbeans.modules.uml.project.ui.nodes;

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
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import javax.swing.Action;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.common.ui.SaveNotifierOkCancel;
import org.netbeans.modules.uml.common.ui.SaveNotifierYesNo;
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
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Trey Spiva
  */ //TODO
@SuppressWarnings("unchecked")
public class UMLDiagramNode extends UMLElementNode
        implements ITreeDiagram    //, IDrawingAreaEventsSink
{
    private IProxyDiagram mDiagram = null;
    private String mDiagramType = ELEMENT_TYPE_DIAGRAM;
    private DiagramPrintCookie mPrintCookie = null;
    private DispatchHelper dispatchHelper = null;
    private boolean bCancelSaveDialog = false;
    private boolean listenersRegistered = false;
    //
    public static final String ETLD_EXTENSION = "etld"; // NOI18N
    public static final String ETLP_EXTENSION = "etlp"; // NOI18N
    
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
        
//        getDispatchHelper().registerDrawingAreaEvents(this);
        setListenersRegistered(true);
    }
    
    public void unregisterListeners()
    {
//        getDispatchHelper().revokeDrawingAreaSink(this);
        setListenersRegistered(false);
    }
    
    
    // enables the node to be renamed
    public boolean canRename()
    {
        return !isOldDiagramFormat();
    }

    @Override
    public boolean canCopy()
    {
        return !isOldDiagramFormat();
    }

    @Override
    public boolean canCut() {
        return !isOldDiagramFormat();
    }
    
    @Override
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
    
    @Override
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
    
    
    @Override
    public Action getPreferredAction()
    {
        // disable double click open action for those unsupported diagram types for 6.5 M1
        int kind = getDiagram().getDiagramKind();
        if (kind != IDiagramKind.DK_COLLABORATION_DIAGRAM &&
            kind != IDiagramKind.DK_COMPONENT_DIAGRAM &&
            kind != IDiagramKind.DK_DEPLOYMENT_DIAGRAM )
            return super.getPreferredAction();
        else
            return null;
    }
    
    
    @Override
    public Action[] getActions(boolean context)
    {
        ArrayList<Action> actions = new ArrayList <Action>();
        
        int kind = getDiagram().getDiagramKind();
        // temporarily take out open action from those three diagram types for 6.5 M1
        if (kind != IDiagramKind.DK_COLLABORATION_DIAGRAM &&
            kind != IDiagramKind.DK_COMPONENT_DIAGRAM &&
            kind != IDiagramKind.DK_DEPLOYMENT_DIAGRAM )
        {
            actions.add(SystemAction.get(OpenAction.class));
            actions.add(null);
        }
        
        // see #102294
        if ( kind != IDiagramKind.DK_SEQUENCE_DIAGRAM &&
                kind != IDiagramKind.DK_COLLABORATION_DIAGRAM && !isOldDiagramFormat())
        {
            actions.add(SystemAction.get(CopyDiagramAction.class));
        }
        actions.add(SystemAction.get(RenameAction.class));
        actions.add(SystemAction.get(DeleteAction.class));
        actions.add(null);
        addContextMenus(actions);
        actions.add(null);
        actions.add(SystemAction.get(PropertiesAction.class));

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
//            String filename = StringUtilities
//                    .ensureExtension(value, FileExtensions.DIAGRAM_LAYOUT_EXT);
//            
//            if (filename.length() > 0)
//            {
//                data.setDescription(filename);
//            }
            data.setDescription(value);
        }
    }
    
    @Override
    public boolean equals(Object obj)
    {
        boolean retVal = false;
        
        if ( (obj != null) && (this.hashCode() == obj.hashCode()))
            return true;
        
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
        // TODO: meteora
//        else if (obj instanceof IDrawingAreaControl)
//        {
//            IDrawingAreaControl control = (IDrawingAreaControl)obj;
//            
//            String testDescription = control.getFilename();
//            String myDescription   = getData().getDescription();
//            
//            retVal = myDescription.equals(testDescription);
//        }
        
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
    
    @Override
    public String getType()
    {
        return mDiagramType;
    }
    
    
    
    public void setName(String newName)
    {
        if (!Util.isDiagramNameValid(newName))
        {
            NotifyDescriptor.Message msg =
                    new NotifyDescriptor.Message(NbBundle.getMessage(
                                                 AddPackageVisualPanel1.class,
                                                 "MSG_Invalid_Diagram_Name", newName)); // NOI18N
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        String nodeName = getName();  // get the node's name
        IProxyDiagram proxyDiagram = getDiagram();
        IDiagram diagram = null;
        String diagramName = null;
        boolean proceed = true;
        
        if (proxyDiagram != null )
        {
            if (nodeName.equals(""))  // this's the case when a diagram is first open
            {   
                setNodeName (nodeName, newName);
                return;
            }
            diagramName = proxyDiagram.getName();  // get the diagram's name
            if ( proxyDiagram.isOpen())
            {
                diagram = proxyDiagram.getDiagram();  // get UIDiagram object
                boolean dirty =  diagram.isDirty();
                
                // take care the diagram name first
                if (!newName.equals(diagramName)) // diagram name not equals to new name
                {
                    if ( dirty )   // ask users if they want to save diagram before changing name
                    {
                         proceed = (askToSaveDiagram(diagramName) == UMLDiagramNode.RESULT_YES);
                    }
                    
                    proceed = (!dirty || proceed);
                    
                    if (proceed) // go ahead changing the diagram name and save the diagram
                    {
                        try
                        {   // change diagram name and diagram tab name
                            proxyDiagram.setName(newName);
                            if (ProductHelper.getShowAliasedNames())
                            {
                                proxyDiagram.setAlias(newName);
                            }
                            diagram.save(); // autosave diagram after rename
                        }
                        catch (IOException ex)
                        {
                            Exceptions.printStackTrace(ex);
                        }
                     }
                }
                
                // take care the diagram node on project tree
                if (!getName().equals(newName) &&  proceed)  
                {
                    setNodeName (diagramName, newName);
                }
            }
            else // Diagram is not open
            {
                // TODO: update the diagram name from diagram file using file IO.
                // Since the diagram is not open, no need to check for diagram dirty state.
                proxyDiagram.setName(newName);
                setNodeName (nodeName, newName);
            }
        }
    }
    
    private void setNodeName (String oldName, String newName)
    {
        // change diagram node name
        super.setName(newName); 
        super.setDisplayName(newName);
        getData().setItemText(newName);
        fireNameChange(oldName, newName);
        fireDisplayNameChange(oldName, newName);
        firePropertySetsChange(null, retreiveProperties());
    }
    
    
//    public void setDisplayName(String val)
//    {
//        setName(val);
        
//        IProjectTreeItem item = getData(); 
//        if (item != null)
//        {
//            IProxyDiagram dia = item.getDiagram();
//            if (dia != null)
//            {
//                //its an unopen diagram
//                String curName = dia.getName();
//                
//                if (!curName.equals(val))
//                {
//                    // dia.setname() will send UMLDiagramTopComponent events
//                    // to let it takes care of prompting user to save the diagram 
//                    // and to reset the diagram tab name as needed.
//                    dia.setName(val);
//                    
//                    // if the diagram edtor name has been renamed, that is thing
//                    // went smoothly, go ahead conitnue with the name change process.
//                    if (val.equals(dia.getName()) ) 
//                    {
//                        if(ProductHelper.getShowAliasedNames())
//                        {
//                            dia.setAlias(val);
//                        }
//                        //dia.setNameWithAlias(val);
//
//                        // cvc - CR 6275795
//                        // autosave the diagram after the rename
//                        IDiagram diagram = dia.getDiagram();
//                        if (diagram != null)
//                        {
//                            try
//                            {
//                                diagram.save();  // save diagram
//
//                            } 
//                            catch (IOException e)
//                            {
//                                Exceptions.printStackTrace(e);
//                            }
//                        }
//                    }
//                }
//            } 
//        }
//    }
    
    private static final int RESULT_CANCEL = 0;
    private static final int RESULT_YES = 1;
    
    private int askToSaveDiagram(String diagramName)
    {
        String title = NbBundle.getMessage(UMLDiagramNode.class,
                "LBL_DIALOG_TITLE_SaveDiagram"); // NOI18N
        
        String message = NbBundle.getMessage(
                UMLDiagramNode.class,
                "LBL_DIALOG_MSG_RenamePreSaveDiagram", // NOI18N
                diagramName); 
        
        int result = RESULT_CANCEL;
        
        Object response = SaveNotifierYesNo.getDefault().displayNotifier(
                title, message, SaveNotifierYesNo.SAVE_CANCEL);
        
        if (response == NotifyDescriptor.YES_OPTION)
        {
            result = RESULT_YES;
        }
        else // cancel or closed (x button)
        {
            result = RESULT_CANCEL;
        }
        
        return result;
    }
    
//    public String getDisplayName()
//    {
//        String retVal = ""; // NOI18N
//        IProjectTreeItem item = getData();
//        
//        if (item != null)
//        {
//            IProxyDiagram dia = item.getDiagram();
//            
//            if (dia != null)
//            {
//                //its an unopen diagram
//                retVal = dia.getNameWithAlias();
//            }
//        }
//        
//        return retVal;
//    }
    
    
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
    
    // The node should not be listening to drawing area events.  It is up to the
    // project tree engine to respond to the events.
//    public void onDrawingAreaTooltipPreDisplay(
//            IDiagram pParentDiagram,
//            IPresentationElement pPE,
//            IToolTipData pTooltip,
//            IResultCell cell)
//    {
//    }
//    
//    public void onDrawingAreaPreSave(
//            IProxyDiagram pParentDiagram, IResultCell cell)
//    {}
//    
//    public void onDrawingAreaPrePropertyChange(
//            IProxyDiagram pProxyDiagram,
//            int nPropertyKindChanged,
//            IResultCell cell)
//    {}
//    
//    public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
//    {}
//    
//    public void onDrawingAreaPreDrop(
//            IDiagram pParentDiagram,
//            IDrawingAreaDropContext pContext,
//            IResultCell cell)
//    {}
//    
//    public void onDrawingAreaPreCreated(
//            IDrawingAreaControl pDiagramControl, IResultCell cell)
//    {}
//    
//    public void onDrawingAreaPostSave(
//            IProxyDiagram pParentDiagram, IResultCell cell)
//    {
//    }
//    
//    public void onDrawingAreaPostPropertyChange(
//            IProxyDiagram pProxyDiagram,
//            int nPropertyKindChanged,
//            IResultCell cell)
//    {
//        if (nPropertyKindChanged == IDrawingAreaPropertyKind.DAPK_NAME &&
//                pProxyDiagram.getXMIID().equals(getDiagram().getXMIID()))
//        {
//            String name = pProxyDiagram.getNameWithAlias();
//            fireNameChange("", name);
//            fireDisplayNameChange("", name);
//        }
//    }
//    
//    public void onDrawingAreaPostDrop(
//            IDiagram pParentDiagram,
//            IDrawingAreaDropContext pContext,
//            IResultCell cell)
//    {
//    }
//    
//    public void onDrawingAreaPostCreated(
//            IDrawingAreaControl pDiagramControl, IResultCell cell)
//    {
//        registerListeners();
//    }
//    
//    public void onDrawingAreaOpened(IDiagram parentDiagram, IResultCell cell)
//    {
//        registerListeners();
//        if (getCookieSet().getCookie(DiagramPrintCookie.class) == null)
//            getCookieSet().add(getPrintCookie());
//    }
//    
//    public void onDrawingAreaKeyDown(
//            IDiagram pParentDiagram,
//            int nKeyCode,
//            boolean bControlIsDown,
//            boolean bShiftIsDown,
//            boolean bAltIsDown,
//            IResultCell cell)
//    {}
//    
//    public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
//    {}
//    
//    public void onDrawingAreaClosed(
//            IDiagram parentDiagram, boolean isDirty, IResultCell cell)
//    {
//        if (parentDiagram.getFilename()
//        .equals(mDiagram.getDiagram().getFilename()))
//        {
//            getCookieSet().remove(getPrintCookie());
//            
//            unregisterListeners();
//        }
//    }
//    
//    public void onDrawingAreaActivated(
//            IDiagram pParentDiagram, IResultCell cell)
//    {}
    
    
    protected boolean save(IDiagram diag, boolean confirm)
    {
        return save(diag, confirm, null);
    }
    
    
    // Thuy rewrites the mehod
//    protected boolean save(IDiagram diag, boolean confirm, String dialogMsg)
//    {
//        //prompt to save..
//        String name = getDiagram().getName();
//            boolean confirmSaveFromDialog = userConfirmedSave(name, dialogMsg);
//        
//        if (getDiagram() != null && getDiagram().getDiagram().isDirty() &&
//                (!confirm || confirmSaveFromDialog))
//            {
//                try
//                {
//                    diag.save();
//            } catch (IOException e)
//                {
//                    Exceptions.printStackTrace(e);
//                }
//                return true;
//            }
//        
//            // cvc - 6263501
//            // if user canceled/escaped/closed out of Save dialog (didn't click Yes),
//            //  then we want to keep the diagram dirty so that when they close
//            //  again, they will be prompted again.
//            else if (!confirmSaveFromDialog && !bCancelSaveDialog)
//            {
//                // do NOT save the diagram
//                getDiagram().getDiagram().setDirty(false);
//                return false;
//            }
//        
//        return false;
//    }
        
    protected boolean save(IDiagram diag, boolean confirm, String dialogMsg)
    {
        //prompt to save..
        boolean isSaved = false;
        //IProxyDiagram pDiagram = getDiagram(); 
        if ( diag != null && diag.isDirty() && 
                dialogMsg != null && dialogMsg.trim().length() > 0 ) 
        {
            //String name = pDiagram.getName();
            boolean confirmSaveFromDialog = userConfirmedSave("", dialogMsg);
        
            if (!confirm || confirmSaveFromDialog)
            {
                try
                {
                    diag.save();
                    isSaved = true;
                } 
                catch (IOException e)
                {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        return isSaved;
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
    private boolean isOldDiagramFormat()
    {
        boolean ret=mDiagram.getFilename().endsWith("."+ETLD_EXTENSION);
        return ret;
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
                        //TODO: Does the diagram need to really know about printgraph? It should be handled by TopComp?
//                        diagram.printGraph(true);
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
