/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.openide;

import javax.swing.text.StyledDocument;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.text.*;

/**
 *
 * @author mmetelka
 */
public class InheritedNbDocumentTest extends NbDocumentTest {
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(InheritedNbDocumentTest.class);
        return suite;
    }
    
    /** Creates a new instance of InheritedUndoRedoTest */
    public InheritedNbDocumentTest(String methodName) {
        super(methodName);
    }
    
    protected StyledDocument createStyledDocument() {
        return new NbEditorDocument(NbEditorKit.class);
    }

}
