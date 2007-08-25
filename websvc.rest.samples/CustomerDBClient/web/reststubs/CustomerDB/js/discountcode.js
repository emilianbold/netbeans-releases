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
