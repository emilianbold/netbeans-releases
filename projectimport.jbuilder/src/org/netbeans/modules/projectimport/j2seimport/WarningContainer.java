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

package org.netbeans.modules.projectimport.j2seimport;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

/**
 *
 * @author Radek Matous
 */
public final class WarningContainer  {
    public static final WarningContainer EMPTY = new WarningContainer();
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(WarningContainer.class);
    
    
    private LinkedHashSet allWarnings = new LinkedHashSet(); 

    public boolean add(final String message, final boolean notifyUser) {
        if (this == EMPTY) {
            logger.warning("unexpected call on EMPTY WarningSupport ");//NOI18N
            return false;
        }
        return allWarnings.add(new WarningContainer.Warning(message, notifyUser));
    }
    
    public boolean addAll(final WarningContainer toAdd) {
        if (this == EMPTY) {
            logger.warning("unexpected call on EMPTY WarningSupport ");//NOI18N
            return false;
        }
        
        return this.allWarnings.addAll(toAdd.allWarnings);
    }
    
    
    public Iterator/*<WarningSupport.Warning>*/ getAllWarnings() {
        return allWarnings.iterator();
    }
    
            
    public static final class Warning {
        private String message;
        private boolean userNotification;
        /** Creates a new instance of Warning */
        private Warning(final String message, final boolean notifyUser) {
            this.message = message;
            this.userNotification = notifyUser;
        }
        
        public String getMessage() {
            return message;
        }
        
        public boolean isUserNotification() {
            return userNotification;
        }
        
        
        public String toString() {
            return getMessage();
        }
    }
}


