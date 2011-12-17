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

import java.util.*;

/**
 * This class is the workhorse behind {@link MetaBuilder}.  It is responsible for building object hierarchies according
 * to the schemas given to it.  It is not intended to be used independently of {@link MetaBuilder}.
 *
 * @author didge
 * @version $Id: MetaObjectGraphBuilder.java 63 2009-07-31 07:10:10Z didge $
 */
public class MetaObjectGraphBuilder extends ObjectGraphBuilder {

    /**
     * The {@link MetaBuilder} that owns this {@link MetaObjectGraphBuilder}.
     */
    private MetaBuilder metaBuilder;

    /**
     * The default schema.
     */
    private SchemaNode defaultSchema;

    /**
     * Keeps track of the current schema while descending into an object graph.
     */
    private LinkedList schemaStack;

    /**
     * Keeps track of which properties were set/unset
     */
    private LinkedList propertiesStack;

    /**
     * Default factory to use when no other can be resolved.
     */
    private Factory defaultFactory;

    /**
     * If set, called after each node is created.
     */
    private Closure objectVisitor;

    /**
     * Constructs a {@link MetaObjectGraphBuilder}.
     *
     * @param metaBuilder the {@link MetaBuilder} providing the build context
     * @param defaultSchema
     * @param defaultFactory
     */
    public MetaObjectGraphBuilder(MetaBuilder metaBuilder, SchemaNode defaultSchema, Factory defaultFactory) {
        this(metaBuilder, defaultSchema, defaultFactory, null);
    }

    /**
     * Constructs a {@link MetaObjectGraphBuilder}.
     *
     * @param metaBuilder the {@link MetaBuilder} providing the build context
     * @param defaultSchema
     * @param defaultFactory
     * @param objectVisitor
     */
    public MetaObjectGraphBuilder(MetaBuilder metaBuilder, SchemaNode defaultSchema, Factory defaultFactory, Closure objectVisitor) {
        super();
        this.metaBuilder = metaBuilder;
        schemaStack = new LinkedList();
        propertiesStack = new LinkedList();
        this.defaultSchema = defaultSchema;
        this.defaultFactory = defaultFactory;
        this.objectVisitor = objectVisitor;

        setClassNameResolver(createClassNameResolver());
        setClassLoader(metaBuilder.getClassLoader());
        setIdentifierResolver(createIdentifierResolver());
    }

    public MetaBuilder getMetaBuilder() {
        return metaBuilder;
    }

    public void pushProperties(SchemaNode propertiesSchema) {
        List propertiesList = (List)propertiesSchema.value();
        Map propertiesMap = new HashMap();
        for(int i = 0; i < propertiesList.size(); i++) {
            SchemaNode propertySchema = (SchemaNode)propertiesList.get(i);
            propertiesMap.put(propertySchema.name(), propertySchema);
        }
        propertiesStack.addFirst(propertiesMap);
    }

    public void pushProperties(Map properties) {
        propertiesStack.addFirst(properties);
    }

    public Map popProperties() {
        return (Map)propertiesStack.removeFirst();
    }

    public Map getCurrentProperties() {
        return (Map)propertiesStack.peek();
    }

    public void pushSchema(SchemaNode schema) {
        schemaStack.addFirst(schema);
    }

    public SchemaNode popSchema() {
        return (SchemaNode)schemaStack.removeFirst();
    }

    public SchemaNode getCurrentSchema() {
        return (SchemaNode)schemaStack.peek();
    }

    public Closure getObjectVisitor() {
        return objectVisitor;
    }

    public void setObjectVisitor(Closure objectVisitor) {
        this.objectVisitor = objectVisitor;
    }

    /**
     * Returns the schema referenced by name.
     *
     * @param schemaRef the schema reference
     * @return see above
     */
    protected SchemaNode resolveSchemaRef(Object schemaRef) {
        return schemaRef instanceof String
                ? metaBuilder.getSchema((String)schemaRef)
                : (SchemaNode)schemaRef;
    }

