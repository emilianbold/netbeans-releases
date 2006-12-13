/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsdl.config.api;

/**
 *
 * @author Peter Williams
 */
public interface Service extends CommonDDBean {
    public void setName(java.lang.String value);

    public java.lang.String getName();

    public void setTargetNamespace(java.net.URI value);

    public java.net.URI getTargetNamespace();

    public void setTypeNamespace(java.net.URI value);

    public java.net.URI getTypeNamespace();

    public void setPackageName(java.lang.String value);

    public java.lang.String getPackageName();

    public void setInterface(int index, Interface value);

    public Interface getInterface(int index);

    public int sizeInterface();

    public void setInterface(Interface[] value);

    public Interface[] getInterface();

    public int addInterface(Interface value);

    public int removeInterface(Interface value);

    public Interface newInterface();

    public void setTypeMappingRegistry(TypeMappingRegistry value);

    public TypeMappingRegistry getTypeMappingRegistry();

    public TypeMappingRegistry newTypeMappingRegistry();

    public void setHandlerChains(HandlerChains value);

    public HandlerChains getHandlerChains();

    public HandlerChains newHandlerChains();

    public void setNamespaceMappingRegistry(NamespaceMappingRegistry value);

    public NamespaceMappingRegistry getNamespaceMappingRegistry();

    public NamespaceMappingRegistry newNamespaceMappingRegistry();

}
