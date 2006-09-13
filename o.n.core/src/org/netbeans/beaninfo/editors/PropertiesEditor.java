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

package org.netbeans.beaninfo.editors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.beans.PropertyEditorSupport;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import org.netbeans.core.UIExceptions;
import org.openide.util.NbBundle;


/** A property editor for Properties class.
* @author   Ian Formanek
*/
public class PropertiesEditor extends PropertyEditorSupport {

    /** Overrides superclass method. */
    public String getAsText() {
        Object value = getValue();
        
        if(value instanceof Properties) {
            Properties prop = (Properties)value;

            StringBuffer buff = new StringBuffer();
            
            for(Enumeration e = prop.keys(); e.hasMoreElements(); ) {
                if(buff.length() > 0) {
                    buff.append("; "); // NOI18N
                }
                
                Object key = e.nextElement();
                
                buff.append(key).append('=').append(prop.get(key)); // NOI18N
            }
            
            return buff.toString();
        }
        
        return String.valueOf(value); // NOI18N
    }

    /** Overrides superclass method.
     * @exception IllegalArgumentException if <code>null</code> value
     * is passes in or some io problem by converting occured */
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            if(text == null) {
                throw new IllegalArgumentException("Inserted value can't be null."); // NOI18N
            }
            Properties prop = new Properties();
            InputStream is = new ByteArrayInputStream(
                text.replace(';', '\n').getBytes("ISO8859_1") // NOI18N
            );
            prop.load(is);
            setValue(prop);
        } catch(IOException ioe) {
            IllegalArgumentException iae = new IllegalArgumentException (ioe.getMessage());
            String msg = ioe.getLocalizedMessage();
            if (msg == null) {
                msg = MessageFormat.format(
                NbBundle.getMessage(
                    PropertiesEditor.class, "FMT_EXC_GENERIC_BAD_VALUE"), new Object[] {text}); //NOI18N
            }
            UIExceptions.annotateUser(iae, iae.getMessage(), msg, ioe, new Date());
            throw iae;
        }
    }

    public String getJavaInitializationString () {
        return null; // does not generate any code
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new PropertiesCustomEditor (this);
    }

}
