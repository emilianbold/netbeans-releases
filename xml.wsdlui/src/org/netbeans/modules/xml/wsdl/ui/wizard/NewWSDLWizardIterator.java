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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public final class NewWSDLWizardIterator implements TemplateWizard.Iterator {
    
    private int index;
    
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    
    private WizardDescriptor.Panel folderPanel;
    private transient SourceGroup[] sourceGroups;
    private transient DefaultProjectCatalogSupport catalogSupport;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private Panel[] createPanels(Project project) {
        Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        folderPanel=new WsdlPanel(project,sourceGroups);
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel = new WizardNewWSDLStep(Templates.createSimpleTargetChooser(project,sourceGroups,folderPanel));
        JComponent c = (JComponent)firstPanel.getComponent();
        // the bottom panel should listen to changes on file name text field
        ((WsdlPanel)folderPanel).setNameTF(findFileNameField(c, Templates.getTargetName(wizard)));
        
        WizardDescriptor.Panel secondPanel = new WizardPortTypeConfigurationStep(project);
        WizardDescriptor.Panel thirdPanel = new WizardBindingConfigurationStep();
                        
        return new WizardDescriptor.Panel[] {
            firstPanel,
            secondPanel,
            thirdPanel
        };
    }
    
    public Set instantiate(TemplateWizard wiz) throws IOException {
//      Here is the default plain behavior. Simply takes the selected
        // template (you need to have included the standard second panel
        // in createPanels(), or at least set the properties targetName and
        // targetFolder correctly), instantiates it in the provided
        // position, and returns the result.
        // More advanced wizards can create multiple objects from template
        // (return them all in the result of this method), populate file
        // contents on the fly, etc.
        
        FileObject dir = Templates.getTargetFolder( wiz );
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wiz );
        WsdlPanel panel = (WsdlPanel)folderPanel;
        boolean importSchemas=false;
        if (panel.isImport() && panel.getSchemas().length>0) {
            importSchemas=true;
//            FileObject templateParent = template.getParent();
//            template = templateParent.getFileObject("WSDL_import","wsdl"); //NOI18N
        }
        
        DataObject dTemplate = DataObject.find( template );
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wiz )  );
        //create new data object
        if (dobj!=null) {
            
            catalogSupport = DefaultProjectCatalogSupport.getInstance(dobj.getPrimaryFile());
            WSDLModel model = null;
           
            //is there a temp wsdl model. it will be if wizard screen is 2 or 3
            WSDLModel tempModel = (WSDLModel) wiz.getProperty(WizardPortTypeConfigurationStep.TEMP_WSDLMODEL);
            wiz.putProperty(WizardPortTypeConfigurationStep.TEMP_WSDLMODEL, null);
            if(tempModel != null) {
                try {
                    postProcessImports(tempModel, dobj);
                } catch(Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                
            	FileObject tmpWsdlFileObject = (FileObject) tempModel.getModelSource().getLookup().lookup(FileObject.class);
                if(tmpWsdlFileObject != null) {
                	File wsdlFile = FileUtil.toFile(dobj.getPrimaryFile());
                	long lastMod = wsdlFile.lastModified();
                	
                    DataObject wsdlDataObj = DataObject.find(tmpWsdlFileObject);
                    EditorCookie editorCookie = (EditorCookie)wsdlDataObj.getCookie(EditorCookie.class);
                    editorCookie.openDocument();
                    javax.swing.text.Document doc = editorCookie.getDocument();

                    //write from tempModel to actual file
                    FileOutputStream stream = new FileOutputStream(wsdlFile);
                    //set the charset to utf-8
                    OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8");
                    
                    try {
                        writer.write(doc.getText(0, doc.getLength()));
                    } catch (BadLocationException e) {
                        ErrorManager.getDefault().notify(e);
                    } finally {
                        writer.close();
                        stream.close();
                    }
                    wsdlFile.setLastModified(lastMod);
                    
                    //get the mode for newly created wsdl file
                    ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(dobj.getPrimaryFile(), 
                    dobj.getPrimaryFile().canWrite());
                    
                    model  = WSDLModelFactory.getDefault().getModel(modelSource);
                    

                }
            } else {
                FileObject wsdlFile = dobj.getPrimaryFile();
                ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(wsdlFile, 
                    wsdlFile.canWrite());
                model  = WSDLModelFactory.getDefault().getModel(modelSource);
                
                String definitionName = Templates.getTargetName(wizard);
                String targetNamespace = panel.getNS();
                model.startTransaction();
                Definitions def = model.getDefinitions();
                def.setName(definitionName);
                def.setTargetNamespace(targetNamespace);
                ((AbstractDocumentComponent) def).addPrefix("tns", targetNamespace);
                if (def.getTypes() != null) {
                    def.setTypes(model.getFactory().createTypes());
                }
                
                model.endTransaction();
            }
            
            if (model != null && importSchemas) {
                addSchemaImport(model, dobj);
            }
            
            SaveCookie save = (SaveCookie)dobj.getCookie(SaveCookie.class);
            if (save!=null) save.save();
        }
        
        return Collections.singleton(dobj);
    }

    private void postProcessImports(WSDLModel model, DataObject dobj) throws Exception {
        Types types = model.getDefinitions().getTypes();
        if(types != null) {
            Collection<WSDLSchema> schemas = types.getExtensibilityElements(WSDLSchema.class);
            
            if(schemas != null) {
                
                if(schemas.iterator().hasNext()) {
	                WSDLSchema wsdlSchema = schemas.iterator().next();
	                SchemaModel sModel = wsdlSchema.getSchemaModel();
	                if(sModel != null) {
		                Schema schema = sModel.getSchema();
		                if(schema != null && schema.getImports() != null) {
		                	model.startTransaction();
		                	Iterator<Import> it = schema.getImports().iterator();
			                while(it.hasNext()) {
			                    Import imp = it.next();
			                    postProcessImport(imp, sModel, dobj);
			                }
			                model.endTransaction();
		                }
		                
	                }
                }
            }
        }
    }
    
    private void postProcessImport(Import imp, SchemaModel model, DataObject dobj) throws Exception {
        String namespace = imp.getNamespace();
        Collection<Schema> schemas = model.findSchemas(namespace);
        Iterator<Schema> it = schemas.iterator();
        while(it.hasNext()) {
            Schema schema = it.next();
            SchemaModel sModel = schema.getModel();
            FileObject schemaFileObj = (FileObject) sModel.getModelSource().getLookup().lookup(FileObject.class);
            String location = getRelativePathOfSchema(dobj, schemaFileObj.getURL().toString());
            imp.setSchemaLocation(location);
        }
    }
    
    private void addSchemaImport(WSDLModel model, DataObject dobj) {
        model.startTransaction();
        WsdlPanel panel = (WsdlPanel)folderPanel;
        String targetNamespace = panel.getNS();

        WsdlUIPanel.SchemaInfo[] infos = panel.getSchemas();
        Schema schema = null;
        WSDLSchema wsdlSchema = null;

        for (int i=0;i<infos.length;i++) {
            String ns = infos[i].getNamespace();
            if (ns.length()==0) ns = targetNamespace;//"urn:WS/types"+String.valueOf(i+1); //NOI18N

            String prefix = "ns" + String.valueOf(i+1);


            String relativePath = null;
            try{
                relativePath = getRelativePathOfSchema(dobj, infos[i].getSchemaName());
            }catch(URISyntaxException e){
                relativePath= infos[i].getSchemaName();
            }

            Definitions def = model.getDefinitions();
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



            if(!isSchemaImportExists(relativePath, ns, schema)) {
                schema.addPrefix(prefix, ns);
                ((AbstractDocumentComponent) def).addPrefix(prefix, ns);

                org.netbeans.modules.xml.schema.model.Import schemaImport =
                    schema.getModel().getFactory().createImport();
                schemaImport.setNamespace(ns);       
                schemaImport.setSchemaLocation(relativePath);

                schema.addExternalReference(schemaImport);
            }
        }

        model.endTransaction();
    }
    
    
    private boolean isSchemaImportExists(String schemaLocation, String namespace, Schema schema) {
        boolean isImportExist = false;
        Collection<Import> imports = schema.getImports();
        Iterator<Import> it = imports.iterator();
        while(it.hasNext()) {
            Import imp = it.next();
            
            String sLoc = imp.getSchemaLocation();
            String ns = imp.getNamespace();
            
            if(ns != null && ns.equals(namespace) && sLoc != null && sLoc.equals(schemaLocation)) {
                isImportExist = true;
                break;
            }
        }
        
        return isImportExist;
    }
    
    private String getRelativePathOfSchema(DataObject wsdlDO, String schemaURL) throws URISyntaxException{
        FileObject fo = wsdlDO.getPrimaryFile();
        File f = FileUtil.toFile(fo);
        FileObject schemaFO = FileUtil.toFileObject(new File(new URI(schemaURL)));
        
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
            relativePath = org.netbeans.modules.xml.retriever.catalog.Utilities.relativize(f.toURI(),new URI(schemaURL));
        }
        return relativePath;
    }
    
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels(project);
        
        // Creating steps.
        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
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
    }



    public void uninitialize(TemplateWizard wiz) {
        
        File tempWSDLFile = (File) wizard.getProperty(WizardPortTypeConfigurationStep.TEMP_WSDLFILE);
        try {
        	if(tempWSDLFile != null) {
    	        
        		FileObject tempFile = FileUtil.toFileObject(tempWSDLFile.getCanonicalFile());
        		if(tempFile != null) {
        			DataObject dObj = DataObject.find(tempFile);
        			if(dObj != null) {
        				dObj.delete();
        			}
        			
        		}
	        }
        } catch(Exception ex) {
        	ErrorManager.getDefault().notify(ex);
        }
        
        this.wizard = null;
        panels = null;
        
    }
    
    
    public Set instantiate() throws IOException {
        return Collections.EMPTY_SET;
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
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
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
     */
    
    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        
        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }
        
        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    //from schema wizard
    private JTextField findFileNameField(Component panel, String text) {
        Collection<Component> allComponents = new ArrayList<Component>();
        getAllComponents(new Component[] {panel}, allComponents);
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
    public static void getAllComponents( Component[] components, Collection<Component> allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }

    
}
