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

package org.netbeans.mobility.antext.preprocessor;
public interface LineParserTokens {
public final static short END_OF_FILE=257;
public final static short END_OF_LINE=258;
public final static short PREPROCESSOR_COMMENT=259;
public final static short OLD_STX_HEADER_START=260;
public final static short OLD_STX_HEADER_END=261;
public final static short OLD_STX_FOOTER_START=262;
public final static short OLD_STX_FOOTER_END=263;
public final static short COMMAND_IF=264;
public final static short COMMAND_IFDEF=265;
public final static short COMMAND_IFNDEF=266;
public final static short COMMAND_ELIF=267;
public final static short COMMAND_ELIFDEF=268;
public final static short COMMAND_ELIFNDEF=269;
public final static short COMMAND_ELSE=270;
public final static short COMMAND_ENDIF=271;
public final static short COMMAND_DEBUG=272;
public final static short COMMAND_MDEBUG=273;
public final static short COMMAND_ENDDEBUG=274;
public final static short COMMAND_DEFINE=275;
public final static short COMMAND_UNDEFINE=276;
public final static short COMMAND_UNKNOWN=277;
public final static short COMMAND_CONDITION=278;
public final static short ABILITY=279;
public final static short STRING=280;
public final static short NUMBER=281;
public final static short LEFT_BRACKET=282;
public final static short RIGHT_BRACKET=283;
public final static short OP_NOT_EQUALS=284;
public final static short OP_AND=285;
public final static short OP_OR=286;
public final static short OP_XOR=287;
public final static short OP_AT=288;
public final static short OP_NOT=289;
public final static short OP_EQUALS=290;
public final static short OP_LESS=291;
public final static short OP_GREATER=292;
public final static short OP_LESS_OR_EQUAL=293;
public final static short OP_GREATER_OR_EQUAL=294;
public final static short COMMA=295;
public final static short COLON_DEFINED=296;
public final static short DEFINED=297;
public final static short ASSIGN=298;
public final static short SIMPLE_COMMENT=299;
public final static short OTHER_TEXT=300;
public final static short UNFINISHED_STRING=301;
}
