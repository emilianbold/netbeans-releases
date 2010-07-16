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
package org.netbeans.modules.xslt.tmap.nodes;

import java.awt.Component;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.nodes.actions.ActionType;
import org.netbeans.modules.xslt.tmap.nodes.properties.Constants;
import org.netbeans.modules.xslt.tmap.nodes.properties.CustomEditorProperty;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyType;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyUtils;
import org.netbeans.modules.xslt.tmap.ui.editors.TransformCustomEditor;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformNode extends TMapComponentNode<DecoratedTransform> {
    private static final Logger LOGGER = Logger.getLogger(TransformNode.class.getName());
    private static final String BASE_DIR = "baseDir"; // NOI18N
    private static final String CURRENT_DIR = "currentDir"; // NOI18N
    private static final String DIRECTORIES = "directories"; // NOI18N
    private static final String FILES = "files"; // NOI18N
    private static final String FILTER = "filter"; // NOI18N
    private static final String XSL = "xsl"; // NOI18N
    private static final String XSLT = "xslt"; // NOI18N
    
    
    
    public TransformNode(Transform ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }

    public TransformNode(Transform ref, Children children, Lookup lookup) {
        super(new DecoratedTransform(ref), children, lookup);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TRANSFORM;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        Node.Property prop;
        PropertyUtils.getInstance().registerAttributeProperty(this.getReference(), mainPropertySet,
                Named.NAME_PROPERTY, PropertyType.NAME, "getName", "setName", null); // NOI18N

        //
//        prop = PropertyUtils.getInstance().registerCalculatedProperty(this.getReference(), 
//                mainPropertySet,
//                /*Transform.FILE,*/ PropertyType.FILE,
//                "getFile", "setFile"); // NOI18N
        //
        prop = PropertyUtils.getInstance().registerAttributeProperty(this.getReference(), mainPropertySet,
                Transform.SOURCE, PropertyType.SOURCE,
                "getSource", "setSource", "removeSource"); // NOI18N
        //
        prop = PropertyUtils.getInstance().registerAttributeProperty(this.getReference(), mainPropertySet,
                Transform.RESULT, PropertyType.RESULT,
                "getResult", "setResult", "removeResult"); // NOI18N
        //
        
        Property<File> fProp = getFileProperty();
        if (fProp != null) {
            mainPropertySet.put(fProp);
        } else {
            LOGGER.log(Level.WARNING, NbBundle.getMessage(TransformNode.class, "MSG_FilePropIsNull")); // NOI18N
        }
        
        return sheet;
    }

    public File getFile() {
        Transform ref = getReference().getReference();
        if (ref == null) {
            return null;
        }
        
        String filePath = ref.getFile();
        if (filePath == null) {
            return null;
        }
//        File file = new File(filePath);
//        boolean isAbsolute  = file.isAbsolute();
//        if (!isAbsolute) {
//            FileObject tmapFo = SoaUtil.getFileObjectByModel(ref.getModel());
//            if (tmapFo == null) {
//                return null;
//            }
//            FileObject parentFo = tmapFo.getParent();
//            if (parentFo == null) {
//                return null;
//            }
//            
////            FileObject xslFo = ModelUtil.getRelativeFO(parentFo, filePath);
////            file = FileUtil.toFile(xslFo);
//            file = new File(FileUtil.toFile(parentFo), filePath);
//        }
        return new File(filePath);
    }
    
    public void setFile(File f) {
        Transform ref = getReference().getReference();
        if (ref == null) {
            return;
        }

        TMapModel model = ref.getModel();
        if (model == null) {
            return;
        }
        boolean wasInTransaction = model.isIntransaction();
        try {
            if (!wasInTransaction) {
                model.startTransaction();
            }
            if (f == null) {
                ref.removeFile();
            } else {
                ref.setFile(f.getPath());
            }
        } finally {
            if (!wasInTransaction) {
                model.endTransaction();
            }
        }
    }
    
    @Override
    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return new TransformCustomEditor(this, editingMode);
    }
    
    @Override
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_NEWTYPES,
            ActionType.SEPARATOR,
            ActionType.GO_TO,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES,
            
        };
    }

    @Override
    public ActionType[] getAddActionArray() {
        Transform transform = getReference().getReference();
        
        return transform != null && SoaUtil.isAllowBetaFeatures(transform.getModel()) ?
            new ActionType[] {
            ActionType.ADD_INVOKE,
            ActionType.ADD_PARAM
            }
            :
            new ActionType[] {
            ActionType.ADD_PARAM
        };
    }

    FileFilter xsltFileFilter = new FileFilter() {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = FileUtil.getExtension(f.getName());
            if (XSL.equals(extension) || XSLT.equals(extension)) {
                return true;
            }
            return false;
        }

        public String getDescription() {
            return NbBundle.getMessage(TransformNode.class, "LBL_XslFileFilterDescr"); // NOI18N

        }
    };
    
    // TODO m
    private File getBaseDir() {
        Transform ref = getReference().getReference();
        TMapModel model = ref == null ? null : ref.getModel();
        if (model == null) {
            return null;
        }
        FileObject xslFo = SoaUtil.getFileObjectByModel(model);
        if (xslFo == null) {
            return null;
        }
        
        FileObject srcFo = Util.getProjectSource(SoaUtil.getProject(xslFo));
        return srcFo == null ? null : FileUtil.toFile(srcFo);
    }
    
    // TODO m
    private File getCurrentDir() {
        Transform ref = getReference().getReference();
        if (ref == null) {
            return null;
        }
        
        String filePath = ref.getFile();
        if (filePath == null) {
            return getBaseDir();
        }
        File file = new File(filePath);
        File parentFile = null;
        boolean isAbsolute  = file.isAbsolute();
        if (!isAbsolute) {
            FileObject tmapFo = SoaUtil.getFileObjectByModel(ref.getModel());
            if (tmapFo == null) {
                return null;
            }
            FileObject parentFo = tmapFo.getParent();
            if (parentFo == null) {
                return null;
            }
            
            parentFile = FileUtil.toFile(parentFo);
        } else {
            parentFile = file.getParentFile();
        }
        return parentFile == null ? getBaseDir() : parentFile;
    }
    
    // TODO m
    private Node.Property<File> getFileProperty() {
        Property<File> p = null;
        try {
            p = new FileProperty(Transform.FILE, java.io.File.class, 
                    PropertyType.XSL_FILE.getDisplayName(), 
                    PropertyType.XSL_FILE.getShortDescription());
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }    
        return p;
    } 
    
    private class FileProperty extends PropertySupport.ReadWrite<File> {

        public FileProperty(
                String propName,
                Class<File> cls,
                String propDisplayName,
                String propShortDescr) 
        {
            super(propName, cls, propDisplayName, propShortDescr);
            setValue(DIRECTORIES, Boolean.FALSE); // should directories be selectable as values for the property 
            setValue(FILES, Boolean.TRUE); // should directories be selectable as values for the property 
            File currDir = TransformNode.this.getCurrentDir();
            if (currDir != null) {
                setValue(CURRENT_DIR, currDir); // the dir that should be preselected when displaying the dialog
            }

            File baseDir = TransformNode.this.getBaseDir();
            if (baseDir != null) {
                setValue(BASE_DIR, baseDir); // an absolute directory which can be used 
                                                //as a base against which relative filenames should be interpreted. 
                                                //Incoming relative paths may be resolved against this base directory 
                                                //when e.g. opening a file chooser, as with the two-argument File constructors. 
                                                //Outgoing paths which can be expressed relative to this base directory 
                                                //may be relativized, according to the discretion of the implementation; 
                                                //currently files selected in the file chooser which are under the base directory 
                                                //(including the base directory itself) will be relativized, 
                                                //while others will be left absolute. The empty abstract pathname 
                                                //(new File("")) is used to represent the base directory itself.
            }
            setValue(FILTER, TransformNode.this.xsltFileFilter); // the value can be of any of the supported types and represents filter for the file dialog
        }

        public File getValue() {
            return TransformNode.this.getFile();
        }

        public void setValue(File o) {
            TransformNode.this.setFile(o);
        }
    }
}

