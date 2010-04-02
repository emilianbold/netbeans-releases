/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */


package org.netbeans.modules.php.editor.indent;

/**
 *
 * @author Petr Pisl
 */
public class FormatToken {


    public enum Kind {
	TEXT,
	ANCHOR,
	UNBREAKABLE_SEQUENCE_START,
	UNBREAKABLE_SEQUENCE_END,
	OPEN_TAG,
	CLOSE_TAG,
	INIT_TAG, // special tag, that will contain some initional information
	HTML,

	INDENT,
	WHITESPACE,
        WHITESPACE_INDENT,
	WHITESPACE_BEFORE_NAMESPACE,
	WHITESPACE_AFTER_NAMESPACE,
	WHITESPACE_BEFORE_USE,
	WHITESPACE_BETWEEN_USE,
	WHITESPACE_AFTER_USE,
	WHITESPACE_BEFORE_CLASS_LEFT_BRACE,
	WHITESPACE_AROUND_OBJECT_OP,
	WHITESPACE_AROUND_CONCAT_OP,
	WHITESPACE_AROUND_UNARY_OP,
	WHITESPACE_AROUND_BINARY_OP,
	WHITESPACE_AROUND_TERNARY_OP,
	WHITESPACE_AROUND_ASSIGN_OP,
	WHITESPACE_AROUND_KEY_VALUE_OP,

	WHITESPACE_BEFORE_METHOD_DEC_PAREN,
	WHITESPACE_BEFORE_METHOD_CALL_PAREN,
	WHITESPACE_BEFORE_IF_PAREN,
	WHITESPACE_BEFORE_FOR_PAREN,
	WHITESPACE_BEFORE_WHILE_PAREN,
	WHITESPACE_BEFORE_CATCH_PAREN,
	WHITESPACE_BEFORE_SWITCH_PAREN,
	WHITESPACE_BEFORE_ARRAY_DECL_PAREN,

	WHITESPACE_AFTER_CLASS_LEFT_BRACE,
	WHITESPACE_BEFORE_FUNCTION_LEFT_BRACE,
	WHITESPACE_BEFORE_IF_LEFT_BRACE,
	WHITESPACE_BEFORE_FOR_LEFT_BRACE,
	WHITESPACE_BEFORE_WHILE_LEFT_BRACE,
	WHITESPACE_BEFORE_SWITCH_LEFT_BACE,
	WHITESPACE_BEFORE_CATCH_LEFT_BRACE,
	WHITESPACE_BEFORE_OTHER_LEFT_BRACE,

	WHITESPACE_BEFORE_CLASS_RIGHT_BRACE,
	WHITESPACE_BEFORE_FUNCTION_RIGHT_BRACE,
	WHITESPACE_BEFORE_IF_RIGHT_BRACE,
	WHITESPACE_BEFORE_FOR_RIGHT_BRACE,
	WHITESPACE_BEFORE_WHILE_RIGHT_BRACE,
	WHITESPACE_BEFORE_SWITCH_RIGHT_BACE,
	WHITESPACE_BEFORE_CATCH_RIGHT_BRACE,
	WHITESPACE_BEFORE_OTHER_RIGHT_BRACE,

	WHITESPACE_WITHIN_ARRAY_DECL_PARENS,
	WHITESPACE_WITHIN_METHOD_DECL_PARENS,
	WHITESPACE_WITHIN_METHOD_CALL_PARENS,
	WHITESPACE_WITHIN_IF_PARENS,
	WHITESPACE_WITHIN_FOR_PARENS,
	WHITESPACE_WITHIN_WHILE_PARENS,
	WHITESPACE_WITHIN_SWITCH_PARENS,
	WHITESPACE_WITHIN_CATCH_PARENS,
	WHITESPACE_WITHIN_ARRAY_BRACKETS_PARENS,
	WHITESPACE_WITHIN_TYPE_CAST_PARENS,

	WHITESPACE_BEFORE_COMMA,
	WHITESPACE_AFTER_COMMA,
	WHITESPACE_BEFORE_SEMI,
	WHITESPACE_AFTER_SEMI,
	WHITESPACE_AFTER_TYPE_CAST,

	WHITESPACE_BEFORE_CLASS,
	WHITESPACE_AFTER_CLASS,
	WHITESPACE_BEFORE_FUNCTION,
	WHITESPACE_AFTER_FUNCTION,
	WHITESPACE_BEFORE_FIELD,
	WHITESPACE_BETWEEN_FIELDS,

