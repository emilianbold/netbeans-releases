/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq;

/**
 * MQ Binding Redelivery extensibility element.
 *
 * @author Noel.Ang@sun.com
 */
public interface MQRedelivery extends MQComponent {
    public static final String ATTR_COUNT = "count";
    public static final String ATTR_DELAY = "delay";
    public static final String ATTR_TARGET = "target";

    String getCount();
    void setCount(String count);
    
    String getDelay();
    void setDelay(String delay);
    
    String getTarget();
    void setTarget(String target);
}
