package org.apache.taglibs.standard.examples.i18n;

import java.util.*;

public class Resources extends ListResourceBundle {
    private static Object[][] contents;

    static {
	contents = new Object[][] {
	    { "greetingMorning", "Good Morning!" },
	    { "greetingEvening", "Good Evening!" },
	    { "serverInfo", "Name/Version of Servlet Container: {0}, "
	                    + "Java Version: {1}" },
	    { "currentTime", "Current time: {0}" },
	    { "com.acme.labels.cancel", "Cancel" },
	    { "java.lang.ArithmeticException", "division by 0" }
	};
    }

    public Object[][] getContents() {
	return contents;
    }
}
