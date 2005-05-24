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
 * CheckpointAtEndOfMethod.java
 *
 * Created on November 18, 2004, 3:35 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author Nitya Doraisamy
 */
public interface CheckpointAtEndOfMethod extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String METHOD = "Method";	// NOI18N

    public Method [] getMethod ();
    public Method  getMethod (int index);
    public void setMethod (Method [] value);
    public void setMethod (int index, Method  value);
    public int addMethod (Method  value);
    public int removeMethod (Method  value);
    public int sizeMethod ();
    public Method  newMethod ();
}
