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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.php.doc.resources.DocCategoriesMarker;
import org.openide.ErrorManager;


/**
 * @author ads
 *
 */
public final class DocumentationRegistry {

    private static final DocumentationRegistry 
                    INSTANCE = new DocumentationRegistry();
    
    private DocumentationRegistry() {
    }
    
    public static DocumentationRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * It is mostly utility method.
     * It retturns all function ids ( they are file names which contains
     * function documentation ).   
     * @return
     */
    public Set<String> getIds() {
        return IDS;
    }
    
    /**
     * @return all available categories. 
     */
    public Collection<CategoryDoc> getCategories(){
        return CATEGORIES.values();
    }
    
    /**
     * @param id identifier of category.
     * @return category by its id.
     */
    public CategoryDoc getCategory( String id ) {
        return CATEGORIES.get( id );
    }
    
    /**
     * Access to functions list via first character in their name.
     * @param ch first letter in name .
     * @return list of functions which name starts from this character 
     */
    public List<FunctionDoc> getFunctionByName( char ch ){
        return FUNCTIONS_HASH[ Character.toLowerCase( ch ) ];
    }
    
    private static void initCategories() {
        CATEGORIES = new HashMap<String,CategoryDoc>();
        String resource = DocCategoriesMarker.getCategoriesIndex();
        InputStream is = DocumentationRegistry.class.getResourceAsStream(resource);
        if (is == null) {
            // XXX build.xml currently does not actually run the doc-gen target for some reason...
            return;
        }
        BufferedReader reader = new BufferedReader( new InputStreamReader( is ));
        String line ;
        try {
            while ( ( line = reader.readLine()) != null ) {
                CategoryDoc category = new CategoryDoc( line );
                CATEGORIES.put( line, category );
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
    
    @SuppressWarnings("unchecked")
    private static void initFunctionDocLists() {
        for ( CategoryDoc categoryDoc : CATEGORIES.values() ) {
            for( FunctionDoc functionDoc : categoryDoc.getFunctions() ){
                String name = functionDoc.getName();
                assert name.length() > 0;
                char ch = Character.toLowerCase( name.charAt( 0 ) );
                List<FunctionDoc> list = FUNCTIONS_HASH[ ch ];
                assert list != null;
                if ( list == EMPTY_FUNCTION_DOC_LIST ){
                    list = new LinkedList<FunctionDoc>();
                    FUNCTIONS_HASH[ ch ] = list;
                }
                list.add( functionDoc );
            }
        }
    }
        
    /**
     * Creates a newly allocated array of the <code>FunctionDoc</code> lists
     * and assigns the <code>EMPTY_FUNCTION_DOC_LIST</code> value to each 
     * element of the array.
     */
    @SuppressWarnings("unchecked")
    private static List<FunctionDoc>[] newFunctionDocHash(int maxValue) {
        List<FunctionDoc>[] hash=  new List[ maxValue +1 ];
        for (int ch=0; ch < hash.length; ch++) {
           hash[ ch ] = EMPTY_FUNCTION_DOC_LIST;
        }
        return hash;
    }

    
    private static Map<String,CategoryDoc> CATEGORIES;
    
    private static Set<String> IDS;
    
    private static final List<FunctionDoc> EMPTY_FUNCTION_DOC_LIST =
            new LinkedList<FunctionDoc>();

    /** 
     * The maximum value of a character that is permissible as the first 
     * character in a PHP identifier.
     * @todo It depends on the PHP grammar. 
     * @see TOKEN:php_identifier in the PHP*.nbs files.
     */
    private static final int MAX_PHP_IDENTIFIER_START = (int)'z';

    private static final List<FunctionDoc>[] FUNCTIONS_HASH =
            newFunctionDocHash(MAX_PHP_IDENTIFIER_START);
    
    static {
        initCategories();
        
        IDS = new HashSet<String>();
        
        for( CategoryDoc  category : CATEGORIES.values() ) {
            IDS.addAll( category.getFunctionsId() );
        }
        
        initFunctionDocLists();
    }

    
}
