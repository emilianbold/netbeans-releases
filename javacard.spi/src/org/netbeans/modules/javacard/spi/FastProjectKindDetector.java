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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.spi;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Use SAX to determine project type - much better performance than XPath or DOM
 * in the critical path of project creation.
 *
 * @author Tim Boudreau
 */
final class FastProjectKindDetector extends DefaultHandler {
    private String[] tags = new String[10];
    private int currDepth = 0;
    private boolean inProjectTypeTag;
    private boolean inTypeTag;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tags[currDepth] = qName;
        currDepth++;
        if ("property".equals(qName)) { //NOI18N
            if ("javacard.project.subtype".equals(attributes.getValue("name"))) { //NOI18N
                inProjectTypeTag = true;
            }
        } else if ("type".equals(qName)) { //NOI18N
            inTypeTag = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        tags[currDepth] = null;
        currDepth--;
        inProjectTypeTag = false;
        inTypeTag = false;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inTypeTag) {
            String val = new String (ch, start, length);
            if (!"org.netbeans.modules.javacard.JCPROJECT".equals(val)) { //NOI18N
                throw new SAXException ("Not a javacard project: " + val); //NOI18N
            }
        }
        if (inProjectTypeTag) {
            String type = new String (ch, start, length);
            throw new FoundProjectTypeException(type);
        }
    }

    //Control flow by exception - vewwwy vewwy naughty.  But faster than
    //parsing the whole file.
    static final class FoundProjectTypeException extends SAXException {

        private FoundProjectTypeException (String type) {
            super (type);
        }

        //These two methods are what make throwing exceptions expensive if
        //the stack is deep - NOP them
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

        @Override
        public StackTraceElement[] getStackTrace() {
            return new StackTraceElement[0];
        }
    }
}
