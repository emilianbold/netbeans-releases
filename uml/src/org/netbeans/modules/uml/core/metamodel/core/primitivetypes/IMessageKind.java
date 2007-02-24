/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * File       : IMessageKind.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.core.primitivetypes;

/**
 * @author Aztec
 */
public interface IMessageKind
{
    // The message type is unknown.
    public static final int MK_UNKNOWN=-1;

    // A message used to show instance creation.
    public static final int MK_CREATE = 0;

    // A synchronous call of a particular operation.
    public static final int MK_SYNCHRONOUS = 1;

    // An asynchronous call of a particular operation.
    public static final int MK_ASYNCHRONOUS = 2;

    // A resulting message from an operation call.
    public static final int MK_RESULT = 3;
}
