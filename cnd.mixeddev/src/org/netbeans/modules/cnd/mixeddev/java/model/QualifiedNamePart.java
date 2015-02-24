/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.mixeddev.java.model;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class QualifiedNamePart {
    
    private final CharSequence text;
    
    private final Kind kind;

    public QualifiedNamePart(CharSequence text, Kind kind) {
        this.text = text;
        this.kind = kind;
    }

    public CharSequence getText() {
        return text;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        return getText().toString();
    }
    
    public static enum Kind {
        PACKAGE,
        CLASS,
        NESTED_CLASS,
        METHOD
    }
}
