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
import org.openide.util.NbBundle;

/**
 * Base class for all conditions that have a list of options to choos
 * frome the mode of operation, e.g. {Contains, Equals,..},
 * {Greater, Equal}, {True, NotTrue},etc.
 *
 * @author Tor Norbye
 * @author tl
 */
abstract class OneOfFilterCondition extends FilterCondition {
    
    private String[] options = null;
    private int id; // the selected option from nameKeys
    
    /**
     * Creates a condition with the given set of options and selected option.
     *
     * @param opts the set of options to choose from
     * @param id one of the constants from this class
     */
    public OneOfFilterCondition(String [] opts, int id) {
        this.options = opts;
        this.id = id;
    }
    
    public OneOfFilterCondition(final OneOfFilterCondition rhs) {
        super(rhs);
        this.options = rhs.options;
        this.id = rhs.id;
    }
    
    protected OneOfFilterCondition(String [] opts) {
        this.options = opts;
        this.id = -1;
    }
    
    
    public boolean sameType(FilterCondition fc) {
        return super.sameType(fc) && this.id == ((OneOfFilterCondition)fc).id;
    }
    
    protected String getDisplayName() {
        return NbBundle.getMessage(this.getClass(), options[id]);
    }
    
    protected int getId() { return id;}
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        id = prefs.getInt( prefix+"_optionId", -1 ); //NOI18N
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.putInt( prefix+"_optionId", id ); //NOI18N
    }
}
