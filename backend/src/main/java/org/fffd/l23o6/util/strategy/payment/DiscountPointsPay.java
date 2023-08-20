package org.fffd.l23o6.util.strategy.payment;

import lombok.RequiredArgsConstructor;
import org.fffd.l23o6.dao.UserDao;
import org.fffd.l23o6.pojo.entity.UserEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscountPointsPay implements Payment{


    @Override
    public boolean pay(UserDao userDao, Long userId, double amount) {
        UserEntity userEntity = userDao.findById(userId).get();
        int mileagePoints = (int)(amount * 100);
        if(mileagePoints > userEntity.getDiscountPoints()){
            return false;
        }
        userEntity.setDiscountPoints(userEntity.getDiscountPoints() - mileagePoints);
        userDao.save(userEntity);
        return true;
    }
}
