/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.websvc.wsdl.config;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import javax.swing.event.ChangeListener;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.DataObjectExistsException;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListeners;

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;

import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.wsdl.config.api.DDProvider;
import org.netbeans.modules.websvc.wsdl.config.api.Configuration;
import org.netbeans.modules.websvc.wsdl.config.api.Wsdl;
import org.netbeans.modules.websvc.jaxrpc.nodes.WsCompileConfigCookie;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.netbeans.modules.websvc.wsdl.xmlutils.SAXParseError;



/** DataObject for JAX-RPC WsCompile config files.
 *
 * @author Peter Williams
 */
public class WsCompileConfigDataObject extends DD2beansDataObject implements WsCompileConfigCookie,
        Node.Cookie, ConfigurationProxy.OutputProvider, ChangeListener {
    
    /** WsCompile confguration files are named [wsdl file name]-config.xml.  This
     *  constant is the suffix '-config'.
     */
    public static final String WSCOMPILE_CONFIG_FILENAME_SUFFIX = "-config"; // NOI18N
    
    /** WsCompile confguration files are named [wsdl file name]-config.xml.  This
     *  constant is the file extension 'xml'.
     */
    public static final String WSCOMPILE_CONFIG_EXTENSION = "xml"; // NOI18N
    
    private WeakReference packageHandlerRef = null;
    
    // If isClientWsdl is true, the WSDL file is in the WSDL folder of a web service
    // client enabled module and thus will have operations and UI exposed that affect
    // the service as it exists within the project.  E.g. deleting such a file will
    // actually remove the service from the project, not just delete the file on disk.
    private boolean isClientConfig;
    
    // !PW Added these members when upgrading to full DD API model
    private Configuration configuration;
    private FileObjectObserver fileListener;
    private boolean unparsable = true;
    
    /** Typical data object constructor.
     */
    public WsCompileConfigDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        init(pf, loader);
    }
    
    private void init(FileObject pf, MultiFileLoader loader) {
        initClientConfig();
        
        InputSource in = DataObjectAdapters.inputSource(this);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
//        getCookieSet().add(WsCompileConfigCookie.class, this);
        
        fileListener = new FileObjectObserver(pf);
        
        // !PW There must be a better way to do this.  DataObject listens for changes
        // across all source roots (including changes to the source roots themselves
        // to keep in sync) so that, suppose a file of interest to this XML file
        // is changed or deleted (Servlet for a WebApp, Handler for this config file),
        // then this object will be notified.
        //
        // Anyway, enable this code and more below (in the FileChangeListener
        // implementation) when handlers are added.
//        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
//        if (project != null) {
//            Sources sources = ProjectUtils.getSources(project);
//            sources.addChangeListener (this);
//        }
//        refreshSourceFolders ();
    }
    
//    private void refreshSourceFolders () {
//        ArrayList srcRootList = new ArrayList ();
//
//        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
//        if (project != null) {
//            Sources sources = ProjectUtils.getSources(project);
//            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
//            for (int i = 0; i < groups.length; i++) {
//                if (WebModule.getWebModule (groups [i].getRootFolder ()) != null) {
//                    try {
//                        FileObject fo = groups [i].getRootFolder ();
//                        srcRootList.add (groups [i].getRootFolder ());
//                        FileSystem fs = fo.getFileSystem ();
//                        fs.removeFileChangeListener(this); //avoid being added multiple times
//                        fs.addFileChangeListener (this);
//                    } catch (FileStateInvalidException ex) {
//                        ErrorManager.getDefault ().notify (ex);
//                    }
//                }
//            }
//        }
//        srcRoots = (FileObject []) srcRootList.toArray (new FileObject [srcRootList.size ()]);
//    }
    
    private void initClientConfig() {
        isClientConfig = false;
        FileObject configFO = getPrimaryFile();
        
        // Check to make sure it has a non-null parent (can't be in WSDL folder if it does).
        FileObject parentFO = configFO.getParent();
        if(parentFO != null) {
            // Does this module support web service clients?
            WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(configFO);
            if(clientSupport != null) {
                // Is this file object in the WSDL folder of the client?
                FileObject wsdlFolder = clientSupport.getWsdlFolder();
                if(wsdlFolder != null && wsdlFolder.equals(parentFO)) {
                    // If we get here, the following conditions should be true:
                    //   The Config file is in a code module that supports webservice clients.
                    //   The Config file is in the proper WSDL folder of that module.
                    isClientConfig = true;
                }
            }
        }
    }
    
    public boolean isUnparsable(){
        return unparsable;
    }
    
    public Configuration getConfiguration() {
        if(configuration == null) {
            parsingDocument();
        }
        
        return configuration;
    }
    
    // !PW FIXME Do we need this method?  What do we need it for?
