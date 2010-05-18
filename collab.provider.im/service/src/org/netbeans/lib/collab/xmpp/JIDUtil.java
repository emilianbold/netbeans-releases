/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.lib.collab.xmpp;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.JIDFormatException;
import org.netbeans.lib.collab.xmpp.util.LegacyJIDUtil;
import org.netbeans.lib.collab.util.StringUtility;

/**
 *
 */
public class JIDUtil {
    
    private static final Provider provider;
    static{
        Provider p = null;
        try{
            String clazz = System.getProperty(StringUtility.UTIL_PROVIDER, 
                    StringUtility.LEGACY_PROVIDER);
            Class clz = Class.forName(clazz);
            p = (Provider)clz.newInstance();
        }catch(Exception cnfEx){
            // ignore ...
        }catch(NoClassDefFoundError cdfEr){
            // ignore...
        }
        if (null == p) p = new LegacyJIDUtil();
        provider = p;
    }
    
    public interface Provider{
        public JID encodedJID(String s);
        String decodedJID(JID jid);

        JID encodedJID(JID jid);
        String decodedJID(String s);
        JID encodedJID(String node, String domain, String resource);





        String decodedResource(JID jid);

        String getBareJIDString(String s);

        String encodedNode(String node);
        String decodedNode(String node);
        String decodedNode(JID jid);

        String quoteSpecialCharacters(String in);
        String unquoteSpecialCharacters(String in);


        String getLocalPartFromAddress(String in);
        String appendDomainToAddress(String in, String defaultDomain);
        
        String encodedString(String s);
        String decodedString(String s);

        String decodedDomain(String domain);
        String decodedDomain(JID jid);
        String getDomainFromAddress(String in, String defaultDomain);
        boolean hasDomain(String in);
        String encodedDomain(String domain);
    }
    
    public static Provider getProvider(){
        return provider;
    }
    
    
    static public JID encodedJID(String s) {
        return provider.encodedJID(s);
    }
    
    static public JID encodedJID(JID jid) {
        return provider.encodedJID(jid);
    }
    
    static public JID encodedJID(String node, String domain, String resource) {
        return provider.encodedJID(node, domain, resource);
    }
    
    static public String decodedJID(JID jid) {
        return provider.decodedJID(jid);
    }
    
    static public String decodedJID(String s) {
        return provider.decodedJID(s);
    }
    
    static public String decodedNode(JID jid) {
        return provider.decodedNode(jid);
    }
    
    public static String decodedNode(String node){
        return provider.decodedNode(node);
    }

    public static String encodedNode(String node){
        return provider.encodedNode(node);
    }
    
    static public String decodedDomain(JID jid) {
        return provider.decodedDomain(jid);
    }
    
    static public String decodedResource(JID jid) {
        return provider.decodedResource(jid);
    }
    
    public static String getBareJIDString(String s){
        return provider.getBareJIDString(s);
    }

    public static String encodedString(String s){
        return provider.encodedString(s);
    }

    public static String decodedString(String s){
        return provider.decodedString(s);
    }
}


