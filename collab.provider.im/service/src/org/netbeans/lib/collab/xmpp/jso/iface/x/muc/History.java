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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.collab.xmpp.jso.iface.x.muc;

import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.StreamElement;
import java.util.Date;

/**
 * <p>
 * Interface for representing a history.</p>
 *
 *
 * @author Rahul Shah
 *
 */
public interface History extends StreamElement {

    public static final NSI NAME = new NSI("history", MUCQuery.NAMESPACE);

    //Methods
    /**
     * <p>
     * Retrieves the value of <tt>maxchars</tt> attribute. </p>
     *
     * @return The value of maxchars or -1 if maxchars is not present.
     */
    public int getMaxChars();
    
    /**
     * <p>
     * Sets the value of <tt>maxchars</tt> attribute. </p>
     *
     * @param The value of maxchars
     * @throws IllegalArgumentException If the parameter is invalid.
     */
    public void setMaxChars(int n) throws IllegalArgumentException;
        
    /**
     * <p>
     * Retrieves the value of <tt>maxstanzas</tt> attribute. </p>
     *
     * @return The value of maxstanzas or -1 if maxstanzas is not present
     */
    public int getMaxStanzas();
    
    /**
     * <p>
     * Sets the value of <tt>maxstanzas</tt> attribute. </p>
     *
     * @param The value of maxstanzas
     * @throws IllegalArgumentException If the parameter is invalid.
     */
    public void setMaxStanzas(int n) throws IllegalArgumentException;
    
    /**
     * <p>
     * Retrieves the value of <tt>seconds</tt> attribute. </p>
     *
     * @return The value of seconds or -1 if seconds is not present
     */
    public int getSeconds();
    
    /**
     * <p>
     * Sets the value of <tt>seconds</tt> attribute. </p>
     *
     * @param The value of seconds
     * @throws IllegalArgumentException If the parameter is invalid.
     */
    public void setSeconds(int n) throws IllegalArgumentException;
    
    /**
     * <p>
     * Retrieves the value of <tt>since</tt> attribute. </p>
     *
     * @return The value of dateTime or null if since is not present
     */
    public Date getSince();
    
    /**
     * <p>
     * Sets the value of <tt>since</tt> attribute. </p>
     *
     * @param The value of dateTime
     * @throws IllegalArgumentException If the parameter is invalid.
     */
    public void setSince(Date dateTime) throws IllegalArgumentException;
    
}
