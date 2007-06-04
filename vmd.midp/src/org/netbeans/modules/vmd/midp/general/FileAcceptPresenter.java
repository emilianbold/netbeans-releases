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

import java.io.FileNotFoundException;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.common.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Karol Harezlak
 */
public abstract class FileAcceptPresenter extends AbstractAcceptPresenter {
    
    private Map<String, TypeID> extensionsMap;
    private Map<String, String> propertyNamesMap;
    
    public static final FileAcceptPresenter create(String propertyName, TypeID typeID, String... fileExtensions) {
        FileAcceptPresenter presenter = new FileAcceptPresenter() {
            public Result accept(Transferable transferable) {
                Result result = super.accept(transferable);
                DesignComponent image = result.getComponents().iterator().next();
                image.writeProperty(ImageCD.PROP_RESOURCE_PATH , MidpTypes.createStringValue(getFilePath(transferable)));
                MidpDocumentSupport.getCategoryComponent(getComponent().getDocument(), ResourcesCategoryCD.TYPEID).addComponent(image);
                return result;
            }
        };
        return presenter.addFileExtensions(propertyName, typeID, fileExtensions);
    }
    
    public FileAcceptPresenter() {
        super(AbstractAcceptPresenter.Kind.TRANSFERABLE);
        extensionsMap = new HashMap<String, TypeID>();
        propertyNamesMap = new HashMap<String, String>();
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
    
    public boolean isAcceptable(Transferable transferable) {
        assert (!extensionsMap.isEmpty());
        String fe = getFileExtension(transferable);

        if (fe == null)
            return false;
        fe = ignoreExtFileCase(fe);
        TypeID typeID = extensionsMap.get(fe);
        if (typeID != null)
            return true;
        return false;
    }
    
    public ComponentProducer.Result accept(Transferable transferable) {
        String fe = getFileExtension(transferable);
        fe = ignoreExtFileCase(fe);
        TypeID typeID = extensionsMap.get(fe);
        String propertyName = propertyNamesMap.get(fe);
        if (propertyName == null)
            return null;
        DesignDocument document = getComponent().getDocument();
        final ComponentProducer producer = DocumentSupport.getComponentProducer(document, typeID);
        if (document == null)
            return null;
        DesignComponent newComponent  = producer.createComponent(document).getComponents().iterator().next();
        if (getComponent().readProperty(propertyName).getKind() == PropertyValue.Kind.ARRAY)
            MidpArraySupport.append(getComponent(), propertyName, newComponent);
        else
            getComponent().writeProperty(propertyName, PropertyValue.createComponentReference(newComponent));
        return new ComponentProducer.Result(newComponent);
    }
    
    protected String getFileExtension(Transferable transferable) {
        for (DataFlavor df : transferable.getTransferDataFlavors()) {
            if (df != df.javaFileListFlavor)
                continue;
            List<File> list = null;
            try  {
                list = (List<File>) transferable.getTransferData(df.javaFileListFlavor);
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (list == null && list.size() <= 0)
                continue;
            File file = list.iterator().next();
            Set<FileObject> filesToScan = getFoldersToScan();
            boolean hasExtension = false;
            for (FileObject f : filesToScan) {
                if (FileUtil.isParentOf(f, FileUtil.toFileObject(file))) {
                    hasExtension = true;
                    break;
                }
            }
            if (!hasExtension)
                return null;
            String filename = file.getName();
            String ext = (filename.lastIndexOf(".") == -1) ? "" : filename.substring(filename.lastIndexOf(".") + 1 , filename.length()); //NOI18N
            if (ext != null)
                return ext;
        }
        return null;
    }
    
    protected String getFilePath(Transferable transferable) {
        for (DataFlavor df : transferable.getTransferDataFlavors()) {
            if (df != df.javaFileListFlavor)
                continue;
            List<File> list = null;
            try  {
                list = (List<File>) transferable.getTransferData(df.javaFileListFlavor);
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (list == null && list.size() <= 0)
                continue;
            File file = list.iterator().next();
            try {
                Set<FileObject> foldersToScan = getFoldersToScan();
                for (FileObject f : foldersToScan) {
                    DataObject rootDataObject = DataObject.find(f);
                    String filePath = createFilePath(rootDataObject, rootDataObject, FileUtil.toFileObject(file));
                    if (filePath != null)
                        return filePath;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return ""; //NOI18N
    }
    
    protected InputStream getInputStream(Transferable transferable) {
        for (DataFlavor df : transferable.getTransferDataFlavors()) {
            if (df != df.javaFileListFlavor) {
                continue;
            }
            java.util.List<java.io.File> list = null;
            try {
                list = (java.util.List<java.io.File>) transferable.getTransferData(df.javaFileListFlavor);
            } catch (java.awt.datatransfer.UnsupportedFlavorException ex) {
                org.openide.util.Exceptions.printStackTrace(ex);
            } catch (java.io.IOException ex) {
                org.openide.util.Exceptions.printStackTrace(ex);
            }
            if (list == null && list.size() <= 0) {
                continue;
            }
            try {
                File file = list.iterator().next();
                return new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    private String ignoreExtFileCase(String fe) {
        for (String key : extensionsMap.keySet()) {
            if (key.equalsIgnoreCase(fe))
                fe = key;
        }
        return fe;
    }
    
    private Set<FileObject> getFoldersToScan() {
        DesignDocument document = getComponent().getDocument();
        Set<FileObject> filesToScan = new HashSet<FileObject>();
        
        FileObject projectDirectory = ProjectUtils.getProject(document).getProjectDirectory();
        DataObjectContext dac = ProjectUtils.getDataObjectContextForDocument(document);
        //Resources
        for (FileObject f : ClassPath.getClassPath(projectDirectory, ClassPath.COMPILE).getRoots()) {
            filesToScan.add((f));
        }
        //Sources
        for (SourceGroup g : ProjectUtils.getSourceGroups(dac)) {
            filesToScan.add((g.getRootFolder()));
        }
        return filesToScan;
    }
    
    private String createFilePath(DataObject rootDataObject, DataObject parentDataObject, FileObject childFileObject) {
        Node[] children = parentDataObject.getNodeDelegate().getChildren().getNodes();
        for (Node node : children) {
            DataObject nodeDataObject = (DataObject) node.getLookup().lookup(DataObject.class);
            String filePath = createFilePath(rootDataObject, nodeDataObject, childFileObject);
            if (filePath != null)
                return filePath;
            if (nodeDataObject.files().contains(childFileObject))
                return childFileObject.getPath().replaceAll(rootDataObject.getPrimaryFile().getPath(),""); //NOI18N
        }
        return null;
    }
}
