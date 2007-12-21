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
package org.netbeans.modules.xslt.tmap.model.xsltmap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XsltMapModel {
    private FileChangeListener xsltMapFileChangeListener;
    private FileObject xsltMapFile;
    private boolean isInit = false;
    
    private Document myDocument;
    private List<TransformationUC> transformationUCs;
    private ChangeXsltMapSupport changeModelSupport;
    
    protected XsltMapModel(FileObject xsltMapFile) {
        if (!XsltMapAccessor.isValidXsltMapFile(xsltMapFile)) {
            throw new IllegalArgumentException("Invalid xsltmap file"); // NOI18N
        }
        this.xsltMapFile = xsltMapFile;
        xsltMapFileChangeListener = new XsltMapFileChangeListener();
        xsltMapFile.addFileChangeListener(xsltMapFileChangeListener);
        changeModelSupport = new ChangeXsltMapSupport();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (xsltMapFile != null && xsltMapFileChangeListener != null) {
            xsltMapFile.removeFileChangeListener(xsltMapFileChangeListener);
        }
    }
    
    /**
     * Search for xsltmap file if no - create new one and return correspondent model
     */
    public static synchronized XsltMapModel getDefault(Project project) throws IOException {
        FileObject xsltMapFile = Util.getXsltMapFo(project);
        if (xsltMapFile == null) {
            xsltMapFile = XmlUtil.createTemplateXsltMapFo(project);
        }

        if (xsltMapFile == null) {
            return null;
        }
        
        XsltMapModel xsltMapModel = new XsltMapModel(xsltMapFile);
        xsltMapModel.initXsltMapModel();
        return xsltMapModel.isInitModel() ? xsltMapModel : null;
    }
    
    public FileObject getXsltMapFile() {
        return xsltMapFile;
    }
    
    /**
     * save model to file
     */
    public synchronized void sync() throws IOException {
        syncImpl();
    }
    
    public void syncImpl() throws IOException {
        if (!isInitModel()) {
            throw new IllegalStateException("xsltMapModel hasn't been initialized"); // NOI18N
        }

        FileObject xsltMapFile = getXsltMapFile();
        FileLock fileLock = null;
        OutputStream outputStream = null;
        try {
            fileLock = xsltMapFile.lock();
            outputStream = xsltMapFile.getOutputStream(fileLock);
            XMLUtil.write(myDocument,outputStream,XmlUtil.UTF8);
            outputStream.flush();
            outputStream.close();
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            
            if (fileLock != null) {
                fileLock.releaseLock();
            }
        }
    }

    /**
     * add TransformationUC to the model
     */
    public synchronized void addTransformationUC(TransformationUC tUC) {
        if (tUC == null) {
            return;
        }
        
        if (!isInitModel()) {
            throw new IllegalStateException("xsltMapModel hasn't been initialized"); // NOI18N
        }
        
        // TODO m
        Node rootNode = XmlUtil.getElementByTagName(myDocument, XsltMapConst.XSLTMAP);
        assert rootNode != null;
        
        Element tUCElement = myDocument.createElement(tUC.getTransformationType().getTagName());
        assert tUCElement != null;
        
        List<TransformationDesc> tDescs = tUC.getTransformationDescs();
        if (tDescs != null) {
            for (TransformationDesc tmpDesc : tDescs) {
                Element tDescElement = myDocument.createElement(tmpDesc.getType().getTagName());
                
                String tmpPLink = tmpDesc.getPartnerLink();
                if (tmpPLink != null) {
                    tDescElement.setAttribute(XsltMapConst.PARTNER_LINK, tmpPLink);
                }
                
                String tmpRoleName = tmpDesc.getRoleName();
                if (tmpRoleName != null) {
                    tDescElement.setAttribute(XsltMapConst.ROLE_NAME, tmpRoleName);
                }
                String tmpPortType = tmpDesc.getPortType();
                if (tmpPortType != null) {
                    tDescElement.setAttribute(XsltMapConst.PORT_TYPE, tmpPortType);
                }
                String tmpOperation = tmpDesc.getOperation();
                if (tmpOperation != null) {
                    tDescElement.setAttribute(XsltMapConst.OPERATION, tmpOperation);
                }
                String tmpMessageType = tmpDesc.getMessageType();
                if (tmpMessageType != null) {
                    tDescElement.setAttribute(XsltMapConst.MESSAGE_TYPE, tmpMessageType);
                }
                String tmpFile = tmpDesc.getFile();
                if (tmpFile != null) {
                    tDescElement.setAttribute(XsltMapConst.FILE, tmpFile);
                }
                String tmpTransformJBI = tmpDesc.getTransformJBI();
                if (tmpTransformJBI != null) {
                    tDescElement.setAttribute(XsltMapConst.TRANSFORM_JBI, tmpTransformJBI);
                }
                
                tUCElement.appendChild(tDescElement);
            }
        }
        rootNode.appendChild(tUCElement);
    }
    
    public synchronized List<TransformationUC> getTransformationUCs() {
        return transformationUCs;
    }
    
    public synchronized List<TransformationDesc> getTransformationDescs() {
        List<TransformationDesc> transfDescsList = new ArrayList<TransformationDesc>();
        List<TransformationUC> transformationUseCases = getTransformationUCs();
        if (transformationUseCases == null) {
            return transfDescsList;
        }
        for (TransformationUC ucElem : transformationUseCases) {
            List<TransformationDesc> tmpDescs = ucElem.getTransformationDescs();
            if (tmpDescs != null && tmpDescs.size() > 0) {
                transfDescsList.addAll(tmpDescs);
            }
        }
        
        return transfDescsList;
    }
    
    public boolean isInitModel() {
        return isInit;
    }
    
    public synchronized void initXsltMapModel() {
        Document document = XmlUtil.getDocument(xsltMapFile);
        if (document == null) {
            return;
        }
        myDocument = document;
        
        Map<TransformationType, Node> transformationUCNodes 
                    = new HashMap<TransformationType, Node>();
        List<TransformationUC> tUCList = new ArrayList<TransformationUC>();

        for (TransformationType transformType : TransformationType.values()) {
//            Node tmpNode = XmlUtil.getElementByTagName(document, transformType.getTagName());
//            TransformationUC tmpUc = createTransformationUCs(transformType, tmpNode);
            NodeList nodeList = document.getElementsByTagName(transformType.getTagName());
            if (nodeList == null) {
                continue;
            }
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node tmpNode = nodeList.item(i);
                TransformationUC tmpUc = createTransformationUCs(transformType, tmpNode);
                if (tmpUc != null) {
                    tUCList.add(tmpUc);
                }
            }
        }
        this.transformationUCs = tUCList;
        
        isInit = true;
    }
    
    /**
     * Utililty method
     */
    public synchronized TransformationDesc getFirstTransformationDesc(FileObject xsltFo) {
        if (xsltFo == null) {
            return null;
        }
        
        TransformationDesc foundedDesc = null;
        List<TransformationDesc> descs = getTransformationDescs();
        for (TransformationDesc desc : descs) {
            if (desc.isEqualInputFile(xsltFo)) {
                foundedDesc = desc;
                break;
            }
        }
        return foundedDesc;
    }
    
    private List<TransformationDesc> getTransformationDescs(TransformationUC transfUC, Node transformNode
            , TransformationType transformType) 
    {
        List<TransformationDesc> descList = new ArrayList<TransformationDesc>();
        TransformationDescType[] tDescTypes = transformType.getTransformationDescs();
        for (TransformationDescType descType : tDescTypes) {
            Node descNode = XmlUtil.getElementByTagName(transformNode.getChildNodes(), descType.getTagName());
            TransformationDesc tmpDesc = createTransformationDesc(transfUC, descType, descNode);
            if (tmpDesc != null) {
                descList.add(tmpDesc);
            }
        }
        return descList;
    }
    
    private TransformationUC createTransformationUCs(TransformationType type, Node ucNode) {
        if (ucNode == null || type == null) {
            return null;
        }
        TransformationUC tUC = null;
        switch (type) {
            case FILTER_ONE_WAY:
                tUC = new FilterOneWayUC(this);
                break;
            case FILTER_REQUEST_REPLY:
                tUC = new FilterRequestReplyUC(this);
                break;
            case REQUEST_REPLY_SERVICE:
                tUC = new RequestReplyServiceUC(this);
                break;
        }
        if (tUC != null) {
            tUC.setTransformationDescs(getTransformationDescs(tUC,ucNode,type));
        }
        
        return tUC;
    }

    private TransformationDesc createTransformationDesc(TransformationUC transfUC, TransformationDescType type, Node descNode) {
        if (descNode == null || type ==null) {
            return null;
        }
        TransformationDesc desc = null;
        switch (type) {
            case INPUT:
                desc = new InputTransformationDesc(this, transfUC);
                break;
            case OUTPUT:
                desc = new OutputTransformationDesc(this, transfUC);
                break;
        }
        if (desc != null) {
            NamedNodeMap attrs = descNode.getAttributes();

            if (attrs != null) {
                desc.setFile(XmlUtil.getAttrValue(attrs, XsltMapConst.FILE));
                desc.setMessageType(XmlUtil.getAttrValue(attrs, XsltMapConst.MESSAGE_TYPE));
                desc.setOperation(XmlUtil.getAttrValue(attrs, XsltMapConst.OPERATION));
                desc.setPartnerLink(XmlUtil.getAttrValue(attrs, XsltMapConst.PARTNER_LINK));
                desc.setPortType(XmlUtil.getAttrValue(attrs, XsltMapConst.PORT_TYPE));
                desc.setRoleName(XmlUtil.getAttrValue(attrs, XsltMapConst.ROLE_NAME));
                desc.setTransformJBI(XmlUtil.getAttrValue(attrs, XsltMapConst.TRANSFORM_JBI));
            }
        }
        
        return desc;
    }

    public void addPropertyChangeListener(XsltMapPropertyChangeListener changeListener) {
        changeModelSupport.addPropertyChangeListener(changeListener);
    }
    
    public void removePropertyChangeListener(XsltMapPropertyChangeListener changeListener) {
        changeModelSupport.removePropertyChangeListener(changeListener);
    }
    
    private synchronized void updateXsltMapModel() {
        List<TransformationDesc> curTransfDesc = getTransformationDescs();
        
        List<TransformationDesc> oldDescs = null;
        if (curTransfDesc != null && curTransfDesc.size() > 0) {
            oldDescs = new ArrayList<TransformationDesc>(curTransfDesc);
            Collections.copy(oldDescs, curTransfDesc);
        }
        initXsltMapModel();
        curTransfDesc = getTransformationDescs();

        List<TransformationDesc> newDescs = null;//new ArrayList<TransformationDesc>();//curTransfDesc == null || curTransfDesc.length == 0 ? null : Arrays.asList(curTransfDesc.clone());
        if (curTransfDesc != null && curTransfDesc.size() > 0) {
            newDescs = new ArrayList<TransformationDesc>(curTransfDesc);
            Collections.copy(newDescs, curTransfDesc);
        }

        if (oldDescs == null || oldDescs.size() == 0 ) {
            return;
        }
        
        List<TransformationDesc> nonDeletedElems = new ArrayList<TransformationDesc>();
        // in case all transformationUCs had been deleted
        if (newDescs != null) {
            for (TransformationDesc newElem : newDescs) {
                assert newElem.getType() != null;
                TransformationUC newParent = newElem.getParent();
                if (newParent == null) {
                    continue;
                }

                Iterator<TransformationDesc> oldDescIterator = oldDescs.iterator();
                while (oldDescIterator.hasNext()) {
                    TransformationDesc oldElem = oldDescIterator.next();
                    assert oldElem.getType() != null;
                    
                    // TODO m add unique id to compare an xsltmap elements
                    if (newParent.equals(oldElem.getParent()) && newElem.getType().equals(oldElem.getType()) ) {
                        nonDeletedElems.add(oldElem);
                        if (!newElem.equals(oldElem)) {
                            fireTransformationDescChanged(oldElem, newElem);
                            break;
                        }
                    }
                }
            }
        }
        oldDescs.removeAll(nonDeletedElems);

        for (TransformationDesc delElem : oldDescs) {
            fireTransformationDescChanged(delElem, null);
        }
    }
    
    private void fireTransformationDescChanged(TransformationDesc oldDesc, TransformationDesc newDesc) {
        changeModelSupport.fireTransformationDescChanged(oldDesc, newDesc);
        
    }
    
    private class XsltMapFileChangeListener implements FileChangeListener {
        public void fileFolderCreated(FileEvent fe) {
        }

        public void fileDataCreated(FileEvent fe) {
        }

        public void fileChanged(FileEvent fe) {
            updateXsltMapModel();
        }

        public void fileDeleted(FileEvent fe) {
        }

        public void fileRenamed(FileRenameEvent fe) {
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    
    }
    
}
