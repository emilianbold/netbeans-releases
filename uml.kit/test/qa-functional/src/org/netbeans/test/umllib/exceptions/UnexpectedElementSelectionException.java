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


/*
 * UnexpectedElementSelection.java
 *
 * Created on December 2, 2005, 1:18 PM
 *
 */

package org.netbeans.test.umllib.exceptions;

/**
 *
 * @author Alexandr Scherbatiy
 */
public class UnexpectedElementSelectionException extends UMLCommonException {
    
    
    private Status status;
    private Object element;
    
    /**
     * Creates a new instance of <code>UnexpectedElementSelection</code> without detail message.
     */

    public UnexpectedElementSelectionException() {
        this("");
    }
    
    /**
     * Constructs an instance of <code>UnexpectedElementSelection</code> with the specified detail message.
     * @param msg the detail message.
     */
    public UnexpectedElementSelectionException(String msg) {
        this(msg, Status.UNKNOWN);
    }

    /**
     * 
     * @param msg 
     * @param status 
     */
    public UnexpectedElementSelectionException(String msg, Status status) {
        this(msg, status, null);
    }

    /**
     * 
     * @param msg 
     * @param element 
     */
    public UnexpectedElementSelectionException(String msg, Object element) {
        this(msg, Status.UNKNOWN, element);
    }


    /**
     * 
     * @param msg 
     * @param status 
     * @param element 
     */
    public UnexpectedElementSelectionException(String msg, Status status, Object element) {
        super(msg);
        this.status = status;
        this.element = element;
    }

    
    /**
     * 
     * @return 
     */
    public String getDescription(){
        return getMessage();
    }
    
    
    /**
     * 
     * @return 
     */
    public Status getStatus(){
        return status;
    }
    
    /**
     * 
     * @return 
     */
    public Object getElement(){
        return element;
    }
    
    
    
    public static enum Status {
        UNKNOWN("Any or unknown status."),
        SELECTED("Element is selected."),
        NOTSELECTED("Element is not selected.");        
        
        private String description;
        /**
         * 
         * @param description 
         */
        Status(String description){
            this.description = description;
        }
        
        /**
         * 
         * @return 
         */
        public String getDescription(){
            return description;
        }
    }
}
