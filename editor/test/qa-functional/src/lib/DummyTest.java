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
