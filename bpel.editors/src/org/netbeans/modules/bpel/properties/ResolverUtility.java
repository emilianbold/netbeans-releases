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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xpath.ext.schema.GetNameVisitor;
import org.openide.util.NbBundle;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.schema.model.Schema;

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
            Schema schema = ((SchemaComponent)comp).getModel().getSchema();
            targetNamespace = schema == null ? null : schema.getTargetNamespace();
            GetNameVisitor nameVisitor = new GetNameVisitor();
            ((SchemaComponent)comp).accept(nameVisitor);
            compName = nameVisitor.getName();
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
     * Returns projectSource related to the given bpelModel which is in lookup
     * Could return null
     */ 
    public static FileObject getProjectSource(Lookup lookup) {
        return getProjectSource(lookup.lookup(BpelModel.class));
    }
    
    /**
     * Returns projectSource related to the given bpelModel which is in lookup
     * Could return null
     */ 
    public static FileObject getProjectSource(BpelModel bpelModel) {
        if (bpelModel == null) {
            return null;
        }
        FileObject bpelFo = SoaUtil.getFileObjectByModel(bpelModel);
        if (bpelFo == null) {
            return null;
        }
        Sources sources = safeGetSources(Utils.safeGetProject(bpelModel));
        if (sources == null) {
            return null;
        }
        
        String bpelFoPath = bpelFo.getPath();
        SourceGroup[] sourceGroupArr = sources.getSourceGroups(Utils.SOURCES_TYPE_BPELPRO);
        for (SourceGroup srcGroup : sourceGroupArr) {
            String srcFolderName = srcGroup.getRootFolder().getPath();
            //
            if (bpelFoPath.startsWith(srcFolderName)) {
                return srcGroup.getRootFolder();
            }
        }
        return null;
    }

    public static String encodeLocation(String location){
        return location.replace(" ", "%20");
    }
    
    public static String decodeLocation(String location){
        return location.replace("%20", " ");
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
                    FileObject bpelFo = bpelLookup.lookup(FileObject.class);
                    if (bpelFo != null) {
                        FileObject bpelFolderFo = bpelFo.getParent();
                        return bpelFolderFo;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Check if the specified model is imported to the current BPEL.
     */
    public static boolean isModelImported(Model model, Lookup lookup) {
        BpelModel bpelModel = lookup.lookup(BpelModel.class);
        return isModelImported(model, bpelModel);
    }
    
    /**
     * Check if the specified model is imported to the current BPEL.
     */
    public static boolean isModelImported(Model model, BpelModel bpelModel) {
        if (model == SchemaModelFactory.getDefault().getPrimitiveTypesModel()) {
            // the primitive types' model considered as imported implicitly.
            return true;
        }
        
        for (Import imp : bpelModel.getProcess().getImports()) {
            if (model == ImportHelper.getWsdlModel(imp, false)) {
                return true;
            }
            if (model == ImportHelper.getSchemaModel(imp, false)) {
                return true;
            }
        }
        
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
    
    public static ModelSource getImportedModelSource(Import importObj) {
        if (Import.SCHEMA_IMPORT_TYPE.equals(importObj.getImportType())) {
            SchemaModel schemaModel = ImportHelper.getSchemaModel(importObj, false);
            if (schemaModel != null) {
                return schemaModel.getModelSource();
            }
        } else if (Import.WSDL_IMPORT_TYPE.equals(importObj.getImportType())) {
            WSDLModel wsdlModel = ImportHelper.getWsdlModel(importObj, false);
            if (wsdlModel != null) {
                return wsdlModel.getModelSource();
            }
        }
        
        return null;
    }
    
    public static FileObject getFileObjectByImport(Import inport) {
      return getImportedFileObject(inport);
    }

    public static FileObject getImportedFileObject(Import importObj) {
        ModelSource modelSource = getImportedModelSource(importObj);
        
        if (modelSource != null) {
            return modelSource.getLookup().lookup(FileObject.class);
        } else {
            return null;
        }
    }
    
    /* 
     * Description is shown in e.g. TypeChooserPanel. It's either a path
     * to the imported file or a message that a file doesn't exist.
     */
    public static String getImportDescription(Import importObj) {
        FileObject fo = getImportedFileObject(importObj);
        if (fo != null && fo.isValid()) {
            return fo.getPath();
        } else {
            //No valid Model or ModelSource or FileObject found - return warning
            String importInfo = importObj.getLocation();
            if (importInfo == null || importInfo.length() == 0) {
                importInfo = importObj.getNamespace();
            }
            return NbBundle.getMessage(FormBundle.class,
                    "ERR_IMPORT_FILE_DOESNT_EXIST", // NOI18N
                    ResolverUtility.decodeLocation(importInfo), "");
        }
    }
    
    /**
     * Returns the project Sources for the specified bpel process.
     */
    public static Sources safeGetSources(Project project) {
        if (project != null) {
            return ProjectUtils.getSources(project);
        } else {
            return null;
        }
    }
    
    /*
     * Returns the relative path of a given FileObject in a given Project.
     * Returns null if either of the parameters is null or given FileObject
     * is not in the given Project.
     */
    public static String safeGetRelativePath(FileObject fo, Project project) {
        if (fo == null || !fo.isValid() || project == null) {
            return null;
        }
        
        if (FileOwnerQuery.getOwner(fo) != project) {
            return null;
        }
        
        String targetFoPath = fo.getPath();
        //
        Sources sources = safeGetSources(project);
        if (sources != null) {
            SourceGroup[] sourceGroupArr = sources.getSourceGroups(Utils.SOURCES_TYPE_BPELPRO);
            //
            for (SourceGroup srcGroup : sourceGroupArr) {
                String srcFolderName = srcGroup.getRootFolder().getPath();
                //
                if (targetFoPath.startsWith(srcFolderName)) {
                    return targetFoPath.substring(srcFolderName.length());
                }
            }
        }
        //TODO:it's strange that we are here since we have already checked that
        //our FileObject belongs to our Project, but still we couldn't calculate the
        //relative path. Should we assert?
        return null;
    }
}
