package org.fffd.l23o6.util.strategy.payment;

import org.fffd.l23o6.dao.UserDao;


public class AliPay implements Payment {
    @Override
    public boolean pay(UserDao userDao, Long userId, double amount) {

        return true;
    }
}
