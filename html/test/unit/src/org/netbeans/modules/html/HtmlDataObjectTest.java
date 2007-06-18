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

package org.netbeans.modules.html;

import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach
 */
public class HtmlDataObjectTest extends TestCase {
    @SuppressWarnings("deprecation")
    private static void init() {
        FileUtil.setMIMEType("html", "text/html");
    }
    
    static {
        init();
    }
    
    public HtmlDataObjectTest(String testName) {
        super(testName);
    }

    
    public void testConstructorHasToRunWithoutChildrenLockBeingNeeded() throws Exception {
        MockServices.setServices(HtmlLoader.class);
        
        
        class Block implements Runnable {
            public void run() {
                if (!Children.MUTEX.isReadAccess()) {
                    Children.MUTEX.readAccess(this);
                    return;
                }
                synchronized (this) {
                    try {
                        notifyAll();

                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        Block b = new Block();
        
        synchronized (b) {
            RequestProcessor.getDefault().post(b);
            b.wait();
        }
        
        try {
        
            FileObject fo = FileUtil.createData(Repository.getDefault().getDefaultFileSystem().getRoot(), "my.html");
            DataObject obj = DataObject.find(fo);
            assertEquals("Successfully created html object", obj.getClass(), HtmlDataObject.class);
            assertNotNull("File encoding query is in the object's lookup", obj.getLookup().lookup(FileEncodingQueryImplementation.class));
        } finally {
            synchronized (b) {
                b.notifyAll();
            }
        }
    } 
    
    
}
