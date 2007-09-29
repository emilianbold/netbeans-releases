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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.api;

import java.io.*;
import java.nio.charset.Charset;
import javax.swing.text.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.xml.core.lib.EncodingHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * XML uses inband encoding detection - this class obtains it.
 *
 * @author  Petr Jiricka
 * @version 1.0
 */
public class EncodingUtil {

    /**
     * Detect input stream encoding.
     * The stream stays intact.
     * @return java encoding names ("UTF8", "ASCII", etc.) or null
     * if the stream is not markable or enoding cannot be detected.
     */
    public static String detectEncoding(InputStream in) throws IOException {
        return EncodingHelper.detectEncoding(in);
    }
        
    
    /** Document itself is encoded as Unicode, but in
     * the document prolog is an encoding attribute.
     * @return java encoding names ("UTF8", "ASCII", etc.) or null if no guess
     */
    public static String detectEncoding(Document doc) throws IOException {
        return EncodingHelper.detectEncoding(doc);
    }
    
    /**
     * Checks the validity of an encoding string.
     */
    public static boolean isValidEncoding(String encoding) {
        boolean valid = true;
        try {
            Charset.forName(encoding);
        } catch (Exception ex) {
            valid = false;
        }
        return valid;
    }
    
    /**
     * Finds the project level encoding for the specified file.
     */
    public static String getProjectEncoding(final FileObject file) throws IOException {
        try {
            final Project project = FileOwnerQuery.getOwner(file);
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<String>() {
                public String run() throws IOException {
                    FileObject propertiesFo = project.getProjectDirectory().
                            getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties ep = null;
                    if (propertiesFo != null) {
                        InputStream is = null;
                        ep = new EditableProperties();
                        try {
                            is = propertiesFo.getInputStream();
                            ep.load(is);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                    }
                    return ep.getProperty("source.encoding"); //NOI18N
                }
            });
        } catch (MutexException ex) {
            return null;
        }
    }

}
