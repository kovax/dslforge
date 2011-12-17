/*
 *      Copyright 2008 the original author or authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovytools.builder;

import groovy.util.*;
import groovy.lang.*;

import java.util.*;

/**
 * Adapts a given {@link Closure} such that it can support the {@link Factory#newInstance(FactoryBuilderSupport, Object, Object, Map)}
 * method.  It is important that the {@link Closure} supports the proper number, type and
 * order of arguments as follows:
 * <ol>
 * <li>If no arguments are supported, it is called without arguments.</li>
 * <li>If one argument is supported, it is the node's name.</li>
 * <li>If two arguments are supported, they are the node's name and value.</li>
 * <li>If three arguments are supported, they are the node's name, value, and attributes.</li>
 * </ol>
 *
 * @author didge
 * @version $Id: ClosureFactoryAdapter.java 35 2008-08-29 20:59:09Z didge $
 */
public class ClosureFactoryAdapter extends AbstractFactory {
    protected Closure closure;

    public ClosureFactoryAdapter(Closure closure) {
        this.closure = closure;
    }

    /**
     * Invocations of this method are delegated to the {@link Closure} to return an object when called.
     *
     * @param builder
     * @param name
     * @param value
     * @param attributes
     *
     * @return see above
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        switch (closure.getMaximumNumberOfParameters()) {
            case 0: return closure.call();
            case 1: return closure.call(new Object[] {name});
            case 2: return closure.call(new Object[] {name, value});
            default: return closure.call(new Object[] {name, value, attributes});
        }
    }
}
