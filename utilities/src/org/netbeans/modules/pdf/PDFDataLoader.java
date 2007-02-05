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

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/** Loader for PDF files (Portable Document Format).
 * Permits simple viewing of them.
 * @author Jesse Glick
 */
public class PDFDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    private static final long serialVersionUID = -4354042385752587850L;
    /** MIME-type of PDF files */
    private static final String PDF_MIME_TYPE = "application/pdf";      //NOI18N

    
    /** Creates loader. */
    public PDFDataLoader() {
        super("org.netbeans.modules.pdf.PDFDataObject"); // NOI18N
    }

    
    /** Initizalized loader, i.e. its extension list. Overrides superclass method. */
    protected void initialize () {
        super.initialize();

        ExtensionList extensions = new ExtensionList ();
        extensions.addMimeType(PDF_MIME_TYPE);
        extensions.addMimeType("application/x-pdf");                    //NOI18N
        extensions.addMimeType("application/vnd.pdf");                  //NOI18N
        extensions.addMimeType("application/acrobat");                  //NOI18N
        extensions.addMimeType("text/pdf");                             //NOI18N
        extensions.addMimeType("text/x-pdf");                           //NOI18N
        setExtensions (extensions);
    }
    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getMessage (PDFDataLoader.class, "LBL_loaderName");
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    protected String actionsContext () {
        return "Loaders/application/pdf/Actions/";                      //NOI18N
    }
    
    /** Creates multi data objcte for specified primary file.
     * Implements superclass abstract method. */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new PDFDataObject (primaryFile, this);
    }

}
