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

    it("filter specifications by version 1", function(done) {
        driver.get(TEST.configuration.baseUrl + "specifications");
        setTimeout(function() {
            driver.findElement({"xpath": "//div[@class=\"span8\"]/form/select/option[@value=\"1\"]"}).click().then(function() {
                driver.findElements({"xpath": "//div[@data-ng-switch]//table/tbody/tr"}).then(function(result) {
                    expect(result.length).toBe(1);
                    done();
                });
            });
        }, 5000);
    });

    it("filter specifications by version 2", function(done) {
        driver.findElement({"xpath": "//div[@class=\"span8\"]/form/select/option[@value=\"2\"]"}).click().then(function() {
            driver.findElements({"xpath": "//div[@data-ng-switch]//table/tbody/tr"}).then(function(result) {
                expect(result.length).toBe(2);
                done();
            });
        });

    });

    it("redirect to create specification page", function(done) {
        driver.findElement({"xpath": "//a[@href=\"#/specification/-1/create\"]"}).click().then(function() {
            driver.findElement({"xpath": "//div[@class=\"span8\"]/h1"}).getText().then(function(result) {
                expect(result).toBe("Create new test specification NetBeans Test Specificationa");
                done();
            });
        });
    });




});
