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

package org.openide.loaders;

import org.openide.filesystems.*;
import java.io.*;
import org.openide.cookies.*;

/**
 *
 * @author  Jaroslav Tulach
 */
public class XMLDataObjectTest extends org.netbeans.junit.NbTestCase {
    private FileObject data;
    
    /** Creates new MultiFileLoaderHid */
    public XMLDataObjectTest (String name) {
        super (name);
    }
    
    public static void main (String[] args) throws Exception {
         junit.textui.TestRunner.run(new org.netbeans.junit.NbTestSuite(XMLDataObjectTest.class));
    }    
    
    protected void setUp () throws Exception {
        super.setUp ();
        System.setProperty ("org.openide.util.Lookup", "org.openide.loaders.XMLDataObjectTest$Lkp");
        String fsstruct [] = new String [] {
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        data = FileUtil.createData (
            fs.getRoot (),
            "kuk/test/my.xml"
        );
        FileLock lock = data.lock ();
        OutputStream os = data.getOutputStream (lock);
        PrintStream p = new PrintStream (os);
        
        p.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        p.println ("<root>");
        p.println ("</root>");
        
        p.close ();
        lock.releaseLock ();
    }
    
    protected void tearDown () throws Exception {
        super.tearDown ();
        TestUtilHid.destroyLocalFileSystem (getName());
    }
    
    public void testGetStatusBehaviour () throws Exception {
        DataObject obj = DataObject.find (data);
        
        assertEquals ("Is xml", XMLDataObject.class, obj.getClass ());
        
        XMLDataObject xml = (XMLDataObject)obj;
        
        assertEquals ("not parsed yet", xml.STATUS_NOT, xml.getStatus ());
        
        org.w3c.dom.Document doc = xml.getDocument ();
        assertEquals ("still not parsed as we have lazy document", xml.STATUS_NOT, xml.getStatus ());
        
        String id = doc.getDoctype ().getPublicId ();
        assertEquals ("still not parsed as we have special support for publilc id", xml.STATUS_NOT, xml.getStatus ());
        
        org.w3c.dom.Element e = doc.getDocumentElement ();
        assertNotNull ("Document parsed", doc);
        
        assertEquals ("status is ok", xml.STATUS_OK, xml.getStatus ());
    }

    public void testCookieIsUpdatedWhenContentChanges () throws Exception {
        
        
        FileLock lck;
        DataObject obj;
        lck = data.lock();
        
        PCL pcl = new PCL ();
        
        // this next line causes the test to fail on 2004/03/03
        obj = DataObject.find (data);
        obj.addPropertyChangeListener (pcl);
        
        assertNull ("No instance cookie", obj.getCookie (org.openide.cookies.InstanceCookie.class));
        assertEquals (0, pcl.cnt);
        
        try {
            OutputStream ostm = data.getOutputStream(lck);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE driver PUBLIC '-//NetBeans//DTD JDBC Driver 1.0//EN' 'http://www.netbeans.org/dtds/jdbc-driver-1_0.dtd'>"); //NOI18N
            pw.println("<driver>"); //NOI18N
            pw.println("  <name value='somename'/>"); //NOI18N
            pw.println("  <class value='java.lang.String'/>"); //NOI18N
            pw.println("  <urls>"); //NOI18N
            pw.println("  </urls>"); //NOI18N
            pw.println("</driver>"); //NOI18N
            pw.flush();
            pw.close();
            ostm.close();
        } finally {
            lck.releaseLock();
        }
        assertEquals ("One change fired when the file was written", 1, pcl.cnt);
        assertNotNull ("There is an cookie", obj.getCookie (org.openide.cookies.InstanceCookie.class));
    }
    
    public void testToolbarsAreBrokenAsTheLookupIsClearedTooOftenIssue41360 () throws Exception {
        FileLock lck;
        DataObject obj;
        lck = data.lock();
        String id = "-//NetBeans//DTD Fake Toolbar 1.0//EN";
        
        XMLDataObject.Info info = new XMLDataObject.Info ();
        info.addProcessorClass (ToolbarProcessor.class);
        try {
            XMLDataObject.registerInfo (id, info);
            
            
            OutputStream ostm = data.getOutputStream(lck);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE toolbar PUBLIC '" + id + "' 'http://www.netbeans.org/dtds/jdbc-driver-1_0.dtd'>"); //NOI18N
            pw.println("<toolbar>"); //NOI18N
            pw.println("  <name value='somename'/>"); //NOI18N
            pw.println("  <class value='java.lang.String'/>"); //NOI18N
            pw.println("  <urls>"); //NOI18N
            pw.println("  </urls>"); //NOI18N
            pw.println("</toolbar>"); //NOI18N
            pw.flush();
            pw.close();
            ostm.close();
            
            obj = DataObject.find (data);
            PCL pcl = new PCL ();
            obj.addPropertyChangeListener (pcl);
            
            InstanceCookie cookie = (InstanceCookie)obj.getCookie (InstanceCookie.class);
            assertNotNull (cookie);
            assertEquals ("No changes yet", 0, pcl.cnt);
            
            ostm = data.getOutputStream(lck);
            pw = new java.io.PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE toolbar PUBLIC '" + id + "' 'http://www.netbeans.org/dtds/jdbc-driver-1_0.dtd'>"); //NOI18N
            pw.println("<toolbar>"); //NOI18N
            pw.println("  <name value='somename'/>"); //NOI18N
            pw.println("  <class value='java.lang.String'/>"); //NOI18N
            pw.println("  <urls>"); //NOI18N
            pw.println("  </urls>"); //NOI18N
            pw.println("</toolbar>"); //NOI18N
            pw.flush();
            pw.close();
            ostm.close();
            
            InstanceCookie newCookie = (InstanceCookie)obj.getCookie (InstanceCookie.class);
            assertNotNull (newCookie);
            assertEquals ("One change in document", 1, pcl.docChange);
            assertEquals ("The cookie is still the same", cookie, newCookie);
            assertEquals ("No cookie change", 0, pcl.cnt);
            
        } finally {
            XMLDataObject.registerInfo (id, null);
            lck.releaseLock ();
        }
    }
    
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup 
    implements org.openide.loaders.Environment.Provider {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (this); // Environment.Provider
        }
        
        public org.openide.util.Lookup getEnvironment (org.openide.loaders.DataObject obj) {
            if (obj instanceof XMLDataObject) {
                try {
                    XMLDataObject xml = (XMLDataObject)obj;
                    final String id = xml.getDocument ().getDoctype ().getPublicId ();
                    if (id != null) {
                        return org.openide.util.lookup.Lookups.singleton (new org.openide.cookies.InstanceCookie () {
                            public Object instanceCreate () {
                                return id;
                            }
                            public Class instanceClass () {
                                return String.class;
                            }
                            public String instanceName () {
                                return instanceClass ().getName ();
                            }
                        });
                    }
                } catch (Exception ex) {
                    fail (ex.getMessage ());
                }
            }
            return null;
        }
        
    } // end of Lkp

    
    /** Processor.
     */
    public static class ToolbarProcessor 
    implements XMLDataObject.Processor, InstanceCookie.Of {
        
        public void attachTo (org.openide.loaders.XMLDataObject xmlDO) {
        }
        
        public Class instanceClass () throws java.io.IOException, ClassNotFoundException {
            return getClass ();
        }
        
        public Object instanceCreate () throws java.io.IOException, ClassNotFoundException {
            return this;
        }
        
        public String instanceName () {
            return getClass ().getName ();
        }
        
        public boolean instanceOf (Class type) {
            return type.isAssignableFrom (getClass());
        }
        
    } // end of ToolbarProcessor

    private static class PCL implements java.beans.PropertyChangeListener {
        int cnt;
        int docChange;

        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            if (DataObject.PROP_COOKIE.equals (ev.getPropertyName ())) {
                cnt++;
            }
            if (XMLDataObject.PROP_DOCUMENT.equals (ev.getPropertyName ())) {
                docChange++;
            }
        }
    } // end of PCL
    
}
