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

import groovy.lang.*;
import groovy.util.*;
import org.codehaus.groovy.runtime.*;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

/**
 * <code>MetaBuilder</code> is a builder that uses schemas to more conveniently and correctly
 * build object hierarchies.
 * <h2>Usage</h2>
 * <code>MetaBuilder</code> is easy to use.  Just follow these steps:
 * <ol>
 * <li>Create a <code>MetaBuilder</code> instance.</li>
 * <li>Define your schemas with {@link #define}.</li>
 * <li>Build your objects with {@link #build}.</li>
 * </ol>
 * <h2>Example</h2>
 * Here is a very simple example demonstrating the steps above:
 * <pre>
 * // Create a MetaBuilder
 * MetaBuilder mb = new MetaBuilder()
 *
 * // Define a schema
 * mb.define {
 *     invoice {
 *         collections {
 *             items {
 *                 item {
 *                     properties {
 *                         qty(req: true)
 *                     }
 *                 }
 *             }
 *             payments {
 *                 payment {
 *                     properties {
 *                         amt(req: true)
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 *
 * // Build an object.
 * mb.build {
 *     invoice {
 *         items {
 *             item(qty: 1)      // two equivalent ways to
 *             item { qty = 20 } // set property values
 *         }
 *         payments {
 *             payment(amt: 100.00)
 *         }
 *     }
 * }
 * </pre>
 * <a name="metaschema"/>
 * <h2>The MetaBuilder Meta-Schema</h2>
 * The schemas that may be defined are governed by the following meta-schema.  Note, <code>'%'</code> is used to stand
 * for any sequence of non-whitespace characters:
 * <pre>
 * // default factories
 * def schemaNodeFactory = new MetaBuilder.SchemaNodeFactory()
 * def collectionSchemaNodeFactory = new MetaBuilder.CollectionSchemaNodeFactory()
 *
 * // some useful check closures
 * def nullOrBoolean = { v -> v == null || v instanceof Boolean }
 * def nullOrClosure = { v -> v == null || v instanceof Closure }
 * def nullOrStringOrClosure = { v -> v == null || v instanceof String || v instanceof Closure }
 * def nullOrStringOrClassOrFactoryOrClosure = { v -> v == null || v instanceof String || v instanceof Class || v instanceof Factory || v instanceof Closure }
 * def nullOrStringOrSchemaNode = { v -> v == null || v instanceof String || v instanceof SchemaNode }
 *
 * def metaSchema = '%'(factory: schemaNodeFactory) {
 *     properties() {
 *         schema(check: nullOrStringOrSchemaNode )
 *         factory(check: nullOrStringOrClassOrFactoryOrClosure )
 *         check()
 *     }
 *     collections() {
 *         collections(factory: schemaNodeFactory) {
 *             '%'(factory: collectionSchemaNodeFactory) {
 *                 properties(factory: schemaNodeFactory) {
 *                     collection(check: nullOrStringOrClosure)
 *                     min(check: nullOrInt)
 *                     max(check: nullOrInt)
 *                     size(check: nullOrStringOrClosure)
 *                     key(check: nullOrStringOrClosure)
 *                     add(check: nullOrStringOrClosure)
 *                 }
 *                 '%'(shema: metaSchema)
 *             }
 *         }
 *         properties(factory: schemaNodeFactory) {
 *             '%'(schema: metaSchema)
 *                 properties() {
 *                     property(check: nullOrStringOrClosure)
 *                     req(check: nullOrBoolean)
 *                     def()
 *                     min()
 *                     max()
 *                     // Inherited from metaSchema:
 *                     // schema()
 *                     // factory()
 *                     // check()
 *                 }
 *             }
 *         }
 *     }
 * }
 * </pre>
 * <h2>Schema Attributes</h2>
 * The following table describes the schema attributes you may use when defining your own schema using <code>MetaBuilder</code>'s
 * default meta-schema. Values may be of type: literal, object, {@link Class} and/or {@link Closure}.
 * <p/>
 * Only one value may be specied for each property at a time.
 * <table border="1" cellspacing="0">
 * <tr>
 *  <td>Name</td>
 *  <td>Description</td>
 *  <td>Literal</td>
 *  <td>Object</td>
 *  <td>Class</td>
 *  <td>Closure</td>
 * </tr>
 * <tr>
 *  <td><code>schema</code></td>
 *  <td>Allows a schema to inherit and extend the properties and collections of another schema.  <a href="#extendingSchema">See below.</a></td>
 *  <td>The name of another schema.  Optional.  The named schema does not have to be previously defined.</td>
 *  <td>A previously defined schema object.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 * </tr>
 * <tr>
 *  <td><code>factory</code></td>
 *  <td>Specifies the factory to use for building nodes.  Optional.  The default factory builds {@link SchemaNode}s.</td>
 *  <td>A fully qualified class name for direct instatiation with {@link Class#newInstance} .</td>
 *  <td>A {@link groovy.util.Factory} object.</td>
 *  <td>A {@link Class} for direct instatiation with {@link Class#newInstance}.</td>
 *  <td>A {@link Closure} returning an object of the form
 *   <ul>
 *    <li><code>{ -> ...}</code></li>
 *    <li><code>{n -> ...}</code></li>
 *    <li><code>{n, v -> ...}</code></li>
 *    <li><code>{n, v, a -> ...}</code></li>
 *   </ul>
 *   where
 *   <ul>
 *    <li><code>n</code> is the name of the node</li>
 *    <li><code>v</code> is the value of the node (may be null)</li>
 *    <li><code>a</code> is a map of the node's attributes (may be empty)</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>check</code></td>
 *  <td>Used to specify a check on the value of the property, which will result in an exception if the check fails.  Optional.</td>
 *  <td colspan='4'>Any object or literal.  The result of the check is logically equivalent to the result of executing the Groovy switch:
 * <pre>
 *     switch(value) {
 *         case check: return true
 *     }
 *     return false
 * </pre>
 * </tr>
 * <tr>
 *  <td>collection</td>
 *  <td>Used to identify or access the actual collection name.  Optional.  The default is to access the collection using the node's name.</td>
 *  <td>A property or field name</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>A {@link Closure} returning the collection of the form
 *   <ul>
 *    <li><code>{o -> ...}</code></li>
 *   </ul>
 *   where
 *   <ul>
 *    <li><code>o</code> is the owner of the collection</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td>min (collection)</td>
 *  <td>Used to specify the minimum collection size.  Optional.  The default is no minimum.</td>
 *  <td>An <code>int</code> greater than or equal to 0.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 * </tr>
 * <tr>
 *  <td>max (collection)</td>
 *  <td>Used to specify the maximum collection size.  Optional.  The default is no maximum.</td>
 *  <td>An <code>int</code>.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 * </tr>
 * <tr>
 *  <td>size</td>
 *  <td>Used to specify an alternative way to retrieve the size of a collection.  Optional.  The default is to access the collection using the node's name.</td>
 *  <td>A property or field name</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>A {@link Closure} returning the size of the form
 *   <ul>
 *    <li><code>{o -> ...}</code></li>
 *   </ul>
 *   where
 *   <ul>
 *    <li><code>o</code> is the owner of the collection</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>key</code></td>
 *  <td>When collection is a {@link Map}, key must be defined to return the key to be used to add an object to children to the collection unless add is also specified with two arguments.</td>
 *  <td>A property or field name</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>A {@link Closure} of the form
 *   <ul>
 *    <li><code>{c -> ...}</code></li>
 *   </ul>
 *   where
 *   <ul>
 *    <li><code>c</code> is the child</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>add</code></td>
 *  <td>Used to specify an alternative for adding children to the collection.  Optional.  Useful when a modifiable collection is not accessible.</td>
 *  <td>A method name.  The method must accept two {@link Object} argument values for the key and value, in that order.</code></td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>A {@link Closure} of the form
 *   <ul>
 *    <li><code>{p, k, c -> ...}</code></li>
 *    <li><code>{p, c -> ...}</code></li>
 *   </ul>
 *   where
 *   <ul>
 *    <li><code>p</code> is the parent</li>
 *    <li><code>k</code> is the key</li>
 *    <li><code>c</code> is the child</li>
 *   </ul>
 *  Note, if the first {@link Closure} form is used, then the property <code>key</code> must be specfied.  By using the
 *  second form, the {@link Closure} is responsible for determining and using the correct key for the child.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>property</code></td>
 *  <td>Used to identify or modify the actual property.  Optional.  The default is to set the property using the node's name.</td>
 *  <td>A property or field name.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>A {@link Closure} of the form
 *   <ul>
 *     <li><code>{o, v -> ...}</code></li>
 *   </li>
 *   where
 *   <ul>
 *    <li><code>o</code> is the object</li>
 *    <li><code>v</code> is the value</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td>min (property)</td>
 *  <td>Used to specify the minimum property size.  Optional.  The default is no minimum.  The size of a property is property dependent:
 *    <ul><li>for {@link String}s, <code>length()</code> is used
 *    </li><li>for {@link Collection}s and {@link Map}s, <code>size()</code> is used
 *    </li><li>for {@link Collection}s and {@link Map}s, <code>size()</code> is used
 *    </li><li>for all other {@link Comparable}s, the value itself is used
 *    </li><ul>It is an error to use any other non-{@link Comparable} type.
 * </td>
 *  <td>A {@link Comparable} type, such as {@link Integer} or {@link Double}.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 * </tr>
 * <tr>
 *  <td>max (property)</td>
 *  <td>Used to specify the maximum property size.  Optional.  The default is no maximum.  See min (property) above.</td>
 *  <td>A {@link Comparable} type, such as {@link Integer} or {@link Double}.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 * </tr>
 * <tr>
 *  <td><code>req</code></td>
 *  <td>Used to specify if a property must be specified.  Optional.</td>
 *  <td>n/a</td>
 *  <td><code>true</code> or <code>false</code></td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 * </tr>
 * <tr>
 *  <td><code>def (property)</code></td>
 *  <td>Used to specify a default value.  Optional.</td>
 *  <td>Any literal value may be passed to the property.</td>
 *  <td>Any object may be passed to the property.</td>
 *  <td>Any {@link Class} is passed like an object.</td>
 *  <td>A {@link Closure} returning a result of the form:
 *   <ul>
 *    <li><code>{-> ...}</code></li>
 *   </ul>
 *   The {@link Closure} is called each time a default value is needed.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>def (collection)</code></td>
 *  <td>Used to specify a default collection.  Optional.</td>
 *  <td>Any literal may be specified for non-map collections.
 *  <td>If a {@link Collection} is specified, its values are added individually to the collection.  Any other object is simply added to the collection.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 * </tr>
 * </table>
 * <a name="extendingSchema"/>
 * <h3>Inheriting and Extending Schemas</h3>
 * By using the <code>schema</code> property, your schemas can inherit and extend from other schemas.  This can be a
 * powerful technique and allows you to reuse schemas and even create recursive models.  For example:
 * <pre>
 *  MetaBuilder mb = new MetaBuilder()
 *  def parentDef = mb.define {
 *      parent(factory: TestParent) {
 *          properties {
 *              name(req: true)
 *          }
 *          collections {
 *              listOfChildren {
 *                  child(schema: 'parent') {
 *                      properties {
 *                          name(req: false)  // would have been true without this
 *                      }
 *                  }
 *              }
 *          }
 *      }
 *  }
 * </pre>
 * The above example also shows how a sub-schema can override the property settings of its super-schema.
 *
 * @see ObjectGraphBuilder
 *
 * @author didge
 * @version $Id: MetaBuilder.java 62 2009-07-28 07:32:10Z didge $
 */
