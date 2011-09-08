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

package org.netbeans.modules.e2e.wsdl;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public interface WSDLConstants {
        
    public static final String WSDL_URI    = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
    
    public static final QName TYPES         = new QName( WSDL_URI, "types" ); // NOI18N
    public static final QName DEFINITIONS   = new QName( WSDL_URI, "definitions" ); // NOI18N
    public static final QName MESSAGE       = new QName( WSDL_URI, "message" ); // NOI18N
    public static final QName PART          = new QName( WSDL_URI, "part" ); // NOI18N
    public static final QName SERVICE       = new QName( WSDL_URI, "service" ); // NOI18N
    public static final QName PORT          = new QName( WSDL_URI, "port" ); // NOI18N
    public static final QName PORT_TYPE     = new QName( WSDL_URI, "portType" ); // NOI18N
    public static final QName OPERATION     = new QName( WSDL_URI, "operation" ); // NOI18N
    public static final QName BINDING       = new QName( WSDL_URI, "binding" ); // NOI18N
    
    public static final QName INPUT         = new QName( WSDL_URI, "input" ); // NOI18N
    public static final QName OUTPUT        = new QName( WSDL_URI, "output" ); // NOI18N
    public static final QName FAULT         = new QName( WSDL_URI, "fault" ); // NOI18N

    public static final QName IMPORT        = new QName( WSDL_URI, "import" ); // NOI18N
    
    public static final QName DOCUMENTATION = new QName( WSDL_URI, "documentation" ); // NOI18N
}
