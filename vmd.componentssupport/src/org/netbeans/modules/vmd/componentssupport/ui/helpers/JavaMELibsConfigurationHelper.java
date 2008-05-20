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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.text.PlainDocument;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.vmd.componentssupport.ui.wizard.CustomComponentWizardIterator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataObject;
import org.openide.text.IndentEngine;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helper class for performing new library descriptor instantiation.
 * To be used from
 * org.netbeans.modules.vmd.componentssupport.ui.wizard.CustomComponentWizardIterator
 * 
 * @author avk
 */
public class JavaMELibsConfigurationHelper extends BaseHelper{

    
    public static final String TEMPLATE_LIB_DESCR   = "libdescriptemplate.xml"; //NOI18N
    
    // library volumes
    public static final String VOLUME_CLASS         = "classpath";              //NOI18N
    public static final String VOLUME_SRC           = "src";                    //NOI18N
    public static final String VOLUME_JAVADOC       = "javadoc";                //NOI18N

    // where to copy library content
    private static final String LIB_EXTRACT_RELEASE = "release/";               //NOI18N
    public static final String LIB_EXTRACT_PATH_LIBS = LIB_EXTRACT_RELEASE+"libs/";  //NOI18N
    public static final String LIB_EXTRACT_SOURCES  = LIB_EXTRACT_RELEASE+"sources/"; //NOI18N
    public static final String LIB_EXTRACT_DOCS     = LIB_EXTRACT_RELEASE+"docs/"; //NOI18N
    
    // browsing layer.xml dom tree
    //// names
    private static final String LAYER_FILESYSTEM    = "filesystem";             //NOI18N
    private static final String LAYER_FOLDER        = "folder";                 //NOI18N
    private static final String LAYER_NAME          = "name";                   //NOI18N
    private static final String LAYER_URL           = "url";                    //NOI18N
    private static final String LAYER_RESOURCE      = "resource";               //NOI18N
    //// tags identifiers (e.g. attribute values)
    private static final String LAYER_TAG_LIBRARIES_ROOT 
                                        = "org-netbeans-api-project-libraries"; //NOI18N
    private static final String LAYER_TAG_LIBRARIES = "Libraries";              //NOI18N
    private static final String LAYER_TAG_RESOURCE_OPEN 
                                                    = "<"+LAYER_RESOURCE+">";   //NOI18N
    private static final String LAYER_TAG_RESOURCE_CLOSE 
                                                    = "</"+LAYER_RESOURCE+">";  //NOI18N
    //// xpaths to tags
    private static final String LAYER_XPATH_FILESYSTEM 
                                                    = "/"+LAYER_FILESYSTEM;     //NOI18N
    private static final String LAYER_XPATH_LIBRARIES_ROOT 
                        = "./folder[@name=\""+LAYER_TAG_LIBRARIES_ROOT+"\"]";    //NOI18N
    private static final String LAYER_XPATH_LIBRARIES 
                        = "./folder[@name=\""+LAYER_TAG_LIBRARIES+"\"]";         //NOI18N
    
    
    private static final String TPL_ENGINE          = "freemarker";             //NOI18N
    private static final String TPL_TOKEN_NAME      = "NAME";                   //NOI18N
    private static final String TPL_TOKEN_BUNDLE    = "BUNDLE";                 //NOI18N
    private static final String TPL_TOKEN_CLASSPATH = "CLASSPATH";              //NOI18N
    private static final String TPL_TOKEN_SRC       = "SRC";                    //NOI18N
    private static final String TPL_TOKEN_JAVADOC   = "JAVADOC";                //NOI18N
    // TODO check if these are really used (copied together with code)
    private static final String TPL_TOKEN_NAME_LOWER = "name";                  //NOI18N
    private static final String TPL_TOKEN_USER      = "user";                   //NOI18N
    private static final String TPL_TOKEN_DATE      = "date";                   //NOI18N
    private static final String TPL_TOKEN_TIME      = "time";                   //NOI18N
    private static final String TPL_TOKEN_NAME_AND_EXT = "nameAndExt";          //NOI18N
    private static final String TPL_TOKEN_ENCODING  = "encoding";               //NOI18N

    
    public static final String SYSTEM_USER          = "user.name"; //NOI18N
    public static final String FILE_PROTOCOL        = "file"; //NOI18N
    
    
    /**
     * Adds library descriptor generated from template "libdescripttemplate.xml"
     * and generates library xml descriptor, changes Bundle.properties, layer.xml.
     * Copies archives and folders listed in library descriptor into project directory.   
     * 
     * @param project in which new library should be configured
     * @param wizard with new library configuration data
     * @throws java.io.IOException
     */
    public static void configureJavaMELibs(Project project, WizardDescriptor wizard) 
            throws IOException 
    {
        
        List<Library> libraries = (List<Library>)wizard.getProperty(
                CustomComponentWizardIterator.LIBRARIES);
        List<String> libNames = (List<String>)wizard.getProperty(
                CustomComponentWizardIterator.LIB_NAMES);
        List<String> libDisplayNames = (List<String>)wizard.getProperty(
                CustomComponentWizardIterator.LIB_DISPLAY_NAMES);

        if (!haveLibsToConfigure(libraries, libNames, libDisplayNames)){
            return; // nothing to do
        }
                
        // assert to check that all necessary props are in provided WizardDescriptor
        assert wizard.getProperty(CustomComponentWizardIterator.BUNDLE_PATH) != null
               && wizard.getProperty(CustomComponentWizardIterator.LAYER_PATH) != null
               && wizard.getProperty(CustomComponentWizardIterator.CODE_BASE_NAME) != null
                : "Some properties necessary for lib configuration are absent"; //NOI18N
        
        Iterator<Library> itLib = libraries.iterator();
        Iterator<String> itName = libNames.iterator();
        Iterator<String> itDName = libDisplayNames.iterator();
        while (itLib.hasNext()) {
            Library library = itLib.next();
            String name = itName.next();
            String displayName = itDName.next();
            
            configureLibrary(library, name, displayName, project, wizard);
        }
    }

