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

package org.netbeans.modules.java.source.parsing;

import java.io.Reader;
import java.io.Writer;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public class SourceFileObjectTest extends NbTestCase {
    
    public SourceFileObjectTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDeadlock() throws Exception {
        clearWorkDir();
        
        FileObject work = FileUtil.toFileObject(getWorkDir());
        FileObject data = FileUtil.createData(work, "test.java");
        Document doc = DataObject.find(data).getLookup().lookup(EditorCookie.class).openDocument();
        SourceFileObject sfo = new SourceFileObject(data, work, new FilterImplementation(doc), true);
    }
    
    private static final class FilterImplementation implements JavaFileFilterImplementation {

        private Document doc;

        public FilterImplementation(Document doc) {
            this.doc = doc;
        }
        
        public Reader filterReader(Reader r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public CharSequence filterCharSequence(CharSequence charSequence) {
            try {
                RequestProcessor.Task t = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            doc.insertString(0, "1", null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });

                assertTrue("Deadlock detected.", t.waitFinished(10000));
            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            }
            
            return charSequence;
        }

        public Writer filterWriter(Writer w) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}
