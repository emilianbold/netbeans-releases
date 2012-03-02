/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.content.file;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class FileContentSignature {

    private final List<CharSequence> signature;
    private final CsmUID<CsmFile> file;
    private final int hashCode;

    private FileContentSignature(List<CharSequence> signature, CsmUID<CsmFile> file) {
        this.signature = signature;
        this.file = file;
        this.hashCode = hash(signature);
    }

    public static FileContentSignature create(CsmFile file) {
        List<CharSequence> signature = createFileSignature(file);
        return new FileContentSignature(signature, UIDCsmConverter.fileToUID(file));
    }

    private static List<CharSequence> createFileSignature(CsmFile file) {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileContentSignature other = (FileContentSignature) obj;
        if (this.hashCode != other.hashCode) {
            return false;
        }
        if (!this.file.equals(other.file)) {
            return false;
        }
        if (!this.signature.equals(other.signature)) {
            return false;
        }
        return true;
    }

    private static int hash(List<CharSequence> signature) {
        int hash = 7;
        for (CharSequence charSequence : signature) {
            hash = 89 * hash + charSequence.hashCode();
        }
        return hash;
    }
}
