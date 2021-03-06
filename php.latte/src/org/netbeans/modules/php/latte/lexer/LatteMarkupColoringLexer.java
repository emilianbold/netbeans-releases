/* The following code was generated by JFlex 1.4.3 on 1.6.16 7:36 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.latte.lexer;

import java.util.Objects;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.modules.web.common.api.ByteStack;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})

/**
 * This class is a scanner generated by
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.3
 * on 1.6.16 7:36 from the specification file
 * <tt>/home/gapon/worx/sun/nb-main/php.latte/tools/LatteMarkupColoringLexer.flex</tt>
 */
public class LatteMarkupColoringLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = LexerInput.EOF;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int ST_HIGHLIGHTING_ERROR = 8;
  public static final int ST_END_MACRO = 4;
  public static final int ST_OTHER = 2;
  public static final int YYINITIAL = 0;
  public static final int ST_IN_D_STRING = 6;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0,  0,  1,  1,  2,  2,  3,  3,  4, 4
  };

  /**
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED =
    "\11\0\1\1\1\53\2\0\1\1\22\0\1\1\1\0\1\2\1\0"+
    "\1\4\2\0\1\6\1\30\1\35\1\0\1\37\1\0\1\45\1\41"+
    "\1\52\12\40\1\42\2\0\1\43\1\44\2\0\1\14\1\26\1\23"+
    "\1\20\1\12\1\13\1\32\1\46\1\25\1\34\1\27\1\15\1\50"+
    "\1\17\1\21\1\31\1\36\1\10\1\16\1\7\1\11\1\51\1\24"+
    "\1\22\1\33\1\36\4\0\1\47\1\0\1\14\1\26\1\23\1\20"+
    "\1\12\1\13\1\32\1\46\1\25\1\34\1\27\1\15\1\50\1\17"+
    "\1\21\1\31\1\36\1\10\1\16\1\7\1\11\1\51\1\24\1\22"+
    "\1\33\1\36\1\3\1\0\1\5\uff82\0";

  /**
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /**
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\5\0\1\1\1\2\1\3\3\1\1\3\10\1\1\4"+
    "\1\5\1\6\1\5\1\6\13\7\2\5\1\10\3\5"+
    "\1\11\1\6\1\12\2\6\1\0\1\13\1\14\21\0"+
    "\1\3\6\0\1\15\1\16\1\0\1\12\1\7\1\0"+
    "\2\7\1\5\3\7\1\17\4\7\7\0\1\10\1\7"+
    "\11\0\1\11\1\20\4\0\1\12\3\0\1\3\25\0"+
    "\10\7\10\0\2\10\11\0\1\11\2\0\2\21\1\12"+
    "\1\0\1\3\16\0\4\7\7\0\1\10\1\11\14\0"+
    "\1\20\16\0\3\7\6\0\1\22\12\0\1\3\7\0"+
    "\1\7\15\0\1\3\1\7\7\0\1\7\2\0\1\7";

  private static int [] zzUnpackAction() {
    int [] result = new int[285];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\54\0\130\0\204\0\260\0\334\0\u0108\0\334"+
    "\0\u0134\0\u0160\0\u018c\0\u01b8\0\u01e4\0\u0210\0\u023c\0\u0268"+
    "\0\u0294\0\u02c0\0\u02ec\0\u0318\0\334\0\334\0\u0344\0\u0370"+
    "\0\u039c\0\u03c8\0\u03f4\0\u0420\0\u044c\0\u0478\0\u04a4\0\u04d0"+
    "\0\u04fc\0\u0528\0\u0554\0\u0580\0\u05ac\0\u05d8\0\u0604\0\u0630"+
    "\0\u065c\0\u0688\0\u06b4\0\u06e0\0\u070c\0\u0738\0\u0764\0\u06e0"+
    "\0\334\0\u0790\0\u07bc\0\u07e8\0\u0814\0\u0840\0\u086c\0\u0898"+
    "\0\u08c4\0\u08f0\0\u091c\0\u0948\0\u0974\0\u09a0\0\u09cc\0\u09f8"+
    "\0\u0a24\0\u0a50\0\u0a7c\0\u0aa8\0\u0ad4\0\u0b00\0\u0b2c\0\u0b58"+
    "\0\u0b84\0\u0344\0\334\0\u0370\0\u039c\0\334\0\u0bb0\0\u0bdc"+
    "\0\u0c08\0\u0c34\0\u0420\0\u0c60\0\u0c8c\0\u0cb8\0\u0420\0\u0ce4"+
    "\0\u0d10\0\u0d3c\0\u0d68\0\u0d94\0\u0dc0\0\u0dec\0\u0e18\0\u0e44"+
    "\0\u0e70\0\u0e9c\0\u0ec8\0\u0ef4\0\u0f20\0\u0f4c\0\u0f78\0\u0fa4"+
    "\0\u0fd0\0\u0ffc\0\u1028\0\u1054\0\u1080\0\334\0\u10ac\0\u10d8"+
    "\0\u1104\0\u10ac\0\u1130\0\u115c\0\u1188\0\u11b4\0\u11e0\0\u120c"+
    "\0\u1238\0\u1264\0\u1290\0\u12bc\0\u12e8\0\u1314\0\u1340\0\u136c"+
    "\0\u1398\0\u13c4\0\u13f0\0\u141c\0\u1448\0\u1474\0\u14a0\0\u14cc"+
    "\0\u14f8\0\u1524\0\u1550\0\u157c\0\u15a8\0\u15d4\0\u1600\0\u162c"+
    "\0\u1658\0\u1684\0\u16b0\0\u16dc\0\u1708\0\u1734\0\u1760\0\u178c"+
    "\0\u17b8\0\u17e4\0\u1810\0\u183c\0\u1868\0\u0ef4\0\u1894\0\u18c0"+
    "\0\u18ec\0\u1918\0\u1944\0\u1970\0\u199c\0\u19c8\0\u19f4\0\u1a20"+
    "\0\u1a4c\0\u1a78\0\u1aa4\0\u1188\0\334\0\u1ad0\0\u1afc\0\u1b28"+
    "\0\u1b54\0\u1b80\0\u1bac\0\u1bd8\0\u1c04\0\u1c30\0\u1c5c\0\u1c88"+
    "\0\u1cb4\0\u1ce0\0\u1d0c\0\u1d38\0\u1d64\0\u1d90\0\u1dbc\0\u1de8"+
    "\0\u1e14\0\u1e40\0\u1e6c\0\u1e98\0\u1ec4\0\u1ef0\0\u1f1c\0\u1f48"+
    "\0\u1f74\0\u1868\0\u1fa0\0\u1fcc\0\u1ff8\0\u2024\0\u2050\0\u207c"+
    "\0\u20a8\0\u20d4\0\u2100\0\u212c\0\u2158\0\u2184\0\u21b0\0\334"+
    "\0\u21dc\0\u2208\0\u2234\0\u2260\0\u228c\0\u22b8\0\u22e4\0\u2310"+
    "\0\u233c\0\u2368\0\u2394\0\u23c0\0\u23ec\0\u2418\0\u2444\0\u2470"+
    "\0\u249c\0\u24c8\0\u24f4\0\u2520\0\u254c\0\u2578\0\u25a4\0\334"+
    "\0\u25d0\0\u25fc\0\u2628\0\u2654\0\u2680\0\u26ac\0\u26d8\0\u2704"+
    "\0\u2730\0\u275c\0\u2788\0\u27b4\0\u27e0\0\u280c\0\u2838\0\u2864"+
    "\0\u2890\0\u28bc\0\u28e8\0\u2914\0\u2940\0\u296c\0\u2998\0\u29c4"+
    "\0\u29f0\0\u2a1c\0\u2a48\0\u2a74\0\u2aa0\0\u2acc\0\u2af8\0\u2b24"+
    "\0\u2b50\0\u2b7c\0\u2ba8\0\u2bd4\0\u2c00\0\u2c2c\0\u2c58\0\u2c84"+
    "\0\u2cb0\0\u2cdc\0\u2d08\0\u2d34\0\u2d60";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[285];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\6\1\7\6\6\1\10\1\11\1\12\1\13\1\6"+
    "\1\14\1\15\1\6\1\16\2\6\1\17\1\20\1\21"+
    "\1\22\2\6\1\23\15\6\1\10\1\6\1\24\1\25"+
    "\1\7\1\26\1\7\1\27\1\26\1\30\1\26\1\31"+
    "\1\32\1\33\2\34\1\35\1\36\2\34\1\37\1\34"+
    "\1\40\1\41\1\42\1\34\1\43\1\44\1\34\1\45"+
    "\4\34\1\26\1\34\1\46\1\47\1\26\1\50\1\51"+
    "\1\26\1\52\4\34\1\26\1\7\1\6\1\7\50\6"+
    "\1\53\1\7\2\54\1\55\1\56\1\57\46\54\1\60"+
    "\1\61\1\62\51\61\1\62\55\0\1\7\51\0\1\7"+
    "\16\0\1\63\52\0\1\64\4\0\1\65\52\0\1\66"+
    "\3\0\1\67\42\0\1\70\10\0\1\71\35\0\1\72"+
    "\2\0\1\73\4\0\1\74\13\0\1\75\31\0\1\76"+
    "\1\77\55\0\1\100\4\0\1\101\57\0\1\102\20\0"+
    "\1\103\20\0\1\104\3\0\1\105\44\0\1\106\4\0"+
    "\1\107\53\0\1\110\52\0\1\111\37\0\2\112\1\113"+
    "\51\112\7\0\21\114\1\0\4\114\1\0\1\114\1\0"+
    "\1\114\5\0\4\114\2\0\6\115\1\116\45\115\7\0"+
    "\1\34\1\117\17\34\1\0\4\34\1\0\1\34\1\0"+
    "\1\34\4\0\1\120\4\34\11\0\3\34\1\121\15\34"+
    "\1\0\4\34\1\0\1\34\1\0\1\34\4\0\1\120"+
    "\4\34\11\0\21\34\1\0\4\34\1\0\1\34\1\0"+
    "\1\34\4\0\1\120\4\34\11\0\5\34\1\122\13\34"+
    "\1\0\4\34\1\0\1\34\1\0\1\34\4\0\1\120"+
    "\4\34\11\0\7\34\1\123\1\124\10\34\1\0\4\34"+
    "\1\0\1\34\1\0\1\34\4\0\1\120\4\34\11\0"+
    "\2\34\1\125\1\126\15\34\1\0\4\34\1\0\1\34"+
    "\1\0\1\34\4\0\1\120\4\34\11\0\1\34\1\127"+
    "\17\34\1\0\4\34\1\0\1\34\1\0\1\34\4\0"+
    "\1\120\4\34\11\0\12\34\1\40\6\34\1\0\4\34"+
    "\1\0\1\34\1\0\1\34\4\0\1\120\4\34\11\0"+
    "\6\34\1\130\3\34\1\131\6\34\1\0\4\34\1\0"+
    "\1\34\1\0\1\34\4\0\1\120\4\34\11\0\10\34"+
    "\1\132\10\34\1\0\4\34\1\0\1\34\1\0\1\34"+
    "\4\0\1\120\4\34\11\0\1\34\1\133\17\34\1\0"+
    "\4\34\1\0\1\34\1\0\1\34\4\0\1\120\4\34"+
    "\14\0\1\134\1\135\1\136\1\0\1\137\2\0\1\140"+
    "\3\0\1\141\1\142\65\0\1\143\22\0\3\34\1\144"+
    "\15\34\1\0\4\34\1\0\1\34\1\0\1\47\1\145"+
    "\3\0\1\120\4\34\44\0\1\26\55\0\1\26\47\0"+
    "\1\143\3\0\1\26\22\0\1\146\1\0\1\147\1\150"+
    "\1\0\1\151\2\0\1\152\1\153\1\154\1\155\20\0"+
    "\1\156\4\0\2\60\1\157\1\160\1\161\47\60\2\55"+
    "\1\162\1\163\1\164\47\55\2\60\1\157\1\60\1\165"+
    "\47\60\5\0\1\60\1\0\21\114\1\0\4\114\1\0"+
    "\1\114\1\0\1\114\5\0\4\114\3\0\1\62\51\0"+
    "\1\62\12\0\1\10\57\0\1\166\44\0\1\167\54\0"+
    "\1\170\53\0\1\171\61\0\1\172\7\0\1\173\4\0"+
    "\1\174\37\0\1\175\50\0\1\176\70\0\1\10\47\0"+
    "\1\177\45\0\1\200\104\0\1\73\16\0\1\201\12\0"+
    "\1\202\50\0\1\203\5\0\1\204\41\0\1\205\54\0"+
    "\1\206\60\0\1\207\44\0\1\210\4\0\1\211\53\0"+
    "\1\212\5\0\1\213\34\0\1\214\62\0\1\215\57\0"+
    "\1\71\36\0\1\10\52\0\2\34\1\216\16\34\1\0"+
    "\4\34\1\0\1\34\1\0\1\34\4\0\1\120\4\34"+
    "\11\0\21\34\1\0\4\34\1\0\1\34\1\0\1\34"+
    "\5\0\4\34\11\0\1\217\20\34\1\0\4\34\1\0"+
    "\1\34\1\0\1\34\4\0\1\120\4\34\11\0\6\34"+
    "\1\220\12\34\1\0\4\34\1\0\1\34\1\0\1\34"+
    "\4\0\1\120\4\34\11\0\11\34\1\127\7\34\1\0"+
    "\4\34\1\0\1\34\1\0\1\34\4\0\1\120\4\34"+
    "\11\0\6\34\1\221\12\34\1\0\4\34\1\0\1\34"+
    "\1\0\1\34\4\0\1\120\4\34\11\0\15\34\1\127"+
    "\3\34\1\0\4\34\1\0\1\34\1\0\1\34\4\0"+
    "\1\120\4\34\11\0\12\34\1\222\6\34\1\0\4\34"+
    "\1\0\1\34\1\0\1\34\4\0\1\120\4\34\11\0"+
    "\10\34\1\223\10\34\1\0\4\34\1\0\1\34\1\0"+
    "\1\34\4\0\1\120\4\34\11\0\7\34\1\224\11\34"+
    "\1\0\4\34\1\0\1\34\1\0\1\34\4\0\1\120"+
    "\4\34\11\0\3\34\1\225\15\34\1\0\4\34\1\0"+
    "\1\34\1\0\1\34\4\0\1\120\4\34\24\0\1\226"+
    "\46\0\1\227\46\0\1\230\52\0\1\231\72\0\1\232"+
    "\44\0\1\233\55\0\1\234\44\0\1\235\25\0\1\143"+
    "\1\145\21\0\21\34\1\0\4\34\1\0\1\34\1\0"+
    "\1\236\4\0\1\120\4\34\42\0\1\237\34\0\1\240"+
    "\3\0\1\241\42\0\1\242\51\0\1\243\4\0\1\244"+
    "\13\0\1\245\32\0\1\246\55\0\1\247\105\0\1\250"+
    "\20\0\1\251\55\0\1\252\36\0\3\162\1\163\1\253"+
    "\47\162\2\60\1\157\1\60\1\254\47\60\5\0\1\60"+
    "\46\0\4\162\1\255\47\162\2\256\2\0\1\256\1\55"+
    "\46\256\5\0\1\257\60\0\1\260\53\0\1\261\53\0"+
    "\1\262\35\0\1\10\21\0\1\172\44\0\1\10\56\0"+
    "\1\263\62\0\1\213\61\0\1\10\33\0\1\264\75\0"+
    "\1\265\31\0\1\266\60\0\1\267\10\0\1\270\37\0"+
    "\1\271\110\0\1\63\14\0\1\272\53\0\1\273\76\0"+
    "\1\210\36\0\1\63\50\0\1\172\52\0\1\274\57\0"+
    "\1\275\47\0\1\172\56\0\1\276\62\0\1\175\37\0"+
    "\3\34\1\127\15\34\1\0\4\34\1\0\1\34\1\0"+
    "\1\34\4\0\1\120\4\34\11\0\2\34\1\277\16\34"+
    "\1\0\4\34\1\0\1\34\1\0\1\34\4\0\1\120"+
    "\4\34\11\0\7\34\1\216\11\34\1\0\4\34\1\0"+
    "\1\34\1\0\1\34\4\0\1\120\4\34\11\0\6\34"+
    "\1\127\12\34\1\0\4\34\1\0\1\34\1\0\1\34"+
    "\4\0\1\120\4\34\11\0\10\34\1\216\10\34\1\0"+
    "\4\34\1\0\1\34\1\0\1\34\4\0\1\120\4\34"+
    "\11\0\1\300\20\34\1\0\4\34\1\0\1\34\1\0"+
    "\1\34\4\0\1\120\4\34\11\0\1\301\20\34\1\0"+
    "\4\34\1\0\1\34\1\0\1\34\4\0\1\120\4\34"+
    "\11\0\5\34\1\302\13\34\1\0\4\34\1\0\1\34"+
    "\1\0\1\34\4\0\1\120\4\34\33\0\1\303\43\0"+
    "\1\304\42\0\1\305\53\0\1\306\77\0\1\307\26\0"+
    "\1\310\65\0\1\311\72\0\1\312\25\0\1\235\25\0"+
    "\1\237\23\0\1\313\53\0\1\314\61\0\1\315\7\0"+
    "\1\316\56\0\1\156\47\0\1\317\45\0\1\320\47\0"+
    "\1\321\63\0\1\322\5\0\1\323\47\0\1\324\44\0"+
    "\1\325\4\0\1\326\51\0\1\327\37\0\1\162\46\0"+
    "\2\256\2\0\50\256\2\257\1\330\1\257\1\165\47\257"+
    "\25\0\1\331\45\0\1\332\50\0\1\333\54\0\1\10"+
    "\47\0\1\334\73\0\1\210\36\0\1\335\50\0\1\336"+
    "\61\0\1\63\66\0\1\337\32\0\1\340\52\0\1\341"+
    "\1\0\1\342\12\0\1\343\36\0\1\344\54\0\1\345"+
    "\71\0\1\346\33\0\1\34\1\347\17\34\1\0\4\34"+
    "\1\0\1\34\1\0\1\34\4\0\1\120\4\34\11\0"+
    "\16\34\1\350\2\34\1\0\4\34\1\0\1\34\1\0"+
    "\1\34\4\0\1\120\4\34\11\0\5\34\1\351\13\34"+
    "\1\0\4\34\1\0\1\34\1\0\1\34\4\0\1\120"+
    "\4\34\11\0\20\34\1\127\1\0\4\34\1\0\1\34"+
    "\1\0\1\34\4\0\1\120\4\34\16\0\1\352\53\0"+
    "\1\353\53\0\1\354\64\0\1\355\40\0\1\356\53\0"+
    "\1\357\22\0\1\360\33\0\1\361\50\0\1\362\35\0"+
    "\1\156\21\0\1\315\44\0\1\156\56\0\1\363\72\0"+
    "\1\364\31\0\1\365\71\0\1\366\74\0\1\367\14\0"+
    "\1\370\61\0\1\367\50\0\1\315\52\0\1\371\65\0"+
    "\1\372\43\0\1\373\60\0\1\334\56\0\1\374\46\0"+
    "\1\10\57\0\1\10\46\0\1\172\64\0\1\375\35\0"+
    "\1\63\64\0\1\263\51\0\1\376\53\0\1\377\44\0"+
    "\1\u0100\63\0\1\u0101\60\0\1\u0102\35\0\10\34\1\127"+
    "\10\34\1\0\4\34\1\0\1\34\1\0\1\34\4\0"+
    "\1\120\4\34\11\0\10\34\1\117\10\34\1\0\4\34"+
    "\1\0\1\34\1\0\1\34\4\0\1\120\4\34\11\0"+
    "\10\34\1\u0103\10\34\1\0\4\34\1\0\1\34\1\0"+
    "\1\34\4\0\1\120\4\34\21\0\1\u0104\43\0\1\u0105"+
    "\77\0\1\u0105\37\0\1\u0106\57\0\1\353\62\0\1\u0107"+
    "\33\0\1\u0108\22\0\1\360\32\0\1\u0109\54\0\1\156"+
    "\67\0\1\325\36\0\1\u010a\56\0\1\367\46\0\1\156"+
    "\52\0\1\u010b\52\0\1\u010c\72\0\1\156\42\0\1\210"+
    "\103\0\1\10\15\0\1\u010d\52\0\1\u010e\55\0\1\u010f"+
    "\54\0\1\u0110\53\0\1\u0111\54\0\1\10\47\0\14\34"+
    "\1\u0112\4\34\1\0\4\34\1\0\1\34\1\0\1\34"+
    "\4\0\1\120\4\34\22\0\1\u0105\70\0\1\360\50\0"+
    "\1\u0105\33\0\1\u0113\55\0\1\u0114\62\0\1\u0115\52\0"+
    "\1\156\41\0\1\367\53\0\1\u0116\55\0\1\u0117\50\0"+
    "\1\u0118\56\0\1\346\60\0\1\172\62\0\1\u0119\34\0"+
    "\3\34\1\u011a\15\34\1\0\4\34\1\0\1\34\1\0"+
    "\1\34\4\0\1\120\4\34\12\0\1\u0105\62\0\1\u0105"+
    "\102\0\1\156\17\0\1\u011b\55\0\1\175\72\0\1\u011c"+
    "\35\0\1\107\45\0\12\34\1\u011d\6\34\1\0\4\34"+
    "\1\0\1\34\1\0\1\34\4\0\1\120\4\34\21\0"+
    "\1\315\65\0\1\63\31\0\4\34\1\127\14\34\1\0"+
    "\4\34\1\0\1\34\1\0\1\34\4\0\1\120\4\34"+
    "\2\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[11660];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\5\0\1\11\1\1\1\11\14\1\2\11\31\1\1\0"+
    "\1\11\1\1\21\0\1\1\6\0\1\11\1\1\1\0"+
    "\1\11\1\1\1\0\13\1\7\0\2\1\11\0\1\11"+
    "\1\1\4\0\1\1\3\0\1\1\25\0\10\1\10\0"+
    "\2\1\11\0\1\1\2\0\1\1\1\11\1\1\1\0"+
    "\1\1\16\0\4\1\7\0\2\1\14\0\1\11\16\0"+
    "\3\1\6\0\1\11\12\0\1\1\7\0\1\1\15\0"+
    "\2\1\7\0\1\1\2\0\1\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[285];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the
   * matched text
   */
  private int yycolumn;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF = false;

  /* user code: */

    private ByteStack stack = new ByteStack();
    private LexerInput input;

    public LatteMarkupColoringLexer(LexerRestartInfo info) {
        this.input = info.input();
        if(info.state() != null) {
            //reset state
            setState((LexerState) info.state());
        } else {
            zzState = zzLexicalState = YYINITIAL;
            stack.clear();
        }

    }

    private enum Syntax {
        LATTE,
        DOUBLE,
        ASP,
        PYTHON,
        OFF;
    }

    public static final class LexerState  {
        final ByteStack stack;
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;

        LexerState(ByteStack stack, int zzState, int zzLexicalState) {
            this.stack = stack;
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + Objects.hashCode(this.stack);
            hash = 29 * hash + this.zzState;
            hash = 29 * hash + this.zzLexicalState;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LexerState other = (LexerState) obj;
            if (!Objects.equals(this.stack, other.stack)) {
                return false;
            }
            if (this.zzState != other.zzState) {
                return false;
            }
            if (this.zzLexicalState != other.zzLexicalState) {
                return false;
            }
            return true;
        }
    }

    public LexerState getState() {
        return new LexerState(stack.copyOf(), zzState, zzLexicalState);
    }

    public void setState(LexerState state) {
        this.stack.copyFrom(state.stack);
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
    }

    protected int getZZLexicalState() {
        return zzLexicalState;
    }

    protected void popState() {
        yybegin(stack.pop());
    }

    protected void pushState(final int state) {
        stack.push(getZZLexicalState());
        yybegin(state);
    }


 // End user code



  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public LatteMarkupColoringLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public LatteMarkupColoringLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /**
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 172) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }



  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = zzPushbackPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return input.readText().toString();
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
     return input.readText().charAt(pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return input.readLength();
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    input.backup(number);
    //zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public LatteMarkupTokenId findNextToken() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    //int zzCurrentPosL;
    //int zzMarkedPosL;
    //int zzEndReadL = zzEndRead;
    //char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      //zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      //zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
      int tokenLength = 0;

      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {
            zzInput = input.read();

            if(zzInput == LexerInput.EOF) {
                //end of input reached
                zzInput = YYEOF;
                break zzForAction;
                //notice: currently LexerInput.EOF == YYEOF
            }

          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            tokenLength = input.readLength();
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      if(zzInput != YYEOF) {
         input.backup(input.readLength() - tokenLength);
      }

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 15:
          { return LatteMarkupTokenId.T_KEYWORD;
          }
        case 19: break;
        case 1:
          { yypushback(yylength());
        pushState(ST_OTHER);
          }
        case 20: break;
        case 6:
          { yypushback(1);
        pushState(ST_HIGHLIGHTING_ERROR);
          }
        case 21: break;
        case 3:
          { pushState(ST_OTHER);
        return LatteMarkupTokenId.T_MACRO_START;
          }
        case 22: break;
        case 16:
          { popState();
        return LatteMarkupTokenId.T_STRING;
          }
        case 23: break;
        case 17:
          { yypushback(1);
        return LatteMarkupTokenId.T_STRING;
          }
        case 24: break;
        case 5:
          { return LatteMarkupTokenId.T_CHAR;
          }
        case 25: break;
        case 11:
          { return LatteMarkupTokenId.T_ERROR;
          }
        case 26: break;
        case 7:
          { return LatteMarkupTokenId.T_SYMBOL;
          }
        case 27: break;
        case 2:
          { return LatteMarkupTokenId.T_WHITESPACE;
          }
        case 28: break;
        case 14:
          { return LatteMarkupTokenId.T_VARIABLE;
          }
        case 29: break;
        case 9:
          { pushState(ST_OTHER);
        return LatteMarkupTokenId.T_MACRO_END;
          }
        case 30: break;
        case 4:
          { yypushback(yylength());
        pushState(ST_END_MACRO);
          }
        case 31: break;
        case 10:
          { return LatteMarkupTokenId.T_STRING;
          }
        case 32: break;
        case 13:
          { yypushback(yylength());
        pushState(ST_IN_D_STRING);
          }
        case 33: break;
        case 8:
          { return LatteMarkupTokenId.T_NUMBER;
          }
        case 34: break;
        case 18:
          { return LatteMarkupTokenId.T_CAST;
          }
        case 35: break;
        case 12:
          { popState();
          }
        case 36: break;
        default:
          if (zzInput == YYEOF)
            //zzAtEOF = true;
              {         if(input.readLength() > 0) {
            // backup eof
            input.backup(1);
            //and return the text as error token
            return LatteMarkupTokenId.T_ERROR;
        } else {
            return null;
        }
 }

          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
