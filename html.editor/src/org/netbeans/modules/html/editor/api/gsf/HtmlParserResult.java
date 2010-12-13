/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.api.gsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.HtmlParsingResult;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.editor.ext.html.parser.api.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.api.SyntaxAnalyzerResult;
import org.netbeans.editor.ext.html.parser.spi.ParseResult;
import org.netbeans.html.api.validation.ValidationException;
import org.netbeans.html.api.validation.Validator;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.html.api.validation.ValidationContext;
import org.netbeans.html.api.validation.ValidationResult;
import org.netbeans.html.api.validation.ValidatorService;
import org.netbeans.modules.html.editor.gsf.HtmlParserResultAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * HTML parser result
 *
 * @author mfukala@netbeans.org
 */
public class HtmlParserResult extends ParserResult implements HtmlParsingResult {

    /**
     * Used as a key of a swing document to find a default fallback dtd.
     */
    public static final String FALLBACK_DTD_PROPERTY_NAME = "fallbackDTD";
    private SyntaxAnalyzerResult result;
    private List<Error> errors;
    private boolean isValid = true;

    private HtmlParserResult(SyntaxAnalyzerResult result) {
        super(result.getSource().getSnapshot());
        this.result = result;
    }

    @Override
    public SyntaxAnalyzerResult getSyntaxAnalyzerResult() {
        return result;
    }

    /** The parser result may be invalidated by the parsing infrastructure.
     * In such case the method returns false.
     * @return true for valid result, false otherwise.
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Returns an html version for the specified parser result input.
     * The return value depends on:
     * 1) doctype declaration content
     * 2) if not present, xhtml file extension
     * 3) if not xhtml extension, present of default XHTML namespace declaration
     *
     */
    @Override
    public HtmlVersion getHtmlVersion() {
        return result.getHtmlVersion();
    }

    @Override
    public HtmlVersion getDetectedHtmlVersion() {
        return result.getDetectedHtmlVersion();
    }

    /** @return a root node of the hierarchical parse tree of the document.
     * basically the tree structure is done by postprocessing the flat parse tree
     * you can get by calling elementsList() method.
     * Use the flat parse tree results if you do not need the tree structure since
     * the postprocessing takes some time and is done lazily.
     */
    @Override
    public AstNode root() {
        try {
            return result.parseHtml().root();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public AstNode rootOfUndeclaredTagsParseTree() {
        try {
            return result.parseUndeclaredEmbeddedCode().root();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /** returns a parse tree for non-html content */
    @Override
    public AstNode root(String namespace) {
        try {
            ParseResult pr = result.parseEmbeddedCode(namespace);
            assert pr != null : "Cannot get ParseResult for " + namespace; //NOI18N
            return pr.root();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /** returns a map of all namespaces to astnode roots.*/
    @Override
    public Map<String, AstNode> roots() {
        Map<String, AstNode> roots = new HashMap<String, AstNode>();
        for (String uri : getNamespaces().keySet()) {
            roots.put(uri, root(uri));
        }

        //non xhtml workaround, add the default namespaces if missing
        if (!roots.containsValue(root())) {
            roots.put(null, root());
        }

        return roots;

    }

    /**declared uri to prefix map */
    @Override
    public Map<String, String> getNamespaces() {
        return result.getDeclaredNamespaces();
    }

    /** Returns a leaf most AstNode from the parse tree to which range the given
     * offset belongs.
     *
     * @param offset of the searched node
     */
    public AstNode findLeafTag(int offset, boolean forward, boolean physicalNodesOnly ) {
        //first try to find the leaf in html content
        AstNode mostLeaf = AstNodeUtils.findNode(root(), offset, forward, physicalNodesOnly);
        //now search the non html trees
        for (String uri : getNamespaces().keySet()) {
            AstNode root = root(uri);
            AstNode leaf = AstNodeUtils.findNode(root, offset, forward, physicalNodesOnly);
            if (leaf == null) {
                continue;
            }
            if (mostLeaf == null) {
                mostLeaf = leaf;
            } else {
                //they cannot overlap, just be nested, at least I think
                if (leaf.logicalStartOffset() > mostLeaf.logicalStartOffset()) {
                    mostLeaf = leaf;
                }
            }
        }
        return mostLeaf;
    }

    @Override
    public synchronized List<? extends Error> getDiagnostics() {
        if (errors == null) {
            errors = new ArrayList<Error>();
            errors.addAll(getValidationResults());
        }
        return errors;
    }

    @Override
    protected void invalidate() {
        this.isValid = false;
    }

    private Collection<Error> getValidationResults() {
        FileObject file = getSnapshot().getSource().getFileObject();
        try {
            //use the filtered snapshot or use the namespaces filtering facility in the nu.validator
            Validator validator = ValidatorService.getValidator(getHtmlVersion());
            if(validator == null) {
                return Collections.emptyList();
            }
            ValidationContext context = new ValidationContext(getSnapshot().getText().toString(), getHtmlVersion(), file, result);

            //XXX possibly make it configurable via hints
            context.enableFeature("filter.foreign.namespaces", true); //NOI18N
            
            ValidationResult res = validator.validate(context);

            Collection<Error> errs = new ArrayList<Error>();
            for (ProblemDescription pd : res.getProblems()) {
                DefaultError error = new DefaultError(pd.getKey(),
                        "nu.validator issue", //NOI18N
                        pd.getText(),
                        res.getContext().getFile(),
                        pd.getFrom(),
                        pd.getTo(),
                        false,
                        forProblemType(pd.getType()));

                errs.add(error);
            }
            return errs;

        } catch (ValidationException ex) {
            Logger.getAnonymousLogger().log(Level.INFO, "An error occured during html code validation", ex);

            DefaultError error = new DefaultError("validator.error",
                    "validator.error",
                    "An internal error occured during validating the code: " + ex.getLocalizedMessage(),
                    file, 0,0, true, Severity.ERROR);

            return Collections.<Error>singletonList(error);
        }

    }

    private static Severity forProblemType(int problemtype) {
        switch (problemtype) {
            case ProblemDescription.INFORMATION:
            case ProblemDescription.WARNING:
                return Severity.WARNING;
            case ProblemDescription.ERROR:
            case ProblemDescription.FATAL:
                return Severity.ERROR;
            default:
                throw new IllegalArgumentException("Invalid ProblemDescription type: " + problemtype); //NOI18N
        }

    }

    public static AstNode getBoundAstNode(Error e) {
        if (e instanceof DefaultError) {
            if (e.getParameters() != null && e.getParameters().length > 0 && e.getParameters()[0] instanceof AstNode) {
                return (AstNode) e.getParameters()[0];
            }
        }

        return null;
    }

    static {
        HtmlParserResultAccessor.set(new Accessor());
    }

    private static class Accessor extends HtmlParserResultAccessor {

        @Override
        public HtmlParserResult createInstance(SyntaxAnalyzerResult result) {
            return new HtmlParserResult(result);
        }
    }
}
