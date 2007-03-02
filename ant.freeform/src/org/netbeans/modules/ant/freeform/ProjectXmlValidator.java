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

package org.netbeans.modules.ant.freeform;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Checks validity of freeform project.xml files.
 * @see "#47288"
 * @author Jesse Glick
 */
final class ProjectXmlValidator extends DefaultHandler implements FileChangeListener {
    
    private final FileObject projectXml;
    private InputOutput io;
    
    public ProjectXmlValidator(FileObject projectXml) {
        this.projectXml = projectXml;
        projectXml.addFileChangeListener(this);
        validateProjectXml();
    }
    
    private void validateProjectXml() {
        if (System.getProperty("netbeans.user") == null) { // NOI18N
            // Probably in a unit test; skip it.
            return;
        }
        open();
        try {
            // XXX may want to preinitialize the desired SAXParserFactory and keep it statically, for speed
            SAXParserFactory f = SAXParserFactory.newInstance();
            f.setNamespaceAware(true);
            f.setValidating(true);
            SAXParser p = f.newSAXParser();
            p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", // NOI18N
                          "http://www.w3.org/2001/XMLSchema"); // NOI18N
            p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", getSchemas()); // NOI18N
            p.parse(projectXml.getURL().toString(), this);
        } catch (SAXParseException e) {
            log(e);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            close();
        }
    }

    /**
     * Compute a list of XML schema locations to be used for validating project.xml files.
     */
    private static String[] getSchemas() {
        Set<String> schemas = new TreeSet<String>();
        // XXX should not refer to schema in another module; wait for #42686 to solve properly
        schemas.add("nbres:/org/netbeans/modules/project/ant/project.xsd"); // NOI18N
        schemas.add("nbres:/org/netbeans/modules/ant/freeform/resources/freeform-project-general.xsd"); // NOI18N
        schemas.add("nbres:/org/netbeans/modules/ant/freeform/resources/freeform-project-general-2.xsd"); // NOI18N
        for (ProjectNature nature : FreeformProject.PROJECT_NATURES.allInstances()) {
            schemas.addAll(nature.getSchemas());
        }
        return schemas.toArray(new String[schemas.size()]);
    }
    
    public void fileChanged(FileEvent fe) {
        validateProjectXml();
    }

    public void fileRenamed(FileRenameEvent fe) {}

    public void fileAttributeChanged(FileAttributeEvent fe) {}

    public void fileFolderCreated(FileEvent fe) {}

    public void fileDeleted(FileEvent fe) {}

    public void fileDataCreated(FileEvent fe) {}

    public void warning(SAXParseException e) throws SAXException {
        log(e);
    }

    public void error(SAXParseException e) throws SAXException {
        log(e);
    }

    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }
    
    /** Close any old error tab. */
    private void open() {
        if (io != null) {
            io.closeInputOutput();
            io = null;
        }
    }

    /** Log a parse error, opening error tab as needed. */
    private void log(SAXParseException e) {
        if (io == null) {
            String title = NbBundle.getMessage(ProjectXmlValidator.class, "LBL_project.xml_errors", FileUtil.getFileDisplayName(projectXml));
            io = IOProvider.getDefault().getIO(title, true);
            io.select();
        }
        try {
            io.getErr().println(e.getLocalizedMessage(), new Hyperlink(e.getSystemId(), e.getLineNumber(), e.getColumnNumber()));
        } catch (IOException x) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, x);
        }
    }

    /** Close the stream for the error tab, if one is open, but leave it visible. */
    private void close() {
        if (io != null) {
            io.getErr().close();
            io.getOut().close(); // XXX why is this necessary?
        }
    }

    private static final class Hyperlink implements OutputListener {
        
        private final String uri;
        private final int line, column;
        
        public Hyperlink(String uri, int line, int column) {
            this.uri = uri;
            this.line = line;
            this.column = column;
        }
        
        public void outputLineAction(OutputEvent ev) {
            FileObject fo;
            try {
                fo = URLMapper.findFileObject(new URL(uri));
            } catch (MalformedURLException e) {
                assert false : e;
                return;
            }
            if (fo == null) {
                return;
            }
            DataObject d;
            try {
                d = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                assert false : e;
                return;
            }
            EditorCookie ec = d.getCookie(EditorCookie.class);
            if (ec == null) {
                return;
            }
            if (line != -1) {
                try {
                    // XXX do we need to call ec.openDocument as in org.apache.tools.ant.module.run.Hyperlink?
                    Line l = ec.getLineSet().getOriginal(line - 1);
                    if (column != -1) {
                        l.show(Line.SHOW_GOTO, column - 1);
                    } else {
                        l.show(Line.SHOW_GOTO);
                    }
                } catch (IndexOutOfBoundsException e) {
                    // forget it
                    ec.open();
                }
            } else {
                ec.open();
            }
        }
        
        public void outputLineSelected(OutputEvent ev) {}
        
        public void outputLineCleared(OutputEvent ev) {}
        
    }
    
}