//    private FileObject getAssociatedWsdl() {
//        FileObject wsdlFO = null;
//        FileObject configFO = getPrimaryFile();
//        String baseName = configFO.getName();
//
//        if(baseName.endsWith(WSCOMPILE_CONFIG_FILENAME_SUFFIX) && baseName.length() > 7) {
//            FileObject parentFO = configFO.getParent();
//            if(parentFO != null && parentFO.isFolder()) {
//                String wsdlName = baseName.substring(0, baseName.length()-7);
//                wsdlFO = parentFO.getFileObject(wsdlName, WsdlDataObject.WSDL_EXTENSION);
//            }
//        }
//
//        return wsdlFO;
//    }
    
    protected boolean isClientConfig() {
        return isClientConfig;
    }
    
    public boolean isRenameAllowed() {
        return !isClientConfig();
    }
    
    public boolean isDeleteAllowed() {
        return true;
    }
    
    /** Create a node to represent the WSDL file. Overrides superclass method.
     * @return node delegate */
    protected Node createNodeDelegate() {
        return new WsCompileConfigDataNode(this);
    }
    
    /** Gets the Icon Base for node delegate when parser accepts the xml document as valid
     *
     * PENDING: move into node
     * @return Icon Base for node delegate
     */
    protected String getIconBaseForInvalidDocument() {
        return "org/netbeans/modules/websvc/wsdl/config/resources/config_broken"; // NOI18N
    }
    
    /** Gets the Icon Base for node delegate when parser finds error(s) in xml document
     * @return Icon Base for node delegate
     *
     * PENDING: move into node
     */
    protected String getIconBaseForValidDocument() {
        return "org/netbeans/modules/websvc/wsdl/config/resources/config"; // NOI18N
    }
    
    /** Create document from the Node. This method is called after Node (Node properties)is changed.
     * The document is generated from data modul (isDocumentGenerable=true)
     */
    protected String generateDocument() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            configuration.write(out);
            out.close();
            return out.toString("UTF8"); //NOI18N
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        } catch(IllegalStateException ex){
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        
        return out.toString();
    }
    
    /** Update document in text editor. This method is called after Node (Node properties)is changed.
     * The document is updated programaticaly - not from data modul (isDocumentGeneratable=false)
     * @param doc document which should be updated
     */
    protected String updateDocument(javax.swing.text.Document doc) {
        return null;
    }
    
    /** Method from ConfigurationProxy.OutputProvider
     */
    public void write(Configuration configurationProxy) throws IOException {
        Configuration config = getConfiguration();
        if(config!=null) {
            config.merge(configurationProxy, Configuration.MERGE_UNION);
        }
        setNodeDirty(true);
    }
    
    /** Method from ConfigurationProxy.OutputProvider
     */
    public FileObject getTarget() {
        return getPrimaryFile();
    }
    
    protected SAXParseError updateNode(InputSource is) throws IOException {
        if(configuration==null) {
            try {
                configuration = DDProvider.getDefault().getDDRoot(getPrimaryFile());
                firePropertyChange(Node.PROP_PROPERTY_SETS,null,null);
                if(unparsable) {
                    unparsable=false;
                }
                return null;
            } catch (RuntimeException ex) {
                if(!unparsable) {
                    unparsable=true;
                }
                return new SAXParseError(new SAXParseException(ex.getMessage(),new org.xml.sax.helpers.LocatorImpl()));
            }
        }
        Configuration config=null;
        try {
            org.w3c.dom.Document doc = getDocument(is);
            config = org.netbeans.modules.websvc.wsdl.config.impl.Configuration.createGraph(doc);
            unparsable = false;
// !PW Not implemented yet.  Add to Configuration interface if required.
//            if(config.getError()!= null) {
//                // semantic error we can badge or tooltip here
//                System.out.println(config.getError().getMessage());
//            }
        } catch(SAXParseException ex) {
            if(!unparsable) {
                unparsable=true;
            }
            return new SAXParseError(ex);
        } catch(SAXException ex) {
            if(!unparsable) {
                unparsable=true;
            }
            throw new IOException();
        }
        if(config!=null) {
            // set new graph or merge it with old one
            configuration.merge(config, org.netbeans.modules.schema2beans.BaseBean.MERGE_UPDATE);
        }
        return null;
    }
    
    private org.w3c.dom.Document getDocument(InputSource inputSource) throws SAXParseException {
        try {
            // creating w3c document
            org.w3c.dom.Document doc = org.netbeans.modules.schema2beans.GraphManager.
                    createXmlDocument(inputSource, false, org.openide.xml.EntityCatalog.getDefault(),
                    new J2eeErrorHandler(this));
            return doc;
        } catch(Exception e) {
            //    XXX Change that
            throw new SAXParseException(e.getMessage(), new org.xml.sax.helpers.LocatorImpl());
        }
    }
    
    public String getServicePackageName() {
        String packageName = null;
        Configuration cfg = getConfiguration();
        if(cfg != null) {
            Wsdl wsdl = cfg.getWsdl();
            if(wsdl != null) {
                packageName = wsdl.getPackageName();
            }
        }
        return packageName;
    }
    
    public void setServicePackageName(String newPackage) {
        Configuration cfg = getConfiguration();
        if(cfg != null) {
            Wsdl wsdl = cfg.getWsdl();
            if(wsdl != null) {
                wsdl.setPackageName(newPackage);
            }
            
            try {
                cfg.write(getTarget());
            } catch(IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
        
        
    }
    
    protected void handleDelete() throws IOException {
        if(isClientConfig()) {
            // !PW FIXME Deleting a client config should probably invoke the same
            // delete code that deleting the corresponding client WSDL would.
            super.handleDelete();
        } else {
            super.handleDelete();
        }
    }
    
    protected DataObject handleCopy(DataFolder f) throws IOException {
        DataObject dObj = super.handleCopy(f);
        try {
            dObj.setValid(false);
        } catch(java.beans.PropertyVetoException e) {
        }
        return dObj;
    }
    
// !PW Not needed yet.
//    protected void dispose () {
//        // no more changes in DD
//        synchronized (this) {
//            updates = null;
//            if (updateTask != null) {
//                updateTask.cancel();
//            }
//        }
//        super.dispose ();
//    }
    
// --------------------------------------------------------------------
//   javax.swing.event.ChangeListener implementation
// --------------------------------------------------------------------
    public void stateChanged(javax.swing.event.ChangeEvent e) {
//        refreshSourceFolders();
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /** WeakListener for accepting external changes to the config file.
     */
    private class FileObjectObserver implements FileChangeListener {
        
        FileObjectObserver(FileObject fo) {
            fo.addFileChangeListener(
                    (FileChangeListener) WeakListeners.create(FileChangeListener.class, this, fo));
        }
        
        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }
        
        public void fileChanged(FileEvent fileEvent) {
            ConfigurationProxy config = (ConfigurationProxy) WsCompileConfigDataObject.this.getConfiguration();
            boolean needRewriting = true;
            if(config != null && config.isWriting()) { // change from outside
                config.setWriting(false);
                needRewriting = false;
            }
            if(isSavingDocument()) { // document is being saved
                setSavingDocument(false);
                needRewriting=false;
            }
            if(needRewriting) {
                getEditorSupport().restartTimer();
            }
        }
        
        public void fileDataCreated(FileEvent fileEvent) {
        }
        
        public void fileDeleted(FileEvent fileEvent) {
        }
        
        public void fileFolderCreated(FileEvent fileEvent) {
        }
        
        public void fileRenamed(FileRenameEvent fileRenameEvent) {
        }
    }
}
