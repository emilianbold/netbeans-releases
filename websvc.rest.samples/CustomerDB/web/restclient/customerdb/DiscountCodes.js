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
* Support js for DiscountCodes
*/

var useWrapDiscountCodes = true;

function DiscountCodes(uri_) {
    this.uri = uri_;
    this.items = new Array();
    this.initialized = false;
}

DiscountCodes.prototype = {

   getUri : function() {
      return this.uri;
   },

   getItems : function() {
      if(!this.initialized)
          this.init();
      return this.items;
   },

   addItem : function(item) {
      this.items.push(item);
   },

   removeItem : function(item) {
      var status = item.delete_();
      if(status)
        this.init(); //re-read items
      return status;
   },

   init : function() {
      var remote = new DiscountCodesRemote(this.uri);
      var c = remote.getJson_();
      if(c != -1) {
         var myObj = eval('('+c+')');
         var discountCodes = myObj.discountCodes;
         if(discountCodes == null || discountCodes == undefined) {
            discountCodes = myObj;
            useWrapDiscountCodes = false;
         }
         this.items = new Array();
         var refs = discountCodes.discountCode;
         if(refs != undefined) {
             if(refs.length == undefined) {
                 this.initChild(refs, 0);
             } else {
                 var j = 0;
                 for(j=0;j<refs.length;j++) {
                    var ref = refs[j];
                    this.initChild(ref, j);
                 }
             }
         } else {
            rjsSupport.debug('discountCode is undefined, so skipping initChild for DiscountCodes');
         }
         this.initialized = true;
      }
   },

   initChild : function(ref, j) {
      var uri2 = ref['@uri'];
      this.items[j] = new DiscountCode(uri2);
   },

   flush : function() {
      var remote = new DiscountCodesRemote(this.uri);
      if(useWrapDiscountCodes)
         return remote.postJson_('{'+this.toString()+'}');
      else
         return remote.postJson_(this.toString());
   },

   flush : function(discountCode) {
      var remote = new DiscountCodesRemote(this.uri);
      if(useWrapDiscountCodes)
         return remote.postJson_('{'+discountCode.toString()+'}');
      else
         return remote.postJson_(discountCode.toString());
   },

   toString : function() {
      if(!this.initialized)
         this.init();
      var s = '';
      var j = 0;
      if(this.items.length > 1)
          s = s + '[';
      for(j=0;j<this.items.length;j++) {
         var c = this.items[j];
         if(j<this.items.length-1)
            s = s + '{"@uri":"'+c.getUri()+'", "discountCodeId":"'+rjsSupport.findIdFromUrl(c.getUri())+'"},';
         else
            s = s + '{"@uri":"'+c.getUri()+'", "discountCodeId":"'+rjsSupport.findIdFromUrl(c.getUri())+'"}';
      }
      if(this.items.length > 1)
          s = s + ']';
      var myObj = '';
      if(s == '') {
          myObj = '{"@uri":"'+this.getUri()+'"}';
      } else {
          myObj = 
            '{'+'"@uri":"'+this.getUri()+'",'+'"discountCode":'+s+''+'}';
      }
      if(useWrapDiscountCodes) {
          myObj = '"discountCodes":'+myObj;
      }
      return myObj;
   }

}

function DiscountCodesRemote(uri_) {
    this.uri = uri_+'?expandLevel=0&start=0&max=50';
}

DiscountCodesRemote.prototype = {

/* Default getJson_() method used by init() method. Do not remove. */
   getJson_ : function() {
      return rjsSupport.get(this.uri, 'application/json');
   },
/* Default postJson_() method used by flush() methods. Do not remove. */
   postJson_ : function(content) {
      return rjsSupport.post(this.uri, 'application/json', content);
   }
   ,
   getXml : function() {
      return rjsSupport.get(this.uri, 'application/xml');
   },

   getJson : function() {
      return rjsSupport.get(this.uri, 'application/json');
   },

   postXml : function(content) {
      return rjsSupport.post(this.uri, 'application/xml', content);
   },

   postJson : function(content) {
      return rjsSupport.post(this.uri, 'application/json', content);
   },

   getDiscountCodeResource : function(discountCode) {
      var link = new DiscountCode(this.uri+'/'+discountCode);
      return link;
   }
}
