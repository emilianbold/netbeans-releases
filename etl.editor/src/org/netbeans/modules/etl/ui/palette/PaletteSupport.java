/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */

package org.netbeans.modules.etl.ui.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.etl.ui.ETLNode;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorXmlInfoModel;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author nithya
 */

public class PaletteSupport {
    
    public static final String MASHUP_DATA_FLAVOR = DataFlavor.javaJVMLocalObjectMimeType;
    
    private static PaletteController controller;
    
    private static ETLCollaborationTopComponent topComp;
    
    public PaletteSupport(ETLCollaborationTopComponent tc) {
        PaletteSupport.topComp = tc;
    }
    
    /**
     * 
     * @return 
     */
    public static PaletteController createPalette() throws IOException {
        controller =  PaletteFactory.createPalette("ETLOperators", new ETLAction(),
                null, new ETLDnDHandler());
        controller.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if(PaletteController.PROP_SELECTED_ITEM.equals(evt.getPropertyName())){
                    Lookup selItem = controller.getSelectedItem();
                    IGraphView graphView = topComp.getGraphView();
                    if(null != selItem ){
                        Node selNode = selItem.lookup(Node.class);
                        if(null != selNode ){
                            IOperatorXmlInfo opXmlInfo = OperatorXmlInfoModel.getInstance("ETLOperators").findOperatorXmlInfo(selNode.getName());
                            graphView.setXMLInfo(opXmlInfo);
                        }
                    }
                }
            }
});
            return controller;
    }
    
    public static class ETLAction extends PaletteActions {
        /**
         * 
         * @return 
         */
        public Action[] getImportActions() {
            return null;
        }
        /**
         * 
         * @return 
         */
        public Action[] getCustomPaletteActions() {
            return null;
        }
        /**
         * 
         * @param lookup 
         * @return 
         */
        public Action[] getCustomCategoryActions(Lookup lookup) {
            return null;
        }
        /**
         * 
         * @param lookup 
         * @return 
         */
        public Action[] getCustomItemActions(Lookup lookup) {
            return null;
        }
        /**
         * 
         * @param lookup 
         * @return 
         */
        public Action getPreferredAction(Lookup lookup){
            return null;
        }
    }
    
    private static class ETLDnDHandler extends DragAndDropHandler {
      
        public void customize(ExTransferable exTransferable, Lookup lookupobj) {
            final Node node = lookupobj.lookup(Node.class);
            DataFlavor flv = null;
            try {
                flv = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            exTransferable.put(new ExTransferable.Single(flv) {                
                protected Object getData() throws IOException, UnsupportedFlavorException {
                    return node;
                }                
            });
        }
       
        
    }
    }
