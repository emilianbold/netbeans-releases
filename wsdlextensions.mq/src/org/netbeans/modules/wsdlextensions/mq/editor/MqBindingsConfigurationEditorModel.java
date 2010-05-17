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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor;

import java.util.Arrays;

/**
 * MqBindingsFormModel implementation.
 *
 * @author Noel.Ang@sun.com
 * @see {@link MqBindingsFormModel}
 */
class MqBindingsConfigurationEditorModel
        implements MqBindingsFormModel {

    MqBindingsConfigurationEditorModel() {
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getQueueManager() {
        return manager;
    }

    public String getQueue() {
        return queue;
    }

    public String getChannel() {
        return channel;
    }

    public String getPolling() {
        return polling;
    }

    public boolean getIsTransactional() {
        return isTransactional;
    }

    public boolean getIsDefaultBindingOption() {
        return useBindingDefaultOption;
    }

    public boolean getIsOnOpenBindingOption() {
        return useBindingOnOpenOption;
    }

    public boolean getIsNoBindingOption() {
        return useBindingNoneOption;
    }

    public boolean getIsDefaultReadOption() {
        return useReadDefaultOption;
    }

    public boolean getIsExclusiveReadOption() {
        return useReadExclusiveOption;
    }

    public boolean getIsSharedReadOption() {
        return useReadSharedOption;
    }

    public String getCipherSuite() {
        return cipherSuite;
    }

    public String getSslPeerName() {
        return sslPeerName;
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    public String getMessageType() {
        return messageType;
    }

    public boolean getIsSyncpoint() {
        return isSyncpoint;
    }

    public void setHost(String value) {
        host = Utils.safeString(value);
    }

    public void setPort(String value) {
        port = Utils.safeString(value);
    }

    public void setQueue(String value) {
        queue = Utils.safeString(value);
    }

    public void setQueueManager(String value) {
        manager = Utils.safeString(value);
    }

    public void setChannel(String value) {
        channel = Utils.safeString(value);
    }

    public void setPolling(String value) {
        polling = Utils.safeString(value);
    }

    public void setIsTransactional(boolean value) {
        isTransactional = value;
    }

    public void setIsDefaultBindingOption(boolean value) {
        useBindingDefaultOption = value;
    }

    public void setIsOnOpenBindingOption(boolean value) {
        useBindingOnOpenOption = value;
    }

    public void setIsNoBindingOption(boolean value) {
        useBindingNoneOption = value;
    }

    public void setIsDefaultReadOption(boolean value) {
        useReadDefaultOption = value;
    }

    public void setIsExclusiveReadOption(boolean value) {
        useReadExclusiveOption = value;
    }

    public void setIsSharedReadOption(boolean value) {
        useReadSharedOption = value;
    }

    public void setSslPeerName(String value) {
        sslPeerName = Utils.safeString(value);
    }

    public void setCipherSuite(String value) {
        cipherSuite = Utils.safeString(value);
    }

    public void setUsername(String value) {
        username = Utils.safeString(value);
    }

    public synchronized void setPassword(char[] value) {
        password = new char[(value != null ? value.length : 0)];
        if (value != null) {
            System.arraycopy(value, 0, password, 0, value.length);
        }
    }

    public void setMessageType(String value) {
        messageType = Utils.safeString(value);
    }

    public void setIsSyncpoint(boolean value) {
        isSyncpoint = value;
    }

    public void adopt(Form.FormModel model) {
        if ((model instanceof MqBindingsFormModel) && this != model) {
            MqBindingsFormModel data = (MqBindingsFormModel) model;
            setHost(data.getHost());
            setPort(data.getPort());
            setChannel(data.getChannel());
            setQueueManager(data.getQueueManager());
            setQueue(data.getQueue());
            setPolling(data.getPolling());
            setCipherSuite(data.getCipherSuite());
            setSslPeerName(data.getSslPeerName());
            setUsername(data.getUsername());
            
            char[] password = data.getPassword();
            setPassword(password);
            if (password != null) {
                Arrays.fill(password, (char) 0);
            }
            
            setMessageType(data.getMessageType());
            setIsSyncpoint(data.getIsSyncpoint());
            setIsTransactional(data.getIsTransactional());
            setIsDefaultBindingOption(data.getIsDefaultBindingOption());
            setIsNoBindingOption(data.getIsNoBindingOption());
            setIsOnOpenBindingOption(data.getIsOnOpenBindingOption());
            setIsDefaultReadOption(data.getIsDefaultReadOption());
            setIsSharedReadOption(data.getIsSharedReadOption());
            setIsExclusiveReadOption(data.getIsExclusiveReadOption());
        }
    }

    private volatile String host = "";
    private volatile String port = "";
    private volatile String manager = "";
    private volatile String queue = "";
    private volatile String channel = "";
    private volatile String polling = "";
    private volatile String username = "";
    private volatile char[] password = new char[0];
    private volatile String messageType = "";
    private volatile String cipherSuite = "";
    private volatile String sslPeerName = "";
    private volatile boolean isSyncpoint;
    private volatile boolean isTransactional;
    private volatile boolean useBindingDefaultOption;
    private volatile boolean useBindingNoneOption;
    private volatile boolean useBindingOnOpenOption;
    private volatile boolean useReadDefaultOption;
    private volatile boolean useReadExclusiveOption;
    private volatile boolean useReadSharedOption;
}
