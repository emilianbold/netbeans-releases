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
package org.netbeans.modules.xslt.tmap.nodes.actions;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.InvokeHandler;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.api.events.VetoException;
import org.netbeans.modules.xslt.tmap.model.spi.NameGenerator;
import org.netbeans.modules.xslt.tmap.util.ImportRegistrationHelper;
import org.openide.util.Exceptions;

/**
 * Factory to create TMapComponent based on user parameters and add them to the TMapModel
 * 
 * @author Vitaly Bychkov
 */
public class AddTMapComponentFactory {

    private static final AtomicReference<AddTMapComponentFactory> FACTORY = new AtomicReference<AddTMapComponentFactory>();
    private static Logger LOGGER = Logger.getLogger(AddTMapComponentFactory.class.getName());
    
    public static AddTMapComponentFactory getInstaince() {
        if (FACTORY.get() == null) {
            FACTORY.compareAndSet(null, new AddTMapComponentFactory());
        }
        return FACTORY.get();
    }
    
    private AddTMapComponentFactory() {
    }

    public void addOperation(Service service, Operation wsdlOperation) {
        if (service == null || wsdlOperation == null) {
            return;
        }
        TMapModel tMapModel = service.getModel();
        org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp 
                = tMapModel.getFactory().createOperation();
        tMapOp.setOperation(tMapOp.createWSDLReference(wsdlOperation, Operation.class));
        NameGenerator nameGen = NameGenerator.getDefault(tMapOp, Variable.class);
        tMapOp.setOutputVariableName(nameGen.getName(tMapOp, NameGenerator.OUTPUT_OPERATION_VARIABLE_PREFIX));
        tMapOp.setInputVariableName(nameGen.getName(tMapOp, NameGenerator.INPUT_OPERATION_VARIABLE_PREFIX));
        
        service.addOperation(tMapOp);
        //service.
    }

    public void addOperation(TransformMap tMap, Operation wsdlOperation) {
        if (tMap == null || wsdlOperation == null) {
            return;
        }
        TMapModel tMapModel = tMap.getModel();
        if (tMapModel == null) {
            return;
        }
        WSDLComponent pt = wsdlOperation.getParent();
        if (!(pt instanceof PortType)) {
            return;
        }
        List<Service> services = tMap.getServices();
        Service service = null; 
        if (services != null) {
            for (Service srv : services) {
                WSDLReference<PortType> ptRef = srv.getPortType();
                if (ptRef != null && pt.equals(ptRef.get())) {
                    service = srv;
                    break;
                }
            }
        }
        
        if (service == null) {
            service = addService(tMap,(PortType) pt);
        }
        
        addOperation(service, wsdlOperation);
    }
    
    public void addTransform(org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp) {
        if (tMapOp == null ) {
            return;
        }
        TMapModel tMapModel = tMapOp.getModel();
        Transform transform = tMapModel.getFactory().createTransform();
        
        NameGenerator nameGen = NameGenerator.getDefault(tMapOp, Transform.class);
        try {
            transform.setName(NameGenerator.getUniqueName(tMapOp, Transform.class));
        } catch (VetoException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        
        tMapOp.addTransform(transform);
    }

    public Service addService(TransformMap tMap, PortType pt) {
        if (tMap == null || pt == null) {
            return null;
        }
        TMapModel tMapModel = tMap.getModel();
        if (tMapModel == null) {
            return null;
        }
        
        boolean isOwnTransact =false;
        if (!tMapModel.isIntransaction()) {
            tMapModel.startTransaction();
            isOwnTransact = true;
        }
        Service service = null;
        try {
            new ImportRegistrationHelper(tMapModel).addImport(pt.getModel());
            service = tMapModel.getFactory().createService();
            String serviceName = NameGenerator.getUniqueName(service, Service.class);
            try {
                service.setName(serviceName);
            } catch (VetoException ex) {
                Exceptions.printStackTrace(ex);
            }
            tMap.addService(service);
            service = getService(serviceName, tMap);
            if (service != null) {
                service.setPortType(service.createWSDLReference((PortType)pt, PortType.class));
            }
        } finally {
            if (isOwnTransact && tMapModel != null && tMapModel.isIntransaction()) {
                tMapModel.endTransaction();
            }
        }
        return service;
    }

    public void addInvoke(InvokeHandler invokeHandler, Operation wsdlOperation) {
        if (invokeHandler == null || wsdlOperation == null) {
            return;
        }
        TMapModel tMapModel = invokeHandler.getModel();
        WSDLComponent pt = wsdlOperation.getParent();
        if (!(pt instanceof PortType)) {
            return;
        }
        boolean isOwnTransact = false;
        if (!tMapModel.isIntransaction()) {
            tMapModel.startTransaction();
            isOwnTransact = true;
        }
        try {
            new ImportRegistrationHelper(tMapModel).addImport(pt.getModel());
            Invoke invoke = tMapModel.getFactory().createInvoke();
            invokeHandler.addInvoke(invoke);

            invoke.setPortType(invokeHandler.createWSDLReference((PortType)pt, PortType.class));
            invoke.setOperation(invokeHandler.createWSDLReference(wsdlOperation, Operation.class));
            // todo m
            // init default variables just for operation invoke because transform invoke doesn't require variables
            if (!(invokeHandler instanceof Transform)) {
                NameGenerator varNameGen = NameGenerator.getDefault(invokeHandler, Variable.class);
                invoke.setOutputVariableName(varNameGen.getName(invokeHandler, NameGenerator.OUTPUT_INVOKE_VARIABLE_PREFIX));
                invoke.setInputVariableName(varNameGen.getName(invokeHandler, NameGenerator.INPUT_INVOKE_VARIABLE_PREFIX));
            }
            try {
                invoke.setName(NameGenerator.getUniqueName(invokeHandler, Invoke.class));
            } catch (VetoException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }

        } finally {
            if (isOwnTransact && tMapModel.isIntransaction()) {
                tMapModel.endTransaction();
            }
        }
    }
    
    private Service getService(String name, TransformMap tMap) {
        List<Service> services = tMap.getServices();
        Service service = null; 
        if (services != null) {
            for (Service srv : services) {
                if (name.equals(srv.getName())) {
                    service = srv;
                    break;
                }
            }
        }
        return service;
    }


}
