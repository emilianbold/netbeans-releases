#! /usr/bin/env python

""" this daemon may be used by 'external' python sollicitors interfaces"""

__version__='$Revision: 1.4 $'
__date__='$Date: 2006/11/25 15:23:48 $'
# $Source: /cvsroot/jpydbg/jpydebugforge/src/python/jpydbg/jpydaemon.py,v $



import sys
import bdb 
import socket
import string
import traceback
import threading
import thread
import os
import time
import weakref
# import inspect
import types
import __builtin__
import dbgutils

#from dbgutils import *

HOST = '' 
PORT = 29000 # default listening port
OK   = "OK"
 
COMMAND = 0
SET_BP  = 2
DEBUG   = 31
STEP    = 4
NEXT    = 5
RUN     = 6
FREEZE  = 7 # remain on current line 
CLEAR_BP  = 8
STACK   = 9
QUIT    = 10
LOCALS  = 11
GLOBALS = 12
SETARGS = 13
READSRC = 14
COMPOSITE = 15
THREAD = 16
STEP_RETURN = 17
UNKNOWN = -1


CP037_OPENBRACKET='\xBA'
CP037_CLOSEBRACKET='\xBB'

def SetTraceForParents(frame, dispatch_func):
    frame = frame.f_back
    while frame:
        frame.f_trace = dispatch_func
        frame = frame.f_back
    del frame

# instanciate a jpyutil object
_utils = dbgutils.jpyutils()

class UntracedSources :
    """ singleton class to just prevent system python sources tracing from beeing enabled"""
    def __init__( self ) :
        self.DONT_TRACE = {
              #commonly used things from the stdlib that we don't want to trace
              'threading.py':1, 
              'Queue.py':1, 
              'socket.py':1, 
              'bdb.py':1,
              'linecache.py':1,
              #things from jpydbg that we don't want to trace
              'jpydaemon.py':1,
              'dbgutils.py':1
        }
        
    def isTraced( self , source ) :
        if self.DONT_TRACE.has_key(source): 
            return False
        return True
            
_checkTraced = UntracedSources()         

isJython = False
try :
    import java.lang
    isJython = True
except :
    pass

def _DEBUG(   message) :
    # DEBUG TRACING when things goes wrong
    from dbgutils import _debugLogger
    if _debugLogger != None :
        _debugLogger.debug(message)


class BdbQuit(Exception):
    """Bdb cloned Exception to give up completely"""


class BdbClone(bdb.Bdb) :

    def __init__( self ) :
        bdb.Bdb.__init__( self)
        
        self.running =  False
        # hide is used to prevent debug inside debugger
        self.hide = 0
        # store debugger script name to avoid debugging it's own frame
        #self.debuggerFName = os.path.normcase(sys.argv[0])
        self.debuggerFName = os.path.normcase(sys._getframe(0).f_code.co_filename)
        print self.debuggerFName
        # client debugger connection
        self._connection = None
        # debugger active information
        self.debuggee = None
        self.cmd = UNKNOWN
        # net buffer content
        self.lastBuffer = ""
        # EXCEPTION raised flag
        self.exceptionRaised = 0
        # EXCEPTION infos
        self.exceptionInfo = None
        # debuggee current 'command line arguments'
        self.debuggeeArgs = None
        # inside a command 
        self._inCommand = 0
        # last executed line exception or None
        self.lastLineException = None
        

    def connect( self ,   myhost = None , myport = PORT  )  :  
        if ( myhost == None ):
            # start in listen mode waiting for incoming sollicitors   
            print "JPyDbg listening on " , myport 
            s = socket.socket( socket.AF_INET , socket.SOCK_STREAM )
            s.bind( (HOST , myport) )
            s.listen(1)
            connection , addr = s.accept()
            self._connection = dbgutils.NetworkSession(connection)
            print "connected by " , addr
            return True
        else:
            # connect back provided listening host
            print "JPyDbg connecting " , myhost , " on port " , myport 
            try:   
                connection = socket.socket( socket.AF_INET , socket.SOCK_STREAM )
                connection.connect( (myhost , myport) )
                self._connection = dbgutils.NetworkSession(connection)
                print "JPyDbgI0001 : connected to " , host
                return True
            except socket.error, (errno,strerror):
                print "ERROR:JPyDbg connection failed errno(%s) : %s" % ( errno , strerror )
                return False

    #
    # handle EBCDIC MVS idiosynchrasies
    #
    def _mvsCp037Check( self , inStr ):
        if sys.platform != 'mvs' :
            return inStr
        inStr = inStr.replace('[',CP037_OPENBRACKET)
        inStr = inStr.replace(']',CP037_CLOSEBRACKET)
        return inStr

        
    def sendBack( self , message ) :
        """ just populate back over wire """
        self._connection.populateXmlToClient(message) 
    
    def receiveCommand( self ) :
        """ get the command over the wire """
        return self._connection.receiveCommand()
     

        
    def  stopTrace( self ) :
        bdb.Bdb.quitting = 1
        self.running = False
        sys.settrace(None)
        
        
    def run(self, cmd, myglobals=None, mylocals=None):
        """A copy of bdb's run but with a local variable added so we
        can find it it a call stack and hide it when desired (which is
        probably most of the time).
        """
        if myglobals is None:
            import __main__
            myglobals = __main__.__dict__
        if mylocals is None:
            mylocals = myglobals
        self.reset()
        if not isinstance(cmd, types.CodeType):
            cmd = cmd+'\n'
        self.running = True
        try:
            _DEBUG("*** ENTERING EXEC *** ")
            exec cmd in myglobals,mylocals
            _DEBUG("*** RETURNING EXEC ***")
            threadList = self.listThreads()
            for threadElement in threadList:
                _DEBUG("thread= " +threadElement[1] )
                    
        except BdbQuit:
            _DEBUG("*** quiting exception raised (forced debuggee quit)")
            # if exceptionInfo is None => we're leaving due to USER STOP REQUEST => just leave debugger
            #if ( self.exceptionInfo != None ) :
                # exception raised in debuggee show it back to client with debuggee error's frame
            #    _DEBUG("*** exception infor =%s " % (str(self.exceptionInfo) ) )
            #    self.populate_exception(self.exceptionInfo)
        self.running = False

    def _discardCallFrame( self , fName ) :
        """ prevent populating debugger frame back """
        if fName == self.debuggerFName or self.hide:
            self.hide = self.hide + 1
        if self.hide:     
            return True
        return False    
            
    def _debuggerContext( self , fName ) :
        # let's assume that all file located in same directory as jpydaemon
        # are debugger's modules looks reasonable
        if os.path.dirname(fName) == os.path.dirname(self.debuggerFName) :
            return True
        return False    
            
