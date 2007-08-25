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
         var refs = customers.customerRef;         
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
      this.items[j] = new Customer(uri2);
   },

   flush : function() {
      var remote = new CustomersRemote(this.uri);
      return remote.postJson(this.toString());
   },
   
   flush : function(customer) {
      var remote = new CustomersRemote(this.uri);
      return remote.postJson(customer.toString());
   },   

   toString : function() {
      if(!this.initialized)
         this.init();
      var s = '';
      var j = 0;
      for(j=0;j<this.items.length;j++) {
         var c = this.items[j];
         var id = findIdFromUrl(c.getUri());
         if(j<this.items.length-1)
            s = s + '{"@uri":"'+c.getUri()+'", "customerId":{"$":"'+id+'"}},';
         else
            s = s + '{"@uri":"'+c.getUri()+'", "customerId":{"$":"'+id+'"}}';
      }
      var myObj = 
         '{"customers":{'+
            '"@uri":"'+this.getUri()+'",'+
            '"customerRef":['+s+']'+
          '}'+
         '}';
      return myObj;
   }

}

function CustomersRemote(uri_) {
    this.uri = uri_;
}

CustomersRemote.prototype = {

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

   getCustomerResource : function(customerId) {
      var link = new Customer(this.uri+'/'+customerId)()
      return link;
   }

}
