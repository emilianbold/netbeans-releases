/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

/**
 * Represents solicit-response operation type.
 *
 * @author Nam Nguyen
 */

public interface SolicitResponseOperation extends Operation {
    void setOutput(Output output);
    void setInput(Input input);
    Output getOutput();
    Input getInput();
}
