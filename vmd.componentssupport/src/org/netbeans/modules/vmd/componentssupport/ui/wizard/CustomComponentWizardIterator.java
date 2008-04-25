/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CustomComponentWizardIterator implements
        WizardDescriptor./* Progress */InstantiatingIterator
{

    private static final String UTF_8       = "UTF-8";                           // NOI18N
    private static final String CODE_NAME_BASE 
                                            = "code-name-base";                  // NOI18N
    private static final String DATA        = "data";                            // NOI18N
    private static final String PROJECT_XML = "nbproject/project.xml";           // NOI18N

    public static final String WIZARD_PANEL_ERROR_MESSAGE 
                                            = "WizardPanel_errorMessage";        // NOI18N
    private static final String LBL_WIZARD_STEPS_COUNT 
                                            = "LBL_WizardStepsCount";            // NOI18N
    
    public static final String CONTENT_DATA = "WizardPanel_contentData";         // NOI18N
    public static final String SELECTED_INDEX 
                                            = "WizardPanel_contentSelectedIndex";// NOI18N

    // steps
    public static final String STEP_BASIC_PARAMS 
                                            = "LBL_BasicProjectParamsStep";      // NOI18N
    private static final String LBL_LIBRARIES 
                                            = "LBL_LibrariesDescStep";
    private static final String LBL_COMPONENT_DESC 
                                            = "LBL_ComponentsDescStep";
    private static final String FINAL_STEP  = "LBL_FinalStep";

    // properties
    public static final String PROJECT_DIR  = "projdir";                         // NOI18N
    public static final String PROJECT_NAME = "projname";                        // NOI18N
    public static final String LAYER_PATH   = "layer";                           // NOI18N
    public static final String CODE_BASE_NAME
                                            = "codeBaseName";                    // NOI18N


    // parameters for project
    private static final String CODE_NAME_PARAM 
                                            = "_CODE_NAME_";                     // NOI18N
    private static final String LAYER_PATH_PARAM    
                                            = "_LAYER_PATH_";                    // NOI18N
    private static final String BUNDLE_PATH_PARAM 
                                            = "_BUNDLE_PATH_";                   // NOI18N
    private static final String PROJECT_NAME_PARAM 
                                            = "_PROJECT_NAME_";                  // NOI18N

    // names of templates
    private static final String BUNDLE_NAME = "src/Bundle.properties";           // NOI18N
    private static final String LAYER_NAME  = "src/layer.xml";                   // NOI18N
    private static final String MANIFEST    = "manifest.mf";                     // NOI18N

    private int index;

    private WizardDescriptor.Panel[] panels;

    private WizardDescriptor wiz;

    public CustomComponentWizardIterator() {
    }

    public static CustomComponentWizardIterator createIterator() {
        return new CustomComponentWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] { new CustomComponentWizardPanel(), 
              new JavaMELibsWizardPanel(),
              new DescriptorsWizardPanel(),
              new BasicModuleConfWizardPanel()};
    }

    private String[] createSteps() {
        return new String[] { 
                NbBundle.getMessage(
                        CustomComponentWizardIterator.class, STEP_BASIC_PARAMS) ,
                NbBundle.getMessage(
                        CustomComponentWizardIterator.class, LBL_LIBRARIES),
                NbBundle.getMessage(
                        CustomComponentWizardIterator.class, LBL_COMPONENT_DESC),
                NbBundle.getMessage(
                        CustomComponentWizardIterator.class, FINAL_STEP)
                        };
    }

    public Set/* <FileObject> */instantiate(/* ProgressHandle handle */)
            throws IOException
    {
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirF = FileUtil.normalizeFile((File) wiz
                .getProperty(PROJECT_DIR));
        dirF.mkdirs();

        FileObject template = Templates.getTemplate(wiz);
        FileObject dir = FileUtil.toFileObject(dirF);
        unZipFile(template.getInputStream(), dir , wiz );

        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration<? extends FileObject> e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        return resultSet;
    }

    public void initialize( WizardDescriptor wiz ) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
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
                jc.putClientProperty(SELECTED_INDEX, new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(CONTENT_DATA, steps);
            }
        }
    }

    public void uninitialize( WizardDescriptor wiz ) {
        this.wiz.putProperty(PROJECT_DIR, null);
        this.wiz.putProperty(PROJECT_NAME, null);
        this.wiz = null;
        panels = null;
    }

    public String name() {
        return MessageFormat.format(NbBundle.getBundle(
                CustomComponentWizardIterator.class).getString(
                LBL_WIZARD_STEPS_COUNT), new Object[] {
                new Integer(index + 1) + "", new Integer(panels.length) + "" });
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

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener( ChangeListener l ) {
    }

    public final void removeChangeListener( ChangeListener l ) {
    }

    private static void unZipFile( InputStream source, FileObject projectRoot ,
            WizardDescriptor wizard )
            throws IOException
    {
        try {
            ZipInputStream zipIS = new ZipInputStream(source);
            ZipEntry entry;
            String layerPath = (String)wizard.getProperty(LAYER_PATH);
            while ((entry = zipIS.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                }
                else {
                    FileObject fo = FileUtil.createData(projectRoot, entry
                            .getName());
                    if (PROJECT_XML.equals(entry.getName())) {
                        // Special handling for setting name of Ant-based
                        // projects; customize as needed:
                        filterProjectXML(fo, zipIS, (String)wizard.getProperty( 
                                CODE_BASE_NAME ));
                    }
                    else if ( MANIFEST.equals(entry.getName())){
                        filterManifest( fo , zipIS, wizard );
                    }
                    else if ( LAYER_NAME.equals(entry.getName())){
                        copyLayer( fo , zipIS , wizard );
                    }
                    else if ( BUNDLE_NAME.equals(entry.getName()) ) {
                        filterBundle( fo , zipIS, (String)wizard.getProperty( 
                                CODE_BASE_NAME ) );
                    }
                    else {
                        writeFile(zipIS, fo);
                    }
                }
            }
        }
        finally {
            source.close();
        }
    }

    private static void filterBundle( FileObject fo, ZipInputStream is,
            String property )
    {
        // TODO Auto-generated method stub
        
    }

    private static void copyLayer( FileObject fo, ZipInputStream is,
            WizardDescriptor wizard )
    {
        // TODO Auto-generated method stub
        
    }

    private static void filterManifest( FileObject fo, ZipInputStream is,
            WizardDescriptor wizard )
    {
        // TODO Auto-generated method stub
        
    }

    private static void writeFile( ZipInputStream is, FileObject fo )
            throws IOException
    {
        OutputStream out = fo.getOutputStream();
        try {
            FileUtil.copy(is, out);
        }
        finally {
            out.close();
        }
    }

    private static void filterProjectXML( FileObject fo, ZipInputStream str,
            String name ) throws IOException
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(str, baos);
            Document doc = XMLUtil.parse(new InputSource(
                    new ByteArrayInputStream(baos.toByteArray())), false,
                    false, null, null);
            NodeList nl = doc.getDocumentElement().getElementsByTagName(
                    CODE_NAME_BASE);
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    if (el.getParentNode() != null
                            && DATA.equals(el.getParentNode().getNodeName()))
                    {
                        NodeList nl2 = el.getChildNodes();
                        if (nl2.getLength() > 0) {
                            nl2.item(0).setNodeValue(name);
                        }
                        break;
                    }
                }
            }
            OutputStream out = fo.getOutputStream();
            try {
                XMLUtil.write(doc, out, UTF_8);
            }
            finally {
                out.close();
            }
        }
        catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            writeFile(str, fo);
        }

    }
}
