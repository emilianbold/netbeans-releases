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


package org.netbeans.modules.i18n.java;


import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;

import org.openide.util.MapFormat;


/**
 * This is <code>I18nString</code> for java sources.
 *
 * @author  Peter Zavadsky
 * @author  Petr Kuzel
 */
public class JavaI18nString extends I18nString {

    /** 
     * Arguments used by creation replacing code enclapsulating 
     * in java.util.MessageFormat.format method call. 
     */
    protected String[] arguments;

    /** Creates 'empty' <code>JavaI18nString</code>.*/
    public JavaI18nString(I18nSupport i18nSupport) {
        super(i18nSupport);
    }

    /**
     * Copy contructor.
     */
    protected JavaI18nString(JavaI18nString copy) {
        super(copy);
        if (arguments == null) return;
        this.arguments = (String[]) copy.arguments.clone();
    }
    
    public void become(JavaI18nString i18nString) {
        super.become(i18nString);
        
        JavaI18nString peer = (JavaI18nString) i18nString;
        this.setArguments(peer.arguments);
    }
    
    public Object clone() {
        return new JavaI18nString(this);
    }
    
    /** Getter for property arguments.
     * @return Value of property arguments.
     */
    public String[] getArguments() {
        if(arguments == null)
            arguments = new String[0];
        return arguments;
    }
    
    /** Setter for property arguments.
     * @param arguments New value of property arguments.
     */
    public void setArguments(String[] arguments) {
        String[] oldArguments = arguments;
        this.arguments = arguments;
    }
    
    /** 
     * Add java specific replacing values. 
     */
    protected void fillFormatMap(Map map) {
        map.put("identifier", ((JavaI18nSupport)getSupport()).getIdentifier()); // NOI18N

        // Arguments.
        String[] arguments = getArguments();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("new Object[] {"); // NOI18N
        
        for(int i=0; i<arguments.length; i++) {
            stringBuffer.append(arguments[i]);
            
            if(i<arguments.length - 1)
                stringBuffer.append(", "); // NOI18N
        }
        
        stringBuffer.append("}"); // NOI18N
        
        map.put("arguments", stringBuffer.toString());
    }
    
}
