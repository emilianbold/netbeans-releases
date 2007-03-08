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

package org.netbeans.modules.cnd.editor.cplusplus;


import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Utilities;




/**
 * This static class groups the whole aspect of bracket
 * completion. It is defined to clearly separate the functionality
 * and keep actions clean. 
 * The methods of the class are called from different actions as
 * KeyTyped, DeletePreviousChar.
 */
public class BracketCompletion {

  /**
   * A hook method called after a character was inserted into the
   * document. The function checks for special characters for
   * completion ()[]'"{} and other conditions and optionally performs
   * changes to the doc and or caret (complets braces, moves caret,
   * etc.)
   * @param doc the document where the change occurred
   * @param dotPos position of the character insertion
   * @param caret caret
   * @param ch the character that was inserted
   * @throws BadLocationException if dotPos is not correct
   */
    static void charInserted(BaseDocument doc,  
			   int dotPos, 
			   Caret caret,
			   char ch) throws BadLocationException 
    {
        SyntaxSupport syntaxSupport = doc.getSyntaxSupport();
        if (!(syntaxSupport instanceof ExtSyntaxSupport) || !completionSettingEnabled()) {
            return;
        }
      
        ExtSyntaxSupport support = (ExtSyntaxSupport)syntaxSupport;
        if (ch == ')'|| ch == ']'|| ch =='('|| ch =='[') {
	    TokenID tokenAtDot = support.getTokenID(dotPos);
	    if (tokenAtDot == CCTokenContext.RBRACKET || tokenAtDot == CCTokenContext.RPAREN) {
	        skipClosingBracket(doc, caret, ch);
	    } else if (tokenAtDot == CCTokenContext.LBRACKET || tokenAtDot == CCTokenContext.LPAREN) {
	        completeOpeningBracket(doc, dotPos, caret, ch);
	    }
        } else if (ch == '\"' || ch == '\'') {
	    completeQuote(doc, dotPos, caret, ch);
        } else if (ch == ';') {
              moveSemicolon(doc, dotPos, caret);
        }
    }

    private static void moveSemicolon(BaseDocument doc, int dotPos, Caret caret) throws BadLocationException {
        int eolPos = Utilities.getRowEnd(doc, dotPos);
        ExtSyntaxSupport ssup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        int lastParenPos = dotPos;
        TokenItem token = ssup.getTokenChain(dotPos, eolPos);
        for (TokenItem item = token.getNext(); item != null && item.getOffset() <= eolPos; item = item.getNext()) {
            TokenID tokenID = item.getTokenID();
            if (tokenID == CCTokenContext.RPAREN) {
                lastParenPos = item.getOffset();
            } else if (tokenID != CCTokenContext.WHITESPACE) {
                return;
            }
        }
        if (isForLoopSemicolon(token) || posWithinAnyQuote(doc,dotPos)) {
            return;
        }
        doc.remove(dotPos, 1);
        doc.insertString(lastParenPos, ";", null); // NOI18N
        caret.setDot(lastParenPos + 1);
    }

    private static boolean isForLoopSemicolon(TokenItem token) {
        if (token == null || token.getTokenID() != CCTokenContext.SEMICOLON) {
            return false;
        }
        int parDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        token = token.getPrevious(); // ignore this semicolon
        while (token != null) {
            if (token.getTokenID() == CCTokenContext.LPAREN) {
                if (parDepth == 0) { // could be a 'for ('
                    token = token.getPrevious();
                    while(token !=null &&
                            (token.getTokenID() == CCTokenContext.WHITESPACE ||
                             token.getTokenID() == CCTokenContext.BLOCK_COMMENT ||
                             token.getTokenID() == CCTokenContext.LINE_COMMENT)) {
                        token = token.getPrevious();
                    }
                    if (token.getTokenID() == CCTokenContext.FOR) {
                        return true;
                    }
                    return false;
                } else { // non-zero depth
                    parDepth--;
                }
            } else if (token.getTokenID() == CCTokenContext.RPAREN) {
                parDepth++;
            } else if (token.getTokenID() == CCTokenContext.LBRACE) {
                if (braceDepth == 0) { // unclosed left brace
                    return false;
                }
                braceDepth--;
            } else if (token.getTokenID() == CCTokenContext.RBRACE) {
                braceDepth++;
            } else if (token.getTokenID() == CCTokenContext.SEMICOLON) {
                if (semicolonFound) { // one semicolon already found
                    return false;
                }
                semicolonFound = true;
            }
            token = token.getPrevious();
        }
        return false;
    }

