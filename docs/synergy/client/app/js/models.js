"use strict";

angular.module("synergy.models",[])
        .factory("SynergyModels",[function () {

            var Models = {};

            Models.Project = function (id,name) {
              this.id = id;
              this.name = name;
              this.multiViewLink = null;
              this.viewLink = null;
              this.reportLink = null;
              this.bugTrackingSystem = "other";
            };
            Models.Registration = function (username,email,firstname,lastname) {
              this.username = username;
              this.firstname = firstname;
              this.lastname = lastname;
              this.email = email;
              this.password = null;
            };

            Models.UserReviewStats = function () {

              var data = {};

              this.addReview = function (review) { // all assignments for user
                if (!data.hasOwnProperty(review.username)) {
                  init(review);
                }
                data[review.username].total++;
                if (review.isFinished) {
                  data[review.username].completed++;
                }
                data[review.username].comments += review.numberOfComments;
                data[review.username].weight += review.weight;
                data[review.username].timeTaken += review.timeTaken;
              };

              this.finish = function () {
                var _indexed = [];
                for (var r in data) {
                  if (data.hasOwnProperty(r)) {
                    var _hours = Math.floor(data[r].timeTaken / 60);
                    data[r].prettyTime = (_hours > 0 ? (_hours + " hours and ") : "") + (data[r].timeTaken % 60) + " minutes";
                    _indexed.push(data[r]);
                  }
                }
                return _indexed;
              };

              function init(review) {
                data[review.username] = {
                  username: review.username,
                  name: review.userDisplayName,
                  weight: 0,
                  timeTaken: 0,
                  comments: 0,
                  total: 0,
                  completed: 0
                };
              }
            };

            Models.Product = function (name,components) {
              this.name = name;
              this.components = components.map(function (e) {
                return new Models.Component(e);
              });
            };
            Models.Component = function (name) {
              this.name = name;
            };
            Models.ReviewAssignment = function (username,reviewUrl) {
              this.username = username;
              this.reviewUrl = reviewUrl;
              this.testRunId = -1;
              this.comments = [];
              this.id = -1;
              this.owner = "";
              this.title = "";
              this.isFinished = false;
              this.timeTaken = 0;
              this.weight = 0;
            };
            Models.ReviewComment = function (reviewUrl) {
              this.reviewUrl = reviewUrl;
              this.text = "";
              this.elements = [];

              this.addElement = function (xpath,elementName) {
                this.elements.push(xpath);
              };

              this.removeElement = function (xpath) {
                for (var i = 0,
                        max = this.elements.length; i < max; i++) {
                  if (this.elements[i] === xpath) {
                    this.elements.splice(i,1);
                    return;
                  }
                }
              };
            };


            Models.Specification = function (title,description,version,owner,id) {
              this.title = title;
              this.desc = description;
              this.version = version;
              this.owner = owner;
              this.id = id;
              this.isFavorite = 0;
              this.simpleName = "";
              var self = this;
              this.testSuites = [];
              this.setSimpleName = function (simple) {
                self.simpleName = simple;
              };
              this.ext = {};
            };

            Models.Suite = function (title,description,product,component,id) {
              this.title = title;
              this.desc = description;
              this.product = product;
              this.component = component;
              this.specificationId = -1;
              this.id = id;
              this.order = -1;
              this.testCases = [];
            };

            Models.TestCase = function (title,steps,result,duration,id) {
              this.title = title;
              this.steps = steps;
              this.result = result;
              this.duration = duration;
              this.orginalDuration = duration;
              this.id = id;
              this.suiteId = -1;
              this.version = "";
              this.order = -1;
            };

            Models.Tribe = function (name,description,leaderUsername,id) {
              this.name = name;
              this.description = description;
              this.leaderUsername = leaderUsername;
              this.id = id;
            };
            Models.TribeRunStats = function (name,id,tribeSpecs) {
              Models.Tribe.call(this,name,"","",parseInt(id,10));

              /**
               * Assigns specification to tribe
               */
              function filterSpecifications() {
                for (var i = 0,
                        max = tribeSpecs.length; i < max; i++) {
                  if (parseInt(self.id,10) === parseInt(tribeSpecs[i].id,10)) {
                    for (var j = 0,
                            max2 = tribeSpecs[i].specificationIds.length; j < max2; j++) {
                      specificationIds.push(parseInt(tribeSpecs[i].specificationIds[j],10));
                    }
                    return;
                  }
                }
              }


              this.testers = []; // actually assignments...
              this.time = 0;
              this.prettyTime = "0 minutes";
              this.testedTotal = 0;
              this.passed = 0;
              this.passedRate = 0;
              this.users = 0;
              this.productivity = 0;
              var specificationIds = [];
              var self = this;
              filterSpecifications();

              /**
               * Add information from assignment to total tribe statistics (only if user is member of tribe && specification belongs to tribe)
               * @param {SimpleAssignment} assignments
               */
              this.addAssignment = function (assignments) { // all assignments for user
                var totalCases = 0;
                var totalTime = 0;
                var passedCases = 0;
                var completedCases = 0;

                for (var i = 0,
                        max = assignments.length; i < max; i++) {
                  if (specificationIds.indexOf(assignments[i].specificationId) > -1 && assignments[i].completedCases > 0) {
                    totalCases += assignments[i].totalCases;
                    totalTime += assignments[i].totalTime;
                    passedCases += assignments[i].passedCases;
                    completedCases += assignments[i].completedCases;
                  }
                }

                if (completedCases > 0) {
                  this.users++;
                  this.testedTotal += completedCases;
                  this.productivity = Math.round(10 * this.testedTotal / this.users) / 10;
                  this.time += totalTime;
                  var _hours = Math.floor(this.time / 60);
                  this.passed += passedCases;
                  this.prettyTime = (_hours > 0 ? (_hours + " hours and ") : "") + (this.time % 60) + " minutes";
                  this.passedRate = Math.round(1000 * this.passed / this.testedTotal) / 10 || 0;
                }
              };
            };

            Models.User = function (firstName,lastName,username,role,id,oldUsername) {
              this.id = id;
              this.email = null;
              this.firstName = firstName;
              this.lastName = lastName;
              this.username = username;
              this.role = role;
              this.oldUsername = oldUsername || this.username;
              this.emailNotifications = true;
              this.password = null;
            };

            Models.Version = function (name,id,isObsolete) {
              this.name = name;
              this.id = id;
              this.isObsolete = isObsolete;
            };

            Models.TestRun = function (title,description,start,end,id) {
              this.title = title;
              this.desc = description;
              this.start = start;
              this.end = end;
              this.id = id;
              this.notifications = -1;
              this.projectId = -1;
              this.projectName = null;
              this.setNotifications = function (n) {
                this.notifications = n;
                return this;
              };
            };

            Models.TestAssignment = function (platformId,username,labelId,specificationId,id) {
              this.username = username;
              this.tribeId = -1;
              this.testRunId = -1;
              this.platformId = platformId;
              this.specificationId = specificationId;
              this.labelId = labelId;
              this.id = id;
            };

            Models.Platform = function (name,id,isActive) {
              this.name = name;
              this.id = id;
              this.isActive = isActive;
            };

            Models.Job = function (jobUrl,specificationId,id) {
              this.id = id;
              this.specificationId = specificationId;
              this.jobUrl = jobUrl;
            };
            Models.OwnershipRequest = function (specificationId,authorUsername,text) {
              this.text = text;
              this.specificationId = specificationId;
              this.authorUsername = authorUsername;
            };
            Models.SpecificationContainer = function (id) {
              this.id = id;
              this.data = {};

              this.add = function (specification) {
                if (!this.data.hasOwnProperty(specification.versionId)) {
                  this.data[specification.versionId] = {
                    id: specification.id,
                    title: specification.title
                  };
                }
              };

              this.get = function (versionId) {
                return this.data[versionId];
              };
            };

            return Models;
          }]);