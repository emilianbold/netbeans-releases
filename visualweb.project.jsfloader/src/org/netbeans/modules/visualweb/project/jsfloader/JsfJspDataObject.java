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


package org.netbeans.modules.visualweb.project.jsfloader;


import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.core.jsploader.api.TagLibParseCookie;
import org.netbeans.modules.web.core.jsploader.api.TagLibParseFactory;

import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.xml.XMLUtil;
import org.openide.cookies.ViewCookie;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.netbeans.modules.visualweb.api.insync.InSyncService;
import org.netbeans.modules.visualweb.api.insync.JsfJspDataObjectMarker;
import org.netbeans.modules.visualweb.project.jsf.api.JsfDataObjectException;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/** Object that represents one JSP file which has corresponding java file.
 *
 * @author  Peter Zavadsky
 * @author  Tor Norbye (the strange designer stuff)
 * @author  Mark Dey (the strange start page stuff)
 */
public class JsfJspDataObject extends MultiDataObject
implements CookieSet.Factory, JsfJspDataObjectMarker {


    static final long serialVersionUID =8354927561693097159L;

    static final String JSF_ATTRIBUTE = "jsfjsp"; // NOI18N
    private static final String JSP_ICON_BASE = "org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject.png"; // NOI18N
    static final String PROP_ENCODING = "encoding"; // NOI18N
    private static final String PROP_CUSTOM_TEMPLATE = "custom-template"; // NOI18N
    private static final String DEFAULT_ENCODING = "UTF-8"; // NOI18N
    
    private transient OpenEdit openEdit;
    private transient Lookup currentLookup;

    /** New instance.
    * @param pf primary file object for this data object
    * @param loader the data loader creating it
    * @exception DataObjectExistsException if there was already a data object for it
    */
    public JsfJspDataObject(FileObject primaryFile, UniFileLoader loader) throws DataObjectExistsException {
        super(primaryFile, loader);
        CookieSet set = getCookieSet();
        
        set.add(OpenCookie.class, this);
        set.add(EditCookie.class, this);
        set.add(JsfJspEditorSupport.class, this);
        set.add(ViewSupport.class, this);
        set.add(TagLibParseCookie.class, this);
        
        createLookup();
    }

    private void createLookup() {
        Lookup noEncodingLookup = getCookieSet().getLookup();

        org.netbeans.spi.queries.FileEncodingQueryImplementation feq = new org.netbeans.spi.queries.FileEncodingQueryImplementation() {

            public Charset getEncoding(FileObject file) {
                assert file != null;
                assert file.equals(getPrimaryFile());

                String charsetName = getFileEncoding();
                try {
                    return Charset.forName(charsetName);
                } catch (IllegalCharsetNameException ichse) {
                    //the jsp templates contains the ${encoding} property 
                    //so the ICHNE is always thrown for them, just ignore
                    Boolean template = (Boolean)file.getAttribute("template");//NOI18N
                    if(template == null || !template.booleanValue()) {
                        Logger.getLogger("global").log(Level.INFO, "Detected illegal charset name in file " + file.getNameExt() + " (" + ichse.getMessage() + ")");
                    }
                } catch (UnsupportedCharsetException uchse) {
                    Logger.getLogger("global").log(Level.INFO, "Detected unsupported charset name in file " + file.getNameExt() + " (" + uchse.getMessage() + ")");
                }

                return null;
            }
        };

        currentLookup = new ProxyLookup(noEncodingLookup, Lookups.singleton(feq));
    }
    
    @Override
    protected Node createNodeDelegate () {
        DataNode n = new JsfJspDataNode(this, Children.LEAF);
        n.setIconBaseWithExtension(JSP_ICON_BASE);
        return n;
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx("org.netbeans.modules.visualweb.project.jsfloader.JsfJspDataLoader" + ".Obj"); // NOI18N
    }

    /** Creates new Cookie */
    public <T extends Node.Cookie> T createCookie(Class<T> klass) {
        if (OpenCookie.class.equals(klass)
        || EditCookie.class.equals(klass)) {
            if(openEdit == null) {
                openEdit = new OpenEdit();
            }
            return klass.cast(openEdit);
        } else if (JsfJspEditorSupport.class.isAssignableFrom(klass)) {
            return klass.cast(getJsfJspEditorSupport());
        } else if (ViewSupport.class.isAssignableFrom(klass)) {
            return klass.cast(new ViewSupport(getPrimaryEntry()));
        } else if (TagLibParseCookie.class.isAssignableFrom(klass)) {
            return klass.cast(TagLibParseFactory.createTagLibParseCookie(getPrimaryFile()));
        } else {
            return null;
        }
    }

    // Package accessibility for JsfJspEditorSupport:
    CookieSet getCookieSet0() {
        return getCookieSet();
    }

    @Override
    public Lookup getLookup() {
        return currentLookup;
    }

    /** Gets the superclass cookie, without hacking save cookie. */
    <T extends Node.Cookie> T getPureCookie(Class<T> clazz) {
        return super.getCookie(clazz);
    }

    /** Overrides behaviour to provide compound save cookie. */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> clazz) {
        if(clazz == SaveCookie.class){
            FileObject primaryJavaFileObject = Utils.findJavaForJsp(getPrimaryFile());
            if(primaryJavaFileObject != null && primaryJavaFileObject.isValid()) {
                SaveCookie jspSaveCookie = (SaveCookie)super.getCookie(clazz);
                JsfJavaDataObject jsfJavaDataObject = Utils.findCorrespondingJsfJavaDataObject(getPrimaryFile(), false);
                SaveCookie javaSaveCookie;
                if(jsfJavaDataObject == null) {
                    javaSaveCookie = null;
                } else {
                    javaSaveCookie = (SaveCookie)jsfJavaDataObject.getPureCookie(clazz);
                }
                
                if(jspSaveCookie == null && javaSaveCookie == null) {
                    return null;
                } else {
                    return clazz.cast(new CompoundSaveCookie(jspSaveCookie, javaSaveCookie));
                }
            }
        } else {
            // XXX NB #80853 Fix the cookie creation when referred by impl classes.
            if (TagLibParseCookie.class.isAssignableFrom(clazz)) {
                return clazz.cast(super.getCookie(TagLibParseCookie.class));
            }
        }
        
        return super.getCookie(clazz);
    }
    
    private transient JsfJspEditorSupport jsfJspEditorSupport;

    private static final Object LOCK_JSF_JSP_EDITOR = new Object();

    
    private JsfJspEditorSupport getJsfJspEditorSupport() {
        synchronized(LOCK_JSF_JSP_EDITOR) {
            if(jsfJspEditorSupport == null) {
                jsfJspEditorSupport = new JsfJspEditorSupport(this);
            }

            return jsfJspEditorSupport;
        }
    }
    
    private class OpenEdit implements OpenCookie, EditCookie {
        public void open() {
            String attr = getAttribute();
            if ((attr != null && attr.equals(JsfJavaEditorSupport.MV_ID_DESIGNER)) || attr == null) {
                getJsfJspEditorSupport().openDesigner();
            }else {
                getJsfJspEditorSupport().editJsp();
            }
        }
        public void edit() {
            String attr = getAttribute();
            if (attr != null && attr.equals(JsfJavaEditorSupport.MV_ID_DESIGNER)) {
                getJsfJspEditorSupport().openDesigner();
            } else {
                getJsfJspEditorSupport().editJsp();
            }
        }
        
        private String getAttribute() {
            try {
                String attr = (String)getPrimaryFile().getAttribute(OpenEditOverride.MULTIVIEW_ATTRIBUTE);
                getPrimaryFile().setAttribute(OpenEditOverride.MULTIVIEW_ATTRIBUTE, null);
                return attr;
            }catch (IOException ex) {
            }
            return null;
        }
        
    }
    
    
    private static final class ViewSupport implements ViewCookie {
        /** entry */
        private MultiDataObject.Entry primary;
        
        /** Constructs new ViewSupport */
        public ViewSupport(MultiDataObject.Entry primary) {
            this.primary = primary;
        }
        
         public void view() {
             try {
                 HtmlBrowser.URLDisplayer.getDefault().showURL(primary.getFile ().getURL ());
             } catch(FileStateInvalidException fsie) {
                 ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fsie);
             }
         }
    }

    // this is just a flag for obtaining encoding at first time.
    private boolean isEncodingRetrieved = false;
    
    public String getFileEncoding() {
        if (!isEncodingRetrieved){
            updateFileEncoding(false);
            isEncodingRetrieved = true;
        }
        String retrievedEncoding = (String)getPrimaryFile().getAttribute(PROP_ENCODING);
        retrievedEncoding = retrievedEncoding != null ? retrievedEncoding : DEFAULT_ENCODING;
        
        return retrievedEncoding ;
    }
    
    void updateFileEncoding(boolean fromEditor) {
        // VW templates are not valid JSP files, so skip the encoding step
        if (isTemplate()) {
            return;
        }
        
        TagLibParseCookie tlps = getCookie(TagLibParseCookie.class);
        if (tlps != null) {
            String encoding = tlps.getOpenInfo(true, fromEditor).getEncoding();
            try {
                getPrimaryFile().setAttribute(PROP_ENCODING, encoding);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }
    
    
    private static final ThreadLocal<Boolean> pureCopy = new ThreadLocal<Boolean>();
    
    /** Copies only this object without touching the corresponding jsf jsp one.
     * Used when copying originated form corresponding file. */
    void pureCopy(DataFolder folder) throws IOException {
        try {
            pureCopy.set(Boolean.TRUE);
            copy(folder);
        } finally {
            pureCopy.set(Boolean.FALSE);
        }
    }
    
    private class MarkupVisitor {    	
    	private String oldName;
    	private String newName;
    	
    	MarkupVisitor(String oldName, String newName) {
    		this.oldName = oldName;
    		this.newName = newName;
    	}

        public void apply(org.w3c.dom.Node node) {
            visit(node);
            NamedNodeMap atts = node.getAttributes();
            if (atts != null) {
                int attc = atts.getLength();
                for (int i = 0; i < attc; i++)
                    apply(atts.item(i));
            }
            NodeList kids = node.getChildNodes();
            int kidc = kids.getLength();
            for (int i = 0; i < kidc; i++)
                apply(kids.item(i));
        }
        
        private void visit(org.w3c.dom.Node node)
        {
        	if (node.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE) {        		
                String attrValue = node.getNodeValue();
                String newval = update(attrValue);
                if (newval != null)
                    node.setNodeValue(newval);
            }
        }
        
        private String update(String attrValue) {
            if (attrValue.startsWith("#{") && attrValue.endsWith("}")) {
                return "${pound}" + updateName(attrValue.substring(1));
            }else {
                return null;
            }
        }
        
        
        private String updateName(String attrValue) {
            if (attrValue.startsWith("{" + oldName + ".") && attrValue.endsWith("}")) {  //NOI18N
            	int dotAt = attrValue.indexOf(".");
            	if (dotAt != -1) {
                    String tail = attrValue.substring(dotAt, attrValue.length() - 1);  // everything to the right
                    StringBuffer buf = new StringBuffer();
                    buf.append("{");
                    buf.append(newName);
                    buf.append(tail);
                    buf.append("}");  //NOI18N
                    return buf.toString();                    
            	}                
            }
            return attrValue;
        }
    }
    
    /** Handles copy. Handles also copy of corresponding jsf jsp file. */
    @Override
    protected DataObject handleCopy(DataFolder folder) throws IOException {
        if(pureCopy.get() == Boolean.TRUE) {
            return super.handleCopy(folder);
        } else {
            FileObject javaFile = Utils.findJavaForJsp(getPrimaryFile());
            if(javaFile == null) {
                throw new JsfDataObjectException("Can't find java file for " + this);
            }

            DataObject dataObject = super.handleCopy(folder);
            boolean doNormalEventNotify = false;
            
            // fix the name of the PageBean to __NAME__ if the destination is a Template folder.
            if (folder.getPrimaryFile().getFileSystem().isDefault()) {
                FileObject primaryFileObject = dataObject.getPrimaryFile();
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                    Document document = documentBuilder.parse(primaryFileObject.getInputStream());
                    String beanName = Utils.getBeanNameForJsp(getPrimaryFile());
                    if (beanName == null) {
                        throw new JsfDataObjectException("Got null bean name for " + getPrimaryFile());
                    }
                    MarkupVisitor markupVisitor = new MarkupVisitor(beanName, "${folder}${name}"); // NOI18N
                    markupVisitor.apply(document);
                    FileLock lock = primaryFileObject.lock();
                    try {
                        OutputStream os = primaryFileObject.getOutputStream(lock);
                        PrintStream ps = new PrintStream(os);
                        ps.println("<#assign pound = '#'>"); // NOI18N
                        XMLUtil.write(document, os, getFileEncoding());  // NOI18N
                    } finally {
                        lock.releaseLock();
                    }

                    // set required attributes
                    primaryFileObject.setAttribute(PROP_ENCODING, getFileEncoding());
                    primaryFileObject.setAttribute(DataObject.PROP_TEMPLATE, Boolean.TRUE);
                    primaryFileObject.setAttribute("javax.script.ScriptEngine", "freemarker"); // NOI18N
                    
                    // set attribute for JsfJspTemplateEncodingQueryImplementation for custom templates
                    primaryFileObject.setAttribute(PROP_CUSTOM_TEMPLATE, Boolean.TRUE);
                    
                    try {
                        Class iteratorClass = Thread.currentThread().getContextClassLoader().loadClass("org.netbeans.modules.visualweb.project.jsf.ui.PageIterator"); //NOI18N
                        Method m = iteratorClass.getMethod("createWebFormIterator"); // NOI18N
                        Object templateIterator = m.invoke(null);
                        primaryFileObject.setAttribute("templateWizardIterator", templateIterator); // NOI18N
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (ParserConfigurationException e) {
                    throw new JsfDataObjectException("Parser Configuration Exception : " + e.getMessage() + " while processing " + primaryFileObject);
                } catch (SAXException e) {
                    throw new JsfDataObjectException("Parsing Exception : " + e.getMessage() + " while processing " + primaryFileObject);
                }
            } else {
            	// do the normal event notification
            	doNormalEventNotify = true;
            }

            try {
                DataObject javaDataObject = DataObject.find(javaFile);
                if(javaDataObject instanceof JsfJavaDataObject) {
                    FileObject javaFolder = Utils.findJavaFolderForJsp(dataObject.getPrimaryFile());
                    DataFolder javaDataFolder = DataFolder.findFolder(javaFolder);
                    ((JsfJavaDataObject)javaDataObject).pureCopy(javaDataFolder);
                }
            } catch(DataObjectNotFoundException dnfe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
            }

            if (doNormalEventNotify) {
                // invalidate the JspDataObject (created because the backing java had not been created)
                // and pick up the new JsfJspDataObject
                try {
                    FileObject fo = dataObject.getPrimaryFile();
                    dataObject.setValid(false);
                    dataObject = DataObject.find(fo);
                }catch (PropertyVetoException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    return dataObject;
                }
                InSyncService.getProvider().copied((JsfJspDataObjectMarker) this, (JsfJspDataObjectMarker) dataObject);
            }
            return dataObject;
        }
    }

    @Override
    protected void handleDelete() throws IOException {
        boolean isTemplate = this.isTemplate();
        FileObject fo = getPrimaryFile();
        super.handleDelete();
        
        // delete corresponding template .java
        if (isTemplate) {
            FileObject javaFO = Utils.findJavaForJsp(fo);
            DataObject javaTemplate = DataObject.find(javaFO);
            if (javaTemplate != null) {
                try {
                    javaTemplate.delete();
                }catch (IOException ex) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, 
                            "Could not delete java template: " + javaFO.getNameExt()); // NOI18N
                }
            }
        }
    }
    
    /** Handles create from template. Also handles creating from template of corresponding java file. */
    @Override
    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        DataObject result = null;
        try {
            JsfJspDataLoader.jspTemplateCreation.set(Boolean.TRUE);
            
            // freemarker-based template encoding handling
            if (Boolean.TRUE.equals(this.getPrimaryFile().getAttribute(PROP_CUSTOM_TEMPLATE))) {
                JsfJspTemplateEncodingQueryImplementation.TEMPLATE_FILEOBJECT.set(this.getPrimaryFile());
            }
            
            result = super.handleCreateFromTemplate(df, name);
        } finally {
            JsfJspDataLoader.jspTemplateCreation.set(Boolean.FALSE);
            
            if (Boolean.TRUE.equals(this.getPrimaryFile().getAttribute(PROP_CUSTOM_TEMPLATE))) {
                JsfJspTemplateEncodingQueryImplementation.TEMPLATE_FILEOBJECT.remove();
            }
        }
        
        result.getPrimaryFile().setAttribute(JSF_ATTRIBUTE, Boolean.TRUE);
        FileObject backingTargetFileObjectFolder = Utils.findJavaFolderForJsp(result.getPrimaryFile());
        if (backingTargetFileObjectFolder == null) {
            throw new JsfDataObjectException("Can't find corresponding java folder for " + result); // NOI18N
        }
        DataFolder backingTargetFolder = (DataFolder)DataObject.find(backingTargetFileObjectFolder);
        // Find the backing file template
        FileObject javaTemplateFile = Utils.findJavaForJsp(getPrimaryFile());
        if (javaTemplateFile == null) {
            throw new JsfDataObjectException("Can't find java file template for jsp template " + this);  // NOI18N
        }
        DataObject backingTemplate = DataObject.find(javaTemplateFile);
        if(backingTargetFileObjectFolder.getFileObject(result.getName(), backingTemplate.getPrimaryFile().getExt()) != null) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, "\ndesigner:JsfJspDataObject#handleCreateFromTemplate: The java file " // NOI18N
                    + result.getName() + "." + backingTemplate.getPrimaryFile().getExt()
                    + " was created by somebody else. It is not necessary now, fix it!"); // NOI18N
        } else {
            DataObject newBackingJava = null;
            try {
                FileObject resultFileObject = result.getPrimaryFile();
                Project project = FileOwnerQuery.getOwner(resultFileObject);
                if (project == null) {                    
                    newBackingJava = backingTemplate.createFromTemplate(backingTargetFolder, result.getName());
                } else {
                    Map<String, String> templateParameters = new HashMap<String, String>();
                    Object newProject = project.getProjectDirectory().getAttribute("NewProject"); //NOI18N
                    if(newProject instanceof Boolean && (Boolean)newProject) {
                        templateParameters.put("creatingProject", "true"); //NOI18N
                    }
                    templateParameters.put("j2eePlatformVersion", JsfProjectUtils.getJ2eePlatformVersion(project)); //NOI18N
                    templateParameters.put("sourceLevel", JsfProjectUtils.getSourceLevel(project)); //NOI18N
                    newBackingJava = backingTemplate.createFromTemplate(backingTargetFolder, result.getName(), templateParameters);
                }
            }catch (Exception ex) {
                // XXX NB issue #113284
                ex.printStackTrace();
            }
            
            try {
                
                // XXX NB issue #81746.
                if (newBackingJava != null) {
                    newBackingJava.getPrimaryFile().setAttribute("NBIssue81746Workaround", Boolean.TRUE); // NOI18N
                    newBackingJava.getPrimaryFile().setAttribute(JsfJavaDataObject.JSF_ATTRIBUTE, Boolean.TRUE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            // Invalidate the JspDataObject if it had been created earlier and replace
            // with the correct DataObject type (since the backing java file now exists)
            if (! (result instanceof JsfJspDataObject)) {
                try {
                    FileObject jspFile = result.getPrimaryFile();
                    result.setValid(false);
                    result = DataObject.find(jspFile);
                    
                    if (! (result instanceof JsfJspDataObject)) {
                        ErrorManager.getDefault().log(ErrorManager.ERROR, 
                                "JsfJspDataObject#handleCreateFromTemplate: DataObject.find() did not return the correct value for "
                                + jspFile);
                    }
                    
                }catch (PropertyVetoException ex) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, 
                            "\nJsfJspDataObject#handleCreateFromTemplate: Unable to change to JsfJspDataLoader for FileObject: " // NOI18N
                            + result.getPrimaryFile());
                }
            }
        }
        
        try {
            // XXX NB issue #81746.
            result.getPrimaryFile().setAttribute("NBIssue81746Workaround", Boolean.TRUE); // NOI18N
        } catch (IOException ex) {
            ex.printStackTrace();
        }
 
        return result;
    }

    /** Renames the file. Handles also rename of corresponding jsf jsp file. */
    @Override
    protected FileObject handleRename(String name) throws IOException {
        FileObject fo = super.handleRename(name);
        return fo;
    }
        
// </rave>

}
