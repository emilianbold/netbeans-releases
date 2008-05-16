/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ElsaResultAnalyser;

import java.io.BufferedReader;
import java.io.FileInputStream;

/**
 *
 * @author nk220367
 */
public class Parser {

    Lexer l;

    Parser(BufferedReader s) {
        l = new Lexer(s);
        l.getNextToken();
    }

    public AstNode parse() {
        return node();
    }

    void matchId() {
        if (l.getCurrentToken().type != Token.TT.TOKEN_ID) {
            System.out.println("id expected");
        }
    }

    void match(String s) {
        if (!l.getCurrentToken().name.equals(s)) {
            System.out.println(s + " expected");
        }
    }

    AstNode node() {
        AstNode node = new AstNode();

        node.line = l.getCurrentToken().line;
        
        // id
        //matchId();
        node.name = l.getCurrentToken().name;
        l.getNextToken();

        // id[1]
        if (l.getCurrentToken().name.equals("[")) {
            l.getNextToken();
            if (l.getCurrentToken().type == Token.TT.TOKEN_DIGIT) {
                l.getNextToken();
                if (l.getCurrentToken().name.equals("]")) {
                    l.getNextToken();
                }
            }
        }

        // id->id
        if (l.getCurrentToken().name.equals("->")) {
            l.getNextToken();
            if (l.getCurrentToken().type == Token.TT.TOKEN_ID) {
                l.getNextToken();
            }
        }

        if (l.getCurrentToken().name.equals("=")) { // id =

            if (l.isEndOfLine()) {
                l.getNextToken();
                return node;
            }
            l.getNextToken();

            node.value = l.getCurrentToken().name;
            if (l.isEndOfLine()) {

                int indent = l.getIndent();
                l.getNextToken();
                if (indent < l.getIndent()) {
                    indent = l.getIndent();
                    while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                        node.children.add(node());
                    }
                }                
                return node;
            }
            l.getNextToken();

            if (l.getCurrentToken().name.equals(":")) { // id = id:

                l.getNextToken();
                int indent = l.getIndent();
                while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                    node.children.add(node());
                }
            } else { // id = string

                node.value += l.getCurrentToken().name;
                node.value += l.getEndOfString().name;

                int indent = l.getIndent();
                l.getNextToken();
                if (indent < l.getIndent()) {
                    indent = l.getIndent();
                    while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                        node.children.add(node());
                    }
                }
            }
        } else if (l.getCurrentToken().name.equals(":")) { // id:

            if (l.isEndOfLine()) { // id:\n

                int indent = l.getIndent();
                l.getNextToken();
                if (indent < l.getIndent()) {
                    indent = l.getIndent();
                    while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                        node.children.add(node());
                    }
                }
            } else { // id: string
                l.getNextToken();
                node.value = l.getCurrentToken().name + l.getEndOfString().name;
                int indent = l.getIndent();
                l.getNextToken();
                if (indent < l.getIndent()) {
                    indent = l.getIndent();
                    while (indent == l.getIndent() && !l.getCurrentToken().name.equals("")) {
                        node.children.add(node());
                    }
                }
            }
        } else if (l.getCurrentToken().name.equals("is")) { // id is string

            l.getNextToken();
            node.value = l.getCurrentToken().name + l.getEndOfString().name;
            l.getNextToken();
        }

        return node;
    }
}
