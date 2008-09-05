/*
* CustomerDB stub
*/

function CustomerDB() {}

function CustomerDB(uri_) {
    this.uri = uri_;
}

CustomerDB.prototype = {

   uri : '__BASE_URL__',

   resources : new Array(),
   
   initialized : false,

   getUri : function() {
      return this.uri;
   },

   getResources : function() {
      if(!this.initialized)
          this.init();
      return this.resources;
   },

   init : function() {
      this.resources[0] = new DiscountCodes(this.uri+'/discountCodes/');
      this.resources[1] = new Customers(this.uri+'/customers/');

      this.initialized = true;
   },

   flush : function(resources_) {
      for(j=0;j<resources_.length;j++) {
        var r = resources_[j];
        r.flush();
      }
   },
   
   getProxy : function() {
       return rjsSupport.getHttpProxy();
   },
   
   setProxy : function(proxy_) {
       rjsSupport.setHttpProxy(proxy_);
   },

   toString : function() {
      var s = '';
      for(j=0;j<this.resources.length;j++) {
        var c = this.resources[j];
        if(j<this.resources.length-1)
            s = s + '{"@uri":"'+c.getUri()+'"},';
        else
            s = s + '{"@uri":"'+c.getUri()+'"}';
      }
      var myObj = 
         '{"resources":'+
         '{'+
         s+
         '}'+
      '}';
      return myObj;
   }

}