public class MetaBuilder {
    private Map schemas;
    private SchemaNode defaultMetaSchema;
    private GroovyClassLoader classLoader;
    private Factory defaultBuildNodeFactory;
    private Factory defaultDefineNodeFactory;

    static {
        String packagePrefixes = System.getProperty("groovy.sanitized.stacktraces",
            "groovy.," +
            "org.codehaus.groovy.," +
            "java.," +
            "javax.," +
            "sun.," +
            "gjdk.groovy.,"
        );
        try {
        System.setProperty("groovy.sanitized.stacktraces",
            packagePrefixes +
            "groovytools.builder.,");
        }
        catch(Exception exc) {
            // let it go....
        }
    }

    protected class SchemaAdder extends Closure {
        public SchemaAdder() {
            super(null);
            maximumNumberOfParameters = 1;
        }

        public Object call(Object e) {
            CreateNodeEvent cne = (CreateNodeEvent)e;
            if(cne.getIsRoot()) {
                schemas.put(cne.getName(), cne.getNode());
            }
            return cne.getNode();
        }
    }

    protected static class ListBuilder extends Closure {
        protected ArrayList objects;
        public ListBuilder() {
            super(null);
            objects = new ArrayList();
            maximumNumberOfParameters = 1;
        }

        public Object call(Object e) {
            CreateNodeEvent cne = (CreateNodeEvent)e;
            if(cne.getIsRoot()) {
                objects.add(cne.getNode());
            }
            return cne.getNode();
        }

