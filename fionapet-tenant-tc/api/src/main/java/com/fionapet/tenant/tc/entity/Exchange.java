package com.fionapet.tenant.tc.entity;


import com.fionapet.tenant.security.audit.AbstractAuditableEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "exchange")
@Getter
@Setter
@ToString
public class Exchange extends AbstractAuditableEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    /**
     * 网址
     */
    private String website;

    /**
     * 位置
     */
    private String location;

    /**
     * 限制ip
     */
    private String ip;

    /**
     * 支持 期货
     */
    private boolean future;

    /**
     * 支持 法币
     */
    private String fiat;

    /**
     * 实例
     */
    private String instanceName;

}
