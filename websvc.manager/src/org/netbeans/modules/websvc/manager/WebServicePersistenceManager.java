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


package org.netbeans.modules.websvc.manager;

import org.netbeans.modules.websvc.manager.api.WebServiceManager;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import java.io.*;
import java.io.BufferedOutputStream;
import java.util.*;

import java.beans.ExceptionListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * WebServicePersistenceManager.java
 * @author  Winston Prakash, quynguyen
 */
public class WebServicePersistenceManager implements ExceptionListener {
    //TODO: (nam) derive from default value used by wscompile
    private static final String DEFAULT_PACKAGE = "websvc"; // NOI18N
    
    private File websvcDir = new File(WebServiceManager.WEBSVC_HOME);
    private File websvcRefFile = new File(websvcDir, "websvc_ref.xml");
    private List<WebServiceDescriptor> descriptorsToWrite = null;
    
    public void load() {        
        if (websvcRefFile.exists()) {
            try{
                XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(websvcRefFile)));
                List<WebServiceData> wsDatas = new ArrayList<WebServiceData>();
                List<WebServiceGroup> wsGroups = new ArrayList<WebServiceGroup>();
                
                List<String> partnerServices = WebServiceListModel.getInstance().getPartnerServices();
                Object firstObject = decoder.readObject();
                int wsDataNums;
                
                if (firstObject instanceof List) {
                    List<String> loadedServices = (List<String>)firstObject;
                    for (String url : loadedServices) {
                        partnerServices.add(url);
                    }
                    wsDataNums = ((Integer)decoder.readObject()).intValue();
                }else {
                    wsDataNums = ((Integer)firstObject).intValue();
                }
                
                for(int i=0; i< wsDataNums; i++){
                    WebServiceData wsData = null;
                    try{
                        wsData = (WebServiceData) decoder.readObject();
                    }catch(Exception exc){
                        ErrorManager.getDefault().notify(exc);
                        decoder.close();
                    }
                    
                    wsDatas.add(wsData);
                }
                int wsGroupSize = ((Integer)decoder.readObject()).intValue();
                for(int i=0; i< wsGroupSize; i++){
                    try{
                        WebServiceGroup group = (WebServiceGroup) decoder.readObject();
                        wsGroups.add(group);
                    }catch(Exception exc){
                        ErrorManager.getDefault().notify(exc);
                        decoder.close();
                    }
                }
                decoder.close();
                
                for (WebServiceData wsData : wsDatas) {
                    if (wsData.getJaxRpcDescriptorPath() != null)
                        wsData.setJaxRpcDescriptor(loadDescriptorFile(websvcDir + File.separator + wsData.getJaxRpcDescriptorPath()));
                    
                    if (wsData.getJaxWsDescriptorPath() != null)
                        wsData.setJaxWsDescriptor(loadDescriptorFile(websvcDir + File.separator + wsData.getJaxWsDescriptorPath()));
                    WebServiceListModel.getInstance().addWebService(wsData);
                }
                
                for (WebServiceGroup group : wsGroups) {
                    try {
                        WebServiceListModel.getInstance().addWebServiceGroup(group);
                    }catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
                
            }catch(Exception exc){
                exc.printStackTrace();
            }
        }
        
