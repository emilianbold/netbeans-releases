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

package org.netbeans.modules.image;

import javax.imageio.ImageIO;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Data loader which recognizes image files.
 * @author Petr Hamernik, Jaroslav Tulach
 * @author Marian Petras
 */
public class ImageDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    static final long serialVersionUID =-8188309025795898449L;
    
    /** MIME-type of BMP files */
    private static final String BMP_MIME_TYPE = "image/bmp";            //NOI18N
    /** is BMP format support status known? */
    private static boolean bmpSupportStatusKnown = false;
    
    /** Creates new image loader. */
    public ImageDataLoader() {
        // Set the representation class.
        super("org.netbeans.modules.image.ImageDataObject"); // NOI18N
        
        ExtensionList ext = new ExtensionList();
        ext.addMimeType("image/gif");                                   //NOI18N
        ext.addMimeType("image/jpeg");                                  //NOI18N
        ext.addMimeType("image/png");                                   //NOI18N
        setExtensions(ext);
    }
    
    protected FileObject findPrimaryFile(FileObject fo){
        FileObject primFile = super.findPrimaryFile(fo);
        
        if ((primFile == null)
                && !bmpSupportStatusKnown 
                && !fo.isFolder()
                && fo.getMIMEType().equals(BMP_MIME_TYPE)) {
            try {
                if (ImageIO.getImageReadersByMIMEType(BMP_MIME_TYPE).hasNext()){
                    getExtensions().addMimeType(BMP_MIME_TYPE);
                    primFile = fo;
                }
            } finally {
                bmpSupportStatusKnown = true;
            }
        }
        
        return primFile;
    }
    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getBundle(ImageDataLoader.class).getString("PROP_ImageLoader_Name");
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    protected String actionsContext () {
        return "Loaders/image/png-gif-jpeg-bmp/Actions/";               //NOI18N
    }
    
    /** Create the image data object.
     * @param primaryFile the primary file (e.g. <code>*.gif</code>)
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has a data object
     * @exception java.io.IOException should not be thrown
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        return new ImageDataObject(primaryFile, this);
    }

}
