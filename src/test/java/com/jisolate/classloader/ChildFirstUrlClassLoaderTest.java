/*
 * Copyright (c) 2013 Christian Gleissner.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the jisolate nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.jisolate.classloader;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ChildFirstUrlClassLoaderTest {

    private final static String CLASS_NAME = "com.jisolate.classloader.IsolatableClass";
    private ClassLoader classLoader;
    private ChildFirstUrlClassLoader isolatedClassLoader;

    @Test
    public void canInvokeIsolatedPingMethod() throws Exception {
        Class<?> nonIsolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Object result = invoke(nonIsolatedClass, "ping", new Class[] { String.class },
                new Object[] { "world" });
        assertEquals("pong world", result);
    }

    @Test
    public void canInvokeIsolatedStaticPingMethod() throws Exception {
        Class<?> isolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Object result = invoke(isolatedClass, "staticPing", new Class[] { String.class },
                new Object[] { "world" });
        assertEquals("staticPong world", result);
    }

    @Test
    public void canInvokeNonIsolatedPingMethod() throws Exception {
        Class<?> nonIsolatedClass = classLoader.loadClass(CLASS_NAME);
        Object result = invoke(nonIsolatedClass, "ping", new Class[] { String.class },
                new Object[] { "world" });
        assertEquals("pong world", result);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void isolatedClassIsNotSameAsNonIsolatedClass() throws Exception {
        Class isolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Class nonIsolatedClass = classLoader.loadClass(CLASS_NAME);
        assertThat(isolatedClass, not(nonIsolatedClass));
    }

    @Before
    public void setUp() {
        URL[] urls = UrlProvider.getClassPathUrls(Lists.<String> newArrayList());
        classLoader = Thread.currentThread().getContextClassLoader();
        isolatedClassLoader = new ChildFirstUrlClassLoader(urls, classLoader);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void transitiveIsolationWorks() throws Exception {
        Class isolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Object isolatedObject = invoke(isolatedClass, "getMessageWrapper",
                new Class[] { String.class }, new Object[] { "msg" });

        Class nonIsolatedClass = classLoader.loadClass(CLASS_NAME);
        Object nonIsolatedObject = invoke(nonIsolatedClass, "getMessageWrapper",
                new Class[] { String.class }, new Object[] { "msg" });

        assertTrue(isolatedObject.getClass() != nonIsolatedObject.getClass());
        assertTrue(isolatedObject.getClass().getName()
                .equals(nonIsolatedObject.getClass().getName()));
        assertEquals("" + isolatedObject, "" + nonIsolatedObject);
    }

    private Object invoke(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object[] args)
            throws Exception {
        Object o = clazz.newInstance();
        Method m = clazz.getMethod(methodName, paramTypes);
        Object result = m.invoke(o, args);
        return result;
    }
}