/*
* Support js for Customers
*/

function Customers(uri_) {
    this.uri = uri_;
    this.items = new Array();
    this.initialized = false;
}

Customers.prototype = {

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
      var remote = new CustomersRemote(this.uri);
      var c = remote.getJson();
      if(c != -1) {
         var myObj = eval('('+c+')');
         var customers = myObj.customers;
         if(customers == null || customers == undefined) {
            rjsSupport.debug('customers is undefined, so skipping init of Customers');
            return;
         }
         var refs = customers.customer;
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
            rjsSupport.debug('customer is undefined, so skipping initChild for Customers');
         }
         this.initialized = true;
      }
   },

   initChild : function(ref, j) {
      var uri2 = ref['@uri'];
      this.items[j] = new Customer(uri2);
   },

   flush : function() {
      var remote = new CustomersRemote(this.uri);
      remote.postJson('{'+this.toString()+'}');
   },

   flush : function(customer) {
      var remote = new CustomersRemote(this.uri);
      return remote.postJson('{'+customer.toString()+'}');
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
            s = s + '{"@uri":"'+c.getUri()+'", "customerId":"'+rjsSupport.findIdFromUrl(c.getUri())+'"},';
         else
            s = s + '{"@uri":"'+c.getUri()+'", "customerId":"'+rjsSupport.findIdFromUrl(c.getUri())+'"}';
      }
      if(this.items.length > 1)
          s = s + ']';
      var myObj = '';
      if(s == '') {
          myObj = '"customers":{"@uri":"'+this.getUri()+'"}';
      } else {
          myObj = 
            '"customers":{'+'"@uri":"'+this.getUri()+'",'+'"customer":'+s+''+'}';
      }
      return myObj;
   }

}

function CustomersRemote(uri_) {
    this.uri = uri_;
}

CustomersRemote.prototype = {

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

   postXml : function(content) {
      return rjsSupport.post(this.uri, 'application/xml', content);
   },

   postXml : function(content) {
      return rjsSupport.post(this.uri, 'application/xml', content);
   },

   postJson : function(content) {
      return rjsSupport.post(this.uri, 'application/json', content);
   },

   getCustomerResource : function(customerId) {
      var link = new Customer(this.uri+'/'+customerId)()
      return link;
   }

}
