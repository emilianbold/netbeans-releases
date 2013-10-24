/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.spi;

import java.util.Date;

/**
 * Provides access to scheduling data for a given task so that they can by used 
 * by the Tasks Dashboard facilities - filtering or grouping by schedule date.
 * <p>
 * It is up to the particular implementation if the values eventually match with  
 * corresponding remote repository fields or if they are merely handed 
 * locally as user private.
 * </p>
 * <p>
 * Note that an implementation of this interface is not mandatory for a 
 * NetBeans bugtracking plugin. 
 * <p>
 * @author Tomas Stupka
 * @param <I> the implementation specific issue type
 */
public interface IssueSchedulingProvider<I> {
        
    /**
     * Sets the due date
     * 
     * @param i
     * @param date 
     */
    public void setDueDate(I i, Date date);

    /**
     * Sets the schedule date. 
     * 
     * @param i
     * @param date 
     */
    public void setSchedule(I i, IssueScheduleInfo date);

    /**
     * Sets the estimate in hours
     * 
     * @param i
     * @param hours 
     */
    public void setEstimate(I i, int hours); 

    /**
     * Returns the due date
     * 
     * @param i
     * @return 
     */
    public Date getDueDate(I i);

    /**
     * Returns the schedule date
     * 
     * @param i
     * @return 
     */
    public IssueScheduleInfo getSchedule(I i);

    /**
     * Returns the estimate in hours
     * 
     * @param i
     * @return 
     */
    public int getEstimate(I i); 
    
}
    
