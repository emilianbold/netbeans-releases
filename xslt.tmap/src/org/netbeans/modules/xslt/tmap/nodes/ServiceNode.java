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

package org.netbeans.modules.xslt.tmap.nodes;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xam.ui.ComponentPasteType;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyType;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ServiceNode extends TMapComponentNode<DecoratedService> {

    public ServiceNode(Service ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }

    public ServiceNode(Service ref, Children children, Lookup lookup) {
        super(new DecoratedService(ref), children, lookup);
    }

//    @Override
//    protected void createPasteTypes(Transferable t, List<PasteType> s) {
////        super.createPasteTypes(t, s);
//        System.out.println("getted transferable: "+t);
//        DataFlavor[] dataFlavors = t.getTransferDataFlavors();
//        int i =0;
//        for (DataFlavor dataFlavor : dataFlavors) {
//            try {
//                i++;
//                System.out.println(i + ") currentDataFlavor: " 
//                        + dataFlavor + "; transferedData: " 
//                        + t.getTransferData(dataFlavor));
//            } catch (UnsupportedFlavorException ex) {
//                System.out.println("unsupproted dataFlavor");
//            } catch (IOException ex) {
//                System.out.println("ioexception when try to get transfered data");
//            }
//        }
//        
//
//        
//        
//    }
    

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet);
        //
        Node.Property prop;
        prop = PropertyUtils.registerProperty(this, mainPropertySet,
                PropertyType.PARTNER_LINK_TYPE,
                "getPartnerLinkType", "setPartnerLinkType"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        prop = PropertyUtils.registerProperty(this, mainPropertySet,
                PropertyType.ROLE,
                "getRole", "setRole"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        //
        return sheet;
    }
    
    
//    
//    @Override
//    protected void createPasteTypes(Transferable transferable, List<PasteType> list) {
//        System.out.println("getted transferable: "+transferable);
//        DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
//        int i =0;
//        for (DataFlavor dataFlavor : dataFlavors) {
//            try {
//                i++;
//                System.out.println(i + ") currentDataFlavor: " 
//                        + dataFlavor + "; transferedData: " 
//                        + transferable.getTransferData(dataFlavor));
//            } catch (UnsupportedFlavorException ex) {
//                System.out.println("unsupproted dataFlavor");
//            } catch (IOException ex) {
//                System.out.println("ioexception when try to get transfered data");
//            }
//        }
//
//        // Make sure this node is still valid.
//        TMapComponent component = getComponentRef();
//        
//        if (component != null && component.getModel() != null && isEditable()) {
//            PasteType type = ComponentPasteType.getPasteType(
//                    component, transferable, null);
//            System.out.println("pasteType::: "+type);
//            if (type != null) {
//                list.add(type);
//            }
//        }
//    }
//
//    @Override
//    public PasteType getDropType(Transferable transferable, int action, int index) {
//        // Make sure this node is still valid.
//        TMapComponent component = getComponentRef();
//        
//        if (component != null && component.getModel() != null && isEditable()) {
//            PasteType type = ComponentPasteType.getDropType(
//                    component, transferable, null, action, index);
//            System.out.println("drop type: "+type);
//            if (type != null) {
//                return type;
//            }
//        }
//        return null;
//    }
//    
//    
//    
//    private class OperationPasteType extends PasteType {
//
//        @Override
//        public Transferable paste() throws IOException {
//            System.out.println("try to perform paste operation");
//            
//            return null;
//        }
//    }

    @Override
    public NewType[] getNewTypes() {
        if (isEditable()) {
            return getNewTypes(getComponentRef());
        }
        return new NewType[] {};
    }
    
    
    
        public NewType[] getNewTypes(TMapComponent newComponent) {
            
            List<NewType> list = new ArrayList<NewType>();

            if (newComponent instanceof Service) {
                list.add(new OperationNewType((Service)newComponent));
            }

            return list.toArray(new NewType[]{});
        }        
    
        public static class OperationNewType extends NewType {

            private Service myService;
            OperationNewType(Service service) {
                myService = service;
            }
            
        @Override
        public String getName() {
            return NbBundle.getMessage(TMapComponentNode.class, "LBL_NewOperationAction"); // NOI18N
        }
            
        @Override
        public void create() throws IOException {
        TMapModel model = myService.getModel();
        model.startTransaction();
//        String operationOutputName = NameGenerator.getInstance().generateUniqueOperationOutputName(mOperation);
        Operation operation = model.getFactory().createOperation();
//        output.setName(operationOutputName);
//        mOperation.setOutput(output);
        myService.addOperation(operation);
        model.endTransaction();
//        ActionHelper.selectNode(operation);
        }
        }
        
}
