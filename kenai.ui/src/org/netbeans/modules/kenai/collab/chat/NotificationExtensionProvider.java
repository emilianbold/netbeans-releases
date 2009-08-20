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

import org.netbeans.modules.kenai.api.KenaiNotification;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.netbeans.modules.kenai.api.KenaiService;
import org.xmlpull.v1.XmlPullParser;


public class NotificationExtensionProvider implements PacketExtensionProvider {

    public NotificationExtensionProvider() {
    }

    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //NOI18N
        formatter.setTimeZone(TimeZone.getTimeZone("UTC")); //NOI18N
        Date stamp = formatter.parse(parser.getAttributeValue("", "stamp")); //NOI18N

        String serviceName = parser.getAttributeValue("", "service");
        String author = parser.getAttributeValue("", "user");
        URI uri = new URI(parser.getAttributeValue("", "uri"));
        KenaiService.Type type = KenaiService.Type.forId(parser.getAttributeValue("", "type"));

        int tag = parser.next();
        List<KenaiNotification.Modification> modifications = new ArrayList();
        while (!(tag == XmlPullParser.END_TAG && "modification".equals(parser.getName()))) {
            if ("modification".equals(parser.getName())) {
                String mid = parser.getAttributeValue("", "id");
                String mresource = parser.getAttributeValue("", "resource");
                KenaiNotification.Modification.Type mtype = KenaiNotification.Modification.Type.valueOf(parser.getAttributeValue("", "type").toUpperCase());
                modifications.add(new KenaiNotification.Modification(mresource, mid, mtype));
            }
            tag = parser.next();
        }
        return new NotificationExtension("notification", "jabber:client",
                new KenaiNotification(stamp,type,uri,author,serviceName,modifications));
    }
}