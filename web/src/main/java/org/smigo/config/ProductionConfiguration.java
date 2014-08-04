package org.smigo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smigo.user.UserAdaptiveMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;

@Configuration
@Profile(EnvironmentProfile.PRODUCTION)
public class ProductionConfiguration extends WebMvcConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Bean(name = "dataSource")
    public DataSource getDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setMaximumPoolSize(15);
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mysql://smigo.org/nosslin2_db");
        ds.setUsername("nosslin2_dbuser");
        ds.setPassword("N9WM[ONGP5yv");
        return ds;
    }


    @Bean
    public HostEnvironmentInfo hostEnvironmentInfo() {
        return new HostEnvironmentInfo(EnvironmentProfile.PRODUCTION, false, "/home/nosslin2/public_html/pic/");
    }

    @Bean
    public MessageSource messageSource() {
        log.debug("getMessageSource");
        return new UserAdaptiveMessageSource(-1);
    }

    @Bean
    public Props props() {
        return new Props() {
            @Override
            public String getResetUrl() {
                return "http://smigo.org/login-reset/";
            }
        };
    }


}
