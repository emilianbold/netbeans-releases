/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
