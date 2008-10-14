// Issue 149226
var foo = {
    fooProp: null,
    fooFunc: function () {
        localObject = {'foo': 1, 'bar': 2};
    }
};

