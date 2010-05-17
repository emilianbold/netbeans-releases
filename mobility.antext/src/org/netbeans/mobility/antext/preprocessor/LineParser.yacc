
%{
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

/* Line Parser
 *
 * !!! do not modify LineParser.java or LineParserTokens.java !!! primary source is LineParser.yacc !!!
 *
 * @author Adam Sotona
 * 
 * byaccJ options:
 * -d -Jpackage=org.netbeans.mobility.antext.preprocessor -Jclass=LineParser -Jsemantic=PPToken LineParser.yacc 
*/
    import java.io.*;
    import java.util.*;
%}

%token END_OF_FILE, END_OF_LINE, PREPROCESSOR_COMMENT, OLD_STX_HEADER_START, OLD_STX_HEADER_END
%token OLD_STX_FOOTER_START, OLD_STX_FOOTER_END, COMMAND_IF, COMMAND_IFDEF, COMMAND_IFNDEF  
%token COMMAND_ELIF, COMMAND_ELIFDEF, COMMAND_ELIFNDEF, COMMAND_ELSE, COMMAND_ENDIF, COMMAND_DEBUG       
%token COMMAND_MDEBUG, COMMAND_ENDDEBUG, COMMAND_DEFINE, COMMAND_UNDEFINE, COMMAND_UNKNOWN  
%token COMMAND_CONDITION, ABILITY, STRING, NUMBER, LEFT_BRACKET, RIGHT_BRACKET, OP_NOT_EQUALS   
%token OP_AND, OP_OR, OP_XOR, OP_AT, OP_NOT, OP_EQUALS, OP_LESS, OP_GREATER, OP_LESS_OR_EQUAL        
%token OP_GREATER_OR_EQUAL, COMMA, COLON_DEFINED, DEFINED, ASSIGN, SIMPLE_COMMENT, OTHER_TEXT, UNFINISHED_STRING   

%left OP_OR
%left OP_XOR
%left OP_AND
%right OP_NOT

%start line

%%

