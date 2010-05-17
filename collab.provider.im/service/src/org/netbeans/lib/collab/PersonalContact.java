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

package org.netbeans.lib.collab;

/**
 * a personal contact is essentially a person listed in an address book
 *
 * @since version 0.1
 *
 */
public interface PersonalContact extends PersonalStoreEntry {

    /**
     * get the addresses for a given type of communication
     * addresses are sorted by priority
     * Elements in the list are Strings.
     * @param addressType the type of address
     * @return a sorted list of addresses
     */
    public java.util.List getAddresses(String addressType);

    /**
     * get the highest priority address for a given type of communication
     * Elements in the list are Strings.
     * @param addressType the type of address
     * @return an address
     */
    public String getAddress(String addressType);
    
    /**
     * removes an address
     * @param addressType the type of address
     * @param address the address to remove
     */
    public void removeAddress(String addressType, String address) throws CollaborationException;
    
    /**
     * adds an address or changes its priority
     * @param addressType the type of address
     * @param address the address to remove
     */
    public void addAddress(String addressType, String address, int priority) throws CollaborationException;

    /**
     * Determines if this contact has subscription to the current user
     * @return The subscription state as Defined in PersonalContact
     */
    public int getOutboundSubscriptionStatus();
    
    /**
     * Determines if the current user has subscription to this contact
     * @return The subscription state as Defined in PersonalContact
     */
    public int getInboundSubscriptionStatus();
    
    /**
     * returns the CollaborationPrincipal for this user
     */
    public CollaborationPrincipal getPrincipal();
    
    public static final String EMAIL = "email";
    
    public static final String PHONE = "phone";
    
    public static final String IM = "im";
    
    public static final int SUBSCRIPTION_STATUS_CLOSED = 0;
    
    public static final int SUBSCRIPTION_STATUS_OPEN = 1;
        
    public static final int SUBSCRIPTION_STATUS_PENDING = 2;
}

