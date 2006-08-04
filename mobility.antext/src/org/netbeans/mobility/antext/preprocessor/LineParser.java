//### This file created by BYACC 1.8(/Java extension  1.11)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";
package org.netbeans.mobility.antext.preprocessor;


//#line 3 "LineParser.yacc"
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
//#line 42 "LineParser.java"




public class LineParser
             implements LineParserTokens
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 500;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//## **user defined:PPToken
String   yytext;//user variable to return contextual strings
PPToken yyval; //used to return semantic vals from action routines
PPToken yylval;//the 'lval' (result) I got from yylex()
PPToken valstk[] = new PPToken[YYSTACKSIZE];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
final void val_init()
{
  yyval=new PPToken();
  yylval=new PPToken();
  valptr=-1;
}
final void val_push(PPToken val)
{
  try {
    valptr++;
    valstk[valptr]=val;
  }
  catch (ArrayIndexOutOfBoundsException e) {
    int oldsize = valstk.length;
    int newsize = oldsize*2;
    PPToken[] newstack = new PPToken[newsize];
    System.arraycopy(valstk,0,newstack,0,oldsize);
    valstk = newstack;
    valstk[valptr]=val;
  }
}
final PPToken val_pop()
{
  return valstk[valptr--];
}
final void val_drop(int cnt)
{
  valptr -= cnt;
}
final PPToken val_peek(int relative)
{
  return valstk[valptr-relative];
}
//#### end semantic value section ####
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    2,    2,    2,    7,    7,
    4,    4,    4,    4,    8,    8,    8,    8,    8,    8,
    8,    8,    8,    8,    8,    8,    8,    8,    8,    8,
    9,    9,    9,    9,    9,   10,   10,   10,   10,   10,
   10,   10,    5,    5,    5,    6,    6,    6,    1,    1,
    3,    3,
};
final static short yylen[] = {                            2,
    1,    2,    4,    3,    3,    4,    3,    3,    2,    2,
    3,    3,    2,    3,    3,    2,    2,    2,    3,    2,
    3,    2,    3,    4,    4,    4,    5,    5,    5,    3,
    5,    3,    3,    2,    2,    1,    2,    1,    1,    3,
    2,    2,    1,    2,    3,    4,    3,    3,    3,    3,
    1,    2,    2,    4,    3,    4,    3,    4,    2,    2,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    2,    1,    1,    1,    2,    2,    1,    2,
    1,    1,
};
final static short yydefred[] = {                         0,
    0,   82,   81,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    1,   79,   35,    2,   38,    0,   39,
    0,    0,    0,    0,    0,    0,    0,   62,   63,    0,
    0,    0,    0,   64,   43,    9,    0,    0,   75,    0,
    0,    0,   13,    0,    0,    0,    0,   76,   16,   17,
    0,   18,    0,   20,   22,    0,    0,    0,    0,   34,
   10,   80,    4,    0,    0,    5,    0,    7,    0,    8,
   42,   53,   65,    0,   59,    0,    0,    0,   60,    0,
   44,    0,    0,    0,   41,   67,   72,   66,   70,   68,
   71,   69,    0,   73,   11,   12,   14,   15,   78,   77,
   19,   21,   30,    0,    0,    0,    0,   23,   33,   32,
    3,   40,    6,   57,   45,    0,    0,   55,    0,   47,
    0,    0,   61,   50,   26,   24,   25,    0,    0,    0,
    0,   58,   56,   46,   54,   31,   29,   27,   28,
};
final static short yydgoto[] = {                         23,
   24,   32,   58,   46,   51,   59,   33,   47,   48,  103,
};
final static short yysindex[] = {                       -88,
 -190,    0,    0, -242,  -37,  -36, -161, -252, -252, -161,
 -252, -252, -238, -238, -150,  -43, -238, -248, -244, -242,
 -161, -190,    0,    0,    0,    0,    0,    0, -242,    0,
 -272,  -32, -285, -242, -176, -190, -283,    0,    0, -134,
  -35, -250, -242,    0,    0,    0, -213,  -15,    0, -282,
 -238, -238,    0, -238, -238, -190, -242,    0,    0,    0,
 -238,    0, -238,    0,    0, -238, -147, -238, -238,    0,
    0,    0,    0, -285, -242,    0, -253,    0, -242,    0,
    0,    0,    0,  -25,    0,  -15, -134, -134,    0, -243,
    0, -134, -134, -134,    0,    0,    0,    0,    0,    0,
    0,    0, -251,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0, -238, -238, -238, -119,    0,    0,    0,
    0,    0,    0,    0,    0, -214,  -19,    0, -237,    0,
 -182, -234,    0,    0,    0,    0,    0, -238, -238, -238,
 -238,    0,    0,    0,    0,    0,    0,    0,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   34,    0,    0,   -4,  -92,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  -40,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   41,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  -53,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 -142,  -48,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    5,   53,   56,    1,   12,  -14,   44,  -23,  -24,   -3,
};
final static int YYTABLESIZE=304;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         60,
   62,   64,   65,   49,   83,   89,   30,   66,   27,   77,
   53,   68,   82,  104,    2,    3,   84,   56,    2,    3,
   52,   71,   54,   55,   70,  122,   50,  133,   38,   39,
   67,   90,   95,   73,   69,  129,  105,  106,   78,  107,
  108,  142,   56,    2,    3,  145,  111,   91,  112,   44,
   92,  113,  118,  119,  120,   25,   26,   22,   35,   25,
   57,  110,   45,  127,  128,   45,    2,    3,  130,  131,
  132,   92,   93,   94,   74,   25,   45,   72,  134,  121,
    2,    3,  126,  123,   25,   57,   79,   76,    0,   25,
   80,   81,    0,    0,   36,    2,    3,    0,   25,  135,
  136,  137,   92,    0,   94,   56,    2,    3,   56,    2,
    3,  109,   25,   48,   48,   48,    0,   37,   38,   39,
   40,   83,    0,  146,  147,  148,  149,   41,   61,    0,
   25,  114,  115,  116,   25,   42,  138,   43,    0,   44,
   48,    0,    0,   48,   37,   38,   39,   40,   57,    0,
  117,   57,    0,    0,   41,    0,   48,    0,    0,  139,
  140,  141,   42,   51,   51,   51,   44,    1,    2,    3,
    4,    5,    0,    6,    0,    7,    8,    9,   10,   11,
   12,   13,   14,   15,   16,   17,   18,   19,   20,   21,
   51,   61,   51,   51,   51,   61,    0,   61,   61,   61,
   61,   61,   52,   52,   52,    0,   51,   49,   49,   49,
    0,   22,   56,    2,    3,   74,   74,   74,   28,   28,
   85,    0,    0,   29,    2,    3,   34,    0,   75,   52,
  124,   52,   52,   52,   49,   63,  143,   49,   49,    0,
    0,   30,   30,   86,    0,   52,   87,    0,    0,    0,
   49,   31,   31,   88,    0,   57,    0,  125,   74,   92,
   93,   94,    0,  144,    0,   92,   93,   94,   96,    0,
    0,    0,   97,    0,   98,   99,  100,  101,  102,   65,
    0,    0,    0,   65,    0,   65,   65,   65,   65,   65,
   36,   36,    0,    0,   36,    0,   36,   37,   37,    0,
    0,   37,    0,   37,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         14,
   15,   16,   17,  256,  256,  256,  279,  256,    4,  295,
   10,  256,  296,  296,  257,  258,   40,  256,  257,  258,
    9,   21,   11,   12,   20,  279,  279,  279,  280,  281,
  279,  282,   47,   29,  279,  279,   51,   52,   34,   54,
   55,  256,  256,  257,  258,  283,   61,   43,   63,  301,
  285,   66,   67,   68,   69,    0,    1,  300,    6,    4,
  299,   57,    7,   87,   88,   10,  257,  258,   92,   93,
   94,  285,  286,  287,   31,   20,   21,   22,  103,   75,
  257,  258,   86,   79,   29,  299,  263,   32,   -1,   34,
   35,   36,   -1,   -1,  256,  257,  258,   -1,   43,  114,
  115,  116,  285,   -1,  287,  256,  257,  258,  256,  257,
  258,   56,   57,  256,  257,  258,   -1,  279,  280,  281,
  282,  256,   -1,  138,  139,  140,  141,  289,  279,   -1,
   75,  279,  280,  281,   79,  297,  256,  299,   -1,  301,
  283,   -1,   -1,  286,  279,  280,  281,  282,  299,   -1,
  298,  299,   -1,   -1,  289,   -1,  299,   -1,   -1,  279,
  280,  281,  297,  256,  257,  258,  301,  256,  257,  258,
  259,  260,   -1,  262,   -1,  264,  265,  266,  267,  268,
  269,  270,  271,  272,  273,  274,  275,  276,  277,  278,
  283,  284,  285,  286,  287,  288,   -1,  290,  291,  292,
  293,  294,  256,  257,  258,   -1,  299,  256,  257,  258,
   -1,  300,  256,  257,  258,  256,  257,  258,  256,  256,
  256,   -1,   -1,  261,  257,  258,  263,   -1,  261,  283,
  256,  285,  286,  287,  283,  279,  256,  286,  287,   -1,
   -1,  279,  279,  279,   -1,  299,  282,   -1,   -1,   -1,
  299,  289,  289,  289,   -1,  299,   -1,  283,  299,  285,
  286,  287,   -1,  283,   -1,  285,  286,  287,  284,   -1,
   -1,   -1,  288,   -1,  290,  291,  292,  293,  294,  284,
   -1,   -1,   -1,  288,   -1,  290,  291,  292,  293,  294,
  257,  258,   -1,   -1,  261,   -1,  263,  257,  258,   -1,
   -1,  261,   -1,  263,
};
}
final static short YYFINAL=23;
final static short YYMAXTOKEN=301;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"END_OF_FILE","END_OF_LINE","PREPROCESSOR_COMMENT",
"OLD_STX_HEADER_START","OLD_STX_HEADER_END","OLD_STX_FOOTER_START",
"OLD_STX_FOOTER_END","COMMAND_IF","COMMAND_IFDEF","COMMAND_IFNDEF",
"COMMAND_ELIF","COMMAND_ELIFDEF","COMMAND_ELIFNDEF","COMMAND_ELSE",
"COMMAND_ENDIF","COMMAND_DEBUG","COMMAND_MDEBUG","COMMAND_ENDDEBUG",
"COMMAND_DEFINE","COMMAND_UNDEFINE","COMMAND_UNKNOWN","COMMAND_CONDITION",
"ABILITY","STRING","NUMBER","LEFT_BRACKET","RIGHT_BRACKET","OP_NOT_EQUALS",
"OP_AND","OP_OR","OP_XOR","OP_AT","OP_NOT","OP_EQUALS","OP_LESS","OP_GREATER",
"OP_LESS_OR_EQUAL","OP_GREATER_OR_EQUAL","COMMA","COLON_DEFINED","DEFINED",
"ASSIGN","SIMPLE_COMMENT","OTHER_TEXT","UNFINISHED_STRING",
};
final static String yyrule[] = {
"$accept : line",
"line : rest_of_line",
"line : PREPROCESSOR_COMMENT rest_of_line",
"line : OLD_STX_HEADER_START old_syntax_expression OLD_STX_HEADER_END rest_of_line",
"line : OLD_STX_HEADER_START OLD_STX_HEADER_END rest_of_line",
"line : OLD_STX_HEADER_START old_syntax_expression line_terminator",
"line : OLD_STX_FOOTER_START old_syntax_expression OLD_STX_FOOTER_END rest_of_line",
"line : OLD_STX_FOOTER_START OLD_STX_FOOTER_END rest_of_line",
"line : OLD_STX_FOOTER_START old_syntax_expression line_terminator",
"line : COMMAND_IF expression",
"line : COMMAND_CONDITION expression",
"line : COMMAND_IFDEF if_ability end_of_command_line",
"line : COMMAND_IFNDEF if_ability end_of_command_line",
"line : COMMAND_ELIF expression",
"line : COMMAND_ELIFDEF if_ability end_of_command_line",
"line : COMMAND_ELIFNDEF if_ability end_of_command_line",
"line : COMMAND_ELSE end_of_command_line",
"line : COMMAND_ENDIF end_of_command_line",
"line : COMMAND_DEBUG end_of_command_line",
"line : COMMAND_DEBUG ABILITY end_of_command_line",
"line : COMMAND_MDEBUG end_of_command_line",
"line : COMMAND_MDEBUG ABILITY end_of_command_line",
"line : COMMAND_ENDDEBUG end_of_command_line",
"line : COMMAND_DEFINE ABILITY end_of_command_line",
"line : COMMAND_DEFINE ABILITY STRING end_of_command_line",
"line : COMMAND_DEFINE ABILITY NUMBER end_of_command_line",
"line : COMMAND_DEFINE ABILITY ABILITY end_of_command_line",
"line : COMMAND_DEFINE ABILITY ASSIGN STRING end_of_command_line",
"line : COMMAND_DEFINE ABILITY ASSIGN NUMBER end_of_command_line",
"line : COMMAND_DEFINE ABILITY ASSIGN ABILITY end_of_command_line",
"line : COMMAND_DEFINE error end_of_command_line",
"line : COMMAND_DEFINE ABILITY ASSIGN error end_of_command_line",
"line : COMMAND_UNDEFINE ABILITY end_of_command_line",
"line : COMMAND_UNDEFINE error end_of_command_line",
"line : COMMAND_UNKNOWN rest_of_line",
"line : error line_terminator",
"old_syntax_expression : old_syntax_exp",
"old_syntax_expression : OP_NOT old_syntax_exp",
"old_syntax_expression : error",
"old_syntax_exp : ABILITY",
"old_syntax_exp : old_syntax_exp COMMA ABILITY",
"expression : exp end_of_command_line",
"expression : error line_terminator",
"expression : line_terminator",
"expression : SIMPLE_COMMENT rest_of_line",
"exp : LEFT_BRACKET exp RIGHT_BRACKET",
"exp : OP_NOT LEFT_BRACKET exp RIGHT_BRACKET",
"exp : exp OP_AND exp",
"exp : exp OP_OR exp",
"exp : exp OP_XOR exp",
"exp : element any_comparator element",
"exp : ABILITY",
"exp : OP_NOT ABILITY",
"exp : ABILITY COLON_DEFINED",
"exp : DEFINED LEFT_BRACKET ABILITY RIGHT_BRACKET",
"exp : OP_NOT OP_NOT exp",
"exp : OP_NOT LEFT_BRACKET exp error",
"exp : LEFT_BRACKET exp error",
"exp : OP_NOT ABILITY any_comparator error",
"exp : OP_NOT error",
"exp : DEFINED error",
"element : ABILITY",
"element : STRING",
"element : NUMBER",
"element : UNFINISHED_STRING",
"element : error",
"any_comparator : OP_EQUALS",
"any_comparator : OP_NOT_EQUALS",
"any_comparator : OP_GREATER",
"any_comparator : OP_GREATER_OR_EQUAL",
"any_comparator : OP_LESS",
"any_comparator : OP_LESS_OR_EQUAL",
"any_comparator : OP_AT",
"if_ability : ABILITY COLON_DEFINED",
"if_ability : ABILITY",
"if_ability : error",
"end_of_command_line : line_terminator",
"end_of_command_line : SIMPLE_COMMENT rest_of_line",
"end_of_command_line : error line_terminator",
"rest_of_line : line_terminator",
"rest_of_line : OTHER_TEXT line_terminator",
"line_terminator : END_OF_LINE",
"line_terminator : END_OF_FILE",
};

