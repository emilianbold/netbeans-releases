package org.apache.taglibs.standard.examples.i18n;

import java.util.*;

public class Resources_it extends ListResourceBundle {
    private static Object[][] contents;

    static {
	contents = new Object[][] {
	    { "greetingMorning", "Buon giorno!" }
	};
    }

    public Object[][] getContents() {
	return contents;
    }
}
