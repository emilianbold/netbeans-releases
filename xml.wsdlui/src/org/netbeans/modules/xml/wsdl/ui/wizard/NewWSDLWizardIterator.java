/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ui.ProjectConstants;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

public final class NewWSDLWizardIterator implements TemplateWizard.Iterator {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor.Panel folderPanel;
    private static final Logger logger = Logger.getLogger(NewWSDLWizardIterator.class.getName());

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private Panel[] createPanels(Project project, TemplateWizard wizard) {
        Sources sources = project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        List<SourceGroup> roots = new ArrayList<SourceGroup>();
        SourceGroup[] javaRoots =
                sources.getSourceGroups(ProjectConstants.JAVA_SOURCES_TYPE);
        roots.addAll(Arrays.asList(javaRoots));
        if (roots.isEmpty()) {
            SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            roots.addAll(Arrays.asList(sourceGroups));
        }
        DataFolder folder = DataFolder.findFolder(roots.get(0).getRootFolder());
        DataFolder projectFolder =
                DataFolder.findFolder(project.getProjectDirectory());
        try {
            if (wizard.getTargetFolder().equals(projectFolder)) {
                wizard.setTargetFolder(folder);
            }
        } catch (IOException ioe) {
            wizard.setTargetFolder(folder);
        }
        SourceGroup[] sourceGroups = roots.toArray(new SourceGroup[roots.size()]);
        folderPanel = new WsdlPanel(project);
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel =
                new WizardNewWSDLStep(Templates.createSimpleTargetChooser(project, sourceGroups, folderPanel));
        JComponent c = (JComponent) firstPanel.getComponent();
        // the bottom panel should listen to changes on file name text field
        ((WsdlPanel) folderPanel).setNameTF(findFileNameField(c, Templates.getTargetName(wizard)));

        WizardDescriptor.Panel secondPanel = new WizardPortTypeConfigurationStep(project);
        WizardDescriptor.Panel thirdPanel = new WizardBindingConfigurationStep();

        return new WizardDescriptor.Panel[]{
                    firstPanel,
                    secondPanel,
                    thirdPanel
                };
    }

