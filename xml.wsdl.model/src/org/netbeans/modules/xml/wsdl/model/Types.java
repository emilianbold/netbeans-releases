/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * Types.java
 *
 * Created on November 11, 2005, 1:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Schema;

/**
 *
 * @author rico
 * Represents the types section of the WSDL
 */
public interface Types extends WSDLComponent {
     public Collection<Schema> getSchemas();
}
