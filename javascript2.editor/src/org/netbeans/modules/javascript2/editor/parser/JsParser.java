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

import java.util.Collections;
import java.util.Map;
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

/**
 *
 * @author Petr Pisl
 */
public class JsParser extends Parser {

    private static final Logger LOGGER = Logger.getLogger(JsParser.class.getName());

    private JsParserResult lastResult = null;

    public JsParser() {
        super();
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
        LOGGER.log(Level.FINE, "Parsing took: {0} ms source: {1}", new Object[]{endTime - startTime, snapshot.getSource().getFileObject()}); //NOI18N
    }

    private JsParserResult parseSource(Snapshot snapshot, final Sanitize sanitizing, JsErrorManager errorManager) throws Exception {
        long startTime = System.nanoTime();
        String scriptName;
        if (snapshot.getSource().getFileObject() != null) {
            scriptName = snapshot.getSource().getFileObject().getNameExt();
        } else {
            scriptName = "javascript.js"; // NOI18N
        }

        JsParserResult result = parseContext(new Context(scriptName, snapshot),
                sanitizing, errorManager);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Parsing took: {0} ms; source: {1}",
                    new Object[]{(System.nanoTime() - startTime) / 1000000, scriptName});
        }
        return result;
    }
    
    JsParserResult parseContext(Context context, Sanitize sanitizing, JsErrorManager errorManager) throws Exception {
        boolean sanitized = false;
        if ((sanitizing != Sanitize.NONE) && (sanitizing != Sanitize.NEVER)) {
            boolean ok = sanitizeSource(context, sanitizing, errorManager);

            if (ok) {
                sanitized = true;
                assert context.getSanitizedSource() != null;
            } else {
                // Try next trick
                return parseContext(context, sanitizing.next(), errorManager);
            }
        }
        
        com.oracle.nashorn.ir.FunctionNode node = parseSource(context.getName(),
                context.getSource(), errorManager);
        if (sanitizing != Sanitize.NEVER) {
            if (!sanitized && errorManager.checkCurlyMissing() != null) {
                return parseContext(context, Sanitize.MISSING_CURLY, errorManager);
            } if (node == null) {
                return parseContext(context, sanitizing.next(), errorManager);
            }
        }
        
        // process comment elements
        Map<Integer, ? extends JsComment> comments = Collections.<Integer, JsComment>emptyMap();
        if (context.getSnapshot() != null) {
            try {
                long startTime = System.nanoTime();
                comments = JsDocParser.parse(context.getSnapshot());
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Parsing of comments took: {0} ms source: {1}",
                            new Object[]{(System.nanoTime() - startTime) / 1000000, context.getName()});
                }
            } catch (Exception ex) {
                // if anything wrong happen during parsing comments
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return new JsParserResult(context.getSnapshot(), node, comments);
    }
    
    private com.oracle.nashorn.ir.FunctionNode parseSource(String name, String text, JsErrorManager errorManager) throws Exception {
        com.oracle.nashorn.runtime.Source source = new com.oracle.nashorn.runtime.Source(name, text);
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
        return node;
    }
    
    private boolean sanitizeSource(Context context, Sanitize sanitizing, JsErrorManager errorManager) {
        if (sanitizing == Sanitize.MISSING_CURLY) {
            org.netbeans.modules.csl.api.Error error = errorManager.checkCurlyMissing();
            
            String source = context.getOriginalSource();
            int balance = 0;
            for (int i = 0; i < source.length(); i++) {
                char current = source.charAt(i);
                if (current == '{') {
                    balance++;
                } else if (current == '}') {
                    balance--;
                }
            }
            if (balance != 0) {
                StringBuilder builder = new StringBuilder(source);
                if (balance < 0) {
                    while (balance < 0) {
                        int index = builder.lastIndexOf("}");
                        if (index < 0) {
                            break;
                        }
                        builder.replace(index, index + 1, " ");
                        balance++;
                    }
                } else if (balance > 0 && error.getStartPosition() >= source.length()) {
                    while (balance > 0) {
                        builder.append('}');
                        balance--;
                    }
                }
                context.setSanitizedSource(builder.toString());
                return true;
            }
        }
        return false;
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


    /**
     * Parsing context
     */
    static class Context {

        private final String name;
        
        private final Snapshot snapshot;

        private String source;
        private String sanitizedSource;

        public Context(String name, Snapshot snapshot) {
            this.name = name;
            this.snapshot = snapshot;
        }
        
        public Context(String name, String source) {
            this(name, (Snapshot) null);
            this.source = source;
        }

        public String getName() {
            return name;
        }

        public Snapshot getSnapshot() {
            return snapshot;
        }

        public String getSource() {
            if (sanitizedSource != null) {
                return sanitizedSource;
            }
            return getOriginalSource();
        }

        public String getOriginalSource() {
            if (source == null) {
                source = snapshot.getText().toString();
            }
            return source;
        }
        
        public String getSanitizedSource() {
            return sanitizedSource;
        }

        public void setSanitizedSource(String sanitizedSource) {
            this.sanitizedSource = sanitizedSource;
        }
        
    }

    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER {

            @Override
            public Sanitize next() {
                return NEVER;
            }
        },

        /** Perform no sanitization */
        NONE {

            @Override
            public Sanitize next() {
                return MISSING_CURLY;
            }
        },
        
        /** Attempt to fix missing } */
        MISSING_CURLY {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_CURRENT;
            }
        },
        
        /** Remove current error token */
        SYNTAX_ERROR_CURRENT {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_PREVIOUS;
            }
        },
        
        /** Remove token before error */
        SYNTAX_ERROR_PREVIOUS {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_PREVIOUS_LINE;
            }
        },
        
        /** remove line with error */
        SYNTAX_ERROR_PREVIOUS_LINE {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_BLOCK;
            }
        },
        
        /** try to delete the whole block, where is the error*/
        SYNTAX_ERROR_BLOCK {

            @Override
            public Sanitize next() {
                return EDITED_DOT;
            }
        },
        
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT {

            @Override
            public Sanitize next() {
                return ERROR_DOT;
            }
        },
        
        /** 
         * Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line
         */
        ERROR_DOT {

            @Override
            public Sanitize next() {
                return BLOCK_START;
            }
        },
        /** 
         * Try to remove the initial "if" or "unless" on the block
         * in case it's not terminated
         */
        BLOCK_START {

            @Override
            public Sanitize next() {
                return ERROR_LINE;
            }
        },
        
        /** Try to cut out the error line */
        ERROR_LINE {

            @Override
            public Sanitize next() {
                return EDITED_LINE;
            }
        },
        
        /** Try to cut out the current edited line, if known */
        EDITED_LINE {

            @Override
            public Sanitize next() {
                return NEVER;
            }
        };

        
        public abstract Sanitize next();
    }

}
