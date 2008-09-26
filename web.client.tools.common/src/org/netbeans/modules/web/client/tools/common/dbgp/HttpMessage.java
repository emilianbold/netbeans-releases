/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.client.tools.common.dbgp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.web.client.tools.common.launcher.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author joelle
 */
public class HttpMessage extends Message {


//    static final String         ENCODING    = "encoding";   // NOI18N
//    static final String         SIZE        = "size";       // NOI18N
    HttpMessage(Node node) {
        super(node);
    }

    private String id;
    public String getId() {
        if ( id == null ){
            id = getChild(getNode(), "id").getChildNodes().item(0).getNodeValue();
        }
        return id;
    }

    private String timeStamp;
    public String getTimeStamp() {
        //Joelle: We should change this to a Date
        if( timeStamp == null ){
            timeStamp = getChild(getNode(), "timestamp").getFirstChild().getNodeValue();
        }
        return timeStamp;
    }

    private String encoding;
    public String getContentEncoding() {
        if ( encoding == null ){
            Map<String, String> map = getHeader();
            if (map != null) {
                encoding = map.get("Content-Encoding");
            }
        }
        return encoding;

    }
    private String responseText;
    public String getResponseText() {
        if (responseText == null) {
            Node responseTextNode = getChild(getNode(), "responseText");
            if (responseTextNode != null && responseTextNode.getFirstChild() != null) {
                String value = responseTextNode.getFirstChild().getNodeValue();
                if (value != null && !value.equals("null")) {
                    byte[] data = Message.getDecodedBytes(Encoding.BASE64, value);
                    String strEnc = getContentEncoding();
//                if( "gzip".equalsIgnoreCase(strEnc) ){
//                    data = Utils.GUnzipper(data);
//                }
                    //        Message.checkValue(bytes, getSize());
                    responseText = new String(data);
                }
            }
        }
        return responseText;
    }

    private String type;
    public String getType() {
        if (type == null ) {
            type = getChild(getNode(), "type").getFirstChild().getNodeValue();
        }
        return type;
    }

    private String url;
    public String getUrl() {
        if( url == null ){
            url = getChild(getNode(), "url").getFirstChild().getNodeValue();
        }
        return url;
    }

    private String methodType;
    public String getMethodType() {
        if( methodType == null) {
            methodType = getChild(getNode(), "method").getFirstChild().getNodeValue();
        }
        return methodType;
    }

    private String postText;
    public String getPostText() {
        if ( postText == null ){
            if (getChild(getNode(), "postText") != null) {
                postText = getChild(getNode(), "postText").getFirstChild().getNodeValue();
            }
        }
        return postText;
    }

    public boolean isLoadTriggerByUser() {
        String val = getChildValue("load_init");
        if (val != null && !val.equals("0")) {
            return true;
        }
        return false;
    }

    public String getUrlParams() {
        Node node = getChild(getNode(), "urlParams");
        if (node != null) {
            NodeList nodeList = node.getChildNodes();
            // XXX #148305 Fixing NPE.
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getNodeValue();
            }
        }
        return null;

//        Map<String,String> map = Collections.emptyMap();
//        return map;
    }

    public String getChildValue(String attributeName) {
        Node node = getChild(getNode(), attributeName);
        if (node != null) {
            Node childNode = node.getFirstChild();
            if (childNode != null) {
                return childNode.getNodeValue();
            }
        }
        return null;
    }
    // Format of the message is:
    // <sources>
    //   <source fileuri="http://..." />
    //   <source fileuri="http://..." />
    //   :
    // </sources>

    public Map<String, String> getHeader() {
        Node header = getChild(getNode(), "header");
        if (header == null) {
            return Collections.<String, String>emptyMap();
        }

        NodeList nodeList = header.getChildNodes();
        Map<String, String> map = new HashMap<String, String>();

//        while(nextNode != null ){
//            map.put(nextNode.getNodeName(),nextNode.getFirstChild().getNodeValue());
//            nextNode = nextNode.getNextSibling();
//        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Node firstChildNode = node.getFirstChild();
            if (firstChildNode != null) {
                map.put(node.getNodeName(), firstChildNode.getNodeValue());
            }
        }
        return map;
    }
}
