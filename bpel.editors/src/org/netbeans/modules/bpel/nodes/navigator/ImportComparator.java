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
package org.netbeans.modules.bpel.nodes.navigator;

import java.util.Comparator;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class ImportComparator implements Comparator<Import> {
    private static final int EQUALS = 0;
    private static final int GREATER = 1;
    private static final int LESS = -1;
    private static final String PATH_SLASH = "/";
//    private static ImportComparator COMPARATOR;
    private Lookup myLookup;
    
    public ImportComparator(Lookup lookup) {
        myLookup = lookup;
    }
    
    private Lookup getLookup() {
        return myLookup;
    }
    
//    public synchronized static ImportComparator getInstance(Lookup lookup) {
//        if (COMPARATOR == null) {
//            COMPARATOR = new ImportComparator(lookup);
//        }
//        return COMPARATOR;
//    }
    
    public int compare(Import o1, Import o2) {
        assert o1 != null;
        assert o2 != null;
        if (o1.equals(o2)) {
            return EQUALS;
        }
        
        String o1Path = getRelativePath(o1);
        String o2Path = getRelativePath(o2);
        String[] o1StArray = o1Path.split(PATH_SLASH);
        String[] o2StArray = o2Path.split(PATH_SLASH);
        if (o1StArray.length != o2StArray.length) {
            return o1StArray.length > o2StArray.length ? GREATER : LESS;
        }
        
        for (int i = 0; i < o1StArray.length; i++) {
            if (! o1StArray[i].equals(o2StArray[i])) {
                return o1StArray[i].compareTo(o2StArray[i]);
            }
        }
        
//            for (int i = 0; i < o1Path.length(); i++) {
//                char o1Char = o1Path.charAt(i);
//                if (i < o2Path.length()) {
//                    char o2Char = o2Path.charAt(i);
//                    if (o1Char != o2Char) {
//                        if (o1Char == '/') {
//                            return GREATER;
//                        }
//
//                        if (o2Char == '/') {
//                            return LESS;
//                        }
//
//                    }
//                } else {
//                    return LESS;
//                }
//            }
        return EQUALS;
    }
    
    /**
     * In case of the imported file is corrupted/renamed/moved/deleted 
     * the method returns the text "[invalid] importLocation"
     */ 
    private String getRelativePath(Import imprt) {
        assert imprt != null;
        StringBuffer result = new StringBuffer();
        FileObject fo = ResolverUtility.getImportedFile(imprt.getLocation(), getLookup());
        if (fo != null && fo.isValid()) {
            return ResolverUtility.calculateRelativePathName(fo, getLookup());
        } else {
            String location = ResolverUtility.decodeLocation(imprt.getLocation());
            return "[" + Constants.MISSING + "] " + location; // NOI18N
        }
    }
}
