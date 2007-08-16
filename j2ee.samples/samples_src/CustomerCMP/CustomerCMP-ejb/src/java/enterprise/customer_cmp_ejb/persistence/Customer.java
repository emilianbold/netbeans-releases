/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package enterprise.customer_cmp_ejb.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.persistence.*;
import javax.persistence.OneToMany;



@Entity
@NamedQueries(
    value={@NamedQuery(name="findCustomerByFirstName", query="select object(c) from Customer c where c.firstName= :firstName"),
    @NamedQuery(name="findCustomerByLastName", query="select object(c) from Customer c where c.lastName= :lastName")}
)

public class Customer implements java.io.Serializable{

    //access methods for cmp fields
    private String id;
    private String firstName;
    private String lastName;
    private Collection<Address> addresses;
    private Collection<Subscription> subscriptions;
    
    public Customer(){
        
    }
    
    @Id
    @Column(name="customerid")
    public String getCustomerID(){      //primary key
        return id;
    }
    public void setCustomerID(String id){
        this.id=id;
    }
    
    public String getFirstName(){
        return firstName;
    }
    public void setFirstName(String firstName){
        this.firstName=firstName;
    }

    public String getLastName(){
        return lastName;
    }
    public void setLastName(String lastName){
        this.lastName=lastName;
    }

    public Customer(String id, String firstName, String lastName) {          
        setCustomerID(id);
        setFirstName(firstName);
        setLastName(lastName);
    }


    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    //targetEntity not needed as the property is a generic java data type
    //cascade will default to none, needs to be ALL because we need to save newly added addresses
    //fetch specified as EAGER, because we need to access the association outside of a txn.
    //mapped by is not needed because this is a unidirectional mapping
    public Collection<Address> getAddresses(){
        return addresses;
    }
    public void setAddresses (Collection<Address> addresses){
        this.addresses=addresses;
    }

    @ManyToMany(fetch=FetchType.EAGER )
    @JoinTable(
            name="CUSTOMERBEANSUBSCRIPTIONBEAN",
            joinColumns=@JoinColumn(name="CUSTOMERBEAN_CUSTOMERID96", referencedColumnName="customerid"), 
            inverseJoinColumns=@JoinColumn(name="SUBSCRIPTIONBEAN_TITLE", referencedColumnName="TITLE") 
    )
    public Collection<Subscription> getSubscriptions(){
        return subscriptions;
    }
    public void setSubscriptions (Collection<Subscription> subscriptions){
        this.subscriptions=subscriptions;
    }

    //business methods
    //We could have as well used a java.util.List for a collection of addresses. 
    //But what's below is only for demonstration purposes and the use of Collection instead of List is for that purpose only.
    @Transient
    //since the signature starts with a get, need to annotate it as @Transient
    public ArrayList getAddressList() {
        ArrayList list = new ArrayList();
        Iterator c = getAddresses().iterator();
        while (c.hasNext()) {
            list.add((Address)c.next());
        }
        return list;
    }

    @Transient
    public ArrayList getSubscriptionList() {
        ArrayList list = new ArrayList();
        Iterator c = getSubscriptions().iterator();
        while (c.hasNext()) {
            list.add((Subscription)c.next());
        }
        return list;
    }

    
    // other EntityBean methods
    @PostPersist
    public void postCreate (){
        System.out.println("Customer::postCreate:");
    }


    
    @PostRemove
    public void ejbRemove() {
        System.out.println("Customer::postRemove");
    }
    

}
