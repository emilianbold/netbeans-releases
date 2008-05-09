Object.extend(Object, {
         inspect: function(object) {
            try {
                var undefined;
                if (object === undefined) return 'undefined';
            } catch (e) {
                puts(e)
            }
        },
    })


