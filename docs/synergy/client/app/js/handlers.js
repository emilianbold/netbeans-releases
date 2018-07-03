"use strict";

angular.module("synergy.handlers", ["synergy.utils"])
        .factory("SynergyHandlers", ["SynergyUtils", function (SynergyUtils) {

                var Synergy = {};
                Synergy.interfaces = {};
                Synergy.control = {};

                Synergy.interfaces.observer = {
                    listeners: [],
                    subscribe: function (callback, event) {
                    },
                    publish: function (data, $scope, event) {
                    },
                    unsubscribe: function (fn, event) {
                    }
                };
                Synergy.interfaces.logger = {
                    level: {
                        "INFO": 2,
                        "DEBUG": 1
                    },
                    printLevel: 2,
                    title: "",
                    msg: "",
                    style: "alert-info",
                    print: false,
                    printMessage: function (title, msg, style) {
                    },
                    log: function (title, msg, level, style) {
                    },
                    error: function (title, msg, level, style) {
                    }
                };
                Synergy.interfaces.server = {
                    init: function (endpoints) {
                        this.endpoints = endpoints;
                    },
                    endpoints: {},
                    buildURL: function (endpoint, parameters) {
                    }
                };

                /**
                 * Handles file uploads
                 * @param {Array} acceptedTypes - e.g. ["image/png"]
                 * @param {String} dropElementID - ID of element where files are being dropped
                 * @param {Number} uploadLimit - file size limit in bytes
                 * @param {String} url - target URL used for uploading
                 * @param {Function} onSuccess callback
                 * @param {Function} onFail callback
                 * @returns {undefined}
                 */
                Synergy.control.FileUploader = function (acceptedTypes, dropElementID, uploadLimit, url, onSuccess, onFail) {

                    this.event = null;
                    this.url = url;
                    this.file = {};
                    var self = this;

                    var dropbox = window.document.getElementById(dropElementID);

                    function dragEnter(evt) {
                        evt.stopPropagation();
                        evt.preventDefault();
                        evt.cancelBubble = true;
                    }

                    function dragExit(evt) {
                        evt.stopPropagation();
                        evt.preventDefault();
                        evt.cancelBubble = true;
                    }

                    function dragOver(evt) {
                        evt.stopPropagation();
                        evt.preventDefault();
                        evt.cancelBubble = true;
                    }

                    /**
                     *
                     * @param {Event} evt
                     */
                    function drop(evt) {
                        evt.stopPropagation();
                        evt.preventDefault();
                        evt.cancelBubble = true;
                        var files = evt.dataTransfer.files;
                        var count = files.length;
                        if (count > 0) {
                            if (files[0].size > uploadLimit) {
                                onFail("File Too Large", "Maximum allowed size is " + uploadLimit / 1000000 + " MB", "INFO", "alert-error");
                            } else {
                                if (fileIsValid(files[0])) {
                                    handleFile(files[0]);
                                } else {
                                    onFail("Unsupported file", "Supported formats are " + printAccepted(), "INFO", "alert-error");
                                }
                            }
                        }
                    }

                    function printAccepted() {
                        var s = "";
                        for (var i = 0, max = acceptedTypes.length; i < max; i += 1) {
                            s += acceptedTypes[i] + " ";
                        }
                        return s;
                    }

                    function fileIsValid(file) {
                        if (acceptedTypes.length < 1) {
                            return true;
                        } else {
                            for (var i = 0, max = acceptedTypes.length; i < max; i += 1) {
                                if (file.type === acceptedTypes[i]) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }

                    if (dropbox !== null) {
                        if (dropbox.addEventListener) {
                            dropbox.addEventListener("dragenter", dragEnter, false);
                            dropbox.addEventListener("dragexit", dragExit, false);
                            dropbox.addEventListener("dragover", dragOver, false);
                            dropbox.addEventListener("drop", drop, false);
                        } else {
                            dropbox.attachEvent("ondragenter", dragEnter);
                            dropbox.attachEvent("ondragexit", dragExit);
                            dropbox.attachEvent("ondragover", dragOver);
                            dropbox.attachEvent("ondrop", drop);
                        }
                    }

                    function showTitleDialog(evt) {
                        evt.stopPropagation();
                        evt.preventDefault();
                        evt.cancelBubble = true;
                        self.event = evt;
                        self.file = (self.event.dataTransfer.files.length > 0) ? self.event.dataTransfer.files[0] : {};
                        $("#addImageModal").modal("toggle");

                    }

                    this.uploadFileFromFileChooser = function (fdid) {
                        var file = window.document.getElementById(fdid).files[0];
                        if (file) {
                            if (file.size > uploadLimit) {

                                onFail("File Too Large", "Maximum allowed size is " + uploadLimit / 1000000 + " MB", "INFO", "alert-error");
                            } else {
                                handleFile(file);
                            }
                        }
                    };

                    this.uploadImage = function (url) {
                        self.url = url;
                        var files = self.event.dataTransfer.files;
                        var count = files.length;
                        if (self.file) {
                            if (self.file > uploadLimit) {
                                onFail("File Too Large", "Maximum allowed size is " + uploadLimit / 1000000 + " MB", "INFO", "alert-error");
                            } else {
                                if (fileIsValid(self.file)) {
                                    handleFile(self.file);
                                } else {
                                    onFail("Unsupported file", "Supported formats are " + printAccepted(), "INFO", "alert-error");
                                }
                            }
                        }
                    };


                    this.initForImages = function (container) {
                        var dropbox = window.document.getElementById(container);
                        if (dropbox !== null) {
                            dropbox.addEventListener("dragenter", dragEnter, false);
                            dropbox.addEventListener("dragexit", dragExit, false);
                            dropbox.addEventListener("dragover", dragOver, false);
                            dropbox.addEventListener("drop", showTitleDialog, false);
                        }
                    };

                    function handleFile(file) {
                        $("#progressContainer").attr("class", "progress");
                        var xhr = new window.XMLHttpRequest();
                        xhr.open("POST", self.url, true);
                        xhr.onerror = function () {
                            onFail("Upload failed", "", "INFO", "alert-error");
                            onFail("Upload failed", this.responseText, "DEBUG", "alert-error");
                        };
                        xhr.onreadystatechange = function (e) {
                            if (xhr.readyState === 4) {
                                if (xhr.status === 200) {
                                    onSuccess("Done", "Upload finished", "INFO", "alert-success", file.name, xhr.responseText);
                                } else {
                                    onFail("Upload failed", "", "INFO", "alert-error");
                                    onFail("Upload failed", this.responseText + ":" + this.status, "DEBUG", "alert-error");
                                }
                            }
                        };

                        xhr.upload.addEventListener("progress", function (event) {
                            var loaded = (parseInt(event.loaded / event.total * 100));
                            $("#progressBar").css("width", loaded + "%");
                        }, false);

                        var formData = new window.FormData();
                        formData.append("myfile", file);
                        xhr.send(formData);
                    }
                };

                /**
                 * Logger implenetation
                 * @returns {Synergy.control.SynergyLogger}
                 */
                Synergy.control.SynergyLogger = function () {

                };

                Synergy.control.SynergyLogger.prototype = Object.create(Synergy.interfaces.logger);
                Synergy.control.SynergyLogger.prototype.printMessage = function (title, msg, style) {
                    this.style = style || "alert-info";
                    this.title = title;
                    this.msg = msg.substring(0, (msg.length > 200) ? 200 : msg.length);
                    this.date = Date();
                };
                Synergy.control.SynergyLogger.prototype.showMsg = function (title, msg, style) {
                    this.style = style || "alert-info";
                    this.title = title;
                    this.msg = msg;
                    this.print = true;
                };
                Synergy.control.SynergyLogger.prototype.log = function (title, msg, level, style) {
                    if (window.console && window.console.log) {
                        window.console.log(title + ": " + msg);
                    }
                    if (this.level[level] >= this.printLevel) {
                        this.print = true;
                        this.printMessage(title, msg, style);
                    }
                };
                /**
                 * Logs only to console, never to UI
                 */
                Synergy.control.SynergyLogger.prototype.logHTTPError = function (data, status, headers, config) {
                    window.console.error("Action failed >> ");
                    if (data && data.length > 0) {
                        window.console.error(data);
                    }
                    window.console.error("HTTP Status code: " + status);
                    window.console.error(config);

                    window.console.error(" <<");
                };

                Synergy.control.SynergyLogger.prototype.error = function (title, msg, level, style) {
                    if (window.console && window.console.error) {
                        window.console.error(title + ": " + msg);
                    }
                    if (this.level[level] >= this.printLevel) {
                        this.print = true;
                        this.printMessage(title, msg, style);
                    }
                };

                /**
                 * Observer implementation
                 * @returns {Synergy.control.SynergyObserver}
                 */
                Synergy.control.SynergyObserver = function () {

                };

                Synergy.control.SynergyObserver.prototype = Object.create(Synergy.interfaces.observer);
                Synergy.control.SynergyObserver.prototype.subscribe = function (callback, event) {
                    event = event || "any";
                    if (typeof this.listeners[event] === "undefined") {
                        this.listeners[event] = [];
                    }
                    this.listeners[event].push(callback);
                };
                Synergy.control.SynergyObserver.prototype.publish = function (data, $scope, event) {
                    event = event || "any";
                    var subs = this.listeners[event] || [];
                    for (var i = 0, max = subs.length; i < max; i += 1) {
                        subs[i]($scope, data, event);
                    }
                };
                Synergy.control.SynergyObserver.prototype.unsubscribe = function (fn, event) {
                    event = event || "any";
                    var subs = this.listeners[event] || [];
                    for (var i = 0, max = subs.length; i < max; i += 1) {
                        if (subs[i] === fn) {
                            subs.splice(i, 1);
                        }
                    }
                };

                /**
                 * Server implementation
                 * @param {type} endpoints
                 * @returns {SynergyServer}
                 */
                Synergy.control.SynergyServer = function (endpoints) {
                    var self = this;
                    this.endpoints = endpoints;
                    this.patterns = {
                        "php": function (endpoint, parameters) {
                            var url = "";
                            for (var parameter in parameters) {
                                if (parameters.hasOwnProperty(parameter)) {
                                    try {
                                        url += "&" + parameter + "=" + encodeURIComponent(parameters[parameter]);
                                    } catch (e) {
                                        window.console.error(e);
                                    }
                                }
                            }
                            return (url.length > 1) ? self.endpoints[endpoint] + "?" + url : self.endpoints[endpoint];
                        }
                    };
                };
                Synergy.control.SynergyServer.prototype = Object.create(Synergy.interfaces.server);
                Synergy.control.SynergyServer.prototype.preferredPattern = "php";
                /**
                 * 
                 * @param {String} key to find URL by in Synergy.control.SynergyServer().endpoints
                 * @param {Object} parameters object with all parameters
                 */
                Synergy.control.SynergyServer.prototype.buildURL = function (endpoint, parameters) {
                    return this.patterns[this.preferredPattern].call(undefined, endpoint, parameters);
                };
                /**
                 * @class text
                 * @param {type} allData
                 * @returns {undefined}
                 */
                Synergy.control.ArchiveDataFilter = function (allData) {

                    var filteredAssignments = {};
                    this.allData = allData;
                    this.allData.testRun = SynergyUtils.shallowClone(allData.testRun);
                    this.allData.testRun.membersCount = 0;
                    this.allData.testRun.completed = 0;
                    this.allData.testRun.total = 0;
                    var allIssues = SynergyUtils.shallowCloneArray(allData.issues);
                    this.allData.issues = [];
                    var self = this;
                    /**
                     * Filters data to contain only assignments where last updated is between given timestamps
                     * @param {Number} start timestamp
                     * @param {Number} stop timestamp
                     */
                    this.getData = function (start, stop) {
                        var _user;
                        var _lu;
                        for (var key in this.allData.assigneesOverview) {
                            if (this.allData.assigneesOverview.hasOwnProperty(key)) {
                                _user = this.allData.assigneesOverview[key];
                                for (var i = 0, max = _user.assignments.length; i < max; i++) {
                                    if (_user.assignments[i].hasOwnProperty("lastUpdated")) {
                                        _lu = lastUpdatedToTimestamp(_user.assignments[i].lastUpdated);
                                        if (_lu >= start && _lu <= stop) {
                                            addAssignment(_user, _user.assignments[i], key);
                                        }
                                    }
                                }
                            }
                        }

                        var _reviews = [];

                        for (var ii = 0, maxx = this.allData.reviews.length; ii < maxx; ii++) {
                            if (this.allData.reviews[ii].hasOwnProperty("lastUpdated")) {
                                _lu = lastUpdatedToTimestamp(this.allData.reviews[ii].lastUpdated);
                                if (_lu >= start && _lu <= stop) {
                                    _reviews.push(this.allData.reviews[ii]);
                                }
                            }
                        }
                        this.allData.reviews = _reviews;
                        this.allData.assigneesOverview = filteredAssignments;
                        return this.allData;
                    };

                    function addAssignment(user, assignment, username) {
                        if (!filteredAssignments.hasOwnProperty(username)) {
                            filteredAssignments[username] = {
                                name: user.name,
                                tribes: [],
                                assignments: []
                            };

                            for (var i = 0, max = user.tribes.length; i < max; i++) {
                                filteredAssignments[username].tribes[i] = SynergyUtils.shallowClone(user.tribes[i]);
                            }
                            self.allData.testRun.membersCount++;
                        }
                        self.allData.testRun.total += assignment.totalCases;
                        self.allData.testRun.completed += assignment.completedCases;
                        filteredAssignments[username].assignments.push(assignment);
                        addAssignmentIssue(assignment);
                    }

                    function addAssignmentIssue(assignment) {
                        if (assignment.issues.length < 1) {
                            return;
                        }
                        var _id;
                        var matchingIssue;
                        var matchingFullIssue;

                        var filterA = function (e) {
                            return e.bugId === _id;
                        };

                        for (var i = 0, max = assignment.issues.length; i < max; i++) {
                            _id = parseInt(assignment.issues[i], 10);
                            matchingIssue = self.allData.issues.filter(filterA);

                            if (matchingIssue.length < 1) {
                                matchingFullIssue = allIssues.filter(filterA);
                                if (matchingFullIssue.length > 0) {
                                    self.allData.issues.push(matchingFullIssue[0]);
                                }
                            }
                        }
                    }

                    function lastUpdatedToTimestamp(d) {
                        if (d.length < 1) {
                            return -1;
                        }
                        try {
                            var _d = new Date();
                            var _split = d.split(" "); //17 Feb 2014 09:39:24 UTC
                            _d.setUTCFullYear(parseInt(_split[2], 10));
                            _d.setUTCMonth(SynergyUtils.monthsNames.shortName.indexOf(_split[1]));
                            _d.setUTCDate(parseInt(_split[0], 10));
                            var _split2 = _split[3].split(":");
                            _d.setUTCHours(parseInt(_split2[0], 10));
                            _d.setUTCMinutes(parseInt(_split2[1], 10));
                            _d.setUTCSeconds(parseInt(_split2[2], 10));
                            return  _d.getTime();
                        } catch (e) {
                            return -1;
                        }
                    }

                };

                return Synergy.control;
            }])
        .factory("SynergyIssue", [function () {

                var Issue = {};
                /**
                 * @class RunIssuesCollector
                 * @returns {RunIssuesCollector}
                 */
                Issue.RunIssuesCollector = function () {

                    this.issuesStats = {
                        total: 0,
                        opened: 0,
                        P1: 0,
                        P2: 0,
                        P3: 0,
                        P4: 0,
                        unknown: 0,
                        P1Issues: [],
                        P2Issues: [],
                        P3Issues: [],
                        P4Issues: [],
                        unknownIssues: [],
                        unresolvedIssues: []
                    };
                    this.issues = {};

                    this.addIssue = function (issue) {
                        if (!this.issues[issue.bugId + "_"]) {
                            this.issuesStats.total++;
                            this.issuesStats[issue.priority]++;
                            if (issue.status.toLowerCase() !== "resolved" && issue.status.toLowerCase() !== "closed" && issue.status.toLowerCase() !== "verified" && issue.status.toLowerCase() !== "unknown") {
                                this.issuesStats.opened++;
                                this.issuesStats.unresolvedIssues.push(issue);
                            }
                            this.issues[issue.bugId + "_"] = issue;
                            this.issuesStats[issue.priority + "Issues"].push(issue);
                        }
                    };

                    this.addIssues = function (bugs) {
                        for (var i = 0, max = bugs.length; i < max; i++) {
                            this.addIssue(bugs[i]);
                        }
                    };
                };
                return Issue;
            }]).factory("TestRunCoverageHandler", ["VersionHandler", function (versionHandler) {

        function TestRunCoverageHandler() {}

        TestRunCoverageHandler.prototype.getLatestMatchingSpecification = function (specificationContainer, selectedVersions, platforms) {
            var maxIndex = -1;
            var _val;
            // get latest version 
            var allMatchingIds = []; // all ids across all versions
            var allMatchingVersions = []; // all versions across all versions
            var matching = []; // all versions across all versions
            for (var j = 0, maxj = selectedVersions.length; j < maxj; j++) { // go over versions; latest first
                for (var i = 0, max = specificationContainer.specifications.length; i < max; i++) { // This will as a side effect also filter out all specifications that don't exists in selected version
                    _val = versionHandler.getVersionFloatValue(specificationContainer.specifications[i].version);
                    if (_val === selectedVersions[j].floatValue && specificationContainer.specifications[i].id !== -1) {
                        // selectedVersions are ordered decreasingly so first matched version is the latest version 
                        maxIndex = i;
                        break;
                    }
                }
                // so that if we found specification with given version, no need to
                // conttinue looking. This will as a side effect also filter out
                // all specifications that don't exists in selected version
                if (maxIndex !== -1) {
                    break;
                }
            }
            if (maxIndex === -1) {
                return null;
            } else {
                // need to get all possible versions of given assignment so that 
                // if there are 2 assigmnets for the "same specification" but different 
                // version (e.g. 8.0 and 7.4), we can use both assigments to get coverage numbers
                specificationContainer.specifications.forEach(function (s) {
                    if (s.id !== -1) { // if specification ID is !== -1, it means given spec exists for given version
                        allMatchingIds.push(s.id);
                        allMatchingVersions.push(s.version);
                        matching.push({
                            specificationId: s.id,
                            version: s.version
                        });
                    }
                });
                return {
                    id: specificationContainer.specifications[maxIndex].id,
                    isVisible: true,
                    title: specificationContainer.title,
                    version: specificationContainer.specifications[maxIndex].version,
                    coverage: generateResults(platforms),
                    matchingIds: allMatchingIds,
                    matchingVersions: allMatchingVersions,
                    matching: matching
                };
            }
        };

        /**
         * Sets data for specification row for each platform
         */
        TestRunCoverageHandler.prototype.collectSpecificationsTotalsPerPlatform = function (specifications, assignments, platforms, selectedVersions) {
            var platformsMap = platformArrayToObj(platforms);
            var foundInfo;
            for (var i = 0, max = specifications.length; i < max; i++) {// go over all specifications to find matching assignments
                for (var j = 0, maxj = assignments.length; j < maxj; j++) {
                    if (specifications[i].isVisible && platformsMap[assignments[j].platform].isVisible) { // increase coverage in given platform by matching assignment
                        foundInfo = getMatchingInfo(assignments[j].specificationId, specifications[i].matching);
                        if (foundInfo !== null && isAssigmentForAnyVersion(foundInfo.version, selectedVersions)) {
                            specifications[i].coverage[assignments[j].platform].totalCases += assignments[j].total;
                            specifications[i].coverage[assignments[j].platform].finishedCases += assignments[j].completed;
                        }
                    }
                }
            }
            return specifications;
        };


        function getMatchingInfo(specificationId, matchingSpecs) {
            for (var i = 0, max = matchingSpecs.length; i < max; i++) {
                if (matchingSpecs[i].specificationId === specificationId) {
                    return matchingSpecs[i];
                }
            }
            return null;
        }

        /**
         * To avoid if you select e.g. version 2 in code coverage, it will show also
         * test assignments against version 1
         */
        function isAssigmentForAnyVersion(assignmentVersion, selectedVersions) {
            for (var i = 0, max = selectedVersions.length; i < max; i++) {
                if (selectedVersions[i].name === assignmentVersion) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Sets data for total value for each specification accross all platforms
         */
        TestRunCoverageHandler.prototype.getSpecificationTotalAcrossPlatforms = function (specifications, platforms) {
            var results = [];
            var totalCases = 0;
            var finishedCases = 0;
            for (var i = 0, max = specifications.length; i < max; i++) {
                totalCases = 0;
                finishedCases = 0;
                for (var j = 0, maxj = platforms.length; j < maxj; j++) {
                    if (platforms[j].isVisible && specifications[i].isVisible) {
                        totalCases += specifications[i].coverage[platforms[j].name].totalCases;
                        finishedCases += specifications[i].coverage[platforms[j].name].finishedCases;
                    }
                }
                results.push({totalCases: totalCases, finishedCases: finishedCases});
            }
            return results;
        };

        TestRunCoverageHandler.prototype.resetColumn = function (specifications, platforms) {
            for (var i = 0, max = specifications.length; i < max; i++) {// go over all specifications to find matching assignments
                specifications[i].total.totalCases = 0;
                specifications[i].total.finishedCases = 0;
                for (var j = 0, maxj = platforms.length; j < maxj; j++) {
                    specifications[i].coverage[platforms[j].name].totalCases = 0;
                    specifications[i].coverage[platforms[j].name].finishedCases = 0;
                }
            }
            return specifications;
        };

        /**
         * Returns data for Total row in table (last row)
         */
        TestRunCoverageHandler.prototype.getTotalRow = function (specifications, platforms) {
            var results = generateResults(platforms);
            for (var i = 0, max = specifications.length; i < max; i++) {
                for (var j = 0, maxj = platforms.length; j < maxj; j++) {
                    if (specifications[i].coverage.hasOwnProperty(platforms[j].name) &&
                            specifications[i].isVisible &&
                            platforms[j].isVisible) {
                        results[platforms[j].name].totalCases += specifications[i].coverage[platforms[j].name].totalCases;
                        results[platforms[j].name].finishedCases += specifications[i].coverage[platforms[j].name].finishedCases;
                    }
                }
            }
            return results;
        };

        /**
         * Returns data for total cell (all specs in all platforms)
         */
        TestRunCoverageHandler.prototype.getTotals = function (specifications, platforms) {
            var totals = {
                totalCases: 0,
                finishedCases: 0
            };
            for (var i = 0, max = specifications.length; i < max; i++) {
                for (var j = 0, maxj = platforms.length; j < maxj; j++) {
                    if (platforms[j].isVisible && specifications[i].isVisible) {
                        totals.totalCases += specifications[i].coverage[platforms[j].name].totalCases;
                        totals.finishedCases += specifications[i].coverage[platforms[j].name].finishedCases;
                    }
                }
            }
            return totals;
        };

        /**
         * 
         * @param {type} specifications
         * @param {type} specificationSizeCache cache to check if given spec's size is already known
         * @returns {Object} object with array property called "ids" and array contains spec IDs
         */
        TestRunCoverageHandler.prototype.getEmptySpecifications = function (specifications, specificationSizeCache) {
            var obj = {
                ids: []
            };
            for (var i = 0, max = specifications.length; i < max; i++) {
                for (var platform in specifications[i].coverage) {
                    if (specifications[i].coverage.hasOwnProperty(platform) &&
                            specifications[i].coverage[platform].totalCases === 0 &&
                            !specificationSizeCache.hasOwnProperty(specifications[i].id + "") &&
                            obj.ids.indexOf(specifications[i].id) < 0) {
                        obj.ids.push(specifications[i].id);
                    }
                }
            }
            return obj;
        };

        TestRunCoverageHandler.prototype.addCaseCounts = function (specifications, sizeMap) {
            for (var i = 0, max = specifications.length; i < max; i++) {
                for (var platform in specifications[i].coverage) {
                    if (specifications[i].coverage.hasOwnProperty(platform) &&
                            specifications[i].coverage[platform].totalCases === 0) {
                        specifications[i].coverage[platform].totalCases += sizeMap[specifications[i].id];
                    }
                }
            }
            return specifications;
        };

        function platformArrayToObj(platforms) {
            var obj = {};
            for (var i = 0, max = platforms.length; i < max; i++) {
                obj[platforms[i].name] = platforms[i];
            }
            return obj;
        }

        function generateResults(platforms) {
            var o = {};
            for (var i = 0, max = platforms.length; i < max; i++) {
                o[platforms[i].name] = {
                    totalCases: 0,
                    finishedCases: 0
                };
            }
            return o;
        }

        return new TestRunCoverageHandler();
    }]).factory("VersionHandler", [function () {

        function VersionHandler() {}

        var VERSION_PATTERN = /(^\d+\.)(.*)/;

        VersionHandler.prototype.getVersionFloatValue = function (version) {
            var result = VERSION_PATTERN.exec(version);
            if (result !== null && result.length >= 3) {
                var end = result[2].replace(/\./g, "");
                return parseFloat(result[1] + end);
            } else {
                return parseFloat(version);
            }
        };

        return new VersionHandler();
    }]);