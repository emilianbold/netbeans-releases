/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.apt.impl.support.clank;

import org.clang.basic.IdentifierInfo;
import org.clang.basic.SourceManager;
import org.clang.basic.tok;
import org.clang.lex.Preprocessor;
import org.clang.lex.SmallVectorToken;
import org.clang.lex.Token;
import org.clank.java.std;
import static org.clank.java.std.$second_uint;
import org.clank.support.Casts;
import org.clank.support.Unsigned;
import org.clank.support.aliases.char$ptr;
import org.llvm.adt.StringMapEntryBase;
import org.llvm.adt.StringRef;
import org.llvm.adt.aliases.SmallVectorChar;
import org.netbeans.modules.cnd.apt.impl.support.APTCommentToken;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteConstTextToken;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteIdToken;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteLiteralToken;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.openide.util.CharSequences;
import org.clank.support.aliases.*;
import org.netbeans.modules.cnd.apt.impl.support.APTConstTextToken;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
/*package*/class ClankToAPTToken implements APTToken {

    private static final CharSequence COMMENT_TEXT_ID = CharSequences.create("/*COMMENT*/");

    static APTToken[] convertToAPT(Preprocessor PP, SmallVectorToken toks) {
        int nrTokens = toks.size();
        Token[] tokens = toks.$array();
        APTToken[] out = new APTToken[nrTokens];
        SmallVectorChar spell = new SmallVectorChar(1024);
        for (int i = 0; i < nrTokens; i++) {
            assert PP != null;
            SourceManager SM = PP.getSourceManager();
            assert SM != null;
            Token token = tokens[i];
            long/*<FileID, uint>*/ decomposedLoc = SM.getDecomposedExpansionLoc(token.getRawLocation());
            int offset = Unsigned.long2uint($second_uint(decomposedLoc));
            out[i] = ClankToAPTToken.convert(PP, token, offset, spell);
        }
        return out;
    }

    static APTToken convert(Preprocessor PP, Token token, /*uint*/int offset, SmallVectorChar spell) {
        if (token.is(tok.TokenKind.eof)) {
            return APTUtils.EOF_TOKEN;
        } else {
            int aptTokenType = ClankToAPTUtils.convertClankToAPTTokenKind(token.getKind());
            if (APTLiteConstTextToken.isApplicable(aptTokenType, offset, FAKE_COLUMN, FAKE_LINE)) {
                APTToken out = new APTLiteConstTextToken(aptTokenType, offset, FAKE_COLUMN, FAKE_LINE);
                return out;
            } else if (aptTokenType == APTTokenTypes.COMMENT) {
                APTCommentToken out = new APTCommentToken();
                out.setOffset(offset);
                out.setTextLength(offset + token.getLength());
                out.setColumn(FAKE_COLUMN);
                out.setLine(FAKE_LINE);
                return out;
            } else {
                // all remainings
                CharSequence textID;
                IdentifierInfo II = token.getIdentifierInfo();
                if (II != null) {
                    StringMapEntryBase entry = II.getEntry();
                    assert entry != null;
                    textID = CharSequences.create(entry.getKeyArray(), entry.getKeyArrayIndex(), entry.getKeyLength());
                } else {
                    textID = null;
                    char$ptr SpellingData = null;
                    int SpellingLen = 0;
                    if (token.isLiteral()) {
                        char$ptr literalData = token.getLiteralData();
                        if (literalData == null) {
                            // i.e. the case of lazy calculated DATE and TIME based strings
                            StringRef spelling = PP.getSpelling(token, spell);
                            SpellingData = spelling.begin();
                            SpellingLen = spelling.size();
                            spell.set_size(0);
                        } else {
                            SpellingData = literalData;
                            SpellingLen = token.getLength();
                        }
                    } else {
                        if (token.is(tok.TokenKind.raw_identifier)) {
                          byte[] $CharPtrData = token.$CharPtrData();
                          if ($CharPtrData != null) {
                              textID = CharSequences.create($CharPtrData, token.$CharPtrDataIndex(), token.getLength());
                          } else {
                              SpellingData = token.getRawIdentifierData();
                              SpellingLen = token.getLength();
                          }
                        }
                    }
                    if (textID == null) {
                        if (SpellingData == null) {
                            StringRef spelling = PP.getSpelling(token, spell);
                            SpellingData = spelling.begin();
                            SpellingLen = spelling.size();
                            spell.set_size(0);
                        }
                        assert SpellingData != null : "" + token;
                        if (SpellingData instanceof char$ptr$array) {
                            textID = CharSequences.create(SpellingData.$array(), SpellingData.$index(), SpellingLen);
                        } else {
                            textID = Casts.toCharSequence(SpellingData, SpellingLen);
                        }
                    }
                }
                assert textID != null : "" + token;
                int literalType = aptTokenType;
                if (aptTokenType > APTTokenTypes.FIRST_LITERAL_TOKEN && aptTokenType < APTTokenTypes.LAST_LITERAL_TOKEN) {
                    aptTokenType = APTTokenTypes.IDENT;
                }
                if (APTLiteLiteralToken.isApplicable(APTTokenTypes.IDENT, offset, FAKE_COLUMN, FAKE_LINE, literalType)) {
                  CharSequence LiteText = APTConstTextToken.getConstTextID(literalType);
                  // check if spelling in clang the same as our token, then reuse
                  // APTLiteLiteralToken otherwise create fallback to APTLiteIdToken with known textID
                  if (CharSequences.comparator().compare(textID, LiteText) == 0) {
                      return new APTLiteLiteralToken(offset, FAKE_COLUMN, FAKE_LINE, literalType);
                  }
                }
                if (APTLiteIdToken.isApplicable(aptTokenType, offset, FAKE_COLUMN, FAKE_LINE)){
                  return new APTLiteIdToken(offset, FAKE_COLUMN, FAKE_LINE, textID);
                } else {
                  return new ClankToAPTToken(token, aptTokenType, offset, textID);
                }
            }
        }
    }
    private static final int FAKE_LINE = 333;
    private static final int FAKE_COLUMN = 111;

    private final int endOffset;
    private final int aptTokenType;
    private final int offset;
    private final CharSequence textID;

    private ClankToAPTToken(Token token, int tokenType, int offset, CharSequence text) {
        this.offset = offset;
        assert offset >= 0 : "negative " + offset + " for " + token;
        this.endOffset = this.offset + token.getLength();
        this.aptTokenType = tokenType;
        assert !(APTLiteConstTextToken.isLiteConstTextType(aptTokenType));
        assert !(APTLiteLiteralToken.isApplicable(APTTokenTypes.IDENT, offset, FAKE_COLUMN, FAKE_LINE, aptTokenType));
        assert !(APTLiteIdToken.isApplicable(aptTokenType, offset, FAKE_COLUMN, FAKE_LINE));
        assert (text != null);
        assert CharSequences.isCompact(text);
        assert (token.isNot(tok.TokenKind.comment));
        textID = TextCache.getManager().getString(text);
        assert textID.length() <= token.getLength(): textID + "\n vs. \n" + token;
    }

    @Override
    public int getType() {
        return aptTokenType;
    }

    @Override
    public String getText() {
        return getTextID().toString();
    }

    @Override
    public CharSequence getTextID() {
        return textID;
    }

    @Override
    public String toString() {
        return "ClankToAPTToken{" + "aptType=" + APTUtils.getAPTTokenName(aptTokenType) + ":" + textID + '}';
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public void setEndOffset(int o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getEndColumn() {
        return 222;
    }

    @Override
    public void setEndColumn(int c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getEndLine() {
        return 444;
    }

    @Override
    public void setEndLine(int l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTextID(CharSequence id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getProperty(Object key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getColumn() {
        return FAKE_COLUMN;
    }

    @Override
    public void setColumn(int c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLine() {
        return FAKE_LINE;
    }

    @Override
    public void setLine(int l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public void setFilename(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setText(String t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setType(int t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
