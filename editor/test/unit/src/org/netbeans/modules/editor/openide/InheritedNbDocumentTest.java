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
