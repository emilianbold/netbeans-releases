package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.Bundle;
import org.netbeans.jemmy.BundleManager;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestCompletedException;

public class jemmy_015 extends JemmyTest {
    public int runIt(Object obj) {
	try {
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