    private static void configureLibrary(Library library, String name, String displayName, 
            Project project, WizardDescriptor wizard) 
            throws IOException
    {
        FileObject template = getTemplate(TEMPLATE_LIB_DESCR);
        // get tokens for library xml
        Map<String, String> tokens = getTokens(library, name, project, wizard);
        // lib xml
        configureLibXml(template, tokens, name, project, wizard);
        // add lib xml to layer.xml
        configureLayerXml(name, project, wizard);
        // add library display name to the Bundle.properties
        configureBundle(name, displayName, project, wizard);
        
    }

    /**
     * Creates library descriptor XML file
     * @param template library descriptor XML file template in freemarker format.
     * @param tokens to be replaced in template.
     * @param libName library name
     * @param project
     * @param wizard WizardDescriptor
     * @return path to created xml file relative to project directory
     * @throws java.io.IOException
     */
    private static void configureLibXml(FileObject template, 
            Map<String, String> tokens, String libName, 
            Project project, WizardDescriptor wizard) 
            throws IOException 
    {

        String codeNameBase = ((String)wizard.getProperty(
                CustomComponentWizardIterator.CODE_BASE_NAME)).replace('.', '/'); //NOI18N
        String xmlPath = SRC + codeNameBase + "/" + libName + XML_EXTENSION; //NOI18N
        
        FileObject prjDir = project.getProjectDirectory();
        FileObject targetFO = FileUtil.createData(prjDir, xmlPath);
        if (template != null) {
            if (tokens == null) {
                copyByteAfterByte(template, targetFO);
            } else {
                copyAndSubstituteTokens(template, targetFO, tokens);
            }
        }
    }
                    
    /**
     * adds record with library display name to bundle properties
     * @param libName library name - will be used as property name
     * @param libDisplayName library display name - will be used as property value
     * @param project Project is used just to know it's directry 
     * (to find bundle properties file inside)
     * @param wizard WizardDescriptor with library configuration data 
     * (used to get relative path to bundle properties file)
     * @throws java.io.IOException
     */
    private static void configureBundle(String libName, String libDisplayName, 
            Project project, WizardDescriptor wizard) 
            throws IOException 
    {
        String bundlePath = SRC + 
                (String)wizard.getProperty(CustomComponentWizardIterator.BUNDLE_PATH);
        FileObject prjDir = project.getProjectDirectory();
        FileObject bundleFO = FileUtil.createData(prjDir, bundlePath);
        EditableProperties ep = loadProperties(bundleFO);
        ep.setProperty(libName, libDisplayName);
        storeProperties(bundleFO, ep);
    }

