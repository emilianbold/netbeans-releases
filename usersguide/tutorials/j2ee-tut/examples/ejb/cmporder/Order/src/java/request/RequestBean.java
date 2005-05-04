/*
 * Copyright (c) 2004 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */

package request;

import dataregistry.LineItemLocal;
import dataregistry.LineItemLocalHome;
import dataregistry.OrdersLocal;
import dataregistry.OrdersLocalHome;
import dataregistry.PartLocal;
import dataregistry.PartLocalHome;
import dataregistry.PartPK;
import dataregistry.VendorKey;
import dataregistry.VendorLocal;
import dataregistry.VendorLocalHome;
import dataregistry.VendorPartLocal;
import dataregistry.VendorPartLocalHome;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.*;

/**
 * This is the bean class for the RequestBean enterprise bean.
 */
public class RequestBean implements javax.ejb.SessionBean, request.RequestRemoteBusiness {
    private javax.ejb.SessionContext context;
    private LineItemLocalHome lineItemHome = null;
    private OrdersLocalHome orderHome = null;
    private PartLocalHome partHome = null;
    private VendorLocalHome vendorHome = null;
    private VendorPartLocalHome vendorPartHome = null;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods or web service operations
    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(javax.ejb.SessionContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    // </editor-fold>
    
    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        try {
            lineItemHome = lookupLineitem();
            orderHome = lookupOrders();
            partHome = lookupPart();
            vendorHome = lookupVendor();
            vendorPartHome = lookupVendorPart();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    
    public void createPart(PartRequest partRequest) {
        try {
            PartLocal part =
                    partHome.create(partRequest.partNumber, partRequest.revision,
                    partRequest.description, partRequest.revisionDate,
                    partRequest.specification, partRequest.drawing);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    public void addPartToBillOfMaterial(BomRequest bomRequest) {
        try {
            PartPK bomkey = new PartPK();
            bomkey.partNumber = bomRequest.bomPartNumber;
            bomkey.revision = new BigDecimal(bomRequest.bomRevision);
            
            PartLocal bom = partHome.findByPrimaryKey(bomkey);
            
            PartPK pkey = new PartPK();
            pkey.partNumber = bomRequest.partNumber;
            pkey.revision = new BigDecimal(bomRequest.revision);
            
            PartLocal part = partHome.findByPrimaryKey(pkey);
            part.setBomPart(bom);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
        
    }
    
    public void createVendor(VendorRequest vendorRequest) {
        try {
            VendorLocal vendor =
                    vendorHome.create(vendorRequest.vendorId, vendorRequest.name,
                    vendorRequest.address, vendorRequest.contact,
                    vendorRequest.phone);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
        
    }
    
    public void createVendorPart(VendorPartRequest vendorPartRequest) {
        try {
            PartPK pkey = new PartPK();
            pkey.partNumber = vendorPartRequest.partNumber;
            pkey.revision = new BigDecimal(vendorPartRequest.revision);
            
            PartLocal part = partHome.findByPrimaryKey(pkey);
            VendorPartLocal vendorPart =
                    vendorPartHome.create(vendorPartRequest.description,
                    vendorPartRequest.price, part);
            
            VendorKey vkey = new VendorKey();
            vkey.vendorId = vendorPartRequest.vendorId;
            
            VendorLocal vendor = vendorHome.findByPrimaryKey(vkey);
            vendorPart.setVendor(vendor);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    public void createOrder(OrderRequest orderRequest) {
        try {
            OrdersLocal order =
                    orderHome.create(orderRequest.orderId, String.valueOf(orderRequest.status),
                    new BigDecimal(orderRequest.discount), orderRequest.shipmentInfo);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
        
    }
    
    public void addLineItem(LineItemRequest lineItemRequest) {
        try {
            OrdersLocal order =
                    orderHome.findByPrimaryKey(lineItemRequest.orderId);
            
            PartPK pkey = new PartPK();
            pkey.partNumber = lineItemRequest.partNumber;
            pkey.revision = new BigDecimal(lineItemRequest.revision);
            
            PartLocal part = partHome.findByPrimaryKey(pkey);
            
            LineItemLocal lineItem =
                    lineItemHome.create(order, new BigDecimal(lineItemRequest.quantity),
                    part.getVendorPartBean());
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }    
    }
    
    public double getBillOfMaterialPrice(BomRequest bomRequest) {
        double price = 0.0;
        
        try {
            PartPK bomkey = new PartPK();
            bomkey.partNumber = bomRequest.bomPartNumber;
            bomkey.revision = new BigDecimal(bomRequest.bomRevision);
            
            PartLocal bom = partHome.findByPrimaryKey(bomkey);
            Collection parts = bom.getPartBean1();
            
            for (Iterator iterator = parts.iterator(); iterator.hasNext();) {
                PartLocal part = (PartLocal) iterator.next();
                VendorPartLocal vendorPart = part.getVendorPartBean();
                price += vendorPart.getPrice().doubleValue();
            }
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
        
        return price;
    }
    
    public double getOrderPrice(Integer orderId) {
        double price = 0.0;
        try {
            OrdersLocal order = orderHome.findByPrimaryKey(orderId);
            price = order.calculateAmmount();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
        return price;
    }
    
    public void adjustOrderDiscount(int adjustment) {
        orderHome.adjustDiscount(adjustment);
    }
    
    public Double getAvgPrice() {
        Double avgPrice = new Double(0);
        try{
            avgPrice = vendorPartHome.getAvgPrice();
        }catch(FinderException ex){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,ex.getMessage());
            throw new EJBException(ex.getMessage());
        }
        return avgPrice;
    }
    
    public Double getTotalPricePerVendor(VendorRequest vendorRequest) {
        return vendorPartHome.getTotalPricePerVendor(vendorRequest.vendorId);
    }
    
    public Collection locateVendorsByPartialName(String name) {
        Collection names = new ArrayList();
        
        try {
            Collection vendors = vendorHome.findByPartialName(name);
            for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
                VendorLocal vendor = (VendorLocal) iterator.next();
                names.add(vendor.getName());
            }
        } catch (FinderException e) {
        }
        
        return names;
    }
    
    public int countAllItems() {
        int count = 0;
        try {
            count = lineItemHome.findAll().size();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
        return count;
    }
    
    public void removeOrder(Integer orderId) {
        try {
            orderHome.remove(orderId);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    public String reportVendorsByOrder(Integer orderId) {
        StringBuffer report = new StringBuffer();
        try {
            Collection vendors = vendorHome.findByOrder(orderId);
    
            for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
                VendorLocal vendor = (VendorLocal) iterator.next();
                report.append(vendor.getVendorId())
                      .append(' ')
                      .append(vendor.getName())
                      .append(' ')
                      .append(vendor.getContact())
                      .append('\n');
            }
        } catch (FinderException e) {
            throw new EJBException(e.getMessage());
        }
        return report.toString();
        
    }
    
    private dataregistry.LineItemLocalHome lookupLineitem() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            dataregistry.LineItemLocalHome rv = (dataregistry.LineItemLocalHome) c.lookup("java:comp/env/ejb/Lineitem");
            return rv;
        } catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }
    
    private dataregistry.OrdersLocalHome lookupOrders() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            dataregistry.OrdersLocalHome rv = (dataregistry.OrdersLocalHome) c.lookup("java:comp/env/ejb/Orders");
            return rv;
        } catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }
    
    private dataregistry.PartLocalHome lookupPart() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            dataregistry.PartLocalHome rv = (dataregistry.PartLocalHome) c.lookup("java:comp/env/ejb/Part");
            return rv;
        } catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }
    
    private dataregistry.VendorLocalHome lookupVendor() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            dataregistry.VendorLocalHome rv = (dataregistry.VendorLocalHome) c.lookup("java:comp/env/ejb/Vendor");
            return rv;
        } catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }
    
    private dataregistry.VendorPartLocalHome lookupVendorPart() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            dataregistry.VendorPartLocalHome rv = (dataregistry.VendorPartLocalHome) c.lookup("java:comp/env/ejb/VendorPart");
            return rv;
        } catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }
    
    
}
