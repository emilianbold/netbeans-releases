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

package org.netbeans.modules.javahelp;

import java.io.IOException;
import java.net.URL;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.SwingUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.help.HelpSet;
import javax.help.HelpSetException;

import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;

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

    public static final BoundedRangeModel parseModel = new DefaultBoundedRangeModel(0, 0, 0, 0);
    
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
        Installer.log.fine("processing help set ref: " + xml.getPrimaryFile());
        BPMChanger.invoke(BPMChanger.INC_MAXIMUM);
    }
    
    /** Decrement count of available help sets.  */
    protected void finalize() {
        BPMChanger.invoke(BPMChanger.DEC_VALUE_AND_MAXIMUM);
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
    public synchronized Object instanceCreate() throws IOException, ClassNotFoundException {
        if (hs == null) {
            Installer.log.fine("creating help set from ref: " + xml.getPrimaryFile());
            try {
                Document doc = xml.getDocument();
                Element el = doc.getDocumentElement();
                if (! el.getNodeName().equals("helpsetref")) throw new IOException(); // NOI18N
                String url = el.getAttribute("url"); // NOI18N
                if (url == null || url.equals("")) throw new IOException("no url attr on <helpsetref>! doc.class=" + doc.getClass().getName() + " doc.documentElement=" + el); // NOI18N
                String mergeS = el.getAttribute("merge"); // NOI18N
                boolean merge = (mergeS == null) || mergeS.equals("") || // NOI18N
                Boolean.valueOf(mergeS).booleanValue();
                // Make sure nbdocs: protocol is ready:
                Object ignore = NbDocsStreamHandler.class; // DO NOT DELETE THIS LINE
                hs = new HelpSet(((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class)), new URL(url));
                hs.setKeyData(HELPSET_MERGE_CONTEXT, HELPSET_MERGE_ATTR, merge ? Boolean.TRUE : Boolean.FALSE);
                BPMChanger.invoke(BPMChanger.INC_VALUE);
            } catch (SAXException saxe) {
                throw (IOException) new IOException(saxe.toString()).initCause(saxe);
            } catch (HelpSetException hse) {
                throw (IOException) new IOException(hse.toString()).initCause(hse);
            }
        }
        return hs;
    }
    
    private static final class BPMChanger implements Runnable {
        public static final int INC_MAXIMUM = 0;
        public static final int DEC_VALUE_AND_MAXIMUM = 1;
        public static final int INC_VALUE = 2;
        public static void invoke(int action) {
            SwingUtilities.invokeLater(new BPMChanger(action));
        }
        private final int action;
        private BPMChanger(int action) {
            this.action = action;
        }
        public void run() {
            switch (action) {
            case INC_MAXIMUM:
                parseModel.setMaximum(parseModel.getMaximum() + 1);
                break;
            case DEC_VALUE_AND_MAXIMUM:
                parseModel.setValue(parseModel.getValue() - 1);
                parseModel.setMaximum(parseModel.getMaximum() - 1);
                break;
            case INC_VALUE:
                parseModel.setValue(parseModel.getValue() + 1);
                break;
            default:
                throw new IllegalStateException();
            }
        }
    }
    
}

