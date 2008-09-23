
            function foo(text){
            return 10;
        }
        

        dojo.provide("dojox.wire.ml.tests.markup.Action");

        dojo.require("dojo.parser");

        dojo.addOnLoad(function(){
            doh.register("dojox.wire.ml.tests.markup.Action", [
                function test_Action_triggerEvent(t){
                    dojox.wire.ml.tests.markup.Action.target = {};
                },
            ]);
            doh.run();
        }
    );
        

