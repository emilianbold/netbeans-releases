/*
 * ITestable.java
 *
 * Created on October 17, 2003, 6:50 PM
 */

package org.netbeans.xtest.testrunner;

/**
 * Interface which allows TestRunnerHarness launch the test boards
 * @author  mb115822
 */
public interface TestBoardLauncher {
    
    public void launchTestBoard(JUnitTestRunnerProperties testsToBeExecuted) throws TestBoardLauncherException;
    
}