    /**
   * Hook called after a character *ch* was backspace-deleted from
   * *doc*. The function possibly removes bracket or quote pair if
   * appropriate.
   * @param doc the document
   * @param dotPos position of the change
   * @param caret caret
   * @param ch the character that was deleted
   */
  static void charBackspaced(BaseDocument doc,
			     int dotPos,
			     Caret caret,
			     char ch) throws BadLocationException 
  {
    if (completionSettingEnabled()) {
      if (ch == '(' || ch == '[') {
	TokenID tokenAtDot = ((ExtSyntaxSupport)doc.getSyntaxSupport()).getTokenID(dotPos);
	if ((tokenAtDot == CCTokenContext.RBRACKET && tokenBalance(doc, CCTokenContext.LBRACKET, CCTokenContext.RBRACKET) != 0) ||
	    (tokenAtDot == CCTokenContext.RPAREN && tokenBalance(doc, CCTokenContext.LPAREN, CCTokenContext.RPAREN) != 0) ) {
	  doc.remove(dotPos, 1);
	}
      } 
      else if (ch == '\"') {
	char match [] = doc.getChars(dotPos, 1);
	if (match != null && match[0] == '\"') {
	  doc.remove(dotPos, 1);
	}
      }
      else if (ch == '\'') {
	char match [] = doc.getChars(dotPos, 1);
	if (match != null && match[0] == '\'') {
	  doc.remove(dotPos, 1);
	}
      }
    }
  }

    /**
     * Resolve whether pairing right curly should be added automatically
     * at the caret position or not.
     * <br>
     * There must be only whitespace or line comment or block comment
     * between the caret position
     * and the left brace and the left brace must be on the same line
     * where the caret is located.
     * <br>
     * The caret must not be "contained" in the opened block comment token.
     *
     * @param doc document in which to operate.
     * @param caretOffset offset of the caret.
     * @return true if a right brace '}' should be added
     *  or false if not.
     */
    static boolean isAddRightBrace(BaseDocument doc, int caretOffset) throws BadLocationException {
        boolean addRightBrace = false;
        if (completionSettingEnabled()) {
            if (caretOffset > 0) {
                // Check whether line ends with '{' ignoring any whitespace
                // or comments
                int tokenOffset = caretOffset;
                TokenItem token = ((ExtSyntaxSupport)doc.getSyntaxSupport()).
                getTokenChain(tokenOffset - 1, tokenOffset);
                
                addRightBrace = true; // suppose that right brace should be added
                
                // Disable right brace adding if caret not positioned within whitespace
                // or line comment
                int off = (caretOffset - token.getOffset());
                if (off > 0 && off < token.getImage().length()) { // caret contained in token
                    switch (token.getTokenID().getNumericID()) {
                        case CCTokenContext.WHITESPACE_ID:
                        case CCTokenContext.LINE_COMMENT_ID:
                            break; // the above tokens are OK
                            
                        default:
                            // Disable brace adding for the remaining ones
                            addRightBrace = false;
                    }
                }
                
                if (addRightBrace) { // still candidate for adding
                    int caretRowStartOffset = Utilities.getRowStart(doc, caretOffset);
                    
                    // Check whether there are only whitespace or comment tokens
                    // between caret and left brace and check only on the line
                    // with the caret
                    while (token != null && token.getOffset() >= caretRowStartOffset) {
                        boolean ignore = false;
                        // Assuming java token context here
                        switch (token.getTokenID().getNumericID()) {
                            case CCTokenContext.WHITESPACE_ID:
                            case CCTokenContext.BLOCK_COMMENT_ID:
                            case CCTokenContext.LINE_COMMENT_ID:
                                // skip
                                ignore = true;
                                break;
                        }
                        
                        if (ignore) {
                            token = token.getPrevious();
                        } else { // break on the current token
                            break;
                        }
                    }
                    
                    if (token == null
                    || token.getTokenID() != CCTokenContext.LBRACE // must be left brace
                    || token.getOffset() < caretRowStartOffset // on the same line as caret
                    ) {
                        addRightBrace = false;
                    }
                    
                }
                
                if (addRightBrace) { // Finally check the brace balance whether there are any missing right braces
                    addRightBrace = (braceBalance(doc) > 0);
                }
            }
        }
        return addRightBrace;
    }
    
