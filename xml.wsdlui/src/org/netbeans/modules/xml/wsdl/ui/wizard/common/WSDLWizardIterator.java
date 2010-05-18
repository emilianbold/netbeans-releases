/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.xml.wsdl.ui.wizard.common;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.api.property.CatalogHelper;
import org.netbeans.modules.xml.wsdl.ui.wizard.Utilities;
import org.netbeans.modules.xml.wsdl.ui.wizard.WSDLWizardBindingConfigurationWrapperStep;
import org.netbeans.modules.xml.wsdl.ui.wizard.WSDLWizardContextImpl;
import org.netbeans.modules.xml.wsdl.ui.wizard.WizardAbstractConfigurationStep;
import org.netbeans.modules.xml.wsdl.ui.wizard.WizardNewWSDLStep;
import org.netbeans.modules.xml.wsdl.ui.wizard.WsdlPanel;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.BindingUtils;
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
import org.openide.util.ChangeSupport;

public class WSDLWizardIterator implements TemplateWizard.Iterator {

    private WizardNewWSDLStep nameAndLocationStep = null;
    private Logger mLogger = Logger.getLogger(WSDLWizardIterator.class.getName());
    private int panelLength = 7;
    private TemplateWizard mWiz;
    private WSDLWizardContextImpl context;
    private ChangeSupport changeSupport = new ChangeSupport(this);
    private int stepNo = 0;
    private ArrayList<WSDLWizardBindingConfigurationWrapperStep> panels;
    private String[] steps;
    private ArrayList<String> virtualSteps = null;

    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        //Copy contents of temp model to a new file.
        //find the dataobject for the new file and return it.
        final FileObject dir = Templates.getTargetFolder(wiz);
        final String encoding = (String) wiz.getProperty(WsdlPanel.ENCODING);
        final String name = Templates.getTargetName(wiz);
        FileSystem filesystem = dir.getFileSystem();
        final FileObject[] fileObject = new FileObject[1];
        context.getWSDLModel().startTransaction();
        try {
            if (context.getWSDLExtensionIterator() != null && !context.getWSDLExtensionIterator().commit()) {
                context.getWSDLModel().rollbackTransaction();
            }                   
        } finally {
            if (context.getWSDLModel().isIntransaction()) {
                context.getWSDLModel().endTransaction();
            }
        }

