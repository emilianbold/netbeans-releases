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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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







