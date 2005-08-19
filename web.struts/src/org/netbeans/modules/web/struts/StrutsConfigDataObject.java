/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.struts;

//import org.netbeans.modules.xml.catalog.settings.CatalogSettings;
import org.netbeans.modules.web.struts.config.model.StrutsConfig;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.xml.sax.InputSource;

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.spi.xml.cookies.*;

/**
 *
 * @author Petr Pisl
 */
public class StrutsConfigDataObject extends MultiDataObject
                                    implements org.openide.nodes.CookieSet.Factory  {
    
    private static StrutsCatalog strutsCatalog;
    
    /** Editor support for text data object. */
    private transient StrutsConfigEditorSupport editorSupport;
    
    /** Creates a new instance of StrutsConfigDataObject */
    public StrutsConfigDataObject(FileObject pf, StrutsConfigLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        init();

    }
 
    private void init() {
        CookieSet cookies = getCookieSet();
        
        getCookieSet().add(StrutsConfigEditorSupport.class, this);
        
        // Creates Check XML and Validate XML context actions
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
    }
    
    /**
     * Provides node that should represent this data object. When a node for
     * representation in a parent is requested by a call to getNode (parent)
     * it is the exact copy of this node
     * with only parent changed. This implementation creates instance
     * <CODE>DataNode</CODE>.
     * <P>
     * This method is called only once.
     *
     * @return the node representation for this data object
     * @see DataNode
     */
    protected synchronized Node createNodeDelegate () {
	return new StrutsConfigNode(this);
    }
    
    /** Implements <code>CookieSet.Factory</code> interface. */
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(StrutsConfigEditorSupport.class))
            return getEditorSupport();
        else
            return null;
    }
    
    /** Gets editor support for this data object. */
    private StrutsConfigEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            synchronized(this) {
                if(editorSupport == null)
                    editorSupport = new StrutsConfigEditorSupport(this);
            }
        }
        
        return editorSupport;
    }
    
    public StrutsConfig getStrutsConfig() throws java.io.IOException {
        java.io.InputStream is = getPrimaryFile().getInputStream();
        try {
            return StrutsConfig.createGraph(is);
        } catch (RuntimeException ex) {
            throw new java.io.IOException(ex.getMessage());
        }
    }
    
    public void write(StrutsConfig config) throws java.io.IOException {
        java.io.File file = org.openide.filesystems.FileUtil.toFile(getPrimaryFile());
        org.openide.filesystems.FileObject configFO = getPrimaryFile();
        try {
            org.openide.filesystems.FileLock lock = configFO.lock();
            try {
                java.io.OutputStream os =configFO.getOutputStream(lock);
                try {
                    config.write(os);
                } finally {
                    os.close();
                }
            } 
            finally {
                lock.releaseLock();
            }
        } catch (org.openide.filesystems.FileAlreadyLockedException ex) {
            // PENDING should write a message
        }
    }
    
    org.openide.nodes.CookieSet getCookieSet0() {
        return getCookieSet();
    }
}
