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
package org.netbeans.modules.gsf.api;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.openide.filesystems.FileObject;


/**
 * Assorted information about the Source.
 *
 * @todo Pass around a context object here that is managed by the client.
 *  This would let all the multiple clients of a particular compilation result
 *  share some work, such as computing the position stack for a caret offset,
 *  and so on. (Each client checks if it's initialized, and if not, perform
 *  work and store it in the context.)
 *
 * @author Petr Hrebejk, Tomas Zezula, Tor Norbye
 */
public abstract class CompilationInfo {
    private FileObject fo;
    private Document doc;

    public CompilationInfo(@NonNull final FileObject fo) throws IOException {
        this.fo = fo;
    }

    /** 
     * Get all embedded results for a given mime type for this compilation info
     */
    @NonNull
    public abstract Collection<? extends ParserResult> getEmbeddedResults(@NonNull String mimeType);
    
    /**
     * Get the embedded result of the given mime type that applies to the given offset
     */
    @CheckForNull
    public abstract ParserResult getEmbeddedResult(@NonNull String mimeType, int offset);

    /**
     * Returns the content of the file.
     *
     * @return String the java source
     */
    @NonNull
    public abstract String getText();
    
    /**
     * Returns the index associated with this file
     */
    @CheckForNull
    public abstract Index getIndex(@NonNull String mimeType);

    @NonNull
    public FileObject getFileObject() {
        return fo;
    }

    @CheckForNull
    public Document getDocument() {
        if (doc == null) {
            if (this.fo == null) {
                return null;
            }

            doc = DataLoadersBridge.getDefault().getDocument(fo);
        }
        
        return doc;
    }

    @NonNull
    public abstract List<Error> getErrors();
}
