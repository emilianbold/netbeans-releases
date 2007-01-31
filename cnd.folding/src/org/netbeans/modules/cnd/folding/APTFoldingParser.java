/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.folding;

import java.io.*;
import java.util.*;
import antlr.*;
import antlr.debug.misc.*;
import antlr.TokenStreamException;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTBuilder;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;


/**
 * Code Folding parser based on matching balanced { and }
 *@author Vladimir Voskresensky
 */
/*package*/ class APTFoldingParser extends antlr.LLkParserNoEx       implements APTTokenTypes {
    
    private static final int CLASS_FOLD = CppFoldRecord.CLASS_FOLD;
    private static final int NAMESPACE_FOLD = CppFoldRecord.NAMESPACE_FOLD;
    private static final int FUNCTION_FOLD = CppFoldRecord.FUNCTION_FOLD;
    
    private static final int FIRST_TOKEN = APTTokenTypes.FLOATONE;
    private static final int LAST_TOKEN = APTTokenTypes.Identifier;
    
    //private int curCurlyLevel = 0;
    
    private List parserFolders = new ArrayList();
        
    private APTFoldingWalker walker = null;
    
    public APTFoldingParser(TokenStream lexer, APTFoldingWalker walker) {
        super(lexer,2);
        this.walker = walker;
        tokenNames = _tokenNames;
    }
    
    private void createFolder(int folderKind, APTToken begin, APTToken end) {
        // remove one symbol because we want to leave closing curly
        if (APTFoldingUtils.isStandalone()) {
            parserFolders.add(new CppFoldRecord(folderKind, begin.getLine(), begin.getColumn(), end.getEndLine(), end.getEndColumn()));
        } else {
            parserFolders.add(new CppFoldRecord(folderKind, begin.getOffset(), end.getEndOffset()));            
        }
    }
    
    protected List getFolders() {
        List walkerFolds = walker.getFolders();
        List out = new ArrayList(walkerFolds.size() + parserFolders.size());
        out.addAll(walkerFolds);
        out.addAll(parserFolders);
        return out;
    }
    
    public static void main(String[] args) {
        try {
            String fileName = "/export/home/jec/projects/antlr/test/freeway/maniac.cc";  // NOI18N
            TokenStream lexer = APTTokenStreamBuilder.buildTokenStream(fileName, new BufferedInputStream(new FileInputStream(fileName), APTTraceFlags.BUF_SIZE));
            APTFoldingParser parser = getParser(fileName, lexer);
            parser.translation_unit();
            
            CommonAST t = (CommonAST)parser.getAST();
            ASTFrame frame = new ASTFrame("AST JTree Example", t);  // NOI18N
            frame.setVisible(true);

        } catch(Exception e) {
            System.err.println("exception: "+e);  // NOI18N
            e.printStackTrace();
        }
    }
    
    private static APTFoldingParser getParser(String name, TokenStream lexer) {
        APTFile apt = APTBuilder.buildAPT(name, lexer);
        APTFoldingWalker walker = new APTFoldingWalker(apt);
        // TODO: may be use simplified filter for everything?
        String filterName = APTLanguageSupport.GNU_CPP;
        APTLanguageFilter filter = APTLanguageSupport.getInstance().getFilter(filterName);
        TokenStream ts = walker.getFilteredTokenStream(filter);
        APTFoldingParser parser = new APTFoldingParser(ts, walker);
        return parser;
    }
    
    public static List parse(String name, Reader source) {
        List folds = new ArrayList();
        try {
            TokenStream lexer = APTTokenStreamBuilder.buildTokenStream(name, source);
            APTFoldingParser parser = getParser(name, lexer);
            parser.translation_unit();

            folds.addAll(parser.getFolders());
 
        } catch(Exception e) {
            if (reportErrors) {
                System.err.println("exception: "+e); // NOI18N
                e.printStackTrace();
            }
        }
        return folds;
        
    }
    private final static boolean reportErrors = Boolean.getBoolean("folding.parser.report.errors"); // NOI18N
    public void reportError(RecognitionException e) {
        if (reportErrors) {
            super.reportError(e);
        }
    }
    
    public void reportError(String s) {
        if (reportErrors) {
            super.reportError(s);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // help methods
    
    protected final void balanceParens() throws TokenStreamException {
        assert (LA(0) == LPAREN);
        balanceBracket(LPAREN, RPAREN);
        assert (matchError || LA(1) == RPAREN);
    }
    
    protected final void balanceCurlies() throws TokenStreamException {
        assert (LA(0) == LCURLY);
        balanceBracket(LCURLY, RCURLY);
        assert (matchError || LA(1) == RCURLY);
    }
    
    protected final void balanceTemplateParams() throws TokenStreamException {
        assert (LA(0) == LESSTHAN);
        balanceBracket(LESSTHAN, GREATERTHAN);
        assert (matchError || LA(1) == GREATERTHAN);
    }
    
    private void balanceBracket(int startType, int endType) throws TokenStreamException {
        int level = 0;
        int LA1 = LA(1);
        for (; LA1 != EOF; LA1 = LA(1)) {
            if (LA1 == endType) {
                if (level <= 0) {
                    break;
                } else {
                    level--;
                }
            } else if (LA1 == startType) {
                level++;
            } else {
                // eat element
            }
            consume();
        }
        if (level != 0 || LA1 == EOF) {
            matchError = true;
            matchException=new RecognitionException("unbalanced bracket " + getBracketName(startType)); // NOI18N
        }
    }
    
    private String getBracketName(int kind) {
        String out = ""; // NOI18N
        switch (kind) {
            case LPAREN:
                out = "LPAREN"; // NOI18N
                break;
            case RPAREN:
                out = "RPAREN"; // NOI18N
                break;
            case LCURLY:
                out = "LCURLY"; // NOI18N
                break;
            case RCURLY:
                out = "RCURLY"; // NOI18N
                break;
            case LESSTHAN:
                out = "LESSTHAN"; // NOI18N
                break;
            case GREATERTHAN:
                out = "GREATERTHAN"; // NOI18N
                break;
        }
        return out;
    }
    
    protected final void createCurlyFolder(int folderKind) throws TokenStreamException {
        do {
            APTToken begin = (APTToken) LT(1);
            match(LCURLY);
            if (matchError) {
                break;
            }
            balanceCurlies();
            if (matchError) {
                break;
            }
            APTToken end = (APTToken) LT(1);
            match(RCURLY);
            if (matchError) {
                break;
            }
            createFolder(folderKind, begin, end);
        } while (false);
        if (matchError) {
            reportError(matchException);
//            recover(matchException,_tokenSet_1);
            resetMatchError();
        }
    }
    
    // state machine
    
    public final void translation_unit() throws TokenStreamException {
        // translation_unit:    external_declarations EOF;
        do {
            int LA1 = LA(1);
            if (LA1 >= FIRST_TOKEN && LA1 <= LAST_TOKEN) {
                external_declarations();
                if (matchError) {
                    break;
                }
            }
            match(Token.EOF_TYPE);
            if (matchError) {
                break ;
            }
        } while (false);
        if (matchError) {
            reportError(matchException);
//            recover(matchException,_tokenSet_0);
            resetMatchError();
        }
    }
    
    protected final void external_declarations() throws TokenStreamException {
        // external_declarations : (external_declaration)*
        // external_declaration
        //    :
        //                //linkage specification
        //                (LITERAL_extern StringLiteral)=> linkage_specification
        //        |
        //                declaration
        //    ;
        main_loop: 
            while (true) {
            // Local LA Cache for 2 element(s):
            int LA1 = LA(1);
            int LA2 = LA(2);
            switch (LA1) {
                case RCURLY:
                    break main_loop;
                case EOF:
                    break main_loop;                
                case LITERAL_extern:
                    if (LA2 == STRING_LITERAL) {                          
                        linkage_specification();
                        if (matchError) {
                            break main_loop;
                        } 
                        break; // break LA1 switch                        
                    }
                    // nobreak
                default:
                    if ((LA1 >= FIRST_TOKEN && LA1 <= LAST_TOKEN)) {
                        declaration();
                        if (matchError) {
                            break main_loop;
                        }
                    } else {
                        matchError=true;
                        matchException=new NoViableAltException(LT(1), getFilename());
                        break main_loop;                        
                    }
            }
        } // End of loop
        if (matchError) {
            reportError(matchException);
 //           recover(matchException,_tokenSet_1);
            resetMatchError();
        }
    }
    
    protected final void linkage_specification() throws TokenStreamException {
        //linkage_specification
        //	:	LITERAL_extern StringLiteral
        //		(
        //                    bb:LCURLY
        //                        (options {greedy=false;}:external_declaration)*
        //                    be:RCURLY
        //                |
        //                    external_declaration
        //		)
        //	;
        main_loop:
            do {
                match(LITERAL_extern);
                if (matchError) {
                    break main_loop;
                }
                match(STRING_LITERAL);
                if (matchError) {
                    break main_loop;
                }
                // Local LA Cache for 2 element(s):
                int LA1 = LA(1);
                int LA2 = LA(2);
                
                if ((LA1==LCURLY) && ((LA2 >= FIRST_TOKEN && LA2 <= LAST_TOKEN))) {
                    APTToken begin = (APTToken) LT(1);
                    // already checked LCURLY, just skip
                    consume();
//                    int endCurlyLevel = this.curCurlyLevel;
//                    this.curCurlyLevel++;
                    ext_decl_loop:
                        do {
                            // nongreedy exit test
//                            if ((LA(1)==RCURLY) && (curlyLevel == this.curCurlyLevel)) {
                            if (LA(1)==RCURLY) {
//                                if (endCurlyLevel != this.curCurlyLevel - 1) {
//                                    matchError = true;
//                                    matchException=new RecognitionException("unbalanced LCURLY " + begin); // NOI18N
//                                }
                                break ext_decl_loop;
                            }
                            if (LA(1) >= FIRST_TOKEN && LA(2) <= LAST_TOKEN) {
                                external_declarations();
                                if (matchError) {
                                    break main_loop;
                                }
                            } else {
                                break ext_decl_loop;
                            }
                        } while (true);
                        APTToken end = (APTToken) LT(1);
                        match(RCURLY);
//                        this.curCurlyLevel--;
                        if (matchError) {
                            break main_loop;
//                        } else if (curCurlyLevel < 0) {
//                            curCurlyLevel = 0;
//                            matchError = true;
//                            matchException=new RecognitionException("unbalanced RCURLY " + end);   // NOI18N 
//                            break main_loop;
                        }
                        // create folder
                        createFolder(CLASS_FOLD, begin, end);
                } else if (LA1 >= FIRST_TOKEN && LA1 <= LAST_TOKEN) {
                    external_declarations();
                } else if (LA1 == EOF) {
                    break main_loop;
                } else {
                    matchError=true;
                    matchException=new NoViableAltException(LT(1), getFilename());
                    break main_loop;
                }
            } while (false);
            if (matchError) {
                reportError(matchException);
//                recover(matchException,_tokenSet_1);
                resetMatchError();
            }
    }
    
    private void eat2Token(int type, boolean checkLeftCurly, boolean checkRightCurly) throws TokenStreamException {
        do {
            // Local LA Cache for 2 element(s):
            int LA1 = LA(1);            
            // nongreedy exit test
            if (LA1==type) {
                break;
            }
            // error handling test
            if (checkLeftCurly && (LA1==LCURLY)) {
                break;
            }
            if (checkRightCurly && (LA1 == RCURLY)) {
                break;
            }
            if (LA1 >= FIRST_TOKEN && LA1 <= LAST_TOKEN) {
                matchNot(EOF);
                if (matchError) {
                    break;
                };
            } else {
                break;
            }
        } while (true);        
    }    
    
    private void eatDeclPrefix() throws TokenStreamException {
        loop: do {
            // Local LA Cache for 2 element(s):
            switch (LA(1)) {
                case LITERAL_extern:
                case LITERAL_typedef:
                    consume();
                    // eat
                    break;
                case LITERAL_template:
                    consume();
                    if (LA(1) == LESSTHAN) {
                        consume();
                        balanceTemplateParams();
                        if (matchError) {
                            break loop;
                        };
                        match(GREATERTHAN);
                        if (matchError) {
                            break loop;
                        };
                    }
                    break;
                default:
                    break loop;
            }            
        } while (true);         
    }
    
    protected final void declaration() throws TokenStreamException {
        
        
        main_loop:  do {
            // eat typedef, template, extern template
            eatDeclPrefix();
            if (matchError) {
                break main_loop;
            };                
            boolean ns = false;
            // Local LA Cache for 2 element(s):
            int LA1 = LA(1);
            switch (LA1) {
                case RCURLY: // RCURLY is necessary above
                    break;
                case LITERAL_enum: // handle enum
                {
                    // enum is LITERAL_enum (ID)? { elems } IDs ;
                    consume();
                    if ( LA(1) == ID ) {
                        // already checked token ref, just skip
                        consume();
                    }
                    createCurlyFolder(CLASS_FOLD);
                    if (matchError) {
                        break main_loop;
                    }
                    eat2Token(SEMICOLON, true, true);
                    // allow errors
                    if (LA(1) == SEMICOLON) {
                        consume();
                    }
                    if (matchError) {
                        break main_loop;
                    }
                    break;
                }
                case LITERAL_namespace: // handle namespace the same way as classes
                    ns = true;
                case LITERAL_class:
                case LITERAL_union:
                case LITERAL_struct: // handle class, union, struct
                {
                    consume();
                    eat2Token(SEMICOLON, true, false);
                    if (matchError) {
                        break main_loop;
                    }   
                    if (LA(1) == LCURLY) {
                        // TODO: for now we just use one level of folding
                        declarationsFold(ns ? NAMESPACE_FOLD : CLASS_FOLD);
                        if (matchError) {
                            break main_loop;
                        }
                    }
                    if (!ns) {
                        eat2Token(SEMICOLON, true, true);
                    }
                    // allow errors
                    if (LA(1) == SEMICOLON) {
                        consume();
                    }
                    if (matchError) {
                        break main_loop;
                    }
                    break;                        
                }
                default:
                    // handle functions and other elements
                {
                    eat2Token(SEMICOLON, true, true);
                    if (matchError) {
                        break main_loop;
                    }   
                    switch (LA(1)) {
                        case LCURLY:
                        {
                            createCurlyFolder(FUNCTION_FOLD);
                            if (matchError) {
                                break main_loop;
                            }
                            break;
                        }
                        case RCURLY:
                            // RCURLY is expected somewhere outside
                            break main_loop;
                    }
                    // allow errors
                    if (LA(1) == SEMICOLON) {
                        consume();
                    }     
                    break;
                }
            }
        } while (false);
        if (matchError) {
            reportError(matchException);
//            recover(matchException,_tokenSet_1);
            resetMatchError();
        }
    }
    
    protected final void declarationsFold(int folderKind) throws TokenStreamException {
       
        main_loop:  while (true) {
            APTToken begin = (APTToken) LT(1);
            match(LCURLY);
            if (matchError) {
                break main_loop;
            };
            
            // declarations loop
            do {
                // nongreedy exit test
                if (LA(1)==RCURLY) {
                    break ;
                }

                if ((LA(1) >= FIRST_TOKEN && LA(1) <= LAST_TOKEN)) {
                    declaration();
                    if (matchError) {
                        break main_loop;
                    };
                } else {
                    break;
                }     
            } while (true);
            
            APTToken end = (APTToken) LT(1);
            match(RCURLY);
            if (matchError) {
                break main_loop;
            };
            
            createFolder(folderKind, begin, end);
            
            break;
        } // End of loop main_loop
        if (matchError) {
            reportError(matchException);
 //           recover(matchException,_tokenSet_1);
            resetMatchError();
        }        
    }
    
//    protected final void namespaceFold() throws TokenStreamException {
//        
//        Token  bb = null;
//        Token  be = null;
//        
//        loop8:  while (true) {
//            bb = LT(1);
//            match(LCURLY);
//            if (matchError) {break loop8;};
//            {
//                _loop42:
//                    do {
//                        // nongreedy exit test
//                        if ((LA(1)==RCURLY) && (_tokenSet_1.member(LA(2)))) break _loop42;
//                        // Local LA Cache for 2 element(s):
//                        int LA1_31 = LA(1);
//                        int LA2_31 = LA(2);
//                        
//                        if (((LA1_31 >= FIRST_TOKEN && LA1_31 <= LAST_TOKEN)) && (_tokenSet_1.member(LA2_31))) {
//                            declaration();
//                            if (matchError) {break loop8;};
//                        } else {
//                            break _loop42;
//                        }
//                        
//                    } while (true);
//            }
//            be = LT(1);
//            match(RCURLY);
//            if (matchError) {break loop8;};
//            
//            parserFolders.add(new int[]{CLASS_FOLD,
//            bb.getLine(), bb.getColumn(),
//            be.getLine(), be.getColumn()});
//            
//            break;} // End of loop loop8
//        if (matchError) {
//            reportError(matchException);
//            recover(matchException,_tokenSet_1);
//            resetMatchError();
//        }
//    }

    public static final String[] _tokenNames = initTokenNames();
    
    private static String[] initTokenNames() {
        String[] names = new String[LAST_TOKEN + 1];
        // fill array by reflection
        // used only for trace
        Field[] fields = APTTokenTypes.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            int flags = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
            Field field = fields[i];
            if ((field.getModifiers() & flags) == flags &&
                    int.class.isAssignableFrom(field.getType())) {
                try {
                    int value = field.getInt(null);
                    String name = field.getName();
                    names[value]=name;
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return names;
    }    
}
