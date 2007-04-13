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

package org.netbeans.modules.tasklist.filter;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.tasklist.impl.ScannerDescriptor;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;

/**
 *
 * @author S. Aubrecht
 */
public final class TaskFilter {
    
    public static final TaskFilter EMPTY = new TaskFilter( Util.getString( "no-filter" ) ); //NOI18N
    
    private String name;
    private KeywordsFilter keywords;
    private TypesFilter types;
    
    TaskFilter( String name ) {
        this.name = name;
    }
    
    TaskFilter() {
    }
    
    private TaskFilter( TaskFilter src ) {
        this.name = src.name;
        keywords = null == src.keywords ? null : (KeywordsFilter)src.keywords.clone();
        types = null == src.types ? null : (TypesFilter)src.types.clone();
    }
     
    public boolean accept( Task task ) {
        return null == keywords ? true : keywords.accept( task );
    }
    
    public boolean isEnabled( FileTaskScanner scanner ) {
        return null == types ? true : types.isEnabled( ScannerDescriptor.getType( scanner ) );
    }
    
    public boolean isEnabled( PushTaskScanner scanner ) {
        return null == types ? true : types.isEnabled( ScannerDescriptor.getType( scanner ) );
    }
    
    public boolean isTaskCountLimitReached( int currentTaskCount ) {
        return null == types ? false : types.isTaskCountLimitReached( currentTaskCount );
    }
    
    public String getName() {
        return name;
    }
    
    void setName( String newName ) {
        this.name = newName;
    }
    
    KeywordsFilter getKeywordsFilter() {
        return keywords;
    }
    
    void setKeywordsFilter( KeywordsFilter f ) {
        this.keywords = f;
    }
    
    TypesFilter getTypesFilter() {
        return types;
    }
    
    void setTypesFilter( TypesFilter f ) {
        this.types = f;
    }
    
    @Override
    public Object clone() {
        return new TaskFilter( this );
    } 
    
    @Override
    public String toString() {
        return name;
    }
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        name = prefs.get( prefix+"_name", "Filter" ); //NOI18N //NOI18N
        if( prefs.getBoolean( prefix+"_types", false ) ) { //NOI18N
            types = new TypesFilter();
            types.load( prefs, prefix+"_types" ); //NOI18N
        } else {
            types = null;
        }
        
        if( prefs.getBoolean( prefix+"_keywords", false ) ) { //NOI18N
            keywords = new KeywordsFilter();
            keywords.load( prefs, prefix+"_keywords" ); //NOI18N
        } else {
            keywords = null;
        }
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.put( prefix+"_name", name ); //NOI18N
        
        if( null != types ) {
            prefs.putBoolean( prefix+"_types", true ); //NOI18N
            types.save( prefs, prefix+"_types" ); //NOI18N
        } else {
            prefs.putBoolean( prefix+"_types", false ); //NOI18N
        }
        
        if( null != keywords ) {
            prefs.putBoolean( prefix+"_keywords", true ); //NOI18N
            keywords.save( prefs, prefix+"_keywords" ); //NOI18N
        } else {
            prefs.putBoolean( prefix+"_keywords", false ); //NOI18N
        }
    }
} 
