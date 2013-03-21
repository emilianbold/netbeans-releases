/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.analysis.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.refactoring.api.Scope;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public final class AnalysisUtils {

    private static final String SERIALIZE_DELIMITER = "|"; // NOI18N


    private AnalysisUtils() {
    }

    public static String serialize(List<String> input) {
        return StringUtils.implode(input, SERIALIZE_DELIMITER);
    }

    public static List<String> deserialize(String input) {
        return StringUtils.explode(input, SERIALIZE_DELIMITER);
    }

    public static Map<FileObject, Integer> countPhpFiles(Scope scope) {
        Map<FileObject, Integer> counts = new HashMap<FileObject, Integer>();
        for (FileObject root : scope.getSourceRoots()) {
            counts.put(root, countPhpFiles(root, true));
        }
        for (FileObject file : scope.getFiles()) {
            counts.put(file, countPhpFiles(file, true));
        }
        for (NonRecursiveFolder nonRecursiveFolder : scope.getFolders()) {
            FileObject folder = nonRecursiveFolder.getFolder();
            counts.put(folder, countPhpFiles(folder, false));
        }
        return counts;
    }

    // XXX remove and use new api method from ErrorDescriptionFactory
    public static int[] computeLineMap(FileObject file, Charset decoder) {
        Reader in = null;
        List<Integer> lineLengthsTemp = new ArrayList<Integer>();
        int currentOffset = 0;

        lineLengthsTemp.add(0);
        lineLengthsTemp.add(0);

        try {
            in = new InputStreamReader(file.getInputStream(), decoder);

            int read;
            boolean wascr = false;
            boolean lineStart = true;

            while ((read = in.read()) != (-1)) {
                currentOffset++;

                switch (read) {
                    case '\r':
                        wascr = true;
                        lineLengthsTemp.add(currentOffset);
                        lineLengthsTemp.add(currentOffset);
                        lineStart = true;
                        break;
                    case '\n':
                        if (wascr) {
                            wascr = false;
                            currentOffset--;
                            break;
                        }
                        lineLengthsTemp.add(currentOffset);
                        lineLengthsTemp.add(currentOffset);
                        wascr = false;
                        lineStart = true;
                        break;
                    default:
                        // noop
                }

                if (lineStart && Character.isWhitespace(read)) {
                    lineLengthsTemp.set(lineLengthsTemp.size() - 2, currentOffset);
                    lineLengthsTemp.set(lineLengthsTemp.size() - 1, currentOffset);
                } else if (!Character.isWhitespace(read)) {
                    lineLengthsTemp.set(lineLengthsTemp.size() - 1, currentOffset);
                    lineStart = false;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        int[] lineOffsets = new int[lineLengthsTemp.size()];
        int i = 0;

        for (Integer o : lineLengthsTemp) {
            lineOffsets[i++] = o;
        }

        return lineOffsets;
    }

    private static int countPhpFiles(FileObject fileObject, boolean recursive) {
        int count = 0;
        if (FileUtils.isPhpFile(fileObject)) {
            count++;
        }
        Enumeration<? extends FileObject> children = fileObject.getChildren(recursive);
        while (children.hasMoreElements()) {
            FileObject child = children.nextElement();
            if (FileUtils.isPhpFile(child)) {
                count++;
            }
        }
        return count;
    }

}
