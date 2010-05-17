/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.io.OutputStream;
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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.CustomComponentWizardIterator;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.NewComponentDescriptor;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.PaletteCategory;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.Version;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract helper for custom component filed preview and instantiation.
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
    
    /**
     * code name base with dot as delimiter
     * @return
     */
    public abstract String getCodeNameBase();
    
    public abstract String getProjectName();
    
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
     * checks if given component descriptor class already exists
     * @param name
     * @return
     */
    public abstract boolean isCDClassNameExist(String name);

    /**
     * checks if given component Producer class already exists
     * @param name
     * @return
     */
    public abstract boolean isProducerClassNameExist(String name);
        
    /**
     * creates path to CD class relative to source directory
     * @param slashCodeNameBase
     * @param cdName
     * @return
     */
    protected String createCDPath(String cdName){
        String slashCodeNameBase = getCodeNameBase().replace('.', '/'); // NOI18N
        return SRC + slashCodeNameBase + "/" + DESCRIPTORS + "/" +
                cdName + JAVA_EXTENSION; // NOI18N
    }

    /**
     * creates path to Producer class relative to project directory
     * @param slashCodeNameBase
     * @param producerName
     * @return
     */
    protected String createProducerPath(String producerName){
        String slashCodeNameBase = getCodeNameBase().replace('.', '/'); // NOI18N
        return SRC + slashCodeNameBase + "/" + PRODUCERS + "/" +
                producerName + JAVA_EXTENSION; // NOI18N
    }
    
    /**
     * CustomComponentHelper implementation for 
     * "New Custom Component" wizard started from 
     * CustomComponentWizardIterator panels.
     * <p>
     * It instantiates data into main WizardDescriptor. 
     * And allows to preview created and modified files that will be actually 
     * updated by main wizard. Doesn't perform any real instantiation.
     */
    public static class InstantiationToWizardHelper extends CustomComponentHelper{

        public InstantiationToWizardHelper(WizardDescriptor mainWizard, 
                WizardDescriptor componentWizard )
        {
            myMainWizard = mainWizard;
            myComponentWizard = componentWizard;
        }

        public boolean isCDClassNameExist(String name) {
            return checkIfComponentValueExists(
                    NewComponentDescriptor.CD_CLASS_NAME, name);
        }

        public boolean isProducerClassNameExist(String name) {
            return checkIfComponentValueExists(
                    NewComponentDescriptor.CP_CLASS_NAME, name);
        }

        @Override
        public String getProjectName() {
            return (String) myMainWizard.getProperty(
                    CustomComponentWizardIterator.PROJECT_NAME);
        }
        
        
        @Override
        public String getCDPath() {
            String name = getCDClassName();
        
            return createCDPath(name);
        }

        @Override
        public String getProducerPath() {
            String name = getProducerClassName();
        
            return createProducerPath(name);
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
        
        /**
         * Returns code name base for project.
         * @return cnb string with '.' as separator.
         */
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

        private boolean checkIfComponentValueExists(String key, Object value) {
            List<Map<String, Object>> list = getExistingComponents();
            if (list == null) {
                return false;
            }
            for (Map<String, Object> comp : list) {
                Object testValue = comp.get(key);
                if (testValue.equals(value)) {
                    return true;
                }
            }
            return false;
        }

        private List<Map<String, Object>> getExistingComponents() {
            Object value = myMainWizard.getProperty(
                        CustomComponentWizardIterator.CUSTOM_COMPONENTS);
            if (value == null || !(value instanceof List)) {
                return null;
            }
            return (List<Map<String, Object>>) value;
        }

        private String getCDClassName() {
            return (String) myComponentWizard.getProperty(
                    NewComponentDescriptor.CD_CLASS_NAME);
        }
        
        private String getProducerClassName() {
            return (String) myComponentWizard.getProperty(
                    NewComponentDescriptor.CP_CLASS_NAME);
        }

        private WizardDescriptor myComponentWizard;
        private WizardDescriptor myMainWizard;
    }

    /**
     * CustomComponentHelper implementation for Independent wizard 
     * started from existing project. instantiate performs real files 
     * updating and creation in existing project.
     */
    public static class RealInstantiationHelper extends CustomComponentHelper{

        private static final String INSTANCE_NAME_EXTENSION  
                                                        = ".instance";          //NOI18N
        //// layer tags identifiers (e.g. attribute values )
        private static final String LAYER_TAG_VMD_MIDP  = "vmd-midp";           //NOI18N
        private static final String LAYER_TAG_COMPONENTS = "components";        //NOI18N
        private static final String LAYER_TAG_PRODUCERS = "producers";          //NOI18N
        //// xpaths to layer tags
        private static final String LAYER_XPATH_VMD_MIDP 
                        = "./folder[@name=\"" + LAYER_TAG_VMD_MIDP + "\"]";     //NOI18N
        private static final String LAYER_XPATH_COMPONENTS 
                        = "./folder[@name=\""+LAYER_TAG_COMPONENTS+"\"]";       //NOI18N
        private static final String LAYER_XPATH_PRODUCERS 
                        = "./folder[@name=\""+LAYER_TAG_PRODUCERS+"\"]";        //NOI18N
    
        private static final String VALIDITY_TOKEN_VALUE_ALWAYS = "always";//NOI18N
        private static final String VALIDITY_TOKEN_VALUE_PLATFORM = "platform";//NOI18N
        private static final String VALIDITY_TOKEN_VALUE_CUSTOM = "custom";//NOI18N
        
        private static final String VMD_MIDP_NAME = "org.netbeans.modules.vmd.midp";//NOI18N
        private static final String VMD_MIDP_VERSION = "1.1";//NOI18N
        private static final String VMD_MODEL_NAME = "org.netbeans.modules.vmd.model";//NOI18N
        private static final String VMD_MODEL_VERSION = "1.1";//NOI18N
        private static final String VMD_PROPERTIES_NAME = "org.netbeans.modules.vmd.properties";//NOI18N
        private static final String VMD_PROPERTIES_VERSION = "1.1";//NOI18N
        private static final String OPENIDE_UTIL_NAME = "org.openide.util";//NOI18N
        private static final String OPENIDE_UTIL_VERSION = "7.12";//NOI18N
                
        /**
         * Constructor to be used in main wizard 
         * (CustomComponentWizardIterator.instantiate() method)
         * to instantiate custom component basing on data stored in component Map.
         * @param project where to store custom component
         * @param component Map with custom component data. Map returned by
         * {@link org.openide.WizardDescriptor.getProperties } is expected. 
         * {@link org.openide.WizardDescriptor.getProperties } returns Map with 
         * already stored values only - So be careful to use it after 
         * custom component wizard is finished.
         */
        public RealInstantiationHelper(Project project, Map<String, Object> component){
            myProject = project;
            myComponent = component;
            myComponentWizard = null;
            
            assert myComponent != null;
        }

        /**
         * Constructor to be used in independent custom component wizard.
         * The only difference from {@link RealInstantiationHelper(Project, Map)} is 
         * that this constructor can be used to create helper when wizard 
         * is not finished yet.
         * @param project where to store custom component
         * @param wizard New Custom Componet WizardDescriptor.
         */
        public RealInstantiationHelper(Project project, WizardDescriptor wizard){
            myProject = project;
            myComponent = null;
            myComponentWizard = wizard;
            
            assert myComponentWizard != null;
        }

        public boolean isCDClassNameExist(String name) {
            FileObject prjDir = getProject().getProjectDirectory();
            String cd = createCDPath(name);
            return isFileExist(prjDir, cd);
        }

        public boolean isProducerClassNameExist(String name) {
            FileObject prjDir = getProject().getProjectDirectory();
            String producer = createProducerPath(name);
            return isFileExist(prjDir, producer);
        }

        @Override
        public String getProjectName() {
            return ProjectUtils.getInformation(myProject).getDisplayName();
        }

        
        @Override
        public Set<FileObject> instantiate() throws IOException {
            Set<FileObject> result = new LinkedHashSet<FileObject>();

            assert myComponent != null || myComponentWizard != null;

            // add module dependencies at first. they can be used at following steps.
            configureDependencies();
            
            FileObject cdFO = configureComponentDescriptor();
            result.add(cdFO);
            
            FileObject producerFO = configureProducer();
            result.add(producerFO);
            
            result.addAll( configureLayerXml() );
            
            result.addAll( configureProducerBundle() );
            
            result.addAll( configureIcons() );
            
            return result;
        }
        
        @Override
        public String getCDPath() {
            String name = getCDClassName();

            return createCDPath(name);
        }

        @Override
        public String getProducerPath() {
            String name = getProducerClassName();

            return createProducerPath(name);
        }
        
        @Override
        public String getCodeNameBase() {
            if (myCodeNameBase != null) {
                return myCodeNameBase;
            }
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
            return myCodeNameBase;
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
                if (attrs.containsKey(OPENIDE_MODULE_LAYER)){
                    myLayerPath = SRC + attrs.getValue(OPENIDE_MODULE_LAYER);
                }
            }
            return myLayerPath;
        }
        
        protected Project getProject(){
            return myProject;
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
                    (String)getProperty(NewComponentDescriptor.CD_TYPE_ID));
            tokens.put("superDescriptorClass", getSuperDescrClassNameToken());
            tokens.put("superDescriptorClassFQN", getSuperDescrClassFQNToken());
            tokens.put("prefix", 
                    (String)getProperty(NewComponentDescriptor.CC_PREFIX));
            tokens.put("canInstantiate", 
                    getProperty(NewComponentDescriptor.CD_CAN_INSTANTIATE).toString());
            tokens.put("canBeSuper", 
                    getProperty(NewComponentDescriptor.CD_CAN_BE_SUPER).toString());
            tokens.put("midpVersion", getMidpVersionToken());
            return tokens;
        }
        
        private String getMidpVersionToken(){
            Version version = (Version)getProperty(NewComponentDescriptor.CD_VERSION);
            assert version != null;
            return version.javaCodeValue();
        }
        
        private String getSuperDescrClassFQNToken(){
            String superString = (String)getProperty(NewComponentDescriptor.CD_SUPER_DESCR_CLASS);
            assert superString != null;
            if (superString.indexOf(".") == -1){
                return "";
            }
            return superString;
        }
        
        private String getSuperDescrClassNameToken(){
            String superString = (String)getProperty(NewComponentDescriptor.CD_SUPER_DESCR_CLASS);
            assert superString != null;
            if (superString.indexOf(".") == -1){
                return superString;
            }
            return superString.substring(superString.lastIndexOf(".")+1, superString.length());
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
            tokens.put("prefix", 
                    (String)getProperty(NewComponentDescriptor.CC_PREFIX));
            if ((Boolean)getProperty(NewComponentDescriptor.CP_ADD_LIB)){
                tokens.put("libraryName", 
                        (String)getProperty(NewComponentDescriptor.CP_LIB_NAME));
            }
            tokens.put("validity", getProducerValidityToken());
            return tokens;
        }
        
        private String getPaletteCategoryToken(){
            PaletteCategory category = (PaletteCategory)getProperty(
                    NewComponentDescriptor.CP_PALETTE_CATEGORY);
            assert category != null;
            return category.javaCodeValue();
        }

        private String getProducerValidityToken(){

            Boolean always = (Boolean) getProperty(
                    NewComponentDescriptor.CP_VALID_ALWAYS);
            Boolean platform = (Boolean) getProperty(
                    NewComponentDescriptor.CP_VALID_PLATFORM);
            Boolean custom = (Boolean) getProperty(
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
            
            String codeNameBase = getCodeNameBase().replace('.', '/'); // NOI18N
            File iconFile = new File(srcPath);
            String name = iconFile.getName();
            
            return codeNameBase + "/" + BaseHelper.RESOURCES + "/" + name;
        }
        
        /**
         * confogures operties in the same pkg as Producer class with values used in producers.
         * @return Set of created files, if any.
         * @throws java.io.IOException
         */
        private Set<FileObject> configureProducerBundle()
                throws IOException
        {
            FileObject prjDir = getProject().getProjectDirectory();
            String pkgPath = getProducerPkg().replace('.', '/'); // NOI18N
            String bundlePath = SRC + pkgPath + "/" +                  // NOI18N
                    CustomComponentWizardIterator.BUNDLE_PROPERTIES;

            boolean exists = isFileExist(prjDir, bundlePath);
            
            FileObject bundleFO = FileUtil.createData(prjDir, bundlePath);
            
            doUpdateProducerBundle(bundleFO);
            
            if (exists){
                return Collections.EMPTY_SET;
            } else {
                Set<FileObject> result = new LinkedHashSet<FileObject>();
                result.add(bundleFO);
                return result;
            }
        }
        
        private void doUpdateProducerBundle(FileObject bundleFO) 
                throws IOException
        {
            String prefix = 
                    (String)getProperty(NewComponentDescriptor.CC_PREFIX)+"_";
            EditableProperties ep = loadProperties(bundleFO);
            
            String nameKey = prefix + "paletteName"; // NOI18N
            String nameValue = (String)getProperty(
                    NewComponentDescriptor.CP_PALETTE_DISP_NAME);
            
            String tooltipKey = prefix + "paletteTooltip"; // NOI18N
            String tooltipValue = (String)getProperty(
                    NewComponentDescriptor.CP_PALETTE_TIP);

            ep.setProperty(nameKey, nameValue);
            ep.setProperty(tooltipKey, tooltipValue);
            
            storeProperties(bundleFO, ep);
        }
        
        /**
         * 
         * @param prjDir project FileObject
         * @param file path related to project directory
         * @return true is file with specified path exists inside project dir
         */
        private boolean isFileExist(FileObject prjDir, String path){
            File file = new File(FileUtil.toFile(prjDir), path);
            return file.exists() ? true : false;
        }
        
        private FileObject createAndRegisterLayerXml() 
                throws IOException
        {
            FileObject prjDir = getProject().getProjectDirectory();
            String cnb = getCodeNameBase().replace(".", "/");
            String layerXmlPath = cnb + "/" + LayerXmlHelper.LAYER_XML;
            
            // create layer xml file
            FileObject layerFO = LayerXmlHelper.createLayerXml(
                    prjDir, SRC + layerXmlPath);
            
            // register layer in manifest.mf
            Manifest manifest = getManifest();
            Attributes attrs = manifest.getMainAttributes();
            attrs.putValue(OPENIDE_MODULE_LAYER, layerXmlPath);
            saveManifest(manifest);
            
            return layerFO;
        }
        
        private Set<FileObject> configureLayerXml()
                throws IOException
        {
            String layerXmlPath = getLayer();
            FileObject prjDir = getProject().getProjectDirectory();
            
            FileObject layerXmlFO = null;
            if (layerXmlPath == null || !isFileExist(prjDir, layerXmlPath)){
                layerXmlFO = createAndRegisterLayerXml();
            } else {
                layerXmlFO = FileUtil.createData(prjDir, layerXmlPath);
            }
            
            try {
                Document doc = LayerXmlHelper.parseXmlDocument(layerXmlFO);
                assert doc != null;
                
                Element docRoot = doc.getDocumentElement();

                XPath xpath = XPathFactory.newInstance().newXPath();

                Node fsNode = LayerXmlHelper.goToFilesystemNode(doc, xpath, docRoot);
                Node vmdMidpNode = goToVmdMidpNode(doc, xpath, fsNode);

                Node compsNode = goToComponentsNode(doc, xpath, vmdMidpNode);
                Element cd = doc.createElement(LayerXmlHelper.LAYER_FILE);
                cd.setAttribute( LayerXmlHelper.LAYER_NAME, getCDLayerInstanceName() );
                compsNode.appendChild(cd);
                
                Node produsersNode = goToProducersNode(doc, xpath, vmdMidpNode);
                Element prod = doc.createElement(LayerXmlHelper.LAYER_FILE);
                prod.setAttribute( LayerXmlHelper.LAYER_NAME, getProducerLayerInstanceName() );
                produsersNode.appendChild(prod);
                
                LayerXmlHelper.saveXmlDocument(doc, layerXmlFO);
                
            } catch (XPathExpressionException ex) {
                ErrorManager.getDefault().notify(ex);
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
            String path = (String) getProperty(
                    NewComponentDescriptor.CP_SMALL_ICON);
            if (path == null || path.length() == 0) {
                return null;
            }
            return path;
        }

        private String getLargeIconPath() {
            String path = (String) getProperty(
                    NewComponentDescriptor.CP_LARGE_ICON);
            if (path == null || path.length() == 0) {
                return null;
            }
            return path;
        }



        private void configureDependencies() {
            try {

                FileObject prjDir = getProject().getProjectDirectory();
                FileObject projectXmlFO = FileUtil.createData(prjDir, 
                        AntProjectHelper.PROJECT_XML_PATH);

                Document doc = LayerXmlHelper.parseXmlDocument(projectXmlFO);

                XPath xpath = XPathFactory.newInstance().newXPath();

                Node confData = ProjectXmlHelper.getPrimaryConfigurationData(xpath, 
                        doc.getDocumentElement());

                Node modDeps = ProjectXmlHelper.goToModuleDependencies(doc, xpath, confData);

                ProjectXmlHelper.testAndAddDependency(doc, xpath, modDeps, 
                        VMD_MIDP_NAME, VMD_MIDP_VERSION);
                ProjectXmlHelper.testAndAddDependency(doc, xpath, modDeps, 
                        VMD_MODEL_NAME, VMD_MODEL_VERSION);
                ProjectXmlHelper.testAndAddDependency(doc, xpath, modDeps, 
                        VMD_PROPERTIES_NAME, VMD_PROPERTIES_VERSION);
                ProjectXmlHelper.testAndAddDependency(doc, xpath, modDeps, 
                        OPENIDE_UTIL_NAME, OPENIDE_UTIL_VERSION);


                ProjectXmlHelper.saveXmlDocument(doc, projectXmlFO);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (XPathExpressionException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }

        private Object getProperty(String name){
            if (myComponent == null) {
                return myComponentWizard.getProperty(name);
            } else {
                return myComponent.get(name);
            }
        }
        
        private String getCDClassName() {
            return (String)getProperty(NewComponentDescriptor.CD_CLASS_NAME);
        }
        
        private String getProducerClassName() {
            return (String) getProperty(
                    NewComponentDescriptor.CP_CLASS_NAME);
        }

        private Manifest getManifest() {
            if (myManifest == null) {
                myManifest = readManifest();
            }
            return myManifest;
        }
        
        private Manifest readManifest() {
            return ProjectManager.mutex().readAccess(new Mutex.Action<Manifest>() {

                public Manifest run() {
                    try {
                        return doReadManifest();
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                    return null;
                }
            });
        }
        
        private Manifest doReadManifest() throws IOException {
            
            Manifest manifest = null;
            FileObject manifestFO = getProject().getProjectDirectory().
                    getFileObject(MANIFEST);
            if (manifestFO != null) {
                InputStream is = manifestFO.getInputStream();
                try {
                    manifest = new Manifest(is);
                } finally {
                    is.close();
                }
            }
            return manifest;
        }

        private void saveManifest(final Manifest manifest) {
            if (manifest == null) {
                return;
            }
            ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {

                public Void run() {
                    try {
                        doSaveManifest(manifest);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                    return null;
                }
            });
        }
        
        private void doSaveManifest(Manifest manifest) throws IOException {
            FileObject manifestFO = getProject().getProjectDirectory().
                    getFileObject(MANIFEST);
            if (manifestFO != null) {
                FileLock lock = manifestFO.lock();
                try {
                    OutputStream os = manifestFO.getOutputStream(lock);
                    try {
                        manifest.write(os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        }

        private Project myProject;
        private Map<String, Object> myComponent;
        private WizardDescriptor myComponentWizard;
        
        private String myBundlePath;
        private String myLayerPath;
        private String myCodeNameBase;
        private Manifest myManifest;
        
    }
}
