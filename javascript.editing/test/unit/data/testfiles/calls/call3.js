var Class = {
  create: function() {
    return function() {
      this.initialize.apply(this, arguments);
    }
  }
}


var foo1 = {
    method: function() {
        jQ // 1
    }
}

var foo2 = Class.create({
      bar: {'foo': 1, 'bar': 2},
      fooBar: function()
      {
        jQ // 2
      }
});

var foo3 = {
    fooFunc: function() {
        $('.class').bind('event', function(event, ui) {
            jQ // 3
        });
    }
}

