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

import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

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
     * @param msg Message never <code>null</code>.
     */
    public void receive(Message msg);
    
    /**
     * Extensible <code>CookieObserver</code> message.
     * Look at particular cookie what detail subclasses it supports.
     */
    public static final class Message {

        // details
        private InstanceContent instanceContent;
        
        // lookup serving details
        private Lookup instanceLookup;

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
         * @param message localized message
         */
        public Message(String message) {
            if (message == null) throw new NullPointerException();
            this.message = message;
            this.level = INFORMATIONAL_LEVEL;
        }

        /**
         * Create new message.
         * @param message localized message
         * @param level message level
         */        
        public Message(String message, int level) {
            if (message == null) throw new NullPointerException();
            if (level < INFORMATIONAL_LEVEL || level > FATAL_ERROR_LEVEL)
                throw new IllegalArgumentException();
            
            this.message = message;
            this.level = level;            
        }
        
        /**
         * @return localized message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return message level
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
            Lookup lookup = getInstanceLookup();
            if (lookup == null) return null;
            return lookup.lookup(klass);
        }
        
        /**
         * Add new detail to the message.
         * @param detail to be added (never <code>null</code>.
         */
        public void addDetail(Object detail) {
            if (detail == null) throw new NullPointerException();
            InstanceContent content = getInstanceContent();
            content.add(detail);
        }
        
        private synchronized InstanceContent getInstanceContent() {
            if (instanceContent == null) {
                instanceContent = new InstanceContent();
            }
            return instanceContent;
        }
        
        private synchronized Lookup getInstanceLookup() {
            if (instanceContent == null) return null;
            if (instanceLookup == null) {
                instanceLookup = new AbstractLookup(instanceContent);
            }
            return instanceLookup;
        }
    }
    
}
