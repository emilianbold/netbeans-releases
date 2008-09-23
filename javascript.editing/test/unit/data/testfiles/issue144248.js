
X = {
    listeners:{
        'keyup':{
            fn: function(field,event) {
                tsGrid.suspendEvents();
                if (this.getRawValue().trim() == ''){
                    this.setValue(0.00);
                    this.focus(true);
                }
                tsGrid.resumeEvents();
            }

               ,scope:tsGrid
        },
        'specialkey': {
            fn: function(field,event) {
                handleNumberFieldKeyPress(event);

            }

               ,scope:this
        }
    }
}







