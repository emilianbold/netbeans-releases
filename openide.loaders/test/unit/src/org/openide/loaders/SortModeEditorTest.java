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

package org.openide.loaders;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author  pzajac
 */
public class SortModeEditorTest extends NbTestCase {

    /** Creates a new instance of SortModeEditorTest */
    public SortModeEditorTest(String name) {
        super(name);
    }


    public void testSortModeEditor () {

        SortModeEditor editor = new SortModeEditor();
        String values[] = editor.getTags();
        for (int i = 0 ; i < values.length ; i++ ) {
            editor.setAsText(values[i]);
            assertEquals (values[i],editor.getAsText());
        }
        
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
