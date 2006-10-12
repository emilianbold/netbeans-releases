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

import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.config.parser.CustomizationParser;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler;
import com.sun.tools.ws.processor.util.ClientProcessorEnvironment;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.JAXWSUtils;
import java.net.URL;
import java.util.*;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.xml.sax.EntityResolver;

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
    private List modelListeners;
    RequestProcessor.Task task;
    int listenersSize;

    protected Properties properties;
    protected ProcessorEnvironment environment;
    protected Configuration configuration;
    
    private Throwable creationException;
    /** Creates a new instance of WsdlModeler */
    WsdlModeler(URL wsdlUrl) {
        this.wsdlUrl=wsdlUrl;
        modelListeners=new ArrayList();
        task = RequestProcessor.getDefault().create(new Runnable () {
            public void run() {
                generateWsdlModel();
                synchronized (this) {
                    listenersSize = modelListeners.size();
                    fireModelCreated(wsdlModel,listenersSize);
                }
            }
        },true);
        task.addTaskListener(new TaskListener(){
           public void taskFinished(Task task) {
               // remove all listeners or reschedule task for listeners that were added at the at last moment
               synchronized (this) {
                   int size = modelListeners.size();
                   if (size>0) {
                       if (size==listenersSize) removeListeners();
                       else {
                           for (int i=listenersSize-1;i>=0;i--) {
                               modelListeners.remove(i);
                           }
                           ((RequestProcessor.Task)task).schedule(0);
                       }
                   }
               }
           }
        });
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
        properties = new Properties();
        bindingFiles = new HashSet<String>();
        if (bindings!=null) {
            for (int i=0;i<bindings.length;i++) {
                bindingFiles.add(JAXWSUtils.absolutize(bindings[i].toExternalForm()));
            }
        }
        properties.put(ProcessorOptions.BINDING_FILES, bindingFiles);
        properties.put(ProcessorOptions.VALIDATE_WSDL_PROPERTY, true);
        properties.put(ProcessorOptions.USE_WSI_BASIC_PROFILE, false);
        properties.setProperty(ProcessorOptions.EXTENSION,"true");
        try {
            environment = createEnvironment();
            configuration = createConfiguration();
            if (packageName!=null) {
                properties.setProperty(ProcessorOptions.DEFAULT_PACKAGE, packageName);
                configuration.getModelInfo().setDefaultJavaPackage(packageName);
            }
            if(catalog != null) {
                CatalogManager manager = new CatalogManager(null);
                manager.setCatalogFiles(catalog.toExternalForm());
                entityResolver = new CatalogResolver(manager);
                configuration.getModelInfo().setEntityResolver(entityResolver);
            }
            ideWSDLModeler = 
                    new WSDLModeler((WSDLModelInfo)configuration.getModelInfo(),properties);
            Model tmpModel = ideWSDLModeler.buildModel();
            //Model tmpModel = configuration.getModelInfo().buildModel(properties);

            if (tmpModel!=null) {
                wsdlModel=new WsdlModel(tmpModel);
                creationException=null;
            }
        } catch (Exception ex){
            wsdlModel=null;
            creationException=ex;
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
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
            ((WsdlModelListener)modelListeners.get(i)).modelCreated(model);
        }
    }
    
    protected Configuration createConfiguration() throws Exception {
        if (environment == null)
            environment = createEnvironment();
        List<String> inputFiles = new ArrayList<String>();
        inputFiles.add(JAXWSUtils.absolutize(wsdlUrl.toExternalForm()));
        
        IdeCustomizationParser parser = new IdeCustomizationParser(entityResolver, environment, properties);
        
        return parser.parse(inputFiles);
    }
    
    private ProcessorEnvironment createEnvironment() throws Exception {
        ProcessorEnvironment env = new ClientProcessorEnvironment(System.out, null, null);
        return env;
    }
    
    private class IdeCustomizationParser extends CustomizationParser {

        public IdeCustomizationParser(EntityResolver entityResolver, ProcessorEnvironment env, Properties options) {
            super(entityResolver,env,options);
        }
        
        protected Configuration  parse(List <String> inputFiles) throws Exception {
            return super.parse(inputFiles);
        }
    }  
}
