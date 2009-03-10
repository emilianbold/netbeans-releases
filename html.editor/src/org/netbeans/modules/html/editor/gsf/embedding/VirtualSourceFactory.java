/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor.gsf.embedding;

import javax.swing.text.Document;
import org.netbeans.modules.css.formatting.api.embedding.VirtualSource;
import org.netbeans.modules.html.editor.HTMLKit;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.css.formatting.api.embedding.VirtualSource.Factory.class)
public class VirtualSourceFactory implements VirtualSource.Factory {

    public VirtualSource createVirtualSource(Document doc, String mimeOfInterest) {
        String mimeType = (String)doc.getProperty("mimeType"); // NOI18N
        if (CssEmbeddingModel.HTML_MIME_TYPE.equals(mimeType) && "text/x-css".equals(mimeOfInterest)) {
            return new CssModelVirtualSource(doc);
        } else if ((CssTemplatedEmbeddingModel.GSP_MIME_TYPE.equals(mimeType) ||
                CssTemplatedEmbeddingModel.JSP_MIME_TYPE.equals(mimeType) ||
                CssTemplatedEmbeddingModel.PHP_MIME_TYPE.equals(mimeType) ||
                CssTemplatedEmbeddingModel.RHTML_MIME_TYPE.equals(mimeType) ||
                CssTemplatedEmbeddingModel.TAG_MIME_TYPE.equals(mimeType))
                && "text/x-css".equals(mimeOfInterest)) {
            return new CssTemplatedModelVirtualSource(doc);
        } else if ((HtmlEmbeddingModel.GSP_TAG_MIME_TYPE.equals(mimeType) ||
                HtmlEmbeddingModel.JSP_MIME_TYPE.equals(mimeType) ||
                HtmlEmbeddingModel.JSP_TAG_MIME_TYPE.equals(mimeType) ||
                HtmlEmbeddingModel.PHP_TAG_MIME_TYPE.equals(mimeType) ||
                HtmlEmbeddingModel.RHTML_MIME_TYPE.equals(mimeType))
                && HTMLKit.HTML_MIME_TYPE.equals(mimeOfInterest)) {
            return new HtmlModelVirtualSource(doc);
        }
        return null;
    }

    private static class CssModelVirtualSource implements VirtualSource {

        private CssModel model;

        public CssModelVirtualSource(Document document) {
            this.model = CssModel.get(document);
        }

        public String getSource(int startOffset, int endOffset) {
            String code = model.getCode();
            int start = model.sourceToGeneratedPos(startOffset);
            int end = model.sourceToGeneratedPos(endOffset);
            if (start == -1 || end == -1) {
                return null;
            }
            code = code.substring(start, end);
            if (code == null || code.trim().length() == 0) {
                return null;
            }
            return code;
        }

        @Override
        public String toString() {
            return "CssModelVirtualSource:["+model.getCode()+"]";
        }
    }

    private static class CssTemplatedModelVirtualSource implements VirtualSource {

        private CssTemplatedModel model;

        public CssTemplatedModelVirtualSource(Document document) {
            this.model = CssTemplatedModel.get(document);
        }

        public String getSource(int startOffset, int endOffset) {
            String code = model.getCode();
            int start = model.sourceToGeneratedPos(startOffset);
            int end = model.sourceToGeneratedPos(endOffset);
            if (start == -1 || end == -1) {
                return null;
            }
            code = code.substring(start, end);
            if (code == null || code.trim().length() == 0) {
                return null;
            }
            return code;
        }

        @Override
        public String toString() {
            return "CssTemplatedModelVirtualSource:["+model.getCode()+"]";
        }
    }

    private static class HtmlModelVirtualSource implements VirtualSource {

        private HtmlModel model;

        public HtmlModelVirtualSource(Document document) {
            this.model = HtmlModel.get(document);
        }

        public String getSource(int startOffset, int endOffset) {
            String code = model.getHtmlCode();
            int start = model.sourceToGeneratedPos(startOffset);
            int end = model.sourceToGeneratedPos(endOffset);
            if (start == -1 || end == -1) {
                return null;
            }
            code = code.substring(start, end);
            if (code == null || code.trim().length() == 0) {
                return null;
            }
            return code;
        }

        @Override
        public String toString() {
            return "HtmlModelVirtualSource:["+model.getHtmlCode()+"]";
        }
    }
}