//#line 411 "LineParser.yacc"
 
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
//#line 603 "LineParser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 47 "LineParser.yacc"
{
            l.setType(PPLine.UNCOMMENTED);
        }
break;
case 2:
//#line 51 "LineParser.yacc"
{
            l.setType(PPLine.COMMENTED);
        }
break;
case 3:
//#line 55 "LineParser.yacc"
{
            l.setType(PPLine.OLDIF);     
            if (val_peek(2).hasValue()) l.setValue(val_peek(2).getValue());
        }
break;
case 4:
//#line 60 "LineParser.yacc"
{
            l.setType(PPLine.OLDIF);
            l.addError("ERR_missing_old_expression", val_peek(1)); /*NOI18N*/
        }
break;
case 5:
//#line 65 "LineParser.yacc"
{
            l.setType(PPLine.OLDIF);     
            l.addError("ERR_missing_enclosing_statement", val_peek(0)); /*NOI18N*/
        }
break;
case 6:
//#line 70 "LineParser.yacc"
{
            l.setType(PPLine.OLDENDIF);  
            if (val_peek(2).hasValue()) l.setValue(val_peek(2).getValue());
        }
break;
case 7:
//#line 75 "LineParser.yacc"
{
            l.setType(PPLine.OLDENDIF);  
            l.addError("ERR_missing_old_expression", val_peek(1)); /*NOI18N*/
        }
