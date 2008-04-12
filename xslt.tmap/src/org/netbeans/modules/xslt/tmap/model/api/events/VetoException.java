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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.tmap.model.api.events;

import java.beans.PropertyChangeEvent;

/**
 * This class is intended for fired Exception about wrong change in Model was
 * trying to perform. This action should be rolled back.
 *
 * @author Vitaly Bychkov
 * @author ads
 * @version 1.0
 */
public class VetoException extends Exception {

    private static final long serialVersionUID = 3499029788731463455L;

    public VetoException( String message, PropertyChangeEvent event ) {
        super(message);
        myEvent = event;
    }

    public PropertyChangeEvent getChangeEvent() {
        return myEvent;
    }

    private final PropertyChangeEvent myEvent;

}
