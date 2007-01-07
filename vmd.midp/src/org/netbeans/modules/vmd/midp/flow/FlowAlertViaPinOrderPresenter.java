package org.netbeans.modules.vmd.midp.flow;

import org.netbeans.modules.vmd.api.flow.FlowPinOrderPresenter;
import org.netbeans.modules.vmd.api.flow.visual.FlowPinDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author David Kaspar
 */
public class FlowAlertViaPinOrderPresenter extends FlowPinOrderPresenter {

    public static final String CATEGORY_ID = "AlertVia"; // NOI18N

    public String getCategoryID () {
        return FlowAlertViaPinOrderPresenter.CATEGORY_ID;
    }

    public String getCategoryDisplayName () {
        return "Vias";
    }

    public List<FlowPinDescriptor> sortCategory (ArrayList<FlowPinDescriptor> descriptors) {
        Collections.sort (descriptors, new Comparator<FlowPinDescriptor>() {
            public int compare (FlowPinDescriptor d1, FlowPinDescriptor d2) {
                long diff = d1.getRepresentedComponent ().getComponentID () - d2.getRepresentedComponent ().getComponentID ();
                if (diff != 0)
                    return (int) diff;
                return d1.getDescriptorID ().compareTo (d2.getDescriptorID ());
            }
        });
        return descriptors;
    }

}