break;
case 8:
//#line 80 "LineParser.yacc"
{
            l.setType(PPLine.OLDENDIF);  
            l.addError("ERR_missing_enclosing_statement", val_peek(0)); /*NOI18N*/
        }
break;
case 9:
//#line 85 "LineParser.yacc"
{
            l.setType(PPLine.IF);        
            if(val_peek(0).hasValue()) l.setValue(val_peek(0).getValue());
        }
break;
case 10:
//#line 90 "LineParser.yacc"
{
            if (l.getLineNumber() == 1) {
                l.setType(PPLine.CONDITION);
                if(val_peek(0).hasValue()) l.setValue(val_peek(0).getValue());
            } else {
                l.setType(PPLine.UNKNOWN);
                l.addError("ERR_condition_outside", val_peek(1)); /*NOI18N*/
            }
        }
break;
case 11:
//#line 100 "LineParser.yacc"
{
            l.setType(PPLine.IFDEF);     
            if(val_peek(1).hasValue()) l.setValue( val_peek(1).getValue());
        }
break;
case 12:
//#line 105 "LineParser.yacc"
{
            l.setType(PPLine.IFNDEF);    
            if(val_peek(1).hasValue()) l.setValue(!val_peek(1).getValue());
        }
break;
case 13:
//#line 110 "LineParser.yacc"
{
            l.setType(PPLine.ELIF);      
            if(val_peek(0).hasValue()) l.setValue( val_peek(0).getValue());
        }
