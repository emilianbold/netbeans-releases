/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.dwarfdump.dwarf;

/**
 *
 * @author ak119685
 */
public class DwarfDeclaration {
    public final String kind;
    public final String declarationString;
    public final String declarationFile;    
    public final String declarationPosition;
            
    public DwarfDeclaration(String kind, String declarationString, String declarationFile, String declarationPosition) {
        this.kind = kind;
        this.declarationString = declarationString;
        this.declarationFile = declarationFile;
        this.declarationPosition = declarationPosition;
    }
    
    @Override
    public String toString() {
        return kind + " " + declarationString + " " + declarationFile + ":" + declarationPosition; // NOI18N
    }
}
