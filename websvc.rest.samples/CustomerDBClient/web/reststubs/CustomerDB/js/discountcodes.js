/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

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
      this.items[this.items.length+1] = item;
   },
   
   removeItem : function(item) {
      var status = item.delete_();
      if(status != '-1')
        this.init(); //re-read items
      return status;
   },  
   
   init : function() {
      var remote = new DiscountCodesRemote(this.uri);
      var c = remote.getJson();
      if(c != -1) {
         var myObj = eval('('+c+')');
         var discountCodes = myObj.discountCodes;
         var refs = discountCodes.discountCodeRef;         
         if(refs.length == undefined) {
             this.initChild(refs, 0);
         } else {
             var j = 0;
             for(j=0;j<refs.length;j++) {
                var ref = refs[j];
                this.initChild(ref, j);
             }
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
      remote.postJson(this.toString());
   },

   toString : function() {
      if(!this.initialized)
         this.init();
      var s = '';
      var j = 0;
      for(j=0;j<this.items.length;j++) {
         var c = this.items[j];
         if(j<this.items.length-1)
            s = s + '{"@uri":"'+c.getUri()+'", "discountCodeId":{"$":"'+findIdFromUrl(c.getUri())+'"}},';
         else
            s = s + '{"@uri":"'+c.getUri()+'", "discountCodeId":{"$":"'+findIdFromUrl(c.getUri())+'"}}';
      }
      var myObj = 
         '{"discountCodes":{'+
            '"@uri":"'+this.getUri()+'",'+
            '"discountCodeRef":['+s+']'+
          '}'+
         '}';
      return myObj;
   }

}

function DiscountCodesRemote(uri_) {
    this.uri = uri_;
}

DiscountCodesRemote.prototype = {

   getXml : function() {
      return get_(this.uri, 'application/xml');
   },

   getJson : function() {
      return get_(this.uri, 'application/json');
   },

   postXml : function(content) {
      return post_(this.uri, 'application/xml', content);
   },

   postJson : function(content) {
      return post_(this.uri, 'application/json', content);
   },

   getDiscountCodeResource : function(discountCode) {
      var link = new DiscountCode(this.uri+'/'+discountCode)()
      return link;
   }

}
