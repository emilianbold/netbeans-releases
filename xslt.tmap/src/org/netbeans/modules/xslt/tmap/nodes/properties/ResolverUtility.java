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
package org.netbeans.modules.xslt.tmap.nodes.properties;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xslt.tmap.model.api.Import;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;

/**
 * The utility class containing auxiliary methods to work with WSDL
 *
 * ATTENTION! Many methods contains the lookup as patamether.
 * It is implied that it contains the BpelModel instance.
 *
 * @author Vitaly Bychkov
 * @author nk160297
 */
public final class ResolverUtility {
    
    private static final Logger LOGGER = Logger.getLogger(ResolverUtility.class.getName());
    
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
    public static String qName2DisplayText(QName qValue, TMapComponent relativeTo) {
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
//    
//    public static String getDisplayName(Component comp) {
//        return getDisplayName(comp, null);
//    }
//    
//    public static String getDisplayName(Component comp, TMapComponent relativeTo) {
//        String targetNamespace = null;
//        String prefix = null;
//        String compName = null;
//        //
//        if (comp instanceof TMapComponent) {
//            targetNamespace = ((TMapComponent)comp).getModel().
//                    getTransformMap().getNamespaceContext().;
//            if (comp instanceof NamedElement) {
//                compName = ((NamedElement)comp).getName();
//            }
//        } else if (comp instanceof WSDLComponent) {
//            targetNamespace = ((WSDLComponent)comp).getModel().
//                    getDefinitions().getTargetNamespace();
//            if (comp instanceof Named) {
//                compName = ((Named)comp).getName();
//            }
//        } else if (comp instanceof SchemaComponent) {
//            targetNamespace = ((SchemaComponent)comp).getModel().
//                    getSchema().getTargetNamespace();
//            if (comp instanceof Named) {
//                compName = ((Named)comp).getName();
//            }
//        }
//        //
//        assert compName != null :
//            "Impossible to calculate the name for a component which hasn't a name!"; // NOI18N
//        //
//        if (targetNamespace != null && targetNamespace.length() > 0) {
//            if (relativeTo != null) {
//                NamespaceContext nc = relativeTo.getNamespaceContext();
//                prefix = nc.getPrefix(targetNamespace);
//            }
//            if (prefix == null || prefix.length() == 0) {
//                return targetNamespace + Constants.COLON + compName;
//            } else {
//                return prefix + Constants.COLON + compName;
//            }
//        } else {
//            return compName;
//        }
//    }
//    
//    /**
//     * Returns projectSource related to the given bpelModel which is in lookup
//     * Could return null
//     */ 
//    public static FileObject getProjectSource(Lookup lookup) {
//        BpelModel bpelModel = lookup.lookup(BpelModel.class);
//        if (bpelModel == null) {
//            return null;
//        }
//        FileObject bpelFo = Util.getFileObjectByModel(bpelModel);
//        if (bpelFo == null) {
//            return null;
//        }
//        Sources sources = safeGetSources(safeGetProject(bpelModel));
//        if (sources == null) {
//            return null;
//        }
//        
//        String bpelFoPath = bpelFo.getPath();
//        SourceGroup[] sourceGroupArr = sources.getSourceGroups(
//                ProjectConstants.SOURCES_TYPE_BPELPRO);
//        for (SourceGroup srcGroup : sourceGroupArr) {
//            String srcFolderName = srcGroup.getRootFolder().getPath();
//            //
//            if (bpelFoPath.startsWith(srcFolderName)) {
//                return srcGroup.getRootFolder();
//            }
//        }
//        return null;
//    }
//    
    public static String encodeLocation(String location){
        return location == null ? null : location.replace(" ", "%20"); // NOI18N
    }
    
    public static String decodeLocation(String location){
        return location == null ? null : location.replace("%20", " "); // NOI18N
    }

//    /**
//     * Returns the FileObject which points to the folder where the specified
//     * BPEL Process is located.
//     *
//     * This method can return null;
//     */
//    public static FileObject getBpelProcessFolder(BpelModel bpelModel) {
//        if (bpelModel != null) {
//            ModelSource bpelMS = bpelModel.getModelSource();
//            if (bpelMS != null) {
//                Lookup bpelLookup = bpelMS.getLookup();
//                if (bpelLookup != null) {
//                    FileObject bpelFo = bpelLookup.lookup(FileObject.class);
//                    if (bpelFo != null) {
//                        FileObject bpelFolderFo = bpelFo.getParent();
//                        return bpelFolderFo;
//                    }
//                }
//            }
//        }
//        return null;
//    }
//    
    /**
     * Check if the specified model is imported to the current transformmap.
     */
    public static boolean isModelImported(Model model, Lookup lookup) {
        TMapModel tMapModel = lookup.lookup(TMapModel.class);
        return isModelImported(model, tMapModel);
    }
    
    /**
     * Check if the specified model is imported to the current transformmap.
     */
    public static boolean isModelImported(Model model, TMapModel tMapModel) {
        if (model == SchemaModelFactory.getDefault().getPrimitiveTypesModel()) {
            // the primitive types' model considered as imported implicitly.
            return true;
        }
        
        for (Import imp : tMapModel.getTransformMap().getImports()) {
            try {
            if (model == imp.getImportModel()) {
                return true;
            }
            } catch (CatalogModelException ex) {
                LOGGER.log(Level.INFO, "exception on imported model resolving");
                return false;
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
            if (obj instanceof VariableReference) {
                result = ((VariableReference)obj).getReferencedVariable().getName();
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
        try {
            WSDLModel wsdlModel = importObj.getImportModel();
            if (wsdlModel != null) {
                return wsdlModel.getModelSource();
            }

        } catch (CatalogModelException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }
    
//    public static FileObject getImportedFileObject(Import importObj) {
//        ModelSource modelSource = getImportedModelSource(importObj);
//        
//        if (modelSource != null) {
//            return modelSource.getLookup().lookup(FileObject.class);
//        } else {
//            return null;
//        }
//    }
//    
//    /* 
//     * Description is shown in e.g. TypeChooserPanel. It's either a path
//     * to the imported file or a message that a file doesn't exist.
//     */
//    public static String getImportDescription(Import importObj) {
//        FileObject fo = getImportedFileObject(importObj);
//        if (fo != null && fo.isValid()) {
//            return fo.getPath();
//        } else {
//            //No valid Model or ModelSource or FileObject found - return warning
//            String importInfo = importObj.getLocation();
//            if (importInfo == null || importInfo.length() == 0) {
//                importInfo = importObj.getNamespace();
//            }
//            return NbBundle.getMessage(FormBundle.class,
//                    "ERR_IMPORT_FILE_DOESNT_EXIST", // NOI18N
//                    ResolverUtility.decodeLocation(importInfo), "");
//        }
//    }
//    
//    /**
//     * Returns the project Sources for the specified bpel process.
//     * Use the following line to access the SourceGroup array.
//     *  SourceGroup[] sourceGroup =
//     *  sources.getSourceGroups(ProjectConstants.SOURCES_TYPE_BPELPRO);
//     */
//    public static Sources safeGetSources(Project project) {
//        if (project != null) {
//            return ProjectUtils.getSources(project);
//        } else {
//            return null;
//        }
//    }
//    
//    public static Project safeGetProject(BpelModel bpelModel) {
//        FileObject fo = Util.getFileObjectByModel(bpelModel);
//        if (fo != null && fo.isValid()) {
//            return FileOwnerQuery.getOwner(fo);
//        } else {
//            return null;
//        }
//    }
//    
//    /*
//     * Returns the relative path of a given FileObject in a given Project.
//     * Returns null if either of the parameters is null or given FileObject
//     * is not in the given Project.
//     */
//    public static String safeGetRelativePath(FileObject fo, Project project) {
//        if (fo == null || !fo.isValid() || project == null) {
//            return null;
//        }
//        
//        if (FileOwnerQuery.getOwner(fo) != project) {
//            return null;
//        }
//        
//        String targetFoPath = fo.getPath();
//        //
//        Sources sources = safeGetSources(project);
//        if (sources != null) {
//            SourceGroup[] sourceGroupArr = sources.getSourceGroups(
//                    ProjectConstants.SOURCES_TYPE_BPELPRO);
//            //
//            for (SourceGroup srcGroup : sourceGroupArr) {
//                String srcFolderName = srcGroup.getRootFolder().getPath();
//                //
//                if (targetFoPath.startsWith(srcFolderName)) {
//                    return targetFoPath.substring(srcFolderName.length());
//                }
//            }
//        }
//        //TODO:it's strange that we are here since we have already checked that
//        //our FileObject belongs to our Project, but still we couldn't calculate the
//        //relative path. Should we assert?
//        return null;
//    }
}
