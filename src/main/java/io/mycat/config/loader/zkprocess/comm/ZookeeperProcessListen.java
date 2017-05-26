package io.mycat.config.loader.zkprocess.comm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.mycat.config.loader.zkprocess.console.ZkNotifyCfg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.config.loader.console.ZookeeperPath;

/**
 * 进行zookeeper操作的监控器器父类信息
 * 
 * @author liujun
 * 
 * @date 2015年2月4日
 * @vsersion 0.0.1
 */
public class ZookeeperProcessListen {

    /**
     * 日志
    * @字段说明 LOGGER
    */
    private static final Logger lOG = LoggerFactory.getLogger(ZookeeperProcessListen.class);

    /**
     * 所有更新缓存操作的集合
     */
    private Map<String, NotifyService> listenCache = new HashMap<String, NotifyService>();

    /**
     * 监控的路径信息
    * @字段说明 watchPath
    */
    private Map<String, Set<String>> watchPathMap = new HashMap<>();

    /**
     * 监控路径对应的缓存key的对应表
    * @字段说明 watchToListen
    */
    private Map<String, String> watchToListenMap = new HashMap<>();

    /**
     * 基本路径信息
    * @字段说明 basePath
    */
    private String basePath;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /**
     * 添加缓存更新操作
     * 
     * @param key
     * @param cacheNotifySercie
     */
    public void addListen(String key, NotifyService cacheNotifySercie) {
        listenCache.put(key, cacheNotifySercie);
    }

    /**
     * 专门针对zk设置的监控路径
    * 方法描述
    * @param key
    * @param path
    * @创建日期 2016年9月19日
    */
    public void watchPath(String key, String path) {
        Set<String> watchPaths = watchPathMap.get(key);

        if (null == watchPaths) {
            watchPaths = new HashSet<>();
        }

        watchPaths.add(path);
        watchPathMap.put(key, watchPaths);
    }

    /**
     * 进行监控路径的转换
    * 方法描述
    * @创建日期 2016年9月20日
    */
    public void watchToParse() {
        if (null != watchPathMap && !watchPathMap.isEmpty()) {
            for (Entry<String, Set<String>> watchPathEntry : watchPathMap.entrySet()) {
                for (String path : watchPathEntry.getValue()) {
                    watchToListenMap.put(watchPathEntry.getKey() + ZookeeperPath.ZK_SEPARATOR.getKey() + path,
                            watchPathEntry.getKey());
                }
            }
        }
    }

    /**
     * 返回路径集合
    * 方法描述
    * @return
    * @创建日期 2016年9月19日
    */
    public Set<String> getWatchPath() {

        if (watchToListenMap.isEmpty()) {
            this.watchToParse();
        }

        return watchToListenMap.keySet();
    }

    /**
     * 进行缓存更新通知
     * 
     * @param key
     *            缓存模块的key
     * @return true 当前缓存模块数据更新成功，false，当前缓存数据更新失败
     */
    public boolean notify(String key) {
        boolean result = false;

        if (null != key && !"".equals(key)) {

            // 进行配制加载所有
            if (ZkNotifyCfg.ZK_NOTIFY_LOAD_ALL.getKey().equals(key)) {
                this.notifyAllNode();
            }
            // 如果是具体的单独更新，则进行单业务的业务刷新
            else {
                String watchListen = watchToListenMap.get(key);

                if (null != watchListen) {
                    // 取得具体的业务监听信息
                    NotifyService cacheService = listenCache.get(watchListen);

                    if (null != cacheService) {
                        try {
                            result = cacheService.notifyProcess();
                        } catch (Exception e) {
                            lOG.error("ZookeeperProcessListen notify key :" + key + " error:Exception info:", e);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * 进行通知所有缓存进行更新操作
     */
    private void notifyAllNode() {

        Iterator<Entry<String, NotifyService>> notifyIter = listenCache.entrySet().iterator();

        Entry<String, NotifyService> item = null;

        while (notifyIter.hasNext()) {
            item = notifyIter.next();

            // 进行缓存更新通知操作
            if (null != item.getValue()) {
                try {
                    item.getValue().notifyProcess();
                } catch (Exception e) {
                    lOG.error("ZookeeperProcessListen notifyAllNode key :" + item.getKey() + ";value " + item.getValue()
                            + ";error:Exception info:", e);
                }
            }
        }
    }

}
