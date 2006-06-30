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
package org.netbeans.tax;

/**
 * Holder for a prefix, URI pair.
 * <p>
 * Default namespace prefix is "".
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TreeNamespace {

    /** It is NOT in any namespace (including default) */
    public static final TreeNamespace NO_NAMESPACE  = new TreeNamespace (null, ""); // NOI18N

    /** */
    public static final TreeNamespace XML_NAMESPACE = new TreeNamespace ("xml", "http://www.w3.org/XML/1998/namespace"); // NOI18N

    /** */
    public static final TreeNamespace XMLNS_NAMESPACE = new TreeNamespace ("xmlns", "http://www.w3.org/2000/xmlns/"); // NOI18N
    
    /** For sticklers. */
    public static final String DEFAULT_NS_PREFIX = ""; // NOI18N
    
    /** */
    private String prefix;
    
    /** */
    private String uri;
    
    
    //
    // init
    //
    
    /**
     * Creates new TreeNamespace.
     * @param prefix namespace prefix or null if no namespace (including default)
     * @param uri string representation of URI
     */
    protected TreeNamespace (String prefix, String uri) {
        if (uri == null) throw new IllegalArgumentException (Util.THIS.getString ("EXC_uri_cannot_be_null"));
        this.prefix = prefix;
        this.uri = uri;
    }
    
    /** Creates new TreeNamespace -- copy constructor. */
    protected TreeNamespace (TreeNamespace namespace) {
        this.prefix = namespace.prefix;
        this.uri    = namespace.uri;
    }
    
    
    //
    // itself
    //
    
    /**
     * @return prefix of null if no namespace
     */
    public String getPrefix () {
        return prefix;
    }
    
    /**
     * @return string representation URI (never null)
     */
    public String getURI () {
        return uri;
    }
    
}
