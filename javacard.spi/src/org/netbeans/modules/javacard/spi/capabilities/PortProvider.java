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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.spi.capabilities;

import java.util.Set;
import org.netbeans.modules.javacard.spi.ICardCapability;

/**
 * Capability of a Card to consume ports on some host and provide them
 * to the IDE for use.  Expected to be found as a capability of any Card
 * which can be interacted with through HTTP.
 * <p/>
 * Used to avoid port conflicts when defining new virtual Cards for the RI,
 * and by other services (such as the APDU Shell, and listing device contents)
 * to determine what port to connect to.
 *
 * @author Tim Boudreau
 */
public interface PortProvider extends ICardCapability {
    /**
     * Get all ports this card may need to use
     * @return A set of TCP/IP ports
     */
    public Set<Integer> getClaimedPorts();
    /**
     * Get all ports this card is currently using.  If the card is not
     * running, this should be an empty set.
     * @return A set of TCP/IP ports
     */
    public Set<Integer> getPortsInUse();
    /**
     * Get the host on which these ports are open
     * @return A host name, usually localhost or 127.0.0.1 but remote cards are
     * possible
     */
    public String getHost();
    /**
     * Get a port with a specific role
     * @param role The role
     * @return A port, or -1 if not supported
     */
    public int getPort(PortKind role);
}
