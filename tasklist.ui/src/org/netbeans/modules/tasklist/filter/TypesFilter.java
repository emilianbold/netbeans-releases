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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.tasklist.impl.ScannerDescriptor;

/**
 *
 * @author S. Aubrecht
 */
class TypesFilter {
    
    private Set<String> disabledProviders = new HashSet<String>();
    private int countLimit = 100;
    
    public TypesFilter() {
    }
    
    private TypesFilter( TypesFilter src ) {
        this.countLimit = src.countLimit;
        this.disabledProviders.addAll( src.disabledProviders );
    }
    
    public boolean isEnabled( String type ) {
        return !disabledProviders.contains( type );
    }
    
    public void setEnabled( String type, boolean enabled ) {
        if( !enabled ) {
            disabledProviders.add( type );
        } else {
            disabledProviders.remove( type );
        }
    }
    
    public boolean isTaskCountLimitReached( int taskCount ) {
        return taskCount >= countLimit;
    }
    
    public void setTaskCountLimit( int limit ) {
        this.countLimit = limit;
    }
    
    public int getTaskCountLimit() {
        return this.countLimit;
    }
    
    public TypesFilter clone() {
        return new TypesFilter( this );
    }
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        countLimit = prefs.getInt( prefix+"_countLimit", 100 ); //NOI18N
        disabledProviders.clear();
        String disabled = prefs.get( prefix+"_disabled", "" ); //NOI18N //NOI18N
        StringTokenizer tokenizer = new StringTokenizer( disabled, "\n" ); //NOI18N
        while( tokenizer.hasMoreTokens() ) {
            disabledProviders.add( tokenizer.nextToken() );
        }
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.putInt( prefix+"_countLimit", countLimit );
        StringBuffer buffer = new StringBuffer();
        for( Iterator<String> type = disabledProviders.iterator(); type.hasNext(); ) {
            buffer.append( type.next() );
            if( type.hasNext() )
                buffer.append( "\n" ); //NOI18N
        }
        prefs.put( prefix+"_disabled", buffer.toString() ); //NOI18N
    }
} 
