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
package org.netbeans.modules.xslt.model.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.namespace.QName;

import org.netbeans.modules.xslt.model.XslComponent;


/**
 * Enumeration representing XSL specific elements.
 * Please note that almost any XML element could 
 * appear in XSL document and we should provide 
 * access to this element. This enumeration 
 * contains only XSL specific elements ( lile xsl instructions ).  
 *  
 * @author ads
 *
 */
enum XslElements {

    ANALIZE_STRING( "analyze-string" ),                     // NOI18N
    APPLY_IMPORTS( "apply-imports" ),                       // NOI18N
    APPLY_TEMPLATES( "apply-templates" ),                   // NOI18N
    ATTRIBUTE( "attribute" ),                               // NOI18N
    ATTRIBUTE_SET( "attribute-set" ),                       // NOI18N
    CALL_TEMPLATE( "call-template" ),                       // NOI18N
    CHARACTER_MAP( "character-map" ),                       // NOI18N
    CHOOSE( "choose" ),                                     // NOI18N
    COMMENT( "comment" ),                                   // NOI18N
    COPY( "copy" ),                                         // NOI18N
    COPY_OF( "copy-of" ),                                   // NOI18N
    DECIMAL_FORMAT( "decimal-format" ),                     // NOI18N
    DOCUMENT( "document" ),                                 // NOI18N
    ELEMENT( "element" ),                                   // NOI18N
    FALLBACK( "fallback" ),                                 // NOI18N
    FOR_EACH( "for-each" ),                                 // NOI18N
    FOR_EACH_GROUP( "for-each-group" ),                     // NOI18N
    FUNCTION( "function" ),                                 // NOI18N
    IF( "if" ),                                             // NOI18N
    IMPORT( "imoprt" ),                                     // NOI18N
    IMPORT_SCHEMA( "import-schema" ),                       // NOI18N
    INCLUDE( "include" ),                                   // NOI18N
    KEY( "key" ),                                           // NOI18N
    MATCHING_SUBSTRING( "matching-substring" ),             // NOI18N
    MESSAGE( "message" ),                                   // NOI18N
    NAMESPACE( "namespace" ),                               // NOI18N
    NAMESPACE_ALIAS( "namespace-alias" ),                   // NOI18N
    NEXT_MATCH( "next-match" ),                             // NOI18N
    NON_MATCHING_SUBSTRING( "non-matching-substring" ),     // NOI18N
    NUMBER( "number" ),                                     // NOI18N
    OTHERWISE( "otherwise" ),                               // NOI18N
    OUTPUT( "output" ),                                     // NOI18N
    OUTPUT_CHARACTER( "output-character" ),                 // NOI18N
    PARAM( "param" ),                                       // NOI18N
    PERFORM_SORT( "perform-sort" ),                         // NOI18N
    PRESERVE_SPACE( "preserve-space" ),                     // NOI18N
    PROCESSING_INSTRUCTION( "processing-instruction" ),     // NOI18N
    RESULT_DOCUMENT( "result-document" ),                   // NOI18N
    SEQUENCE( "sequence" ),                                 // NOI18N
    SORT( "sort" ),                                         // NOI18N
    STRIP_SPACE( "strip-space" ),                           // NOI18N
    STYLESHEET( "stylesheet"),                              // NOI18N
    TRANSFORM( "transform" ),                               // NOI18N this is the same tag as previous "stylesheet".
    TEMPLATE( "template" ),                                 // NOI18N
    TEXT( "text" ),                                         // NOI18N
    VALUE_OF( "value-of" ),                                 // NOI18N
    VARIABLE( "variable" ),                                 // NOI18N
    WHEN( "when" ),                                         // NOI18N
    WHITH_PARAM( "with-param" )                             // NOI18N
    ;
    
    XslElements( String str ){
        myName = str;
    }
    
    public String getName() {
        return myName;
    }
    
    public QName getQName() {
        return new QName( XslComponent.XSL_NAMESPACE, getName() ); 
    }
    
    public static Set<QName> allQNames() {
        if ( myQNames.get() == null ) {
            Set<QName> set = new HashSet<QName>( values().length );
            for (XslElements element : values() ) {
                set.add( element.getQName() );
            }
            myQNames.compareAndSet( null, set );
        }
        return myQNames.get();
    }
    
    private String myName;
    
    private static AtomicReference<Set<QName>> myQNames = 
        new AtomicReference<Set<QName>>();
}

