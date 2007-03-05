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
package com.sun.rave.web.ui.model;
import java.io.Serializable;


public class ClockTime implements Serializable {
    
    
    /** Creates a new instance of ClockTime */
    public ClockTime() {
    }
    
    /**
     * Holds value of property hour.
     */
    private Integer hour;
    
    /**
     * Getter for property hour.
     * @return Value of property hour.
     */
    public Integer getHour() {
        
        return this.hour;
    }
    
    /**
     * Setter for property hour.
     * @param hour New value of property hour.
     */
    public void setHour(Integer hour) {
        if(hour.intValue() > -1 && hour.intValue() < 24) {
            this.hour = hour;
        } else {
            throw new RuntimeException();
        }
    }
    
    /**
     * Holds value of property minute.
     */
    private Integer minute;
    
    /**
     * Getter for property minute.
     * @return Value of property minute.
     */
    public Integer getMinute() {
        
        return this.minute;
    }
    
    /**
     * Setter for property minute.
     * @param minute New value of property minute.
     */
    public void setMinute(Integer minute) {
        if(minute.intValue() > -1 && minute.intValue() < 60) {
            this.minute = minute;
        } else {
            throw new RuntimeException();
        }
    }
    
    public boolean equals(Object obj) {
        if(obj instanceof ClockTime) {
            return (((ClockTime)obj).getHour().equals(hour) &&
                    ((ClockTime)obj).getMinute().equals(minute));
        }
        return false;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer(128);
        buffer.append(this.getClass().getName());
        buffer.append(": ");
        buffer.append(String.valueOf(hour));
        buffer.append(":");
        buffer.append(String.valueOf(minute));
        return buffer.toString();
    }
}