#    def trace( self , message ):
#      self.dbgTrc.write(message + '\n');
    def do_clear(self, arg):
        pass

    # bdb overwitten to capture call debug event
    def user_call(self, frame, args):
        name = frame.f_code.co_name
        if not name: name = '???'
        fn = self.canonic(frame.f_code.co_filename)
        if not fn: 
            fn = '???'
        if not self._discardCallFrame(fn) :
            self.sendBack( [ '<CALL',
                                 'cmd="'+ __builtin__.str(self.cmd)+'"' , 
                                 'fn="'+ _utils.removeForXml(fn) +'"' ,
                                 'name="'+_utils.removeForXml(name)+'"',
                                 'args="'+__builtin__.str(args)+'"' ,
                                 '/>' ]
                             )
                             
    # bdb overwitten to capture line debug event  
    def user_line(self, frame):
        import linecache
        name = frame.f_code.co_name
        if not name: name = '???'
        fn = self.canonic(frame.f_code.co_filename)
        if not fn: fn = '???'
        # populate info to client side
        line = linecache.getline(fn, frame.f_lineno)
        xmlLine =  [ '<LINE',
                               'cmd="'+ __builtin__.str(self.cmd)+'"' , 
                               'fn="'+ _utils.removeForXml(fn)+'"' ,
                               'lineno="'+__builtin__.str(frame.f_lineno)+'"' ,
                               'name="' + _utils.removeForXml(name) + '"' ,
                               'line="' + _utils.removeForXml(line.strip())+'"',
                               '/>']
        self.sendBack( xmlLine )
        
    def send_client_exception( self , cmd , content ):
        # self.trace("exception sent")
        self.sendBack( ['<EXCEPTION',
                               'cmd="'+cmd+'"' , 
                               'content="'+self._mvsCp037Check(content)+'"' ,
                              '/>'] ) 
      
                                  
    def populate_exception( self , exc_stuff):
        # self.trace("exception populated")
        if ( self.exceptionRaised == 0 ): # exception not yet processed
            _DEBUG("*** exc_stuff = %s" % (str(exc_stuff) ) )
            extype  = exc_stuff[0]
            value = exc_stuff[1]
            tb  = exc_stuff[2]
            excTrace = __builtin__.str( traceback.format_exception( extype , value , tb ) )
          
            #ex = exc_stuff
            # Deal With SystemExit in specific way to reflect debuggee's return
            if issubclass( extype , SystemExit):
                content = 'System Exit REQUESTED BY DEBUGGEE  =' + str(value)
            elif issubclass(extype, SyntaxError):  
                content = __builtin__.str(value)
                error = value[0]
                compd = value[1]
                content = 'SOURCE:SYNTAXERROR:"'+\
                       __builtin__.str(compd[0])+ '":('+\
                       __builtin__.str(compd[1])+','+\
                       __builtin__.str(compd[2])+\
                       ')'+':'+error
            elif issubclass(extype,NameError):
                content = 'SOURCE:NAMEERROR:'+__builtin__.str(value)
            elif issubclass(extype,ImportError):
                content = 'SOURCE::IMPORTERROR:'+__builtin__.str(value)
            else:
                content = __builtin__.str(value)
            # keep track of received exception
            # populate exception
            self.lastLineException = ['<EXCEPTION',
                                    'cmd="'+__builtin__.str(self.cmd)+'"' , 
                                    'content="'+ _utils.removeForXml(content)+ \
                                    '"' ,
                                    '/>']
            self.send_client_exception( __builtin__.str(self.cmd) , _utils.removeForXml(content+excTrace) )
            self.exceptionRaised = 1 # set ExceptionFlag On 
            
    # bdb overwitten to capture return debug event  
    def user_return(self, frame, retval):
        fn = self.canonic(frame.f_code.co_filename)
        if not fn: fn = '???'
        if self.hide:
            self.hide = self.hide - 1
            return None  
        self.sendBack( [  '<RETURN',
                                  'cmd="'+__builtin__.str(self.cmd)+'"' , 
                                  'fn="'+ _utils.removeForXml(fn)+'"' ,
                                  'retval="'+_utils.removeForXml(__builtin__.str(retval))+'"' ,
                                  '/>'] )
                                  
    
    # acting as stdin for commands => redirect read to the wire 
    def readline( self ):
        command = self._connection.readNetBuffer()
        if self._inCommand :
            verb , mhelp = self.commandSyntax( command )
            return mhelp
        return command[4:] # bypass CMD header for debuggee user input
      
      
    # acting as stdout => redirect to client side 
    def write( self , toPrint ):
        # transform eol pattern   
        if ( toPrint == "\n" ):
            toPrint = "/EOL/"
        self._connection.populateXmlToClient( ['<STDOUT' , 'content="'+ _utils.removeForXml(toPrint)+'"' , '/>' ] )
      
    # acting as stdout => redirect to client side 
    def writeline( self , toPrint ):
        # stdout redirection
        self.write(toPrint )
        self.write("\n")

      # stdout flush override
    def flush( self ):
        pass
    
    def listThreads( self ) :
        returned = []
        threads = threading.enumerate()
        for t in threads: 
            returned.append( ( "P" ,  t.getName()  )  )
        if isJython :            
            # get Java Running Threads
            from java.lang import Thread
            from jarray import zeros
            rootGrp = Thread.currentThread().getThreadGroup()
            while rootGrp.getParent() != None :
                rootGrp = rootGrp.getParent()
            jthreads = zeros( rootGrp.activeCount() , Thread )
            count = rootGrp.enumerate( jthreads) 
            for jt in jthreads :
                if jt != None :
                    if jt.isAlive() :
                        returned.append(  ("J"  ,  jt.getName() )  ) 
        return returned ;      

    def isSingleThreaded( self ) :
        """ 
        Check the threading Python + java to determine if we're over
        the Jython case is  due to the fact that jython will exit event if 
        awt  or java threads are running in the back
        """ 
        tList = self.listThreads() 
        _DEBUG(  'living THREAD are : %i '  % ( len(tList))  ) 
        if isJython :
            if ( len(tList) > 6) :
                return False
        else :
            if len(tList) > 1 :
                return False 
        return True
        
