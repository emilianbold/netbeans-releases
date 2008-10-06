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

package org.netbeans.modules.web.core.jsploader;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.netbeans.modules.web.core.QueryStringCookie;
import org.netbeans.modules.web.core.WebExecSupport;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Object that provides main functionality for internet data loader.
 *
 * @author Petr Jiricka
 */
public class JspDataObject extends MultiDataObject implements QueryStringCookie {
    
    public static final String EA_JSP_ERRORPAGE = "jsp_errorpage"; // NOI18N
    // property for the servlet dataobject corresponding to this page
    public static final String PROP_SERVLET_DATAOBJECT = "servlet_do"; // NOI18N
    public static final String PROP_CONTENT_LANGUAGE   = "contentLanguage"; // NOI18N
    public static final String PROP_SCRIPTING_LANGUAGE = "scriptingLanguage"; // NOI18N
    public static final String PROP_SERVER_CHANGE = "PROP_SERVER_CHANGE";// NOI18N
    public static final String PROP_REQUEST_PARAMS = "PROP_REQUEST_PARAMS"; //NOI18N
    
    static final String ATTR_FILE_ENCODING = "Content-Encoding"; // NOI18N
    
    private static final String DEFAULT_ENCODING = "ISO-8559-1"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(JspDataObject.class.getName());

    transient private EditorCookie servletEdit;
    transient protected JspServletDataObject servletDataObject;
    // it is guaranteed that if servletDataObject != null, then this is its
    // last modified date at the time of last refresh
    transient private Date servletDataObjectDate;
    transient private CompileData compileData;
    transient private boolean firstStart;
    transient private Listener listener;
    transient private BaseJspEditorSupport editorSupport;
    transient final private static boolean debug = false;
    
    transient volatile private Lookup currentLookup;
    
    transient private String encoding = null;

    public JspDataObject(FileObject pf, final UniFileLoader l) throws DataObjectExistsException {
        super(pf, l);
        getCookieSet().add(BaseJspEditorSupport.class, new CookieSet.Factory() {
            public <T extends Cookie> T createCookie(Class<T> klass) {
                return klass.cast(getJspEditorSupport());
            }
        });
        
        initialize();
    }
    
    @Override
    public Lookup getLookup() {
        return currentLookup;
    }
    
    // Public accessibility for e.g. JakartaServerPlugin.
    // [PENDING] Handle this more nicely.
    public org.openide.nodes.CookieSet getCookieSet0() {
        return super.getCookieSet();
    }
    
    @Override
    protected org.openide.nodes.Node createNodeDelegate() {
        return new JspNode(this);
    }
    
    private synchronized BaseJspEditorSupport getJspEditorSupport() {
        if (editorSupport == null) {
            editorSupport = new BaseJspEditorSupport(this);
        }
        return editorSupport;
    }
    
    protected EditorCookie createServletEditor() {
        return new ServletEditor(this);
    }
    
    public synchronized CompileData getPlugin() {
        if (compileData == null) {
            if ( firstStart ) {
                firstStart=false;
            }
            compileData = new CompileData(this);
            checkRefreshServlet();
        }
        return compileData;
    }
    
    /** Invalidates the current copy of server plugin for this JSP.
     * @param reload true if the new version of the plugin should be loaded.
     */
    public synchronized void refreshPlugin(boolean reload) {
        compileData = null;
        if (reload)
            getPlugin();
    }
    
    public void refreshPlugin() {
        refreshPlugin(true);
    }
    
    public JspServletDataObject getServletDataObject() {
        // force registering the servlet
        getPlugin();
        return servletDataObject;
    }
    
    /** Returns the MIME type of the content language for this page set in this file's attributes.
     * If nothing is set, defaults to 'text/html'.
     */
    public String getContentLanguage() {
        return "text/html"; // NOI18N
    }
    
    /** Returns the MIME type of the scripting language for this page set in this file's attributes.
     * If nothing is set, defaults to 'text/x-java'.
     */
    public String getScriptingLanguage() {
        return "text/x-java"; // NOI18N
    }
    
    public String getFileEncoding() {
        if (encoding == null){
            updateFileEncoding(false); //from file
        }
        return encoding;
    }
    
