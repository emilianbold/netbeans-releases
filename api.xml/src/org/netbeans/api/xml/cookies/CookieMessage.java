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

package org.netbeans.api.xml.cookies;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Extensible and immutable <code>CookieObserver</code> message.
 * Look at particular cookie what detail subclasses it supports.
 *
 * @author  Petr Kuzel
 * @since   0.8
 */
public final class CookieMessage {

    // details
    private final Lookup details;

    // localized message
    private final String message;

    // message level
    private final int level;

    /**
     * Receive a localized message not tied with the processing problems.
     */
    public static final int INFORMATIONAL_LEVEL = 0;

    /**
     * Receive notification of a warning.
     */            
    public static final int WARNING_LEVEL = 1;

    /**
     * Receive notification of a recoverable error.
     */        
    public static final int ERROR_LEVEL = 2;

    /**
     * Receive notification of a non-recoverable error.
     */            
    public static final int FATAL_ERROR_LEVEL = 3;

    /**
     * Create new informational level message.
     * @param message Localized message.
     */
    public CookieMessage(String message) {
        this( message, INFORMATIONAL_LEVEL, null);
    }

    /**
     * Create new message.
     * @param message Localized message.
     * @param level Message level.
     */        
    public CookieMessage(String message, int level) {
        this( message, level, null);
    }

    /**
     * Create new informational level message with structured detail.
     * @param message Localized message.
     * @param detail Structured detail attached to the message.
     */
    public CookieMessage(String message, Object detail) {
        this( message, INFORMATIONAL_LEVEL, detail);
    }
    
    /**
     * Create new message with structured detail.
     * @param message Localized message.
     * @param level Message level.
     * @param detail Structured detail attached to the message.
     */        
    public CookieMessage(String message, int level, Object detail) {        
        this(message, level, Lookups.singleton(detail));
    }

    /**
     * Create new message with structured detail.
     * @param message Localized message.
     * @param level Message level.
     * @param details Lookup holding structured details.
     */        
    public CookieMessage(String message, int level, Lookup details) {
        if (message == null) throw new NullPointerException();
        if (level < INFORMATIONAL_LEVEL || level > FATAL_ERROR_LEVEL)
            throw new IllegalArgumentException();

        this.message = message;
        this.level = level;
        this.details = details == null ? Lookup.EMPTY : details;
    }
    
    
    /**
     * @return Localized message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return Message level.
     */        
    public final int getLevel() {
        return level;
    }

    /**
     * Query for structured detail attached to the message.
     * @param  klass Requested detail subclass.
     * @return Instance of requested structured detail or <code>null</code>.
     */
    public Object getDetail(Class klass) {
        return details.lookup(klass);
    }

    /**
     * Query for structured details attached to the message.
     * @return Lookup of attached structured details.
     */    
    public Lookup getDetails() {
        return details;
    }
}
