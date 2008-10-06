function test_func() {
    var ctl = {};
    
    new Ajax.Request("dummy-url", {
        method: "get",
        
        onSuccess: function(transport) {
            var updated = [];
            
            updated.each(function(item) {
                ctl.load(item, function() {
                    test_func_2(item);
                }, function() {
                });
            });
        }
    });
}

