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

package org.netbeans.modules.web.client.tools.common.dbgp;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import org.netbeans.modules.web.client.tools.common.dbgp.StreamMessage.StreamType;
import org.w3c.dom.Node;


/**
 * @author ads, jdeva
 *
 */
class MessageBuilder {
    private static final String TYPE = "type";       // NOI18N

    private MessageBuilder() {
        // avoid inst-ion
    }

    static Message createStream( Node node ) {
        Node attr = node.getAttributes().getNamedItem( TYPE );
        assert attr!=null;
        String type = attr.getNodeValue();
        if ( StreamType.STDOUT.name().equalsIgnoreCase(type)) {
            return new StreamMessage( node , StreamType.STDOUT );
        }
        else if ( StreamType.STDERR.name().equalsIgnoreCase(type) ) {
            return new StreamMessage( node , StreamType.STDERR );
        }
        else {
            assert false;
            return null;
        }
    }

    static ResponseMessage createResponse( Node node ) {
        try {
            String command = Message.getAttribute(node, ResponseMessage.COMMAND);
            assert command != null;
            Class responseClass = CommandMap.valueOf(command.toUpperCase()).getResponseClass();
            Constructor ctor = responseClass.getDeclaredConstructor(Node.class);
            if (ctor != null) {
                return (ResponseMessage) ctor.newInstance(node);
            }
        } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE, "Unable to construct response message from dbgp XML response", e);
        }
        return null;
    }
}
