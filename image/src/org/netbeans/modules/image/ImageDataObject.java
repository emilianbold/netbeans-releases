/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.image;


import java.io.*;
import java.net.URL;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.actions.OpenAction;
import org.openide.util.actions.*;
import org.openide.nodes.*;


/** 
 * Object that represents one file containing an image.
 * @author Petr Hamernik, Jaroslav Tulach, Ian Formanek
 */
public class ImageDataObject extends MultiDataObject {
    
    /** Generated serialized version UID. */
    static final long serialVersionUID = -6035788991669336965L;

    /** Base for image resource. */
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/image/imageObject"; // NOI18N

    /** Helper variable. Speeds up <code>DataObject</code> recognition. */
    private boolean shouldInitCookieSet = true;
    
    
    /** Constructor.
    * @param pf primary file object for this data object
    * @param loader the data loader creating it
    * @exception DataObjectExistsException if there was already a data object for it 
    */
    public ImageDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
    }

    
    /** Help context for this object.
    * @return the help context
    */
    public org.openide.util.HelpCtx getHelpCtx () {
        return new org.openide.util.HelpCtx (ImageDataObject.class);
    }

    /** Get a URL for the image.
    * @return the image url
    */
    java.net.URL getImageURL() {
        try {
            return getPrimaryFile().getURL();
        } catch (FileStateInvalidException ex) {
            return null;
        }
    }

    /** Get image data for the image.
    * @return the image data
    */
    public byte[] getImageData() {
        try {
            FileObject fo = getPrimaryFile();
            byte[] imageData = new byte[(int)fo.getSize()];
            BufferedInputStream in = new BufferedInputStream(fo.getInputStream());
            in.read(imageData, 0, (int)fo.getSize());
            in.close();
            return imageData; 
        } catch(IOException ioe) {
            return new byte[0];
        }
    }


    /** Create a node to represent the image.
    * @return the node
    */
    protected Node createNodeDelegate () {
        DataNode node = new DataNode (this, Children.LEAF);
        node.setIconBase(IMAGE_ICON_BASE);
        node.setDefaultAction (SystemAction.get (OpenAction.class));
        return node;
    }
    
    /** Overrides superclass method. */
    public CookieSet getCookieSet() {
        if (shouldInitCookieSet) {
            initCookieSet();
        }
        return super.getCookieSet();
    }

    /**
     * Overrides superclass method. 
     * Look for a cookie in the current cookie set matching the requested class.
     * @param type the class to look for
     * @return an instance of that class, or <code>null</code> if this class of cookie
     *    is not supported
     */
    public Node.Cookie getCookie(Class type) {
        if (org.openide.cookies.CompilerCookie.class.isAssignableFrom(type)) {
            return null;
        }
        
        if (shouldInitCookieSet) {
            initCookieSet();
        }
        return super.getCookie(type);
    }
    
    /** Initializes cookie set. */
    private synchronized void initCookieSet() {
        if (!shouldInitCookieSet) {
            return;
        }
        CookieSet cookies = super.getCookieSet();
        cookies.add(new ImageOpenSupport (getPrimaryEntry ()));
        shouldInitCookieSet = false;
    }
}
