/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.mixeddev.java.model;

import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.repeat;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class JavaTypeInfo implements JavaEntityInfo {
    
    private final CharSequence fullQualifiedName;
    
    private final CharSequence name;
    
    private final int array;

    public JavaTypeInfo(CharSequence fullQualifiedName, CharSequence name, int array) {
        this.fullQualifiedName = fullQualifiedName;
        this.name = name;
        this.array = array;
    }

    public CharSequence getFullQualifiedName() {
        return fullQualifiedName;
    }

    public CharSequence getName() {
        return name;
    }
    
    public CharSequence getText() {
        return (name != null ? name.toString() : "<null>") + (array > 0 ? repeat("[]", array) : ""); // NOI18N
    }
    
    public int getArrayDepth() {
        return array;
    }    

    @Override
    public String toString() {
        return getText().toString();
    }
}
