package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Order;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ShipServiceImpl implements ShipService {

    private ShipRepository shipRepository;

    public ShipServiceImpl() {}

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        //super();
        this.shipRepository = shipRepository;
    }

    @Override
    public Ship getShip(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteShip(Ship ship) {
        shipRepository.delete(ship);
    }

    @Override
    public List<Ship> getShips(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed,
                               Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                                           Double minRating, Double maxRating) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Ship> list = new ArrayList<>();
        shipRepository.findAll().forEach((ship) -> {
            if (name != null && !ship.getName().contains(name)) return;
            if (planet != null && !ship.getPlanet().contains(planet)) return;
            if (shipType != null && ship.getShipType() != shipType) return;
            if (afterDate != null && ship.getProdDate().before(afterDate)) return;
            if (beforeDate != null && ship.getProdDate().after(beforeDate)) return;
            if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) return;
            if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) return;
            if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) return;
            if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) return;
            if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) return;
            if (minRating != null && ship.getRating().compareTo(minRating) < 0) return;
            if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) return;

            list.add(ship);
        });
        return list;
    }

    @Override
    public Ship createShip(Ship ship) {
        return shipRepository.save(ship);
    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {
        boolean shouldChangeRating = false;

        final String name = newShip.getName();
        if (name != null) {
            if (isStringValid(name)) {
                oldShip.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }
        final String planet = newShip.getPlanet();
        if (planet != null) {
            if (isStringValid(planet)) {
                oldShip.setPlanet(planet);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newShip.getShipType() != null) {
            oldShip.setShipType(newShip.getShipType());
        }
        final Date prodDate = newShip.getProdDate();
        if (prodDate != null) {
            if (checkTimeBorders(prodDate)) {
                oldShip.setProdDate(prodDate);
                shouldChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newShip.getUsed() != null) {
            oldShip.setUsed(newShip.getUsed());
            shouldChangeRating = true;
        }
        final Double speed = newShip.getSpeed();
        if (speed != null) {
            if (isSpeedValid(speed)) {
                oldShip.setSpeed(speed);
                shouldChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        final Integer crewSize = newShip.getCrewSize();
        if (crewSize != null) {
            if (isCrewValid(crewSize)) {
                oldShip.setCrewSize(crewSize);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (shouldChangeRating) {
            final double rating = calculateRating(oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate());
            oldShip.setRating(rating);
        }
        shipRepository.save(oldShip);
        return oldShip;
    }



    public boolean paramsValid(Ship ship) {
        return isCrewValid(ship.getCrewSize()) && isSpeedValid(ship.getSpeed()) &&
                isStringValid(ship.getName()) && isStringValid(ship.getPlanet()) && checkTimeBorders(ship.getProdDate());
    }

    private boolean isCrewValid(Integer crewSize) {
        final int minCrewSize = 1;
        final int maxCrewSize = 9999;
        return crewSize != null && crewSize.compareTo(minCrewSize) >= 0 && crewSize.compareTo(maxCrewSize) <= 0;

    }

    private boolean isStringValid(String s) {
        return !s.isEmpty() && s !=null && s.length()<50;
    }

    private boolean isSpeedValid(Double d) {
        return d != null && d.compareTo(0.01)>=0 && d.compareTo(0.99) <=0;
    }

    private boolean checkTimeBorders(Date date) {
        final Date startProd = getDateForYear(2800);
        final Date endProd = getDateForYear(3019);
        return date != null && date.after(startProd) && date.before(endProd);
    }

    public Double calculateRating(double speed, boolean isUsed, Date prod) {
        final int now = 3019;
        final int prodYear = getYearFromDate(prod);
        final double k = isUsed ? 0.5 : 1;
        final double rating = 80 * speed * k / (now - prodYear + 1);
        return round(rating);
    }

    private int getYearFromDate(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    public List<Ship> sortShips(List<Ship> ships, ShipOrder order) {
        if (order != null) {
            ships.sort((ship1, ship2) -> {
                switch (order) {
                    case ID: return ship1.getId().compareTo(ship2.getId());
                    case SPEED: return ship1.getSpeed().compareTo(ship2.getSpeed());
                    case DATE: return ship1.getProdDate().compareTo(ship2.getProdDate());
                    case RATING: return ship1.getRating().compareTo(ship2.getRating());
                    default: return 0;
                }
            });
        }
        return ships;
    }


    public List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > ships.size()) to = ships.size();
        return ships.subList(from, to);
    }

    private double round(double value) {
        return Math.round(value * 100) / 100D;
    }
}
