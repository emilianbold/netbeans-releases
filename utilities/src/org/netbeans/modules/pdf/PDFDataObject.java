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

package org.netbeans.modules.pdf;

import java.io.File;

import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/** Data object representing a PDF file.
 * Only interesting feature is the {@link OpenCookie}
 * which lets you view it in e.g. Acrobat Reader or similar.
 * @author Jesse Glick
 */
public class PDFDataObject extends MultiDataObject {

    private static final long serialVersionUID = -1073885636989804140L;
    
    public PDFDataObject (FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        CookieSet cookies = getCookieSet ();
        // [PENDING] try also Java-implemented reader
        File f = NbClassPath.toFile (pf);
        if (f != null)
            cookies.add (new PDFOpenSupport (f));
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (PDFDataObject.class);
    }

    protected Node createNodeDelegate () {
        return new PDFDataNode (this);
    }

}
