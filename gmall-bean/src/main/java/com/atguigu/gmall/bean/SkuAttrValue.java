package com.atguigu.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
@Data
public class SkuAttrValue implements Serializable {
    @Id
    @Column
    String id;

    // baseAttrInfo.id
    @Column
    String attrId;
    // baseAttrValue.id
    @Column
    String valueId;

    @Column
    String skuId;
}
