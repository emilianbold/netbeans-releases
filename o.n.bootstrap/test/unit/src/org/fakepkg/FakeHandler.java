/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.fakepkg;

import java.io.PrintWriter;
import java.util.Map;
import org.netbeans.CLIHandler;

/**
 *
 * @author Jaroslav Tulach
 */
public class FakeHandler extends CLIHandler {
    public static Runnable toRun;
    public static Map chained;
    
    /** Creates a new instance of FakeHandler */
    public FakeHandler() {
        super(WHEN_INIT);
        
        Runnable r = toRun;
        toRun = null;
        
        if (r != null) {
            r.run();
        }
    }

    protected int cli(CLIHandler.Args args) {
        if (chained != null) {
            Integer i = (Integer)chained.get(args);
            return i.intValue();
        }
        return 0;
    }

    protected void usage(PrintWriter w) {
    }
    
}
