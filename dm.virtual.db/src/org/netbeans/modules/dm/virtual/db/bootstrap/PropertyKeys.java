/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.bootstrap;

/**
 * @author Ahimanikya Satapathy
 */
public interface PropertyKeys {

    public static final String WIZARDCUSTOMFIELDDELIMITER = "WIZARDCUSTOMFIELDDELIMITER"; // NOI18N
    public static final String FIELDDELIMITER = "FIELDDELIMITER"; // NOI18N
    public static final String FILENAME = "FILENAME"; // NOI18N
    public static final String HEADERBYTESOFFSET = "HEADERBYTESOFFSET"; // NOI18N
    public static final String ISFIRSTLINEHEADER = "ISFIRSTLINEHEADER"; // NOI18N
    public static final String CREATE_IF_NOT_EXIST = "CREATE_IF_NOT_EXIST"; // NOI18N

    /* Constant: name of loadtype property ('DELIMITED', 'FIXEDWIDTH', etc.) */
    public static final String LOADTYPE = "LOADTYPE"; // NOI18N
    public static final String DBLINK = "DBLINK";
    public static final String DELIMITED = "Delimited";
    public static final String FIXEDWIDTH = "FixedWidth";
    public static final String RSS = "RSS";
    public static final String WEB = "WEB";
    public static final String XML = "XML";
    public static final String WEBROWSET = "WEBROWSET";
    public static final String SPREADSHEET = "SPREADSHEET";
    public static final String REMOTETABLE = "REMOTETABLE";
    public static final String JDBC = "REMOTE";
    public static final String TABLENUMBER = "TABLENUMBER";
    public static final String REFRESH = "REFRESH";
    public static final String URL = "URL";
    public static final String TYPE = "TYPE";
    public static final String ROWNAME = "ROWNAME";
    public static final String SHEET = "SHEET";
    public static final String READONLY = "READONLY";
    public static final String READWRITE = "READWRITE";
    public static final String MAXFAULTS = "MAXFAULTS"; // NOI18N
    public static final String QUALIFIER = "QUALIFIER"; // NOI18N
    public static final String RECORDDELIMITER = "RECORDDELIMITER"; // NOI18N
    public static final String ROWSTOSKIP = "ROWSTOSKIP"; // NOI18N
    public static final String WIZARDDEFAULTPRECISION = "WIZARDDEFAULTPRECISION"; // NOI18N
    public static final String WIZARDDEFAULTSQLTYPE = "WIZARDDEFAULTSQLTYPE"; // NOI18N
    public static final String WIZARDFIELDCOUNT = "WIZARDFIELDCOUNT"; // NOI18N
    public static final String WIZARDRECORDLENGTH = "WIZARDRECORDLENGTH"; // NOI18N
    public static final String WIZARDFILEPATH = "WIZARDFILEPATH"; // NOI18N
    public static final String TRIMWHITESPACE = "TRIMWHITESPACE";
}