    void updateFileEncoding(boolean fromEditor) {
        TagLibParseSupport tlps = (TagLibParseSupport) getCookie(TagLibParseSupport.class);
        if (tlps != null) {
            encoding = tlps.getCachedOpenInfo(true, fromEditor).getEncoding();
        }

        if (encoding == null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Retrieved encoding is null for file " + getPrimaryFile().getNameExt());//NOI18N
            }
            encoding = DEFAULT_ENCODING;
        }
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Encoding updated for file " + getPrimaryFile().getNameExt() //NOI18N
                    + " to " + encoding);  //NOI18N
        }

    }
    
    private void initialize() {
        firstStart = true;
        listener = new Listener();
        listener.register(getPrimaryFile());
        refreshPlugin(false);
        createLookup();
        assert currentLookup != null;
    }
    
    /** Updates classFileData, servletDataObject, servletEdit
     * This does not need to be synchronized, because the calling method
     * getPlugin() is synchronized.
     */
    private void checkRefreshServlet() {
        
        final DataObject oldServlet = servletDataObject;
        if (debug)
            System.out.println("refreshing servlet, old = " + oldServlet); // NOI18N
        
        // dataobject
        try {
            FileObject servletFileObject = updateServletFileObject();
            if(debug) System.out.println("refreshing servlet, new servletFile = " + servletFileObject); // NOI18N
            if (servletFileObject != null) {
                // if the file has not changed, just return
                if ((oldServlet != null) &&
                        (oldServlet.getPrimaryFile() == servletFileObject) &&
                        (servletFileObject.lastModified().equals(servletDataObjectDate)))
                    return; // performance
                
                // set the origin JSP page
                JspServletDataObject.setSourceJspPage(servletFileObject, this);
                
                //set the preferred DataLoader
                DataLoaderPool.setPreferredLoader(servletFileObject, DataLoader.getLoader(JspServletDataLoader.class));
                
                
                // now the loader should recognize that this servlet was generated from a JSP
                DataObject dObj= DataObject.find(servletFileObject);
                if (debug) {
                    System.out.println("checkRefr::servletDObj=" +  // NOI18N
                            ((dObj == null) ? "null" : dObj.getClass().getName()) + // NOI18N
                            "/" + dObj); // NOI18N
                }
                /*if (!(dObj instanceof JspServletDataObject)) {
                    // need to re-recognize
                    dObj = rerecognize(dObj);
                }*/
                if (dObj instanceof JspServletDataObject) {
                    servletDataObject = (JspServletDataObject)dObj;
                    servletDataObjectDate = dObj.getPrimaryFile().lastModified();
                }
                // set the encoding of the generated servlet
                String servletEncoding = compileData.getServletEncoding();
                if (servletEncoding != null) {
                    if (!"".equals(servletEncoding)) {  //NOI18N
                        try {
                            Charset.forName(servletEncoding);
                        } catch (IllegalArgumentException ex) {
                            IOException t = new IOException(NbBundle.getMessage(JspDataObject.class, "FMT_UnsupportedEncoding", servletEncoding));  //NOI18N
                            t.initCause(ex);
                            Logger.getLogger("global").log(Level.INFO, null, t);  //NOI18N
                        }
                    } else
                        servletEncoding = null;
                }
                try {
                    // actually set the encoding
                    servletFileObject.setAttribute(ATTR_FILE_ENCODING, servletEncoding); //NOI18N
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);  //NOI18N
                }
            } else
                servletDataObject = null;
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);  //NOI18N
            servletDataObject = null;
        }
        
        // editor
        if ((oldServlet == null)/*&&(servletDataObject != null)*/) {
        } else {
            RequestProcessor.postRequest(
                    new Runnable() {
                public void run() {
                    updateServletEditor();
                    // Bugfix 31143: oldValue must be null, since if oldValue == newValue, no change will be fired
                    JspDataObject.this.firePropertyChange0(PROP_SERVLET_DATAOBJECT, null, getServletDataObject());
                    // the state of some CookieActions may need to be updated
                    JspDataObject.this.firePropertyChange0(PROP_COOKIE, null, null);
                }
            }
            );
        }
    }
    
    /** This method causes a DataObject to be re-recognized by the loader system.
     *  This is a poor practice and should not be normally used, as it uses reflection
     *  to call a protected method DataObject.dispose().
     */
   /* private DataObject rerecognize(DataObject dObj) {
        // invalidate the object so it can be rerecognized
        FileObject prim = dObj.getPrimaryFile();
        try {
            dObj.setValid(false);
            return DataObject.find(prim);
        }
        catch (java.beans.PropertyVetoException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return dObj;
    }*/
    
    /** JDK 1.2 compiler hack. */
    public void firePropertyChange0(String propertyName, Object oldValue, Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    /** Returns an editor for the servlet. Architecturally, a better solution would be to attach a cookie for
     * editing the servlet, but we choose this approach for performance reasons - this allows lazy initialization of
     * the editor (unlike the cookie). */
    public EditorCookie getServletEditor() {
        DataObject obj = getServletDataObject();
        if ((obj == null) != (servletEdit == null))
            updateServletEditor();
        return servletEdit;
    }
    
    private void updateServletEditor() {
        if (servletDataObject == null) {
            if (servletEdit != null) {
                servletEdit.close();
                servletEdit = null;
            }
        } else {
            if (servletEdit == null) {
                servletEdit = createServletEditor();
            }
        }
    }
    
    
    /** Gets the current fileobject of the servlet corresponding to this JSP or null if may not exist.
     * Note that the file still doesn't need to exist, even if it's not null.
     * This does not need to be synchronized, because the calling method
     * getPlugin() is synchronized.
     */
    private FileObject updateServletFileObject() throws IOException {
        return compileData.getServletFileObject();
    }
    
    
    /////// -------- FIELDS AND METHODS FOR MANIPULATING THE PARSED INFORMATION -------- ////////
    
    public void setQueryString(String params) throws java.io.IOException {
        WebExecSupport.setQueryString(getPrimaryEntry().getFile(), params);
        firePropertyChange(PROP_REQUEST_PARAMS, null, null);
    }
    
    @Override
    protected org.openide.filesystems.FileObject handleRename(String str) throws java.io.IOException {
        if ("".equals(str)) // NOI18N
            throw new IOException(NbBundle.getMessage(JspDataObject.class, "FMT_Not_Valid_FileName"));  //NOI18N
        
        org.openide.filesystems.FileObject retValue;
        
        retValue = super.handleRename(str);
        return retValue;
    }
    
    public void addSaveCookie(SaveCookie cookie){
        getCookieSet().add(cookie);
    }
    
    public void removeSaveCookie(){
        Node.Cookie cookie = getCookie(SaveCookie.class);
        if (cookie!=null) getCookieSet().remove(cookie);
    }
    
    @Override
    protected FileObject handleMove(DataFolder df) throws IOException {
        
        FileObject retValue;
        
        retValue = super.handleMove(df);
        
        // fix for issue #55961 - remove old TagLibParseSupport and add new one.
        TagLibParseSupport tlps = null;
        tlps = (TagLibParseSupport)getCookie(TagLibParseSupport.class);
        if (tlps != null){
            getCookieSet().remove(tlps);
            tlps = new TagLibParseSupport(retValue);
            getCookieSet().add(tlps);
        }
        return retValue;
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
                        Logger.getLogger("global").log(Level.INFO, "Detected illegal charset name in file " + file.getNameExt() + " (" + ichse.getMessage() + ")");  //NOI18N
                    }
                } catch (UnsupportedCharsetException uchse) {
                    Logger.getLogger("global").log(Level.INFO, "Detected unsupported charset name in file " + file.getNameExt() + " (" + uchse.getMessage() + ")");  //NOI18N
                }

                return null;
            }
        };

        currentLookup = new ProxyLookup(noEncodingLookup, Lookups.singleton(feq));
    }
    
    
    ////// -------- INNER CLASSES ---------
    
    private class Listener extends FileChangeAdapter implements PropertyChangeListener/*, ServerRegistryImpl.ServerRegistryListener */{
        WeakReference weakListener;
        
        Listener() {
        }
        
        private void register(FileObject fo) {
            EventListener el = WeakListeners.create(FileChangeListener.class, this, fo);
            fo.addFileChangeListener((FileChangeListener) el);
            weakListener = new WeakReference(el);
        }
        private void unregister(FileObject fo) {
            FileChangeListener listener = (FileChangeListener) weakListener.get();
            if (listener != null) {
                fo.removeFileChangeListener(listener);
            }
        }
        public void propertyChange(PropertyChangeEvent evt) {
            // listening on properties which could affect the server plugin
            // saving the file
            if (PROP_MODIFIED.equals(evt.getPropertyName())) {
                if ((Boolean.FALSE).equals(evt.getNewValue())) {
                    refreshPlugin(false);
                }
            }
            // primary file changed or files changed
            if (PROP_PRIMARY_FILE.equals(evt.getPropertyName()) ||
                    PROP_FILES.equals(evt.getPropertyName())) {
                if (evt.getOldValue() instanceof FileObject)
                    unregister((FileObject)evt.getOldValue());
                if (evt.getNewValue() instanceof FileObject)
                    register((FileObject)evt.getNewValue());
                    refreshPlugin(true);
            }
            // the context object has changed
            if (DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                if (evt.getSource() instanceof DataObject) {
                    DataObject dobj = (DataObject)evt.getSource();
                    if (dobj.getPrimaryFile().getPackageNameExt('/','.').equals("")) { // NOI18N
                        dobj.removePropertyChangeListener(this);
                        // PENDING
                        //ServerRegistryImpl.getRegistry().removeServerRegistryListener(this);
                        //JspDataObject.this.addWebContextListener();
                    }
                }
            }
            
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            refreshPlugin(true);
        }
        
        // implementation of ServerRegistryImpl.ServerRegistryListener
        /*
        PENDING
        public void added(ServerRegistryImpl.ServerEvent added) {
            serverChange();
        }
         
        public void setAppDefault(ServerRegistryImpl.InstanceEvent inst) {
            serverChange();
        }
         
        public void setWebDefault(ServerRegistryImpl.InstanceEvent inst) {
            serverChange();
        }
         
        public void removed(ServerRegistryImpl.ServerEvent removed) {
            serverChange();
        }
         */
        /*
        private void serverChange() {
            refreshPlugin(true);
            firePropertyChange0(PROP_SERVER_CHANGE, null, null);
        }*/
    }
}