        public List getList() {
            return objects;
        }
    }

    /**
     * Constructs a <code>MetaBuilder</code> with the default meta schema, node factory and class loader.
     *
     * @see #createDefaultMetaSchema()
     */
    public MetaBuilder() {
        this(null, null);
        setClassLoader(getClass().getClassLoader());
        this.defaultMetaSchema = createDefaultMetaSchema();
    }

    /**
     * Constructs a <code>MetaBuilder</code> with the default meta schema, node factory and specified class loader.
     *
     * @param classLoader
     * @see #createDefaultMetaSchema()
     */
    public MetaBuilder(ClassLoader classLoader) {
        this(null, classLoader);
        this.defaultMetaSchema = createDefaultMetaSchema();
    }

    /**
     * Constructs a MetaBuilder with the given default meta schema
     *
     * @param defaultMetaSchema the default schema
     */
    public MetaBuilder(SchemaNode defaultMetaSchema, ClassLoader classLoader) {
        schemas = new HashMap();
        setClassLoader(classLoader);
        this.defaultMetaSchema = defaultMetaSchema;
        this.defaultBuildNodeFactory = createDefaultBuildNodeFactory();
        this.defaultDefineNodeFactory = createDefaultDefineNodeFactory();
    }

    public Factory getDefaultBuildNodeFactory() {
        return defaultBuildNodeFactory;
    }

