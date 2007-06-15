/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteCategoryID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteItemID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPalettePlugin;
import org.netbeans.modules.compapp.casaeditor.api.PluginDropHandler;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphUtilities;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetEngineExternal;
import org.netbeans.modules.compapp.casaeditor.graph.CasaRegionWidget;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.JBIServiceUnitTransferObject;
import org.netbeans.modules.compapp.casaeditor.palette.CasaCommonAcceptProvider;
import org.netbeans.modules.compapp.casaeditor.palette.CasaPalette;
import org.netbeans.modules.compapp.casaeditor.palette.DefaultPluginDropHandler;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 *author
 * @author rdara
 */
public class CasaPaletteAcceptProvider extends CasaCommonAcceptProvider {
    
    private CasaWrapperModel mModel;
    private List<String> artifactTypes = new ArrayList<String>();
    private static final DataFlavor genericDataFlavor = new DataFlavor( Object.class, "whatever" );
    
    public CasaPaletteAcceptProvider(CasaModelGraphScene scene, CasaWrapperModel model) {
        super(scene);
        mModel = model;
        artifactTypes.add(JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA);
    }
    
    
    private String getJbiProjectType(Project p) {
        if (p == null) {
            return null;
        }

        // todo: 06/08/07, allow JavaEE DnD in CASA
        if (JbiDefaultComponentInfo.isJavaEEProject(p)) {
            return (JbiProjectConstants.JAVA_EE_SE_COMPONENT_NAME);
        }

        AntArtifactProvider prov = (AntArtifactProvider)p.getLookup().lookup(AntArtifactProvider.class);
        if (prov != null) {
            AntArtifact[] artifacts = prov.getBuildArtifacts();
            Iterator<String> artifactTypeItr = null;
            String artifactType = null;
            if (artifacts != null) {
                for (int i = 0; i < artifacts.length; i++) {
                    artifactTypeItr = this.artifactTypes.iterator();
                    while (artifactTypeItr.hasNext()){
                        artifactType = artifactTypeItr.next();
                        String arts = artifacts[i].getType();
                        if (arts.startsWith(artifactType)) {
                            int idx = arts.indexOf(':') + 1;
                            return arts.substring(idx);
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable){
        ConnectorState retState = ConnectorState.REJECT;
        try {
            if (transferable.isDataFlavorSupported(CasaPalette.CasaPaletteDataFlavor)) {
                CasaPaletteItemID itemID = (CasaPaletteItemID) transferable.getTransferData(CasaPalette.CasaPaletteDataFlavor);
                if (itemID != null) {
                    retState = isAcceptableFromPalette(point, itemID);
                }
            } else {
                retState = isAcceptableFromOther(point, transferable, false);
            }
        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retState;
    }
    
    private ConnectorState isAcceptableFromPalette(Point point, CasaPaletteItemID itemID) {
        CasaRegionWidget region = getApplicableRegion(itemID);
        ConnectorState retState = ConnectorState.REJECT;
        if (
                region != null &&
                region.getBounds().contains(region.convertSceneToLocal(point))) {
            retState = ConnectorState.ACCEPT;
        }
        return retState;
    }
    
    private ConnectorState isAcceptableFromOther(Point point, Transferable transferable, boolean bIgnoreRegionCheck) 
    throws Exception {
        CasaRegionWidget region = null; 
        ConnectorState curState = ConnectorState.REJECT;
        for(Object dfo : getTransferableObjects(transferable)) {
            curState = ConnectorState.REJECT;
            if (dfo instanceof Node) {
                region = getScene().getEngineRegion();
                DataObject obj = (DataObject) ((Node) dfo).getCookie(DataObject.class);
                Project p = getProjectFromDataObject(obj); // ProjectManager.getDefault().findProject(obj.getPrimaryFile());
                if (getJbiProjectType(p) != null) {
                    String pname = p.getProjectDirectory().getName();
                    // todo: 01/24/07 needs to check for duplicates...
                    if (!mModel.existingServiceEngineServiceUnit(pname)) {
                        curState = ConnectorState.ACCEPT;
                    } 
                } 
            } else if(dfo instanceof List) {
                List dfoList = (List) dfo;
                if(dfoList.size() == 3 &&
                    dfoList.get(0) instanceof String &&
                    dfoList.get(0).equals("JBIMGR_SU_TRANSFER")) {
                    region = getScene().getExternalRegion();
                    String projName /*suName*/ = (String) dfoList.get(1); // FIXME: 
                    if (!mModel.existingServiceEngineServiceUnit(projName)) { // FIXME: existingExternalServiceUnit?
                        curState = ConnectorState.ACCEPT;
                    } 
                }
            } else {
                curState = ConnectorState.REJECT;
            }
            if(curState == ConnectorState.REJECT) {
                break;
            }
        }
        if(curState == ConnectorState.ACCEPT) { //Check further whether its droppable region and visual feedback needed
            if(region != null) {
                if(bIgnoreRegionCheck) {    //HighLight region!
                    highlightRegion(region);
                } else {
                    if (!region.getBounds().contains(region.convertSceneToLocal(point))) {  //Check for region bounds
                        curState = ConnectorState.REJECT;
                    }
                } 
            }
        }
        return curState;
    }
    
    /* Receive all transferable objects */
    private List getTransferableObjects(Transferable transferable) {
        List retList = new ArrayList();
        DataFlavor[] dfs = transferable.getTransferDataFlavors();
        if (dfs.length > 0) {
            try {
                if(dfs[0].getRepresentationClass().equals(MultiTransferObject.class)){
                    MultiTransferObject mto = (MultiTransferObject)transferable.getTransferData(dfs[0]);
                    if(mto.getCount() > 0) {
                        DataFlavor[] df = mto.getTransferDataFlavors(0);
                        if(df.length > 0) {
                            for(int i = 0; i < mto.getCount(); i++) {
                                if (transferable.isDataFlavorSupported(genericDataFlavor)) {
                                    retList.add(mto.getTransferData(i, genericDataFlavor));
                                } else {
                                    retList.add(mto.getTransferData(i, df[0]));
                                }
                            }
                        }
                    }
                } else {
                    if (transferable.isDataFlavorSupported(genericDataFlavor)) {
                        retList.add(transferable.getTransferData(genericDataFlavor));                        
                    } else {
                        retList.add(transferable.getTransferData(dfs[0]));
                    }
                }
            } catch (UnsupportedFlavorException ex) {
                ex.printStackTrace();
            }  catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return retList;
    }
    
    public void accept(Widget widget, Point point, Transferable transferable) {
        try {
            if (transferable.isDataFlavorSupported(CasaPalette.CasaPaletteDataFlavor)) {
                CasaPaletteItemID itemID = (CasaPaletteItemID) transferable.getTransferData(CasaPalette.CasaPaletteDataFlavor);
                if (itemID != null) {
                    acceptFromPalette(point, itemID);
                }
            } else {
                acceptFromOther(point, transferable);
            }
        } catch (Throwable t) {
            // Catch all exceptions, including those from the model.
            // There must be visual feedback of an error if the drop failed.
            ErrorManager.getDefault().notify(t);
        }
    }
    
    private void acceptFromPalette(Point point, CasaPaletteItemID itemID) {
        PluginDropHandler handler = new DefaultPluginDropHandler(getScene(), point);
        itemID.getCategory().getPlugin().handleDrop(handler, itemID);
    }
    
    private void acceptFromOther(Point point, Transferable transferable)
    throws Exception {
        for(Object dfo : getTransferableObjects(transferable)) {
            if (dfo instanceof Node) {
                DataObject obj = (DataObject) ((Node) dfo).getCookie(DataObject.class);
                Project p = getProjectFromDataObject(obj); // ProjectManager.getDefault().findProject(obj.getPrimaryFile());
                String type = getJbiProjectType(p);
                point = getScene().getEngineRegion().convertSceneToLocal(point);
                mModel.addInternalJBIModule(p, type, point.x, point.y);
            } else if(dfo instanceof List) {
                List dfoList = (List) dfo;
                if (dfoList.size() == 3 &&
                    dfoList.get(0) instanceof String &&
                    dfoList.get(0).equals("JBIMGR_SU_TRANSFER")) {
                
                    final JBIServiceUnitTransferObject suTransfer =
                            new JBIServiceUnitTransferObject(
                            (String) dfoList.get(1),
                            (String) dfoList.get(2));

                    point = getScene().getExternalRegion().convertSceneToLocal(point);
                    String suName = suTransfer.getServiceUnitName();
                    final CasaServiceEngineServiceUnit seSU = 
                            mModel.addServiceEngineServiceUnit(
                            suName, "", false, false, point.x, point.y); // NOI18N // FIXME

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {  
                            mModel.addEndpointsToServiceEngineServiceUnit(suTransfer, seSU);    
                        }
                    });
                }
            }
        }
    }

    // todo: 02/15/07 fix the problem created by bpel project changes...
    private Project getProjectFromDataObject(DataObject obj) throws Exception {
        if (obj == null) {
            return null;
        }
        ProjectManager pm = ProjectManager.getDefault();
        Project p = null;
        for (FileObject fo=obj.getPrimaryFile(); fo != null; fo=fo.getParent()) {
            
            if(fo.isFolder() && fo != null) {
                p = pm.findProject(fo);
                if (p != null) {
                    return p;
                }
            }
        }
        return p;
    }
    
    
    public void acceptStarted(Transferable transferable) {
        super.acceptStarted(transferable);
        if (transferable.isDataFlavorSupported(CasaPalette.CasaPaletteDataFlavor)) {
            CasaPaletteItemID itemID = getCasaPaletteItem(transferable);
            CasaRegionWidget region = getApplicableRegion(itemID);
            if (region == null) {
                if (itemID != null) {
                    if (itemID.getCategory().equals(CasaPalette.CATEGORY_ID_END_POINTS)) {
                        highlightExtSUs(true);
                    }
                }
            } else {
                highlightRegion(region); 
            }
       } else {
            try {
               isAcceptableFromOther(null, transferable, true);    //Also highlights the region
            }
            catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
       }
    }
    
    public void acceptFinished() {
        super.acceptFinished();
        highlightExtSUs(false);
        getScene().getBindingRegion().setHighlighted(false);
        getScene().getEngineRegion().setHighlighted(false);
        getScene().getExternalRegion().setHighlighted(false);
    }
    
    private void highlightRegion(CasaRegionWidget region) {
        region.setHighlighted(true);
        CasaModelGraphUtilities.ensureVisibity(region);
    }
    
    private void highlightExtSUs(boolean bValue) {
        for (Widget widget : getScene().getExternalRegion().getChildren()) {
            if (widget instanceof CasaNodeWidgetEngineExternal) {
                ((CasaNodeWidgetEngineExternal) widget).setHighlighted(bValue);
            }
        }
        if (bValue) {
            CasaModelGraphUtilities.ensureVisibity(getScene().getExternalRegion());
        }
    }
    
    private CasaRegionWidget getApplicableRegion(CasaPaletteItemID itemID) {
        CasaPalettePlugin.REGION regionID = 
                itemID.getCategory().getPlugin().getDropRegion(itemID);
        if (regionID == null) {
            return null;
        }
        
        CasaRegionWidget region = null;
        switch(regionID) {
            case WSDL_ENDPOINTS:
                region = getScene().getBindingRegion();
                break;
            case JBI_MODULES:
                region = getScene().getEngineRegion();
                break;
            case EXTERNAL:
                region = getScene().getExternalRegion();
                break;
        }
        return region;
    }
    
    private CasaPaletteItemID getCasaPaletteItem(Transferable transferable) {
        CasaPaletteItemID itemID = null;
        if (transferable.isDataFlavorSupported(CasaPalette.CasaPaletteDataFlavor)) {
            try {
                itemID = (CasaPaletteItemID) transferable.getTransferData(CasaPalette.CasaPaletteDataFlavor);
            } catch (UnsupportedFlavorException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return itemID;
    }
}
