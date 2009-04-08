/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.scrplugin.tags.annotation.defaulttag;

import com.thoughtworks.qdox.model.Annotation;

public abstract class Util {

    public static boolean getValue(Annotation annotation, String name, boolean defaultValue) {
        final Object obj = annotation.getNamedParameter(name);
        if ( obj != null ) {
            if ( obj instanceof String ) {
                return Boolean.valueOf((String)obj);
            } else if ( obj instanceof Boolean ) {
                return (Boolean)obj;
            }
            return Boolean.valueOf(obj.toString());
        }
        return defaultValue;
    }

    public static String getValue(Annotation annotation, String name, String defaultValue) {
        final Object obj = annotation.getNamedParameter(name);
        if ( obj != null ) {
            if ( obj instanceof String ) {
                return (String)obj;
            }
            return obj.toString();
        }
        return defaultValue;
    }
}