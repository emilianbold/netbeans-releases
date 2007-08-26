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
package org.netbeans.modules.websvc.manager.api;

import com.sun.tools.ws.processor.model.java.JavaMethod;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.manager.model.WebServiceData;

/**
 * Contains the DataFlavors and the classes for transferring web service
 * metadata to web service consumers.  Current support is for the transfer
 * of web service ports and methods.
 * 
 * XXX should be unified with base NB Web Service DnD functionality
 * 
 * @author quynguyen
 */
public class WebServiceMetaDataTransfer {

    /**
     * The {@link DataFlavor} representing a web service port
     */
    public static DataFlavor PORT_FLAVOR;
    
    /**
     * The {@link DataFlavor} representing a web service method
     */
    public static DataFlavor METHOD_FLAVOR;

    public static final DataFlavor METHOD_NODE_FLAVOR;
    protected static final DataFlavor PORT_NODE_FLAVOR;
    
    static {
        try {
            PORT_FLAVOR = new DataFlavor("application/x-java-netbeans-websvcmgr-port;class=org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$Port"); // NOI18N
            PORT_NODE_FLAVOR = new DataFlavor("application/x-java-netbeans-websvcmgr-port;class=org.openide.nodes.Node");
            METHOD_FLAVOR = new DataFlavor("application/x-java-netbeans-websvcmgr-method;class=org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$Method"); // NOI18N
            METHOD_NODE_FLAVOR = new DataFlavor("application/x-java-netbeans-websvcmgr-method;class=org.openide.nodes.Node");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    
    public static final class Port {
        private final WebServiceData wsData;
        private final String portName;
        
        public Port(WebServiceData wsData, String portName) {
            this.wsData = wsData;
            this.portName = portName;
        }

        public WebServiceData getWebServiceData() {
            return wsData;
        }

        public String getPortName() {
            return portName;
        }
    }
    
    public static final class Method {
        private final WebServiceData wsData;
        private final JavaMethod method;
        private final String portName;
        private final WsdlOperation operation;
        
        public Method(WebServiceData wsData, JavaMethod method, String portName, WsdlOperation operation) {
            this.wsData = wsData;
            this.method = method;
            this.portName = portName;
            this.operation = operation;
        }
        
        public WebServiceData getWebServiceData() {
            return wsData;
        }
        
        public JavaMethod getMethod() {
            return method;
        }
        
        public String getPortName() {
            return portName;
        } 
        
        public WsdlOperation getOperation() {
            return operation;
        }        
    }
    
    
    public static final class MethodTransferable implements Transferable {
        private static final DataFlavor[] SUPPORTED_FLAVORS = { METHOD_FLAVOR, METHOD_NODE_FLAVOR };
        private final WebServiceMetaDataTransfer.Method transferData;
        
        public MethodTransferable(WebServiceMetaDataTransfer.Method transferData) {
            this.transferData = transferData;
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return SUPPORTED_FLAVORS;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == SUPPORTED_FLAVORS[0] || flavor == SUPPORTED_FLAVORS[1];
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }else {
                return transferData;
            }
        }
    }

    public static final class PortTransferable implements Transferable {
        private static final DataFlavor[] SUPPORTED_FLAVORS = { PORT_FLAVOR, PORT_NODE_FLAVOR };
        private final WebServiceMetaDataTransfer.Port transferData;
        
        public PortTransferable(WebServiceMetaDataTransfer.Port transferData) {
            this.transferData = transferData;
        }
        
        public PortTransferable(Method method) {
             WebServiceData wsData = method.getWebServiceData();
             String portName = method.getPortName();
             this.transferData = new Port(wsData, portName);
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return SUPPORTED_FLAVORS;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == SUPPORTED_FLAVORS[0] || flavor == SUPPORTED_FLAVORS[1];
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }else {
                return transferData;
            }
        }
    }
    
    
}
