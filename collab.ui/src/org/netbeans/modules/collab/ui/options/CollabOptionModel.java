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
package org.netbeans.modules.collab.ui.options;

/**
 *
 * @author catlan
 */
final class CollabOptionModel {

    boolean getPlayAudioNotifications() {
        NotificationSettings notificationSettings = (NotificationSettings) NotificationSettings.findObject
            (NotificationSettings.class, true);

        return notificationSettings.getPlayAudioNotifications().booleanValue();
    }

    void setPlayAudioNotifications(final boolean value) {
        NotificationSettings notificationSettings = (NotificationSettings) NotificationSettings.findObject 
            (NotificationSettings.class, true);
        
        notificationSettings.setPlayAudioNotifications(Boolean.valueOf(value));
    }
            
    boolean getShowPresenceNotifications() {
        NotificationSettings notificationSettings = (NotificationSettings) NotificationSettings.findObject 
            (NotificationSettings.class, true);
        
        return notificationSettings.getShowPresenceNotifications().booleanValue();
    }
            
    void setShowPresenceNotifications(final boolean value) {
        NotificationSettings notificationSettings = (NotificationSettings) NotificationSettings.findObject 
            (NotificationSettings.class, true);
        
        notificationSettings.setShowPresenceNotifications(Boolean.valueOf(value));
    }
    
    Integer getIdleTimeout() {
        CollabSettings settings = (CollabSettings) CollabSettings.findObject 
            (CollabSettings.class, true);
        
        return settings.getIdleTimeout();
    }
    
    void setIdleTimeout(final Integer value) {
        CollabSettings settings = (CollabSettings) CollabSettings.findObject 
            (CollabSettings.class, true);
        
        settings.setIdleTimeout(value);
    }
            
    boolean getAutoApprove() {
        CollabSettings settings = (CollabSettings) CollabSettings.findObject 
            (CollabSettings.class, true);
        
        return settings.getAutoApprove().booleanValue();
    }
            
    void setAutoApprove(final boolean value) {
        CollabSettings settings = (CollabSettings) CollabSettings.findObject 
            (CollabSettings.class, true);
        
        settings.setAutoApprove(Boolean.valueOf(value));
    }
            
    boolean getAutoLogin() {
        CollabSettings settings = (CollabSettings) CollabSettings.findObject 
            (CollabSettings.class, true);
        
        return settings.getAutoLogin().booleanValue();
    }
            
    void setAutoLogin(final boolean value) {
        CollabSettings settings = (CollabSettings) CollabSettings.findObject 
            (CollabSettings.class, true);
        
        settings.setAutoLogin(Boolean.valueOf(value));
    }
            
    boolean getAutoAcceptConversation() {
        CollabSettings settings = (CollabSettings) CollabSettings.findObject 
            (CollabSettings.class, true);
        
        return settings.getAutoAcceptConversation().booleanValue();
    }
            
    void setAutoAcceptConversation(final boolean value) {
        CollabSettings settings = (CollabSettings) CollabSettings.findObject 
            (CollabSettings.class, true);
        
        settings.setAutoAcceptConversation(Boolean.valueOf(value));
    }
    
}
