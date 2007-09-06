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

function Customer(uri_) {
    this.Customer(uri_, false);
}

function Customer(uri_, initialized_) {
    this.uri = uri_;
    this.customerId = '';
    this.zip = '';
    this.name = '';
    this.addressline1 = '';
    this.addressline2 = '';
    this.city = '';
    this.state = '';
    this.phone = '';
    this.fax = '';
    this.email = '';
    this.creditLimit = '';
    this.discountCodeRef = '';

    this.initialized = initialized_;
}

Customer.prototype = {

   getUri : function() {
      return this.uri;
   },

   getCustomerId : function() {
      if(!this.initialized)
         this.init();
      return this.customerId;
   },

   setCustomerId : function(customerId_) {
      this.customerId = customerId_;
   },

   getZip : function() {
      if(!this.initialized)
         this.init();
      return this.zip;
   },

   setZip : function(zip_) {
      this.zip = zip_;
   },

   getName : function() {
      if(!this.initialized)
         this.init();
      return this.name;
   },

   setName : function(name_) {
      this.name = name_;
   },

   getAddressline1 : function() {
      if(!this.initialized)
         this.init();
      return this.addressline1;
   },

   setAddressline1 : function(addressline1_) {
      this.addressline1 = addressline1_;
   },

   getAddressline2 : function() {
      if(!this.initialized)
         this.init();
      return this.addressline2;
   },

   setAddressline2 : function(addressline2_) {
      this.addressline2 = addressline2_;
   },

   getCity : function() {
      if(!this.initialized)
         this.init();
      return this.city;
   },

   setCity : function(city_) {
      this.city = city_;
   },

   getState : function() {
      if(!this.initialized)
         this.init();
      return this.state;
   },

   setState : function(state_) {
      this.state = state_;
   },

   getPhone : function() {
      if(!this.initialized)
         this.init();
      return this.phone;
   },

   setPhone : function(phone_) {
      this.phone = phone_;
   },

   getFax : function() {
      if(!this.initialized)
         this.init();
      return this.fax;
   },

   setFax : function(fax_) {
      this.fax = fax_;
   },

   getEmail : function() {
      if(!this.initialized)
         this.init();
      return this.email;
   },

   setEmail : function(email_) {
      this.email = email_;
   },

   getCreditLimit : function() {
      if(!this.initialized)
         this.init();
      return this.creditLimit;
   },

   setCreditLimit : function(creditLimit_) {
      this.creditLimit = creditLimit_;
   },

   getDiscountCode : function() {
      if(!this.initialized)
         this.init();
      return this.discountCodeRef;
   },

   setDiscountCode : function(discountCodeRef_) {
      this.discountCodeRef = discountCodeRef_;
   },



   init : function() {
      var remote = new CustomerRemote(this.uri);
      var c = remote.getJson();
      if(c != -1) {
         var myObj = eval('(' +c+')');
         var customer = myObj.customer;
         this.uri = customer['@uri'];
         this.customerId = customer['customerId']['$'];
         this.zip = customer['zip']['$'];
         this.name = customer['name']['$'];
         this.addressline1 = customer['addressline1']['$'];
         this.addressline2 = customer['addressline2']['$'];
         this.city = customer['city']['$'];
         this.state = customer['state']['$'];
         this.phone = customer['phone']['$'];
         this.fax = customer['fax']['$'];
         this.email = customer['email']['$'];
         this.creditLimit = customer['creditLimit']['$'];
         this.discountCodeRef = new DiscountCode(customer['discountCodeRef']['@uri']);

         this.initialized = true;
      }
   },

   flush : function() {
      var remote = new CustomerRemote(this.uri);
      return remote.putJson(this.toString());
   },
   
   delete_ : function() {
      var remote = new CustomerRemote(this.uri);
      return remote.delete_();
   },   

   toString : function() {
      if(!this.initialized)
         this.init();
      var myObj = 
         '{"customer":'+
         '{'+
         '"@uri":"'+this.uri+'",'+
                  '"customerId":{"$":"'+this.customerId+'"},'+
         '"zip":{"$":"'+this.zip+'"},'+
         '"name":{"$":"'+this.name+'"},'+
         '"addressline1":{"$":"'+this.addressline1+'"},'+
         '"addressline2":{"$":"'+this.addressline2+'"},'+
         '"city":{"$":"'+this.city+'"},'+
         '"state":{"$":"'+this.state+'"},'+
         '"phone":{"$":"'+this.phone+'"},'+
         '"fax":{"$":"'+this.fax+'"},'+
         '"email":{"$":"'+this.email+'"},'+
         '"creditLimit":{"$":"'+this.creditLimit+'"},'+
         '"discountCodeRef":{"@uri":"'+this.discountCodeRef.getUri()+'", "discountCode":{"$":"'+this.discountCodeRef.getDiscountCode()+'"}}'+

         '}'+
      '}';
      return myObj;
   }

}

function CustomerRemote(uri_) {
    this.uri = uri_;
}

CustomerRemote.prototype = {

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

   getDiscountCodeResource : function(discountCode) {
      var link = new DiscountCode(this.uri+'/'+discountCode)()
      return link;
   }

}
