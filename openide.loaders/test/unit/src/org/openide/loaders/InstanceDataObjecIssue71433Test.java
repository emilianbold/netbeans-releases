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

/** Test behavior of method escapeAndCut. This is important for window system persistence
 * which uses InstanceDataObject to serialize TopComponent instance.
 *
 * @author Marek Slama
 */
public class InstanceDataObjecIssue71433Test extends NbTestCase {

    public InstanceDataObjecIssue71433Test(String name) {
        super (name);
    }

    /** escapeAndCut must create the same result when
     * 1.espaceAndCut is used
     * 2.espaceAndCut, unescape, espaceAndCut is used
     */
    public void testEscapeAndCut () throws Exception {
        //This special case caused escaped string to be cut just after '#' char
        //from beginning.
        String testName = "com.pantometrics.editor.form.display.PantoEditTopComponent";
        String resultName1 = InstanceDataObject.escapeAndCut(testName);
        String resultName2 = InstanceDataObject.unescape(resultName1);
        resultName2 = InstanceDataObject.escapeAndCut(resultName2);
        
        assertEquals ("Must be the same", resultName1, resultName2);
    }
    
}
