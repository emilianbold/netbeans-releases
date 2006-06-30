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
 * Immutable representation of <code>qName</code>.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TreeName {

    /**
     * The treeName prefix. For example, the prefix for the treeName "a:foo"
     * is "a".
     */
    final private String prefix;

    /**
     * The treeName name. For example, the name for the treeName "a:foo"
     * is "foo".
     */
    final private String name;


    /**
     * The treeName rawName. For example, the rawName for the treeName "a:foo"
     * is "a:foo".
     */
    final private String rawName;
    
    
    //
    // init
    //
    
    /** Creates new TreeName.
     * @throws InvalidArgumentException
     */
    public TreeName (String prefix, String name) throws InvalidArgumentException {
        checkPrefix (prefix);
        checkName (name);
        
        this.prefix  = prefix;
        this.name    = name;
        this.rawName = getQualifiedName (prefix, name);;
    }
    
    /** Creates new TreeName.
     * @throws InvalidArgumentException
     */
    public TreeName (String rawName) throws InvalidArgumentException {
        checkRawName (rawName);
        
        this.prefix  = getPrefix (rawName);
        this.name    = getName (rawName);
        this.rawName = rawName;
    }
    
    //      /** Creates new TreeName -- copy constructor. */
    //      public TreeName (TreeName name) {
    //  	this.prefix  = name.prefix;
    //  	this.name    = name.name;
    //  	this.rawName = name.rawName;
    //      }
    
    
    //
    // itself
    //
    
    /**
     */
    private static String getPrefix (String rawName) {
        int i = rawName.indexOf (":"); // NOI18N
        
        if (i < 0) {
            return ""; // NOI18N
        } else {
            return rawName.substring (0, i);
        }
    }
    
    /**
     */
    private static String getName (String rawName) {
        int i = rawName.indexOf (":"); // NOI18N
        
        if (i < 0) {
            return rawName;
        } else {
            return rawName.substring (i + 1);
        }
    }
    
    /**
     */
    private static String getQualifiedName (String prefix, String name) {
        if ( "".equals (prefix) ) { // NOI18N
            return name;
        } else {
            return (prefix + ":" + name); // NOI18N
        }
        
    }
    
    /**
     */
    public String getPrefix () {
        return prefix;
    }
    
    /**
     */
    private void checkPrefix (String prefix) throws InvalidArgumentException {
        if ( prefix == null ) {
            throw createInvalidNullArgumentException ();
        }
    }
    
    /**
     */
    public String getName () {
        return name;
    }
    
    /**
     */
    private void checkName (String name) throws InvalidArgumentException {
        if ( name == null ) {
            throw createInvalidNullArgumentException ();
        }
    }
    
    /**
     * Should not it be just getQName() ???
     */
    public String getQualifiedName () {
        return rawName;
    }
    
    /**
     */
    private void checkRawName (String rawName) throws InvalidArgumentException {
        if ( rawName == null ) {
            throw createInvalidNullArgumentException ();
        }
    }
    
    /**
     */
    private InvalidArgumentException createInvalidNullArgumentException () {
        return new InvalidArgumentException
        (Util.THIS.getString ("EXC_invalid_null_value"),
        new NullPointerException ());
    }
    
    /**
     */
    public boolean equals (Object obj) {
        if ( obj instanceof TreeName ) {
            return rawName.equals (((TreeName)obj).rawName);
        }
        return false;
    }
    
    /**
     */
    public int hashCode () {
        return rawName.hashCode ();
    }
    
    /**
     */
    public String toString () {
        return rawName;
    }
    
}
