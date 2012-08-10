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
package org.netbeans.modules.web.client.rest.wizard;

import java.util.Locale;
import java.util.Map;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.client.rest.wizard.JSClientGenerator.HttpRequests;


/**
 * @author ads
 *
 */
class RouterGenerator {
    
    RouterGenerator(StringBuilder routers, String name ){
        myRouters = routers;
        myRouterName = name;
    }

    void generateRouter( TypeElement entity, String path,
            String collectionPath, Map<HttpRequests, String> httpPaths,
            Map<HttpRequests, Boolean> useIds, CompilationController controller, 
            ModelGenerator modelGenerator )
    {
        myRouters.append("var ");                                         // NOI18N
        myRouters.append(myRouterName);
        myRouters.append(" = Backbone.Router.extend({\n");                // NOI18N
        
        boolean hasCollection = collectionPath != null; 
        String modelVar = modelGenerator.getModelName().toLowerCase(Locale.ENGLISH);
        /*
         *  Fill routes
         */
        // default route used on page loading 
        myRouters.append("routes:{\n");                                   // NOI18N
        if ( hasCollection ){
            myRouters.append("'':'list'");                                // NOI18N
        }
        else {
            myRouters.append("'':'details'");                             // NOI18N
        }
        // #new route if there is a corresponding POST request in the REST
        if ( httpPaths.get( HttpRequests.POST) != null ){
            myRouters.append(",\n'new':'create'\n");                      // NOI18N
        }
        // #id route if REST has a method for collection
        if ( hasCollection ){
            myRouters.append(",\n':id':'details'\n");                     // NOI18N
        }
        myRouters.append("},\n");                                         // NOI18N
        
        // CTOR ( initialize ) function assign CreateView for "tpl-create" template
        myRouters.append("initialize:function(){\n");                     // NOI18N
        myRouters.append("var self = this;\n");                           // NOI18N
        myRouters.append("$('#");                                         // NOI18N
        myRouters.append(getHeaderId());
        myRouters.append("').html(new views.CreateView({\n");             // NOI18N
        myRouters.append("// tpl-create is template identifier for 'create' block\n");// NOI18N
        myRouters.append("templateName :'#");                             // NOI18N
        myRouters.append(getCreateTemplate());
        myRouters.append("',\n");                                         // NOI18N
        myRouters.append("navigate: function(){\n");                      // NOI18N
        myRouters.append("self.navigate('new', true);\n}\n");             // NOI18N
        myRouters.append("}).render().el);\n},\n");                       // NOI18N
        
        if ( hasCollection ){
            mySideBarId = "sidebar";                                      // NOI18N
            myRouters.append("list:function () {\n");                     // NOI18N
            myRouters.append("this.collection = new models.");            // NOI18N
            myRouters.append(modelGenerator.getCollectionModelName());
            myRouters.append("();\nvar self = this;\n");                  // NOI18N
            myRouters.append("this.collection.fetch({\n");                // NOI18N
            myRouters.append("success:function () {\n");                  // NOI18N
            myRouters.append("self.listView = new views.ListView({\n");   // NOI18N
            myRouters.append("model:self.collection,\n");
            StringBuilder builder = new StringBuilder("tpl-");            // NOI18N
            builder.append(modelVar);
            builder.append("-list-item");                                 // NOI18N
            myListItemTemplate = builder.toString();
            myRouters.append("// ");                                      // NOI18N
            myRouters.append(myListItemTemplate);
            myRouters.append("is template identifier for item\n");        // NOI18N
            myRouters.append("templateName : '#");                        // NOI18N
            myRouters.append(myListItemTemplate);
            myRouters.append("'\n});\n");                                 // NOI18N
            myRouters.append("$('#");                                      // NOI18N
            myRouters.append(mySideBarId);
            myRouters.append("').html(self.listView.render().el);\n");    // NOI18N
            myRouters.append("if (self.requestedId) {\n");                // NOI18N
            myRouters.append("self.details(self.requestedId);\n");        // NOI18N
            myRouters.append("}\n}\n});\n},\n");                          // NOI18N
        }
        
        StringBuilder builder = new StringBuilder("tpl-");                // NOI18N
        builder.append(modelVar);
        builder.append("-details");                                       // NOI18N
        myDetailsTemplateName = builder.toString();
        
        // details function
        myRouters.append("details:function (");                           // NOI18N
        if ( hasCollection ){
            myRouters.append("id");                                       // NOI18N
        }
        myRouters.append("){\n");                                         // NOI18N
        if ( hasCollection ){
            myRouters.append("if (this.collection) {\n");                 // NOI18N
            myRouters.append("this.");                                    // NOI18N
            myRouters.append(modelVar);
            myRouters.append("= this.collection.get(id);\n");             // NOI18N
            myRouters.append("if (this.view) {\n");                       // NOI18N
            myRouters.append("this.view.close();\n}\n");                  // NOI18N
            myRouters.append("var self = this;\n");                       // NOI18N  
            myRouters.append("this.view = new views.ModelView({\n");      // NOI18N
            myRouters.append("model:this.");                              // NOI18N
            myRouters.append(modelVar);
            myRouters.append(",\n// ");                                   // NOI18N
            myRouters.append( myDetailsTemplateName );
            myRouters.append(" is template identifier for chosen model element\n");// NOI18N
            myRouters.append("templateName: '#");                         // NOI18N
            myRouters.append( myDetailsTemplateName );
            myRouters.append("',\ngetHashObject: function(){\n");         // NOI18N
            myRouters.append("return self.getData();\n}\n});\n");         // NOI18N
            myRouters.append("$('#");                                      // NOI18N
            myRouters.append(getContentId());
            myRouters.append("').html(this.view.render().el);");          // NOI18N
            myRouters.append("} else {\n");                               // NOI18N
            myRouters.append("this.requestedId = id;\n");                 // NOI18N          
            myRouters.append("this.list();\n}\n},\n");                    // NOI18N
        }
        else {
            myRouters.append("if (this.view) {\n");                       // NOI18N
            myRouters.append("this.view.close();\n}\n");                  // NOI18N
            myRouters.append("var self = this;\n");                       // NOI18N  
            myRouters.append("this.");                                    // NOI18N 
            myRouters.append(modelVar);
            myRouters.append(" = models.");                               // NOI18N 
            myRouters.append(modelGenerator.getModelName());
            myRouters.append("();\nthis.");                               // NOI18N
            myRouters.append(modelVar);
            myRouters.append(".fetch({\n");                               // NOI18N
            myRouters.append("success:function(){\n");                    // NOI18N
            myRouters.append("self.view = new views.ModelView({\n");      // NOI18N
            myRouters.append("model: self.newclass,\n// ");               // NOI18N
            myRouters.append(myDetailsTemplateName);
            myRouters.append(" is template identifier for chosen model element\n");// NOI18N
            myRouters.append("templateName : '#");                        // NOI18N
            myRouters.append(myDetailsTemplateName);
            myRouters.append("'\n});\n");                                 // NOI18N
            myRouters.append("$('#");                                     // NOI18N
            myRouters.append(getContentId());
            myRouters.append("').html(self.view.render().el);}\n});\n},\n");// NOI18N
        }
        
        if ( httpPaths.get( HttpRequests.POST) != null){
            myRouters.append("create:function () {\n");                   // NOI18N
            myRouters.append("if (this.view) {\n");                       // NOI18N
            myRouters.append("this.view.close();\n}\n");                  // NOI18N
            myRouters.append("var self = this;\n");                       // NOI18N
            
            myRouters.append("var model = new models.");
            myRouters.append( modelGenerator.getModelName());
            myRouters.append("();\n");                                    // NOI18N
            myRouters.append("// see isNew() method implementation in the model\n");// NOI18N
            myRouters.append("model.notSynced = true;\n");                // NOI18N
            
            myRouters.append("this.view = new views.ModelView({\n");      // NOI18N
            myRouters.append("model: model,\n");
            if ( hasCollection ){
                myRouters.append("collection: this.collection,\n");       // NOI18N
            }
            myRouters.append("// ");                                      // NOI18N
            myRouters.append(myDetailsTemplateName);
            myRouters.append(" is a template identifier for chosen model element\n");// NOI18N
            myRouters.append("templateName: '#");                          // NOI18N
            myRouters.append(myDetailsTemplateName);
            myRouters.append("',\n");                                      // NOI18N
            myRouters.append("navigate: function( id ){\n");               // NOI18N
            myRouters.append("self.navigate(id, false);\n},\n\n");         // NOI18N
            myRouters.append("getHashObject: function(){\n");              // NOI18N
            myRouters.append("return self.getData();\n}\n");               // NOI18N
            myRouters.append("});\n");                                     // NOI18N
            myRouters.append("$('#");                                      // NOI18N
            myRouters.append(getContentId());                               
            myRouters.append("').html(this.view.render().el);\n},\n");     // NOI18N
        }
        
        // add method getData which returns composite object data got from HTML controls 
        myRouters.append("getData: function(){\n");                       // NOI18N
        myRouters.append("return {\n");                                   // NOI18N
        String mainModelAttribute = modelGenerator.getDisplayNameAlias();
        myRouters.append("/*\n* get values from the HTML controls and");  // NOI18N
        myRouters.append(" put them here as a hash of attributes\n");     // NOI18N
        myRouters.append("* f.e.\n* ");                                   // NOI18N
        myRouters.append(mainModelAttribute);
        myRouters.append(":$('#");                                        // NOI18N
        myRouters.append(mainModelAttribute);
        myRouters.append("').val(),\n* ....\n*/\n");                      // NOI18N
        myRouters.append("};\n}\n");                                      // NOI18N
        
        myRouters.append("});\n");                                        // NOI18N
        myRouters.append("new ");                                         // NOI18N
        myRouters.append(myRouterName);                              
        myRouters.append("();\n");                                        // NOI18N
    }
    
    String getDetailsTemplate(){
        return myDetailsTemplateName;
    }
    
    String getListItemTemplate(){
        return myListItemTemplate;
    }
    
    String getCreateTemplate(){
        return "tpl-create";                                               // NOI18N
    }
    
    String getHeaderId(){
        return "header";                                                   // NOI18N
    }
    
    String getContentId(){
        return "content";                                                  // NOI18N
    }
    
    String getSideBarId(){
        return mySideBarId;
    }
    
    private StringBuilder myRouters;
    private String myRouterName;
    private String myDetailsTemplateName;
    private String mySideBarId;
    private String myListItemTemplate;
}
