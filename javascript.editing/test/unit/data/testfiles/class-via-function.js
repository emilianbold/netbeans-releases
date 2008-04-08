function JMaki () {
  //private
  var _jmaki = this;

  // public property
  this.debug = true;

  // public function
  this.doSomething = function() {
    // do something here
  };

  // private function
  function doAnotherThing() {
  };
}



if (typeof jmaki == 'undefined') {
   var jmaki = new JMaki();
}

