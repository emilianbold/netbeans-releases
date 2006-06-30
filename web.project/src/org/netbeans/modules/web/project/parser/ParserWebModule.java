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

package org.netbeans.modules.web.project.parser;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.openide.filesystems.FileObject;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 *
 * @author Martin Grebac
 */
public class ParserWebModule extends JspParserAPI.WebModule {

    private WebModule module;

    /** Creates a new instance of ParserWebModule */
    public ParserWebModule(WebModule wm) {
        module = wm;
    }

    /** Returns the document base directory of the web module.
     * May return null if we are parsing a tag file that is outside a web module
     * (that will be packaged into a tag library).
     */
    public FileObject getDocumentBase() {
        return module.getDocumentBase();
    }

    /** Returns InputStream for the file open in editor or null
     * if the file is not open.
     */
    public java.io.InputStream getEditorInputStream (FileObject fo) {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {}

    public void removePropertyChangeListener(PropertyChangeListener l) {}
}