class JPyDbgFrame :

    def __init__(self, *args):
        #args = mainDebugger, filename, base, info, t, frame
        #yeap, much faster than putting in self and the getting it from self later on
        self._args = args[:-1]
        self._botFrame = args[-1] # store the bottom frame of thread (last instruction)

#    def checkDbgAction( self , debugger , frame , lthread ):
#        if ( debugger.cmd == DEBUG )  or ( debugger.cmd == STEP ) or ( debugger.cmd == NEXT ) or ( debugger.cmd == RUN ):
            # DEBUG STARTING event  
            # Debuggin starts stop on first line wait for NEXT , STEP , RUN , STOP ....  
#            while ( debugger.parseSubCommand( debugger.receiveCommand() , frame , lthread) == FREEZE ):
#                pass
        
    def dispatchLineAndBreak( self , debugger , frame  , lthread) :
        debugger.user_line(frame)
        _DEBUG( 'THREAD Dispatch before checkdbgAction') 
        while ( debugger.parseSubCommand( debugger.receiveCommand() , frame , lthread ) == FREEZE ):
            pass

    def printDispatchContext( self , event , frame ) :
        name = frame.f_code.co_name
        fn = frame.f_code.co_filename
        if not fn: fn = '???'
        # populate info to client side
        line =  frame.f_lineno
        backFrameLine=-1
        if frame.f_back != None :
            backFrameLine = frame.f_back.f_lineno
        _DEBUG(  (' THREAD Dispatch  entering :  event=%s , fname=%s ,  name =%s , line=%i , backframeline=%i') % (event,fn,name,line , backFrameLine)  )
                
    def checkForBeakpoint( self , mainDebugger , frame , fileName , lineNumber , lthread) :
        # just check that we reached a breakpoint in RUN mode
        _DEBUG( 'check bp  for file %s on line %i'   % (fileName,lineNumber) )
        if mainDebugger.break_here(frame)  :
            # break point have been reached => stop
            _DEBUG( 'BREAKPOINT reached')
            mainDebugger.dbgContinue = False  # continue suspended                    
            self.dispatchLineAndBreak(mainDebugger, frame,lthread)
        
    def discardedException( self,  exc_stuff  ):
        """ discard here any intermediate exception that may be received as event """
        extype  = exc_stuff[0]
        value = exc_stuff[1]
        tb  = exc_stuff[2]
        if isJython  :
            # Misc intermediate ImportError exceptions may occur 
            # during Jython import we need to ignore them ; if a module can't be
            # really imported a final ImportError will break the execution of the debuggee
            # so it's safe to ignore ImportError for Jythn here
            if  issubclass(extype,ImportError) :
                return True # discarded
        return False


    def trace_dispatch(self, frame, event, arg):
        """ debugger dispatching entry point """

        # trace Frame on entry
        self.printDispatchContext( event, frame )
        
        if event not in ('line', 'call', 'return', 'exception'):
            return None
            
        mainDebugger, filename, info, lthread = self._args
        
        fileName = frame.f_code.co_filename
        lineNumber = frame.f_lineno

        if fileName == '<string>' :
            # just discard those frames
            return None
        
        _DEBUG(  ' THREAD Dispatch  Second:  %s  '   % (  id(lthread)   )  )

        # return  mainDebugger.trace_dispatch(frame , event , arg)
        if event == 'line':
            _DEBUG( 'THREAD Dispatch before dispatch line , info.cmd=%s' %( str(info.cmd) )  ) 
            info.last_line_frame = frame # store last thread instruction
            if ( not mainDebugger.dbgContinue  )   :    
                #  not in RUN/CONTINUE
                # => handle NEXT stop in next line in current frame
                # => where for thread in NEXT context + should bypass CALLED frames 
                if ( info.cmd == NEXT )   :  # THREAD in NEXT
                    # prevent stepping in children's frame or after a 'return' event
                    if info.stop_frame  == frame.f_back   :
                        # next statement reached
                        _DEBUG( 'NEXT reached , expectedbackframeline=%i' % (info.stop_frame.f_lineno) )
                        info.cmd = None 
                        self.dispatchLineAndBreak(mainDebugger, frame , lthread )
                    # check for breakpoints as well   
                    else :
                        self.checkForBeakpoint(mainDebugger,frame,fileName,lineNumber,lthread)
                    # return trace fx in any cases    
                    return self.trace_dispatch
                else :
                    #  DEBUG starting , STEP command => stop on next line
                    #_DEBUG( 'info.cmd=% '  % (info.cmd) )
                    #  thread frame in STEP INTO or DEBUG initial start 
                    if  info.cmd == STEP  or  mainDebugger.cmd == DEBUG :  
                        _DEBUG( 'STEP reached')
                        info.cmd = None 
                        self.dispatchLineAndBreak(mainDebugger, frame , lthread ) 
                    return self.trace_dispatch
            else :
                # just check that we reached a breakpoint in RUN mode
                self.checkForBeakpoint(mainDebugger,frame,fileName,lineNumber,lthread)
            # return trace fx in any cases    
            return self.trace_dispatch
            
        if event == 'call':
            # just dispatch to client side for any interest
            _DEBUG( 'THREAD Dispatch before dispatch call')
            mainDebugger.dispatch_call(frame, arg)
            return self.trace_dispatch
            
        if event == 'return':
            # just dispatch to client side for any interest
            _DEBUG( 'THREAD Dispatch before dispatch return , info.cmd=%s' %( str(info.cmd) ) ) 
            mainDebugger.dispatch_return(frame, arg)
            if info.stop_frame == frame.f_back :
                # set STOP in next backFrame
                info.stop_frame = info.stop_frame.f_back
            return self.trace_dispatch
            
        if event == 'exception':
            # set user exception info to be able to populate to client side later 
            # mainDebugger.user_exception(frame,arg)
            if not self.discardedException(arg) :
                mainDebugger.populate_exception(arg)
                sys.settrace(None)
                # leave debuggee
                raise BdbQuit
            return self.trace_dispatch
              
            
        return None


        
