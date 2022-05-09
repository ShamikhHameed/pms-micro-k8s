package com.nsss.pizzamsmsatoppingbackend.controller;

import com.nsss.pizzamsmsatoppingbackend.model.Order;
import com.nsss.pizzamsmsatoppingbackend.model.OrderItem;
import com.nsss.pizzamsmsatoppingbackend.model.Topping;
import com.nsss.pizzamsmsatoppingbackend.response.MessageResponse;
import com.nsss.pizzamsmsatoppingbackend.response.ToppingStatistics;
import com.nsss.pizzamsmsatoppingbackend.repository.ToppingRepository;
import com.nsss.pizzamsmsatoppingbackend.request.GetToppingRequest;
import com.nsss.pizzamsmsatoppingbackend.request.ToppingRequest;
import com.nsss.pizzamsmsatoppingbackend.request.ToppingStatisticsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/topping")
public class ToppingController {
    @Autowired
    ToppingRepository toppingRepository;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/toppings")
    public ResponseEntity<List<Topping>> getAllToppings(@RequestParam(required = false) String toppingName) {
        try {
            List<Topping> toppings = new ArrayList<Topping>();

            if (toppingName == null)
                toppingRepository.findAll().forEach(toppings::add);
            else
                toppingRepository.findAllByName(toppingName).forEach(toppings::add);

            if (toppings.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(toppings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/toppings/search")
    public ResponseEntity<Topping> getToppingByName(@Valid @RequestBody GetToppingRequest topping) {
        try {
            Optional<Topping> toppingData = toppingRepository.findByName(topping.getName().toString());

            return new ResponseEntity<>(toppingData.get(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/toppings")
    public ResponseEntity<?> addTopping(@Valid @RequestBody ToppingRequest toppingRequest) {
        Topping topping = new Topping(
                toppingRequest.getName(),
                toppingRequest.getSmallPrice(),
                calculateMediumToppingPrice(toppingRequest.getSmallPrice()),
                calculateLargeToppingPrice(toppingRequest.getSmallPrice()),
                toppingRequest.isVegan()
        );

        toppingRepository.save(topping);

        return ResponseEntity.ok(new MessageResponse("Topping added successfully"));
    }

    @DeleteMapping("/toppings/{id}")
    public ResponseEntity<HttpStatus> deleteTopping(@PathVariable("id") String id) {
        try {
            toppingRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/toppings/{id}")
    public ResponseEntity<Topping> updateTopping(@PathVariable("id") String id, @Valid @RequestBody ToppingRequest toppingRequest) {
        Optional<Topping> toppingData = toppingRepository.findById(id);

        if(toppingData.isPresent()) {
            Topping _topping = toppingData.get();
            _topping.setName(toppingRequest.getName());
            _topping.setSmallPrice(toppingRequest.getSmallPrice());
            _topping.setMediumPrice(calculateMediumToppingPrice(toppingRequest.getSmallPrice()));
            _topping.setLargePrice(calculateLargeToppingPrice(toppingRequest.getSmallPrice()));
            _topping.setVegan(toppingRequest.isVegan());

            return new ResponseEntity<>(toppingRepository.save(_topping), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/toppings/stats")
    public ResponseEntity<List<ToppingStatistics>> getAllToppingStatistics(@Valid @RequestBody ToppingStatisticsRequest toppingStatisticsRequest) {
        try {
            Date fromTime = toppingStatisticsRequest.getFromTimestamp();
            Date toTime = toppingStatisticsRequest.getToTimestamp();

            List<ToppingStatistics> toppingStatistics = new ArrayList<>();

            List<Topping> toppings = new ArrayList<>(toppingRepository.findAll());

            if (toppings.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            for(Topping topping : toppings){
                toppingStatistics.add(new ToppingStatistics(topping.getId(), topping.getName(), 0, 0, 0, 0));
            }

            // List<Order> orders = new ArrayList<>(orderRepository.findAllByOrderTimestampBetween(fromTime,
            //         toTime));

            List<Order> orders = restTemplate.exchange("http://ORDER-SERVICE/order/orders/between", HttpMethod.GET, null, new ParameterizedTypeReference<List<Order>>() {}).getBody();

            for(Order order : orders){
                List<OrderItem> orderItems = order.getItems();

                for(OrderItem orderItem : orderItems){
                    for(ToppingStatistics toppingStatistic : toppingStatistics){
                        if(toppingStatistic.getToppingName().equals(orderItem.getTopping())){
                            if(orderItem.getSize().equals("small")){
                                toppingStatistic.setSmallOrders(toppingStatistic.getSmallOrders() + orderItem.getQuantity());
                            } else if(orderItem.getSize().equals("medium")){
                                toppingStatistic.setMediumOrders(toppingStatistic.getMediumOrders() + orderItem.getQuantity());
                            } else {
                                toppingStatistic.setLargeOrders(toppingStatistic.getLargeOrders() + orderItem.getQuantity());
                            }
                        }
                    }
                }
            }

            for(ToppingStatistics toppingStatistic : toppingStatistics){
                toppingStatistic.setTotalOrders(toppingStatistic.getSmallOrders() + toppingStatistic.getMediumOrders() + toppingStatistic.getLargeOrders());
            }

            return new ResponseEntity<>(toppingStatistics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private double calculateMediumToppingPrice(double smallPrice) {
        //price of a medium topping is 1.85 times the price of a small
        return Math.round((smallPrice*1.85)/10.0)*10;
    }

    private double calculateLargeToppingPrice(double smallPrice) {
        //price of a medium topping is 3.25 times the price of a small
        return Math.round((smallPrice*3.25)/10.0)*10;
    }
}
