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

package org.netbeans.modules.kenai.ui;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.kenai.api.KenaiProjectMember;
import org.netbeans.modules.kenai.collab.chat.ChatNotifications;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
import org.netbeans.modules.kenai.ui.spi.MemberHandle;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class MemberHandleImpl extends MemberHandle {

    private KenaiProjectMember delegate;
    private boolean isOwner;
    public MemberHandleImpl (KenaiProjectMember member, boolean isOwner) {
        this.delegate = member;
        this.isOwner = isOwner;
    }

    @Override
    public String getDisplayName() {
        return delegate.getKenaiUser().getUserName();
    }

    @Override
    public String getName() {
        return delegate.getKenaiUser().getUserName();
    }

    @Override
    public String getRole() {
        KenaiProjectMember.Role r = delegate.getRole();
        String result = null;
        if (r != null) {
            switch (r) {
                case ADMIN: result = NbBundle.getMessage(MemberHandleImpl.class, "Role.Admin"); break;// NOI18N
                case DEVELOPER: result = NbBundle.getMessage(MemberHandleImpl.class, "Role.Developer");break; // NOI18N
                case CONTENT: result = NbBundle.getMessage(MemberHandleImpl.class, "Role.Content");break; // NOI18N
                case OBSERVER: result = NbBundle.getMessage(MemberHandleImpl.class, "Role.Observer");break; // NOI18N
            }
        }
        if (isOwner) {
            result += ", " + NbBundle.getMessage(MemberHandleImpl.class, "Role.Owner");
        }

        return result;
    }

    @Override
    public boolean hasMessages() {
        return ChatNotifications.getDefault().hasNewPrivateMessages(delegate.getKenaiUser().getFQN());
    }

    @Override
    public boolean isOnline() {
        return delegate.getKenaiUser().isOnline();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        delegate.getKenaiUser().addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        delegate.getKenaiUser().removePropertyChangeListener(listener);
    }

    @Override
    public String getFullName() {
        return delegate.getKenaiUser().getFirstName() + " " + delegate.getKenaiUser().getLastName(); // NOI18N
    }

    public int compareTo(MemberHandle o) {
        MemberHandleImpl other = (MemberHandleImpl) o;
        if (this.isOwner) {
            return -1;
        }
        if (other.isOwner) {
            return 1;
        }

        int res = this.delegate.getRole().compareTo(other.delegate.getRole());
        if (res==0)
            return getDisplayName().compareToIgnoreCase(o.getDisplayName());
        return res;
    }

    @Override
    public String getFQN() {
        return getName() + "@" + this.delegate.getKenaiUser().getKenai().getUrl().getHost().toString(); // NOI18N
    }

}