    /**
     * Returns position of the first unpaired closing paren/brace/bracket from the caretOffset
     * till the end of caret row. If there is no such element, position after the last non-white
     * character on the caret row is returned.
     */
    static int getRowOrBlockEnd(BaseDocument doc, int caretOffset) throws BadLocationException {
        int rowEnd = Utilities.getRowLastNonWhite(doc, caretOffset);
        if (rowEnd == -1 || caretOffset >= rowEnd) {
            return caretOffset;
        }
        rowEnd += 1;
        int parenBalance = 0;
        int braceBalance = 0;
        int bracketBalance = 0;
        ExtSyntaxSupport ssup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token = ssup.getTokenChain(caretOffset, rowEnd);
        while (token != null && token.getOffset() < rowEnd) {
            switch (token.getTokenID().getNumericID()) {
                case CCTokenContext.LPAREN_ID:
                    parenBalance++;
                    break;
                case CCTokenContext.RPAREN_ID:
                    if (parenBalance-- == 0)
                        return token.getOffset();
                case CCTokenContext.LBRACE_ID:
                    braceBalance++;
                    break;
                case CCTokenContext.RBRACE_ID:
                    if (braceBalance-- == 0)
                        return token.getOffset();
                case CCTokenContext.LBRACKET_ID:
                    bracketBalance++;
                    break;
                case CCTokenContext.RBRACKET_ID:
                    if (bracketBalance-- == 0)
                        return token.getOffset();
            }
            token = token.getNext();
        }
        return rowEnd;
    } 
    
  /** 
   * Counts the number of braces starting at dotPos to the end of the
   * document. Every occurence of { increses the count by 1, every
   * occurrence of } decreses the count by 1. The result is returned.
   * @return The number of { - number of } (>0 more { than } ,<0 more } than {)
   */
  private static int braceBalance(BaseDocument doc) 
    throws BadLocationException
  {
    return tokenBalance(doc, CCTokenContext.LBRACE, CCTokenContext.RBRACE);
  }

  /** 
   * The same as braceBalance but generalized to any pair of matching
   * tokens. 
   * @param open the token that increses the count
   * @param close the token that decreses the count
   */
  private static int tokenBalance(BaseDocument doc, TokenID open, TokenID close) 
              throws BadLocationException {
      
      ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
      BalanceTokenProcessor balanceTP = new BalanceTokenProcessor(open, close);
      sup.tokenizeText(balanceTP, 0, doc.getLength(), true);
      return balanceTP.getBalance();
  }

  /**
   * A hook to be called after closing bracket ) or ] was inserted into
   * the document. The method checks if the bracket should stay there
   * or be removed and some exisitng bracket just skipped.
   *
   * @param doc the document
   * @param dotPos position of the inserted bracket
   * @param caret caret
   * @param theBracket the bracket character ']' or ')'
   */
  private static void skipClosingBracket(BaseDocument doc, Caret caret, char theBracket)
  throws BadLocationException {

      TokenID bracketId = (theBracket == ')')
          ? CCTokenContext.RPAREN 
          : CCTokenContext.RBRACKET;

      int caretOffset = caret.getDot();
      if (isSkipClosingBracket(doc, caretOffset, bracketId)) {
          doc.remove(caretOffset - 1, 1);
          caret.setDot(caretOffset); // skip closing bracket
      }
  }
  