line : rest_of_line
        {
            l.setType(PPLine.UNCOMMENTED);
        } 
    | PREPROCESSOR_COMMENT rest_of_line
        {
            l.setType(PPLine.COMMENTED);
        }
    | OLD_STX_HEADER_START old_syntax_expression OLD_STX_HEADER_END rest_of_line 
        {
            l.setType(PPLine.OLDIF);     
            if ($2.hasValue()) l.setValue($2.getValue());
        }
    | OLD_STX_HEADER_START OLD_STX_HEADER_END rest_of_line 
        {
            l.setType(PPLine.OLDIF);
            l.addError("ERR_missing_old_expression", $2); //NOI18N
        }
    | OLD_STX_HEADER_START old_syntax_expression line_terminator 
        {
            l.setType(PPLine.OLDIF);     
            l.addError("ERR_missing_enclosing_statement", $3); //NOI18N
        }
    | OLD_STX_FOOTER_START old_syntax_expression OLD_STX_FOOTER_END rest_of_line 
        {
            l.setType(PPLine.OLDENDIF);  
            if ($2.hasValue()) l.setValue($2.getValue());
        }
    | OLD_STX_FOOTER_START OLD_STX_FOOTER_END rest_of_line                       
        {
            l.setType(PPLine.OLDENDIF);  
            l.addError("ERR_missing_old_expression", $2); //NOI18N
        }
    | OLD_STX_FOOTER_START old_syntax_expression line_terminator                 
        {
            l.setType(PPLine.OLDENDIF);  
            l.addError("ERR_missing_enclosing_statement", $3); //NOI18N
        }
    | COMMAND_IF expression                                                      
        {
            l.setType(PPLine.IF);        
            if($2.hasValue()) l.setValue($2.getValue());
        }
    | COMMAND_CONDITION expression                                                      
        {
            if (l.getLineNumber() == 1) {
                l.setType(PPLine.CONDITION);
                if($2.hasValue()) l.setValue($2.getValue());
            } else {
                l.setType(PPLine.UNKNOWN);
                l.addError("ERR_condition_outside", $1); //NOI18N
            }
        }
    | COMMAND_IFDEF if_ability end_of_command_line                               
        {
            l.setType(PPLine.IFDEF);     
            if($2.hasValue()) l.setValue( $2.getValue());
        }
    | COMMAND_IFNDEF if_ability end_of_command_line                              
        {
            l.setType(PPLine.IFNDEF);    
            if($2.hasValue()) l.setValue(!$2.getValue());
        }
    | COMMAND_ELIF expression                                                    
        {
            l.setType(PPLine.ELIF);      
            if($2.hasValue()) l.setValue( $2.getValue());
        }
    | COMMAND_ELIFDEF if_ability end_of_command_line                             
        {
            l.setType(PPLine.ELIFDEF);   
            if($2.hasValue()) l.setValue( $2.getValue());
        }
    | COMMAND_ELIFNDEF if_ability end_of_command_line                            
        {
            l.setType(PPLine.ELIFNDEF);  
            if($2.hasValue()) l.setValue(!$2.getValue());
        }
    | COMMAND_ELSE end_of_command_line                                           
        {
            l.setType(PPLine.ELSE);
            l.setValue(true);
        }
    | COMMAND_ENDIF end_of_command_line                                          
        {
            l.setType(PPLine.ENDIF);
        }
    | COMMAND_DEBUG end_of_command_line                                          
        {
            l.setType(PPLine.DEBUG);
            l.setValue(compareDebugLevel(null));
        }
    | COMMAND_DEBUG ABILITY end_of_command_line                                  
        {
            l.setType(PPLine.DEBUG);     
            l.setValue(compareDebugLevel($2.getText()));
        }
    | COMMAND_MDEBUG end_of_command_line                                         
        {
            l.setType(PPLine.MDEBUG);    
            l.setValue(compareDebugLevel(null));
        }
    | COMMAND_MDEBUG ABILITY end_of_command_line                                 
        {
            l.setType(PPLine.MDEBUG);    
            l.setValue(compareDebugLevel($2.getText()));
        }
    | COMMAND_ENDDEBUG end_of_command_line                                       
        {
            l.setType(PPLine.ENDDEBUG);
        }
    | COMMAND_DEFINE ABILITY end_of_command_line                                 
        {
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility($2.getText(), null);
        }
    | COMMAND_DEFINE ABILITY STRING end_of_command_line                   
        {
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility($2.getText(), getValueOf($3));
        }
    | COMMAND_DEFINE ABILITY NUMBER end_of_command_line                   
        {
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility($2.getText(), getValueOf($3));
        }
    | COMMAND_DEFINE ABILITY ABILITY end_of_command_line                  
        {
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility($2.getText(), getValueOf($3));
        }
    | COMMAND_DEFINE ABILITY ASSIGN STRING end_of_command_line                   
        {
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility($2.getText(), getValueOf($4));
        }
    | COMMAND_DEFINE ABILITY ASSIGN NUMBER end_of_command_line                   
        {
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility($2.getText(), getValueOf($4));
        }
    | COMMAND_DEFINE ABILITY ASSIGN ABILITY end_of_command_line                  
        {
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility($2.getText(), getValueOf($4));
        }
    | COMMAND_DEFINE error end_of_command_line                                   
        {
            l.setType(PPLine.DEFINE);    
            l.addError("ERR_missing_ability", $2); //NOI18N
        }
    | COMMAND_DEFINE ABILITY ASSIGN error end_of_command_line                    
        {
            l.setType(PPLine.DEFINE);    
            l.addError("ERR_missing_assigment_value", $4); //NOI18N
        }
    | COMMAND_UNDEFINE ABILITY end_of_command_line                               
        {
            l.setType(PPLine.UNDEFINE);  
            if (eval != null) eval.requestUndefineAbility($2.getText());
        }
    | COMMAND_UNDEFINE error end_of_command_line                                 
        {
            l.setType(PPLine.UNDEFINE);  
            l.addError("ERR_missing_ability", $2); //NOI18N
        }
    | COMMAND_UNKNOWN rest_of_line                                               
        {
            l.setType(PPLine.UNKNOWN);   
            l.addWarning("ERR_unknown_directive", $1); //NOI18N
        }
    | error line_terminator                                                      
        {
            l.setType(PPLine.UNKNOWN);   
            l.addError("ERR_unknown_syntax_error", $1); //NOI18N
        }
    ;

