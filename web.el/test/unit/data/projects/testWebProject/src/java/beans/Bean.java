/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Bean {

    public List<String> any() {
        return Arrays.asList(
                "Jeden",
                "Dva");
    }

    public String getProperty() {
        return "property";
    }
}
