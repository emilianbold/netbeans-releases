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

package org.netbeans.modules.web.taglib;

import java.io.*;
import org.xml.sax.InputSource;

import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.spi.xml.cookies.*;

import org.netbeans.modules.web.taglib.model.Taglib;

/** Object that provides main functionality for TLDLoader(data loader).
 * This class is final only for performance reasons,
 * can be unfinaled if desired.
 *
 */
public final class TLDDataObject extends MultiDataObject implements org.openide.nodes.CookieSet.Factory { 

    private static final boolean debug = false;
    /** Editor support for text data object. */
    private transient TLDEditorSupport editorSupport;
    /** generated Serialized Version UID */
    private static final long serialVersionUID = -7581377241494497816L;
    
    public TLDDataObject ()
    	throws DataObjectExistsException, IOException {
	super(null, null);
	if (debug) System.out.println("====> TLDDataObject():constructor"); // NOI18N
    }

    public TLDDataObject (final FileObject obj, final MultiFileLoader loader)
	throws DataObjectExistsException, IOException {
	super (obj, loader);
        
        getCookieSet().add(TLDEditorSupport.class, this);
        
        // Creates Check XML and Validate XML context actions
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        
	if (debug) System.out.println("====> TLDDataObject(FileObject, loader):constructor()"); // NOI18N

	//
	// Sometimes the FileObject is not valid. This most usually
	// occurs when the tag library exists in a source controlled
	// filesystem such as a CVS or Teamware filesystem, has been
	// checked in and then deleted. The source control system
	// reports the existence of the FileObject, but the filesystem
	// does not.  In this case we throw an IOException, and the
	// data object does not get built.
	//
        /*
	if (!isValid(obj)) {
	    
	    MessageFormat msgFormat =
		new MessageFormat(resbundle.getString("TLDDataObject_FileDoesntExist"));    // NOI18N
	    Object[] arg0 = new Object[] {getPrimaryFile().getName()};
	    // PENDING: somehow we seem to be doing nothing here. 
	    //String msg = msgFormat.format(arg0);
	    //System.out.println(msg);
	    //throw new IOException(msg);
	}
        */
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
	return new TLDNode(this);
    }
    
     // Accessibility from TXTEditorSupport:
    org.openide.nodes.CookieSet getCookieSet0() {
        return getCookieSet();
    }
    
    public Taglib getTaglib() throws java.io.IOException {
        java.io.InputStream is = getPrimaryFile().getInputStream();
        try {
            return Taglib.createGraph(is);
        } catch (RuntimeException ex) {
            throw new java.io.IOException(ex.getMessage());
        }
    }
    
    public void write(Taglib taglib) throws java.io.IOException {
        java.io.File file = org.openide.filesystems.FileUtil.toFile(getPrimaryFile());
        org.openide.filesystems.FileObject tldFO = getPrimaryFile();
        try {
            org.openide.filesystems.FileLock lock = tldFO.lock();
            try {
                java.io.OutputStream os = tldFO.getOutputStream(lock);
                try {
                    String version=taglib.getAttributeValue("version"); //NOI18N
                    if (version==null) { //JSP1.2 version
                        taglib.changeDocType("-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN", //NOI18N
                                             "http://java.sun.com/dtd/web-jsptaglibrary_1_2.dtd"); //NOI18N
                        taglib.setAttributeValue("xmlns",null); //NOI18N
                    }
                    taglib.write(os);
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
    
    /** Implements <code>CookieSet.Factory</code> interface. */
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(TLDEditorSupport.class))
            return getEditorSupport();
        else
            return null;
    }
    
    /** Gets editor support for this data object. */
    private TLDEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            synchronized(this) {
                if(editorSupport == null)
                    editorSupport = new TLDEditorSupport(this);
            }
        }
        
        return editorSupport;
    }
    
}
