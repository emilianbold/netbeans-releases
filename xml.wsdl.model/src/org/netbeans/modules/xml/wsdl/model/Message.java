/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * Message.java
 *
 * Created on November 11, 2005, 1:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author rico
 * Represents a message in the WSDL document.
 */
public interface Message extends ReferenceableWSDLComponent {
    public static final String PART_PROPERTY = "part";
    
    void addPart(Part part);
    void removePart(Part part);
    Collection<Part> getParts();
}
