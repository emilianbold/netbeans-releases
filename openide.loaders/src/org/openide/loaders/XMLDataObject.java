/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;


import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.logging.*;
import javax.swing.Action;
import javax.xml.parsers.DocumentBuilder;
import org.openide.actions.OpenAction;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.text.DataEditorSupport;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.xml.*;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/** 
 * Object that provides main functionality for xml documents.
 * These objects are recognized by the <code>xml</code> extension and
 * <code>text/xml</code> MIME type. 
 * <p>
 * It is declaratively extensible by an {@link Environment}. 
 * The <code>Environment</code> is assigned to document instances using a provider
 * registered by DOCTYPE's public ID in the system filesystem under
 * <code>xml/lookups/{Transformed-DOCTYPE}</code> where the DOCTYPE transformation
 * is the same as that defined for {@link EntityCatalog} registrations.
 * 
 * @see XMLUtil
 * @see EntityCatalog
 *
 * @author  Libor Kramolis, Jaroslav Tulach, Petr Kuzel
 */
public class XMLDataObject extends MultiDataObject {
     /** generated Serialized Version UID */
    static final long serialVersionUID = 8757854986453256578L;
    
   /** Public ID of xmlinfo dtd. 
    * @deprecated replaced with Lookup
    */
    @Deprecated
    public static final String XMLINFO_DTD_PUBLIC_ID_FORTE = "-//Forte for Java//DTD xmlinfo//EN"; // NOI18N
    /** @deprecated replaced with Lookup
     */
    @Deprecated
    public static final String XMLINFO_DTD_PUBLIC_ID = "-//NetBeans IDE//DTD xmlinfo//EN"; // NOI18N

    /** Mime type of XML documents. */
    public static final String MIME = "text/xml";  //NOI18N
    //public static final String MIME2 = "application/xml"; //NOI18N
    
    /** PROP_DOCUMENT not parsed yet. Constant for getStatus method. */
    public static final int STATUS_NOT     = 0;
    /** PROP_DOCUMENT parsed ok. Constant for getStatus method. */
    public static final int STATUS_OK      = 1;
    /** PROP_DOCUMENT parsed with warnings. Constant for getStatus method. */
    public static final int STATUS_WARNING = 2;
    /** PROP_DOCUMENT parsed with errors. Constant for getStatus method. */
    public static final int STATUS_ERROR   = 3;
    
    /** property name of DOM document property */
    public static final String PROP_DOCUMENT = "document"; //??? it is not bound well // NOI18N

    /** property name of info property 
     * @deprecated info is not supported anymore. Replaced with lookup.
     */
    @Deprecated
    public static final String PROP_INFO = "info"; // NOI18N

    /** Default XML parser error handler */
    private static ErrorPrinter errorHandler = new ErrorPrinter();
        
    /**
     * Chain of resolvers contaning all EntityResolvers registred by a user.
     */
    private static XMLEntityResolverChain chainingEntityResolver;
    
    /** map of DTD publicID => Info. */
    private static HashMap<String, Info> infos = new HashMap<String, Info>();
    // the lock can be seamlesly shared by all instances
    private static Object emgrLock = new Object ();
    
    
    
    
    
    // 
    // Instance variables
    // 
    
    
    /** the XML document we delegate to */
    private DelDoc doc;
    

    /** the result of parsing */
    private int status;  //??? why it is not a bound property?
                         // it if often out-of date (e.g. garbage collection)

    /** @deprecated EditorCookie provided by subclass support 
     * need to prevail build in cookies.
     */
    @Deprecated
    private EditorCookie editor = null;

    /** 
     * Task body triggered by file change (primaryFile() or xmlinfo) parsing document
     * for extension (info) assigment information (xmlinfo or public id)
     */
    private InfoParser infoParser;

