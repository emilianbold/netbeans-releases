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
package org.netbeans.modules.extbrowser.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author ads
 *
 */
class Message {

    private static final String MESSAGE = "message";        // NOI18N
    
    enum MessageType {
        INIT,
        RELOAD,
        URLCHANGE,
        CLOSE;
        
        @Override
        public String toString() {
            return super.toString().toLowerCase( Locale.US);
        }
        
        public static MessageType forString( String str ){
            for( MessageType type : values() ){
                if ( type.toString().equals( str )){
                    return type;
                }
            }
            return null;
        }
    }
    
    Message(MessageType type , Map<String,String> map ){
        this.type = type;
        this.map = Collections.unmodifiableMap(map);
    }
    
    public static Message parse( String message ){
        /*
         *  TODO : rewrite this code accurately based on restricted
         *  JSON format application layer protocol or use 
         *  existing general JSON parser  
         */
        int index = message.indexOf('{');
        if ( index != -1 ){
            message = message.substring( index +1);
        }
        index = message.lastIndexOf('}');
        if ( index != -1 ){
            message = message.substring( 0 , index );
        }
        String[] parts = message.split(",");    // NOI18N
        Map<String, String> map = new HashMap<String, String>();
        for (String part : parts) {
            part = part.trim();
            index = part.indexOf(':');
            if ( index == -1 ){
                continue;
            }
            String key = part.substring( 0, index );
            String value = part.substring( index +1);
            map.put( unquote( key ), unquote( value ));
        }
        String type = map.remove(MESSAGE);
        if ( type == null ){
            return null;
        }
        else {
            return new Message(MessageType.forString(type), map);
        }
    }
    
    public MessageType getType(){
        return type;
    }
    
    public String getValue( String key ){
        return map.get(key);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");     // NOI18N
        builder.append( DEFAULT_PRESENTER.getPresentation(MESSAGE));
        builder.append(':');
        builder.append( DEFAULT_PRESENTER.getPresentation( type.toString()));
        for( Entry<String,String> entry : map.entrySet()){
            builder.append(',');
            String key = entry.getKey();
            String value = entry.getValue();
            value = getPresenter(key).getPresentation(value);
            builder.append( DEFAULT_PRESENTER.getPresentation(key));
            builder.append(':');
            builder.append( value );
        }
        builder.append('}');
        return builder.toString();
    }
    
    private ValuePresenter getPresenter( String key ){
        ValuePresenter valuePresenter = VALUE_PRESENTERS.get( key );
        if ( valuePresenter == null ){
            return DEFAULT_PRESENTER;
        }
        else {
            return valuePresenter;
        }
    }
    
    private static String unquote( String str ){
        if ( str.length() == 0 ){
            return str;
        }
        if ( str.charAt(0) =='"'){
            str = str.substring(1);
            if ( str.charAt( str.length()-1 ) == '"'){
                str = str.substring( 0, str.length() -1 );
            }
        }
        return str;
    }
    
    static interface ValuePresenter {
        String getPresentation( String value );
    }
    
    static class DefaultPresenter implements ValuePresenter {
        /* (non-Javadoc)
         * @see org.netbeans.modules.web.common.reload.Message.ValuePresenter#getPresentation(java.lang.String)
         */
        @Override
        public String getPresentation( String value ) {
            StringBuilder builder = new StringBuilder("\"");        // NOI18N
            builder.append( value );
            builder.append('"');
            return builder.toString();
        }
    }
    
    static class NumberPresenter implements ValuePresenter {
        /* (non-Javadoc)
         * @see org.netbeans.modules.web.common.reload.Message.ValuePresenter#getPresentation(java.lang.String)
         */
        @Override
        public String getPresentation( String value ) {
            return value;
        }
    }
    
    private final MessageType type;
    private final Map<String,String> map;
    static final String TAB_ID = "tabId";       // NOI18N
    private static final Map<String,ValuePresenter> VALUE_PRESENTERS = 
        new HashMap<String, Message.ValuePresenter>();
    private static final ValuePresenter DEFAULT_PRESENTER = new DefaultPresenter();
    
    static {
        VALUE_PRESENTERS.put(Message.TAB_ID, new NumberPresenter() );
    }
}
