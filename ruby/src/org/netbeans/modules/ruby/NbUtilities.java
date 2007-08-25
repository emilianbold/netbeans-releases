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
package org.netbeans.modules.ruby;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;


/**
 * Utilities related to NetBeans - finding active editor, opening a file location, etc.
 *
 * @author Tor Norbye
 */
public class NbUtilities {

    private NbUtilities() {
    }

    public static JEditorPane getOpenPane() {
        Node[] arr = TopComponent.getRegistry().getActivatedNodes();

        if (arr.length > 0) {
            EditorCookie ec = arr[0].getCookie(EditorCookie.class);

            if (ec != null) {
                JEditorPane[] openedPanes = ec.getOpenedPanes();

                if ((openedPanes != null) && (openedPanes.length > 0)) {
                    return openedPanes[0];
                }
            }
        }

        return null;
    }

    public static FileObject findFileObject(JTextComponent target) {
        Document doc = target.getDocument();
        DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

        if (dobj == null) {
            return null;
        }

        return dobj.getPrimaryFile();
    }

    // Copied from UiUtils. Shouldn't this be in a common library somewhere?
    public static boolean open(final FileObject fo, final int offset, final String search) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        doOpen(fo, offset, search);
                    }
                });

            return true; // not exactly accurate, but....
        }

        return doOpen(fo, offset, search);
    }

    // Private methods ---------------------------------------------------------
    private static boolean doOpen(FileObject fo, int offset, String search) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getCookie(EditorCookie.class);
            LineCookie lc = od.getCookie(LineCookie.class);

            // If the caller hasn't specified an offset, and the document is
            // already open, don't jump to a particular line!
            if (offset == -1 && ec.getDocument() != null && search == null) {
                ec.open();
                return true;
            }
            
            // Simple text search if no known offset (e.g. broken/unparseable source)
            if ((ec != null) && (search != null) && (offset == -1)) {
                StyledDocument doc = ec.openDocument();

                try {
                    String text = doc.getText(0, doc.getLength());
                    offset = text.indexOf(search);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }

            if ((ec != null) && (lc != null) && (offset != -1)) {
                StyledDocument doc = ec.openDocument();

                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;

                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);

                        if (l != null) {
                            l.show(Line.SHOW_GOTO, column);

                            return true;
                        }
                    }
                }
            }

            OpenCookie oc = od.getCookie(OpenCookie.class);

            if (oc != null) {
                oc.open();

                return true;
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return false;
    }
    
    public static void extractZip(final FileObject extract, final FileObject dest) throws IOException {
        File extractFile = FileUtil.toFile(extract);
        extractZip(dest, new BufferedInputStream(new FileInputStream(extractFile)));
    }
    
    // Based on openide/fs' FileUtil.extractJar
    private static void extractZip(final FileObject fo, final InputStream is)
    throws IOException {
        FileSystem fs = fo.getFileSystem();

        fs.runAtomicAction(
            new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    extractZipImpl(fo, is);
                }
            }
        );
    }

    /** Does the actual extraction of the Jar file.
     */
    // Based on openide/fs' FileUtil.extractJarImpl
    private static void extractZipImpl(FileObject fo, InputStream is)
    throws IOException {
        ZipEntry je;

        ZipInputStream jis = new ZipInputStream(is);

        while ((je = jis.getNextEntry()) != null) {
            String name = je.getName();

            if (name.toLowerCase().startsWith("meta-inf/")) {
                continue; // NOI18N
            }

            if (je.isDirectory()) {
                FileUtil.createFolder(fo, name);

                continue;
            }

            // copy the file
            FileObject fd = FileUtil.createData(fo, name);
            FileLock lock = fd.lock();

            try {
                OutputStream os = fd.getOutputStream(lock);

                try {
                    FileUtil.copy(jis, os);
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
    }
}