	WHITESPACE_BETWEEN_LINE_COMMENTS,
	WHITESPACE_BETWEEN_OPEN_CLOSE_BRACES,

	WHITESPACE_IN_INTERFACE_LIST,
	WHITESPACE_IN_PARAMETER_LIST,
	WHITESPACE_IN_CHAINED_METHOD_CALLS,

	WHITESPACE_BEFORE_OPEN_PHP_TAG,
	WHITESPACE_AFTER_OPEN_PHP_TAG,
	WHITESPACE_BEFORE_CLOSE_PHP_TAG,
	WHITESPACE_AFTER_CLOSE_PHP_TAG,

	WHITESPACE_BEFORE_EXTENDS_IMPLEMENTS,
	WHITESPACE_BEFORE_FOR_STATEMENT,
	WHITESPACE_BEFORE_WHILE_STATEMENT,
	WHITESPACE_BEFORE_DO_STATEMENT,
	WHITESPACE_BEFORE_IF_ELSE_STATEMENT,
	WHITESPACE_IN_FOR,
	WHITESPACE_IN_TERNARY_OP,
	
	WHITESPACE_BEFORE_WHILE,
	WHITESPACE_BEFORE_ELSE,
	WHITESPACE_BEFORE_CATCH,
	
	LINE_COMMENT,
	COMMENT,
	DOC_COMMENT,
	DOC_COMMENT_END;
    }

    private int offset;
    private Kind id;
    private boolean whitespace;
    private boolean breakable;
    private String oldText;

    public FormatToken(Kind id, int offset) {
	this(id, offset, null);
    }

    public FormatToken(Kind id, int offset, String oldText) {
	this.offset = offset;
	this.id = id;
	this.oldText = oldText;
	this.whitespace = isWhitespace(id);
	this.breakable = true;
    }

    public Kind getId() {
	return id;
    }

    public int getOffset() {
	return offset;
    }

    public boolean isBreakable() {
	return breakable;
    }

    public String getOldText() {
	return oldText;
    }

    public boolean isWhitespace() {
	return whitespace;
    }

    private boolean isWhitespace (Kind kind) {
	return kind != Kind.TEXT &&  kind != Kind.ANCHOR 
		&& kind != Kind.UNBREAKABLE_SEQUENCE_START
		&& kind != Kind.UNBREAKABLE_SEQUENCE_END
		&& kind != Kind.INDENT && kind != Kind.LINE_COMMENT
		&& kind != Kind.COMMENT
		&& kind != Kind.DOC_COMMENT
		&& kind != Kind.DOC_COMMENT_END
		&& kind != Kind.OPEN_TAG
		&& kind != Kind.CLOSE_TAG
		&& kind != Kind.INIT_TAG
		&& kind != Kind.HTML;
    }

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append(id.name());
	sb.append(" offset: ").append(offset);
	if (oldText != null)
	    sb.append(" lexerToken <").append(oldText.length()).append(">: ").append("'").append(oldText).append("'");
	return sb.toString();
    }

    public static class IndentToken extends FormatToken {
	private int delta;

	public IndentToken(int offset, int delta) {
	    super(Kind.INDENT, offset, null);
	    this.delta = delta;
	}

	public int getDelta() {
	    return delta;
	}
    }

    public static class AnchorToken extends FormatToken {

	private int anchorColumn;

	public AnchorToken(int offset) {
	    super(Kind.ANCHOR, offset);
	    anchorColumn = 0;
	}

	public int getAnchorColumn() {
	    return anchorColumn;
	}

	public void setAnchorColumn(int column) {
	    this.anchorColumn = column;
	}

    }

    public static class UnbreakableSequenceToken extends FormatToken {

	private AnchorToken anchor;

	public UnbreakableSequenceToken(int offset, AnchorToken anchor, Kind start) {
	    super(start, offset);
	    this.anchor = anchor;
	}

	public AnchorToken getAnchor() {
	    return anchor;
	}
	
    }

    /**
     * The first tag in the list that contains information for the processing list
     * of formatting commants. 
     */
    public static class InitToken extends FormatToken {

	boolean hasHTML;

	public InitToken() {
	    super(Kind.INIT_TAG, 0);
	    hasHTML = false;
	}

	public boolean hasHTML() {
	    return hasHTML;
	}

	public void setHasHTML(boolean hasHTML) {
	    this.hasHTML = hasHTML;
	}
    }
    
}
