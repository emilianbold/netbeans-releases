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
