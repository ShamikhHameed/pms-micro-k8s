package com.nsss.pizzamsmsacrustbackend.controller;

import com.nsss.pizzamsmsacrustbackend.model.Crust;
import com.nsss.pizzamsmsacrustbackend.model.Order;
import com.nsss.pizzamsmsacrustbackend.model.OrderItem;
import com.nsss.pizzamsmsacrustbackend.response.CrustStatistics;
import com.nsss.pizzamsmsacrustbackend.response.MessageResponse;
import com.nsss.pizzamsmsacrustbackend.repository.CrustRepository;
// import com.nsss.pizzamsmsacrustbackend.repository.OrderRepository;
import com.nsss.pizzamsmsacrustbackend.request.CrustRequest;
import com.nsss.pizzamsmsacrustbackend.request.CrustStatisticsRequest;
import com.nsss.pizzamsmsacrustbackend.request.GetCrustRequest;
import com.nsss.pizzamsmsacrustbackend.request.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/crust")
public class CrustController {
    @Autowired
    CrustRepository crustRepository;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/crusts")
    public ResponseEntity<List<Crust>> getAllCrusts(@RequestParam(required = false) String crustName) {
        try {
            List<Crust> crusts = new ArrayList<Crust>();

            if (crustName == null)
                crustRepository.findAll().forEach(crusts::add);
            else
                crustRepository.findAllByName(crustName).forEach(crusts::add);

            if (crusts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(crusts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/crusts/search")
    public ResponseEntity<Crust> getCrustByName(@Valid @RequestBody GetCrustRequest crust) {
        try {
            Optional<Crust> crustData = crustRepository.findByName(crust.getName().toString());

            return new ResponseEntity<>(crustData.get(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/crusts/stats")
    public ResponseEntity<List<CrustStatistics>> getAllCrustStatistics(@Valid @RequestBody CrustStatisticsRequest crustStatisticsRequest) {
        try {
            Date fromTime = crustStatisticsRequest.getFromTimestamp();
            Date toTime = crustStatisticsRequest.getToTimestamp();

            List<CrustStatistics> crustStatistics = new ArrayList<>();

            List<Crust> crusts = new ArrayList<>(crustRepository.findAll());

            if (crusts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            for(Crust crust : crusts){
                crustStatistics.add(new CrustStatistics(crust.getId(), crust.getName(), 0, 0, 0, 0));
            }

            // List<Order> orders = new ArrayList<>(orderRepository.findAllByOrderTimestampBetween(fromTime,
            //         toTime));

            List<Order> orders = restTemplate.exchange("http://ORDER-SERVICE/order/orders/between", HttpMethod.GET, null, new ParameterizedTypeReference<List<Order>>() {}).getBody();

            for(Order order : orders){
                List<OrderItem> orderItems = order.getItems();

                for(OrderItem orderItem : orderItems){
                    for(CrustStatistics crustStatistic : crustStatistics){
                        if(crustStatistic.getCrustName().equals(orderItem.getCrust())){
                            if(orderItem.getSize().equals("small")){
                                crustStatistic.setSmallOrders(crustStatistic.getSmallOrders() + orderItem.getQuantity());
                            } else if(orderItem.getSize().equals("medium")){
                                crustStatistic.setMediumOrders(crustStatistic.getMediumOrders() + orderItem.getQuantity());
                            } else {
                                crustStatistic.setLargeOrders(crustStatistic.getLargeOrders() + orderItem.getQuantity());
                            }
                        }
                    }
                }
            }

            for(CrustStatistics crustStatistic : crustStatistics){
                crustStatistic.setTotalOrders(crustStatistic.getSmallOrders() + crustStatistic.getMediumOrders() + crustStatistic.getLargeOrders());
            }

            return new ResponseEntity<>(crustStatistics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/crusts")
    public ResponseEntity<?> addCrust(@Valid @RequestBody CrustRequest crustRequest) {
        Crust crust = new Crust(
                crustRequest.getName(),
                crustRequest.getSmallPrice(),
                calculateMediumCrustPrice(crustRequest.getSmallPrice()),
                calculateLargeCrustPrice(crustRequest.getSmallPrice()),
                crustRequest.isVegan()
        );

        crustRepository.save(crust);

        return ResponseEntity.ok(new MessageResponse("Crust added successfully"));
    }

    @DeleteMapping("/crusts/{id}")
    public ResponseEntity<HttpStatus> deleteCrust(@PathVariable("id") String id) {
        try {
            crustRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/crusts/{id}")
    public ResponseEntity<Crust> updateCrust(@PathVariable("id") String id, @Valid @RequestBody CrustRequest crustRequest) {
        Optional<Crust> crustData = crustRepository.findById(id);

        if(crustData.isPresent()) {
            Crust _crust = crustData.get();
            _crust.setName(crustRequest.getName());
            _crust.setSmallPrice(crustRequest.getSmallPrice());
            _crust.setMediumPrice(calculateMediumCrustPrice(crustRequest.getSmallPrice()));
            _crust.setLargePrice(calculateLargeCrustPrice(crustRequest.getSmallPrice()));
            _crust.setVegan(crustRequest.isVegan());

            return new ResponseEntity<>(crustRepository.save(_crust), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private double calculateMediumCrustPrice(double smallPrice) {
        //price of a medium crust is 1.85 times the price of a small
        return Math.round((smallPrice*1.85)/10.0)*10;
    }

    private double calculateLargeCrustPrice(double smallPrice) {
        //price of a medium crust is 3.25 times the price of a small
        return Math.round((smallPrice*3.25)/10.0)*10;
    }
}
