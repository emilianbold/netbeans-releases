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

package org.netbeans.api.xml.parsers;

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.text.Document;

import org.xml.sax.InputSource;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * Integrate NetBeans widely used Swing's {@link Document} with SAX API's.
 * Let it look like {@link InputSource}.
 *
 * @author  Petr Kuzel
 */
public final class DocumentInputSource extends InputSource {

    private final Document doc;
     
    /** 
     * Creates new instance of <code>DocumentInputSource</code>. Client should
     * set system ID if available otherwise default one is derived.
     * @param doc Swing document used to be wrapped
     * @see   #getSystemId()
     */
    public DocumentInputSource(Document doc) {
        this.doc = doc;
    }

    // inherit JavaDoc
    public Reader getCharacterStream() {
        String text = documentToString(doc);
        return new StringReader(text);
    }

    /**
     * This <code>InputSource</code> is backended by Swing's <code>Document</code>.
     * Consequently its character stream is read-only, it
     * always reads content of associted <code>Document</code>.
     */
    public final void setCharacterStream(Reader reader) {
        // do nothing
    }

    /**
     * Get InputSource system ID. Use ordered logic:
     * <ul>
     *  <li>use client's <code>setSystemId()</code>, or
     *  <li>try to derive it from <code>Document</code>
     *      <p>e.g. look at <code>Document.StreamDescriptionProperty</code> for
     *      {@link DataObject} and use URL of its primary file.
     * </ul>
     * @return entity system Id or <code>null</code>
     */
    public String getSystemId() {
        
        String system = super.getSystemId();;
        
        // XML module specifics property, promote into this API
//        String system = (String) doc.getProperty(TextEditorSupport.PROP_DOCUMENT_URL);        
        
        if (system == null) {
            Object obj = doc.getProperty(Document.StreamDescriptionProperty);        
            if (obj instanceof DataObject) {
                try { 
                        DataObject dobj = (DataObject) obj;
                        FileObject fo = dobj.getPrimaryFile();
                        URL url = fo.getURL();
                        system = url.toExternalForm();
                } catch (IOException io) {
                    ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);
                    emgr.notify(io);
                }
            } else {
                ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);
                emgr.log("XML:DocumentInputSource:Unknown stream description:" + obj);
            }
        }
        
        return system;
    }
        
    
    /**
     * @return current state of Document as string
     */
    private static String documentToString(final Document doc) {
        
        final String[] str = new String[1];

        // safely take the text from the document
        Runnable run = new Runnable() {
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    e.printStackTrace();
                }
            }
        };

        doc.render(run);
        return str[0];
        
    }
    
    /**
     * For debugging purposes only.
     */
    public String toString() {
        return "DocumentInputSource SID:" + getSystemId();
    }
}
