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

/*
 * EditorTestCase.java
 *
 * Created on 24. srpen 2004, 12:32
 */

package lib;

/**
 * Dummy test that just tries to open the default project.
 * <br>
 * It should exclude problems related to project opening.
 * <br>
 * It will also eliminate the extra time for the project opening
 * to be added into the first test being executed.
 *
 * @author Miloslav Metelka
 */
public class DummyTest extends EditorTestCase {

    public DummyTest() {
        super("test");
    }

    public void test() {
        openDefaultProject();
    }

}
