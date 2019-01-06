package com.alibaba.jvm.sandbox.core;

import com.alibaba.jvm.sandbox.core.enhance.weaver.EventListenerHandlers;
import com.alibaba.jvm.sandbox.core.manager.CoreLoadedClassDataSource;
import com.alibaba.jvm.sandbox.core.manager.CoreModuleManager;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultCoreModuleManager;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultLoadedClassDataSource;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultProviderManager;
import com.alibaba.jvm.sandbox.core.util.SpyUtils;

import java.lang.instrument.Instrumentation;

public class JvmSandbox {

    private final CoreConfigure cfg;
    private final Instrumentation inst;
    private final CoreLoadedClassDataSource loadedClassDataSource;
    private final CoreModuleManager coreModuleManager;

    public JvmSandbox(final CoreConfigure cfg,
                      final Instrumentation inst) {
        EventListenerHandlers.getSingleton();
        this.cfg = cfg;
        this.coreModuleManager = new DefaultCoreModuleManager(
                cfg,
                this.inst = inst,
                this.loadedClassDataSource = new DefaultLoadedClassDataSource(inst, cfg.isEnableUnsafe()),
                new DefaultProviderManager(cfg)
        );

        init();
    }

    private void init() {
        SpyUtils.init(cfg.getNamespace());
        inst.addTransformer(this.loadedClassDataSource);
    }


    /**
     * 获取模块管理器
     *
     * @return
     */
    public CoreModuleManager getCoreModuleManager() {
        return coreModuleManager;
    }

    public void destroy() {

        inst.removeTransformer(loadedClassDataSource);

        // 卸载所有的模块
        coreModuleManager.unloadAll();

        // 清理Spy
        SpyUtils.clean(cfg.getNamespace());

    }

}