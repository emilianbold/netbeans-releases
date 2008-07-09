/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.languages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeListener;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserResult;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.modules.languages.parser.StringInput;
import org.netbeans.modules.languages.parser.SyntaxError;
import org.netbeans.modules.languages.parser.TokenInputUtils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SchedulerEvent;


/**
 *
 * @author hanz
 */
public class ParserImpl extends Parser {

    private ParserResult parserResult;
    
    @Override
    public void parse (Snapshot snapshot, Task task, SchedulerEvent event) {
        try {
            String mimeType = snapshot.getMimeType ();
            if (!LanguagesManager.getDefault ().isSupported (mimeType)) {
                parserResult = null;
                return;
            }
            Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
            org.netbeans.modules.languages.parser.Parser parser = language.getParser ();
            if (parser == null) {
                parserResult = null;
                return;
            }
            TokenInput ti = TokenInputUtils.create (
                language,
                parser, 
                new StringInput (snapshot.getText ())
            );
            List<SyntaxError> syntaxErrors = new ArrayList<SyntaxError> ();
            ASTNode root = language.getAnalyser ().read (
                ti, 
                true, 
                syntaxErrors, 
                new boolean[] {false}
            );
            Feature astProperties = language.getFeatureList ().getFeature ("AST");
            if (astProperties != null && root != null) {
                ASTNode root1 = (ASTNode) astProperties.getValue (
                    "process", 
                    SyntaxContext.create (null, ASTPath.create (root))
                );
                if (root1 != null)
                    root = root1;
            }
            parserResult = ParserResult.create (null, root, syntaxErrors);
        } catch (LanguageDefinitionNotFoundException ex) {
            ex.printStackTrace ();
        } catch (ParseException ex) {
            ex.printStackTrace ();
        }
//        try {
//            String mimeType = (String) document.getProperty ("mimeType");
//            if (!LanguagesManager.getDefault ().isSupported (mimeType)) return null;
//            Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
//            LLSyntaxAnalyser analyser = language.getAnalyser ();                           //long start = System.currentTimeMillis ();
//            TokenInput input = createTokenInput (document);
//            if (cancel [0]) return null;                                                 //S ystem.out.println ("lex " + (System.currentTimeMillis () - start));start = System.currentTimeMillis ();
//            List<SyntaxError> syntaxErrors = new ArrayList<SyntaxError> ();
//            ASTNode ast;
//            try {
//                ast = analyser.read (
//                    input, 
//                    true, 
//                    syntaxErrors,
//                    cancel
//                );                                                                  //S ystem.out.println ("syntax " + (System.currentTimeMillis () - start));
//            } catch (ParseException ex) {
//                // should not be called - read (skipErrors == true)
//                Utils.notify (ex);
//                ast = ASTNode.create (language, "Root", 0);
//ex.printStackTrace();                
//                return null;
//            }
//            if (cancel [0]) return null;                                                 //long start = System.currentTimeMillis ();
//            try {
//                Feature astProperties = language.getFeatureList ().getFeature ("AST");
//                if (astProperties != null) {
//                    ASTNode processedAst = (ASTNode) astProperties.getValue (
//                        "process", 
//                        SyntaxContext.create (document, ASTPath.create (ast))
//                    );
//                    if (processedAst != null) {
//                        ast = processedAst;
//                    }
//                }
//            } catch (Exception ex) {
//                Utils.notify (ex);
//                ast = ASTNode.create (language, "Root", 0);
//            }                                                                       //start = System.currentTimeMillis () - start;if (start > 100)S ystem.out.println ("postprocess " + start);
//            if (ast == null) {
//                Utils.notify (new NullPointerException ());
//                ast = ASTNode.create (language, "Root", 0);
//            }                                                                   //start = System.currentTimeMillis ();
//            return ParserResult.create (document, ast, syntaxErrors);
//        } catch (LanguageDefinitionNotFoundException ex) {
//System.err.println (document.getProperty ("mimeType"));            
//ex.printStackTrace ();
//        }
//        return null;
    }

    @Override
    public Result getResult (Task task, SchedulerEvent event) {
        return parserResult;
    }

    @Override
    public void cancel () {
    }

