package org.mysite.classtaglib;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

//this is just testing class, not supposed to do anything
@FacesComponent(value="org.mysite.classtaglib.HyperComponent")
public class HyperComponent extends UIComponentBase {

    public HyperComponent() {
    }

    public String getValue() {
        return "avalue";
    }

    public void setValue(String value) {
        //no-op
    }

    @Override
    public String getFamily() {
        return "myfamily";
    }

}
