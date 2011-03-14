/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el.hints;

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstMethodSuffix;
import com.sun.el.parser.AstPropertySuffix;
import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.el.ELException;
import javax.lang.model.element.Element;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELParserResult;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Checks for unknown EL identifiers, properties and methods.
 *
 * @author Erno Mononen
 */
public final class Identifiers extends ELRule {

    private WebModule webModule;
    private FileObject context;

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(ELHintsProvider.Kind.DEFAULT);
    }

    @Override
    public boolean appliesTo(RuleContext ruleContext) {
        this.context = ruleContext.parserResult.getSnapshot().getSource().getFileObject();
        if (context == null) {
            return false;
        }
        return (webModule = WebModule.getWebModule(context)) != null;
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    protected void run(RuleContext ruleContext, final List<Hint> result) {
        final ELParserResult elResult = (ELParserResult)ruleContext.parserResult;
        final ELTypeUtilities typeUtilities = ELTypeUtilities.create(this.context);
        for (final ELElement each : elResult.getElements()) {
            if (!each.isValid()) {
                // broken AST, skip
                continue;
            }

            each.getNode().accept(new NodeVisitor() {

                private boolean finished;

                @Override
                public void visit(final Node node) throws ELException {
                    if (finished) {
                        return;
                    }
                    if (node instanceof AstIdentifier) {
                        if (typeUtilities.resolveElement(each, node) == null) {
                            // currently we can't reliably resolve all identifiers, so 
                            // if we couldn't resolve the base identifier skip checking properties / methods
                            finished = true;
                        }
                    }
                    if (node instanceof AstPropertySuffix || node instanceof AstMethodSuffix) {
                        Element resolvedElement = typeUtilities.resolveElement(each, node);
                        if (resolvedElement == null) {
                            Hint hint = new Hint(Identifiers.this,
                                    getMsg(node),
                                    elResult.getFileObject(),
                                    each.getOriginalOffset(node),
                                    Collections.<HintFix>emptyList(), 200);
                            result.add(hint);
                            finished = true; // warn only about the first unknown property

                        }
                    }
                }
            });
        }
    }

    private static String getMsg(Node node) {
        if (node instanceof AstIdentifier) {
            return NbBundle.getMessage(Identifiers.class, "Identifiers_Unknown_Identifier", node.getImage());
        }
        if (node instanceof AstPropertySuffix) {
            return NbBundle.getMessage(Identifiers.class, "Identifiers_Unknown_Property", node.getImage());
        }
        if (node instanceof AstMethodSuffix) {
            return NbBundle.getMessage(Identifiers.class, "Identifiers_Unknown_Method", node.getImage());
        }
        assert false;
        return null;
    }
}
