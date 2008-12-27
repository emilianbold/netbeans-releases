##############################################################################
# Test Runner for NetBeans, based on the unittest TextTestRunner (but prints
# out more information during execution that is parsed and swallowed by the
# NetBeans test runner GUI.)
#
# Tor Norbye <tor@netbeans.org> Dec 20 2008
##############################################################################

import sys
import time
from optparse import OptionParser
import os
from unittest import TestResult

class _NbWritelnDecorator:
    """Used to decorate file-like objects with a handy 'writeln' method"""
    def __init__(self, stream):
        self.stream = stream

    def __getattr__(self, attr):
        return getattr(self.stream, attr)

    def writeln(self, arg=None):
        if arg: self.write(arg)
        self.write('\n') # text-mode streams translate to \r\n if needed

class _NbTextTestResult(TestResult):
    """
    A test result class that can print specially formatted test status messages for use by
    the GUI test runner in the NetBeans IDE.
    """

    def __init__(self, stream):
        TestResult.__init__(self)
        self.stream = stream
        self._start_time = None

    def getDescription(self, test):
        #return test.shortDescription() or str(test)
        return str(test)

    def startTest(self, test):
        TestResult.startTest(self, test)
        self.stream.writeln("%%TEST_STARTED%% %s" % self.getDescription(test))
        self._start_time = time.time()


    def addSuccess(self, test):
        time_taken = time.time() - self._start_time
        TestResult.addSuccess(self, test)
        self.stream.writeln("%%TEST_FINISHED%% time=%.6f %s" % (time_taken, self.getDescription(test)))

    def _generate_stack(self, tb):
        # Default stack format:
        #stack = traceback.format_tb(tb)
        #stackstr = ""
        #for line in stack:
        #    stackstr += line
        #stackstr = stackstr.replace('\n', '%BR%')

        # More compact stack format
        stackstr = ""
        stack = []
        while tb:
            stack.append(tb.tb_frame)
            tb = tb.tb_next
        stack.reverse()
        for frame in stack:
            stackstr += "%s() in %s:%s%%BR%%" % (frame.f_code.co_name,
                                                 frame.f_code.co_filename,
                                                 frame.f_lineno)

        return stackstr

    def addError(self, test, err):
        time_taken = time.time() - self._start_time
        TestResult.addError(self, test, err)
        (error, message, tb) = err
        stackstr = self._generate_stack(tb)
        self.stream.writeln("%%TEST_ERROR%% time=%.6f testname=%s message=%s location=%s" %
                            (time_taken, self.getDescription(test), message, stackstr))

    def addFailure(self, test, err):
        time_taken = time.time() - self._start_time
        TestResult.addFailure(self, test, err)

        (error, message, tb) = err
        stackstr = self._generate_stack(tb)
        self.stream.writeln("%%TEST_FAILED%% time=%.6f testname=%s message=%s location=%s" %
                            (time_taken, self.getDescription(test), message, stackstr))

    def printErrors(self):
        pass

    def printErrorList(self, flavour, errors):
        pass


class _NetBeansTestRunner:
    """A test runner class that displays results in textual form.

    It prints out the names of tests as they are run, errors as they
    occur, and a summary of the results at the end of the test run.
    """
    def __init__(self, stream=sys.stdout):
        self.stream = _NbWritelnDecorator(stream)


    def _makeResult(self):
        return _NbTextTestResult(self.stream)

    def _get_suite_name(self, test):
        if (hasattr(test, "_tests")):
            name_set = set()
            for o in test._tests:
                for s in o._tests:
                    name_set.add(s.__class__)
            name = ""
            for s in name_set:
                if (len(name) > 0):
                    name += ","
                #name += s.__module__ + '.' + s.__name__
                name += s.__name__
            return name;
        else:
            class_ = test.__class__
            classname = class_.__module__ + "." + class_.__name__
            return classname

    def run(self, test):
        "Run the given test case or test suite."
        suite_name = self._get_suite_name(test)
        self.stream.writeln("%%SUITE_STARTING%% %s" % suite_name)
        result = self._makeResult()
        startTime = time.time()
        test(result)
        stopTime = time.time()
        timeTaken = stopTime - startTime
        result.printErrors()
        if not result.wasSuccessful():
            failed, errored = map(len, (result.failures, result.errors))
            if failed:
                self.stream.writeln("%%SUITE_FAILURES%% %d" % failed)
            if errored:
                self.stream.writeln("%%SUITE_ERRORS%% %d" % errored)
        else:
            print "%SUITE_SUCCESS%"
        self.stream.writeln("%%SUITE_FINISHED%% time=%.4f" % timeTaken)
        return result

##############################################################################
# Driver for running tests from NetBeans
##############################################################################
if __name__ == '__main__':
    import unittest

    parser = OptionParser(usage="%prog <[--method <name> ]--file|--directory>  <files/directories...>", version="%prog 1.0")
    parser.add_option("-f", "--file",
                      action="store_true", dest="filename",
                      help="Test the given file")
    parser.add_option("-m", "--method",
                      action="store", type="string", dest="method",
                      help="Test the given method")
    parser.add_option("-d", "--directory",
                      action="store_true", dest="directory",
                      help="Test the given directory")
    (options, args) = parser.parse_args()
    if len(args) == 0:
        parser.error("Don't forget to specify files/directories")
    if (options.method and not options.filename):
        parser.error("must specify --file if you specify --method")
    if (options.filename and options.directory):
        parser.error("--directory and --file are mutually exclusive")
    if (not options.filename and not options.directory):
        parser.error("You must specify at least one of --file, --method and --directory")

    if options.filename or options.method:
        if len(args) > 1:
            parser.error("You can only specify one file with --file")
        file_name = args[0]
        module_name = os.path.splitext(file_name)[0:-1][0]
        module = __import__(module_name, globals(), locals(), module_name)
        if (options.method):
            suite = unittest.TestLoader().loadTestsFromName(options.method, module)
        else:
            suite = unittest.TestLoader().loadTestsFromModule(module)
    else:
        assert options.directory;
        test_modules = []
        for dir in args:
            file_list = os.listdir(dir)
            for file_name in file_list:
                extension = os.path.splitext(file_name)[-1]
                if extension == '.py':
                    test_module_name = os.path.splitext(file_name)[0:-1][0]
                    try:
                        module = __import__(test_module_name, globals(), locals(), test_module_name)
                        test_modules.append(module)
                    except:
                        # No complaints - just test the files we can (user may have run
                        # test project on an unfinished project where not all files are valid)
                        pass
        suite = unittest.TestSuite(map(unittest.defaultTestLoader.loadTestsFromModule, test_modules))

    # Run all the tests
    _NetBeansTestRunner().run(suite)