class ExtraThreadInfos :
    """ 
    used to provide complementary debugging context for standard PythonThreads object
    including a tracing routine for all threads
    """
    def __init__ (self , dbg ) :
        self.stop_frame = None
        self.last_line_frame = None
        self.cmd = None
        self.notify_kill = False
        self._lock = threading.Lock()
        self.dbg = dbg

    def CreateDbFrame(self, mainDebugger, filename, additionalInfo, t, frame):
        #the frame must be cached as a weak-ref (we return the actual db frame -- which will be kept
        #alive until its trace_dispatch method is not referenced anymore).
        
        #  
        db_frame = JPyDbgFrame(mainDebugger, filename, additionalInfo, t, frame)
        db_frame.frame = frame
        return db_frame
    
        
        
        
    def __str__(self):
        return 'Stop:%s Cmd: %s Kill:%s ' % ( self.stop_frame, self.cmd, self.notify_kill)

#===================================================
# just host the main JpyDbg debug trace main hook
#===================================================
class JpyDbgTracer :

    def __init__( self  ,  dbg ) :
        self._dbg  =  dbg
        self._running_threads_ids = {}
    
    def processThreadNotAlive(self, threadId):
        """ if thread is not alive, cancel trace_dispatch processing """
        mythread = self._running_threads_ids.get(threadId, None)
        if mythread is None:
            return
         # remove from threadlists        
        del self._running_threads_ids[threadId]
        # TODO : cancel thread trace_dispatch processing

        
    def trace_dispatch(self, frame, event, arg):
        "''' This is the callback used when we enter some context in the JpyDbg debugger """
        
        try:
            f = frame.f_code.co_filename    
            filename, base = os.path.split(f)
            if not _checkTraced.isTraced(base) :
                #we don't want to debug threading or anything in jpydbg code
                return None
    
            _DEBUG(  '**** NEW FRAME : trace_dispatch :  frame < base %s ,lineno %s ,event %s ,code %s>' % ( base, frame.f_lineno, event, frame.f_code.co_name) )
    
    
            # capture the current thread info which goes with received stack
            t = threading.currentThread()
            
            # if thread is not alive, cancel trace_dispatch processing
            if not t.isAlive():
                self.processThreadNotAlive(id(t))
                return None # suspend tracing

            if not hasattr( t , 'additionalInfo' ) :
                t.additionalInfo = additionalInfo = ExtraThreadInfos( self._dbg )
            else :
                additionalInfo = t.additionalInfo
            
            #always keep a reference to the topmost frame so that we're able to start tracing it (if it was untraced)
            #that's needed when a breakpoint is added in a current frame for a currently untraced context.
            
            #each new frame...
            dbFrame = additionalInfo.CreateDbFrame(self._dbg, filename, additionalInfo, t, frame)
            return dbFrame.trace_dispatch(frame, event, arg)
        except:
            traceback.print_exc()
            return None
        
