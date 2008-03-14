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

package com.sun.data.provider.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.ResultSetMetaData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JohnBaker
 */
public class MetaDataSerializer {
    private static Logger LOGGER = Logger.getLogger(CachedRowSetDataProvider.class.getName());
    
    /**
     * Creates a new folder in the userdir and if needed and generates a new serialized filename
     * @param serFileName name of file used to generate an absolute filename
     * @return an absolute filename
     */
    public String generateMetaDataName(String serFileName) {      
        File cachedMetadataUserdirFolder = new File(System.getProperty("netbeans.user") + File.separator + "config" + File.separator + "Databases" +  File.separator + "CachedMetadata"); // NOI18N
        if (!cachedMetadataUserdirFolder.exists()) {
            cachedMetadataUserdirFolder.mkdir();
        }
        
        return cachedMetadataUserdirFolder + File.separator + serFileName + ".ser";    // NOI18N           
    }
    
    /**
     * Checks if file containing a serialized object exists
     * @param mdFileName absolute filename 
     * @return
     */
    public boolean mdFileNameExists(String mdFileName) {
        mdFileName = mdFileName.replaceAll("\\n", ""); // NOI18N
        return new File(mdFileName).exists();
    }
    
    /**
     * Serializes an instance of ResultSetMetaData and writes the object to a file
     * @param resultSetMetaData metadata to serialized
     * @param mdFileName absolute filename
     */
    public void serialize(ResultSetMetaData resultSetMetaData, String mdFileName) {
        mdFileName = mdFileName.replaceAll("\\n", ""); // NOI18N
        if (resultSetMetaData != null) {
            ObjectOutputStream os = null;
            try {    
                os = new ObjectOutputStream(new FileOutputStream(mdFileName));
                os.writeObject(resultSetMetaData);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    // if new FileOutputStream(mdFileName) is null (due to FileNotFoundException) then os would be null and no stream would be opened for writing
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }
}

