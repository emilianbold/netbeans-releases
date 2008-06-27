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

package org.netbeans.core.ui.quicksearch.web;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse raw HTML from Google search
 * 
 * @author S. Aubrecht
 */
class Result {

    private List<Item> items = new ArrayList<Item>(Query.MAX_NUM_OF_RESULTS);

    Result() {
    }
    
    public List<Item> getItems() {
        return items;
    }
    
    void parse( String html ) {
        items.clear();
        try {
            html = new String(html.getBytes(), "UTF-8"); //NOI18N
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Result.class.getName()).log(Level.FINE, null, ex);
        }
        Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"(.*?)\"[^>]*>(.*?)</a>", //NOI18N
                Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
        Matcher m = p.matcher(html);
        while( m.find() ) {
            String url = m.group(1);
            String title = m.group(2);
            if( url.startsWith("/") ) //NOI18N
                continue; 
            if( url.contains("google.com") ) //NOI18N
                continue;
            title = "<html>" + title; //NOI18N
            Item si = new Item(url, title, null);
            items.add( si );
        }
    }
}
