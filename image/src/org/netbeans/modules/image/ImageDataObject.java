/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.image;

import java.io.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import javax.swing.*;

import org.openide.*;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.actions.OpenAction;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.openide.text.EditorSupport;

/** Object that represents one file containing an image.
*
* @author Petr Hamernik, Jaroslav Tulach, Ian Formanek
*/
public class ImageDataObject extends MultiDataObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6035788991669336965L;

    private static final String IMAGE_ICON_BASE =
        "org/netbeans/modules/image/imageObject"; // NOI18N

    /** New instance.
    * @param pf primary file object for this data object
    * @param loader the data loader creating it
    * @exception DataObjectExistsException if there was already a data object for it 
    */
    public ImageDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        // Support OpenCookie.
        getCookieSet ().add (new ImageOpenSupport (getPrimaryEntry ()));
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
    public byte[] getImageData() throws IOException {
        FileObject fo = getPrimaryFile();
        byte[] imageData = new byte[(int)fo.getSize()];
        BufferedInputStream in = new BufferedInputStream(fo.getInputStream());
        in.read(imageData, 0, (int)fo.getSize());
        in.close();
        return imageData; 
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

    /** Inner class. OpenSupport flavored with little EditorSupport" features like 
    * listening on changes of image file and renames on dataobject, 
    * so it can work appropriate in Editor window.
    */
    class ImageOpenSupport extends OpenSupport implements OpenCookie {

        /** Saves last modified time
        */
        private long lastSaveTime;

        /** Listens for changes on file
        */
        private FileChangeListener fileChangeL; 


        /** Constructs ImageOpenSupportObject on given MultiDataObject.Entry
        */
        public ImageOpenSupport (MultiDataObject.Entry ent) {
            super (ent);
        }

        /** Creates the CloenableTOPComponent viewer of image
        */
        public CloneableTopComponent createCloneableTopComponent () {
            prepareViewer();
            return new ImageViewer(ImageDataObject.this);
        }

        /**  Set listener for changes on image file
        */
        void prepareViewer() {
            // listen for changes on the image file
            if(fileChangeL == null) {
                fileChangeL = new FileChangeAdapter() {
                    public void fileChanged(final FileEvent evt) {
                        if (evt.getTime() > lastSaveTime) {
                            lastSaveTime = System.currentTimeMillis();
                            // post in AWT event thread because of possible dialog popup
                            SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        reload(evt);
                                    }
                                }
                            );
                        }
                    }
                };
            }
            entry.getFile().addFileChangeListener(fileChangeL);
            lastSaveTime = System.currentTimeMillis();
        }
        
        /** Ask and reload/close image views
        */
        private void reload(FileEvent evt) {
            // ask if reload?
            MessageFormat fmt = new MessageFormat(NbBundle.getBundle(EditorSupport.class).getString("FMT_External_change")); // NOI18N
            String msg = fmt.format(new Object[] { entry.getFile().getPackageNameExt('/', '.')});
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
            Object ret = TopManager.getDefault().notify(nd);

            if (NotifyDescriptor.YES_OPTION.equals(ret)) {
                RequestProcessor.postRequest(new Runnable() {
                    // load icon from file
                    public void run() {
                        Icon icon1; // accessory vraiable cause javac don't compile following code with final icon variable
                        try {
                            icon1 = new NBImageIcon(ImageDataObject.this);
                        } catch (IOException ioe) {
                            icon1 = new ImageIcon(new byte[0]); // empty icon
                        }
                        
                        final Icon icon = icon1;

                        Enumeration e = allEditors.getComponents();
                        while(e.hasMoreElements()) {
                            final Object pane = e.nextElement();
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    ((ImageViewer)pane).reloadIcon(icon);
                                }
                            });
                        }
                    }
                });
            }
        }
    } // end of inner ImageOpenSupport class
}

/*
 * Log
 *  14   Gandalf   1.13        1/5/00   Ian Formanek    NOI18N
 *  13   Gandalf   1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  11   Gandalf   1.10        6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  10   Gandalf   1.9         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         4/13/99  Jesse Glick     Clean-ups of comments 
 *       and such for public perusal.
 *  8    Gandalf   1.7         3/22/99  Ian Formanek    Icons moved from 
 *       modules/resources to this package
 *  7    Gandalf   1.6         2/3/99   Jaroslav Tulach 
 *  6    Gandalf   1.5         1/22/99  Ian Formanek    
 *  5    Gandalf   1.4         1/15/99  Petr Hamernik   image source repaired
 *  4    Gandalf   1.3         1/7/99   Jaroslav Tulach Uses OpenSupport
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
