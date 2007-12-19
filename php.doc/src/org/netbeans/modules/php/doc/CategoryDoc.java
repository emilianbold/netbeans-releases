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
package org.netbeans.modules.php.doc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.php.doc.resources.DocCategoriesMarker;
import org.openide.ErrorManager;


/**
 * Category represent set of functions that are 
 * grouped by type.
 * F.e. category apache specific functions, etc.
 * 
 * @author ads
 *
 */
public class CategoryDoc {
    
    private final static String NAME            = "name";         // NOI18N
    
    private final static String HTML            = ".html";       // NOI18N

    CategoryDoc( String id ) {
        myId = id;
        
        myFunctionsSet = new HashMap<String,FunctionDoc>();
        initName(id); 
        
        initFunctionsIds();
    }


    public String getId() {
        return myId;
    }

    public String getName() {
        return myName;
    }
    
    public Set<String> getFunctionsId(){
        return myIds;
    }
    
    public Collection<FunctionDoc> getFunctions(){
        return Collections.unmodifiableCollection( myFunctionsSet.values());
    }
    
    public FunctionDoc getFunctionDoc( String id ) {
        return myFunctionsSet.get(id);
    }

    private void initName( String id ) {
        String path = DocCategoriesMarker.getCategoryPath(id) +  NAME;
        InputStream stream = CategoryDoc.class.getResourceAsStream(path);
        BufferedReader reader = new BufferedReader( new InputStreamReader( stream));
        try {
            myName = reader.readLine();
        }
        catch (IOException e) {
            ErrorManager.getDefault().notify( e );
        } 
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify( e );
            }
        }
    }
    
    private void initFunctionsIds() {
        String path = DocCategoriesMarker.getMethodsIndex( getId() );
        myFiles = new HashSet<String>();
        myIds = new HashSet<String>();
        if ( CategoryDoc.class.getResource( path ) == null ){
            return;
        }
        InputStream stream = CategoryDoc.class.getResourceAsStream(path);
        BufferedReader reader = new BufferedReader( new InputStreamReader( stream));
        try {
            String line ;
            while ( (line = reader.readLine() ) != null ) {
                myFiles.add( line );
                assert line.endsWith( HTML );
                String id = line.substring( 0, line.length() - HTML.length() );
                myIds.add( id );
                initFunction(id , line );
            }
        }
        catch (IOException e) {
            ErrorManager.getDefault().notify( e );
        } 
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify( e );
            }
        }        
    }
    
    private Set<String> getFiles(){
        return myFiles;
    }
    
    private void initFunction( String id , String file) {
        FunctionDoc doc = new FunctionDoc(getId(), file, id);
        myFunctionsSet.put(id, doc);
    }
    
    private String myId;
    
    private String myName; 
    
    private Set<String> myFiles;
    
    private Set<String> myIds;
    
    private Map<String,FunctionDoc> myFunctionsSet;
}
