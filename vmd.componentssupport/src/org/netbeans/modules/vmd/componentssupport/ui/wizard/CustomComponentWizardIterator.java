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

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.BaseHelper;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.JavaMELibsConfigurationHelper;
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

    // unzipped project filtering
    private static final String UTF_8       = "UTF-8";                           // NOI18N
    
    private static final String CODE_NAME_BASE 
                                            = "code-name-base";                  // NOI18N
    private static final String DATA        = "data";                            // NOI18N
    private static final String PROJECT_XML = "nbproject/project.xml";           // NOI18N
    private static final String BUILD_IMPL_XML 
                                            = "nbproject/build-impl.xml";        // NOI18N
    private static final String BUILD_XML   = "build.xml";                       // NOI18N

    // wizard properties
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
    public static final String LBL_LIBRARIES 
                                            = "LBL_LibrariesDescStep";           // NOI18N 
    public static final String LBL_COMPONENT_DESC 
                                            = "LBL_ComponentsDescStep";          // NOI18N
    public static final String FINAL_STEP   = "LBL_FinalStep";                   // NOI18N

    // properties
    public static final String PROJECT_DIR  = "projDir";                         // NOI18N
    public static final String PROJECT_NAME = "projName";                        // NOI18N
    public static final String LAYER_PATH   = "layer";                           // NOI18N
    public static final String BUNDLE_PATH  = "bundle";                          // NOI18N
    public static final String CODE_BASE_NAME
                                            = "codeBaseName";                    // NOI18N
    public static final String DISPLAY_NAME = "displayName";                     // NOI18N
    // added library descriptors
    public static final String LIBRARIES    = "libraries";                       // NOI18N
    public static final String LIB_DISPLAY_NAMES
                                            = "libDisplayNames";                 // NOI18N
    public static final String LIB_NAMES    = "libNames";                        // NOI18N

    // parameters for project
    private static final String CODE_NAME_PARAM 
                                            = "_CODE_NAME_";                     // NOI18N
    private static final String BUNDLE_PATH_PARAM 
                                            = "_BUNDLE_PATH_";                   // NOI18N
    private static final String PROJECT_NAME_PARAM 
                                            = "_PROJECT_NAME_";                  // NOI18N
    // names of templates
    public static final String BUNDLE_PROPERTIES 
                                            = "Bundle.properties";               // NOI18N
    public static final String LAYER_XML    = "layer.xml";             // NOI18N

    private static final String SRC         = "src/";                            // NOI18N
    private static final String BUNDLE_NAME = SRC + BUNDLE_PROPERTIES;           // NOI18N
    private static final String LAYER_NAME  = SRC + LAYER_XML;                   // NOI18N
    private static final String MANIFEST    = "manifest.mf";                     // NOI18N
    private static final String LAYER       = "OpenIDE-Module-Layer: ";          // NOI18N

    private CustomComponentWizardIterator() {
    }

    public static CustomComponentWizardIterator createIterator() {
        return new CustomComponentWizardIterator();
    }

    WizardDescriptor.Panel[] createPanels() {
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
        File dirF = FileUtil.normalizeFile((File) myWizard
                .getProperty(PROJECT_DIR));
        dirF.mkdirs();

        FileObject template = Templates.getTemplate(myWizard);
        FileObject dir = FileUtil.toFileObject(dirF);
        unZipFile(template.getInputStream(), dir , myWizard );

        
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
        
        Project createdProject = FileOwnerQuery.getOwner(dir);
        JavaMELibsConfigurationHelper
                .configureJavaMELibs(createdProject, myWizard);

        return resultSet;
    }

    public void initialize( WizardDescriptor wiz ) {
        myWizard = wiz;
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
        wiz.putProperty(PROJECT_DIR, null);
        wiz.putProperty(PROJECT_NAME, null);
        wiz.putProperty(LIBRARIES, null );
        wiz.putProperty(LIB_NAMES, null);
        wiz.putProperty(LIB_DISPLAY_NAMES, null);
        wiz = null;
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
            while ((entry = zipIS.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                }
                else {
                    FileObject fo = null;
                    if (PROJECT_XML.equals(entry.getName())) {
                        // Special handling for setting name of Ant-based
                        // projects; customize as needed:
                        fo = FileUtil.createData(projectRoot, entry
                                .getName());
                        filterProjectXML(fo, zipIS, (String)wizard.getProperty( 
                                CODE_BASE_NAME ));
                    }
                    else if ( MANIFEST.equals(entry.getName())){
                        fo = FileUtil.createData(projectRoot, entry
                                .getName());
                        filterManifest( fo , zipIS, wizard );
                    }
                    else if ( LAYER_NAME.equals(entry.getName())){
                        copyLayer( projectRoot , zipIS , wizard );
                    }
                    else if ( BUNDLE_NAME.equals(entry.getName()) ) {
                        filterBundle( projectRoot , zipIS, wizard );
                    }
                    else if ( BUILD_XML.equals(entry.getName()) 
                            || BUILD_IMPL_XML.equals(entry.getName())) 
                    {
                        fo = FileUtil.createData(projectRoot, entry
                                .getName());
                        filterBuild( fo , zipIS, (String)wizard.getProperty( 
                                CODE_BASE_NAME ) );
                    }
                    else {
                        fo = FileUtil.createData(projectRoot, entry
                                .getName());
                        BaseHelper.copyByteAfterByte(zipIS, fo);
                    }
                }
            }
        }
        finally {
            source.close();
        }
    }

    private static void filterBuild( FileObject fo, ZipInputStream zipIS,
            String codeBaseName ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy( zipIS , baos);
        String content = baos.toString(UTF_8);
        
        content = content.replace( CODE_NAME_PARAM , codeBaseName );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content
                .getBytes(UTF_8));
        OutputStream out = fo.getOutputStream();
        try {
            FileUtil.copy(inputStream, out);
        }
        finally {
            out.close();
        }    
    }

    private static void filterBundle( FileObject projectRoot, ZipInputStream is,
            WizardDescriptor wizardDescriptor ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(is, baos);
        String content = baos.toString(UTF_8);
        
        content = content.replace( PROJECT_NAME_PARAM , 
                (String)wizardDescriptor.getProperty( DISPLAY_NAME ) );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content
                .getBytes(UTF_8));
        FileObject fileObject = FileUtil.createData(projectRoot, 
                SRC + (String)wizardDescriptor.getProperty( BUNDLE_PATH) );
        OutputStream out = fileObject.getOutputStream();
        try {
            FileUtil.copy(inputStream, out);
        }
        finally {
            out.close();
        }                
    }

    private static void copyLayer( FileObject projectRoot, ZipInputStream is,
            WizardDescriptor wizard ) throws IOException
    {
        String layer = (String)wizard.getProperty( LAYER_PATH);
        if ( layer == null || layer.length() ==0 ){
            return;
        }
        FileObject fileObject = FileUtil.createData(projectRoot, SRC + layer );
        BaseHelper.copyByteAfterByte(is, fileObject );
    }

    private static void filterManifest( FileObject fo, ZipInputStream is,
            WizardDescriptor wizard ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(is, baos);
        String content = baos.toString(UTF_8);
        
        content = content.replace( CODE_NAME_PARAM , (String)wizard.getProperty( 
                CODE_BASE_NAME ));
        content = content.replace( BUNDLE_PATH_PARAM, (String)wizard.getProperty( 
                BUNDLE_PATH));
        StringBuilder builder = new StringBuilder( content );
        String layer = (String)wizard.getProperty( LAYER_PATH);
        if ( layer != null){
            builder.append( LAYER );
            builder.append( layer );
            builder.append( "\n" );
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                builder.toString().getBytes(UTF_8));
        OutputStream out = fo.getOutputStream();
        try {
            FileUtil.copy(inputStream, out);
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
            BaseHelper.copyByteAfterByte(str, fo);
        }

    }
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor myWizard;
}
