package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

class OneOrMoreBlock extends BlockWithImpliedExitPath {

    public OneOrMoreBlock(Grammar g) {
        super(g);
    }

    public OneOrMoreBlock(Grammar g, Token start) {
        super(g, start);
    }

    public void generate(Context context) {
        grammar.generator.gen(this, context);
    }

    public Lookahead look(int k) {
        return grammar.theLLkAnalyzer.look(k, this);
    }

    public String toString() {
        return super.toString() + "+";
    }
}
