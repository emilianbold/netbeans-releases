/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import jdk.nashorn.internal.ir.FunctionNode;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.model.ModelFactory;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Pisl
 */
public class JsParserResult extends ParserResult {

    private static final Logger LOGGER = Logger.getLogger(JsParserResult.class.getName());

    private final FunctionNode root;
    private final boolean embedded;
    private List<? extends Error> errors;
    private Model model;
    private JsDocumentationHolder docHolder;

    public JsParserResult(@NonNull Snapshot snapshot, @NullAllowed FunctionNode root) {
        super(snapshot);
        this.root = root;
        this.errors = Collections.<Error>emptyList();
        this.model = null;
        this.docHolder = null;

        this.embedded = isEmbedded(snapshot);
    }

    public static boolean isEmbedded(@NonNull Snapshot snapshot) {
        return !JsTokenId.JAVASCRIPT_MIME_TYPE.equals(snapshot.getMimePath().getPath())
                && !JsTokenId.JSON_MIME_TYPE.equals(snapshot.getMimePath().getPath());
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    @Override
    protected void invalidate() {

    }

    public FunctionNode getRoot() {
        return root;
    }

    public void setErrors(List<? extends Error> errors) {
        this.errors = errors;
    }

    public Model getModel() {
        synchronized (this) {
            if (model == null) {
                model = ModelFactory.getModel(this);

                if (LOGGER.isLoggable(Level.FINEST)) {
                    model.writeModel(new Model.Printer() {
                        @Override
                        public void println(String str) {
                            LOGGER.log(Level.FINEST, str);
                        }
                    });
                }
            }
            return model;
        }
    }

    public JsDocumentationHolder getDocumentationHolder() {
        synchronized (this) {
            if (docHolder == null) {
                docHolder = JsDocumentationSupport.getDocumentationHolder(this);
            }
            return docHolder;
        }
    }

    public boolean isEmbedded() {
        return embedded;
    }

}
