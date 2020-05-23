package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import javax.persistence.criteria.Order;
import java.util.Date;
import java.util.List;

public interface ShipService {

    List<Ship> getShips(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed,
                        Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                        Double minRating, Double maxRating);

    Ship createShip(Ship ship);

    Ship getShip(Long id);

    void deleteShip(Ship ship);

    Ship updateShip(Ship oldShip, Ship newShip);

    Double calculateRating (double speed, boolean isUsed, Date prod);

    boolean paramsValid(Ship ship);

    List<Ship> sortShips(List<Ship> ships, ShipOrder order);

    List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize);





}
