/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.xml.cookies;

/**
 * Cookie observer interface. It can be implemented by cookie clients that
 * are insterested in progress of their request.
 *
 * @author      Petr Kuzel
 * @deprecated  XML Tools API candidate
 * @since       0.3
 */
public interface CookieObserver {

    /**
     * Receive a cookie message. Implementation is not
     * allowed to call directly or indirecly any cookie method
     * from handling code. Handling implementation should be as
     * fast as possible.
     * @param msg Message never <code>null</code>
     */
    public void receive(Message msg);
    
    /**
     * Base for all <code>CookieObserver</code> messages.
     * Look at particular cookie what subclasses it supports.
     */
    public abstract static class Message {

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
         * @return localized message
         */
        public abstract String getMessage();

        /**
         * @return message level
         */        
        public abstract int getLevel();
        
    }
    
}
