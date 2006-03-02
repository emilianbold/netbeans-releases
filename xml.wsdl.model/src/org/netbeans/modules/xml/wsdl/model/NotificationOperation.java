/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

/**
 * Represents notification operation type.
 *
 * @author Nam Nguyen
 */
public interface NotificationOperation extends Operation {
    void setOutput(Output output);
    Output getOutput();
}