    private static void configureLayerXml(String libName,
            Project project, WizardDescriptor wizard) 
            throws IOException 
    {
        String codeNameBase = ((String)wizard.getProperty(
                CustomComponentWizardIterator.CODE_BASE_NAME)).replace('.', '/'); //NOI18N
        String layerXmlPath = SRC + codeNameBase + "/" + LAYER_XML; //NOI18N
        FileObject prjDir = project.getProjectDirectory();
        FileObject layerXmlFO = FileUtil.createData(prjDir, layerXmlPath);
        

        // add tag <file name="_library_name_.xml" value="_library_name_.xml" />
        // to org-netbeans-api-project-libraries/Libraries
        /////////////////////////////
        try {
            Document doc = parseXmlDocument(layerXmlFO);
            Element docRoot = doc.getDocumentElement();

            XPath xpath = XPathFactory.newInstance().newXPath();

            Node fsNode = goToFilesystemNode(doc, xpath, docRoot);
            Node libsAreaNode = goToLibsRootNode(doc, xpath, fsNode);
            Node libsNode = goToLibsNode(doc, xpath, libsAreaNode);

            // create record for new library descriptor
            Element library = doc.createElement(LAYER_FOLDER);
            library.setAttribute(LAYER_NAME, libName + XML_EXTENSION);
            library.setAttribute(LAYER_URL, libName + XML_EXTENSION);
            libsNode.appendChild(library);
            
            saveXmlDocument(doc, layerXmlFO);
        }
        catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }

    private static Node goToFilesystemNode(Document doc, XPath xpath, Node parent)
            throws XPathExpressionException 
    {
        String expression = LAYER_XPATH_FILESYSTEM;
        Node fsNode = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
        if (fsNode == null) {
            fsNode = doc.createElement(LAYER_FILESYSTEM);
            parent.appendChild(fsNode);
        }
        return fsNode;
    }
    
    private static Node goToLibsRootNode(Document doc, XPath xpath, Node parent) 
            throws XPathExpressionException
    {
        String expression = LAYER_XPATH_LIBRARIES_ROOT;
        Node libsAreaNode = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
        if (libsAreaNode == null) {
            Element libsAreaElement = doc.createElement(LAYER_FOLDER);
            libsAreaElement.setAttribute(LAYER_NAME, LAYER_TAG_LIBRARIES_ROOT);
            parent.appendChild(libsAreaElement);
            libsAreaNode = libsAreaElement;
        }
        return libsAreaNode;

    }
            
    private static Node goToLibsNode(Document doc, XPath xpath, Node parent)
            throws XPathExpressionException 
    {
        String expression = LAYER_XPATH_LIBRARIES;
        Node libsNode = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
        if (libsNode == null) {
            Element libsElement = doc.createElement(LAYER_FOLDER);
            libsElement.setAttribute(LAYER_NAME, LAYER_TAG_LIBRARIES);
            parent.appendChild(libsElement);
            libsNode = libsElement;
        }
        return libsNode;
    }
    
