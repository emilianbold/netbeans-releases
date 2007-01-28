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
/*
 * JsfDataObjectException.java
 *
 * Created on May 23, 2005, 5:35 PM
 */

package org.netbeans.modules.visualweb.project.jsf.api;


import java.io.IOException;


/**
 * Exception depicting a problem in detecting 'fake' Jsf Data Object, which
 * in fact consist from Jsf Jsp and Jsf Java Data Objects.
 *
 * @author  Peter Zavadsky
 */
public class JsfDataObjectException extends IOException {

    /** Creates a new instance of JsfDataObjectException */
    public JsfDataObjectException() {
        super();
    }

    public JsfDataObjectException(String message) {
        super(message);
    }

}
