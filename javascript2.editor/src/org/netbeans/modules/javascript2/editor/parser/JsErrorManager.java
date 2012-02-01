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

import com.oracle.nashorn.parser.Token;
import com.oracle.nashorn.runtime.ErrorManager;
import com.oracle.nashorn.runtime.Source;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsErrorManager extends ErrorManager {

    ArrayList<ParserError> parserErrors = null;
    private FileObject fileObject;
    
    public JsErrorManager(FileObject fileObject) {
        this.fileObject = fileObject;
    }
    
    @Override
    public void error(String message, Source source, int line, int column, long token) {
//        super.error(message, source, line, column, token);
        System.out.println("Error1 " + message + "[" + line + "," + column + "]");
        //source.getString(token)
        addParserError(new ParserError(message, line, column, token));
    }

    @Override
    public void error(String message, Source source, long token) {
//        super.error(message, source, token);
        System.out.println("Error2 " + message + "(" + token + ")");
        addParserError(new ParserError(message, token));
    }

    @Override
    public void error(String message) {
//        super.error(message);
        System.out.println("Error3 " + message);
        addParserError(new ParserError(message));
    }

    private void addParserError(ParserError error) {
        if (parserErrors == null) {
            parserErrors = new ArrayList<ParserError>();
        }
        parserErrors.add(error);
    }
    
    @Override
    public void warning(String message, Source source, int line, int column, long token) {
//        super.warning(message, source, line, column, token);
        System.out.println("Warning1 " + message + "[" + line + "," + column + "]");
    }

    @Override
    public void warning(String message, Source source, long token) {
//        super.warning(message, source, token);
        System.out.println("Warning2 " + message + "(" + token + ")");
    }

    @Override
    public void warning(String message) {
//        super.warning(message);
        System.out.println("Warning3 " + message);
    }
    
    public List<Error> getErrors() {
        if (parserErrors == null) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<Error> errors = new ArrayList<Error>(parserErrors.size());
        for (ParserError error : parserErrors) {
            errors.add(convert(error));
        }
        return errors;
    }
    
    private Error convert(ParserError error) {
        String message = error.message;
        int line = -1;
        int offset = -1;
        if (error.line == -1 && error.column == -1) {
            String parts[] = error.message.split(":");
            if (parts.length > 4) {
                message = parts[4];
                int index = message.indexOf('\n');
                if (index > 0) {
                    message = message.substring(0, index);
                }
                    
            }
            if (parts.length > 3) {
                try {
                    offset = Integer.parseInt(parts[3]);
                } catch (NumberFormatException nfe) {
                    // do nothing
                }
            }
        }
        
        return new JsParserError(message, fileObject, offset, offset, Severity.ERROR, null,  true);
    }
    
    private static class ParserError {
        protected String message;
        protected int line;
        protected int column;
        protected long token;

        public ParserError(String message, int line, int column, long token) {
            this.message = message;
            this.line = line;
            this.column = column;
            this.token = token;
        }
        
        public ParserError(String message, long token) {
            this(message, -1, -1, token);
        }
        
        public ParserError(String message) {
            this(message, -1, -1, -1);
        }
    }
}
