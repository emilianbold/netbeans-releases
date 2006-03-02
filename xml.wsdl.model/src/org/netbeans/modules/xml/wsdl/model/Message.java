/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