#===================================================
# Main debugging frontend class
#===================================================        
class JPyDbg(BdbClone) :

    def __init__(self):
        BdbClone.__init__(self)
        # frame debuggee contexts
        self.globalContext = None
        self.localContext = None 
        self.verbose = 0
        self.dbgTracer =  None 
        self.stdout = sys.stdout
        self.stdin = sys.stdin
        self.dbgstdout = self
        self.dbgstdin = self
        self.dbgContinue = False
      
    def parsedReturned( self , command = 'COMMAND' , argument = None , message = None , details = None ):
        parsedCommand = []
        parsedCommand.append(command)
        parsedCommand.append(argument)
        parsedCommand.append(message)
        parsedCommand.append(details)
        return parsedCommand

        
    def buildEvalArguments( self , arg ):
        posEqual = arg.find('=')
        if posEqual == -1:
            return None,None # Syntax error on provided expession couple
        return arg[:posEqual].strip() , arg[posEqual+1:].strip()

    #
    # parse & execute buffer command 
    #
    def dealWithCmd( self , 
                     verb , 
                     arg , 
                     myGlobals = globals() , 
                     myLocals = locals() 
                   ):
        #cmd = COMMAND
        msgOK = OK
        cmdType = "single"
        silent , silentarg = self.commandSyntax( arg )
        if silent == 'silent':
            arg = silentarg # consume
            # "exec" is the magic way which makes 
            # used debuggees dictionaries updatable while 
            # stopped in debugging hooks
            cmdType = "exec"  
            msgOK = silent
        # we use ';' as a python multiline syntaxic separator 
        arg = string.replace(arg,';','\n')
        # execute requested dynamic command on this side
        try:
            # redirect screen and keyboard io to jpydaemon
            oldstd = sys.stdout
            oldstdin = sys.stdin
            sys.stdout=self
            sys.stdin =self
            self._inCommand = 1
            code = compile( arg ,"<string>" , cmdType)  
            exec code in myGlobals , myLocals
            sys.stdout=oldstd
            sys.stdin =oldstdin
            self._inCommand = 0
            return _utils.parsedReturned( argument = arg , message = msgOK ) 
        except:
            try: 
                return _utils.populateCMDException(arg,oldstd)
            except:
                tb , exctype , value = sys.exc_info()
                excTrace = traceback.format_exception( tb , exctype , value )
                print excTrace
          
    #
    # build an xml CDATA structure
    # usage of plus is for jpysource.py xml CDATA encapsulation of itself
    #
    def CDATAForXml( self , data ):
        if sys.platform == 'mvs' :
            return '<'+'!'+ CP037_OPENBRACKET + 'CDATA' + \
               CP037_OPENBRACKET + data + \
               CP037_CLOSEBRACKET+ CP037_CLOSEBRACKET+'>'
        else:
            return '<'+'![CDATA['+ data + ']'+']>'
      
    #
    # parse & execute buffer command
    #
    def dealWithRead( self , verb , arg ):
        #cmd = READSRC
        # check python code and send back any found syntax error
        if arg == None:
            return _utils.parsedReturned( message = "JPyDaemon ReadSrc Argument missing")
        try:
            arg , lineno = _utils.nextArg(arg)  
            candidate = open(arg) # use 2.1 compatible open builtin for Jython
            myBuffer = _utils.parsedReturned( argument = arg , message=OK )
          # 
          # append the python source in <FILEREAD> TAG
            myBuffer.append( ['<FILEREAD' ,
                              'fn="'+arg+'"' ,
                              'lineno="'+__builtin__.str(lineno)+'">' +
                              self.CDATAForXml(self._mvsCp037Check(candidate.read())) +
                              '</FILEREAD>' ] )
            return myBuffer
        except IOError, e:
            return _utils.parsedReturned( argument = arg , message = e.strerror )
    #
    # parse & execute buffer command
    #
    def dealWithSetArgs( self , arg ):
        #cmd = SETARGS
        # populate given command line argument before debugging start
        # first slot reserved for program name 
        self.debuggeeArgs = [''] # nor args provided
        if arg != None:
            # loop on nextArg
            current , remainder = _utils.nextArg(arg)
            while current != None :
                self.debuggeeArgs.append(current)
                current , remainder = _utils.nextArg(remainder)
          
          # self.debuggeeArgs = string.split(arg)
        sys.argv = self.debuggeeArgs # store new argument list ins sys argv
        return _utils.parsedReturned( argument = arg , message = OK ) 

    def runIt(self, filename):
        # Start with fresh empty copy of globals and locals and tell the script
        # that it's being run as __main__ to avoid scripts being able to access
        # the tpdb.py namespace.
        mainpyfile = self.canonic(filename)
        globals_ = {"__name__"     : "__main__",
                    "__file__"     : mainpyfile,
                    "__builtin__" : __builtin__ ,
                    }
        locals_ = globals_
        statement = 'execfile( "%s")' % filename
        self.running = True
        # set debuggng traces
        self.dbgTracer = JpyDbgTracer(self)
        sys.settrace ( self.dbgTracer.trace_dispatch )
        self.run(statement, myglobals=globals_, mylocals=locals_)
        if ( self.isSingleThreaded()    ) :   
            _DEBUG( '**** DEBUGGING IS OVER for current debuggee') 
            self.quiting()
            return "ENDED"
            
        return "INPROGRESS"


    # load the candidate source to debug
    # Run under debugger control 
    def dealWithDebug( self , verb , arg ):
        self.cmd = DEBUG
        if self.debuggee == None:
            result = "source not found : " + arg
            for dirname in sys.path:
                fullname = os.path.join(dirname,arg)
                if os.path.exists(fullname):
                    # Insert script directory in front of module search path
                    # and make it current path (#sourceforge REQID 88108 fix)
                    debugPath = os.path.dirname(fullname)
                    sys.path.insert(0, debugPath)
                    if (  len(debugPath) != 0 ):
                        # following test added for JYTHON support
                        if ( not _utils.isJython ):
                            # chdir not available in jython
                            os.chdir(debugPath)
                    sys.stdout=self.dbgstdout
                    sys.stdin=self.dbgstdin
                    self.debuggee = fullname
                    sys.argv[0] = fullname # keep sys.argv in sync
                    # apply DEBUG rule to threading as well  
                    # NB : jython does not implement this function so test
                    if threading.__dict__.has_key("settrace"):
                        threading.settrace(self.trace_dispatch)
                    result = self.runIt(fullname)
                else :
                    print "inexisting debugee's file : " , fullname
                    return None
                break 
        else:
            result = "debug already in progress on : " + self.debuggee   
        return _utils.parsedReturned( command = 'DEBUG' , argument = arg , message = result ) 
    
    def formatStackElement( self , element ):
        curCode = element[0].f_code
        fName = curCode.co_filename
        line  =  element[1]
        if ( fName == '<string>' ):
            return ("program entry point")
        return _utils.removeForXml(fName + ' (' + __builtin__.str(line) + ') ')
    
    # populate current stack info to client side 
    def dealWithStack( self , frame ):
        stackList , size = self.get_stack ( frame , None )
        stackList.reverse() 
        xmlStack = ['<STACKLIST>' ] 
        for stackElement in stackList:
            xmlStack.append('<STACK')
            xmlStack.append('content="'+ self.formatStackElement(stackElement) +'"')
            xmlStack.append( '/>')
        xmlStack.append('</STACKLIST>') 
        self._connection.populateXmlToClient( xmlStack )

    # populate current threads infos to client side 
    def dealWithThread( self  ):
        threadList = threading.enumerate() 
        curThread = threading.currentThread()
        xmlThread = ['<THREADLIST>' ] 
        for threadElement in threadList:
            if curThread == threadElement :
                current = "true"
            else:
                current = "false" 
            xmlThread.append('<THREAD')
            xmlThread.append('name="'+ threadElement.getName()+'" current="'+current+'"')
            xmlThread.append( '/>')
        xmlThread.append('</THREADLIST>') 
        self._connection.populateXmlToClient( xmlThread )

    # populate requested disctionary to client side
    def dealWithVariables( self , frame , type , stackIndex  ):
        # get the stack frame first   
        stackList , size = self.get_stack ( frame , None )
        stackList.reverse() 
        stackElement = stackList[int(stackIndex)]
        if ( type == 'GLOBALS' ):
            variables = stackElement[0].f_globals
        else:
            variables = stackElement[0].f_locals
        xmlVariables = ['<VARIABLES type="'+type+'">' ]
        for mapElement in variables.items():
            xmlVariables.append('<VARIABLE ')
            xmlVariables.append('name="'+ _utils.removeForXml(mapElement[0])+'" ')
            xmlVariables.append('content="'+ _utils.removeForXml(__builtin__.str(mapElement[1]))+'" ')
            xmlVariables.append('vartype="'+ self.getVarType(mapElement[1])+'" ')
            xmlVariables.append( '/>')
        xmlVariables.append('</VARIABLES>') 
        self._connection.populateXmlToClient( xmlVariables )
    
    # return true when selected element is composite candidate
    def isComposite( self , value ):
        if isinstance(value , types.DictType ) :
            return 0 
        elif isinstance(value , types.ListType ) :
            return 0 
        elif isinstance(value , types.TupleType ) :
            return 0 
        elif not ( isinstance(value , types.StringType ) or \
               isinstance(value , types.ComplexType ) or \
               isinstance(value , types.FloatType ) or \
               isinstance(value , types.IntType ) or \
               isinstance(value , types.LongType ) or \
               isinstance(value , types.NoneType ) or \
               isinstance(value , types.UnicodeType ) ):
            return 1
        else:
            return 0

    # return true when selected element is composite candidate
    def getSimpleType( self , value ):
        if  isinstance( value , types.StringType ):
            return 'String'
        elif isinstance( value , types.ComplexType ):
            return 'ComplexNumber'
        elif isinstance( value , types.FloatType ):
            return 'Float' 
        elif isinstance( value , types.IntType ):
            return 'Integer' 
        elif isinstance( value , types.LongType ):
            return 'Long'
        elif isinstance(value , types.NoneType ):
            return 'None'  
        elif isinstance( value , types.UnicodeType ) :
            return 'Unicode'
        else:
            return 'UNMANAGED DATA TYPE'

    # return true when selected element is map
    def isMap ( self , value ) :
        if isinstance(value , types.DictType ) :
            return 1
        return 0 

    # return true when selected element is List
    def isList ( self , value ) :
        if isinstance(value , types.ListType ) :
            return 1
        return 0 
       
    # return true when selected element is List
    def isTuple ( self , value ) :
        if isinstance(value , types.TupleType ) :
            return 1
        return 0 
       
    # return true when selected element is composite candidate
    def getVarType( self , value ):
        if self.isComposite(value):
            return 'COMPOSITE'
        else:
            if self.isMap( value):
                return 'MAP'
            elif self.isList( value):
                return 'LIST'
            elif self.isTuple( value):
                return 'TUPLE'
            return self.getSimpleType(value)
   
    def populateVariable( self , xmlVariables , name , value ) :
        xmlVariables.append('<VARIABLE ')
        xmlVariables.append('name="'+ name +'" ')
        xmlVariables.append('content="'+ _utils.removeForXml(__builtin__.str(value)) +'" ')
        xmlVariables.append('vartype="'+ self.getVarType(value)+'" ')
        xmlVariables.append( '/>')
        

    # populate a variable XML structure back 
    def dealsWithComposites( self , oName , myGlobals , myLocals ):
        xmlVariables = ['<VARIABLES>' ]
        try :
            myObject = eval(oName ,  myGlobals , myLocals )
            if self.isList(myObject) or self.isTuple(myObject) :
                self.dealsWithLists( xmlVariables , myObject )  
            elif self.isMap(myObject) : 
                self.dealsWithMaps( xmlVariables , myObject )  
            else:
                # standard composite cases
                for key in dir(myObject):
                    try :
                        value = getattr(myObject, key)
                        #if self.isComposite(value):
                        self.populateVariable( xmlVariables , _utils.removeForXml(key) , value )
                    except :
                        # since many kind of exception may arise here specially in jython
                        # just use a general exception case which will prevent
                        # components in exception to get displayed
                        pass
        except NameError :
            self.populateVariable( xmlVariables , _utils.removeForXml(oName) , "NameError : can't guess" )
        xmlVariables.append('</VARIABLES>') 
        self._connection.populateXmlToClient( xmlVariables )
      
    # populate a variable XML structure back Python List case 
    def dealsWithLists( self , xmlVariables , myList ):
        for ii in range( len(myList) ) :
            value = myList[ii]
            #if self.isComposite(value):
            self.populateVariable( xmlVariables , '%03i'%(ii) , value )
      
    # populate a variable XML structure back Python MAP case 
    def dealsWithMaps( self , xmlVariables , myMap  ):
        keys = myMap.keys()
        for key in keys :
            value = myMap[key]
            #if self.isComposite(value):
            self.populateVariable( xmlVariables , str(key) , value )
      
    def variablesSubCommand( self , frame , verb , arg , cmd ):
        self.cmd = cmd
        if ( arg == None ):
            arg = "0"  
        else:    
            arg , optarg = _utils.nextArg(arg) # split BP arguments  
        self.dealWithVariables( frame , verb , arg )
        self.cmd = FREEZE 
    
    
    # rough command/subcommand syntax analyzer    
    def commandSyntax( self , command ):
        self.cmd  = UNKNOWN
        verb , arg  = _utils.nextArg(command)
        return verb , arg  
    
    
    def quiting( self ):
        sys.stdout=self.stdout
        sys.stdin=self.stdin
        self.debuggee = None 
        self.set_quit()

    def parseSingleCommand( self , command ):
        verb , arg = self.commandSyntax( command )
        if ( string.upper(verb) == "CMD" ):
            return self.dealWithCmd( verb , arg )
        if ( string.upper(verb) == "READSRC" ):
            return self.dealWithRead( verb , arg )
        if ( string.upper(verb) == "SETARGS" ):
            return self.dealWithSetArgs( arg )
        elif ( string.upper(verb) == "DBG" ):
            return self.dealWithDebug( verb, arg )
        elif ( string.upper(verb) == "STOP" ):
            return None
        else:
            return _utils.parsedReturned( message = "JPyDaemon SYNTAX ERROR : " + command ) 
        
    # receive a command when in debugging state using debuggee's frame local and global
    # contexts
    def parseSubCommand( self , command , frame , lthread ):
        if ( command == None ): # in case of IP socket Failures
            return UNKNOWN
        verb , arg = self.commandSyntax( command )
        if ( string.upper(verb) == "CMD" ):
            self.populateCommandToClient( command ,
                                        self.dealWithCmd( verb ,
                                                          arg ,
                                                          myGlobals= frame.f_globals ,
                                                          myLocals = frame.f_locals
                                                        )
                                        )
            self.cmd = FREEZE

        elif ( string.upper(verb) == "READSRC" ):
            self.populateCommandToClient( command ,
                                        self.dealWithRead( verb , arg )
                                      )
            self.cmd = FREEZE
        
        elif ( string.upper(verb) == "NEXT" ):
            self.cmd = NEXT
            lthread.additionalInfo.cmd=NEXT
            # Store the backframe to check that next statement matches it
            lthread.additionalInfo.stop_frame=lthread.additionalInfo.last_line_frame.f_back
            _DEBUG( 'NEXT stop in frame=%s' %( str(lthread.additionalInfo.stop_frame.f_lineno) )  ) 

            self.set_next(frame)
        elif ( string.upper(verb) == "STEP" ):
            self.cmd = STEP
            lthread.additionalInfo.cmd=STEP
            # self.set_step()
        elif ( string.upper(verb) == "RUN" ):
            self.dbgContinue = True
            self.set_continue()
        elif ( string.upper(verb) == "STOP"):
            self.cmd = QUIT  
            # raise BdbQuit exception to force debuggee termination
            raise BdbQuit

        elif ( string.upper(verb) == "BP+"):
            self.cmd = SET_BP
            # split the command line argument on the last blank
            col = string.rfind( arg, ' ' )
            arg ,optarg  = arg[:col].strip(),arg[col+1:]
            self.set_break( arg , int(optarg) )
            self.cmd = FREEZE 
        elif ( string.upper(verb) == "STACK"):
            self.cmd = STACK
            self.dealWithStack(frame)
            self.cmd = FREEZE 
        elif ( string.upper(verb) == "THREAD"):
            self.cmd = THREAD
            self.dealWithThread()
            self.cmd = FREEZE 
        elif ( string.upper(verb) == "LOCALS"):
            self.variablesSubCommand( frame , verb , arg , LOCALS )
        elif ( string.upper(verb) == "GLOBALS"):
            self.variablesSubCommand( frame , verb , arg , GLOBALS )
        elif ( string.upper(verb) == "COMPOSITE"):
            self.cmd=COMPOSITE
            arg , optarg = _utils.nextArg(arg) # split BP arguments  
            self.dealsWithComposites( arg ,  frame.f_globals ,  frame.f_locals )
            self.cmd = FREEZE
        elif ( string.upper(verb) == "BP-"):
            self.cmd = CLEAR_BP
            arg , optarg = _utils.nextArg(arg) # split BP arguments  
            self.clear_break( arg , int(optarg) )
            self.cmd = FREEZE 
        return self.cmd       
      
    # send command result back 
    def populateCommandToClient( self , command , result ):
        self._connection.populateXmlToClient( [ '<' + result[0] , 
                               'cmd="' + _utils.removeForXml(command) +'"' ,
                               'operation="' + _utils.removeForXml(__builtin__.str(result[1]))+'"' ,
                               'result="' +__builtin__.str(result[2])+'"' ,
                               '/>' ] )
        if ( result[3] != None ):
            for element in result[3]:
