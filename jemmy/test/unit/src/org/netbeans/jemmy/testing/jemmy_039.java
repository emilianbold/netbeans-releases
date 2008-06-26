package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.util.RegExComparator;

public class jemmy_039 extends JemmyTest {
    String[][] data = {
        {"one", ".n.", "true"},
        {"one", "n",   "false"},
        {"one", ".e",   "false"},
        {"one", ".*e",   "true"},
        {"one", "..*e",   "true"},
        {"one", "...*e",   "true"},
        {"one", "....*e",   "false"},
        {"teen", "te*.", "true"},
        {"seventeen", ".*e*.", "true"},
        {"seventeen", "sevente*.", "true"},
        {"seventeen", ".*ent.*", "true"}
    };
    RegExComparator comparator;
    public int runIt(Object obj) {
        comparator = new RegExComparator();
        for(int i = 0; i < data.length; i++) {
            if(!compare(data[i][0], data[i][1], data[i][2])) {
                return(1);
            }
        }
	return(0);
    }
    private boolean compare(String caption, String match, String value) {
        getOutput().printLine("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        String realValue = comparator.equals(caption, match) ? "true" : "false";
        getOutput().printLine(caption + ", " + match + ", " + realValue);
        boolean result = realValue.equals(value);
        if(!result) {
            getOutput().printLine("Error!!!");
        }
        return(result);
    }
}
