/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.componentssupport.ui.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.CustomComponentWizardIterator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author akorostelev
 */
public class ProjectTemplateZipHelper extends BaseHelper {

    private static final String PROJECT_XML = "nbproject/project.xml";           // NOI18N
    private static final String BUILD_IMPL_XML 
                                            = "nbproject/build-impl.xml";        // NOI18N
    private static final String BUILD_XML   = "build.xml";                       // NOI18N
    private static final String PLATFORM_PROPERTIES 
                                            = "nbproject/platform.properties";   // NOI18N
    private static final String SUITE_PROPERTIES 
                                            = "nbproject/suite.properties";      // NOI18N
    private static final String SUITE_PRIVATE_PROPERTIES 
                            = "nbproject/private/suite-private.properties";      // NOI18N

    // unzipped project filtering
    private static final String CODE_NAME_BASE 
                                            = "code-name-base";                  // NOI18N
    private static final String DATA        = "data";                            // NOI18N

    // parameters for project
    private static final String CODE_NAME_PARAM 
                                            = "_CODE_NAME_";                     // NOI18N
    private static final String BUNDLE_PATH_PARAM 
                                            = "_BUNDLE_PATH_";                   // NOI18N
    private static final String PROJECT_NAME_PARAM 
                                            = "_PROJECT_NAME_";                  // NOI18N
    private static final String PLATFORM_ID_PARAM 
                                            = "_PLATFORM_ID_";                   // NOI18N
    private static final String SUITE_PATH_PARAM 
                                            = "suite.dir";                       // NOI18N

    // names of templates
    private static final String BUNDLE_NAME = SRC + BUNDLE_PROPERTIES;           // NOI18N
    private static final String LAYER_NAME  = SRC + LAYER_XML;                   // NOI18N
    private static final String MANIFEST    = "manifest.mf";                     // NOI18N
    private static final String LAYER       = "OpenIDE-Module-Layer: ";          // NOI18N

    public static void unZipFile( InputStream source, FileObject projectRoot ,
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
                                CustomComponentWizardIterator.CODE_BASE_NAME ));
                    }
                    else if ( MANIFEST.equals(entry.getName())){
                        fo = FileUtil.createData(projectRoot, entry
                                .getName());
                        filterManifest( fo , zipIS, wizard );
                    }
                    else if ( PLATFORM_PROPERTIES.equals(entry.getName())){
                        fo = FileUtil.createData(projectRoot, entry
                                .getName());
                        filterPlatformProperties( fo , zipIS, wizard );
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
                                CustomComponentWizardIterator.CODE_BASE_NAME ) );
                    }
                    else {
                        fo = FileUtil.createData(projectRoot, entry
                                .getName());
                        copyByteAfterByte(zipIS, fo);
                    }
                }
            }
        }
        finally {
            source.close();
        }
        createSuiteProperties(projectRoot, wizard);
    }
    
    /**
     * Detects whether <code>projectDir</code> is relative to
     * <code>suiteDir</code> and creates <em>nbproject/suite.properties</em> or
     * <em>nbproject/private/suite-private.properties</em> with
     * <em>suite.dir</em> appropriately set.
     */
    // copied from org.netbeans.modules.apisupport.project.NbModuleProjectGenerator
    private static void createSuiteProperties(FileObject projectDir, 
            WizardDescriptor wizard) 
            throws IOException 
    {
        if (!isSuiteComponent(wizard)){
            return;
        }
        
        File projectDirF = FileUtil.toFile(projectDir);
        String suiteDirPath = getSuiteRoot(wizard);
        File suiteDir = FileUtil.normalizeFile(new File(suiteDirPath));
        
        String suiteLocation;
        String suitePropertiesLocation;
        
        String rel = PropertyUtils.relativizeFile(projectDirF, suiteDir);
        if (rel != null) {
            suiteLocation = "${basedir}/" + rel; // NOI18N
            suitePropertiesLocation = SUITE_PROPERTIES;
        } else {
            suiteLocation = suiteDir.getAbsolutePath();
            suitePropertiesLocation = SUITE_PRIVATE_PROPERTIES;
        }
        EditableProperties props = new EditableProperties(true);
        props.setProperty(SUITE_PATH_PARAM, suiteLocation); 
        FileObject suiteProperties = FileUtil.createData(projectDir, suitePropertiesLocation);
        storeProperties(suiteProperties, props);
    }

    private static void filterBuild( FileObject fo, ZipInputStream zipIS,
            String codeBaseName ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy( zipIS , baos);
        String content = baos.toString(UTF_8);
        
        content = content.replace( CODE_NAME_PARAM , codeBaseName );
        copyByteAfterByte(content, fo);
    }

    private static void filterBundle( FileObject projectRoot, ZipInputStream is,
            WizardDescriptor wizardDescriptor ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(is, baos);
        String content = baos.toString(UTF_8);
        
        content = content.replace( PROJECT_NAME_PARAM , 
                (String)wizardDescriptor.getProperty( 
                        CustomComponentWizardIterator.DISPLAY_NAME ) );

        FileObject fileObject = FileUtil.createData(projectRoot, 
                SRC + (String)wizardDescriptor.getProperty( 
                        CustomComponentWizardIterator.BUNDLE_PATH) );
        copyByteAfterByte(content, fileObject);
    }

    private static void copyLayer( FileObject projectRoot, ZipInputStream is,
            WizardDescriptor wizard ) throws IOException
    {
        String layer = (String)wizard.getProperty( 
                CustomComponentWizardIterator.LAYER_PATH);
        if ( layer == null || layer.length() ==0 ){
            return;
        }
        FileObject fileObject = FileUtil.createData(projectRoot, SRC + layer );
        copyByteAfterByte(is, fileObject );
    }

    private static void filterManifest( FileObject fo, ZipInputStream is,
            WizardDescriptor wizard ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(is, baos);
        String content = baos.toString(UTF_8);
        
        content = content.replace( CODE_NAME_PARAM , (String)wizard.getProperty( 
                CustomComponentWizardIterator.CODE_BASE_NAME ));
        content = content.replace( BUNDLE_PATH_PARAM, (String)wizard.getProperty( 
                CustomComponentWizardIterator.BUNDLE_PATH));
        StringBuilder builder = new StringBuilder( content );
        String layer = (String)wizard.getProperty( 
                CustomComponentWizardIterator.LAYER_PATH);
        if ( layer != null){
            builder.append( LAYER );
            builder.append( layer );
            builder.append( "\n" );
        }

        copyByteAfterByte(builder.toString(), fo);
    }

    private static void filterPlatformProperties( FileObject fo, ZipInputStream is,
            WizardDescriptor wizard ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(is, baos);
        String content = baos.toString(UTF_8);
        
        content = content.replace( PLATFORM_ID_PARAM , getActivePlatform(wizard));

        copyByteAfterByte(content, fo);
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
            copyByteAfterByte(str, fo);
        }

    }
    
}
