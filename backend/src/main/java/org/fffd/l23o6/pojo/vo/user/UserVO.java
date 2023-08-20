package org.fffd.l23o6.pojo.vo.user;

import lombok.Data;

@Data
public class UserVO {
    private String username;
    private String name;
    private String phone;
    private String idn;
    private String type;
    // Todo(finished): add Credit
    private int MileagePoints;
    // 里程积分：每完成一个订单增长50里程积分，显示在个人信息中，用于打折
    private int DiscountPoints;
    // 抵扣积分：每完成一个订单增长10抵扣积分，现实在个人信息中，用于抵扣金额
}
