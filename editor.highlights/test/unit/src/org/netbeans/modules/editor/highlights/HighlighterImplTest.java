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
package org.netbeans.modules.editor.highlights;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class HighlighterImplTest extends NbTestCase {
    
    public HighlighterImplTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testIsReclaimable() throws Exception {
        FileObject dir = makeScratchDir(this);
        FileObject file = dir.createData("test.txt");
        JTextComponent c = new JEditorPane();
        
        HighlighterImpl.getDefault().assureRegistered(c);
        
        Document d = new PlainDocument();
        
        d.putProperty(Document.StreamDescriptionProperty, DataObject.find(file));
        c.setDocument(d);
        
        Reference fileR = new WeakReference(file);
        Reference cR = new WeakReference(c);
        Reference docR = new WeakReference(d);
        
        dir  = null;
        file = null;
        c    = null;
        d    = null;
        
        assertGC("", cR);
        assertGC("", docR);
        
        //it is necessary to touch the HighlighterImpl.comp2FO map to actually free the FileObject:
        HighlighterImpl.getDefault().assureRegistered(new JEditorPane());
        
        assertGC("", fileR);
    }
    
    public boolean runInEQ() {
        return true;
    }
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
}
