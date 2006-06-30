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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.CodeFoldingSideBar;

/**
 *  Java Code Folding Side Bar. Component responsible for drawing folding signs and responding
 *  on user fold/unfold action.
 *
 *  @author  Martin Roskanin
 */
public class NbJavaCodeFoldingSideBar extends CodeFoldingSideBar{

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