/* expressions */

old_syntax_expression : old_syntax_exp 
        {
            $$ = $1;
        }
    | OP_NOT old_syntax_exp            
        {
            $$ = $2; 
            if ($$.hasValue()) $$.setValue(!$$.getValue());
        }
    | error                            
        {
            l.addError("ERR_old_expression_error", $1); //NOI18N
        }
    ;

old_syntax_exp : ABILITY               
        {
            $$ = $1; 
            if (eval != null) $$.setValue(eval.isAbilityDefined($$.getText()));
        }
    | old_syntax_exp COMMA ABILITY     
        {
            $$ = $2; 
            if (eval != null && $1.hasValue()) $$.setValue($1.getValue() || eval.isAbilityDefined($3.getText()));
        }
    ;

expression : exp end_of_command_line   
        {
            $$ = $1;
        }
    | error line_terminator            
        {
            l.addError("ERR_expression_error", $1); //NOI18N
            }
    | line_terminator                  
        {
            l.addError("ERR_missing_expression", $1); //NOI18N
        }
    | SIMPLE_COMMENT rest_of_line      
        {
            l.addError("ERR_missing_expression", $1); //NOI18N
        }
    ;

exp : LEFT_BRACKET exp RIGHT_BRACKET   
        {
            $$ = $2;
        }
    | OP_NOT LEFT_BRACKET exp RIGHT_BRACKET   
        {
            $$ = $3;
            if ($$.hasValue()) $$.setValue(!$$.getValue());
        }
    | exp OP_AND exp                   
        {
            $$ = $2; 
            if ($1.hasValue() && $3.hasValue()) $$.setValue($1.getValue() && $3.getValue());
        }
    | exp OP_OR exp                    
        {
            $$ = $2; 
            if ($1.hasValue() && $3.hasValue()) $$.setValue($1.getValue() || $3.getValue());
        }
    | exp OP_XOR exp                   
        {
            $$ = $2; 
            if ($1.hasValue() && $3.hasValue()) $$.setValue($1.getValue() ^ $3.getValue());
        }
    | element any_comparator element   
        {
            $$ = $2; compare($1, $2, $3);
        }
    | ABILITY                          
        {
            $$ = $1;
            if (eval != null) $$.setValue(eval.isAbilityDefined($$.getText()));
        }
    | OP_NOT ABILITY                       
        {
            $$ = $2;
            if (eval != null) $$.setValue(!eval.isAbilityDefined($$.getText()));
        }
    | ABILITY COLON_DEFINED            
        {
            $$ = $1;
            if (eval != null) $$.setValue(eval.isAbilityDefined($$.getText()));
        }
    | DEFINED LEFT_BRACKET ABILITY RIGHT_BRACKET            
        {
            $$ = $3;
            if (eval != null) $$.setValue(eval.isAbilityDefined($$.getText()));
        }
    | OP_NOT OP_NOT exp
        {
            $$ = $3;
        }
    | OP_NOT LEFT_BRACKET exp error           
        {
            l.addError("ERR_missing_right_bracket", $4); //NOI18N
        }
    | LEFT_BRACKET exp error           
        {
            l.addError("ERR_missing_right_bracket", $3); //NOI18N
        }
    | OP_NOT ABILITY any_comparator error                       
       {
            l.addError("ERR_expression_error", $3); //NOI18N
        }
    | OP_NOT error                       
       {
            l.addError("ERR_expression_error", $2); //NOI18N
        }
    ;
    | DEFINED error                       
       {
            l.addError("ERR_expression_error", $2); //NOI18N
        }
    ;

