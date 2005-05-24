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
 * FieldHelper.java
 *
 * Created on October 5, 2002, 6:20 PM
 */
package org.netbeans.modules.j2ee.sun.sunresources.beans;



/**
 *
 * @author  shirleyc
 */
public class FieldGroupHelper {
 

    public static FieldGroup getFieldGroup(Wizard wiz, String groupName) {
        FieldGroup[] groups = wiz.getFieldGroup();
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].getName().equals(groupName))
                return groups[i];
        }
        return null;
    }
}
