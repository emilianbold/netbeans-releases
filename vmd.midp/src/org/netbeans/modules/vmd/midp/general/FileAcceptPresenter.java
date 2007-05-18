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

import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.common.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
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
import org.openide.util.Exceptions;

/**
 *
 * @author devil
 */
public abstract class FileAcceptPresenter extends AbstractAcceptPresenter {
    
    private Map<String, TypeID> extensionsMap;
    private Map<String, String> propertyNamesMap;
    
    public static final FileAcceptPresenter createImage(String propertyName, TypeID typeID, String... fileExtensions) {
        FileAcceptPresenter presenter = new FileAcceptPresenter() {
            public Result accept(Transferable transferable) {
                Result ir = super.accept(transferable);
                DesignComponent image = ir.getComponents().iterator().next();
                image.writeProperty(ImageCD.PROP_RESOURCE_PATH , MidpTypes.createStringValue(getFilePath(transferable)));
                MidpDocumentSupport.getCategoryComponent(getComponent().getDocument(), ResourcesCategoryCD.TYPEID).addComponent(image);
                return new Result(image);
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
    
    private String getFileExtension(Transferable transferable) {
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
                DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
                DataObject fileDataObject = DataObject.find(FileUtil.toFileObject(file));
                Project fileProject =  ProjectUtils.getProject(fileDataObject);
                Project currentProject = ProjectUtils.getProject(document);
                if (fileProject != currentProject)
                    return null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
                DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
                FileObject fileObject = FileUtil.toFileObject(file);
                Project project = ProjectUtils.getProject(document);
                String projectID = ProjectUtils.getProjectID(project);
                for (SourceGroup g : ProjectUtils.getSourceGroups(projectID)) {
                    if (g.contains(fileObject))
                        return fileObject.getPath().replaceAll(g.getRootFolder().getPath(),""); //NOI18N
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
}
