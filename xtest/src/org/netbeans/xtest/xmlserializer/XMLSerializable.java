/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.xtest.xmlserializer;

/*
 * marker interface - all objects implementing XMLBean interface are
 * XMLBeans - so they can be serialized/deserialized to XML
 */
public interface XMLSerializable {

    ClassMappingRegistry registerXMLMapping();
    
}