        FileSystem.AtomicAction fsAction = new FileSystem.AtomicAction() {

            public void run() throws IOException {

                FileObject fo = dir.createData(name, "wsdl"); //NOI18N

                FileLock lock = null;
                try {
                    lock = fo.lock();
                    OutputStream out = fo.getOutputStream(lock);
                    out = new BufferedOutputStream(out);
                    Writer writer = new OutputStreamWriter(out, encoding);
                    WSDLModel tempModel = (WSDLModel) mWiz.getProperty(WizardAbstractConfigurationStep.TEMP_WSDLMODEL);

                    DefaultProjectCatalogSupport catalogSupport = DefaultProjectCatalogSupport.getInstance(fo);

                    if (tempModel != null) {
                        postProcessImports(tempModel, fo, catalogSupport);

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

    public void initialize(TemplateWizard wiz) {
        //mLogger.info("initialize...");
        mWiz = wiz;
        
        // set the flag if we show just the concrete configurations only
        DataObject dataObj = mWiz.getTemplate();
        if (dataObj != null) {
            FileObject fileObj = dataObj.getPrimaryFile();
            if (fileObj != null) {
                Object flag = fileObj.getAttribute("bindingConcreteConfiguration");
                if (flag != null) {
                    Boolean boolVal = Boolean.valueOf(flag.toString());
                    if (boolVal.booleanValue()) {
                        mWiz.putProperty("bindingConcreteConfiguration", true);
                    }
                }
            }
        }

        Project project = Templates.getProject(wiz);
        //Project can be null when dynamically invoked.
        if ( project == null) {
            //When dynamically invoked caller will be setting the PROJECT_INSTANCE
            //property.
            project = (Project) wiz.getProperty(BindingUtils.PROJECT_INSTANCE );
        }
        createPanels(project, wiz);

        JComponent jc = (JComponent) nameAndLocationStep.getComponent();
        jc.putClientProperty("WizardPanel_contentData", getSteps());
        jc.putClientProperty("WizardPanel_contentSelectedIndex", 0);

        String encoding = EncodingUtil.getProjectEncoding(project.getProjectDirectory());
        if (encoding == null) {
            encoding = "UTF8";
        }
        wiz.putProperty(WsdlPanel.ENCODING, encoding);
    }

    String[] getSteps() {
        return steps;
    }

    public Integer getCurrentStepIndex() {
        return stepNo;
    }

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
        FileObject projectDirectory = project.getProjectDirectory();
        //IZ 150431, after delete the project fileobject returns invalid state.
        //So DataFolder.find(projectDirectory) fails with FileStateInvalidException
        //Workaround is to check if fileobjects are equal or not.
        try {
            if (wizard.getTargetFolder().getPrimaryFile().equals(projectDirectory)) {
                wizard.setTargetFolder(folder);
            }
        } catch (IOException ioe) {
            wizard.setTargetFolder(folder);
        }
        SourceGroup[] sourceGroups = roots.toArray(new SourceGroup[roots.size()]);
        context = new WSDLWizardContextImpl(this, mWiz);
        //Support Virtual Steps:
        if ( virtualSteps != null) {
            context.addStep(virtualSteps);
        }
        context.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("STEPS_CHANGED")) {
                    Component component = current().getComponent();
                    if (component instanceof JComponent) {
                        JComponent jc = (JComponent) component;
                        jc.putClientProperty("WizardPanel_contentData", context.getSteps());
                        jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(stepNo));
                        changeSupport.fireChange();
                    }
                }
            }
        });

        WsdlPanel folderPanel = new WsdlPanel(context, project);
        // creates simple wizard panel with bottom panel
        nameAndLocationStep =
                new WizardNewWSDLStep(Templates.createSimpleTargetChooser(project, sourceGroups, folderPanel), folderPanel);

        JComponent c = (JComponent) nameAndLocationStep.getComponent();
        Object prop = wizard.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        beforeSteps = Utilities.createSteps(beforeSteps);
        steps = new String[beforeSteps.length + 1];
        for (int i = 0; i < beforeSteps.length; i++) {
            steps[i] = beforeSteps[i];
        }
        steps[steps.length - 1] = c.getName();
        context.setInitialSteps(steps);
        // the bottom panel should listen to changes on file name text field
        ((WsdlPanel) folderPanel).setNameTF(findFileNameField(c, Templates.getTargetName(wizard)));

        //WSDLWizardConstants abstractWSDLStep = new WSDLWizardConstants(context);
        panels = new ArrayList<WSDLWizardBindingConfigurationWrapperStep>();

        //panels.add(abstractWSDLStep);
        for (int i = 0; i < panelLength; i++) {
            panels.add(new WSDLWizardBindingConfigurationWrapperStep(context));
        }



        return panels.toArray(new WizardDescriptor.Panel[panels.size()]);
    }

    public void uninitialize(TemplateWizard wiz) {
        //mLogger.info("uninitialize...");
        if (context != null && context.getWSDLExtensionIterator() != null) {
            context.getWSDLExtensionIterator().cleanup();
        }
        panels = null;
        wiz.putProperty(WSDLWizardConstants.TEMP_WSDLMODEL, null);

        File file = (File) wiz.getProperty(WSDLWizardConstants.TEMP_WSDLFILE);
        wiz.putProperty(WSDLWizardConstants.TEMP_WSDLFILE, null);

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

    public Panel<WizardDescriptor> current() {
        if (stepNo == 0) {
            return nameAndLocationStep;
        }

        WSDLWizardBindingConfigurationWrapperStep panel = panels.get(stepNo);
        panel.setWizardDescriptorPanel(context.getWSDLExtensionIterator().current());
        return panels.get(stepNo);
    }
    /** 
     * Append Step name to current panel. The implementation of the added step is 
     * reponsibility of the class extending this.
     * @param stepName Step name to append.
     */
    protected void appendStep(String stepName) {
        if ( virtualSteps == null) {
            virtualSteps = new ArrayList();
        }
        virtualSteps.add(stepName);
        
    }

    public String name() {
        //mLogger.info("name...");
        return "New WSDL Doucment";
    }

    public boolean hasNext() {
        //mLogger.info("hasNext...");
        return context.hasNext();
    }

    public boolean hasPrevious() {
        //mLogger.info("hasPrevious...");
        return stepNo > 0;
    //return context.hasPrevious();
    }

    public void nextPanel() {
        //mLogger.info("nextPanel...");
        if (stepNo >= 0) {
            context.getWSDLExtensionIterator().nextPanel();
        }
        stepNo++;


    }

    public void previousPanel() {
        //mLogger.info("previousPanel...");
        if (stepNo > 0) {
            context.getWSDLExtensionIterator().previousPanel();
        }
        stepNo--;
    }

    public void addChangeListener(ChangeListener cl) {
        //mLogger.info("addChangeListener...");
        changeSupport.addChangeListener(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        //mLogger.info("removeChangeListener...");
        changeSupport.removeChangeListener(cl);
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

    private void postProcessImports(WSDLModel model, FileObject fobj, DefaultProjectCatalogSupport catalogSupport) {
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
                mLogger.log(Level.SEVERE, "Not able to convert " + schemaFileObj.getName() + "to a valid URI", e);
            }
            String location = getRelativePathOfSchema(fobj, uri, catalogSupport);
            imp.setSchemaLocation(location);
        }
    }

    private String getRelativePathOfSchema(FileObject fo, URI schemaURI, DefaultProjectCatalogSupport catalogSupport) {
        File f = FileUtil.toFile(fo);
        FileObject schemaFO = FileUtil.toFileObject(new File(schemaURI));

        String relativePath = null;
        Project wsdlProject = FileOwnerQuery.getOwner(fo);
        if (wsdlProject != null) {
            //check if its a remote reference and is in catalog.
            CatalogHelper helper = new CatalogHelper(wsdlProject);
            relativePath = helper.getReferencePath(schemaFO, "xsd");
            if (relativePath != null) {
                return relativePath;
            }
        }
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
}
