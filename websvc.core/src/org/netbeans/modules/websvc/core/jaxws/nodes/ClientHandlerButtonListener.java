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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Collection;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsComponentFactory;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandler;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChain;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerChains;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsHandlerClass;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModelFactory;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.DefinitionsBindings;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.GlobalBindings;
import org.netbeans.modules.websvc.core.webservices.ui.panels.MessageHandlerPanel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Binding;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author Roderico Cruz
 */
public class ClientHandlerButtonListener implements ActionListener{
    private MessageHandlerPanel panel;
    private BindingsModel bindingsModel;
    private Client client;
    private Node node;
    private JaxWsModel jaxWsModel;
    private FileObject bindingHandlerFO;
    private String wsdlRelativePath;
    private String bindingsHandlerFile;
    
    
    public ClientHandlerButtonListener(MessageHandlerPanel panel,
            BindingsModel bindingsModel, Client client, Node node, JaxWsModel jaxWsModel){
        
        this.panel = panel;
        this.bindingsModel = bindingsModel;
        this.client = client;
        this.node = node;
        this.jaxWsModel = jaxWsModel;
    }
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == NotifyDescriptor.OK_OPTION) {
            try{
                FileObject srcRoot = (FileObject)node.getLookup().lookup(FileObject.class);
                JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
                final FileObject bindingsFolder = support.getBindingsFolderForClient(node.getName(), true);
                Client client = (Client)node.getLookup().lookup(Client.class);
                assert client != null;
                bindingsHandlerFile = client.getHandlerBindingFile();
                if(bindingsHandlerFile == null){
                    String baseBindingsHandlerFile = node.getName() + "_handler";
                    bindingsHandlerFile = FileUtil.findFreeFileName(bindingsFolder, baseBindingsHandlerFile, "xml") +
                            ".xml";
                    client.setHandlerBindingFile(bindingsHandlerFile);
                }
                
                //if bindingsModel is null, create it
                if(bindingsModel == null){
                    final String bindingsContent = readResource(Repository.getDefault().getDefaultFileSystem().
                            findResource("jax-ws/default-binding-handler.xml").getInputStream()); //NOI18N
                    
                    bindingsFolder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            bindingHandlerFO =
                                    FileUtil.createData(bindingsFolder, bindingsHandlerFile);//NOI18N
                            BufferedWriter bw = null;
                            OutputStream os = null;
                            OutputStreamWriter osw = null;
                            FileLock lock = bindingHandlerFO.lock();
                            try {
                                os = bindingHandlerFO.getOutputStream(lock);
                                osw = new OutputStreamWriter(os);
                                bw = new BufferedWriter(osw);
                                bw.write(bindingsContent);
                            } finally {
                                try{
                                    if(bw != null){
                                        bw.close();
                                    }
                                    if(os != null) {
                                        os.close();
                                    }
                                    if(osw != null){
                                        osw.close();
                                    }
                                }catch(IOException e){
                                    ErrorManager.getDefault().notify(e);
                                }
                                
                                if(lock != null){
                                    lock.releaseLock();
                                }
                            }
                        }
                    });
                    
                    //now load the model and add the entry
                    ModelSource ms = Utilities.getModelSource(bindingHandlerFO, true);
                    bindingsModel =  BindingsModelFactory.getDefault().getModel(ms);
                    //get the relative path of the wsdl
                    FileObject localWsdlFile =
                            support.getLocalWsdlFolderForClient(client.getName(),false).getFileObject(client.getLocalWsdlFile());
                    File f = FileUtil.toFile(bindingHandlerFO);
                    String relativePath = Utilities.relativize(f.toURI(), new URI(localWsdlFile.getURL().toExternalForm()));
                    GlobalBindings gb = bindingsModel.getGlobalBindings();
                    bindingsModel.startTransaction();
                    gb.setWsdlLocation(relativePath);
                    bindingsModel.endTransaction();
                    
                }//end if bindingsModel == null
                
                //get handler chain
                DefaultListModel listModel = panel.getListModel();
                GlobalBindings gb = bindingsModel.getGlobalBindings();
                DefinitionsBindings db = gb.getDefinitionsBindings();
                BindingsHandlerChains bhc = db.getHandlerChains();
                BindingsHandlerChain chain = bhc.getHandlerChains().iterator().next();
                
                //add new handlers
                BindingsComponentFactory factory = bindingsModel.getFactory();
                if(listModel.getSize() > 0){
                    bindingsModel.startTransaction();
                    for(int i = 0; i < listModel.getSize(); i++){
                        String className = (String)listModel.getElementAt(i);
                        if(isNewHandler(className, chain)){
                            BindingsHandler handler = factory.createHandler();
                            BindingsHandlerClass handlerClass = factory.createHandlerClass();
                            handlerClass.setClassName(className);
                            handler.setHandlerClass(handlerClass);
                            chain.addHandler(handler);
                        }
                    }
                    bindingsModel.endTransaction();
                }
                
                //reset bindingHandlerFO so we can save it
                if(bindingHandlerFO == null){
                    bindingHandlerFO = bindingsFolder.getFileObject(client.getHandlerBindingFile());
                }
                
                //remove handlers that have been deleted
                Collection<BindingsHandler> handlers = chain.getHandlers();
                bindingsModel.startTransaction();
                for(BindingsHandler handler : handlers){
                    String clsName = handler.getHandlerClass().getClassName();
                    if(!isInModel(clsName, listModel)){
                        chain.removeHandler(handler);
                    }
                }
                bindingsModel.endTransaction();
                
                //save bindingshandler file
                DataObject dobj = DataObject.find(bindingHandlerFO);
                if(dobj.isModified()){
                    SaveCookie saveCookie = (SaveCookie)dobj.getCookie(SaveCookie.class);
                    saveCookie.save();
                }
                
                if(listModel.getSize() > 0){
                    Binding binding = client.getBindingByFileName(bindingsHandlerFile);
                    if(binding == null){
                        binding = client.newBinding();
                        binding.setFileName(bindingsHandlerFile);
                        client.addBinding(binding);
                    }
                } else{
                    Binding binding = client.getBindingByFileName(bindingsHandlerFile);
                    if(binding != null){
                        client.removeBinding(binding);
                    }
                }                               
                //save the jaxws model
                jaxWsModel.write();
            }catch(Exception e){
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    private boolean isInModel(String className, ListModel model){
        for(int i = 0; i < model.getSize(); i++){
            String cls = (String)model.getElementAt(i);
            if(className.equals(cls)){
                return true;
            }
        }
        return false;
    }
    
    private boolean isNewHandler(String className, BindingsHandlerChain handlerChain){
        if(handlerChain != null){
            Collection<BindingsHandler> handlers = handlerChain.getHandlers();
            for(BindingsHandler handler : handlers){
                if (handler.getHandlerClass().getClassName().equals(className)){
                    return false;
                }
            }
        }
        return true;
    }
    
    
    //TODO: close all streams properly
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        BufferedReader br= null;
        StringBuilder sb = new StringBuilder();
        try{
            
            String lineSep = System.getProperty("line.separator");//NOI18N
            br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(lineSep);
                line = br.readLine();
            }
        }finally{
            br.close();
        }
        return sb.toString();
    }
    
}
