/**
 * This interface has all of the bean info accessor methods.
 * 
 * @Generated
 */

package org.netbeans.modules.uml.codegen.dataaccess.xmlbeans;

public interface FamilyInterface {
	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setExpanded(boolean value);

	public boolean isExpanded();

	public void setDomainObject(int index, DomainObject value);

	public DomainObject getDomainObject(int index);

	public int sizeDomainObject();

	public void setDomainObject(DomainObject[] value);

	public DomainObject[] getDomainObject();

	public int addDomainObject(org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject value);

	public int removeDomainObject(org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject value);

	public DomainObject newDomainObject();

}
