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

package org.netbeans.modules.groovy.editor.api.completion.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;

/**
 * Carry completion context around since this logic is split across lots of methods
 * and I don't want to pass dozens of parameters from method to method; just pass
 * a request context with supporting info needed by the various completion helpers
 *
 * @author Martin Janicek
 */
public class CompletionRequest {

    private static final Logger LOG = Logger.getLogger(CompletionRequest.class.getName());

    public int lexOffset;
    public int astOffset;
    public ParserResult info;
    public BaseDocument doc;
    public String prefix = "";
    
    public boolean scriptMode;
    public boolean behindImport;
    public CaretLocation location;
    public CompletionContext ctx;
    public AstPath path;
    public AstPath beforeDotPath;
    public ClassNode declaringClass;
    public DotCompletionContext dotContext;

    
    public CompletionRequest(int lexOffset, int astOffset, ParserResult info, BaseDocument doc, String prefix) {
        this.lexOffset = lexOffset;
        this.astOffset = astOffset;
        this.info = info;
        this.doc = doc;
        this.prefix = prefix;
        this.scriptMode = false;
    }

    /**
     * Try to initiate the rest of CompilationRequest attributes.
     *
     * This method returns false if it find out that there is nothing that could
     * be proposed by code completion (for example when we are above package
     * declaration or inside a comment), true otherwise
     *
     * @return false if nothing could be proposed, true otherwise
     */
    public boolean initContextAttributes() {
        path = RequestHelper.getPathFromRequest(this);
        LOG.log(Level.FINEST, "complete(...), path        : {0}", path);

        location = RequestHelper.getCaretLocationFromRequest(this);
        LOG.log(Level.FINEST, "I am here in sourcecode: {0}", location); // NOI18N

        // if we are above a package statement or inside a comment there's no completion at all.
        if (location == CaretLocation.ABOVE_PACKAGE || location == CaretLocation.INSIDE_COMMENT) {
            return false;
        }

        // now let's figure whether we are in some sort of definition line
        ctx = RequestHelper.getCompletionContext(this);

        // Are we invoked right behind a dot? This is information is used later on in
        // a couple of completions.
        dotContext = RequestHelper.getDotCompletionContext(this);

        if (isBehindDot()) {
            declaringClass = RequestHelper.getBeforeDotDeclaringClass(this);
        }

        // are we're right behind an import statement?
        behindImport = RequestHelper.checkForRequestBehindImportStatement(this);

        return true;
    }

    public boolean isBehindDot() {
        return dotContext != null;
    }
}
