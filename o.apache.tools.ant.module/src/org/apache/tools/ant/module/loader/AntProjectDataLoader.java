/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick
 */
 
package org.apache.tools.ant.module.loader;

import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.DOMParser;

import org.openide.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/** Recognizes single files in the Repository as being of Ant Project type.
 */
public class AntProjectDataLoader extends UniFileLoader {
    private static final String REQUIRED_EXTENSION = "xml"; // NOI18N
    private static final String KNOWN_ANT_FILE = "org.apache.tools.ant.module.loader.AntProjectDataLoader.KNOWN_ANT_FILE"; // NOI18N

    private static final long serialVersionUID = 3642056255958054115L;

    public AntProjectDataLoader () {
        super (AntProjectDataObject.class);
    }

    protected void initialize () {
        setDisplayName (NbBundle.getMessage (AntProjectDataLoader.class, "LBL_loader_name"));

        ExtensionList extensions = new ExtensionList ();
        extensions.addExtension (REQUIRED_EXTENSION);
        setExtensions (extensions);

        setActions (new SystemAction[] {
            SystemAction.get (OpenAction.class),
            SystemAction.get (FileSystemAction.class),
            null,
            SystemAction.get (OpenLocalExplorerAction.class),
            null,
            SystemAction.get (ExecuteAction.class),
            null,
            SystemAction.get (CutAction.class),
            SystemAction.get (CopyAction.class),
            SystemAction.get (PasteAction.class),
            null,
            SystemAction.get (DeleteAction.class),
            SystemAction.get (RenameAction.class),
            null,
            SystemAction.get (NewAction.class),
            null,
            SystemAction.get (SaveAsTemplateAction.class),
            null,
            SystemAction.get (ToolsAction.class),
            SystemAction.get (PropertiesAction.class),
        });

    }
  
    /** Determines whether a given file should be handled by this loader.
     * @param fo the file object to interrogate
     * @return the fileojbect if we will handle it otherwise null
     */
    protected FileObject findPrimaryFile (FileObject fo) {
        FileObject fo2 = super.findPrimaryFile (fo);
        if (fo2 == null) {
            // Incorrect extension.
            return null;
        } else {
            // Check to see if we know that it is an Ant file already
            // (i.e. cache the parsing result).
            Object myGuy = fo2.getAttribute (KNOWN_ANT_FILE);
            if (myGuy != null && (myGuy instanceof Boolean)) {
                return ((Boolean) myGuy).booleanValue () ? fo2 : null;
            } else {
                if (fo2.getSize () == 0) {
                    // Empty XML files are definitely too early; don't waste time
                    // trying to parse them and failing.
                    // Try again when they have content.
                    return null;
                }
                // OK, first attempt to parse this file.
                // [PENDING] would probably be more efficient and not much more work to use a SAXParser
                // here instead. No need to parse the whole document when only the document element matters.
                DOMParser parser = new DOMParser();
                // Make sure it just does a raw scan, no evil web connections!
                parser.setEntityResolver (new EntityResolver () {
                        public InputSource resolveEntity (String publicID, String systemID) {
                            //System.err.println ("resolveEntity: " + publicID + " " + systemID);
                            // Read nothing whatsoever.
                            return new InputSource (new ByteArrayInputStream (new byte[] { }));
                        }
                    });
                try {

                    // Parse the document as XML
                    parser.parse (new InputSource (fo2.getInputStream ()));
                    Document document = parser.getDocument();

                    if (document != null) {

                        Element docElem = document.getDocumentElement();
                        if (docElem != null) {

                            // Test for the "project" element // NOI18N
                            if (docElem.getTagName().equals ("project") &&  // NOI18N
                                docElem.getAttributeNode ("name") != null && // NOI18N
                                docElem.getAttributeNode ("default") != null && // NOI18N
                                docElem.getAttributeNode ("basedir") != null) { // NOI18N
                                recognizeIt (fo2, true);
                                return fo2;
                            } else {
                                recognizeIt (fo2, false);
                                return null;
                            }
                        } else {
                            TopManager.getDefault ().getErrorManager ().log (ErrorManager.INFORMATIONAL,
                                                                             "Document.getDocumentElement -> null"); // NOI18N
                            return null;
                        }
                    } else {
                        TopManager.getDefault ().getErrorManager ().log (ErrorManager.INFORMATIONAL,
                                                                         "DOMParser.getDocument -> null"); // NOI18N
                        return null;
                    }
                } catch (Exception e) {
                    TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
                    // [PENDING] use ErrorManager.annotate(Throwable,String) when it works...
                    System.err.println ("Affected file: " + fo2); // NOI18N
                    return null;
                }
            }
        }
    }

    /** Mark a certain file as definitely mine or not mine.
     * @param fo the file object to remember about
     * @param mine whether it is an Ant file or not
     */
    private static void recognizeIt (FileObject fo, boolean mine) {
        if (fo.isReadOnly ()) {
            // Don't even try.
            return;
        }
        try {
            fo.setAttribute (KNOWN_ANT_FILE, new Boolean (mine));
        } catch (IOException ioe) {
            TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, ioe);
        }
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new AntProjectDataObject(primaryFile, this);
    }

    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject fo) {
        return new AntProjectFileEntry (obj, fo);
    }

    /** Purely for efficiency: if we are doing something with an Ant script, the result
     * will logically still be an Ant script; do not waste time reparsing it.
     */
    private static class AntProjectFileEntry extends FileEntry {

        private static final long serialVersionUID = -1765455606632045537L;
        
        public AntProjectFileEntry (MultiDataObject obj, FileObject fo) {
            super (obj, fo);
        }

        public FileObject createFromTemplate (FileObject f, String name) throws IOException {
            FileObject f2 = super.createFromTemplate (f, name);
            if (f2 != null) {
                recognizeIt (f2, true);
            }
            return f2;
        }

        public FileObject copy (FileObject f, String suffix) throws IOException {
            FileObject f2 = super.copy (f, suffix);
            if (f2 != null) {
                recognizeIt (f2, true);
            }
            return f2;
        }

        public FileObject rename (String name) throws IOException {
            FileObject f2 = super.rename (name);
            if (f2 != null) {
                recognizeIt (f2, true);
            }
            return f2;
        }

        public FileObject move (FileObject f, String suffix) throws IOException {
            FileObject f2 = super.move (f, suffix);
            if (f2 != null) {
                recognizeIt (f2, true);
            }
            return f2;
        }

    }

}
