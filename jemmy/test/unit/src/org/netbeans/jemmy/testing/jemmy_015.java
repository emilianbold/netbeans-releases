package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.ActionProducer;
import org.netbeans.jemmy.Bundle;
import org.netbeans.jemmy.BundleManager;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestCompletedException;
import org.netbeans.jemmy.TimeoutExpiredException;

public class jemmy_015 extends JemmyTest {
    private boolean interrupted = true;
    public int runIt(Object obj) {
	try {
	    try {
		new ActionProducer(new Action() {
			public Object launch(Object obj) {
			    try {
				Thread.sleep((long)(JemmyProperties.getCurrentTimeout("ActionProducer.MaxActionTime") * 1.5));
				interrupted = false;
			    } catch(Exception e) {
				getOutput().printStackTrace(e);
			    }
			    return(null);
			}
			public String getDescription() {
			    return("Fifteen seconds sleep");
			}
		    }).produceAction(null);
		return(1);
	    } catch(TimeoutExpiredException e) {
	    }
	    Thread.sleep((long)(JemmyProperties.getCurrentTimeout("ActionProducer.MaxActionTime") * 0.5) + 1);
	    if(!interrupted) {
		return(1);
	    }
	    if(!JemmyProperties.getCurrentResource("fjie.main_window").equals("Forte for Java")) {
		output.printLine("fjie.main_window = \"" + 
				 JemmyProperties.getCurrentResource("fjie.main_window") + "\"\n" +
				 "should be \"Forte for Java\"");
		return(1);
	    }
	    if(!JemmyProperties.getCurrentResource("", "fjie.main_window").equals("Forte for Java")) {
		output.printLine("fjie.main_window in main bundle = \"" + 
				 JemmyProperties.getCurrentResource("fjie.main_window") + "\"\n" +
				 "should be \"Forte for Java\"");
		return(1);
	    }

	    if(JemmyProperties.getCurrentTimeout("Waiter.WaitingTime") != 111111) {
		output.printLine("Waiter.WaitingTime = \"" + 
				 Long.toString(JemmyProperties.getCurrentTimeout("Waiter.WaitingTime")) + "\"\n" +
				 "should be \"111111\"");
		return(1);
	    }
	} catch(TestCompletedException e) {
	    throw(e);
	} catch(Exception e) {
	    throw(new TestCompletedException(1, e));
	}
	return(0);
    }
}
