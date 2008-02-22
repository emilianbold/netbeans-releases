var declaredglobal;
global = 5;
Autocompleter.Base.prototype = {
  baseInitialize: function(element1, update1, options1) {
    var foo1 = 5, bar1;
    alert('foo1 ' + foo1);
  },
  other: function(element2, update2, options2) {
    var foo2 = 5, bar2;
    alert('foo2 ' + foo2);
  }
}

