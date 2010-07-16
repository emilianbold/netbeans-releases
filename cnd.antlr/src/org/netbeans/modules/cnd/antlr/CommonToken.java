package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

public class CommonToken extends TokenImpl {
    // most tokens will want line and text information
    protected int line;
    protected String text = null;
    protected int col;

    public CommonToken() {
    }

    public CommonToken(int t, String txt) {
        type = t;
        setText(txt);
    }

    public CommonToken(String s) {
        text = s;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setLine(int l) {
        line = l;
    }

    @Override
    public final void setText(String s) {
        text = s;
    }

    @Override
    public String toString() {
        return "[\"" + getText() + "\",<" + type + ">,line=" + line + ",col=" + col + "]";
    }

    /** Return token's start column */
    @Override
    public int getColumn() {
        return col;
    }

    @Override
    public void setColumn(int c) {
        col = c;
    }
}
