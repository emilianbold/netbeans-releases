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

/*
 * InlineIncludeHint.java
 *
 * Created on August 2, 2005, 1:24 PM
 *
 */
package org.netbeans.modules.mobility.editor.hints;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.mobility.editor.actions.RecommentAction;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Adam Sotona
 */
public class InlineIncludeHint implements Fix {
    
    private static File LAST_DIRECTORY;
    
    protected Document doc;
    protected int start, end;
    protected File file;
    
    private String fileName;
    
    
    /** Creates a new instance of InlineIncludeHint */
    public InlineIncludeHint(BaseDocument doc, int start, String fileName) {
        this.doc = doc;
        this.start = start;
        try {
            this.end = Utilities.getRowEnd(doc, start);
        } catch (BadLocationException ble) {
            this.end = start;
        }
        this.fileName = fileName;
    }
    
    private synchronized void findFile() {
        if (file != null) return;
        file = new File(fileName);
        if (file.isFile()) return;
        final Project p = J2MEProjectUtils.getProjectForDocument(doc);
        if (p != null) {
            final Sources src = p.getLookup().lookup(Sources.class);
            if (src != null) {
                file = findInGroups(src, JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (file != null) return;
                file = findInGroups(src, Sources.TYPE_GENERIC);
                if (file != null) return;
            }
        }
    }
    
    private File findInGroups(final Sources src, final String type) {
        final SourceGroup sg[] = src.getSourceGroups(type);
        for (int i=0; i<sg.length; i++) {
            final File root = FileUtil.toFile(sg[i].getRootFolder());
            if (root != null) {
                synchronized (this)
                {
                    if (LAST_DIRECTORY == null) LAST_DIRECTORY = root;
                }
                final File f = new File(root, fileName);
                if (f.isFile()) return f;
            }
        }
        return null;
    }
    
    public synchronized ChangeInfo implement() {
        if (file == null) {
            final JFileChooser fch = new JFileChooser(LAST_DIRECTORY);
            fch.setDialogTitle(NbBundle.getMessage(InlineIncludeHint.class, "Title_SelectFileToInclude")); //NOI18N
            final FF ff = new FF(fileName);
            fch.addChoosableFileFilter(ff);
            fch.setFileFilter(ff);
            if (JFileChooser.APPROVE_OPTION != fch.showOpenDialog(WindowManager.getDefault().getMainWindow())) return null;
            file = fch.getSelectedFile();
            synchronized (this)
            {
                LAST_DIRECTORY = file.getParentFile();
            }
        }
        final String content = readFile();
        NbDocument.runAtomic((StyledDocument)doc, new Runnable() {
            public void run() {
                try {
                    doc.insertString(end, NbBundle.getMessage(InlineIncludeHint.class, "Comment_After_Insert", file.getAbsolutePath()), null); //NOI18N
                    doc.insertString(end, content, null);
                    doc.insertString(end, NbBundle.getMessage(InlineIncludeHint.class, "Comment_Before_Insert", file.getAbsolutePath()), null); //NOI18N
                    doc.remove(start, end - start);
                } catch (BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }
                RecommentAction.actionPerformed(doc);
            }
        });
        return null;
    }
    
	private String readFile() {
        final FileObject fo = FileUtil.toFileObject(file);
        String encoding = (fo != null)  ? (String)fo.getAttribute("Content-Encoding") : null; //NOI18N
        if (encoding == null || encoding.length() == 0) encoding = System.getProperty("file.encoding"); //NOI18N
        final char ch[] = new char[(int)file.length()];
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(new FileInputStream(file), encoding);
            in.read(ch);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ioe) {}
        }
        return new String(ch);
    }
    
//    public int getType() {
//        return ERROR;
//    }
    
    public String getText() {
        findFile();
        if (file == null)
            return NbBundle.getMessage(InlineIncludeHint.class, "HintInlineInclude...", fileName); //NOI18N
        return NbBundle.getMessage(InlineIncludeHint.class, "HintInlineInclude", file.getName(), file.getParent()); //NOI18N
    }
    
    private static class FF extends FileFilter {
        String name;
        public FF(String path) {
            name = path;
            int i = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
            if (i >= 0) name = name.substring(i + 1);
        }
        public boolean accept(final File f) {
            return f.isDirectory() || name.equals(f.getName());
        }
        public String getDescription(){
            return name;
        }
    }
}
