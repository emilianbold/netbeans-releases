package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import javax.swing.SwingUtilities;

public class jemmy_026 extends JemmyTest {

    public int runIt(Object obj) {

	try {

	    JemmyProperties.push();

	    QueueTool cleaner = new QueueTool();
	    cleaner.getTimeouts().setTimeout("QueueTool.WaitQueueEmptyTimeout",
					   30000);
	    cleaner.setOutput(getOutput().createErrorOutput());

	    JemmyProperties.setCurrentTimeout("EventDispatcher.WaitQueueEmptyTimeout",
					      2000);

	    getOutput().printLine("===================================================");
	    getOutput().printLine("Positive empty");
	    SwingUtilities.invokeLater(new Sleeper(1000));
	    EventDispatcher.waitQueueEmpty();
	    SwingUtilities.invokeLater(new Sleeper(1000));
	    getOutput().printLine("END Positive empty");
	    getOutput().printLine("===================================================");

	    cleaner.waitEmpty();

	    getOutput().printLine("===================================================");
	    getOutput().printLine("Negative empty");
	    SwingUtilities.invokeLater(new Sleeper(1000));
	    SwingUtilities.invokeLater(new Sleeper(1000));
	    SwingUtilities.invokeLater(new Sleeper(1000));
	    try {
		EventDispatcher.waitQueueEmpty();
		throw(new TestCompletedException(1, ""));
	    } catch(TimeoutExpiredException e) {
	    }
	    getOutput().printLine("END Negative empty");
	    getOutput().printLine("===================================================");

	    cleaner.waitEmpty();

	    JemmyProperties.setCurrentTimeout("EventDispatcher.WaitQueueEmptyTimeout",
					      5000);

	    getOutput().printLine("===================================================");
	    getOutput().printLine("Positive timed");
	    SwingUtilities.invokeLater(new Sleeper(2000));
	    EventDispatcher.waitQueueEmpty(100);
	    getOutput().printLine("END Positive timed");
	    getOutput().printLine("===================================================");

	    cleaner.waitEmpty();

	    getOutput().printLine("===================================================");
	    getOutput().printLine("Negative timed");
	    SwingUtilities.invokeLater(new Sleeper(1000));
	    SwingUtilities.invokeLater(new Sleeper(1000));
	    SwingUtilities.invokeLater(new Sleeper(1000));
	    SwingUtilities.invokeLater(new Sleeper(1000));
	    SwingUtilities.invokeLater(new Sleeper(500));
	    try {
		EventDispatcher.waitQueueEmpty(600);
		throw(new TestCompletedException(1, ""));
	    } catch(TimeoutExpiredException e) {
	    }
	    getOutput().printLine("END Negative timed");
	    getOutput().printLine("===================================================");

	} catch(Exception e) {
	    if(e instanceof TestCompletedException) {
		throw((TestCompletedException)e);
	    } else {
		throw(new TestCompletedException(1, e));
	    }
	} finally {
	    finalize();
	    JemmyProperties.pop();
	}

	return(0);
    }

    class Sleeper implements Runnable {
	long timeToSleep;
	public Sleeper(long timeToSleep) {
	    this.timeToSleep = timeToSleep;
	}
	public void run() {
	    try {
		Thread.currentThread().sleep(timeToSleep);
	    } catch(InterruptedException e) {
		getOutput().printStackTrace(e);
	    }
	}
    }
}
