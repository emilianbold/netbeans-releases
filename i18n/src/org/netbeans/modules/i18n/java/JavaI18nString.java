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


package org.netbeans.modules.i18n.java;

import java.util.Map;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;

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
        if (copy.arguments == null) {
            return;
        }
        this.arguments = copy.arguments.clone();
    }
    
    public void become(JavaI18nString i18nString) {
        super.become(i18nString);        
        
        JavaI18nString peer = i18nString;
            this.setArguments(peer.arguments);    
        }        
    
    @Override
    public Object clone() {
        return new JavaI18nString(this);
    }
    
    /** Getter for property arguments.
     * @return Value of property arguments.
     */
    public String[] getArguments() {
        if (arguments == null) {
            arguments = new String[0];
        }
        return arguments;
    }
    
    /** Setter for property arguments.
     * @param arguments New value of property arguments.
     */
    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
    
    /** 
     * Add java specific replacing values. 
     */
    @Override
    protected void fillFormatMap(Map<String,String> map) {
        map.put("identifier", ((JavaI18nSupport) getSupport()).getIdentifier()); // NOI18N

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
