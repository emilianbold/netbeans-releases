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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.CustomComponentWizardIterator;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.NewComponentDescriptor;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.PaletteCategory;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.Version;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author avk
 */
public abstract class CustomComponentHelper extends BaseHelper {
    
    public static final String OPENIDE_MODULE = "OpenIDE-Module"; // NOI18N
    public static final String OPENIDE_MODULE_LAYER = "OpenIDE-Module-Layer"; // NOI18N
    public static final String OPENIDE_MODULE_LOCALIZING_BUNDLE 
                                        = "OpenIDE-Module-Localizing-Bundle"; // NOI18N

    public static final String TEMPLATE_COMP_DESCR  = "CustomComponentCD.java"; //NOI18N
    public static final String TEMPLATE_PRODUCER    = "CustomComponentProducer.java"; //NOI18N

    private static final String MANIFEST  = "manifest.mf";                      // NOI18N
    
    public abstract Set<FileObject> instantiate() throws IOException ;
    
    public abstract String getCodeNameBase();
    
    /**
     * creates path to ComponentDescriptor java file
     * @return path to ComponentDescriptor java file relative to Project directory
     */
    public abstract String getCDPath();

    /**
     * creates path to Producer java file
     * @return path to Producer java file relative to Project directory
     */
    public abstract String getProducerPath();
    
    /**
     * creates path to CD class relative to source directory
     * @param slashCodeNameBase
     * @param cdName
     * @return
     */
    protected String createCDPath(String slashCodeNameBase, String cdName){
        return slashCodeNameBase + "/" + DESCRIPTORS + "/" +
                cdName + JAVA_EXTENSION; // NOI18N
    }

    /**
     * creates path to Producer class relative to source directory
     * @param slashCodeNameBase
     * @param producerName
     * @return
     */
    protected String createProducerPath(String slashCodeNameBase, String producerName){
        return slashCodeNameBase + "/" + PRODUCERS + "/" +
                producerName + JAVA_EXTENSION; // NOI18N
    }
    
    public static class InstantiationToWizardHelper extends CustomComponentHelper{

        public InstantiationToWizardHelper(WizardDescriptor mainWizard, 
                WizardDescriptor componentWizard )
        {
            myMainWizard = mainWizard;
            myComponentWizard = componentWizard;
        }
        
        @Override
        public String getCDPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getProducerPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * stores created custom component from componentWizard into targetWizard.
         * Is used when target wizard on one of it's steps invokes component wizard
         * to store component wizard data and perform real instantiation in instantiate()
         * method of target wizard iterator.
         * @param componentWizard New Custom component wizard descriptor
         * @param targetWizard parent wizard descriptor, from which component Wizard
         * was invoked
         */
        @Override
        public Set<FileObject> instantiate() throws IOException {
            assert (myComponentWizard != null) && (myMainWizard != null);

            List<Map> components = (List<Map>) myMainWizard.getProperty(
                    CustomComponentWizardIterator.CUSTOM_COMPONENTS);
            if (components == null) {
                components = new LinkedList<Map>();
                myMainWizard.putProperty(CustomComponentWizardIterator.CUSTOM_COMPONENTS,
                        components);
            }
            components.add(myComponentWizard.getProperties());
            return Collections.EMPTY_SET;
        }
        
        public String getCodeNameBase() {
            String codeNameBase = (String) myMainWizard.getProperty(
                    CustomComponentWizardIterator.CODE_BASE_NAME);
            String projectName = (String) myMainWizard.getProperty(
                    CustomComponentWizardIterator.PROJECT_NAME);
            if (codeNameBase == null) {
                codeNameBase = getDefaultCodeNameBase(projectName);
            }
            return codeNameBase;
        }

        private WizardDescriptor myComponentWizard;
        private WizardDescriptor myMainWizard;
    }

    public static class RealInstantiationHelper extends CustomComponentHelper{

        private static final String INSTANCE_NAME_EXTENSION  
                                                        = ".instance";          //NOI18N
        //// tags identifiers (e.g. attribute values )
        private static final String LAYER_TAG_VMD_MIDP  = "vmd-midp";           //NOI18N
        private static final String LAYER_TAG_COMPONENTS = "components";        //NOI18N
        private static final String LAYER_TAG_PRODUCERS = "producers";          //NOI18N
        //// xpaths to tags
        private static final String LAYER_XPATH_VMD_MIDP 
                        = "./folder[@name=\"" + LAYER_TAG_VMD_MIDP + "\"]";     //NOI18N
        private static final String LAYER_XPATH_COMPONENTS 
                        = "./folder[@name=\""+LAYER_TAG_COMPONENTS+"\"]";       //NOI18N
        private static final String LAYER_XPATH_PRODUCERS 
                        = "./folder[@name=\""+LAYER_TAG_PRODUCERS+"\"]";        //NOI18N
    
