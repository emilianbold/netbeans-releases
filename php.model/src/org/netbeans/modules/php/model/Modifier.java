/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.model;

import java.util.List;


/**
 * @author ads
 *
 */
public enum Modifier {

    PUBLIC(1<<1),
    PROTECTED(1<<2),
    PRIVATE(1<<3),
    STATIC(1<<4),
    ABSTRACT(1<<5),
    FINAL(1<<6),
    /**
     * @see http://www.php.net/manual/en/language.oop5.visibility.php
     * Note:  The PHP 4 method of declaring a variable with the var keyword is 
     * still supported for compatibility reasons (as a synonym for the public 
     * keyword). In PHP 5 before 5.1.3, its usage would generate an 
     * E_STRICT warning."
     */
    VAR(PUBLIC.flag()),
    ;

    public static final int VISIBILITY_MASK = 
            PUBLIC.flag() & PROTECTED.flag() & PRIVATE.flag();
    
    private final int flag;
    Modifier(int flag) { this.flag = flag; }

    /**
     * Returns the bit-flag associated with the underlying <code>Modifier<code>.
     * @return the bit-flag.
     */
    public int flag() { return flag; }

    /**
     * 
     * @param flags the flags value
     * @return <code>true</code> if underlying modifier flag is on the specified
     * <code>flags</code>.
     */
    public boolean isOn(int flags) { return (flags & flag) == flag; }

    @Override
    public String toString(){
        return super.toString().toLowerCase();
    }
    
    public static Modifier forString( String str ){
        for( Modifier modifier : values() ){
            if ( modifier.toString().equals( str )){
                return modifier;
            }
        }
        return null;
    }
    
    /**
     * Converts the specified list of modifiers to the bit-flags.  
     * Returned value can be processed via a bit-mask.
     * @param list the list of actual modifiers.
     * @return the bit-flags value according to the modifiers actually defined 
     * in the source.
     */
    public static int toFlags(List<Modifier> list) {
        int flags = 0;
        for(Modifier m: list) {
            flags |= m.flag();
        }
        return flags;
    }
    
    public static int toLogicalFlags(int actualFlags) {
        return isDefaultVisibility(actualFlags) ? 
            actualFlags |= PUBLIC.flag() : actualFlags;
    }
    
    public static boolean isDefaultVisibility(int actualFlags) {
        return (actualFlags & VISIBILITY_MASK) == 0;
    }
}
