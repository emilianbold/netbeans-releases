/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.ui.basic;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.ui.basic.SchemaModelCookie;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.w3c.dom.NodeList;

/**
 * A utility class.
 *
 * @author Jun Xu
 */
public class ModelUtils {

    private static final String EMPTY_DOC =
            "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\"/>"; //NOI18N
    
    /**
     * Gets the schema model from a XSD node.  This method is copied from
     * <code>org.netbeans.modules.xml.schema.core.actions.SchemaTransformAction</code>.
     * @param node the XSD Node instance
     * @return the schema model if the node has one, otherwise null
     */
    public static SchemaModel getSchemaModelFromNode(final Node node)
            throws IOException {
        if(node == null)
            return null;
        
        DataObject dobj = node.getCookie(DataObject.class);
        SchemaModelCookie modelCookie =
                dobj.getCookie(SchemaModelCookie.class);
        
        return modelCookie.getModel();
    }

    /**
     * Checks if the schema model is writable.
     * @param model the schema model
     * @return true if the model is writable
     */
    public static boolean isModelWritable(Model model) {
        if (model != null) {
            ModelSource ms = model.getModelSource();
            if (ms.isEditable()) {
                FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
                if (fo != null) {
                    return fo.canWrite();
                }
            }
        }
        return false;
    }
    
    public static String getFilePath(Model model) {
        if (model != null) {
            ModelSource ms = model.getModelSource();
            FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
            if (fo != null) {
                return FileUtil.toFile(fo).getAbsolutePath();
            }
        }
        return null;
    }
    
    public static URL getFileURL(Model model) throws FileStateInvalidException {
        if (model != null) {
            ModelSource ms = model.getModelSource();
            FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
            if (fo != null) {
                return fo.getURL();
            }
        }
        return null;
    }
    
    public static String getPublicId(SchemaModel model) {
        return model.getSchema().getTargetNamespace();
    }
    
    public static String getSystemId(SchemaModel model) {
        URL url = null;
        String systemId;
        try {
            url = getFileURL(model);
        } catch (FileStateInvalidException ex1) {
        }
        if (url != null) {
            systemId = url.toExternalForm();
        } else {
            systemId = getFilePath(model);
        }
        return systemId;
    }
    
    public static void applyEncodingMark(SchemaModel model, String displayName,
            String namespace, String style) throws IOException {
        Schema schema = model.getSchema();
        Annotation anno = schema.getAnnotation();
        if (anno == null) {
            anno = model.getFactory().createAnnotation();
            schema.setAnnotation(anno);
        }
        for (AppInfo appInfo : anno.getAppInfos()) {
            if (EncodingConst.URI.equals(appInfo.getURI())) {
                return;
            }
        }
        AppInfo newAppInfo = model.getFactory().createAppInfo();
        newAppInfo.setURI(EncodingConst.URI);
        StringBuffer sb = new StringBuffer();
        sb.append("<enc:encoding xmlns:enc=\"").append(EncodingConst.URI).append('"'); //NOI18N
        sb.append(" name=\"").append(displayName).append('"'); //NOI18N
        sb.append(" namespace=\"").append(namespace).append('"'); //NOI18N
        sb.append(" style=\"").append(style).append("\"/>"); //NOI18N
        newAppInfo.setContentFragment(sb.toString());
        anno.addAppInfo(newAppInfo);
    }
    
    public static boolean isEncodedWith(SchemaModel model, String style) {
        EncodingMark mark = getEncodingMark(model);
        return mark != null && style.equals(mark.getStyle());
    }
    
