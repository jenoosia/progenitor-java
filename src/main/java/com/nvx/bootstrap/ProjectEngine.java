package com.nvx.bootstrap;

import java.util.Properties;

public class ProjectEngine {
    
    /* file.properties */
    
    private static Properties fileProps = null;
    public static Properties fileProps() {
        return fileProps;
    }
    public static void reloadFileProps() throws Exception {
        fileProps = null;
        fileProps = new Properties();
        fileProps.load(ProjectEngine.class.getResourceAsStream("/file.properties"));
    }

    /* project.properties */
    
    private static Properties projProps = null;
    public static Properties projProps() {
        return projProps;
    }
    public static void reloadProjProps() throws Exception {
        projProps = null;
        projProps = new Properties();
        projProps.load(ProjectEngine.class.getResourceAsStream("/project.properties"));
    }
    
    /* general methods */
    
    public static void reload() throws Exception {
        reloadFileProps();
        reloadProjProps();
    }
    public static void destroy() {
        fileProps = null;
        projProps = null;
    }
}
