package org.fffd.l23o6.pojo.vo.train;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketInfo {
    private String type;
    private Integer count;
    private double price;
}