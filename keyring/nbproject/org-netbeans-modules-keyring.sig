#Signature file v4.1
#Version 1.6

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public org.netbeans.api.keyring.Keyring
meth public static char[] read(java.lang.String)
meth public static void delete(java.lang.String)
meth public static void save(java.lang.String,char[],java.lang.String)
supr java.lang.Object
hfds LOG,PROVIDER
hcls DummyKeyringProvider

CLSS abstract interface org.netbeans.api.keyring.package-info

CLSS public abstract interface org.netbeans.spi.keyring.KeyringProvider
meth public abstract boolean enabled()
meth public abstract char[] read(java.lang.String)
meth public abstract void delete(java.lang.String)
meth public abstract void save(java.lang.String,char[],java.lang.String)

CLSS abstract interface org.netbeans.spi.keyring.package-info

