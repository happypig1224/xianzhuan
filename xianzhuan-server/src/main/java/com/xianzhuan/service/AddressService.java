package com.xianzhuan.service;

import com.xianzhuan.entity.Address;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface AddressService {

    List<Address> list(Address address);

    void save(Address address);

    Address getById(Long id);

    void update(Address address);

    void setDefault(Address address);

    void deleteById(Long id);

}