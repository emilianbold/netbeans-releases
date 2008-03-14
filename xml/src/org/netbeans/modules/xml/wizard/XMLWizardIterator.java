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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.wizard;

import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Set;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.api.EncodingUtil;

import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.modules.xml.retriever.RetrieverEngine;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor.Panel;
import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;


/**
 * Controls new XML Docuemnt wizard. It is kind of dynamic wizard with 
 * multiple way of diferent length.
 *
 * @author  Petr Kuzel
 */
public class XMLWizardIterator implements TemplateWizard.Iterator {
    /** Serial Version UID */
    private static final long serialVersionUID = 5070430920636117204L;
    

    private static final String XML_EXT = "xml";                                // NOI18N
    
    // parent wizard
    
    private transient TemplateWizard templateWizard;
    
    // model collecting our data
    
    private transient DocumentModel model;

    // panels

    private transient int current;
    
    private static final int TARGET_PANEL = 0;
    private transient WizardDescriptor.Panel targetPanel;
    
    private static final int DOCUMENT_PANEL = 1;
    private transient DocumentPanel documentPanel;
    
    private static final int CONSTRAINT_PANEL = 2;
    private transient SchemaPanel schemaPanel;
    private transient DTDPanel dtdPanel;
    
    private static final int CONTENT_PANEL = 3;
    private transient XMLContentPanel xmlPanel;
    
    /** Singleton instance of JavaWizardIterator, should it be ever needed.
     */
    private static Reference<XMLWizardIterator> instance;
    
    private transient Map listenersMap = new HashMap(2);
    private transient String[] beforeSteps;
    private transient Object targetSteps;
    

    /** Returns JavaWizardIterator singleton instance. This method is used
     * for constructing the instance from filesystem.attributes.
     */
    public static synchronized XMLWizardIterator singleton() {
        XMLWizardIterator it = instance == null ? null : instance.get();
        if (it == null) {
            it = new XMLWizardIterator();
            instance = new WeakReference<XMLWizardIterator>(it);
        }
            return it;
    }

    public void initialize(TemplateWizard templateWizard) {
        this.templateWizard = templateWizard;
        current = TARGET_PANEL;
        URL targetFolderURL = null;
        try {
            DataFolder folder = templateWizard.getTargetFolder();
            targetFolderURL = folder.getPrimaryFile().getURL();
            //#25604 workaround
            if (targetFolderURL.toExternalForm().endsWith("/") == false) {
                targetFolderURL = new URL(targetFolderURL.toExternalForm() + "/");
            }
        } catch (IOException ignore) {
        }
        model = new DocumentModel(targetFolderURL);
        Object prop = templateWizard.getProperty ("WizardPanel_contentData"); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
    }

    public void uninitialize(TemplateWizard templateWizard) {
        if (targetPanel!=null) {
            ((JComponent)targetPanel.getComponent()).putClientProperty("WizardPanel_contentData", targetSteps);
            targetPanel = null;
        }
        current = -1;
        model = null;
        templateWizard = null;
        schemaPanel = null;
        dtdPanel = null;
        documentPanel = null;
        xmlPanel = null;
    }
    
