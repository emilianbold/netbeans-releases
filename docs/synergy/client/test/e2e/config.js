var fs = require("fs");

exports.configuration = {
    baseUrl: process.env.baseUrl || "http://localhost/synergy_nb/synergy~synergy-code/client/test/app/test.html#/",
    output: process.env.output || "/tmp/",
    screenshots: true
};

exports.util = {
    escapeString: function(str) {
        return str.toLowerCase().replace(/[^\w]/g, "_");
    }
};

exports.screenshot = {
    create: function(filename, path, data, callback) {
        fs.exists(path, function(exists) {
            if (exists) {
                write();
            } else {
                fs.mkdir(path, 0777, function(e) {
                    if (!e) {
                        write();
                    } else {
                        callback(e, null);
                    }
                });
            }
        });

        function write() {
            fs.writeFile(path + "/" + filename + ".png", data.replace(/^data:image\/png;base64,/, ""), "base64", function(err) {
                if (typeof callback === "function" && err) {
                    callback(err, null);
                } else if (typeof callback === "function" && !err) {
                    callback(null, null);
                }
            });
        }
    }
};