/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jshell.model;

/**
 * Describes a range of text. For performance {@link #javax.swing.text.Position}s 
 * are not used, can be only used for text which does not change.
 */
public final class Rng {
    public final int start;
    public final int end;

    public Rng(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public String toString() {
        return "{" + start + "-" + end + "}";
    }
    
    public int len() {
        return end - start;
    }
}
