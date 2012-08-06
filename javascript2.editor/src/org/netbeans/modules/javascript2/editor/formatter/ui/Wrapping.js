var colors = [0, 1, 2]
function computeColor(limit) {
    try {
        color = 0; color++;
        for (var a = -1; a < limit; a++) color += Math.round(Math.random() * 2);
    } catch (error) {
        println(error);
    } finally {
        log();
    }
    while (color > 100) {
        do {
            color = (color / 2) + 1;
        } while (isOk());
    }
    color = color < 1 ? 1 : color
}

var color = computeColor();
switch (color) {
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