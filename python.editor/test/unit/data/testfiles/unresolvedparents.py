from socket import SocketType
import bdb

class GoodParent :
  """ simply a good top parent"""
  pass

class GoodChild( GoodParent ):
  """ good inherit """
  pass

class BadChild( BadParent ):
  """inherit from inexisting parent"""
  pass

class GoodImportedFrom( SocketType ):
  """ inherit from a from imported class"""
  pass

class GoodImported( bdb.Bdb ):
  """ inherit from an imported public module """
  pass

class BadImported( undefined.Undefined ):
  """ inherit from bad module bad class """
  pass


class BadImported2( pdb.Pdb ): # import missing
  """ inherit from non imported module """
  pass