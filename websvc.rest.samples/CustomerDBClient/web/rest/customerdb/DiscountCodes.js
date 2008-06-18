/*
* Support js for DiscountCodes
*/

function DiscountCodes(uri_) {
    this.uri = uri_;
    this.items = new Array();
    this.initialized = false;
}

DiscountCodes.prototype = {

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
      var remote = new DiscountCodesRemote(this.uri);
      var c = remote.getJson();
      if(c != -1) {
         var myObj = eval('('+c+')');
         var discountCodes = myObj.discountCodes;
         if(discountCodes == null || discountCodes == undefined) {
            rjsSupport.debug('discountCodes is undefined, so skipping init of DiscountCodes');
            return;
         }
         var refs = discountCodes.discountCode;
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
            rjsSupport.debug('discountCode is undefined, so skipping initChild for DiscountCodes');
         }
         this.initialized = true;
      }
   },

   initChild : function(ref, j) {
      var uri2 = ref['@uri'];
      this.items[j] = new DiscountCode(uri2);
   },

   flush : function() {
      var remote = new DiscountCodesRemote(this.uri);
      remote.postJson('{'+this.toString()+'}');
   },

   flush : function(discountCode) {
      var remote = new DiscountCodesRemote(this.uri);
      return remote.postJson('{'+discountCode.toString()+'}');
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
            s = s + '{"@uri":"'+c.getUri()+'", "discountCodeId":"'+rjsSupport.findIdFromUrl(c.getUri())+'"},';
         else
            s = s + '{"@uri":"'+c.getUri()+'", "discountCodeId":"'+rjsSupport.findIdFromUrl(c.getUri())+'"}';
      }
      if(this.items.length > 1)
          s = s + ']';
      var myObj = '';
      if(s == '') {
          myObj = '"discountCodes":{"@uri":"'+this.getUri()+'"}';
      } else {
          myObj = 
            '"discountCodes":{'+'"@uri":"'+this.getUri()+'",'+'"discountCode":'+s+''+'}';
      }
      return myObj;
   }

}

function DiscountCodesRemote(uri_) {
    this.uri = uri_;
}

DiscountCodesRemote.prototype = {

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

   getDiscountCodeResource : function(discountCode) {
      var link = new DiscountCode(this.uri+'/'+discountCode)()
      return link;
   }

}