    /**
     * Finds and returns a child schema with the given name, in the specified container or null if not found.  This
     * method will also search any super-schemas specified with the 'schema' attribute.
     *
     * @param parentSchema  the parent schema
     * @param containerName the name of a container of child schemas, e.g. collections, properties
     * @param name          the name of the child schema
     * @return see above
     */
    protected SchemaNode findSchema(SchemaNode parentSchema, String containerName, String name) {
        SchemaNode childSchema = null;
        SchemaNode containerSchema = (SchemaNode)parentSchema.firstChild(containerName);
        if(containerSchema != null) {
            childSchema = (SchemaNode)containerSchema.firstChild(name);
        }
        if(childSchema == null) {
            // Search super schemas for name
            Object extendSchemaRef = parentSchema.attribute("schema");
            SchemaNode extendSchema = resolveSchemaRef(extendSchemaRef);
            if(extendSchema != null) childSchema = findSchema(extendSchema, containerName, name);
        }

        return childSchema;
    }

    protected SchemaNode findCollectionSchema(SchemaNode parentSchema, String containerName, String name) {
        SchemaNode result = null;
        SchemaNode collectionsSchema = (SchemaNode)parentSchema.firstChild(containerName);
        List collectionList = collectionsSchema != null ? collectionsSchema.children() : null;

        /*
            Priority :
            1. parentSchema's collection
            2. parentSchema's super schema's collection
         */

        if(collectionList != null) {
            // have a collection, so check each member
            for(int i = 0; i < collectionList.size(); i++) {
                Object o = collectionList.get(i);
                if(o instanceof SchemaNode == false) continue;
                SchemaNode collectionSchema = (SchemaNode)o;
                result = (SchemaNode)collectionSchema.firstChild(name);

                if(result == null) {
                    // Search the collection's super schemas for name
                    Object extendSchemaRef = collectionSchema.attribute("schema");
                    SchemaNode extendSchema = resolveSchemaRef(extendSchemaRef);
                    if(extendSchema != null) {
                        result = findCollectionSchema(extendSchema, containerName, name);
                    }
                }
                if(result != null) return result;
            }
        }
        // still no result, check the parentSchema's super schema
        Object extendSchemaRef = parentSchema.attribute("schema");
        SchemaNode extendSchema = resolveSchemaRef(extendSchemaRef);
        if(extendSchema != null) {
            result = findCollectionSchema(extendSchema, containerName, name);
        }
        return result;
    }

    protected Object findSchemaAttribute(SchemaNode parentSchema, String name) {
        Object attribute = parentSchema.attribute(name);
        if(attribute == null) {
            Object extendedSchemaRef = parentSchema.attribute("schema");
            SchemaNode extendedSchema = resolveSchemaRef(extendedSchemaRef);
            if(extendedSchema != null) attribute = findSchemaAttribute(extendedSchema, name);
        }
        return attribute;
    }

    /**
     * Overrides in order to construct nodes based on the current schema definition.
     *
     * @param name       the name of the node
     * @param attributes optional attributes of the current node
     * @param value      optional value of the current node
     * @return a node
     */
    protected Object createNode(Object name, Map attributes, Object value) {
        String childSchemaName = (String)name;
        Object current = getCurrent();
        // MetaObjectGraphBuilder basically works by matching name against a child node of the current schema.
        SchemaNode currentSchema = getCurrentSchema();
        SchemaNode childSchema = null;
        if(currentSchema == null) {
            // If there is no current schema, its because the builder is building the root node.
            currentSchema = metaBuilder.getSchema(childSchemaName);
            // Can't find a schema?  Check for wild card schema:
            if(currentSchema == null) {
                currentSchema = metaBuilder.getSchema("%");
            }
        }
        if(currentSchema == null && defaultSchema != null) {
            // If we can't find a schema using the root node's name, try using the default schema
            childSchema = defaultSchema;
        }
        else {
            if(current == null) {
                if(currentSchema == null) {
                    // This occurs when the root doesn't match and there is no default or anything else to work with
                    throw MetaBuilder.createSchemaNotFoundException(childSchemaName);
                }
                else {
                    // No current node exists, see if the  schema supports any name
                    String rootName = (String)currentSchema.name();
                    if(!rootName.equals(childSchemaName) && !rootName.equals("%")) {
                        throw MetaBuilder.createSchemaNotFoundException(childSchemaName);
                    }
                    childSchema = currentSchema;
                }
            }
            else {
                // search for a property
                childSchema = childSchema != null ? childSchema : findSchema(currentSchema, "properties", childSchemaName);

                // search for a named collection
                childSchema = childSchema != null ? childSchema : findSchema(currentSchema, "collections", childSchemaName);

                // search for a collection member (current schema is a collection node)
                childSchema = childSchema != null ? childSchema : (SchemaNode)currentSchema.firstChild(childSchemaName);

                // search all collections for a named collection member
                childSchema = childSchema != null ? childSchema : findCollectionSchema(currentSchema, "collections", childSchemaName);

                // search for an unnamed property
                childSchema = childSchema != null ? childSchema : findSchema(currentSchema, "properties", "%");

                // search for an unnamed collection
                childSchema = childSchema != null ? childSchema : findSchema(currentSchema, "collections", "%");

                // search for an unnamed schema node
                childSchema = childSchema != null ? childSchema : (SchemaNode)currentSchema.firstChild("%");
            }
        }
        if(childSchema == null) {
            throw MetaBuilder.createSchemaNotFoundException(childSchemaName);
        }

        pushSchema(childSchema);
        // Store a mutable copy of the merged properties for later checking of defaults and missing req properties
        // As properties are set, they are removed from the copy of merged properties.
        // Only the merged properties that are left are checked for req and def
        SchemaNode mergedProperties = getMergedProperties(childSchema);
        pushProperties(mergedProperties);

        Object node = null;
        try {
            node = super.createNode(childSchemaName, attributes, value);
        }
        catch(RuntimeException e) {
            // If FactoryBuilderSupport throws an exception caused by
            // a MetaBuilder, simply unwrap and rethrow the cause.
            // This will be a lot easier for the user to understand
            // than the FactoryBuilderSupport exception.
            Throwable t = e.getCause();
            if(t instanceof MetaBuilderException) {
                throw (MetaBuilderException)t;
            }
            else throw e;
        }

        if(objectVisitor != null) {
            CreateNodeEvent e = new CreateNodeEvent(childSchemaName, node, current);
            node = objectVisitor.call(e);
        }

        return node;
    }

