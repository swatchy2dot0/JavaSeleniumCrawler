package de.urmann.base;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;
import java.util.concurrent.Callable;

public class RuntimeContext {

    private final static Logger logger = LoggerFactory.getLogger(RuntimeContext.class);

    private static final ThreadLocal<RuntimeContext> runtimeContexts = new ThreadLocal<>();

    private final Stack<String> driverContext = new Stack();

    private boolean useProxy = false;

    private RuntimeContext() {
        super();
    }

    public static synchronized RuntimeContext getCurrent() {
        RuntimeContext currentContext = runtimeContexts.get();
        if (currentContext == null) {
            currentContext = new RuntimeContext();
            logger.debug("RuntimeContext created for thread {}", Thread.currentThread().getName());
            runtimeContexts.set(currentContext);
        }
        return currentContext;
    }

    public String peekDriverContext() {
        String driverContext = this.driverContext.isEmpty() ? null : this.driverContext.peek();
        if (StringUtils.isBlank(driverContext)) {
            String newDriverContext = Thread.currentThread().getName();
            pushDriverContext(newDriverContext);
            logger.debug("created new driver context '{}'", newDriverContext);
        }
        return driverContext;
    }

    public String popDriverContext() {
        return driverContext.pop();
    }

    public void pushDriverContext(String driverContext) {
        this.driverContext.push(driverContext);
        logger.debug("driver context '{}' pushed to RuntimeContext of thread {}", driverContext, Thread.currentThread().getName());
    }

    public <T extends Object> T run(String driverContext, boolean useProxy, Callable<T> callable) throws Exception {
        RuntimeContext current = RuntimeContext.getCurrent();
        boolean oldUseProxy = current.useProxy();
        current.setUseProxy(useProxy);
        current.pushDriverContext(driverContext);
        try {
            return callable.call();
        } finally {
            current.popDriverContext();
            current.setUseProxy(oldUseProxy);
        }
    }

    public boolean useProxy() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }
}
