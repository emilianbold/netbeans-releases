/*
* Support js for DiscountCode
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
      return remote.putJson('{'+this.toString()+'}');
   },

   delete_ : function() {
      var remote = new DiscountCodeRemote(this.uri);
      return remote.delete_();
   },

   toString : function() {
      if(!this.initialized)
         this.init();
      var myObj = 
         '"discountCode":'+
         '{'+
         '"@uri":"'+this.uri+'",'+
                  '"discountCode":"'+this.discountCode+'",'+
         '"rate":"'+this.rate+'",'+
         this.customers.toString()+''+

         '}';
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
    this.uri = uri_;
}

DiscountCodeRemote.prototype = {

/* Default getJson() method used by Container/Containee init() methods. Do not remove. */
   getJson : function() {
      return rjsSupport.get(this.uri, 'application/json');
   },

   getXml : function() {
      return rjsSupport.get(this.uri, 'application/xml');
   },

   getXml : function() {
      return rjsSupport.get(this.uri, 'application/xml');
   },

   getJson : function() {
      return rjsSupport.get(this.uri, 'application/json');
   },

   putXml : function(content) {
      return rjsSupport.put(this.uri, 'application/xml', content);
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
      var link = new Customers(this.uri+'/'+customerCollection)()
      return link;
   }

}