#               print strElement
                self._connection.populateXmlToClient( [ '<COMMANDDETAIL ' ,
                                       'content="'+ _utils.removeForXml(element)+'"',
                                       ' />'
                                      ]
                                    )
        # complementary TAG may be provided starting at position 4
        if len(result) > 4 and (result[4]!=None):
            self._connection.populateXmlToClient( result[4] )
        # mark the end of <COMMANDDETAIL> message transmission 
        self._connection.populateXmlToClient( [ '<COMMANDDETAIL/>' ] ) 
      
      
    # check and execute a received command
    def parseCommand( self , command ):
        # IP exception populating None object  
        if ( command == None ):
            return 0 # => stop daemon
      
        if ( self.verbose ):   
            print command
        result = self.parseSingleCommand(command)
        if ( result == None ):
#           self._connection.populateXmlToClient( ['<TERMINATE/>'] )
            return 0 # stop requested
        self.populateCommandToClient( command , result )
        return 1
    

    # start the deamon 
    def start( self , port = PORT , host = None , debuggee = None ,debuggeeArgs = None ):
        if not self.connect(host,port) :
            return # just leave
        welcome = [ '<WELCOME/>' ]
        # populate debuggee's name for remote debugging bootstrap
        if debuggee != None:
            welcome = [ '<WELCOME' ,  
                        'debuggee="'+ _utils.removeForXml(debuggee)]
            if debuggeeArgs != None:
                welcome.append(string.join(debuggeeArgs))
              # populate arguments after program Name
            # finally append XML closure  
            welcome.append('" />')
          
        self._connection.populateXmlToClient( welcome )
        while ( self.parseCommand( self._connection.receiveCommand() ) ):
            pass    
          
        print "'+++ JPy/sessionended/"
        sys.stdout = self.stdout
        sys.stdin = self.stdin
        self._connection.close()