  /**
   * Check whether the typed bracket should stay in the document
   * or be removed.
   * <br>
   * This method is called by <code>skipClosingBracket()</code>.
   *
   * @param doc document into which typing was done.
   * @param caretOffset
   */
  static boolean isSkipClosingBracket(BaseDocument doc, int caretOffset, TokenID bracketId)
              throws BadLocationException {

      // First check whether the caret is not after the last char in the document
      // because no bracket would follow then so it could not be skipped.
      if (caretOffset == doc.getLength()) {
          return false; // no skip in this case
      }
      
      boolean skipClosingBracket = false; // by default do not remove

      // Examine token at the caret offset
      TokenItem token = ((ExtSyntaxSupport)doc.getSyntaxSupport()).getTokenChain(
          caretOffset, caretOffset + 1);

      // Check whether character follows the bracket is the same bracket
      if (token != null && token.getTokenID() == bracketId) {
          int bracketIntId = bracketId.getNumericID();
          int leftBracketIntId = (bracketIntId == CCTokenContext.RPAREN_ID)
                      ? CCTokenContext.LPAREN_ID
                      : CCTokenContext.LBRACKET_ID;
          
        // Skip all the brackets of the same type that follow the last one
          TokenItem nextToken = token.getNext();
          while (nextToken != null && nextToken.getTokenID() == bracketId) {
              token = nextToken;
              nextToken = nextToken.getNext();
          }
          // token var points to the last bracket in a group of two or more right brackets
          // Attempt to find the left matching bracket for it
          // Search would stop on an extra opening left brace if found
          int braceBalance = 0; // balance of '{' and '}'
          int bracketBalance = -1; // balance of the brackets or parenthesis
          TokenItem lastRBracket = token;
          token = token.getPrevious();
          boolean finished = false;
          while (!finished && token != null) {
              int tokenIntId = token.getTokenID().getNumericID();
              switch (tokenIntId) {
                  case CCTokenContext.LPAREN_ID:
                  case CCTokenContext.LBRACKET_ID:
                      if (tokenIntId == bracketIntId) {
                          bracketBalance++;
                          if (bracketBalance == 0) {
                              if (braceBalance != 0) {
                                  // Here the bracket is matched but it is located
                                  // inside an unclosed brace block
                                  // e.g. ... ->( } a()|)
                                  // which is in fact illegal but it's a question
                                  // of what's best to do in this case.
                                  // We chose to leave the typed bracket
                                  // by setting bracketBalance to 1.
                                  // It can be revised in the future.
                                  bracketBalance = 1;
                              }
                              finished = true;
                          }
                      }
                      break;
                      
                  case CCTokenContext.RPAREN_ID:
                  case CCTokenContext.RBRACKET_ID:
                      if (tokenIntId == bracketIntId) {
                          bracketBalance--;
                      }
                      break;

                  case CCTokenContext.LBRACE_ID:
                      braceBalance++;
                      if (braceBalance > 0) { // stop on extra left brace
                          finished = true;
                      }
                      break;
                      
                  case CCTokenContext.RBRACE_ID:
                      braceBalance--;
                      break;
                      
              }
              
              token = token.getPrevious(); // done regardless of finished flag state
          }
          
          if (bracketBalance != 0) { // not found matching bracket
              // Remove the typed bracket as it's unmatched
              skipClosingBracket = true;
              
          } else { // the bracket is matched
              // Now check whether the bracket would be matched
              // when the closing bracket would be removed
              // i.e. starting from the original lastRBracket token
              // and search for the same bracket to the right in the text
              // The search would stop on an extra right brace if found
              braceBalance = 0;
              bracketBalance = 1; // simulate one extra left bracket
              token = lastRBracket.getNext();
              finished = false;
              while (!finished && token != null) {
                  int tokenIntId = token.getTokenID().getNumericID();
                  switch (tokenIntId) {
                      case CCTokenContext.LPAREN_ID:
                      case CCTokenContext.LBRACKET_ID:
                          if (tokenIntId == leftBracketIntId) {
                              bracketBalance++;
                          }
                          break;
                          
                      case CCTokenContext.RPAREN_ID:
                      case CCTokenContext.RBRACKET_ID:
                          if (tokenIntId == bracketIntId) {
                              bracketBalance--;
                              if (bracketBalance == 0) {
                                  if (braceBalance != 0) {
                                      // Here the bracket is matched but it is located
                                      // inside an unclosed brace block
                                      // which is in fact illegal but it's a question
                                      // of what's best to do in this case.
                                      // We chose to leave the typed bracket
                                      // by setting bracketBalance to -1.
                                      // It can be revised in the future.
                                      bracketBalance = -1;
                                  }
                                  finished = true;
                              }
                          }
                          break;
                          
                      case CCTokenContext.LBRACE_ID:
                          braceBalance++;
                          break;
                          
                      case CCTokenContext.RBRACE_ID:
                          braceBalance--;
                          if (braceBalance < 0) { // stop on extra right brace
                              finished = true;
                          }
                          break;
                          
                  }
                  
                  token = token.getPrevious(); // done regardless of finished flag state
              }
              
              // If bracketBalance == 0 the bracket would be matched
              // by the bracket that follows the last right bracket.
              skipClosingBracket = (bracketBalance == 0);
          }
      }
      return skipClosingBracket;
  }
  
