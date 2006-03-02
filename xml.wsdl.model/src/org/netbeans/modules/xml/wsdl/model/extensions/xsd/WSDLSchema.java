/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * WSDLSchema.java
 *
 * Created on December 12, 2005, 6:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.xsd;

import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author rico
 */
public interface WSDLSchema extends ExtensibilityElement.Embedder {
    SchemaModel getSchemaModel();
}
