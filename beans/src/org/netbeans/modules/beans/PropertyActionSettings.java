/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Options for setting properties of generating property patterns from fields
 *
 * @author  Petr Suchomel
 */
public class PropertyActionSettings extends SystemOption {

    /** define value for property marking setting Get/Set property */    
    public static final String PROP_ACCESS  = "gen_access";    
    /** define value for property marking setting bound property */    
    public static final String PROP_BOUND   = "gen_bound";
    /** define value for property marking setting constrained property */    
    public static final String PROP_CONSTR  = "gen_constr";
    /** define value for property marking setting indexed property */    
    public static final String PROP_INDEXED = "gen_indexed";
    /** define value for property marking setting using inheritance */    
    public static final String PROP_INHER   = "gen_use_inher";
    /** define value for property marking setting ask before generating */    
    public static final String ASK_BEFORE   = "ask_before";

    
    /** inicialize object
     */    
    protected void initialize () {
        super.initialize ();
        setGenAccess (PropertyPattern.READ_WRITE);
        setGenBound (false);
        setGenConstrained (false);
        setGenIndexed(false);
        setUseInherit(true);
        setAskBeforeGen(false);
    }
    
    /** Human readable class name
     * @return readable name
     */    
    public String displayName () {
        return NbBundle.getMessage(PropertyActionSettings.class, "PROP_Option_Menu");
    }

    /** Return HelpCtx key
     * @return HelpCtx
     */    
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Default instance of this system option, for the convenience of associated classes.
     * @return itself, only one instance
     */
    public static PropertyActionSettings getDefault() {
        return (PropertyActionSettings) findObject(PropertyActionSettings.class, true);
    }

    /** Return setting for generating GET/SET property
     * @return setting for GET/SET property
     */    
    public int getGenAccess () {
        return ((Integer) getProperty (PROP_ACCESS)).intValue();
    }

    /** Sets setting for generating GET/SET property
     * @param access setting for GET/SET property
     */    
    public void setGenAccess (int access) {
        putProperty (PROP_ACCESS, new Integer (access), true);        
    }

    /** Return setting for generating bound event
     * @return setting for bound event
     */    
    public boolean isGenBound () {
        return ((Boolean) getProperty (PROP_BOUND)).booleanValue ();
    }

    /** Sets setting for generating bound event
     * @param bound setting for bound event
     */    
    public void setGenBound (boolean bound) {
        putProperty (PROP_BOUND, new Boolean (bound), true);
    }
    
    /** Return setting for generating constrained event
     * @return setting for constrained event
     */    
    public boolean isGenConstrained () {
        return ((Boolean) getProperty (PROP_CONSTR)).booleanValue ();
    }

    /** Set setting for generating constrained event
     * @param constrained setting for constrained event
     */    
    public void setGenConstrained (boolean constrained ) {
        putProperty (PROP_CONSTR, new Boolean (constrained), true);
    }    
    
    /** Return setting for generating indexed property
     * @return setting for indexed property
     */    
    public boolean isGenIndexed () {
        return ((Boolean) getProperty (PROP_INDEXED)).booleanValue ();
    }

    /** Set setting for generating indexed property
     * @param indexed setting for indexed property
     */    
    public void setGenIndexed (boolean indexed ) {
        putProperty (PROP_INDEXED, new Boolean (indexed), true);
    }    

    /** Return setting for generating indexed property
     * @return setting for indexed property
     */    
    public boolean isUseInherit () {
        return ((Boolean) getProperty (PROP_INHER)).booleanValue ();
    }

    /** Set setting for generating indexed property
     * @param indexed setting for indexed property
     */    
    public void setUseInherit (boolean inherit ) {
        putProperty (PROP_INHER, new Boolean (inherit), true);
    }    
    
    /** Return setting for generating indexed property
     * @return setting for indexed property
     */    
    public boolean isAskBeforeGen () {
        return ((Boolean) getProperty (ASK_BEFORE)).booleanValue ();
    }

    /** Set setting for generating indexed property
     * @param indexed setting for indexed property
     */    
    public void setAskBeforeGen (boolean ask ) {
        putProperty (ASK_BEFORE, new Boolean (ask), true);
    }        
}
