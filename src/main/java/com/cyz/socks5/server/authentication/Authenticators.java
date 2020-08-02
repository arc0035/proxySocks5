package com.cyz.socks5.server.authentication;

import com.cyz.socks5.server.enums.AuthenticationMethod;
import com.cyz.socks5.server.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Authenticators {

    private static final Logger log = LoggerFactory.getLogger(Authenticators.class);

    private static final Map<AuthenticationMethod, Authenticator> authenticators;


    static {
        authenticators = new HashMap<>();
        String pkgName = Authenticator.class.getPackage().getName();
        try{
            List<Class> classes = ClassUtil.listClasses(pkgName, Authenticator.class.getClassLoader());
            for(Class clazz: classes){
                if(clazz != Authenticator.class && Authenticator.class.isAssignableFrom(clazz)){
                    Authenticator authenticator = (Authenticator) clazz.newInstance();
                    authenticators.put(authenticator.getAuthenticationMethod(), authenticator);
                    log.info("Loading authenticator:{}", clazz);
                }
            }
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static Authenticator getAuthenticator(AuthenticationMethod method){
        return authenticators.get(method);
    }
}
