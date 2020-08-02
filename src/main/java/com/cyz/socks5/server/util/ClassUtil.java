package com.cyz.socks5.server.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ClassUtil {

    public static List<Class> listClasses(final String pkgName, ClassLoader classLoader)
            throws IOException, URISyntaxException, ClassNotFoundException {
        if(pkgName == null){
            throw new IllegalArgumentException("pkgName");
        }
        List<Class> result = new ArrayList<>();
        //like com/webank
        String dirResource = pkgName.replace(".","/");
        Enumeration<URL> enumeration = classLoader.getResources(dirResource);
        while (enumeration.hasMoreElements()){
            URL url = enumeration.nextElement();
            String protocal = url.getProtocol();
            if(protocal == "file"){
                File folder = new File(url.toURI());
                File[] files = folder.listFiles();
                for(File f: files){
                    String fileName = f.getName();
                    if(fileName.endsWith(".class")){
                        String className = pkgName + "." + fileName.substring(0, fileName.indexOf(".class"));
                        Class clz = classLoader.loadClass(className);
                        result.add(clz);
                    }
                }
            }
            else if(protocal == "jar"){
                String res = url.toString();
                int begin = "jar:file:".length();
                int end = res.lastIndexOf("!");
                String jarFilePosition = res.substring(begin, end);
                JarFile jarFile = new JarFile(new File(jarFilePosition));
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()){
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if(entryName.startsWith(dirResource) && entryName.endsWith(".class")){
                        String classname = pkgName + "." + entryName.substring(dirResource.length()+1, entryName.lastIndexOf(".class"));
                        result.add(classLoader.loadClass(classname));
                    }
                }
            }
        }
        return result;
    }

    private ClassUtil(){}
}
