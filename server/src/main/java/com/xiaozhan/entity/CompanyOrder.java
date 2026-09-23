package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业订单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("company_order")
public class CompanyOrder extends BaseEntity {

    private Long companyId;

    private String orderNo;

    private String packageType;

    private BigDecimal amount;

    /** 1-待支付 2-已支付 3-已取消 */
    private Integer payStatus;

    private LocalDateTime payTime;

    private String invoiceTitle;

    private String invoiceTaxNo;

    public static final int PAY_PENDING = 1;
    public static final int PAY_PAID = 2;
    public static final int PAY_CANCELED = 3;
}
