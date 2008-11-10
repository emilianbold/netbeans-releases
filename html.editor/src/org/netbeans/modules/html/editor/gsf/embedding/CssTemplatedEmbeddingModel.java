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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor.gsf.embedding;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.EmbeddingModel;
import org.netbeans.modules.gsf.api.TranslatedSource;


/**
 * An implementation of EmbeddingModel providing CSS virtual source for JSP files.
 *
 * @author Marek Fukala
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.gsf.api.EmbeddingModel.class)
public class CssTemplatedEmbeddingModel implements EmbeddingModel {

    static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N
    static final String PHP_MIME_TYPE = "text/x-php5"; // NOI18N
    static final String RHTML_MIME_TYPE = "application/x-httpd-eruby"; // NOI18N
    static final String GSP_MIME_TYPE = "application/x-gsp"; // NOI18N
    
    
    final Set<String> sourceMimeTypes = new HashSet<String>();

    public CssTemplatedEmbeddingModel() {
        sourceMimeTypes.add(JSP_MIME_TYPE);
        sourceMimeTypes.add(TAG_MIME_TYPE);
        sourceMimeTypes.add(PHP_MIME_TYPE);
        sourceMimeTypes.add(RHTML_MIME_TYPE);
        sourceMimeTypes.add(GSP_MIME_TYPE);
        
    }
    
    public String getTargetMimeType() {
        return "text/x-css";
    }

    public Set<String> getSourceMimeTypes() {
        return sourceMimeTypes;
    }

    public Collection<? extends TranslatedSource> translate(Document doc) {
        // This will cache
        CssTemplatedModel model = CssTemplatedModel.get(doc);
        return Collections.singletonList(new CssTemplatedTranslatedSource(this, model));
    }

    @Override
    public String toString() {
        return "CssJspEmbeddingModel(target=" + getTargetMimeType() + ",sources=" + getSourceMimeTypes() + ")";
    }
    
    
}
