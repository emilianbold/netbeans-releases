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

import com.oracle.nashorn.ir.FunctionNode;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javascript2.editor.jsdoc.JsDocParser;
import org.netbeans.modules.javascript2.editor.model.JsComment;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class JsParser extends Parser {

    private static final Logger LOGGER = Logger.getLogger(JsParser.class.getName());

    private JsParserResult lastResult = null;

    public JsParser() {

    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        long startTime = System.currentTimeMillis();
        try {
            JsErrorManager errorManager = new JsErrorManager(snapshot.getSource().getFileObject());
            lastResult = parseSource(snapshot, Sanitize.NONE, errorManager);
            lastResult.setErrors(errorManager.getErrors());
        } catch (Exception ex) {
            LOGGER.log (Level.INFO, "Exception during parsing: {0}", ex);
            // TODO create empty result
            lastResult = new JsParserResult(snapshot, null, Collections.<Integer, JsComment>emptyMap());
        }
        long endTime = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Parsing took: {0}ms source: {1}", new Object[]{endTime - startTime, snapshot.getSource().getFileObject()}); //NOI18N
    }

    private JsParserResult parseSource(Snapshot snapshot, final Sanitize sanitizing, JsErrorManager errorManager) throws Exception {
        long startTime = System.nanoTime();
        String scriptName;
        if (snapshot.getSource().getFileObject() != null) {
            scriptName = snapshot.getSource().getFileObject().getNameExt();
        } else {
            scriptName = "javascript.js"; // NOI18N
        }

        String text = snapshot.getText().toString();

        com.oracle.nashorn.runtime.Source source = new com.oracle.nashorn.runtime.Source(scriptName, text);
        com.oracle.nashorn.runtime.options.Options options = new com.oracle.nashorn.runtime.options.Options("nashorn");
        options.process(new String[]{
            "--parse-only=true",
            "--empty-statements=true",
            //"--print-parse=true",
            "--debug-lines=false"});

        errorManager.setLimit(0);
        com.oracle.nashorn.runtime.Context contextN = new com.oracle.nashorn.runtime.Context(options, errorManager);
        com.oracle.nashorn.runtime.Context.setContext(contextN);
        com.oracle.nashorn.codegen.Compiler compiler = new com.oracle.nashorn.codegen.Compiler(source, contextN);
        com.oracle.nashorn.parser.Parser parser = new com.oracle.nashorn.parser.Parser(compiler);
        com.oracle.nashorn.ir.FunctionNode node = parser.parse(com.oracle.nashorn.codegen.CompilerConstants.runScriptName);

        // process comment elements
        Map<Integer, ? extends JsComment> comments;
        try {
            long startTimeForDoc = System.currentTimeMillis();
            comments = JsDocParser.parse(snapshot);
            long endTimeForDoc = System.currentTimeMillis();
            LOGGER.log(Level.FINE, "Parsing of comments took: {0}ms source: {1}",
                    new Object[]{endTimeForDoc - startTimeForDoc, scriptName});
        } catch (Exception ex) {
            // if anything wrong happen during parsing comments
            LOGGER.log(Level.WARNING, null, ex);
            comments = Collections.<Integer, JsComment>emptyMap();
        }

        JsParserResult result = new JsParserResult(snapshot, node, comments);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Parsing took: {0} ms; source: {1}",
                    new Object[]{(System.nanoTime() - startTime) / 1000000, scriptName});
        }
        return result;
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return lastResult;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        LOGGER.log(Level.FINE, "Adding changeListener: {0}", changeListener); //NOI18N)
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        LOGGER.log(Level.FINE, "Removing changeListener: {0}", changeListener); //NOI18N)
    }




    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Perform no sanitization */
        NONE,
        /** Remove current error token */
        SYNTAX_ERROR_CURRENT,
        /** Remove token before error */
        SYNTAX_ERROR_PREVIOUS,
        /** remove line with error */
        SYNTAX_ERROR_PREVIOUS_LINE,
        /** try to delete the whole block, where is the error*/
        SYNTAX_ERROR_BLOCK,
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT,
        /** Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line */
        ERROR_DOT,
        /** Try to remove the initial "if" or "unless" on the block
         * in case it's not terminated
         */
        BLOCK_START,
        /** Try to cut out the error line */
        ERROR_LINE,
        /** Try to cut out the current edited line, if known */
        EDITED_LINE,
        /** Attempt to fix missing } */
        MISSING_CURLY,
        /** Try to fix incomplete 'require("' function for FS code complete */
        REQUIRE_FUNCTION_INCOMPLETE,
    }

}
