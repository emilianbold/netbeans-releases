/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

/**
 * Represent one-way operation type
 *
 * @author Nam Nguyen
 */
public interface OneWayOperation extends Operation {
    Input getInput();
    void setInput(Input input);
}
