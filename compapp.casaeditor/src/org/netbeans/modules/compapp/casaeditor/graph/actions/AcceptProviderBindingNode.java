/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.io.IOException;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
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
                    String categoryID = itemID.getCategory();
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
                    String categoryID = itemID.getCategory();
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
