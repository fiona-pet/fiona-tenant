package com.fionapet.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class TcApp {

    private static final Logger log = LoggerFactory.getLogger(TcApp.class);
    
    public static void main(String[] args) {
    	SpringApplication app = new SpringApplication(TcApp.class);
        ConfigurableEnvironment env = app.run(args).getEnvironment();
        log.info("\n------------------------------------------\n" +
        		"Application '{}' is running at localhost:{}\n" + 
        		"------------------------------------------", 
        		env.getProperty("spring.application.name"),
        		env.getProperty("server.port")
        		);
    }

}
