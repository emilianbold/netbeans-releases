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
import os
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
UNKNOWN = -1


CP037_OPENBRACKET='\xBA'
CP037_CLOSEBRACKET='\xBB'



# instanciate a jpyutil object
_utils = dbgutils.jpyutils() 

class JPyDbg(bdb.Bdb) :
    
    def __init__(self):
        bdb.Bdb.__init__(self)
        # store debugger script name to avoid debugging it's own frame
        #self.debuggerFName = os.path.normcase(sys.argv[0])
        self.debuggerFName = os.path.normcase(sys._getframe(0).f_code.co_filename)
        print self.debuggerFName
        # client debugger connection
        self._connection = None
        # frame debuggee contexts
        self.globalContext = None
        self.localContext = None 
        self.verbose = 0
        # hide is used to prevent debug inside debugger
        self.hide = 0
        # debugger active information
        self.debuggee = None
        self.cmd = UNKNOWN
        # net buffer content
        self.lastBuffer = ""
        # EXCEPTION raised flag
        self.exceptionRaised = 0
        # debuggee current 'command line arguments'
        self.debuggeeArgs = None
        # inside a command 
        self._inCommand = 0
        # last executed line exception or None
        self.lastLineException = None
        # tracing facility
        # self.dbgTrc = file("./jpydbgtrace.TXT" , "w+")

