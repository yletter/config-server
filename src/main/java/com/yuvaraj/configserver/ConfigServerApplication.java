package com.yuvaraj.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;

@SpringBootApplication
@EnableConfigServer
public class CloudConfigServerApplication extends SpringBootServletInitializer implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(CloudConfigServerApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CloudConfigServerApplication.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        CloudConfigServerApplication.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "oid.crossAccountRole", name = "enabled", havingValue = "true")
    public AWSCredentialsProvider oidCredentialsProvider() {
        // Sts client uses credentials from normal instance role


	        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder
                .standard()
                .withRegion(Regions.fromName("us-east-1"))
                .build();
        return new STSAssumeRoleSessionCredentialsProvider
                .Builder("arn:aws:iam::044014527626:role/cross-account-role", "yuvaraj-session")
                .withStsClient(stsClient)
                .build();
    }

}
