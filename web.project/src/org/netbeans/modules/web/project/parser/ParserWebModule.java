/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
