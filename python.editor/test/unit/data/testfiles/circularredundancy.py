#
# checking redundancy cycling
#

# circular
class First (Third ):
    pass

class Second (First) :
    pass

class Third (Second) :
    pass

# not circular
class Fourth :
    pass

class Fifth(Fourth):
    pass