        private static final String VALIDITY_TOKEN_VALUE_ALWAYS = "always";//NOI18N
        private static final String VALIDITY_TOKEN_VALUE_PLATFORM = "platform";//NOI18N
        private static final String VALIDITY_TOKEN_VALUE_CUSTOM = "custom";//NOI18N
        
        public RealInstantiationHelper(Project project, Map<String, Object> component){
            myProject = project;
            myComponent = component;
        }

        @Override
        public Set<FileObject> instantiate() throws IOException {
            Set<FileObject> result = new LinkedHashSet<FileObject>();

            FileObject cdFO = configureComponentDescriptor();
            result.add(cdFO);
            
            FileObject producerFO = configureProducer();
            result.add(producerFO);
            
            result.addAll( configureLayerXml() );
            
            result.addAll( configureIcons() );
            
            return result;
        }
        
        @Override
        public String getCDPath() {
            String dotCodeNameBase = getCodeNameBase();
            String name = getCDClassName();

            String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        
            return SRC + createCDPath(codeNameBase, name);
        }

        @Override
        public String getProducerPath() {
            String dotCodeNameBase = getCodeNameBase();
            String name = getProducerClassName();

            String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        
            return SRC + createProducerPath(codeNameBase, name);
        }
        
        @Override
        public String getCodeNameBase() {
            if (myCodeNameBase == null){
                Manifest manifest = getManifest();
                Attributes attrs = manifest.getMainAttributes();
        String codename = attrs.getValue(OPENIDE_MODULE);
        if (codename != null) {
            int slash = codename.lastIndexOf('/');
            if (slash == -1) {
                myCodeNameBase = codename;
            } else {
                myCodeNameBase = codename.substring(0, slash);
            }
        }
            
            }
            return myCodeNameBase;
        }
        
        protected String getLocalizingBundle(){
            if (myBundlePath == null){
                Manifest manifest = getManifest();
                Attributes attrs = manifest.getMainAttributes();
                myBundlePath = attrs.getValue(OPENIDE_MODULE_LOCALIZING_BUNDLE);
            }
            return myBundlePath;
        }
        
        protected String getLayer(){
            if (myLayerPath == null){
                Manifest manifest = getManifest();
                Attributes attrs = manifest.getMainAttributes();
                myLayerPath = SRC + attrs.getValue(OPENIDE_MODULE_LAYER);
            }
            return myLayerPath;
        }
        
        protected Project getProject(){
            return myProject;
        }
        
        public String getCDPkg() {
            return getCodeNameBase() + "." + DESCRIPTORS;
        }

        public String getProducerPkg() {
            return getCodeNameBase() + "." + PRODUCERS;
        }

        public String getCDLayerInstanceName() {
            String dotCodeNameBase = getCodeNameBase();
            String name = getCDClassName();

            String codeNameBase = dotCodeNameBase.replace('.', '-');            // NOI18N
            return codeNameBase + "-" + DESCRIPTORS + "-" +
                    name + INSTANCE_NAME_EXTENSION;                             // NOI18N
        }

        public String getProducerLayerInstanceName() {
            String dotCodeNameBase = getCodeNameBase();
            String name = getProducerClassName();

            String codeNameBase = dotCodeNameBase.replace('.', '-');            // NOI18N
            return codeNameBase + "-" + PRODUCERS + "-" +
                    name + INSTANCE_NAME_EXTENSION;                             // NOI18N
        }
        
        private FileObject configureComponentDescriptor()
                throws IOException
        {
            FileObject template = getTemplate(TEMPLATE_COMP_DESCR);
            Map<String, String> tokens = getCDTokens();
            
            return doCopyFile(getProject().getProjectDirectory(), getCDPath(), 
                    template, tokens);
        }

        private FileObject configureProducer()
                throws IOException
        {
            FileObject template = getTemplate(TEMPLATE_PRODUCER);
            Map<String, String> tokens = getProducerTokens();
            
            return doCopyFile(getProject().getProjectDirectory(), getProducerPath(), 
                    template, tokens);
        }

