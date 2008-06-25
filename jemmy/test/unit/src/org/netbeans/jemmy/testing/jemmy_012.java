package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.Bundle;
import org.netbeans.jemmy.BundleManager;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestCompletedException;

public class jemmy_012 extends JemmyTest {
    public int runIt(Object obj) {
	BundleManager oldbm = JemmyProperties.getCurrentBundleManager();
	try {
	    BundleManager bm = new BundleManager();
	    Bundle prop1 = bm.loadBundleFromFile(System.getProperty("user.dir") + 
						 System.getProperty("file.separator") + "jemmy_012_prop1", "jemmy_012_prop1");
	    if(prop1 == null) {
		return(1);
	    }
	    if(bm.getBundle("jemmy_012_prop1") != prop1) {
		return(1);
	    }
	    Bundle prop2 = bm.loadBundleFromFile(System.getProperty("user.dir") + 
						 System.getProperty("file.separator") + "jemmy_012_prop2", "jemmy_012_prop2");
	    if(prop2 == null) {
		return(1);
	    }
	    if(bm.getBundle("jemmy_012_prop2") != prop2) {
		return(1);
	    }
	    if(!bm.getResource("one.two").equals("one_two")) {
		return(1);
	    }
	    if(!bm.getResource("two.two").equals("two_two")) {
		return(1);
	    }
	    if(!bm.getResource("jemmy_012_prop1", "one.two").equals("one_two")) {
		return(1);
	    }
	    if(!bm.getResource("jemmy_012_prop2", "two.two").equals("two_two")) {
		return(1);
	    }
	    if(bm.getResource("jemmy_012_prop1", "two.two") != null) {
		return(1);
	    }
	    if(bm.getResource("jemmy_012_prop2", "one.two") != null) {
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

	    JemmyProperties.setCurrentBundleManager(bm);
	    if(!JemmyProperties.getCurrentResource("one.two").equals("one_two")) {
		JemmyProperties.setCurrentBundleManager(oldbm);
		return(1);
	    }
	    if(!JemmyProperties.getCurrentResource("two.two").equals("two_two")) {
		JemmyProperties.setCurrentBundleManager(oldbm);
		return(1);
	    }
	    if(!JemmyProperties.getCurrentResource("jemmy_012_prop1", "one.two").equals("one_two")) {
		JemmyProperties.setCurrentBundleManager(oldbm);
		return(1);
	    }
	    if(!JemmyProperties.getCurrentResource("jemmy_012_prop2", "two.two").equals("two_two")) {
		JemmyProperties.setCurrentBundleManager(oldbm);
		return(1);
	    }

	} catch(TestCompletedException e) {
	    JemmyProperties.setCurrentBundleManager(oldbm);
	    throw(e);
	} catch(Exception e) {
	    JemmyProperties.setCurrentBundleManager(oldbm);
	    throw(new TestCompletedException(1, e));
	}
	return(0);
    }
}
