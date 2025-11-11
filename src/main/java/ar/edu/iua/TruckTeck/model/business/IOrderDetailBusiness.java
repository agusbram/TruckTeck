package ar.edu.iua.TruckTeck.model.business;

import java.util.List;

import ar.edu.iua.TruckTeck.model.OrderDetail;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;

public interface IOrderDetailBusiness {

    public List<OrderDetail> listId(Long orderID) throws BusinessException;
}
