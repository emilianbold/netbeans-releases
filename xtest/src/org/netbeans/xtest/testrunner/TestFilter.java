/*
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * TestFilter.java
 *
 * Created on April 26, 2001, 3:21 PM
 */

package org.netbeans.xtest.testrunner;

import org.netbeans.junit.Filter;

/**
 *
 * @author <a href="mailto:vitezslav.stejskal@czech.sun.com">Vitezslav Stejskal</a>
 * @version 1.0
 */
public class TestFilter {
    protected String file;
    protected Filter filter;
    
    /** Creates new TestFilter */
    public TestFilter(String file, Filter filter) {
        this.file = file;
        this.filter = filter;
    }

    public String getFile() {
        return file;
    }
    
    public Filter getFilter() {
        return filter;
    }
}
