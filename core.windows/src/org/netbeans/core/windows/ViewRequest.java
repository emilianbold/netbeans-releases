/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows;


/**
 * Class which describes one type of change (in model) which is sent
 * <code>ViewRequestor</code> from <code>Central</code>.
 *
 * @author  Peter Zavadsky
 */
final class ViewRequest {

    /** To distinguish between individual mode or top components. */
    public final Object source;

    public final int type;
    
    public final Object oldValue;
    
    public final Object newValue;
    
    
    /** Creates a new instance of ChangeInfo */
    public ViewRequest(Object source, int type, Object oldValue, Object newValue) {
        this.source   = source;
        this.type     = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
        
}

