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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class JSFConfigRenameToViewIdElement extends SimpleRefactoringElementImplementation {
    private final FacesRefactoringUtils.OccurrenceItem item;
    
    JSFConfigRenameToViewIdElement(FacesRefactoringUtils.OccurrenceItem item){
        this.item = item;
    }
    
    public String getText() {
        return getDisplayText();
    }
    
    public String getDisplayText() {
        return item.getRenameMessage();
    }
    
    public void performChange() {
        item.performRename();
    }
           
    public FileObject getParentFile() {
        return item.getFacesConfig();
    }
    
    public PositionBounds getPosition() {
        return null;
    }
    
    public Lookup getLookup() {
        return Lookups.singleton(item.getFacesConfig());
    }
}