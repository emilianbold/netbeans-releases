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
import java.io.IOException;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteCategoryID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteItemID;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetBinding;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.palette.CasaCommonAcceptProvider;
import org.netbeans.modules.compapp.casaeditor.palette.CasaPalette;
import org.openide.ErrorManager;

/**
 * As no pins can be droppable on WSDL prots, this class is no more needed.
 * @author rdara
 */
public class AcceptProviderBindingNode extends CasaCommonAcceptProvider {
    
    
    public AcceptProviderBindingNode(CasaNodeWidgetBinding node) {
        super((CasaModelGraphScene) node.getScene());
    }
    
    
    public ConnectorState isAcceptable (Widget widget, Point point, Transferable transferable){
        ConnectorState retState = ConnectorState.REJECT;
        
        try {
            if (transferable.isDataFlavorSupported(CasaPalette.CasaPaletteDataFlavor)) {
                CasaPaletteItemID itemID = (CasaPaletteItemID) transferable.getTransferData(CasaPalette.CasaPaletteDataFlavor);
                if (itemID != null) {
                    CasaPaletteCategoryID categoryID = itemID.getCategory();
                    if (categoryID.equals(CasaPalette.CATEGORY_ID_END_POINTS)) {
                        if (canAddEndpoint(widget, itemID)) {
                            retState = ConnectorState.ACCEPT;
                        }
                    } else {
                        retState = ConnectorState.REJECT;
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
                CasaPaletteItemID itemID = (CasaPaletteItemID) transferable.getTransferData(CasaPalette.CasaPaletteDataFlavor);
                if (itemID != null) {
                    CasaPaletteCategoryID categoryID = itemID.getCategory();
                    if (categoryID.equals(CasaPalette.CATEGORY_ID_END_POINTS)) {
                        addEndpoint(widget, itemID);
                    }
                }
            }
        } catch (Throwable t) {
            // Catch all exceptions, including those from the model.
            // There must be visual feedback of an error if the drop failed.
            ErrorManager.getDefault().notify(t);
        }
    }

    private boolean canAddEndpoint(Widget widget, CasaPaletteItemID type) {
        CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
        CasaPort casaPort = (CasaPort) scene.findObject(widget);
        if        (type.equals(CasaPalette.ITEM_ID_CONSUME)) {
            return casaPort.getConsumes() == null;
        } else if (type.equals(CasaPalette.ITEM_ID_PROVIDE)) {
            return casaPort.getProvides() == null;
        }
        return false;
    }
    
    private void addEndpoint(Widget widget, CasaPaletteItemID type) {
        CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
        CasaPort casaPort = (CasaPort) scene.findObject(widget);
        // This is not allowed any more.
//        if        (type.equals(CasaPalette.ITEM_ID_CONSUME)) {
//            scene.getModel().addEndpointToCasaPort(casaPort, true);
//        } else if (type.equals(CasaPalette.ITEM_ID_PROVIDE)) {
//            scene.getModel().addEndpointToCasaPort(casaPort, false);
//        }
    }
}
