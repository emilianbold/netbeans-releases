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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.tmap.model.api;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xslt.tmap.model.impl.InvalidNamespaceException;

/**
 *
 * @author ads
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface ExNamespaceContext extends NamespaceContext {
    /**
     * @return All prefixes that exists in current context.
     */
    Iterator<String> getPrefixes();

    /**
     * Adds new namespace to the context. If namesapce already exists then
     * nothing will happen. One of its prefix will be return. If namespace
     * doesn't exist then it will be added with generated automatically prefix
     * and this prefix will be return. This namespace could be added at any
     * scope that contains current element. This is up to implementation to
     * determine place where namespace will be added.
     * 
     * @param uri
     *            Uri of namespace.
     * @return Prefix for added namespace.
     * @throws InvalidNamespaceException
     *             Will be thrown if uri is not acceptable for namespace.
     */
    String addNamespace( String uri ) throws InvalidNamespaceException;

    /**
     * Adds new namespace to the context. Prefix passed as argument will be used
     * for namespace. If such prefix already exist with other uri then
     * InvalidNamespaceException will be thrown. If namespace declaration with
     * specified prefix already exists then nothing will happen. If such prefix
     * doesn't exist then new namespace declaration will be added. See previus
     * method about scope for adding namespace.
     * 
     * @param prefix Prefix that suppose to be set for namespace uri.
     * @param uri   Namespace uri that will be added in to namespaces declaration.
     * @throws InvalidNamespaceException
     *             Will be thrown if uri is not acceptable for namespace, bad
     *             prefix is specified or prefix already exist.
     */
    void addNamespace( String prefix, String uri )
            throws InvalidNamespaceException;

}
