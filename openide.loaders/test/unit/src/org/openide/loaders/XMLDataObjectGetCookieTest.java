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
package org.openide.loaders;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.Environment.Provider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;

/** Ensuring that getCookie really works.
 *
 * @author Jaroslav Tulach
 */
public class XMLDataObjectGetCookieTest extends LoggingTestCaseHid 
implements Node.Cookie {

    private ErrorManager err;
    
    public XMLDataObjectGetCookieTest(String s) {
        super(s);
    }
    protected void setUp() throws Exception {
        clearWorkDir();
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
        
        registerIntoLookup(ENV);
    }
    
    public void testGetTheLookupWhileWaitingBeforeAssigningIt() throws IOException {
        doTest(
            "THREAD:t2 MSG:parsedId set to NULL" + 
            "THREAD:t1 MSG:Has already been parsed.*" +
            "THREAD:t1 MSG:Query for class org.openide.loaders.XMLDataObjectGetCookieTest"
        );
    }

    public void testGetTheLookupWhileWaitingAfterParsing() throws IOException {
        doTest(
            "THREAD:t1 MSG:New id.*" +
            "THREAD:t2 MSG:Going to read parseId.*" 
        );
    }
    
    private void doTest(String switches) throws IOException {
        FileObject res = FileUtil.createData(
            Repository.getDefault().getDefaultFileSystem().getRoot(), 
            getName() + "/R.xml"
        );
        
        err.log("file created: " + res);
        org.openide.filesystems.FileLock l = res.lock();
        OutputStream os = res.getOutputStream(l);
        err.log("stream opened");
        PrintStream ps = new PrintStream(os);
        
        ps.println("<?xml version='1.0' encoding='UTF-8'?>");
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println("    <file>");
        ps.println("        <ext name='lenka'/>");
        ps.println("        <resolver mime='hodna/lenka'/>");
        ps.println("    </file>");
        ps.println("</MIME-resolver>");

        err.log("Content written");
        os.close();
        err.log("Stream closed");
        l.releaseLock();
        err.log("releaseLock");
    
        
        final DataObject obj = DataObject.find(res);
        
        class Run implements Runnable {
            public EP cookie;
            
            public void run () {
                cookie = (EP) obj.getCookie(EP.class);
            }
        }
        
        Run r1 = new Run();
        Run r2 = new Run();
        
        
        registerSwitches(switches, 200);
        
        RequestProcessor.Task t1 = new RequestProcessor("t1").post(r1);
        RequestProcessor.Task t2 = new RequestProcessor("t2").post(r2);
        
        t1.waitFinished();
        t2.waitFinished();
        
        if (r1.cookie == null && r2.cookie == null) {
            fail("Both cookies are null");
        }
        
        assertEquals("First result is ok", ENV, r1.cookie);
        assertEquals("Second result is ok", ENV, r2.cookie);
    }
    
    
    
    private static Object ENV = new EP();
        
    private static final class EP implements Environment.Provider, Node.Cookie {
        public Lookup getEnvironment(DataObject obj) {
            assertEquals("Right object: ", XMLDataObject.class, obj.getClass());
            XMLDataObject xml = (XMLDataObject)obj;
            String id = null;
            try {
                id = xml.getDocument().getDoctype().getPublicId();
            } catch (IOException ex) {
                ex.printStackTrace();
                fail("No exception");
            } catch (SAXException ex) {
                ex.printStackTrace();
                fail("No exception");
            }
            assertEquals("-//NetBeans//DTD MIME Resolver 1.0//EN", id);
            return Lookups.singleton(this);
        }
    };
}