break;
case 14:
//#line 115 "LineParser.yacc"
{
            l.setType(PPLine.ELIFDEF);   
            if(val_peek(1).hasValue()) l.setValue( val_peek(1).getValue());
        }
break;
case 15:
//#line 120 "LineParser.yacc"
{
            l.setType(PPLine.ELIFNDEF);  
            if(val_peek(1).hasValue()) l.setValue(!val_peek(1).getValue());
        }
break;
case 16:
//#line 125 "LineParser.yacc"
{
            l.setType(PPLine.ELSE);
            l.setValue(true);
        }
break;
case 17:
//#line 130 "LineParser.yacc"
{
            l.setType(PPLine.ENDIF);
        }
break;
case 18:
//#line 134 "LineParser.yacc"
{
            l.setType(PPLine.DEBUG);
            l.setValue(compareDebugLevel(null));
        }
break;
case 19:
//#line 139 "LineParser.yacc"
{
            l.setType(PPLine.DEBUG);     
            l.setValue(compareDebugLevel(val_peek(1).getText()));
        }
break;
case 20:
//#line 144 "LineParser.yacc"
{
            l.setType(PPLine.MDEBUG);    
            l.setValue(compareDebugLevel(null));
        }
break;
case 21:
//#line 149 "LineParser.yacc"
{
            l.setType(PPLine.MDEBUG);    
            l.setValue(compareDebugLevel(val_peek(1).getText()));
        }
