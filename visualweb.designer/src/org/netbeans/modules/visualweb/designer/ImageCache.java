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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.designer;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;


/**
 * Image cache, to avoid repeated loading of the same image urls.
 *
 * @author Tor Norbye
 */
public class ImageCache {
//    HashMap images;
    private Map<URL, ImageIcon> images;

    /** Construct a new image cache */
    public ImageCache() {
    }

    /** Get an image by a particular URL */
    public ImageIcon get(URL url) {
        if (images == null) {
            return null;
        }

        return images.get(url);
    }

    /** Put an image into the cache */
    public void put(URL url, ImageIcon image) {
        if (images == null) {
            images = new HashMap<URL, ImageIcon>(); // TODO - initial size?
        }

        images.put(url, image);
    }

    /** Clear out the cache */
    public void flush() {
        if (images != null) {
            // Ensure that the files actually get reloaded from disk;
            // NetBeans may be hanging on to old bits in its filesystem
            // layer
            Iterator<URL> it = images.keySet().iterator();

            while (it.hasNext()) {
                URL url = it.next();

                // XXX Lame validity check, missing nice NB API.
                try {
                    new URI(url.toExternalForm());
                } catch(URISyntaxException ex) {
                    // XXX #6368790 It means the url is not valid URI and URLMapper.findFileObject
                    // would show an exception to the user. We just log it.
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    continue;
                }

                FileObject fo = URLMapper.findFileObject(url);

                if (fo != null) {
                    fo.refresh(false);
                }
            }

            images = null;
        }
    }

    public String toString() {
        return super.toString() + "[images=" + images + "]"; // NOI18N
    }
}
