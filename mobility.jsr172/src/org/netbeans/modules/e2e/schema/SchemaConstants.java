/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.e2e.schema;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public interface SchemaConstants {

    public static String SCHEMA_URI = "http://www.w3.org/2001/XMLSchema"; // NOI18N

    /* qnames */
    public static QName SCHEMA              = new QName( SCHEMA_URI, "schema" ); // NOI18N
    public static QName COMPLEX_TYPE        = new QName( SCHEMA_URI, "complexType" ); // NOI18N
    public static QName ELEMENT             = new QName( SCHEMA_URI, "element" ); // NOI18N
    public static QName SEQUENCE            = new QName( SCHEMA_URI, "sequence" ); // NOI18N
    public static QName SIMPLE_TYPE         = new QName( SCHEMA_URI, "simpleType" ); // NOI18N

    /* unsupported */
    public static QName SIMPLE_CONTENT      = new QName( SCHEMA_URI, "simpleContent" ); // NOI18N
    public static QName RESTRICTION         = new QName( SCHEMA_URI, "restriction" ); // NOI18N
    public static QName EXTENSION           = new QName( SCHEMA_URI, "extension" ); // NOI18N
    public static QName ATTRIBUTE           = new QName( SCHEMA_URI, "attribute" ); // NOI18N
    public static QName ATTRIBUTE_GROUP     = new QName( SCHEMA_URI, "attributeGroup" ); // NOI18N
    public static QName ANY_ATTRIBUTE       = new QName( SCHEMA_URI, "anyAttribute" ); // NOI18N
    public static QName COMPLEX_CONTENT     = new QName( SCHEMA_URI, "complexContent" ); // NOI18N
    public static QName ALL                 = new QName( SCHEMA_URI, "all" ); // NOI18N
    public static QName CHOICE              = new QName( SCHEMA_URI, "choice" ); // NOI18N
    public static QName GROUP               = new QName( SCHEMA_URI, "group" ); // NOI18N
    public static QName ANY                 = new QName( SCHEMA_URI, "any" ); // NOI18N
    public static QName ANNOTATION          = new QName( SCHEMA_URI, "annotation" ); // NOI18N

    /* types */
    public static QName TYPE_STRING         = new QName( SCHEMA_URI, "string" ); // NOI18N
    public static QName TYPE_INT            = new QName( SCHEMA_URI, "int" ); // NOI18N
    public static QName TYPE_LONG           = new QName( SCHEMA_URI, "long" ); // NOI18N
    public static QName TYPE_SHORT          = new QName( SCHEMA_URI, "short" ); // NOI18N
    public static QName TYPE_BOOLEAN        = new QName( SCHEMA_URI, "boolean" ); // NOI18N
    public static QName TYPE_BYTE           = new QName( SCHEMA_URI, "byte" ); // NOI18N
    public static QName TYPE_FLOAT          = new QName( SCHEMA_URI, "float" ); // NOI18N
    public static QName TYPE_DOUBLE         = new QName( SCHEMA_URI, "double" ); // NOI18N
    public static QName TYPE_BASE64_BINARY  = new QName( SCHEMA_URI, "base64Binary" ); // NOI18N
    public static QName TYPE_HEX_BINARY     = new QName( SCHEMA_URI, "hexBinary" ); // NOI18N
    public static QName TYPE_QNAME          = new QName( SCHEMA_URI, "QName" ); // NOI18N

}
