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
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.palette.CasaCommonAcceptProvider;
import org.netbeans.modules.compapp.casaeditor.palette.CasaPalette;
import org.netbeans.modules.compapp.casaeditor.palette.CasaPaletteItem;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;

/**
 *
 * @author rdara
 */
public class CasaPaletteAcceptProvider extends CasaCommonAcceptProvider {

    private CasaWrapperModel mModel;
    private List<String> artifactTypes = new ArrayList();
    

    public CasaPaletteAcceptProvider(CasaModelGraphScene scene, CasaWrapperModel model) {
        super(scene);
        
        mModel = model;

        // init
        //artifactTypes.add("jar"); // No I18N
        //artifactTypes.add("war"); // No I18N
        //artifactTypes.add("ear"); // No I18N  TODO ear project does not have AntArtifactProvider
        artifactTypes.add(JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA);
    }

    private String getJbiProjectType(Project p) {
        if (p == null) {
            return null;
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

    public ConnectorState isAcceptable (Widget widget, Point point, Transferable transferable){
        ConnectorState retState = ConnectorState.REJECT;
        LayerWidget region = null;                
        try {
            if (transferable.isDataFlavorSupported(CasaPalette.CasaPaletteDataFlavor)) {
                CasaPaletteItem selNode = (CasaPaletteItem) transferable.getTransferData(CasaPalette.CasaPaletteDataFlavor);
                if(selNode != null) {
                    switch(selNode.getCategory()) {
                        case WSDL_BINDINGS :
                            region = getScene().getBindingRegion();
                            if(region.getBounds().contains(region.convertSceneToLocal(point))){
                                retState = ConnectorState.ACCEPT;
                            }
                            break;
                        case SERVICE_UNITS :
                            if        (CasaPalette.CASA_PALETTE_ITEM_TYPE.INT_SU == selNode.getPaletteItemType()) {
                                region = getScene().getEngineRegion();
                            } else if (CasaPalette.CASA_PALETTE_ITEM_TYPE.EXT_SU == selNode.getPaletteItemType()) {
                                region = getScene().getExternalRegion();
                            }
                            if (
                                    region != null && 
                                    region.getBounds().contains(region.convertSceneToLocal(point))){
                                retState = ConnectorState.ACCEPT;
                            }
                            break;
                        default:
                            retState = ConnectorState.REJECT;
                    }
                }
            } else { // check for SU project node
                DataFlavor[] dfs = transferable.getTransferDataFlavors();
                region = getScene().getEngineRegion();
                if(region.getBounds().contains(region.convertSceneToLocal(point))){
                    if (dfs.length > 0) {
                        Object dfo = transferable.getTransferData(dfs[0]);
                        if (dfo instanceof Node) {
                            try {
                                DataObject obj = (DataObject) ((Node) dfo).getCookie(DataObject.class);
                                Project p = getProjectFromDataObject(obj); // ProjectManager.getDefault().findProject(obj.getPrimaryFile());
                                if (getJbiProjectType(p) != null) {
                                    // todo: 01/24/07 needs to check for duplicates...
                                    if (mModel.existingServiceUnit(obj.getName())) {
                                        retState = ConnectorState.REJECT;
                                    } else {
                                        retState = ConnectorState.ACCEPT;
                                    }
                                }
                            } catch (Exception ex) { // bad data objects..
                                retState = ConnectorState.REJECT;
                            }
                        }
                    }
                }
            }
        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return retState;
    }

    public void accept(Widget widget, Point point, Transferable transferable) {
        try {
            if (transferable.isDataFlavorSupported(CasaPalette.CasaPaletteDataFlavor)) {
                CasaPaletteItem selNode = 
                        (CasaPaletteItem) transferable.getTransferData(CasaPalette.CasaPaletteDataFlavor);
                if (selNode != null) {
                    acceptFromPalette(widget, point, selNode);
                }
            } else {
                acceptFromOther(widget, point, transferable);
            }
        } catch (Throwable t) {
            // Catch all exceptions, including those from the model.
            // There must be visual feedback of an error if the drop failed.
            ErrorManager.getDefault().notify(t);
        }
    }
    
    private void acceptFromPalette(Widget widget, Point point, CasaPaletteItem selNode) {
        switch(selNode.getCategory()) {
            case WSDL_BINDINGS :
                point = getScene().getBindingRegion().convertSceneToLocal(point);
                mModel.addCasaPort(
                        selNode.getTitle(),
                        selNode.getComponentName(),
                        point.x,
                        point.y);
                break;
            case SERVICE_UNITS :
                if        (CasaPalette.CASA_PALETTE_ITEM_TYPE.INT_SU == selNode.getPaletteItemType()) {
                    // add an internal SU to the model
                    point = getScene().getEngineRegion().convertSceneToLocal(point);
                    mModel.addUnknownEngineServiceUnit(true, point.x, point.y);
                } else if (CasaPalette.CASA_PALETTE_ITEM_TYPE.EXT_SU == selNode.getPaletteItemType()) {
                    // add an external SU to the model
                    point = getScene().getExternalRegion().convertSceneToLocal(point);
                    mModel.addUnknownEngineServiceUnit(false, point.x, point.y);
                }
                break;
            default:
                break;
        }
    }
    
    private void acceptFromOther(Widget widget, Point point, Transferable transferable) 
    throws Exception {
        // check for SU project node
        DataFlavor[] dfs = transferable.getTransferDataFlavors();
        if (dfs.length > 0) {
            Object dfo = transferable.getTransferData(dfs[0]);
            if (dfo instanceof Node) {
                DataObject obj = (DataObject) ((Node) dfo).getCookie(DataObject.class);
                Project p = getProjectFromDataObject(obj); // ProjectManager.getDefault().findProject(obj.getPrimaryFile());
                String type = getJbiProjectType(p);
                point = getScene().getEngineRegion().convertSceneToLocal(point);
                mModel.addInternalJBIModule(p, type, point.x, point.y);
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
            p = pm.findProject(fo);
            if (p != null) {
                return p;
            }
        }

        return p;
    }
}
