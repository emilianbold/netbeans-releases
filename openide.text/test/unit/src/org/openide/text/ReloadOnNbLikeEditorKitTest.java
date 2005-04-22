/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openide.text;


import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;



/** Test to simulate problem #46885
 * @author  Jaroslav Tulach
 */
public class ReloadOnNbLikeEditorKitTest extends ReloadTest {
    public ReloadOnNbLikeEditorKitTest (String s) {
        super(s);
    }

    /** For subclasses to change to more nb like kits. */
    protected javax.swing.text.EditorKit createEditorKit () {
        return new NbLikeEditorKit ();
    }
}
