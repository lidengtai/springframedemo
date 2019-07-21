package com.taylor.servlet;

import com.taylor.annotation.MyAutowired;
import com.taylor.annotation.MyController;
import com.taylor.annotation.MyRequestMapping;
import com.taylor.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 启动入口
 * 初始化流程
 * 1 定义成员变量
 * 2 简写流程步骤
 */
public class MyDispatcherServlet extends HttpServlet {


    //跟web.xml中param-name 的值一致
    private static final String LOCATION = "contextConfigLocation";

    //保存所有的配置信息
    private Properties properties = new  Properties();

    //保存所有扫描到的相关类名
    private List<String> classNames = new ArrayList<String>();

    //核心IOC容器，保存所有初始化的bean
    private Map<String,Object> ioc = new HashMap<String,Object>();

    //保存所有的URL和方法的映射关系
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();



    public MyDispatcherServlet(){
        super();
    }

    /**
     * 当 Servlet 容器启动时，会调用 MyDispatcherServlet 的 init()方法，
     * 从 init 方法的参数中，我们可以拿到主配置文件的路径，从能够读取到配置文件中的信息
     * 初始化 加载配置文件
     * @param servletConfig
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        // 1 加载配置文件
        doLoadConfig(servletConfig.getInitParameter(LOCATION));

        // 2 扫描所有相关类
        doScanner(properties.getProperty("scanPackage"));

        // 3 初始化所有相关类的实例，并保存到ioc容器中
        doInstance();

        // 4 依赖注入
        doAutowired();

        // 5 构造handlerMapping
        initHandlerMapping();


    }

    /**
     *  将MyRequestMapping 中配置的信息和 Method 进行关联，并保存这些关系
     */
    private void initHandlerMapping() {
        if (ioc.isEmpty())return;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class))continue;

            String baseUrl ="";
            //获取controller中的url配置
            if (clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //获取方法的url配置
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                //没有加requestmapping注解直接忽略
                if (!method.isAnnotationPresent(MyRequestMapping.class))continue;

                //映射url
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                String url = ("/"+baseUrl+"/"+requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println("url和Method关系 "+ url + ","+ method);
            }


        }
    }

    /**
     * 初始化到 IOC 容器中的类，需要赋值的字段进行赋值
     */
    private void doAutowired() {
        if (ioc.isEmpty())return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //拿到实例对象中的所有属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MyAutowired.class))continue;
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                //设置私有属性的访问权限
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    /**
     * 初始化所有相关的类，并放入到 IOC 容器之中。IOC 容器的 key 默认是类名首字母小写，
     * 如果是自己设置类名，则优先使用自定义的。因此，要先写一个针对类名首字母处理的工具方法。
     */
    private void doInstance() {
        if (classNames.size()==0)return;
        try {
            for(String className:classNames){
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)){
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,clazz.newInstance());
                }else if (clazz.isAnnotationPresent(MyService.class)){
                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName = service.value();
                    //若果用户设置了自己名称就设置的名字
                    if (!"".equals(beanName.trim())){
                        ioc.put(beanName,clazz.newInstance());
                        continue;
                    }
                    //如果没设 则按接口类型创建一个实例
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces){
                        ioc.put(i.getName(),clazz.newInstance());
                    }
                }else {
                    continue;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 首字母小写
     * @param str
     * @return
     */
    private String lowerFirstCase(String str){
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 递归扫描出所有的 Class 文件
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        //将所有的包路径转换成文件路径
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file: dir.listFiles()){
            //如果是文件夹继续递归
            if (file.isDirectory()) {
                doScanner(scanPackage+"."+file.getName());
            }else {
                classNames.add(scanPackage+"."+file.getName().replace(".class","").trim());
            }
        }
    }

    /**
     * 文件读取到 Properties 对象中
     * @param location
     */
    private void doLoadConfig(String location) {
        InputStream input = null;
        input = this.getClass().getClassLoader().getResourceAsStream(location);
        //读取配置文件
        try {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //匹配到开始对应的方法
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param req
     * 等待请求 匹配url 定位方法 反射调用执行
     * @param resp
     * @throws Exception
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (this.handlerMapping.isEmpty())return;

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");
        if (!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404");
            return;
        }
        Map<String,String[]> parameterMap = req.getParameterMap();
        Method method = this.handlerMapping.get(url);
        String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(this.ioc.get(beanName), req, resp, parameterMap.get("name")[0]);
    }
}
