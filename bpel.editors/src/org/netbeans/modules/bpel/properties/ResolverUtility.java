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
package org.netbeans.modules.bpel.properties;

import java.util.Collection;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.project.ProjectConstants;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.filesystems.FileUtil;

/**
 * The utility class containing auxiliary methods to work with WSDL
 *
 * ATTENTION! Many methods contains the lookup as patamether.
 * It is implied that it contains the BpelModel instance.
 *
 * @author nk160297
 */
public final class ResolverUtility {
    
    /**
     * Calculate a String to display from a QName.
     */
    public static String qName2DisplayText(QName qValue) {
        return qName2DisplayText(qValue, null);
    }
    
    /**
     * Calculate a String to display from a QName.
     * The relativeTo parameter is used to calculate the prefix if it isn't specified.
     */
    public static String qName2DisplayText(QName qValue, BpelEntity relativeTo) {
        if (qValue == null) {
            return "";
        }
        //
        String prefix = qValue.getPrefix();
        String namespace = null;
        //
        if (prefix == null || prefix.length() == 0) {
            namespace = qValue.getNamespaceURI();
            if (relativeTo != null && namespace != null && namespace.length() != 0) {
                prefix = relativeTo.getNamespaceContext().getPrefix(namespace);
            }
        }
        //
        if (prefix == null || prefix.length() == 0) {
            if (namespace != null && namespace.length() != 0) {
                String retValue = qValue.getLocalPart() + "{" + namespace + "}"; // NOI18N
                return retValue;
            } else {
                prefix = "";
            }
        } else {
            prefix = prefix + ":"; // NOI18N
        }
        String retValue = prefix + qValue.getLocalPart();
        return retValue;
    }
    
    public static String getDisplayName(Component comp) {
        return getDisplayName(comp, null);
    }
    
    public static String getDisplayName(Component comp, BpelEntity relativeTo) {
        String targetNamespace = null;
        String prefix = null;
        String compName = null;
        //
        if (comp instanceof BpelEntity) {
            targetNamespace = ((BpelEntity)comp).getBpelModel().
                    getProcess().getTargetNamespace();
            if (comp instanceof NamedElement) {
                compName = ((NamedElement)comp).getName();
            }
        } else if (comp instanceof WSDLComponent) {
            targetNamespace = ((WSDLComponent)comp).getModel().
                    getDefinitions().getTargetNamespace();
            if (comp instanceof Named) {
                compName = ((Named)comp).getName();
            }
        } else if (comp instanceof SchemaComponent) {
            targetNamespace = ((SchemaComponent)comp).getModel().
                    getSchema().getTargetNamespace();
            if (comp instanceof Named) {
                compName = ((Named)comp).getName();
            }
        }
        //
        assert compName != null :
            "Impossible to calculate the name for a component which hasn't a name!"; // NOI18N
        //
        if (targetNamespace != null && targetNamespace.length() > 0) {
            if (relativeTo != null) {
                NamespaceContext nc = relativeTo.getNamespaceContext();
                prefix = nc.getPrefix(targetNamespace);
            }
            if (prefix == null || prefix.length() == 0) {
                return targetNamespace + Constants.COLON + compName;
            } else {
                return prefix + Constants.COLON + compName;
            }
        } else {
            return compName;
        }
    }
    
    /**
     * This methos calculate the relative path from project source root folder to the
     * specified target file object.
     */
    public static String calculateRelativePathName(FileObject targetFo, Lookup lookup) {
        BpelModel bpelModel = (BpelModel)lookup.lookup(BpelModel.class);
        if (bpelModel == null) {
            return targetFo.getPath();
        }
        //
        return calculateRelativePathName(targetFo, bpelModel);
    }
    
    /**
     * Returns projectSource related to the given bpelModel which is in lookup
     * Could return null
     */ 
    public static FileObject getProjectSource(Lookup lookup) {
        BpelModel bpelModel = lookup.lookup(BpelModel.class);
        if (bpelModel == null) {
            return null;
        }
        Sources sources = getProjectSources(bpelModel);
        FileObject bpelFo = Util.getFileObjectByModel(bpelModel);
        if (bpelFo == null) {
            return null;
        }
        String bpelFoPath = bpelFo.getPath();
        
        if (sources != null) {
            SourceGroup[] sourceGroupArr = sources.getSourceGroups(
                    ProjectConstants.SOURCES_TYPE_BPELPRO);
            //
            for (SourceGroup srcGroup : sourceGroupArr) {
                String srcFolderName = srcGroup.getRootFolder().getPath();
                //
                if (bpelFoPath.startsWith(srcFolderName)) {
                    return srcGroup.getRootFolder();
                }
            }
        }
        return null;
    }
    