  /**
   * Check for various conditions and possibly add a pairing bracket
   * to the already inserted.
   * @param doc the document
   * @param dotPos position of the opening bracket (already in the doc)
   * @param caret caret
   * @param theBracket the bracket that was inserted
   */
  private static void completeOpeningBracket(BaseDocument doc,
					      int dotPos,
					      Caret caret,
					      char theBracket) throws BadLocationException 
  {
    if (isCompletablePosition(doc, dotPos+1)) {
      String matchinBracket = "" + matching(theBracket);
      doc.insertString(dotPos + 1, matchinBracket,null);
      caret.setDot(dotPos+1);
    }
  }
  
  private static boolean isEscapeSequence(BaseDocument doc, int dotPos) throws BadLocationException{
      if (dotPos <= 0) return false;
      char previousChar = doc.getChars(dotPos-1,1)[0];
      return previousChar == '\\';
  }

  /** 
   * Check for conditions and possibly complete an already inserted
   * quote .
   * @param doc the document
   * @param dotPos position of the opening bracket (already in the doc)
   * @param caret caret
   * @param theBracket the character that was inserted
   */
  private static void completeQuote(BaseDocument doc, int dotPos, Caret caret,
  char theBracket) 
				    throws BadLocationException 
  {
    if (isEscapeSequence(doc, dotPos)){
        return;
    }
    TokenID tokenID = theBracket =='\"' ? CCTokenContext.STRING_LITERAL : CCTokenContext.CHAR_LITERAL;
    if ((posWithinQuotes(doc, dotPos+1, theBracket, tokenID) &&	isCompletablePosition(doc, dotPos+1)) && 
        (isUnclosedStringAtLineEnd(doc, dotPos, tokenID) && doc.getLength() != dotPos+1 && doc.getChars(dotPos+1, 1)[0] != theBracket)) {
      doc.insertString(dotPos + 1, "" + theBracket ,null);
      caret.setDot(dotPos+1);
    } else {
      char [] charss = doc.getChars(dotPos+1, 1);
      // System.out.println("NOT Within string, " + new String(charss));
      if (charss != null && charss[0] == theBracket) {
	doc.remove(dotPos+1, 1);
      }
    }
  }

  /** 
   * Checks whether dotPos is a position at which bracket and quote
   * completion is performed. Brackets and quotes are not completed
   * everywhere but just at suitable places .
   * @param doc the document
   * @param dotPos position to be tested
   */
  private static boolean isCompletablePosition(BaseDocument doc, int dotPos) 
    throws BadLocationException
  {
    if (dotPos == doc.getLength()) // there's no other character to test
      return true;
    else {
      // test that we are in front of ) , " or '
      char chr = doc.getChars(dotPos,1)[0];
      return (chr == ')' || 
	      chr == ',' || 
	      chr == '\"'|| 
	      chr == '\''|| 
	      chr == ' ' || 
	      chr == '-' || 
	      chr == '+' || 
	      chr == '|' || 
	      chr == '&' || 
	      chr == ']' || 
	      chr == '}' || 
	      chr == '\n'|| 
	      chr == '\t'|| 
	      chr == ';');
    }
  }


  /** 
   * Returns true if bracket completion is enabled in options.
   */
  private static boolean completionSettingEnabled() {
    return ((Boolean)Settings.getValue(CCKit.class, SettingsNames.PAIR_CHARACTERS_COMPLETION)).booleanValue();
  }

  /**
   * Returns for an opening bracket or quote the appropriate closing
   * character.
   */
  private static char matching(char theBracket) {
    switch (theBracket) {
    case '(' : return ')';
    case '[' : return ']';
    case '\"' : return '\"'; // NOI18N
    case '\'' : return '\'';
    default:  return ' ';
    }
  }



