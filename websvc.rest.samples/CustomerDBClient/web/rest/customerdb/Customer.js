/*
* Support js for Customer
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
    this.discountCode = '';

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
      return this.discountCode;
   },

   setDiscountCode : function(discountCode_) {
      this.discountCode = discountCode_;
   },



   init : function() {
      var remote = new CustomerRemote(this.uri);
      var c = remote.getJson();
      if(c != -1) {
         var myObj = eval('(' +c+')');
         var customer = myObj.customer;
         this.uri = customer['@uri'];
         this.customerId = this.findValue(this.customerId, customer['customerId']);
         this.zip = this.findValue(this.zip, customer['zip']);
         this.name = this.findValue(this.name, customer['name']);
         this.addressline1 = this.findValue(this.addressline1, customer['addressline1']);
         this.addressline2 = this.findValue(this.addressline2, customer['addressline2']);
         this.city = this.findValue(this.city, customer['city']);
         this.state = this.findValue(this.state, customer['state']);
         this.phone = this.findValue(this.phone, customer['phone']);
         this.fax = this.findValue(this.fax, customer['fax']);
         this.email = this.findValue(this.email, customer['email']);
         this.creditLimit = this.findValue(this.creditLimit, customer['creditLimit']);
         this.discountCode = this.findValue(this.discountCode, customer['discountCode']);

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
      var remote = new CustomerRemote(this.uri);
      return remote.putJson('{'+this.toString()+'}');
   },

   delete_ : function() {
      var remote = new CustomerRemote(this.uri);
      return remote.delete_();
   },

   toString : function() {
      if(!this.initialized)
         this.init();
      var myObj = 
         '"customer":'+
         '{'+
         '"@uri":"'+this.uri+'",'+
                  '"customerId":"'+this.customerId+'",'+
         '"zip":"'+this.zip+'",'+
         '"name":"'+this.name+'",'+
         '"addressline1":"'+this.addressline1+'",'+
         '"addressline2":"'+this.addressline2+'",'+
         '"city":"'+this.city+'",'+
         '"state":"'+this.state+'",'+
         '"phone":"'+this.phone+'",'+
         '"fax":"'+this.fax+'",'+
         '"email":"'+this.email+'",'+
         '"creditLimit":"'+this.creditLimit+'",'+
         this.discountCode+

         '}';
      return myObj;
   },

   getFields : function() {
      var fields = [];
         fields.push('customerId');
         fields.push('zip');
         fields.push('name');
         fields.push('addressline1');
         fields.push('addressline2');
         fields.push('city');
         fields.push('state');
         fields.push('phone');
         fields.push('fax');
         fields.push('email');
         fields.push('creditLimit');
         fields.push('discountCode');

      return fields;
   }

}

function CustomerRemote(uri_) {
    this.uri = uri_;
}

CustomerRemote.prototype = {

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

   getDiscountCodeResource : function(discountCode) {
      var link = new DiscountCode(this.uri+'/'+discountCode)()
      return link;
   }

}
