/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.editor.CodeFoldingSideBar;

/**
 *  Java Code Folding Side Bar. Component responsible for drawing folding signs and responding 
 *  on user fold/unfold action.
 *
 *  @author  Martin Roskanin
 */
public class NbJavaCodeFoldingSideBar extends CodeFoldingSideBar{
    
    private int startPos;
    private int endPos;
    private List elems = new ArrayList();
    
    public NbJavaCodeFoldingSideBar(){
    }
    
    /** Creates a new instance of NbCodeFoldingSideBar */
    public NbJavaCodeFoldingSideBar(JTextComponent target) {
        super(target);

    }
    
    public javax.swing.JComponent createSideBar(javax.swing.text.JTextComponent target) {
        return new NbJavaCodeFoldingSideBar(target);
    }

}
