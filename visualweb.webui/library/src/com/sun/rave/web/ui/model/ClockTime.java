/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
