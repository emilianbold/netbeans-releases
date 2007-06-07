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

package org.netbeans.modules.j2ee.ejbverification;

/**
 * This class defines constants that represent various annotation type names
 * defined in EJB specification.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface EJBAPIAnnotations {
    String REMOTE = "javax.ejb.Remote"; //NOI18N
    String LOCAL = "javax.ejb.Local"; //NOI18N

    String STATELESS = "javax.ejb.Stateless"; // NOI18N

    String STATEFUL = "javax.ejb.Stateful"; // NOI18N
    String INIT = "javax.ejb.Init"; // NOI18N
    String REMOVE = "javax.ejb.Remove"; // NOI18N

    String MESSAGE_DRIVEN = "javax.ejb.MessageDriven"; // NOI18N
    String ACTIVATION_CONFIG_PROPERTY = "javax.ejb.ActivationConfigProperty"; // NOI18N

    String REMOTE_HOME = "javax.ejb.RemoteHome"; //NOI18N
    String LOCAL_HOME = "javax.ejb.LocalHome"; //NOI18N

    String TRANSACTION_MANAGEMENT = "javax.ejb.TransactionManagement"; //NOI18N

    //value attribute in annotations with single attribute
    String VALUE = "value"; //NOI18N

    // TODO: Add other ones here including enum types
}
