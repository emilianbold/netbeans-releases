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

package org.netbeans.lib.collab.xmpp.util;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jabberstudio.jso.JID;
import org.netbeans.lib.collab.util.StringUtility;
import org.netbeans.lib.collab.xmpp.JIDUtil;

/**
 *
 * @author mridul
 */
public class XEP106JIDUtil extends JIDUtilProviderImpl {
    
    private final String replaceWhat[] = 
    {   " ",   "\"",    "&",   "'",    "/",    ":",    "<",    ">",    "@",   "\\"};
    private final String replaceWithNoEscape[] = 
    {"\\20", "\\22", "\\26", "\\27", "\\2f", "\\3a", "\\3c", "\\3e", "\\40", "\\5c"};
    private final Pattern needEncodeMatch = createNeedEncodeMatchPattern();
    private final Pattern needDecodeMatch = createNeedDecodeMatchPattern();
    private final Pattern needEncode = createNeedEncodePattern();
    private final Pattern needDecode = createNeedDecodePattern();
    private final Hashtable replaceWhatTable = createReplaceWhatTable();
    private final Hashtable replaceWithTable = createReplaceWithTable();
    

    public JID encodedJID(JID jid) {
        return jid;
    }

    /*
    public String decodedJID(JID jid) {
        return null != jid ? jid.toString() : null;
    }
     */

    public String decodedNode(JID jid) {
        return null != jid ? decodedNode(jid.getNode()) : null;
    }

    public String decodedDomain(JID jid) {
        return null != jid ? jid.getDomain() : null;
    }

    public String decodedResource(JID jid) {
        return null != jid ? jid.getResource() : null;
    }

    /*
    public String quoteSpecialCharacters(String in) {
        return null != in ? encodedNode(in) : null;
    }

    public String unquoteSpecialCharacters(String in) {
        return null != in ? decodedNode(in) : null;
    }
     */

    /*
    public String getDomainFromAddress(String in, String defaultDomain) {
        if (in == null) return null;
        int i = in.lastIndexOf('@');
        if (-1 != i) {
            return in.substring(i+1);
        }
        return defaultDomain;
    }
     */

    /*
    public boolean hasDomain(String in) {
        return null != in && -1 != in.indexOf('@');
    }
     */

    /*
    public String getLocalPartFromAddress(String in) {
        if (null == in) return null;
        int i = in.lastIndexOf('@');
        if (-1 != i) {
            return in.substring(0, i);
        }
        return in;
    }
     */

    /*
    public String appendDomainToAddress(String in, String defaultDomain) {
        if (null == in) return null;
        int i = in.lastIndexOf('@');
        if (-1 != i) {
            return in;
        }
        return in + "@" + defaultDomain;
    }
     */
    
    private String getEncodePattern(){
        return "[ \"&\'/:<>@\\\\]";
    }
    
    private String getDecodePattern(){
        StringBuffer sb = new StringBuffer();
        int count = 0;
        sb.append("(");
        while (count < replaceWithNoEscape.length - 1){
            sb.append("(\\").append(replaceWithNoEscape[count]).append(")|");
            count ++;
        }
        
        sb.append("(\\").append(replaceWithNoEscape[count]).append("))");
        return sb.toString();
    }
    
    private Pattern createNeedEncodePattern(){
        return Pattern.compile(getEncodePattern());
    }
    
    private Pattern createNeedEncodeMatchPattern(){
        return Pattern.compile(".*" + getEncodePattern() + ".*");
    }
    
    private Pattern createNeedDecodePattern(){
        return Pattern.compile(getDecodePattern());
    }
    
    private Pattern createNeedDecodeMatchPattern(){
        return Pattern.compile(".*" + getDecodePattern() + ".*");
    }
    
    private Hashtable createReplaceWithTable(){
        Hashtable retval = new Hashtable();
        int count = 0;
        while (count < replaceWhat.length){
            retval.put(replaceWhat[count], "\\" + replaceWithNoEscape[count]);
            count ++;
        }
        return retval;
    }
    
    private Hashtable createReplaceWhatTable(){
        Hashtable retval = new Hashtable();
        int count = 0;
        while (count < replaceWithNoEscape.length){
            retval.put(replaceWithNoEscape[count], replaceWhat[count]);
            count ++;
        }
        return retval;
    }

    public String decodedNode(String node){
        if (null == node || !needDecodeMatch.matcher(node).matches()){
            return node;
        }
        
        Matcher matcher = needDecode.matcher(node);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()){
            String what = matcher.group();
            String replace = (String)replaceWhatTable.get(what);
            matcher.appendReplacement(sb, replace);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    public String encodedNode(String node){
        if (null == node || !needEncodeMatch.matcher(node).matches()){
            return node;
        }
        
        Matcher matcher = needEncode.matcher(node);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()){
            String replace = matcher.group();
            String what = (String)replaceWithTable.get(replace);
            matcher.appendReplacement(sb, what);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String decodedDomain(String domain) {
        if (null == domain) return null;
        try{
            return (new JID(domain)).toString();
        }catch(Exception ex){
            // ret null ?
            return domain;
        }
    }

    public String encodedDomain(String domain) {
        if (null == domain) return null;
        try{
            return (new JID(domain)).toString();
        }catch(Exception ex){
            // ret null ?
            return domain;
        }
    }

    protected String encodedResource(String resource){
        return resource;
    }
    
    protected String decodedResource(String resource){
        return resource;
    }
}
