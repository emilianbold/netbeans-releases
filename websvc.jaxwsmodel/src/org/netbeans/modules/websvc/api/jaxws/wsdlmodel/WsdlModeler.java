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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler;
import com.sun.tools.ws.wscompile.AbortException;
import com.sun.tools.ws.wscompile.BadCommandLineException;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.xml.ws.util.JAXWSUtils;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.openide.util.RequestProcessor;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXParseException;

/**
 *
 * @author mkuchtiak
 */
public class WsdlModeler {
    
    private WsdlModel wsdlModel;
    private WSDLModeler ideWSDLModeler;
    private URL wsdlUrl;
    private URL[] bindings;
    private URL catalog;
    private EntityResolver entityResolver;
    private Set<String> bindingFiles;
    private String packageName;
    private List<WsdlModelListener> modelListeners;
    RequestProcessor.Task task;
    int listenersSize;
    
    protected Properties properties;
    
    private Throwable creationException;
    /** Creates a new instance of WsdlModeler */
    WsdlModeler(URL wsdlUrl) {
        this.wsdlUrl=wsdlUrl;
        modelListeners = new ArrayList<WsdlModelListener>();
        task = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                generateWsdlModel();
                synchronized (this) {
                    listenersSize = modelListeners.size();
                    fireModelCreated(wsdlModel,listenersSize);
                    removeListeners();
                }
            }
        },true);
    }
    
    public void setPackageName(String packageName) {
        this.packageName=packageName;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setJAXBBindings(URL[] bindings) {
        this.bindings=bindings;
    }
    
    public URL[] getJAXBBindings() {
        return bindings;
    }
    
    public void setCatalog(URL catalog) {
        this.catalog=catalog;
    }
    
    public URL getCatalog() {
        return catalog;
    }
    
    void setWsdlUrl(URL url){
        wsdlUrl = url;
    }
    
    public URL getWsdlUrl(){
        return wsdlUrl;
    }
    
    public Throwable getCreationException() {
        return creationException;
    }
    
    public WsdlModel getWsdlModel() {
        return wsdlModel;
    }
    
    public WsdlModel getAndWaitForWsdlModel() {
        if (getWsdlModel()==null) generateWsdlModel();
        return wsdlModel;
    }
    
    public void generateWsdlModel(WsdlModelListener listener) {
        generateWsdlModel(listener,false);
    }
    
    public void generateWsdlModel(WsdlModelListener listener, boolean forceReload) {
        if (forceReload) {
            try {task.waitFinished(10000);} catch (InterruptedException ex) {}
            addWsdlModelListener(listener);
            task.schedule(0);
        } else {
            addWsdlModelListener(listener);
            if (task.isFinished()) {
                task.schedule(0);
            }
        }
    }
    
    private synchronized void generateWsdlModel() {
        WsimportOptions options = new WsimportOptions();
        properties = new Properties();
        bindingFiles = new HashSet<String>();
        if (bindings!=null) {
            for (int i=0;i<bindings.length;i++) {
                try {
                    options.addBindings(JAXWSUtils.absolutize(bindings[i].toExternalForm()));
                } catch (BadCommandLineException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.FINE, "WsdlModeler.generateWsdlModel", ex); //NOI18N
                }
            }
        }
        try {
            options.addWSDL(new File(wsdlUrl.toURI()));
            options.compatibilityMode = WsimportOptions.EXTENSION;
            
            if (packageName!=null) {
                options.defaultPackage = packageName;
            }
            if(catalog != null) {
                CatalogManager manager = new CatalogManager(null);
                manager.setCatalogFiles(catalog.toExternalForm());
                entityResolver = new CatalogResolver(manager);
                options.entityResolver = entityResolver;
            }
            
            options.parseBindings(new IdeErrorReceiver());
            
            ideWSDLModeler =
                    new WSDLModeler(options, new IdeErrorReceiver());
            Model tmpModel = ideWSDLModeler.buildModel();
            
            if (tmpModel!=null) {
                wsdlModel=new WsdlModel(tmpModel);
                creationException=null;
            }
        } catch (Exception ex){
            wsdlModel=null;
            creationException=ex;
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "WsdlModeler.generateWsdlModel", ex); //NOI18N
        }
        
    }
    
    private synchronized void addWsdlModelListener(WsdlModelListener listener) {
        if (listener!=null)
            modelListeners.add(listener);
    }
    
    private void removeListeners() {
        modelListeners.clear();
    }
    
    private void fireModelCreated(WsdlModel model, int listenersSize) {
        for (int i=0;i<listenersSize;i++) {
            modelListeners.get(i).modelCreated(model);
        }
    }
    
    private class IdeErrorReceiver extends ErrorReceiver{
        public void warning(SAXParseException ex) throws AbortException {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, 
                    "WsdlModeler.generateWsdlModel", ex); //NOI18N
        }
        
        public void info(SAXParseException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, 
                    "WsdlModeler.generateWsdlModel", ex); //NOI18N
        }
        
        public void fatalError(SAXParseException ex) throws AbortException {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, 
                    "WsdlModeler.generateWsdlModel", ex); //NOI18N
        }
        
        public void error(SAXParseException ex) throws AbortException {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, 
                    "WsdlModeler.generateWsdlModel", ex); //NOI18N
        }
        
    }
}
