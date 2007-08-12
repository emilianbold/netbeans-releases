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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.inc;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.LanguageOperation;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.spi.lexer.TokenValidator;


/**
 * Token updater fixes a list of tokens constructed for a document
 * after text of the document gets modified.
 * <br>
 * Subclasses need to define all the abstract methods
 * so that the updating method can work on real token sequences.
 *
 * <p>
 * Updater looks similar to list iterator
 * but there are differences in the semantics
 * of iterator's modification operations.
 * <p>
 * The algorithm used in the {@link #update(int, int)}
 * is based on "General Incremental Lexical Analysis" written
 * by Tim A. Wagner and Susan L. Graham, University
 * of California, Berkeley. It's available online
 * at <a href="http://www.cs.berkeley.edu/Research/Projects/harmonia/papers/twagner-lexing.pdf">
 * twagner-lexing.pdf</a>.
 * <br>
 * Ending <code>EOF</code> token is not used but the lookahead
 * of the ending token(s) is increased by one (past the end of the input)
 * if they have reached the EOF.
 * <br>
 * Non-startable tokens are not supported.
 * <br>
 * When updating a token with lookback one as a result
 * of modification the lookahead of the preceding token is inspected
 * to find out whether the modification has really affected it.
 * This can often save the previous token from being relexed.
 * <br>
 * Currently the algorithm computes the lookback values on the fly
 * and it does not store the lookback in the tokens. For typical languages
 * the lookback is reasonably small (0, 1 or 2) so it's usually not worth
 * to consume extra space in token instances for storing of the lookback.
 * There would also be an additional overhead of updating the lookback
 * values in the tokens after the modification and the algorithm code would
 * be somewhat less readable.
 *
 * <p>
 * The algorithm removes the affected tokens in the natural order as they
 * follow in the token stream. That can be used when the removed tokens
 * need to be collected (e.g. in an array).
 * <br>
 * If the offset and state after token recognition matches
 * the end offset and state after recognition of the originally present
 * token then the relexing is stopped because a match was found and the newly
 * produced tokens would match the present ones.
 * <br>
 * Otherwise the token(s) in the list are removed and replaced
 * by the relexed token and the relexing continues until a match is reached.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenListUpdater {

    // -J-Dorg.netbeans.lib.lexer.inc.TokenListUpdater.level=FINE
    private static final Logger LOG = Logger.getLogger(TokenListUpdater.class.getName());

    /**
     * Use incremental algorithm to update the list of tokens
     * after a modification done in the underlying storage.
     * 
     * @param tokenList non-null token list that is being updated. It may be top-level list
     *  or embedded token list.
     * @param eventInfo non-null event info containing information like modification offset
     *  and removed/inserted length etc.
     * @param change non-null change that will incorporate the performed chagnes.
     */
    public static <T extends TokenId> void update(MutableTokenList<T> tokenList,
    TokenHierarchyEventInfo eventInfo, TokenListChange<T> change) {
        // Ensure the offsets in token list are up-to-date
        if (tokenList.getClass() == EmbeddedTokenList.class) {
            ((EmbeddedTokenList<? extends TokenId>)tokenList).updateStatus();
        }

        // Fetch offset where the modification occurred
        int modOffset = eventInfo.modificationOffset();
        LanguageOperation<T> languageOperation = LexerUtilsConstants.mostEmbeddedLanguageOperation(
                tokenList.languagePath());

        int tokenCount = tokenList.tokenCountCurrent(); // presently created token count
        // Now determine which token is the first to be relexed.
        // If it would be either modified token or previous-of-modified token
        // (for modification right at the begining of modified token)
        // then the token will be attempted to be validated (without running
        // a lexer).
        AbstractToken<T> modToken;
        // modTokenOffset holds begining of the token in which the modification occurred.
        int modTokenOffset;
        // index points to the modified token
        int index;

        boolean loggable = LOG.isLoggable(Level.FINE);
        if (loggable) {
            LOG.log(Level.FINE, "TokenListUpdater.update() STARTED\nmodOffset=" + modOffset
                    + ", insertedLength=" + eventInfo.insertedLength()
                    + ", removedLength=" + eventInfo.removedLength()
                    + ", tokenCount=" + tokenCount + "\n");
        }

        if (tokenCount == 0) { // no tokens yet or all removed
            if (!tokenList.isFullyLexed()) {
                // No tokens created yet (they get created lazily).
                if (loggable) {
                    LOG.log(Level.FINE, "TokenListUpdater.update() FINISHED: Not fully lexed yet.\n");
                }
                return; // Do nothing in this case
            }
            // If fully lexed and no tokens then the tokens should start
            // right at the modification offset
            modToken = null;
            modTokenOffset = modOffset;
            index = 0;

        } else { // at least one token exists
            // Check whether the modification at modOffset might affect existing tokens
            // Get index of the token in which the modification occurred
            // Get the offset of the last token into modTokenOffset variable
            index = tokenCount - 1;
            modTokenOffset = tokenList.tokenOffset(index);
            if (modOffset >= modTokenOffset) { // inside or above the last token?
                modToken = token(tokenList, index);
                int modTokenEndOffset = modTokenOffset + modToken.length();
                if (modOffset >= modTokenEndOffset) { // above last token
                    // Modification was right at the end boundary of the last token
                    // or above it (token list can be created lazily so that is valid case).
                    // Check whether the last token could be affected at all
                    // by checking the last token's lookahead.
                    // For fully lexed inputs the characters added to the end
                    // must be properly lexed and notified (even if the last present
                    // token has zero lookahead).
                    if (!tokenList.isFullyLexed()
                        && modOffset >= modTokenEndOffset + tokenList.lookahead(index)
                    ) {
                        if (loggable) {
                            LOG.log(Level.FINE, "TokenListUpdater.update() FINISHED: Not fully lexed yet. modTokenOffset="
                                    + modTokenOffset + ", modToken.length()=" + modToken.length() + "\n");
                        }
                        return; // not affected at all
                    }

                    index++;
                    modToken = null;
                    modTokenOffset = modTokenEndOffset;
                } // else -> modification inside the last token

            } else { // modification in non-last token
                // Find modified token by binary search
                int low = 0; // use index as 'high'
                while (low <= index) {
                    int mid = (low + index) / 2;
                    int midStartOffset = tokenList.tokenOffset(mid);

                    if (midStartOffset < modOffset) {
                        low = mid + 1;
                    } else if (midStartOffset > modOffset) {
                        index = mid - 1;
                    } else {
                        // Token starting exactly at modOffset found
                        index = mid;
                        modTokenOffset = midStartOffset;
                        break;
                    }
                }
                if (index < low) { // no token starting right at 'modOffset'
                    modTokenOffset = tokenList.tokenOffset(index);
                }
                modToken = token(tokenList, index);
                if (loggable) {
                    LOG.log(Level.FINE, "BIN-SEARCH: index=" + index
                            + ", modTokenOffset=" + modTokenOffset
                            + ", modToken.id()=" + modToken.id() + "\n");
                }
            }
        }

        // Store the index that points to the modified token
        // i.e. modification at its begining or inside.
        // Index variable can later be modified but present value is important
        // for moving of the offset gap later.
        change.setOffsetGapIndex(index);

        // Index and offset from which the relexing will start.
        int relexIndex;
        int relexOffset;
        // Whether the token validation should be attempted or not.
        boolean attemptValidation = false;

        if (index == 0) { // modToken is first in the list
            relexIndex = index;
            relexOffset = modTokenOffset;
            // Can validate modToken if removal does not span whole token
            if (modToken != null && eventInfo.removedLength() < modToken.length()) {
                attemptValidation = true;
            }

        } else { // Previous token exists
            // Check for insert-only right at the end of the previous token
            if (modOffset == modTokenOffset && eventInfo.removedLength() == 0) {
                index--; // move to previous token
                modToken = token(tokenList, index);
                modTokenOffset -= modToken.length();
            }

            // Check whether modification affected previous token
            if (index == 0 || modTokenOffset + tokenList.lookahead(index - 1) <= modOffset) {
                // Modification did not affect previous token
                relexIndex = index;
                relexOffset = modTokenOffset;
                // Check whether modification was localized to modToken only
                if (modOffset + eventInfo.removedLength() < modTokenOffset + modToken.length()) {
                    attemptValidation = true;
                }

            } else { // at least previous token affected
                relexOffset = modTokenOffset - token(tokenList, index - 1).length();
                relexIndex = index - 2; // Start with token below previous token
                
                // Go back and mark all affected tokens for removals
                while (relexIndex >= 0) {
                    AbstractToken<T> token = token(tokenList, relexIndex);
                    // Check if token was not affected by modification
                    if (relexOffset + tokenList.lookahead(relexIndex) <= modOffset) {
                        break;
                    }
                    relexIndex--;
                    relexOffset -= token.length();
                }
                relexIndex++; // Next token will be relexed
            }
        }
        
        // The lowest offset at which the relexing can end
        // (the relexing may end at higher offset if the relexed
        // tokens will end at different boundaries than the original
        // tokens or if the states after the tokens' recognition
        // will differ from the original states in the original tokens.
        int matchOffset;

        // Perform token validation of modToken if possible.
        // The index variable will hold the token index right before the matching point.
        if (attemptValidation) {
            matchOffset = modTokenOffset + modToken.length();
            TokenValidator tokenValidator = languageOperation.tokenValidator(modToken.id());
            if (tokenValidator != null
                && (tokenList.getClass() != IncTokenList.class
                    || eventInfo.tokenHierarchyOperation().canModifyToken(index, modToken))
            ) {
                    
//                if (tokenValidator.validateToken(modToken, modOffset - modTokenOffset, modRelOffset,
//                        removedLength, insertedLength)
//                ) {
//                    // Update positions
//                                change.initRemovedAddedOffsets()

//                    return; // validated successfully
//                }
            }

        } else { // Validation cannot be attempted
            // Need to compute matchOffset and matchIndex
            // by iterating forward
            if (index < tokenCount) {
                matchOffset = modTokenOffset + modToken.length();
                int removeEndOffset = modOffset + eventInfo.removedLength();
                while (matchOffset < removeEndOffset && index + 1 < tokenCount) {
                    index++;
                    matchOffset += token(tokenList, index).length();
                }

            } else // After last token
                matchOffset = modTokenOffset;
        }

        // State from which the lexer can be started
        Object relexState = (relexIndex > 0) ? tokenList.state(relexIndex - 1) : null;
        // Update the matchOffset so that it corresponds to the state
        // after the modification
        matchOffset += eventInfo.insertedLength() - eventInfo.removedLength();
        change.setOffset(relexOffset);
        
        // Variables' values:
        // 'index' - points to modified token. Or index == tokenCount for modification
        //     past the last token.
        // 'tokenCount' - token count in the original token list.
        // 'relexIndex' - points to the token that will be relexed as first.
        // 'relexOffset' - points to begining of the token that will be relexed as first.
        // 'matchOffset' - points to end of token after which the fixed token list could
        //     possibly match the original token list. Points to end of token at 'index'
        //     variable if 'index < tokenCount' and to the end of the last token
        //     if 'index == tokenCount'.

        // Check whether relexing is necessary.
        // Necessary condition for no-relexing is that the matchToken
        // has zero lookahead (if lookahead would be >0 
        // then the matchToken would be affected and relexOffset != matchOffset).
        // The states before relex token must match the state after the modified token
        // In case of removal starting and ending at token boundaries
        // the relexing might not be necessary.
        boolean relex = (relexOffset != matchOffset)
                || index >= tokenCount
                || !LexerUtilsConstants.statesEqual(relexState, tokenList.state(index));

        // There is an extra condition that the lookahead of the matchToken
        // must not span the next (retained) token. This condition helps to ensure
        // that the lookaheads will be the same like during regular batch lexing.
        // As the empty tokens are not allowed the situation may only occur
        // for lookahead > 1.
        int lookahead;
        if (!relex && (lookahead = tokenList.lookahead(index)) > 1 && index + 1 < tokenCount) {
            relex = (lookahead > token(tokenList, index + 1).length()); // check next token
        }

        if (loggable) {
            LOG.log(Level.FINE, "BEFORE-RELEX: index=" + index + ", modTokenOffset=" + modTokenOffset
                    + ", relexIndex=" + relexIndex + ", relexOffset=" + relexOffset
                    + ", relexState=" + relexState
                    + ", matchOffset=" + matchOffset
                    + ", perform relex: " + relex + "\n");
        }
        
        if (relex) { // Start relexing
            LexerInputOperation<T> lexerInputOperation
                    = tokenList.createLexerInputOperation(relexIndex, relexOffset, relexState);

            do { // Fetch new tokens from lexer as necessary
                AbstractToken<T> token = lexerInputOperation.nextToken();
                if (token == null) {
                    attemptValidation = false;
                    break;
                }

                lookahead = lexerInputOperation.lookahead();
                Object state = lexerInputOperation.lexerState();
                if (loggable) {
                    LOG.log(Level.FINE, "LEXED-TOKEN: id=" + token.id()
                            + ", length=" + token.length()
                            + ", lookahead=" + lookahead
                            + ", state=" + state + "\n");
                }
                
                change.addToken(token, lookahead, state);

                relexOffset += token.length();
                // Remove obsolete tokens that would cover the area of just lexed token
                // 'index' will point to the last token that was removed
                // 'matchOffset' will point to the end of the last removed token
                if (relexOffset > matchOffset && index < tokenCount) {
                    attemptValidation = false;
                    do {
                        index++;
                        if (index == tokenCount) {
                            // Make sure the number of removed tokens will be computed properly later
                            modToken = null;
                            // Check whether it should lex till the end
                            // or whether 'Match at anything' should be done
                            if (tokenList.isFullyLexed()) {
                                // Will lex till the end of input
                                matchOffset = Integer.MAX_VALUE;
                            } else {
                                // Force stop lexing
                                relex = false;
                            }
                            break;
                        }
                        matchOffset += token(tokenList, index).length();
                    } while (relexOffset > matchOffset);
                }

                // Check whether the new token ends at matchOffset with the same state
                // like the original which typically means end of relexing
                if (relexOffset == matchOffset
                    && (index < tokenCount)
                    && LexerUtilsConstants.statesEqual(state, tokenList.state(index))
                ) {
                    // Here it's a potential match and the relexing could end.
                    // However there are additional conditions that need to be checked.
                    // 1. Check whether lookahead of the last relexed token
                    //  does not exceed length plus LA of the subsequent (original) token.
                    //  See initial part of SimpleRandomTest.test() verifies this.
                    // 2. Algorithm attempts to have the same lookaheads in tokens
                    //  like the regular batch scanning would produce.
                    //  Although not strictly necessary requirement
                    //  it helps to simplify the debugging in case the lexer does not work
                    //  well in the incremental setup.
                    //  The following code checks that the lookahead of the original match token
                    //  (i.e. the token right before matchOffset) does "end" inside
                    //  the next token - if not then relexing the next token is done.
                    //  The second part of SimpleRandomTest.test() verifies this.

                    // 'index' points to the last token that was removed
                    int matchTokenLookahead = tokenList.lookahead(index);
                    // Optimistically suppose that the relexing will end
                    relex = false;
                    // When assuming non-empty tokens the lookahead 1
                    // just reaches the end of the next token
                    // so lookhead < 1 is always fine from this point of view.
                    if (matchTokenLookahead > 1 || lookahead > 1) {
                        // Start with token right after the last removed token starting at matchOffset
                        int i = index + 1;
                        // Process additional removals by increasing 'index'
                        // 'lookahead' holds
                        while (i < tokenCount) {
                            int tokenLength = token(tokenList, i).length();
                            lookahead -= tokenLength; // decrease extra lookahead
                            matchTokenLookahead -= tokenLength;
                            if (lookahead <= 0 && matchTokenLookahead <=0) {
                                break; // No more work
                            }
                            if (lookahead != tokenList.lookahead(i)
                                    || matchTokenLookahead > 0
                            ) {
                                // This token must be relexed
                                if (loggable) {
                                    LOG.log(Level.FINE, "EXTRA-RELEX: index=" + index + ", lookahead=" + lookahead
                                            + ", tokenLength=" + tokenLength + "\n");
                                }
                                index = i;
                                matchOffset += tokenLength;
                                relex = true;
                                // Continue - further tokens may be affected
                            }
                            i++;
                        }
                    }

                    if (!relex) {
                        if (attemptValidation) {
//                            if (modToken.id() == token.id()
//                                    && tokenList.lookahead(index) == lookahead
//                                    && !modToken.isFlyweight()
//                                    && !token.isFlyweight()
//                                    && (tokenList.getClass() != IncTokenList.class
//                                        || change.tokenHierarchyOperation().canModifyToken(index, modToken))
//                                    && LexerSpiTokenPackageAccessor.get().restoreToken(
//                                            languageOperation.tokenHandler(),
//                                            modToken, token)
//                            ) {
//                                // Restored successfully
//                                // TODO implement - fix token's length and return
//                                // now default in fact to failed validation
//                            }
                            attemptValidation = false;
                        }
                    }
                }
            } while (relex); // End of the relexing loop
            lexerInputOperation.release();

            // If at least two tokens were lexed it's possible that e.g. the last added token
            // will be the same like the last removed token and in such case
            // the addition of the last token should be 'undone'.
            // This all may happen due to the fact that for larger lookaheads
            // the algorithm must relex the token(s) within lookahead (see the code above).
            int lastAddedTokenIndex = change.addedTokensOrBranchesCount() - 1;
            // There should remain at least one added token since that one
            // may not be the same like the original removed one because
            // token lengths would differ because of the input source modification.
            while (lastAddedTokenIndex >= 1 && index > relexIndex && index < tokenCount) {
                AbstractToken<T> addedToken = LexerUtilsConstants.token(
                        change.addedTokensOrBranches().get(lastAddedTokenIndex));
                AbstractToken<T> removedToken = token(tokenList, index);
                if (addedToken.id() != removedToken.id()
                    || addedToken.length() != removedToken.length()
                    || change.laState().lookahead(lastAddedTokenIndex) != tokenList.lookahead(index)
                    || !LexerUtilsConstants.statesEqual(change.laState().state(lastAddedTokenIndex),
                        tokenList.state(index))
                ) {
                    break;
                }
                // Last removed and added tokens are the same so undo the addition
                if (loggable) {
                    LOG.log(Level.FINE, "RETAIN-ORIGINAL: index=" + index + ", id=" + removedToken.id() + "\n");
                }
                lastAddedTokenIndex--;
                index--;
                relexOffset -= addedToken.length();
                change.removeLastAddedToken();
            }
        }

        // Now ensure that the original tokens will be replaced by the relexed ones.
        int removedTokenCount = (modToken != null) ? (index - relexIndex + 1) : (index - relexIndex);
        if (loggable) {
            LOG.log(Level.FINE, "TokenListUpdater.update() FINISHED: Removed:"
                    + removedTokenCount + ", Added:" + change.addedTokensOrBranchesCount() + " tokens.\n");
        }
        change.setIndex(relexIndex);
        change.setAddedEndOffset(relexOffset);
        tokenList.replaceTokens(eventInfo, change, removedTokenCount);
    }
    
    private static <T extends TokenId> AbstractToken<T> token(MutableTokenList<T> tokenList, int index) {
        Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainerUnsync(index); // Unsync impl suffices
        return LexerUtilsConstants.token(tokenOrEmbeddingContainer);
    }

}
