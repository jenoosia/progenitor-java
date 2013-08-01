package com.nvx.bootstrap.spring;

import java.lang.reflect.Field;

import javax.sql.DataSource;

import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.nvx.tools.logging.Logging;

@Configuration //Replaces Spring XML configuration
@ComponentScan(basePackages = "com.enovax") //Scans packages for Spring IOC/DI Annotations
@EnableTransactionManagement //Enables declarative Transaction annotations
public class SpringAppConfig {
    
    
    public static final String PROPS_DATA_SOURCE = "data-source.properties";
    public static final String PROPS_APP = "application.properties";

    /**
     * Used to enable placeholder values in @Value using the ${prop.key} EL. <br />
     * Has to be a static method.
     * @return
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer pspc = 
                new PropertySourcesPlaceholderConfigurer();
        final Resource[] resources = new ClassPathResource[] {
                new ClassPathResource(PROPS_DATA_SOURCE),
                new ClassPathResource(PROPS_APP)
        };

        pspc.setLocations(resources);
        pspc.setIgnoreUnresolvablePlaceholders(true);
        return pspc;
    }


    private @Value("${ds.driverClass}") String driverClass;
    private @Value("${ds.jdbcUrl}") String jdbcUrl;
    private @Value("${ds.jdbcUser}") String jdbcUser;
    private @Value("${ds.jdbcPassword}") String jdbcPassword;
    
    @Bean
    public DataSource sqlServerDataSource() throws Exception {
        ComboPooledDataSource c3p0 = new ComboPooledDataSource();
        
        c3p0.setDriverClass(driverClass);
        c3p0.setJdbcUrl(jdbcUrl);
        c3p0.setUser(jdbcUser);
        c3p0.setPassword(jdbcPassword);
        c3p0.setAcquireIncrement(20);
        c3p0.setIdleConnectionTestPeriod(120);
        c3p0.setMaxPoolSize(255);
        c3p0.setMaxStatementsPerConnection(20);
        c3p0.setMinPoolSize(32);
        c3p0.setMaxIdleTime(180);
        c3p0.setNumHelperThreads(3);
        
        return c3p0;
    }
    
    @Bean
    @Autowired
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager htm = new HibernateTransactionManager();
        htm.setSessionFactory(sessionFactory);
        return htm;
    }
    
    @Bean
    public AnnotationSessionFactoryBean getSessionFactory() throws Exception {
        AnnotationSessionFactoryBean sf = new AnnotationSessionFactoryBean();
        sf.setDataSource(sqlServerDataSource());
        sf.setPackagesToScan(new String[]{"com.enovax.mflg.datamodel", "com.enovax.mflg.model"});
        sf.setHibernateProperties(PropertiesLoaderUtils.loadProperties(
                new ClassPathResource(PROPS_DATA_SOURCE)));
        return sf;
    }
    
    @Bean
    public static PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
    
    @Bean
    public BeanPostProcessor loggerPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }
            
            @Override
            public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
                ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {
                    @Override
                    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                        ReflectionUtils.makeAccessible(field); // make the field accessible if defined private
                        if (field.getAnnotation(Logging.class) != null) { 
                            final Logger log = LoggerFactory.getLogger(bean.getClass());
                            field.set(bean, log);
                        }  
                    }
                });
                return bean;
            }
        };
    }

    
    @Bean
    public DefaultWebSecurityManager securityManager() {
        final DefaultWebSecurityManager sm = new DefaultWebSecurityManager();
        //sm.setRealm(mainRealm()); set after initialization
        
        return sm;
    }
    
    @Bean
    public ShiroFilterFactoryBean shiroFilter() {
        final ShiroFilterFactoryBean filter = new ShiroFilterFactoryBean();
        filter.setSecurityManager(securityManager());
        
        //Will use Struts2 Interceptors instead
//        Map<String, String> chain = new HashMap<String, String>();
//        chain.put("/admin/**", "authc");
//        filter.setFilterChainDefinitionMap(chain);
        
        return filter;
    }
    
    /**
     * This method needs to be static due to issues defined here:<br>
     * https://issues.apache.org/jira/browse/SHIRO-222
     */
    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        LifecycleBeanPostProcessor lbpp = new LifecycleBeanPostProcessor();
        return lbpp;
    }
    
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public static DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }
    
    @Bean
    @Autowired
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advBean = new AuthorizationAttributeSourceAdvisor();
        advBean.setSecurityManager(securityManager);
        return advBean;
    }
}
