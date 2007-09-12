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

package org.netbeans.modules.wsdlextensions.file.model.impl;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author sweng
 */
public enum FileAttribute implements Attribute {
    FILE_ADDRESS_FILEDIRECTORY_PROPERTY("fileDirectory"),
    FILE_ADDRESS_RELATIVEPATH_PROPERTY("relativePath"),
    FILE_ADDRESS_PATHRELATIVETO_PROPERTY("pathRelativeTo"),
    FILE_ADDRESS_LOCK_NAME("lockName"),
    FILE_ADDRESS_WORK_AREA("workArea"),
    FILE_ADDRESS_SEQ_NAME("seqName"),
    
    FILE_MESSAGE_FILETYPE_PROPERTY("fileType"),
    FILE_MESSAGE_ENCODINGSTYLE_PROPERTY("encodingStyle"),
    FILE_MESSAGE_USE_PROPERTY("use"),
    FILE_MESSAGE_PART_PROPERTY("part"),
    FILE_MESSAGE_POLLINTERVAL_PROPERTY("pollingInterval"),
    FILE_MESSAGE_FILENAME_PROPERTY("fileName"),
    FILE_MESSAGE_FILENAMEISPATTERN_PROPERTY("fileNameIsPattern"),
    FILE_MESSAGE_REMOVEOL_PROPERTY("removeEOL"),
    FILE_MESSAGE_ADDEOL_PROPERTY("addEOL"),
    FILE_MESSAGE_MULTIPLERECORDSPERFILE_MESSAGE__PROPERTY("multipleRecordsPerFile"),
    FILE_MESSAGE_RECORDDELIMITER_PROPERTY("recordDelimiter"),
    FILE_MESSAGE_MAXBYTESPERRECORD_PROPERTY("maxBytesPerRecord"),
    FILE_MESSAGE_PROTECT_PROPERTY("protect"),
    FILE_MESSAGE_ARCHIVE_PROPERTY("archive"),
    FILE_MESSAGE_STAGE_PROPERTY("stage"),
    FILE_MESSAGE_PROTECT_DIR_PROPERTY("protectDirectory"),
    FILE_MESSAGE_ARCHIVE_DIR_PROPERTY("archiveDirectory"),
    FILE_MESSAGE_STAGE_DIR_PROPERTY("stageDirectory"),
    FILE_MESSAGE_PROTECT_DIR_IS_RELATIVE("protectDirIsRelative"),
    FILE_MESSAGE_ARCHIVE_DIR_IS_RELATIVE("archiveDirIsRelative"),
    FILE_MESSAGE_STAGE_DIR_IS_RELATIVE("stageDirIsRelative");
    

    private String name;
    private Class type;
    private Class subtype;
    
    FileAttribute(String name) {
        this(name, String.class);
    }
    
    FileAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    FileAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { return name; }
    
    public Class getType() { return type; }
    
    public String getName() { return name; }
    
    public Class getMemberType() { return subtype; }
}