        private Map<String, String> getCDTokens(){
            Map<String, String> tokens = new HashMap<String, String>();
            tokens.put("package", getCDPkg());
            tokens.put("cdName", getCDClassName());
            tokens.put("typeId", 
                    (String)myComponent.get(NewComponentDescriptor.CD_TYPE_ID));
            tokens.put("superDescriptorClass", 
                    (String)myComponent.get(NewComponentDescriptor.CD_SUPER_DESCR_CLASS));
            tokens.put("prefix", 
                    (String)myComponent.get(NewComponentDescriptor.CC_PREFIX));
            tokens.put("canInstantiate", 
                    myComponent.get(NewComponentDescriptor.CD_CAN_INSTANTIATE).toString());
            tokens.put("canBeSuper", 
                    myComponent.get(NewComponentDescriptor.CD_CAN_BE_SUPER).toString());
            tokens.put("midpVersion", getMidpVersionToken());
            return tokens;
        }
        
        private String getMidpVersionToken(){
            Version version = (Version)myComponent.get(NewComponentDescriptor.CD_VERSION);
            assert version != null;
            return version.javaCodeValue();
        }
        
        private Map<String, String> getProducerTokens(){
            Map<String, String> tokens = new HashMap<String, String>();
            tokens.put("package", getProducerPkg());
            tokens.put("producerName", getProducerClassName());
            tokens.put("iconPathSmall", getSmallIconToken());
            tokens.put("iconPathLarge", getLargeIconToken());
            tokens.put("cdName", getCDClassName());
            tokens.put("cdPackage", getCDPkg());
            tokens.put("paletteCategory", getPaletteCategoryToken());
            tokens.put("paletteDisplayName", 
                    (String)myComponent.get(NewComponentDescriptor.CP_PALETTE_DISP_NAME));
            tokens.put("paletteTooltip", 
                    (String)myComponent.get(NewComponentDescriptor.CP_PALETTE_TIP));
            
            if ((Boolean)myComponent.get(NewComponentDescriptor.CP_ADD_LIB)){
                tokens.put("libraryName", 
                        (String)myComponent.get(NewComponentDescriptor.CP_LIB_NAME));
            }
            tokens.put("validity", getProducerValidityToken());
            return tokens;
        }
        
        private String getPaletteCategoryToken(){
            PaletteCategory category = (PaletteCategory)myComponent.get(
                    NewComponentDescriptor.CP_PALETTE_CATEGORY);
            assert category != null;
            return category.javaCodeValue();
        }

        private String getProducerValidityToken(){

            Boolean always = (Boolean) myComponent.get(
                    NewComponentDescriptor.CP_VALID_ALWAYS);
            Boolean platform = (Boolean) myComponent.get(
                    NewComponentDescriptor.CP_VALID_PLATFORM);
            Boolean custom = (Boolean) myComponent.get(
                    NewComponentDescriptor.CP_VALID_CUSTOM);

            
            assert always != null || platform != null || custom != null;
            
            if (always != null){
                return VALIDITY_TOKEN_VALUE_ALWAYS;
            } else if (platform != null){
                return VALIDITY_TOKEN_VALUE_PLATFORM;
            } else if (custom != null){
                return VALIDITY_TOKEN_VALUE_CUSTOM;
            }
            
            return VALIDITY_TOKEN_VALUE_ALWAYS;
        }
        
        private String getSmallIconToken(){
            return getIconToken(getSmallIconPath());
        }
        
        private String getLargeIconToken(){
            return getIconToken(getLargeIconPath());
        }
        
        private String getIconToken(String srcPath){
            if (srcPath == null){
                return NULL;
            }
            
            String dotCodeNameBase = getCodeNameBase();
            File iconFile = new File(srcPath);
            String name = iconFile.getName();
            
            return dotCodeNameBase + "." + BaseHelper.RESOURCES + "." + name;
        }
        
