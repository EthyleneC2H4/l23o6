package org.fffd.l23o6.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.catalina.User;
import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.mapper.TrainMapper;
import org.fffd.l23o6.mapper.TrainMapperImpl;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.StationEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.entity.UserEntity;
import org.fffd.l23o6.pojo.enum_.TrainType;
import org.fffd.l23o6.pojo.vo.order.OrderPriceCalculator;
import org.fffd.l23o6.pojo.vo.train.AdminTrainVO;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;
import org.fffd.l23o6.pojo.vo.train.TrainVO;
import org.fffd.l23o6.pojo.vo.train.TrainDetailVO;
import org.fffd.l23o6.service.TrainService;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.TrainSeatStrategy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import io.github.lyc8503.spring.starter.incantation.exception.CommonErrorType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    @Override
    public TrainDetailVO getTrain(Long trainId) {
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        return TrainDetailVO.builder().id(trainId).date(train.getDate()).name(train.getName())
                .stationIds(route.getStationIds()).arrivalTimes(train.getArrivalTimes())
                .departureTimes(train.getDepartureTimes()).extraInfos(train.getExtraInfos()).build();
    }

    @Override
    public List<TrainVO> listTrains(Long startStationId, Long endStationId, String date) {
        // TODO: 创建订单 已完成 By wyx
        List<TrainVO> ret = new ArrayList<>();
        List<TrainEntity> trainEntities = trainDao.findAll();
        List<Long> routeIds = new ArrayList<>();
        Map<Long, Integer> routeStartIndex = new HashMap<Long, Integer>();
        Map<Long, Integer> routeEndIndex = new HashMap<Long, Integer>();
        //First, get all routes contains [startCity, endCity]
        for (RouteEntity routeEntity : routeDao.findAll()) {
            List<Long> stationIds = routeEntity.getStationIds();
            int startIndex = stationIds.indexOf(startStationId);
            int endIndex = stationIds.indexOf(endStationId);
            if(startIndex<endIndex && startIndex!=-1 && endIndex!=-1){
                Long routeId=routeEntity.getId();
                routeIds.add(routeId);
                routeStartIndex.put(routeId, startIndex);
                routeEndIndex.put(routeId, endIndex);
            }
        }
        // Then, Get all trains on that day with the wanted routes
        for (TrainEntity trainEntity : trainEntities) {
            Long routeId = trainEntity.getRouteId();
            String trainDate = trainEntity.getDate();
            if(trainDate.equals(date) && routeIds.contains(routeId)){
                int startIndex=routeStartIndex.get(routeId);
                int endIndex=routeEndIndex.get(routeId);
                List<TicketInfo> ticketInfo = new ArrayList<>(4);
                if(trainEntity.getTrainType().equals(TrainType.HIGH_SPEED)) {
                    for (Map.Entry<GSeriesSeatStrategy.GSeriesSeatType, Integer> entry :
                            GSeriesSeatStrategy.INSTANCE
                                    .getLeftSeatCount(startIndex, endIndex, trainEntity.getSeats())
                                    .entrySet()) {
                        double price = 0;
                        String seatType = entry.getKey().getText();
                        if (seatType.equals("商务座")) {
                            price = 500;
                        } else if (seatType.equals("一等座")) {
                            price = 300;
                        } else if (seatType.equals("二等座")) {
                            price = 100;
                        }

                        ticketInfo.add(TicketInfo.builder()
                                .type(seatType)
                                .count(entry.getValue())
                                .price(price)
                                .build());
                    }
                    //无座情况
                    //单独讨论，因为TYPE_MAP中没有NO_SEAT
                    ticketInfo.add(TicketInfo.builder().type(GSeriesSeatStrategy.GSeriesSeatType.NO_SEAT.getText())
                            .count(100)
                            .price(100)
                            .build());
                }
                else if(trainEntity.getTrainType().equals(TrainType.NORMAL_SPEED)){
                    for (Map.Entry<KSeriesSeatStrategy.KSeriesSeatType, Integer> entry :
                            KSeriesSeatStrategy.INSTANCE
                                    .getLeftSeatCount(startIndex, endIndex, trainEntity.getSeats())
                                    .entrySet()) {
                        int price = 0;
                        String seatType = entry.getKey().getText();
                        if (seatType.equals("软卧")) {
                            price = 300;
                        } else if (seatType.equals("硬卧")) {
                            price = 250;
                        } else if (seatType.equals("软座")) {
                            price = 200;
                        }else if(seatType.equals("硬座")){
                            price = 150;
                        }else if(seatType.equals("无座")){
                            price = 100;
                        }

                        ticketInfo.add(TicketInfo.builder()
                                .type(seatType)
                                .count(entry.getValue())
                                .price(price)
                                .build());
                    }
                    //无座情况
                    ticketInfo.add(TicketInfo.builder().type(KSeriesSeatStrategy.KSeriesSeatType.NO_SEAT.getText())
                            .count(100)
                            .price(100)
                            .build());

                }
                ret.add(TrainVO.builder().id(trainEntity.getId())
                        .name(trainEntity.getName())
                        .trainType(trainEntity.getTrainType().getText())
                        .startStationId(startStationId)
                        .endStationId(endStationId)
                        .departureTime(trainEntity.getDepartureTimes().get(startIndex))
                        .arrivalTime(trainEntity.getArrivalTimes().get(endIndex))
                        .ticketInfo(ticketInfo).build());
            }
        }
        return ret;
    }

    @Override
    public List<AdminTrainVO> listTrainsAdmin() {
        return trainDao.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(TrainMapper.INSTANCE::toAdminTrainVO).collect(Collectors.toList());
    }

    @Override
    public void addTrain(String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
            List<Date> departureTimes) {
        TrainEntity entity = TrainEntity.builder().name(name).routeId(routeId).trainType(type)
                .date(date).arrivalTimes(arrivalTimes).departureTimes(departureTimes).build();
        RouteEntity route = routeDao.findById(routeId).get();
        if (route.getStationIds().size() != entity.getArrivalTimes().size()
                || route.getStationIds().size() != entity.getDepartureTimes().size()) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "列表长度错误");
        }

        entity.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));
        switch (entity.getTrainType()) {
            case HIGH_SPEED:
                entity.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
            case NORMAL_SPEED:
                entity.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
        }
        trainDao.save(entity);
    }

    @Override
    public void changeTrain(Long id, String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
                            List<Date> departureTimes) {
        //By wyx
        TrainEntity entity = TrainEntity.builder().name(name).routeId(routeId).trainType(type)
                .date(date).arrivalTimes(arrivalTimes).departureTimes(departureTimes).build();
        RouteEntity route = routeDao.findById(routeId).get();
        if (route.getStationIds().size() != entity.getArrivalTimes().size()
                || route.getStationIds().size() != entity.getDepartureTimes().size()) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "列表长度错误");
        }
        entity.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));
        switch (entity.getTrainType()) {
            case HIGH_SPEED:
                entity.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
            case NORMAL_SPEED:
                entity.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
        }
        //多加一个删除
        trainDao.deleteById(id);
        trainDao.save(entity);

    }
    @Override
    public void deleteTrain(Long id) {
        trainDao.deleteById(id);
    }
}
