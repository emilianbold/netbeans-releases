package org.netbeans.modules.cnd.antlr.collections.impl;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

public class IntRange {
    int begin, end;


    public IntRange(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String toString() {
        return begin + ".." + end;
    }
}
