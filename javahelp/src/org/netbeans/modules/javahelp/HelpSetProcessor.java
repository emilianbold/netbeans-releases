/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javahelp;

import java.io.IOException;
import java.net.URL;
import javax.swing.BoundedRangeModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.help.HelpSet;
import javax.help.HelpSetException;

import org.openide.TopManager;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;

/** An XML processor for help set references.
 * Provides an instance of javax.swing.HelpSet.
 * @author Jesse Glick
 */
public final class HelpSetProcessor implements XMLDataObject.Processor, InstanceCookie.Of {
    
    /** "context" for merge attribute on helpsets
     */    
    public static final String HELPSET_MERGE_CONTEXT = "OpenIDE"; // NOI18N
    
    /** attribute (type Boolean) on helpsets indicating
     * whether they should be merged into the master or
     * not; by default, true
     */    
    public static final String HELPSET_MERGE_ATTR = "mergeIntoMaster"; // NOI18N

    /** the XML file being parsed
     */
    private XMLDataObject xml;
    
    /** the cached help set
     */
    private HelpSet hs;
    
    /** Bind to an XML file.
     * @param xml the file
     */
    public void attachTo(XMLDataObject xml) {
        if (this.xml == xml) return;
        hs = null;
        // XXX this is called way too often, why?
        this.xml = xml;
        Installer.err.log("processing help set ref: " + xml.getPrimaryFile());
        // XXX event thread?
        BoundedRangeModel pm = Installer.getHelp().getParseModel();
        pm.setMaximum(pm.getMaximum() + 1);
    }
    
    /** Decrement count of available help sets.  */
    protected void finalize() {
        BoundedRangeModel pm = Installer.getHelp().getParseModel();
        pm.setValue(pm.getValue() - 1);
        pm.setMaximum(pm.getMaximum() - 1);
    }
    
    /** The class being produced.
     * @throws IOException doesn't
     * @throws ClassNotFoundException doesn't
     * @return the class of helpsets
     */
    public Class instanceClass() throws IOException, ClassNotFoundException {
        return HelpSet.class;
    }
    
    /** Get the name of the produced class.
     * @return the class of helpsets
     */
    public String instanceName() {
        return "javax.help.HelpSet"; // NOI18N
    }
    
    /** Test whether a given superclass will be produced.
     * @param type the superclass
     * @return true if it is HelpSet
     */
    public boolean instanceOf(Class type) {
        return type == HelpSet.class;
    }
    
    /** Create the help set.
     * @throws IOException if there was a problem parsing the XML
     * of the helpset file or otherwise producing
     * the helpset from its resource
     * @throws ClassNotFoundException doesn't
     * @return the help set
     */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        if (hs == null) {
            Installer.err.log("creating help set from ref: " + xml.getPrimaryFile());
            try {
                Document doc = xml.getDocument();
                Element el = doc.getDocumentElement();
                if (! el.getNodeName().equals("helpsetref")) throw new IOException(); // NOI18N
                String url = el.getAttribute("url"); // NOI18N
                if (url == null || url.equals("")) throw new IOException(); // NOI18N
                String mergeS = el.getAttribute("merge"); // NOI18N
                boolean merge = (mergeS == null) || mergeS.equals("") || // NOI18N
                Boolean.valueOf(mergeS).booleanValue();
                // Make sure nbdocs: protocol is ready:
                Object ignore = NbDocsStreamHandler.class;
                hs = new HelpSet(TopManager.getDefault().systemClassLoader(), new URL(url));
                hs.setKeyData(HELPSET_MERGE_CONTEXT, HELPSET_MERGE_ATTR, new Boolean(merge));
                BoundedRangeModel pm = Installer.getHelp().getParseModel();
                pm.setValue(pm.getValue() + 1);
            } catch (SAXException saxe) {
                IOException ioe = new IOException(saxe.toString());
                Installer.err.annotate(ioe, saxe);
                throw ioe;
            } catch (HelpSetException hse) {
                IOException ioe = new IOException(hse.toString());
                Installer.err.annotate(ioe, hse);
                throw ioe;
            }
        }
        return hs;
    }
    
}

