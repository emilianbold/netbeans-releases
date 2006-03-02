/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

/**
 * Represents request-response operation type.
 *
 * @author Nam Nguyen
 */

public interface RequestResponseOperation extends Operation {
    void setInput(Input in);
    void setOutput(Output out);
    Input getInput();
    Output getOutput();
}
