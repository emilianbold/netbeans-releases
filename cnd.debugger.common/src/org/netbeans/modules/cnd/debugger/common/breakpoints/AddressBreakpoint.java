/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.common.breakpoints;

/**
 *
 * @author eu155513
 */
public class AddressBreakpoint extends CndBreakpoint implements Comparable {
    public static final String          PROP_ADDRESS_VALUE = "address"; // NOI18N
    public static final String          PROP_REFRESH = "refresh"; // NOI18N
    
    private String address;

    private AddressBreakpoint(String address) {
        this.address = address;
    }
    
    public static AddressBreakpoint create(String address) {
        return new AddressBreakpoint(address);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String newAddress) {
        String old;
        synchronized (this) {
            if (newAddress.equals(address)) {
                return;
            }
            old = address;
            address = newAddress;
        }
        firePropertyChange(PROP_ADDRESS_VALUE, old, newAddress);
    }

    @Override
    public int getLineNumber() {
        return 1; // to look like line breakpoint
    }
    
    @Override
    public String toString() {
        return "AddressBreakpoint " + address; // NOI18N
    }
    
    public void refresh() {
        firePropertyChange(PROP_REFRESH, null, null);
    }

    public int compareTo(Object o) {
        if (o instanceof AddressBreakpoint) {
            return this.address.compareTo(((AddressBreakpoint)o).address);
        } else {
            return -1;
        }
    }
}
