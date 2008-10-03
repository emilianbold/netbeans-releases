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
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.lib.GuiUtil;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.text.TextEditorSupport;
import org.netbeans.modules.xml.wizard.AbstractPanel;
import org.netbeans.modules.xml.wizard.DocumentModel;
import org.netbeans.modules.xml.wizard.SchemaParser;
import org.netbeans.modules.xml.wizard.XMLContentPanel;
import org.netbeans.modules.xml.wizard.XMLGeneratorVisitor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public final class SampleXMLGeneratorWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private Project srcProject;
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
        dialog.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(SampleXMLGeneratorWizardIterator.class,"TITLE_XML_WIZARD"));
        dialog.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(SampleXMLGeneratorWizardIterator.class,"TITLE_XML_WIZARD"));
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
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
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
        int i = 1;        
        while (targetFolder.getFileObject(uniqueTargetName, extension) != null) {
            uniqueTargetName = targetName + i;
            i++;
        }

        final String name = uniqueTargetName;
        
           // in atomic action create data object and return it
        
        FileSystem filesystem = targetFolder.getFileSystem();        
        final FileObject[] fileObject = new FileObject[1];
        String encoding = EncodingUtil.getProjectEncoding(targetFolder);
        if (!EncodingUtil.isValidEncoding(encoding)) {
            encoding = "UTF-8"; //NOI18N
        }
        String nameExt = name + "." + extension;
        FileSystem.AtomicAction fsAction = new FileSystem.AtomicAction() {
            public void run() throws IOException {
               
                FileObject fo = targetFolder.createData(name, extension);
                fileObject[0] = fo;
            }
        };
         
        filesystem.runAtomicAction(fsAction);
        StringBuffer sb = new StringBuffer();
        //write the comment
        writeXMLComment(sb, nameExt, encoding);
        //write the body
        writeXMLFile(sb);
                
        FileLock lock = null;
        try {
            lock = fileObject[0].lock();
            OutputStream out = fileObject[0].getOutputStream(lock);
            out = new BufferedOutputStream(out, 999);
            Writer writer = new OutputStreamWriter(out, encoding); 
            writer.write(sb.toString());
            writer.flush();
            writer.close();
            lock.releaseLock();
            lock = null;

        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
        // perform default action and return
               
        Set set = new HashSet(1);                
        DataObject createdObject = DataObject.find(fileObject[0]);        
        GuiUtil.performDefaultAction(createdObject);
        set.add(createdObject); 
        
        formatXML(fileObject[0]);
        return set;
    }

    public void initialize(WizardDescriptor wiz) {
        index=0;
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
        String ns = SchemaParser.getNamespace(schemaFileObject);
        model.setNamespace(ns);   
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return NbBundle.getMessage(SchemaWizardIterator.class, "TITLE_x_of_y",
            Integer.valueOf(index + 1), Integer.valueOf(panels.length));
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

      
    private void generateXMLBody(DocumentModel model, String root, StringBuffer writer){
        XMLGeneratorVisitor visitor = new XMLGeneratorVisitor(model.getPrimarySchema(), model.getXMLContentAttributes(), writer);
        visitor.generateXML(root);
       
    }
    
    private void modifyRootElementAttrs(FileObject fobj) {
        try {
            File file = new File(fobj.getPath());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            
            NamedNodeMap rootAttributes = doc.getDocumentElement().getAttributes();
            Map<String, String> nsAttrs = model.getXMLContentAttributes().getNamespaceToPrefixMap();
            
            if(nsAttrs == null || nsAttrs.size() == 0)
                return;
            for(String ns:nsAttrs.keySet()) {
                Attr galaxy = doc.createAttribute("xmlns:" + nsAttrs.get(ns));
                galaxy.setValue(ns);
                rootAttributes.setNamedItem(galaxy);
            }

            //write to oputput file
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            Result result = new StreamResult(file);
            transformer.transform(source, result);
 
       } catch(Exception e) {
          
       }
    }
    
    private void writeXMLComment(StringBuffer writer, String filename, String encoding) throws IOException {
        writer.append("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n");  // NOI18N
        writer.append("\n");                                         // NOI18N
        // comment
        Date now = new Date();
        String currentDate = DateFormat.getDateInstance(DateFormat.LONG).format(now);
        String currentTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(now);
        String userName = System.getProperty("user.name");
        writer.append("<!--\n"); // NOI18N
        writer.append("    Document   : " + filename + "\n"); // NOI18N
        writer.append("    Created on : " + currentDate + ", " + currentTime + "\n"); // NOI18N
        writer.append("    Author     : " + userName + "\n"); // NOI18N
        writer.append("    Description:\n"); // NOI18N
        writer.append("        Purpose of the document follows.\n"); // NOI18N
        writer.append("-->\n"); // NOI18N
        writer.append("\n");
    }
    
    
    
    private void formatXML(FileObject fobj){
        try {
            DataObject dobj = DataObject.find(fobj);
            EditorCookie ec = dobj.getCookie(EditorCookie.class);
            if (ec == null) {
                return;
            }
            BaseDocument doc = (BaseDocument) ec.getDocument();
            org.netbeans.modules.xml.text.api.XMLFormatUtil.reformat(doc, 0, doc.getLength());
            EditCookie cookie = dobj.getCookie(EditCookie.class);
            if (cookie instanceof TextEditorSupport) {
                if (cookie != null) {
                    ((TextEditorSupport) cookie).saveDocument();
                } 
            }

        } catch (Exception e) {
            //if exception , then the file will be informatted
        }
                 
        
    }
    
    private void modifyRootElementAttrs(StringBuffer xmlBuffer) {
         Map<String, String> nsAttrs = model.getXMLContentAttributes().getNamespaceToPrefixMap();
           
         if (nsAttrs == null || nsAttrs.size() == 0) {
             return;
         }
         int firstOccur = xmlBuffer.indexOf("xmlns");
         int insertLoc = xmlBuffer.indexOf("xmlns", firstOccur + 1);

         StringBuffer sb = new StringBuffer();
         for (String ns : nsAttrs.keySet()) {
             String xmlnsString = "xmlns:" + nsAttrs.get(ns) + "='" + ns + "'";
             if (xmlBuffer.indexOf(xmlnsString) == -1) {
                 xmlBuffer.insert(insertLoc, xmlnsString + "\n   ");
             }
         }
         xmlBuffer.insert(insertLoc, sb.toString());            
            
     }

     
    private void writeXMLFile(StringBuffer writer) throws IOException {
        String root = model.getRoot();
        if (root == null) {
            root = "root";
        }
        String prefix = model.getPrefix();
        String namespace = model.getNamespace();
        if (prefix == null || "".equals(prefix)) {
            writer.append("<" + root + "  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n");
        } else {
            writer.append("<" + prefix + ":" + root + "  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n");
        }

        if (prefix == null || "".equals(prefix)) {
            writer.append("   xmlns='" + namespace + "'\n");
        } else {
            writer.append("   xmlns:" + prefix + "='" + namespace + "'\n");
        }
        writer.append("   xsi:schemaLocation='" + namespace + " " + schemaFileObject.getNameExt() + "'>\n");
        generateXMLBody(model, root, writer);
        modifyRootElementAttrs(writer);
        if (prefix == null || "".equals(prefix)) {
            writer.append("\n");                                         // NOI18N
            writer.append("</" + root + ">\n");                          // NOI18N
        } else {
            writer.append("\n");                                         // NOI18N
            writer.append("</" + prefix + ":" + root + ">\n");
        }

      //  writer.flush();
     //   writer.close();

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
