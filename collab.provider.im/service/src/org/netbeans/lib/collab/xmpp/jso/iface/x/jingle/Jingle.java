/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.lib.collab.xmpp.jso.iface.x.jingle;

import java.util.List;
import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.NSI;

/**
 *
 * @author jerry
 */
public interface Jingle extends Extension {

    public static final NSI NAME = new NSI("jingle", "http://www.xmpp.org/extensions/xep-0166.html#ns");
    public static final String ACTION_CONTENT_ACCEPT = "content-accept";
    public static final String ACTION_CONTENT_ADD = "content-add";
    //public static final String ACTION_CONTENT_DECLINE = "content-decline";
    public static final String ACTION_CONTENT_MODIFY = "content-modify";
    public static final String ACTION_CONTENT_REMOVE = "content-remove";
//    public static final String ACTION_DESCRIPTION_ACCEPT = "description-accept";
//    public static final String ACTION_DESCRIPTION_DECLINE = "description-decline";
//    public static final String ACTION_DESCRIPTION_INFO = "description-info";
//    public static final String ACTION_DESCRIPTION_MODIFY = "description-modify";
    public static final String ACTION_SESSION_ACCEPT = "session-accept";
    public static final String ACTION_SESSION_INFO = "session-info";
    public static final String ACTION_SESSION_INITIATE = "session-initiate";
    //public static final String ACTION_SESSION_REDIRECT = "session-redirect";
    public static final String ACTION_SESSION_TERMINATE = "session-terminate";
    //public static final String ACTION_TRANSPORT_ACCEPT = "transport-accept";
    //public static final String ACTION_TRANSPORT_DECLINE = "transport-decline";
    public static final String ACTION_TRANSPORT_INFO = "transport-info";
    //public static final String ACTION_TRANSPORT_MODIFY = "transport-modify";
    public static final int INFO_BUSY = 0;
    public static final int INFO_HOLD = 1;
    public static final int INFO_RINGING = 2;
    public static final int INFO_MUTE = 3;
    
    
    public void setAction(String action) throws IllegalArgumentException;
    public String getAction();
    public void setInitiator(String initiator) throws IllegalArgumentException;
    public String getInitiator();
    public void setResponder(String responder) throws IllegalArgumentException;
    public String getResponder();
    public void setSessionID(String sid);
    public String getSessionID();
    //public void setRedirect(String redir_url);
    //public String getRedirect();
    public void setInfo(int infotype);
    public int getInfo();
    
    public void addContent(String name, String creator);
    public void addDescription(String contentName, JingleAudio desc);
    public List getContentList();
    public JingleAudio getContentDescription(String contentName);

    public void addTransport(String contentName, JingleTransport transport);
    public JingleTransport getContentTransport(String contentName);
}