    /**
     * Sets thet node's property value by name referencing the current schema.  Invoked when using '=' to set a property
     * value.
     *
     * @param name the property name
     * @param value the property value
     *
     * @see #setVariable(Object, SchemaNode, String, Object)
     */
    public void setVariable(String name, Object value) {
        setVariable(getCurrent(), getCurrentSchema(), name, value);
    }



    /**
     * Sets the given node's property value by name referencing the given schema.
     *
     * @param node the property owner
     * @param schema the property owner's schema
     * @param name the property name
     * @param value the property value
     */
    public void setVariable(Object node, SchemaNode schema, String name, Object value) {
        SchemaNode mergedProperties = getMergedProperties(schema);
        SchemaNode propertySchema = (SchemaNode)mergedProperties.firstChild(name);

        // remove the entry from the currentProperties so we won't try to set a default or check req later.
        Map currentProperties = getCurrentProperties();
        currentProperties.remove(name);

        if(propertySchema == null) {
            // Check for the possibility of a wild card indicated by a schema with name = %
            propertySchema = findSchema(schema, "properties", "%");
            if(propertySchema == null) {
                throw MetaBuilder.createPropertyException(schema.fqn() + '.' + name, "property unkown");
            }
        }

        if(value != null) {
            Comparable min = (Comparable)propertySchema.attribute("min");
            Comparable minMaxValComp = null;
            if(min != null && min.compareTo(minMaxValComp = getMinMaxValComp(schema, name, value)) > 0) {
                throw MetaBuilder.createPropertyException(schema.fqn(name), "min check failed");
            }
            Comparable max = (Comparable)propertySchema.attribute("max");
            if(max != null && max.compareTo(minMaxValComp) < 0) {
                throw MetaBuilder.createPropertyException(schema.fqn(name), "max check failed");
            }
        }
        checkPropertyValue(propertySchema, value);

        setProperty(node, value, propertySchema);
    }

    /**
     * Overrides the default implementation to:
     * <ul><li>sync the schema with the current node
     * </li><li>handle unset properties
     * </li><li>check collections
     * </li><li>execute any check on the node iteself
     * </li></ul>
     *
     * @param parent the parent node
     * @param node the node that is being completed
     */
    protected void nodeCompleted(Object parent, Object node) {
        SchemaNode currentSchema = popSchema();
        handleUnsetProperties(currentSchema, node);
        checkCollections(currentSchema, node);
        checkNode(currentSchema, node);

        super.nodeCompleted(parent, node);
    }

    protected void checkCollections(SchemaNode currentSchema, Object node) {
        SchemaNode collectionsSchema = getMergedCollections(currentSchema);
        List collectionsList = collectionsSchema.children();
        if(collectionsList == null) return;

        for(int i = 0; i < collectionsList.size(); i++) {
            CollectionSchemaNode collectionSchema = (CollectionSchemaNode)collectionsList.get(i);
            collectionSchema.checkDef(this, node);
            collectionSchema.checkSize(node);
        }
    }

