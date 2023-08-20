package org.fffd.l23o6.util.strategy.payment;

import org.fffd.l23o6.dao.UserDao;

public interface Payment {
    boolean pay(UserDao userDao, Long userId, double amount);
}
