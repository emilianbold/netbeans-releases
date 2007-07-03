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

package org.netbeans.modules.vmd.midp.general;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Exceptions;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Karol Harezlak
 */
public abstract class FileAcceptPresenter extends AcceptPresenter {
    
    private Map<String, TypeID> extensionsMap;
    private Map<String, String> propertyNamesMap;
    
    public FileAcceptPresenter(String propertyName, TypeID typeID, String... fileExtensions) {
        super(AcceptPresenter.Kind.TRANSFERABLE);
        extensionsMap = new HashMap<String, TypeID>();
        propertyNamesMap = new HashMap<String, String>();
        addFileExtensions(propertyName, typeID, fileExtensions);
    }
    
    public FileAcceptPresenter addFileExtensions(String propertyName, TypeID typeID, String... fileExtensions) {
        assert typeID != null;
        assert fileExtensions.length != 0;
        
        for (String fe : fileExtensions) {
            extensionsMap.put(fe, typeID);
            propertyNamesMap.put(fe, propertyName);
        }
        return this;
    }
    
    public boolean isAcceptable (Transferable transferable, AcceptSuggestion suggestion) {
        assert (!extensionsMap.isEmpty());
        FileObject fileObject = getNodeFileObject(transferable);
        if (fileObject == null && !belongsToProject(fileObject))
            return false;
        DesignDocument document = getComponent().getDocument();
        Map<FileObject, String> mapFile = MidpProjectSupport.getAllFilesForProjectByExt(document, extensionsMap.keySet());
        if (mapFile.get(fileObject) == null)
            return false;
        TypeID typeID = getTypeForExtension(fileObject.getExt());
        if (typeID != null)
            return true;
        return false;
    }
    
    public ComponentProducer.Result accept (Transferable transferable, AcceptSuggestion suggestion) {
        FileObject fileObject = getNodeFileObject(transferable);
        TypeID typeID = getTypeForExtension(fileObject.getExt());
        String propertyName = getPropertyNameForExtension(fileObject.getExt());
        if (propertyName == null)
            return super.accept(transferable, suggestion);
        DesignDocument document = getComponent().getDocument();
        final ComponentProducer producer = DocumentSupport.getComponentProducer(document, typeID.toString ());
        if (document == null || producer == null)
            return super.accept(transferable, suggestion);
        DesignComponent newComponent = producer.createComponent(document).getMainComponent ();
        if (getComponent().readProperty(propertyName).getKind() == PropertyValue.Kind.ARRAY)
            MidpArraySupport.append(getComponent(), propertyName, newComponent);
        else
            getComponent().writeProperty(propertyName, PropertyValue.createComponentReference(newComponent));
        return new ComponentProducer.Result(newComponent);
    }
    
    protected boolean belongsToProject(FileObject fileObject) {
        DesignDocument document = getComponent().getDocument();
        Map<FileObject, String> fileMap = MidpProjectSupport.getAllFilesForProjectByExt(document, extensionsMap.keySet());
        return fileMap.get(fileObject) != null;
    }
    
    protected InputStream getInputStream(Transferable transferable) {
        try {
            FileObject fileNode = getNodeFileObject(transferable);
            if (fileNode == null)
                return null;
            File file = FileUtil.toFile(fileNode);
            if (file == null)
                return null;
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    protected String getFileClasspath(FileObject fileObject) {
        DesignDocument document = getComponent().getDocument();
        Map<FileObject,String> fileMap = MidpProjectSupport.getAllFilesForProjectByExt(document, Arrays.asList(fileObject.getExt()));
        return fileMap.get(fileObject);
    }
    
    protected FileObject getNodeFileObject(Transferable transferable) {
        Node node = NodeTransfer.node(transferable, NodeTransfer.DND_COPY_OR_MOVE);
        if (node == null)
            return null;
        DataObject dataObject = (DataObject) node.getLookup().lookup(DataObject.class);
        if (dataObject == null)
            return null;
        FileObject file = dataObject.getPrimaryFile();
        return file;
    }
    
    private TypeID getTypeForExtension(String fe) {
        for (String key : extensionsMap.keySet()) {
            if (key.equalsIgnoreCase(fe))
                return extensionsMap.get(key);
        }
        return null;
    }
    private String getPropertyNameForExtension(String fe) {
        for (String key : propertyNamesMap.keySet()) {
            if (key.equalsIgnoreCase(fe))
                return propertyNamesMap.get(key);
        }
        return null;
    }
    
}
