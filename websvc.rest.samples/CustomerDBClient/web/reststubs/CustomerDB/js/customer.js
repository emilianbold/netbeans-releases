/*
* Support js for Customer
*/

function Customer(uri_) {
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

    this.initialized = false;
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

   save : function() {
      var remote = new CustomerRemote(this.uri);
      remote.putJson(this.toString());
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
         '"discountCodeRef":{"@uri":"'+this.discountCodeRef.getUri()+'", "discountCode":{"$":"'+this.discountCodeRef.getDiscountCode()+'"}},'+

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
      return delete_(this.uri);
   },

   getDiscountCodeResource : function(discountCode) {
      var link = new DiscountCode(this.uri+'/'+discountCode)()
      return link;
   }

}
