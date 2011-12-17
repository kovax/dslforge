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

import java.util.*;

/**
 * A somewhat backwards-compatible {@link ConfigSlurper} alternative that adds schema support.
 * <p/>
 * Not currently supported:
 * <ul><li>Flattened properties, such as in traditional dotted properties files.
 * </li><li>The special <code>envrionments</code> enclosure.
 * </li></ul>
 *
 * @author didge
 * @version $Id: MetaConfigBuilder.java 37 2008-09-04 00:31:12Z didge $
 */
public class MetaConfigBuilder extends MetaBuilder {

    public MetaConfigBuilder() {
        setDefaultBuildNodeFactory(new MetaConfigObjectFactory());
    }

    protected class MetaConfigObjectFactory extends AbstractFactory {
        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            return new ConfigObject();
        }

        public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
            MetaObjectGraphBuilder mogb = (MetaObjectGraphBuilder)builder;
            SchemaNode schema = mogb.getCurrentSchema();
            Object name = schema.name();
            ((ConfigObject)parent).put(name, child);
        }
    }
}