    protected void handleUnsetProperties(SchemaNode currentSchema, Object node) {
        // go through the unset properties and set defaults or check if req
        Map unsetProperties = new HashMap(getCurrentProperties()); // use copy to avoid ConcurrentModificationException
        for(Iterator properties = unsetProperties.entrySet().iterator(); properties.hasNext();) {
            Map.Entry property = (Map.Entry)properties.next();
            SchemaNode propertySchema = (SchemaNode)property.getValue();
            Map attributes = propertySchema.attributes();
            if(attributes.containsKey("def")) {
                Object value = attributes.get("def");
                if(value instanceof Closure) {
                    value = ((Closure)value).call();
                }
                setVariable(node, currentSchema, (String)propertySchema.name(), value);
            }
            else {
                Boolean req = (Boolean)attributes.get("req");
                if(req != null && req) {
                    throw MetaBuilder.createPropertyException(propertySchema.fqn(), "property required");
                }
            }
        }
        popProperties();

        // remove the child schema from the parents list of unset properties
        // in case it was defined as a node and not as an attribute
        Map parentProperties = getCurrentProperties();
        if(parentProperties != null) {
            parentProperties.remove(currentSchema.name());
        }
    }

    /**
     * Override to modify the {@link groovy.util.ObjectGraphBuilder.IdentifierResolver} behavior.
     *
     * @return see above
     */
    protected IdentifierResolver createIdentifierResolver() {
        // id is a pretty common property name, so rename it
        return new ObjectGraphBuilder.IdentifierResolver() {
            public String getIdentifierFor(String nodeName) {
                return "metaId";
            }
        };
    }

    /**
     * Override to modify the {@link ClassNameResolver} behavior.
     *
     * @return see above
     */
    protected ClassNameResolver createClassNameResolver() {
        return new MetaObjectGraphBuilder.FactoryClassNameResolver();
    }

    /**
     * Overrides the default implementation in {@link ClassNameResolver} in order to support
     * resolution of the class name using the <code>factory</code> schema attribute.
     */
    public class FactoryClassNameResolver implements ObjectGraphBuilder.ClassNameResolver {
        public String resolveClassname(String className) {
            SchemaNode schema = getCurrentSchema();
            Object factory = findSchemaAttribute(schema, "factory");
            if(factory instanceof String)
                return (String)factory;
            else if(factory instanceof Class) {
                return ((Class)factory).getName();  // kind of silly...
            }
            throw MetaBuilder.createClassNameNotFoundException(className);
        }
    }

    /**
     * Overrides the default implementation to support lookup of a factory defined in a schema attribute.  The
     * {@link Factory} is resolved as follows:
     * <ol>
     * <li>If the schema itself is an instance of {@link Factory}, then it is returned.</li>
     * <li>If the schema defines an attribute value called <code>factory</code> which is an instance of {@link Factory},
     * then the attribute value is returned.</li>
     * <li>If the schema defines an attribute value called <code>factory</code> which is an instance of {@link Closure},
     * then the Closure will be returned, wrapped by {@link ClosureFactoryAdapter}</li>
     * <li>If the schema defines an attribute value called <code>factory</code> which is an instance of {@link String}
     * or {@link Class}, then the corresponding class will be instantiated and returned.</li>
     * </ol>
     *
     * @param name
     * @param attributes
     * @param value
     * @return see above
     */
    protected Factory resolveFactory(Object name, Map attributes, Object value) {
        // Need to have this implementation act first before super,
        // but FactoryBuilderSupport.resolveFactory() sets the CHILD_BUILDER context
        // So it must be done directly here.  Not using CHILD_BUILDER for Groovy 1.5 compatibility.
        getContext().put("_CHILD_BUILDER_"/* CHILD_BUILDER */, this);

        SchemaNode schema = getCurrentSchema();
        if(schema instanceof Factory) {
            return (Factory)schema;
        }
        Object factory = findSchemaAttribute(schema, "factory");
        if(factory != null && factory instanceof Factory) {
            return (Factory)factory;
        }
        if(factory instanceof Closure) {
            // Is it cool to wrap the closure and replace it?
            // It does save having to keep wrapping the closure everytime, but is there a downside?
            ClosureFactoryAdapter closureFactoryAdapter = new ClosureFactoryAdapter((Closure)factory);
            schema.attributes().put("factory", closureFactoryAdapter);
            return closureFactoryAdapter;
        }
        if(factory instanceof String || factory instanceof Class) {
            return super.resolveFactory(name, attributes, value);
        }
        return defaultFactory;
    }

