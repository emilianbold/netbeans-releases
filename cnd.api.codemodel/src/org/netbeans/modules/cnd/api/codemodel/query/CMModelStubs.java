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
package org.netbeans.modules.cnd.api.codemodel.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMModel;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntity;
import org.netbeans.modules.cnd.api.codemodel.visit.CMReference;
import org.openide.util.Exceptions;

/**
 * A placeholder for methods that are absent in current API
 *
 * @author Vladimir Kvashin
 */
public class CMModelStubs{

    private CMModelStubs() {}

    public static CharSequence getReferenceText(CMReference ref) {
        return ref.getReferencedEntity().toString();
    }

    public static CharSequence getText(CMSourceRange range) {
        throw new UnsupportedOperationException(); //TODO: implement!
    }

    public static CharSequence getSignature(CMEntity entity) {
        new UnsupportedOperationException("getSignature is not supported yet").printStackTrace(); //NOI18N
        return entity.getName().toString();
    }

    public static CharSequence getQualifiedName(CMEntity entity) {
        new UnsupportedOperationException().printStackTrace();
        return entity.getName();
    }

    public static boolean isFile(CMEntity entity) {
        new UnsupportedOperationException("isFile not supported yet").printStackTrace(); //NOI18N
        return false;
    }

    public static boolean isMacro(CMEntity entity) {
        new UnsupportedOperationException("isMacro not supported yet").printStackTrace(); //NOI18N
        return false;
    }

    public static CMEntity getSemanticParent(CMEntity child) {
        new UnsupportedOperationException("getSemanticParent not supported yet").printStackTrace(); //NOI18N
        return null;
    }

    public static CMIndex getIndex(CMEntity entity) {
        Iterator<CMIndex> iterator = CMModel.getIndices().iterator();
        return iterator.hasNext() ?  iterator.next() : null;
    }

    public static CharSequence getDisplayName(CMEntity entity) {
        if (CMKindUtilities.isFunction(entity)) {
            // TODO: how to get function parameters?
        }
        try {
            return entity.getName().toString();
        } catch (Throwable e) {
            e.printStackTrace();
            return "Display name is NPE";
        }
    }

    public static boolean isVirtual(CMEntity referencedObject) {
        return false;
    }
    
    public static int getLineCount(CMTranslationUnit file) {
        throw new UnsupportedOperationException();
    }
 
    public enum FileKind {
        SOURCE_FILES,
        USER_HEADER_FILES,
        SYSTEM_HEADER_FILES;
    }
    
    public static Collection<CMFile> getIndexedFiles(CMIndex idx, FileKind... kinds) {
        // for now return all main files from translation units
        Collection<CMFile> res = new ArrayList<>();
        for (CMTranslationUnit tu : idx.getTranslationUnits()) {
            res.add(tu.getFile(new File(tu.getMainFilePath().toString()).toURI()));
        }
        return res;
    }
    
    public static int getLineCount(CMFile file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file.getFilePath().toString()));
            int lines = 0;
            while (reader.readLine() != null) {
                lines++;
            }
            return lines;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return -1;
    }

    public static Collection<CMEntity> findDeclarationByName(CMIndex index, CharSequence functionName) {
        throw new UnsupportedOperationException();
    }
    
    public static boolean isIncluded(CMFile file) {
        return false;
    }
}
