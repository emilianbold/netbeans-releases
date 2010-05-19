/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.namespace.QName;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;

import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class Utility {

    public static String getNamespacePrefix(String namespace, AbstractDocumentComponent component) {
        if (component != null && namespace != null) {
            return component.lookupPrefix(namespace);
        }
        return null;
    }

    public static String getNamespacePrefix(String namespace, WLMComponent element) {
        if (element != null && namespace != null) {
            return ((AbstractDocumentComponent) element).lookupPrefix(namespace);
        }
        return null;
    }

    public static String getNamespaceURI(String prefix, WSDLComponent element) {
        if (element != null && prefix != null) {
            return ((AbstractDocumentComponent) element).lookupNamespaceURI(prefix, true);
        }
        return null;
    }

    public static String getNamespaceURI(String prefix, WLMModel model) {
        if (model != null && prefix != null) {
            return ((AbstractDocumentComponent) model.getTask()).lookupNamespaceURI(prefix, true);
        }
        return null;
    }

    public static Import getImport(String namespace, WSDLModel model) {
        Collection imports = model.getDefinitions().getImports();
        if (imports != null) {
            Iterator iter = imports.iterator();
            for (; iter.hasNext();) {
                Import existingImport = (Import) iter.next();
                if (existingImport.getNamespace().equals(namespace)) {
                    return existingImport;
                }
            }
        }
        return null;
    }

    public static Collection<WSDLModel> getImportedDocuments(WSDLModel model) {
        Collection<Import> imports = model.getDefinitions().getImports();
        Collection<WSDLModel> returnImports = new ArrayList<WSDLModel>();
        if (imports != null) {
            Iterator iter = imports.iterator();
            for (; iter.hasNext();) {
                Import existingImport = (Import) iter.next();
                List<WSDLModel> impModels = model.findWSDLModel(existingImport.getNamespace());
                returnImports.addAll(impModels);
            }
        }
        return returnImports;
    }

    public static Map getNamespaces(Definitions def) {
        return ((AbstractDocumentComponent) def).getPrefixes();
    }

    public static String fromQNameToString(QName qname) {
        if (qname.getPrefix() != null && qname.getPrefix().trim().length() > 0) {
            return qname.getPrefix() + ":" + qname.getLocalPart();
        }
        return qname.getLocalPart();
    }

    public static String getNameAndDropPrefixIfInCurrentModel(String ns, String localPart, WLMModel model) {
        if (ns == null || model == null) {
            return localPart;
        }
        String tns = model.getTask().getTargetNamespace();
        if (tns != null && !tns.equals(ns)) {
            String prefix = getNamespacePrefix(ns, model.getTask());
            if (prefix != null) {
                return prefix + ":" + localPart;
            }
        }

        return localPart;
    }

    public static List<QName> getExtensionAttributes(WSDLComponent comp) {
        ArrayList<QName> result = new ArrayList<QName>();
        Map<QName, String> attrMap = comp.getAttributeMap();
        Set<QName> set = attrMap.keySet();
        if (set != null) {
            Iterator<QName> iter = set.iterator();
            while (iter.hasNext()) {
                QName name = iter.next();
                String ns = name.getNamespaceURI();
                if (ns != null && ns.trim().length() > 0 && !ns.equals(((AbstractDocumentComponent) comp).getQName().getNamespaceURI())) {
                    result.add(name);
                }
            }
        }
        return result;
    }

    /**
     * Expands nodes on the treeview till given levels
     * @param tv the treeview object
     * @param level the level till which the nodes should be expanded. 0 means none.
     * @param rootNode the rootNode
     */
    public static void expandNodes(TreeView tv, int level, Node rootNode) {
        if (level == 0) {
            return;
        }
        Children children = rootNode.getChildren();
        if (children != null) {
            Node[] nodes = children.getNodes();
            if (nodes != null) {
                for (int i = 0; i < nodes.length; i++) {
                    tv.expandNode(nodes[i]); //Expand node
                    expandNodes(tv, level - 1, nodes[i]); //expand children
                }
            }
        }
    }

    public static void addNamespacePrefix(WSDLComponent comp, WLMModel model, String prefix) {
        if (comp != null && model != null) {
            addNamespacePrefix(comp.getModel(), model, prefix);
        }
    }

    private static void addNamespacePrefix(WSDLModel imported, WLMModel model, String prefix) {
        assert model != null;
        if (imported != null) {
            TTask task = model.getTask();
            String targetNamespace = imported.getDefinitions().getTargetNamespace();
            String computedPrefix = null;

            if (targetNamespace != null) {
                if (Utility.getNamespacePrefix(targetNamespace, task) != null) {
                    //already exists, doesnt need to be added
                    return;
                }
                //Use the prefix (in parameter) or generate new one.
                if (prefix != null) {
                    computedPrefix = prefix;
                } else {
                    computedPrefix = NameGenerator.getInstance().generateNamespacePrefix(null, task);
                }
                boolean isAlreadyInTransaction = Utility.startTransaction(model);
                ((AbstractDocumentComponent) task).addPrefix(computedPrefix, targetNamespace);

                Utility.endTransaction(model, isAlreadyInTransaction);

            }

        }

    }

    public static boolean startTransaction(WLMModel model) {
        boolean isInTransaction = model.isIntransaction();
        if (isInTransaction) {
            return true;
        }
        model.startTransaction();
        return false;
    }

    public static void endTransaction(WLMModel model, boolean isInTransaction) {
        if (isInTransaction) {
            return;
        }
        model.endTransaction();
    }

    public static Collection<Operation> getImplementableOperations(PortType portType, Binding binding) {
        if (portType == null || portType.getOperations() == null || portType.getOperations().size() == 0 || binding == null) {
            return null;
        }
        List<Operation> listData = new ArrayList<Operation>(portType.getOperations().size());
        Set<String> bindingOperationsSet = new HashSet<String>();
        Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
        if (bindingOperations != null) {
            Iterator<BindingOperation> iter = bindingOperations.iterator();
            while (iter.hasNext()) {
                bindingOperationsSet.add(iter.next().getOperation().get().getName());
            }

        }
        Iterator it = portType.getOperations().iterator();

        while (it.hasNext()) {
            Operation operation = (Operation) it.next();
            if (operation.getName() != null) {
                if (!bindingOperationsSet.contains(operation.getName())) {
                    listData.add(operation);
                }
            }
        }

        return listData;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getPrefixes(WSDLComponent wsdlComponent) {
        AbstractDocumentComponent comp = ((AbstractDocumentComponent) wsdlComponent);
        Map<String, String> prefixes = comp.getPrefixes();
        while (comp.getParent() != null) {
            comp = (AbstractDocumentComponent) comp.getParent();
            prefixes.putAll(comp.getPrefixes());
        }

        return prefixes;
    }

    public static void splitExtensibilityElements(List<ExtensibilityElement> list,
            Set<String> specialTargetNamespaces,
            List<ExtensibilityElement> specialExtensibilityElements,
            List<ExtensibilityElement> nonSpecialExtensibilityElements) {
        if (specialExtensibilityElements == null) {
            specialExtensibilityElements = new ArrayList<ExtensibilityElement>();
        }
        if (nonSpecialExtensibilityElements == null) {
            nonSpecialExtensibilityElements = new ArrayList<ExtensibilityElement>();
        }

        if (list != null) {
            for (ExtensibilityElement element : list) {
                if (specialTargetNamespaces.contains(element.getQName().getNamespaceURI())) {
                    specialExtensibilityElements.add(element);
                } else {
                    nonSpecialExtensibilityElements.add(element);
                }
            }
        }
    }

    public static List<ExtensibilityElement> getSpecialExtensibilityElements(List<ExtensibilityElement> list,
            String specialNamespace) {
        List<ExtensibilityElement> specialList = new ArrayList<ExtensibilityElement>();
        if (list != null) {
            for (ExtensibilityElement element : list) {
                if (specialNamespace.equals(element.getQName().getNamespaceURI())) {
                    specialList.add(element);
                }
            }
        }
        return specialList;
    }

    /**
     * This method finds the absolute index in the definitions where the component needs to be inserted, 
     * such that the component is at given index with respect to its kind.
     * 
     * For example, There are 5 messages, and one needs to insert another at index 4. Then this method will insert
     * it at some index on Definitions, which will make it look like the 4th Message.
     * 
     * it doesnt call startTransaction or endTransaction, so its the responsibility of the caller to do it.
     * 
     * @param index
     * @param model
     * @param compToInsert
     * @param propertyName
     */
    public static void insertIntoDefinitionsAtIndex(int index, WSDLModel model, WSDLComponent compToInsert, String propertyName) {
        assert model.isIntransaction() : "Need to call startTransaction on this model, before calling this method";
        //find index among all definitions elements. 
        //for inserting at index = 5, find index of the 4th PLT and insert after this index
        int defIndex = -1;
        int indexOfPreviousPLT = index - 1;
        List<WSDLComponent> comps = model.getDefinitions().getChildren();
        for (int i = 0; i < comps.size(); i++) {
            WSDLComponent comp = comps.get(i);
            if (compToInsert.getClass().isAssignableFrom(comp.getClass())) {
                if (indexOfPreviousPLT > defIndex) {
                    defIndex++;
                } else {
                    ((AbstractComponent<WSDLComponent>) model.getDefinitions()).insertAtIndex(propertyName, compToInsert, i);
                    break;
                }
            }
        }
    }

    public static void addWSDLImport(WSDLComponent comp, WSDLModel wsdlModel) {
        if (comp != null && wsdlModel != null && comp.getModel() != wsdlModel) {

            String importedWSDLTargetNamespace = comp.getModel().getDefinitions().getTargetNamespace();

            if (importedWSDLTargetNamespace != null) {
                Import wsdlImport = wsdlModel.getFactory().createImport();
                wsdlImport.setNamespace(importedWSDLTargetNamespace);

                FileObject wsdlFileObj = wsdlModel.getModelSource().getLookup().lookup(FileObject.class);
                URI wsdlFileURI = FileUtil.toFile(wsdlFileObj).toURI();

                FileObject fo = comp.getModel().getModelSource().getLookup().lookup(FileObject.class);
                String path = null;
                if (!FileUtil.toFile(fo).toURI().equals(wsdlFileURI)) {
                    DefaultProjectCatalogSupport catalogSupport = DefaultProjectCatalogSupport.getInstance(wsdlFileObj);
                    if (catalogSupport.needsCatalogEntry(wsdlFileObj, fo)) {
                        // Remove the previous catalog entry, then create new one.
                        URI uri;
                        try {
                            uri = catalogSupport.getReferenceURI(wsdlFileObj, fo);
                            catalogSupport.removeCatalogEntry(uri);
                            catalogSupport.createCatalogEntry(wsdlFileObj, fo);
                            path = catalogSupport.getReferenceURI(wsdlFileObj, fo).toString();
                        } catch (URISyntaxException use) {
                            ErrorManager.getDefault().notify(use);
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ioe);
                        } catch (CatalogModelException cme) {
                            ErrorManager.getDefault().notify(cme);
                        }
                    } else {
                        //path = RelativePath.getRelativePath(FileUtil.toFile(wsdlFileObj).getParentFile(), FileUtil.toFile(fo));
                        path = FileUtil.getRelativePath(wsdlFileObj.getParent(), fo);
                    }
                }

                if (path != null) {
                    wsdlImport.setLocation(path);
                }
                wsdlModel.getDefinitions().addImport(wsdlImport);
            }
        }
    }

    public static void writeOutputFile(Document doc, FileObject fo, String encoding) {
        try {
            OutputStream out = fo.getOutputStream();
            out = new BufferedOutputStream(out);
            Writer writer = new OutputStreamWriter(out, encoding);
            try {
                writer.write(doc.getText(0, doc.getLength()));
                writer.flush();
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            } finally {
                writer.close();
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    public static String readFileContent(String fileTemplateUrl) throws UnsupportedEncodingException, IOException {
        InputStream inputStream = Utility.class.getResourceAsStream(fileTemplateUrl);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder strBuilder = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null) {
            strBuilder.append(line).append('\n');
        }
        return strBuilder.toString();
    }

    public static File getFile(String content, String encoding, String prefix, String suffix) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        File  file = File.createTempFile(prefix, suffix);
        FileOutputStream stream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(stream, encoding);
        writer.write(content);
        writer.close();
        stream.close();
        return file;
    }
    
     public static WSDLModel createWSDLModel(String wsdlFileURL) {
        File f = FileUtil.normalizeFile(new File(wsdlFileURL));
        FileObject wsdlFileObj = FileUtil.toFileObject(f);
        ModelSource wsdlModelSource = Utilities.getModelSource(wsdlFileObj, wsdlFileObj.canWrite());
        WSDLModel wsdlModel = null;
        if (wsdlModelSource != null) {
            wsdlModel = WSDLModelFactory.getDefault().getModel(wsdlModelSource);
        }
        return wsdlModel;
    }
}
