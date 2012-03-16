var engine = {
    cylinders : 8,
    power: "22k",
    getDescription: function () {
        with(this) {
            if(!disabled) {
                println ('Cylinders: '
                    + cylinders + ' with power: ' + power);
            }   else {
                log();
            }
        }
    }
}

function computeColor() {
    try {
        color = 0;
        for   (var a = -1; a < 3; a++) {
            color += Math.round(Math.random() * 2);
        }
    }catch (error) {
        println (error);
    }  finally {
        log();
    }
    while(color > 100) {
        do {
            color = color / 2;
        }   while (isOk());
    }
    color = color < 1 ? 1 : color
}

var color = computeColor();
switch   (color) {
    case 0:
    case 1:
        code = 'low';
        break;
    case 2:
        code = 'high';
        break;
    default:
        code = undefined
}