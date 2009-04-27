package antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

// class implements a String-like object whose sole purpose is to be
// entered into a lexer HashTable.  It uses a lexer object to get
// information about case sensitivity.

public final class ANTLRHashString {
    // only one of s or buf is non-null
    private String s;
    private char[] buf;
    private int len;
    private CharScanner lexer;
    private boolean caseSensitiveLiterals;
    private int hashCode = -1;
    private static final int prime = 151;


    public ANTLRHashString(char[] buf, int length, CharScanner lexer) {
        this.lexer = lexer;
        this.caseSensitiveLiterals = lexer.getCaseSensitiveLiterals();
        setBuffer(buf, length);
    }

    // Hash strings constructed this way are unusable until setBuffer or setString are called.
    public ANTLRHashString(CharScanner lexer) {
        this.lexer = lexer;
        this.caseSensitiveLiterals = lexer.getCaseSensitiveLiterals();
    }

    public ANTLRHashString(String s, CharScanner lexer) {
        this.lexer = lexer;
        this.caseSensitiveLiterals = lexer.getCaseSensitiveLiterals();
        setString(s);
    }

    public ANTLRHashString(String s, boolean caseSensitiveLiterals) {
        this.lexer = null;
        this.caseSensitiveLiterals = caseSensitiveLiterals;
        setString(s);
    }

    private final char charAt(int index) {
        return (s != null) ? s.charAt(index) : buf[index];
    }

    // Return true if o is an ANTLRHashString equal to this.
    public boolean equals(Object o) {
        if (!(o instanceof ANTLRHashString) && !(o instanceof String)) {
            return false;
        }

        ANTLRHashString s;
        if (o instanceof String) {
            s = new ANTLRHashString((String)o, lexer);
        }
        else {
            s = (ANTLRHashString)o;
        }
        int l = length();
        if (s.length() != l) {
            return false;
        }
        if (lexer != null) {
            if (lexer.getCaseSensitiveLiterals()) {
                for (int i = 0; i < l; i++) {
                    if (charAt(i) != s.charAt(i)) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < l; i++) {
                    if (lexer.toLower(charAt(i)) != lexer.toLower(s.charAt(i))) {
                        return false;
                    }
                }
            }
        } else {
            if (caseSensitiveLiterals) {
                for (int i = 0; i < l; i++) {
                    if (charAt(i) != s.charAt(i)) {
                        return false;
                    }
                }
            }
            else {
                for (int i = 0; i < l; i++) {
                    if (toLower(charAt(i)) != toLower(s.charAt(i))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private char toLower(char c) {
        return Character.toLowerCase(c);
    }

    public int hashCode() {
        // don't clear why, but if used internally by ANTLR => hash code changes it's value :-(
        if (hashCode == -1 || lexer != null) {
            int hashval = 0;
            int l = length();

            if (lexer != null) {
                if (lexer.getCaseSensitiveLiterals()) {
                    for (int i = 0; i < l; i++) {
                        hashval = hashval * prime + charAt(i);
                    }
                } else {
                    for (int i = 0; i < l; i++) {
                        hashval = hashval * prime + lexer.toLower(charAt(i));
                    }
                }
            } else {
                if (caseSensitiveLiterals) {
                    for (int i = 0; i < l; i++) {
                        hashval = hashval * prime + charAt(i);
                    }
                }
                else {
                    for (int i = 0; i < l; i++) {
                        hashval = hashval * prime + toLower(charAt(i));
                    }
                }
            }
            hashCode = hashval;
        }
        return hashCode;
    }

    private final int length() {
        return (s != null) ? s.length() : len;
    }

    public void setBuffer(char[] buf, int length) {
        this.buf = buf;
        this.len = length;
        s = null;
    }

    public void setString(String s) {
        this.s = s;
        buf = null;
    }
}
