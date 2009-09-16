/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.collab.chat;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class FakeRosterGroup implements Comparable<FakeRosterGroup> {
    private MultiUserChat muc;

    FakeRosterGroup(MultiUserChat muc) {
        this.muc = muc;
    }

    public String getName() {
        return StringUtils.parseName(muc.getRoom());
    }

    public Collection<FakeRosterEntry> getEntries() {
        TreeSet<FakeRosterEntry> entries = new TreeSet();
        Iterator<String> i = muc.getOccupants();
        while (i.hasNext()) {
            String name = i.next();
            if (!StringUtils.parseResource(name).equals(Kenai.getDefault().getPasswordAuthentication().getUserName())) {
                entries.add(new FakeRosterEntry(name));
            }
        }
        return entries;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FakeRosterGroup other = (FakeRosterGroup) obj;
        if (this.muc != other.muc && (this.muc == null || !this.muc.equals(other.muc))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.muc != null ? this.muc.hashCode() : 0);
        return hash;
    }

    public int compareTo(FakeRosterGroup o) {
        try {
            String thisName = Kenai.getDefault().getProject(getName()).getDisplayName();
            String otherName = Kenai.getDefault().getProject(o.getName()).getDisplayName();
            return thisName.compareToIgnoreCase(otherName);
        } catch (KenaiException kenaiException) {
            Exceptions.printStackTrace(kenaiException);
        }
        return getName().compareToIgnoreCase(o.getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}
