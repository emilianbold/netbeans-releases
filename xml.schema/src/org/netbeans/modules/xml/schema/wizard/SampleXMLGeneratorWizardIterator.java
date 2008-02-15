/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.xml.schema.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.wizard.AbstractPanel;
import org.netbeans.modules.xml.wizard.DocumentModel;
import org.netbeans.modules.xml.wizard.Util;
import org.netbeans.modules.xml.wizard.XMLContentPanel;
import org.netbeans.modules.xml.wizard.XMLGeneratorVisitor;
import org.netbeans.modules.xml.xam.ui.ProjectConstants;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

public final class SampleXMLGeneratorWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private Project srcProject;
    private DataFolder df;
    private FileObject schemaFileObject;
    private DocumentModel model;
    private XMLContentPanel xmlPanel;
    private static String PREFIX = "ns0";
    private static final String XML_EXT = "xml";  
    
    public SampleXMLGeneratorWizardIterator(SchemaDataObject dobj) {
         schemaFileObject = dobj.getPrimaryFile();
         this.srcProject = FileOwnerQuery.getOwner(schemaFileObject);
    }

    public void show() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        initialize(wizardDescriptor);
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(SampleXMLGeneratorWizardIterator.class,"TITLE_XML_WIZARD"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        
        if (!cancelled) {
            try {
                instantiate();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
               
        xmlPanel = new XMLContentPanel(true);
        
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
               //Templates.createSimpleTargetChooser(srcProject, folders)
                new AbstractPanel.WizardStep(xmlPanel)
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    public Set instantiate() throws IOException {
        final String extension = XML_EXT;
        
        String targetName = schemaFileObject.getName();
        if (targetName == null || "null".equals(targetName)) {                  // NOI18N
            targetName = "XMLDocument";                                         // NOI18N
        }
        
        final FileObject targetFolder = schemaFileObject.getParent();
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
                String encoding = EncodingUtil.getProjectEncoding(targetFolder);
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
                    
                    
                    String namespace = model.getNamespace();
                        
                    if(prefix == null || "".equals(prefix)){
                        writer.write("<" + root + "  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n");
                    } else{
                        writer.write("<" +prefix +":" + root + "  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n"); 
                    }
                                         
                   if(prefix == null || "".equals(prefix) ){
                       writer.write("   xmlns='" + namespace + "'\n");
                   }else {
                       writer.write("   xmlns:" + prefix + "='" + namespace + "'\n" );
                   }                    
                  
                    writer.write("   xsi:schemaLocation='" + namespace + " " + schemaFileObject.getNameExt()+ "'>\n");
                    generateXMLBody(model, root, writer);
                    
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

    public void initialize(WizardDescriptor wiz) {
        index=0;
        wizard = wiz;
        URL targetFolderURL = null;
        try {
            FileObject folder = schemaFileObject.getParent();
            targetFolderURL = folder.getURL();
            //#25604 workaround
            if (targetFolderURL.toExternalForm().endsWith("/") == false) {
                targetFolderURL = new URL(targetFolderURL.toExternalForm() + "/");
            }
        } catch (IOException ignore) {
        }
        
        model = new DocumentModel(targetFolderURL);
        File file = FileUtil.toFile(schemaFileObject);
        String uri = file.getPath();
        if (uri != null) {
               try {
                    // escape the non-ASCII characters
                    uri = new URI(uri).toASCIIString();
                } catch (URISyntaxException e) {
                  // the specified uri is not valid, it is too late to fix it now
                }
             }
        model.setPrimarySchema(uri);
        
        xmlPanel.setObject(model);
        model.setPrefix(PREFIX);
        String ns = Util.getNamespace(schemaFileObject);
        model.setNamespace(ns);   
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
        wizard=null;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
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

      
    private void generateXMLBody(DocumentModel model, String root, Writer writer){
        XMLGeneratorVisitor visitor = new XMLGeneratorVisitor(model.getPrimarySchema(), model.getXMLContentAttributes(), writer);
        visitor.generateXML(root);
       
    }

    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
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

    }