    private static Document parseXmlDocument(FileObject xmlFO) 
            throws SAXException, IOException, FileNotFoundException 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = xmlFO.getInputStream();
        try {
            FileUtil.copy(in, baos);
        } finally {
            in.close();
        }
        return XMLUtil.parse(new InputSource(
                new ByteArrayInputStream(baos.toByteArray())), 
                false, false, null, null);
    }

    private static void saveXmlDocument(Document doc, FileObject xmlFO) 
            throws IOException 
    {
        OutputStream out = xmlFO.getOutputStream();
        try {
            XMLUtil.write(doc, out, UTF_8);
        } finally {
            out.close();
        }
    }

    /*
     * some parts of code were copied from 
     * org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor.CreatedModifiedFilesProvider
     * and org.netbeans.modules.apisupport.project.CreatedModifiedFilesFactory.
     */
    private static Map<String, String> getTokens(Library library, String libName,
            Project project, WizardDescriptor wizard)
            throws IOException 
    {
        Map<String, String> retval = new HashMap<String, String>();
        
        retval.put(TPL_TOKEN_NAME, libName);
        String bundle = ((String)wizard.getProperty(
                CustomComponentWizardIterator.BUNDLE_PATH)).replace('/','.'); //NOI18N
        retval.put(TPL_TOKEN_BUNDLE, bundle );
        
        Iterator<URL> it = library.getContent(VOLUME_CLASS).iterator();
        retval.put(TPL_TOKEN_CLASSPATH, getTokenSubstitution(it, LIB_EXTRACT_PATH_LIBS, libName, project, wizard));
        
        it = library.getContent(VOLUME_SRC).iterator();
        retval.put(TPL_TOKEN_SRC, getTokenSubstitution(it, LIB_EXTRACT_SOURCES, libName, project, wizard));
        
        it = library.getContent(VOLUME_JAVADOC).iterator();
        retval.put(TPL_TOKEN_JAVADOC, getTokenSubstitution(it, LIB_EXTRACT_DOCS, libName, project, wizard));
        
        return retval;
    }

    private static String getTokenSubstitution(Iterator<URL> it, String pathPrefix, 
            String libName, Project project, WizardDescriptor wizard)
            throws IOException 
    {
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            URL originalURL = it.next();
            String archiveName;
            archiveName = addArchiveToCopy(originalURL, pathPrefix, project, libName);
            if (archiveName != null) {
                String codeNameBase = (String)wizard.getProperty(
                        CustomComponentWizardIterator.CODE_BASE_NAME);
                String urlToString = transformURL(codeNameBase, pathPrefix, archiveName);
                sb.append(LAYER_TAG_RESOURCE_OPEN);
                sb.append(urlToString);
                sb.append(LAYER_TAG_RESOURCE_CLOSE);
                if (it.hasNext()) {
                    sb.append("\n");//NOI18N
                }
            }
        }
        return sb.toString();
    }
    
    /** 
     * copies lirary's archive file to project dir.
     * @returns archive name or temporarily null 
     * cause there is no zip support for file protocol  
     */
    private static String addArchiveToCopy(URL originalURL, 
            String pathPrefix, Project project, String libName) 
            throws IOException 
    {
        String archName = null;
        
        URL archivURL = FileUtil.getArchiveFile(originalURL);
        if (archivURL != null && FileUtil.isArchiveFile(archivURL)) {
            FileObject archiv = URLMapper.findFileObject(archivURL);
            assert archiv != null : archivURL; // #129617
            archName = archiv.getNameExt();
            doCopyFile(project, pathPrefix + archName, archiv, null);
        } else {
            if (FILE_PROTOCOL.equals(originalURL.getProtocol())) {
                FileObject folderToZip;
                folderToZip = URLMapper.findFileObject(originalURL);
                if (folderToZip != null) {
                    archName = libName + ZIP_EXTENSION;
                    pathPrefix += archName;
                    doZipAndCopyFolder( project, pathPrefix+archName, folderToZip);
                }
            }
        }
        return archName;
    }
    
    private static void doCopyFile(Project project, String path, 
            FileObject content, Map<String, String> tokens) 
            throws IOException
    {
        FileObject target = FileUtil.createData(project.getProjectDirectory(), path);
        if (tokens == null) {
            copyByteAfterByte(content, target);
        } else {
            copyAndSubstituteTokens(content, target, tokens);
        }
    }
    
    private static void doZipAndCopyFolder(Project project, 
            String target, FileObject source) 
            throws IOException
    {
            Collection<? extends FileObject> files = Collections.list(source.getChildren(true));
            if (files.isEmpty()) return;
            FileObject prjDir = project.getProjectDirectory();
            assert prjDir != null;
            
            FileObject zipedTarget  = prjDir.getFileObject(target);
            if (zipedTarget == null) {
                zipedTarget = FileUtil.createData(prjDir, target);
            }
            
            assert zipedTarget != null;
            FileLock fLock = null;
            OutputStream os = null;
            
            try {
                fLock = zipedTarget.lock();
                os = zipedTarget.getOutputStream(fLock);
                createZipFile(os, source, files);
            } finally {
                if (os != null) {
                    os.close();
                }
                
                if (fLock != null) {
                    fLock.releaseLock();
                }
            }
    }
    
    private static void createZipFile(OutputStream target, FileObject root, 
            Collection<? extends FileObject> files) 
            throws IOException 
    {
        ZipOutputStream str = null;
        try {
            str = new ZipOutputStream(target);
            for (FileObject fo : files) {
                String relativePath = FileUtil.getRelativePath(root, fo);
                if (fo.isFolder()) {
                    if (fo.getChildren().length > 0) {
                        continue;
                    } else if (!relativePath.endsWith("/")) { //NOI18N
                        relativePath += "/"; //NOI18N
                    }
                }
                ZipEntry entry = new ZipEntry(relativePath);
                str.putNextEntry(entry);
                if (fo.isData()) {
                    InputStream in = null;
                    try {
                        in = fo.getInputStream();
                        FileUtil.copy(in, str);
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
                str.closeEntry();
            }
        } finally {
            if (str != null) {
                str.close();
            }
        }
    }

    
    private static void copyAndSubstituteTokens(FileObject content, FileObject target, Map<String,String> tokens) throws IOException {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByName(TPL_ENGINE);
        assert engine != null : scriptEngineManager.getEngineFactories();
        Map<String,Object> bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        String basename = target.getName();
        for (CreateFromTemplateAttributesProvider provider : Lookup.getDefault().lookupAll(CreateFromTemplateAttributesProvider.class)) {
            DataObject d = DataObject.find(content);
            Map<String,?> map = provider.attributesFor(d, d.getFolder(), basename);
            if (map != null) {
                bindings.putAll(map);
            }
        }
        bindings.put(TPL_TOKEN_NAME_LOWER, basename.replaceFirst("\\.[^./]+$", "")); // NOI18N
        bindings.put(TPL_TOKEN_USER, System.getProperty(SYSTEM_USER)); 
        Date d = new Date();
        bindings.put(TPL_TOKEN_DATE, DateFormat.getDateInstance().format(d)); 
        bindings.put(TPL_TOKEN_TIME, DateFormat.getTimeInstance().format(d)); 
        bindings.put(TPL_TOKEN_NAME_AND_EXT, target.getNameExt()); 
        bindings.putAll(tokens);
        Charset targetEnc = FileEncodingQuery.getEncoding(target);
        Charset sourceEnc = FileEncodingQuery.getEncoding(content);
        bindings.put(TPL_TOKEN_ENCODING, targetEnc.name());
        Writer w = new OutputStreamWriter(target.getOutputStream(), targetEnc);
        try {
            IndentEngine format = IndentEngine.find(content.getMIMEType());
            if (format != null) {
                PlainDocument doc = new PlainDocument();
                doc.putProperty(PlainDocument.StreamDescriptionProperty, content);
                w = format.createWriter(doc, 0, w);
            }
            engine.getContext().setWriter(w);
            engine.getContext().setAttribute(FileObject.class.getName(), content, ScriptContext.ENGINE_SCOPE);
            engine.getContext().setAttribute(ScriptEngine.FILENAME, content.getNameExt(), ScriptContext.ENGINE_SCOPE);
            Reader is = new InputStreamReader(content.getInputStream(), sourceEnc);
            try {
                engine.eval(is);
            } catch (ScriptException x) {
                throw (IOException) new IOException(x.toString()).initCause(x);
            } finally {
                is.close();
            }
        } finally {
            w.close();
        }
    }
    
    private static String transformURL(final String cnb, final String pathPrefix, final String archiveName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append("jar:nbinst://").append(cnb).append("/");//NOI18N
        sb.append(pathPrefix).append(archiveName).append("!/");//NOI18N
        
        return sb.toString();
    }
    
    private static boolean haveLibsToConfigure(List<Library> libraries, 
            List<String> libNames, List<String> libDisplayNames)
    {
        if (libraries == null || libNames == null || libDisplayNames == null){
            return false;
        }
        //check that data is consistent
        assert ( libraries.size() == libNames.size() )
                && ( libraries.size() == libDisplayNames.size() )
                && ( libDisplayNames.size() == libNames.size() )
                : "libraries data is not consistent";
                
        if (libraries.size() == 0){
            return false;
        }
        
        return true;
    }

    
}