    public Set instantiate(TemplateWizard templateWizard) throws IOException {
        final DataFolder folder = templateWizard.getTargetFolder();
        
        final File pobj = FileUtil.toFile(folder.getPrimaryFile());
               
        final String extension = XML_EXT;

        // #22812 we do not control validity constrains of target panel
        // assure uniquess to "<default>" name
        
        String targetName = templateWizard.getTargetName();
        if (targetName == null || "null".equals(targetName)) {                  // NOI18N
            targetName = "XMLDocument";                                         // NOI18N
        }
        final FileObject targetFolder = folder.getPrimaryFile();
        String uniqueTargetName = targetName;
        int i = 2;
        
        while (targetFolder.getFileObject(uniqueTargetName, extension) != null) {
            uniqueTargetName = targetName + i;
            i++;
        }

        final String name = uniqueTargetName;

        // in atomic action create data object and return it
        
        FileSystem filesystem = targetFolder.getFileSystem();        
        final FileObject[] fileObject = new FileObject[1];
        FileSystem.AtomicAction fsAction = new FileSystem.AtomicAction() {
            public void run() throws IOException {
                //use the project's encoding if there is one
                String encoding = EncodingUtil.getProjectEncoding(folder.getPrimaryFile());
                if(!EncodingUtil.isValidEncoding(encoding))
                    encoding = "UTF-8"; //NOI18N
                FileObject fo = targetFolder.createData(name, extension);
                FileLock lock = null;
                try {
                    lock = fo.lock();
                    OutputStream out = fo.getOutputStream(lock);
                    out = new BufferedOutputStream(out, 999);
                    Writer writer = new OutputStreamWriter(out, encoding);        // NOI18N

                    String root = model.getRoot();
                    if (root == null) root = "root";
                    String prefix = model.getPrefix();
                    
                    // generate file content
                    // header
                    writer.write("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n");  // NOI18N
                    writer.write("\n");                                         // NOI18N
                    // comment
                    String nameExt = name + "." + extension; // NOI18N
                    Date now = new Date();
                    String currentDate = DateFormat.getDateInstance (DateFormat.LONG).format (now);
                    String currentTime = DateFormat.getTimeInstance (DateFormat.SHORT).format (now);
                    String userName = System.getProperty ("user.name");
                    writer.write ("<!--\n"); // NOI18N
                    writer.write ("    Document   : " + nameExt + "\n"); // NOI18N
                    writer.write ("    Created on : " + currentDate + ", " + currentTime + "\n"); // NOI18N
                    writer.write ("    Author     : " + userName + "\n"); // NOI18N
                    writer.write ("    Description:\n"); // NOI18N
                    writer.write ("        Purpose of the document follows.\n"); // NOI18N
                    writer.write ("-->\n"); // NOI18N
                    writer.write ("\n");                                         // NOI18N
                    
                    if (model.getType() == model.DTD) {
                        if (model.getPublicID() == null) {
                            writer.write("<!DOCTYPE " + root + " SYSTEM '" + model.getSystemID() + "'>\n");                                 // NOI18N
                        } else {
                            writer.write("<!DOCTYPE " + root + " PUBLIC '" + model.getPublicID() + "' '" + model.getSystemID() + "'>\n");   // NOI18N
                        }
                        writer.write("<" + root + ">\n");                                                                                   // NOI18N
                    } else if (model.getType() == model.SCHEMA) {
                        String namespace = model.getNamespace();
                        List nodes = model.getSchemaNodes();
                        
                            if(prefix == null || "".equals(prefix)){
                                writer.write("<" + root + "  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n");
                            } else{
                                writer.write("<" +prefix +":" + root + "  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n"); 
                            }
                        
                            //if (namespace == null || "".equals(namespace)) {
                              //  writer.write("  xsi:noNamespaceSchemaLocation='" + model.getSystemID() + "'>\n");
                            //}
                            
                            if(nodes != null){
                                for(int i=0;i < nodes.size(); i++ ){
                                    SchemaObject erdn = (SchemaObject)nodes.get(i);
                                    if(erdn.getPrefix() == null || "".equals(erdn.getPrefix()) ){
                                        writer.write("   xmlns='" + erdn.getNamespace() + "'\n");
                                    }else {
                                        writer.write("   xmlns:" + erdn.getPrefix() + "='" + erdn.getNamespace() + "'\n" );
                                    }
                                }
                                for(int i=0; i<nodes.size();i++){
                                    SchemaObject erdn = (SchemaObject)nodes.get(i);
                                    String relativePath = null;
                                    if(erdn.toString().startsWith("http"))
                                        relativePath = erdn.toString();
                                    else
                                        relativePath = Util.getRelativePath((new File(erdn.getSchemaFileName())), pobj);
                                    if(i==0) {
                                        if(nodes.size() ==1)
                                            writer.write("   xsi:schemaLocation='" + erdn.getNamespace() + " " + relativePath + "'>\n");
                                        else
                                            writer.write("   xsi:schemaLocation='" + erdn.getNamespace() + " " + relativePath + "\n");
                                    } else if(i == nodes.size() -1 )
                                        writer.write("   " + erdn.getNamespace() + " " + relativePath + "'>\n");
                                    else
                                        writer.write("   " + erdn.getNamespace() + " " + relativePath + "\n");
                                }
                               
                            }
                            
                        generateXMLBody(model, root, writer);
                        
                    } else {
                        writer.write("<" + root + ">\n");                       // NOI18N
                    }
                    
                    if(prefix== null || "".equals(prefix)){
                        writer.write("\n");                                         // NOI18N
                        writer.write("</" + root + ">\n");                          // NOI18N
                    }else{
                        writer.write("\n");                                         // NOI18N
                        writer.write("</" +prefix + ":"+ root + ">\n");
                    }

                    writer.flush();
                    writer.close();
                    
                    // return DataObject
                    lock.releaseLock();
                    lock = null;
                    
                    fileObject[0] = fo;
                    
                } finally {
                    if (lock != null) lock.releaseLock();
                }
            }
        };
        
                
        filesystem.runAtomicAction(fsAction);

        // perform default action and return
        
        Set set = new HashSet(1);                
        DataObject createdObject = DataObject.find(fileObject[0]);        
        Util.performDefaultAction(createdObject);
        set.add(createdObject);      
       
        return set;
    }
    
    
    public WizardDescriptor.Panel current() {
        WizardDescriptor.Panel panel = currentComponent();
        if (panel.getComponent() instanceof JComponent) {
            ((JComponent)panel.getComponent()).putClientProperty(
                "WizardPanel_contentSelectedIndex",                             // NOI18N
                new Integer(current)
            );        
        }
        return panel;
    }
    
    
    private WizardDescriptor.Panel currentComponent() {   
        switch (current) {
            case TARGET_PANEL:
                return getTargetPanel();
            case DOCUMENT_PANEL:
                return getDocumentPanel();
            case CONSTRAINT_PANEL:
                switch (model.getType()) {
                    case DocumentModel.DTD:
                        return getDTDPanel();
                    case DocumentModel.SCHEMA:
                        return getSchemaPanel();
                    default:
                        throw new IllegalStateException();
                }
            case CONTENT_PANEL:
                return getXMLContentPanel();
            default:
                throw new IllegalStateException();
        }
    }
    
