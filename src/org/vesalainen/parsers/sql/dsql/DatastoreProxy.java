/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.parsers.sql.dsql;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

/**
 * @author Timo Vesalainen
 */
public abstract class DatastoreProxy<T> implements Runnable, InvocationHandler
{
    private String server;
    private int port = 443;
    private String user;
    private String password;
    private BlockingQueue<MethodCall> queue = new SynchronousQueue<>();
    private Semaphore semaphore = new Semaphore(0);
    private Thread thread;
    private Class<T> cls;
    private T proxy;

    public DatastoreProxy(String server, String user, String password, Class<T> cls)
    {
        this.server = server;
        this.user = user;
        this.password = password;
        this.cls = cls;
    }

    public void start() throws InterruptedException
    {
        thread = new Thread(this, getClass().getName());
        thread.setDaemon(true);
        thread.start();
        proxy = (T) Proxy.newProxyInstance(
                cls.getClassLoader(),
                new Class<?>[]
                {
                    cls
                },
                this);
    }

    public T getProxy()
    {
        return proxy;
    }

    public void stop()
    {
        thread.interrupt();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        MethodCall mc = new MethodCall(proxy, method, args);
        queue.put(mc);
        semaphore.acquire();
        if (mc.succeeded())
        {
            return mc.getReturnValue();
        }
        else
        {
            throw mc.getThrowable();
        }
    }

    @Override
    public void run()
    {
        try
        {
            RemoteApiInstaller installer = new RemoteApiInstaller();
            RemoteApiOptions options = new RemoteApiOptions();
            options.server(server, port);
            options.credentials(user, password);
            installer.install(options);
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            T engine = create(datastore);
            while (true)
            {
                MethodCall mc = queue.take();
                try
                {
                    Object rv = mc.invoke(engine);
                    mc.setReturnValue(rv);
                }
                catch (InvocationTargetException ex)
                {
                    mc.setThrowable(ex.getCause());
                }
                semaphore.release();
            }
        }
        catch (InterruptedException ex)
        {
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    protected abstract T create(DatastoreService datastore);

    private static class MethodCall
    {

        private Object proxy;
        private Method method;
        private Object[] args;
        private Object returnValue;
        private Throwable throwable;

        public MethodCall(Object proxy, Method method, Object[] args)
        {
            this.proxy = proxy;
            this.method = method;
            this.args = args;
        }

        public Object invoke(Object obj) throws InvocationTargetException
        {
            try
            {
                return method.invoke(obj, args);
            }
            catch (IllegalAccessException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }

        public boolean succeeded()
        {
            return throwable == null;
        }

        public Object[] getArgs()
        {
            return args;
        }

        public Method getMethod()
        {
            return method;
        }

        public Object getProxy()
        {
            return proxy;
        }

        public Object getReturnValue()
        {
            return returnValue;
        }

        public void setReturnValue(Object returnValue)
        {
            this.returnValue = returnValue;
        }

        public Throwable getThrowable()
        {
            return throwable;
        }

        public void setThrowable(Throwable throwable)
        {
            this.throwable = throwable;
        }
    }
}
