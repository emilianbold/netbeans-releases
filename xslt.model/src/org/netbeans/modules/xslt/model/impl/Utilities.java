/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.model.impl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xslt.model.Import;
import org.netbeans.modules.xslt.model.Include;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.XslModel;


/**
 * @author ads
 *
 */
final class Utilities {
    
    // should not be instantiated
    private Utilities() {
    }
    
    /*
     * This method returns XslModels with import precedence order.
     * This list doesn't contain any marks for comparing
     * two models from this list, so you cannot determine
     * from this list whether one model should preceed another model
     * ( f.e. there can be two included models in this list
     * and they has equal import precedence and one model
     * could be before or after other. But there can be also 
     * imported model and included model. In the latter case included 
     * model will be before imported in this list always).     
     */
    static LinkedHashSet<XslModel> getAvailibleModels( XslModel model ){
        if ( model == null ) {
            return new LinkedHashSet<XslModel>();
        }
        LinkedHashSet<XslModel> list = new LinkedHashSet<XslModel>();
        list.add( model );
        collectModels( model , list );
        return list;
    }
    
   static void collectModels( XslModel model, LinkedHashSet<XslModel> list ) {
       Stylesheet stylesheet = model.getStylesheet();
       if ( stylesheet == null ) {
           return;
       }
       List<Include> includes = stylesheet.getChildren( Include.class );
       for (Include include : includes) {
           XslModel refModel;
           try {
               refModel = include.resolveReferencedModel();
           }
           catch (CatalogModelException e) {
               // ignore exception and proceed with other models
               continue;
           }
           if ( list.contains( refModel )) {
               continue;
           }
           else {
               list.add( refModel );
               collectModels(refModel, list);
           }
       }
       
       LinkedList<Import> imports = 
           new LinkedList<Import>(stylesheet.getImports());
       Collections.reverse( imports );
       for (Import imprt : imports) {
           XslModel refModel;
           try {
               refModel = imprt.resolveReferencedModel();
           }
           catch (CatalogModelException e) {
               // ignore exception and proceed with other models
               continue;
           }
           if ( list.contains( refModel )) {
               continue;
           }
           else {
               list.add( refModel );
               collectModels(refModel, list);
           }
       }
   }
   
   
   static boolean equals( String first , String second ) {
       if ( first == null ) {
           return second == null;
       }
       else {
           return first.equals(second);
       }
   }

}