    @Override
    public void addChangeListener (ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener (ChangeListener changeListener) {
    }
    
//    private boolean[] cancel = new boolean[] {false};
//    
//    private TokenInput createTokenInput (final Document document) {
//        final TokenInput[] ret = new TokenInput[1];
//        document.render(new Runnable() {
//            public void run() {
//                TokenHierarchy tokenHierarchy = TokenHierarchy.get (document);
//                if (tokenHierarchy == null) {
//                    ret[0] = TokenInputUtils.create (Collections.<ASTToken>emptyList());
//                    return;
//                }
//                TokenSequence ts =  tokenHierarchy.tokenSequence();
//                if (ts == null) {
//                    ret [0] = TokenInputUtils.create (Collections.<ASTToken>emptyList());
//                    return;
//                }
//                List<ASTToken> tokens = getTokens(ts);
//                if (cancel[0]) {
//                    // Leave null in ret[0]
//                    return;
//                }
//                ret[0] = TokenInputUtils.create(tokens);
//            }
//        });
//        return ret[0];
//    }
//    
//    private List<ASTToken> getTokens (TokenSequence ts) {
//        if (ts == null) return null;
//        Language language = null;
//        try {
//            language = LanguagesManager.getDefault ().getLanguage (ts.language ().mimeType ());
//        } catch (LanguageDefinitionNotFoundException ex) {
//        }
//        List<ASTToken> tokens = new ArrayList<ASTToken> ();
//        if (!ts.moveNext ()) return tokens;
//        Token t = ts.token ();
//        int type = t.id ().ordinal ();
//        int offset = ts.offset ();
//        String ttype = (String) t.getProperty ("type");
//        List<ASTToken> firstInjection = null;
//        if (ttype == SLexer.INJECTED_CODE) {
//            // first token can be injected 
//            TokenSequence ts2 = ts.embedded ();
//            firstInjection = getTokens (ts2);
//            if (!ts.moveNext ()) {
//                tokens.add (ASTToken.create (
//                    language,
//                    0, 
//                    "", 
//                    offset,
//                    0,
//                    firstInjection
//                ));
//                return tokens;
//            }
//            t = ts.token ();
//            type = t.id ().ordinal ();
//            offset = ts.offset ();
//            ttype = (String) t.getProperty ("type");
//        }
//        for (;;) {
//            if (cancel [0]) return null;
//            if (ttype == null) {
//                List<ASTToken> children = getTokens (ts.embedded ());
//                if (firstInjection != null) {
//                    if (children != null)
//                        children.addAll (firstInjection);
//                    else
//                        children = firstInjection;
//                    firstInjection = null;
//                }
//                tokens.add (ASTToken.create (
//                    language,
//                    type, 
//                    t.text ().toString (), 
//                    offset,
//                    t.length (),
//                    children
//                ));
//                children = null;
//            } else
//            if (ttype == SLexer.CONTINUOUS_TOKEN_START) {
//                StringBuilder sb = new StringBuilder (t.text ());
//                List<ASTToken> children = new ArrayList<ASTToken> ();
//                TokenSequence ts2 = ts.embedded ();
//                while (ts.moveNext ()) {
//                    if (cancel [0]) return null;
//                    t = ts.token ();
//                    ttype = (String) t.getProperty ("type");
//                    if (ttype == null) {
//                        ts.movePrevious ();
//                        break;
//                    }
//                    if (ttype == SLexer.INJECTED_CODE) {
//                        ts2 = ts.embedded ();
//                        if (ts2 != null) {
//                            List<ASTToken> tokens2 = getTokens (ts2);
//                            if (cancel [0]) return null;
//                            children.addAll (tokens2);
//                        }
//                        continue;
//                    }
//                    if (ttype == SLexer.CONTINUOUS_TOKEN_START) {
//                        ts.movePrevious ();
//                        break;
//                    }
//                    if (ttype != SLexer.CONTINUOUS_TOKEN)
//                        throw new IllegalArgumentException ();
//                    if (type != t.id ().ordinal ())
//                        throw new IllegalArgumentException ();
//                    sb.append (t.text ());
//                }
//                int no = ts.offset () + ts.token ().length ();
//                tokens.add (ASTToken.create (
//                    language,
//                    type, 
//                    sb.toString (), 
//                    offset,
//                    no - offset,
//                    children
//                ));
//            } else
//                throw new IllegalArgumentException ();
//            if (!ts.moveNext ()) return tokens;
//            t = ts.token ();
//            type = t.id ().ordinal ();
//            offset = ts.offset ();
//            ttype = (String) t.getProperty ("type");
//        }
//    }
    
    public static class AParserFactory extends ParserFactory {

        @Override
        public Parser createParser (Collection<Snapshot> snapshots) {
            String mimeType = snapshots.iterator ().next ().getMimeType ();
            if (!LanguagesManager.getDefault ().isSupported (mimeType)) return null;
            return new ParserImpl ();
        }
    }
}






