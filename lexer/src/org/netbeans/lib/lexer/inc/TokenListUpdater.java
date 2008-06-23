/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.lib.lexer.inc;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.EmbeddedJoinInfo;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.JoinLexerInputOperation;
import org.netbeans.lib.lexer.JoinTokenList;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.PartToken;


/**
 * Token list updater fixes a list of tokens constructed for a document
 * after text of the document gets modified.
 * <br>
 * Subclasses need to define all the abstract methods
 * so that the updating method can work on real token sequences.
 *
 * <p>
 * Updater looks similar to list iterator
 * but there are differences in the semantics
 * of iterator's modification operations.
 * <br/>
 * The algorithm used in the {@link #update(int, int)}
 * is based on "General Incremental Lexical Analysis" written
 * by Tim A. Wagner and Susan L. Graham, University
 * of California, Berkeley. It's available online
 * at <a href="http://www.cs.berkeley.edu/Research/Projects/harmonia/papers/twagner-lexing.pdf">
 * twagner-lexing.pdf</a>.
 * <br/>
 * Ending <code>EOF</code> token is not used but the lookahead
 * of the ending token(s) is increased by one (past the end of the input)
 * if they have reached the EOF.
 * <br/>
 * Non-startable tokens are not supported.
 * <br/>
 * When updating a token with lookback one as a result
 * of modification the lookahead of the preceding token is inspected
 * to find out whether the modification has really affected it.
 * This can often save the previous token from being relexed.
 * <br/>
 * Currently the algorithm computes the lookback values on the fly
 * and it does not store the lookback in the tokens. For typical languages
 * the lookback is reasonably small (0, 1 or 2) so it's usually not worth
 * to consume extra space in token instances for storing of the lookback.
 * There would also be an additional overhead of updating the lookback
 * values in the tokens after the modification and the algorithm code would
 * be somewhat less readable.
 * </p>
 *
 * <p>
 * The algorithm removes the affected tokens in the natural order as they
 * follow in the token stream. That can be used when the removed tokens
 * need to be collected (e.g. in an array).
 * <br/>
 * If the offset and state after token recognition matches
 * the end offset and state after recognition of the originally present
 * token then the relexing is stopped because a match was found and the newly
 * produced tokens would match the present ones.
 * <br/>
 * Otherwise the token(s) in the list are removed and replaced
 * by the relexed token and the relexing continues until a match is reached.
 * </p>
 * 
 * <p>
 * When using token list updater with JoinTokenList.Mutable there is a special treatment
 * of offsets independent of the underlying JoinTokenListChange and LexerInputOperation.
 * The updater treats the modOffset to be relative (in the number of characters)
 * to the relexOffset point (which is a real first relexed token's offset; it's necessary
 * for restarting of the lexer input operation) so when going over a JoinToken
 * the modOffset must be recomputed to not contain the gaps between individual join token parts.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenListUpdater {

    // -J-Dorg.netbeans.lib.lexer.inc.TokenListUpdater.level=FINE
    private static final Logger LOG = Logger.getLogger(TokenListUpdater.class.getName());

    /**
     * Use incremental algorithm to update a regular list of tokens (IncTokenList or EmbeddedTokenList)
     * after a modification done in the underlying storage.
     * 
     * @param change non-null change that will incorporate the performed chagnes.
     * @param eventInfo non-null info about modification offset and inserted and removed length.
     */
    static <T extends TokenId> void updateRegular(TokenListChange<T> change, TokenHierarchyEventInfo eventInfo) {
        MutableTokenList<T> tokenList = change.tokenList();
        int tokenCount = tokenList.tokenCountCurrent();
        boolean loggable = LOG.isLoggable(Level.FINE);
        if (loggable) {
            logModification(tokenList, eventInfo, false);
        }
        
        // Find modified token by binary search in existing tokens
        // Use LexerUtilsConstants.tokenIndexBinSearch() to NOT lazily create new tokens here
        int[] indexAndTokenOffset = LexerUtilsConstants.tokenIndexBinSearch(tokenList, eventInfo.modOffset(), tokenCount);
        // Index and offset from which the relexing will start
        int relexIndex = indexAndTokenOffset[0];
        // relexOffset points to begining of a token in which the modification occurred
        // or which is affected by a modification (its lookahead points beyond modification point).
        int relexOffset = indexAndTokenOffset[1];
        if (relexIndex == -1) { // No tokens at all
            relexIndex = 0;
            relexOffset = tokenList.startOffset();
        }

        // Index of token before which the relexing will end (or == tokenCount)
        int matchIndex = relexIndex;
        // Offset of token at matchIndex
        int matchOffset = relexOffset;

        if (relexIndex == tokenCount) { // Change right at end of last token or beyond it (if not fully lexed)
            // relexOffset set to end offset of the last token
            if (!tokenList.isFullyLexed() && eventInfo.modOffset() >= relexOffset +
                    ((relexIndex > 0) ? tokenList.lookahead(relexIndex - 1) : 0)
            ) { // Do nothing if beyond last token's lookahed
                // Check whether the last token could be affected at all
                // by checking whether the modification was performed
                // in the last token's lookahead.
                // For fully lexed inputs the characters added to the end
                // must be properly lexed and notified (even if the last present
                // token has zero lookahead).
                if (loggable) {
                    LOG.log(Level.FINE, "TLU.updateRegular() FINISHED: Not fully lexed yet. rOff=" +
                            relexOffset + ", mOff=" + eventInfo.modOffset() + "\n");
                }
                change.setIndex(relexIndex);
                change.setOffset(relexOffset);
                change.setMatchIndex(matchIndex); // matchIndex == relexIndex
                change.setMatchOffset(matchOffset); // matchOffset == relexOffset
                tokenList.replaceTokens(change, eventInfo.diffLength());
                return; // not affected at all
            } // change.setIndex() will be performed later in relex()

            // Leave matchOffset as is (will possibly end relexing at tokenCount and unfinished relexing
            // will be continued by replaceTokens()).
            // For fully lexed lists it is necessary to lex till the end of input.
            if (tokenList.isFullyLexed())
                matchOffset = Integer.MAX_VALUE;

        } else { // relexIndex < tokenCount
            // Possibly increase matchIndex and matchOffset by skipping the tokens in the removed area
            if (eventInfo.removedLength() > 0) { // At least remove token at relexOffset
                matchOffset += tokenList.tokenOrEmbeddingUnsync(matchIndex++).token().length();
                int removedEndOffset = eventInfo.modOffset() + eventInfo.removedLength();
                while (matchOffset < removedEndOffset && matchIndex < tokenCount) {
                    matchOffset += tokenList.tokenOrEmbeddingUnsync(matchIndex++).token().length();
                }
            } else { // For inside-token inserts match on the next token
                if (matchOffset < eventInfo.modOffset()) {
                    matchOffset += tokenList.tokenOrEmbeddingUnsync(matchIndex++).token().length();
                }
            }
            // Update the matchOffset so that it corresponds to the state
            // after the modification
            matchOffset += eventInfo.diffLength();
        }

        // Check whether modification affected previous token
        while (relexIndex > 0 && relexOffset + tokenList.lookahead(relexIndex - 1) > eventInfo.modOffset()) {
            relexIndex--;
            if (loggable) {
                LOG.log(Level.FINE, "    Token at rInd=" + relexIndex + " affected (la=" + // NOI18N
                        tokenList.lookahead(relexIndex) + ") => relex it\n"); // NOI18N
            }
            AbstractToken<T> token = tokenList.tokenOrEmbeddingUnsync(relexIndex).token();
            relexOffset -= token.length();
        }
        
        // Check whether actual relexing is necessary
        // State from which the lexer can be started
        Object relexState = (relexIndex > 0) ? tokenList.state(relexIndex - 1) : null;
        change.setIndex(relexIndex);
        change.setOffset(relexOffset);
        change.setMatchIndex(matchIndex);
        change.setMatchOffset(matchOffset);
        
        // Check whether relexing is necessary.
        // Necessary condition for no-relexing is a removal at token's boundary
        // and the token right before modOffset must have zero lookahead (if lookahead would be >0 
        // then the token would be affected) and the states before relexIndex must equal
        // to the state before matchIndex.
        boolean relex = (relexOffset != matchOffset)
                || (eventInfo.insertedLength() > 0)
                || (matchIndex == 0) // ensure the tokenList.state(matchIndex - 1) will not fail with IOOBE
                || !LexerUtilsConstants.statesEqual(relexState, tokenList.state(matchIndex - 1));

        // There is an extra condition that the lookahead of the matchToken
        // must not span the next (retained) token. This condition helps to ensure
        // that the lookaheads will be the same like during regular batch lexing.
        // As the empty tokens are not allowed the situation may only occur
        // for lookahead > 1.
        int lookahead;
        if (!relex && (lookahead = tokenList.lookahead(matchIndex - 1)) > 1 && matchIndex < tokenCount) {
            // Check whether lookahead of the token before match point exceeds the whole token right after match point
            relex = (lookahead > tokenList.tokenOrEmbeddingUnsync(matchIndex).token().length()); // check next token
        }

        if (loggable) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("updateRegular() BEFORE-RELEX:\n  relex=").append(relex);
            sb.append(", rInd=").append(relexIndex).append(", rOff=").append(relexOffset);
            sb.append(", mInd=").append(matchIndex).append(", mOff=").append(matchOffset).append('\n');
            sb.append(", rSta=").append(relexState).append(", tokenList-part:\n");
            LexerUtilsConstants.appendTokenList(sb, tokenList, matchIndex, matchIndex - 3, matchIndex + 3, false, 4, false);
            sb.append('\n');
            LOG.log(Level.FINE, sb.toString());
        }

        assert (relexIndex >= 0);
        if (relex) {
            // Create lexer input operation for the given token list
            LexerInputOperation<T> lexerInputOperation
                    = tokenList.createLexerInputOperation(relexIndex, relexOffset, relexState);
            relex(change, lexerInputOperation, tokenCount);
        }

        tokenList.replaceTokens(change, eventInfo.diffLength());
        if (loggable) {
            LOG.log(Level.FINE, "TLU.updateRegular() FINISHED: change:" + change + "\nMods:" + change.toStringMods(4));
        }
    }


    /**
     * Use incremental algorithm to update a JoinTokenList after a modification done in the underlying storage.
     * <br>
     * The assumption is that there may only be two states:
     * <ul>
     *   <li> There is a local input source modification bounded to a particular ETL.
     *        In such case there should be NO token lists removed/added.
     *   </li>
     *   <li> The modification spans multiple ETLs and all the affected ETLs will be removed
     *        and possibly new ones inserted.
     *        The modification is "bounded" by the removed ETLs i.e.
     *            modOffset &gt;= first-removed-ETL.startOffset()
     *        and modOffset + removedLength &lt;= last-removed-ETL.endOffset()
     *   </li>
     * </ul>
     * 
     * @param change non-null change that will incorporate the performed chagnes.
     * @param eventInfo non-null info about modification offset and inserted and removed length.
     */
    static <T extends TokenId> void updateJoined(JoinTokenListChange<T> change, TokenHierarchyEventInfo eventInfo) {
        MutableJoinTokenList<T> jtl = (MutableJoinTokenList<T>) change.tokenList();
        TokenListListUpdate<T> tokenListListUpdate = change.tokenListListUpdate();
        int tokenCount = jtl.tokenCountCurrent();
        boolean loggable = LOG.isLoggable(Level.FINE);
        if (loggable) {
            logModification(jtl, eventInfo, true);
        }
        
        // First determine what area is affected by removed/added ETLs
        int relexJoinIndex;
        int modOffset = eventInfo.modOffset();
        // Relative distance of mod against relex point (or point of ETLs added/removed)
        int relModOffset;
        if (tokenListListUpdate.isTokenListsMod()) {
            // Find relexJoinIndex by examining previous ETL
            // This way the code is more uniform than examining ETL at modTokenListIndex.
            if (tokenListListUpdate.modTokenListIndex > 0) { // non-first ETL
                // Use previous ETL for inspecting join token etc.
                jtl.setActiveTokenListIndex(tokenListListUpdate.modTokenListIndex - 1);
                relexJoinIndex = jtl.activeEndJoinIndex();
                if (jtl.activeTokenList().joinInfo.joinTokenLastPartShift() > 0) { // Mod points inside join token
                    // Find first non-empty ETL below to determine partTextOffset()
                    // Since LPS > 0 there must be first non-empty part (thus non-empty ETL) below
                    //   and therefore no need to check for jtl.activeTokenListIndex() > 0
                    while (jtl.activeTokenList().tokenCountCurrent() == 0) { // No tokens in ETL
                        jtl.setPrevActiveTokenListIndex();
                    }
                    // relexEtl is non-empty - last token is PartToken
                    relModOffset = ((PartToken<T>) jtl.activeTokenList().tokenOrEmbeddingUnsync(
                            jtl.activeTokenList().tokenCountCurrent() - 1).token()).partTextEndOffset();
                } else { // Not a join token => right after regular token
                    relModOffset = 0;
                }
            } else { // (modTokenListIndex == 0)
                // It's possible that all ETLs will be removed. In such case the subsequent
                //   JoinLexerInputOperation init (that picks an active ETL) would fail
                // since it's fetching after-update-ETLs and there would be none.
                if (jtl.tokenListCount() + tokenListListUpdate.tokenListCountDiff() == 0) {
                    // Only replace token lists and stop
                    change.replaceTokenLists();
                    return;
                }
                if (tokenCount > 0) { // Only position if tokens (and ETLs) exist
                    jtl.setActiveTokenListIndex(0); // Should exist since (jtl.tokenListCount() > 0)
                }
                relexJoinIndex = 0;
                relModOffset = 0;
            }

        } else { // No token list mod
            assert ((eventInfo.insertedLength() > 0) || (eventInfo.removedLength() > 0)) : "No modification";
            jtl.setActiveTokenListIndex(tokenListListUpdate.modTokenListIndex); // Index of ETL where a change occurred.
            change.charModTokenList = jtl.activeTokenList();
            // Search within releEtl only - can use binary search safely (unlike on JTL with removed ETLs)
            int[] indexAndTokenOffset = jtl.activeTokenList().tokenIndex(modOffset); // Index could be -1 TBD
            int localIndex = indexAndTokenOffset[0];
            relexJoinIndex = jtl.activeTokenList().joinInfo.joinTokenIndex() + localIndex;
            if (localIndex != jtl.activeTokenList().tokenCountCurrent()) { // tokenCount may be zero
                // Check whether the token is joined and if so then relModOffset must be computed specially
                AbstractToken<T> modToken = jtl.activeTokenList().tokenOrEmbeddingUnsync(localIndex).token();
                relModOffset = modOffset - indexAndTokenOffset[1];
                if (modToken.getClass() == PartToken.class) {
                    PartToken<T> partToken = (PartToken<T>) modToken;
                    relModOffset += partToken.partTextOffset();
                } // For regular tokens the value is fine
            } else if (jtl.activeTokenList().joinInfo.joinTokenLastPartShift() > 0) { // Inside join token
                relexJoinIndex--; // Do not count the partToken at end of active ETL
                // Find first non-empty ETL below to determine partTextOffset()
                // Since LPS > 0 there must be first non-empty part (thus non-empty ETL) below
                //   and therefore no need to check for jtl.activeTokenListIndex() > 0
                while (jtl.activeTokenList().tokenCountCurrent() == 0) { // No tokens in ETL
                    jtl.setPrevActiveTokenListIndex();
                }
                // relexEtl is non-empty - last token is PartToken
                relModOffset = ((PartToken<T>) jtl.activeTokenList().tokenOrEmbeddingUnsync(
                        jtl.activeTokenList().tokenCountCurrent() - 1).token()).partTextEndOffset();
            } else { // At end of regular token => relModOffset=0
                relModOffset = 0;
            }
        }

        // Matching point index and offset. Matching point vars are assigned early
        // and relex-vars are possibly shifted down first and then the match-vars are updated.
        // That's because otherwise the "working area" of JTL (above/below token list mod)
        // would have to be switched below and above.
        int matchJoinIndex = relexJoinIndex;
        // matchOffset holds offset at matchJoinIndex (where lexing can possibly match)
        // For now set it to begining of a non-joined mod token
        //   - suitable for single-ETL update (other cases will be corrected later)
        int matchOffset = modOffset - relModOffset;
        
        // Update relex-vars according to lookahead of tokens before relexJoinIndex
        while (relexJoinIndex > 0 && jtl.lookahead(relexJoinIndex - 1) > relModOffset) {
            AbstractToken<T> relexToken = jtl.tokenOrEmbeddingUnsync(--relexJoinIndex).token();
            relModOffset += relexToken.length(); // User regular token.length() here
            if (loggable) {
                LOG.log(Level.FINE, "    Token at rInd=" + relexJoinIndex + " affected (la=" + // NOI18N
                        jtl.lookahead(relexJoinIndex) + ") => relex it\n"); // NOI18N
            }
        }

        // Create lexer input operation now since JTL should be positioned before removed ETLs
        // and JLIO needs to scan tokens backwards for fly sequence length.
        Object relexState = (relexJoinIndex > 0) ? jtl.state(relexJoinIndex - 1) : null;
        int relexLocalIndex;
        int relexOffset;
        if (relexJoinIndex < tokenCount) {
            relexLocalIndex = jtl.tokenStartLocalIndex(relexJoinIndex);
            // Special case when inserting at end of a token with zero lookahead
            //   that is a last in an ETL:
            //     doc:"{x}<a>y" and insert(3,"u")
            // Then the relexJoinIndex would be 1 and relexOffset would point to "y"
            //   but it has to point to the inserted 'u'.
            if (jtl.activeTokenListIndex() > tokenListListUpdate.modTokenListIndex) {
                jtl.setPrevActiveTokenListIndex();
                // The last token will not be join token since otherwise this situation would not happen
                relexLocalIndex = jtl.activeTokenList().tokenCountCurrent();
                relexOffset = modOffset;
            } else { // Regular case
                // Check whether the relexing points to ETL being removed and possibly use the after-update-ETL
                if (tokenListListUpdate.isTokenListsMod() && jtl.activeTokenListIndex() == tokenListListUpdate.modTokenListIndex) {
                    assert (relexLocalIndex == 0);
                    relexOffset = tokenListListUpdate.afterUpdateTokenList(jtl, tokenListListUpdate.modTokenListIndex).startOffset();
                } else { // May need to get first added ETL
                    relexOffset = jtl.activeTokenList().tokenOffsetByIndex(relexLocalIndex);
                }
            }
        } else { // Relexing at token count
            if (jtl.tokenListCount() > 0) {
                jtl.setActiveTokenListIndex(jtl.tokenListCount() - 1);
                relexLocalIndex = jtl.activeTokenList().tokenCountCurrent();
                relexOffset = jtl.activeTokenList().endOffset();
            } else { // No token lists => the TLLUpdate should add some
                relexLocalIndex = 0;
                relexOffset = tokenListListUpdate.addedTokenLists.get(0).startOffset();
            }
        }

        // If TLL has no ETLs then its activeTokenListIndex == -1 => use 0 then (first added ETL).
        int relexTokenListIndex = Math.max(jtl.activeTokenListIndex(), 0);
        JoinLexerInputOperation<T> lexerInputOperation = new MutableJoinLexerInputOperation<T>(
                jtl, relexJoinIndex, relexState, relexTokenListIndex, relexOffset, tokenListListUpdate);
        lexerInputOperation.init();
        change.setIndex(relexJoinIndex);
        change.setOffset(relexOffset);
        change.setStartInfo(lexerInputOperation, relexLocalIndex);
        // setMatchIndex() and setMatchOffset() called later below

        // Index of token before which the relexing will end (or == tokenCount)
        if (tokenListListUpdate.isTokenListsMod()) { // Assign first token after last removed ETL
            int afterModTokenListIndex = tokenListListUpdate.modTokenListIndex + tokenListListUpdate.removedTokenListCount;
            if (afterModTokenListIndex > 0) {
                // Inspect prev ETL
                jtl.setActiveTokenListIndex(afterModTokenListIndex - 1);
                matchJoinIndex = jtl.activeEndJoinIndex();
                if (jtl.activeTokenList().joinInfo.joinTokenLastPartShift() > 0) { // Inside join token
                    // Will end somewhere in ETLs that stay
                    matchJoinIndex++;
                    if (matchJoinIndex < tokenCount) {
                        JoinToken<T> joinToken = (JoinToken<T>)jtl.tokenOrEmbeddingUnsync(matchJoinIndex - 1).token();
                        matchOffset = joinToken.endOffset();
                    } else {
                        matchOffset = Integer.MAX_VALUE;
                    }
                } else { // Not inside join token => Use end of last added ETL or prev removed ETL
                    int addedTokenListsCount = tokenListListUpdate.addedTokenLists.size();
                    if (addedTokenListsCount > 0) {
                        matchOffset = tokenListListUpdate.addedTokenLists.get(addedTokenListsCount - 1).endOffset();
                    } else { // Use end of ETL before mod
                        if (tokenListListUpdate.modTokenListIndex > 0) {
                            jtl.setActiveTokenListIndex(tokenListListUpdate.modTokenListIndex - 1);
                            matchOffset = jtl.activeTokenList().endOffset();
                        } else { // Removal at 
                            matchOffset = relexOffset; // should be fine since it should get relexed
                        }
                    }
                }
            } else { // afterModTokenListIndex == 0
                // Leave equal to relexJoinIndex and relexOffset
            }
            
        } else { // No token ETLs removed/added
            // matchOffset already initialized to (modOffset - orig-relModOffset).
            // Flag whether matchOffset should be updated by eventInfo.diffLength()
            //  In case the modified token is part token its end (to which matchOffset will point)
            //  will point into another ETL which has its startOffset already updated
            //  so the flag will be set to false.
            boolean matchOffsetModUpdate = true;
            int removedEndOffset = modOffset + eventInfo.removedLength();
            if (removedEndOffset > modOffset || matchOffset != modOffset) {
                AbstractToken<T> modToken = jtl.tokenOrEmbeddingUnsync(matchJoinIndex++).token();
                // At least remove modToken (token around modOffset)
                if (modToken.getClass() == JoinToken.class) {
                    matchOffset = ((JoinToken<T>) modToken).endOffset();
                    // JTL's active index should be positioned to last part
                    if (jtl.activeTokenListIndex() != tokenListListUpdate.modTokenListIndex) {
                        matchOffsetModUpdate = false;
                    }
                } else { // modToken is regular token
                    // matchOffset points to begining of modToken => make it point to its end
                    matchOffset += modToken.length();
                }
                // Possibly increase matchOffset as result of a longer removal
                while (matchOffset < removedEndOffset && matchJoinIndex < tokenCount) {
                    AbstractToken<T> token = jtl.tokenOrEmbeddingUnsync(matchJoinIndex++).token();
                    if (token.getClass() == JoinToken.class) {
                        matchOffset = ((JoinToken<T>) token).endOffset();
                        if (jtl.activeTokenListIndex() != tokenListListUpdate.modTokenListIndex) {
                            matchOffsetModUpdate = false;
                        }
                    } else { // modToken is regular token
                        // matchOffset points to begining of modToken => make it point to its end
                        matchOffset += token.length();
                    }
                }
            } else { // For inside-token inserts match on the next token
                if (matchOffset != modOffset) { // If the insert was not at token's begining
                    AbstractToken<T> token = jtl.tokenOrEmbeddingUnsync(matchJoinIndex++).token();
                    if (token.getClass() == JoinToken.class) {
                        matchOffset = ((JoinToken<T>) token).endOffset();
                        if (jtl.activeTokenListIndex() != tokenListListUpdate.modTokenListIndex) {
                            matchOffsetModUpdate = false;
                        }
                    } else { // modToken is regular token
                        // matchOffset points to begining of modToken => make it point to its end
                        matchOffset += token.length();
                    }
                }
            }
            // Update the matchOffset so that it corresponds to the state
            // after the modification
            if (matchOffsetModUpdate) {
                matchOffset += eventInfo.diffLength();
            }
        }

        // TBD relexing necessity optimizations like in updateRegular()
        change.setMatchIndex(matchJoinIndex);
        change.setMatchOffset(matchOffset);
        if (loggable) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("updateJoined() BEFORE-RELEX:");
            sb.append(", rInd=").append(relexJoinIndex).append(", rOff=").append(relexOffset);
            sb.append(", mInd=").append(matchJoinIndex).append(", mOff=").append(matchOffset).append('\n');
            sb.append(", rSta=").append(relexState).append(", tokenList-part:\n");
//            LexerUtilsConstants.appendTokenList(sb, tokenList, matchIndex, matchIndex - 3, matchIndex + 3, false, 4, false);
            sb.append('\n');
            LOG.log(Level.FINE, sb.toString());
        }
        // Perform relexing
        relex(change, lexerInputOperation, tokenCount);
        // addedEndOffset set in relex()
        jtl.replaceTokens(change, eventInfo.diffLength());
        if (loggable) {
            LOG.log(Level.FINE, "TLU.updateJoined() FINISHED: change:" + change + // NOI18N
                    "\nMods:" + change.toStringMods(4)); // NOI18N
        }
    }


    /**
     * Relex part of input to create new tokens. This method may sometimes be skipped e.g. for removal of chars
     * corresponding to a single token preceded by a token with zero lookahead.
     * <br/>
     * This code is common for both updateRegular() and updateJoined().
     * 
     * @param change non-null token list change.
     * @param lexerInputOperation non-null lexer input operation by which the new tokens
     *  will be produced.
     * @param change non-null token list change into which the created tokens are being added.
     * @param tokenCount current token count in tokenList.
     */
    private static <T extends TokenId> void relex(TokenListChange<T> change,
            LexerInputOperation<T> lexerInputOperation, int tokenCount
    ) {
        boolean loggable = LOG.isLoggable(Level.FINE);
        MutableTokenList<T> tokenList = change.tokenList();
        // Remember the match index below which the comparison of extra relexed tokens
        // (matching the original ones) cannot go.
        int lowestMatchIndex = change.matchIndex;

        AbstractToken<T> token;
        int relexOffset = lexerInputOperation.lastTokenEndOffset();
        while ((token = lexerInputOperation.nextToken()) != null) {
            // Get lookahead and state; Will certainly use them both since updater runs for inc token lists only
            int lookahead = lexerInputOperation.lookahead();
            Object state = lexerInputOperation.lexerState();
            if (loggable) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("LEXED-TOKEN: ");
                int tokenEndOffset = lexerInputOperation.lastTokenEndOffset();
                CharSequence inputSourceText = tokenList.inputSourceText();
                if (tokenEndOffset > inputSourceText.length()) {
                    sb.append(tokenEndOffset).append("!! => ");
                    tokenEndOffset = inputSourceText.length();
                    sb.append(tokenEndOffset);
                }
                sb.append('"');
                CharSequenceUtilities.debugText(sb, inputSourceText.subSequence(relexOffset, tokenEndOffset));
                sb.append('"');
                token.dumpInfo(sb, null, false, false, 0);
                sb.append("\n");
                LOG.log(Level.FINE, sb.toString());
            }

            change.addToken(token, lookahead, state);
            // Here add regular token length even for JoinToken instances
            // since this is used solely for comparing with matchOffset which
            // also uses the per-input-chars coordinates. Real token's offset is independent value
            // assigned by the underlying TokenListChange and LexerInputOperation.
            relexOffset = lexerInputOperation.lastTokenEndOffset();
            // Marks all original tokens that would cover the area of just lexed token as removed.
            // 'matchIndex' will point right above the last token that was removed
            // 'matchOffset' will point to the end of the last removed token
            if (relexOffset > change.matchOffset) {
                do { // Mark all tokens below
                    if (change.matchIndex == tokenCount) { // index == tokenCount
                        if (tokenList.isFullyLexed()) {
                            change.matchOffset = Integer.MAX_VALUE; // Force lexing till end of input
                        } else { // Not fully lexed -> stop now
                            // Fake the conditions to break the relexing loop
                            change.matchOffset = relexOffset;
                            state = tokenList.state(change.matchIndex - 1);
                        }
                        break;
                    }
                    // Skip the token at matchIndex and also increase matchOffset
                    // The default (increasing matchOffset by token.length()) is overriden for join token list.
                    change.increaseMatchIndex();
                } while (relexOffset > change.matchOffset);
            }

            // Check whether the new token ends at matchOffset with the same state
            // like the original which typically means end of relexing
            if (relexOffset == change.matchOffset
                && LexerUtilsConstants.statesEqual(state, 
                    (change.matchIndex > 0) ? tokenList.state(change.matchIndex - 1) : null)
            ) {
                // Here it's a potential match and the relexing could end.
                // However there are additional SAME-LOOKAHEAD requirements
                // that are checked here and if not satisfied the relexing will continue.
                // SimpleLexerRandomTest.test() contains detailed description.
                
                // If there are no more original tokens to be removed then stop since
                // there are no tokens ahead that would possibly have to be relexed because of LA differences.
                if (change.matchIndex == tokenCount)
                    break;

                int matchPointOrigLookahead = (change.matchIndex > 0)
                        ? tokenList.lookahead(change.matchIndex - 1)
                        : 0;
                // If old and new LAs are the same it should be safe to stop relexing.
                // Also since all tokens are non-empty it's enough to just check
                // LA > 1 (because LA <= 1 cannot span more than one token).
                // The same applies for current LA.
                if (lookahead == matchPointOrigLookahead ||
                    matchPointOrigLookahead <= 1 && lookahead <= 1
                ) {
                    break;
                }
                
                int afterMatchPointTokenLength = tokenList.tokenOrEmbeddingUnsync(change.matchIndex).token().length();
                if (matchPointOrigLookahead <= afterMatchPointTokenLength &&
                    lookahead <= afterMatchPointTokenLength
                ) {
                    // Here both the original and relexed before-match-point token
                    // have their LAs ending within bounds of the after-match-point token so it's OK
                    break;
                }

                // It's true that nothing can be generally predicted about LA if the token after match point
                // would be relexed (compared to the original's token LA). However the following criteria
                // should possibly suffice.
                int afterMatchPointOrigTokenLookahead = tokenList.lookahead(change.matchIndex);
                if (lookahead - afterMatchPointTokenLength <= afterMatchPointOrigTokenLookahead &&
                    (matchPointOrigLookahead <= afterMatchPointTokenLength ||
                        lookahead >= matchPointOrigLookahead)
                ) {
                    // The orig LA of after-match-point token cannot be lower than the currently lexed  LA's projection into it.
                    // Also check that the orig lookahead ended in the after-match-point token
                    // or otherwise require the relexed before-match-point token to have >= lookahead of the original
                    // before-match-point token).
                    break;
                }

                // The token at matchIndex must be relexed
                if (loggable) {
                    LOG.log(Level.FINE, "    EXTRA-RELEX: mInd=" + change.matchIndex + ", LA=" + lookahead + "\n");
                }
                // Skip the token at matchIndex
                change.increaseMatchIndex();
                // Continue by fetching next token
            }
        }
        lexerInputOperation.release();

        // If at least two tokens were lexed it's possible that e.g. the last added token
        // will be the same like the last removed token and in such case
        // the addition of the last token should be 'undone'.
        // This all may happen due to the fact that for larger lookaheads
        // the algorithm must relex the token(s) within lookahead (see the code above).
        int lastAddedTokenIndex = change.addedTokenOrEmbeddingsCount() - 1;
        // There should remain at least one added token since that one
        // may not be the same like the original removed one because
        // token lengths would differ because of the input source modification.
        
        if (change.matchOffset != Integer.MAX_VALUE) { // would not make sense when lexing past end of existing tokens
            while (lastAddedTokenIndex >= 1 && // At least one token added
                    change.matchIndex > lowestMatchIndex // At least one token removed
            ) {
                AbstractToken<T> lastAddedToken = change.addedTokenOrEmbeddings().get(lastAddedTokenIndex).token();
                AbstractToken<T> lastRemovedToken = tokenList.tokenOrEmbeddingUnsync(change.matchIndex - 1).token();
                if (lastAddedToken.id() != lastRemovedToken.id()
                    || lastAddedToken.length() != lastRemovedToken.length()
                    || change.laState().lookahead(lastAddedTokenIndex) != tokenList.lookahead(change.matchIndex - 1)
                    || !LexerUtilsConstants.statesEqual(change.laState().state(lastAddedTokenIndex),
                        tokenList.state(change.matchIndex - 1))
                ) {
                    break;
                }
                // Last removed and added tokens are the same so undo the addition
                if (loggable) {
                    LOG.log(Level.FINE, "    RETAIN-ORIGINAL at (mInd-1)=" + (change.matchIndex-1) +
                            ", id=" + lastRemovedToken.id() + "\n");
                }
                lastAddedTokenIndex--;
                // Includes decreasing of matchIndex and matchOffset
                change.removeLastAddedToken();
                relexOffset = change.addedEndOffset;
            }
        } else { // matchOffset == Integer.MAX_VALUE
            // Fix matchOffset to point to end of last token since it's used
            //   as last-added-token-end-offset in event notifications
            change.setMatchOffset(tokenList.endOffset());
        }
    }

    private static <T extends TokenId> void logModification(MutableTokenList<T> tokenList,
            TokenHierarchyEventInfo eventInfo, boolean updateJoined
    ) {
        int modOffset = eventInfo.modOffset();
        int removedLength = eventInfo.removedLength();
        int insertedLength = eventInfo.insertedLength();
        CharSequence inputSourceText = tokenList.inputSourceText();
        String insertedText = "";
        if (insertedLength > 0) {
            insertedText = ", insTxt:\"" + CharSequenceUtilities.debugText(
                    inputSourceText.subSequence(modOffset, modOffset + insertedLength)) + '"';
        }
        // Debug 10 chars around modOffset
        int afterInsertOffset = modOffset + insertedLength;
        CharSequence beforeText = inputSourceText.subSequence(Math.max(afterInsertOffset - 5, 0), afterInsertOffset);
        CharSequence afterText = inputSourceText.subSequence(afterInsertOffset,
                Math.min(afterInsertOffset + 5, inputSourceText.length()));
        StringBuilder sb = new StringBuilder(200);
        sb.append("TLU.update");
        sb.append(updateJoined ? "Joined" : "Regular");
        sb.append("() ").append(tokenList.languagePath().mimePath()).append('\n');
        sb.append("  modOff=").append(modOffset);
        sb.append(", text-around:\"").append(beforeText).append('|');
        sb.append(afterText).append("\", insLen=");
        sb.append(insertedLength).append(insertedText);
        sb.append(", remLen=").append(removedLength);
        sb.append(", tCnt=").append(tokenList.tokenCountCurrent()).append('\n');
        LOG.log(Level.FINE, sb.toString());
    }

}
