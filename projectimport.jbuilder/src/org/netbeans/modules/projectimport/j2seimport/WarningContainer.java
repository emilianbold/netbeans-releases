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
    
    public boolean isEmpty() {
        return allWarnings.isEmpty();
    }

    public int size() {
        return allWarnings.size();
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