    /**
     * Checks if the model has an encoding mark
     *
     * @param model the schema model
     * @return true if the schema model has the encoding marker, otherwise false
     */
    public static boolean hasEncodingMark(SchemaModel model) {
        Schema schema = model.getSchema();
        Annotation anno = schema.getAnnotation();
        if (anno == null) {
            return false;
        }
        for (AppInfo appInfo : anno.getAppInfos()) {
            if (EncodingConst.URI.equals(appInfo.getURI())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes the encoding mark from a schema model.
     *
     * @param model the schema model
     * @return true if indeed modified something
     */
    public static boolean removeEncodingMark(SchemaModel model) {
        Schema schema = model.getSchema();
        Annotation anno = schema.getAnnotation();
        if (anno == null) {
            return false;
        }
        AppInfo[] appInfos = anno.getAppInfos().toArray(new AppInfo[0]);
        boolean modified = false;
        for (int i = 0; appInfos != null && i < appInfos.length; i++) {
            if (EncodingConst.URI.equals(appInfos[i].getURI())) {
                anno.removeAppInfo(appInfos[i]);
                modified = true;
            }
        }
        return modified;
    }
    
    public static AppInfo getEncodingAppinfo(Annotation anno) {
        Collection<AppInfo> appinfos = anno.getAppInfos();
        if (appinfos == null || appinfos.size() == 0) {
            return null;
        }
        for (AppInfo appinfo : appinfos) {
            if (EncodingConst.URI.equals(appinfo.getURI())) {
                return appinfo;
            }
        }
        return null;
    }
    
    /**
     * Gets the encoding mark of the model
     *
     * @param model the schema model
     * @return the encoding mark if the schema model has an encoding mark,
     *          otherwise <code>null</code>
     */
    public static EncodingMark getEncodingMark(SchemaModel model) {
        Schema schema = model.getSchema();
        Annotation anno = schema.getAnnotation();
        if (anno == null) {
            return null;
        }
        for (AppInfo appInfo : anno.getAppInfos()) {
            if (EncodingConst.URI.equals(appInfo.getURI())) {
                NodeList nodeList =
                        appInfo.getPeer().getChildNodes();
                if (nodeList == null || nodeList.getLength() == 0) {
                    return null;
                }
                org.w3c.dom.Element encodingElem = null;
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (nodeList.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                        // Skip non-element nodes, e.g. text node (containing Character Data such as \n, spaces, etc.
                        continue;
                    }
                    org.w3c.dom.Element elem = (org.w3c.dom.Element) nodeList.item(i);
                    if (EncodingConst.URI.equals(elem.getNamespaceURI())
                            && "encoding".equals(elem.getLocalName())) { //NOI18N
                        encodingElem = elem;
                        break;
                    } else {
                        String elemName = elem.getNodeName();
                        int pos;
                        String ns, localName;
                        if ((pos = elemName.indexOf(':')) > 0) {
                            ns = elem.lookupNamespaceURI(elemName.substring(0, pos));
                            localName = elemName.substring(pos + 1);
                        } else {
                            ns = elem.lookupNamespaceURI(""); //NOI18N
                            localName = elemName;
                        }
                        if (EncodingConst.URI.equals(ns)
                                && "encoding".equals(localName)) { //NOI18N
                            encodingElem = elem;
                            break;
                        }
                    }
                }
                if (encodingElem != null) {
                    return new EncodingMark(encodingElem.getAttribute("name"), //NOI18N
                            encodingElem.getAttribute("namespace"), encodingElem.getAttribute("style"));  //NOI18N
                }
            }
        }
        return null;
    }
    
    public static Collection<GlobalElement> getTopElements(SchemaModel model) {
        List<GlobalElement> topElements = new ArrayList<GlobalElement>();
        Schema schema = model.getSchema();
        Collection<GlobalElement> elements = schema.getElements();
        if (elements == null) {
            return topElements;
        }
        NodeList nodeList;
        for (GlobalElement element : elements) {
            Annotation anno = element.getAnnotation();
            if (anno == null) {
                continue;
            }
            Collection<AppInfo> appInfos = anno.getAppInfos();
            for (AppInfo appInfo : appInfos) {
                if (EncodingConst.URI.equals(appInfo.getURI())) {
                    nodeList = appInfo.getPeer().getChildNodes();
                    if (nodeList == null || nodeList.getLength() == 0) {
                        break;
                    }
                    org.w3c.dom.Element topFlagElem = null;
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        if (nodeList.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                            continue;
                        }
                        org.w3c.dom.Element elem = (org.w3c.dom.Element) nodeList.item(i);
                        if (EncodingConst.URI.equals(elem.getNamespaceURI())
                                && EncodingConst.TOP_FLAG.equals(elem.getLocalName())) {
                            topFlagElem = elem;
                            break;
                        } else {
                            String elemName = elem.getNodeName();
                            int pos;
                            String ns, localName;
                            if ((pos = elemName.indexOf(':')) > 0) {
                                ns = elem.lookupNamespaceURI(elemName.substring(0, pos));
                                localName = elemName.substring(pos + 1);
                            } else {
                                ns = elem.lookupNamespaceURI("");  //NOI18N
                                localName = elemName;
                            }
                            if (EncodingConst.URI.equals(ns)
                                    && EncodingConst.TOP_FLAG.equals(localName)) {
                                topFlagElem = elem;
                                break;
                            }
                        }
                    }
                    if (topFlagElem != null) {
                        nodeList = topFlagElem.getChildNodes();
                        if (nodeList.getLength() > 0
                                && nodeList.item(0).getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                            if ("true".equals(nodeList.item(0).getNodeValue())) { //NOI18N
                                topElements.add(element);
                            }
                        }
                    }
                }
            }
        }
        return topElements;
    }
    
    public static void visitModel(SchemaModel model, ModelVisitor visitor,
            boolean includeReferenced)
            throws CatalogModelException {
        Set<SchemaModel> modelVisited = new HashSet<SchemaModel>();
        Set<SchemaModel> modelToBeVisited = new HashSet<SchemaModel>();
        modelToBeVisited.add(model);
        SchemaModel refModel;
        while (!modelToBeVisited.isEmpty()) {
            model = modelToBeVisited.iterator().next();
            if (!visitor.visit(model)) {
                break;
            }
            ModelSource source = model.getModelSource();
            if (!visitor.visit((Document) source.getLookup().lookup(Document.class))) {
                break;
            }
            FileObject fo = (FileObject) source.getLookup().lookup(FileObject.class);
            if (!visitor.visit(fo)) {
                break;
            }
            try {
                DataObject dataObj = DataObject.find(fo);
                if (!visitor.visit(dataObj)) {
                    break;
                }
            } catch (DataObjectNotFoundException ex) {
                //Ignore
            }
            if (!includeReferenced) {
                break;
            }
            modelToBeVisited.remove(model);
            modelVisited.add(model);
            Collection<SchemaModelReference> refs = model.getSchema().getSchemaReferences();
            for (SchemaModelReference schemaRef : refs) {
                refModel = schemaRef.resolveReferencedModel();
                if (!modelVisited.contains(refModel)) {
                    modelToBeVisited.add(refModel);
                }
            }
        }
    }
    
    public static ModelStatus getModelStatus(SchemaModel model)
            throws CatalogModelException {
        return getModelStatus(model, true);
    }
    
    public static ModelStatus getModelStatus(SchemaModel model, boolean includeReferenced)
            throws CatalogModelException {
        ModelStatusCollector msc = new ModelStatusCollector();
        visitModel(model, msc, includeReferenced);
        return msc.getModelStatus();
    }
    
    public static class ModelStatus {
        private final int mTotalCharSize;
        private final boolean mIsModified;
        private final String[] mModifiedFiles;
        
        private ModelStatus (int totalCharSize, boolean modified, String[] modifiedFiles) {
            mTotalCharSize = totalCharSize;
            mIsModified = modified;
            mModifiedFiles = modifiedFiles;
        }
        
        public int getTotalCharSize() {
            return mTotalCharSize;
        }
        
        public boolean isModified() {
            return mIsModified;
        }
        
        public String[] getModifiedFiles() {
            return mModifiedFiles;
        }
    }
    
    private static class ModelStatusCollector implements ModelVisitor {
        private int mTotalCharSize = 0;
        private boolean mIsModified = false;
        private List<String> mModifiedFiles = new ArrayList<String>();
        
        public boolean visit(SchemaModel model) {
            return true;
        }

        public boolean visit(Document doc) {
            mTotalCharSize += (doc == null ? 0 : doc.getLength());
            return true;
        }

        public boolean visit(FileObject fileObj) {
            return true;
        }

        public boolean visit(DataObject dataObj) {
            if (dataObj == null) {
                return true;
            }
            if (dataObj.isModified()) {
                mIsModified = true;
                Set<FileObject> files = dataObj.files();
                if (files != null) {
                    Iterator<FileObject> iter = files.iterator();
                    while (iter.hasNext()) {
                        mModifiedFiles.add(iter.next().getNameExt());
                    }
                }
            }
            return true;
        }
        
        public ModelStatus getModelStatus() {
            return new ModelStatus(mTotalCharSize, mIsModified,
                    mModifiedFiles.toArray(new String[0]));
        }
    }
}