#    def trace( self , message ):
#      self.dbgTrc.write(message + '\n');
    def do_clear(self, arg):
        pass
      
    # bdb overwitten to capture call debug event  
    def user_call(self, frame, args):
        name = frame.f_code.co_name
        if not name: name = '???'
        fn = self.canonic(frame.f_code.co_filename)
        if not fn: fn = '???'
        # discard debugger frame 
        if fn == self.debuggerFName or self.hide:
            self.hide = self.hide + 1
        if self.hide:     
            return None
        self._connection.populateXmlToClient( [ '<CALL',
                                 'cmd="'+ __builtin__.str(self.cmd)+'"' , 
                                 'fn="'+ _utils.removeForXml(fn) +'"' ,
                                 'name="'+_utils.removeForXml(name)+'"',
                                 'args="'+__builtin__.str(args)+'"' ,
                                 '/>' ]
                             )
      
      
    def checkDbgAction( self , frame ):
        if ( self.cmd == DEBUG )  or ( self.cmd == STEP ) or ( self.cmd == NEXT ) or ( self.cmd == RUN ):
            # DEBUG STARTING event  
            # Debuggin starts stop on first line wait for NEXT , STEP , RUN , STOP ....  
            while ( self._parseSubCommand( self._connection.receiveCommand() , frame ) == FREEZE ):
                pass
        
    # bdb overwitten to capture line debug event  
    def user_line(self, frame):
        if self.hide:
            return None
        import linecache
        name = frame.f_code.co_name
        if not name: name = '???'
        fn = self.canonic(frame.f_code.co_filename)
        if not fn: fn = '???'
        # populate info to client side
        line = linecache.getline(fn, frame.f_lineno)
        self._connection.populateXmlToClient( [ '<LINE',
                               'cmd="'+ __builtin__.str(self.cmd)+'"' , 
                               'fn="'+ _utils.removeForXml(fn)+'"' ,
                               'lineno="'+__builtin__.str(frame.f_lineno)+'"' ,
                               'name="' + _utils.removeForXml(name) + '"' ,
                               'line="' + _utils.removeForXml(line.strip())+'"',
                               '/>'] )
        # what's on next
        self.checkDbgAction( frame ) 
        
    # bdb overwitten to capture return debug event  
    def user_return(self, frame, retval):
        fn = self.canonic(frame.f_code.co_filename)
        if not fn: fn = '???'
        if self.hide:
            self.hide = self.hide - 1
            return None  
        self._connection.populateXmlToClient( [  '<RETURN',
                                  'cmd="'+__builtin__.str(self.cmd)+'"' , 
                                  'fn="'+ _utils.removeForXml(fn)+'"' ,
                                  'retval="'+_utils.removeForXml(__builtin__.str(retval))+'"' ,
                                  '/>'] )
    
    #
    # handle EBCDIC MVS idiosynchrasies
    #
    def _mvsCp037Check( self , inStr ):
        if sys.platform != 'mvs' :
            return inStr
        inStr = inStr.replace('[',CP037_OPENBRACKET)
        inStr = inStr.replace(']',CP037_CLOSEBRACKET)
        return inStr

    def send_client_exception( self , cmd , content ):
        # self.trace("exception sent")
        self._connection.populateXmlToClient( ['<EXCEPTION',
                               'cmd="'+cmd+'"' , 
                               'content="'+self._mvsCp037Check(content)+'"' ,
                              '/>'] ) 
      
                                  
    def populate_exception( self , exc_stuff):
        # self.trace("exception populated")
        if ( self.exceptionRaised == 0 ): # exception not yet processed
            extype  = exc_stuff[0]
            details = exc_stuff[1]
          
            #ex = exc_stuff
            # Deal With SystemExit in specific way to reflect debuggee's return
            if issubclass( extype , SystemExit):
                content = 'System Exit REQUESTED BY DEBUGGEE  =' + str(details)
            elif issubclass(extype, SyntaxError):  
                content = __builtin__.str(details)
                error = details[0]
                compd = details[1]
                content = 'SOURCE:SYNTAXERROR:"'+\
                       __builtin__.str(compd[0])+ '":('+\
                       __builtin__.str(compd[1])+','+\
                       __builtin__.str(compd[2])+\
                       ')'+':'+error
            elif issubclass(extype,NameError):
                content = 'SOURCE:NAMEERROR:'+__builtin__.str(details)
            elif issubclass(extype,ImportError):
                content = 'SOURCE::IMPORTERROR:'+__builtin__.str(details)
            else:
                content = __builtin__.str(details)
            # keep track of received exception
            # populate exception
            self.lastLineException = ['<EXCEPTION',
                                    'cmd="'+__builtin__.str(self.cmd)+'"' , 
                                    'content="'+ _utils.removeForXml(content)+ \
                                    '"' ,
                                    '/>']
            self.send_client_exception( __builtin__.str(self.cmd) , _utils.removeForXml(content) )
            self.exceptionRaised = 1 # set ExceptionFlag On 
            
        
    # bdb overwitten to capture Exception events  
    def user_exception(self, frame, exc_stuff):
      # self.trace("first exception populated")
      # capture next / step go ahead when exception is around 
      # current steatement while steping
        if self.cmd==NEXT or self.cmd==STEP:
            # self.populate_exception( exc_stuff )
            self.set_step()
            sys.settrace(self.trace_dispatch)
            # apply the same rule for threading as well 
            # caveat : Jython does not implement this
            if threading.__dict__.has_key("settrace"):
                threading.settrace(self.trace_dispatch)
        else:   
            self.populate_exception( exc_stuff )
            self.set_continue()
  

    def parsedReturned( self , command = 'COMMAND' , argument = None , message = None , details = None ):
        parsedCommand = []
        parsedCommand.append(command)
        parsedCommand.append(argument)
        parsedCommand.append(message)
        parsedCommand.append(details)
        return parsedCommand

        
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
                    oldstd = sys.stdout
                    oldstdin = sys.stdin
                    sys.stdout=self
                    sys.stdin=self
                    self.debuggee = fullname
                    sys.argv[0] = fullname # keep sys.argv in sync
                    # apply DEBUG rule to threading as well  
                    # NB : jython does not implement this function so test
                    if threading.__dict__.has_key("settrace"):
                        threading.settrace(self.trace_dispatch)
                    try:
                        self.run('execfile(' + `fullname` + ')')
                        # send a dedicated message for syntax error in order for the
                        # frontend debugger to handle a specific message and display the involved line
                        # in side the frontend editor
                    except:
                        tb , exctype , value = sys.exc_info()
                        excTrace = __builtin__.str(traceback.format_exception( tb , exctype , value ))
                        # self.populateException(excTrace)
                        #self.trace("populating exception here : ("+str(len(excTrace))+")" + excTrace)
                        #self.trace("populating exception for XML here : " +  _utils.removeForXml(excTrace))
                        self.send_client_exception(__builtin__.str(self.cmd) , _utils.removeForXml(excTrace))
                        #print excTrace
                        pass
                else :
                    print "inexisting debugee's file : " , fullname
                    return None
                    
                sys.stdout=oldstd
                sys.stdin=oldstd
                result ="OK"
                self.debuggee = None 
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
        self._connection.populateXmlToClient( ['<TERMINATE/>'] )
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
    def _parseSubCommand( self , command , frame ):
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
            self.stopthread = threading.currentThread()
            self.set_next(frame)
        elif ( string.upper(verb) == "STEP" ):
            self.cmd = STEP
            self.stopthread = threading.currentThread()
            self.set_step()
        elif ( string.upper(verb) == "RUN" ):
            self.cmd = RUN
            self.set_continue()
        elif ( string.upper(verb) == "STOP"):
            self.cmd = QUIT  
            self.quiting()
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
            self._connection.populateXmlToClient( ['<TERMINATE/>'] )
            return 0 # stop requested
        self.populateCommandToClient( command , result )
        return 1
    

    # start the deamon 
    def start( self , port = PORT , host = None , debuggee = None ,debuggeeArgs = None ):
        if ( host == None ):
            # start in listen mode waiting for incoming sollicitors   
            print "JPyDbg listening on " , port 
            s = socket.socket( socket.AF_INET , socket.SOCK_STREAM )
            s.bind( (HOST , port) )
            s.listen(1)
            connection , addr = s.accept()
            self._connection = dbgutils.NetworkSession(connection)
            print "connected by " , addr
        else:
            # connect back provided listening host
            print "JPyDbg connecting " , host , " on port " , port 
            try:   
                connection = socket.socket( socket.AF_INET , socket.SOCK_STREAM )
                connection.connect( (host , port) )
                self._connection = dbgutils.NetworkSession(connection)
                print "JPyDbgI0001 : connected to " , host
            except socket.error, (errno,strerror):
                print "ERROR:JPyDbg connection failed errno(%s) : %s" % ( errno , strerror )
                return None
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