    /**
     * Return a shallow set of merged properties by searching for all properties of schema and its parents, in a depth
     * first manner.  This facilitates fast lookup of property attributes and tracking which properties
     * have been set and which have not.
     *
     * @param schema the owner of the properties
     * @return see above
     */
    protected SchemaNode getMergedProperties(SchemaNode schema) {
        SchemaNode mergedProperties = (SchemaNode)schema.firstChild("mergedProperties");

        if(mergedProperties == null) {
            mergedProperties = new SchemaNode(schema, "mergedProperties");

            SchemaNode superSchema = resolveSchemaRef(schema.attribute("schema"));
            if(superSchema != null) {
                // add all of the super schema's properties first
                SchemaNode superMergedProperties = getMergedProperties(superSchema);
                for(Iterator children = superMergedProperties.children().iterator(); children.hasNext();) {
                    SchemaNode property = (SchemaNode)children.next();
                    new SchemaNode(mergedProperties, property.name(), property.attributes());
                }
            }
            SchemaNode properties = (SchemaNode)schema.firstChild("properties");
            if(properties != null) {
                for(Iterator children = properties.children().iterator(); children.hasNext();) {
                    SchemaNode property = (SchemaNode)children.next();
                    SchemaNode mergedProperty = (SchemaNode)mergedProperties.firstChild((String)property.name());
                    if(mergedProperty == null) {
                        // simple copy
                        new SchemaNode(mergedProperties, property.name(), property.attributes());
                    }
                    else {
                        // Copies and overwrites any previous attributes, if present
                        Map mergedPropertyAttributes = new HashMap(mergedProperty.attributes());
                        mergedPropertyAttributes.putAll(property.attributes());
                        mergedProperties.remove(mergedProperty);
                        new SchemaNode(mergedProperties, property.name(), mergedPropertyAttributes);
                    }
                }
            }
        }
        return mergedProperties;
    }

    /**
     * Return a shallow set of merged collections by searching for all collections of schema and its parents, in a depth
     * first manner.  This facilitates fast lookup of collection attributes and checking.
     *
     * @param schema the owner of the collections
     * @return see above
     */
    protected SchemaNode getMergedCollections(SchemaNode schema) {
        SchemaNode mergedCollections = (SchemaNode)schema.firstChild("mergedCollections");

        if(mergedCollections == null) {
            mergedCollections = new SchemaNode(schema, "mergedCollections");

            SchemaNode superSchema = resolveSchemaRef(schema.attribute("schema"));
            if(superSchema != null) {
                // add all of the super schema's collections first
                SchemaNode superMergedCollections = getMergedCollections(superSchema);
                for(Iterator children = superMergedCollections.children().iterator(); children.hasNext();) {
                    SchemaNode collection = (SchemaNode)children.next();
                    new CollectionSchemaNode(mergedCollections, collection.name(), collection.attributes());
                }
            }

            SchemaNode collections = (SchemaNode)schema.firstChild("collections");
            if(collections != null) {
                for(Iterator children = collections.children().iterator(); children.hasNext();) {
                    SchemaNode collection = (SchemaNode)children.next();
                    SchemaNode mergedCollection = (SchemaNode)mergedCollections.firstChild((String)collection.name());
                    if(mergedCollection == null) {
                        // simple copy
                        new CollectionSchemaNode(mergedCollections, collection.name(), collection.attributes());
                    }
                    else {
                        // Copies and overwrites any previous attributes, if present
                        Map mergedCollectionAttributes = new HashMap(mergedCollection.attributes());
                        mergedCollectionAttributes.putAll(collection.attributes());
                        mergedCollections.remove(mergedCollection);
                        new CollectionSchemaNode(mergedCollections, collection.name(), mergedCollectionAttributes);
                    }
                }
            }
        }
        return mergedCollections;
    }

    /**
     * Sets properties on the node based on the current schema.
     *
     * @param node
     * @param attributesMap
     *
     * @see #setProperty
     */
    protected void setNodeAttributes(Object node, Map attributesMap) {
        for(Iterator attributes = attributesMap.entrySet().iterator(); attributes.hasNext();) {
            Map.Entry attribute = (Map.Entry)attributes.next();
            setVariable(node, getCurrentSchema(), (String)attribute.getKey(), attribute.getValue());
        }
    }

