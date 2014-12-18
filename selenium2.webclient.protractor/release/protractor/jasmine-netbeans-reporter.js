(function() {
    if (! jasmine) {
        throw new Exception("jasmine library does not exist in global namespace!");
    }

    /**
     * Usage:
     *
     * jasmine.getEnv().addReporter(new jasmine.NetbeansReporter());
     * jasmine.getEnv().execute();
     */
    var NetbeansReporter = function() {
        this.started = false;
        this.finished = false;
        this.REPORTER_MESSAGE = 'jasmine-netbeans-reporter ';
    };

    NetbeansReporter.prototype = {

        reportRunnerStarting: function(runner) {
            this.started = true;
            this.start_time = (new Date()).getTime();
            this.n = 0;
            this.passes = 0;
            this.failures = 0;
            this.skipped = 0;
            // should have at least 1 spec, otherwise it's considered a failure
            this.log(this.REPORTER_MESSAGE + '1..'+ Math.max(runner.specs().length, 1));
        },

        reportSpecStarting: function(spec) {
            this.start_time_spec = (new Date()).getTime();
            this.n++;
        },

        reportSpecResults: function(spec) {
            var dur = (new Date()).getTime() - this.start_time_spec;

            var results = spec.results();

            if (results.passed()) {
                this.passes++;
                this.log(this.REPORTER_MESSAGE + "ok " + this.n + " " + spec.suite.description + " " + spec.description + ", suite=" + spec.suite.description + ", testcase=" + spec.description + ", duration=" + dur);
            } else if (results.skipped) {
                this.skipped++;
                this.log(this.REPORTER_MESSAGE + "ok " + this.n + " " + spec.suite.description + " " + spec.description + " # SKIP -, suite=" + spec.suite.description + ", testcase=" + spec.description);
            } else {
                this.failures++;
                this.log(this.REPORTER_MESSAGE + "not ok " + this.n + " " + spec.suite.description + " " + spec.description + ", suite=" + spec.suite.description + ", testcase=" + spec.description + ", duration=" + dur);
                var items = results.getItems();
                var i = 0;
                var errorMessage = '';
                var expectationResult, stackMessage;
                while (expectationResult = items[i++]) {
                    if (expectationResult.trace) {
                        stackMessage = expectationResult.trace.stack ? expectationResult.trace.stack : expectationResult.message;
                        errorMessage += '\n  '+ stackMessage;
                    }
                }
                // add REPORTER_MESSAGE in the beginning of each stacktrace line
                // so that they can be filtered out in TestRunnerReporter
                errorMessage = errorMessage.replace(/\n/g, '\n' + this.REPORTER_MESSAGE);
                this.log(errorMessage);
            }
        },

        reportRunnerResults: function(runner) {
            this.log(this.REPORTER_MESSAGE + 'tests ' + (this.passes + this.failures)+ ', pass ' + this.passes + ', fail ' + this.failures + ', skip ' + this.skipped);
            this.finished = true;
        },

        log: function(str) {
            var console = jasmine.getGlobal().console;
            if (console && console.log) {
                console.log(str);
            }
        }
    };

    // export public
    jasmine.NetbeansReporter = NetbeansReporter;
})();
