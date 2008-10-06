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

package org.netbeans.modules.db.sql.editor;

import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.modules.editor.NbEditorKit;

/**
 * This class implements the editor kit for text/x-sql files
 * in the editor
 *
 * @author Jesse Beaumont
 * @author Andrei Badea
 */
public class SQLEditorKit extends NbEditorKit {

    public static final String MIME_TYPE = "text/x-sql"; // NOI18N

    /**
     * Creates a new instance of SQLEditorKit
     */
    public SQLEditorKit() {
    }

    /**
     * Create a syntax object suitable for highlighting SQL syntax
     */
    @Override
    public Syntax createSyntax(Document doc) {
        return new SQLSyntax();
    }

    /** Create syntax support */
    @Override
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new SQLSyntaxSupport(doc);
    }

    @Override
    protected Action[] createActions() {
        Action[] superActions = super.createActions();
        Action[] sqlActions = new Action[] {
            new ToggleCommentAction("--") // NOI18N
        };
        return TextAction.augmentList(superActions, sqlActions);
    }

    /**
     * Retrieves the content type for this editor kit
     */
    @Override
    public String getContentType() {
        return MIME_TYPE;
    }
}
