package com.xawl.cateen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 美食评估管理系统启动类
 *
 * @author xawl
 * @date 2025-10-03
 */
@SpringBootApplication
@MapperScan("com.xawl.cateen.mapper")
public class CateenApplication {

    public static void main(String[] args) {
        SpringApplication.run(CateenApplication.class, args);
        System.out.println("====================================");
        System.out.println("===  美食评估管理系统启动成功！  ===");
        System.out.println("====================================");
    }

}