    public Set<DataObject> instantiate(final TemplateWizard wiz) throws IOException {
        //Copy contents of temp model to a new file.
        //find the dataobject for the new file and return it.
        final FileObject dir = Templates.getTargetFolder(wiz);
        final String encoding = (String) wiz.getProperty(WsdlPanel.ENCODING);
        final String name = Templates.getTargetName(wiz);
        FileSystem filesystem = dir.getFileSystem();
        final FileObject[] fileObject = new FileObject[1];
        FileSystem.AtomicAction fsAction = new FileSystem.AtomicAction() {

            public void run() throws IOException {

                FileObject fo = dir.createData(name, "wsdl"); //NOI18N

                FileLock lock = null;
                try {
                    lock = fo.lock();
                    OutputStream out = fo.getOutputStream(lock);
                    out = new BufferedOutputStream(out);
                    Writer writer = new OutputStreamWriter(out, encoding);
                    WSDLModel tempModel = (WSDLModel) wiz.getProperty(WizardPortTypeConfigurationStep.TEMP_WSDLMODEL);

                    DefaultProjectCatalogSupport catalogSupport = DefaultProjectCatalogSupport.getInstance(fo);

                    if (tempModel != null) {
                        try {
                            postProcessImports(tempModel, fo, catalogSupport);
                            addSchemaImport(tempModel, fo, catalogSupport);
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ex);
                        }

                        Document doc = tempModel.getBaseDocument();
                        try {
                            writer.write(doc.getText(0, doc.getLength()));
                            writer.flush();
                        } catch (BadLocationException e) {
                            ErrorManager.getDefault().notify(e);
                        } finally {
                            writer.close();
                        }

                        fileObject[0] = fo;
                    }
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        };

        filesystem.runAtomicAction(fsAction);

        Set set = new HashSet(1);
        DataObject createdObject = DataObject.find(fileObject[0]);
        set.add(createdObject);
        return set;

    }

    private void postProcessImports(WSDLModel model, FileObject fobj, DefaultProjectCatalogSupport catalogSupport) throws Exception {
        Types types = model.getDefinitions().getTypes();
        if (types != null) {
            Collection<WSDLSchema> schemas = types.getExtensibilityElements(WSDLSchema.class);

            if (schemas != null) {

                if (schemas.iterator().hasNext()) {
                    WSDLSchema wsdlSchema = schemas.iterator().next();
                    SchemaModel sModel = wsdlSchema.getSchemaModel();
                    if (sModel != null) {
                        Schema schema = sModel.getSchema();
                        if (schema != null && schema.getImports() != null) {
                            model.startTransaction();
                            Iterator<Import> it = schema.getImports().iterator();
                            while (it.hasNext()) {
                                Import imp = it.next();
                                postProcessImport(imp, sModel, fobj, catalogSupport);
                            }
                            model.endTransaction();
                        }

                    }
                }
            }
        }
    }

    private void postProcessImport(Import imp, SchemaModel model, FileObject fobj, DefaultProjectCatalogSupport catalogSupport) {
        String namespace = imp.getNamespace();
        Collection<Schema> schemas = model.findSchemas(namespace);
        Iterator<Schema> it = schemas.iterator();
        while (it.hasNext()) {
            Schema schema = it.next();
            SchemaModel sModel = schema.getModel();
            FileObject schemaFileObj = sModel.getModelSource().getLookup().lookup(FileObject.class);
            URI uri = null;
            try {
                uri = schemaFileObj.getURL().toURI();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Not able to convert " + schemaFileObj.getName() + "to a valid URI", e);
            }
            String location = getRelativePathOfSchema(fobj, uri, catalogSupport);
            imp.setSchemaLocation(location);
        }
    }

    private void addSchemaImport(WSDLModel model, FileObject fobj, DefaultProjectCatalogSupport catalogSupport) {
        WsdlPanel panel = (WsdlPanel) folderPanel;
        WsdlUIPanel.SchemaInfo[] infos = panel.getSchemas();
        if (panel.isImport() && infos.length > 0) {
            String targetNamespace = panel.getNS();

            Schema schema = null;
            WSDLSchema wsdlSchema = null;

            for (int i = 0; i < infos.length; i++) {
                String ns = infos[i].getNamespace();
                if (ns.length() == 0) {
                    ns = targetNamespace;
                }

                String prefix = "ns" + String.valueOf(i + 1);


                String relativePath = null;
                String schemaFileName = infos[i].getSchemaName();
                File schemaFile = new File(schemaFileName);
                URI schemaFileURI = null;
                if (schemaFile.exists()) {
                    schemaFile = FileUtil.normalizeFile(schemaFile);
                    FileObject schemaFO = FileUtil.toFileObject(schemaFile);
                    try {
                        schemaFileURI = schemaFO.getURL().toURI();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Not able to convert " + schemaFileName + "to a valid URI", e);
                    }
                }
                if (schemaFileURI == null) {
                    try {
                        schemaFileURI = new URI(schemaFileName);
                    } catch (URISyntaxException e) {
                        logger.log(Level.SEVERE, schemaFileName + "is not a valid URI", e);
                    }
                }
                relativePath = getRelativePathOfSchema(fobj, schemaFileURI, catalogSupport);

                Definitions def = model.getDefinitions();
                model.startTransaction();
                try {
                    Types types = def.getTypes();
                    if (types == null) {
                        types = model.getFactory().createTypes();
                        def.setTypes(types);
                    }

                    List<WSDLSchema> wsdlSchemas = types.getExtensibilityElements(WSDLSchema.class);

                    if (wsdlSchemas == null || wsdlSchemas.size() == 0) {
                        wsdlSchema = model.getFactory().createWSDLSchema();
                        SchemaModel schemaModel = wsdlSchema.getSchemaModel();
                        schema = schemaModel.getSchema();
                        schema.setTargetNamespace(model.getDefinitions().getTargetNamespace());
                        types.addExtensibilityElement(wsdlSchema);
                    } else {
                        wsdlSchema = wsdlSchemas.get(0);
                        SchemaModel schemaModel = wsdlSchema.getSchemaModel();
                        schema = schemaModel.getSchema();
                    }



                    if (!isSchemaImportExists(relativePath, ns, schema)) {
                        schema.addPrefix(prefix, ns);
                        ((AbstractDocumentComponent) def).addPrefix(prefix, ns);

                        org.netbeans.modules.xml.schema.model.Import schemaImport =
                                schema.getModel().getFactory().createImport();
                        schemaImport.setNamespace(ns);
                        schemaImport.setSchemaLocation(relativePath);

                        schema.addExternalReference(schemaImport);
                    }
                } finally {
                    model.endTransaction();
                }
            }
        }

    }

    private boolean isSchemaImportExists(String schemaLocation, String namespace, Schema schema) {
        boolean isImportExist = false;
        Collection<Import> imports = schema.getImports();
        Iterator<Import> it = imports.iterator();
        while (it.hasNext()) {
            Import imp = it.next();

            String sLoc = imp.getSchemaLocation();
            String ns = imp.getNamespace();

            if (ns != null && ns.equals(namespace) && sLoc != null && sLoc.equals(schemaLocation)) {
                isImportExist = true;
                break;
            }
        }

        return isImportExist;
    }

    private String getRelativePathOfSchema(FileObject fo, URI schemaURI, DefaultProjectCatalogSupport catalogSupport) {
        File f = FileUtil.toFile(fo);
        FileObject schemaFO = FileUtil.toFileObject(new File(schemaURI));

        String relativePath = null;
        if (catalogSupport != null && catalogSupport.needsCatalogEntry(fo, schemaFO)) {
//          Remove the previous catalog entry, then create new one.
            URI uri;
            try {
                uri = catalogSupport.getReferenceURI(fo, schemaFO);
                catalogSupport.removeCatalogEntry(uri);
                catalogSupport.createCatalogEntry(fo, schemaFO);
                relativePath = catalogSupport.getReferenceURI(fo, schemaFO).toString();
            } catch (URISyntaxException use) {
                ErrorManager.getDefault().notify(use);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            } catch (CatalogModelException cme) {
                ErrorManager.getDefault().notify(cme);
            }
        } else {
            relativePath = org.netbeans.modules.xml.retriever.catalog.Utilities.relativize(f.toURI(), schemaURI);
        }
        return relativePath;
    }

    public void initialize(TemplateWizard wiz) {
        index = 0;
        Project project = Templates.getProject(wiz);
        panels = createPanels(project, wiz);

        // Creating steps.
        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }

        String encoding = EncodingUtil.getProjectEncoding(project.getProjectDirectory());
        if (encoding == null) {
            encoding = "UTF8";
        }
        wiz.putProperty(WsdlPanel.ENCODING, encoding);

    }

