/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore;

import java.io.IOException;
import org.netbeans.modules.mobility.svgcore.view.SVGViewTopComponent;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

public class SVGDataObject extends MultiDataObject implements CookieSet.Factory {
    
    /** Open support for this image data object. */
    private transient SVGOpenSupport openSupport;
    
    public SVGDataObject(FileObject pf, SVGDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        //pf.addFileChangeListener(FileUtil.weakFileChangeListener()
        getCookieSet().add(SVGOpenSupport.class, this);
        
    }
    
    protected Node createNodeDelegate() {
        return new SVGDataNode(this);
    }
    
    /** Implements <code>CookieSet.Factory</code> interface. */
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(SVGOpenSupport.class))
            return getOpenSupport();
        else
            return null;
    }
    
    /** Gets image open support. */
    private SVGOpenSupport getOpenSupport() {
        if(openSupport == null) {
            synchronized(this) {
                if(openSupport == null)
                    openSupport = new SVGOpenSupport(getPrimaryEntry());
            }
        }
        
        return openSupport;
    }    
}
