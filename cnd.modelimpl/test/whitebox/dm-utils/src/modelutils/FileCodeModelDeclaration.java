/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package modelutils;

import java.util.ArrayList;
import java.util.Iterator;

public class FileCodeModelDeclaration {
    private String kind = null;
    private String declarationString = null;
    private String declarationPosition = null;
    private ArrayList<FileCodeModelDeclaration> children = new ArrayList<FileCodeModelDeclaration>();
    
    public FileCodeModelDeclaration() {
    }
    
    public FileCodeModelDeclaration(String kind, String declarationString, String declarationPosition) {
        setKind(kind);
        setDeclarationString(declarationString);
        setDeclarationPosition(declarationPosition);
    }
    
    public void setKind(String kind) {
        this.kind = kind;
    }
    
    public void setDeclarationString(String declarationString) {
        this.declarationString = declarationString;
    }
    
    public String getDeclaration() {
        return declarationString;
    }
    
    public void setDeclarationPosition(String declarationPosition) {
        this.declarationPosition = declarationPosition;
    }
    
    public void addChild(FileCodeModelDeclaration child) {
        children.add(child);
    }
    
    public void dump() {
        dump(0);
    }
    
    private void dump(int level) {
        String formatString = ((level > 0) ? ("%" + (level * 2) + "s") : ("%s")) + "%s %s %s\n"; // NOI18N
        System.out.printf(formatString, "", kind, declarationString, declarationPosition);
        for (Iterator<FileCodeModelDeclaration> i = children.iterator(); i.hasNext(); ) {
            i.next().dump(level + 1);
        }
    }
    
    public String toString() {
        return kind + " " + declarationString + " " + declarationPosition; // NOI18N
    }
}

