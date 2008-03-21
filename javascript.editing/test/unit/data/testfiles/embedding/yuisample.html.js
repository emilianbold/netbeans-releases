__netbeans_import__('../../build/yahoo/yahoo-min.js');

__netbeans_import__('../../build/dom/dom-min.js');

__netbeans_import__('../../build/event/event-min.js');

__netbeans_import__('../../build/element/element-beta-min.js');

__netbeans_import__('../../build/button/button-min.js');



    YAHOO.example.init = function () {

        // "click" event handler for each Button instance

        function onButtonClick(p_oEvent) {

            YAHOO.log("You clicked button: " + this.get("id"), "info", "example1");
        
        }


        // "contentready" event handler for the "pushbuttonsfrommarkup" <fieldset>

        YAHOO.util.Event.onContentReady("pushbuttonsfrommarkup", function () {

            // Create Buttons using existing <input> elements as a data source

            var oPushButton1 = new YAHOO.widget.Button("pushbutton1");
            oPushButton1.on("click", onButtonClick);
            
            var oPushButton2 = new YAHOO.widget.Button("pushbutton2", { onclick: { fn: onButtonClick } });
            var oPushButton3 = new YAHOO.widget.Button("pushbutton3", { onclick: { fn: onButtonClick } });


            // Create Buttons using the YUI Button markup

            var oPushButton4 = new YAHOO.widget.Button("pushbutton4");
            oPushButton4.on("click", onButtonClick);

            var oPushButton5 = new YAHOO.widget.Button("pushbutton5", { onclick: { fn: onButtonClick } });
            var oPushButton6 = new YAHOO.widget.Button("pushbutton6", { onclick: { fn: onButtonClick } });        
        
        });


        // Create Buttons without using existing markup

        var oPushButton7 = new YAHOO.widget.Button({ label:"Add", id:"pushbutton7", container:"pushbuttonsfromjavascript" });
        oPushButton7.on("click", onButtonClick);

        var oPushButton8 = new YAHOO.widget.Button({ label:"Add", id:"pushbutton8", container:"pushbuttonsfromjavascript", onclick: { fn: onButtonClick } });
        var oPushButton9 = new YAHOO.widget.Button({ label:"Add", id:"pushbutton9", container:"pushbuttonsfromjavascript", onclick: { fn: onButtonClick } });

    } ();


__netbeans_import__('../../assets/dpSyntaxHighlighter.js');

 
dp.SyntaxHighlighter.HighlightAll('code'); 

__netbeans_import__('../../assets/YUIexamples.js');

