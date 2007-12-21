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

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
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
import org.netbeans.modules.compapp.projects.jbi.api.JbiInstalledProjectPluginInfo;
import org.netbeans.modules.compapp.projects.jbi.api.InternalProjectTypePlugin;
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
    private static final DataFlavor genericDataFlavor = new DataFlavor( Object.class, "whatever" );
    
    
    public CasaPaletteAcceptProvider(CasaModelGraphScene scene, CasaWrapperModel model) {
        super(scene);
        mModel = model;
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

    private InternalProjectTypePlugin getProjectPluginHandler(Object src) {
        JbiInstalledProjectPluginInfo plugins = JbiInstalledProjectPluginInfo.getProjectPluginInfo();
        if (plugins != null) {
            List<InternalProjectTypePlugin> plist = plugins.getUncategorizedProjectPluginList();
            for (InternalProjectTypePlugin plugin : plist) {
                if (plugin.isAcceptableProjectSource(src) == true) {
                    return plugin;
                }
            }
        }
        return null;
    }
    
    private ConnectorState isAcceptableFromOther(Point point, Transferable transferable, boolean bIgnoreRegionCheck) 
    throws Exception {
        CasaRegionWidget region = null; 
        ConnectorState curState = ConnectorState.REJECT;
        for(Object dfo : getTransferableObjects(transferable)) {
            curState = ConnectorState.REJECT;
            if (dfo instanceof Node) {
                region = getScene().getEngineRegion();
                Project p = ((Node) dfo).getLookup().lookup(Project.class);
                /*
                if (p == null) {
                    DataObject obj = (DataObject) ((Node) dfo).getCookie(DataObject.class);
                    p = getProjectFromDataObject(obj); // ProjectManager.getDefault().findProject(obj.getPrimaryFile());
                }
                if (mModel.getJbiProjectType(p) != null) {
                    String pname = p.getProjectDirectory().getName();
                    // todo: 01/24/07 needs to check for duplicates...
                    if (!mModel.existingServiceEngineServiceUnit(pname)) {
                        curState = ConnectorState.ACCEPT;
                    } 
                }
                */
                if (p != null) { // standard NetBeans projects...
                    if (mModel.getJbiProjectType(p) != null) {
                        String pname = p.getProjectDirectory().getName();
                        // todo: 01/24/07 needs to check for duplicates...
                        if (!mModel.existingServiceEngineServiceUnit(pname)) {
                            curState = ConnectorState.ACCEPT;
                        }
                    }
                } else { // nonstandard Nb projects, e.g., jcaps
                    InternalProjectTypePlugin plugin = getProjectPluginHandler(dfo);
                    if (plugin != null) {
                        String pname = ((Node) dfo).getDisplayName();
                        if (!mModel.existingServiceEngineServiceUnit(pname)) {
                            curState = ConnectorState.ACCEPT;
                        }
                    }
                }

            } else if(dfo instanceof List) {
                List dfoList = (List) dfo;
                if(dfoList.size() == 5 &&
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
        List<Object> retList = new ArrayList<Object>();
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
        try {
            itemID.getPlugin().handleDrop(handler, itemID);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    private void acceptFromOther(Point point, Transferable transferable)
    throws Exception {
        for(Object dfo : getTransferableObjects(transferable)) {
            if (dfo instanceof Node) {
                Project p = ((Node) dfo).getLookup().lookup(Project.class);
                /*
                if (p == null) {
                    DataObject obj = (DataObject) ((Node) dfo).getCookie(DataObject.class);
                    p = getProjectFromDataObject(obj); // ProjectManager.getDefault().findProject(obj.getPrimaryFile());
                }
                String type = mModel.getJbiProjectType(p);
                point = getScene().getEngineRegion().convertSceneToLocal(point);
                mModel.addJBIModule(p, type, point.x, point.y);
                */
                if (p != null) { // standard NetBeans projects
                    String type = mModel.getJbiProjectType(p);
                    point = getScene().getEngineRegion().convertSceneToLocal(point);
                    mModel.addJBIModule(p, type, point.x, point.y);
                }  else { // non-standard project, e.g., jcaps
                    InternalProjectTypePlugin plugin = getProjectPluginHandler(dfo);
                    if (plugin != null) {
                        String pname = ((Node) dfo).getDisplayName();
                        String type = plugin.getJbiTargetName();
                        point = getScene().getEngineRegion().convertSceneToLocal(point);
                        mModel.addJBIModuleFromPlugin(plugin, pname, type, point.x, point.y);
                    }
                }

            } else if(dfo instanceof List) {
                List dfoList = (List) dfo;
                if (dfoList.size() == 5 &&
                    dfoList.get(0) instanceof String &&
                    dfoList.get(0).equals("JBIMGR_SU_TRANSFER")) { // NOI18N
                
                    final JBIServiceUnitTransferObject suTransfer =
                            new JBIServiceUnitTransferObject(
                            (String) dfoList.get(1),
                            (String) dfoList.get(2),
                            (String) dfoList.get(3),
                            (String) dfoList.get(4));

                    point = getScene().getExternalRegion().convertSceneToLocal(point);
                    String suName = suTransfer.getServiceUnitName();
                    String suDescription = suTransfer.getServiceUnitDescription();
                    String compName = suTransfer.getComponentName();
                    final CasaServiceEngineServiceUnit seSU = 
                            mModel.addServiceEngineServiceUnit(
                            suName, compName, suDescription, false, false, true, point.x, point.y); 

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {  
                            mModel.addExternalEndpoints(suTransfer, seSU);    
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
                itemID.getPlugin().getDropRegion(itemID);
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