        private Set<FileObject> configureLayerXml()
                throws IOException
        {
            String layerXmlPath = getLayer();
            FileObject prjDir = getProject().getProjectDirectory();
            FileObject layerXmlFO = FileUtil.createData(prjDir, layerXmlPath);
            
            try {
                Document doc = LayerXmlHelper.parseXmlDocument(layerXmlFO);
                Element docRoot = doc.getDocumentElement();

                XPath xpath = XPathFactory.newInstance().newXPath();

                Node fsNode = LayerXmlHelper.goToFilesystemNode(doc, xpath, docRoot);
                Node vmdMidpNode = goToVmdMidpNode(doc, xpath, fsNode);
                Node compsNode = goToComponentsNode(doc, xpath, vmdMidpNode);
                
                Element cd = doc.createElement(LayerXmlHelper.LAYER_FOLDER);
                cd.setAttribute( LayerXmlHelper.LAYER_NAME, getCDLayerInstanceName() );
                compsNode.appendChild(cd);
                
                Node produsersNode = goToProducersNode(doc, xpath, vmdMidpNode);
                
                Element prod = doc.createElement(LayerXmlHelper.LAYER_FOLDER);
                prod.setAttribute( LayerXmlHelper.LAYER_NAME, getProducerLayerInstanceName() );
                produsersNode.appendChild(prod);
                
                LayerXmlHelper.saveXmlDocument(doc, layerXmlFO);
                
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            
            return Collections.EMPTY_SET;
        }

    private static Node goToProducersNode(Document doc, XPath xpath, Node parent)
            throws XPathExpressionException 
    {
        String expression = LAYER_XPATH_PRODUCERS;
        Node libsNode = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
        if (libsNode == null) {
            Element libsElement = doc.createElement(LayerXmlHelper.LAYER_FOLDER);
            libsElement.setAttribute(LayerXmlHelper.LAYER_NAME, LAYER_TAG_PRODUCERS);
            parent.appendChild(libsElement);
            libsNode = libsElement;
        }
        return libsNode;
    }
    
    private static Node goToComponentsNode(Document doc, XPath xpath, Node parent)
            throws XPathExpressionException 
    {
        String expression = LAYER_XPATH_COMPONENTS;
        Node libsNode = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
        if (libsNode == null) {
            Element libsElement = doc.createElement(LayerXmlHelper.LAYER_FOLDER);
            libsElement.setAttribute(LayerXmlHelper.LAYER_NAME, LAYER_TAG_COMPONENTS);
            parent.appendChild(libsElement);
            libsNode = libsElement;
        }
        return libsNode;
    }
    
    private static Node goToVmdMidpNode(Document doc, XPath xpath, Node parent) 
            throws XPathExpressionException
    {
        String expression = LAYER_XPATH_VMD_MIDP;
        Node libsAreaNode = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
        if (libsAreaNode == null) {
            Element libsAreaElement = doc.createElement(LayerXmlHelper.LAYER_FOLDER);
            libsAreaElement.setAttribute(LayerXmlHelper.LAYER_NAME, LAYER_TAG_VMD_MIDP);
            parent.appendChild(libsAreaElement);
            libsAreaNode = libsAreaElement;
        }
        return libsAreaNode;

    }
            
        private Set<FileObject> configureIcons()
                throws IOException {
            Set<FileObject> result = new LinkedHashSet<FileObject>();
            String small = getSmallIconPath();
            if (small != null) {
                result.add( copyIcon(small) );
            }

            String large = getLargeIconPath();
            if (large != null) {
                result.add( copyIcon(large) );
            }
            return result;
        }

        private FileObject copyIcon(String srcPath) throws IOException {
            String dotCodeNameBase = getCodeNameBase();
            String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N

            File iconFile = new File(srcPath);
            
            String name = iconFile.getName();
            String targetPath = SRC + codeNameBase + "/" + 
                    BaseHelper.RESOURCES + "/" + name;
            
            FileObject targetFO = FileUtil.createData(
                    getProject().getProjectDirectory(), targetPath);
            copyByteAfterByte(iconFile, targetFO);
            
            return targetFO;
        }
        
        private String getSmallIconPath() {
            String path = (String) myComponent.get(
                    NewComponentDescriptor.CP_SMALL_ICON);
            if (path == null || path.length() == 0) {
                return null;
            }
            return path;
        }

        private String getLargeIconPath() {
            String path = (String) myComponent.get(
                    NewComponentDescriptor.CP_LARGE_ICON);
            if (path == null || path.length() == 0) {
                return null;
            }
            return path;
        }

        private String getCDClassName() {
            return (String) myComponent.get(
                    NewComponentDescriptor.CD_CLASS_NAME);
        }
        
        private String getProducerClassName() {
            return (String) myComponent.get(
                    NewComponentDescriptor.CP_CLASS_NAME);
        }

        private Manifest getManifest() {
            if(myManifest != null){
                return myManifest;
            }
            
            FileObject manifestFO = getProject().getProjectDirectory().
                    getFileObject(MANIFEST);
            if (manifestFO != null) {
                try {
                    InputStream is = manifestFO.getInputStream();
                    try {
                        myManifest = new Manifest(is);
                    } finally {
                        is.close();
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            return myManifest;
        }
        
        private Project myProject;
        private Map<String, Object> myComponent;
        private String myBundlePath;
        private String myLayerPath;
        private String myCodeNameBase;
        private Manifest myManifest;
        
    }
}
