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
 * {@link SchemaNode} sub-class that handles certain collection behaviors.
 *
 * @author didge
 * @version $Id: CollectionSchemaNode.java 62 2009-07-28 07:32:10Z didge $
 */
public class CollectionSchemaNode extends SchemaNode implements Factory {
    /**
     * Holder for the actual parent object.
     */
    private Object parentBean;

    public CollectionSchemaNode(SchemaNode parent, Object name) {
        super(parent, name);
    }

    public CollectionSchemaNode(SchemaNode parent, Object name, Object value) {
        super(parent, name, value);
    }

    public CollectionSchemaNode(SchemaNode parent, Object name, Map attributes) {
        super(parent, name, attributes);
    }

    public CollectionSchemaNode(SchemaNode parent, Object name, Map attributes, Object value) {
        super(parent, name, attributes, value);
    }

    public boolean isLeaf() {
        return false;
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if(value != null) {
            throw new CollectionException("Collection '" + name + "': collections may not be specified with a value.  Use a property instead.");
        }
        return this;
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        return false;
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
    }

    public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        parentBean = parent;
        if(parentBean instanceof SchemaNode) {
            String name = (String)attribute("collection");
            if(name == null) {
                name = (String)name();
            }
            new SchemaNode((SchemaNode)parentBean, name);
        }
    }

    /**
     * Returns the build-time parent of the collection.
     *
     * @return see above
     */
    public Object getParentBean() {
        return parentBean;
    }

    /**
     * Returns the key that may be used to add a child to the collection.
     *
     * @param keyAttr may be a property name or closure accepting the child as the only argument
     * @param child   the child
     * @return see above
     */
    protected Object key(Object keyAttr, Object child) {
        Object key = null;
        if(keyAttr instanceof Closure) {
            Closure keyClosure = (Closure)keyAttr;
            key = keyClosure.call(child);
        }
        else if(keyAttr instanceof String) {
            key = InvokerHelper.getProperty(child, (String)keyAttr);
        }
        else {
            throw MetaBuilder.createCollectionException(fqn(), "schema's key value is not a supported type");
        }
        return key;
    }

    /**
     * Sets the <code>child</code> on the <code>parent</code>.
     * <p/>
     * By default, reflection is used to find collection on the <code>parent</code> using the
     * name of the schema.  For example, if the schema's name is <code>foos</code> then this method will attempt to access
     * the collection using the method, <code>foos()</code>.  The access method of the collection may be overridden with the
     * attribute, <code>collection</code>.  If <code>collection</code> is a {@link String}, then the collection is accessed
     * as a property of the <code>parent</code> using the <code>collection</code> value as the property's name.  However, if the
     * <code>collection</code> attribute value is a {@link Closure} where the first argument is the <code>parent</code>,
     * then the closure will be used to access the collection.
     * <p/>
     * When the collection object is not accessible or updateable, the <code>add</code> attribute must be used to specify the name of an
     * alternate method accepting a single argument (the <code>child</code>) or a {@link Closure} accepting two arguments (the <code>parent</code> and the <code>child</code>).
     * <p/>
     * For example, if <code>foos()</code> is not available or does not return an updateable collection, then 'add' maybe set to either
     * <code>addFoo</code> or <code>{&nbsp;p,&nbsp;c&nbsp;->&nbsp;p.addFoo(c)&nbsp;}</code>.
     * <p/>
     * When using the <code>attribute</code> the <code>collection</code> attribute is ignored since it is superflous.
     * <p/>
     * In either case, if the collection is a {@link Map}, then the <code>key</code> attribute must be specified in order
     * to retrieve the <code>child</code>'s key.  The <code>key</code> may either specify a property name or a {@link Closure} accepting one argument (the <code>child</code>).
     * <p/>
     * The value returned by calling <code>key</code> on the <code>child</code>, if it exists, is to put the
     * <code>child</code> into the <code>parent</code>'s collection.
     * <p/>
     * The following shows the different ways in which to use the attributes described above:
     * <pre>
     * parent {
     *  collections {
     *      listOfChildren { // simple example of a collection of child objects
     *          child()
     *      }
     *      listOfChildren2(collection: 'listOfChildren') { // uses the collection above
     *          child()
     *      }
     *      listOfChildren3(collection: { p -> p.getListOfChildren() } ) {
     *          child()
     *      }
     *      listOfChildren4(add: 'addChild' ) {
     *          child()
     *      }
     *      listOfChildren5(add: { p, c -> p.addChild(c) } ) {
     *          child()
     *      }
     *      mapOfChildren(key: 'name') { // simple example of a Map of child objects, using getName() as the key
     *          child(name: 'Jer')
     *      }
     *      mapOfChildren2(collection: 'mapOfChildren', key: 'name') {
     *          child(name: 'Joe')
     *      }
     *      mapOfChildren3(collection: { p -> getMapOfChildren() }, key: 'name') {
     *          child(name: 'Jen')
     *      }
     *      mapOfChildren4(add: 'addChild', key: 'name') {  // note, addChild called like this: p.addChild(key, child)
     *          child(name: 'Jay')
     *      }
     *      mapOfChildren5(add: { p, k, c -> p.addChild(k, c) }, key: 'name') {
     *          child(name: 'Jan')
     *      }
     *      mapOfChildren6(add: { p, c -> p.addChild(c.getName(), c) }) {
     *          child(name: 'Jon')
     *      }
     * }
     * </pre>
     *
     * @param builder
     * @param parent
     * @param child
     */
    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        Object addAttr = attribute("add");
        Object keyAttr = attribute("key");

        try {
            // If there is an add attribute, use it
            if(addAttr != null) {
                if(addAttr instanceof Closure) {
                    Closure addClosure = (Closure)addAttr;
                    if(keyAttr != null) {
                        Object key = key(keyAttr, child);
                        addClosure.call(new Object[]{parentBean, key, child});
                    }
                    else {
                        addClosure.call(new Object[]{parentBean, child});
                    }
                }
                else if(addAttr instanceof String) {
                    if(keyAttr != null) {
                        Object key = key(keyAttr, child);
                        InvokerHelper.invokeMethod(parentBean, (String)addAttr, new Object[]{key, child});
                    }
                    else {
                        InvokerHelper.invokeMethod(parentBean, (String)addAttr, child);
                    }
                }
                else {
                    throw MetaBuilder.createCollectionException(fqn(), "schema's add value is not a String or Closure");
                }
            }
            else {
                Object collectionAttr = attribute("collection");
                Object property = null;

                if(collectionAttr == null) {
                    collectionAttr = name();
                }

                if(collectionAttr instanceof Closure) {
                    Closure collectionClosure = (Closure)collectionAttr;
                    property = collectionClosure.call(parentBean);
                }
                else if(collectionAttr instanceof String) {
                    // Special handling when both parent and child are SchemaNodes.
                    // Can't use an 'add' Closure since that would affect non-SchemaNode types, by default.
                    // Place here so that any 'add' or 'collection' Closures specified have the first chance
                    // to override this behavior.
                    if(parentBean instanceof SchemaNode && child instanceof SchemaNode) {
                        SchemaNode parentNode = (SchemaNode)parentBean;
                        NodeList nodeList = (NodeList)parentNode.get((String)collectionAttr);
                        SchemaNode collectionNode = (SchemaNode)nodeList.get(0);
                        collectionNode.append((Node)child);
                        return;
                    }
                    property = InvokerHelper.getProperty(parentBean, (String)collectionAttr);
                }
                else {
                    throw MetaBuilder.createCollectionException(fqn(), "schema's collection value is not a String or Closure");
                }

                if(property != null) {
                    if(Collection.class.isAssignableFrom(property.getClass())) {
                        ((Collection)property).add(child);
                    }
                    else if(Map.class.isAssignableFrom(property.getClass())) {
                        Object key = key(keyAttr, child);
                        ((Map)property).put(key, child);
                    }
                }
                else {
                    InvokerHelper.setProperty(parentBean, (String)collectionAttr, child);
                }
            }
        }
        catch(Exception e) {
            throw MetaBuilder.createCollectionException(fqn(), e);
        }
    }

    protected Object size(Object sizeAttr, Object parent) {
        Object size = null;
        if(sizeAttr instanceof Closure) {
            Closure sizeClosure = (Closure)sizeAttr;
            size = sizeClosure.call(parent);
        }
        else if(sizeAttr instanceof String) {
            size = InvokerHelper.getProperty(parent, (String)sizeAttr);
        }
        else {
            throw MetaBuilder.createCollectionException(fqn(), "schema's size value is not a supported type");
        }
        return size;
    }


    public void checkDef(FactoryBuilderSupport builder, Object collectionParent) {
        Object defAttr = attribute("def");
        if (defAttr == null) return;

        Integer size = calculateCollectionSize(collectionParent);

        if(size != null && size == 0 ) {
            Object value = null;
            // Value can be either a Closure or an Object/Literal
            if (defAttr instanceof Closure) {
                value = ((Closure)defAttr).call(collectionParent);
            } else {
                value = defAttr;
            }
            if (value != null) {
                // parentBean is null - does setting the parent has any side effects?
                setParent(builder, collectionParent, value);
                if (value instanceof Collection) {
                    // Value is a collection. Add each element to the parent collection
                    // Slow for large collections!
                    for (Iterator iter = ((Collection)value).iterator(); iter.hasNext();) {
                        Object element = iter.next();
                        setChild(builder, collectionParent, element);
                    }
                } else {
                    // Value is not a collection. Add single element to parent collection
                    setChild(builder, collectionParent, value);
                }
            } else {
                throw MetaBuilder.createCollectionException(fqn(), "null is not valid default for a collection");
            }
        }

    }

    public void checkSize(Object collectionParent) {
        Integer min = (Integer)attribute("min");
        Integer max = (Integer)attribute("max");

        if(min == null && max == null) return;

        Integer size = calculateCollectionSize(collectionParent);

        if(min != null) {
            if((min > 0 && (size == null || min.compareTo(size) > 0))) {
                throw MetaBuilder.createCollectionException(fqn(), "min check failed");
            }
        }

        if(max != null) {
            if(max.compareTo(size) < 0) {
                throw MetaBuilder.createCollectionException(fqn(), "max check failed");
            }
        }
    }

    private Integer calculateCollectionSize(Object collectionParent) {
        Object sizeAttr = attribute("size");
        Integer size = null;

        try {
            // If there is an size attribute, use it
            if(sizeAttr != null) {
                size = (Integer)size(sizeAttr, collectionParent);
            }
            else {
                Object collectionAttr = attribute("collection");
                Object property = null;

                if(collectionAttr == null) {
                    collectionAttr = name();
                }

                if(collectionAttr instanceof Closure) {
                    Closure collectionClosure = (Closure)collectionAttr;
                    property = collectionClosure.call(collectionParent);
                }
                else if(collectionAttr instanceof String) {
                    // Special handling when both parent and child are SchemaNodes.
                    // Can't use an 'add' Closure since that would affect non-SchemaNode types, by default.
                    // Place here so that any 'add' or 'collection' Closures specified have the first chance
                    // to override this behavior.
                    if(collectionParent instanceof SchemaNode) {
                        SchemaNode parentNode = (SchemaNode)collectionParent;
                        NodeList nodeList = (NodeList)parentNode.get((String)collectionAttr);
                        SchemaNode collectionNode = (SchemaNode)nodeList.get(0);
                        property = collectionNode.children();
                    }
                    else {
                        property = InvokerHelper.getProperty(collectionParent, (String)collectionAttr);
                    }
                }

                if(property != null) {
                    if(Collection.class.isAssignableFrom(property.getClass())) {
                        size = ((Collection)property).size();
                    }
                    else if(Map.class.isAssignableFrom(property.getClass())) {
                        size = ((Map)property).size();
                    }
                }
            }
        }
        catch(Exception e) {
            throw MetaBuilder.createCollectionException(fqn(), e);
        }
        return size;
    }

    public boolean isHandlesNodeChildren() {
        return false;
    }

    public void onFactoryRegistration(FactoryBuilderSupport builder, String registerdName, String registeredGroupName) {
    }

    public boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
        return false;
    }

    public SchemaNode deepCopy() {
        SchemaNode copy = new CollectionSchemaNode(null, name(), new HashMap(attributes()));
        deepCopyChildren(copy);
        return copy;
    }
}