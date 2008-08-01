// Regression test for 132374
function foo() {  
  var t = this[related ? "getRelatedTarget" : "getTarget"]();
            return t && Ext.fly(el).contains(t);
}