break;
case 22:
//#line 154 "LineParser.yacc"
{
            l.setType(PPLine.ENDDEBUG);
        }
break;
case 23:
//#line 158 "LineParser.yacc"
{
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility(val_peek(1).getText(), null);
        }
break;
case 24:
//#line 163 "LineParser.yacc"
{
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility(val_peek(2).getText(), getValueOf(val_peek(1)));
        }
break;
case 25:
//#line 168 "LineParser.yacc"
{
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility(val_peek(2).getText(), getValueOf(val_peek(1)));
        }
break;
case 26:
//#line 173 "LineParser.yacc"
{
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility(val_peek(2).getText(), getValueOf(val_peek(1)));
        }
break;
case 27:
//#line 178 "LineParser.yacc"
{
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility(val_peek(3).getText(), getValueOf(val_peek(1)));
        }
break;
case 28:
//#line 183 "LineParser.yacc"
{
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility(val_peek(3).getText(), getValueOf(val_peek(1)));
        }
break;
case 29:
//#line 188 "LineParser.yacc"
{
            l.setType(PPLine.DEFINE);    
            if (eval != null) eval.requestDefineAbility(val_peek(3).getText(), getValueOf(val_peek(1)));
        }
break;
case 30:
//#line 193 "LineParser.yacc"
{
            l.setType(PPLine.DEFINE);    
            l.addError("ERR_missing_ability", val_peek(1)); /*NOI18N*/
        }
