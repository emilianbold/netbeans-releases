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

package org.netbeans.modules.visualweb.designer.jsf;


import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.net.URL;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;


/**
 * XXX Moved from designer/../DocumentCache
 * Cache of external JsfForms, to avoid repeated loading and computing of the
 * layout of external document resources included in our documents
 * (via say &lt;iframe&gt;)
 *
 * @author Tor Norbye
 */
class ExternalDomProviderCache {
//    private Map<URL, WebForm> forms;
    private final Map<URL, JsfForm> externals = new HashMap<URL, JsfForm>();

    /** Construct a new box cache */
    public ExternalDomProviderCache() {
    }

    /** Return number of entries in this cache */
    public int size() {
//        return (forms != null) ? forms.size() : 0;
        return externals.size();
    }

//    /** Get a box by a particular URL */
//    public WebForm get(URL url) {
//        if (forms == null) {
//            return null;
//        }
//
//        WebForm result = forms.get(url);
//
////        if ((result != null) && (result.getModel() != null) &&
////                (result.getModel().isValid())) {
//        if (result != null && result.isModelValid()) {
//            forms.put(url, null);
//            result = null;
//        }
//
//        return result;
//    }
    /** Get a box by a particular URL */
    public JsfForm get(URL url) {
        JsfForm result = externals.get(url);

//        if ((result != null) && (result.getModel() != null) &&
//                (result.getModel().isValid())) {
        if (result != null && result.isModelValid()) {
            externals.put(url, null);
            result = null;
        }

        return result;
    }

//    /** Put a box into the cache */
//    public void put(URL url, WebForm page) {
//        if (forms == null) {
//            forms = new HashMap<URL, WebForm>(); // TODO - initial size?
//        }
//
//        forms.put(url, page);
//    }
    /** Put a box into the cache */
    public void put(URL url, JsfForm page) {
        externals.put(url, page);
    }
    
    public void remove(URL url) {
        externals.remove(url);
    }

//    /** Clear out the cache */
//    public void flush() {
//        if (forms != null) {
//            // Ensure that the files actually get reloaded from disk;
//            // NetBeans may be hanging on to old bits in its filesystem
//            // layer
//            Iterator<URL> it = forms.keySet().iterator();
//
//            while (it.hasNext()) {
//                URL url = it.next();
//                WebForm web = get(url);
//
//                if ((web != null) && (web.getJspDom() != null)) {
//                    // Flush styles as well
////                    CssLookup.refreshEffectiveStyles(web.getDom());
//                    CssProvider.getEngineService().refreshStylesForDocument(web.getJspDom());
//                    // XXX Should this be here too (or the above?).
//                    CssProvider.getEngineService().refreshStylesForDocument(web.getHtmlDom());
//                }
//
//                // XXX Lame validity check, missing nice NB API.
//                try {
//                    new URI(url.toExternalForm());
//                } catch(URISyntaxException ex) {
//                    // XXX #6368790 It means the url is not valid URI and URLMapper.findFileObject
//                    // would show an exception to the user. We just log it.
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                    continue;
//                }
//
//                FileObject fo = URLMapper.findFileObject(url);
//
//                if (fo != null) {
//                    fo.refresh(false);
//                }
//            }
//
//            forms = null;
//        }
//    }
    /** Clear out the cache */
    public void flush() {
        // Ensure that the files actually get reloaded from disk;
        // NetBeans may be hanging on to old bits in its filesystem
        // layer
        Iterator<URL> it = externals.keySet().iterator();

        while (it.hasNext()) {
            URL url = it.next();
            JsfForm web = get(url);
            if ((web != null) && (web.getJspDom() != null)) {
                // Flush styles as well
//                    CssLookup.refreshEffectiveStyles(web.getDom());
                CssProvider.getEngineService().refreshStylesForDocument(web.getJspDom());
                // XXX Should this be here too (or the above?).
                CssProvider.getEngineService().refreshStylesForDocument(web.getHtmlDom());
            }

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

        externals.clear();
    }

    @Override
    public String toString() {
        return super.toString() + "[externals=" + externals + "]"; // NOI18N
    }
}
