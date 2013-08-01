package com.nvx.tools.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

@Component("mailTemplate")
public class MailTemplate {
    
    private String templatePath = TEMPL_PATH_DEFAULT;
    
    public static final String TEMPL_PATH_DEFAULT = "com/enovax/tools/mail/template/";
    
    public MailTemplate() {
        
    }
    
    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }
    
    public String getTemplateString(String templateName) throws IOException {
        return IOUtils.toString(getTemplateStream(templateName), "UTF-8");
    }
    
    public InputStream getTemplateStream(String templateName) {
        return getClass().getClassLoader().getResourceAsStream(this.templatePath + templateName);
    }
    
    public String replaceParams(String template, Map<String, String> params) {
        for (Map.Entry<String, String> param : params.entrySet()) {
            template = template.replace(param.getKey(), param.getValue());
        }
        
        return template;
    }
}
