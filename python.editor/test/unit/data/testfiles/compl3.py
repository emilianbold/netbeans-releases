#!/usr/bin/env python
# Testcase from http://www.amk.ca/files/simple/fingerd.txt

# fingerd.py
# By A.M. Kuchling (amk@amk.ca)
#
# This little program illustrates how easy it is to write network
# servers using the classes in the SocketServer module.
#
# The finger protocol is a very simple TCP-based protocol; the client
# sends a single line containing the name of the user whose
# information is being requested, followed by a newline.
# Information about the user should then be sent over the socket
# connection to the client.  In this implementation, the server simply
# tells you who you've fingered; you'd probably want it to retrieve
# information from the user's home directory or a database of some sort.
#

import SocketServer

class FingerHandler(SocketServer.StreamRequestHandler):
    def handle(self):
        # Read a line of text, limiting it to 512 bytes.
        # This will prevent someone trying to crash the server machine
        # by sending megabytes of data.
        username=self.rfile.readline(512)

        # Remove any leading and trailing whitespace, including the
        # trailing newline.
        import string
        username=string.strip(username)

        # Call the method to get the user's information, and return it
        # to the client.  The SocketServer classes make self.wfile
        # available to send data to the client.
        info = self.find_user_info(username)
        self.wfile.write(info)

    # The following method takes a string containing the username,
    # and returns another string containing whatever information is
    # desired.  You can subclass the FingerHandler class and override
    # this method with your own to produce customized output.

    def find_user_info(self, username):
        "Return a string containing the desired user information."
        return "You fingered the user %s\n" % (repr(username),)

# If this script is being run directly, it'll start acting as a finger
# daemon.  If someone's importing it in order to subclass
# FingerHandler, that shouldn't be done.  The following "if" statement
# is the usual Python idiom for running code only in a script.

if __name__=='__main__':
    # Create an instance of our server class
    server=SocketServer.TCPServer( ('', 79), FingerHandler)

    # Enter an infinite loop, waiting for requests and then servicing them.
    server.serve_forever()

