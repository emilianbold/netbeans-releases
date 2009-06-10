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
package org.netbeans.modules.kenai.collab.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.AbstractListModel;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.MUCUser;

/**
 * List model, which listens on Presence
 * @see Buddy
 * @author Jan Becicka
 */
public class BuddyListModel extends AbstractListModel implements PacketListener {

    /**
     * sorted list of online Buddies
     */
    private final ArrayList<Buddy> usrs = new ArrayList<Buddy>();

    public BuddyListModel(MultiUserChat chat) {
        super();
        Iterator<String> string = chat.getOccupants();
        while (string.hasNext()) {
            usrs.add(new Buddy(chat.getOccupant(string.next()).getJid()));
        }
        Collections.sort(usrs);
    }

    public BuddyListModel(Roster roster) {
        for (RosterEntry re:roster.getEntries()) {
            usrs.add(new Buddy(re.getName()));
        }
        Collections.sort(usrs);
    }

    public int getSize() {
        return usrs.size();
    }

    public Object getElementAt(int i) {
        return usrs.get(i);
    }

    public void processPacket(Packet packet) {
        final Presence presence = (Presence) packet;
        Buddy from = new Buddy(presence.getFrom());
        if (!usrs.contains(from) && presence.getType().equals(Type.available)) {
            usrs.add(new Buddy(((MUCUser) presence.getExtension("http://jabber.org/protocol/muc#user")).getItem().getJid()));
            Collections.sort(usrs);
        }
        if (presence.getType().equals(Type.unavailable)) {
            usrs.remove(from);
        }
        fireContentsChanged(this, 0, usrs.size());
    }
}

