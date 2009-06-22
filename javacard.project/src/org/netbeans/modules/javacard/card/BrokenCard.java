/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.javacard.card;

import org.netbeans.modules.javacard.api.JavacardPlatform;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import org.netbeans.modules.javacard.api.Card;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public final class BrokenCard extends Card {

    public BrokenCard(String name) {
        super(JavacardPlatform.createBrokenJavacardPlatform(name), 
                NbBundle.getMessage(BrokenCard.class, "INVALID_CARD", name)); //NOI18N
    }

    public Set<Integer> getPortsInUse() {
        return Collections.emptySet();
    }

    public Set<Integer> getPortsInActiveUse() {
        return getPortsInUse();
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public String getServerURL() {
        return "http://localhost:8019";
    }

    @Override
    public String getCardManagerURL() {
        return "http://localhost:8019/cardmanager"; //NOI18N
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == BrokenCard.class;
    }

    @Override
    public int hashCode() {
        return 23;
    }

    @Override
    public Condition startServer(boolean forDebug, Object... args) {
        return null;
    }

    @Override
    public void stopServer() {
        //do nothing
    }

    @Override
    public void resumeServer() {
        //do nothing
    }
}
