package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.Bundle;
import org.netbeans.jemmy.BundleManager;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestCompletedException;

public class jemmy_013 extends JemmyTest {
    public int runIt(Object obj) {
	try {
	    BundleManager bm = new BundleManager();
	    Bundle prop1 = bm.loadBundleFromJar(System.getProperty("user.dir") + 
						System.getProperty("file.separator") + "jemmy_013_prop1.jar", 
						"Bundles/prop1", "jemmy_013_prop1");
	    if(prop1 == null) {
		return(1);
	    }
	    if(bm.getBundle("jemmy_013_prop1") != prop1) {
		return(1);
	    }
	    Bundle prop2 = bm.loadBundleFromJar(System.getProperty("user.dir") + 
						System.getProperty("file.separator") + "jemmy_013_prop2.jar", 
						"Bundles/prop2", "jemmy_013_prop2");
	    if(prop2 == null) {
		return(1);
	    }
	    if(bm.getBundle("jemmy_013_prop2") != prop2) {
		return(1);
	    }
	    if(!bm.getResource("one.two").equals("one_two")) {
		return(1);
	    }
	    if(!bm.getResource("two.two").equals("two_two")) {
		return(1);
	    }
	    if(!bm.getResource("jemmy_013_prop1", "one.two").equals("one_two")) {
		return(1);
	    }
	    if(!bm.getResource("jemmy_013_prop2", "two.two").equals("two_two")) {
		return(1);
	    }
	    if(bm.getResource("jemmy_013_prop1", "two.two") != null) {
		return(1);
	    }
	    if(bm.getResource("jemmy_013_prop2", "one.two") != null) {
		return(1);
	    }

	    if(!prop1.getResource("one.two").equals("one_two")) {
		return(1);
	    }
	    if(!prop2.getResource("two.two").equals("two_two")) {
		return(1);
	    }
	    if(prop1.getResource("two.two") != null) {
		return(1);
	    }
	    if(prop2.getResource("one.two") != null) {
		return(1);
	    }

	    prop1.print(JemmyProperties.getCurrentOutput().getOutput());
	    prop2.print(JemmyProperties.getCurrentOutput().getOutput());
	    bm.print(JemmyProperties.getCurrentOutput().getOutput());
	} catch(TestCompletedException e) {
	    throw(e);
	} catch(Exception e) {
	    throw(new TestCompletedException(1, e));
	}
	return(0);
    }
}