break;
case 31:
//#line 198 "LineParser.yacc"
{
            l.setType(PPLine.DEFINE);    
            l.addError("ERR_missing_assigment_value", val_peek(1)); /*NOI18N*/
        }
break;
case 32:
//#line 203 "LineParser.yacc"
{
            l.setType(PPLine.UNDEFINE);  
            if (eval != null) eval.requestUndefineAbility(val_peek(1).getText());
        }
break;
case 33:
//#line 208 "LineParser.yacc"
{
            l.setType(PPLine.UNDEFINE);  
            l.addError("ERR_missing_ability", val_peek(1)); /*NOI18N*/
        }
break;
case 34:
//#line 213 "LineParser.yacc"
{
            l.setType(PPLine.UNKNOWN);   
            l.addWarning("ERR_unknown_directive", val_peek(1)); /*NOI18N*/
        }
break;
case 35:
//#line 218 "LineParser.yacc"
{
            l.setType(PPLine.UNKNOWN);   
            l.addError("ERR_unknown_syntax_error", val_peek(1)); /*NOI18N*/
        }
break;
case 36:
//#line 227 "LineParser.yacc"
{
            yyval = val_peek(0);
        }
break;
case 37:
//#line 231 "LineParser.yacc"
{
            yyval = val_peek(0); 
            if (yyval.hasValue()) yyval.setValue(!yyval.getValue());
        }
break;
case 38:
//#line 236 "LineParser.yacc"
{
            l.addError("ERR_old_expression_error", val_peek(0)); /*NOI18N*/
        }
break;
case 39:
//#line 242 "LineParser.yacc"
{
            yyval = val_peek(0); 
            if (eval != null) yyval.setValue(eval.isAbilityDefined(yyval.getText()));
        }
break;
case 40:
//#line 247 "LineParser.yacc"
{
            yyval = val_peek(1); 
            if (eval != null && val_peek(2).hasValue()) yyval.setValue(val_peek(2).getValue() || eval.isAbilityDefined(val_peek(0).getText()));
        }
break;
case 41:
//#line 254 "LineParser.yacc"
{
            yyval = val_peek(1);
        }
break;
case 42:
//#line 258 "LineParser.yacc"
{
            l.addError("ERR_expression_error", val_peek(1)); /*NOI18N*/
            }
break;
case 43:
//#line 262 "LineParser.yacc"
{
            l.addError("ERR_missing_expression", val_peek(0)); /*NOI18N*/
        }
break;
case 44:
//#line 266 "LineParser.yacc"
{
            l.addError("ERR_missing_expression", val_peek(1)); /*NOI18N*/
        }
break;
case 45:
//#line 272 "LineParser.yacc"
{
            yyval = val_peek(1);
        }
break;
case 46:
//#line 276 "LineParser.yacc"
{
            yyval = val_peek(1);
            if (yyval.hasValue()) yyval.setValue(!yyval.getValue());
        }
break;
case 47:
//#line 281 "LineParser.yacc"
{
            yyval = val_peek(1); 
            if (val_peek(2).hasValue() && val_peek(0).hasValue()) yyval.setValue(val_peek(2).getValue() && val_peek(0).getValue());
        }
