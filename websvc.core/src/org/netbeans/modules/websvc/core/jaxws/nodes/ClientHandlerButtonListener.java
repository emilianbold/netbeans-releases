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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import javax.swing.ListModel;
import javax.swing.table.TableModel;
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
            if(!panel.isChanged()) return;
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
                TableModel tableModel = panel.getHandlerTableModel();
                GlobalBindings gb = bindingsModel.getGlobalBindings();
                DefinitionsBindings db = gb.getDefinitionsBindings();
                BindingsHandlerChains bhc = db.getHandlerChains();
                BindingsHandlerChain chain = bhc.getHandlerChains().iterator().next();
                
                //refresh handlers
                bindingsModel.startTransaction();
                Collection<BindingsHandler> handlers = chain.getHandlers();
                for(BindingsHandler handler : handlers){
                    chain.removeHandler(handler);
                }
                
                if(tableModel.getRowCount() > 0){
                    BindingsComponentFactory factory = bindingsModel.getFactory();
                    for(int i = 0; i < tableModel.getRowCount(); i++){
                        String className = (String)tableModel.getValueAt(i, 0);
                        BindingsHandler handler = factory.createHandler();
                        BindingsHandlerClass handlerClass = factory.createHandlerClass();
                        handlerClass.setClassName(className);
                        handler.setHandlerClass(handlerClass);
                        chain.addHandler(handler);
                    }
                }
                
            //}
            bindingsModel.endTransaction();
            
            //reset bindingHandlerFO so we can save it
            if(bindingHandlerFO == null){
                bindingHandlerFO = bindingsFolder.getFileObject(client.getHandlerBindingFile());
            }
 
            //save bindingshandler file
            DataObject dobj = DataObject.find(bindingHandlerFO);
            if(dobj.isModified()){
                SaveCookie saveCookie = dobj.getCookie(SaveCookie.class);
                saveCookie.save();
            }
            
            if(tableModel.getRowCount() > 0){
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

//TODO: UNUSED METHOD => DEAD CODE???
private boolean isInModel(String className, ListModel model){
    for(int i = 0; i < model.getSize(); i++){
        String cls = (String)model.getElementAt(i);
        if(className.equals(cls)){
            return true;
        }
    }
    return false;
}

//TODO: UNUSED METHOD => DEAD CODE???
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
