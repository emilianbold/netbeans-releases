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
      var c = remote.getJson();
      if(c != -1) {
         var myObj = eval('(' +c+')');
         var discountCode = myObj.discountCode;
         this.uri = discountCode['@uri'];
         this.discountCode = discountCode['discountCode']['$'];
         this.rate = discountCode['rate']['$'];
         this.customers = new Customers(discountCode['customers']['@uri']);

         this.initialized = true;
      }
   },

   flush : function() {
      var remote = new DiscountCodeRemote(this.uri);
      return remote.putJson(this.toString());
   },

   delete_ : function() {
      var remote = new DiscountCodeRemote(this.uri);
      return remote.delete_();
   }, 
   
   toString : function() {
      if(!this.initialized)
         this.init();
      var myObj = 
         '{"discountCode":'+
         '{'+
         '"@uri":"'+this.uri+'",'+
                  '"discountCode":{"$":"'+this.discountCode+'"},'+
         '"rate":{"$":"'+this.rate+'"},'+
         this.customers.toString()+

         '}'+
      '}';
      return myObj;
   }

}

function DiscountCodeRemote(uri_) {
    this.uri = uri_;
}

DiscountCodeRemote.prototype = {

   getXml : function() {
      return get_(this.uri, 'application/xml');
   },

   getJson : function() {
      return get_(this.uri, 'application/json');
   },

   putXml : function(content) {
      return put_(this.uri, 'application/xml', content);
   },

   putJson : function(content) {
      return put_(this.uri, 'application/json', content);
   },

   delete_ : function() {
      return delete__(this.uri);
   },

   getCustomersResource : function(customers) {
      var link = new Customers(this.uri+'/'+customers)()
      return link;
   }

}
