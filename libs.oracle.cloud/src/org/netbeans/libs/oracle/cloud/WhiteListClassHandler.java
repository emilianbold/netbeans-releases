/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.libs.oracle.cloud;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class WhiteListClassHandler extends DefaultHandler2 {

    private String className;
    private String startElement = null;
    private String methodName;
    private String parameter;

    public enum Type {
        Class,
        Extendable,
        Instantiable,
    };
    
    private Type type;
    private String topLevelTag;
    private String classNameTag;
    private String methodBlockNameTag;
    private String methodNameTag;
    private String paramNameTag;
    private boolean empty;

    public WhiteListClassHandler(Type type, String topLevelTag, String classNameTag, String methodBlockNameTag, String methodNameTag, String paramNameTag) {
        this.type = type;
        this.topLevelTag = topLevelTag;
        this.classNameTag = classNameTag;
        this.methodBlockNameTag = methodBlockNameTag;
        this.methodNameTag = methodNameTag;
        this.paramNameTag = paramNameTag;
    }
    
    public void endDocument() throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        startElement = qName;
        if (classNameTag.equals(qName)) {
            className = "";
            methodName = "";
            parameter = "";
        } else if (methodBlockNameTag.equals(qName)) {
            methodName = "";
            parameter = "";
        } else if (paramNameTag.equals(qName)) {
            parameter = "";
        } else if (topLevelTag.equals(qName)) {
            empty = true;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String value = new String(ch, start, length);
        if (classNameTag.equals(startElement)) {
            className += value;
        }

        if (methodNameTag != null && methodNameTag.equals(startElement)) {
            methodName += value;
        }

        if (paramNameTag.equals(startElement)) {
            parameter += value;
        }

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        startElement = null;
        if (methodBlockNameTag.equals(qName)) {
            String args[] = new String[0];
            if (!("()".equals(parameter))) {
                parameter = parameter.substring(1, parameter.length()-1);
                args = parameter.split(", ");
            }
            switch (type) {
                case Class:
                    WhiteListConfigReader.getBuilder().addInvocableMethod(className, methodName, args);
                    invocableMethodCount++;
                    empty = false;
                    break;
                case Extendable:
                    WhiteListConfigReader.getBuilder().addOverridableMethod(className, methodName, args);
                    overridableMethodCount++;
                    break;
                case Instantiable:
                    WhiteListConfigReader.getBuilder().addInvocableMethod(className, "<init>", args);
                    invocableMethodCount++;
                    break;
            }
        } else if (empty &&
                   type == Type.Class &&
                   topLevelTag.equals(qName)) {
            WhiteListConfigReader.getBuilder().addInvocableClass(className);
        }
    }
 
    public static int invocableMethodCount = 0;
    public static int overridableMethodCount = 0;
}