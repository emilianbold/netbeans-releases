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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import javax.swing.*;

import org.openide.*;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.CloneableTopComponent;
import org.openide.util.*;
import org.openide.text.EditorSupport;

/** OpenSupport flavored with little EditorSupport features like 
* listening on changes of image file and renames on dataobject, 
* so it can work appropriate in Editor window.
*
* @author Peter Zavadsky
*/

public class ImageOpenSupport extends OpenSupport implements OpenCookie {

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
        return new ImageViewer((ImageDataObject)entry.getDataObject());
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
            // due to compiler 1.2 bug only
            final ImageDataObject imageObj = (ImageDataObject)entry.getDataObject();
            final CloneableTopComponent.Ref editors = allEditors;

            RequestProcessor.postRequest(new Runnable() {
                // load icon from file
                public void run() {
                    final Icon icon = new NBImageIcon(imageObj);

                    Enumeration e = editors.getComponents();
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
}

