package com.how2java.tmall.interceptor;

import com.how2java.tmall.util.RedisUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MethodCacheInterceptor implements MethodInterceptor {
    private static final Logger logger = LogManager.getLogger(MethodCacheInterceptor.class);

    private RedisUtil redisUtil;

    //不加入缓存的service名称
    private List<String> targetNamesList;

    //不加入缓存的方法名称
    private List<String> methodNamesList;

    //缓存默认过期时间
    private String defaultCacheExpireTime;

    public RedisUtil getRedisUtil() {
        return redisUtil;
    }

    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public List<String> getTargetNamesList() {
        return targetNamesList;
    }

    public void setTargetNamesList(List<String> targetNamesList) {
        this.targetNamesList = targetNamesList;
    }

    public List<String> getMethodNamesList() {
        return methodNamesList;
    }

    public void setMethodNamesList(List<String> methodNamesList) {
        this.methodNamesList = methodNamesList;
    }

    public String getDefaultCacheExpireTime() {
        return defaultCacheExpireTime;
    }

    public void setDefaultCacheExpireTime(String defaultCacheExpireTime) {
        this.defaultCacheExpireTime = defaultCacheExpireTime;
    }

    /**
     * 初始化读取不需要缓存的类名和方法名称
     */
    public MethodCacheInterceptor() {
        try {
            String[] targetNames = {};
            String[] methodNames = {};

            //创建list
            targetNamesList = new ArrayList<>(targetNames.length);
            methodNamesList = new ArrayList<>(methodNames.length);
            Integer maxLen = targetNames.length > methodNames.length ? targetNames.length : methodNames.length;
            //将不需要缓存的类和方法加入到list中
            for(int i = 0; i < maxLen; i++) {
                if(i < targetNames.length)
                    targetNamesList.add(targetNames[i]);
                if(i < methodNames.length)
                    methodNamesList.add(methodNames[i]);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object value = null;

        String targetName = invocation.getThis().getClass().getName();
        String methodName = invocation.getMethod().getName();

        // 不需要缓存的内容
        if (!isAddCache(targetName, methodName)) {
            // 执行方法返回结果
            return invocation.proceed();
        }

        Object[] arguments = invocation.getArguments();
        String key = getCacheKey(targetName, methodName, arguments);
        try {
            // 判断是否有缓存
            if (redisUtil.exists(key)) {
                return redisUtil.get(key);
            }
            // 写入缓存
            value = invocation.proceed();
            if (value != null) {
                final String tKey = key;
                final Object tValue = value;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        redisUtil.set(tKey, tValue, Long.parseLong(defaultCacheExpireTime));
                    }
                }).start();
            }
        } catch (Exception e) {
            logger.error(e);
            if (value == null) {
                return invocation.proceed();
            }
        }
        return value;
    }

    /**
     * 判断是否加入缓存
     *
     * @param targetName
     * @param methodName
     * @return
     */
    private boolean isAddCache(String targetName, String methodName) {
        boolean flag = true;
        if(targetNamesList.contains(targetName) ||
                methodNamesList.contains(methodName) ||
                targetNamesList.contains("$$EnhancerBySpringCGLIB$$")) {
            flag = false;
        }
        return flag;
    }

    /**
     * 创建缓存key
     *
     * @param targetName
     * @param methodName
     * @param arguments
     * @return
     */
    private String getCacheKey(String targetName, String methodName, Object[] arguments) {
        StringBuffer sb = new StringBuffer();
        sb.append(targetName).append("_").append(methodName);
        if(arguments != null && arguments.length != 0) {
            for(int i = 0; i < arguments.length; i++) {
                sb.append("_").append(arguments[i]);
            }
        }
        return sb.toString();
    }
}
