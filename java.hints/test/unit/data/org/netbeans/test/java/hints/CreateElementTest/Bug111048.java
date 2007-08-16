package org.netbeans.test.java.hints;

import java.util.HashMap;
import java.util.Map;

public class Bug111048 {

    public Bug111048() {
    }

    public void t() {
	Map m = new HashMap();
	if (m.contains("")) {
	}
	
	if (bb.contains("")) {
	}
	
	if (m.fieldOrClass.equals("")) {
	}
	
	if (bb.fieldOrClass.equals("")) {
	    
	}
    }
}

class bb {
    
}