    public boolean hasNext() {
        boolean none = model.getType() == model.NONE;
        int length = 0;
        if(model.getType() == model.SCHEMA)
            length = CONTENT_PANEL;
        if(model.getType() == model.NONE)
            length = DOCUMENT_PANEL;
        else if(model.getType() == model.DTD)
            length = CONSTRAINT_PANEL;
       // int length = none ? DOCUMENT_PANEL : CONSTRAINT_PANEL;
        return current < length;
    }
    
    public boolean hasPrevious() {
        return current > TARGET_PANEL;
    }
            
    public String name() {
        return "//TODO";
    }
    
    public void nextPanel() {
        current++;
    }
    
    public void previousPanel() {
        current--;
    }

    // events source ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    //
    // To symplify synchronization use bridge listeners delegating to model
    // events. We do not need to sample listeners in sync block and then fire
    // changes over the sampled listener copies out-of the sync block.
    //
    
    public void removeChangeListener(ChangeListener changeListener) {
        if (changeListener == null) return;        
        synchronized (listenersMap) {            
            Object bridge = listenersMap.remove(changeListener);
            if (bridge == null) return;
            if (model == null) return;
            model.removePropertyChangeListener((PropertyChangeListener) bridge);
        }
    }

    public void addChangeListener(final ChangeListener changeListener) {
        if (changeListener == null) return;
        synchronized (listenersMap) {
            PropertyChangeListener listenerBridge = new PropertyChangeListener() {
                final ChangeEvent EVENT = new ChangeEvent(XMLWizardIterator.this);
                public void propertyChange(PropertyChangeEvent e) {
                    changeListener.stateChanged(EVENT);
                }
            };
            
            if (listenersMap.put(changeListener, listenerBridge) == null) {
                model.addPropertyChangeListener(listenerBridge);
            }
        }
    }
    
    // implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private WizardDescriptor.Panel getDocumentPanel() {
        if (documentPanel == null) {
            documentPanel = new DocumentPanel();
            documentPanel.setObject(model);
            
            String[]  steps = new String[3];
            steps[0] = getTargetPanelName();
            steps[1] = getDocumentPanelName();
            steps[2] = Util.THIS.getString("MSG_unknown");
            String[] newSteps = createSteps(beforeSteps,steps);
            documentPanel.putClientProperty(
                "WizardPanel_contentData",                                      // NOI18N
                newSteps
            );
            
        }
        return new AbstractPanel.WizardStep(documentPanel);
    }

    private WizardDescriptor.Panel getDTDPanel() {
        if (dtdPanel == null) {
            dtdPanel = new DTDPanel();
            dtdPanel.setObject(model);
            
            String[] steps = new String[3];
            steps[0] = getTargetPanelName();
            steps[1] = getDocumentPanelName();
            steps[2] = getDTDPanelName();
            String[] newSteps = createSteps(beforeSteps,steps);
            dtdPanel.putClientProperty(
                "WizardPanel_contentData",                                      // NOI18N
                newSteps
            );
            
        }
        return new AbstractPanel.WizardStep(dtdPanel);
    }

    private WizardDescriptor.Panel getSchemaPanel() {
        if (schemaPanel == null) {
            schemaPanel = new SchemaPanel(templateWizard);
            schemaPanel.setObject(model);
            
            String[] steps = new String[4];
            steps[0] = getTargetPanelName();
            steps[1] = getDocumentPanelName();
            steps[2] = getSchemaPanelName();
            steps[3] = getXMLContentPanelName();
            String[] newSteps = createSteps(beforeSteps,steps);
            schemaPanel.putClientProperty(
                "WizardPanel_contentData",                                      // NOI18N
                newSteps
            );
        }
        return new AbstractPanel.WizardStep(schemaPanel);
    }
    
    private WizardDescriptor.Panel getTargetPanel() {
        if (targetPanel == null) {
            targetPanel = templateWizard.targetChooser();
            // fill component with step hints
            if (targetPanel.getComponent() instanceof JComponent) {
                JComponent panel = (JComponent) targetPanel.getComponent();
                targetSteps = panel.getClientProperty("WizardPanel_contentData");
                String[] steps = new String[3];
                //steps[0] = "Hello";
                steps[0] = getTargetPanelName();
                steps[1] = getDocumentPanelName();
                steps[2] = Util.THIS.getString("MSG_unknown");
                String[] newSteps = createSteps(beforeSteps,steps);
                panel.putClientProperty(
                    "WizardPanel_contentData",                                  // NOI18N
                    newSteps
                );
            }
            
        }
        return targetPanel;
    }
    
    private String getTargetPanelName() {
        Object panel = getTargetPanel().getComponent();
        if (panel instanceof JComponent) {
            return ((JComponent)panel).getName();
        } else {
            return "";  //??? some fallback
        }
    }
    
    private String getDocumentPanelName() {
        return Util.THIS.getString("PROP_doc_panel_name");
    }
        
    private String getDTDPanelName() {
        return Util.THIS.getString("PROP_dtd_panel_name");
    }
    
    private String getSchemaPanelName() {
        return Util.THIS.getString("PROP_schema_panel_name");
    }
    
    private static String[] createSteps(String[] before, String[] panelNames) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panelNames.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panelNames[i - before.length + diff];
            }
        }
        return res;
    }

    private WizardDescriptor.Panel getXMLContentPanel() {
         if (xmlPanel == null) {
            xmlPanel = new XMLContentPanel();
            xmlPanel.setObject(model);
            
            String[] steps = new String[4];
            steps[0] = getTargetPanelName();
            steps[1] = getDocumentPanelName();
            steps[2] = getSchemaPanelName();
            steps[3] = getXMLContentPanelName();
            String[] newSteps = createSteps(beforeSteps,steps);
            xmlPanel.putClientProperty(
                "WizardPanel_contentData",                                      // NOI18N
                newSteps
            );
        }
        return new AbstractPanel.WizardStep(xmlPanel);
    }

    private String getXMLContentPanelName() {
        return Util.THIS.getString("PROP_xml_content_panel_name");
    }
    
    private void generateXMLBody(DocumentModel model, String root, Writer writer){
        String schemaFileName = model.getPrimarySchema();
        if(model.getPrimarySchema().startsWith("http")) {
            schemaFileName = retrieveURLSchema(model.getPrimarySchema());             
        }
         XMLGeneratorVisitor visitor = new XMLGeneratorVisitor(schemaFileName, model.getXMLContentAttributes(), writer);
         visitor.generateXML(root);
    }
    
    private String retrieveURLSchema(String sourceURL)  {
        try {
            Project prj = Templates.getProject(templateWizard); 
            FileObject prjrtfo = prj.getProjectDirectory();
           // File saveFile = new File(selectedSaveRootFolder.getPath() + File.separator + "nbproject" + File.separator + "private" + File.separator+ schemaFileName);
            
            File prjrt = FileUtil.toFile(prjrtfo);
            URI privateCatalogURI = null;
            URI privateCacheURI = null;
            //determine the cache dir
            CacheDirectoryProvider cdp = (CacheDirectoryProvider) prj.getLookup().
                lookup(CacheDirectoryProvider.class);
            String cachestr = Utilities.DEFAULT_PRIVATE_CAHCE_URI_STR;
            try{
                if( (cdp != null) && (cdp.getCacheDirectory() != null) ){
                    URI prjrturi = prjrt.toURI();
                    URI cpduri = FileUtil.toFile(cdp.getCacheDirectory()).toURI();
                    String cachedirstr = Utilities.relativize(prjrturi, cpduri);
                    cachestr = cachedirstr+"/"+Utilities.PRIVATE_CAHCE_URI_STR;
                }
                privateCacheURI = new URI(cachestr);
           }catch(Exception e){
                
           }
           if(privateCacheURI == null)
               return null;
            URI cacheURI = prjrt.toURI().resolve(privateCacheURI);
            File saveFile = new File(cacheURI );
            if(!saveFile.isDirectory())
               saveFile.mkdirs();
            
            RetrieverEngine instance = RetrieverEngine.getRetrieverEngine(saveFile, false);
            RetrieveEntry rent =new RetrieveEntry(null, sourceURL, null, null, DocumentTypesEnum.schema, true);
            instance.addResourceToRetrieve(rent);
            instance.setFileOverwrite(true);
            instance.start();
           
            //find where the file was downloaded, remove the "http:/" from the url
            String returnstr = saveFile.getPath() + sourceURL.substring(6, sourceURL.length());
            return returnstr;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    

}
