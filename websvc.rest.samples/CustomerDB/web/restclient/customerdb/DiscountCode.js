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

/*
* Support js for DiscountCode
*/

var useWrapDiscountCodes = true;

function DiscountCode(uri_) {
    this.DiscountCode(uri_, false);
}

function DiscountCode(uri_, initialized_) {
    this.uri = uri_;
    this.discountCode = '';
    this.rate = '';
    this.customers = new Array();

    this.initialized = initialized_;
}

DiscountCode.prototype = {

   getUri : function() {
      return this.uri;
   },

   getDiscountCode : function() {
      if(!this.initialized)
         this.init();
      return this.discountCode;
   },

   setDiscountCode : function(discountCode_) {
      this.discountCode = discountCode_;
   },

   getRate : function() {
      if(!this.initialized)
         this.init();
      return this.rate;
   },

   setRate : function(rate_) {
      this.rate = rate_;
   },

   getCustomers : function() {
      if(!this.initialized)
         this.init();
      return this.customers;
   },

   setCustomers : function(customers_) {
      this.customers = customers_;
   },

   init : function() {
      var remote = new DiscountCodeRemote(this.uri);
      var c = remote.getJson_();
      if(c != -1) {
         var myObj = eval('(' +c+')');
         var discountCode = myObj.discountCode;
         if(discountCode == null || discountCode == undefined || discountCode['@uri'] == undefined) {
            discountCode = myObj;
            useWrapDiscountCode = false;
         }
         this.uri = discountCode['@uri'];
         this.discountCode = this.findValue(this.discountCode, discountCode['discountCode']);
         this.rate = this.findValue(this.rate, discountCode['rate']);
         this.customers = new Customers(discountCode['customerCollection']['@uri']);

         this.initialized = true;
      }
   },

   findValue : function(field, value) {
      if(value == undefined)
          return field;
      else
         return value;
   },

   flush : function() {
      var remote = new DiscountCodeRemote(this.uri);
      if(useWrapDiscountCode)
         return remote.putJson_('{'+this.toString()+'}');
      else
         return remote.putJson_(this.toString());
   },

   delete_ : function() {
      var remote = new DiscountCodeRemote(this.uri);
      return remote.deleteJson_();
   },

   toString : function() {
      if(!this.initialized)
         this.init();
      var myObj = 
         '{'+
         '"@uri":"'+this.uri+'"'+
                  ', "discountCode":"'+this.discountCode+'"'+
         ', "rate":"'+this.rate+'"'+
         ', "customerCollection":{"@uri":"'+this.customers.getUri()+'"}'+

         '}';
      if(useWrapDiscountCode) {
          myObj = '"discountCode":'+myObj;
      }
      return myObj;
   },

   getFields : function() {
      var fields = [];
         fields.push('discountCode');
         fields.push('rate');

      return fields;
   }

}

function DiscountCodeRemote(uri_) {
    this.uri = uri_+'?expandLevel=1';
}

DiscountCodeRemote.prototype = {

/* Default getJson_() method used by init() method. Do not remove. */
   getJson_ : function() {
      return rjsSupport.get(this.uri, 'application/json');
   },
/* Default putJson_() method used by flush() method. Do not remove. */
   putJson_ : function(content) {
      return rjsSupport.put(this.uri, 'application/json', content);
   },
/* Default deleteJson_() method used by delete_() method. Do not remove. */
   deleteJson_ : function() {
      return rjsSupport.delete_(this.uri);
   }
   ,
   getXml : function() {
      return rjsSupport.get(this.uri, 'application/xml');
   },

   getJson : function() {
      return rjsSupport.get(this.uri, 'application/json');
   },

   putXml : function(content) {
      return rjsSupport.put(this.uri, 'application/xml', content);
   },

   putJson : function(content) {
      return rjsSupport.put(this.uri, 'application/json', content);
   },

   delete_ : function() {
      return rjsSupport.delete_(this.uri);
   },

   getCustomerCollectionResource : function(customerCollection) {
      var link = new Customers(this.uri+'/'+customerCollection);
      return link;
   }
}