        loadPartnerServices();
    }
    
    public void save() {
        WebServiceListModel model = WebServiceListModel.getInstance();
        if (!model.isInitialized()) {
            return;
        }
        
        if (!websvcDir.exists())websvcDir.mkdirs();
        if (websvcRefFile.exists()) websvcRefFile.delete();
        try{
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(websvcRefFile)));
            encoder.setExceptionListener(this);
            
            DefaultPersistenceDelegate delegate = new WebServiceDataPersistenceDelegate();
            encoder.setPersistenceDelegate(WsdlService.class, delegate);
            encoder.setPersistenceDelegate(WebServiceDescriptor.class, delegate);
            
            encoder.writeObject(model.getPartnerServices());
            
            List<WebServiceData> wsDataSet =  model.getWebServiceSet();
            encoder.writeObject(new Integer(wsDataSet.size()));
            
            synchronized (wsDataSet) {
                for (WebServiceData wsData : wsDataSet) {
                    encoder.writeObject(wsData);
                }
            }
            
            List<WebServiceGroup> wsGroupSet =  model.getWebServiceGroupSet();
            encoder.writeObject(new Integer(wsGroupSet.size()));
            
            synchronized (wsGroupSet) {
                for (WebServiceGroup group : wsGroupSet) {
                    encoder.writeObject(group);
                }
            }
            
            encoder.close();
            encoder.flush();
            delegate = new DefaultPersistenceDelegate();
            encoder.setPersistenceDelegate(WsdlService.class, delegate);
            encoder.setPersistenceDelegate(WebServiceDescriptor.class, delegate);
            
            if (descriptorsToWrite != null) {
                for (WebServiceDescriptor descriptor : descriptorsToWrite) {
                    saveWebServiceDescriptor(descriptor);
                }
            }
        } catch(Exception exc){
            ErrorManager.getDefault().notify(exc);
            return;
        }
    }
    
    private WebServiceDescriptor loadDescriptorFile(String descriptorPath) {
        if (descriptorPath == null || descriptorPath.length() == 0) {
            return null;
        }else {
            XMLDecoder decoder = null;
            try {
                decoder = new java.beans.XMLDecoder(new java.io.BufferedInputStream(new java.io.FileInputStream(descriptorPath)));
                return (WebServiceDescriptor)decoder.readObject();
            } catch (Exception ex) {
                exceptionThrown(ex);
                return null;
            } finally {
                if (decoder != null) {
                    decoder.close();
                }
            }
        }
        
    }
    
    public void loadPartnerServices() {
        List<String> partnerUrls = WebServiceListModel.getInstance().getPartnerServices();
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        
        FileObject f = sfs.findResource("RestComponents"); // NOI18N
        if (f != null && f.isFolder()) {
            Enumeration<? extends FileObject> en = f.getFolders(false);
            while (en.hasMoreElements()) {
                FileObject nextFolder = en.nextElement();
                List<String> currentUrls = new ArrayList<String>();
                
                FileObject[] contents = nextFolder.getChildren();
                for (int i = 0; i < contents.length; i++) {
                    try {
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(contents[i].getInputStream());
                        NodeList nodes = doc.getElementsByTagName("method"); // NOI18N
                    
                        for (int j = 0; j < nodes.getLength(); j++) {
                            NamedNodeMap attributes = nodes.item(j).getAttributes();
                            String type = attributes.getNamedItem("type").getNodeValue(); // NOI18N
                            String url = attributes.getNamedItem("url").getNodeValue(); // NOI18N
                            
                            if ("http://schemas.xmlsoap.org/wsdl/".equals(type) && !partnerUrls.contains(url)) { // NOI18N
                                partnerUrls.add(url);
                                currentUrls.add(url);
                            }
                        }
                    }catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }

                if (currentUrls.size() > 0) {
                    String groupName = nextFolder.getName();
                    //TODO: (nam) if and how to internalize partner's name
                    if (groupName.equals("StrikeIron")) { // NOI18N
                        groupName = NbBundle.getMessage(WebServicePersistenceManager.class, "STRIKE_IRON_GROUP");
                    }
                    
                    WebServiceGroup newGroup = new WebServiceGroup(WebServiceListModel.getInstance().getUniqueWebServiceGroupId());
                    newGroup.setName(groupName);
                    
                    for (String url : currentUrls) {
                        WebServiceData wsData = new WebServiceData(url, DEFAULT_PACKAGE, newGroup.getId());
                        WebServiceListModel.getInstance().addWebService(wsData);
                        
                        newGroup.add(wsData.getId(), true);
                    }
                    
                    WebServiceListModel.getInstance().addWebServiceGroup(newGroup);
                }
            }
        }
    }
    
    private void saveWebServiceDescriptor(WebServiceDescriptor descriptor) {
        try {
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(descriptor.getXmlDescriptor())));
            encoder.setExceptionListener(this);
            DefaultPersistenceDelegate delegate = new WebServiceDataPersistenceDelegate();
            encoder.setPersistenceDelegate(WsdlService.class, delegate);
            encoder.writeObject(descriptor);
            
            encoder.close();
            encoder.flush();
        }catch (IOException ex) {
            exceptionThrown(ex);
        }
    }
    
    public void exceptionThrown(Exception exc) {
        ErrorManager.getDefault().notify(exc);
    }
    
    public class WebServiceDataPersistenceDelegate extends DefaultPersistenceDelegate {
        /**
         * Suppress the writing of 
         * org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService
         * It will be created from the WSDL file
         */  
        public void writeObject(Object oldInstance, Encoder out) {
            if(oldInstance instanceof WsdlService) {
                return;
            }else if (oldInstance instanceof WebServiceDescriptor) {
                if (descriptorsToWrite == null) {
                    descriptorsToWrite = new ArrayList<WebServiceDescriptor>();
                }
                descriptorsToWrite.add((WebServiceDescriptor)oldInstance);
            }else {
                super.writeObject(oldInstance,out);
            } 
        }  
    }
    
}