    public void setDefaultBuildNodeFactory(Factory defaultBuildNodeFactory) {
        this.defaultBuildNodeFactory = defaultBuildNodeFactory;
    }

    public Factory getDefaultDefineNodeFactory() {
        return defaultDefineNodeFactory;
    }

    public void setDefaultDefineNodeFactory(Factory defaultDefineNodeFactory) {
        this.defaultDefineNodeFactory = defaultDefineNodeFactory;
    }

    /**
     * Subclasses may override this to specify an alternative factory for defining schema.
     *
     * @return see above
     */
    private Factory createDefaultDefineNodeFactory() {
        return new DefaultDefineSchemaNodeFactory();
    }

    /**
     * Subclasses may override this to specify an alternative factory for building object
     *
     * @return see above
     */
    private Factory createDefaultBuildNodeFactory() {
        return new DefaultBuildSchemaNodeFactory();
    }

    /**
     * The default implementantion returns the MetaBuilder meta schema:
     * Subclasses may override this method to implement their own default meta schemas as needed.
     *
     * @return see above
     */
    protected SchemaNode createDefaultMetaSchema() {

        Factory schemaNodeFactory = new DefaultDefineSchemaNodeFactory();
        Factory collectionNodeFactory = new DefaultCollectionSchemaNodeFactory();
        Closure nullOrBoolean = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof Boolean;
            }
        };

        Closure nullOrClosure = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof Closure;
            }
        };

        Closure nullOrClosureOrPattern = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof Closure || v instanceof Pattern;
            }
        };

        Closure nullOrInt = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof Integer;
            }
        };

        Closure nullOrStringOrClosure = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof String || v instanceof Closure;
            }
        };

        Closure nullOrStringOrClassOrFactoryOrClosure = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof String ||v instanceof Class ||  v instanceof Factory || v instanceof Closure;
            }
        };

        Closure nullOrStringOrSchemaNode = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof String || v instanceof SchemaNode;
            }
        };

        SchemaNode metaSchema = new SchemaNode(null, "%");
        metaSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode propertiesMetaSchema = new SchemaNode(metaSchema, "properties");
        propertiesMetaSchema.attributes().put("factory", schemaNodeFactory);
        SchemaNode checkNode = new SchemaNode(propertiesMetaSchema, "check");
        SchemaNode schemaNode = new SchemaNode(propertiesMetaSchema, "schema");
        schemaNode.attributes().put("check", nullOrStringOrSchemaNode);
        SchemaNode factoryNode = new SchemaNode(propertiesMetaSchema, "factory");
        factoryNode.attributes().put("check", nullOrStringOrClassOrFactoryOrClosure);

        SchemaNode colMetaSchema = new SchemaNode(metaSchema, "collections");
        SchemaNode colsSchema = new SchemaNode(colMetaSchema, "collections");
        colsSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode colSchema = new SchemaNode(colsSchema, "%");  // allows the collection to have any name, e.g. foos
        colSchema.attributes().put("factory", collectionNodeFactory);
        SchemaNode colSchemaProperties = new SchemaNode(colSchema, "properties");
        SchemaNode colSchemaPropertiesCollection = new SchemaNode(colSchemaProperties, "collection");
        colSchemaPropertiesCollection.attributes().put("check", nullOrStringOrClosure);
        SchemaNode colSchemaPropertiesMin = new SchemaNode(colSchemaProperties, "min");
        colSchemaPropertiesMin.attributes().put("check", nullOrInt);
        SchemaNode colSchemaPropertiesMax = new SchemaNode(colSchemaProperties, "max");
        colSchemaPropertiesMax.attributes().put("check", nullOrInt);
        SchemaNode colSchemaPropertiesSize = new SchemaNode(colSchemaProperties, "size");
        colSchemaPropertiesSize.attributes().put("check", nullOrStringOrClosure);
        SchemaNode colSchemaPropertiesAdd = new SchemaNode(colSchemaProperties, "add");
        colSchemaPropertiesAdd.attributes().put("check", nullOrStringOrClosure);
        SchemaNode colSchemaPropertiesKey = new SchemaNode(colSchemaProperties, "key");
        colSchemaPropertiesKey.attributes().put("check", nullOrStringOrClosure);
        SchemaNode colSchemaPropertiesDef = new SchemaNode(colSchemaProperties, "def");
        // don't need a check for collection default since it can take any object as a default value

        SchemaNode colElementSchema = new SchemaNode(colSchema, "%");  // allows the collection's element to have any name, e.g. foo
        colElementSchema.attributes().put("schema", metaSchema);

        SchemaNode propertiesSchema = new SchemaNode(colMetaSchema, "properties");
        propertiesSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode propertiesElementSchema = new SchemaNode(propertiesSchema, "%");
        propertiesElementSchema.attributes().put("schema", metaSchema);

        SchemaNode propertiesElementSchemaProperties = new SchemaNode(propertiesElementSchema, "properties");
        SchemaNode propertyNode = new SchemaNode(propertiesElementSchemaProperties, "property");
        propertyNode.attributes().put("check", nullOrStringOrClosure);
        SchemaNode reqNode = new SchemaNode(propertiesElementSchemaProperties, "req");
        reqNode.attributes().put("check", nullOrBoolean);
        SchemaNode defNode = new SchemaNode(propertiesElementSchemaProperties, "def");
        // no check needed for defa
        SchemaNode minNode = new SchemaNode(propertiesElementSchemaProperties, "min");
        SchemaNode maxNode = new SchemaNode(propertiesElementSchemaProperties, "max");

        return metaSchema;
    }

    /**
     * Defines, registers and returns a new schema using the default meta schema.  Defined schemas are available for
     * use in building new objects on the MetaBuilder directly.
     *
     * @param c
     *
     * @return see above
     */
    public Object define(Closure c) {
        c.setDelegate(createMetaObjectGraphBuilder(defaultMetaSchema, defaultDefineNodeFactory, new SchemaAdder()));
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object schema = c.call();
        return schema;

    }

    public Object define(Class viewClass) {
        if (Script.class.isAssignableFrom(viewClass)) {
            Script script = InvokerHelper.createScript(viewClass, new Binding());
            return define(script);
        } else {
            throw new RuntimeException("Only scripts can be executed via build(Class)");
        }
    }

    public Object define(Script script) {
        synchronized (script) {
            MetaClass scriptMetaClass = script.getMetaClass();
            try {
                MetaObjectGraphBuilder metaObjectGraphBuilder = createMetaObjectGraphBuilder(defaultMetaSchema, defaultDefineNodeFactory, new SchemaAdder());
                script.setMetaClass(new FactoryInterceptorMetaClass(scriptMetaClass, metaObjectGraphBuilder));
                script.setBinding(metaObjectGraphBuilder);
                return script.run();
            } finally {
                script.setMetaClass(scriptMetaClass);
            }
        }
    }

    public Object define(URL url) throws IOException {
        return define(classLoader.parseClass(url.openStream()));
    }

    public Object build(Closure objectVisitor, Closure c) {
        MetaObjectGraphBuilder builder = createMetaObjectGraphBuilder(null, defaultBuildNodeFactory, objectVisitor);
        c.setDelegate(builder);
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object schema = c.call();
        return schema;
    }

    public Object build(Closure c) {
        return build(null, c);
    }

    public List buildList(Closure c) {
        ListBuilder listBuilder = new ListBuilder();
        MetaObjectGraphBuilder graphBuilder = createMetaObjectGraphBuilder(null, defaultBuildNodeFactory, listBuilder);
        c.setDelegate(graphBuilder);
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        c.call();
        return listBuilder.getList();
    }

    public Object build(Class viewClass) {
        return build(null, viewClass);
    }

    /**
     * Creates a Script from viewClass and returns the last object built by running the resulting script.
     *
     * @param objectVisitor a Closure that must accept two arguments: the node name and the node
     * @param viewClass
     * @return see above
     */
    public Object build(Closure objectVisitor, Class viewClass) {
        if (Script.class.isAssignableFrom(viewClass)) {
            Script script = InvokerHelper.createScript(viewClass, new Binding());
            return build(objectVisitor, script);
        } else {
            throw new RuntimeException("Only scripts can be executed via build(Class)");
        }
    }

    public List buildList(Class viewClass) {
        if (Script.class.isAssignableFrom(viewClass)) {
            Script script = InvokerHelper.createScript(viewClass, new Binding());
            return buildList(script);
        } else {
            throw new RuntimeException("Only scripts can be executed via build(Class)");
        }
    }

    public Object build(URL url) throws IOException {
        return build(classLoader.parseClass(url.openStream()));
    }

    public Object build(Closure objectVisitor, URL url) throws IOException {
        return build(objectVisitor, classLoader.parseClass(url.openStream()));
    }

    public List buildList(URL url) throws IOException {
        return buildList(classLoader.parseClass(url.openStream()));
    }

    public Object build(Script script) {
        return build(null, script);
    }

    public Object build(Closure objectVisitor, Script script) {
        synchronized (script) {
            MetaClass scriptMetaClass = script.getMetaClass();
            try {
                MetaObjectGraphBuilder metaObjectGraphBuilder = createMetaObjectGraphBuilder(null, defaultBuildNodeFactory, objectVisitor);
                script.setMetaClass(new FactoryInterceptorMetaClass(scriptMetaClass, metaObjectGraphBuilder));
                script.setBinding(metaObjectGraphBuilder);
                return script.run();
            } finally {
                script.setMetaClass(scriptMetaClass);
            }
        }
    }

    public List buildList(Script script) {
        synchronized (script) {
            MetaClass scriptMetaClass = script.getMetaClass();
            try {
                ListBuilder listBuilder = new ListBuilder();
                MetaObjectGraphBuilder metaObjectGraphBuilder = createMetaObjectGraphBuilder(null, defaultBuildNodeFactory, listBuilder);
                script.setMetaClass(new FactoryInterceptorMetaClass(scriptMetaClass, metaObjectGraphBuilder));
                script.setBinding(metaObjectGraphBuilder);
                script.run();
                return listBuilder.getList();
            } finally {
                script.setMetaClass(scriptMetaClass);
            }
        }
    }

    /**
     * Returns a previously defined schema with the given name.
     *
     * @param name see above
     *
     * @return see above
     */
    public SchemaNode getSchema(String name) {
        return (SchemaNode)schemas.get(name);
    }

    /**
     * Adds a previously defined schema with the given name.
     *
     * @param name see above
     * @param schema see above
     *
     */
    public void addSchema(String name, Object schema) {
        schemas.put(name, schema);
    }

    /**
     * Returns the class loader in use by the MetaBuilder.
     *
     * @return see above
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the default schema.
     *
     * @return see above
     */
    public SchemaNode getDefaultMetaSchema() {
        return defaultMetaSchema;
    }

    /**
     * Returns a new {@link MetaObjectGraphBuilder} with the given default schema and node factory
     *
     * @param defaultSchema
     * @param defaultNodeFactory
     *
     * @return see above
     */
    protected MetaObjectGraphBuilder createMetaObjectGraphBuilder(SchemaNode defaultSchema, Factory defaultNodeFactory, Closure objectVisitor) {
        return new MetaObjectGraphBuilder(this, defaultSchema, defaultNodeFactory, objectVisitor);
    }

    /**
     * Sets the {@link ClassLoader} to use by the <code>MetaBuilder</code>.  It is sometimes necessary, especially in Groovy scripts,
     * to provide {@link ClassLoader} explicity to resolve classes by name.
     *
     * @param classLoader the {@link ClassLoader} to use
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader instanceof GroovyClassLoader ? (GroovyClassLoader)classLoader : new GroovyClassLoader(classLoader);
    }

    public static RuntimeException createNodeException(String name, String error) {
        StringBuilder message = new StringBuilder("Node '").append(name).append("': ").append(error);
        return (RuntimeException)StackTraceUtils.sanitize(new NodeException(message.toString()));
    }

    public static RuntimeException createNodeException(String name, Throwable error) {
        StringBuilder message = new StringBuilder("Node '").append(name).append("': ").append(error);
        return (RuntimeException)StackTraceUtils.sanitize(new NodeException(message.toString(), error));
    }

    public static RuntimeException createPropertyException(String name, String error) {
        StringBuilder message = new StringBuilder("Property '").append(name).append("': ").append(error);
        return (RuntimeException)StackTraceUtils.sanitize(new PropertyException(message.toString()));
    }

    public static RuntimeException createPropertyException(String name, Throwable error) {
        StringBuilder message = new StringBuilder("Property '").append(name).append("': ").append(error);
        return (RuntimeException)StackTraceUtils.sanitize(new PropertyException(message.toString(), error));
    }

    public static RuntimeException createCollectionException(String name, String error) {
        StringBuilder message = new StringBuilder("Collection '").append(name).append("': ").append(error);
        return (RuntimeException)StackTraceUtils.sanitize(new CollectionException(message.toString()));
    }

    public static RuntimeException createCollectionException(String name, Throwable error) {
        StringBuilder message = new StringBuilder("Collection '").append(name).append("': ").append(error);
        return (RuntimeException)StackTraceUtils.sanitize(new CollectionException(message.toString(), error));
    }

    public static RuntimeException createFactoryException(String name, String error) {
        StringBuilder message = new StringBuilder("'").append(name).append("' factory: ").append(error);
        return (RuntimeException)StackTraceUtils.sanitize(new FactoryException(message.toString()));
    }

    public static RuntimeException createFactoryException(String name, Throwable error) {
        StringBuilder message = new StringBuilder("'").append(name).append("' factory: ").append(error);
        return (RuntimeException)StackTraceUtils.sanitize(new FactoryException(message.toString(), error));
    }

    public static RuntimeException createSchemaNotFoundException(String name) {
        StringBuilder message = new StringBuilder(name);
        return (RuntimeException)StackTraceUtils.sanitize(new SchemaNotFoundException(message.toString()));
    }

    public static RuntimeException createClassNameNotFoundException(String name) {
        StringBuilder message = new StringBuilder(name);
        return (RuntimeException)StackTraceUtils.sanitize(new ClassNotFoundException(message.toString()));
    }

    /**
     * Default {@link SchemaNode} factory used when {@link MetaBuilder#define} is called.  Differs from
     * {@link DefaultBuildSchemaNodeFactory} in that it does include {@link CollectionSchemaNode}s in the result.
     */
    protected static class DefaultDefineSchemaNodeFactory extends AbstractFactory {
        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            return new SchemaNode((SchemaNode)builder.getCurrent(), name);
        }
    }

    /**
     * Default {@link SchemaNode} factory used when {@link MetaBuilder#build} is called.  Differs from
     * {@link DefaultDefineSchemaNodeFactory} in that it doesn't include {@link CollectionSchemaNode}s in the result.
     */
    protected static class DefaultBuildSchemaNodeFactory extends AbstractFactory {
        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            return new SchemaNode(null, name);
        }
    }

    /**
     * Default {@link CollectionSchemaNode} factory used when {@link MetaBuilder#define} is called.
     */
    protected static class DefaultCollectionSchemaNodeFactory extends AbstractFactory {
        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            return new CollectionSchemaNode((SchemaNode)builder.getCurrent(), name);
        }
    }

    /**
     * Supports builder scripts by dispatching methods against {@link MetaObjectGraphBuilder}.
     * <p>
     * Borrowed from {@link FactoryBuilderSupport}.  Is there a reason it wasn't made a public class to begin with?
     */
    public static class FactoryInterceptorMetaClass extends DelegatingMetaClass {

        FactoryBuilderSupport factory;

        public FactoryInterceptorMetaClass(MetaClass delegate, FactoryBuilderSupport factory) {
            super(delegate);
            this.factory = factory;
        }

        /* (non-Javadoc)
            * @see groovy.lang.MetaClass#setProperty(java.lang.Object, java.lang.String, java.lang.Object)
            */
        public void setProperty(Object object, String property, Object newValue) {
            super.setProperty(object, property, newValue);
        }

        public void setProperty(String property, Object newValue) {
            super.setProperty(property, newValue);
        }

        public void setProperty(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
            super.setProperty(sender, receiver, messageName, messageValue, useSuper, fromInsideClass);
        }

        /* (non-Javadoc)
        * @see groovy.lang.MetaClass#setAttribute(java.lang.Object, java.lang.String, java.lang.Object)
        */
        public void setAttribute(Object object, String attribute, Object newValue) {
            super.setAttribute(object, attribute, newValue);
        }

        public void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
            super.setAttribute(sender, receiver, messageName, messageValue, useSuper, fromInsideClass);
        }

        public Object invokeMethod(Object object, String methodName, Object arguments) {
            try {
                return delegate.invokeMethod(object, methodName, arguments);
            } catch (MissingMethodException mme) {
                // attempt factory resolution
                try {
                    if (factory.getMetaClass().respondsTo(factory, methodName).isEmpty()) {
                        // dispatch to factories if it is not a literal method
                        return factory.invokeMethod(methodName, arguments);
                    } else {
                        return InvokerHelper.invokeMethod(factory, methodName, arguments);
                    }
                } catch (MissingMethodException mme2) {
                    // throw original
                    // should we chain in mme2 somehow?
                    throw mme;
                }
            }
        }

        public Object invokeMethod(Object object, String methodName, Object[] arguments) {
            try {
                return delegate.invokeMethod(object, methodName, arguments);
            } catch (MissingMethodException mme) {
                // attempt factory resolution
                try {
                    if (factory.getMetaClass().respondsTo(factory, methodName).isEmpty()) {
                        // dispatch to factories if it is not a literal method
                        return factory.invokeMethod(methodName, arguments);
                    } else {
                        return InvokerHelper.invokeMethod(factory, methodName, arguments);
                    }
                } catch (MissingMethodException mme2) {
                    // throw original
                    // should we chain in mme2 somehow?
                    throw mme;
                }
            }
        }
    }
}
