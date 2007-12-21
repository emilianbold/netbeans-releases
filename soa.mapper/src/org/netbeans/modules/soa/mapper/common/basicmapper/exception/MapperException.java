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

package org.netbeans.modules.soa.mapper.common.basicmapper.exception;

/**
 * <p>
 *
 * Title: </p> MapperException <p>
 *
 * Description: </p> MapperException indicates there is error occurs when mapper
 * performs internal function.<p>
 *
 * @author    Un Seng Leong
 * @created   July 22, 2003
 */

public class MapperException extends Exception {

    /**
     * Constructor for the MapperException object with the specified message.
     *
     * @param msg  the message of this exception
     */
    public MapperException(String msg) {
        super(msg);
    }

    /**
     * Constructor for the MapperException object with the specified message and
     * the cause of the exception.
     *
     * @param msg    the message of this exception
     * @param cause  the cause exception of this exception.
     */
    public MapperException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
