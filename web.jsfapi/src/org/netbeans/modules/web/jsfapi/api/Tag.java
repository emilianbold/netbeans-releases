package org.netbeans.modules.web.jsfapi.api;

import java.util.Collection;

public interface Tag {

    public String getName();

    public String getDescription();

    public boolean hasNonGenenericAttributes();

    public Collection<Attribute> getAttributes();

    public Attribute getAttribute(String name);
}