    /**
     * This methos calculate the relative path from project source root folder to the
     * specified target file object.
     */
    public static String calculateRelativePathName(FileObject targetFo,
            BpelModel bpelModel) {
        if (targetFo == null) {
            return null;
        }
        //
        String targetFoPath = targetFo.getPath();
        //
        Sources sources = getProjectSources(bpelModel);
        if (sources != null) {
            SourceGroup[] sourceGroupArr = sources.getSourceGroups(
                    ProjectConstants.SOURCES_TYPE_BPELPRO);
            //
            for (SourceGroup srcGroup : sourceGroupArr) {
                String srcFolderName = srcGroup.getRootFolder().getPath();
                //
                if (targetFoPath.startsWith(srcFolderName)) {
                    return targetFoPath.substring(srcFolderName.length());
                }
            }
        }
        //
        return targetFoPath;
    }
    
    public static String encodeLocation(String location){
        return location.replace(" ", "%20");
    }
    
    public static String decodeLocation(String location){
        return location.replace("%20", " ");
    }
    
    public static FileObject getImportedFile(String imprtLocation, Lookup lookup) {
        
        if (imprtLocation == null) {
            return null;
        }
        
        imprtLocation = decodeLocation(imprtLocation);
        
        FileObject bpelFolderFo = getBpelProcessFolder(lookup);
        //
        if (bpelFolderFo != null) {
            return Util.getRelativeFO(bpelFolderFo, imprtLocation);
        }
        //
        return null;
    }
    
    public static WSDLModel getImportedWsdlModel(String imprtLocation, Lookup lookup) {
        
        if (imprtLocation == null) {
            return null;
        }
        imprtLocation = decodeLocation(imprtLocation);
        
        FileObject fo = getImportedFile(imprtLocation, lookup);
        if (fo == null || !fo.isValid()){
            return null;
        }
        ModelSource modelSource = Utilities.getModelSource(fo, true);
        if (modelSource != null) {
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().
                    getModel(modelSource);
            if (wsdlModel.getState() != Model.State.NOT_WELL_FORMED) {
                return wsdlModel;
            }
        }
        //
        return null;
        
    }
    
    public static SchemaModel getImportedScemaModel(String imprtLocation, Lookup lookup) {
        FileObject fo = getImportedFile(imprtLocation, lookup);
        if (fo == null || !fo.isValid()){
            return null;
        }
        ModelSource modelSource = Utilities.getModelSource(fo, true);
        if (modelSource != null) {
            SchemaModel schemaModel = SchemaModelFactory.getDefault().
                    getModel(modelSource);
            if (schemaModel.getState() != Model.State.NOT_WELL_FORMED) {
                return schemaModel;
            }
        }
        //
        return null;
        
    }
    
    /**
     * Returns the FileObject which points to the folder where the current
     * BPEL Process is located.
     *
     * This method can return null;
     */
    public static FileObject getBpelProcessFolder(Lookup lookup) {
        BpelModel bpelModel = (BpelModel)lookup.lookup(BpelModel.class);
        return getBpelProcessFolder(bpelModel);
    }
    
    /**
     * Returns the FileObject which points to the folder where the specified
     * BPEL Process is located.
     *
     * This method can return null;
     */
    public static FileObject getBpelProcessFolder(BpelModel bpelModel) {
        if (bpelModel != null) {
            ModelSource bpelMS = bpelModel.getModelSource();
            if (bpelMS != null) {
                Lookup bpelLookup = bpelMS.getLookup();
                if (bpelLookup != null) {
                    FileObject bpelFo = (FileObject)bpelLookup.lookup(FileObject.class);
                    if (bpelFo != null) {
                        FileObject bpelFolderFo = bpelFo.getParent();
                        return bpelFolderFo;
                    }
                }
            }
        }
        return null;
    }
    
    public static FileObject getProjectRoot(Lookup lookup) {
        BpelModel bpelModel = (BpelModel)lookup.lookup(BpelModel.class);
        ModelSource source = bpelModel.getModelSource();
        Project project =  FileOwnerQuery.getOwner((FileObject)
        source.getLookup().lookup(FileObject.class));
        FileObject result = project.getProjectDirectory();
        return result;
    }
    
    /**
     * Check if the specified model is imported to the current BPEL.
     */
    public static boolean isModelImported(Model model, Lookup lookup) {
        BpelModel bpelModel = (BpelModel)lookup.lookup(BpelModel.class);
        return isModelImported(model, bpelModel);
    }
    
