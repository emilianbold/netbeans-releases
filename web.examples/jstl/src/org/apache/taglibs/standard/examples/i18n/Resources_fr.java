package org.apache.taglibs.standard.examples.i18n;

import java.util.*;

public class Resources_fr extends ListResourceBundle {
    private static Object[][] contents;

    static {
	contents = new Object[][] {
	    { "greetingMorning", "Bonjour!!" },
	    { "greetingEvening", "Bonsoir!" },
	    { "serverInfo", "Nom/Version du Servlet Container: {0}, "
	                    + "Version Java: {1}" },
	    { "currentTime", "Nous sommes le: {0}" },
	    { "com.acme.labels.cancel", "Annuler" },
	    { "java.lang.ArithmeticException", "division par 0" }
	};
    }

    public Object[][] getContents() {
	return contents;
    }
}
