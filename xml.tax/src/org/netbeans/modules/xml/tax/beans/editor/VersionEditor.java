/*
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
package org.netbeans.modules.xml.tax.beans.editor;

/**
 *
 * @author  Vladimir Zboril
 * @author  Libor Kramolis
 * @version 0.2
 */
public class VersionEditor extends NullChoicePropertyEditor {

    /** */
    private static String[] items;


    //
    // init
    //

    /** Creates new VersionEditor */
    public VersionEditor () {
        super (getItems());
    }


    //
    // itself
    //

    /**
     */
    public static String[] getItems () {
        if ( items == null ) {
            items = new String[] { DEFAULT_NULL, "1.0" }; // NOI18N
        }
        return items;
    }

}
