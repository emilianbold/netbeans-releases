function Runner1() {
    this.lnick = "";
    this.dobl = new Date();
    this.lhello = function () {
    };
    this.lconf = {
        la: 1, lb: 2, laa: 2
    };
}

exports.literal = {
    prop1: {
        iprop: 1,
        iprop2: 1
    },
    den: new Date(),
    ob: new Runner1(),
    foo2: {
        fprop: 1,
        fprop2: 1
    }
};

var lRef = {
    propX: {
        iprop: 1,
        iprop2: 1
    },
    denX: new Date(),
    obX: new Runner1(),
    fooX: {
        fprop: 1,
        fprop2: 1
    }
};


exports.literalRef = lRef;