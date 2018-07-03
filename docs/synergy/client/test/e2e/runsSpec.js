var webdriver = require("selenium-webdriver");
var driver = new webdriver.Builder().withCapabilities(webdriver.Capabilities.chrome()).build();
jasmine.getEnv().defaultTimeoutInterval = 50000;
var TEST = require("./config.js");

describe("test runs page", function() {
    var counter = 0;
    var numberOfTests = 9;
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

    it("display test runs", function(done) {
        driver.get(TEST.configuration.baseUrl + "runs");
        setTimeout(function() {
            driver.findElements({"xpath": "//div[@id=\"specpool_partial\"]/div/h1"}).then(function(headings) {
                expect(headings.length).toBe(1);
                return headings[0];
            }).then(function(heading) {
                return heading.getInnerHtml();
            }).then(function(content) {
                expect(content).toBe("Test runs");
                done();
            });
        }, 2000);
    });

    it("table row", function(done) {
        driver.findElements({"xpath": "//div[@id=\"specpool_partial\"]/div/table/tbody/tr"}).then(function(rows) {
            expect(rows.length).toEqual(1);
            done();
        });
    });
    it("single test run row", function(done) {
        driver.findElement({"xpath": "//div[@id=\"specpool_partial\"]/div/table/tbody/tr/td[2]"}).getInnerHtml().then(function(c) {
            expect(c).toBe("<a href=\"#/run/1\" class=\"ng-binding\">Sample test run</a>");
            done();
        });
    });

    it("name", function(done) {
        driver.findElement({"xpath": "//div[@id=\"specpool_partial\"]/div/table/tbody/tr/td[2]"}).getText().then(function(c) {
            expect(c).toBe("Sample test run");
            done();
        });
    });
    it("start date", function(done) {
        driver.findElement({"xpath": "//div[@id=\"specpool_partial\"]/div/table/tbody/tr/td[3]"}).getText().then(function(c) {
            expect(c).toBe("1 Nov 2013 01:00:00");
            done();
        });
    });
    it("end date", function(done) {
        driver.findElement({"xpath": "//div[@id=\"specpool_partial\"]/div/table/tbody/tr/td[4]"}).getText().then(function(c) {
            expect(c).toBe("28 Nov 2013 01:00:00");
            done();
        });
    });
    it("assignments", function(done) {
        driver.findElement({"xpath": "//div[@id=\"specpool_partial\"]/div/table/tbody/tr/td[5]"}).getText().then(function(c) {
            expect(parseInt(c, 10)).toBe(5);
            done();
        });
    });
    it("total cases", function(done) {
        driver.findElement({"xpath": "//div[@id=\"specpool_partial\"]/div/table/tbody/tr/td[6]"}).getText().then(function(c) {
            expect(parseInt(c, 10)).toBe(25);
            done();
        });
    });
    it("completed cases", function(done) {
        driver.findElement({"xpath": "//div[@id=\"specpool_partial\"]/div/table/tbody/tr/td[7]"}).getText().then(function(c) {
            expect(parseInt(c, 10)).toBe(10);
            done();
        });
    });

});
