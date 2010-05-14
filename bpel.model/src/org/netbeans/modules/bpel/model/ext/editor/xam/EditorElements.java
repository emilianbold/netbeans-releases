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
package org.netbeans.modules.bpel.model.ext.editor.xam;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.ext.editor.api.Editor;



/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public enum EditorElements {
    EDITOR( "editor" ),
    CAST( "cast" ),
    PSEUDO_COMP( "pseudoComp" ),
    PREDICATE( "predicate" ),
    NM_PROPERTIES( "nmProperties" ),
    NM_PROPERTY( "nmProperty" ),
    ;

    EditorElements( String name ) {
        this ( name , Editor.EDITOR_NAMESPACE_URI);
    }

    EditorElements( String name , String nsUri ) {
        myTag = name;
        myNS = nsUri;
    }

    /**
     * @return Name of tag.
     */
    public String getName() {
        return myTag;
    }


    /**
     * @return namespace if any
     */
    public String getNamespace() {
        return myNS;
    }

    /**
     * @return QName of tag.
     */
    public QName getQName() {
        if ( getNamespace() == null ) {
            return new QName(Editor.EDITOR_NAMESPACE_URI, getName());
        }
        else {
            return new QName( getNamespace() , getName() );
        }
    }

    /**
     * @return All set of qnames of Editor extensions in BPEL.
     */
    public static Set<QName> allQNames() {
        return QNAMES;
    }

    private final String myTag;
    private final String myNS;
    private static final Set<QName> QNAMES = new HashSet<QName>();

    static {
        for (EditorElements v : values()) {
            QNAMES.add(v.getQName());
        }
    }

}
