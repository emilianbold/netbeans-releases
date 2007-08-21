/*
* CustomerDB stub
*/

function CustomerDB() {}

CustomerDB.prototype = {

   uri : 'http://localhost:8080/CustomerDB/restbean',

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
      this.resources[0] = new Customers(this.uri+'/customers/');
      this.resources[1] = new DiscountCodes(this.uri+'/discountCodes/');

      this.initialized = true;
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