element : ABILITY                     
        {
            $$ = $1;
        }    
    | STRING                          
        {
            $$ = $1;
        }
    | NUMBER                          
        {
            $$ = $1;
        }
    | UNFINISHED_STRING               
        {
            l.addError("ERR_unfinished_string", $1); //NOI18N
        }
    | error                           
        {
            l.addError("ERR_expression_error", $1); //NOI18N
        }
    ;

any_comparator : OP_EQUALS
    | OP_NOT_EQUALS
    | OP_GREATER 
    | OP_GREATER_OR_EQUAL 
    | OP_LESS 
    | OP_LESS_OR_EQUAL 
    | OP_AT 


/* helpers */

if_ability : ABILITY COLON_DEFINED    
        {
            $$ = $1; 
            if (eval != null) $$.setValue(eval.isAbilityDefined($$.getText()));
        }
    | ABILITY                         
        {
            $$ = $1; 
            if (eval != null) $$.setValue(eval.isAbilityDefined($$.getText()));
        }
    | error                           
        {
            l.addError("ERR_missing_ability", $1); //NOI18N
        }
    ;
    
end_of_command_line : line_terminator
    | SIMPLE_COMMENT rest_of_line
    | error line_terminator           
        {
            l.addError("ERR_invalid_argument", $1); //NOI18N
        }
    ;    
    
rest_of_line : line_terminator
    | OTHER_TEXT line_terminator    

line_terminator : END_OF_LINE
    | END_OF_FILE
    ;

%%
 
public static final String DEBUG_LEVEL = "DebugLevel"; //NOI18N

private PreprocessorScanner scanner;
private boolean endOfLine = false;
private PPLine l;
private CommentingPreProcessor.AbilitiesEvaluator eval;

private int yylex() {
    if (!endOfLine) try {
        int type=scanner.yylex();
        yylval = scanner.getLastToken();
        l.addToken(yylval);
        endOfLine = type == END_OF_LINE || type == END_OF_FILE;
        return type;
    } catch (IOException e) {
        throw new PreprocessorException("IOException during read", e);
    }
    return -1;
}

private void yyerror(String s) {
//    System.out.println("YYError: "+s);
}

public LineParser(Reader in, CommentingPreProcessor.AbilitiesEvaluator eval) {
    this.scanner = new PreprocessorScanner(in);
    this.eval = eval;
}

public boolean hasMoreLines() {
    return scanner.hasMoreTokens();
}

public PPLine nextLine() {
    endOfLine = false;	
    l = new PPLine();
    yyparse();
    return l;
}

private void compare(PPToken a, PPToken op, PPToken b) {
    boolean lexOnly = a.getType() == STRING || b.getType() == STRING;
    boolean numOnly = a.getType() == NUMBER || b.getType() == NUMBER;
    if (lexOnly && numOnly) {
        l.addWarning("WARN_comparing_string_and_number", op); //NOI18N
        //return;
    }
    String aVal = getValueOf(a), bVal = getValueOf(b);
    if (aVal == null || bVal == null) return;
    if (!lexOnly) try {
    	int i = Integer.parseInt(aVal), j = Integer.parseInt(bVal);
    	switch (op.getType()) {
    	    case OP_EQUALS           : op.setValue(i == j); return;
    	    case OP_NOT_EQUALS       : op.setValue(i != j); return;
    	    case OP_GREATER          : op.setValue(i > j); return; 
    	    case OP_GREATER_OR_EQUAL : op.setValue(i >= j); return; 
    	    case OP_LESS             : op.setValue(i < j); return; 
    	    case OP_LESS_OR_EQUAL    : op.setValue(i <= j); return; 
    	    case OP_AT               : op.setValue(i == j); return;
    	    default : return;
    	}	
    } catch (NumberFormatException nfe) {
      if (numOnly) {
          l.addWarning("WARN_comparing_string_ability_and_number", op); //NOI18N
          //return;
      }
    }		
    switch (op.getType()) {
        case OP_EQUALS           : op.setValue(aVal.equals(bVal)); return;
        case OP_NOT_EQUALS       : op.setValue(!aVal.equals(bVal)); return;
        case OP_GREATER          : op.setValue(aVal.compareTo(bVal) > 0); return; 
        case OP_GREATER_OR_EQUAL : op.setValue(aVal.compareTo(bVal) >= 0); return; 
        case OP_LESS             : op.setValue(aVal.compareTo(bVal) < 0); return; 
        case OP_LESS_OR_EQUAL    : op.setValue(aVal.compareTo(bVal) <= 0); return; 
        case OP_AT               : op.setValue(tokenize(bVal).containsAll(tokenize(aVal))); return;
        default : return;
    }	
}