#
# Instanciate a client side debugging session
#
def remoteDbgSession( localDebuggee , host , port=PORT , args = None ):
    minstance = JPyDbg()
    minstance.start( host=host , 
                     port=port , 
                     debuggee=localDebuggee ,
                     debuggeeArgs=args
                   )

# start a listening instance when invoked as main program
# without arguments
# when [host [port]] are provided as argv jpydamon will try to
# connect back host port instead of listening
if __name__ == "__main__":
    instance = JPyDbg()
    print "args = " , sys.argv
    host = _utils.consumeArgv()
    port = _utils.consumeArgv()
    if port == None:
        port = PORT
    else:
        port = int(port)
    # Jython check and support
    if ( os.name == "java" ):
        pathArgs = "JYTHONPATH"
        os.environ[pathArgs] = os.environ["PYTHONPATH"]
    else:
        pathArgs = "PYTHONPATH"
    
#    pyPathArg = _utils.consumeArgv(pathArgs)
#    if (pyPathArg != None ):
#        pythonPath = dbgutils.PythonPathHandler(pyPathArg)
#        pythonPath.getPyPathFromFile()
#    else :
    pythonPath = dbgutils.PythonPathHandler(None)
    pythonPath.getPyPathFromEnv()

    # finally get the optional local debuggee  
    localDebuggee = _utils.consumeArgv()
    print "localDebuggee=" , localDebuggee
    #
    instance.start( host=host , 
                    port=port , 
                    debuggee=localDebuggee ,
                    debuggeeArgs=sys.argv
                  )
    print "deamon ended\n"
    # ack end on client side
    
