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

package org.netbeans.modules.pdf;

import java.io.File;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
        File f = FileUtil.toFile (pf);
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
