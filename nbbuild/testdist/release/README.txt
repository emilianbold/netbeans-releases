Binary test distribution for NetBeans 6.0
-----------------------------------------

The test distribution contains tests for NetBeans 6.0. There are two types
of tests:

unit tests - developed by development team
qa-functional tests -  developed by qa team

How to run tests by junit 3.x harness
-------------------------------------

cd unit [etc.]
ant -f ../all-tests.xml -Dbasedir=`pwd` -Dnetbeans.dest.dir=${netbeans.home}

The 'netbeans.dest.dir' is required property and contains absolute path 
to directory with NetBeans 6.0 installation. 

Custom properties:

modules.list - list of modules separated by ':' in format ${cluster}/${code-base-name}
    example: platform11/org-openide-filesystems:platform11/org-openide-masterfs

test.required.modules - run tests only with listed modules when property is defined
    example: org-openide-explorer.jar,org-openide-master-fs.jar runs modules which needs
          the org-openide-explorer.jar and org-openide-master-fs.jar for test run

Generated report:

Reports are generated to */report folders.
