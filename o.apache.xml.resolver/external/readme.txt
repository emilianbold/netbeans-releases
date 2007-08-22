Created by: Samaresh Panda
Last updated: 08-22-2007
Current Version being used: xml-commons-resolver-1.2

resolver.jar is a third party library being used in Netbeans.
This came from http://xml.apache.org/commons/ with Apache license 2.0.

Earlier, it was patched on xml-commons-resolver-1.1 to cater to our needs.
See resolver.patch file for the patches applied xml-commons-resolver-1.1.
The patch on org.apache.xml.resolver.tools.CatalogResolver.java causes
these two issues:

http://www.netbeans.org/issues/show_bug.cgi?id=98212
http://www.netbeans.org/issues/show_bug.cgi?id=112679

Hence we have applied the following patches on xml-commons-resolver-1.2:

1. Add a new API to org.apache.xml.resolver.Catalog.java:

  /**
   * Return all registered public IDs.
   */
  public Iterator getPublicIDs() {
      Vector v = new Vector();
      Enumeration enumeration = catalogEntries.elements();

      while (enumeration.hasMoreElements()) {
        CatalogEntry e = (CatalogEntry) enumeration.nextElement();
        if (e.getEntryType() == PUBLIC) {
            v.add(e.getEntryArg(0));
        }
      }
      return v.iterator();
  }

2. Handle null in org.apache.xml.resolver.CatalogManager.java::readProperties()
  + if (propertyFile == null) return;