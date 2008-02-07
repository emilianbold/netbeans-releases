Binary test distribution for NetBeans 6.0
-----------------------------------------

The test distribution contains tests for NetBeans 6.0. There are two types
of tests:

unit tests - developed by development team
qa-functional tests -  developed by qa team

How to run test by xtest harness 
--------------------------------

Running unit tests:
ant unit -Dnetbeans.dest.dir=${netbeans.home}

Running qa-functional tests:
ant qa-functional -Dnetbeans.dest.dir=${netbeans.home}

Running all tests:
ant -Dnetbeans.dest.dir=${netbeans.home}

The 'netbeans.dest.dir' is required property and contains absolute path 
to directory with NetBeans 6.0 installation. 

Custom properties:

xtest.attribs - xtest atributtes, default value for qa-functional is 'validation' ,
    for unit is 'stable'.

xtest.modules.list - list of modules seperated by ':' in format ${cluster}/${code-base-name}
    example: platform8/org-openide-filesystems:platform8/org-openide-masterfs

test.required.modules - run tests only with listed modules when property is defined,
        it's applied to only 'unit' tests. 
    example: org-openide-explorer.jar,org-netbeans-modules-masterfs.jar run modules which need
          either org-openide-explorer.jar or org-netbeans-modules-masterfs.jar for test run.

Generated report:

Report is generated to unit/report folder for unit tests. 
Report is generated to qa-functional/report folder for qa-functional tests.


How to run tests by junit 3.x harness
-------------------------------------

Only unit tests can be run by junit 3.x harness:

cd unit
ant -f unit-all-unit.xml -Dnetbeans.dest.dir=${netbeans.home}

The 'netbeans.dest.dir' is required property and contains absolute path 
to directory with NetBeans 6.0 installation. 

Custom properties:

unit.modules.list - list of modules separated by ':' in format ${cluster}/${code-base-name}
    example: platform8/org-openide-filesystems:platform8/org-openide-masterfs

test.required.modules - run tests only with listed modules when property is defined
    example: org-openide-explorer.jar,org-openide-master-fs.jar runs modules which needs
          the org-openide-explorer.jar and org-openide-master-fs.jar for test run

Generated report:

Report is generated to unit/report folder.
