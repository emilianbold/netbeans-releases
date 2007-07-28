/*
 * BEGIN_HEADER - DO NOT EDIT
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)HttpSoapHeaderFaultValidator.java
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.validation;

import org.netbeans.modules.xml.wsdl.model.Message;

public class MessagePart {

    private final String messageName;
    private final String partName;
    private final int hashCode;

    public MessagePart(Message message, String partName) {
        if (message == null) {
            throw new NullPointerException("null messageName");
        }
        if (partName == null) {
            throw new NullPointerException("null partName");
        }
        if ("".equals(partName)) {
            throw new IllegalArgumentException("blank partName");
        }

        messageName = message.getName();
        this.partName = partName;
        hashCode = messageName.toString().concat(partName).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || !(other instanceof MessagePart)) {
            return false;
        }

        MessagePart that = (MessagePart) other;
        if (!messageName.equals(that.messageName)) {
            return false;
        }

        return partName.equals(that.partName);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
