package com.nvx.tools.dao;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepoUtil {
    
    private static Logger log = LoggerFactory.getLogger(RepoUtil.class);
    
    public static final String SQL_PROPS_FILE = "sql.properties";
    
    private static Properties sql;
    
    static {
        log.info("Pre-loading SQL properties file for use in the system.");
        reloadProps();
    }
    
    public static void reloadProps() {
        try {
            sql = new Properties();
            sql.load(RepoUtil.class.getResourceAsStream(SQL_PROPS_FILE));
        } catch (IOException ioe) {
            log.error("Error loading repository properties. This will cascade through the entire system.", ioe);
        }
    }
    
    public static void destroy() {
        sql = null;
    }
    
    public static Properties getSqls() {
        if (devMode) { reloadProps(); }
        return sql;
    }
    
    private static boolean devMode = false;
    public static void setDevMode(boolean flag) {
        devMode = flag;
    }
    
    /*
     * SQL retrieval follows.
     */
    
    public static String getSql(String key) {
        if (devMode) { reloadProps(); }
        return sql.getProperty(key);
    }

    public static final String ORDER_CLAUSE = "{{orderClause}}";
    
    public static String getSqlCustomOrder(String key, String orderByCol, boolean asc) {
        if (devMode) { reloadProps(); }
        String baseQl = sql.getProperty(key);
        if (baseQl == null) { return null; }
        
        return baseQl.replace(ORDER_CLAUSE, "order by " + orderByCol + (asc ? " asc" : " desc"));
    }

    public static final String FILTER_CLAUSE = "{{filterClause}}";
    
    public static final String FILTER_NEGATE = "NEGATE";
    public static final String FILTER_RAW_AS_IS = "RAW_ASIS";
    public static final String FILTER_RAW_WITH_PARAM = "RAW_WITHPARAM";
    public static final String FILTER_RAW_WITH_PARAM_ESC = ":WITHPARAM:";
    public static final String FILTER_RAW_WITH_PARAM_PARTNER = "PARTNER_RAW_WITHPARAM";
    public static final String FILTER_LIKE = "FILTER_LIKE";
    public static final String FILTER_LIKE_PRE = "filterLike";
    
    public static Map<String, Object> cleanFilterMapping(Map<String, Object> dirtyFilterMap) {
        Map<String, Object> cleanedMap = new HashMap<String, Object>();
        for (String key : dirtyFilterMap.keySet()) {
            if (key.startsWith(FILTER_LIKE)) {
                cleanedMap.put(key, "%" + ((String)dirtyFilterMap.get(key)) + "%");
            } else if (key.startsWith(FILTER_RAW_WITH_PARAM)) {
                cleanedMap.put(key.replace(FILTER_RAW_WITH_PARAM, ""), 
                        dirtyFilterMap.get(FILTER_RAW_WITH_PARAM_PARTNER + key));
            } else if (key.startsWith(FILTER_RAW_WITH_PARAM_PARTNER)) {
                //do nothing
            } else if (!key.startsWith(FILTER_RAW_AS_IS)) {
                cleanedMap.put(key.replace(FILTER_NEGATE, ""), dirtyFilterMap.get(key));
            }
        }
        
        return cleanedMap;
    }

    public static String populateFilterClause(String sql, Map<String, Object> dirtyFilterMap) {
        StringBuilder sb = new StringBuilder("");
        for (String filter : dirtyFilterMap.keySet()) {
            if (filter.startsWith(FILTER_RAW_AS_IS)) {
                sb.append(" and ").append(dirtyFilterMap.get(filter)).append(" ");
            } else if (filter.startsWith(FILTER_LIKE)) {
                String likeParam = filter.replace(FILTER_LIKE, "");
                sb.append(" and ").append(likeParam).append(" like :").append(filter).append(" ");
            } else if (filter.startsWith(FILTER_RAW_WITH_PARAM)) {
                String withParam = filter.replace(FILTER_RAW_WITH_PARAM, "");
                sb.append(" and ").append(
                        ((String)dirtyFilterMap.get(filter)).replace(FILTER_RAW_WITH_PARAM_ESC, ":" + withParam)).append(" ");
            } else if (filter.startsWith(FILTER_RAW_WITH_PARAM_PARTNER)) {
                //do nothing
            } else {
                boolean equals = true;
                if (filter.startsWith(FILTER_NEGATE)) {
                    filter = filter.replace(FILTER_NEGATE, "");
                    equals = false;
                }
                sb.append(" and ").append(filter).append(equals ? " = :" : " != :").append(filter).append(" ");
            }
        }
        
        return sql.replace("{{filterClause}}", sb.toString());
    }
    
    public static void includePaginationInFilter(int rowStart, int rowEnd, Map<String, Object> filterMap) {
        filterMap.put("rowStart", rowStart);
        filterMap.put("rowEnd", rowEnd);
    }
    
    public static void includeDateRangeInFilter(String fieldName, 
            Date startDate, Date endDate, Map<String, Object> dirtyFilterMap) {
        String baseFilterName = FILTER_RAW_WITH_PARAM + "startDate";
        dirtyFilterMap.put(baseFilterName, 
                "cast(" + fieldName + " as date) >= cast(" + FILTER_RAW_WITH_PARAM_ESC + " as date)");
        dirtyFilterMap.put(FILTER_RAW_WITH_PARAM_PARTNER + baseFilterName, startDate);
        
        baseFilterName = FILTER_RAW_WITH_PARAM + "endDate";
        dirtyFilterMap.put(baseFilterName, 
                "cast(" + fieldName + " as date) <= cast(" + FILTER_RAW_WITH_PARAM_ESC + " as date)");
        dirtyFilterMap.put(FILTER_RAW_WITH_PARAM_PARTNER + baseFilterName, endDate);
    }
}
