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
/*
 * OneOneFinders.java
 *
 * Created on November 18, 2004, 9:53 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface OneOneFinders extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String FINDER = "Finder";	// NOI18N
    
    public Finder[] getFinder();
    public Finder getFinder(int index);
    public void setFinder(Finder[] value);
    public void setFinder(int index, Finder value);
    public int addFinder(Finder value);
    public int removeFinder(Finder value);
    public int sizeFinder();
    public Finder newFinder();
}
