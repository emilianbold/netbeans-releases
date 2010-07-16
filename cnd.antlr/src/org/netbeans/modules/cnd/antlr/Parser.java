package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;
import org.netbeans.modules.cnd.antlr.debug.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public abstract class Parser extends MatchExceptionState {
	public static final int MEMO_RULE_FAILED = -2;
	public static final int MEMO_RULE_UNKNOWN = -1;
	public static final int INITIAL_FOLLOW_STACK_SIZE = 100;

        protected int guessing = 0;
        protected String filename;
        protected TokenBuffer input;
        private static int meaningLessErrorsLimit = 100;
        
    /** Nesting level of registered handlers */
    // protected int exceptionLevel = 0;

    /** Table of token type to token names */
    protected String[] tokenNames;

    /** AST return value for a rule is squirreled away here */
    protected AST returnAST;

    /** AST support code; parser delegates to this object.
	 *  This is set during parser construction by default
	 *  to either "new ASTFactory()" or a ctor that
	 *  has a token type to class map for hetero nodes.
	 */
    protected ASTFactory astFactory = null;

	/** Constructed if any AST types specified in tokens{..}.
	 *  Maps an Integer->Class object.
	 */
	protected Hashtable tokenTypeToASTClassMap = null;

    private boolean ignoreInvalidDebugCalls = false;

    /** Used to keep track of indentdepth for traceIn/Out */
    protected int traceDepth = 0;

	/** An array[size num rules] of Map<Integer,Integer> that tracks
	 *  the stop token index for each rule.  ruleMemo[ruleIndex] is
	 *  the memoization table for ruleIndex.  For key ruleStartIndex, you
	 *  get back the stop token for associated rule or MEMO_RULE_FAILED.
	 */
	protected Map[] ruleMemo;

	/** Set to true upon any error; reset upon first valid token match */
        // functionality moved to matchError from the MatchExceptionState
	//protected boolean failed = false;

	/** If the user specifies a tokens{} section with heterogeneous
	 *  AST node types, then ANTLR generates code to fill
	 *  this mapping.
	 */
	public Hashtable getTokenTypeToASTClassMap() {
		return tokenTypeToASTClassMap;
	}

    public void addMessageListener(MessageListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new IllegalArgumentException("addMessageListener() is only valid if parser built for debugging");
    }

    public void addParserListener(ParserListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new IllegalArgumentException("addParserListener() is only valid if parser built for debugging");
    }

    public void addParserMatchListener(ParserMatchListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new IllegalArgumentException("addParserMatchListener() is only valid if parser built for debugging");
    }

    public void addParserTokenListener(ParserTokenListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new IllegalArgumentException("addParserTokenListener() is only valid if parser built for debugging");
    }

    public void addSemanticPredicateListener(SemanticPredicateListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new IllegalArgumentException("addSemanticPredicateListener() is only valid if parser built for debugging");
    }

    public void addSyntacticPredicateListener(SyntacticPredicateListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new IllegalArgumentException("addSyntacticPredicateListener() is only valid if parser built for debugging");
    }

    public void addTraceListener(TraceListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new IllegalArgumentException("addTraceListener() is only valid if parser built for debugging");
    }

    /**Get another token object from the token stream */
    public abstract void consume();

    /** Consume tokens until one matches the given token */
    public void consumeUntil(int tokenType) {
        int LA1 = LA(1);
        while (LA1 != Token.EOF_TYPE && LA1 != tokenType) {
            consume();
            LA1 = LA(1);
        }
    }

    /** Consume tokens until one matches the given token set */
    public void consumeUntil(BitSet set) {
        int LA1 = LA(1);
        while (LA1 != Token.EOF_TYPE && !set.member(LA1)) {
            consume();
            LA1 = LA(1);
        }
    }

    protected void defaultDebuggingSetup(TokenStream lexer, TokenBuffer tokBuf) {
        // by default, do nothing -- we're not debugging
    }

    /** Get the AST return value squirreled away in the parser */
    public AST getAST() {
        return returnAST;
    }

    public ASTFactory getASTFactory() {
        return astFactory;
    }

    public String getFilename() {
        return filename;
    }

    /*public ParserSharedInputState getInputState() {
        return inputState;
    }*/

    /*public void setInputState(ParserSharedInputState state) {
        inputState = state;
    }*/

    public String getTokenName(int num) {
        return tokenNames[num];
    }

    public String[] getTokenNames() {
        return tokenNames;
    }

    public boolean isDebugMode() {
        return false;
    }

    /** Return the token type of the ith token of lookahead where i=1
     * is the current token being examined by the parser (i.e., it
     * has not been matched yet).
     */
    public abstract int LA(int i);

    /**Return the ith token of lookahead */
    public abstract Token LT(int i);

    // Forwarded to TokenBuffer
    public int mark() {
        return input.mark();
    }

    /**Make sure current lookahead symbol matches token type <tt>t</tt>.
     * Throw an exception upon mismatch, which is catch by either the
     * error handler or by the syntactic predicate.
     */
    public void match(int t) throws MismatchedTokenException {
        assert(matchError == false);
        if (LA(1) != t) {
			matchError = true;
			throw new MismatchedTokenException(tokenNames, LT(1), t, false, getFilename());
		}
		else {
        	// mark token as consumed -- fetch next token deferred until LA/LT
			matchError = false;
            consume();
		}
	}

    /**Make sure current lookahead symbol matches the given set
     * Throw an exception upon mismatch, which is catch by either the
     * error handler or by the syntactic predicate.
     */
    public void match(BitSet b) throws MismatchedTokenException {
        assert(matchError == false);
        if (!b.member(LA(1))) {
			matchError = true;
            throw new MismatchedTokenException(tokenNames, LT(1), b, false, getFilename());
		}
		else {
        	// mark token as consumed -- fetch next token deferred until LA/LT
			matchError = false;
            consume();
		}
	}

    public void matchNot(int t) throws MismatchedTokenException {
        assert(matchError == false);
        if (LA(1) == t) {
        	// Throws inverted-sense exception
			matchError = true;
            throw new MismatchedTokenException(tokenNames, LT(1), t, true, getFilename());
		}
		else {
        	// mark token as consumed -- fetch next token deferred until LA/LT
			matchError = false;
            consume();
		}
	}

    /** @deprecated as of 2.7.2. This method calls System.exit() and writes
     *  directly to stderr, which is usually not appropriate when
     *  a parser is embedded into a larger application. Since the method is
     *  <code>static</code>, it cannot be overridden to avoid these problems.
     *  ANTLR no longer uses this method internally or in generated code.
     */
    public static void panic() {
        System.err.println("Parser: panic");
        System.exit(1);
    }

    public void removeMessageListener(MessageListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new RuntimeException("removeMessageListener() is only valid if parser built for debugging");
    }

    public void removeParserListener(ParserListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new RuntimeException("removeParserListener() is only valid if parser built for debugging");
    }

    public void removeParserMatchListener(ParserMatchListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new RuntimeException("removeParserMatchListener() is only valid if parser built for debugging");
    }

    public void removeParserTokenListener(ParserTokenListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new RuntimeException("removeParserTokenListener() is only valid if parser built for debugging");
    }

    public void removeSemanticPredicateListener(SemanticPredicateListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new IllegalArgumentException("removeSemanticPredicateListener() is only valid if parser built for debugging");
    }

    public void removeSyntacticPredicateListener(SyntacticPredicateListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new IllegalArgumentException("removeSyntacticPredicateListener() is only valid if parser built for debugging");
    }

    public void removeTraceListener(TraceListener l) {
        if (!ignoreInvalidDebugCalls)
            throw new RuntimeException("removeTraceListener() is only valid if parser built for debugging");
    }

    /** Parser error-reporting function can be overridden in subclass */
    public void reportError(RecognitionException ex) {
        if (ex.fileName == null) {
            if (meaningLessErrorsLimit > 0) {
                System.err.println(ex);
                meaningLessErrorsLimit--;
            }
        } else {
            System.err.println(ex);
        }
    }

    /** Parser error-reporting function can be overridden in subclass */
    public void reportError(String s) {
        if (getFilename() == null) {
            if (meaningLessErrorsLimit > 0) {
                System.err.println("error: " + s);
                meaningLessErrorsLimit--;
            }
        }
        else {
            System.err.println(getFilename() + ": error: " + s);
        }
    }

    /** Parser warning-reporting function can be overridden in subclass */
    public void reportWarning(String s) {
        if (getFilename() == null) {
            if (meaningLessErrorsLimit > 0) {
                System.err.println("warning: " + s);
                meaningLessErrorsLimit--;
            }
        }
        else {
            System.err.println(getFilename() + ": warning: " + s);
        }
    }

	public void recover(RecognitionException ex,
						BitSet tokenSet) {
		consume();
		consumeUntil(tokenSet);
	}

    public void rewind(int pos) {
        input.rewind(pos);
    }

    /** Specify an object with support code (shared by
     *  Parser and TreeParser.  Normally, the programmer
     *  does not play with this, using setASTNodeType instead.
     */
    public void setASTFactory(ASTFactory f) {
        astFactory = f;
    }

    public void setASTNodeClass(String cl) {
        astFactory.setASTNodeClass(cl);
    }

    /** Specify the type of node to create during tree building; use setASTNodeClass now
     *  to be consistent with Token Object Type accessor.
	 *  @deprecated since 2.7.1
     */
    public void setASTNodeType(String nodeType) {
        setASTNodeClass(nodeType);
    }

    public void setDebugMode(boolean debugMode) {
        if (!ignoreInvalidDebugCalls)
            throw new RuntimeException("setDebugMode() only valid if parser built for debugging");
    }

    public void setFilename(String f) {
        filename = f;
    }

    public void setIgnoreInvalidDebugCalls(boolean value) {
        ignoreInvalidDebugCalls = value;
    }

    /** Set or change the input token buffer */
    public void setTokenBuffer(TokenBuffer t) {
        input = t;
    }

    public void traceIndent() {
        for (int i = 0; i < traceDepth; i++)
            System.out.print(" ");
    }

    public void traceIn(String rname) {
        traceDepth += 1;
        traceIndent();
        System.out.println("> " + rname + "; LA(1)==" + LT(1).getText() +
                           ((guessing > 0)?" [guessing="+guessing+"]":""));
    }

    public void traceOut(String rname) {
        traceIndent();
        System.out.println("< " + rname + "; LA(1)==" + LT(1).getText() +
                           ((guessing > 0)?" [guessing="+guessing+"]":""));
        traceDepth -= 1;
    }

	/** Given a rule number and a start token index number, return
	 *  MEMO_RULE_UNKNOWN if the rule has not parsed input starting from
	 *  start index.  If this rule has parsed input starting from the
	 *  start index before, then return where the rule stopped parsing.
	 *  It returns the index of the last token matched by the rule.
	 *
	 *  For now we use a hashtable and just the slow Object-based one.
	 *  Later, we can make a special one for ints and also one that
	 *  tosses out data after we commit past input position i.
	 */
	public int getRuleMemoization(int ruleIndex, int ruleStartIndex) {
		if ( ruleMemo[ruleIndex]==null ) {
			ruleMemo[ruleIndex] = new HashMap();
		}
		Integer stopIndexI =
			(Integer)ruleMemo[ruleIndex].get(new Integer(ruleStartIndex));
		if ( stopIndexI==null ) {
			return MEMO_RULE_UNKNOWN;
		}
		return stopIndexI.intValue();
	}

	/** Has this rule already parsed input at the current index in the
	 *  input stream?  Return the stop token index or MEMO_RULE_UNKNOWN.
	 *  If we attempted but failed to parse properly before, return
	 *  MEMO_RULE_FAILED.
	 *
	 *  This method has a side-effect: if we have seen this input for
	 *  this rule and successfully parsed before, then seek ahead to
	 *  1 past the stop token matched for this rule last time.
	 */
	public boolean alreadyParsedRule(int ruleIndex) {
		//System.out.println("alreadyParsedRule("+ruleIndex+","+inputState.input.index()+")");
		int stopIndex = getRuleMemoization(ruleIndex, input.index());
		if ( stopIndex==MEMO_RULE_UNKNOWN ) {
                        //System.out.println("rule unknown");
			return false;
		}
		if ( stopIndex==MEMO_RULE_FAILED ) {
			//System.out.println("rule "+ruleIndex+" will never succeed");
			matchError=true;
		}
		else {
			
			/*System.out.println("seen rule "+ruleIndex+" before; skipping ahead to "+
				inputState.input.get(stopIndex+1)+"@"+(stopIndex+1)+" failed="+matchError);
			*/
                        matchError=false;
			input.seek(stopIndex+1); // jump to one past stop token
		}
		return true;
	}

	/** Record whether or not this rule parsed the input at this position
	 *  successfully.  Use a standard java hashtable for now.
	 */
	public void memoize(int ruleIndex, int ruleStartIndex) {
		int stopTokenIndex = matchError ? MEMO_RULE_FAILED : input.index()-1;
		//System.out.println("memoize("+ruleIndex+", "+ruleStartIndex+"); failed="+matchError+" stop="+stopTokenIndex);
		if ( ruleMemo[ruleIndex]!=null ) {
			ruleMemo[ruleIndex].put(
				new Integer(ruleStartIndex), new Integer(stopTokenIndex)
			);
		}
	}
}