    /**
     * Check <code>value</code> against a <code>propertySchema</code>'s <code>check</code> attribute, if it exists.
     *
     * @param schema see above
     * @param val the value
     */
    protected void checkPropertyValue(SchemaNode schema, Object val) {
        if(val == null || schema.attributes().containsKey("check") == false) return;
        Object check = schema.attribute("check");
        boolean b = true;
        try {
            b =  ScriptBytecodeAdapter.isCase(val, check);
        }
        catch(Throwable t) {
            throw MetaBuilder.createPropertyException(schema.fqn(), t);
        }
        if(!b) {
            throw MetaBuilder.createPropertyException((String)schema.fqn(), "value invalid");
        }
    }

    /**
     * Execute check against <code>node</code>, if one exists.
     *
     * @param schema see above
     * @param node the node
     */
    protected void checkNode(SchemaNode schema, Object node) {
        if(node == null || schema.attributes().containsKey("check") == false) return;
        Object check = schema.attribute("check");
        boolean b = true;
        try {
            b =  ScriptBytecodeAdapter.isCase(node, check);
        }
        catch(Throwable t) {
            throw MetaBuilder.createNodeException(schema.fqn(), t);
        }
        if(!b) {
            throw MetaBuilder.createNodeException(schema.fqn(), "check failed");
        }
    }

    /**
     * Returns a {@link Comparable} object that can be used with the <code>min</code> and <code>max</code> constraints.
     *
     * @param schema the property owner (used to report errors)
     * @param name the property name (used to report errors)
     * @param val the property value
     * @return see above
     */
    protected Comparable getMinMaxValComp(SchemaNode schema, String name, Object val) {
        if(val instanceof String) {
            return ((String)val).length();
        }
        else if(val instanceof Collection) {
            return ((Collection)val).size();
        }
        else if(val instanceof Map) {
            return ((Map)val).size();
        }
        else if(val instanceof Comparable) {
            return (Comparable)val;
        }
        else {
            throw MetaBuilder.createPropertyException(schema.fqn(name), "value is not comparable");
        }
    }

    /**
     * Sets a node's property value, allowing for property renaming.  The property name is taken from the property
     * schema's <code>property</code> attribute, if any, else the property node name is used by default.
     *
     * @param node           the current node
     * @param value          the value of the property
     * @param propertySchema       the property's property attribute or name
     */
    protected void setProperty(Object node, Object value, SchemaNode propertySchema) {
        String propertyName = null;
        Object propertyAttr = propertySchema.attribute("property");
        if(propertyAttr != null) {
            if(propertyAttr instanceof Closure) {
                Closure propertyClosure = (Closure)propertyAttr;
                propertyClosure.call(new Object[]{node, value});
                return;
            }
            else if(propertyAttr instanceof String) {
                propertyName = (String)propertyAttr;
            }
            else {
                throw MetaBuilder.createPropertyException(propertySchema.fqn(propertyName), "'property' attribute of schema does not specify a string or closure.");
            }
        }
        else {
            propertyName = (String)propertySchema.name();
        }
        // todo - didge: Rethink special case for SchemaNodes.  An alternative would be to mess around with its MetaClass
        if(node instanceof SchemaNode) {
            SchemaNode schemaNode = (SchemaNode)node;
            schemaNode.attributes().put(propertyName, value);
        }
        else {
            InvokerHelper.setProperty(node, propertyName, value);
        }
    }

    /**
     * Overrides the default implementation in order set the parent using the current schema definition.
     *
     * @param parent
     * @param child
     */
    protected void setParent(Object parent, Object child) {
        SchemaNode currentSchema = getCurrentSchema();
        SchemaNode parentSchema = (SchemaNode)currentSchema.parent();
        if(parent != parentSchema && parentSchema instanceof CollectionSchemaNode) {
            // This section is needed in case a collection member was used outside of a collection
            Factory parentFactory = (Factory)parentSchema;
            parentFactory.setParent(this, parent, child);
            parentFactory.setChild(this, parent, child);
        }
        else {
            FactoryBuilderSupport proxyBuilder = getProxyBuilder();
            Factory currentFactory = proxyBuilder.getCurrentFactory();
            currentFactory.setParent(this, parent, child);
            Factory parentFactory = proxyBuilder.getParentFactory();
            if(child instanceof CollectionSchemaNode == false && parentFactory != null) {
                parentFactory.setChild(this, parent, child);
            }
        }
    }
}