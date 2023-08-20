package org.fffd.l23o6.pojo.vo.order;
//create by zyh
public class OrderPriceCalculator {

    private static final int[] SCORE_RANGE = {0, 1000, 3000, 10000, 30000, 50000};

    private static final double[] SCORE_DISCOUNT = {0, 0.1, 0.15, 0.2, 0.25, 0.3};

    public double calculateOrderPrice(int score, double basePrice) {
        double discount = 0;
        for (int i = 0; i < SCORE_RANGE.length; i++) {
            if(i == SCORE_RANGE.length - 1){
                discount = SCORE_DISCOUNT[i];
                break;
            }
            if (score >= SCORE_RANGE[i] && (score < SCORE_RANGE[i + 1])) {
                discount = SCORE_DISCOUNT[i];
                break;
            }

        }
        double totalPrice = basePrice * (1 - discount);
        return totalPrice;
    }
}



