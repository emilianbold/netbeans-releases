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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.wsdlextensions.jms;

/**
 * JMSMessage
 */
public interface JMSMessage extends JMSComponent {
    
    public static final String ELEMENT_PROPERTIES = "properties";
    public static final String ELEMENT_MAPMESSAGE = "mapmessage";
    
    public static final String ATTR_MESSAGE_TYPE = "messageType";
    public static final String ATTR_TEXTPART = "textPart";
    public static final String ATTR_CORRELATION_ID_PART = "correlationIdPart";
    public static final String ATTR_DELIVERY_MODE_PART = "deliveryModePart";    
    public static final String ATTR_PRIORITY_PART = "priorityPart";
    public static final String ATTR_TYPE_PART = "typePart";
    public static final String ATTR_MESSAGE_ID_PART = "messageIDPart";
    public static final String ATTR_REDELIVERED_PART = "redeliveredPart";
    public static final String ATTR_TIMESTAMP_PART = "timestampPart";
    public static final String ATTR_USE = "use";
    public static final String ATTR_ENCODING_STYLE = "encodingStyle";

    public static final String ATTR_USE_TYPE_LITERAL = "literal";
    public static final String ATTR_USE_TYPE_ENCODED = "encoded";
            
    public String getMessageType();
    public void setMessageType(String val);

    public String getUse();
    public void setUse(String val);
    
    public String getTextPart();
    public void setTextPart(String val);
    
    public String getCorrelationIdPart();
    public void setCorrelationIdPart(String val);

    public String getDeliveryModePart();
    public void setDeliveryModePart(String val);

    public String getPriorityPart();
    public void setPriorityPart(String val);

    public String getTypePart();
    public void setTypePart(String val);

    public String getMessageIDPart();
    public void setMessageIDPart(String val);

    public String getRedeliveredPart();
    public void setRedeliveredPart(String val);
    
    public String getTimestampPart();
    public void setTimestampPart(String val);
    
    public void setJMSEncodingStyle(String val);
    public String getJMSEncodingStyle();    
}
