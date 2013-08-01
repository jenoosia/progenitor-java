package com.nvx.bootstrap;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.Task;

import java.beans.Introspector;
import java.lang.ref.WeakReference;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import ch.qos.logback.classic.LoggerContext;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class AppListener implements ServletContextListener {

    private WeakReference<AnnotationConfigWebApplicationContext> ctx;
    public AppListener(AnnotationConfigWebApplicationContext ctx) {
        this.ctx = new WeakReference<AnnotationConfigWebApplicationContext>(ctx);
    }
    
    private static final Logger log = LoggerFactory.getLogger(AppListener.class);
    
    private Scheduler scheduler;
    public static final String SKIP_JOB = "skip";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            this.ctx.get().getAutowireCapableBeanFactory().autowireBean(this);

            /* Initialize engine */
            
            log.info("Reloading Engine properties.");
            ProjectEngine.reload();
            
            /* Initialize Shiro realm */
            
            /* Initialize batch scheduler */
            
            log.info("Initializing scheduler.");
            this.scheduler = new Scheduler();
            
            if (!SKIP_JOB.equals("skip")) {
                log.info("Scheduling a task.");
                this.scheduler.schedule("", (Task)null); //TODO Scheduled jobs
            }
            
            this.scheduler.start();
            
        } catch (Exception e) {
            System.out.println("Error loading: " + e.getMessage());
            throw new Error("Critical system error", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (this.scheduler.isStarted()) {
            this.scheduler.stop();
            this.scheduler = null;
        }
        
        ((LoggerContext)LoggerFactory.getILoggerFactory()).stop();
        
        Introspector.flushCaches();
        
        ((ComboPooledDataSource)this.ctx.get().getBean("sqlServerDataSource")).close();
        
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            try {
                DriverManager.deregisterDriver(drivers.nextElement());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        this.ctx.clear();
        this.ctx = null;
    }
}
