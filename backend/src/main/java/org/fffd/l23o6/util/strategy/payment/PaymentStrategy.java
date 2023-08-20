package org.fffd.l23o6.util.strategy.payment;

import java.util.HashMap;
import java.util.Map;

public abstract class PaymentStrategy {

    public static final String ALI_PAY = "1";
    public static final String DiscountPoints_PAY = "2";

    private static Map<String, Payment> PAYMENT_STRATEGY_MAP = new HashMap<>();

    static {
        PAYMENT_STRATEGY_MAP.put(ALI_PAY, new AliPay());
        PAYMENT_STRATEGY_MAP.put(DiscountPoints_PAY, new DiscountPointsPay());
    }
    /**
     * 获取支付方式类
     * @param payType 前端传入支付方式
     * @return
     */
    public static Payment getPayment(String payType) {
        Payment payment = PAYMENT_STRATEGY_MAP.get(payType);
        if (payment == null) {
            throw new NullPointerException("支付方式选择错误");
        }
        return payment;
    }

    // TODO: implement this by adding necessary methods and implement specified strategy
}