private Set tokenize(String s) {
    StringTokenizer stk = new StringTokenizer(s, " \t\r\n\f,;"); //NOI18N
    HashSet set = new HashSet();
    while (stk.hasMoreTokens()) set.add(stk.nextToken());
//    System.out.println(set);
    return set;
}

private String getValueOf(PPToken a) {
    if (a.getType() == STRING) return unescapeString(a.getText());
    if (a.getType() == NUMBER) return a.getText();
    if (a.getType() == ABILITY) {
    	if (eval == null) return null; 
    	String val = null;
    	if (!eval.isAbilityDefined(a.getText())) {
    	    l.addWarning("WARN_undefined_ability" , a); //NOI18N
    	} else if ((val = eval.getAbilityValue(a.getText())) == null) {
    	    l.addWarning("WARN_undefined_ability_value", a); //NOI18N
    	}
    	return val == null ? "" : val; //NOI18N
    }
    return null;
}

private boolean compareDebugLevel(String inSource) {
    String definedLevel = eval == null ? null : eval.getAbilityValue(DEBUG_LEVEL);
    if (definedLevel == null) definedLevel = "debug"; //NOI18N
    int level = getDebugLevel(definedLevel);
    if (level == 0) return definedLevel.equalsIgnoreCase(inSource);
    return level >= getDebugLevel(inSource);
}

private int getDebugLevel(String level) {
    if (level == null || level.equalsIgnoreCase("debug")) return 5; //NOI18N
    if (level.equalsIgnoreCase("info"))  return 4; //NOI18N
    if (level.equalsIgnoreCase("warn"))  return 3; //NOI18N
    if (level.equalsIgnoreCase("error")) return 2; //NOI18N
    if (level.equalsIgnoreCase("fatal")) return 1; //NOI18N
    return 0;
}

static String unescapeString(String s) {
    if (s == null) return null;
    StringBuffer unib = new StringBuffer(4);
    StringBuffer sb = new StringBuffer();
    boolean slash = false;
    boolean uni = false;
    for (int i = 1; i < s.length() - 1; i++) {
        char ch = s.charAt(i);
        if (uni) {
            unib.append(ch);
            if (unib.length() == 4) {
                try {
                    sb.append((char) Integer.parseInt(unib.toString(), 16));
                } catch (NumberFormatException nfe) {
                    sb.append('\\').append('u').append(unib);
                }
                unib.setLength(0);
                uni = false;
                slash = false;
            }
        } else if (slash) {
            slash = false;
            switch (ch) {
                case '\\': sb.append('\\'); break;
                case '\'': sb.append('\''); break;
                case '\"': sb.append('"'); break;
                case 'r': sb.append('\r'); break;
                case 'f': sb.append('\f'); break;
                case 't': sb.append('\t'); break;
                case 'n': sb.append('\n'); break;
                case 'b': sb.append('\b'); break;
                case 'u': uni = true; break;
                default : sb.append(ch);
            }
        } else if (ch == '\\') {
            slash = true;
        } else sb.append(ch);
    }
    if (uni) sb.append('\\').append('u').append(unib);
    if (slash) sb.append('\\');
    return sb.toString();
}

public static void main(String argv[]) throws Exception {
    if (argv.length == 0) {
        System.out.println("Usage : java PreprocessorParser <inputfile>");
    } else {
        LineParser parser = new LineParser(new FileReader(argv[0]), null);
//        parser.yydebug = true;
        while (parser.hasMoreLines()) {
            System.out.println(parser.nextLine());
        }
    }
}