    public void uninitialize(TemplateWizard wiz) {

        panels = null;
        folderPanel = null;
        wiz.putProperty(WizardPortTypeConfigurationStep.TEMP_WSDLMODEL, null);
        
        File file = (File) wiz.getProperty(WizardPortTypeConfigurationStep.TEMP_WSDLFILE);
        wiz.putProperty(WizardPortTypeConfigurationStep.TEMP_WSDLFILE, null);
        
        if (file != null && file.exists()) {
            file = FileUtil.normalizeFile(file);
            FileObject fileObj = FileUtil.toFileObject(file);
            if (fileObj != null) {
                DataObject dobj;
                try {
                    dobj = DataObject.find(fileObj);
                    dobj.delete();
                } catch (Exception e) {
                    //ignore.
                }
                
            }
        }

    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return index + 1 + ". from " + panels.length;
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    //from schema wizard
    private JTextField findFileNameField(Component panel, String text) {
        Collection<Component> allComponents = new ArrayList<Component>();
        getAllComponents(new Component[]{panel}, allComponents);
        for (Component c : allComponents) {
            // we assume that the first text field is the file text field
            if (c instanceof JTextField) {
                JTextField tf = (JTextField) c;
                //if (text.equals(tf.getText())) {
                return tf;
            //}
            }
        }
        return null;
    }

    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public void getAllComponents(Component[] components, Collection<Component> allComponents) {
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                allComponents.add(components[i]);
                if (((Container) components[i]).getComponentCount() != 0) {
                    getAllComponents(((Container) components[i]).getComponents(), allComponents);
                }
            }
        }
    }
}