  /**
   * posWithinString(doc, pos) iff position *pos* is within a string
   * literal in document doc.
   * @param doc the document
   * @param dotPos position to be tested
   */
  static  boolean posWithinString(BaseDocument doc, int dotPos) {
    return posWithinQuotes(doc, dotPos, '\"', CCTokenContext.STRING_LITERAL);
  }

  /**
   * Generalized posWithingString to any token and delimiting
   * character. It works for tokens are delimited by *quote* and
   * extend up to the other *quote* or whitespace in case of an
   * incomplete token.
   * @param doc the document
   * @param dotPos position to be tested
   */
  static  boolean posWithinQuotes(BaseDocument doc, int dotPos, char quote, TokenID tokenID) 
  {
    try {
      MyTokenProcessor proc = new MyTokenProcessor();
      doc.getSyntaxSupport().tokenizeText( proc,
					   dotPos-1, 
					   doc.getLength(), true);
      return proc.tokenID == tokenID &&
      (dotPos - proc.tokenStart == 1 || doc.getChars(dotPos-1,1)[0]!=quote);
    } catch (BadLocationException ex) {
      return false;
    }
  }

  static boolean posWithinAnyQuote(BaseDocument doc, int dotPos) {
      try {
          MyTokenProcessor proc = new MyTokenProcessor();
          doc.getSyntaxSupport().tokenizeText( proc,
                  dotPos-1,
                  doc.getLength(), true);
          if(proc.tokenID == CCTokenContext.STRING_LITERAL ||
                  proc.tokenID == CCTokenContext.CHAR_LITERAL) {
              char[] ch = doc.getChars(dotPos-1,1);
              return dotPos - proc.tokenStart == 1 || (ch[0]!='\"' && ch[0]!='\'');
          }
          return false;
      } catch (BadLocationException ex) {
          return false;
      }
  }


  static boolean isUnclosedStringAtLineEnd(BaseDocument doc, int dotPos, TokenID tokenID) {
    try {
      MyTokenProcessor proc = new MyTokenProcessor();
        doc.getSyntaxSupport().tokenizeText(proc, Utilities.getRowLastNonWhite(doc, dotPos), doc.getLength(), true);
        return proc.tokenID == tokenID;
    } catch (BadLocationException ex) {
      return false;
    }
  }

  /**
   * A token processor used to find out the length of a token.
   */
  static class MyTokenProcessor implements TokenProcessor {
    public TokenID tokenID = null;
    public int tokenStart = -1;

    public boolean token(TokenID tokenID, TokenContextPath tcp,
			 int tokBuffOffset, int tokLength) {
      this.tokenStart = tokenBuffer2DocumentOffset(tokBuffOffset);
      this.tokenID = tokenID;

      // System.out.println("token " + tokenID.getName() + " at " + tokenStart + " (" + 
      //		 tokBuffOffset + ") len:" + tokLength);


      return false;
    }

    public int eot(int offset) { // System.out.println("EOT"); 
      return 0;}

    public void nextBuffer(char [] buffer, int offset, int len, int startPos, int preScan, boolean lastBuffer) {
      // System.out.println("nextBuffer "+ new String(buffer) + "," + offset + "len: " + len + " startPos:"+startPos + " preScan:" + preScan + " lastBuffer:" + lastBuffer);

      this.bufferStartPos = startPos - offset;
    }

    private int bufferStartPos = 0;
    private int tokenBuffer2DocumentOffset(int offs) { return offs + bufferStartPos;}
  }

    /**
     * Token processor for finding of balance of brackets and braces.
     */
    private static class BalanceTokenProcessor implements TokenProcessor {

        private TokenID leftTokenID;
        private TokenID rightTokenID;
        
        private int balance;
        
        BalanceTokenProcessor(TokenID leftTokenID, TokenID rightTokenID) {
            this.leftTokenID = leftTokenID;
            this.rightTokenID = rightTokenID;
        }
        
        public boolean token(TokenID tokenID, TokenContextPath tcp,
        int tokBuffOffset, int tokLength) {
            
            if (tokenID == leftTokenID) {
                balance++;
            } else if (tokenID == rightTokenID) {
                balance--;
            }

            return true;
        }
        
        public int eot(int offset) {
            return 0;
        }
        
        public void nextBuffer(char [] buffer, int offset, int len, int startPos, int preScan, boolean lastBuffer) {
        }
        
        public int getBalance() {
            return balance;
        }
        
    }
    

}
