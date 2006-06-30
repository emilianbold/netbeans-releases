/*
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
public class TestFilter implements Comparable {
    protected String file;
    protected Filter filter;
    protected int position;

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
    
    public void setPosition (int position) {
        this.position = position;
    }
    
    public int compareTo(Object o) {
        if (position == ((TestFilter)o).position)
            return 0;
        if (position > ((TestFilter)o).position)
            return 1;
        return -1;
    }
    
}
