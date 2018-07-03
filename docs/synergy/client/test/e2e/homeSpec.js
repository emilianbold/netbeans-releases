var webdriver = require("selenium-webdriver");
var driver = new webdriver.Builder().withCapabilities(webdriver.Capabilities.chrome()).build();
jasmine.getEnv().defaultTimeoutInterval = 50000;
var TEST = require("./config.js");

describe("homepage structure test", function() {
    var counter = 0;
    var numberOfTests = 3;
    var lastFailuresCount = 0;

    afterEach(function() {
        var specificationName = this.suite.env.currentSpec.description;
        var suiteName = this.suite.env.currentSpec.suite.description;
        var currentResults = this.suite.env.currentSpec.results_;
        counter++;
        if (lastFailuresCount < currentResults.failedCount && TEST.configuration.screenshots) {
            driver.takeScreenshot().then(function(data) {
                TEST.screenshot.create(TEST.util.escapeString(specificationName), TEST.configuration.output + TEST.util.escapeString(suiteName), data);
            });
        }
        if (counter >= numberOfTests) {
            driver.quit();
        }
    });

    it("should be on correct page", function(done) {
        driver.get(TEST.configuration.baseUrl);
        setTimeout(function() {
            driver.getTitle().then(function(title) {
                setTimeout(function() {
                    expect(title).toBe("Test page");
                    done();
                });
            });
        }, 5000);
    });

    it("list of latest specifications", function(done) {
        driver.findElements({"xpath": "//div[@id=\"specpool_partial\"]/div/div[2]/table/tbody/tr"}).then(function(els) {
            expect(els.length).toBe(5);
            return els[0].getInnerHtml();
        }).then(function(ee) {
            expect(ee.indexOf("title/cordova_support_test_specification_for_netbeans_7.4/8.0") > 0).toBe(true);
            done();

        });
    });

    it("user button", function(done) {
        var b = driver.findElement({"id": "usermenu_user"});
        b.getInnerHtml().then(function(content) {
            expect("tester&nbsp;<b class=\"caret\" id=\"userCaret\"></b>" === content).toBe(true);
            done();
        });
    });
});
