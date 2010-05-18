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

/**
 * Model that encompasses the MQ bindings data set that is manipulatable in
 * the WSDL Wizard Binding Extension.
 * 
 * @author Noel.Ang@sun.com
 */
public interface MqBindingsFormModel extends Form.FormModel {
    String getHost();

    String getPort();

    String getQueueManager();

    String getQueue();

    String getChannel();

    String getPolling();

    String getCipherSuite();

    String getSslPeerName();

    String getUsername();

    char[] getPassword();

    String getMessageType();

    boolean getIsTransactional();

    boolean getIsSyncpoint();

    // open options
    boolean getIsDefaultBindingOption();

    boolean getIsOnOpenBindingOption();

    boolean getIsNoBindingOption();

    // read options
    boolean getIsDefaultReadOption();

    boolean getIsExclusiveReadOption();

    boolean getIsSharedReadOption();

    void setHost(String value);

    void setPort(String value);

    void setQueue(String value);

    void setQueueManager(String value);

    void setChannel(String value);

    void setPolling(String value);

    void setSslPeerName(String value);

    void setCipherSuite(String value);

    void setUsername(String value);

    void setPassword(char[] value);

    void setMessageType(String value);

    void setIsTransactional(boolean value);

    void setIsSyncpoint(boolean value);

    // open options
    void setIsDefaultBindingOption(boolean value);

    void setIsOnOpenBindingOption(boolean value);

    void setIsNoBindingOption(boolean value);

    // read options
    void setIsDefaultReadOption(boolean value);

    void setIsExclusiveReadOption(boolean value);

    void setIsSharedReadOption(boolean value);
}
