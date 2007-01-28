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
package org.netbeans.modules.visualweb.designer.markup;

import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarPool;

/**
 * Simple modification to the Xerces DOM parser to get my own Document
 * created when it processes a startElement; my own document will then
 * (a) allow me to keep some state, and more importantly, (b) allow me
 * to implement the StylableElement interface from batik's CSS handling
 * code.
 *
 * @author Tor Norbye
 */
public class RaveDomParser extends org.apache.xerces.parsers.DOMParser {

//    static XMLGrammarPool grammarPool = new XMLGrammarPoolImpl();

    public RaveDomParser(boolean sourceDocument) {
        // I had tried just calling this on the domParser field of the
        // document builder impl, but it's not accessible so I had to
        // create my own parser subclass
        if (sourceDocument) {
            setDocumentClassName("org.netbeans.modules.visualweb.designer.markup.RaveSourceDocument");
            fConfiguration.setProperty (DOCUMENT_CLASS_NAME,
                                  "org.netbeans.modules.visualweb.designer.markup.RaveSourceDocument");
        } else { // XXX render document
            setDocumentClassName("org.netbeans.modules.visualweb.designer.markup.RaveRenderedDocument");
            fConfiguration.setProperty (DOCUMENT_CLASS_NAME,
                                  "org.netbeans.modules.visualweb.designer.markup.RaveRenderedDocument");
        }
        // Enable dtd/grammar caching so we don't keep processing the
        // entity DTD declaration
        //fConfiguration.setProperty(XMLGRAMMAR_POOL, grammarPool);
    }

    protected void setDocumentClassName (String documentClassName) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        boolean reset = false;
        try {
            //ClassLoader may be set to the project's ClassLoader earlier in the
            //call stack when this is being called
            ClassLoader thisClassLoader = this.getClass().getClassLoader();
            if (oldContextClassLoader != thisClassLoader) {
                Thread.currentThread().setContextClassLoader(thisClassLoader);
                reset = true;
            }
            super.setDocumentClassName (documentClassName);
        } finally {
            if (reset) {
                Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            }
        }
    }
}







