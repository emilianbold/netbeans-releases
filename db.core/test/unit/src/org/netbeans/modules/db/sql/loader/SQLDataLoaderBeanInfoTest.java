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

package org.netbeans.modules.db.sql.loader;

import junit.framework.*;
import java.beans.*;

/**
 *
 * @author Andrei Badea
 */
public class SQLDataLoaderBeanInfoTest extends TestCase {
    
    public SQLDataLoaderBeanInfoTest(String testName) {
        super(testName);
    }

    public void testGetIconIssue67671() {
        SQLDataLoaderBeanInfo instance = new SQLDataLoaderBeanInfo();
        assertNotNull(instance.getIcon(BeanInfo.ICON_COLOR_16x16));
        assertNotNull(instance.getIcon(BeanInfo.ICON_COLOR_32x32));
    }
}
