package org.fffd.l23o6.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.OrderDao;
import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.dao.UserDao;
import org.fffd.l23o6.mapper.TrainMapper;
import org.fffd.l23o6.pojo.entity.UserEntity;
import org.fffd.l23o6.pojo.enum_.OrderStatus;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.vo.order.OrderPriceCalculator;
import org.fffd.l23o6.pojo.vo.order.OrderVO;
import org.fffd.l23o6.pojo.vo.train.TrainVO;
import org.fffd.l23o6.service.OrderService;
import org.fffd.l23o6.service.TrainService;
import org.fffd.l23o6.util.strategy.payment.PaymentStrategy;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.springframework.stereotype.Service;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final TrainDao trainDao;
    private final RouteDao routeDao;
    private TrainService trainService;

    @Override
    public Long createOrder(String username, Long trainId, Long fromStationId, Long toStationId, String seatType,
            Long seatNumber) {
        Long userId = userDao.findByUsername(username).getId();
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        double price = 0;
        if(seatType.equals("商务座")){
            price = 500;
        }else if(seatType.equals("一等座")){
            price = 300;
        }else if(seatType.equals("二等座")){
            price = 100;
        }else if(seatType.equals("无座")){
            price = 100;
        }else if(seatType.equals("软卧")){
            price = 300;
        }else if(seatType.equals("硬卧")){
            price = 250;
        }else if(seatType.equals("软座")){
            price = 200;
        }else if(seatType.equals("硬座")){
            price = 150;
        }
        int MileagePoints=userDao.findByUsername(username).getMileagePoints();
        OrderPriceCalculator OPC=new OrderPriceCalculator();
        price=OPC.calculateOrderPrice(MileagePoints,price);
        train.setPrice(price);
        int startStationIndex = route.getStationIds().indexOf(fromStationId);
        int endStationIndex = route.getStationIds().indexOf(toStationId);
        String seat = null;
        switch (train.getTrainType()) {
            case HIGH_SPEED:
                seat = GSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        GSeriesSeatStrategy.GSeriesSeatType.fromString(seatType), train.getSeats());
                break;
            case NORMAL_SPEED:
                seat = KSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        KSeriesSeatStrategy.KSeriesSeatType.fromString(seatType), train.getSeats());
                break;
        }
        if (seat == null) {
            throw new BizException(BizError.OUT_OF_SEAT);
        }
        OrderEntity order = OrderEntity.builder().trainId(trainId).userId(userId).seat(seat)
                .status(OrderStatus.PENDING_PAYMENT).arrivalStationId(toStationId).departureStationId(fromStationId).method("2")
                .build();
        train.setUpdatedAt(null);// force it to update
        trainDao.save(train);
        orderDao.save(order);
        return order.getId();
    }

    @Override
    public List<OrderVO> listOrders(String username) {
        Long userId = userDao.findByUsername(username).getId();
        List<OrderEntity> orders = orderDao.findByUserId(userId);
        orders.sort((o1,o2)-> o2.getId().compareTo(o1.getId()));
        return orders.stream().map(order -> {
            TrainEntity train = trainDao.findById(order.getTrainId()).get();
            RouteEntity route = routeDao.findById(train.getRouteId()).get();
            int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
            int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
            return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                    .seat(order.getSeat()).status(order.getStatus().getText())
                    .createdAt(order.getCreatedAt())
                    .startStationId(order.getDepartureStationId())
                    .endStationId(order.getArrivalStationId())
                    .departureTime(train.getDepartureTimes().get(startIndex))
                    .arrivalTime(train.getArrivalTimes().get(endIndex))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        TrainEntity train = trainDao.findById(order.getTrainId()).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
        int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
        return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                .seat(order.getSeat()).status(order.getStatus().getText())
                .createdAt(order.getCreatedAt())
                .startStationId(order.getDepartureStationId())
                .endStationId(order.getArrivalStationId())
                .departureTime(train.getDepartureTimes().get(startIndex))
                .arrivalTime(train.getArrivalTimes().get(endIndex))
                .build();
    }

    @Override
    public void cancelOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }

        // TODO:
        //  refund user's money and credits if needed


        order.setStatus(OrderStatus.CANCELLED);
        orderDao.save(order);
    }

    @Override
    public void setMethod(Long id, String method) {
        OrderEntity order = orderDao.findById(id).get();
        order.setMethod(method);
        orderDao.save(order);

    }

    @Override
    public void payOrder(Long id, String username) {
        OrderEntity order = orderDao.findById(id).get();
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }
        UserEntity userEntity = userDao.findByUsername(username);
        //userEntity.setDiscountPoints(1000000);
        //userEntity.setMileagePoints(1000);
        TrainEntity trainEntity = trainDao.findById(order.getTrainId()).get();
        double price = trainEntity.getPrice();
        System.out.println(price);

        String payType = order.getMethod();
        System.out.println(payType);
        boolean paySuccess;
        // 调用支付接口
        paySuccess = PaymentStrategy.getPayment(payType).pay(userDao,order.getUserId(), price);

        if (!paySuccess) {
            throw new BizException(BizError.FAIL_TO_PAY);
        }else{
            order.setStatus(OrderStatus.COMPLETED);
        }

        // TODO:
        //  use payment strategy to pay!
        // TODO(finished): update user's credits, so that user can get discount next time
        orderDao.save(order);
        updateCredits(username);
    }

    // Todo(finished):implement
    @Override
    public void updateCredits(String username){
        UserEntity user = userDao.findByUsername(username);
        userDao.save(user.setMileagePoints(user.getMileagePoints() + 200).setDiscountPoints(user.getDiscountPoints() + 100));
    }

}