break;
case 48:
//#line 286 "LineParser.yacc"
{
            yyval = val_peek(1); 
            if (val_peek(2).hasValue() && val_peek(0).hasValue()) yyval.setValue(val_peek(2).getValue() || val_peek(0).getValue());
        }
break;
case 49:
//#line 291 "LineParser.yacc"
{
            yyval = val_peek(1); 
            if (val_peek(2).hasValue() && val_peek(0).hasValue()) yyval.setValue(val_peek(2).getValue() ^ val_peek(0).getValue());
        }
break;
case 50:
//#line 296 "LineParser.yacc"
{
            yyval = val_peek(1); compare(val_peek(2), val_peek(1), val_peek(0));
        }
break;
case 51:
//#line 300 "LineParser.yacc"
{
            yyval = val_peek(0);
            if (eval != null) yyval.setValue(eval.isAbilityDefined(yyval.getText()));
        }
break;
case 52:
//#line 305 "LineParser.yacc"
{
            yyval = val_peek(0);
            if (eval != null) yyval.setValue(!eval.isAbilityDefined(yyval.getText()));
        }
break;
case 53:
//#line 310 "LineParser.yacc"
{
            yyval = val_peek(1);
            if (eval != null) yyval.setValue(eval.isAbilityDefined(yyval.getText()));
        }
break;
case 54:
//#line 315 "LineParser.yacc"
{
            yyval = val_peek(1);
            if (eval != null) yyval.setValue(eval.isAbilityDefined(yyval.getText()));
        }
break;
case 55:
//#line 320 "LineParser.yacc"
{
            yyval = val_peek(0);
        }
break;
case 56:
//#line 324 "LineParser.yacc"
{
            l.addError("ERR_missing_right_bracket", val_peek(0)); /*NOI18N*/
        }
break;
case 57:
//#line 328 "LineParser.yacc"
{
            l.addError("ERR_missing_right_bracket", val_peek(0)); /*NOI18N*/
        }
break;
case 58:
//#line 332 "LineParser.yacc"
{
            l.addError("ERR_expression_error", val_peek(1)); /*NOI18N*/
        }
break;
case 59:
//#line 336 "LineParser.yacc"
{
            l.addError("ERR_expression_error", val_peek(0)); /*NOI18N*/
        }
break;
case 60:
//#line 341 "LineParser.yacc"
{
            l.addError("ERR_expression_error", val_peek(0)); /*NOI18N*/
        }
break;
case 61:
//#line 347 "LineParser.yacc"
{
            yyval = val_peek(0);
        }
break;
case 62:
//#line 351 "LineParser.yacc"
{
            yyval = val_peek(0);
        }
break;
case 63:
//#line 355 "LineParser.yacc"
{
            yyval = val_peek(0);
        }
break;
case 64:
//#line 359 "LineParser.yacc"
{
            l.addError("ERR_unfinished_string", val_peek(0)); /*NOI18N*/
        }
break;
case 65:
//#line 363 "LineParser.yacc"
{
            l.addError("ERR_expression_error", val_peek(0)); /*NOI18N*/
        }
break;
case 73:
//#line 380 "LineParser.yacc"
{
            yyval = val_peek(1); 
            if (eval != null) yyval.setValue(eval.isAbilityDefined(yyval.getText()));
        }
break;
case 74:
//#line 385 "LineParser.yacc"
{
            yyval = val_peek(0); 
            if (eval != null) yyval.setValue(eval.isAbilityDefined(yyval.getText()));
        }
break;
case 75:
//#line 390 "LineParser.yacc"
{
            l.addError("ERR_missing_ability", val_peek(0)); /*NOI18N*/
        }
break;
case 78:
//#line 398 "LineParser.yacc"
{
            l.addError("ERR_invalid_argument", val_peek(1)); /*NOI18N*/
        }
break;
//#line 1203 "LineParser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
/**
 * A default run method, used for operating this parser
 * object in the background.  It is intended for extending Thread
 * or implementing Runnable.  Turn off with -Jnorun .
 */
public void run()
{
  yyparse();
}
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public LineParser()
{
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public LineParser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
