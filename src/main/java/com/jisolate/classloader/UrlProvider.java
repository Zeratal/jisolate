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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.jisolate.util.ClassPathUtil;

public class UrlProvider {

    private static final Logger LOG = LoggerFactory.getLogger(UrlProvider.class);

    public static URL[] getClassPathUrls(final Collection<String> jarsToExcludeFromClassPath) {
        return getClassPathUrls(ClassPathUtil.getJavaHome(System.getProperties()),
                ClassPathUtil.getClassPath(),
                ClassPathUtil.getPathSeparator(System.getProperties()), jarsToExcludeFromClassPath);
    }

    private static URL[] getClassPathUrls(final String javaHomePath, final String classPath,
            final String pathSeparator, final Collection<String> jarsToExcludeFromClassPath) {
        final List<URL> classPathUrls = Lists.newArrayList();
        for (final String classPathElement : classPath.split(pathSeparator)) {
            if (!classPathElement.startsWith(javaHomePath)) {
                boolean includeInClassPath = true;
                if (jarsToExcludeFromClassPath != null) {
                    for (final String jarToExclude : jarsToExcludeFromClassPath) {
                        final String trimmedJarToExclude = jarToExclude.trim();
                        if (trimmedJarToExclude.length() != 0
                                && classPathElement.contains(trimmedJarToExclude)) {
                            LOG.debug("JAR {} excluded from classpath", trimmedJarToExclude);
                            includeInClassPath = false;
                        }
                    }
                }
                if (includeInClassPath) {
                    try {
                        classPathUrls.add(new File(classPathElement).toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw Throwables.propagate(e);
                    }
                }
            }
        }
        return classPathUrls.toArray(new URL[classPathUrls.size()]);
    }
}