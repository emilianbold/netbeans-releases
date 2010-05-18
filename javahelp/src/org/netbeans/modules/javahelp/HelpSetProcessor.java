/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.javahelp;

import java.io.IOException;
import java.net.URL;

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
            } catch (SAXException saxe) {
                throw (IOException) new IOException(saxe.toString()).initCause(saxe);
            } catch (HelpSetException hse) {
                throw (IOException) new IOException(hse.toString()).initCause(hse);
            }
        }
        return hs;
    }
    
}