    /**
     * Check if the specified model is imported to the current BPEL.
     */
    public static boolean isModelImported(Model model, BpelModel bpelModel) 
        throws IllegalStateException 
    {
        if (model == null|| bpelModel == null) {
            throw new IllegalStateException();
        }
        
        FileObject targetModelFo = (FileObject)model.getModelSource().
                getLookup().lookup(FileObject.class);
        if (targetModelFo == null) {
            if (model == SchemaModelFactory.getDefault().getPrimitiveTypesModel()) {
                // the primitive types' model considered as imported implicitly.
                return true;
            }
        } else {
            FileObject bpelFolderFo = getBpelProcessFolder(bpelModel);
            if (bpelFolderFo != null) {

                Import[] importArr = bpelModel.getProcess().getImports();
                for (Import importObj : importArr) {
                    String location = importObj.getLocation();
                    if (location != null) {
                        FileObject fo = Util.getRelativeFO(bpelFolderFo, location);
                        if (targetModelFo.equals(fo)) {
                            return true;
                        }
                    }
                }
            }
        }
        //
        return false;
    }
    
    /**
     * Check if the specified file is imported to the current BPEL.
     */
    public static boolean isFileImported(FileObject targetFo, Lookup lookup) {
        BpelModel bpelModel = (BpelModel)lookup.lookup(BpelModel.class);
        Import[] importArr = bpelModel.getProcess().getImports();
        FileObject bpelFolderFo = getBpelProcessFolder(lookup);
        //
        if (bpelFolderFo != null) {
            for (Import importObj : importArr) {
                String location = importObj.getLocation();
                if (location != null) {
                    FileObject fo = Util.getRelativeFO(bpelFolderFo, location);
                    if (targetFo.equals(fo)) {
                        return true;
                    }
                }
            }
        }
        //
        return false;
    }
    
    /**
     * Tries to resolve the reference and take it's name.
     */
    public static String getNameByRef(Reference ref)  {
        if (ref == null) {
            return null;
        }
        String result = null;
        //
        Object obj = null;
        try {
            obj = ref.get();
        } catch(IllegalStateException ex) {
            //This exception may happen if referenced object was removed from model
            //A kind of workaround required to work with cached references.
        }
        
        
        if (obj != null) {
            if (obj instanceof VariableDeclaration) {
                result = ((VariableDeclaration)obj).getVariableName();
            } else if (obj instanceof Named) {
                result = ((Named)obj).getName();
            }
        } else {
            result = ref.getRefString();
        }
        //
        return result;
    }
    
    /**
     * Returns the project Sources.
     * Use the following line to access the SourceGroup array.
     *  SourceGroup[] sourceGroup =
     *  sources.getSourceGroups(ProjectConstants.SOURCES_TYPE_BPELPRO);
     */
    public static Sources getProjectSources(Lookup lookup) {
        BpelModel bpelModel = (BpelModel)lookup.lookup(BpelModel.class);
        if (bpelModel == null) {
            return null;
        }
        //
        return getProjectSources(bpelModel);
    }
    
    /**
     * Returns the project Sources for the specified bpel process.
     * Use the following line to access the SourceGroup array.
     *  SourceGroup[] sourceGroup =
     *  sources.getSourceGroups(ProjectConstants.SOURCES_TYPE_BPELPRO);
     */
    public static Sources getProjectSources(BpelModel bpelModel) {
        FileObject modelFo = getBpelProcessFolder(bpelModel);
        if (modelFo != null) {
            Project project =  FileOwnerQuery.getOwner(modelFo);
            if (project != null) {
                Sources sources = ProjectUtils.getSources(project);
                return sources;
            }
        }
        return null;
    }
    
    /**
     * Determines if the file located under the source folder.
     */
    public static boolean isSourceFile(FileObject file, Lookup lookup) {
        Sources sources = getProjectSources(lookup);
        if (sources != null) {
            SourceGroup[] sourceGroupArr = sources.getSourceGroups(
                    ProjectConstants.SOURCES_TYPE_BPELPRO);
            //
            for (SourceGroup srcGroup : sourceGroupArr) {
                FileObject sourceRoot = srcGroup.getRootFolder();
                //
                if (FileUtil.isParentOf(sourceRoot, file)) {
                    return true;
                }
            }
        }
        //
        return false;
    }
    
    public static GlobalSimpleType findSympleTypeByQName(QName qName) {
        Collection<GlobalSimpleType> simpleTypes =
                SchemaModelFactory.getDefault().getPrimitiveTypesModel().
                getSchema().getSimpleTypes();
        for (GlobalSimpleType type : simpleTypes) {
            if (type.getName().equals(qName.getLocalPart())) {
                return type;
            }
        }
        return null;
    }
    
}
