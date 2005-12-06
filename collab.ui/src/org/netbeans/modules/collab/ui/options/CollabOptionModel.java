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