    /* For logging and debugging. */
    private static final Logger ERR = Logger.getLogger(XMLDataObject.class.getName());

               
    /** 
     * Create new XMLDataObject. It is usually called by a loader.
     * A user can get existing XMLDataObject by calling {@link DataObject#find(FileObject) 
     * <code>DataObject.find(FileObject f)</code>} instead.
     *
     * @param fo the primary file object, never <code>null</code>
     * @param loader loader of this data object, never <code>null</code>
     *
     */
    public XMLDataObject (FileObject fo, MultiFileLoader loader)
    throws DataObjectExistsException {
        super (fo, loader);
        
        fo.addFileChangeListener (FileUtil.weakFileChangeListener (getIP (), fo));

        status = STATUS_NOT;

        // register provided cookies
        // EditorCookie must be for back compatability consulted with subclasses
        //
        // In new model subclasses should directly provide its CookieSet.Factory that
        // uses last prevails order instead of old CookieSet first prevails order.
        // It completely prevails over this factory :-)
        
        CookieSet.Factory factory = new CookieSet.Factory() {
            public <T extends Node.Cookie> T createCookie(Class<T> klass) {
                if (klass.isAssignableFrom(EditorCookie.class)
                   || klass.isAssignableFrom(OpenCookie.class) 
                   || klass.isAssignableFrom(CloseCookie.class) 
                   || klass.isAssignableFrom(PrintCookie.class) ) {
                
                    if (editor == null) editor = createEditorCookie();  // the first pass
                    if (editor == null) return null;                    //??? gc unfriendly

                    return klass.isAssignableFrom(editor.getClass()) ? (T)editor : null;
                } else {
                    return null;
                }
            }
        };
                
        CookieSet cookies = getCookieSet();
        // EditorCookie.class must be synchronized with 
        // XMLEditor.Env->findCloneableOpenSupport
        cookies.add(EditorCookie.class, factory);
        cookies.add(OpenCookie.class, factory);
        cookies.add(CloseCookie.class, factory);
        cookies.add(PrintCookie.class, factory);
        
        // set info for this file
        //getIP ().resolveInfo ();        #16045
        cookies.assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                EditorCookie ec = getCookieSet().getCookie( EditorCookie.class );
                if( ec instanceof DataEditorSupport ) {
                    ((DataEditorSupport)ec).saveAs( folder, fileName );
                } else {
                    Logger.getLogger( XMLDataObject.class.getName() ).log( Level.FINE, "'Save As' requires DataEditorSupport" ); //NOI18N
                }
            }
        });
    }

    /** Getter for info parser. Initializes the infoparser in "lazy" way so it is accessble even before
        * the constructor finishes.
        */
    private final InfoParser getIP () {
        synchronized (emgrLock) {
            if (infoParser == null) {
                infoParser = new InfoParser ();
            }
        }
        return infoParser;
    }



     /** If the Info associated with this data object (if any) provides 
    * a subclass of Node, then this object is created to represent the
    * XML data object, otherwise DataNode is created.
    *
    * @return the node representation for this data object
    * @see DataNode
    */
    protected Node createNodeDelegate () {  //??? what about interaction with Looks
        XMLNode xn = new XMLNode (this);
        // netbeans.core.nodes.description
        xn.setShortDescription (NbBundle.getMessage ( 
                                        XMLDataObject.class, "HINT_XMLDataObject")); // NOI18N
        return xn;
    }

    /** Called when the info file is parsed and the icon should change.
    * @param res resource for the icon
    * @deprecated it is better to listen on properties
    */
    @Deprecated
    protected void updateIconBase (String res) {
        //??? we could add default behaviour, taking status into account
    }

    /*
     * Wait until background parsing terminates to avoid concurent file access.
     * It should terminate very early if just running, we can wait for it.
     */
    protected void handleDelete() throws IOException {

        getIP ().waitFinished();         // too late wait for finnish
        super.handleDelete();
    }

    public HelpCtx getHelpCtx () {
        // help for fix #23528, objects represents 'settings' nodes in Options dialog
        // returns DEFAULT_HELP for next processing
        try {
            if (getPrimaryFile ().getFileSystem ().isDefault ()) {
                if (getCookie (InstanceCookie.class)!=null) {
                    return HelpCtx.DEFAULT_HELP;
                }
            }
        } catch (FileStateInvalidException fsie) {
            // cannot determine type of this file object ==> return help id as normal
        }
        return new HelpCtx (XMLDataObject.class);
    }

    /**     
     * Cookies from assigned Environment are not placed into 
     * protected CookieSet and can be obtained only by invoking this method.
     * <p>
     * Cookie order for Info environments are handled  consistently with
     * CookieSet i.e. FIFO.
     * @return a cookie (instanceof cls) that has been found in info or
     * super.getCookie(cls).
     */
    public <T extends Node.Cookie> T getCookie(Class<T> cls) {
        getIP ().waitFinished();

        Object cake = getIP().lookupCookie(cls);
       
        if (ERR.isLoggable(Level.FINE)) {
            ERR.fine("Query for " + cls + " for " + this); // NOI18N
            ERR.fine("Gives a cake " + cake + " for " + this); // NOI18N
        }
        
        if (cake instanceof InstanceCookie) {
            cake = ofCookie ((InstanceCookie)cake, cls);
        }

        if (ERR.isLoggable(Level.FINE)) {
            ERR.fine("After ofCookie: " + cake + " for " + this); // NOI18N
        }
        
        if (cake == null) {
            cake = super.getCookie (cls);
        }
        
        if (ERR.isLoggable(Level.FINE)) {
            ERR.fine("getCookie returns " + cake + " for " + this); // NOI18N
        }
        
        return cls.cast(cake);
    }

    /** Special support of InstanceCookie.Of. If the Info class
     * provides InstanceCookie but not IC.Of, we add the extra interface to
     * this data object.
     *
     * @param ic instance cookie
     * @param cls constraining class
     * @return instance of InstanceCookie.Of
     */
    private InstanceCookie ofCookie (InstanceCookie ic, Class cls) {
        if (ic instanceof InstanceCookie.Of) {
            return ic;
        } else if (! cls.isAssignableFrom (ICDel.class)) {
            // Someone was looking for, and a processor etc. was
            // providing, some specialization which ICDel cannot
            // provide. Return the real implementation and forget
            // about making this a IC.Of.
            return ic;
        } else {
            ICDel d = new ICDel (this, ic);
            return d;
        }
    }

    private void notifyEx(Exception e) {
        Exceptions.attachLocalizedMessage(e,
                                          "Cannot resolve following class in xmlinfo."); // NOI18N
        Exceptions.printStackTrace(e);
    }
    
    /** Allows subclasses to provide their own editor cookie.
     * @return an editor cookie to be used as a result of <code>getCookie(EditorCookie.class)</code>
     *
     * @deprecated CookieSet factory should be used by subclasses instead.
     */
    @Deprecated
    protected EditorCookie createEditorCookie () {
        return new XMLEditorSupport (this);
    }
    
    // Vertical CookieManager
    private final void addSaveCookie (SaveCookie save) {
        getCookieSet ().add (save);
    }
    private final void removeSaveCookie (SaveCookie save) {
        getCookieSet ().remove (save);
    }

    //??? we ahould add it into class comment to make it public
    // or should we introduce second layer XMLDataObject extending this one
    // and having documented this functionality  (we cannot because of 
    // so this huge DataObject will survive createEditorCookie())
    /*
     * Really simple implementation of OpenCookie, EditorCookie, PrintCookie,
     * CloseCookie and managing SaveCookie.
     */
    private static class XMLEditorSupport extends DataEditorSupport implements OpenCookie, EditorCookie.Observable, PrintCookie, CloseCookie {
        public XMLEditorSupport (XMLDataObject obj) {
            super (obj, new XMLEditorEnv (obj));
            setMIMEType ("text/xml"); // NOI18N
        }
        class Save implements SaveCookie {
            public void save () throws IOException {
                saveDocument ();
                getDataObject ().setModified (false);
            }
        }
        protected boolean notifyModified () {
            if (! super.notifyModified ()) {
                return false;
            }
            if (getDataObject ().getCookie (SaveCookie.class) == null) {
                ((XMLDataObject) getDataObject ()).addSaveCookie (new Save ());
                getDataObject ().setModified (true);
            }
            return true;
        }
        protected void notifyUnmodified () {
            super.notifyUnmodified ();
            SaveCookie save = (SaveCookie) getDataObject ().getCookie (SaveCookie.class);
            if (save != null) {
                ((XMLDataObject) getDataObject ()).removeSaveCookie (save);
                getDataObject ().setModified (false);
            }
        }
        
        //!!! it also stays for SaveCookie however does not understand
        // encoding declared in XML header => need to be rewritten.
        private static class XMLEditorEnv extends DataEditorSupport.Env {
            private static final long serialVersionUID = 6593415381104273008L;
            
            public XMLEditorEnv (DataObject obj) {
                super (obj);
            }
            protected FileObject getFile () {
                return getDataObject ().getPrimaryFile ();
            }
            protected FileLock takeLock () throws IOException {
                return ((XMLDataObject) getDataObject ()).getPrimaryEntry ().takeLock ();
            }
            public CloneableOpenSupport findCloneableOpenSupport () {
                // must be sync with cookies.add(EditorCookie.class, factory);
                // #12938 XML files do not persist in Source editor
                return (CloneableOpenSupport) getDataObject ().getCookie (EditorCookie.class);
            }
        }
    }

    /** Creates w3c's document for the xml file. Either returns cached reference
    * or parses the file and creates new document.
    * 
    * @return the parsed document
    * @exception SAXException if there is a parsing error
    * @exception IOException if there is an I/O error
    */
    public final Document getDocument () throws IOException, SAXException {
        if (ERR.isLoggable(Level.FINE)) ERR.fine("getDocument" + " for " + this);
        synchronized (this) {
            DelDoc d = doc;
            if (d == null) {
                d = new DelDoc ();
                doc = d;
            }
            return d.getProxyDocument();
        }
    }
    
    /** Clears the document. Called when the document file is changed.
     */
    final void clearDocument () {
        if (ERR.isLoggable(Level.FINE)) ERR.fine("clearDocument" + " for " + this);
        //err.notify (ErrorManager.INFORMATIONAL, new Throwable ("stack dump"));
        doc = null;
        firePropertyChange (PROP_DOCUMENT, null, null);
    }

    /** 
     * @return one of STATUS_XXX constants representing PROP_DOCUMENT state. 
     */
    public final int getStatus () {
        return status;
    }

    /** @deprecated not used anymore
     * @return null
     */
    @Deprecated
    public final Info getInfo () {
        return null;
    }

    /** @deprecated does not do anything useful
     */
    @Deprecated
    public final synchronized void setInfo (Info ii) throws IOException {
    }

    /** Parses the primary file of this data object.
    * and provide different implementation.
    *
    * @return the document in the primary file
    * @exception IOException if error during parsing occures
    */
    final Document parsePrimaryFile () throws IOException, SAXException {
        if (ERR.isLoggable(Level.FINE)) ERR.fine("parsePrimaryFile" + " for " + this);
        String loc = getPrimaryFile().getURL().toExternalForm();
        try {
            return XMLUtil.parse(new InputSource(loc), false, /* #36295 */true, errorHandler, getSystemResolver());
        } catch (IOException e) {
            // Perhaps this document was not on a mounted filesystem.
            // Try again with an input stream - no relative URLs will work, but this
            // is extremely unlikely to matter. Cf. #36340.
            InputStream is = getPrimaryFile().getInputStream();
            try {
                return XMLUtil.parse(new InputSource(is), false, true, errorHandler, getSystemResolver());
            } finally {
                is.close();
            }
        }
    }


    // ~~~~~~~~~~~~~~~~~~~~ Start of Utilities ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned. Used non validating parser.
    *
    * @param url the url to read the file from
    * @deprecated Use {@link XMLUtil#parse(InputSource, boolean, boolean, ErrorHandler, EntityResolver) XMLUtil} instead
    * setting null error handler and validation to false.
    */
    @Deprecated
    public static Document parse (URL url) throws IOException, SAXException {
        return parse (url, errorHandler, false);
    }

    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned. Used non validating parser.
    *
    * @param url the url to read the file from
    * @param validate if true validating parser is used
    * @deprecated Use {@link XMLUtil#parse(InputSource, boolean, boolean, ErrorHandler, EntityResolver) XMLUtil} instead
    * setting null handler.
    */
    @Deprecated
    public static Document parse (URL url, boolean validate) throws IOException, SAXException {
        return parse (url, errorHandler, validate);
    }

    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned.
    *
    * @param url the url to read the file from
    * @param eh error handler to notify about exception
    * @deprecated Use {@link XMLUtil#parse(InputSource, boolean, boolean, ErrorHandler, EntityResolver) XMLUtil} instead
    * setting validation to false.
    */
    @Deprecated
    public static Document parse (URL url, ErrorHandler eh) throws IOException, SAXException {
        return parse (url, eh, false);
    }

    /** Factory a DocumentBuilder and let it create a org.w3c.dom.Document
    * This method takes URL. After successful finish the
    * document tree is returned.
    * A parser producing the Document has
    * set entity resolver to system entity resolver chain.
    *
    * @param url the url to read the file from
    * @param eh error handler to notify about exception
    * @param validate if true validating parser is used
    * @throws SAXException annotated if thrown due to configuration problem
    * @throws FactoryConfigurationError
    * @return org.w3c.dom.Document
    * @deprecated Use {@link XMLUtil#parse(InputSource, boolean, boolean, ErrorHandler, EntityResolver) XMLUtil} instead.     
    */
    @Deprecated
    public static Document parse (URL url, ErrorHandler eh, boolean validate) throws IOException, SAXException {
        
        DocumentBuilder builder = XMLDataObjectImpl.makeBuilder(validate);
        builder.setErrorHandler(eh);
        builder.setEntityResolver(getChainingEntityResolver());
        
        return builder.parse (new InputSource(url.toExternalForm()));

    }

    /** Creates SAX parse that can be used to parse XML files.
     * @return sax parser
     * @deprecated Use {@link XMLUtil#createXMLReader() XMLUtil} instead.
     * It will create a SAX XMLReader that is SAX Parser replacement.
     * You will have to replace DocumentHandler by ContentHandler
     * besause XMLReader accepts just ContentHandler. 
     * <p>Alternatively if not interested in new callbacks defined by
     * SAX 2.0 you can wrap returned XMLReader into XMLReaderAdapter
     * that implements Parser.
     */
    @Deprecated
    public static Parser createParser () {
        return createParser (false);
    }
   
    
    /** Factory SAX parser that can be used to parse XML files.
     * The factory is created according to javax.xml.parsers.SAXParserFactory property.
     * The parser has set entity resolver to system entity resolver chain.
     * @param validate if true validating parser is returned
     * @throws FactoryConfigurationError 
     * @return sax parser or null if no parser can be created
     * @deprecated Use {@link XMLUtil#createXMLReader(boolean,boolean ) Util} instead
     * setting ns to false.
     * For more details see {@link #createParser() createParser}
     */
    @Deprecated
    public static Parser createParser (boolean validate) {
        
        Parser parser = XMLDataObjectImpl.makeParser(validate);
        parser.setEntityResolver(getChainingEntityResolver());
        return parser;
        
    }


    /** 
     * Creates empty DOM Document using JAXP factoring.
     * @return Document or null on problems with JAXP factoring
     * @deprecated Replaced with {@link XMLUtil#createDocument(String,String,String,String) XMLUtil}
     *             It directly violates DOM's root element reference read-only status.
     *             If you can not move to XMLUtil for compatabilty reasons please
     *             replace with following workaround:
     * <pre>
     * String templ = "<myroot/>";
     * InputSource in = new InputSource(new StringReader(templ));
     * in.setSystemId("StringReader");  //workaround
     * DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
     * Document doc = builder.parse(in);
     * </pre>
     */
    @Deprecated
    public static Document createDocument() {
        
        deprecated();
        
        try {
            return XMLDataObjectImpl.makeBuilder(false).newDocument();
        } catch (IOException ex) {
            return null;
        } catch (SAXException ex) {
            return null;
        }
    }

    /** 
     * Writes DOM Document to writer. 
     *
     * @param doc DOM Document to be written
     * @param writer OutoutStreamWriter preffered otherwise
     *        encoding will be left for implementation specific autodection
     *
     * @deprecated Encoding used by Writer
     * may be in direct conflict with encoding
     * declared in document. Replaced with {@link XMLUtil#write(Document, OutputStream, String) Util}.
     */
    @Deprecated
    public static void write (Document doc, Writer writer) throws IOException {

        deprecated();

        // WARNING: back compatability code
        
        //use reflection to access "friendly" implementation in other package
        
        final String FAILURE = "org.openide.xml.XMLUtilImpl.write() invocation failed.";  //NOI18N
        
        try {
            Class clzz = Class.forName("org.openide.xml.XMLUtilImpl");  //NOI18N

            Method impl = clzz.getDeclaredMethod("write", new Class[] {//NOI18N
                Document.class, Object.class, String.class
            }); 
            impl.setAccessible(true);
            impl.invoke(null, new Object[] {doc, writer, null});                    

        } catch (IllegalAccessException ex) {
            throw new IOException(FAILURE);
        } catch (IllegalArgumentException ex) {
            throw new IOException(FAILURE);
        } catch (NoSuchMethodException ex) {
            throw new IOException(FAILURE);
        } catch (ClassNotFoundException ex) {
            throw new IOException(FAILURE);
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            if (t instanceof IOException) {
                throw (IOException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            }
            throw new IOException(FAILURE);
        }
    }

    /**
     * Write Document into OutputStream using given encoding. 
     * It is a shortcut for writing configurations etc. It guarantee 
     * just that data will be written. Structure and indentation
     * may change.
     *
     * @param doc DOM Document to be written
     * @param out data sink     
     * @param enc - XML defined encoding name (i.e. IANA defined, one of UTF-8, UNICODE, ASCII).
     * @deprecated Moved to {@link XMLUtil#write(Document, OutputStream, String) XMLUtil}.
     */
    @Deprecated
    public static void write(Document doc, OutputStream out, String enc) throws IOException {
        XMLUtil.write(doc, out, enc);
    }
    
    
    /** 
     * Creates SAX InputSource for specified URL 
     * @deprecated Deprecated as it was a workaround method. Replace
     * with <code>new InputSource(url.toExternalForm())</code>.
     */
    @Deprecated
    public static InputSource createInputSource(URL url) throws IOException {                
        return new InputSource(url.toExternalForm());
    }

    /**
     * Registers the given public ID as corresponding to a particular 
     * URI, typically a local copy.  This URI will be used in preference
     * to ones provided as system IDs in XML entity declarations.  This
     * mechanism would most typically be used for Document Type Definitions
     * (DTDs), where the public IDs are formally managed and versioned.
     *
     * <P> Any created parser use global entity resolver and you can
     * register its catalog entry.
     *
     * @param publicId The managed public ID being mapped
     * @param uri The URI of the preferred copy of that entity
     *
     * @deprecated Do not rely on global (non-modular) resolvers.
     *             Use {@link EntityCatalog} and {@link XMLUtil}
     *             instead.
     */
    @Deprecated
    public static void registerCatalogEntry (String publicId, String uri) {
        
        if (publicId == null) 
            throw new IllegalArgumentException("null public ID is not allowed."); //NOI18N

        XMLDataObjectImpl.registerCatalogEntry(publicId, uri);
        
    }

    /**
     * Registers a given public ID as corresponding to a particular Java
     * resource in a given class loader, typically distributed with a
     * software package.  This resource will be preferred over system IDs
     * included in XML documents.  This mechanism should most typically be
     * used for Document Type Definitions (DTDs), where the public IDs are
     * formally managed and versioned.
     *
     * <P> If a mapping to a URI has been provided, that mapping takes
     * precedence over this one.
     *
     * <P> Any created parser use global entity resolver and you can
     * register its catalog entry.
     *
     * @param publicId The managed public ID being mapped
     * @param resourceName The name of the Java resource
     * @param loader The class loader holding the resource, or null if
     *  it is a system resource.
     *
     * @deprecated Do not rely on global (non-modular) resolvers.
     *             Use {@link EntityCatalog} and {@link XMLUtil}
     *             instead.
     */
    @Deprecated
    public static void registerCatalogEntry (String publicId, String resourceName, ClassLoader loader) {
        if (publicId == null) 
            throw new IllegalArgumentException("null public ID is not allowed."); //NOI18N

        XMLDataObjectImpl.registerCatalogEntry(publicId, "nbres:/" + resourceName);  //NOI18N
    }

    /**
     * Add a given entity resolver to the NetBeans resolver chain.
     * The resolver chain is searched by private chaining resolver
     * until some registered resolver succed.
     *
     * <P>Every created parser use global entity resolver and then chain.
     *
     * @deprecated EntityResolver is a parser user responsibility. 
     *             Every time set a EntityResolver to an XML parser you use.
     *             The OpenIDE now defines a system {@link EntityCatalog}.
     *
     * @param resolver non null resolver to be added
     *
     * @return true if successfully added
     */
    @Deprecated
    public static final boolean addEntityResolver(EntityResolver resolver) {
        // return false; Is is deprecated :-)
        return getChainingEntityResolver().addEntityResolver(resolver);
    }

    /**
     * Remove a given entity resolver from the NetBeans resolver chain.
     *
     * <P>Every created parser use global entity resolver and then chain.
     *
     * @deprecated EntityResolver is a parser user responsibility.
     *
     * @param resolver non null resolver to be removed
     * @return removed resolver instance or null if not present
     */    
    @Deprecated
    public static final EntityResolver removeEntityResolver(EntityResolver resolver) {
        return getChainingEntityResolver().removeEntityResolver(resolver);
    }


    /** Accessor method for chaining entity resolver implementation. */
    private static synchronized XMLEntityResolverChain getChainingEntityResolver() {

        if (chainingEntityResolver == null) {                    
            chainingEntityResolver = new XMLEntityResolverChain();
            chainingEntityResolver.addEntityResolver(getSystemResolver());
        }
        
        return chainingEntityResolver;
        
    }
    
    /** Lazy initialized system resolver. */
    private static EntityResolver getSystemResolver() {        
        return  EntityCatalog.getDefault();
    }
    
    /**
     * Registers new Info to particular XML document content type as 
     * recognized by DTD public id. The registration is valid until JVM termination.
     *
     * @param publicId used as key
     * @param info associated value or null to unregister
     *
     * @deprecated Register an {@link Environment} via lookup, see
     * {@link XMLDataObject some details}.
     */    
    @Deprecated
    public static void registerInfo (String publicId, Info info) {  //!!! to be replaced by lookup
        synchronized (infos) {
            if (info == null) {
                infos.remove(publicId);
            } else {
                infos.put(publicId, info);
            }
        }
    }

    /**
    * Obtain registered Info for particular DTD public ID.
    *
    * @param publicId key which value is required
    * @return Info clone that is used for given publicId or null
     *
     * @deprecated Register via lookup
    */
    @Deprecated
    public static Info getRegisteredInfo(String publicId) {  //!!! to be replaced by lookup
        synchronized (infos) {
            Info ret = (Info) infos.get(publicId);
            return ret == null ? null : (Info)ret.clone ();
        }
    }

    /*
     * Guess from stack trace who calls deprecated method and print it.
     */
    private static void deprecated() {
        StringWriter wr = new StringWriter();
        PrintWriter pr = new PrintWriter(wr);
        new Exception("").printStackTrace(pr); // NOI18N
        pr.flush();
        String stack = wr.toString().trim();

        int start = stack.indexOf("\n"); // NOI18N
        int end = stack.indexOf("\n", start + 1); // NOI18N
        while (stack.indexOf("XMLDataObject", start + 1 )>0) { // NOI18N
            start = end;
            end = stack.indexOf("\n", start + 1); // NOI18N
        }        

        String line =  stack.substring(start + 1, end).trim();

        System.out.println("Warning: deprecated method called " + line); // NOI18N
    }
    
    /**
     * Default ErrorHandler reporting to log.
     */
    static class ErrorPrinter implements ErrorHandler {
        
        private void message(final String level, final SAXParseException e) {
            
            if (!LOG.isLoggable(Level.FINE)) {
                return;
            }
            
            final String msg = NbBundle.getMessage(
                XMLDataObject.class,
                "PROP_XmlMessage",  //NOI18N
                new Object [] {
                    level,
                    e.getMessage(),
                    e.getSystemId() == null ? "" : e.getSystemId(), // NOI18N
                    "" + e.getLineNumber(), // NOI18N
                    "" + e.getColumnNumber() // NOI18N
                }
            );
                
            LOG.fine(msg);
        }

        public void error(SAXParseException e) {
            message (NbBundle.getMessage(XMLDataObject.class, "PROP_XmlError"), e);  //NOI18N
        }

        public void warning(SAXParseException e) {
            message (NbBundle.getMessage(XMLDataObject.class, "PROP_XmlWarning"), e); //NOI18N
        }

        public void fatalError(SAXParseException e) {
            message (NbBundle.getMessage(XMLDataObject.class, "PROP_XmlFatalError"), e); //NOI18N
        }
    } // end of inner class ErrorPrinter
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ PARSER ----------------------------------

    // internally stops documet parsing when looking for public id
    private static class StopSaxException extends SAXException {
        public StopSaxException() { super("STOP"); } //NOI18N
    }

    // static fields that that are logically a part of InfoParser
    
    private static final StopSaxException STOP = new StopSaxException();
    
    /** We are guaranteed to be executed in one thread let reuse parser, etc. */
    private static XMLReader sharedParserImpl = null;
   
    static {
        try {
            sharedParserImpl = XMLUtil.createXMLReader();        
            sharedParserImpl.setEntityResolver(new EmptyEntityResolver());
        } catch (SAXException ex) {
            Exceptions.attachLocalizedMessage(ex,
                                              "System does not contain JAXP 1.1 compliant parser!"); // NOI18N
            Logger.getLogger(XMLDataObject.class.getName()).log(Level.WARNING, null, ex);
        }
        
        
        //initialize stuff possibly needed by libs that do not use 
        //JAXP but SAX 2 directly
        try {
            final Properties props = System.getProperties();
            final String SAX2_KEY = "org.xml.sax.driver";  //NOI18N
            if (props.getProperty(SAX2_KEY) == null) {
                props.put(SAX2_KEY, sharedParserImpl.getClass().getName());                
            }
        } catch (RuntimeException ex) {
            //ignore it (we did the best efford)
        }
    }
    
    /** a string to signal null value for parsedId */
    private static final String NULL = ""; // NOI18N
   
    /**
     * It simulates null that forbiden by SAX specs.
     */
    private static class NullHandler extends DefaultHandler implements LexicalHandler {

        static final NullHandler INSTANCE = new NullHandler();
        
        NullHandler() {}
        
        // LexicalHandler

        public void startDTD(String root, String pID, String sID) throws SAXException {
        }

        public void endDTD() throws SAXException {
        }

        public void startEntity(String name) throws SAXException {
        }

        public void endEntity(String name) throws SAXException {
        }

        public void startCDATA() throws SAXException {
        }

        public void endCDATA() throws SAXException {
        }

        public void comment(char[] ch, int start, int length) throws SAXException {
        }
    }

    /** 
     * Parser that parses XML document header to get some hints from it (such as DOCTYPE public ID.
     * It is named as InfoParser for historical reasons as it originally parsed
     * a coupled file (with xmlinfo extension) that contained NetBeans-related metadata.
     * That mechanism was replaced by /xml/lookup/<mangled pid> registrations and
     * by file attributes.
     */
    private final class InfoParser extends DefaultHandler
    implements FileChangeListener, LexicalHandler, LookupListener {
            
        /** the result of parsing the IDE */
        private String parsedId;
        
        /** Lookup associated with this document.
         */
        private Lookup lookup;
        
        /** result used for this lookup */
        private Lookup.Result result;
        
        private ThreadLocal<Class<?>> QUERY = new ThreadLocal<Class<?>> ();
        
        InfoParser() {}
        
        //~~~~~~~~~~~~~~~~~~~~~ Task body and control of queue ~~~~~~~~~~~~~~~~~~~
        
        /** Getter for public ID of the document.
         */
        public String getPublicId () {
            String id = waitFinished ();
            return id == NULL ? null : id;
        }
        
        /** Does lookup for specific cookie. It also
         * calls lookup on the result of Node.Cookie.class query and that is why it 
         * initializes the listeners to notify changes made on the data object.
         *
         * @param class to look for
         */
        public Object lookupCookie(final Class<?> clazz) {
            if (QUERY.get () == clazz) {
                if (ERR.isLoggable(Level.FINE)) ERR.fine("Cyclic deps on queried class: " + clazz + " for " + XMLDataObject.this); // NOI18N
                // somebody is querying for the same cookie in the same thread
                // probably neverending-loop - ignore
                return new InstanceCookie () {
                    public Class<?> instanceClass () {
                        return clazz;
                    }
                    
                    public Object instanceCreate () throws IOException {
                        throw new IOException ("Cyclic reference, sorry: " + clazz);
                    }
                    
                    public String instanceName () {
                        return clazz.getName ();
                    }
                };
            }
            
            Class<?> previous = QUERY.get ();
            try {
                QUERY.set (clazz);
                if (ERR.isLoggable(Level.FINE)) ERR.fine("Will do query for class: " + clazz + " for " + XMLDataObject.this); // NOI18N
                
                Lookup l;
                for (;;) {
                    String id = waitFinished();
                    synchronized (this) {
                        if (lookup != null) {
                            l = lookup;
                        } else {
                            l = null;
                        }
                    }
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("Lookup is " + l + " for id: " + id); // NOI18N
                    
                    if (l == null) {
                        l = updateLookup(null, id);
                        if (ERR.isLoggable(Level.FINE)) ERR.fine("Updating lookup: " + l); // NOI18N
                    }

                    if (ERR.isLoggable(Level.FINE)) ERR.fine("Wait lookup is over: " + l + XMLDataObject.this); // NOI18N
                    if (l != null) {
                        break;
                    }
                    if (parsedId == null) {
                        l = Lookup.EMPTY;
                        break;
                    }
                }

                Lookup.Result r = result;
                if (r != null) {
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("Querying the result: " + r); // NOI18N
                    // just to initialize all listeners
                    r.allItems ();
                } else {
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("No result for lookup: " + lookup); // NOI18N                
                }
                Object ret = l.lookup (clazz);
                if (ERR.isLoggable(Level.FINE)) ERR.fine("Returning value: " + ret + " for " + XMLDataObject.this); // NOI18N
                return ret;
            } finally {
                QUERY.set (previous);
            }
        }
           
        /*
         * Find out DTD public ID. Parses the document and updates content of
         * parsedId variable.
         * @return the parsed ID
         */
        public String waitFinished () {
            return  waitFinished (null);
        }
        
        /*
         * Find out DTD public ID.
         * Info is then assigned according to it from registry.
         */
        private String waitFinished (String ignorePreviousId) {
            if (sharedParserImpl == null) {
                ERR.fine("No sharedParserImpl, exiting"); // NOI18N
                return NULL;
            }
            
            XMLReader parser = sharedParserImpl;
            FileObject myFileObject = getPrimaryFile();
            String newID = null;
            
            if (ERR.isLoggable(Level.FINE)) ERR.fine("Going to read parsedId for " + XMLDataObject.this);
            
            String previousID;
            synchronized (this) {
                previousID = parsedId;
            }

            if (previousID != null) {
                if (ERR.isLoggable(Level.FINE)) {
                    ERR.fine("Has already been parsed: " + parsedId + " for " + XMLDataObject.this); // NOI18N
                }
                // ok, has already been parsed
                return previousID;
            }
            
            URL url = null;
            InputStream in = null;
            try {
                url = myFileObject.getURL();
            } catch (IOException ex) {
                warning(ex, "I/O exception while retrieving xml FileObject URL."); //NOI18N
                return NULL;  // cannot parse
            }

            synchronized (this) {
                try {
                    if (!myFileObject.isValid()) {
                        if (ERR.isLoggable(Level.FINE)) {
                            ERR.fine("Invalid file object: " + myFileObject); // NOI18N
                        }
                        return NULL;
                    }
                    
                    parsedId = NULL;
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("parsedId set to NULL for " + XMLDataObject.this); // NOI18N
                    try {
                        in =  myFileObject.getInputStream();
                    } catch (IOException ex) {
                        warning(ex, "I/O exception while openning xml."); //NOI18N
                        return NULL;  // cannot parse
                    }
                    try {

                        //
                        // we use one shared parser instance, so we must protect
                        // its integrity
                        //
                        synchronized (sharedParserImpl) {                    

                            configureParser(parser, false, this);
                            parser.setContentHandler(this);
                            parser.setErrorHandler(this);                    

                            InputSource input =  new InputSource(url.toExternalForm());
                            input.setByteStream(in);
                            parser.parse (input);
                        }
                        if (ERR.isLoggable(Level.FINE)) {
                            ERR.fine("Parse finished for " + XMLDataObject.this); // NOI18N
                        }
                    } catch (StopSaxException stopped) {
                        newID = parsedId;
                        ERR.fine("Parsing successfully stopped: " + parsedId + " for " + XMLDataObject.this); // NOI18N
                    } catch (SAXException checkStop) {
                        // stop parsing anyway
                        if (STOP.getMessage ().equals (checkStop.getMessage ())) {
                            newID = parsedId;
                            ERR.fine("Parsing stopped with STOP message: " + parsedId + " for " + XMLDataObject.this); // NOI18N
                        } else {
                            String msg = "Thread:" + Thread.currentThread().getName(); //NOI18N
                            ERR.warning("DocListener should not throw SAXException but STOP one.\n" + msg);  //NOI18N
                            ERR.log(Level.WARNING, null, checkStop);
                            Exception ex = checkStop.getException();
                            if (ex != null) {
                                ERR.log(Level.WARNING, null, ex);
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        // thrown when there is a problem with URL for example
                        ERR.log(Level.WARNING, null, ex);
                    } catch (IOException ex) {
                       // error while parsing for public id hide it.
                        // somebody have deleted the file meanwhile, because I do not lock?
                        ERR.log(Level.WARNING, null, ex);
                    } finally {
                        
                        // such small memory leak can complicate profiling a lot
                        // on the other hand it might cause performance regression
                        // guard it by dedicated property
                        if (Boolean.getBoolean("netbeans.profile.memory")) {    // NOI18N
                            // dettach from shared impl, it is static!
                            parser.setContentHandler(NullHandler.INSTANCE);
                            parser.setErrorHandler(NullHandler.INSTANCE);
                            try {
                                parser.setProperty("http://xml.org/sax/properties/lexical-handler", NullHandler.INSTANCE);  //NOI18N
                            } catch (SAXException ignoreIt) {
                            }                        

                            try {
                                // Crimson requires it to release old properties and handlers
                                parser.parse((InputSource)null);  
                            } catch (Exception ignoreIt) {
                            }
                        }
                        
                        parser = null;
                    }

                } finally {
                    try {
                        if (in != null) in.close();
                    } catch (IOException ex) {
                        ERR.log(Level.WARNING, null, ex);
                    }
                }
            
            }
            
            if (ignorePreviousId != null && newID.equals (ignorePreviousId)) {
                // no updates in lookup
                if (ERR.isLoggable(Level.FINE)) ERR.fine("No update to ID: " + ignorePreviousId + " for " + XMLDataObject.this); // NOI18N
                return newID;
            }
            
            // out of any synchronized blocks udpate the lookup
            // because it can call into unknown places via its 
            // Environment.findForOne
            if (ERR.isLoggable(Level.FINE)) ERR.fine("New id: " + newID + " for " + XMLDataObject.this); // NOI18N
            
            if (newID != null) {
                updateLookup (previousID, newID);
            }
            
            return newID;
        }

        
        /** Updates the ID.
         */
        private Lookup updateLookup (String previousID, String id) {
            synchronized (this) {
                if (previousID != null && previousID.equals (id) && lookup != null) {
                    ERR.fine("No need to update lookup: " + id + " for " + XMLDataObject.this); // NOI18N
                    return lookup;
                }
            }
            
            
            Lookup newLookup;

            // no lock here, because createInfoLookup & findForOne can call
            // foreing code
            Info info = getRegisteredInfo (id);
            if (info != null) {
                // use info
                newLookup = createInfoLookup (XMLDataObject.this, info);
                if (ERR.isLoggable(Level.FINE)) ERR.fine("Lookup from info: " + newLookup + " for " + XMLDataObject.this); // NOI18N
            } else {
                // ask the environment for the lookups
                newLookup = Environment.findForOne (XMLDataObject.this);
                if (ERR.isLoggable(Level.FINE)) ERR.fine("Lookup from env: " + newLookup + " for " + XMLDataObject.this); // NOI18N
                if (newLookup == null) {
                    newLookup = Lookup.EMPTY;
                }
            }
            
            synchronized (this) {
                // just one update of lookup in this InfoParser
                Lookup.Result prevRes = result;

                lookup = newLookup;
                if (ERR.isLoggable(Level.FINE)) ERR.fine("Shared lookup updated: " + lookup + " for " + XMLDataObject.this); // NOI18N
                result = lookup.lookupResult(Node.Cookie.class);
                result.addLookupListener (this);

                if (prevRes != null) {
                    prevRes.removeLookupListener (this);
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("Firing property change for " + XMLDataObject.this); // NOI18N
                    XMLDataObject.this.firePropertyChange (DataObject.PROP_COOKIE, null, null);
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("Firing done for " + XMLDataObject.this); // NOI18N
                }
                
                return newLookup;
            }
        }


        /*
         * We reuse the parser so it must be reconfigured prior every parsing task.
         */
        private void configureParser(XMLReader parser, boolean validation, LexicalHandler lex) {

            try {
                parser.setFeature("http://xml.org/sax/features/validation", validation);  //NOI18N
            } catch (SAXException sex) {
                ERR.fine("Warning: XML parser does not support validation feature.");  //NOI18N                    
            }

            try {
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", lex);  //NOI18N
            } catch (SAXException sex) {
                ERR.fine("Warning: XML parser does not support lexical-handler feature.");  //NOI18N
                //throw new Error("");
            }
        }


        
        // ~~~~~~~~~~ ERROR REPORTING ~~~~~~~~~~~~~~~~~~~~~~

        public void warning (Throwable ex) {
            warning(ex, null);
        }

        public void warning (Throwable ex, String annotation) {
           ERR.log(Level.WARNING, annotation, ex);
        }
        
        // LexicalHandler

        public void startDTD(String root, String pID, String sID) throws SAXException {
            parsedId = pID == null ? NULL : pID;
            ERR.fine("Parsed to " + parsedId); // NOI18N
            stop();
        }

        public void endDTD() throws SAXException {
            stop();
        }

        public void startEntity(String name) throws SAXException {
        }

        public void endEntity(String name) throws SAXException {
        }

        public void startCDATA() throws SAXException {
        }

        public void endCDATA() throws SAXException {                
        }

        public void comment(char[] ch, int start, int length) throws SAXException {
        }

        // redefine DefaultHandler

        //!!! should we stop on error?
        public void error(final SAXParseException p1) throws org.xml.sax.SAXException {
            stop();
        }

        public void fatalError(final SAXParseException p1) throws org.xml.sax.SAXException {
            stop();
        }

        public void endDocument() throws SAXException {
            stop();
        }

        public void startElement(String uri, String lName, String qName, Attributes atts) throws SAXException {
            // no DTD present
            stop(); 
        }

        private void stop() throws SAXException {
            throw STOP;
        }

        
        //~~~~~~~~~~~~~~~~~~ FS LISTENER ~~~~~~~~~~~~~~~~~
        //listening at parent folder
        
        public void fileFolderCreated (FileEvent fe) {
            // not interesting
        }

        public void fileDataCreated (FileEvent fe) {
//            FileObject fo = fe.getFile();
//            fileCreated(fo);
        }

        private void fileCreated(FileObject fo) {
//            if (
//                fo.getName ().equals (getPrimaryFile ().getName ())
//            ) {
//                // new info file created => force it to be reparsed
//                resolveInfo();
//            }
        }

        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        public void fileChanged (FileEvent fe) {
            if (getPrimaryFile ().equals (fe.getFile ())) {
                // the main file changed => invalidate DOM document
                //resolveInfo (getPrimaryFile ());  //reparse info again
                clearDocument ();
                String prevId = parsedId;
                parsedId = null;
                ERR.fine("cleared parsedId"); // NOI18N
                // parse update only if the ID is different
                waitFinished (prevId);
            }
        }

        public void fileDeleted (FileEvent fe) {
        }

        public void fileRenamed (FileRenameEvent fe) {
//            // the same behaviour as when the file is deleted
//            fileDeleted (fe);
//            // and new created
//            fileCreated(fe.getFile());
        }

        public void fileAttributeChanged (FileAttributeEvent fe) {
            // not interested in
        }
        
        /** A change in lookup.
         */
        public void resultChanged(LookupEvent lookupEvent) {
            XMLDataObject.this.firePropertyChange (DataObject.PROP_COOKIE, null, null);
            
            Node n = XMLDataObject.this.getNodeDelegateOrNull ();
            if (n instanceof XMLNode) {
                ((XMLNode)n).update ();
            }
        }
        
    } // end of InfoParser


    /** Avoid Internet connections */
    private static class EmptyEntityResolver implements EntityResolver {
        EmptyEntityResolver() {}
        public InputSource resolveEntity(String publicId, String systemID) {
            InputSource ret = new InputSource(new StringReader(""));  //??? we should tolerate file: and nbfs: // NOI18N
            ret.setSystemId("StringReader");  //NOI18N
            return ret;
        }
    }
    
    
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~ private Loader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
        
    /** The DataLoader for XmlDataObjects.
     */
    static final class Loader extends MultiFileLoader {
        static final long serialVersionUID =3917883920409453930L;
        /** Creates a new XMLDataLoader */
        public Loader () {
	    super ("org.openide.loaders.XMLDataObject");  //!!! so the relation loader data object is fixed // NOI18N
            //super (XMLDataObject.class);                // nothing like looks loader can be constructed
        }                                                 // can it produce subclasses?

        protected String actionsContext () {
            return "Loaders/text/xml/Actions"; // NOI18N
        }
        
        /** Get the default display name of this loader.
        * @return default display name
        */
        protected String defaultDisplayName () {
            return NbBundle.getMessage (XMLDataObject.class, "PROP_XmlLoader_Name");
        }
        
        /** For a given file finds a primary file.
        * @param fo the file to find primary file for
        *
        * @return the primary file for the file or null if the file is not
        *   recognized by this loader
        */
        protected FileObject findPrimaryFile (FileObject fo) {
            String mime = fo.getMIMEType ();
            if (mime.endsWith("/xml") || mime.endsWith("+xml")) { // NOI18N
                return fo;
            }
            // not recognized
            return null;            
        }
        
        /** Creates the right data object for given primary file.
        * It is guaranteed that the provided file is realy primary file
        * returned from the method findPrimaryFile.
        *
        * @param primaryFile the primary file
        * @return the data object for this file
        * @exception DataObjectExistsException if the primary file already has data object
        */
        protected MultiDataObject createMultiObject (FileObject primaryFile)
        throws DataObjectExistsException {
            return new XMLDataObject (primaryFile, this);
        }

        /** Creates the right primary entry for given primary file.
        *
        * @param primaryFile primary file recognized by this loader
        * @return primary entry for that file
        */
        protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }

        /** Creates right secondary entry for given file. The file is said to
        * belong to an object created by this loader.
        *
        * @param secondaryFile secondary file for which we want to create entry
        * @return the entry
        */
        protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
            // JST: We do not have secondary entries anymore, but it probably does not matter...
            return new FileEntry (obj, secondaryFile);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~ extension support via info ~~~~~~~~~~~~~~~~~~~~~~~~
    
    // i would like to throw it away sometimes in future and replace it by better one

    /** This class has to be implemented by all processors in the
    * xmlinfo file. It is cookie, so after parsing such class is instantiated
    * and put into data objects cookie set.
    *
    * @deprecated use lookup
    */
    @Deprecated
    public static interface Processor extends Node.Cookie {
        /** When the XMLDataObject creates new instance of the processor,
        * it uses this method to attach the processor to the data object.
        *
        * @param xmlDO XMLDataObject
        */
        public void attachTo (XMLDataObject xmlDO);
    }
    
    
    /** @deprecated use Lookup
     * Representation of xmlinfo file holding container of Processors.
     */
    @Deprecated
    public static final class Info implements Cloneable {
        List<Class<?>> processors;
        String iconBase;

        /** Create info */
        public Info () {
            processors = new ArrayList<Class<?>> ();
            iconBase = null;
        }

        public Object clone () {
            Info ii = new Info();
            for (Class<?> proc: processors) {
                ii.processors.add (proc);
            }
            ii.iconBase = iconBase;
            return ii;
        }

        /** Add processor class to info. 
        * The class should be public and either implement the Processor 
        * interface or should
        * have public constructor with one argument (DataObject or XMLDataObject).
        *
        * @param proc the class to add to this info
        * @exception IllegalArgumentException if the class does not seem to be valid
        */
        public synchronized void addProcessorClass(Class<?> proc) {
            if (!Processor.class.isAssignableFrom (proc)) {
                Constructor[] arr = proc.getConstructors();
                for (int i = 0; i < arr.length; i++) {
                    Class[] params = arr[i].getParameterTypes();
                    if (params.length == 1) {
                        if (
                            params[0] == DataObject.class || 
                            params[0] == XMLDataObject.class 
                        ) {
                            arr = null;
                            break;
                        }
                    }
                }
                
                if (arr != null) {
                    // no suitable constructor
                    throw new IllegalArgumentException();
                }
            }
            
            processors.add (proc);
        }

        /** Remove processor class from info.
         * @return true if removed
         */
        public boolean removeProcessorClass(Class<?> proc) {
            return processors.remove (proc);
        }

        public Iterator<Class<?>> processorClasses() {
            return processors.iterator();
        }

        /** Set icon base */
        public void setIconBase (String base) {
            iconBase = base;
        }

        /** @return icon base */
        public String getIconBase () {
            return iconBase;
        }

        /** Write specified info to writer */
        public void write (Writer writer) throws IOException {
            throw new IOException ("Not supported anymore"); // NOI18N
        }
        
        public boolean equals (Object obj) {
            if (obj == null) return false;
            if (obj instanceof Info == false) return false;
            
            Info i = (Info) obj;
            
            return ((iconBase != null && iconBase.equals(i.iconBase)) || (i.iconBase == iconBase)) 
                    && processors.equals(i.processors);
        }
    } // end of inner class Info
    
    
    /** A method for backward compatibility to create a lookup from data object and info
     * @param obj xml data object
     * @param info the info that should be associated
     */
    static Lookup createInfoLookup (XMLDataObject obj, Info info) {
        return new InfoLkp (obj, info);
    }

    
    /** A backward compatibility class that converts the content of 
     * an Info object into a Lookup class.
     */
    private static final class InfoLkp extends AbstractLookup {
        public final Info info;
        
        public InfoLkp (XMLDataObject obj, Info info) {
            this.info = info;
            
            Iterator<Class<?>> it = info.processorClasses ();
            ArrayList<InfoPair> arr = new ArrayList<InfoPair> (info.processors.size ());
            while (it.hasNext ()) {
                Class<?> c = it.next ();
                arr.add (new InfoPair (obj, c));
            }
            
            setPairs (arr);
        }
        
        /** A pair that receives a class and can create its instance either
         * using default constructor or by passing data object into one 
         * argument constructor.
         */
        private static final class InfoPair extends AbstractLookup.Pair {
            /** the class to use or null if object has already been created */
            private Class<?> clazz;
            /** XMLDataObject associated or object created */
            private Object obj;
            
            /** For use by subclasses. */
            protected InfoPair (XMLDataObject obj, Class<?> c) {
                this.obj = obj;
                this.clazz = c;
            }

            /** Tests whether this item can produce object
            * of class c.
            */
            protected boolean instanceOf (Class c) {
                Class<?> temp = clazz;
                if (temp == null) {
                    return c.isInstance (obj);
                } else {
                    return c.isAssignableFrom (temp);
                }
            }

            /** Method that can test whether an instance of a class has been created
             * by this item.
             *
             * @param obj the instance
             * @return if the item has already create an instance and it is the same
             *   as obj.
             */
            protected boolean creatorOf (Object obj) {
                return this.obj == obj;
            }

            /** The class of the result item.
             * @return the instance of the object.
             */
            public synchronized Object getInstance () {
                if (clazz == null) {
                    // already created an object
                    return obj;
                }
                
                // after this method the obj or null will contain the created object
                // instead of reference to XMLDataObject
                XMLDataObject xmlDataObject = (XMLDataObject)obj;
                obj = null;
                
                // the clazz will be null to signal, that an instance
                // of object has been created
                Class next = clazz;
                clazz = null;

                try {
                    if (Processor.class.isAssignableFrom (next)) {
                        // the class implements Processor interface, so use
                        // default constructor to construct instance
                        obj = next.newInstance ();
                        Processor proc = (Processor) obj;
                        proc.attachTo (xmlDataObject);
                        return obj;
                    } else {
                        // does not implement processor, try to search
                        // for constructor with one argument of DataObject or
                        // XMLDataObject

                        Constructor[] arr = next.getConstructors();
                        for (int i = 0; i < arr.length; i++) {
                            Class[] params = arr[i].getParameterTypes();
                            if (params.length == 1) {
                                if (
                                    params[0] == DataObject.class || 
                                    params[0] == XMLDataObject.class 
                                ) {
                                    obj = arr[i].newInstance(
                                        new Object[] { xmlDataObject }
                                    );
                                    return obj;
                                }
                            }
                        }
                    }
                    throw new InternalError ("XMLDataObject processor class " + next + " invalid"); // NOI18N
                } catch (InvocationTargetException e) {
                    xmlDataObject.notifyEx (e);
                } catch (InstantiationException e) {
                    xmlDataObject.notifyEx(e);
                } catch (IllegalAccessException e) {
                    xmlDataObject.notifyEx(e);
                }
                
                return obj;
            }

            /** The class of the result item.
             * @return the class of the item
             */
            public Class getType () {
                Class temp = clazz;
                return temp != null ? temp : obj.getClass ();
            }

            /** A persistent indentifier of the item. Can be stored and use 
             * in next run of the system.
             *
             * @return a string id of the item
             */
            public String getId () {
                return "Info[" + getType ().getName (); // NOI18N
            }
            
            /** The best display name is probably the name of type...
             */
            public String getDisplayName () {
                return getType ().getName ();
            }
        }
    }
    

    /** Computes correct node for given XMLDataObject.
     */
    private Node findNode () {
        Node n = (Node)getIP ().lookupCookie (Node.class);

        if (n == null) {
            return new PlainDataNode();
        } else {
            return n;
        }
    }
    
    private final class PlainDataNode extends DataNode {
        public PlainDataNode() {
            super(XMLDataObject.this, Children.LEAF);
            setIconBaseWithExtension("org/openide/loaders/xmlObject.gif"); // NOI18N
        }
        public Action getPreferredAction() {
            return SystemAction.get(OpenAction.class);
        }
    }
        
    
    /** Node that delegates either to data node or to a node provided by
     * the data object itself.
     */
    private final class XMLNode extends FilterNode {
        public XMLNode (XMLDataObject obj) {
            this (obj.findNode ());
        }
        private XMLNode (Node del) {
            super (del, new FilterNode.Children (del));            
            //setShortDescription("XML FILE");
        }
        private void update () {
            changeOriginal (XMLDataObject.this.findNode (), true);
        }
        
    }
    
    /** A special delegator that adds InstanceCookie.Of to objects that miss it
     */
    private static class ICDel extends Object implements InstanceCookie.Of {
        /** object we belong to
         */
        private XMLDataObject obj;
        /** cookie we delegate to */
        private InstanceCookie ic;

        public ICDel (XMLDataObject obj, InstanceCookie ic) {
            this.obj = obj;
            this.ic = ic;
        }


        public String instanceName () {
            return ic.instanceName ();
        }

        public Class<?> instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return ic.instanceClass ();
        }

        public Object instanceCreate ()
        throws java.io.IOException, ClassNotFoundException {
            return ic.instanceCreate ();
        }

        public boolean instanceOf (Class cls2) {
            if (ic instanceof InstanceCookie.Of) {
                return ((InstanceCookie.Of) ic).instanceOf (cls2);
            } else {
                try {
                    return cls2.isAssignableFrom (instanceClass ());
                } catch (IOException ioe) {
                    // ignore exception
                    return false;
                } catch (ClassNotFoundException cnfe) {
                    // ignore exception
                    return false;
                }
            }
        }
        
        public int hashCode () {
            return 2 * obj.hashCode () + ic.hashCode ();
        }
        
        public boolean equals (Object obj) {
            if (obj instanceof ICDel) {
                ICDel d = (ICDel)obj;
                return d.obj == obj && d.ic == ic;
            }
            return false;
        }
    } // end of ICDel    
    
    /** Delegating DOM document that provides fast implementation of
     * getDocumentType and getPublicID methods.
     */
    private final class DelDoc implements InvocationHandler {
        
        private Reference<Document> xmlDocument;
        private final Document proxyDocument;
        
        DelDoc() {
            proxyDocument = (Document)Proxy.newProxyInstance(
                DelDoc.class.getClassLoader(), new Class[] {Document.class}, this);
        }

        /** Creates w3c's document for the xml file. Either returns cached reference
        * or parses the file and creates new document.
        * 
        * @param force really create the document if it does not exists yet?
        * @return the parsed document or null if not forced
        */
        private final Document getDocumentImpl (boolean force) {
            synchronized (this) {
                Document doc = xmlDocument == null ? null : xmlDocument.get ();
                if (doc != null) {
                    return doc;
                }
                
                if (!force) {
                    return null;
                }

                status = STATUS_OK;
                try {
                    Document d = parsePrimaryFile ();
                    xmlDocument = new SoftReference<Document> (d);
                    return d;
                } catch (SAXException e) {
                    ERR.log(Level.WARNING, null, e);
                } catch (IOException e) {
                    ERR.log(Level.WARNING, null, e);
                }
                
                status = STATUS_ERROR;
                Document d = XMLUtil.createDocument("brokenDocument", null, null, null); // NOI18N
                
                xmlDocument = new SoftReference<Document> (d);
                
                // fire property change, because the document is errornous
                firePropertyChange (PROP_DOCUMENT, null, null);
                
                return d;
            }
        }
        
        /**
         * Get the externally usable, lazy document.
         * Delegates everything to the parsed document on disk (parsing as necessary),
         * except that getDoctype().getPublicId() is specially implemented so as to
         * not require loading the whole document.
         */
        public Document getProxyDocument() {
            return proxyDocument;
        }
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getDoctype") && args == null) { // NOI18N
                return Proxy.newProxyInstance(DelDoc.class.getClassLoader(), new Class[] {DocumentType.class}, this);
            } else if (method.getName().equals("getPublicId") && args == null) { // NOI18N
                Document d = getDocumentImpl(false);
                if (d != null) {
                    DocumentType doctype = d.getDoctype();
                    return doctype == null ? null : doctype.getPublicId();
                } else {
                    return getIP().getPublicId();
                }
            } else {
                return method.invoke(getDocumentImpl(true), args);
            }
        }
        
    }
    
}
